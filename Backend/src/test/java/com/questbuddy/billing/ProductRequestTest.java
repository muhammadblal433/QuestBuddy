package com.questbuddy.billing;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProductRequestTest {

    @Test
    public void testGettersSetters() {
        ProductRequest pr = new ProductRequest();
        pr.setAmount(100L);
        pr.setQuantity(2L);
        pr.setProductName("Premium");
        pr.setCurrency("usd");

        assertEquals(Long.valueOf(100L), pr.getAmount());
        assertEquals(Long.valueOf(2L), pr.getQuantity());
        assertEquals("Premium", pr.getProductName());
        assertEquals("usd", pr.getCurrency());
    }

    @Test
    public void testConstructor() {
        ProductRequest pr = new ProductRequest(200L, 1L, "Gold", "eur");

        assertEquals(Long.valueOf(200L), pr.getAmount());
        assertEquals(Long.valueOf(1L), pr.getQuantity());
        assertEquals("Gold", pr.getProductName());
        assertEquals("eur", pr.getCurrency());
    }
}
