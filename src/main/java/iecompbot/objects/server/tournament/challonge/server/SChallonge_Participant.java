package iecompbot.objects.server.tournament.challonge.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.server.tournament.challonge.BaseCParticipant;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class SChallonge_Participant extends BaseCParticipant<SChallonge_Tournament, SChallonge_Match, SChallonge_Participant> {

    public SChallonge_Participant() {}
    public SChallonge_Participant(SChallonge_Tournament T, at.stefangeyer.challonge.model.Participant P, Long discordid) {
        this.T = T;
        this.P = P;
        this.ID = P.getId();
        this.DiscordID = discordid;
        this.TournamentID = T.getId();
        RefreshParticipant(P);
        Write();
    }

    public static List<SChallonge_Participant> list() {
        return getAll(SChallonge_Participant.class);
    }
    public static List<SChallonge_Participant> getOfUser(long userid) {
        return getAllWhere(SChallonge_Participant.class, "ID = ? OR DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ? ORDER BY Position ASC", userid, userid, userid, userid, userid, userid, userid, userid, userid);
    }
    public static List<SChallonge_Participant> ofTournament(SChallonge_Tournament T) {
        List<SChallonge_Participant> P = getAllWhere(SChallonge_Participant.class, "TournamentID = ? ORDER BY Position ASC", T.getId());
        for (SChallonge_Participant CM : P) {
            CM.T = T;
        }
        return P;
    }
    public static List<SChallonge_Participant> ofTournament(long tournamentid) {
        return getAllWhere(SChallonge_Participant.class, "TournamentID = ?", tournamentid);
    }
    public static SChallonge_Participant get(SChallonge_Tournament T, long userid) {
        SChallonge_Participant P = getWhere(SChallonge_Participant.class, "TournamentID = ? AND (ID = ? OR DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ?)", T.getId(), userid, userid, userid, userid, userid, userid, userid, userid, userid).orElse(null);
        if (P == null) return null;
        P.T = T;
        return P;
    }

    @Override
    public SChallonge_Tournament getTournament() {
        return T == null ? T = SChallonge_Tournament.get(getTournamentID()) : T;
    }
}
