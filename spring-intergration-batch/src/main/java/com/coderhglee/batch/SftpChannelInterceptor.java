package com.coderhglee.batch;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 *
 */
@Log4j2
public class SftpChannelInterceptor implements ChannelInterceptor {
    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        log.info("SftpChannelInterceptor.afterReceiveCompletion");
        log.info(message);
        log.info(ex);
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        log.info("SftpChannelInterceptor.afterSendCompletion");
        log.info(message);
        log.info(sent);
        log.info(ex);
    }
}
