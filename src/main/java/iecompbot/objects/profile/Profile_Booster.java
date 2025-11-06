package iecompbot.objects.profile;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile_Booster extends DatabaseObject<Profile_Booster> {

    private long ID;
    private long UserID;
    private String Name;
    private String BoosterType;
    private double Multiplier;

    private Profile_Booster() {}
    public Profile_Booster(long userID, long timeFinishEpochMilli, String boosterType, String name, double multiplier) {
        ID = timeFinishEpochMilli;
        UserID = userID;
        Name = name;
        BoosterType = boosterType;
        Multiplier = multiplier;
        Write();
    }

    public Instant getTimeFinished() {
        return Instant.ofEpochSecond(ID);
    }
    public long getUserID() {
        return UserID;
    }
    public String getName() {
        return Name;
    }
    public String getBoosterType() {
        return BoosterType;
    }
    public double getMultiplier() {
        return Multiplier;
    }

    public static List<Profile_Booster> get(long userId) {
        List<Profile_Booster> PB = getAllWhere(Profile_Booster.class, "UserID = ?", userId);
        for (Profile_Booster pb : new ArrayList<>(PB)) {
            if (pb.getTimeFinished().isBefore(Instant.now())) {
                PB.remove(pb);
                pb.Delete();
            }
        }
        return PB;
    }

}
