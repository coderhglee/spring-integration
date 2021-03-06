package com.coderhglee.batch.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Log4j2
@Service
public class FileService {

    @Value("working.path")
    public String WORKING_PATH;

    public boolean afterProcess(String filePath, String status) {
        File org_file = new File(filePath);
        String copyPath;

        if (status.equals("success")) {
            copyPath = WORKING_PATH+"/success";
        } else {
            copyPath = WORKING_PATH+"/fail";
        }

        File copy_file = new File(copyPath);

        try {

            if (copy_file.exists()) {
                copy_file.delete();
            }

            FileUtils.moveFileToDirectory(org_file, copy_file, true);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean saveSuccess(Object file,String fileName) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            FileOutputStream fileOut = new FileOutputStream(WORKING_PATH+"/success/"+fileName);
            ObjectOutputStream os = new ObjectOutputStream(fileOut);
            os.writeObject(file);
            os.close();
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }
}
