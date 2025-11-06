package iecompbot.objects.server.tournament;

import at.stefangeyer.challonge.model.enumeration.MatchState;
import iecompbot.objects.match.BaseDuel;
import iecompbot.objects.match.MatchLog;

import java.time.Instant;
import java.util.List;

public class SubMatch extends BaseDuel<SubMatch> {

    public Long MatchID;
    public Long MatchLogID;

    public SubMatch() {}
    public SubMatch(BaseMatch<?> M, long p1id, long p2id) {
        this.ID = Instant.now().toEpochMilli();
        this.MatchID = M.getId();
        P1ID = p1id;
        P2ID = p2id;
        Write();
    }

    public void reopenMatch() {
        P1Score = 0;
        P2Score = 0;
        MatchLogID = null;
        State = MatchState.PENDING.toString();
        Update();
    }

    public void setState(MatchState state) {
        State = state.toString();
        UpdateOnly("State");
    }

    public void setMatchLog(MatchLog ml) {
        MatchLogID = ml.getId();
        UpdateOnly("MatchLogID");
    }

    public static List<SubMatch> get(BaseMatch<?> M) {
        return getAllWhere(SubMatch.class, "MatchID = ?", M.getId());
    }
    public static SubMatch get(long id) {
        return getById(SubMatch.class, id).orElse(null);
    }
}
