package com.hrflow.hrflow_backend.storage;

import com.hrflow.hrflow_backend.exceptionHandler.StorageException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;

@Component
public class FileValidator {

    public void validateImage(MultipartFile file) {
        validate(file, Set.of("image/jpeg", "image/png", "image/webp"), 5 * 1024 * 1024);
    }

    public void validate(MultipartFile file, Set<String> allowedTypes, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new StorageException("Unsupported file type: " + file.getContentType());
        }
        if (file.getSize() > maxSizeBytes) {
            throw new StorageException("File exceeds size limit of " + maxSizeBytes + " bytes");
        }
    }
}