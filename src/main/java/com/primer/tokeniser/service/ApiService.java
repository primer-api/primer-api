package com.primer.tokeniser.service;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.dto.SaleDTO;
import com.primer.tokeniser.repository.CreditCardRepository;
import com.primer.tokeniser.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.valueOf;

@Service
public class ApiService {

    private final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final CreditCardRepository creditCardRepository;
    private final TokenRepository tokenRepository;
    private final BraintreeGateway braintreeGateway;

    public ApiService(
        TokenRepository tokenRepository,
        final CreditCardRepository creditCardRepository,
        final BraintreeGateway braintreeGateway
    ) {
        this.tokenRepository = tokenRepository;
        this.creditCardRepository = creditCardRepository;
        this.braintreeGateway = braintreeGateway;
    }

    /**
     * Tokenise a credit card
     * @param inputCreditCard number and expiration date
     * @return token
     */
    public String tokenise(final CreditCard inputCreditCard) {
        final CreditCard creditCard = creditCardRepository.save(inputCreditCard);
        // TODO generate a more secure token
        String origin = "1" + creditCard.getNumber().substring(1).replaceAll(".", "0");
        String bound = creditCard.getNumber().replaceAll(".", "9");
        final String tokenised = valueOf(ThreadLocalRandom.current().nextLong(Long.parseLong(origin), Long.parseLong(bound)));
        Token token = new Token(tokenised, creditCard);
        // TODO handle unique constraint exception
        return tokenRepository.save(token).getToken();
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
