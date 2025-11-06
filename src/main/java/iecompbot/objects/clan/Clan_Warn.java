package iecompbot.objects.clan;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.List;

import static iecompbot.objects.Retrieval.getUserByID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Clan_Warn extends DatabaseObject<Clan_Warn> {

    private long ID;
    private long ClanID;
    private long UserID;
    private String Name;
    private String Description;
    private int Hours;
    private boolean isCompleted;
    private double PowerLoss;

    private transient User user;

    public long getId() {
        return ID;
    }
    public long getClanID() {
        return ClanID;
    }
    public long getUserID() {
        return UserID;
    }
    public String getName() {
        return Name;
    }
    public String getDescription() {
        return Description;
    }
    public long getEpochSecond() {
        return ID / 1000;
    }
    public int getHours() {
        return Hours;
    }
    public boolean isCompleted() {
        return isCompleted;
    }
    public double getPowerLoss() {
        return PowerLoss;
    }
    public Instant getTimeCreated() {
        return Instant.ofEpochMilli(ID);
    }

    public User getUser() {
        if (user == null) {
            user = getUserByID(getUserID());
        } return user;
    }

    private Clan_Warn() {}
    protected Clan_Warn(long clanID, long userID, String name, String description, int hours, double powerloss) {
        ID = Instant.now().toEpochMilli();
        ClanID = clanID;
        UserID = userID;
        Name = name;
        Description = description;
        Hours = hours;
        PowerLoss = powerloss;
        Write();
    }

    public void setCompleted(boolean set) {
        isCompleted = set;
    }

    public static List<Clan_Warn> get(long clanID) {
        return getAllWhere(Clan_Warn.class, "ClanID = ? ORDER BY ID DESC", clanID);
    }
}