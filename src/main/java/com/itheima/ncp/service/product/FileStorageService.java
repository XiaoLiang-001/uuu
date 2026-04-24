package com.itheima.ncp.service.product;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 商品图片文件存储
 */
public interface FileStorageService {

    /** 返回商品图片根目录。 */
    Path getProductsDirectory();

    /** 判断文件是否为允许上传的图片类型。 */
    boolean isAllowedImage(MultipartFile file);

    /** 保存商品图片并返回存储文件名。 */
    String saveProductImage(MultipartFile file) throws IOException;

    /** 安全解析存储文件路径。 */
    Path resolveStoredFile(String storedName);

    /** 尝试删除文件，失败时静默忽略。 */
    void deleteStoredSilently(String storedName);
}
