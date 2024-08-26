package com.finalproject.stayease.config;

import com.midtrans.Config;
import com.midtrans.ConfigFactory;
import com.midtrans.service.MidtransCoreApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidtransConfig {
    @Value("${midtrans.server.key}")
    private String serverKey;

    @Value("${midtrans.client.key}")
    private String clientKey;

    @Value("${midtrans.is.production}")
    private boolean isProduction;

    @Bean
    public MidtransCoreApi midtransCoreApi() {
        Config config = Config.builder()
                .setIsProduction(isProduction)
                .setServerKey(serverKey)
                .setClientKey(clientKey)
                .build();

        return new ConfigFactory(config).getCoreApi();
    }
}
