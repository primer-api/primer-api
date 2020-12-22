package com.primer.tokeniser.service;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.dto.SaleDTO;
import com.primer.tokeniser.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.security.SecureRandom;

@Service
public class ApiService {

    private final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final TokenRepository tokenRepository;
    private final BraintreeGateway braintreeGateway;

    public ApiService(
        TokenRepository tokenRepository,
        final BraintreeGateway braintreeGateway
    ) {
        this.tokenRepository = tokenRepository;
        this.braintreeGateway = braintreeGateway;
    }

    /**
     * Tokenise a credit card
     * @param inputCreditCard number and expiration date
     * @return token
     */
    public String tokenise(final CreditCard inputCreditCard) {
        final String tokenised = generateToken(inputCreditCard.getNumber());
        Token token = new Token(tokenised, inputCreditCard);
        final Token newToken = tokenRepository.save(token);
        return newToken.getToken();
    }

    /**
     * Generate a secure random token with the same length as the PAN (credit card number)
     *
     * @param pan credit card number
     * @return secure random token
     */
    protected String generateToken(final String pan) {
        SecureRandom randomGenerator = new SecureRandom();
        String token = String.valueOf(randomGenerator.nextLong());
        if (token.length() < pan.length()) {
            return generateToken(pan);
        }
        return token.substring(token.length() - pan.length());
    }

    /**
     * Process a sale on the payment gateway
     * @param sale token and amount
     * @return Approved or error message
     */
    public String sale(final SaleDTO sale) {
        Assert.notNull(sale, "Sale payload is missing");
        Assert.hasText(sale.getToken(), "Token is missing.");
        Assert.notNull(sale.getAmount(), "Amount is missing.");

        final Token token = tokenRepository.findByToken(sale.getToken());

        TransactionRequest request = new TransactionRequest()
            .amount(sale.getAmount())
            .creditCard()
                .expirationDate(token.getCreditCard().getExpirationDate())
                .number(token.getCreditCard().getNumber())
                .done();

        Result<Transaction> result = braintreeGateway.transaction().sale(request);
        if (result.isSuccess()) {
            // See result.getTarget() for details
            log.info(result.getTarget().getProcessorResponseText());
            return result.getTarget().getProcessorResponseText();
        }

        // Handle errors
        result.getErrors()
            .getAllDeepValidationErrors()
            .forEach(validationError -> log.error(validationError.getMessage()));

        return result.getMessage();
    }

}
