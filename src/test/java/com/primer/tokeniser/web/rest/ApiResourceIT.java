package com.primer.tokeniser.web.rest;

import com.primer.tokeniser.TokeniserApp;
import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.dto.SaleDTO;
import com.primer.tokeniser.repository.CreditCardRepository;
import com.primer.tokeniser.repository.TokenRepository;
import com.primer.tokeniser.service.ApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link ApiResource} REST controller.
 */
@SpringBootTest(classes = TokeniserApp.class)
@AutoConfigureMockMvc
@WithMockUser
class ApiResourceIT {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private ApiService apiService;

    @Autowired
    private MockMvc restTokenMockMvc;

    private static Stream<Arguments> generateValidCreditCard() {
        return Stream.of(
            Arguments.of("378282246310005", "12/20"), // Amex
            Arguments.of("371449635398431", "12/20"), // Amex
            Arguments.of("36259600000004", "03/24"), // Diners Club
            Arguments.of("6011000991300009", "01/14"), // Discover
            Arguments.of("3530111333300000", "11/30"), // JCB
            Arguments.of("6304000000000000", "11/30"), // Maestro
            Arguments.of("2223000048400011", "11/30"), // Mastercard
            Arguments.of("5555555555554444", "11/30"), // Mastercard
            Arguments.of("4500600000000061", "10/22"), // Visa
            Arguments.of("4217651111111119", "10/22"), // Visa
            Arguments.of("4012888888881881", "10/22"), // Visa
            Arguments.of("4012000077777777", "10/22"), // Visa
            Arguments.of("4012000033330026", "10/22"), // Visa
            Arguments.of("4009348888881881", "10/22"), // Visa
            Arguments.of("4005519200000004", "10/22"), // Visa
            Arguments.of("4111111111111111", "10/22") // Visa
        );
    }

    private static Stream<Arguments> generateBraintreeValidData() {
        return Stream.of(
            Arguments.of("6304000000000000", "11/30", BigDecimal.valueOf(55)), // Maestro
            Arguments.of("2223000048400011", "11/30", BigDecimal.valueOf(100_000)), // Mastercard
            Arguments.of("5555555555554444", "11/30", BigDecimal.valueOf(100_000)), // Mastercard
            Arguments.of("4500600000000061", "10/22", BigDecimal.valueOf(10_000)), // Visa
            Arguments.of("4217651111111119", "10/22", BigDecimal.valueOf(1_000_000)), // Visa
            Arguments.of("4012888888881881", "10/22", BigDecimal.valueOf(1_000_000)), // Visa
            Arguments.of("4012000077777777", "10/22", BigDecimal.valueOf(1_000_000)), // Visa
            Arguments.of("4012000033330026", "10/22", BigDecimal.valueOf(1_000_000)), // Visa
            Arguments.of("4009348888881881", "10/22", BigDecimal.valueOf(1_000_000)), // Visa
            Arguments.of("4005519200000004", "10/22", BigDecimal.valueOf(1_000_000)), // Visa
            Arguments.of("4111111111111111", "10/22", BigDecimal.valueOf(5002)) // Visa
        );
    }

    @ParameterizedTest
    @MethodSource("generateValidCreditCard")
    @Transactional
    void tokeniseSuccess(final String number, final String expirationDate) throws Exception {
        int databaseSizeBeforeCreate = tokenRepository.findAll().size();
        int databaseSizeBeforeCreateCC = creditCardRepository.findAll().size();
        CreditCard creditCard = new CreditCard(number, expirationDate);

        // Create the Token
        restTokenMockMvc.perform(post("/api/tokenise")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(creditCard)))
            .andExpect(status().isCreated());

        // Validate the Token in the database
        List<Token> tokenList = tokenRepository.findAll();
        List<CreditCard> creditCardList = creditCardRepository.findAll();
        assertThat(tokenList).hasSize(databaseSizeBeforeCreate + 1);
        assertThat(creditCardList).hasSize(databaseSizeBeforeCreateCC + 1);
        CreditCard testCreditCard = creditCardList.get(creditCardList.size() - 1);
        assertThat(testCreditCard.getNumber()).isEqualTo(number);
        assertThat(testCreditCard.getExpirationDate()).isEqualTo(expirationDate);
        Token testToken = tokenList.get(tokenList.size() - 1);
        assertThat(testToken.getToken()).isNotEqualTo(number);
        assertThat(testToken.getToken().length()).isEqualTo((number.length()));
    }

    @Test
    @Transactional
    void tokeniseWithInvalidNumber() throws Exception {
        int databaseSizeBeforeCreate = tokenRepository.findAll().size();
        int databaseSizeBeforeCreateCC = creditCardRepository.findAll().size();
        final String number = "123123132132";
        CreditCard creditCard = new CreditCard(number, "12/20");
        // Create the Token
        restTokenMockMvc.perform(post("/api/tokenise")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(creditCard)))
            .andExpect(status().is4xxClientError());

        // Validate the Token in the database
        List<Token> tokenList = tokenRepository.findAll();
        List<CreditCard> creditCardList = creditCardRepository.findAll();
        assertThat(tokenList).hasSize(databaseSizeBeforeCreate);
        assertThat(creditCardList).hasSize(databaseSizeBeforeCreateCC);
    }

    @Test
    @Transactional
    void tokeniseWithInvalidExpirationDate() throws Exception {
        int databaseSizeBeforeCreate = tokenRepository.findAll().size();
        int databaseSizeBeforeCreateCC = creditCardRepository.findAll().size();
        final String number = "123123132132";
        CreditCard creditCard = new CreditCard(number, "14/20");
        // Create the Token
        restTokenMockMvc.perform(post("/api/tokenise")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(creditCard)))
            .andExpect(status().is4xxClientError());

        // Validate the Token in the database
        List<Token> tokenList = tokenRepository.findAll();
        List<CreditCard> creditCardList = creditCardRepository.findAll();
        assertThat(tokenList).hasSize(databaseSizeBeforeCreate);
        assertThat(creditCardList).hasSize(databaseSizeBeforeCreateCC);
    }

    @ParameterizedTest
    @MethodSource("generateBraintreeValidData")
    @Transactional
    void saleSuccess(final String number, final String expDate, final BigDecimal amount) throws Exception {
        final String token = apiService.tokenise(new CreditCard(number, expDate));
        SaleDTO sale = new SaleDTO(token, amount);
        // Create the Token
        restTokenMockMvc.perform(post("/api/sale")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(sale)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Approved")));

        // Validate the Token in the database
        List<Token> tokenList = tokenRepository.findAll();
        List<CreditCard> creditCardList = creditCardRepository.findAll();

    }

    // TODO test sale with not approved responses https://developers.braintreepayments.com/reference/general/testing/java

    @Test
    @Transactional
    void saleWithTokenMissing() throws Exception {
        SaleDTO sale = new SaleDTO(null, BigDecimal.TEN);

        // Create the Token
        restTokenMockMvc.perform(post("/api/sale")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(sale)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    void saleWithAmountMissing() throws Exception {
        SaleDTO sale = new SaleDTO("12213213213213", null);

        // Create the Token
        restTokenMockMvc.perform(post("/api/sale")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(sale)))
            .andExpect(status().is4xxClientError());
    }

}
