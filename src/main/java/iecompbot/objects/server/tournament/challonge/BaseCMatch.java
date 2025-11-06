package iecompbot.objects.server.tournament.challonge;

import at.stefangeyer.challonge.exception.DataAccessException;
import at.stefangeyer.challonge.model.enumeration.MatchState;
import at.stefangeyer.challonge.model.query.MatchQuery;
import iecompbot.objects.match.BaseDuel;
import iecompbot.objects.match.MatchLog;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.tournament.BaseMatch;
import iecompbot.objects.server.tournament.SubMatch;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.Main.ChallongeAccount;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static my.utilities.util.Utilities.GenerateRandomNumber;

@DatabaseObject.TableName(value = "challonge_match")
public abstract class BaseCMatch<T extends BaseCTournament<T, M, P>, M extends BaseCMatch<T, M, P>, P extends BaseCParticipant<T, M, P>> extends BaseMatch<M> {
    protected transient at.stefangeyer.challonge.model.Match M;
    public transient T T;
    protected transient P P1;
    protected transient P P2;

    public Long GroupID;
    public int Round;
    public String Identifier;
    public String Location;
    public String State;
    public boolean isForfeited;
    public boolean isOptional;
    public Long ParticipantID1 = null;
    public Long ParticipantID2 = null;
    public long Player1PrerequisiteMatchID;
    public long Player2PrerequisiteMatchID;
    public boolean isP1PrerequisiteLoser;
    public boolean isP2PrerequisiteLoser;
    public long CreatedAtTimeEpochSecond;
    public long StartedAtTimeEpochSecond;
    public long UnderwayAtTimeEpochSecond;
    public long ScheduledTimeEpochSecond;
    public long UpdatedAtTimeEpochSecond;
    public long CompletedAtTimeEpochSecond;

    public Long getGroupID() {
        return GroupID;
    }
    public int getRound() {
        return Round;
    }
    public String getIdentifier() {
        return Identifier;
    }
    public String getLocation() {
        return Location;
    }
    public MatchState getState() {
        return State == null ? MatchState.PENDING : MatchState.valueOf(State.toUpperCase().replaceAll(" ","_"));
    }
    public boolean isForfeited() {
        return isForfeited;
    }
    public boolean isOptional() {
        return isOptional;
    }
    public Long getParticipantID1() {
        return ParticipantID1;
    }
    public Long getParticipantID2() {
        return ParticipantID2;
    }
    public int getP1Votes() {
        return P1Votes;
    }
    public int getP2Votes() {
        return P2Votes;
    }
    public long getDeadlineEpochSecond() {
        return DeadlineEpochSecond;
    }
    public long getPlayer1PrerequisiteMatchID() {
        return Player1PrerequisiteMatchID;
    }
    public long getPlayer2PrerequisiteMatchID() {
        return Player2PrerequisiteMatchID;
    }
    public boolean isP1PrerequisiteLoser() {
        return isP1PrerequisiteLoser;
    }
    public boolean isP2PrerequisiteLoser() {
        return isP2PrerequisiteLoser;
    }
    public Instant getCreatedAtTime() {
        return Instant.ofEpochSecond(CreatedAtTimeEpochSecond);
    }
    public Instant getStartedAtTime() {
        return Instant.ofEpochSecond(StartedAtTimeEpochSecond);
    }
    public Instant getUnderwayAtTime() {
        return Instant.ofEpochSecond(UnderwayAtTimeEpochSecond);
    }
    public Instant getScheduledTime() {
        return Instant.ofEpochSecond(ScheduledTimeEpochSecond);
    }
    public Instant getUpdatedAtTime() {
        return Instant.ofEpochSecond(UpdatedAtTimeEpochSecond);
    }
    public Instant getCompletedAtTime() {
        return Instant.ofEpochSecond(CompletedAtTimeEpochSecond);
    }
    public boolean isCompleted() {
        return getState().equals(MatchState.COMPLETE);
    }
    public boolean isOpen() {
        return getState().equals(MatchState.OPEN);
    }
    public boolean isPending() {
        return getState().equals(MatchState.PENDING);
    }


    public String getMatchProgress() {
        int i = 0;
        for (SubMatch M : getSubMatches()) {
            if (M.isFinished()) i++;
        }
        return i + "/" + getSubMatches().size();
    }
    public boolean canTeamDQ(P P) {
        if (P.getId() == getParticipantID1()) {
            return getTeam1GoalsSum() <= getTeam2GoalsSum() + 3;
        } else if (P.getId() == getParticipantID2()) {
            return getTeam1GoalsSum() + 3 >= getTeam2GoalsSum();
        } return false;
    }

