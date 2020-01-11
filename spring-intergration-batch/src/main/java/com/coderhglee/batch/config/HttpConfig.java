package com.coderhglee.batch.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

@Configuration
public class HttpConfig {

    @Bean
    public RestTemplateBuilder restTemplate() {
        return new RestTemplateBuilder(new CustomRestTemplateCustomizer());
    }

    @Bean
    public SimpleRetryPolicy retryPolicy(){
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        return retryPolicy;
    }

    @Bean
    public FixedBackOffPolicy fixedBackOffPolicy(){
        FixedBackOffPolicy p = new FixedBackOffPolicy();
        p.setBackOffPeriod(1000);
        return p;
    }
}
