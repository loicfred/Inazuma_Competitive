package iecompbot.objects.server.tournament.challonge;

import at.stefangeyer.challonge.exception.DataAccessException;
import at.stefangeyer.challonge.model.query.ParticipantQuery;
import iecompbot.objects.server.tournament.BaseParticipant;
import iecompbot.objects.server.tournament.SubMatch;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.Main.ChallongeAccount;
import static my.utilities.util.Utilities.isNumeric;

@DatabaseObject.TableName(value = "challonge_participant")
public abstract class BaseCParticipant<T extends BaseCTournament<T, M, P>, M extends BaseCMatch<T, M, P>, P extends BaseCParticipant<T, M, P>> extends BaseParticipant<P> {
    protected transient at.stefangeyer.challonge.model.Participant P;
    public transient T T;

    public long GroupStageParticipantID = 0;
    public long GroupId;
    public String DisplayName;
    public String ChallongeUsername;
    public Integer Seed;
    public boolean hasIrrelevantSeed;
    public long CheckedInTimeEpochSecond;
    public long CreatedAtTimeEpochSecond;
    public long UpdatedAtTimeEpochSecond;
    public String Misc;

    public long getGroupStageParticipantID() {
        return GroupStageParticipantID;
    }
    public long getGroupId() {
        return GroupId;
    }
    public String getDisplayName() {
        return DisplayName;
    }
    public String getChallongeUsername() {
        return ChallongeUsername;
    }
    public Integer getSeed() {
        return Seed;
    }
    public boolean hasIrrelevantSeed() {
        return hasIrrelevantSeed;
    }
    public long getCheckedInTimeEpochSecond() {
        return CheckedInTimeEpochSecond;
    }
    public long getCreatedAtTimeEpochSecond() {
        return CreatedAtTimeEpochSecond;
    }
    public long getUpdatedAtTimeEpochSecond() {
        return UpdatedAtTimeEpochSecond;
    }
    public String getMisc() {
        return Misc;
    }

    public List<Long> getParticipantIDs() {
        List<Long> L = new ArrayList<>();
        L.add(ID);
        if (getTournament().isGroupStageEnabled()) {
            L.add(GroupStageParticipantID);
        }
        return L;
    }

    public void setName(String name) throws Exception {
        P = ChallongeAccount.updateParticipant(getParticipant(), ParticipantQuery.builder().name(name).build());
        Name = name;
    }
    public void setSeed(Integer seed) {
        try {
            P = ChallongeAccount.updateParticipant(getParticipant(), ParticipantQuery.builder().seed(seed).build());
        } catch (Exception ignored) {}
        Seed = P.getSeed();
    }
    public void setLeaderID(long discordID) {
        DiscordID = discordID;
        try {
            P = ChallongeAccount.updateParticipant(getParticipant(), ParticipantQuery.builder().misc(String.valueOf(DiscordID)).build());
        } catch (DataAccessException ignored) {}
    }
    public void AddTeammateID(long id) {
        if (DiscordID2 == null) DiscordID2 = id;
        else if (DiscordID3 == null) DiscordID3 = id;
        else if (DiscordID4 == null) DiscordID4 = id;
        else if (DiscordID5 == null) DiscordID5 = id;
        else if (DiscordID6 == null) DiscordID6 = id;
        else if (DiscordID7 == null) DiscordID7 = id;
        else if (DiscordID8 == null) DiscordID8 = id;
    }
    public boolean RemoveTeammate(long id) {
        boolean A = getAllTeammatesIDs().contains(id);
        if (DiscordID2 != null && DiscordID2 == id) DiscordID2 = null;
        if (DiscordID3 != null && DiscordID3 == id) DiscordID3 = null;
        if (DiscordID4 != null && DiscordID4 == id) DiscordID4 = null;
        if (DiscordID5 != null && DiscordID5 == id) DiscordID5 = null;
        if (DiscordID6 != null && DiscordID6 == id) DiscordID6 = null;
        if (DiscordID7 != null && DiscordID7 == id) DiscordID7 = null;
        if (DiscordID8 != null && DiscordID8 == id) DiscordID8 = null;
        return A;
    }
    public boolean isTeamFull() {
        return getAllTeammatesIDs().size() == T.getVSAmount();
    }

    public List<M> getDQMatches() {
        List<M> L = new ArrayList<>();
        for (M M : getTournament().getMatches()) {
            if (M.isCompleted() || M.isForfeited()) {
                if (M.getParticipantID1() != null && M.getParticipantID1() == getId()) {
                    if (M.getP1Score() == -1) {
                        L.add(M);
                    }
                } else if (M.getParticipantID2() != null && M.getParticipantID2() == getId()) {
                    if (M.getP2Score() == -1) {
                        L.add(M);
                    }
                }
            }
        }
        return L;
    }
    public List<M> getMatches() {
        List<M> L = new ArrayList<>();
        for (M M : getTournament().getMatches()) {
            if (M.getParticipantID1() != null && M.getParticipantID1() == getId()) {
                L.add(M);
            } else if (M.getParticipantID2() != null && M.getParticipantID2() == getId()) {
                L.add(M);
            }
        }
        return L;
    }
    public List<M> getUnplayedMatches() {
        return getMatches().stream().filter(M -> !M.isCompleted()).collect(Collectors.toList());
    }
    public M getMatchWithOpponent(long opponentID, boolean isCompleted) {
        for (M M : getMatches()) {
            if (isCompleted || !M.isCompleted()) {
                if (M.getParticipantID1() != null && (M.getParticipantID1() == opponentID || M.getPlayer1().getAllTeammatesIDs().contains(opponentID))) {
                    return M;
                } else if (M.getParticipantID2() != null && (M.getParticipantID2() == opponentID || M.getPlayer2().getAllTeammatesIDs().contains(opponentID))) {
                    return M;
                }
            }
        }
        return null;
    }


