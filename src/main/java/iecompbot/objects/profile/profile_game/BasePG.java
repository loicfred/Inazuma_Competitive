package iecompbot.objects.profile.profile_game;

import iecompbot.objects.match.Game;
import iecompbot.objects.match.League;
import iecompbot.objects.profile.Profile;
import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;
import java.util.Map;

public class BasePG<T> extends DatabaseObject<T> {
    protected transient League L;
    public transient Profile pf;
    public transient Game g;
    public transient Instant LastTimePlayed;

    public long ID;
    public long UserID;
    public String GameCode;
    public int Wins = 0;
    public int Ties = 0;
    public int Loses = 0;
    public int GoalsScored = 0;
    public int GoalsTaken = 0;
    public int Medals = 0;
    public int WinStreak = 0;
    public int LastStreak = 0;
    public int HighestWinStreak = 0;

    public long getId() {
        return ID;
    }
    public long getUserID() {
        return UserID;
    }
    public String getGameCode() {
        return GameCode;
    }
    public int getWins() {
        return Wins;
    }
    public int getTies() {
        return Ties;
    }
    public int getLoses() {
        return Loses;
    }
    public int getGoalsScored() {
        return GoalsScored;
    }
    public int getGoalsTaken() {
        return GoalsTaken;
    }
    public int getMedals() {
        return Medals;
    }
    public int getWinStreak() {
        return WinStreak;
    }
    public int getLastStreak() {
        return LastStreak;
    }
    public int getHighestWinStreak() {
        return HighestWinStreak;
    }

    public int getMatchesCount() {
        return Wins + Ties + Loses;
    }

    public League getLeague() {
        return L == null ? L = League.getByMedal(Medals) : L;
    }
    public Profile getProfile() {
        return pf == null ? pf = Profile.get(getUserID()) : pf;
    }
    public Game getGame() {
        return g == null ? g = Game.get(getGameCode()) : g;
    }


    public void setWinStreak(int winStreak) {
        WinStreak = winStreak;
    }
    public void setLastStreak(int lastStreak) {
        LastStreak = lastStreak;
    }
    public void setHighestWinStreak(int highestWinStreak) {
        HighestWinStreak = highestWinStreak;
    }


    public void AddScoreStats(int win, int tie, int lose, int goalsscored, int goalstaken, int medals, int winstreak) {
        IncrementColumns(Map.of("Wins", win,
                        "Ties", tie,
                        "Loses", lose,
                        "GoalsScored", goalsscored,
                        "GoalsTaken", goalstaken,
                        "Medals", medals,
                        "WinStreak", winstreak)
        );
    }

    public boolean hasEverPlayed() {
        return getMatchesCount() > 0;
    }
}
