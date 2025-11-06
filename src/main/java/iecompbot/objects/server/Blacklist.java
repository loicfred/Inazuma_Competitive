package iecompbot.objects.server;

import iecompbot.objects.match.Game;
import iecompbot.objects.profile.Profile;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.List;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;

public class Blacklist extends DatabaseObject<Blacklist> {
    private transient ServerInfo I = null;
    private transient User U = null;
    private transient Game G = null;

    public long ID;
    public long ServerID;
    public long UserID;
    public int TournamentsLeft;
    public String GameCode;
    public String Reason;


    private Blacklist() {}
    public Blacklist(ServerInfo I, User u, Game game, int tournamentsLeft, String reason) {
        ID = Instant.now().toEpochMilli();
        ServerID = I.getId();
        UserID = u.getIdLong();
        TournamentsLeft = tournamentsLeft;
        GameCode = game != null ? game.getCode() : null;
        Reason = reason;
        this.U = u;
        this.G = game;
        this.I = I;
        Write();
    }

    public long getId() {
        return ID;
    }

    public synchronized ServerInfo getServerInfo() {
        return I == null ? I = ServerInfo.get(ServerID) : I;
    }
    public synchronized User getUser() {
        return U == null ? U = getUserByID(UserID) : U;
    }
    public synchronized Game getGame() {
        if (GameCode == null) return null;
        return G == null ? G = Game.get(GameCode) : G;
    }

    public String getTournamentsLeftTLG() {
        if (TournamentsLeft == -1) return "`" + TLG(getServerInfo(),"Indefinitely") + "`";
        return "`" + TLG(getServerInfo(),"After_X_Tournaments", TournamentsLeft) + "`";
    }

    public void decrementTournamentLeft() {
        if (TournamentsLeft > 0) TournamentsLeft--;
        if (TournamentsLeft == 0) {
            Profile P = Profile.get(getUser());
            sendPrivateMessage(getUser(), ":tada: " + TL(P, "blacklist-nolonger-success-1", "**" + (GameCode == null ? TL(P,"All") : getGame().getEmojiFormatted() + " " + getGame().getName()) + "**", "**" + I.getName() + "**"));
            I.LogSlash("**" + getUser().getEffectiveName() + ":** " + TLG(I, "blacklist-success-2", "**" + getUser().getEffectiveName() + "**", "**" + (GameCode == null ? TLG(I,"All") : getGame().getEmojiFormatted() + " " + getGame().getName()) + "**") + "\n");
            Delete();
        } else Update();
    }


    public static List<Blacklist> get(ServerInfo I) {
        List<Blacklist> T = getAllWhere(Blacklist.class, "ServerID = ?", I.getId());
        for (Blacklist CT : T) CT.I = I;
        return T;
    }
    public static List<Blacklist> get(ServerInfo I, Game game) {
        List<Blacklist> T;
        if (game != null) T = getAllWhere(Blacklist.class, "ServerID = ? AND GameCode = ?", I.getId(), game.getCode());
        else T = getAllWhere(Blacklist.class, "ServerID = ? AND GameCode IS NULL", I.getId());
        for (Blacklist CT : T) CT.I = I;
        return T;
    }
    public static Blacklist get(ServerInfo I, User user, Game game) {
        Blacklist T = null;
        if (game != null) T = getWhere(Blacklist.class, "ServerID = ? AND UserID = ? AND GameCode = ?", I.getId(), user.getIdLong(), game.getCode()).orElse(null);
        if (T == null) T = getWhere(Blacklist.class, "ServerID = ? AND UserID = ? AND GameCode IS NULL", I.getId(), user.getIdLong()).orElse(null);
        if (T == null) return null;
        T.I = I;
        return T;
    }

}
