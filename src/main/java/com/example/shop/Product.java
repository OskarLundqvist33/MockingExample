package com.example.shop;

public record Product(String name, double price) {
    public Product {
        if (price < 0) throw new IllegalArgumentException("Pris kan inte vara negativt");
    }
}