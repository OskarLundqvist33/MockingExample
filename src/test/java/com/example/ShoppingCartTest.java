package com.example;

import com.example.shop.Product;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;


class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    @DisplayName("Ska beräkna totalpris för en vara")
    void shouldCalculateTotalForSingleItem() {
        cart.add(new Product("Apple", 10.0));
        assertThat(cart.getTotalPrice()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Ska gruppera dubbletter och summera pris")
    void shouldGroupDuplicateItemsAndSumPrice() {
        Product apple = new Product("Apple", 10.0);
        cart.add(apple);
        cart.add(apple);

        assertThat(cart.getTotalPrice()).isEqualTo(20.0);
        assertThat(cart.getItemCount()).isEqualTo(1);
        assertThat(cart.getQuantityOf(apple)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ska uppdatera kvantitet")
    void shouldUpdateQuantity() {
        Product banana = new Product("Banana", 5.0);
        cart.add(banana);
        cart.updateQuantity(banana, 5);

        assertThat(cart.getTotalPrice()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Ska ta bort vara om kvantitet sätts till 0")
    void shouldRemoveItemIfQuantityIsZero() {
        Product banana = new Product("Banana", 5.0);
        cart.add(banana);
        cart.updateQuantity(banana, 0);

        assertThat(cart.getItemCount()).isZero();
    }

    @Test
    @DisplayName("Ska applicera rabatt på totalen")
    void shouldApplyDiscount() {
        cart.add(new Product("Laptop", 1000.0));
        cart.applyDiscount(20.0); // 20%
        assertThat(cart.getTotalPrice()).isEqualTo(800.0);
    }

    @Test
    @DisplayName("Ska kasta exception vid ogiltig rabatt")
    void shouldThrowOnInvalidDiscount() {
        assertThatThrownBy(() -> cart.applyDiscount(110.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Ska kasta exception vid null produkt")
    void shouldThrowOnNullProduct() {
        assertThatThrownBy(() -> cart.add(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
