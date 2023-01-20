package com.github.stevenkin.jim.gateway.config;

import com.github.stevenkin.jim.forward.ForwardServer;
import com.github.stevenkin.jim.gateway.server.WebsocketServer;
import com.github.stevenkin.jim.gateway.service.EncryptKeyService;
import com.github.stevenkin.serialize.FrameSerialization;
import com.github.stevenkin.serialize.Serialization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Autowired
    private GatewayProperties properties;
    @Autowired
    private EncryptKeyService encryptKeyService;

    @Bean
    public WebsocketServer websocketServer() {
        return new WebsocketServer(properties, encryptKeyService);
    }
}
