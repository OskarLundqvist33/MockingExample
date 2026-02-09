package com.example.shop;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    // Map av Produkt -> Antal
    private final Map<Product, Integer> items = new HashMap<>();

    public void add(Product product) {
        items.put(product, items.getOrDefault(product, 0) + 1);
    }

    public double getTotalPrice() {
        return items.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().price() * entry.getValue())
                .sum();
    }

    public int getItemCount() {
        return items.size();
    }
}