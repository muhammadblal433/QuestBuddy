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
    private final Map<Long, Artist> artists = new LinkedHashMap<>(); // Insert Order Preservation

    // Index: lowercase name -> id (so we can map by name easily)
    private final Map<String, Long> nameIndex = new HashMap<>();

    private final AtomicLong artistSeq = new AtomicLong(1);
    private final Map<Long, AtomicLong> songSeq = new HashMap<>(); // per-artist counters


    // Artist Related
    /**
     * LIST
     *
      * @param shape
     * @return all artists as an array
     */
    @GetMapping("/artists")
    public List<Artist> listArtists(@RequestParam(value="shape", required=false) String shape) {
        if ("map".equalsIgnoreCase(shape)) {
            // name-keyed shape
            Map<String, Artist> byName = new LinkedHashMap<>();
            // preserve insertion order by iterating artists.values
            for (Artist a : artists.values()) {
                byName.put(a.getName().toLowerCase(), a);
            }
            // returning as list of a single map keeps it JSON object when serialized by Jackson? No,
            // easier: let spring serialize the map directly by returning ResponseEntity<Object>.
            throw new UnsupportedOperationException("Use /artists-map for name-keyed map.");
        }
        return new ArrayList<>(artists.values());
    }

    /**
     * LIST
     *
     * @return artists as a name-keyed map
     */
    // Name-keyed map (nice for your “map should be name → info” requirement)
    @GetMapping("/artists-map")
    public Map<String, Artist> listArtistsAsNameMap() {
        Map<String, Artist> byName = new LinkedHashMap<>();
        for (Artist a : artists.values()) {
            byName.put(a.getName().toLowerCase(), a);
        }
        return byName;
    }

    /**
     * READ
     *
     * @param name
     * @return one artist by name
     */
    @GetMapping("/artists/by-name/{name}")
    public ResponseEntity<Artist> getByName(@PathVariable String name) {
        Long id = nameIndex.get(name.toLowerCase());
        if (id == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(artists.get(id));
    }

    /**
     * CREATE
     *
     * enforces unique names across artists
     * @param name
     */
    @PostMapping(value="/artists", consumes="application/json", produces="application/json")
    public ResponseEntity<Artist> createArtist(@RequestBody Artist body) {
        String key = body.getName().toLowerCase();
        if (nameIndex.containsKey(key)) {
            return ResponseEntity.status(409).build();
        }

        long id = artistSeq.getAndIncrement();
        body.setId(id);
        artists.put(id, body);
        nameIndex.put(key, id);
        songSeq.put(id, new AtomicLong(1));
        return ResponseEntity.created(URI.create("/api/v1/artists/" + id)).body(body);
    }

    /**
     * CREATE
     *
     * enforces unique names across artists (in batches)
     * @param name
     */
    @PostMapping(value="/artists/batch", consumes="application/json", produces="application/json")
    public ResponseEntity<List<Artist>> createArtistsBatch(@RequestBody List<Artist> bodies) {
        List<Artist> created = new ArrayList<>();
        for (Artist body : bodies) {
            String key = body.getName().toLowerCase();
            if (nameIndex.containsKey(key)) {
                // skip duplicates, or return 409—your call
                continue;
            }
            long id = artistSeq.getAndIncrement();
            body.setId(id);
            artists.put(id, body);
            nameIndex.put(key, id);
            songSeq.put(id, new AtomicLong(1));
            created.add(body);
        }
        return ResponseEntity.created(URI.create("/api/v1/artists")).body(created);
    }

    /**
     * READ
     *
     * @param id
     * @return fetches the artist by the given numeric id.
     */
    @GetMapping("/artists/{id}")
    public ResponseEntity<Artist> getArtist(@PathVariable Long id) {
        Artist a = artists.get(id);
        if (a == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(a);
        }
    }

    /**
     * UPDATE
     *
     * full replace of artist; preserves unique name constraint
     * @param id
     * @param body
     */
    @PutMapping(value="/artists/{id}", consumes="application/json", produces="application/json")
    public ResponseEntity<Artist> replaceArtist(@PathVariable Long id, @RequestBody Artist body) {
        if (!artists.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        String newKey = body.getName().toLowerCase();
        Long existingId = nameIndex.get(newKey);
        if (existingId != null && !existingId.equals(id)) {
            return ResponseEntity.status(409).build();
        }

        String oldKey = artists.get(id).getName().toLowerCase();
        nameIndex.remove(oldKey);

        body.setId(id);
        artists.put(id, body);
        nameIndex.put(newKey, id);
        return ResponseEntity.ok(body);
    }

    /**
     * DELETE
     *
     * deletes an artist by id
     * @param id
     */
    @DeleteMapping("/artists/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        Artist removed = artists.remove(id);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        nameIndex.remove(removed.getName().toLowerCase());
        songSeq.remove(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * LIST
     *
     * Lists all songs for a given artist id
     * @param id
     * @return Array of all songs by an artist (by id)
     */
    // Song Related
    @GetMapping("/artists/{id}/songs")
    public ResponseEntity<List<Song>> listSongs(@PathVariable Long id) {
        Artist a = artists.get(id);
        if (a == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ArrayList<>(a.getSongs().values()));
    }

    /**
     * CREATE
     *
     * Creates a song under an artist; assigns per-artist song id.
     * @param id
     * @param body
     */
    @PostMapping(value="/artists/{id}/songs", consumes="application/json", produces="application/json")
    public ResponseEntity<Song> createSong(@PathVariable Long id, @RequestBody Song body) {
        Artist a = artists.get(id);
        if (a == null) {
            return ResponseEntity.notFound().build();
        }

        long next = songSeq.get(id).getAndIncrement();
        body.setId(next);
        a.getSongs().put(next, body);
        return ResponseEntity.created(URI.create("/api/v1/artists/" + id + "/songs/" + next)).body(body);
    }

    /**
     * CREATE
     *
     *      * Creates a song under an artist; assigns per-artist song id (by batch)
     * @param id
     * @param bodies
     */
    @PostMapping(value="/artists/{id}/songs/batch", consumes="application/json", produces="application/json")
    public ResponseEntity<List<Song>> createSongsBatch(@PathVariable Long id, @RequestBody List<Song> bodies) {
        Artist a = artists.get(id);
        if (a == null) return ResponseEntity.notFound().build();

        AtomicLong counter = songSeq.get(id);
        if (counter == null) {
            counter = new AtomicLong(1);
            songSeq.put(id, counter);
        }

        List<Song> created = new ArrayList<>();
        for (Song body : bodies) {
            long next = counter.getAndIncrement();
            body.setId(next);
            a.getSongs().put(next, body);
            created.add(body);
        }
        return ResponseEntity.created(URI.create("/api/v1/artists/" + id + "/songs")).body(created);
    }

    /**
     * READ
     *
     * Reads a single song under an artist.
     * @param id
     * @param songId
     */
    @GetMapping("/artists/{id}/songs/{songId}")
    public ResponseEntity<Song> getSong(@PathVariable Long id, @PathVariable Long songId) {
        // finding artist
        Artist a = artists.get(id);
        if (a == null) {
            return ResponseEntity.notFound().build();
        }

        // find the song under that artist
        Song s = a.getSongs().get(songId);
        if (s == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(s);
    }

    /**
     * UPDATE
     *
     * Full replace of a song under an artist.
     * @param id
     * @param songId
     * @param body
     */
    @PutMapping(value="/artists/{id}/songs/{songId}", consumes="application/json", produces="application/json")
    public ResponseEntity<Song> replaceSong(@PathVariable Long id, @PathVariable Long songId, @RequestBody Song body) {
        Artist a = artists.get(id);
        if (a == null) {
            return ResponseEntity.notFound().build();
        }
        if (!a.getSongs().containsKey(songId)) {
            return ResponseEntity.notFound().build();
        }

        body.setId(songId);
        a.getSongs().put(songId, body);
        return ResponseEntity.ok(body);
    }

    /**
     * DELETE
     *
     * Deletes a song under an artist.
     * @param id
     * @param songId
     */
    @DeleteMapping("/artists/{id}/songs/{songId}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id, @PathVariable Long songId) {
        Artist a = artists.get(id);
        if (a == null) {
            return ResponseEntity.notFound().build();
        }
        if (a.getSongs().remove(songId) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }


    // Special

    /**
     * LIST
     *
     * @param n
     * @return Array of top n artists by plays
     */
    @GetMapping("/artists/top")
    public List<Artist> topByPlays(@RequestParam(defaultValue = "3") int n) {
        // ensure n >= 1
        int limit = n;
        if (limit < 1) {
            limit = 1;
        }

        // copy artists to a mutable list
        List<Artist> list = new ArrayList<>(artists.values());

        // sort by plays descending
        list.sort((a, b) -> {
            int pa = a.getPlays();
            int pb = b.getPlays();
            return Integer.compare(pb, pa); // descending
        });

        // trim to top-N
        if (list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        } else {
            return list;
        }
    }

    /**
     * READ
     *
     * @param id
     * @return the most recent song
     */
    public ResponseEntity<Map<String, Object>> latestSong(@PathVariable Long id) {
        // find artist
        Artist artist = artists.get(id);
        if (artist == null) {
            return ResponseEntity.notFound().build();
        }

        // find most recent song by releaseDate
        Song latest = null;
        for (Song s : artist.getSongs().values()) {
            if (latest == null) {
                latest = s;
            } else {
                LocalDate curr = s.getReleaseDate();
                LocalDate best = latest.getReleaseDate();
                if (curr != null && best != null && curr.isAfter(best)) {
                    latest = s;
                }
            }
        }

        // none found
        if (latest == null) {
            return ResponseEntity.notFound().build();
        }

        // build payload without inline conditionals
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("title", latest.getTitle());

        String releaseDateText;
        LocalDate rd = latest.getReleaseDate();
        if (rd == null) {
            releaseDateText = null;
        } else {
            releaseDateText = rd.toString();
        }
        payload.put("releaseDate", releaseDateText);

        int playsValue = latest.getPlays(); // int, no null checks
        payload.put("plays", playsValue);

        // return
        return ResponseEntity.ok(payload);
    }
}
