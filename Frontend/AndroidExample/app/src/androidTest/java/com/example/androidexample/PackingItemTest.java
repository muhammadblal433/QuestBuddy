package com.example.androidexample;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.androidexample.packing.PackingItem;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PackingItemTest {

    @Test
    public void packingItem_gettersReturnCorrectValues() {
        PackingItem item = new PackingItem(10, "Socks");

        assertEquals(10, item.getId());
        assertEquals("Socks", item.getName());
    }

    @Test
    public void packingItem_handlesDifferentValues() {
        PackingItem item = new PackingItem(999, "Toothbrush");

        assertEquals(999, item.getId());
        assertEquals("Toothbrush", item.getName());
    }
}