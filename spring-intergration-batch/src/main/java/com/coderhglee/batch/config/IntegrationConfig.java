package com.coderhglee.batch.config;

import com.coderhglee.batch.interceptor.ExecutorInterceptor;
import com.coderhglee.batch.interceptor.PollersInterceptor;
import org.aopalliance.aop.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${working.path}")
    private String WORKING_PATH;

    private String FILE_PATTERN = "*.*";

    @Autowired
    private ApplicationContext appContext;

    @Bean
    public DirectChannel pollerChannel() {
        DirectChannel directChannel = new DirectChannel();
        directChannel.addInterceptor(new PollersInterceptor());
        return directChannel;
    }

    /**
     * SimpleMetadataStore Bean
     * 별도의 파일로 저장하지않고 메모리에 MessageSource 파일 내용을 저장한다.
     * @return
     */
    @Bean
    public SimpleMetadataStore simpleMetadataStore() {
        return new SimpleMetadataStore();
    }

    /**
     * PropertiesPersistingMetadataStore Bean
     * MessageSource 에서 읽은 파일을 .properties 파일에 목록을 저장한다.
     * @return
     */
    @Bean
    public PropertiesPersistingMetadataStore getMetadataStore() {
        PropertiesPersistingMetadataStore metadataStore = new PropertiesPersistingMetadataStore();
        metadataStore.setBaseDirectory(WORKING_PATH+"/tmp");
        metadataStore.afterPropertiesSet();
//        Logger.info(metadataStore.getClass().getName(), " metadataStore ");
        return metadataStore;
    }

    /**
     * 멀티 스레드 환경을 만들기 위한 Bean
     * Filter를 설정하지않으면 다른 Thread에서 같은 File을 참고하게 된다. 이것을 방지하기 위해 Filter를 설정한다.
     * FileSystemPersistentAcceptOnceFileListFilter
     *
     * AcceptOnceFileListFilter 를 정의할수도있지만 이 클래스는 파일이름에 한정하여 Filtering 한다.
     *
     * @return
     */
    @Bean
    public FileSystemPersistentAcceptOnceFileListFilter acceptOnceFileListFilter(){
        FileSystemPersistentAcceptOnceFileListFilter fileListFilter = new FileSystemPersistentAcceptOnceFileListFilter(getMetadataStore(),"fileReadingMessageSource");
        // Determine whether the metadataStore should be flushed on each update
        fileListFilter.setFlushOnUpdate(true);

        return fileListFilter;
    }

    /**
     * MessageSource<File> Bean
     * FileReadingMessageSource
     * @return
     */
    @Bean
//    @InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
    public MessageSource<File> fileReadingMessageSource(){
        FileReadingMessageSource sourceReader = new FileReadingMessageSource();

        //subscribe directory
        sourceReader.setDirectory(new File(WORKING_PATH+"/in"));
        //setting filter
        sourceReader.setFilter(new CompositeFileListFilter<File>()
                .addFilter(acceptOnceFileListFilter())
//                .addFilter(new AcceptOnceFileListFilter<>())
//                .addFilter(new LastModifiedFileListFilter())
//                .addFilter(new IgnoreHiddenFileListFilter())
                .addFilter(new SimplePatternFileListFilter(FILE_PATTERN)));
        return sourceReader;
    }

    /**
     * FileToByteArrayTransformer
     * File을 Byte[]으로 변환하여 handler로 전달하는 transformer
     * @return
     */
    @Transformer
    public FileToByteArrayTransformer transformer() {
        FileToByteArrayTransformer transformer = new FileToByteArrayTransformer();
        transformer.setDeleteFiles(true);
        return transformer;
    }

    /**
     *
     * @return
     */
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
}
