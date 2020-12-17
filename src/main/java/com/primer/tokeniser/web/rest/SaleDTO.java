package com.primer.tokeniser.web.rest;

import com.primer.tokeniser.domain.Token;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class SaleDTO {

    private Token token;
    private BigDecimal amount;

}
