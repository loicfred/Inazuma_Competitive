package iecompbot.objects.profile.profile_game;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.match.Game;
import iecompbot.objects.match.MatchLog;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile_Game extends BasePG<Profile_Game> {

    public Instant getLastTimePlayed() {
        List<MatchLog> Latest = MatchLog.getMatchesOf(getUserID(), null, List.of(getGame()), 1, 1);
        return !Latest.isEmpty() ? LastTimePlayed = Instant.ofEpochMilli(Latest.getFirst().getId()) : LastTimePlayed;
    }

    public double getPower() {
        return doQuery("CALL DisplayUserPower(?,?,?)", getUserID(), null, getGameCode()).orElse(new Row(Map.of("Total Power", 1))).getAsDouble("Total Power");
    }
    public int getRank() {
        return doQuery("""
                    WITH RankedRows AS (
                        SELECT Medals, UserID, GameCode, RANK() OVER (ORDER BY Medals DESC) AS Position
                        FROM inazuma_competitive.profile_game
                        WHERE GameCode = ?
                    )
                    SELECT Position
                    FROM RankedRows
                    WHERE UserID = ?""", getGame().getCode(), getUserID()).orElse(new Row(Map.of("Position", 1))).getAsInt("Position");
    }

    private Profile_Game() {}
    public Profile_Game(long userid, Game game) {
        this.ID = Instant.now().toEpochMilli();
        this.UserID = userid;
        this.GameCode = game.getCode();
        Write();
    }

    public static Profile_Game get(long userid, Game G) {
        return getWhere(Profile_Game.class,"UserID = ? AND GameCode = ?", userid, G.getCode()).orElseGet(() -> new Profile_Game(userid, G));
    }

    public static List<Profile_Game> ofUser(long userID) {
        return getAllWhere(Profile_Game.class, "UserID = ? ORDER BY Medals DESC", userID);
    }

    public static List<Profile_Game> list(Game G) {
        return getAllWhere(Profile_Game.class, "GameCode = ?", G.getCode());
    }

}
