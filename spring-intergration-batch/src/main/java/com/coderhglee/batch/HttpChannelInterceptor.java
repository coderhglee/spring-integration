package com.coderhglee.batch;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.AdviceName;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

@Log4j2
public class HttpChannelInterceptor implements ChannelInterceptor {
    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        log.info("HttpChannelInterceptor.afterReceiveCompletion");
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        log.info("HttpChannelInterceptor.afterSendCompletion");
    }


}
