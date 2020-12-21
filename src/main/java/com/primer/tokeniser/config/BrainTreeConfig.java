package com.primer.tokeniser.config;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.braintree")
public class BrainTreeConfig {

    private String merchantId;
    private String privateKey;
    private String publicKey;

    @Bean
    public BraintreeGateway braintreeGateway() {
        return new BraintreeGateway(
            Environment.SANDBOX,
            merchantId,
            publicKey,
            privateKey
        );
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(final String merchantId) {
        this.merchantId = merchantId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(final String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(final String publicKey) {
        this.publicKey = publicKey;
    }
}
