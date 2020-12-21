package com.primer.tokeniser.service;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.repository.CreditCardRepository;
import com.primer.tokeniser.repository.TokenRepository;
import com.primer.tokeniser.web.rest.SaleDTO;
import com.primer.tokeniser.web.rest.errors.BadRequestAlertException;
import com.primer.tokeniser.web.rest.errors.SaleFailedException;
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

    public Token tokenise(final CreditCard inputCreditCard) {
        validateCreditCard(inputCreditCard.getNumber());
        validateCardExpiryDate(inputCreditCard.getExpirationDate());
        final CreditCard creditCard = creditCardRepository.save(inputCreditCard);
        // TODO generate a more secure token
        String origin = "1" + creditCard.getNumber().substring(1).replaceAll(".", "0");
        String bound = creditCard.getNumber().replaceAll(".", "9");
        final String tokenised = valueOf(ThreadLocalRandom.current().nextLong(Long.parseLong(origin), Long.parseLong(bound)));
        Token token = new Token(tokenised, creditCard);
        return tokenRepository.save(token);
    }

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

        throw new SaleFailedException(result.getMessage());
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
