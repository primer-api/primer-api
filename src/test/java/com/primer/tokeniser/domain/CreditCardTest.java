package com.primer.tokeniser.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.primer.tokeniser.web.rest.TestUtil;

public class CreditCardTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CreditCard.class);
        CreditCard creditCard1 = new CreditCard();
        creditCard1.setId(1L);
        CreditCard creditCard2 = new CreditCard();
        creditCard2.setId(creditCard1.getId());
        assertThat(creditCard1).isEqualTo(creditCard2);
        creditCard2.setId(2L);
        assertThat(creditCard1).isNotEqualTo(creditCard2);
        creditCard1.setId(null);
        assertThat(creditCard1).isNotEqualTo(creditCard2);
    }
}
