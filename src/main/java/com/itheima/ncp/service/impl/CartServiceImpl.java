package com.itheima.ncp.service.impl;

import com.itheima.ncp.dto.CartLineDto;
import com.itheima.ncp.entity.product.Product;
import com.itheima.ncp.entity.product.ProductStatus;
import com.itheima.ncp.entity.shop.CartItem;
import com.itheima.ncp.mapper.cart.CartItemMapper;
import com.itheima.ncp.mapper.product.ProductMapper;
import com.itheima.ncp.service.shop.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 购物车服务实现，封装购物车条目查询、金额计算与增删改逻辑。
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartServiceImpl(CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    /**
     * 查询用户购物车并按商品实时状态组装展示行数据。
     */
    @Override
    public List<CartLineDto> listLines(Long userId) {
        List<CartItem> rows = cartItemMapper.findByUserId(userId);
        List<CartLineDto> out = new ArrayList<CartLineDto>();
        for (CartItem row : rows) {
            Product p = productMapper.findById(row.getProductId());
            if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
                continue;
            }
            CartLineDto d = new CartLineDto();
            d.setCartItemId(row.getId());
            d.setProductId(p.getId());
            d.setProductName(p.getName());
            d.setCoverStoredName(firstImageName(p.getImages()));
            d.setUnitPrice(p.getPrice());
            int q = row.getQuantity() != null ? row.getQuantity() : 1;
            int stock = p.getStock() != null ? p.getStock() : 0;
            if (q > stock) {
                q = stock;
                if (q > 0) {
                    cartItemMapper.updateQuantity(row.getId(), q);
                } else {
                    cartItemMapper.deleteByIdAndUserId(row.getId(), userId);
                    continue;
                }
            }
            d.setQuantity(q);
            d.setStock(stock);
            BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(q)).setScale(2, RoundingMode.HALF_UP);
            d.setLineSubtotal(sub);
            out.add(d);
        }
        return out;
    }

    /**
     * 从商品图片 CSV 中提取第一张图片文件名。
     */
    private static String firstImageName(String imagesCsv) {
        if (imagesCsv == null) {
            return null;
        }
        String raw = imagesCsv.trim();
        if (raw.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<String>();
        Collections.addAll(names, raw.split(","));
        for (String n : names) {
            if (n != null && !n.trim().isEmpty()) {
                return n.trim();
            }
        }
        return null;
    }

    /**
     * 统计购物车总价。
     */
    @Override
    public BigDecimal sumCartTotal(Long userId) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CartLineDto line : listLines(userId)) {
            sum = sum.add(line.getLineSubtotal());
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 向购物车新增商品，存在同商品时累加数量并受库存约束。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProduct(Long userId, long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("数量至少为 1");
        }
        Product p = productMapper.findById(productId);
        if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
            throw new IllegalArgumentException("商品不可购买");
        }
        int stock = p.getStock() != null ? p.getStock() : 0;
        CartItem existing = cartItemMapper.findByUserIdAndProductId(userId, productId);
        int newQty = quantity;
        if (existing != null) {
            newQty = (existing.getQuantity() != null ? existing.getQuantity() : 0) + quantity;
        }
        if (newQty > stock) {
            newQty = stock;
        }
        if (newQty < 1) {
            throw new IllegalArgumentException("库存不足");
        }
        if (existing == null) {
            CartItem row = new CartItem();
            row.setUserId(userId);
            row.setProductId(productId);
            row.setQuantity(newQty);
            cartItemMapper.insert(row);
        } else {
            cartItemMapper.updateQuantity(existing.getId(), newQty);
        }
    }

    /**
     * 更新购物车某条目的购买数量。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long userId, long cartItemId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("数量至少为 1");
        }
        CartItem row = null;
        for (CartItem c : cartItemMapper.findByUserId(userId)) {
            if (c.getId().equals(cartItemId)) {
                row = c;
                break;
            }
        }
        if (row == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        Product p = productMapper.findById(row.getProductId());
        if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
            cartItemMapper.deleteByIdAndUserId(cartItemId, userId);
            throw new IllegalArgumentException("商品已下架，已从购物车移除");
        }
        int stock = p.getStock() != null ? p.getStock() : 0;
        int q = Math.min(quantity, stock);
        if (q < 1) {
            cartItemMapper.deleteByIdAndUserId(cartItemId, userId);
            throw new IllegalArgumentException("库存不足");
        }
        cartItemMapper.updateQuantity(cartItemId, q);
    }

    /**
     * 删除购物车指定条目。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeLine(Long userId, long cartItemId) {
        int n = cartItemMapper.deleteByIdAndUserId(cartItemId, userId);
        if (n == 0) {
            throw new IllegalArgumentException("购物车项不存在");
        }
    }

    /**
     * 清空指定用户购物车。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        cartItemMapper.deleteByUserId(userId);
    }
}
