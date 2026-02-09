package com.example;

import com.example.shop.Product;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ShoppingCartTest {

    @Test
    void shouldCalculateTotalForSingleItem() {
        // Arrange
        ShoppingCart cart = new ShoppingCart();
        Product apple = new Product("Apple", 10.0);

        // Act
        cart.add(apple);

        // Assert
        assertThat(cart.getTotalPrice()).isEqualTo(10.0);
    }
}