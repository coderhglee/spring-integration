package com.coderhglee.batch.config;

import com.coderhglee.batch.interceptor.ExecutorInterceptor;
import com.coderhglee.batch.vo.InboundVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.aop.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.FileSystemPersistentAcceptOnceFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.transformer.FileToByteArrayTransformer;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
import org.springframework.integration.transformer.GenericTransformer;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class InboundFlowConfig {

    @Autowired
    private Inbounds inbounds;

    private String FILE_PATTERN = "*.*";

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private IntegrationFlowContext flowContext;

    private HttpRequestExecutingMessageHandler httpMessageHandler;
    private Advice retryAdvice;
    private PropertiesPersistingMetadataStore metadataStore;

    @PostConstruct
    public void init() throws Exception {
        this.httpMessageHandler = appContext.getBean("httpRequestExecutingMessageHandler", HttpRequestExecutingMessageHandler.class);
        this.retryAdvice = appContext.getBean("retryAdvice", Advice.class);
        this.metadataStore = appContext.getBean("metadataStore", PropertiesPersistingMetadataStore.class);

        for (String target : inbounds.getInbound()) {

            ObjectMapper mapper = new ObjectMapper();
            File resource = new ClassPathResource(target + "-env.json").getFile();
            InboundVo inboundVo = mapper.readValue(resource, new TypeReference<InboundVo>() {
            });

            MessageSource<?> messageSource = null;
            ExecutorChannel executorChannel = null;
            GenericTransformer genericTransformer = null;
            GenericHandler genericHandler = null;
            Advice advice = null;


            if (inboundVo.getProtocol().equals("file")) {
                FileReadingMessageSource sourceReader = new FileReadingMessageSource();
                //subscribe directory
                sourceReader.setDirectory(new File(inboundVo.getRemote_file_path()));
                //setting filter
                sourceReader.setFilter(new CompositeFileListFilter<File>()
                        .addFilter(acceptOnceFileListFilter())
                        .addFilter(new SimplePatternFileListFilter(FILE_PATTERN)));
                messageSource = sourceReader;
            }

            ThreadFactory threadFactory = new CustomThreadsFactory(target);
            ExecutorService exec = Executors.newFixedThreadPool(10, threadFactory);
            executorChannel = new ExecutorChannel(exec);
            executorChannel.addInterceptor(new ExecutorInterceptor());

            FileToByteArrayTransformer transformer = new FileToByteArrayTransformer();
            transformer.setDeleteFiles(true);

            this.flowContext.registration(makeFlow(messageSource, executorChannel, transformer, httpMessageHandler, retryAdvice)).register();
        }
    }

    @Bean
    public FileSystemPersistentAcceptOnceFileListFilter acceptOnceFileListFilter() {
        FileSystemPersistentAcceptOnceFileListFilter fileListFilter = new FileSystemPersistentAcceptOnceFileListFilter(this.metadataStore, "fileReadingMessageSource");
        // Determine whether the metadataStore should be flushed on each update
        fileListFilter.setFlushOnUpdate(true);

        return fileListFilter;
    }


    public IntegrationFlow makeFlow(MessageSource<?> messageSource, ExecutorChannel inboundExecutor, GenericTransformer transformer, AbstractMessageHandler handler, Advice advice) {
        return IntegrationFlows
                .from(messageSource,
                        c -> c.poller(Pollers.fixedDelay(5000).maxMessagesPerPoll(10)))
                .channel(inboundExecutor)
                .transform(transformer)
                .handle(handler, e -> e.advice(advice))
                .get();
    }
}
