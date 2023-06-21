package com.azureproblem.bug.controller;

import com.azure.storage.common.implementation.Constants;
import java.io.ByteArrayInputStream;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@Slf4j
public class FileServerRestController {

    public FileServerRestController() {
    }

    @GetMapping(path = "/serve-file/{fileSizeInMb}")
    public InputStreamResource serverFile(@PathVariable int fileSizeInMb) {
        log.info("serverFile {}", fileSizeInMb);
        try {
////            File resource = new ClassPathResource("TestFile50MB.pdf").getFile();
//            File resource = new ClassPathResource("TestFile1GB.pdf").getFile();
//            byte[] bytes = Files.readAllBytes(resource.toPath());
            byte[] bytes = new byte[fileSizeInMb * Constants.MB];
            SecureRandom.getInstanceStrong().nextBytes(bytes);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);) {
                return new InputStreamResource(inputStream);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


}

