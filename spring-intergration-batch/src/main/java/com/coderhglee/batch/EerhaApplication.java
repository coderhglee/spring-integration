package com.coderhglee.batch;

import org.aopalliance.aop.Advice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

//@SpringBootApplication
public class EerhaApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(EerhaApplication.class, args);
        MessageChannel in = context.getBean("advised.input", MessageChannel.class);
        in.send(new GenericMessage<>("good"));
        in.send(new GenericMessage<>("bad"));
        context.close();
    }

    @Bean
    public IntegrationFlow advised() {
        return f -> f.handle((GenericHandler<String>) (payload, headers) -> {
            if (payload.equals("good")) {
                return null;
            }
            else {
                throw new RuntimeException("some failure");
            }
        }, c -> c.advice(expressionAdvice()));
    }

    @Bean
    public Advice expressionAdvice() {
        ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
        advice.setSuccessChannelName("success.input");
        advice.setOnSuccessExpressionString("payload + ' was successful'");
        advice.setFailureChannelName("failure.input");
        advice.setOnFailureExpressionString(
                "payload + ' was bad, with reason: ' + #exception.cause.message");
        advice.setTrapException(true);
        return advice;
    }

    @Bean
    public IntegrationFlow success() {
        return f -> f.handle(System.out::println);
    }

    @Bean
    public IntegrationFlow failure() {
        return f -> f.handle(System.out::println);
    }

}