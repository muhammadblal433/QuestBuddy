package coms309.music;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1")
public class ArtistController {

    // Store: id -> artist (source of truth)
    private final Map<Long, Artist> artists = new LinkedHashMap<>();
    // Index: lowercase name -> id (so we can map by name easily)
    private final Map<String, Long> nameIndex = new HashMap<>();

    private final AtomicLong artistSeq = new AtomicLong(1);
    private final Map<Long, AtomicLong> songSeq = new HashMap<>(); // per-artist counters

    // ---------- ARTISTS ----------
    @GetMapping("/artists")
    public List<Artist> listArtists(@RequestParam(value="shape", required=false) String shape) {
        if ("map".equalsIgnoreCase(shape)) {
            // name-keyed shape
            Map<String, Artist> byName = new LinkedHashMap<>();
            // preserve insertion order by iterating artists.values
            for (Artist a : artists.values()) byName.put(a.getName().toLowerCase(), a);
            // returning as list of a single map keeps it JSON object when serialized by Jackson? No,
            // easier: let spring serialize the map directly by returning ResponseEntity<Object>.
            throw new UnsupportedOperationException("Use /artists-map for name-keyed map.");
        }
        return new ArrayList<>(artists.values());
    }

    // Name-keyed map (nice for your “map should be name → info” requirement)
    @GetMapping("/artists-map")
    public Map<String, Artist> listArtistsAsNameMap() {
        Map<String, Artist> byName = new LinkedHashMap<>();
        for (Artist a : artists.values()) byName.put(a.getName().toLowerCase(), a);
        return byName;
    }

    @GetMapping("/artists/by-name/{name}")
    public ResponseEntity<Artist> getByName(@PathVariable String name) {
        Long id = nameIndex.get(name.toLowerCase());
        if (id == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(artists.get(id));
    }

    @PostMapping(value="/artists", consumes="application/json", produces="application/json")
    public ResponseEntity<Artist> createArtist(@RequestBody Artist body) {
        String key = body.getName().toLowerCase();
        if (nameIndex.containsKey(key)) return ResponseEntity.status(409).build();

        long id = artistSeq.getAndIncrement();
        body.setId(id);
        artists.put(id, body);
        nameIndex.put(key, id);
        songSeq.put(id, new AtomicLong(1));
        return ResponseEntity.created(URI.create("/api/v1/artists/" + id)).body(body);
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<Artist> getArtist(@PathVariable Long id) {
        Artist a = artists.get(id);
        return a == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(a);
    }

    @PutMapping(value="/artists/{id}", consumes="application/json", produces="application/json")
    public ResponseEntity<Artist> replaceArtist(@PathVariable Long id, @RequestBody Artist body) {
        if (!artists.containsKey(id)) return ResponseEntity.notFound().build();

        String newKey = body.getName().toLowerCase();
        Long existingId = nameIndex.get(newKey);
        if (existingId != null && !existingId.equals(id)) return ResponseEntity.status(409).build();

        String oldKey = artists.get(id).getName().toLowerCase();
        nameIndex.remove(oldKey);

        body.setId(id);
        artists.put(id, body);
        nameIndex.put(newKey, id);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/artists/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        Artist removed = artists.remove(id);
        if (removed == null) return ResponseEntity.notFound().build();
        nameIndex.remove(removed.getName().toLowerCase());
        songSeq.remove(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- SONGS (discography) ----------
    @GetMapping("/artists/{id}/songs")
    public ResponseEntity<List<Song>> listSongs(@PathVariable Long id) {
        Artist a = artists.get(id);
        if (a == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new ArrayList<>(a.getSongs().values()));
    }

    @PostMapping(value="/artists/{id}/songs", consumes="application/json", produces="application/json")
    public ResponseEntity<Song> createSong(@PathVariable Long id, @RequestBody Song body) {
        Artist a = artists.get(id);
        if (a == null) return ResponseEntity.notFound().build();

        long next = songSeq.get(id).getAndIncrement();
        body.setId(next);
        a.getSongs().put(next, body);
        return ResponseEntity.created(URI.create("/api/v1/artists/" + id + "/songs/" + next)).body(body);
    }

    @GetMapping("/artists/{id}/songs/{songId}")
    public ResponseEntity<Song> getSong(@PathVariable Long id, @PathVariable Long songId) {
        Artist a = artists.get(id);
        if (a == null) return ResponseEntity.notFound().build();
        Song s = a.getSongs().get(songId);
        return s == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(s);
    }

    @PutMapping(value="/artists/{id}/songs/{songId}", consumes="application/json", produces="application/json")
    public ResponseEntity<Song> replaceSong(@PathVariable Long id, @PathVariable Long songId, @RequestBody Song body) {
        Artist a = artists.get(id);
        if (a == null) return ResponseEntity.notFound().build();
        if (!a.getSongs().containsKey(songId)) return ResponseEntity.notFound().build();

        body.setId(songId);
        a.getSongs().put(songId, body);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/artists/{id}/songs/{songId}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id, @PathVariable Long songId) {
        Artist a = artists.get(id);
        if (a == null) return ResponseEntity.notFound().build();
        if (a.getSongs().remove(songId) == null) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }


    // Top artists by monthly plays
    @GetMapping("/artists/top")
    public List<Artist> topByPlays(@RequestParam(defaultValue="3") int n) {
        return artists.values().stream()
                .sorted(Comparator.comparing(Artist::getMonthlyPlays).reversed())
                .limit(Math.max(1, n))
                .toList();
    }

    // Latest song by release date
    @GetMapping("/artists/{id}/latest")
    public ResponseEntity<Map<String,Object>> latestSong(@PathVariable Long id) {
        Artist a = artists.get(id);
        if (a == null) return ResponseEntity.notFound().build();

        return a.getSongs().values().stream()
                .max(Comparator.comparing(Song::getReleaseDate))
                .<ResponseEntity<Map<String,Object>>>map(s ->
                        ResponseEntity.ok(Map.of(
                                "title", s.getTitle(),
                                "releaseDate", s.getReleaseDate().toString(),
                                "plays", s.getPlays(),
                                "likeRatio", s.getLikeRatio()
                        )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
