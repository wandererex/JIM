package com.github.stevenkin.jim.business.server.config;

import com.github.stevenkin.jim.business.server.BusinessDistributor;
import com.github.stevenkin.serialize.Serialization;
import com.github.stevenkin.serialize.PackageSerialization;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class BusinessServerConfig implements EnvironmentAware, ApplicationContextAware {
    private Environment environment;

    private ApplicationContext applicationContext;

    @Bean
    public Serialization serialization() {
        return new PackageSerialization();
    }

    @Bean
    public BusinessDistributor businessDistributor(Serialization serialization) throws ClassNotFoundException {
        String mqConsumerImpl = environment.getProperty("mqConsumer", "com.github.stevenkin.jim.mq.redis.RedisMqMqConsumer");
        return new BusinessDistributor(applicationContext);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
