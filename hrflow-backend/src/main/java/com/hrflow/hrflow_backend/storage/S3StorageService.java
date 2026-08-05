package com.hrflow.hrflow_backend.storage;

import com.hrflow.hrflow_backend.exceptionHandler.storage.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final StorageProperties props;

    @Override
    public void upload(String key, InputStream data, String contentType, long size) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(props.bucket())
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(data, size)
            );
        } catch (S3Exception e) {
            throw new StorageException("Failed to upload file: " + key + "Caused by" + e);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        return props.publicBaseUrl() + "/" + key;
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(props.bucket()).key(key).build());
        } catch (S3Exception e) {
            throw new StorageException("Failed to delete file: " + "Caused by" + key + e);
        }
    }
}