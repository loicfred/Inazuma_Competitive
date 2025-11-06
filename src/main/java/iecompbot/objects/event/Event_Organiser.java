package iecompbot.objects.event;

import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

import static iecompbot.objects.Retrieval.getUserByID;

public class Event_Organiser extends DatabaseObject<Event_Organiser> {
    private transient User u;

    private long ID;
    private long UserID;
    private String EventID;
    private String Role;

    public long getId() {
        return ID;
    }
    public long getUserID() {
        return UserID;
    }
    public String getEventID() {
        return EventID;
    }
    public String getRole() {
        return Role;
    }
    public User getUser() {
        return u == null ? u = getUserByID(getUserID()) : u;
    }

    public static List<Event_Organiser> ofUser(long id) {
        return getAllWhere(Event_Organiser.class, "UserID = ?", id);
    }
    public static List<Event_Organiser> getOrganiserTeams(String eventid) {
        return getAllWhere(Event_Organiser.class, "EventID = ?", eventid);
    }

    private Event_Organiser() {}
}