package iecompbot.objects.server;

import iecompbot.springboot.data.DatabaseObject;

import java.util.List;

public class Criminal extends DatabaseObject<Criminal> {
    private static List<Criminal> Crims;

    private long UserID;
    private int DangerLevel;
    private String Reason;


    private Criminal() {}
    public Criminal(long userID, byte dangerLevel, String reason) {
        this.UserID = userID;
        this.DangerLevel = dangerLevel;
        this.Reason = reason;
    }

    public long getUserID() {
        return UserID;
    }

    public int getDangerLevel() {
        return DangerLevel;
    }

    public String getReason() {
        return Reason;
    }

    public static List<Criminal> list(boolean reset) {
        return Crims == null || reset ? Crims = getAll(Criminal.class) : Crims;
    }

}
