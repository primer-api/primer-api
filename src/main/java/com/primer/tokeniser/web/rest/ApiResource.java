package com.primer.tokeniser.web.rest;

import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.dto.SaleDTO;
import com.primer.tokeniser.service.ApiService;
import com.primer.tokeniser.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * API controller
 */
@Controller
@RequestMapping("/api")
@Transactional
public class ApiResource {

    private final Logger log = LoggerFactory.getLogger(ApiResource.class);

    private static final String ENTITY_NAME = "tokeniserToken";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ApiService apiService;

    public ApiResource(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * {@code POST  /tokenise} : Create a new token.
     *
     * @param creditCard the creditCard to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new token, or with status {@code 400 (Bad Request)} if the token has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/tokenise")
    public ResponseEntity<Token> tokenise(@RequestBody CreditCard creditCard) throws URISyntaxException {
        log.debug("REST request to create Token for creditCard: {}", creditCard);
        validateCreditCard(creditCard.getNumber());
        validateCardExpiryDate(creditCard.getExpirationDate());
        Token result = apiService.tokenise(creditCard);
        return ResponseEntity.created(new URI("/api/tokenise/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code POST  /sale} : Process a new sale.
     *
     * @param sale token and amount
     * @return the final message with status {@code 200 (Success)}, or with status {@code 400 (Bad Request)} if the token is not valid.
     */
    @PostMapping("/sale")
    public ResponseEntity<String> sale(@RequestBody SaleDTO sale) {
        log.debug("REST request to create a sale: {}", sale);
        String result = apiService.sale(sale);
        return ResponseEntity.ok(result);
    }

    private void validateCreditCard(String input) {
        String purportedCC = input.replaceAll(" ", "");
        int sum = 0;

        for (int i = 0; i < purportedCC.length(); i++) {
            int cardNum = Integer.parseInt(Character.toString(purportedCC.charAt(i)));

            if ((purportedCC.length() - i) % 2 == 0) {
                cardNum = cardNum * 2;

                if (cardNum > 9) {
                    cardNum = cardNum - 9;
                }
            }

            sum += cardNum;
        }
        if (sum % 10 != 0) {
            throw new BadRequestAlertException("Credit Card number not valid", "CreditCard", "numbernotvalid");
        }
    }

    private void validateCardExpiryDate(String expiryDate) {
        if (!expiryDate.matches("(?:0[1-9]|1[0-2])/[0-9]{2}")) {
            throw new BadRequestAlertException(
                "Credit Card expiration date not valid. Format MM/YY",
                "CreditCard",
                "expddatenotvalid"
            );
        }
    }
}
