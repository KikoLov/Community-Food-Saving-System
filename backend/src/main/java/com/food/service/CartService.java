package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.dto.OrderCreateDTO;
import com.food.entity.Cart;
import com.food.entity.Order;
import com.food.entity.Product;
import com.food.mapper.CartMapper;
import com.food.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final OrderService orderService;

    /**
     * 添加商品到购物车
     */
    @Transactional
    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        // 查询商品
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        if (product.getExpireDatetime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("商品已过期");
        }

        // 检查购物车是否已存在该商品
        Cart existCart = cartMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, productId)
        );

        if (existCart != null) {
            // 更新数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            cartMapper.updateById(existCart);
            return existCart;
        }

        // 新增购物车记录
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setProductName(product.getProductName());
        cart.setProductImage(product.getProductImage());
        cart.setDiscountPrice(product.getDiscountPrice());
        cart.setExpireDatetime(product.getExpireDatetime());
        cart.setStock(product.getStock());
        cartMapper.insert(cart);

        return cart;
    }

    /**
     * 获取用户购物车列表
     */
    public List<Cart> getCartList(Long userId) {
        return cartMapper.selectCartListByUser(userId);
    }

    /**
     * 更新购物车商品数量
     */
    public Cart updateCartQuantity(Long cartId, Long userId, Integer quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车记录不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }

        // 检查库存
        Product product = productMapper.selectById(cart.getProductId());
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        cart.setQuantity(quantity);
        cartMapper.updateById(cart);

        return cart;
    }

    /**
     * 删除购物车商品
     */
    public void deleteCartItem(Long cartId, Long userId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车记录不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }
        cartMapper.deleteById(cartId);
    }

    /**
     * 清空用户购物车
     */
    public void clearCart(Long userId) {
        cartMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
        );
    }

    /**
     * 购物车结算（可指定结算项；不传则默认结算全部）
     */
    @Transactional
    public List<Order> checkoutCart(Long userId, List<Long> cartIds) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId);
        if (cartIds != null && !cartIds.isEmpty()) {
            wrapper.in(Cart::getCartId, cartIds);
        }
        List<Cart> items = cartMapper.selectList(wrapper);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("购物车为空，无法结算");
        }

        List<Order> createdOrders = new ArrayList<>();
        for (Cart item : items) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("购物车存在非法数量，请刷新后重试");
            }
            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setProductId(item.getProductId());
            dto.setQuantity(item.getQuantity());
            createdOrders.add(orderService.createOrder(userId, dto));
        }

        List<Long> doneCartIds = items.stream().map(Cart::getCartId).toList();
        if (!doneCartIds.isEmpty()) {
            cartMapper.deleteBatchIds(doneCartIds);
        }
        return createdOrders;
    }
}
