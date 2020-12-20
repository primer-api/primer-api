package com.primer.tokeniser.web.rest.errors;

import com.braintreegateway.exceptions.BraintreeException;

public class SaleFailedException extends BraintreeException {

    public SaleFailedException(final String message) {
        super(message);
    }
}
