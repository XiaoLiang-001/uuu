package com.itheima.ncp.service.impl;

import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import com.itheima.ncp.entity.user.User;
import com.itheima.ncp.mapper.product.ProductMapper;
import com.itheima.ncp.service.product.FileStorageService;
import com.itheima.ncp.service.product.ProductService;
import com.itheima.ncp.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商品服务实现，统一处理商品查询、保存、上下架及图片关联校验。
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public ProductServiceImpl(ProductMapper productMapper, UserService userService, FileStorageService fileStorageService) {
        this.productMapper = productMapper;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 查询全部商品（管理视角）。
     */
    @Override
    public List<Product> listAll() {
        return productMapper.findAllOrderByIdDesc();
    }

    /**
     * 管理端列表查询：按关键字和状态筛选商品。
     */
    @Override
    public List<Product> listForAdmin(String keyword, ProductStatus status) {
        String kw = keyword == null ? "" : keyword.trim();
        String st = status == null ? null : status.name();
        if (kw.isEmpty() && (st == null || st.isEmpty())) {
            return productMapper.findAllOrderByIdDesc();
        }
        return productMapper.findByKeywordAndStatusOrderByIdDesc(kw.isEmpty() ? null : kw, st);
    }

    /**
     * 查询全部上架商品。
     */
    @Override
    public List<Product> listOnShelf() {
        return productMapper.findOnShelfOrderByIdDesc();
    }

    /**
     * 统计当前上架商品总数。
     */
    @Override
    public int countOnShelf() {
        return productMapper.countOnShelf();
    }

    /**
     * 按分页参数查询上架商品。
     */
    @Override
    public List<Product> listOnShelfPage(int offset, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        int o = offset < 0 ? 0 : offset;
        return productMapper.findOnShelfOrderByIdDescPaged(o, limit);
    }

    /**
     * 根据主键查询商品。
     */
    @Override
    public Product getById(Long id) {
        return productMapper.findById(id);
    }

    /**
     * 将商品图片字段按逗号拆分为文件名列表。
     */
    @Override
    public List<String> splitStoredImageNames(Product p) {
        if (p == null || p.getImages() == null) {
            return Collections.emptyList();
        }
        String raw = p.getImages().trim();
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<String>();
        for (String s : raw.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    /**
     * 批量提取商品首图映射（商品ID -> 首图文件名）。
     */
    @Override
    public Map<Long, String> mapFirstImageStoredByProducts(List<Product> products) {
        Map<Long, String> map = new HashMap<Long, String>();
        if (products == null) {
            return map;
        }
        for (Product p : products) {
            if (p == null || p.getId() == null) {
                continue;
            }
            List<String> names = splitStoredImageNames(p);
            if (!names.isEmpty()) {
                map.put(p.getId(), names.get(0));
            }
        }
        return map;
    }

    /**
     * 判断文件名是否已被任一商品引用。
     */
    @Override
    public boolean isStoredImageRegistered(String storedName) {
        if (storedName == null || storedName.trim().isEmpty()) {
            return false;
        }
        return productMapper.countHavingStoredImage(storedName.trim()) > 0;
    }

    /**
     * 创建商品并保存上传图片。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProduct(String operatorUsername, String name, String description,
                              BigDecimal price, int stock, ProductStatus status,
                              MultipartFile[] images) throws IOException {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格无效");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }
        Long createdBy = null;
        User u = userService.getByUsername(operatorUsername == null ? "" : operatorUsername.trim());
        if (u != null) {
            createdBy = u.getId();
        }

        List<String> storedNames = new ArrayList<String>();
        if (images != null) {
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                if (!fileStorageService.isAllowedImage(file)) {
                    throw new IllegalArgumentException("存在不支持的图片文件：" + file.getOriginalFilename());
                }
                storedNames.add(fileStorageService.saveProductImage(file));
            }
        }

        String imagesCsv = storedNames.isEmpty() ? null : String.join(",", storedNames);

        Product p = new Product();
        p.setName(n);
        p.setDescription(description == null ? null : description.trim());
        p.setPrice(price);
        p.setStock(stock);
        p.setStatus(status == null ? ProductStatus.ON_SHELF : status);
        p.setImages(imagesCsv);
        p.setCreatedBy(createdBy);
        productMapper.insert(p);
    }

    /**
     * 更新商品上下架状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, ProductStatus newStatus) {
        if (id == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("状态无效");
        }
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        productMapper.updateStatusByRow(id, newStatus.name());
    }

    /**
     * 更新商品基础信息及图片增删。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, String name, String description, BigDecimal price, int stock,
                              ProductStatus status, MultipartFile[] newImages, String[] removeImageNames) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格无效");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }
        if (status == null) {
            throw new IllegalArgumentException("状态无效");
        }

        List<String> names = new ArrayList<String>(splitStoredImageNames(existing));
        Set<String> toRemove = new HashSet<String>();
        if (removeImageNames != null) {
            for (String r : removeImageNames) {
                if (r == null || r.trim().isEmpty()) {
                    continue;
                }
                String t = r.trim();
                if (!names.contains(t)) {
                    throw new IllegalArgumentException("无效的图片项：" + t);
                }
                toRemove.add(t);
            }
        }
        for (String t : toRemove) {
            names.remove(t);
        }

        if (newImages != null) {
            for (MultipartFile file : newImages) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                if (!fileStorageService.isAllowedImage(file)) {
                    throw new IllegalArgumentException("存在不支持的图片文件：" + file.getOriginalFilename());
                }
                names.add(fileStorageService.saveProductImage(file));
            }
        }

        String imagesCsv;
        if (names.isEmpty()) {
            imagesCsv = null;
        } else {
            imagesCsv = String.join(",", names);
        }

        Product p = new Product();
        p.setId(id);
        p.setName(n);
        p.setDescription(description == null ? null : description.trim());
        p.setPrice(price);
        p.setStock(stock);
        p.setStatus(status);
        p.setImages(imagesCsv);
        productMapper.updateById(p);

        for (String removed : toRemove) {
            if (productMapper.countHavingStoredImage(removed) == 0) {
                fileStorageService.deleteStoredSilently(removed);
            }
        }
    }

    /**
     * 删除商品及其未被其他商品引用的图片文件。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (existing.getStatus() != ProductStatus.OFF_SHELF) {
            throw new IllegalArgumentException("仅已下架的商品可以删除");
        }
        List<String> imageNames = new ArrayList<String>(splitStoredImageNames(existing));
        int n = productMapper.deleteById(id);
        if (n == 0) {
            throw new IllegalArgumentException("删除失败");
        }
        for (String name : imageNames) {
            fileStorageService.deleteStoredSilently(name);
        }
    }
}
