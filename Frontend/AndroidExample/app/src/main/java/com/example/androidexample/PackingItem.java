package com.example.androidexample;

//data model for each packing list item
public class PackingItem {
    private long id;
    private String name;

    public PackingItem(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() { return id; }
    public String getName() { return name; }
}
