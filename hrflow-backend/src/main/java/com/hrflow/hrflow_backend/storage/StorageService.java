package com.hrflow.hrflow_backend.storage;

import java.io.InputStream;

public interface StorageService {

    void upload(String key, InputStream data, String contentType, long size);

    String getPublicUrl(String key);

    void delete(String key);
}
