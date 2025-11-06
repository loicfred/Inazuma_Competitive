package iecompbot.objects.match;

import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;
import java.util.List;

public class Bet_Voter extends DatabaseObject<Bet_Voter> {

    private long ID;
    private long BetID;
    private long VoterID;
    private long CandidateID;
    private int Amount = 0;

    public Bet_Voter() {}
    public Bet_Voter(long betID, long voterID, long candidateID, int amount) {
        ID = Instant.now().toEpochMilli();
        BetID = betID;
        VoterID = voterID;
        CandidateID = candidateID;
        Amount = amount;
        Write();
    }

    public long getId() {
        return ID;
    }
    public long getBetID() {
        return BetID;
    }
    public long getVoterID() {
        return VoterID;
    }
    public long getCandidateID() {
        return CandidateID;
    }
    public int getAmount() {
        return Amount;
    }

    public static List<Bet_Voter> get(long betId) {
        return getAllWhere(Bet_Voter.class, "BetID = ?", betId);
    }
    public static Bet_Voter get(long betId, long userid) {
        return getWhere(Bet_Voter.class, "BetID = ? AND VoterID = ?", betId, userid).orElse(null);
    }

    public void setAmount(int amount) {
        Amount = amount;
        Update();
    }

}
