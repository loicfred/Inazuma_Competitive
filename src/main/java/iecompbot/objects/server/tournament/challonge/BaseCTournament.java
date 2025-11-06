package iecompbot.objects.server.tournament.challonge;

import at.stefangeyer.challonge.exception.DataAccessException;
import at.stefangeyer.challonge.model.Match;
import at.stefangeyer.challonge.model.Participant;
import at.stefangeyer.challonge.model.Tournament;
import at.stefangeyer.challonge.model.enumeration.RankedBy;
import at.stefangeyer.challonge.model.enumeration.TournamentState;
import at.stefangeyer.challonge.model.enumeration.TournamentType;
import at.stefangeyer.challonge.model.query.ParticipantQuery;
import at.stefangeyer.challonge.model.query.TournamentQuery;
import com.opencsv.CSVWriter;
import iecompbot.interaction.Automation;
import iecompbot.interaction.cmdbreakdown.server.ChallongeCommand;
import iecompbot.objects.match.Game;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.BaseTournament;
import iecompbot.objects.server.tournament.Challonge_WaitingList;
import iecompbot.objects.server.tournament.SubMatch;
import iecompbot.objects.server.tournament.challonge.global.GChallonge_Tournament;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.Main.ChallongeAccount;
import static iecompbot.Main.TempDirectory;
import static iecompbot.interaction.Automation.handleException;
import static iecompbot.interaction.Automation.replyException;
import static iecompbot.interaction.GuildReady.RefreshAllPrivateCMDs;
import static iecompbot.objects.BotManagers.isTournamentManager;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static my.utilities.json.JSONItem.GSON;
import static my.utilities.util.Utilities.CutString;
import static my.utilities.util.Utilities.StopString;
import static my.utilities.var.Constants.ProgramZoneId;

