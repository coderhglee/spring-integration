package com.coderhglee.batch.config;

import com.coderhglee.batch.interceptor.ExecutorInterceptor;
import com.coderhglee.batch.interceptor.PollersInterceptor;
import org.aopalliance.aop.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.*;
import org.springframework.integration.file.transformer.FileToByteArrayTransformer;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableIntegration
public class IntegrationConfig {
    public String INPUT_DIR = "C:\\Users\\KLCOM000\\repository\\spring-integration\\spring-intergration-batch\\in";
    public String FILE_PATTERN = "*.*";

    @Autowired
    private ApplicationContext appContext;

    @Bean
    public DirectChannel pollerChannel() {
        DirectChannel directChannel = new DirectChannel();
        directChannel.addInterceptor(new PollersInterceptor());
        return directChannel;
    }

//    @Bean
//    public MetadataStore metadataStore() {
//        PropertiesPersistingMetadataStore metadataStore = new PropertiesPersistingMetadataStore();
//        metadataStore.setBaseDirectory("C:\\Users\\KLCOM000\\repository\\spring-integration\\spring-intergration-batch\\tmp");
//        return metadataStore;
//    }

    @Bean
    public SimpleMetadataStore simpleMetadataStore() {
        return new SimpleMetadataStore();
    }

    @Bean
    public PropertiesPersistingMetadataStore getMetadataStore() {
        PropertiesPersistingMetadataStore metadataStore = new PropertiesPersistingMetadataStore();
        metadataStore.setBaseDirectory("C:\\Users\\KLCOM000\\repository\\spring-integration\\spring-intergration-batch\\tmp");
        metadataStore.afterPropertiesSet();
//        Logger.info(metadataStore.getClass().getName(), " metadataStore ");
        return metadataStore;
    }

    @Bean
//    @InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
    public MessageSource<File> fileReadingMessageSource(){
        FileReadingMessageSource sourceReader = new FileReadingMessageSource();
        sourceReader.setDirectory(new File(INPUT_DIR));
        FileSystemPersistentAcceptOnceFileListFilter fileListFilter = new FileSystemPersistentAcceptOnceFileListFilter(getMetadataStore(),"fileReadingMessageSource");

        // Determine whether the metadataStore should be flushed on each update
        fileListFilter.setFlushOnUpdate(true);

        sourceReader.setFilter(new CompositeFileListFilter<File>()
                .addFilter(fileListFilter)
//                .addFilter(new AcceptOnceFileListFilter<>())
//                .addFilter(new LastModifiedFileListFilter())
//                .addFilter(new IgnoreHiddenFileListFilter())
                .addFilter(new SimplePatternFileListFilter(FILE_PATTERN)));
        return sourceReader;
    }

//    public FileWritingMessageHandler fileWritingMessageHandler() {
//        FileWritingMessageHandler handler = new FileWritingMessageHandler();
//        return handler;
//    }

    @Transformer
    public FileToByteArrayTransformer transformer() {
        FileToByteArrayTransformer transformer = new FileToByteArrayTransformer();
        transformer.setDeleteFiles(true);
        return transformer;
    }

    @Bean
    public HttpRequestExecutingMessageHandler httpRequestExecutingMessageHandler() {
        RestTemplateBuilder restTemplate = appContext.getBean("restTemplate", RestTemplateBuilder.class);
        Map<String, Expression> expressionMap = new HashMap<>();
        ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
        expressionMap.put("foo", EXPRESSION_PARSER.parseExpression("headers.file_name"));
        HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://127.0.0.1:8080/hello/{foo}", restTemplate.build());
        handler.setUriVariableExpressions(expressionMap);
        handler.setExpectReply(false);
        return handler;
    }


    @Bean
    public LoggingHandler loggingHandler() {
        LoggingHandler loggingHandler = new LoggingHandler(LoggingHandler.Level.DEBUG);
        loggingHandler.setLoggerName("TEST_LOGGER");
        loggingHandler.setLogExpressionString("headers.id + ': ' + payload");
        return loggingHandler;
    }

    @Bean
    public Advice retryAdvice() {
        RequestHandlerRetryAdvice retryAdvice = new RequestHandlerRetryAdvice();
        RetryTemplate retryTemplate = new RetryTemplate();
        RetryPolicy retryPolicy = appContext.getBean("retryPolicy", RetryPolicy.class);
//        FixedBackOffPolicy fixedBackOffPolicy = appContext.getBean("fixedBackOffPolicy",FixedBackOffPolicy.class);
//        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);
        retryAdvice.setRetryTemplate(retryTemplate);
        retryAdvice.setRecoveryCallback(new CustomRecoveryCallback());
        return retryAdvice;
    }

    @Bean
    public ExecutorChannel executorChannel() {
        ExecutorService exec = Executors.newFixedThreadPool(5);
        ExecutorChannel executorChannel = new ExecutorChannel(exec);
        executorChannel.addInterceptor(new ExecutorInterceptor());
        return executorChannel;
    }

    @Bean
    public IntegrationFlow pollingFlow() {
//        ExecutorService exec = Executors.newFixedThreadPool(5);
        return IntegrationFlows
                .from(fileReadingMessageSource(),
                        c -> c.poller(Pollers.fixedDelay(5000).maxMessagesPerPoll(5)))
//                .channel(pollerChannel())
//                .filter(new ChainFileListFilter<File>()
//                        .addFilter(new AcceptOnceFileListFilter<>())
//                        .addFilter(new SimplePatternFileListFilter(FILE_PATTERN)))
                .channel(executorChannel())
//                .route(new HeaderValueRouter("STATE"),c -> c.advice(expressionAdvice()))
//                .routeToRecipients(r -> r
//                                .recipient(executorChannel())
//                                .recipientFlow("true",failure())
//                        .defaultOutputToParentFlow())
                .log(LoggingHandler.Level.DEBUG, "TEST_LOGGER",
                        m -> m.getHeaders().getId() + ": " + m.getPayload())
                .transform(transformer())
//                .<File,Boolean>route(File::canExecute, c -> c.advice(expressionAdvice()))
//                .channel(pollerChannel())
                .handle(httpRequestExecutingMessageHandler(), e -> e.advice(retryAdvice()))
//                .channel(pollerChannel())
                .get();
    }

//    @Bean
//    public Advice expressionAdvice() {
//        ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
//        advice.setSuccessChannelName("success.input");
//        advice.setOnSuccessExpressionString("payload + ' was successful'");
//        advice.setFailureChannelName("failure.input");
//        advice.setOnFailureExpressionString(
//                "payload + ' was bad, with reason: ' + #exception.cause.message");
//        advice.setTrapException(true);
//        return advice;
//    }
//
//    @Bean
//    public IntegrationFlow success() {
//        return f -> f.handle(System.out::println);
//    }
//
//    @Bean
//    public IntegrationFlow failure() {
//        return f -> f.handle(System.out::println);
//    }
}
