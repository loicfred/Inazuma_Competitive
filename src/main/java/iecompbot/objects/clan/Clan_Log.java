package iecompbot.objects.clan;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.List;

import static iecompbot.objects.Retrieval.getUserByID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Clan_Log extends DatabaseObject<Clan_Log> {

    private long ID;
    private long ClanID;
    private long EditorID;
    private String Name;
    private String Description;

    private transient User user;

    public long getId() {
        return ID;
    }
    public long getClanID() {
        return ClanID;
    }
    public long getEditorID() {
        return EditorID;
    }
    public String getName() {
        return Name;
    }
    public String getDescription() {
        return Description;
    }

    public User getEditor() {
       return user == null ? user = getUserByID(EditorID) : user;
    }

    public long getEpochSecond() {
        return getId() / 1000;
    }
    private Clan_Log() {}
    public Clan_Log(long clanID, long editorID, String name, String description) {
        ID = Instant.now().toEpochMilli();
        ClanID = clanID;
        EditorID = editorID;
        Name = name;
        Description = description;
        Write();
    }

    public static List<Clan_Log> get(long clanID, int page, int amountperpage) {
        return doQueryAll(Clan_Log.class,"CALL DisplayClanLogs(?,?,?);", clanID, page, amountperpage);
    }

}
