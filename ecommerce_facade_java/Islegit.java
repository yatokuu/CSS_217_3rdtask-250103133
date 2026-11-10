package com.ecommerce;

/**
 * Contract for verifying that a product is genuine.
 */
public interface Islegit {

    /**
     * @param productId The unique identifier of the item.
     * @return true if the product is fake, false if genuine.
     */
    boolean isFake(String productId);
}
