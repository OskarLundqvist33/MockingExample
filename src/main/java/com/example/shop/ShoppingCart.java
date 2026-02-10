package com.example.shop;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private final Map<Product, Integer> items = new HashMap<>();
    private double discountPercentage = 0.0; // Nytt fält

    public void add(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Produkten får inte vara null");
        }

        items.put(product, items.getOrDefault(product, 0) + 1);
    }

    public int getItemCount() {
        return items.size();
    }

    public void updateQuantity(Product product, int quantity) {
        if (quantity <= 0) {
            items.remove(product);
        } else {
            items.put(product, quantity);
        }
    }

    public void remove(Product product) {
        items.remove(product);
    }

    public void applyDiscount(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Rabatt måste vara mellan 0 och 100");
        }
        this.discountPercentage = percentage;
    }

    public double getTotalPrice() {
        double subtotal = items.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().price() * entry.getValue())
                .sum();

        return subtotal * (1 - (discountPercentage / 100.0));
    }

    public int getQuantityOf(Product product) {
        return items.getOrDefault(product, 0);
    }
}
