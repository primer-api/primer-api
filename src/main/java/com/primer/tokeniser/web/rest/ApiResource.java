package com.primer.tokeniser.web.rest;

import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.service.ApiService;
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
        Token result = apiService.tokenise(creditCard);
        return ResponseEntity.created(new URI("/api/tokenise/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code POST  /sale} : Create a new sale.
     *
     * @param sale the sale to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new token, or with status {@code 400 (Bad Request)} if the token has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/sale")
    public ResponseEntity<String> sale(@RequestBody SaleDTO sale) throws URISyntaxException {
        log.debug("REST request to create a sale: {}", sale);
        String result = apiService.sale(sale);
        return ResponseEntity.ok(result);
    }
}
