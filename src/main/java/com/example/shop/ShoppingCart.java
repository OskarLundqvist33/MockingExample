package com.example.shop;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private final List<Product> products = new ArrayList<>();

    public void add(Product product) {
        products.add(product);
    }

    public double getTotalPrice() {
        return products.stream().mapToDouble(Product::price).sum();
    }
}