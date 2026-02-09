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

    @Test
    void shouldGroupDuplicateItemsAndSumPrice() {
        ShoppingCart cart = new ShoppingCart();
        Product apple = new Product("Apple", 10.0);

        cart.add(apple);
        cart.add(apple); // Lägger till igen

        assertThat(cart.getTotalPrice()).isEqualTo(20.0);
        assertThat(cart.getItemCount()).isEqualTo(1); // Fortfarande bara 1 "rad" i korgen
    }
}