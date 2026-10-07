package com.vegeshop.service;

import com.vegeshop.dto.CartItemRequestDTO;
import com.vegeshop.dto.CartResponseDTO;

public interface CartService {
    CartResponseDTO getMyCart();
    CartResponseDTO addItem(CartItemRequestDTO request);
    CartResponseDTO updateItem(Integer productId, CartItemRequestDTO request);
    CartResponseDTO removeItem(Integer productId);
    void clear();
}
