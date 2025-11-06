package iecompbot.ai.info;

import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Match;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Participant;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;

import java.util.List;
import java.util.stream.Collectors;

public class AI_Tournament {

    public String ServerOrganisedOn;
    public String Name;
    public String Type;
    public String Description;
    public String GameName;
    public int ParticipantCount;
    public int SignupCap;

    public String TournamentInviteLink;

    public long StartAtTimeEpochSecond = 0;
    public long CompletedAtTimeEpochSecond = 0;

    public transient List<AI_Participant> Participants;
    public transient List<AI_Match> Matches;

    public String State;

    public String FullChallongeURL;
    public int ProgressMeterPercentage;

    public String Members;

    public AI_Tournament(SChallonge_Tournament tournament) {
        try {
            this.Name = tournament.getName();
            this.Type = tournament.getType().toString();
            this.Description = tournament.getDescription() + "\n\n" + tournament.getVSAmount() + "v" + tournament.getVSAmount();
            this.GameName = tournament.getGame().getEmojiFormatted() + " " + tournament.getGame().getFullName();
            this.ParticipantCount = tournament.getParticipantCount();
            this.SignupCap = tournament.getSignupCap();

            this.TournamentInviteLink = tournament.InscriptionChannelInviteLink;

            this.StartAtTimeEpochSecond = tournament.getStartAtTimeEpochSecond();
            this.CompletedAtTimeEpochSecond = tournament.getCompletedAtTime().getEpochSecond();

            this.State = tournament.State;


            this.FullChallongeURL = tournament.FullChallongeURL;
            this.ProgressMeterPercentage = tournament.ProgressMeter;

            this.Participants = tournament.getParticipants().stream().map(AI_Participant::new).collect(Collectors.toList());
            this.Matches = tournament.getMatches().stream().map(AI_Match::new).collect(Collectors.toList());

            if (tournament.I == null) tournament.I = ServerInfo.get(tournament.ServerID);
            this.ServerOrganisedOn = tournament.I.Name;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class AI_Participant {
        public String Name;
        public int PositionIfTournamentIsCompleted;
        public int GoalsScored;
        public int GoalsTaken;
        public int Wins;
        public int Loses;
        public int Ties;
        public AI_Participant(SChallonge_Participant p) {
            this.Name = p.Name;
            this.PositionIfTournamentIsCompleted = p.Position;
            this.GoalsScored = p.GoalsScored;
            this.GoalsTaken = p.GoalsTaken;
            this.Wins = p.Wins;
            this.Loses = p.Loses;
            this.Ties = p.Ties;
        }
    }

    public static class AI_Match {
        public String P1Name;
        public String P2Name;
        public int P1Score;
        public int P2Score;
        public String Round;
        public AI_Match(SChallonge_Match m) {
            this.P1Name = m.getPlayer1().getName();
            this.P2Name = m.getPlayer2().getName();
            this.P1Score = m.getP1Score();
            this.P2Score = m.getP2Score();
            this.Round = m.getRoundLong();
        }
    }
}
