package com.primer.tokeniser.service;

import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.repository.CreditCardRepository;
import com.primer.tokeniser.repository.TokenRepository;
import com.primer.tokeniser.web.rest.SaleDTO;
import com.primer.tokeniser.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.valueOf;

@Service
public class ApiService {

    private final CreditCardRepository creditCardRepository;
    private final Logger log = LoggerFactory.getLogger(ApiService.class);
    private final TokenRepository tokenRepository;

    public ApiService(
        TokenRepository tokenRepository,
        final CreditCardRepository creditCardRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.creditCardRepository = creditCardRepository;
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

    public Token sale(final SaleDTO sale) {
        return null;
    }

    private void validateCreditCard(String input) {
        String purportedCC = input.replaceAll(" ", "");
        int sum = 0;

        for (int i = 0; i < purportedCC.length(); i++) {
            int cardNum = Integer.parseInt(
                Character.toString(purportedCC.charAt(i)));

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
                "Credit Card expiration date not valid",
                "CreditCard",
                "expddatenotvalid"
            );
        }
    }
}