    @NotNull
    protected String formatScore(String s) {
        if (s.contains("--")) {
            s = s.replaceAll("--", " - -");
        } else {
            if (s.charAt(0) == '-') {
                s = s.replaceFirst("-", "_").replaceAll("-", " - ");
                s = s.replaceFirst("_", "-");
            } else {
                s = s.replaceAll("-", " - ");
            }
        }
        return s;
    }
    public long getTeam1WinSum() {
        return getSubMatches().stream().filter((BaseDuel<?> BD) -> BD.getP1Score() > BD.getP2Score()).count();
    }
    public long getTeam2WinSum() {
        return getSubMatches().stream().filter((BaseDuel<?> BD) -> BD.getP1Score() < BD.getP2Score()).count();
    }
    public int getTeam1GoalsSum() {
        return getSubMatches().stream().map(BaseDuel::getP1Score).mapToInt(Integer::intValue).sum();
    }
    public int getTeam2GoalsSum() {
        return getSubMatches().stream().map(BaseDuel::getP2Score).mapToInt(Integer::intValue).sum();
    }

    public P getOpponent(Long participantId) {
        if (participantId == null) return null;
        if (Objects.equals(participantId, ParticipantID1)) {
            return getPlayer2();
        } else if (Objects.equals(participantId, ParticipantID2)) {
            return getPlayer1();
        }
        return null;
    }
    public P getPlayer1() {
        return getParticipantID1() != null && P1 == null ? P1 = T.getParticipantById(getParticipantID1()) : P1;
    }
    public P getPlayer2() {
        return getParticipantID2() != null && P2 == null ? P2 = T.getParticipantById(getParticipantID2()) : P2;
    }
    public P getWinner() {
        return getWinnerId() != null ? T.getParticipantById(getWinnerId()) : null;
    }
    public P getLooser() {
        return getLooserId() != null ? T.getParticipantById(getLooserId()) : null;
    }

    public String getRoundLong() {
        return T.getRoundLong(Round, GroupID);
    }
    public String getRoundShort() {
        return T.getRoundShort(Round, GroupID);
    }

    public void setMatchLog(MatchLog ml) {
        if (T.getVSAmount() == 1) {
            MatchLogID = ml.getId();
            UpdateOnly("MatchLogID");
        }
        else if (T.getVSAmount() > 1) {
            SubMatch CSM = getSubMatch(ml.getP1ID(), ml.getP2ID());
            if (CSM != null) CSM.setMatchLog(ml);
        }
    }

