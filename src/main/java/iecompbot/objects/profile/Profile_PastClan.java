package iecompbot.objects.profile;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.clan.Clan;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static iecompbot.Utility.formatRelativeTimeTL;
import static my.utilities.util.Utilities.getYear;
import static my.utilities.var.Constants.ProgramZoneId;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile_PastClan extends DatabaseObject<Profile_PastClan> {
    private transient Clan C;

    private long ID;
    private long UserID;
    private long ClanID;
    private long TimeJoinedEpochMilli;
    private boolean WasMainClan = true;

    private Profile_PastClan() {}
    public Profile_PastClan(long userID, long clanID, long timeJoinedEpochMilli, long timeLeftEpochMilli, boolean wasMainClan) {
        ID = timeLeftEpochMilli;
        UserID = userID;
        ClanID = clanID;
        TimeJoinedEpochMilli = timeJoinedEpochMilli;
        WasMainClan = wasMainClan;
        Write();
    }

    public Clan getClan() {
        return C == null ? C = Clan.get(ClanID) : C;
    }

    public long getId() {
        return ID;
    }
    public long getUserID() {
        return UserID;
    }
    public long getClanID() {
        return ClanID;
    }
    public Instant getTimeJoined() {
        return Instant.ofEpochMilli(TimeJoinedEpochMilli);
    }
    public String getTimeJoined(String pattern) {
        return getTimeJoined().atZone(ProgramZoneId).format(DateTimeFormatter.ofPattern(pattern));
    }
    public Instant getTimeLeft() {
        return Instant.ofEpochMilli(getId());
    }
    public String getTimeLeft(String pattern) {
        return getTimeLeft().atZone(ProgramZoneId).format(DateTimeFormatter.ofPattern(pattern));
    }
    public int getYearJoined() {
        return Integer.parseInt(getYear(getTimeJoined()));
    }
    public int getYearLeft() {
        return Integer.parseInt(getYear(getTimeLeft()));
    }
    public String getDuration(InteractionHook M) {
        return formatRelativeTimeTL(M, Duration.between(getTimeJoined(), getTimeLeft()));
    }
    public boolean wasMainClan() {
        return WasMainClan;
    }

    public void setTimeJoined(Instant i) {
        TimeJoinedEpochMilli = i.toEpochMilli();
        Update();
    }

    public static List<Profile_PastClan> ofUser(long userid, boolean mainClan) {
        return getAllWhere(Profile_PastClan.class, "UserID = ? AND WasMainClan = ? ORDER BY TimeJoinedEpochMilli DESC", userid, mainClan);
    }
    public static List<Profile_PastClan> ofClan(long clanId) {
        return getAllWhere(Profile_PastClan.class, "ClanID = ? ORDER BY TimeJoinedEpochMilli DESC", clanId);
    }
    public static List<Profile_PastClan> ofClan(long clanId, Instant atTime) {
        return getAllWhere(Profile_PastClan.class, "ClanID = ? AND TimeJoinedEpochMilli < ? AND ID > ? ORDER BY TimeJoinedEpochMilli DESC", clanId, atTime.getEpochSecond(), atTime.getEpochSecond());
    }

    public long getTimeJoinedEpochSecond() {
        return TimeJoinedEpochMilli;
    }

}
