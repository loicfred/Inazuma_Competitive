package iecompbot.objects.server.tournament;

import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;
import java.util.List;

public class Challonge_WaitingList extends DatabaseObject<Challonge_WaitingList> {

    private long ID;
    private Long TournamentID;
    private Long UserID;

    public long getId() {
        return ID;
    }
    public long getTournamentId() {
        return TournamentID;
    }
    public long getUserId() {
        return UserID;
    }

    private Challonge_WaitingList() {}
    public Challonge_WaitingList(long tournamentID, long userID) {
        ID = Instant.now().toEpochMilli();
        TournamentID = tournamentID;
        UserID = userID;
        Write();
    }

    public static Challonge_WaitingList getNext(long tournamentID) {
        return getWhere(Challonge_WaitingList.class, "TournamentID = ? ORDER BY ID ASC", tournamentID).orElse(null);
    }
    public static Challonge_WaitingList get(long tournamentID, long userID) {
        return getWhere(Challonge_WaitingList.class, "TournamentID = ? AND UserID = ?", tournamentID, userID).orElse(null);
    }
    public static List<Challonge_WaitingList> get(long tournamentID) {
        return getAllWhere(Challonge_WaitingList.class, "TournamentID = ?", tournamentID);
    }

}
