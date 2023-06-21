package com.azureproblem.blob;

import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azureproblem.blob.controller.AzureBlobBugDownloaderController;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;

public class MsGeneralMockConfiguration {


    @Bean
    public AzureBlobBugDownloaderController azureBlobBugDownloaderController(
        BlobServiceClientBuilder blobServiceClientBuilder) {
        return new AzureBlobBugDownloaderController(blobServiceClientBuilder);
    }


    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        var p = new DefaultPrettyPrinter();
        Indenter i = new DefaultIndenter("  ", "\n");
        p.indentArraysWith(i);
        p.indentObjectsWith(i);
        objectMapper.setDefaultPrettyPrinter(p);

        return objectMapper;
    }

}