    public List<SubMatch> getSubmatches(long id) {
        return getMatches().stream().map(M::getSubMatches).flatMap(List::stream).filter(M -> M.P1ID == id || M.P2ID == id).toList();
    }
    public int getTeamPlayerGoalsScored(long id) {
        return T.getVSAmount() > 1 ? getSubmatches(id).stream().map(BD -> BD.P1ID == id ? BD.getP1Score() : BD.P2ID == id ? BD.getP2Score() : 0).mapToInt(Integer::intValue).sum() : GoalsScored;
    }
    public int getTeamPlayerGoalsTaken(long id) {
        return T.getVSAmount() > 1 ? getSubmatches(id).stream().map(BD -> BD.P1ID == id ? BD.getP2Score() : BD.P2ID == id ? BD.getP1Score() : 0).mapToInt(Integer::intValue).sum() : GoalsTaken;
    }
    public int getTeamPlayerWins(long id) {
        return T.getVSAmount() > 1 ? (int) getSubmatches(id).stream().filter(BD -> (BD.P1ID == id && BD.getP1Score() > BD.getP2Score()) || (BD.P2ID == id && BD.getP2Score() > BD.getP1Score())).count() : Wins;
    }
    public int getTeamPlayerTies(long id) {
        return T.getVSAmount() > 1 ? (int) getSubmatches(id).stream().filter(BD -> (BD.P1ID == id || BD.P2ID == id) && (BD.getP2Score() == BD.getP1Score())).count() : Ties;
    }
    public int getTeamPlayerLoses(long id) {
        return T.getVSAmount() > 1 ? (int) getSubmatches(id).stream().filter(BD -> (BD.P1ID == id && BD.getP1Score() < BD.getP2Score()) || (BD.P2ID == id && BD.getP2Score() < BD.getP1Score())).count() : Loses;
    }

    public EmbedBuilder getModificationUI(InteractionHook M) {
        EmbedBuilder E = T.getTournamentEmbed();
        E.setTitle(TL(M,"tournament-manager"));
        E.setDescription(TL(M,"challonge-manage-participant-success"));
        E.addField(TL(M,"Name"), getName(), false);
        E.addField(TL(M,"Discord-Account"), getLeader() != null ? getLeader().getAsMention() : "-", false);
        E.addField("Seed", String.valueOf(getSeed()), false);
        return E;
    }

    public void RefreshParticipant(at.stefangeyer.challonge.model.Participant P) {
        this.ID = P.getId();
        this.TournamentID = P.getTournamentId() != null ? P.getTournamentId() : 0;
        this.Name = P.getName() != null ? P.getName() : "";
        this.DisplayName = P.getDisplayName() != null ? P.getDisplayName() : "";
        this.ChallongeUsername = P.getChallongeUsername() != null ? P.getChallongeUsername() : "";
        this.Seed = P.getSeed() != null ? P.getSeed() : 0;
        this.hasIrrelevantSeed = P.getHasIrrelevantSeed() != null ? P.getHasIrrelevantSeed() : false;
        this.Misc = P.getMisc() != null ? P.getMisc() : "";
        this.CheckedInTimeEpochSecond = P.getCheckedInAt() != null ? P.getCheckedInAt().toEpochSecond() : 0;
        this.CreatedAtTimeEpochSecond = P.getCreatedAt() != null ? P.getCreatedAt().toEpochSecond() : 0;
        this.UpdatedAtTimeEpochSecond = P.getUpdatedAt() != null ? P.getUpdatedAt().toEpochSecond() : 0;
        this.Position = P.getFinalRank() != null ? P.getFinalRank() : 0;
        this.GroupId = P.getGroupId() != null ? P.getGroupId() : 0;
        if (this.DiscordID == null) this.DiscordID = this.Misc != null && isNumeric(this.Misc) ? Long.parseLong(this.Misc) : null;

        Wins = 0;
        Loses = 0;
        Ties = 0;
        GoalsTaken = 0;
        GoalsScored = 0;
        for (M M : getTournament().getMatches(true)) {
            if (getId() == M.getParticipantID1() || getId() == M.getParticipantID2()) {
                if (M.getWinnerId() != null && getId() == M.getWinnerId()) {
                    ++Wins;
                } else if (M.getLooserId() != null && getId() == M.getLooserId()) {
                    ++Loses;
                } else {
                    ++Ties;
                }
                if (getId() == M.getParticipantID1()) {
                    GoalsScored += Math.max(0, M.getP1Score());
                    GoalsTaken += Math.max(0, M.getP2Score());
                } else if (getId() == M.getParticipantID2()) {
                    GoalsTaken += Math.max(0, M.getP1Score());
                    GoalsScored += Math.max(0, M.getP2Score());
                }
            }
        }
    }

    public abstract T getTournament();
    public at.stefangeyer.challonge.model.Participant getParticipant() throws DataAccessException {
        return P == null ? P = ChallongeAccount.getParticipant(getTournament().getTournament(), getId()) : P;
    }
}
