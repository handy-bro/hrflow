package com.hrflow.hrflow_backend.storage;

import com.hrflow.hrflow_backend.exceptionHandler.StorageException;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class StorageKeyGenerator {

    public String employeePhotoKey(Long employeeId, String contentType) {
        return "employees/%d/photos/%s.%s".formatted(
                employeeId, UUID.randomUUID(), extensionFor(contentType));
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new StorageException("Unsupported file type: " + contentType);
        };
    }
}