package com.coderhglee.api.controller;

import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@Log4j2
@RestController
public class OutBoundController {

    @Value("${working.path}")
    private String WORKING_PATH;

    @RequestMapping("/hello/{file_name}")
    public String callOutBound(@PathVariable("file_name") String fileName, HttpServletRequest request) throws Exception{

        log.info(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();

            File targetFile = new File(WORKING_PATH+"/out/"+fileName);
            OutputStream outStream = new FileOutputStream(targetFile);

            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                char[] charBuffer = new char[128];
                byte[] buffer = new byte[8 * 1024];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) > 0) {
//                    stringBuilder.append(charBuffer, 0, bytesRead);
                    outStream.write(buffer, 0, bytesRead);
                }
            }

            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outStream);

            Thread.sleep(5000);
        } catch (IOException ex) {
            throw ex;
        }

//        log.info(stringBuilder.toString());
        return "ok";
    }
}
