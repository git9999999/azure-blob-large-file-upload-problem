# Start the App

Run a `docker-compose up` in the root leve of this project, this will start `azurite` to simulate
the Azure Blob storage

Start the apps `MsAzureBlobstorageLargeFileProblemApp`
and `MsAzureBlobstorageLargeFileProblemFileServerApp`  with parameter
`-Xmx5G -XX:+CrashOnOutOfMemoryError` just to be sure we do not run into a memory problem.

Note there is a persisted Intellij Run-configuration

`MsAzureBlobstorageLargeFileProblemApp.run.xml`
`MsAzureBlobstorageLargeFileProblemFileServerApp.run.xml`

# Execute the Test

Call the following REST-endpoint with a `get`. The last parameter is the size in MB, for the
testdata to be generated.

Example:
`localhost:8544/trigger-download-to-file/50`
`localhost:8544/trigger-download-to-blob/50`

# Problem

This works
`localhost:8544/trigger-download-to-blob/250`
and this
`localhost:8544/trigger-download-to-file/500`

This does not
`localhost:8544/trigger-download-to-blob/260`

please checkout the log of the app. You see that it only this msg and the app hangs for ever....

```
2023-06-01T12:59:25.079+02:00  INFO 21628 --- [ctor-http-nio-4] c.a.b.c.AzureBlobBugDownloaderController : write bytes, bytes transferred '0'
2023-06-01T12:59:25.086+02:00  INFO 21628 --- [ctor-http-nio-4] c.a.b.c.AzureBlobBugDownloaderController : write bytes, bytes transferred '0'
2023-06-01T12:59:25.091+02:00  INFO 21628 --- [ctor-http-nio-4] c.a.b.c.AzureBlobBugDownloaderController : write bytes, bytes transferred '0'
2023-06-01T12:59:25.096+02:00  INFO 21628 --- [ctor-http-nio-4] c.a.b.c.AzureBlobBugDownloaderController : write bytes, bytes transferred '0'
2023-06-01T12:59:25.102+02:00  INFO 21628 --- [ctor-http-nio-4] c.a.b.c.AzureBlobBugDownloaderController : write bytes, bytes transferred '0'
```

## Analyse

It is strange that the app can write (stream) any file to the file system. But we can not stream
something bigger than 250MB to the blobstorage, although we use a Output stream the same as in
the File case. 

## Generate Testdata on the client side and store it to the BlobStorage

This calls generate test data on the client side and writes it directly to the Blob Storage. Not 
Data is transmitted via REST.
`localhost:8544/trigger-generation-of-random-data-to-blobstore/260`