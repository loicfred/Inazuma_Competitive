package iecompbot.objects.event;

import iecompbot.springboot.data.DatabaseObject;

import java.util.List;

public class Event_Rediff extends DatabaseObject<Event_Organiser> {

    private long ID;
    private String EventID;
    private String Name;
    private String Link;

    public static List<Event_Rediff> getEventRediffs(String eventid) {
        return getAllWhere(Event_Rediff.class, "EventID = ?", eventid);
    }

    private Event_Rediff() {}

    public long getId() {
        return ID;
    }

    public String getEventID() {
        return EventID;
    }

    public String getName() {
        return Name;
    }

    public String getLink() {
        return Link;
    }

}
