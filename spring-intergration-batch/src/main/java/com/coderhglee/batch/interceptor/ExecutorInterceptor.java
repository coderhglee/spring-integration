package com.coderhglee.batch.interceptor;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.locking.NioFileLocker;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.ManagementFactory;

@Log4j2
public class ExecutorInterceptor implements ChannelInterceptor {

    @Autowired
    private SimpleMetadataStore simpleMetadataStore;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//        mbs.get
        log.info("PollersInterceptor.preSend");
        GenericMessage genericMessage = (GenericMessage) message;
//        log.info(simpleMetadataStore.metadata);
        return message;
    }
}
