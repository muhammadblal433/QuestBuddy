package coms309.artist;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class Artist {
    private Long id;              // server-assigned (stable for URLs)
    private String name;          // required, unique (case-insensitive), <= 60
    private Instant joined;       // default now
    private Genre genre;          // required
    private Long monthlyPlays;    // >= 0

    // Discography: songs only, keyed by per-artist song id
    private final Map<Long, Song> songs = new LinkedHashMap<>();

    public Artist() { this.joined = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (name.length() > 60) throw new IllegalArgumentException("name <= 60 chars");
        this.name = name;
    }

    public Instant getJoined() { return joined; }
    public void setJoined(Instant joined) { this.joined = (joined == null ? Instant.now() : joined); }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { if (genre == null) throw new IllegalArgumentException("genre required"); this.genre = genre; }

    public Long getMonthlyPlays() { return monthlyPlays; }
    public void setMonthlyPlays(Long monthlyPlays) {
        if (monthlyPlays != null && monthlyPlays < 0) throw new IllegalArgumentException("monthlyPlays >= 0");
        this.monthlyPlays = monthlyPlays == null ? 0L : monthlyPlays;
    }

    public Map<Long, Song> getSongs() { return songs; }
}
