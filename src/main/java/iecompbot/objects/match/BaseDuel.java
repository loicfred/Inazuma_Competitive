package iecompbot.objects.match;

import at.stefangeyer.challonge.model.enumeration.MatchState;
import iecompbot.objects.profile.Profile;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.util.Objects;

import static iecompbot.objects.Retrieval.getUserByID;

public class BaseDuel<T> extends DatabaseObject<T> {
    protected transient User u1;
    protected transient User u2;
    protected transient Profile p1;
    protected transient Profile p2;
    protected transient Game G;

    public long ID;
    public long P1ID;
    public long P2ID;
    public int P1Score = 0;
    public int P2Score = 0;
    public String State = MatchState.PENDING.toString();
    public String GameCode;

    public long getId() {
        return ID;
    }
    public int getP1Score() {
        return Math.max(0,P1Score);
    }
    public int getP2Score() {
        return Math.max(0,P2Score);
    }
    public long getP1ID() {
        return P1ID;
    }
    public long getP2ID() {
        return P2ID;
    }
    public MatchState getState() {
        return MatchState.valueOf(State.toUpperCase().replaceAll(" ","_"));
    }

    public Profile getProfileP1() {
        return p1 == null ? p1 = Profile.get(getP1ID()) : p1;
    }
    public Profile getProfileP2() {
        return p2 == null ? p2 = Profile.get(getP2ID()) : p2;
    }

    public User getP1() {
        return u1 == null ? u1 = (p1 != null ? p1.getUser() : getUserByID(getP1ID())) : u1;
    }
    public User getP2() {
        return u2 == null ? u2 = (p2 != null ? p2.getUser() : getUserByID(getP2ID())) : u2;
    }

    public Game getGame() {
        return G == null ? G = Game.get(GameCode) : G;
    }

    public boolean isFinished() {
        return getState().equals(MatchState.COMPLETE);
    }


    public User getOpponent(Long userId) {
        if (userId == null) return null;
        if (Objects.equals(userId, P1ID)) {
            return getP2();
        } else if (Objects.equals(userId, P2ID)) {
            return getP1();
        }
        return null;
    }

    public void AddScore(int p1Score, int p2Score) {
        P1Score = Math.max(0, p1Score);
        P2Score = Math.max(0, p2Score);
        State = MatchState.COMPLETE.toString();
    }
}
