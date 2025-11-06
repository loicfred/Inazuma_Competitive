package iecompbot.objects.match;

import at.stefangeyer.challonge.model.enumeration.MatchState;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.profile_game.Profile_Game_S;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.BaseTournament;
import net.dv8tion.jda.api.entities.Message;

import java.time.Instant;
import java.util.List;

import static my.utilities.util.Utilities.GenerateRandomNumber;

public class MatchLog_S extends BaseMatchLog<MatchLog_S> {
    private transient ServerInfo I;
    private transient Profile_Game_S p1g;
    private transient Profile_Game_S p2g;

    public Long P1Item1ID;
    public Long P2Item1ID;
    public int P1Item1Amount = 0;
    public int P2Item1Amount = 0;

    public void complete(MatchState s, long messageID) {
        State = s.toString();
        MessageID = messageID;
        Update();
    }

    public Profile_Game_S getPGP1() {
        return p1g == null ? p1g = Profile_Game_S.get(getP1ID(), getServerID(), getGame()) : p1g;
    }
    public Profile_Game_S getPGP2() {
        return p2g == null ? p2g = Profile_Game_S.get(getP2ID(), getServerID(), getGame()) : p2g;
    }
    public ServerInfo getServerInfo() {
        return I == null ? I = ServerInfo.get(ServerID) : I;
    }

    public int CalculateMedalsReward(int P1Goals, int P2Goals, Profile_Game_S P1, Profile_Game_S P2) {
        I = getServerInfo();
        double points;
        if (P1Goals > P2Goals) { // If you win
            points = I.Ranking().getWinPts();
            if (P2.getLeague(I).getId() > P1.getLeague(I).getId()) {
                points += (I.Ranking().getLeagueDiffPts() * (P2.getLeague(I).getId() - P1.getLeague(I).getId()));
            } else if (P2.getLeague(I).getId() < P1.getLeague(I).getId()) {
                points -= (I.Ranking().getLeagueDiffPts() * (P1.getLeague(I).getId() - P2.getLeague(I).getId()));
            }
            points += GenerateRandomNumber(I.Ranking().getMinRNG(), I.Ranking().getMaxRNG());
            points *= Math.min(1 + (0.05 * P1.WinStreak), 1.5);
            points = Math.max(points, 5); // ensure points is more or equal to 5.
        } else if (P1Goals < P2Goals) { // If you loose
            points = I.Ranking().getLosePts();
            if (P2.getLeague(I).getId() > P1.getLeague(I).getId()) {
                points -= (I.Ranking().getLeagueDiffPts() * (P2.getLeague(I).getId() - P1.getLeague(I).getId()));
            } else if (P2.getLeague(I).getId() < P1.getLeague(I).getId()) {
                points += (I.Ranking().getLeagueDiffPts() * (P1.getLeague(I).getId() - P2.getLeague(I).getId()));
            }
            points -= GenerateRandomNumber(I.Ranking().getMinRNG(), I.Ranking().getMaxRNG());
            points = Math.min(points, -1); // ensure points is less or equal to -1.
            if (0 >= P1.getMedals() + points) points = -P1.getMedals();
        } else {
            points = I.Ranking().getTiePts();
        }
        return (int) points;
    }
    public void giveRewards() {
        if (P1Score > P2Score) {
            getPGP1().AddScoreStats(1,0,0, P1Score, P2Score, getP1Medals(), 1);
            getPGP2().AddScoreStats(0,0,1, P2Score, P1Score, getP2Medals(), 0);
            getPGP2().setLastStreak(getPGP2().getWinStreak());
            getPGP2().setWinStreak(0);
            if (getPGP1().getWinStreak() > getPGP1().getHighestWinStreak()) {
                getPGP1().setHighestWinStreak(getPGP1().getWinStreak());
            }
        } else if (P1Score < P2Score) {
            getPGP2().AddScoreStats(1,0,0, P2Score, P1Score, getP2Medals(), 1);
            getPGP1().AddScoreStats(0,0,1, P1Score, P2Score, getP1Medals(), 0);
            getPGP1().setLastStreak(getPGP1().getWinStreak());
            getPGP1().setWinStreak(0);
            if (getPGP2().getWinStreak() > getPGP2().getHighestWinStreak()) {
                getPGP2().setHighestWinStreak(getPGP2().getWinStreak());
            }
        } else {
            getPGP1().AddScoreStats(0,1,0, P1Score, P2Score, getP1Medals(), 0);
            getPGP2().AddScoreStats(0,1,0, P2Score, P1Score, getP2Medals(), 0);
        }
        getPGP1().UpdateOnly("WinStreak", "LastStreak", "HighestWinStreak");
        getPGP2().UpdateOnly("WinStreak", "LastStreak", "HighestWinStreak");
    }
    public void removeRewards() {
        if (P1Score > P2Score) {
            getPGP1().AddScoreStats(-1,0,0, -P1Score, -P2Score, -getP1Medals(), -1);
            getPGP2().AddScoreStats(0,0,-1, -P2Score, -P1Score, -getP2Medals(), 0);
        } else if (P1Score < P2Score) {
            getPGP2().AddScoreStats(-1,0,0, -P2Score, -P1Score, -getP2Medals(), -1);
            getPGP1().AddScoreStats(0,0,-1, -P1Score, -P2Score, -getP1Medals(), 0);
        } else {
            getPGP1().AddScoreStats(0,-1,0, -P1Score, -P2Score, -getP1Medals(), 0);
            getPGP2().AddScoreStats(0,-1,0, -P2Score, -P1Score, -getP2Medals(), 0);
        }
    }

