package iecompbot.objects.profile.profile_game;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.match.Game;
import iecompbot.objects.match.League;
import iecompbot.objects.server.ServerInfo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile_Game_S extends BasePG<Profile_Game_S> {

    public long ServerID;
    public long getServerID() {
        return ServerID;
    }

    public void syncWith(Profile_Game PG) {
        if (getUserID() == PG.getUserID() && getGame().equals(PG.getGame())) {
            setWins(PG.getWins());
            setTies(PG.getTies());
            setLoses(PG.getLoses());
            setGoalsScored(PG.getGoalsScored());
            setGoalsTaken(PG.getGoalsTaken());
            setMedals(PG.getMedals());
            setWinStreak(PG.getWinStreak());
            setHighestWinStreak(PG.getHighestWinStreak());
        }
    }

    public void setWins(int wins) {
        Wins = wins;
    }
    public void setTies(int ties) {
        Ties = ties;
    }
    public void setLoses(int loses) {
        Loses = loses;
    }
    public void setGoalsScored(int goalsScored) {
        GoalsScored = goalsScored;
    }
    public void setGoalsTaken(int goalsTaken) {
        GoalsTaken = goalsTaken;
    }
    public void setMedals(int medals) {
        Medals = medals;
    }

    public double getPower() {
        return doQuery("CALL DisplayUserPower(?,?,?)", getUserID(), getServerID(), getGameCode()).orElse(new Row(Map.of("Total Power", 1))).getAsDouble("Total Power");
    }
    public int getRank() {
        return doQuery("""
                    WITH RankedRows AS (
                        SELECT Medals, UserID, GameCode, RANK() OVER (ORDER BY Medals DESC) AS Position
                        FROM inazuma_competitive.profile_game_s
                        WHERE GameCode = ? AND ServerID = ?
                    )
                    SELECT Position
                    FROM RankedRows
                    WHERE UserID = ?""", GameCode, getServerID(), getUserID()).orElse(new Row(Map.of("Position", 1))).getAsInt("Position");
    }

    @Override
    public League getLeague() {
        ServerInfo I = ServerInfo.get(ServerID);
        if (I.Ranking().hasPrivateLeagues()) return L == null ? L = I.Ranking().getLeagueByMedal(Medals) : L;
        return super.getLeague();
    }
    public League getLeague(ServerInfo I) {
        if (I == null) return super.getLeague();
        if (I.Ranking().hasPrivateLeagues()) return L == null ? L = I.Ranking().getLeagueByMedal(Medals) : L;
        return super.getLeague();
    }

    private Profile_Game_S() {}
    public Profile_Game_S(long userid, Game game, Long serverid) {
        this.ID = Instant.now().toEpochMilli();
        this.UserID = userid;
        this.GameCode = game.getCode();
        this.ServerID = serverid;
        Write();
    }

    public static Profile_Game_S get(long userid, long serverid, Game G) {
        return getWhere(Profile_Game_S.class,"UserID = ? AND ServerID = ? AND GameCode = ?", userid, serverid, G.getCode()).orElseGet(() -> new Profile_Game_S(userid, G, serverid));
    }

    public static List<Profile_Game_S> ofUser(long userID, long serverid) {
        return getAllWhere(Profile_Game_S.class, "UserID = ? AND ServerID = ?", userID, serverid);
    }

    public static List<Profile_Game_S> list(long serverid, Game G) {
        return getAllWhere(Profile_Game_S.class, "ServerID = ? AND GameCode = ?", serverid, G.getCode());
    }

}
