package com.primer.tokeniser.service;

import com.primer.tokeniser.domain.CreditCard;
import com.primer.tokeniser.domain.Token;
import com.primer.tokeniser.repository.TokenRepository;
import com.primer.tokeniser.web.rest.SaleDTO;
import com.primer.tokeniser.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

    private final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final TokenRepository tokenRepository;

    public ApiService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token tokenise(final CreditCard creditCard) {
        validateCreditCard(creditCard.getNumber());
        validateCardExpiryDate(creditCard.getExpirationDate());

        return null;
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
