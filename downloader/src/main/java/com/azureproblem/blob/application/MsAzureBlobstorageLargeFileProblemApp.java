package com.azureproblem.blob.application;

import com.azureproblem.blob.MsGeneralMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({MsGeneralMockConfiguration.class})
@Slf4j
public class MsAzureBlobstorageLargeFileProblemApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MsAzureBlobstorageLargeFileProblemApp.class, args);
    }

    @Override
    public void run(String... arg) {
        log.info("for the UI just call http://localhost:8100/index.html");
    }

}
