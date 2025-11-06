package iecompbot.objects.clan;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.match.Game;
import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Clan_Trophy extends DatabaseObject<Clan_Trophy> {
    private transient Game G;

    public long ID;
    public long ClanID;
    public String Emoji;
    public String Name;
    public String Description;
    public String GameCode;
    public double Power;

    public Clan_Trophy() {}
    public Clan_Trophy(long clanID, String emoji, String name, String description, Game game, double power) {
        this.ID = Instant.now().toEpochMilli();
        ClanID = clanID;
        Emoji = emoji;
        Name = name;
        Description = description;
        if (GameCode != null) GameCode = game.getCode();
        Power = power;
        Write();
    }


    public long getId() {
        return ID;
    }

    public long getClanID() {
        return ClanID;
    }

    public String getEmoji() {
        return Emoji;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public Game getGame() {
        return G == null ? G = Game.get(GameCode) : G;
    }

    public double getPower() {
        return Power;
    }

    public static List<Clan_Trophy> get(long id) {
        return getAllWhere(Clan_Trophy.class, "ClanID = ?", id);
    }

}