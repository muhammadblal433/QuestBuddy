package coms309.players;

import java.time.Instant;

/**
 * This is the definition for a hypothetical player class
 *
 * @author Ayaan Syed
 */
public class Player
{
    private Long id; // server-assigned (may be null before creation)
    private String gamertag; // must be unique
    private String email; // linked to account
    private Integer elo; // non negative integer
    private Rank rank;
    private String platform; // e.g., PC/PS/Xbox (optional)
    private Instant createdAt; // time of creation

    public Player()
    {
        this.createdAt = Instant.now();
    }

    public Player(Long id, String gamertag, String email, Integer elo, Rank rank, String platform, Instant createdAt)
    {
        setId(id);
        setGamertag(gamertag);
        setEmail(email);
        setElo(elo);
        setRank(rank);
        setPlatform(platform);
        if (createdAt == null)
        {
            this.createdAt = Instant.now();
        }
        else
        {
            this.createdAt = createdAt;
        }
    }

    // ---- getters/setters (with basic validation) ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGamertag() {
        return gamertag;
    }

    public void setGamertag(String gamertag) {
        if (gamertag == null || gamertag.isBlank())
        {
            throw new IllegalArgumentException("gamertag is required");
        }
        if (gamertag.length() > 24)
        {
            throw new IllegalArgumentException("gamertag length must be <= 24");
        }
        this.gamertag = gamertag;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        if (email == null || email.isBlank())
        {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (email.length() > 120)
        {
            throw new IllegalArgumentException("email length must be <= 120");
        }
        this.email = email;
    }

    public Integer getElo() {

        return elo;
    }

    public void setElo(Integer elo)
    {
        if (elo != null && elo < 0) {
            throw new IllegalArgumentException("elo must be >= 0");
        }

        this.elo = elo;
    }

    public Rank getRank()
    {
        return rank;
    }

    public void setRank(Rank rank)
    {
        if (rank == null)
        {
            throw new IllegalArgumentException("rank must not be null");
        }
        this.rank = rank;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform)
    {
        if (platform != null && platform.length() > 20)
        {
            throw new IllegalArgumentException("platform length must be <= 20");
        }
        this.platform = platform;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        if (createdAt == null)
        {
            this.createdAt = Instant.now();
        }
        else
        {
            this.createdAt = createdAt;
        }
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", gamertag='" + gamertag + '\'' +
                ", email='" + email + '\'' +
                ", elo=" + elo +
                ", rank=" + rank +
                ", platform='" + platform + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
