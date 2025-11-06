package iecompbot.objects.server.tournament;

import iecompbot.objects.Retrieval;
import iecompbot.objects.profile.Profile;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class BaseParticipant<T> extends DatabaseObject<T> {
    private transient Profile P;
    private transient Profile P2;
    private transient Profile P3;
    private transient Profile P4;
    private transient Profile P5;
    private transient Profile P6;
    private transient Profile P7;
    private transient Profile P8;

    public long ID;
    public long TournamentID;
    public Long DiscordID;
    public Long DiscordID2;
    public Long DiscordID3;
    public Long DiscordID4;
    public Long DiscordID5;
    public Long DiscordID6;
    public Long DiscordID7;
    public Long DiscordID8;
    public String Name;
    public Integer Position = 0;
    public int Wins = 0;
    public int Loses = 0;
    public int Ties = 0;
    public int GoalsScored = 0;
    public int GoalsTaken = 0;

    public long getId() {
        return ID;
    }
    public long getTournamentID() {
        return TournamentID;
    }
    public String getName() {
        return Name;
    }
    public Long getLeaderID() {
        return DiscordID;
    }
    public List<Long> getAllTeammatesIDs() {
        List<Long> L = new ArrayList<>();
        if (DiscordID != null) L.add(DiscordID);
        if (DiscordID2 != null) L.add(DiscordID2);
        if (DiscordID3 != null) L.add(DiscordID3);
        if (DiscordID4 != null) L.add(DiscordID4);
        if (DiscordID5 != null) L.add(DiscordID5);
        if (DiscordID6 != null) L.add(DiscordID6);
        if (DiscordID7 != null) L.add(DiscordID7);
        if (DiscordID8 != null) L.add(DiscordID8);
        return L;
    }
    public List<User> getTeammates() {
        return getAllTeammatesIDs().stream().map(Retrieval::getUserByID).toList();
    }
    public Integer getPosition() {
        return Position;
    }
    public int getWins() {
        return Wins;
    }
    public int getLoses() {
        return Loses;
    }
    public int getTies() {
        return Ties;
    }
    public int getGoalsScored() {
        return GoalsScored;
    }
    public int getGoalsTaken() {
        return GoalsTaken;
    }


    public Profile getLeaderPf()  {
        return P == null && DiscordID != null ? P = Profile.get(DiscordID) : P;
    }
    public User getLeader() {
        try {
            return getLeaderPf().getUser();
        } catch (Exception ignored) {
            return null;
        }
   }

}
