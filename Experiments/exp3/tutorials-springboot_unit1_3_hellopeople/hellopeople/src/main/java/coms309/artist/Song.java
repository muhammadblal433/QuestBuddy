package coms309.artist;

import java.time.LocalDate;

public class Song {
    private Long id;                // server-assigned per-artist
    private String title;           // required, <= 80
    private LocalDate releaseDate;  // required (YYYY-MM-DD)
    private int plays;


    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title required");
        }
        if (title.length() > 80) {
            throw new IllegalArgumentException("title <= 80 chars");
        }
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            throw new IllegalArgumentException("releaseDate required");
        }
        this.releaseDate = releaseDate;
    }

    public int getPlays() {
        return plays;
    }
    public void setPlays(int plays) {
        if (plays < 0) {
            throw new IllegalArgumentException("plays >= 0");
        }
        this.plays = plays;
    }
}
