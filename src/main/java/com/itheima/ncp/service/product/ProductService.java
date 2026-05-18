package com.itheima.ncp.service.product;

import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import com.itheima.ncp.dto.ProvinceOptionDto;
import com.itheima.ncp.dto.CategoryOptionDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 商品业务
 */
public interface ProductService {

    /** 查询全部商品（管理视角）。 */
    List<Product> listAll();

    /** 管理端按关键字、状态、省份、分类查询商品。 */
    List<Product> listForAdmin(String keyword, ProductStatus status, Long provinceId, Long categoryId);

    /** 查询全部上架商品。 */
    List<Product> listOnShelf();

    /** 统计上架商品数。 */
    int countOnShelf();

    /** 按关键词 + 省份 + 分类筛选统计上架商品数。 */
    int countOnShelf(String keyword, Long provinceId, Long categoryId);

    /** 分页查询上架商品。 */
    List<Product> listOnShelfPage(int offset, int limit);

    /** 按关键词 + 省份 + 分类分页查询上架商品。 */
    List<Product> listOnShelfPage(String keyword, Long provinceId, Long categoryId, int offset, int limit);

    /** 查询集市筛选可用省份（仅有上架商品的省份）。 */
    List<ProvinceOptionDto> listProvinceOptionsForMarket();

    /** 查询集市筛选可用分类（仅有上架商品的分类）。 */
    List<CategoryOptionDto> listCategoryOptionsForMarket();

    /** 查询集市筛选可用分类（按省份联动，仅有上架商品的分类）。 */
    List<CategoryOptionDto> listCategoryOptionsForMarket(Long provinceId);

    /** 查询全部省份选项（管理端商品表单下拉框）。 */
    List<ProvinceOptionDto> listAllProvinceOptions();

    /** 查询全部分类选项（管理端商品表单下拉框）。 */
    List<CategoryOptionDto> listAllCategoryOptions();

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

    /** 创建商品并设置省份关联。 */
    void createProduct(String operatorUsername, String name, String description,
                       BigDecimal price, int stock, ProductStatus status,
                       MultipartFile[] images, Long provinceId) throws IOException;

    /** 创建商品并支持手填省份名称（不存在则自动入库）。 */
    void createProduct(String operatorUsername, String name, String description,
                       BigDecimal price, int stock, ProductStatus status,
                       MultipartFile[] images, Long provinceId, String customProvinceName) throws IOException;

    /** 创建商品并支持手填分类名称（不存在则自动入库）。 */
    void createProduct(String operatorUsername, String name, String description,
                       BigDecimal price, int stock, ProductStatus status,
                       MultipartFile[] images, Long provinceId, String customProvinceName,
                       Long categoryId, String customCategoryName) throws IOException;

    /** 更新商品上下架状态。 */
    void updateStatus(Long id, ProductStatus newStatus);

    /** 更新商品信息并处理图片增删。 */
    void updateProduct(Long id, String name, String description, BigDecimal price, int stock,
                      ProductStatus status, MultipartFile[] newImages, String[] removeImageNames) throws IOException;

    /** 更新商品并维护省份关联。 */
    void updateProduct(Long id, String name, String description, BigDecimal price, int stock,
                      ProductStatus status, MultipartFile[] newImages, String[] removeImageNames,
                      Long provinceId) throws IOException;

    /** 更新商品并支持手填省份名称（不存在则自动入库）。 */
    void updateProduct(Long id, String name, String description, BigDecimal price, int stock,
                      ProductStatus status, MultipartFile[] newImages, String[] removeImageNames,
                      Long provinceId, String customProvinceName) throws IOException;

    /** 更新商品并支持手填分类名称（不存在则自动入库）。 */
    void updateProduct(Long id, String name, String description, BigDecimal price, int stock,
                      ProductStatus status, MultipartFile[] newImages, String[] removeImageNames,
                      Long provinceId, String customProvinceName,
                      Long categoryId, String customCategoryName) throws IOException;

    /** 删除商品。 */
    void deleteProduct(Long id);

    /**
     * 扣减库存等不经过本服务写库路径时，由订单侧调用以失效相关读缓存。
     */
    void evictAfterStockChange(Collection<Long> productIds);

    /**
     * 字典或展示维度变更后，主动失效集市查询缓存。
     */
    void evictMarketCaches();
}
