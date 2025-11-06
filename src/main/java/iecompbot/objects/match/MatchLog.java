package iecompbot.objects.match;

import at.stefangeyer.challonge.model.enumeration.MatchState;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.Constants;
import iecompbot.objects.match.loot.LootTable;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.profile_game.Profile_Game;
import iecompbot.objects.profile.quest.quest.Profile_Quest;
import iecompbot.objects.server.tournament.BaseTournament;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.Main.DiscordAccount;
import static my.utilities.util.Utilities.GenerateRandomNumber;
import static my.utilities.util.Utilities.Range;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class MatchLog extends BaseMatchLog<MatchLog> {
    private transient Profile_Game p1g;
    private transient Profile_Game p2g;

    private transient Item P1I1 = null;
    private transient Item P1I2 = null;
    private transient Item P1I3 = null;
    private transient Item P2I1 = null;
    private transient Item P2I2 = null;
    private transient Item P2I3 = null;


    private int P1XP = 0;
    private int P2XP = 0;
    private int P1Coins = 0;
    private int P2Coins = 0;
    private int P1RainbowCoins = 0;
    private int P2RainbowCoins = 0;
    private Long P1Item1ID = null;
    private Long P1Item2ID = null;
    private Long P1Item3ID = null;
    private Long P2Item1ID = null;
    private Long P2Item2ID = null;
    private Long P2Item3ID = null;
    public Long TiedWinner;


    public int getP1XP() {
        return P1XP;
    }
    public int getP2XP() {
        return P2XP;
    }
    public int getP1Coins() {
        return P1Coins;
    }
    public int getP2Coins() {
        return P2Coins;
    }
    public int getP1RainbowCoins() {
        return P1RainbowCoins;
    }
    public int getP2RainbowCoins() {
        return P2RainbowCoins;
    }

    public void complete(MatchState s, long messageID) {
        State = s.toString();
        MessageID = messageID;
        Update();
    }

    public Profile_Game getPGP1() {
        return p1g == null ? p1g = Profile_Game.get(getP1ID(), getGame()) : p1g;
    }
    public Profile_Game getPGP2() {
        return p2g == null ? p2g = Profile_Game.get(getP2ID(), getGame()) : p2g;
    }

    public Item getP1Item1() {
        return P1Item1ID != null && P1I1 == null ? P1I1 = Item.get(P1Item1ID) : P1I1;
    }
    public Item getP1Item2() {
        return P1Item2ID != null && P1I2 == null ? P1I2 = Item.get(P1Item2ID) : P1I2;
    }
    public Item getP1Item3() {
        return P1Item3ID != null && P1I3 == null ? P1I3 = Item.get(P1Item3ID) : P1I3;
    }

    public Item getP2Item1() {
        return P2Item1ID != null && P2I1 == null ? P2I1 = Item.get(P2Item1ID) : P2I1;
    }
    public Item getP2Item2() {
        return P2Item2ID != null && P2I2 == null ? P2I2 = Item.get(P2Item2ID) : P2I2;
    }
    public Item getP2Item3() {
        return P2Item3ID != null && P2I3 == null ? P2I3 = Item.get(P2Item3ID) : P2I3;
    }

    public List<Item> getP1Drops() {
        List<Item> l = new ArrayList<>();
        if (getP1Item1() != null) l.add(getP1Item1());
        if (getP1Item2() != null) l.add(getP1Item2());
        if (getP1Item3() != null) l.add(getP1Item3());
        return l;
    }
    public List<Item> getP2Drops() {
        List<Item> l = new ArrayList<>();
        if (getP2Item1() != null) l.add(getP2Item1());
        if (getP2Item2() != null) l.add(getP2Item2());
        if (getP2Item3() != null) l.add(getP2Item3());
        return l;
    }

    public int CalculateXPReward(int P1Goals, int P2Goals, Profile opponent) throws Exception {
        double points;
        if (P1Goals > P2Goals) { // If you win
            points = 30;
            // "**30** *(Victory)*  ";
        } else if (P2Goals == P1Goals) { // If you tie
            points = 10;
            // "**10** (Tie)  ";
        } else {
            points = 5;
            // "**5** *(Defeat)* ";
        }

        int opponentlevel = opponent.Totals().Level;
        if (P1Goals >= 5 & P2Goals == 0) { // If Win = S Rank + 20
            points = (int) ((points * (1 + opponentlevel * 0.1)) * 2) + opponentlevel - 1;
            // "**x2** *(S Rank)*  ";
        } else if (P1Goals >= 3){ // If Win = A Rank +10
            points = (int) ((points * (1 + opponentlevel * 0.1)) * 1.5) + opponentlevel - 1;
            // "**x1.5** *(A Rank)*  ";
        } else {
            points = (int) (points * (1 + opponentlevel * 0.1)) + opponentlevel - 1;
        }

        if (opponent.getId() != getProfileP1().getId()) {
            if (getProfileP1().getBoosters().stream().anyMatch(b -> b.getBoosterType().equals("XP"))) {
                points = (int) (points * getProfileP1().getBoosters().stream().filter(b -> b.getBoosterType().equals("XP")).collect(Collectors.toList()).getFirst().getMultiplier());
            }
        } else if (opponent.getId() != getProfileP2().getId()) {
            if (getProfileP2().getBoosters().stream().anyMatch(b -> b.getBoosterType().equals("XP"))) {
                points = (int) (points * getProfileP2().getBoosters().stream().filter(b -> b.getBoosterType().equals("XP")).collect(Collectors.toList()).getFirst().getMultiplier());
            }
        }
        return (int) points;
    }
    public int CalculateMedalsReward(int P1Goals, int P2Goals, Profile_Game P1, Profile_Game P2) {
        double points;
        if (P1Goals > P2Goals) { // If you win
            points = Constants.GlobalRanking.getWinPts();
            if (P1.getLeague().getId() > P2.getLeague().getId()) {
                points -= (Constants.GlobalRanking.getLeagueDiffPts() * (P1.getLeague().getId() - P2.getLeague().getId()));
            } else if (P1.getLeague().getId() < P2.getLeague().getId()) {
                points += (Constants.GlobalRanking.getLeagueDiffPts() * (P2.getLeague().getId() - P1.getLeague().getId()));
            }
            points += GenerateRandomNumber(Constants.GlobalRanking.getMinRNG(), Constants.GlobalRanking.getMaxRNG());
            points *= Math.min(1 + (0.05 * P1.WinStreak), 1.5);
            points = Math.max(points, 5); // ensure points is more or equal to 5.
        } else if (P1Goals < P2Goals) { // If you loose
            points = Constants.GlobalRanking.getLosePts();
            if (P1.getLeague().getId() > P2.getLeague().getId()) {
                points -= (Constants.GlobalRanking.getLeagueDiffPts() * (P1.getLeague().getId() - P2.getLeague().getId()));
            } else if (P1.getLeague().getId() < P2.getLeague().getId()) {
                points += (Constants.GlobalRanking.getLeagueDiffPts() * (P2.getLeague().getId() - P1.getLeague().getId()));
            }
            points -= GenerateRandomNumber(Constants.GlobalRanking.getMinRNG(), Constants.GlobalRanking.getMaxRNG());
            points = Math.min(points, -1); // ensure points is less or equal to -1.
            if (0 >= P1.getMedals() + points) points = -P1.getMedals();
        } else {
            points = Constants.GlobalRanking.getTiePts();
        }
        return (int) points;
    }
    public int CalculateCoin(Profile_Game PG) {
        if (PG == getPGP1()) {
            return (int) ((getP1XP() * 0.2 + Math.max(getP1Medals(), 0)) * getProfileP1().Totals().getCoinMultiplier());
        } else if (PG == getPGP2()) {
            return (int) ((getP2XP() * 0.2 + Math.max(getP2Medals(), 0)) * getProfileP2().Totals().getCoinMultiplier());
        }
        return 0;
    }
    public int CalculateRainbow() {
        if (P1Score == 0 && P2Score >= 5) {
            return 20;
        } else if (P1Score + 3 < P2Score) {
            return 15;
        } else if (P1Score < P2Score) {
            return 10;
        } else {
            return 5;
        }
    }
    public void MakeDrops() {
        List<Item> D1 = MakeDrops(getPGP1());
        List<Item> D2 = MakeDrops(getPGP2());
        P1Item1ID = !D1.isEmpty() && D1.getFirst() != null ? D1.getFirst().getId() : null;
        P1Item2ID = D1.size() > 1 && D1.get(1) != null ? D1.get(1).getId() : null;
        P1Item3ID = D1.size() > 2 && D1.get(2) != null ? D1.get(2).getId() : null;
        P2Item1ID = !D2.isEmpty() && D2.getFirst() != null ? D2.getFirst().getId() : null;
        P2Item2ID = D2.size() > 1 && D2.get(1) != null ? D2.get(1).getId() : null;
        P2Item3ID = D2.size() > 2 && D2.get(2) != null ? D2.get(2).getId() : null;
    }
    public List<Item> MakeDrops(Profile_Game PG) {
        List<Item> drops = new ArrayList<>();
        LootTable lootTable = LootTable.GAME_LOOT_TABLES.get(GameCode);
        if (lootTable != null) {
            double DropChance = 0;
            if (PG == getPGP1()) {
                if (P1Score >= P2Score) {
                    DropChance = 10; // 10% chance drop if win or tie
                    if (P1Score > P2Score) {
                        DropChance = 20; // 20% chance drop if win
                        if (P1Score >= 3) {
                            DropChance = 25; // 25% chance drop if win with 3 goals
                            if (P1Score >= 5 && P2Score == 0) {
                                DropChance = 30; // 30% chance drop if win with S Rank
                            }
                        }
                    }
                    DropChance += PG.getLeague().getId(); // +1% chance drop per league.
                    DropChance *= Math.min(1 + (0.05 * PG.WinStreak), 1.5); // x1.05% chance drop per streak.
                }
            } else if (PG == getPGP2()) {
                if (P2Score >= P1Score) {
                    DropChance = 10; // 10% chance drop if win or tie
                    if (P2Score > P1Score) {
                        DropChance = 20; // 20% chance drop if win
                        if (P2Score >= 3) {
                            DropChance = 25; // 25% chance drop if win with 3 goals
                            if (P2Score >= 5 && P1Score == 0) {
                                DropChance = 30; // 30% chance drop if win with S Rank
                            }
                        }
                    }
                    DropChance += PG.getLeague().getId(); // +1% chance drop per league.
                    DropChance *= Math.min(1 + (0.05 * PG.WinStreak), 1.5); // x1.05% chance drop per streak.
                }
            }

            for (int ThreeDrops = 0; ThreeDrops < 3; ThreeDrops++) {
                if (Range(GenerateRandomNumber(1, 100), 1, DropChance)) {
                    String itemName = lootTable.pickRandomItem();
                    drops.add(Item.get(itemName));
                }
            }
        }
        return drops;
    }
    public void giveRewards() {
        p1 = null;
        p2 = null;
        Profile P1 = getProfileP1();
        Profile P2 = getProfileP2();
        if (P1Score > P2Score) {
            P1.IncrementProgress("WIN_MATCH/null", 1);
            P1.IncrementProgress("WIN_MATCH/" + getGame(), 1);

            P2.IncrementProgress("LOSE_MATCH_" + P1ID + "/null", 1);
            P2.IncrementProgress("LOSE_MATCH_" + P1ID + "/" + getGame(), 1);

            P1.IncrementProgress("WIN_DUEL_" + P2ID + "/null", 1);
            P1.IncrementProgress("WIN_DUEL_" + P2ID + "/" + getGame(), 1);

            P2.IncrementProgress("LOSE_DUEL_" + P1ID + "/null", 1);
            P2.IncrementProgress("LOSE_DUEL_" + P1ID + "/" + getGame(), 1);

            getPGP1().AddScoreStats(1,0,0, P1Score, P2Score, getP1Medals(), 1);
            getPGP2().AddScoreStats(0,0,1, P2Score, P1Score, getP2Medals(), 0);
            getPGP2().setLastStreak(getPGP2().getLastStreak());
            getPGP2().setWinStreak(0);
            if (getPGP1().getWinStreak() > getPGP1().getHighestWinStreak()) {
                getPGP1().setHighestWinStreak(getPGP1().getWinStreak());
            }
         } else if (P1Score < P2Score) {
            P2.IncrementProgress("WIN_MATCH/null", 1);
            P2.IncrementProgress("WIN_MATCH/" + getGame(), 1);

            P1.IncrementProgress("LOSE_MATCH_" + P2ID + "/null", 1);
            P1.IncrementProgress("LOSE_MATCH_" + P2ID + "/" + getGame(), 1);

            P2.IncrementProgress("WIN_DUEL_" + P1ID + "/null", 1);
            P2.IncrementProgress("WIN_DUEL_" + P1ID + "/" + getGame(), 1);

            P1.IncrementProgress("LOSE_DUEL_" + P2ID + "/null", 1);
            P1.IncrementProgress("LOSE_DUEL_" + P2ID + "/" + getGame(), 1);

            getPGP2().AddScoreStats(1,0,0, P2Score, P1Score, getP2Medals(), 1);
            getPGP1().AddScoreStats(0,0,1, P1Score, P2Score, getP1Medals(), 0);
            getPGP1().setLastStreak(getPGP1().getLastStreak());
            getPGP1().setWinStreak(0);
            if (getPGP2().getWinStreak() > getPGP2().getHighestWinStreak()) {
                getPGP2().setHighestWinStreak(getPGP2().getWinStreak());
            }
        } else {
            P1.IncrementProgress("TIE_MATCH/null", 1);
            P1.IncrementProgress("TIE_MATCH/" + getGame(), 1);
            P2.IncrementProgress("TIE_MATCH/null", 1);
            P2.IncrementProgress("TIE_MATCH/" + getGame(), 1);

            P1.IncrementProgress("TIE_DUEL_" + P2ID + "/null", 1);
            P1.IncrementProgress("TIE_DUEL_" + P2ID + "/" + getGame(), 1);
            P2.IncrementProgress("TIE_DUEL_" + P1ID + "/null", 1);
            P2.IncrementProgress("TIE_DUEL_" + P1ID + "/" + getGame(), 1);

            getPGP1().AddScoreStats(0,1,0, P1Score, P2Score, getP1Medals(), 0);
            getPGP2().AddScoreStats(0,1,0, P2Score, P1Score, getP2Medals(), 0);
        }

        getPGP1().UpdateOnly("WinStreak", "LastStreak", "HighestWinStreak");
        getPGP2().UpdateOnly("WinStreak", "LastStreak", "HighestWinStreak");

        P1.addItem(1, P1Coins);
        P2.addItem(1, P2Coins);
        P1.addItem(2, P1RainbowCoins);
        P2.addItem(2, P2RainbowCoins);
        for (Item i : getP1Drops()) {
            if (i.getType().equals(Item.ItemType.INSTANT_USE)) {
                if (i.getId() == 3) { // Quest
                    Profile_Quest.GenerateRandomQuestForUser(DiscordAccount.getGuildById(ServerID), getGame(), getPGP1());
                }
            } else P1.addItem(i.getId(), 1);
        }
        for (Item i : getP2Drops()) {
            if (i.getType().equals(Item.ItemType.INSTANT_USE)) {
                if (i.getId() == 3) { // Quest
                    Profile_Quest.GenerateRandomQuestForUser(DiscordAccount.getGuildById(ServerID), getGame(), getPGP2());
                }
            } else P1.addItem(i.getId(), 1);
        }

        P1.Totals().LevelUp(P1XP);
        P2.Totals().LevelUp(P2XP);

        P1.IncrementProgress("PLAY_MATCH/null", 1);
        P1.IncrementProgress("PLAY_MATCH/" + getGame(), 1);
        P2.IncrementProgress("PLAY_MATCH/null", 1);
        P2.IncrementProgress("PLAY_MATCH/" + getGame(), 1);

        P1.IncrementProgress("SCORE_GOAL/null", P1Score);
        P1.IncrementProgress("SCORE_GOAL/" + getGame(), P1Score);
        P2.IncrementProgress("SCORE_GOAL/null", P2Score);
        P2.IncrementProgress("SCORE_GOAL/" + getGame(), P2Score);

        P1.IncrementProgress("DUEL_" + P2ID + "/null", 1);
        P1.IncrementProgress("DUEL_" + P2ID + "/" + getGame(), 1);
        P2.IncrementProgress("DUEL_" + P1ID + "/null", 1);
        P2.IncrementProgress("DUEL_" + P1ID + "/" + getGame(), 1);

        P1.UpdateAchievement();
        P2.UpdateAchievement();
    }
    public void removeRewards() {
        if (P1Score > P2Score) {
            getProfileP1().IncrementProgress("WIN_MATCH/null", -1);
            getProfileP1().IncrementProgress("WIN_MATCH/" + getGame(), -1);

            getProfileP2().IncrementProgress("LOSE_MATCH_" + P1ID + "/null", -1);
            getProfileP2().IncrementProgress("LOSE_MATCH_" + P1ID + "/" + getGame(), -1);

            getProfileP1().IncrementProgress("WIN_DUEL_" + P2ID + "/null", -1);
            getProfileP1().IncrementProgress("WIN_DUEL_" + P2ID + "/" + getGame(), -1);

            getProfileP2().IncrementProgress("LOSE_DUEL_" + P1ID + "/null", -1);
            getProfileP2().IncrementProgress("LOSE_DUEL_" + P1ID + "/" + getGame(), -1);

            getPGP1().AddScoreStats(-1,0,0, -P1Score, -P2Score, -getP1Medals(), -1);
            getPGP2().AddScoreStats(0,0,-1, -P2Score, -P1Score, -getP2Medals(), 0);
        } else if (P1Score < P2Score) {
            getProfileP2().IncrementProgress("WIN_MATCH/null", -1);
            getProfileP2().IncrementProgress("WIN_MATCH/" + getGame(), -1);

            getProfileP1().IncrementProgress("LOSE_MATCH_" + P2ID + "/null", -1);
            getProfileP1().IncrementProgress("LOSE_MATCH_" + P2ID + "/" + getGame(), -1);

            getProfileP2().IncrementProgress("WIN_DUEL_" + P1ID + "/null", -1);
            getProfileP2().IncrementProgress("WIN_DUEL_" + P1ID + "/" + getGame(), -1);

            getProfileP1().IncrementProgress("LOSE_DUEL_" + P2ID + "/null", -1);
            getProfileP1().IncrementProgress("LOSE_DUEL_" + P2ID + "/" + getGame(), -1);

            getPGP2().AddScoreStats(-1,0,0, -P2Score, -P1Score, -getP2Medals(), -1);
            getPGP1().AddScoreStats(0,0,-1, -P1Score, -P2Score, -getP1Medals(), 0);
        } else {
            getProfileP1().IncrementProgress("TIE_MATCH/null", -1);
            getProfileP1().IncrementProgress("TIE_MATCH/" + getGame(), -1);
            getProfileP2().IncrementProgress("TIE_MATCH/null", -1);
            getProfileP2().IncrementProgress("TIE_MATCH/" + getGame(), -1);

            getProfileP1().IncrementProgress("TIE_DUEL_" + P2ID + "/null", -1);
            getProfileP1().IncrementProgress("TIE_DUEL_" + P2ID + "/" + getGame(), -1);
            getProfileP2().IncrementProgress("TIE_DUEL_" + P1ID + "/null", -1);
            getProfileP2().IncrementProgress("TIE_DUEL_" + P1ID + "/" + getGame(), -1);

            getPGP1().AddScoreStats(0,-1,0, -P1Score, -P2Score, -getP1Medals(), 0);
            getPGP2().AddScoreStats(0,-1,0, -P2Score, -P1Score, -getP2Medals(), 0);
        }

        getProfileP1().removeItem(1, P1Coins);
        getProfileP2().removeItem(1, P2Coins);
        getProfileP1().removeItem(2, P1RainbowCoins);
        getProfileP2().removeItem(2, P2RainbowCoins);
        for (Item i : getP1Drops()) {
            getProfileP1().removeItem(i.getId(), 1);
        }
        for (Item i : getP2Drops()) {
            getProfileP2().removeItem(i.getId(), 1);
        }


        getProfileP1().Totals().LevelUp(-P1XP);
        getProfileP2().Totals().LevelUp(-P2XP);

        getProfileP1().IncrementProgress("PLAY_MATCH/null", -1);
        getProfileP1().IncrementProgress("PLAY_MATCH/" + getGame(), -1);
        getProfileP2().IncrementProgress("PLAY_MATCH/null", -1);
        getProfileP2().IncrementProgress("PLAY_MATCH/" + getGame(), -1);

        getProfileP1().IncrementProgress("SCORE_GOAL/null", -P1Score);
        getProfileP1().IncrementProgress("SCORE_GOAL/" + getGame(), -P1Score);
        getProfileP2().IncrementProgress("SCORE_GOAL/null", -P2Score);
        getProfileP2().IncrementProgress("SCORE_GOAL/" + getGame(), -P2Score);

        getProfileP1().IncrementProgress("DUEL_" + P2ID + "/null", -1);
        getProfileP1().IncrementProgress("DUEL_" + P2ID + "/" + getGame(), -1);
        getProfileP2().IncrementProgress("DUEL_" + P1ID + "/null", -1);
        getProfileP2().IncrementProgress("DUEL_" + P1ID + "/" + getGame(), -1);
    }


    public MatchLog() {}
    public MatchLog(Game game, long p1ID, long p2ID, int p1Score, int p2Score, Message M, User winnertie) {
        ID = Instant.now().toEpochMilli();
        GameCode = game.getCode();
        P1ID = p1ID;
        P2ID = p2ID;
        P1Score = p1Score;
        P2Score = p2Score;
        ServerID = M.isFromGuild() ? M.getGuildIdLong() : null;
        ChannelID = M.getChannelIdLong();
        MessageID = M.getIdLong();
        TiedWinner = winnertie != null ? winnertie.getIdLong() : null;
        Write();
    }
    public void makeRewards(BaseTournament<?> T) throws Exception {
        P1XP = CalculateXPReward(P1Score, P2Score, getProfileP2()) / (T == null ? 1 : T.getVSAmount());
        P2XP = CalculateXPReward(P2Score, P1Score, getProfileP1()) / (T == null ? 1 : T.getVSAmount());
        P1Medals = CalculateMedalsReward(P1Score, P2Score, getPGP1(), getPGP2()) / (T == null ? 1 : T.getVSAmount());
        P2Medals = CalculateMedalsReward(P2Score, P1Score, getPGP2(), getPGP1()) / (T == null ? 1 : T.getVSAmount());
        P1Coins = CalculateCoin(getPGP1()) / (T == null ? 1 : T.getVSAmount());
        P2Coins = CalculateCoin(getPGP2()) / (T == null ? 1 : T.getVSAmount());
        P1RainbowCoins = CalculateRainbow() / (T == null ? 1 : T.getVSAmount());
        P2RainbowCoins = CalculateRainbow() / (T == null ? 1 : T.getVSAmount());
        MakeDrops();
    }

    public static List<MatchLog> getUncomplete() {
        return getAllWhere(MatchLog.class, "NOT State = ? ORDER BY ID DESC", MatchState.COMPLETE.toString());
    }

    public static MatchLog getByMessage(long messageId) {
        return getWhere(MatchLog.class, "MessageID = ?", messageId).orElse(null);
    }
    public static MatchLog getLog(long p1, long p2, int score1, int score2, Game game) {
        return getWhere(MatchLog.class, "((P1Score = ? AND P2Score = ? AND P1ID = ? AND P2ID = ?) OR (P1Score = ? AND P2Score = ? AND P1ID = ? AND P2ID = ?)) AND GameCode = ? ORDER BY ID DESC"
                , score1, score2, p1, p2, score2, score1, p2, p1, game.getCode()).orElse(null);
    }
    public static MatchLog getRandom() {
        return getRandom(MatchLog.class, "TRUE ORDER BY ID DESC LIMIT 3");
    }

    public static List<MatchLog> getMatchesOf(long userid, Long serverid, List<Game> games, int pages, int amountperpages) {
        return doQueryAll(MatchLog.class, "CALL DisplayUserHistory(?,?,?,?,?);", userid, serverid, games == null || games.isEmpty() ? null : games.stream().map(Game::getCode).collect(Collectors.joining(",")), pages, amountperpages);
    }

    public boolean isTied() {
        return P1Score == P2Score;
    }

    public void confirmBet() {
        Bet B = Bet.getIncomplete(P1ID, P2ID);
        if (B != null) B.EndBet(this);
    }




}
