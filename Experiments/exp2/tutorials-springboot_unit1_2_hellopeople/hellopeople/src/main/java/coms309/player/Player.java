package coms309.players;

import java.time.Instant;
import java.util.Objects;

/**
 * This is the definition for a hypothetical player class
 *
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

    // ---- constructors ----
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
        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
    }

    // ---- getters/setters (with basic validation) ----
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getGamertag()
    {
        return gamertag;
    }

    public void setGamertag(String gamertag)
    {
        if (gamertag == null || gamertag.isBlank())
        {
            throw new IllegalArgumentException("gamertag must not be blank");
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

    public Integer getElo()
    {
        return elo;
    }

    public void setElo(Integer elo)
    {
        if (elo != null && (elo < 0 || elo > 5000))
        {
            throw new IllegalArgumentException("elo must be between 0 and 5000");
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

    public String getPlatform()
    {
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

    public Instant getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt)
    {
        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
    }

    // ---- object methods ----
    @Override
    public String toString()
    {
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player other = (Player) o;

        // If both have IDs, compare by ID
        if (this.id != null && other.id != null) return this.id.equals(other.id);

        // Otherwise compare by natural keys (gamertag+email)
        String gtA = this.gamertag == null ? null : this.gamertag.toLowerCase();
        String gtB = other.gamertag == null ? null : other.gamertag.toLowerCase();
        String emA = this.email == null ? null : this.email.toLowerCase();
        String emB = other.email == null ? null : other.email.toLowerCase();
        return Objects.equals(gtA, gtB) && Objects.equals(emA, emB);
    }

    @Override
    public int hashCode()
    {
        if (id != null) return id.hashCode();
        String gt = gamertag == null ? null : gamertag.toLowerCase();
        String em = email == null ? null : email.toLowerCase();
        return Objects.hash(gt, em);
    }
}
