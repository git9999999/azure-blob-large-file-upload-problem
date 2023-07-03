package com.azureproblem.blob.controller;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.netty.http.client.HttpClient;

@RestController
@RequestMapping()
@Slf4j
public class AzureBlobBugDownloaderController {

    public static final int  MS_COMMON_BUFFER_SIZE_INT  = 40 * Constants.MB;
    public static final long MS_COMMON_BUFFER_SIZE_LONG = 40 * Constants.MB;

    public static final String BLOB_CONTAINER_NAME = "azure-blob-storage-bug";

    private final BlobContainerClient containerClient;

    private final BlobServiceClient blobServiceClient;

    private final WebClient webClient;

    private final ParallelTransferOptions parallelTransferOptions;
    private final BlobContainerAsyncClient blobContainerAsyncClient;


    public AzureBlobBugDownloaderController(BlobServiceClientBuilder blobServiceClientBuilder) {
        this.blobServiceClient = blobServiceClientBuilder.buildClient();
        this.containerClient = this.blobServiceClient.getBlobContainerClient(BLOB_CONTAINER_NAME);
        BlobServiceAsyncClient blobServiceAsyncClient = blobServiceClientBuilder.buildAsyncClient();
        this.blobContainerAsyncClient = blobServiceAsyncClient.getBlobContainerAsyncClient(BLOB_CONTAINER_NAME);
        log.debug("technicalAppIp '{}' technicalAppPort '{}'", "8545", "8545");
        this.webClient = msCommonWebClientBuilder().baseUrl(createUrl("client", "localhost", "8545")).build();

        this.parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(MS_COMMON_BUFFER_SIZE_LONG)
            .setMaxConcurrency(5)
            .setProgressListener(bytesTransferred -> {
                log.info("write bytes, bytes transferred '{}'", bytesTransferred);
            });
        createBlobContainer();
    }

    public void createBlobContainer() {
        if (!this.blobServiceClient.getBlobContainerClient(BLOB_CONTAINER_NAME).exists()) {
            log.info("blob container '{}' doesn't exist yet - creating it", BLOB_CONTAINER_NAME);
            this.blobServiceClient.getBlobContainerClient(BLOB_CONTAINER_NAME).create();
        }
    }

