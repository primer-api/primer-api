package com.primer.tokeniser.web.rest;

import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.repository.CreditCardRepository;
import com.primer.tokeniser.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.primer.tokeniser.domain.CreditCard}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class CreditCardResource {

    private final Logger log = LoggerFactory.getLogger(CreditCardResource.class);

    private static final String ENTITY_NAME = "tokeniserCreditCard";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CreditCardRepository creditCardRepository;

    public CreditCardResource(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    /**
     * {@code POST  /credit-cards} : Create a new creditCard.
     *
     * @param creditCard the creditCard to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new creditCard, or with status {@code 400 (Bad Request)} if the creditCard has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/credit-cards")
    public ResponseEntity<CreditCard> createCreditCard(@RequestBody CreditCard creditCard) throws URISyntaxException {
        log.debug("REST request to save CreditCard : {}", creditCard);
        if (creditCard.getId() != null) {
            throw new BadRequestAlertException("A new creditCard cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CreditCard result = creditCardRepository.save(creditCard);
        return ResponseEntity.created(new URI("/api/credit-cards/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /credit-cards} : Updates an existing creditCard.
     *
     * @param creditCard the creditCard to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated creditCard,
     * or with status {@code 400 (Bad Request)} if the creditCard is not valid,
     * or with status {@code 500 (Internal Server Error)} if the creditCard couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/credit-cards")
    public ResponseEntity<CreditCard> updateCreditCard(@RequestBody CreditCard creditCard) throws URISyntaxException {
        log.debug("REST request to update CreditCard : {}", creditCard);
        if (creditCard.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CreditCard result = creditCardRepository.save(creditCard);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, creditCard.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /credit-cards} : get all the creditCards.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of creditCards in body.
     */
    @GetMapping("/credit-cards")
    public List<CreditCard> getAllCreditCards() {
        log.debug("REST request to get all CreditCards");
        return creditCardRepository.findAll();
    }

    /**
     * {@code GET  /credit-cards/:id} : get the "id" creditCard.
     *
     * @param id the id of the creditCard to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the creditCard, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/credit-cards/{id}")
    public ResponseEntity<CreditCard> getCreditCard(@PathVariable Long id) {
        log.debug("REST request to get CreditCard : {}", id);
        Optional<CreditCard> creditCard = creditCardRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(creditCard);
    }

    /**
     * {@code DELETE  /credit-cards/:id} : delete the "id" creditCard.
     *
     * @param id the id of the creditCard to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/credit-cards/{id}")
    public ResponseEntity<Void> deleteCreditCard(@PathVariable Long id) {
        log.debug("REST request to delete CreditCard : {}", id);
        creditCardRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
