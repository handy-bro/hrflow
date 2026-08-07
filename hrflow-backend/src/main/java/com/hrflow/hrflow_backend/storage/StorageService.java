package com.hrflow.hrflow_backend.storage;

import java.io.InputStream;
import java.io.OutputStream;

public interface StorageService {

    void upload(String key, InputStream data, String contentType, long size);

    String getPublicUrl(String key);

    void delete(String key);

    void download(String key, OutputStream out);
}
