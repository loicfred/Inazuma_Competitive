package iecompbot.objects.event;

import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

import static iecompbot.objects.Retrieval.getUserByID;

public class Event_Team extends DatabaseObject<Event_Team> {

    private long ID;
    private String EventID;
    private String Name;
    private int Position;
    private double Power;
    private Long ClanID;
    private Long UserID1;
    private Long UserID2;
    private Long UserID3;
    private Long UserID4;
    private Long UserID5;
    private Long UserID6;
    private Long UserID7;
    private Long UserID8;
    private Long UserID9;
    private Long UserID10;

    private String Bonus1;
    private String Bonus2;
    private String Bonus3;
    private String Bonus4;
    private String Bonus5;
    private String Bonus6;
    private String Bonus7;
    private String Bonus8;
    private String Bonus9;
    private String Bonus10;
    private String Bonus11;

    public long getId() {
        return ID;
    }

    public String getEventID() {
        return EventID;
    }

    public String getName() {
        return Name;
    }

    public int getPosition() {
        return Position;
    }

    public double getPower() {
        return Power;
    }

    public Long getClanID() {
        return ClanID;
    }

    public Long getUserID1() {
        return UserID1;
    }

    public List<User> getUsers() {
        List<User> u = new ArrayList<>();
        if (UserID1 != null) u.add(getUserByID(UserID1));
        if (UserID2 != null) u.add(getUserByID(UserID2));
        if (UserID3 != null) u.add(getUserByID(UserID3));
        if (UserID4 != null) u.add(getUserByID(UserID4));
        if (UserID5 != null) u.add(getUserByID(UserID5));
        if (UserID6 != null) u.add(getUserByID(UserID6));
        if (UserID7 != null) u.add(getUserByID(UserID7));
        if (UserID8 != null) u.add(getUserByID(UserID8));
        if (UserID9 != null) u.add(getUserByID(UserID9));
        if (UserID10 != null) u.add(getUserByID(UserID10));
        return u;
    }
    public List<String> getUnknown() {
        List<String> u = new ArrayList<>();
        if (Bonus1 != null) u.add(Bonus1);
        if (Bonus2 != null) u.add(Bonus2);
        if (Bonus3 != null) u.add(Bonus3);
        if (Bonus4 != null) u.add(Bonus4);
        if (Bonus5 != null) u.add(Bonus5);
        if (Bonus6 != null) u.add(Bonus6);
        if (Bonus7 != null) u.add(Bonus7);
        if (Bonus8 != null) u.add(Bonus8);
        if (Bonus9 != null) u.add(Bonus9);
        if (Bonus10 != null) u.add(Bonus10);
        if (Bonus11 != null) u.add(Bonus11);
        return u;
    }

    public static List<Event_Team> ofUser(long id) {
        return getAllWhere(Event_Team.class, "UserID1 = ? OR UserID2 = ? OR UserID3 = ? OR UserID4 = ? OR UserID5 = ? OR UserID6 = ? OR UserID7 = ? OR UserID8 = ? OR UserID9 = ? OR UserID10 = ?", id, id, id, id, id, id, id, id, id, id);
    }
    public static List<Event_Team> getEventTeams(String eventid) {
        return getAllWhere(Event_Team.class, "EventID = ?", eventid);
    }

    private Event_Team() {}

}