@DatabaseObject.TableName(value = "challonge_tournament")
public abstract class BaseCTournament
               <T extends BaseCTournament<T, M, P>,
                M extends BaseCMatch<T, M, P>,
                P extends BaseCParticipant<T, M, P>>
        extends BaseTournament<T> {
    protected transient List<Challonge_WaitingList> WaitingPeople = null;
    protected transient List<P> Participants;
    protected transient List<M> Matches;
    protected transient Tournament T;

    public long EventID;
    public String State;

    public String URL;
    public String FullChallongeURL;
    public String Subdomain;
    public String RankedBy;
    public boolean hasThirdPlace;
    public boolean isGroupStageEnabled;
    public boolean hasGroupStageStarted;
    public int ProgressMeter;
    public String LiveImageURL;

    public Float pointsForMatchWin;
    public Float pointsForMatchTie;
    public Float pointsForGameWin;
    public Float pointsForGameTie;
    public Float pointsForBye;
    public int swissRounds;
    public int roundRobinIterations;
    public Float roundRobinPointsForGameWin;
    public Float roundRobinPointsForGameTie;
    public Float roundRobinPointsForMatchWin;
    public Float roundRobinPointsForMatchTie;


    public Long getEventID() {
        return EventID;
    }
    public TournamentState getState() {
        return TournamentState.valueOf(State.toUpperCase().replaceAll(" ","_"));
    }
    public TournamentType getType() {
        return TournamentType.valueOf(Type.toUpperCase().toUpperCase().replaceAll(" ","_"));
    }
    public String getURL() {
        return URL;
    }
    public String getFullChallongeURL() {
        return FullChallongeURL;
    }
    public String getSubdomain() {
        return Subdomain;
    }
    public RankedBy getRankedBy() {
        return at.stefangeyer.challonge.model.enumeration.RankedBy.valueOf(RankedBy);
    }
    public boolean hasThirdPlace() {
        return hasThirdPlace;
    }
    public boolean isGroupStageEnabled() {return isGroupStageEnabled;}
    public boolean isHasGroupStageStarted() {return hasGroupStageStarted;}
    public int getProgressMeter() {
        return ProgressMeter;
    }
    public String getLiveImageURL() {
        return LiveImageURL;
    }
    public Float getPointsForMatchWin() {
        return pointsForMatchWin;
    }
    public Float getPointsForMatchTie() {
        return pointsForMatchTie;
    }
    public Float getPointsForGameWin() {
        return pointsForGameWin;
    }
    public Float getPointsForGameTie() {
        return pointsForGameTie;
    }
    public Float getPointsForBye() {
        return pointsForBye;
    }
    public Integer getSwissRounds() {
        return swissRounds;
    }
    public Integer getRoundRobinIterations() {
        return roundRobinIterations;
    }
    public Float getRoundRobinPointsForGameWin() {
        return roundRobinPointsForGameWin;
    }
    public Float getRoundRobinPointsForGameTie() {
        return roundRobinPointsForGameTie;
    }
    public Float getRoundRobinPointsForMatchWin() {
        return roundRobinPointsForMatchWin;
    }
    public Float getRoundRobinPointsForMatchTie() {
        return roundRobinPointsForMatchTie;
    }

    public Tournament getTournament() throws DataAccessException {
        if (T == null) {
            if (getId() > 100000) {
                T = ChallongeAccount.getTournament(String.valueOf(getId()), true, true);
            }
            if (T == null) {
                T = ChallongeAccount.getTournament(URL, true, true);
            }
        }
        Update();
        return T;
    }


    public boolean isPending() {
        return TournamentState.valueOf(State.toUpperCase().replaceAll(" ","_")).equals(TournamentState.PENDING);
    }
    public boolean isComplete() {
        return TournamentState.valueOf(State.toUpperCase().replaceAll(" ","_")).equals(TournamentState.COMPLETE);
    }
    public boolean isUnderway() {
        return TournamentState.valueOf(State.toUpperCase().replaceAll(" ","_")).equals(TournamentState.UNDERWAY);
    }
    public boolean isAwaitingReview() {
        return TournamentState.valueOf(State.toUpperCase().replaceAll(" ","_")).equals(TournamentState.AWAITING_REVIEW);
    }
    public boolean isGroupStageUnderway() {
        return TournamentState.valueOf(State.toUpperCase().replaceAll(" ","_")).equals(TournamentState.GROUP_STAGES_UNDERWAY);
    }
    public boolean isGroupStageFinalized() {
        return TournamentState.valueOf(State.toUpperCase().replaceAll(" ","_")).equals(TournamentState.GROUP_STAGES_FINALIZED);
    }
    public boolean isSingleElimination() {
        return TournamentType.valueOf(Type.toUpperCase().toUpperCase().replaceAll(" ","_")).equals(TournamentType.SINGLE_ELIMINATION);
    }
    public boolean isDoubleElimination() {
        return TournamentType.valueOf(Type.toUpperCase().toUpperCase().replaceAll(" ","_")).equals(TournamentType.DOUBLE_ELIMINATION);
    }
    public boolean isRoundRobin() {
        return TournamentType.valueOf(Type.toUpperCase().toUpperCase().replaceAll(" ","_")).equals(TournamentType.ROUND_ROBIN);
    }
    public boolean isSwiss() {
        return TournamentType.valueOf(Type.toUpperCase().toUpperCase().replaceAll(" ","_")).equals(TournamentType.SWISS);
    }
    public boolean isFreeForAll() {
        return TournamentType.valueOf(Type.toUpperCase().toUpperCase().replaceAll(" ","_")).equals(TournamentType.FREE_FOR_ALL);
    }

    public boolean isRegistrationOpen() {
        return (Instant.now().isBefore(getStartAtTime()) || StartAtTimeEpochSecond == 0) && isPending();
    }
    public boolean isAccountOldEnough(User U) {
        return Instant.now().isAfter(U.getTimeCreated().toInstant().plus(7, ChronoUnit.DAYS));
    }

    public boolean isScoreValid(int s1, int s2) {
        return !isSingleElimination() && !isDoubleElimination() || s1 != s2 || getVSAmount() > 1;
    }

    public List<Challonge_WaitingList> getWaitingList() {
        return WaitingPeople == null ? WaitingPeople = Challonge_WaitingList.get(getId()) : WaitingPeople;
    }
    public Challonge_WaitingList AddToWaitingList(long userId) {
        Challonge_WaitingList nw = new Challonge_WaitingList(getId(), userId);
        getWaitingList().add(nw);
        return nw;
    }
    public Challonge_WaitingList RemoveFromWaitingList() {
        Challonge_WaitingList nw = Challonge_WaitingList.getNext(getId());
        nw.Delete();
        return nw;
    }
    public boolean RemoveFromWaitingList(long userId) {
        try {
            return Challonge_WaitingList.get(getId(), userId).Delete() > 0;
        } catch (NullPointerException ignored) {
            return false;
        }
    }

    public File getJSON() {
        File f = new File(TempDirectory + " /file.json");
        try (PrintWriter PW = new PrintWriter(f, StandardCharsets.UTF_8)) {
            PW.println(toJSON());
        } catch (Exception ignored) {}
        return f;
    }
    public File getCSV() {
        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        List<String> csvData = new ArrayList<>();
        try {
            for (Field field : fields) {
                if (!Modifier.isTransient(field.getModifiers())) {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value instanceof List<?> listValue) {
                        for (Object listItem : listValue) {
                            if (listItem != null) {
                                Class<?> listItemClass = listItem.getClass();
                                Field[] listItemFields = listItemClass.getDeclaredFields();
                                for (Field listItemField : listItemFields) {
                                    if (!Modifier.isTransient(listItemField.getModifiers())) {
                                        listItemField.setAccessible(true);
                                        Object fieldValue = listItemField.get(listItem);
                                        csvData.add(fieldValue != null ? fieldValue.toString() : "");
                                    }
                                }
                            }
                        }
                    } else {
                        if (value != null) {
                            csvData.add(value.toString());
                        } else {
                            csvData.add("");
                        }
                    }
                }
            }
            new File(TempDirectory + "/" + getId() + ".csv").delete();
            try (CSVWriter writer = new CSVWriter(new FileWriter(TempDirectory + "/" + getId() + ".csv"))) {
                writer.writeNext(csvData.toArray(new String[0]));
            }
        } catch (IOException | IllegalAccessException e) {
            Automation.handleException(e);
        }
        return new File(TempDirectory + "/" + getId() + ".csv");
    }



    public void setType(TournamentType type) throws DataAccessException {
        ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().tournamentType(type).build());
        this.Type = type.toString();
    }
    public void setURL(String url) throws DataAccessException {
        ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().url(url).build());
        this.URL = url;
    }
    public void setSubdomain(String subdomain) throws DataAccessException {
        ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().subdomain(subdomain).build());
        this.Subdomain = subdomain;
    }
    public void setRankedBy(RankedBy rankedBy) throws DataAccessException {
        ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().rankedBy(rankedBy).build());
        this.RankedBy = rankedBy.toString();
    }
    public void setThirdPlace(boolean hold) throws DataAccessException {
        ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().holdThirdPlaceMatch(hold).build());
        this.hasThirdPlace = hold;
    }
    public void setName(String name) throws DataAccessException {
        ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().name(name).build());
        this.Name = name;
    }
    public void setDescription(String description) throws DataAccessException {
        ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().description(description).build());
        Description = CutString(description, 512);
    }
    public void setSignupCap(int participantCap) throws DataAccessException {
        if (participantCap > 3 && isPending()) {
            ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().signupCap(participantCap).build());
            this.SignupCap = participantCap;
        }
    }
    public void setStartAtTime(Instant startTime) throws DataAccessException {
        ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().startAt(OffsetDateTime.ofInstant(startTime, ProgramZoneId.getRules().getOffset(startTime))).build());
        this.StartAtTimeEpochSecond = startTime.getEpochSecond();
    }
    public void setGameName(Game game) {
        this.GameCode = game.getCode();
        try {
            ChallongeAccount.updateTournament(getTournament(), TournamentQuery.builder().gameName(game.getFullName()).name(getTournament().getName()).build());
        } catch (DataAccessException ignored) {}
    }

    public void setSignupImageURL(String imageurl) {
        if (imageurl != null) this.SignupImageURL = imageurl;
    }
    public void setRegistrationStartTime(Instant startTime) {
        this.RegistrationStartEpochSecond = startTime.getEpochSecond();
    }
    public void setInscriptionByBot(boolean isInscriptionByBot) {
        this.isInscriptionByBot = isInscriptionByBot;
    }
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    public void setVSAmount(int amount) {
        this.VSAmount = amount;
    }



    public boolean areAllParticipantsLinked() {
        return getParticipants().stream().allMatch(P -> P.getLeaderID() != null);
    }
    public boolean areThereMatchesLeft(int inRound, long GroupID) {
        return getMatches().stream().anyMatch(M -> !M.isCompleted() && M.getRound() == inRound && M.getGroupID() == GroupID);
    }
    public int getPosition(String userid) {
        return doQueryValue(Integer.class, "SELECT Position FROM challonge_participant WHERE TournamentID = ? AND DiscordID = ?", getId(), userid).orElse(0);
    }



    public abstract List<M> getMatches();
    public List<M> getUnplayedMatches() {
        return getMatches().stream().filter(M -> !M.isCompleted()).collect(Collectors.toList());
    }
    public List<M> getMatches(int ofRound, long groupId) {
        return getMatches().stream().filter(M -> M.getRound() == ofRound && M.getGroupID() == groupId).collect(Collectors.toList());
    }
    public List<M> getMatchesOfGroup(long groupId) {
        return getMatches().stream().filter(M -> M.getGroupID() == groupId).collect(Collectors.toList());
    }
    public List<M> getMatches(boolean completedOnly) {
        return getMatches().stream().filter(M -> M.isCompleted() || !completedOnly).collect(Collectors.toList());
    }
    public List<M> getOpenMatches() {
        return getMatches().stream().filter(M::isOpen).collect(Collectors.toList());
    }
    public List<M> getMatches(int ofRound, long groupId, boolean completedOnly) {
        return getMatches().stream().filter(M -> M.getRound() == ofRound && M.getGroupID() == groupId && (M.isCompleted() || !completedOnly)).collect(Collectors.toList());
    }
    public M getMatchById(long id) {
        return getMatches().stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }
    public M getMatchByLog(long matchlogId) {
        return getMatches().stream().filter(m -> (m.isCompleted() && m.getId() == matchlogId) || m.getSubMatches().stream().anyMatch(CSM -> CSM.isFinished() && CSM.MatchLogID == matchlogId)).findFirst().orElse(null);
    }


    public abstract List<P> getParticipants();
    public P getParticipantById(String id) {
        return getParticipantById(Long.parseLong(id));
    }
    public P getParticipantById(long id) {
        return getParticipants().stream().filter(p -> (p.getLeaderID() != null && p.getLeaderID() == id) || p.getId() == id).findFirst().orElse(null);
    }
    public P getTeamByMyId(String id) {
        return getTeamByMyId(Long.parseLong(id));
    }
    public P getTeamByMyId(long id) {
        return getParticipants().stream().filter(p -> (p.getAllTeammatesIDs().contains(id)) || p.getId() == id).findFirst().orElse(null);
    }
    public List<P> getParticipantByRank(int position) {
        return getParticipants().stream().filter(p -> p.getPosition() == position).collect(Collectors.toList());
    }

    public void SeedByPower() {
        getParticipants().sort(Comparator.comparingDouble((P P) -> {
            try {
                return P.getAllTeammatesIDs().stream().mapToDouble(ID -> {
                    try {
                        return Profile.get(ID).getPG(getGame()).getPower();
                    } catch (Exception e) {
                        return 0;
                    }
                }).sum();
            } catch (Exception e) {
                return 0;
            }
        }).reversed());
        int i = 0;
        for (P CP : getParticipants()) CP.setSeed(++i);
    }
    public void SeedByActivity() {
        getParticipants().sort(Comparator.comparingDouble((P P) -> {
            try {
                return P.getAllTeammatesIDs().stream().mapToDouble(ID -> {
                    try {
                        DatabaseObject.Row TR = Profile.get(ID).getActivity(null, getGame().toString());
                        return TR.getAsDouble("Average Match Activity") + TR.getAsDouble("Average Tournament Activity");
                    } catch (Exception e) {
                        return 0;
                    }
                }).sum();
            } catch (Exception e) {
                return 0;
            }
        }).reversed());
        int i = 0;
        for (P CP : getParticipants()) CP.setSeed(++i);
    }

    public List<Long> getGroupIDs() {
        List<Long> L = new ArrayList<>();
        if (isGroupStageUnderway() || isGroupStageFinalized()) {
            for (M M : getMatches()) {
                if (M.getParticipantID1() != 0 && M.getParticipantID2() != 0) {
                    if (!L.contains(M.getGroupID())) {
                        L.add(M.getGroupID());
                    }
                }
            }
        } else {
            L.add(0L);
        }
        return L;
    }
    public String getGroupName(long groupid) {
        try {
            if (groupid == getGroupIDs().getFirst()) {
                return "Group A";
            } else if (groupid == getGroupIDs().get(1)) {
                return "Group B";
            } else if (groupid == getGroupIDs().get(2)) {
                return "Group C";
            } else if (groupid == getGroupIDs().get(3)) {
                return "Group D";
            } else if (groupid == getGroupIDs().get(4)) {
                return "Group E";
            } else if (groupid == getGroupIDs().get(5)) {
                return "Group F";
            } else if (groupid == getGroupIDs().get(6)) {
                return "Group G";
            } else if (groupid == getGroupIDs().get(7)) {
                return "Group H";
            } else if (groupid == getGroupIDs().get(8)) {
                return "Group I";
            }
            return "";
        } catch (NullPointerException ignored) {return "";}
    }
    public String getGroupProgress(long GroupID) {
        int i = 0;
        for (M M : getMatchesOfGroup(GroupID)) {
            if (M.isCompleted() && !M.isOptional()) {
                i++;
            }
        }
        return i + "/" + getMatchesOfGroup(GroupID).size();
    }

    public List<Integer> getRounds() {
        List<Integer> L = new ArrayList<>();
        for (M M : getMatches()) {
            if (!L.contains(M.getRound()) && !M.isOptional()) {
                L.add(M.getRound());
            }
        }
        return L;
    }
    public List<Integer> getRounds(long GroupID) {
        List<Integer> L = new ArrayList<>();
        for (M M : getMatches()) {
            if (!L.contains(M.getRound()) && M.getGroupID() == GroupID && !M.isOptional()) {
                L.add(M.getRound());
            }
        }
        return L;
    }
    public String getRoundLong(int Round, long GroupID) {
        String R = "";
        if (Round > 0) {
            R = "Round " + Round;
            if (!doesRoundExist(Round + 1, GroupID)) {
                R = "Finals";
            } else if (!doesRoundExist(Round + 2, GroupID)) {
                R = "Semi-finals";
            } else if (!doesRoundExist(Round + 3, GroupID)) {
                R = "Quarter-finals";
            }
        } else if (Round < 0) {
            R = ("Losers Round " + Round).replaceAll("-", "");
            if (!doesRoundExist(Round - 1, GroupID)) {
                R = "Losers Finale";
            } else if (!doesRoundExist(Round - 2, GroupID)) {
                R = "Losers Semi-finals";
            } else if (!doesRoundExist(Round - 3, GroupID)) {
                R = "Losers Quarter-finals";
            }
        }
        if (Round == 0) {
            R = "Third Place";
        }
        return R;
    }
    public String getRoundShort(int Round, long GroupID) {
        String R = "";
        if (Round > 0) {
            R = "R" + Round;
            if (!doesRoundExist(Round + 1, GroupID)) {
                R = "F";
            } else if (!doesRoundExist(Round + 2, GroupID)) {
                R = "SF";
            } else if (!doesRoundExist(Round + 3, GroupID)) {
                R = "QF";
            }
        } else if (Round < 0) {
            R = ("LR" + Round).replaceAll("-", "");
            if (!doesRoundExist(Round - 1, GroupID)) {
                R = "LF";
            } else if (!doesRoundExist(Round - 2, GroupID)) {
                R = "LSF";
            } else if (!doesRoundExist(Round - 3, GroupID)) {
                R = "LQF";
            }
        }
        if (Round == 0) {
            R = "TP";
        }
        return R;
    }
    private boolean doesRoundExist(int Round, long GroupID) {
        for (M M : getMatchesOfGroup(GroupID)) {
            if (M.getRound() == Round && !M.isOptional()) {
                return true;
            }
        }
        return false;
    }
    public String getRoundProgress(int Round, long GroupID) {
        int i = 0;
        for (M M : getMatches(Round, GroupID)) {
            if (M.getRound() == Round && M.isCompleted() && !M.isOptional()) {
                i++;
            }
        }
        return i + "/" + getMatches(Round, GroupID).size();
    }
    public Instant getDeadline(int Round, long GroupID) {
        if (!getMatches(Round, GroupID).isEmpty()) {
            return getMatches(Round, GroupID).getFirst().getDeadline();
        }
        return null;
    }

    public void ResyncChallonge() throws Exception {
        T = null;
        this.ID = getTournament().getId();
        this.EventID = T.getEventId() != null ? T.getEventId() : 0;
        this.State = T.getState().toString();
        this.Type = T.getTournamentType().toString();
        this.Name = T.getName();
        this.GameCode = Game.get(T.getGameName()).getCode();
        this.Description = StopString(T.getDescription(), 1000);

        this.URL = T.getUrl();
        this.FullChallongeURL = T.getFullChallongeUrl();
        this.Subdomain = T.getSubdomain();
        this.SignupCap = T.getSignupCap() != null ? T.getSignupCap() : 0;
        this.CreatedAtTimeEpochSecond = T.getCreatedAt() != null ? T.getCreatedAt().toEpochSecond() : 0;
        this.StartedAtTimeEpochSecond = T.getStartedAt() != null ? T.getStartedAt().toEpochSecond() : 0;
        this.StartAtTimeEpochSecond = T.getStartAt() != null ? T.getStartAt().toEpochSecond() : 0;
        this.UpdatedAtTimeEpochSecond = T.getUpdatedAt() != null ? T.getUpdatedAt().toEpochSecond() : 0;
        this.CompletedAtTimeEpochSecond = T.getCompletedAt() != null ? T.getCompletedAt().toEpochSecond() : 0;
        this.RankedBy = T.getRankedBy().toString();
        this.ParticipantCount = T.getParticipantsCount() != null ? T.getParticipantsCount() : 0;
        this.hasThirdPlace = T.getHoldThirdPlaceMatch();
        this.ProgressMeter = T.getProgressMeter() != null ? T.getProgressMeter() : 0;
        this.LiveImageURL = T.getLiveImageUrl();
        this.isGroupStageEnabled = T.getGroupStagesEnabled();
        this.hasGroupStageStarted = T.getGroupStagesWereStarted();

        this.pointsForMatchWin = T.getPointsForMatchWin() != null ? T.getPointsForMatchWin() : 0;
        this.pointsForMatchTie = T.getPointsForMatchTie() != null ? T.getPointsForMatchTie() : 0;
        this.pointsForGameWin = T.getPointsForGameWin() != null ? T.getPointsForGameWin() : 0;
        this.pointsForGameTie = T.getPointsForGameTie() != null ? T.getPointsForGameTie() : 0;
        this.pointsForBye = T.getPointsForBye() != null ? T.getPointsForBye() : 0;
        this.swissRounds = T.getSwissRounds() != null ? T.getSwissRounds() : 0;
        this.roundRobinIterations = T.getRoundRobinIterations() != null ? T.getRoundRobinIterations() : 0;
        this.roundRobinPointsForGameWin = T.getRoundRobinPointsForGameWin() != null ? T.getRoundRobinPointsForGameWin() : 0;
        this.roundRobinPointsForGameTie = T.getRoundRobinPointsForGameTie() != null ? T.getRoundRobinPointsForGameTie() : 0;
        this.roundRobinPointsForMatchWin = T.getRoundRobinPointsForMatchWin() != null ? T.getRoundRobinPointsForMatchWin() : 0;
        this.roundRobinPointsForMatchTie = T.getRoundRobinPointsForMatchTie() != null ? T.getRoundRobinPointsForMatchTie() : 0;
        Matches = null;
        Participants = null;
        if (!isPending()) setPublic(getParticipantCount() * getVSAmount() >= 8);
        if (!isComplete()) {
            for (M M : new ArrayList<>(getMatches())) {
                if (T.getMatches().stream().noneMatch(MM -> MM.getId() == M.getId())) {
                    getMatches().remove(M);
                    M.Delete();
                }
            }
            for (P P : new ArrayList<>(getParticipants())) {
                if (T.getParticipants().stream().noneMatch(PP -> PP.getId() == P.getId())) {
                    getParticipants().remove(P);
                    P.Delete();
                }
            }
            getWaitingList().removeIf(W -> getParticipants().stream().anyMatch(PP -> PP.getAllTeammatesIDs().contains(W.getUserId())));
        }

        for (Match M : T.getMatches()) {
            try {
                M MM = getMatchById(M.getId());
                if (MM != null) {
                    MM.RefreshMatch(M);
                } else {
                    getMatches().add(createMatch(M));
                }
            } catch (Exception e) {handleException(e);}
        }
        for (Participant P : T.getParticipants()) {
            try {
                P PP = getParticipantById(P.getId());
                if (PP != null) {
                    PP.RefreshParticipant(P);
                } else {
                    getParticipants().add(createParticipant(P, null));
                }
            } catch (Exception e) {handleException(e);}
        }
        doUpdate("CALL UpsertChallongeTournament(?,?,?)", GSON.toJson(this), GSON.toJson(getParticipants()), GSON.toJson(getMatches()));
        Matches = null;
        Participants = null;
    }

    public void validateAllTeamsScores() throws Exception {
        if (getVSAmount() > 1) for (M CM : getMatches()) {
            CM.validateTeamScores(null);
        }
    }

    public void Start(InteractionHook M) {
        try {
            if (isPending()) {
                if (this instanceof SChallonge_Tournament CT) {
                    if (CT.getInscriptionChannel().getMessage() != null && !CT.getInscriptionChannel().getMessage().getReactions().isEmpty()) {
                        CT.getInscriptionChannel().getMessage().getReactions().getFirst().retrieveUsers().takeWhileAsync(ignored -> true).whenComplete((users, throwable) -> {
                            for (User u : users) {
                                AddParticipantFromInscription(null, u, false);
                            }
                        });
                        CT.PanelMessageID = null;
                        CT.UpdateOnly("PanelMessageID");
                        CT.getPanelChannel().DeleteMessage1();
                    }
                }

                try {
                    T = ChallongeAccount.startTournament(getTournament(), true, true);
                } catch (DataAccessException ignored) {}
                ResyncChallonge();

                Update();
                EmbedBuilder E = getTournamentEmbed();
                E.setTitle(getName());
                E.setColor(Color.red);
                E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-start-description"));
                M.editOriginalEmbeds(E.build()).queue();

                for (M CM : getUnplayedMatches()) {
                    P P1 = CM.getPlayer1();
                    P P2 = CM.getPlayer2();
                    if (P1 != null && P2 != null && !CM.isCompleted()) {
                        if (P1.isTeamFull() && P2.isTeamFull()) {
                            CM.NotifyStart();
                        }
                    }
                }
                if (this instanceof SChallonge_Tournament CT) {
                    CT.RefreshPanelMessage();
                    CT.RefreshInscriptionMessage();
                    RefreshAllPrivateCMDs();
                }
            }
        } catch (Exception e) {
            net.dv8tion.jda.api.components.buttons.Button START = net.dv8tion.jda.api.components.buttons.Button.danger("challonge-panel-confirm-start/" + getId(), TL(M,"Start"));
            M.editOriginalComponents(ActionRow.of(START)).queue();
        }
    }
    public void Complete(InteractionHook M) {
        try {
            if (!isComplete()) {
                try {
                    T = ChallongeAccount.finalizeTournament(getTournament(), true, true);
                } catch (DataAccessException ignored) {}
                ResyncChallonge();
                LogTournamentResult(false);

                EmbedBuilder E = getTournamentEmbed();
                E.setDescription(TL(M,"challonge-manage-complete-description"));
                M.editOriginalEmbeds(E.build()).queue();

                for (BaseCParticipant<?,?,?> P : getParticipantByRank(1)) {
                    for (User U : P.getTeammates()) {
                        if (U != null) {
                            Profile PP = Profile.get(U);
                            PP.resetClanCards();
                            sendPrivateMessage(U, TL(PP, "congrat-for-winning", ":first_place:", "1", "**" + T.getName() + "**"));
                        }
                    }
                }
                for (BaseCParticipant<?,?,?> P : getParticipantByRank(2)) {
                    for (User U : P.getTeammates()) {
                        if (U != null) {
                            Profile PP = Profile.get(U);
                            PP.resetClanCards();
                            sendPrivateMessage(U, TL(PP, "congrat-for-winning-2", ":second_place:", "2", "**" + T.getName() + "**"));
                        }
                    }
                }
                for (BaseCParticipant<?,?,?> P : getParticipantByRank(3)) {
                    for (User U : P.getTeammates()) {
                        if (U != null) {
                            Profile PP = Profile.get(U);
                            PP.resetClanCards();
                            sendPrivateMessage(U, TL(PP, "congrat-for-winning-3", ":third_place:", "3", "**" + T.getName() + "**"));
                        }
                    }
                }
                if (this instanceof SChallonge_Tournament CT) {
                    CT.getPanelChannel().DeleteMessage1();
                    CT.RefreshPanelMessage();
                    CT.I.RefreshGuildInformation();
                    CT.I.ReductionBlacklist(getGame());
                    CT.RemoveRoleFromEveryone();
                }
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }

    public abstract EmbedBuilder getTournamentEmbed();

    protected void ClearDuplicateParticipants(P P) {
        if (isPending() && P.getLeaderID() != null) {
            for (P P2 : new ArrayList<>(getParticipants())) {
                if (P2.getLeaderID() != null && P.getLeaderID().equals(P2.getLeaderID()) && P.getId() != P2.getId()) {
                    try {
                        DeleteParticipant(P.getId());
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    public P AddParticipant(User member) throws Exception {
        if (member != null) {
           if (getParticipants().stream().noneMatch(P -> P.getAllTeammatesIDs().contains(member.getIdLong()))) {
                Participant P = ChallongeAccount.addParticipant(getTournament(),
                        ParticipantQuery.builder()
                                .name(hasModifierAnonymous() ? String.valueOf(member.getEffectiveName().hashCode()) :  member.getEffectiveName())
                                .misc(member.getId())
                                .build());
                P NewParticipant = createParticipant(P, member.getIdLong());
                ParticipantCount++;
                getParticipants().add(NewParticipant);
                Update();
                return NewParticipant;
            }
        }
        return null;
    }
    public P DeleteParticipant(long participantId) throws DataAccessException {
        P P = getParticipantById(participantId);
        if (P != null) {
            ChallongeAccount.deleteParticipant(P.getParticipant());
            P.Delete();
            getParticipants().remove(P);
            ParticipantCount--;
            Update();
        }
        return P;
    }

    public abstract void AddParticipantFromInscription(InteractionHook M, User u, boolean force);
    public abstract void DeleteParticipantFromInscription(InteractionHook M, User u);
    public abstract void AddTeamFromInscription(InteractionHook M, User u);
    public abstract void SendTeamRequests(InteractionHook M, List<User> teammates);

    public abstract void CleanTournament() throws Exception;
    public abstract void LogTournamentResult(boolean force);

    public void TournamentManageUI(InteractionHook M, ChallongeCommand CMD) {
        if (isOrganiser(M.getInteraction().getUser())) {
            Interaction event = M.getInteraction();
            List<ActionRow> rows = new ArrayList<>();
            List<SelectOption> options = new ArrayList<>();
            if (isUnderway() || isTournamentManager(event.getUser())) {
                options.add(SelectOption.of(TL(M,"edit-add-score"), "add-match").withEmoji(Emoji.fromUnicode("U+2705")).withDescription(TL(M,"challonge-add-match-desc")));
                options.add(SelectOption.of(TL(M,"edit-add-prediction"), "add-prediction").withEmoji(Emoji.fromUnicode("U+1f4e5")).withDescription(TL(M,"challonge-add-predi-desc")));
                options.add(SelectOption.of(TL(M,"edit-edit-match"), "edit-match").withEmoji(Emoji.fromUnicode("U+1F4DD")).withDescription(TL(M,"challonge-edit-match-desc")));
                if (getVSAmount() > 1) options.add(SelectOption.of(TL(M,"edit-random-match"), "random-match").withEmoji(Emoji.fromUnicode("U+2754")).withDescription(TL(M,"tournament-random-match-desc")));
            }
            if (isPending() || isTournamentManager(event.getUser())) {
                options.add(SelectOption.of(TL(M,"edit-seed", TL(M,"Random")), "seed-random").withEmoji(Emoji.fromUnicode("U+2754")).withDescription(TL(M,"challonge-edit-seed-desc")));
                options.add(SelectOption.of(TL(M,"edit-seed", TL(M,"Power")), "seed-power").withEmoji(Emoji.fromUnicode("U+1f4c8")).withDescription(TL(M,"challonge-edit-seed-desc")));
                options.add(SelectOption.of(TL(M,"edit-seed", TL(M,"Activity")),"seed-activity").withEmoji(Emoji.fromUnicode("U+1f4ca")).withDescription(TL(M,"challonge-edit-seed-desc")));
                options.add(SelectOption.of(TL(M,"edit-third-place"), "edit-third-place").withEmoji(Emoji.fromUnicode("U+1f949")).withDescription(TL(M,"challonge-edit-third-place-desc")));
            }
            if (!isComplete() || isTournamentManager(event.getUser())) {
                options.add(SelectOption.of(TL(M,"edit-participants"), "edit-participants").withEmoji(Emoji.fromUnicode("U+1F3C3")).withDescription(TL(M,"challonge-edit-participants-desc")));
            }
            options.add(SelectOption.of(TL(M,"Name"), "edit-name").withEmoji(Emoji.fromUnicode("U+1F4DD")).withDescription(TL(M,"challonge-edit-name-desc")));
            options.add(SelectOption.of(TL(M,"Description"), "edit-desc").withEmoji(Emoji.fromUnicode("U+1F4DD")).withDescription(TL(M,"challonge-edit-desc-desc")));
            options.add(SelectOption.of(TL(M,"Game"), "edit-game").withEmoji(Emoji.fromUnicode("U+1F3AE")).withDescription(TL(M,"challonge-edit-game-desc")));
            options.add(SelectOption.of(TL(M,"invite_link"), "edit-invite").withEmoji(Emoji.fromUnicode("U+2709")).withDescription(TL(M,"challonge-edit-invite-desc")));
            options.add(SelectOption.of(TL(M,"start_time"), "edit-start_time").withEmoji(Emoji.fromUnicode("U+23F0")).withDescription(TL(M,"challonge-edit-start_time-desc")));
            options.add(SelectOption.of(TL(M,"signup_cap"), "edit-signup_cap").withEmoji(Emoji.fromUnicode("U+1F465")).withDescription(TL(M,"challonge-edit-signup_cap-desc")));
            options.add(SelectOption.of(TL(M,"Channels"), "edit-channels").withEmoji(Emoji.fromUnicode("U+2194U+fe0f")).withDescription(TL(M,"challonge-edit-channels-desc")));
            options.add(SelectOption.of(TL(M,"Roles"), "edit-roles").withEmoji(Emoji.fromUnicode("U+2194U+fe0f")).withDescription(TL(M,"challonge-edit-roles-desc")));
            if (isTournamentManager(event.getUser())) {
                options.add(SelectOption.of("[ADMIN] Download", "download-tournament").withEmoji(Emoji.fromUnicode("U+2b07U+fe0f")).withDescription(TL(M,"challonge-edit-download")));
                options.add(SelectOption.of("[ADMIN] " + TL(M, "Delete"), "delete-tournament").withEmoji(Emoji.fromUnicode("U+274c")).withDescription(TL(M,"challonge-edit-delete")));
                options.add(SelectOption.of("[ADMIN] Get List Raw", "list-raw").withEmoji(Emoji.fromUnicode("U+1F4DC")).withDescription("List matches in your dms."));
                options.add(SelectOption.of("[ADMIN] Force Log Advertisement", "log-advertisement").withEmoji(Emoji.fromUnicode("U+1F4DC")).withDescription("Send an announcement in all infos channels of the com."));
                options.add(SelectOption.of("[ADMIN] Force Log Results", "log-results").withEmoji(Emoji.fromUnicode("U+1F4DC")).withDescription("Send an announcement of results in all infos channels of the com."));
                options.add(SelectOption.of("[ADMIN] Force DM Advertisement", "dm-advertisement").withEmoji(Emoji.fromUnicode("U+1F4DC")).withDescription("Send an announcement in dm to people of the server."));
            }
            options.add(SelectOption.of("Force Refresh", "refresh").withEmoji(Emoji.fromUnicode("U+1F504")));
            rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-manage-config"))
                    .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                    .addOptions(options).build()));

            if (isPending()) {
                rows.add(ActionRow.of(EntitySelectMenu.create(CMD.Command("challonge-manage-add-participants"), EntitySelectMenu.SelectTarget.USER)
                        .setPlaceholder(event.getUser().getEffectiveName()).setRequiredRange(1, 10).build()));
            }
            EmbedBuilder E = getTournamentEmbed();
            E.setTitle(TL(M,"tournament-manager"));
            E.addField(":small_blue_diamond: " + TL(M,"Name"), "> " + getName(), false);
            E.addField(":video_game: " + TL(M,"Game"), "> " + getGame().getFullName(), false);
            E.addField(":people_hugging: " + TL(M,"Players"), "> " + getParticipantCount(), false);
            E.addField(":blue_book: " + TL(M,"Description"), "> " + StopString(getDescription(), 512), false);
            E.addField(TL(M,"Other") + " ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                    "Start Time: " + (getStartAtTimeEpochSecond() != 0 ? " <t:" + getStartAtTimeEpochSecond() + ":R>" : "`" + TL(M,"None") + "`") + "\n" +
                            "Signup Cap: " + (getSignupCap() != 0 ? getSignupCap() : "`" + TL(M,"None")  + "`") + "\n", false);
            E.setDescription(TL(M, "tournament-manage-description") + "\n" + TL(M, "tournament-bracket-desc-1", getFullChallongeURL()));
            M.editOriginalEmbeds(E.build()).setComponents(rows).queue();
        } else {
            M.editOriginal(TL(M,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).queue();
        }
    }

    public void AddScoreSelectionUI(InteractionHook M, ChallongeCommand CMD) {
        List<SelectOption> options = new ArrayList<>();
        List<ActionRow> row = new ArrayList<>();
        for (M Match : getUnplayedMatches()) {
            if (Match.getPlayer1() != null && Match.getPlayer2() != null && !Match.isCompleted()) {
                if (getVSAmount() > 1) {
                    for (SubMatch CSM : Match.getSubMatches()) {
                        if (!CSM.isFinished()) {
                            options.add(SelectOption.of(Match.getRoundLong() + ": " + CSM.getP1().getName() + " vs. " + CSM.getP2().getName(), String.valueOf(CSM.getId())).withDescription("0 - 0"));
                            if (options.size() % 25 == 0) {
                                row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-manage-add-score-" + row.size()))
                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                                        .addOptions(options).build()));
                                options = new ArrayList<>();
                            }
                        }
                    }
                } else {
                    options.add(SelectOption.of(Match.getRoundLong() + ": " + Match.getPlayer1().getName() + " vs. " + Match.getPlayer2().getName(), String.valueOf(Match.getId())).withDescription("0 - 0"));
                    if (options.size() % 25 == 0) {
                        row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-manage-add-score-" + row.size()))
                                .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                                .addOptions(options).build()));
                        options = new ArrayList<>();
                    }
                }
            }
        }
        if (!options.isEmpty()) {
            row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-manage-add-score-" + row.size()))
                    .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
        }
        EmbedBuilder E = getTournamentEmbed();
        E.setTitle(TL(M,"tournament-manager"));
        E.setDescription(TL(M, "tournament-add-score", "**[" + TL(M, "Refresh") + "]**") + "\n</dq:1143587946793025557> `[user]` = DQ");
        M.editOriginalEmbeds(E.build()).setComponents(row).queue();
    }
    public void AddPredictionSelectionUI(InteractionHook M, ChallongeCommand CMD) {
        List<SelectOption> options = new ArrayList<>();
        List<ActionRow> row = new ArrayList<>();
        for (M Match : getMatches()) {
            if (Match.getPlayer1() != null && Match.getPlayer2() != null && !Match.isCompleted()) {
                options.add(SelectOption.of(Match.getRoundLong() + ": " + Match.getPlayer1().getName() + " vs. " + Match.getPlayer2().getName(), String.valueOf(Match.getId())).withDescription("0 - 0"));
                if (options.size() % 25 == 0) {
                    row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-manage-add-predi-" + row.size()))
                            .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                            .addOptions(options).build()));
                    options = new ArrayList<>();
                }
            }
        }
        if (!options.isEmpty()) {
            row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-manage-add-predi-" + row.size()))
                    .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
        }
        EmbedBuilder E = getTournamentEmbed();
        E.setTitle(TL(M,"tournament-manager"));
        E.setDescription(TL(M, "tournament-add-predi"));
        M.editOriginalEmbeds(E.build()).setComponents(row).queue();
    }
    public void RerandomMatchSelectionUI(ChallongeCommand CMD, InteractionHook M) {
        List<SelectOption> options = new ArrayList<>();
        List<ActionRow> row = new ArrayList<>();
        for (M Match : getOpenMatches()) {
            options.add(SelectOption.of(Match.getRoundLong() + ": " + Match.getPlayer1().getName() + " vs. " + Match.getPlayer2().getName(), String.valueOf(Match.getId())));
            if (options.size() % 25 == 0) {
                row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-edit-random-" + row.size()))
                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                        .addOptions(options).build()));
                options = new ArrayList<>();
            }
        }
        if (!options.isEmpty()) {
            row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-edit-random-" + row.size()))
                    .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
        }
        EmbedBuilder E = getTournamentEmbed();
        E.setTitle(TL(M,"tournament-manager"));
        E.setDescription(TL(M, "tournament-random-match-desc"));
        M.editOriginalEmbeds(E.build()).setComponents(row).queue();
    }
    public void EditMatchSelectionUI(ChallongeCommand CMD, InteractionHook M) {
        List<SelectOption> options = new ArrayList<>();
        List<ActionRow> row = new ArrayList<>();
        for (M Match : getMatches(true)) {
            String score = (Match.getP1Score() + " - " + Match.getP2Score()).replaceAll("-1", "DQ");
            options.add(SelectOption.of(Match.getRoundLong() + ": " + Match.getPlayer1().getName() + " vs. " + Match.getPlayer2().getName(), String.valueOf(Match.getId())).withDescription(score));
            if (options.size() % 25 == 0) {
                row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-edit-score-" + row.size()))
                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                        .addOptions(options).build()));
                options = new ArrayList<>();
            }
        }
        if (!options.isEmpty()) {
            row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-edit-score-" + row.size()))
                    .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
        }
        EmbedBuilder E = getTournamentEmbed();
        E.setTitle(TL(M,"tournament-manager"));
        E.setDescription(TL(M, "tournament-add-score", "**[" + TL(M, "Refresh") + "]**"));
        M.editOriginalEmbeds(E.build()).setComponents(row).queue();
    }
    public void EditParticipantSelectionUI(ChallongeCommand CMD, InteractionHook M) {
        List<SelectOption> options = new ArrayList<>();
        List<ActionRow> row = new ArrayList<>();
        for (P P : getParticipants()) {
            String name = P.getLeader() != null ? (P.getLeader().getName() + " / " + P.getLeader().getEffectiveName()) : TL(M, "None");
            options.add(SelectOption.of("• " + P.getName(), P.getId() + "").withDescription("@" + name));
            if (options.size() % 25 == 0) {
                row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-edit-participant-" + row.size()))
                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                        .addOptions(options).build()));
                options = new ArrayList<>();
            }
        }
        if (!options.isEmpty()) {
            row.add(ActionRow.of(StringSelectMenu.create(CMD.Command("challonge-edit-participant-" + row.size()))
                    .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
        }
        EmbedBuilder E = getTournamentEmbed();
        E.setTitle(TL(M,"tournament-manager"));
        E.setDescription(TL(M,"challonge-manage-participant-success-1", getParticipantCount()));
        M.editOriginalEmbeds(E.build()).setComponents(row).queue();
    }

    public abstract P createParticipant(Participant P, Long discordID);
    public abstract M createMatch(Match M);

    public static BaseCTournament<?,?,?> get(long challongeId) {
        if (doQueryValue(Long.class, "SELECT ServerID FROM challonge_tournament WHERE ID = ?", challongeId).orElse(null) == null) {
            return GChallonge_Tournament.get(challongeId);
        } else {
            SChallonge_Tournament T = SChallonge_Tournament.get(challongeId);
            T.I = ServerInfo.get(T.ServerID);
            return T;
        }
    }
}
