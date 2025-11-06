package iecompbot.ai.info;

import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;

import java.util.List;
import java.util.stream.Collectors;

public class AI_ServerInfo {

    public DatabaseObject.Row ActivityDetails = null;
    public String OwnerName = null;

    public String Name;
    public String Nationality;
    public String PermanentInviteLink;
    public String Description;

    public String Background;
    public String WebsiteURL = null;
    public String TwitterURL = null;
    public String TwitchURL = null;
    public String YouTubeURL = null;
    public String InstagramURL = null;
    public String DiscordURL = null;
    public String TiktokURL = null;

    public boolean isPublic;

    public int TournamentCount;
    public int MemberCount;
    public int ToleranceLevel;

    public long CreatedAtTimeEpochSecond = 0;

    public List<AI_Tournament> OpenForRegistrationTournaments;
    public List<AI_Tournament> UnderwayTournaments;

    public AI_ServerInfo(ServerInfo serverInfo) {
        try {
            this.Name = serverInfo.getGuild().getName();
            this.Nationality = serverInfo.getNationality().getName();
            this.PermanentInviteLink = serverInfo.getPermanentInviteLink();
            this.MemberCount = serverInfo.MemberCount;
            this.ToleranceLevel = serverInfo.ToleranceLevel;
            this.Description = serverInfo.getGuild().getDescription() == null ? "Inazuma Eleven Server" : serverInfo.getGuild().getDescription();
            this.isPublic = serverInfo.isPublic;
            this.TournamentCount = serverInfo.getTournamentCount();
            this.Background = serverInfo.History;

            this.WebsiteURL = serverInfo.WebsiteURL;
            this.TwitterURL = serverInfo.TwitterURL;
            this.TwitchURL = serverInfo.TwitchURL;
            this.YouTubeURL = serverInfo.YouTubeURL;
            this.InstagramURL = serverInfo.InstagramURL;
            this.TiktokURL = serverInfo.TiktokURL;

            this.OpenForRegistrationTournaments = serverInfo.getActiveChallonges().stream().filter(SChallonge_Tournament::isPending).map(AI_Tournament::new).collect(Collectors.toList());
            this.UnderwayTournaments = serverInfo.getActiveChallonges().stream().filter(SChallonge_Tournament::isUnderway).map(AI_Tournament::new).collect(Collectors.toList());

            if (OpenForRegistrationTournaments.isEmpty()) OpenForRegistrationTournaments = null;
            if (UnderwayTournaments.isEmpty()) UnderwayTournaments = null;


            //ActivityDetails = DBM.processQuery("CALL DisplayServerActivity(?,?,?,?)", serverInfo.getId(), null, 30, 3).getFirst();

            this.CreatedAtTimeEpochSecond = serverInfo.getGuild().getTimeCreated().toEpochSecond();
            this.OwnerName = serverInfo.getGuild().getOwner().getUser().getEffectiveName();
        } catch (Exception ignored) {}
    }
}