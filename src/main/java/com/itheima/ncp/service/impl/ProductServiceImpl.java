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
        // 管理侧默认按 ID 倒序展示最新商品。
        return productMapper.findAllOrderByIdDesc();
    }

    /**
     * 管理端列表查询：按关键字和状态筛选商品。
     */
    @Override
    public List<Product> listForAdmin(String keyword, ProductStatus status) {
        // 关键字空值统一转空串。
        String kw = keyword == null ? "" : keyword.trim();
        // 枚举状态转为数据库查询用字符串。
        String st = status == null ? null : status.name();
        // 两个筛选都为空时走全量查询 SQL。
        if (kw.isEmpty() && (st == null || st.isEmpty())) {
            return productMapper.findAllOrderByIdDesc();
        }
        // 否则走条件查询 SQL。
        return productMapper.findByKeywordAndStatusOrderByIdDesc(kw.isEmpty() ? null : kw, st);
    }

    /**
     * 查询全部上架商品。
     */
    @Override
    public List<Product> listOnShelf() {
        // 用户侧仅展示在售商品。
        return productMapper.findOnShelfOrderByIdDesc();
    }

    /**
     * 统计当前上架商品总数。
     */
    @Override
    public int countOnShelf() {
        // 分页总数统计。
        return productMapper.countOnShelf();
    }

    /**
     * 按分页参数查询上架商品。
     */
    @Override
    public List<Product> listOnShelfPage(int offset, int limit) {
        // 非法页大小直接返回空列表。
        if (limit <= 0) {
            return Collections.emptyList();
        }
        // 偏移量最小为 0。
        int o = offset < 0 ? 0 : offset;
        // 执行分页查询。
        return productMapper.findOnShelfOrderByIdDescPaged(o, limit);
    }

    /**
     * 根据主键查询商品。
     */
    @Override
    public Product getById(Long id) {
        // 按主键查询商品。
        return productMapper.findById(id);
    }

    /**
     * 将商品图片字段按逗号拆分为文件名列表。
     */
    @Override
    public List<String> splitStoredImageNames(Product p) {
        // 商品或图片字段为空时返回空集合。
        if (p == null || p.getImages() == null) {
            return Collections.emptyList();
        }
        // 去首尾空格后再拆分。
        String raw = p.getImages().trim();
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        // 顺序保留图片名，供前端轮播/封面使用。
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
        // 结果映射：商品ID -> 首图文件名。
        Map<Long, String> map = new HashMap<Long, String>();
        if (products == null) {
            return map;
        }
        // 逐个提取首图。
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
        // 空文件名直接视为未注册。
        if (storedName == null || storedName.trim().isEmpty()) {
            return false;
        }
        // 查询该文件名是否仍被任一商品引用。
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
        // 商品名去空白后校验非空。
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        // 价格必须非负。
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格无效");
        }
        // 库存不能为负。
        if (stock < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }
        // 默认可为空（未知操作者）；识别到用户则记录 createdBy。
        Long createdBy = null;
        User u = userService.getByUsername(operatorUsername == null ? "" : operatorUsername.trim());
        if (u != null) {
            createdBy = u.getId();
        }

        // 收集上传后生成的存储文件名。
        List<String> storedNames = new ArrayList<String>();
        // 先校验后保存，避免混入非法文件格式。
        if (images != null) {
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                if (!fileStorageService.isAllowedImage(file)) {
                    throw new IllegalArgumentException("存在不支持的图片文件：" + file.getOriginalFilename());
                }
                // 保存文件并记录其存储名。
                storedNames.add(fileStorageService.saveProductImage(file));
            }
        }

        // 多图以 CSV 持久化。
        String imagesCsv = storedNames.isEmpty() ? null : String.join(",", storedNames);

        // 组装并落库商品实体。
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
        // 主键不能为空。
        if (id == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        // 目标状态不能为空。
        if (newStatus == null) {
            throw new IllegalArgumentException("状态无效");
        }
        // 必须先确认商品存在。
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        // 仅更新状态字段。
        productMapper.updateStatusByRow(id, newStatus.name());
    }

    /**
     * 更新商品基础信息及图片增删。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, String name, String description, BigDecimal price, int stock,
                              ProductStatus status, MultipartFile[] newImages, String[] removeImageNames) throws IOException {
        // 基础存在性校验。
        if (id == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        // 重新校验表单字段。
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

        // 先复制一份当前图片列表做可变操作。
        List<String> names = new ArrayList<String>(splitStoredImageNames(existing));
        // 待删除图片集合（用 Set 去重）。
        Set<String> toRemove = new HashSet<String>();
        // 删除清单必须来自原有图片，防止前端伪造任意文件名删除。
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
        // 从当前列表中移除待删项。
        for (String t : toRemove) {
            names.remove(t);
        }

        // 新增图片追加到现有图片列表末尾。
        if (newImages != null) {
            for (MultipartFile file : newImages) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                if (!fileStorageService.isAllowedImage(file)) {
                    throw new IllegalArgumentException("存在不支持的图片文件：" + file.getOriginalFilename());
                }
                // 新图写盘后追加到图片列表。
                names.add(fileStorageService.saveProductImage(file));
            }
        }

        // 重新拼接图片 CSV。
        String imagesCsv;
        if (names.isEmpty()) {
            imagesCsv = null;
        } else {
            imagesCsv = String.join(",", names);
        }

        // 组装 patch 并更新数据库。
        Product p = new Product();
        p.setId(id);
        p.setName(n);
        p.setDescription(description == null ? null : description.trim());
        p.setPrice(price);
        p.setStock(stock);
        p.setStatus(status);
        p.setImages(imagesCsv);
        productMapper.updateById(p);

        // 仅当图片已无数据库引用时才删除物理文件，避免误删被复用图片。
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
        // 主键与存在性校验。
        if (id == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        // 仅允许删除已下架商品，避免误删在售数据。
        if (existing.getStatus() != ProductStatus.OFF_SHELF) {
            throw new IllegalArgumentException("仅已下架的商品可以删除");
        }
        // 先记录图片名，供删除后清理文件。
        List<String> imageNames = new ArrayList<String>(splitStoredImageNames(existing));
        // 删除商品主记录。
        int n = productMapper.deleteById(id);
        if (n == 0) {
            throw new IllegalArgumentException("删除失败");
        }
        // 商品删除后清理关联图片文件。
        for (String name : imageNames) {
            fileStorageService.deleteStoredSilently(name);
        }
    }
}
