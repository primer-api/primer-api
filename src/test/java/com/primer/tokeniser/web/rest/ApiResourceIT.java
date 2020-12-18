package com.primer.tokeniser.web.rest;

import com.primer.tokeniser.TokeniserApp;
import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.repository.TokenRepository;

import lombok.val;
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
 * Integration tests for the {@link ApiResource} REST controller.
 */
@SpringBootTest(classes = TokeniserApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class ApiResourceIT {

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTokenMockMvc;

    private Token token;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Token createEntity(EntityManager em) {
        Token token = new Token()
            .token(DEFAULT_TOKEN);
        return token;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Token createUpdatedEntity(EntityManager em) {
        Token token = new Token()
            .token(UPDATED_TOKEN);
        return token;
    }

    @BeforeEach
    public void initTest() {
        token = createEntity(em);
    }

    @Test
    @Transactional
    public void createToken() throws Exception {
        int databaseSizeBeforeCreate = tokenRepository.findAll().size();
        final String number = "346979435224470";
        val creditCard = new CreditCard(number, "12/20");
        // Create the Token
        restTokenMockMvc.perform(post("/api/tokenise")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(creditCard)))
            .andExpect(status().isCreated());

        // Validate the Token in the database
        List<Token> tokenList = tokenRepository.findAll();
        assertThat(tokenList).hasSize(databaseSizeBeforeCreate + 1);
        Token testToken = tokenList.get(tokenList.size() - 1);
        assertThat(testToken.getToken()).isNotEqualTo((number));
    }

    @Test
    @Transactional
    public void createTokenWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = tokenRepository.findAll().size();

        // Create the Token with an existing ID
        token.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTokenMockMvc.perform(post("/api/tokens")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(token)))
            .andExpect(status().isBadRequest());

        // Validate the Token in the database
        List<Token> tokenList = tokenRepository.findAll();
        assertThat(tokenList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllTokens() throws Exception {
        // Initialize the database
        tokenRepository.saveAndFlush(token);

        // Get all the tokenList
        restTokenMockMvc.perform(get("/api/tokens?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(token.getId().intValue())))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)));
    }

    @Test
    @Transactional
    public void getToken() throws Exception {
        // Initialize the database
        tokenRepository.saveAndFlush(token);

        // Get the token
        restTokenMockMvc.perform(get("/api/tokens/{id}", token.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(token.getId().intValue()))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN));
    }
    @Test
    @Transactional
    public void getNonExistingToken() throws Exception {
        // Get the token
        restTokenMockMvc.perform(get("/api/tokens/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateToken() throws Exception {
        // Initialize the database
        tokenRepository.saveAndFlush(token);

        int databaseSizeBeforeUpdate = tokenRepository.findAll().size();

        // Update the token
        Token updatedToken = tokenRepository.findById(token.getId()).get();
        // Disconnect from session so that the updates on updatedToken are not directly saved in db
        em.detach(updatedToken);
        updatedToken
            .token(UPDATED_TOKEN);

        restTokenMockMvc.perform(put("/api/tokens")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedToken)))
            .andExpect(status().isOk());

        // Validate the Token in the database
        List<Token> tokenList = tokenRepository.findAll();
        assertThat(tokenList).hasSize(databaseSizeBeforeUpdate);
        Token testToken = tokenList.get(tokenList.size() - 1);
        assertThat(testToken.getToken()).isEqualTo(UPDATED_TOKEN);
    }

    @Test
    @Transactional
    public void updateNonExistingToken() throws Exception {
        int databaseSizeBeforeUpdate = tokenRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTokenMockMvc.perform(put("/api/tokens")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(token)))
            .andExpect(status().isBadRequest());

        // Validate the Token in the database
        List<Token> tokenList = tokenRepository.findAll();
        assertThat(tokenList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteToken() throws Exception {
        // Initialize the database
        tokenRepository.saveAndFlush(token);

        int databaseSizeBeforeDelete = tokenRepository.findAll().size();

        // Delete the token
        restTokenMockMvc.perform(delete("/api/tokens/{id}", token.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Token> tokenList = tokenRepository.findAll();
        assertThat(tokenList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
