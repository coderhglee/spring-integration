package com.coderhglee.batch.interceptor;

import com.coderhglee.batch.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.AdviceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.locking.NioFileLocker;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;

import java.io.File;

@Log4j2
public class PollersInterceptor implements ChannelInterceptor {
//    @Autowired
//    private FileService fileService;


    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        log.info("PollersInterceptor.afterReceiveCompletion");
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        FileService fileService = new FileService();
        log.info("PollersInterceptor.afterSendCompletion");
        if(sent){
            GenericMessage genericMessage = (GenericMessage) message;
            log.info(genericMessage.getHeaders().get("file_name"));
            log.info(genericMessage.getPayload());
            fileService.saveSuccess(genericMessage.getPayload(),genericMessage.getHeaders().get("file_name").toString());
        }
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("PollersInterceptor.preSend");
        GenericMessage genericMessage = (GenericMessage) message;

        NioFileLocker locker = new NioFileLocker();
        File file = new File(genericMessage.getPayload().toString());
        locker.unlock(file);
        return message;
    }
}
