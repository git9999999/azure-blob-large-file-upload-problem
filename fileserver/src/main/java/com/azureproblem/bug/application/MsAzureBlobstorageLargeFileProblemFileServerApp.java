package com.azureproblem.bug.application;

import com.azureproblem.bug.AzureProblemBlobFileServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AzureProblemBlobFileServer.class})
@Slf4j
public class MsAzureBlobstorageLargeFileProblemFileServerApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MsAzureBlobstorageLargeFileProblemFileServerApp.class, args);
    }

    @Override
    public void run(String... arg) {
        log.info("for the UI just call http://localhost:8100/index.html");
    }

}
