package com.example.androidexample.budget;

import static org.junit.Assert.*;
import org.junit.Test;

public class SplitTest {

    @Test
    public void testConstructorAndGetters() {
        Split s = new Split("alice", 50.0, 20.0, 30.0);

        assertEquals("alice", s.getUsername());
        assertEquals(50.0, s.getShareAmount(), 0.0001);
        assertEquals(20.0, s.getPaidAmount(), 0.0001);
        assertEquals(30.0, s.getBalance(), 0.0001);
    }

    @Test
    public void testSetShareAmount() {
        Split s = new Split("bob", 10.0, 5.0, 5.0);

        s.setShareAmount(99.0);
        assertEquals(99.0, s.getShareAmount(), 0.0001);
    }

    @Test
    public void testSetPaidAmount() {
        Split s = new Split("charlie", 20.0, 10.0, 10.0);

        s.setPaidAmount(42.0);
        assertEquals(42.0, s.getPaidAmount(), 0.0001);
    }

    @Test
    public void testZeroValues() {
        Split s = new Split("zero", 0.0, 0.0, 0.0);

        assertEquals(0.0, s.getShareAmount(), 0.0001);
        assertEquals(0.0, s.getPaidAmount(), 0.0001);
        assertEquals(0.0, s.getBalance(), 0.0001);
    }

    @Test
    public void testNegativeValues() {
        Split s = new Split("neg", -10.0, -3.0, -7.0);

        assertEquals(-10.0, s.getShareAmount(), 0.0001);
        assertEquals(-3.0, s.getPaidAmount(), 0.0001);
        assertEquals(-7.0, s.getBalance(), 0.0001);
    }
}
