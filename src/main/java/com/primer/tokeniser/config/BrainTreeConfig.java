package com.primer.tokeniser.config;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.braintree")
@Data
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
}
