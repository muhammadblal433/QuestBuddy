package coms309.players;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/players") // This acts as a "base call" for all other URLs
public class PlayerController {
    private final HashMap<Long, Player> playerList = new HashMap<>();
    private final AtomicLong seq = new AtomicLong(1);


    /**
     * LIST
     *
     * of all players (no id)
     * This takes in no input
     *
     * @return list of players in JSON format
     */
    @GetMapping(produces = "application/json")
    public ArrayList<Player> list_no_id() {
        // return as an array (values of the map)
        return new ArrayList<>(playerList.values());
    }

    /**
     * LIST
     *
     * of all players
     * This takes in no input
     *
     * @return list of players w/ id as key in JSON format
     */
    @GetMapping(params = "shape=map", produces = "application/json")
    public HashMap<Long, Player> getPlayerList() {
        return playerList;
    }

    /**
     * CREATE
     *
     * NOTE: Using ResponseEntity class to better handle and manage the HTTPS response
     * @param body
     * @return ResponseEntity w/ default header and body as player
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Player> create(@RequestBody Player body)
    {
        Long id = seq.getAndIncrement();

        Player p = new Player();
        p.setId(id);
        p.setGamertag(body.getGamertag());
        p.setEmail(body.getEmail());
        p.setElo(body.getElo());
        p.setRank(body.getRank());
        p.setPlatform(body.getPlatform());

        playerList.put(id, p);

        return ResponseEntity.created(URI.create("/api/v1/players/" + id)).body(p);
    }

    /**
     * CREATE
     *
     * allows to create players by a list of players
     * @param bodies
     * @return ResponseEntity w/ default header and body as player
     */
    @PostMapping(value = "/batch", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<Player>> createMany(@RequestBody List<Player> bodies) {
        List<Player> created = new ArrayList<>();
        for (Player body : bodies) {
            Long id = seq.getAndIncrement();

            Player p = new Player();
            p.setId(id);
            p.setGamertag(body.getGamertag());
            p.setEmail(body.getEmail());
            p.setElo(body.getElo());
            p.setRank(body.getRank());
            p.setPlatform(body.getPlatform());

            playerList.put(id, p);
            created.add(p);
        }
        // 201 Created with the list in the body
        return ResponseEntity.created(URI.create("/api/v1/players"))
                .body(created);
    }

    /**
     * READ
     *
     * allows to look up players via ID
     * NOTE: Using ResponseEntity class to better handle and manage the HTTPS response
     *
     * @param id
     * @return player if id is present, 404 otherwsie
     */
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Player> get_player_via_id(@PathVariable Long id)
    {
        Player p = playerList.get(id);
        if (p == null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(p);
    }

    /**
     * UPDATE
     *
     * allows to update player info with given info
     * access player via id number
     *
     * NOTE: Using ResponseEntity class to better handle and manage the HTTPS response
     * @param id
     * @param body
     * @return updated player body as a ResponseEntity
     */
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Player> replace(@PathVariable Long id, @RequestBody Player body)
    {
        Player existing = playerList.get(id);
        if (existing == null)
        {
            return ResponseEntity.notFound().build();
        }

        existing.setGamertag(body.getGamertag());
        existing.setEmail(body.getEmail());
        existing.setElo(body.getElo());
        existing.setRank(body.getRank());
        existing.setPlatform(body.getPlatform());

        playerList.put(id, existing);
        return ResponseEntity.ok(existing);
    }

    /**
     * DELETE
     *
     * allows us to remove players
     * NOTE: Using ResponseEntity class to better handle and manage the HTTPS response
     * @param id
     * @return ResponseEntity as the current id of the player not present
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id)
    {
        Player removed = playerList.remove(id);
        if (removed == null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
