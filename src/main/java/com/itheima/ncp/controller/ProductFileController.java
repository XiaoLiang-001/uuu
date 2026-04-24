package com.itheima.ncp.controller;

import com.itheima.ncp.service.product.FileStorageService;
import com.itheima.ncp.service.product.ProductService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 商品文件访问控制器，按受控白名单方式返回商品图片资源。
 */
@Controller
public class ProductFileController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    public ProductFileController(ProductService productService, FileStorageService fileStorageService) {
        this.productService = productService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 访问或下载图片：须为某条商品 images 字段中出现的文件名。
     */
    @GetMapping("/files/products/{storedName:.+}")
    public ResponseEntity<Resource> serveByStoredName(@PathVariable String storedName,
                                                      @RequestParam(defaultValue = "false") boolean download) {
        if (!productService.isStoredImageRegistered(storedName)) {
            return ResponseEntity.notFound().build();
        }
        Path path = fileStorageService.resolveStoredFile(storedName);
        if (path == null || !Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path.toFile());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String probe = Files.probeContentType(path);
            if (probe != null && !probe.isEmpty()) {
                mediaType = MediaType.parseMediaType(probe);
            }
        } catch (Exception ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData(download ? "attachment" : "inline", storedName);
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
