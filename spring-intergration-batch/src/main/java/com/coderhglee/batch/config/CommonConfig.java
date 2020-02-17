package com.coderhglee.batch.config;

import com.coderhglee.batch.interceptor.ExecutorInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.file.filters.FileSystemPersistentAcceptOnceFileListFilter;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 모든 integration에서 사용하는 공통 Bean을 주입한다.
 */
@Configuration
//@Order(1)
public class CommonConfig {

    @Value("${working.path}")
    private String WORKING_PATH;

    /**
     * SimpleMetadataStore Bean
     * 별도의 파일로 저장하지않고 메모리에 MessageSource 파일 내용을 저장한다.
     *
     * @return
     */
//    @Bean
//    public SimpleMetadataStore simpleMetadataStore() {
//        return new SimpleMetadataStore();
//    }

    /**
     * PropertiesPersistingMetadataStore Bean
     * MessageSource 에서 읽은 파일을 .properties 파일에 목록을 저장한다.
     *
     * @return
     */
    @Bean
    public PropertiesPersistingMetadataStore metadataStore() {
        PropertiesPersistingMetadataStore metadataStore = new PropertiesPersistingMetadataStore();
        metadataStore.setBaseDirectory(WORKING_PATH + "/tmp");
//        metadataStore.afterPropertiesSet();
//        Logger.info(metadataStore.getClass().getName(), " metadataStore ");
        return metadataStore;
    }

//    @Bean
//    public ExecutorChannel executorChannel() {
//        ExecutorService exec = Executors.newFixedThreadPool(5);
//        ExecutorChannel executorChannel = new ExecutorChannel(exec);
//        executorChannel.addInterceptor(new ExecutorInterceptor());
//        return executorChannel;
//    }

    @Bean
    public LoggingHandler loggingHandler() {
        LoggingHandler loggingHandler = new LoggingHandler(LoggingHandler.Level.DEBUG);
        loggingHandler.setLoggerName("TEST_LOGGER");
        loggingHandler.setLogExpressionString("headers.id + ': ' + payload");
        return loggingHandler;
    }
}
