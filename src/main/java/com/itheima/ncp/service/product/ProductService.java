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

    /** 查询全部商品（管理视角）。 */
    List<Product> listAll();

    /** 管理端按关键字与状态查询商品。 */
    List<Product> listForAdmin(String keyword, ProductStatus status);

    /** 查询全部上架商品。 */
    List<Product> listOnShelf();

    /** 统计上架商品数。 */
    int countOnShelf();

    /** 分页查询上架商品。 */
    List<Product> listOnShelfPage(int offset, int limit);

    /** 按主键查询商品。 */
    Product getById(Long id);

    /** 拆分商品图片 CSV 为文件名列表。 */
    List<String> splitStoredImageNames(Product p);

    /** 批量提取商品首图映射（商品ID -> 首图名）。 */
    Map<Long, String> mapFirstImageStoredByProducts(List<Product> products);

    /** 判断图片文件名是否被商品引用。 */
    boolean isStoredImageRegistered(String storedName);

    /** 创建商品并处理图片上传。 */
    void createProduct(String operatorUsername, String name, String description,
                       BigDecimal price, int stock, ProductStatus status,
                       MultipartFile[] images) throws IOException;

    /** 更新商品上下架状态。 */
    void updateStatus(Long id, ProductStatus newStatus);

    /** 更新商品信息并处理图片增删。 */
    void updateProduct(Long id, String name, String description, BigDecimal price, int stock,
                      ProductStatus status, MultipartFile[] newImages, String[] removeImageNames) throws IOException;

    /** 删除商品。 */
    void deleteProduct(Long id);
}