    public static Builder msCommonWebClientBuilder() {
        return WebClient.builder()
            // https://github.com/reactor/reactor-netty/issues/756
            .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MS_COMMON_BUFFER_SIZE_INT));
    }

    @GetMapping(path = "/trigger-download-to-file/{fileSizeInMb}")
    public void triggerDownloadToFile(@PathVariable int fileSizeInMb) {
        log.info("triggerDownloadToFile");

        var flux = this.webClient
            .get()
            .uri("/serve-file/" + fileSizeInMb)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .exchangeToFlux(clientResponse -> clientResponse.body(BodyExtractors.toDataBuffers()));

        Path targetFile = Path.of("C:\\develop\\tmp\\large-file-test").resolve(UUID.randomUUID().toString());
        try {
            Files.createDirectories(targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (OutputStream outputStream = new FileOutputStream(targetFile.resolve("testData.pdf").toAbsolutePath().toString(), false)) {
            DataBufferUtils.write(flux, outputStream).map(DataBufferUtils::release).blockLast(Duration.ofHours(22));
            outputStream.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!   end download of {}", targetFile);
    }

    @GetMapping(path = "/trigger-generation-of-random-data-to-blobstore/{fileSizeInMb}")
    public void triggerGenerationOfRandomDataToBlobStore(@PathVariable int fileSizeInMb) {
        log.info("triggerGenerationOfRandomDataToBlobStore");
        try {
            byte[] bytes = new byte[fileSizeInMb * Constants.MB];
            SecureRandom.getInstanceStrong().nextBytes(bytes);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);) {
                var destination = "TestDownloadToAzureBlobStorage" + System.currentTimeMillis() + ".pdf";

                var blobClientTarget = this.containerClient.getBlobClient(destination);

                try (var outputStream = blobClientTarget.getBlockBlobClient().getBlobOutputStream(this.parallelTransferOptions, null, null, null, null)) {
                    outputStream.write(inputStream.readAllBytes());
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!   end download of {}", destination);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    @GetMapping(path = "/trigger-download-to-blob/{fileSizeInMb}")
    public void triggerDownloadToBlob(@PathVariable int fileSizeInMb) {
        log.info("triggerDownloadToBlob");

        var flux = this.webClient
            .get()
            .uri("/serve-file/" + fileSizeInMb)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .exchangeToFlux(clientResponse -> clientResponse.body(BodyExtractors.toDataBuffers()));

        var destination = "TestDownloadToAzureBlobStorage" + System.currentTimeMillis() + ".pdf";

        var blobClientTarget = this.containerClient.getBlobClient(destination);

        try (var outputStream = blobClientTarget.getBlockBlobClient().getBlobOutputStream(this.parallelTransferOptions, null, null, null, null)) {
            DataBufferUtils.write(flux, outputStream)
                .map(DataBufferUtils::release)
                .blockLast(Duration.ofHours(22));
            outputStream.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!   end download of {}", destination);
    }


    @GetMapping(path = "/trigger-download-to-blob-working-ok/{fileSizeInMb}")
    public void triggerDownloadToBlobWorkingOk(@PathVariable int fileSizeInMb) {
        log.info("triggerDownloadToBlob");

        var flux = this.webClient
            .get()
            .uri("/serve-file/" + fileSizeInMb)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .exchangeToFlux(clientResponse -> clientResponse.body(BodyExtractors.toDataBuffers()));

        var destination = "TestDownloadToAzureBlobStorage" + System.currentTimeMillis() + ".pdf";

        var blobClientTarget = this.blobContainerAsyncClient.getBlobAsyncClient(destination);

        blobClientTarget.upload(flux.map(dataBuffer -> {
            ByteBuffer buffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
            dataBuffer.toByteBuffer(buffer);
            DataBufferUtils.release(dataBuffer);
            return buffer;
        }), this.parallelTransferOptions).block();

        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!   end download of {}", destination);
    }


    /**
     * https://stackoverflow.com/questions/76575117/can-not-transfer-more-than-250mb-with-databufferutils-write-to-azure-blob-storag/76580753#76580753
     */
    @GetMapping(path = "/trigger-download-to-blob-2/{fileSizeInMb}")
    public void triggerDownloadToBlob2(@PathVariable int fileSizeInMb) {
        log.info("triggerDownloadToBlob2");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        var flux = this.webClient
            .get()
            .uri("/serve-file/" + fileSizeInMb)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .exchangeToFlux(clientResponse -> clientResponse.body(BodyExtractors.toDataBuffers()));

        var destination = "TestDownloadToAzureBlobStorage" + System.currentTimeMillis() + ".pdf";

        var blobClientTarget = this.containerClient.getBlobClient(destination);

        try (var outputStream = blobClientTarget.getBlockBlobClient().getBlobOutputStream()) {
            DataBufferUtils.write(flux, outputStream)
                .doOnError(throwable -> {
                    // Handle the error gracefully
                    stopWatch.stop();
                    System.out.println("Error occurred during data streaming: " + throwable.getMessage());
                })
                .doFinally(signal -> {
                    try {
                        outputStream.flush();
                        outputStream.close();
                        stopWatch.stop();
                        System.out.println("File uploaded successfully " + stopWatch.getTotalTimeMillis() + "ms");
                    } catch (IOException e) {
                        System.out.println("Error occurred while closing the output stream: " + e.getMessage());
                    }
                })
                .blockLast(Duration.ofSeconds(111111111));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!   end download of {}", destination);
    }

    private static final String HTTP = "http://";

    public static String createUrl(String name, String ip, String port) {
        if (isEmpty(name)) {
            throw new IllegalStateException("name is empty");
        }
        if (isEmpty(ip)) {
            throw new IllegalStateException("ip is empty for " + name);
        }
        if (isEmpty(port)) {
            throw new IllegalStateException("ip is empty for " + port);
        }
        var url = HTTP + ip + ":" + port;
        log.info("url for '{}' is '{}'", name, url);
        return url;
    }

}

