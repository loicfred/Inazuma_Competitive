package iecompbot.objects.profile;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.match.Game;
import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile_Trophy extends DatabaseObject<Profile_Trophy> {
    private transient Game G;

    public long ID;
    public long UserID;
    public String Emoji;
    public String Name;
    public String Description;
    public String GameCode;
    public double Power;

    public Profile_Trophy() {}
    public Profile_Trophy(long userid, String emoji, String name, String description, Game game, double power) {
        this.ID = Instant.now().toEpochMilli();
        UserID = userid;
        Emoji = emoji;
        Name = name;
        Description = description;
        if (game != null) GameCode = game.getCode();
        Power = power;
        Write();
    }

    public long getId() {
        return ID;
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

    public static List<Profile_Trophy> get(long userId) {
        return getAllWhere(Profile_Trophy.class, "UserID = ?", userId);
    }
    public static List<Profile_Trophy> get(long userId, Game G) {
        return getAllWhere(Profile_Trophy.class, "UserID = ? AND GameCode = ?", userId, G.getCode());
    }
}
