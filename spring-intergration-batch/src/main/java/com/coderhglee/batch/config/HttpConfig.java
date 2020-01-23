package com.coderhglee.batch.config;

import org.aopalliance.aop.Advice;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Configuration
//@Order(2)
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

    /**
     *
     * @return
     */
    @Bean
    public HttpRequestExecutingMessageHandler httpRequestExecutingMessageHandler() {
        Map<String, Expression> expressionMap = new HashMap<>();
        ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
        expressionMap.put("foo", EXPRESSION_PARSER.parseExpression("headers.file_name"));
        HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://127.0.0.1:8080/hello/{foo}", restTemplate().build());
        handler.setUriVariableExpressions(expressionMap);
        handler.setExpectReply(false);
        return handler;
    }

    @Bean
    public Advice retryAdvice() {
        RequestHandlerRetryAdvice retryAdvice = new RequestHandlerRetryAdvice();
        RetryTemplate retryTemplate = new RetryTemplate();
//        FixedBackOffPolicy fixedBackOffPolicy = appContext.getBean("fixedBackOffPolicy",FixedBackOffPolicy.class);
//        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy());
        retryAdvice.setRetryTemplate(retryTemplate);
        retryAdvice.setRecoveryCallback(new CustomRecoveryCallback());
        return retryAdvice;
    }
}
