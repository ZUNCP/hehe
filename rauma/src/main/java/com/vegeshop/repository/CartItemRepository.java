package com.vegeshop.repository;

import com.vegeshop.model.CartItem;
import com.vegeshop.model.id.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    void deleteByCart_CartId(Integer cartId);
    List<CartItem> findByCart_CartId(Integer cartId);
}
