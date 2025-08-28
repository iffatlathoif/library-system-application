package com.enigma.library_app.config;

import com.midtrans.service.MidtransSnapApi;
import com.midtrans.service.impl.MidtransSnapApiImpl;
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
    public MidtransSnapApi midtransSnapApi() {
        return new MidtransSnapApiImpl(
                com.midtrans.Config.builder()
                        .setServerKey(serverKey)
                        .setClientKey(clientKey)
                        .setIsProduction(isProduction)
                        .build()
        );
    }
}
