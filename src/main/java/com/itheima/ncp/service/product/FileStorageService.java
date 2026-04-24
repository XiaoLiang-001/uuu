package com.itheima.ncp.service.product;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 商品图片文件存储
 */
public interface FileStorageService {

    Path getProductsDirectory();

    boolean isAllowedImage(MultipartFile file);

    String saveProductImage(MultipartFile file) throws IOException;

    Path resolveStoredFile(String storedName);

    void deleteStoredSilently(String storedName);
}
