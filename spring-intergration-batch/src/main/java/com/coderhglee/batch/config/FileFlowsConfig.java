//package com.coderhglee.batch.config;
//
//import com.coderhglee.batch.interceptor.PollersInterceptor;
//import org.aopalliance.aop.Advice;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.integration.annotation.Transformer;
//import org.springframework.integration.channel.DirectChannel;
//import org.springframework.integration.channel.ExecutorChannel;
//import org.springframework.integration.config.EnableIntegration;
//import org.springframework.integration.core.MessageSource;
//import org.springframework.integration.dsl.IntegrationFlow;
//import org.springframework.integration.dsl.IntegrationFlows;
//import org.springframework.integration.dsl.Pollers;
//import org.springframework.integration.dsl.context.IntegrationFlowContext;
//import org.springframework.integration.file.FileReadingMessageSource;
//import org.springframework.integration.file.filters.CompositeFileListFilter;
//import org.springframework.integration.file.filters.FileSystemPersistentAcceptOnceFileListFilter;
//import org.springframework.integration.file.filters.SimplePatternFileListFilter;
//import org.springframework.integration.file.transformer.FileToByteArrayTransformer;
//import org.springframework.integration.handler.LoggingHandler;
//import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
//import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//
//@Configuration
//@EnableIntegration
////@Profile(value = "file")
//public class FileFlowsConfig {
//
//    @Value("${working.path}")
//    private String WORKING_PATH;
//
//    private String FILE_PATTERN = "*.*";
//
//    @Autowired
//    private ApplicationContext appContext;
//
//    private PropertiesPersistingMetadataStore metadataStore;
//    private ExecutorChannel executorChannel;
//    private HttpRequestExecutingMessageHandler httpMessageHandler;
//    private Advice retryAdvice;
//
//    @Autowired
//    private IntegrationFlowContext flowContext;
////    RestTemplateBuilder restTemplate = appContext.getBean("restTemplate", RestTemplateBuilder.class);
////    LoggingHandler loggingHandler = appContext.getBean("LoggingHandler", LoggingHandler.class);
//
//    @PostConstruct
//    public void init() {
//        this.metadataStore = appContext.getBean("metadataStore", PropertiesPersistingMetadataStore.class);
//        this.executorChannel = appContext.getBean("executorChannel", ExecutorChannel.class);
//        this.httpMessageHandler = appContext.getBean("httpRequestExecutingMessageHandler", HttpRequestExecutingMessageHandler.class);
//        this.retryAdvice = appContext.getBean("retryAdvice", Advice.class);
//
//        this.flowContext.registration(pollingFlow()).register();
//        this.flowContext.registration(pollingFlow2()).register();
//    }
//
//    @Bean
//    public DirectChannel pollerChannel() {
//        DirectChannel directChannel = new DirectChannel();
//        directChannel.addInterceptor(new PollersInterceptor());
//        return directChannel;
//    }
//
//    /**
//     * 멀티 스레드 환경을 만들기 위한 Bean
//     * Filter를 설정하지않으면 다른 Thread에서 같은 File을 참고하게 된다. 이것을 방지하기 위해 Filter를 설정한다.
//     * FileSystemPersistentAcceptOnceFileListFilter
//     *
//     * AcceptOnceFileListFilter 를 정의할수도있지만 이 클래스는 파일이름에 한정하여 Filtering 한다.
//     *
//     * @return
//     */
//    @Bean
//    public FileSystemPersistentAcceptOnceFileListFilter acceptOnceFileListFilter(){
//        FileSystemPersistentAcceptOnceFileListFilter fileListFilter = new FileSystemPersistentAcceptOnceFileListFilter(this.metadataStore,"fileReadingMessageSource");
//        // Determine whether the metadataStore should be flushed on each update
//        fileListFilter.setFlushOnUpdate(true);
//
//        return fileListFilter;
//    }
//
//    /**
//     * MessageSource<File> Bean
//     * FileReadingMessageSource
//     * @return
//     */
//    @Bean
////    @InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
//    public MessageSource<File> fileReadingMessageSource(){
//        FileReadingMessageSource sourceReader = new FileReadingMessageSource();
//        //subscribe directory
//        sourceReader.setDirectory(new File(WORKING_PATH+"/in"));
//        //setting filter
//        sourceReader.setFilter(new CompositeFileListFilter<File>()
//                .addFilter(acceptOnceFileListFilter())
////                .addFilter(new AcceptOnceFileListFilter<>())
////                .addFilter(new LastModifiedFileListFilter())
////                .addFilter(new IgnoreHiddenFileListFilter())
//                .addFilter(new SimplePatternFileListFilter(FILE_PATTERN)));
//        return sourceReader;
//    }
//
//    /**
//     * MessageSource<File> Bean
//     * FileReadingMessageSource
//     * @return
//     */
//    @Bean
////    @InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
//    public MessageSource<File> fileReadingMessageSource2(){
//        FileReadingMessageSource sourceReader = new FileReadingMessageSource();
//        //subscribe directory
//        sourceReader.setDirectory(new File(WORKING_PATH+"/in2"));
//        //setting filter
//        sourceReader.setFilter(new CompositeFileListFilter<File>()
//                .addFilter(acceptOnceFileListFilter())
////                .addFilter(new AcceptOnceFileListFilter<>())
////                .addFilter(new LastModifiedFileListFilter())
////                .addFilter(new IgnoreHiddenFileListFilter())
//                .addFilter(new SimplePatternFileListFilter(FILE_PATTERN)));
//        return sourceReader;
//    }
//
//    /**
//     * FileToByteArrayTransformer
//     * File을 Byte[]으로 변환하여 handler로 전달하는 transformer
//     * @return
//     */
//    @Transformer
//    public FileToByteArrayTransformer transformer() {
//        FileToByteArrayTransformer transformer = new FileToByteArrayTransformer();
//        transformer.setDeleteFiles(true);
//        return transformer;
//    }
//
//
//
//
//
////    @Bean
//    public IntegrationFlow pollingFlow() {
////        ExecutorService exec = Executors.newFixedThreadPool(5);
//        return IntegrationFlows
//                .from(fileReadingMessageSource(),
//                        c -> c.poller(Pollers.fixedDelay(5000).maxMessagesPerPoll(5)))
////                .channel(pollerChannel())
////                .filter(new ChainFileListFilter<File>()
////                        .addFilter(new AcceptOnceFileListFilter<>())
////                        .addFilter(new SimplePatternFileListFilter(FILE_PATTERN)))
//                .channel(this.executorChannel)
////                .route(new HeaderValueRouter("STATE"),c -> c.advice(expressionAdvice()))
////                .routeToRecipients(r -> r
////                                .recipient(executorChannel())
////                                .recipientFlow("true",failure())
////                        .defaultOutputToParentFlow())
//                .log(LoggingHandler.Level.DEBUG, "TEST_LOGGER",
//                        m -> m.getHeaders().getId() + ": " + m.getPayload())
//                .transform(transformer())
////                .<File,Boolean>route(File::canExecute, c -> c.advice(expressionAdvice()))
////                .channel(pollerChannel())
//                .handle(this.httpMessageHandler, e -> e.advice(this.retryAdvice))
////                .channel(pollerChannel())
//                .get();
//    }
//
//    public IntegrationFlow pollingFlow2() {
////        ExecutorService exec = Executors.newFixedThreadPool(5);
//        return IntegrationFlows
//                .from(fileReadingMessageSource2(),
//                        c -> c.poller(Pollers.fixedDelay(5000).maxMessagesPerPoll(5)))
////                .channel(pollerChannel())
////                .filter(new ChainFileListFilter<File>()
////                        .addFilter(new AcceptOnceFileListFilter<>())
////                        .addFilter(new SimplePatternFileListFilter(FILE_PATTERN)))
//                .channel(this.executorChannel)
////                .route(new HeaderValueRouter("STATE"),c -> c.advice(expressionAdvice()))
////                .routeToRecipients(r -> r
////                                .recipient(executorChannel())
////                                .recipientFlow("true",failure())
////                        .defaultOutputToParentFlow())
//                .log(LoggingHandler.Level.DEBUG, "TEST_LOGGER",
//                        m -> m.getHeaders().getId() + ": " + m.getPayload())
//                .transform(transformer())
////                .<File,Boolean>route(File::canExecute, c -> c.advice(expressionAdvice()))
////                .channel(pollerChannel())
//                .handle(this.httpMessageHandler, e -> e.advice(this.retryAdvice))
////                .channel(pollerChannel())
//                .get();
//    }
//}
