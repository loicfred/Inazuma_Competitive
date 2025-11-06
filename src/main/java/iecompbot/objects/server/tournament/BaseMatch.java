package iecompbot.objects.server.tournament;

import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;
import java.util.List;

public class BaseMatch<M> extends DatabaseObject<M> {
    protected transient List<SubMatch> CMs;

    public long ID;
    public long TournamentID;
    public Long MatchLogID;
    public int P1Score;
    public int P2Score;
    public int P1Votes;
    public int P2Votes;
    public long DeadlineEpochSecond;
    public Long WinnerId = null;
    public Long LooserId = null;

    public long getId() {
        return ID;
    }
    public long getTournamentID() {
        return TournamentID;
    }
    public long getMatchLogID() {
        return MatchLogID;
    }
    public int getP1Score() {
        return P1Score;
    }
    public int getP2Score() {
        return P2Score;
    }
    public Instant getDeadline() {
        if (DeadlineEpochSecond == 0) return null;
        return Instant.ofEpochSecond(DeadlineEpochSecond);
    }
    public Long getWinnerId() {
        return WinnerId;
    }
    public Long getLooserId() {
        return LooserId;
    }

    public boolean isTied() {
        return getP1Score() == getP2Score();
    }



    public void setDeadline(Instant time) {
        DeadlineEpochSecond = time.getEpochSecond();
    }

    public List<SubMatch> getSubMatches() {
        return CMs == null ? CMs = SubMatch.get(this) : CMs;
    }
    public SubMatch getSubMatch(long p1id, long p2id) {
        return getSubMatches().stream().filter(SM -> (SM.getP1ID() == p1id && SM.getP2ID() == p2id) || (SM.getP2ID() == p1id && SM.getP1ID() == p2id)).findFirst().orElse(null);
    }
    public SubMatch getSubMatch(long userid) {
        return getSubMatches().stream().filter(SM -> SM.getP1ID() == userid || SM.getP2ID() == userid).findFirst().orElse(null);
    }
    public SubMatch getSubMatchByLog(long matchlog) {
        return getSubMatches().stream().filter(SM -> SM.MatchLogID == matchlog).findFirst().orElse(null);
    }
}
