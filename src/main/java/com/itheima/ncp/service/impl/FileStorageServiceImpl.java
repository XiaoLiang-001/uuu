package com.itheima.ncp.service.impl;

import com.itheima.ncp.service.product.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    ));

    @Value("${app.upload.dir:uploads}")
    private String uploadRoot;

    private Path productsDirectory;

    @PostConstruct
    public void init() throws IOException {
        Path root = Paths.get(uploadRoot).toAbsolutePath().normalize();
        this.productsDirectory = root.resolve("products").normalize();
        Files.createDirectories(productsDirectory);
    }

    @Override
    public Path getProductsDirectory() {
        return productsDirectory;
    }

    @Override
    public boolean isAllowedImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String ct = file.getContentType();
        if (ct == null || !ct.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return false;
        }
        String ext = extension(file.getOriginalFilename());
        return ALLOWED_EXT.contains(ext);
    }

    @Override
    public String saveProductImage(MultipartFile file) throws IOException {
        if (!isAllowedImage(file)) {
            throw new IllegalArgumentException("仅支持常见图片格式（jpg/png/gif/webp）");
        }
        String ext = extension(file.getOriginalFilename());
        String stored = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = productsDirectory.resolve(stored).normalize();
        if (!target.startsWith(productsDirectory)) {
            throw new IOException("非法路径");
        }
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return stored;
    }

    @Override
    public Path resolveStoredFile(String storedName) {
        if (storedName == null || storedName.contains("..") || storedName.contains("/") || storedName.contains("\\")) {
            return null;
        }
        Path p = productsDirectory.resolve(storedName).normalize();
        if (!p.startsWith(productsDirectory)) {
            return null;
        }
        return p;
    }

    private static String extension(String original) {
        if (original == null || !original.contains(".")) {
            return "";
        }
        return original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    @Override
    public void deleteStoredSilently(String storedName) {
        try {
            Path p = resolveStoredFile(storedName);
            if (p != null && Files.exists(p)) {
                Files.delete(p);
            }
        } catch (IOException ignored) {
        }
    }
}
