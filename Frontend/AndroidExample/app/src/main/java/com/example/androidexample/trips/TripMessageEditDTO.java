package com.example.androidexample.trips;

public class TripMessageEditDTO {
    private String content;
    private long version;

    public TripMessageEditDTO(String content, long version) {
        this.content = content; this.version = version;
    }

    public String getContent() { return content; }
    public long getVersion() { return version; }
}
