package com.itheima.ncp.service.product;

import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品业务
 */
public interface ProductService {

    List<Product> listAll();

    List<Product> listForAdmin(String keyword, ProductStatus status);

    List<Product> listOnShelf();

    int countOnShelf();

    List<Product> listOnShelfPage(int offset, int limit);

    Product getById(Long id);

    List<String> splitStoredImageNames(Product p);

    Map<Long, String> mapFirstImageStoredByProducts(List<Product> products);

    boolean isStoredImageRegistered(String storedName);

    void createProduct(String operatorUsername, String name, String description,
                       BigDecimal price, int stock, ProductStatus status,
                       MultipartFile[] images) throws IOException;

    void updateStatus(Long id, ProductStatus newStatus);

    void updateProduct(Long id, String name, String description, BigDecimal price, int stock,
                      ProductStatus status, MultipartFile[] newImages, String[] removeImageNames) throws IOException;

    void deleteProduct(Long id);
}