    public void validateTeamScores(Long forcedWinnerId) throws Exception {
        if (T.getVSAmount() > 1 && !isCompleted()) {
            CMs = null;
            if (getSubMatches().size() == T.getVSAmount() && getSubMatches().stream().allMatch(BaseDuel::isFinished)) {
                long P1Win = getSubMatches().stream().filter(SM -> SM.getP1Score() > SM.getP2Score()).count();
                long P2Win = getSubMatches().stream().filter(SM -> SM.getP1Score() < SM.getP2Score()).count();
                int GoalsSum1 = getTeam1GoalsSum(), GoalsSum2 = getTeam2GoalsSum();
                String scorecsv = getSubMatches().stream().map(SM -> SM.getP1Score() + "-" + SM.getP2Score()).collect(Collectors.joining(","));
                if (P1Win > P2Win) {
                    M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(scorecsv).winnerId(forcedWinnerId != null ? forcedWinnerId : getParticipantID1()).build());
                } else if (P1Win < P2Win) {
                    M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(scorecsv).winnerId(forcedWinnerId != null ? forcedWinnerId : getParticipantID2()).build());
                } else {
                    if (GoalsSum1 > GoalsSum2) {
                        M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(scorecsv).winnerId(forcedWinnerId != null ? forcedWinnerId : getParticipantID1()).build());
                    } else if (GoalsSum1 < GoalsSum2) {
                        M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(scorecsv).winnerId(forcedWinnerId != null ? forcedWinnerId : getParticipantID2()).build());
                    } else if (forcedWinnerId != null) {
                        M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(scorecsv).winnerId(forcedWinnerId).build());
                    } else {
                        M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(scorecsv).build());
                    }
                }
                T.ResyncChallonge();
                afterValidateTeam();
                Update();
            }
        }
    }
    protected abstract void afterValidateTeam();

    public void AddScore(int P1, int P2, Long winnerParticipantId) throws DataAccessException {
        P1Score = P1;
        P2Score = P2;
        if (P1 > P2) {
            M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(P1 + "-" + P2).winnerId(getParticipantID1()).build());
        } else if (P1 < P2) {
            M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(P1 + "-" + P2).winnerId(getParticipantID2()).build());
        } else if (winnerParticipantId != null) {
            M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(P1 + "-" + P2).winnerId(winnerParticipantId).build());
        } else {
            M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().scoresCsv(P1 + "-" + P2).build());
        }
        WinnerId = M.getWinnerId();
        LooserId = M.getLoserId();
    }
    public void reopenMatch(long P1ID, long P2ID) throws DataAccessException {
        if (T.getVSAmount() > 1) getSubMatch(P1ID, P2ID).reopenMatch();
        M = ChallongeAccount.reopenMatch(getMatch());
        MatchLogID = null;
        RefreshMatch(M);
        Update();
    }

    public abstract void NotifyStart();
    public abstract void NotifyNextOpponent();
    public void NotifyDeadline() {
        EmbedBuilder E = T.getTournamentEmbed();
        if (getPlayer1() != null && getPlayer2() != null) {
            if (T.getVSAmount() > 1) {
                for (SubMatch CSM : getSubMatches()) {
                    if (!CSM.isFinished()) {
                        EmbedBuilder E2 = new EmbedBuilder(E);
                        E2.setDescription(":alarm_clock: " + TL(Profile.get(CSM.getP1()), "challonge-manage-deadline-warning",
                                "`" + getRoundLong() + "`",
                                "<t:" + getDeadline().getEpochSecond() + ":R>", CSM.getP2().getAsMention() + " **(@" + CSM.getP2().getName() + ")**"));
                        sendPrivateMessage(CSM.getP1(), new MessageCreateBuilder().setContent(CSM.getP1().getAsMention()).setEmbeds(E2.build()));
                        E2 = new EmbedBuilder(E);
                        E2.setDescription(":alarm_clock: " + TL(Profile.get(CSM.getP2()), "challonge-manage-deadline-warning",
                                "`" + getRoundLong() + "`",
                                "<t:" + getDeadline().getEpochSecond() + ":R>", CSM.getP1().getAsMention() + " **(@" + CSM.getP1().getName() + ")**"));
                        sendPrivateMessage(CSM.getP2(), new MessageCreateBuilder().setContent(CSM.getP2().getAsMention()).setEmbeds(E2.build()));
                    }
                }
            } else if (T.getVSAmount() == 1 && !isCompleted()) {
                EmbedBuilder E2 = new EmbedBuilder(E);
                E2.setDescription(":alarm_clock: " + TL(getPlayer1().getLeaderPf(), "challonge-manage-deadline-warning",
                        "`" + getRoundLong() + "`",
                        "<t:" + getDeadline().getEpochSecond() + ":R>", getPlayer2().getLeader().getAsMention() + " **(@" + getPlayer2().getLeader().getName() + ")**"));
                sendPrivateMessage(getPlayer1().getLeader(), new MessageCreateBuilder().setContent(getPlayer1().getLeader().getAsMention()).setEmbeds(E2.build()));
                E2 = new EmbedBuilder(E);
                E2.setDescription(":alarm_clock: " + TL(getPlayer2().getLeaderPf(), "challonge-manage-deadline-warning",
                        "`" + getRoundLong() + "`",
                        "<t:" + getDeadline().getEpochSecond() + ":R>", getPlayer1().getLeader().getAsMention() + " **(@" + getPlayer1().getLeader().getName() + ")**"));
                sendPrivateMessage(getPlayer2().getLeader(), new MessageCreateBuilder().setContent(getPlayer2().getLeader().getAsMention()).setEmbeds(E2.build()));
            }
        }
    }

    public boolean isMatchUsedAsPrerequisite() {
        return T.getMatches(true).stream().anyMatch(CM -> CM.getPlayer1PrerequisiteMatchID() == getId() || CM.getPlayer2PrerequisiteMatchID() == getId());
    }
    public void GenerateSubMatchesIfNeeded(boolean force) {
        try {
            if (force || (getSubMatches().stream().anyMatch(CSM -> !getPlayer1().getAllTeammatesIDs().contains(CSM.getP1ID()) || !getPlayer2().getAllTeammatesIDs().contains(CSM.getP2ID()))) || (getSubMatches().size() != T.getVSAmount() && getPlayer1() != null && getPlayer2() != null && getPlayer1().isTeamFull() && getPlayer2().isTeamFull())) {
                for (SubMatch SM : getSubMatches()) SM.Delete();
                CMs = new ArrayList<>();
                List<Long> P1 = getPlayer1().getAllTeammatesIDs();
                List<Long> P2 = getPlayer2().getAllTeammatesIDs();
                Collections.shuffle(P1);
                Collections.shuffle(P2);
                for (Long L : P1) {
                    Long choice = P2.get(GenerateRandomNumber(0, P2.size() - 1));
                    CMs.add(new SubMatch(this, L, choice));
                    P2.remove(choice);
                }
            }
        } catch (Exception ignored) {}
    }

    public void RefreshMatch(at.stefangeyer.challonge.model.Match M) {
        this.ID = M.getId();
        this.TournamentID = M.getTournamentId() != null ? M.getTournamentId() : 0;
        this.Identifier = M.getIdentifier() != null ? M.getIdentifier() : "";
        this.Location = M.getLocation() != null ? M.getLocation() : "";
        if (T.getVSAmount() > 1 && isCompleted() && !M.getState().equals(MatchState.COMPLETE)) {
            for (SubMatch SM : getSubMatches()) SM.setState(M.getState());
        }
        this.State = M.getState().toString();
        this.Round = M.getRound() != null ? M.getRound() : 0;
        this.GroupID = M.getGroupId() != null ? M.getGroupId() : 0;
        this.isForfeited = M.getForfeited() != null ? M.getForfeited() : false;
        this.isOptional = M.getOptional() != null ? M.getOptional() : false;
        this.ParticipantID1 = M.getPlayer1Id() != null ? M.getPlayer1Id() : 0;
        this.ParticipantID2 = M.getPlayer2Id() != null ? M.getPlayer2Id() : 0;
        this.Player1PrerequisiteMatchID = M.getPlayer1PrerequisiteMatchId() != null ? M.getPlayer1PrerequisiteMatchId() : 0;
        this.Player2PrerequisiteMatchID = M.getPlayer2PrerequisiteMatchId() != null ? M.getPlayer2PrerequisiteMatchId() : 0;
        this.isP1PrerequisiteLoser = M.getPlayer1IsPrerequisiteMatchLoser() != null ? M.getPlayer1IsPrerequisiteMatchLoser() : false;
        this.isP2PrerequisiteLoser = M.getPlayer2IsPrerequisiteMatchLoser() != null ? M.getPlayer2IsPrerequisiteMatchLoser() : false;
        this.P1Votes = M.getPlayer1Votes() != null ? M.getPlayer1Votes() : 0;
        this.P2Votes = M.getPlayer2Votes() != null ? M.getPlayer2Votes() : 0;
        this.CreatedAtTimeEpochSecond = M.getCreatedAt() != null ? M.getCreatedAt().toEpochSecond() : 0;
        this.StartedAtTimeEpochSecond = M.getStartedAt() != null ? M.getStartedAt().toEpochSecond() : 0;
        this.UnderwayAtTimeEpochSecond = M.getUnderwayAt() != null ? M.getUnderwayAt().toEpochSecond() : 0;
        this.ScheduledTimeEpochSecond = M.getScheduledTime() != null ? M.getScheduledTime().toEpochSecond() : 0;
        this.UpdatedAtTimeEpochSecond = M.getUpdatedAt() != null ? M.getUpdatedAt().toEpochSecond() : 0;
        this.CompletedAtTimeEpochSecond = M.getCompletedAt() != null ? M.getCompletedAt().toEpochSecond() : 0;
        this.WinnerId = M.getWinnerId();
        this.LooserId = M.getLoserId();
        this.P1Score = 0;
        this.P2Score = 0;
        String score = M.getScoresCsv();
        if (T.getVSAmount() > 1) {
            GenerateSubMatchesIfNeeded(false);
            if (!score.isEmpty()) {
                int P1Score = 0, P2Score = 0, i = 0;
                for (String s : score.split(",")) {
                    s = formatScore(s);
                    if (i < getSubMatches().size()) getSubMatches().get(i).AddScore(Integer.parseInt(s.split(" - ")[0]), Integer.parseInt(s.split(" - ")[1]));
                    i++;
                    if (Integer.parseInt(s.split(" - ")[0]) > Integer.parseInt(s.split(" - ")[1])) {
                        P1Score++;
                    } else if (Integer.parseInt(s.split(" - ")[1]) > Integer.parseInt(s.split(" - ")[0])) {
                        P2Score++;
                    }
                }
                this.P1Score = P1Score;
                this.P2Score = P2Score;
                for (SubMatch SM : getSubMatches()) if (!SM.isFinished()) SM.AddScore(0, 0); // clean up if someone put a single score manually
            }
        } else {
            if (!score.isEmpty()) {
                score = formatScore(score);
                this.P1Score = Integer.parseInt(score.split(" - ")[0]);
                this.P2Score = Integer.parseInt(score.split(" - ")[1]);
            }
        }
    }

    public at.stefangeyer.challonge.model.Match getMatch() throws DataAccessException {
        return M == null ? M = ChallongeAccount.getMatch(T.getTournament(), getId()) : M;
    }
    public abstract T getTournament();
}
