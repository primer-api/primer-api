package com.primer.tokeniser.dto;

import java.math.BigDecimal;

public class SaleDTO {

    private String token;
    private BigDecimal amount;

    public SaleDTO() {
    }

    public SaleDTO(final String token, final BigDecimal amount) {
        this.token = token;
        this.amount = amount;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }
}
