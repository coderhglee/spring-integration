package com.coderhglee.batch.config;

import com.coderhglee.batch.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryContext;

@Log4j2
public class CustomRecoveryCallback implements RecoveryCallback {

    @Autowired
    private FileService fileService;

    @Override
    public Object recover(RetryContext retryContext) throws Exception {
        GenericMessage genericMessage = (GenericMessage) retryContext.getAttribute("message");
        for (String s : genericMessage.getHeaders().keySet()) {
            log.info("key {} value {}", s, genericMessage.getHeaders().get(s).toString());
        }

        String file_originalFile_path = genericMessage.getHeaders().get("file_originalFile").toString();
//        String file_name = genericMessage.getHeaders().get("file_name").toString();
//        File org_file = new File(file_originalFile_path);
//        File recover_file = new File(recoveryFilePath);
//        FileUtils.moveFileToDirectory(org_file, recover_file, true);
        return fileService.afterProcess(file_originalFile_path,"fail");
    }
}
