package coms309.artist;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Definition of the Artist Class for a app where users post music
 *
 * @author Ayaan Syed
 */
public class Artist {
    private Long id;              // server-assigned (stable for URLs)
    private String name;          // required, unique (case-insensitive), <= 60
    private Instant joined;       // default now
    private Genre genre;          // required
    private int plays;  // default 0

    // Discography: songs only, keyed by per-artist song id
    private final Map<Long, Song> songs = new LinkedHashMap<>(); // linkedHashMap preseves insertion order

    public Artist() {
        this.joined = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name required");
        }
        if (name.length() > 60) {
            throw new IllegalArgumentException("name <= 60 chars");
        }
        this.name = name;
    }

    public Instant getJoined() {
        return joined;
    }

    public void setJoined(Instant joined) {
        if (joined == null) {
            this.joined = Instant.now();
        } else {
            this.joined = joined;
        }
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        if (genre == null) {
            throw new IllegalArgumentException("genre required");
        }
        this.genre = genre;
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

    public Map<Long, Song> getSongs() {
        return songs;
    }
}
