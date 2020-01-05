package com.coderhglee.batch;

import org.aopalliance.aop.Advice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.config.GlobalChannelInterceptorInitializer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.transformer.FileToByteArrayTransformer;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableIntegration
public class HttpConfig {
    public String INPUT_DIR = "C:\\Users\\KLCOM000\\repository\\spring-integration\\batch\\in";
    public String OUTPUT_DIR = "the_dest_dir";
    public String FILE_PATTERN = "*.*";

    @Bean
    public DirectChannel sftpChannel() {
        DirectChannel directChannel = new DirectChannel();
        directChannel.addInterceptor(new SftpChannelInterceptor());
        return directChannel;
    }

    @Bean
    public DirectChannel httpChannel() {
        DirectChannel directChannel = new DirectChannel();
        directChannel.addInterceptor(new HttpChannelInterceptor());
        return directChannel;
    }

//    @Bean
//    @GlobalChannelInterceptor(patterns= {"pollingFlow.channel#1","pollingFlow.channel#2"})
//    public HttpChannelInterceptor messageChannelInterceptor() {
//        return new HttpChannelInterceptor();
//    }

    @Bean
    public GlobalChannelInterceptorInitializer interceptorInitializer(){
        return new GlobalChannelInterceptorInitializer();
    }
    @Bean
//    @InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource sourceReader= new FileReadingMessageSource();
        sourceReader.setDirectory(new File(INPUT_DIR));
        sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
        return sourceReader;
    }

    @Transformer
    public FileToByteArrayTransformer transformer(){
        FileToByteArrayTransformer file = new FileToByteArrayTransformer();
        file.setDeleteFiles(true);
        return file;
    }

    @Bean
    public HttpRequestExecutingMessageHandler httpRequestExecutingMessageHandler() {

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Expression> expressionMap = new HashMap<>();
        ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
        expressionMap.put("foo", EXPRESSION_PARSER.parseExpression("headers.file_name"));
        HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://127.0.0.1:8080/hello/{foo}", restTemplate);
        handler.setUriVariableExpressions(expressionMap);
        handler.setExpectReply(false);
//        List<Advice> advice = new ArrayList<>();
//        advice.add(handlerRetryAdvice());
//        handler.setAdviceChain(advice);
//        handler.setRequestFactory(clientHttpRequestFactory());
        return handler;
    }

    @Bean
    public LoggingHandler loggingHandler(){
        LoggingHandler loggingHandler = new LoggingHandler(LoggingHandler.Level.DEBUG);
        loggingHandler.setLoggerName("TEST_LOGGER");
        loggingHandler.setLogExpressionString("headers.id + ': ' + payload");
        return loggingHandler;
    }

    @Bean
    public RequestHandlerRetryAdvice handlerRetryAdvice() {
        return new RequestHandlerRetryAdvice();
    }


    @Bean
    public IntegrationFlow pollingFlow() {
        return IntegrationFlows
                .from(fileReadingMessageSource(),
                c -> c.poller(Pollers.fixedRate(1000)))
//                .channel("inputChannel")
//                .from(fileReadingMessageSource())
//                .bridge(c -> c.poller(Pollers.fixedRate(1000)))
//                .channel(httpChannel())
                .log(LoggingHandler.Level.DEBUG, "TEST_LOGGER",
                        m -> m.getHeaders().getId() + ": " + m.getPayload())
//                .channel(sftpChannel())
                .transform(transformer())
//                .channel("transformer")
//                .channel(MessageChannels.queue())
//                .channel(MessageChannels.direct("application.pollingFlow.channel#2").interceptor(new HttpChannelInterceptor()))
//                .handle(httpRequestExecutingMessageHandler())
//                .channel("handler")
                .handle(httpRequestExecutingMessageHandler(),e -> e.advice(expressionAdvice()))
//                .channel(httpChannel())
                .get();
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
