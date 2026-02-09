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

    @Test
    void shouldUpdateQuantityAndRemoveIfZero() {
        ShoppingCart cart = new ShoppingCart();
        Product banana = new Product("Banana", 5.0);
        cart.add(banana);

        // Ändra till 3 bananer
        cart.updateQuantity(banana, 3);
        assertThat(cart.getTotalPrice()).isEqualTo(15.0);

        // Ändra till 0 bananer (ska ta bort den)
        cart.updateQuantity(banana, 0);
        assertThat(cart.getItemCount()).isZero();
    }

    @Test
    void shouldRemoveProductEntirely() {
        ShoppingCart cart = new ShoppingCart();
        Product pear = new Product("Pear", 8.0);
        cart.add(pear);

        cart.remove(pear);
        assertThat(cart.getItemCount()).isZero();
    }

    @Test
    void shouldApplyDiscountPercentage() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(new Product("Laptop", 1000.0));

        cart.applyDiscount(20.0); // 20% rabatt

        assertThat(cart.getTotalPrice()).isEqualTo(800.0);
    }

    @Test
    void shouldThrowExceptionForInvalidDiscount() {
        ShoppingCart cart = new ShoppingCart();

        // AssertJ sätt att kolla exceptions
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> cart.applyDiscount(105.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mellan 0 och 100");
    }
}