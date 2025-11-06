package iecompbot.objects.event;

import iecompbot.objects.match.Game;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static iecompbot.objects.Retrieval.getUserByID;
import static my.utilities.util.Utilities.similarity;

public class Event extends DatabaseObject<Event> {
    public transient List<Event_Organiser> Organisers = null;


    private String ID;
    private String Name;
    private String Description;
    private String Description2;
    private String Teaser;
    private String Intro;
    private String Trailer;
    private String Type;
    private String Trivia;
    private String Colorcode;
    private String GroupsInfo;
    private String Link;
    private String GameCode;
    private String Winner;
    private long StartedAtTimeEpochSecond;
    private long CompletedAtTimeEpochSecond;
    private Long CasterID1;
    private Long CasterID2;
    private Long CasterID3;
    private Long CasterID4;
    private Long CasterID5;

    public String getId() {
        return ID;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public String getDescription2() {
        return Description2;
    }

    public String getColorcode() {
        return Colorcode;
    }
    public Color getColor() {
        return Color.decode(Colorcode);
    }

    public String getTeaser() {
        return Teaser;
    }
    public String getIntro() {
        return Intro;
    }
    public String getTrailer() {
        return Trailer;
    }
    public String getTrivia() {
        return Trivia;
    }
    public String getGroupsInfo() {
        return GroupsInfo;
    }

    public EventType getType() {
        return EventType.valueOf(Type);
    }

    public String getLink() {
        return Link;
    }

    public String getWinner() {
        return Winner;
    }

    public Game getGame() {
        return Game.get(GameCode);
    }

    public long getStartedAtTimeEpochSecond() {
        return StartedAtTimeEpochSecond;
    }

    public long getCompletedAtTimeEpochSecond() {
        return CompletedAtTimeEpochSecond;
    }

    public List<Event_Organiser> getOrganisers() {
        return Organisers == null ? Organisers = Event_Organiser.getOrganiserTeams(getId()) : Organisers;
    }

    public List<User> getCasters() {
        List<User> u = new ArrayList<>();
        if (CasterID1 != null) u.add(getUserByID(CasterID1));
        if (CasterID2 != null) u.add(getUserByID(CasterID2));
        if (CasterID3 != null) u.add(getUserByID(CasterID3));
        if (CasterID4 != null) u.add(getUserByID(CasterID4));
        if (CasterID5 != null) u.add(getUserByID(CasterID5));
        return u;
    }

    public static Event getEvent(String id) {
        return getById(Event.class, id).orElse(null);
    }
    public static List<Event> find(String like) {
        if (like.length() < 3) return new ArrayList<>();
        like = like.toLowerCase().replaceAll(" wc", " world cup").replaceAll(" cc", " clan cup");
        List<Event> S = getAllWhere(Event.class, "Name LIKE ? OR Description LIKE ? OR Description2 LIKE ? OR GroupsInfo LIKE ? OR Trivia LIKE ? LIMIT 5", "%" + like + "%", "%" + like + "%", "%" + like + "%", "%" + like + "%", "%" + like + "%");
        if (S.isEmpty()) {
            List<Row> TR = doQueryAll("SELECT ID, Name FROM inazuma_competitive.event;");
            for (DatabaseObject.Row row : TR) {
                if (similarity(row.getAsString("Name"), like) > 50) {
                    S.add(Event.getEvent(row.getAsString("ID")));
                }
            }
        }
        return S;
    }

    public List<Event_Team> getTeams() {
        return Event_Team.getEventTeams(getId());
    }

    public List<Event_Rediff> getEventRediffs() {
        return Event_Rediff.getEventRediffs(getId());
    }

    public static List<Event> getWCs() {
        return getAllWhere(Event.class, "Type = ? ORDER BY CompletedAtTimeEpochSecond DESC", EventType.WC.toString());
    }
    public static List<Event> getEUs() {
        return getAllWhere(Event.class, "Type = ? ORDER BY CompletedAtTimeEpochSecond DESC", EventType.EU.toString());
    }
    public static List<Event> getCANs() {
        return getAllWhere(Event.class, "Type = ? ORDER BY CompletedAtTimeEpochSecond DESC", EventType.CAN.toString());
    }
    public static List<Event> getCCs() {
        return getAllWhere(Event.class, "Type = ? ORDER BY CompletedAtTimeEpochSecond DESC", EventType.CC.toString());
    }

    private Event() {}


    public enum EventType {
        WC, EU, CAN, CC
    }
}
