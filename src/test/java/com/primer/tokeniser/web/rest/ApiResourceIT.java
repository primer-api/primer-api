package com.primer.tokeniser.web.rest;

import com.primer.tokeniser.TokeniserApp;
import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.repository.CreditCardRepository;
import com.primer.tokeniser.repository.TokenRepository;

import lombok.val;
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

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ApiResource} REST controller.
 */
@SpringBootTest(classes = TokeniserApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class ApiResourceIT {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private MockMvc restTokenMockMvc;

    private static Stream<Arguments> generateData() {
        return Stream.of(
            Arguments.of("346979435224470", "12/20"), // Amex
            Arguments.of("30228809510150", "03/24"), // Diners Club
            Arguments.of("6011904544257428", "01/14"), // Discover
            Arguments.of("5335349966750735", "11/30"), // Mastercard
            Arguments.of("4716859822546814", "10/22"), // Visa
            Arguments.of("4485064840670", "11/30"), // Visa 13
            Arguments.of("869926355806445", "11/30"), // Voyager
            Arguments.of("210059417746122", "11/30"), // JCB 15
            Arguments.of("3096342876197794", "11/30"), // JCB
            Arguments.of("214978091287606", "11/30") // enRoute
        );
    }

    @ParameterizedTest
    @MethodSource("generateData")
    @Transactional
    public void tokeniseSuccess(final String number, final String expirationDate) throws Exception {
        int databaseSizeBeforeCreate = tokenRepository.findAll().size();
        int databaseSizeBeforeCreateCC = creditCardRepository.findAll().size();
        val creditCard = new CreditCard(number, expirationDate);

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
    public void tokeniseWithInvalidNumber() throws Exception {
        int databaseSizeBeforeCreate = tokenRepository.findAll().size();
        int databaseSizeBeforeCreateCC = creditCardRepository.findAll().size();
        final String number = "123123132132";
        val creditCard = new CreditCard(number, "12/20");
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
    public void tokeniseWithInvalidExpirationDate() throws Exception {
        int databaseSizeBeforeCreate = tokenRepository.findAll().size();
        int databaseSizeBeforeCreateCC = creditCardRepository.findAll().size();
        final String number = "123123132132";
        val creditCard = new CreditCard(number, "14/20");
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
}
