package iecompbot.springboot.projection;

import iecompbot.objects.Nationality;
import iecompbot.objects.server.ServerInfo;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

import static iecompbot.Main.DiscordAccount;
import static iecompbot.springboot.data.DatabaseObject.doQuery;

public class P_ServerInfo {

    public DatabaseObject.Row Act = null;
    public List<DatabaseObject.Row> PlayerStats = null;
    public User Owner;

    public long ID;
    public String Name;
    public String IconUrl;
    public Nationality Nationality;
    public String DominantColorcode;
    public String PermanentInviteLink;
    public String Description;
    public int MemberCount;
    public int ToleranceLevel;

    public String History;

    public long CreatedAtTimeEpochSecond;

    public boolean isPublic;
    public boolean AdminAcceptOnly;
    public boolean areScoresAllowed;
    public boolean areTournamentsAllowed;
    public boolean areWinnerRolesAllowed;

    public boolean areClanRolesAllowed;
    public boolean areClanTagsAllowed;
    public boolean areClanUpdatesGlobal;

    public boolean areGlobalRankAllowed;
    public boolean areGalaxyRanksAllowed;
    public boolean areCSRanksAllowed;
    public boolean areGO1RanksAllowed;
    public boolean areStrikersRanksAllowed;
    public boolean areXtremeRanksAllowed;
    public boolean areVRBetaRanksAllowed;
    public boolean areVROHRanksAllowed;
    public boolean areSDRanksAllowed;
    public boolean areIE3RanksAllowed;
    public boolean areIE2RanksAllowed;
    public boolean areIE1RanksAllowed;

    public boolean showBoardMembersOnly;
    public boolean isGalaxyBoardAllowed;
    public boolean isCSBoardAllowed;
    public boolean isGO1BoardAllowed;
    public boolean isStrikersBoardAllowed;
    public boolean isXtremeBoardAllowed;
    public boolean isVRBetaBoardAllowed;
    public boolean isVROHBoardAllowed;
    public boolean isIE3BoardAllowed;
    public boolean isIE2BoardAllowed;
    public boolean isIE1BoardAllowed;
    public boolean isSDBoardAllowed;

    public int TournamentCount;

    public String WebsiteURL = null;
    public String TwitterURL = null;
    public String TwitchURL = null;
    public String YouTubeURL = null;
    public String InstagramURL = null;
    public String TiktokURL = null;

    public int MatchesCount = 0;
    public P_ServerInfo(ServerInfo serverInfo) {
        this.ID = serverInfo.getId();
        this.Name = serverInfo.getGuild().getName();
        this.IconUrl = serverInfo.getGuild().getIconUrl() == null ? DiscordAccount.getSelfUser().getEffectiveAvatarUrl() : serverInfo.getGuild().getIconUrl();
        this.Owner = serverInfo.getGuild().getOwner().getUser();
        this.Nationality = serverInfo.getNationality();
        this.DominantColorcode = serverInfo.DominantColorcode;
        this.PermanentInviteLink = serverInfo.getPermanentInviteLink();
        this.MemberCount = serverInfo.MemberCount;
        this.ToleranceLevel = serverInfo.ToleranceLevel;
        this.CreatedAtTimeEpochSecond = serverInfo.getGuild().getTimeCreated().toEpochSecond();
        this.Description = serverInfo.getGuild().getDescription() == null ? "Inazuma Eleven Server" : serverInfo.getGuild().getDescription();

        this.isPublic = serverInfo.isPublic;
        this.AdminAcceptOnly = serverInfo.AdminAcceptOnly;
        this.areScoresAllowed = serverInfo.areScoresAllowed;
        this.areTournamentsAllowed = serverInfo.areTournamentsAllowed;
        this.areWinnerRolesAllowed = serverInfo.areWinnerRolesAllowed;

        this.areClanRolesAllowed = serverInfo.areClanRolesAllowed;
        this.areClanTagsAllowed = serverInfo.areClanTagsAllowed;

        this.areGlobalRankAllowed = serverInfo.areGlobalRankAllowed;
        this.areGalaxyRanksAllowed = serverInfo.areGalaxyRanksAllowed;
        this.areCSRanksAllowed = serverInfo.areCSRanksAllowed;
        this.areGO1RanksAllowed = serverInfo.areGO1RanksAllowed;
        this.areStrikersRanksAllowed = serverInfo.areStrikersRanksAllowed;
        this.areXtremeRanksAllowed = serverInfo.areXtremeRanksAllowed;
        this.areVRBetaRanksAllowed = serverInfo.areVRBetaRanksAllowed;
        this.areVROHRanksAllowed = serverInfo.areVROHRanksAllowed;
        this.areSDRanksAllowed = serverInfo.areSDRanksAllowed;
        this.areIE3RanksAllowed = serverInfo.areIE3RanksAllowed;
        this.areIE2RanksAllowed = serverInfo.areIE2RanksAllowed;
        this.areIE1RanksAllowed = serverInfo.areIE1RanksAllowed;

        this.showBoardMembersOnly = serverInfo.showBoardMembersOnly;
        this.isGalaxyBoardAllowed = serverInfo.isGalaxyBoardAllowed;
        this.isCSBoardAllowed = serverInfo.isCSBoardAllowed;
        this.isGO1BoardAllowed = serverInfo.isGO1BoardAllowed;
        this.isStrikersBoardAllowed = serverInfo.isStrikersBoardAllowed;
        this.isXtremeBoardAllowed = serverInfo.isXtremeBoardAllowed;
        this.isVRBetaBoardAllowed = serverInfo.isVRBetaBoardAllowed;
        this.isVROHBoardAllowed = serverInfo.isVROHBoardAllowed;
        this.isIE3BoardAllowed = serverInfo.isIE3BoardAllowed;
        this.isIE2BoardAllowed = serverInfo.isIE2BoardAllowed;
        this.isIE1BoardAllowed = serverInfo.isIE1BoardAllowed;
        this.isSDBoardAllowed = serverInfo.isSDBoardAllowed;

        this.TournamentCount = serverInfo.getTournamentCount();
        this.History = serverInfo.History;

        this.WebsiteURL = serverInfo.WebsiteURL;
        this.TwitterURL = serverInfo.TwitterURL;
        this.TwitchURL = serverInfo.TwitchURL;
        this.YouTubeURL = serverInfo.YouTubeURL;
        this.InstagramURL = serverInfo.InstagramURL;
        this.TiktokURL = serverInfo.TiktokURL;

        this.MatchesCount = serverInfo.getMatchesCount();
    }

    public DatabaseObject.Row getAct() {
        return Act == null ? Act = doQuery("CALL DisplayServerActivity(?,?,?,?)", ID, null, 30, 3).orElse(null) : Act;
    }
}