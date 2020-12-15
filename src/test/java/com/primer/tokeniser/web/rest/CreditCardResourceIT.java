package com.primer.tokeniser.web.rest;

import com.primer.tokeniser.TokeniserApp;
import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.repository.CreditCardRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CreditCardResource} REST controller.
 */
@SpringBootTest(classes = TokeniserApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class CreditCardResourceIT {

    private static final String DEFAULT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_EXPIRATION_DATE = "AAAAAAAAAA";
    private static final String UPDATED_EXPIRATION_DATE = "BBBBBBBBBB";

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCreditCardMockMvc;

    private CreditCard creditCard;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CreditCard createEntity(EntityManager em) {
        CreditCard creditCard = new CreditCard()
            .number(DEFAULT_NUMBER)
            .expirationDate(DEFAULT_EXPIRATION_DATE);
        return creditCard;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CreditCard createUpdatedEntity(EntityManager em) {
        CreditCard creditCard = new CreditCard()
            .number(UPDATED_NUMBER)
            .expirationDate(UPDATED_EXPIRATION_DATE);
        return creditCard;
    }

    @BeforeEach
    public void initTest() {
        creditCard = createEntity(em);
    }

    @Test
    @Transactional
    public void createCreditCard() throws Exception {
        int databaseSizeBeforeCreate = creditCardRepository.findAll().size();
        // Create the CreditCard
        restCreditCardMockMvc.perform(post("/api/credit-cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(creditCard)))
            .andExpect(status().isCreated());

        // Validate the CreditCard in the database
        List<CreditCard> creditCardList = creditCardRepository.findAll();
        assertThat(creditCardList).hasSize(databaseSizeBeforeCreate + 1);
        CreditCard testCreditCard = creditCardList.get(creditCardList.size() - 1);
        assertThat(testCreditCard.getNumber()).isEqualTo(DEFAULT_NUMBER);
        assertThat(testCreditCard.getExpirationDate()).isEqualTo(DEFAULT_EXPIRATION_DATE);
    }

    @Test
    @Transactional
    public void createCreditCardWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = creditCardRepository.findAll().size();

        // Create the CreditCard with an existing ID
        creditCard.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCreditCardMockMvc.perform(post("/api/credit-cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(creditCard)))
            .andExpect(status().isBadRequest());

        // Validate the CreditCard in the database
        List<CreditCard> creditCardList = creditCardRepository.findAll();
        assertThat(creditCardList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCreditCards() throws Exception {
        // Initialize the database
        creditCardRepository.saveAndFlush(creditCard);

        // Get all the creditCardList
        restCreditCardMockMvc.perform(get("/api/credit-cards?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(creditCard.getId().intValue())))
            .andExpect(jsonPath("$.[*].number").value(hasItem(DEFAULT_NUMBER)))
            .andExpect(jsonPath("$.[*].expirationDate").value(hasItem(DEFAULT_EXPIRATION_DATE)));
    }
    
    @Test
    @Transactional
    public void getCreditCard() throws Exception {
        // Initialize the database
        creditCardRepository.saveAndFlush(creditCard);

        // Get the creditCard
        restCreditCardMockMvc.perform(get("/api/credit-cards/{id}", creditCard.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(creditCard.getId().intValue()))
            .andExpect(jsonPath("$.number").value(DEFAULT_NUMBER))
            .andExpect(jsonPath("$.expirationDate").value(DEFAULT_EXPIRATION_DATE));
    }
    @Test
    @Transactional
    public void getNonExistingCreditCard() throws Exception {
        // Get the creditCard
        restCreditCardMockMvc.perform(get("/api/credit-cards/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCreditCard() throws Exception {
        // Initialize the database
        creditCardRepository.saveAndFlush(creditCard);

        int databaseSizeBeforeUpdate = creditCardRepository.findAll().size();

        // Update the creditCard
        CreditCard updatedCreditCard = creditCardRepository.findById(creditCard.getId()).get();
        // Disconnect from session so that the updates on updatedCreditCard are not directly saved in db
        em.detach(updatedCreditCard);
        updatedCreditCard
            .number(UPDATED_NUMBER)
            .expirationDate(UPDATED_EXPIRATION_DATE);

        restCreditCardMockMvc.perform(put("/api/credit-cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedCreditCard)))
            .andExpect(status().isOk());

        // Validate the CreditCard in the database
        List<CreditCard> creditCardList = creditCardRepository.findAll();
        assertThat(creditCardList).hasSize(databaseSizeBeforeUpdate);
        CreditCard testCreditCard = creditCardList.get(creditCardList.size() - 1);
        assertThat(testCreditCard.getNumber()).isEqualTo(UPDATED_NUMBER);
        assertThat(testCreditCard.getExpirationDate()).isEqualTo(UPDATED_EXPIRATION_DATE);
    }

    @Test
    @Transactional
    public void updateNonExistingCreditCard() throws Exception {
        int databaseSizeBeforeUpdate = creditCardRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCreditCardMockMvc.perform(put("/api/credit-cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(creditCard)))
            .andExpect(status().isBadRequest());

        // Validate the CreditCard in the database
        List<CreditCard> creditCardList = creditCardRepository.findAll();
        assertThat(creditCardList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCreditCard() throws Exception {
        // Initialize the database
        creditCardRepository.saveAndFlush(creditCard);

        int databaseSizeBeforeDelete = creditCardRepository.findAll().size();

        // Delete the creditCard
        restCreditCardMockMvc.perform(delete("/api/credit-cards/{id}", creditCard.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CreditCard> creditCardList = creditCardRepository.findAll();
        assertThat(creditCardList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
