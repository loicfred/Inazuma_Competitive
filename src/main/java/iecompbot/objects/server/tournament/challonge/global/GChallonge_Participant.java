package iecompbot.objects.server.tournament.challonge.global;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.server.tournament.challonge.BaseCParticipant;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class GChallonge_Participant extends BaseCParticipant<GChallonge_Tournament, GChallonge_Match, GChallonge_Participant> {

    public GChallonge_Participant() {}
    public GChallonge_Participant(GChallonge_Tournament T, at.stefangeyer.challonge.model.Participant P, Long discordid) {
        this.T = T;
        this.P = P;
        this.ID = P.getId();
        this.DiscordID = discordid;
        this.TournamentID = T.getId();
        RefreshParticipant(P);
        Write();
    }

    public static List<GChallonge_Participant> list() {
        return getAll(GChallonge_Participant.class);
    }
    public static List<GChallonge_Participant> getOfUser(long userid) {
        return getAllWhere(GChallonge_Participant.class, "ID = ? OR DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ? ORDER BY Position ASC", userid, userid, userid, userid, userid, userid, userid, userid, userid);
    }
    public static List<GChallonge_Participant> ofTournament(GChallonge_Tournament T) {
        List<GChallonge_Participant> P = getAllWhere(GChallonge_Participant.class, "TournamentID = ? ORDER BY Position ASC", T.getId());
        for (GChallonge_Participant CM : P) CM.T = T;
        return P;
    }
    public static List<GChallonge_Participant> ofTournament(long tournamentid) {
        return getAllWhere(GChallonge_Participant.class, "TournamentID = ?", tournamentid);
    }
    public static GChallonge_Participant get(GChallonge_Tournament T, long userid) {
        GChallonge_Participant P = getWhere(GChallonge_Participant.class, "TournamentID = ? AND (ID = ? OR DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ?)", T.getId(), userid, userid, userid, userid, userid, userid, userid, userid, userid).orElse(null);
        if (P == null) return null;
        P.T = T;
        return P;
    }

    @Override
    public GChallonge_Tournament getTournament() {
        return T == null ? T = GChallonge_Tournament.get(getTournamentID()) : T;
    }
}
