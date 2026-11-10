package com.ecommerce;


public class Checker implements Islegit {

    @Override
    public boolean isFake(String productId) {
        System.out.println("[Checker] Checking authenticity of item: " + productId);
        return productId.toUpperCase().startsWith("FAKE");
    }
}
