package com.primer.tokeniser.domain;

import lombok.Builder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A CreditCard.
 */
@Entity
@Table(name = "credit_card")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CreditCard implements Serializable {

    private static final long serialVersionUID = 1L;

    public CreditCard() {
    }

    public CreditCard(String number, String expirationDate) {
        this.number = number;
        this.expirationDate = expirationDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number")
    private String number;

    @Column(name = "expiration_date")
    private String expirationDate;

    @OneToMany(mappedBy = "creditCard")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Token> tokens = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public CreditCard number(String number) {
        this.number = number;
        return this;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public CreditCard expirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Set<Token> getTokens() {
        return tokens;
    }

    public CreditCard tokens(Set<Token> tokens) {
        this.tokens = tokens;
        return this;
    }

    public CreditCard addToken(Token token) {
        this.tokens.add(token);
        token.setCreditCard(this);
        return this;
    }

    public CreditCard removeToken(Token token) {
        this.tokens.remove(token);
        token.setCreditCard(null);
        return this;
    }

    public void setTokens(Set<Token> tokens) {
        this.tokens = tokens;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreditCard)) {
            return false;
        }
        return id != null && id.equals(((CreditCard) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CreditCard{" +
            "id=" + getId() +
            ", number='" + getNumber() + "'" +
            ", expirationDate='" + getExpirationDate() + "'" +
            "}";
    }
}
