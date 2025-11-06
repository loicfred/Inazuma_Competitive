package iecompbot.objects.profile;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import ie.enums.Element;
import ie.enums.Gender;
import ie.enums.Position;
import iecompbot.objects.match.League;
import iecompbot.springboot.data.DatabaseObject;

import java.util.Map;

import static iecompbot.L10N.TL;
import static iecompbot.objects.UserAction.sendPrivateMessage;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile_Total extends DatabaseObject<Profile_Total> {
    private transient Integer Tournaments = null;
    private transient Profile P;

    public long ID;

    public int Level = 1; // Level
    public int CurrentXP = 0; // Current XP
    public int XPForNextLevel = 40; // Current XP

    public int TotalDailies = 0;
    public int TotalQuests = 0;

    public String Element = "Void";
    public String Position = "Forward";
    public String Gender = "Male";

    public int GP = 30;
    public int TP = 30;
    public int Kick = 0;
    public int Dribble = 0;
    public int Block = 0;
    public int Catch = 0;
    public int Technique = 0;
    public int Speed = 0;
    public int Stamina = 0;
    public int Luck = 0;
    public int Freedom = 0;

    public Profile_Total() {}
    public Profile_Total(Profile P) {
        this.P = P;
        this.ID = P.getId();
        Write();
    }
    public Profile_Total(long userID) {
        this.ID = userID;
        Write();
    }

    public static int XPtillNextLevel(int Level) {
        return (int) ((40 + Level) * (Level * 2.5) * 0.4) - 1;
    }

    public long getId() {
        return ID;
    }

    private transient Row TotalStats = null;
    public Row getTotalStats() {
        return TotalStats == null ? TotalStats = doQuery("SELECT * FROM inazuma_competitive.profile_stats WHERE UserID = ?", getId()).orElse(null) : TotalStats;
    }

    public int getWins() {
        return getTotalStats().getAsInt("Wins");
    }

    public int getTies() {
        return getTotalStats().getAsInt("Ties");
    }

    public int getLoses() {
        return getTotalStats().getAsInt("Loses");
    }

    public int getGoalsScored() {
        return getTotalStats().getAsInt("GoalsScored");
    }

    public int getGoalsTaken() {
        return getTotalStats().getAsInt("GoalsTaken");
    }

    public int getTotalMedals() {
        return getTotalStats().getAsInt("Medals");
    }

    public Row getTotalStats(long serverId) {
        return doQuery("SELECT * FROM inazuma_competitive.profile_stats_s WHERE ServerID = ? AND UserID = ?", serverId, getId()).orElse(null);
    }

    public int getRank() {
        return doQuery("WITH RankedRows AS ( " +
                        "    SELECT TotalMedals, UserID, RANK() OVER (ORDER BY TotalMedals DESC) AS Position " +
                        "    FROM inazuma_competitive.profile_total_s " +
                        ") " +
                        "SELECT Position " +
                        "FROM RankedRows " +
                        "WHERE UserID = ?", getId()).orElse(new Row(Map.of("Position", 1))).getAsInt("Position");
    }
    public League getLeague() {
        try {
            return League.getByMedal(Integer.parseInt(getTotalStats().get("Medals").toString()));
        } catch (Exception e) {
            return League.getByMedal(0);
        }
    }
    public League getLeague(long serverId) {
        try {
            return League.getByMedal(Integer.parseInt(getTotalStats(serverId).get("Medals").toString()));
        } catch (Exception e) {
            return League.getByMedal(0);
        }
    }

    public Element getElement() {
        return ie.enums.Element.valueOf(Element);
    }
    public Position getPosition() {
      return ie.enums.Position.valueOf(Position);
    }
    public Gender getGender() {
        return ie.enums.Gender.valueOf(Gender);
    }

    public int getGP() {
        return GP;
    }
    public int getTP() {
        return TP;
    }
    public int getKick() {
        return Kick;
    }
    public int getDribble() {
        return Dribble;
    }
    public int getBlock() {
        return Block;
    }
    public int getCatch() {
        return Catch;
    }
    public int getTechnique() {
        return Technique;
    }
    public int getSpeed() {
        return Speed;
    }
    public int getStamina() {
        return Stamina;
    }
    public int getLuck() {
        return Luck;
    }
    public int getFreedom() {
        return Freedom;
    }

    public void setElement(Element element) {
        Element = element.toString();
    }
    public void setPosition(String position) {
        Position = position;
    }
    public void setGender(Gender gender) {
        Gender = gender.toString();
    }
    public void setGP(int GP) {
        this.GP = GP;
    }
    public void setTP(int TP) {
        this.TP = TP;
    }
    public void setKick(int kick) {
        this.Kick = kick;
    }
    public void setDribble(int dribble) {
        this.Dribble = dribble;
    }
    public void setBlock(int block) {
        this.Block = block;
    }
    public void setCatch(int aCatch) {
        this.Catch = aCatch;
    }
    public void setTechnique(int technique) {
        this.Technique = technique;
    }
    public void setSpeed(int speed) {
        this.Speed = speed;
    }
    public void setStamina(int stamina) {
        this.Stamina = stamina;
    }
    public void setLuck(int luck) {
        this.Luck = luck;
    }
    public void setFreedom(int freedom) {
        this.Freedom = freedom;
    }

    public void addTotalQuests() {
        TotalQuests++;
        IncrementColumn("TotalQuests", 1);
    }
    public void addTotalDailies() {
        TotalDailies++;
        IncrementColumn("TotalDailies", 1);
    }

    public void LevelUp(int XP) {
        CurrentXP += XP;
        while (CurrentXP >= XPForNextLevel) {
            Level++;
            CurrentXP = CurrentXP - XPForNextLevel;
            XPForNextLevel = XPtillNextLevel(Level);
            sendPrivateMessage(getProfile().getUser(), TL(P, "level-up", getProfile().getUser().getAsMention(), "**" + Level + "**"));
        }
        Update();
    }

    public double getCoinMultiplier() {
        double Multiplier = getLeague().getMultiplier();
        if (getProfile().getBoosters().stream().anyMatch(b -> b.getBoosterType().equals("Coin"))) {
            Multiplier = Multiplier * getProfile().getBoosters().stream().filter(b -> b.getBoosterType().equals("Coin")).toList().getFirst().getMultiplier();
        }
        return Multiplier;
    }

    public Profile getProfile() {
        return P == null ? P = Profile.get(ID) : P;
    }

    public static Profile_Total get(Profile p) {
        Profile_Total T = getById(Profile_Total.class, p.getId()).orElseGet(() -> new Profile_Total(p));
        T.P = p;
        return T;
    }
    public static Profile_Total get(long id) {
        return getById(Profile_Total.class, id).orElseGet(() -> new Profile_Total(id));
    }
}
