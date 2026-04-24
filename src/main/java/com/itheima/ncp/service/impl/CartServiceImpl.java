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
        // 读取当前用户全部购物车记录（数据库原始行）。
        List<CartItem> rows = cartItemMapper.findByUserId(userId);
        // 最终返回给前端展示的数据集合。
        List<CartLineDto> out = new ArrayList<CartLineDto>();
        // 逐条转换购物车记录 -> 展示 DTO。
        for (CartItem row : rows) {
            // 重新查商品，确保价格/库存/状态都是最新值。
            Product p = productMapper.findById(row.getProductId());
            // 商品不存在或已下架时，不再展示该条目。
            if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
                continue;
            }
            // 构建购物车展示行。
            CartLineDto d = new CartLineDto();
            // 购物车主键。
            d.setCartItemId(row.getId());
            // 商品主键。
            d.setProductId(p.getId());
            // 商品名称。
            d.setProductName(p.getName());
            // 取商品首图用于列表缩略图展示。
            d.setCoverStoredName(firstImageName(p.getImages()));
            // 单价来自商品当前价格。
            d.setUnitPrice(p.getPrice());
            // 购物车数量为空时按 1 兜底。
            int q = row.getQuantity() != null ? row.getQuantity() : 1;
            // 库存为空时按 0 处理。
            int stock = p.getStock() != null ? p.getStock() : 0;
            // 若购物车数量超过库存，自动下修到库存上限。
            if (q > stock) {
                q = stock;
                // 有库存时回写数据库，保持购物车数量与库存一致。
                if (q > 0) {
                    cartItemMapper.updateQuantity(row.getId(), q);
                } else {
                    // 已无库存则删除该条购物车项。
                    cartItemMapper.deleteByIdAndUserId(row.getId(), userId);
                    continue;
                }
            }
            // 写入最终展示数量。
            d.setQuantity(q);
            // 写入当前库存，便于前端限制加减按钮。
            d.setStock(stock);
            // 小计 = 单价 * 数量，并统一保留 2 位小数。
            BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(q)).setScale(2, RoundingMode.HALF_UP);
            d.setLineSubtotal(sub);
            // 加入返回集合。
            out.add(d);
        }
        // 返回已完成清洗和修正后的购物车数据。
        return out;
    }

    /**
     * 从商品图片 CSV 中提取第一张图片文件名。
     */
    private static String firstImageName(String imagesCsv) {
        // 无图片字段直接返回空。
        if (imagesCsv == null) {
            return null;
        }
        // 去除首尾空白，避免出现 "  a.jpg,b.jpg  " 的情况。
        String raw = imagesCsv.trim();
        // 空串等价于无图片。
        if (raw.isEmpty()) {
            return null;
        }
        // 按逗号拆分文件名。
        List<String> names = new ArrayList<String>();
        Collections.addAll(names, raw.split(","));
        // 返回第一个非空文件名作为封面。
        for (String n : names) {
            if (n != null && !n.trim().isEmpty()) {
                return n.trim();
            }
        }
        // 全是空片段时返回空。
        return null;
    }

    /**
     * 统计购物车总价。
     */
    @Override
    public BigDecimal sumCartTotal(Long userId) {
        // 从 0 开始累计总金额。
        BigDecimal sum = BigDecimal.ZERO;
        // 复用 listLines，保证金额计算和展示数据一致。
        for (CartLineDto line : listLines(userId)) {
            sum = sum.add(line.getLineSubtotal());
        }
        // 统一保留 2 位小数。
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 向购物车新增商品，存在同商品时累加数量并受库存约束。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProduct(Long userId, long productId, int quantity) {
        // 数量下限校验。
        if (quantity < 1) {
            throw new IllegalArgumentException("数量至少为 1");
        }
        // 校验商品是否存在且可售。
        Product p = productMapper.findById(productId);
        if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
            throw new IllegalArgumentException("商品不可购买");
        }
        // 读取可用库存。
        int stock = p.getStock() != null ? p.getStock() : 0;
        // 查找购物车中是否已存在该商品。
        CartItem existing = cartItemMapper.findByUserIdAndProductId(userId, productId);
        // 默认新增数量。
        int newQty = quantity;
        // 已存在则累加数量。
        if (existing != null) {
            newQty = (existing.getQuantity() != null ? existing.getQuantity() : 0) + quantity;
        }
        // 超库存时按库存上限截断。
        if (newQty > stock) {
            newQty = stock;
        }
        // 截断后若不足 1，说明无可售库存。
        if (newQty < 1) {
            throw new IllegalArgumentException("库存不足");
        }
        // 不存在则插入新行；存在则更新数量。
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
        // 数量下限校验。
        if (quantity < 1) {
            throw new IllegalArgumentException("数量至少为 1");
        }
        // 从当前用户购物车中定位目标条目，避免越权修改他人条目。
        CartItem row = null;
        for (CartItem c : cartItemMapper.findByUserId(userId)) {
            if (c.getId().equals(cartItemId)) {
                row = c;
                break;
            }
        }
        // 找不到条目直接报错。
        if (row == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        // 二次校验商品状态。
        Product p = productMapper.findById(row.getProductId());
        if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
            // 已下架商品从购物车移除，并提示用户。
            cartItemMapper.deleteByIdAndUserId(cartItemId, userId);
            throw new IllegalArgumentException("商品已下架，已从购物车移除");
        }
        // 按库存上限裁剪目标数量。
        int stock = p.getStock() != null ? p.getStock() : 0;
        int q = Math.min(quantity, stock);
        // 库存不足到 0 时移除该项。
        if (q < 1) {
            cartItemMapper.deleteByIdAndUserId(cartItemId, userId);
            throw new IllegalArgumentException("库存不足");
        }
        // 持久化更新数量。
        cartItemMapper.updateQuantity(cartItemId, q);
    }

    /**
     * 删除购物车指定条目。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeLine(Long userId, long cartItemId) {
        // 按 userId + cartItemId 双条件删除，防止误删他人数据。
        int n = cartItemMapper.deleteByIdAndUserId(cartItemId, userId);
        // 删除行数为 0 说明条目不存在或不属于当前用户。
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
        // 清理当前用户所有购物车数据，常用于下单成功后。
        cartItemMapper.deleteByUserId(userId);
    }
}