    public MatchLog_S() {}
    public MatchLog_S(Game game, long p1ID, long p2ID, int p1Score, int p2Score, Message M) {
        ID = Instant.now().toEpochMilli();
        GameCode = game.getCode();
        P1ID = p1ID;
        P2ID = p2ID;
        P1Score = p1Score;
        P2Score = p2Score;
        ServerID = M.getGuildIdLong();
        ChannelID = M.getChannelIdLong();
        MessageID = M.getIdLong();
        Write();
    }
    public void makeRewards(BaseTournament<?> T) {
        P1Medals = CalculateMedalsReward(P1Score, P2Score, getPGP1(), getPGP2()) / (T == null ? 1 : T.getVSAmount());
        P2Medals = CalculateMedalsReward(P2Score, P1Score, getPGP2(), getPGP1()) / (T == null ? 1 : T.getVSAmount());
        I = getServerInfo();
        if (I.getCurrency() != null) {
            if (P1Score > P2Score) {
                Profile P1 = getProfileP1();
                P1Item1ID = I.getCurrency().getId();
                P1Item1Amount = I.getCurrencyPerWin();
                P1.addItem(P1Item1ID, P1Item1Amount);
            } else if (P1Score < P2Score) {
                Profile P2 = getProfileP2();
                P2Item1ID = I.getCurrency().getId();
                P2Item1Amount = I.getCurrencyPerWin();
                P2.addItem(P2Item1ID, P2Item1Amount);
            }
        }
    }

    public static List<MatchLog_S> getUncomplete() {
        return getAllWhere(MatchLog_S.class, "NOT State = ? ORDER BY ID DESC", MatchState.COMPLETE.toString());
    }

    public static MatchLog_S getByMessage(long messageId) {
        return getWhere(MatchLog_S.class, "MessageID = ?", messageId).orElse(null);
    }
    public static MatchLog_S getLog(long p1, long p2, int score1, int score2, Game game) {
        return getWhere(MatchLog_S.class, "((P1Score = ? AND P2Score = ? AND P1ID = ? AND P2ID = ?) OR (P1Score = ? AND P2Score = ? AND P1ID = ? AND P2ID = ?)) AND GameCode = ?"
                , score1, score2, p1, p2, score2, score1, p2, p1, game.getCode()).orElse(null);
    }
    public static MatchLog_S getRandom() {
        return getRandom(MatchLog_S.class, "TRUE ORDER BY ID DESC LIMIT 3");
    }

}
