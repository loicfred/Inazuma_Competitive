package iecompbot.objects.server;

import at.stefangeyer.challonge.model.enumeration.TournamentState;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.Constants;
import iecompbot.objects.match.Game;
import iecompbot.objects.match.League;
import iecompbot.objects.Nationality;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.match.MatchLog_S;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Match;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Participant;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static iecompbot.L10N.TLG;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.img.ImgUtilities.getDominantColor;
import static iecompbot.img.ImgUtilities.getHexValue;
import static iecompbot.interaction.Automation.hasPermissionInChannel;
import static iecompbot.interaction.GuildReady.RefreshAllLeaderboards;
import static iecompbot.interaction.GuildReady.RefreshAllPrivateCMDs;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.springboot.config.AppConfig.cacheService;
import static my.utilities.util.Utilities.*;
import static my.utilities.var.Constants.ProgramZoneId;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ServerInfo extends DatabaseObject<ServerInfo> {
    private transient Guild Guild;
    private transient ServerInfo_Roles Roles;
    private transient ServerInfo_Channels Channels;
    private transient ServerInfo_Ranking Ranking;
    private transient List<ServerInfo_Matchmaking> Matchmakings;
    private transient List<Blacklist> Blacklists;
    private transient List<Item> AllItems;

    public long ID;
    public String Name;
    public String Description;
    public String IconUrl;
    public String OwnerID;
    public String NationalityName = "English, UK";
    public String DominantColorcode = "#808080";
    public String PermanentInviteLink;
    public int MemberCount = 0;
    public int ToleranceLevel = 4;

    public boolean isPublic = true;

    public boolean AdminAcceptOnly = false;

    public boolean areScoresAllowed = false;
    public boolean areTournamentsAllowed = false;
    public boolean areWinnerRolesAllowed = false;

    public boolean areClanRolesAllowed = false;
    public boolean areClanTagsAllowed = false;

    public boolean areGlobalRankAllowed = false;
    public boolean areGalaxyRanksAllowed = false;
    public boolean areCSRanksAllowed = false;
    public boolean areGO1RanksAllowed = false;
    public boolean areStrikersRanksAllowed = false;
    public boolean areXtremeRanksAllowed = false;
    public boolean areVRBetaRanksAllowed = false;
    public boolean areVROHRanksAllowed = false;
    public boolean areSDRanksAllowed = false;
    public boolean areIE3RanksAllowed = false;
    public boolean areIE2RanksAllowed = false;
    public boolean areIE1RanksAllowed = false;

    public boolean showBoardMembersOnly = false;
    public boolean isGalaxyBoardAllowed = true;
    public boolean isCSBoardAllowed = true;
    public boolean isGO1BoardAllowed = true;
    public boolean isStrikersBoardAllowed = true;
    public boolean isXtremeBoardAllowed = true;
    public boolean isVRBetaBoardAllowed = true;
    public boolean isVROHBoardAllowed = true;
    public boolean isIE3BoardAllowed = true;
    public boolean isIE2BoardAllowed = true;
    public boolean isIE1BoardAllowed = true;
    public boolean isSDBoardAllowed = true;

    public int TournamentCount = 0;

    public String History = "-";

    public String WebsiteURL = null;
    public String TwitterURL = null;
    public String TwitchURL = null;
    public String YouTubeURL = null;
    public String InstagramURL = null;
    public String TiktokURL = null;

    public int CurrencyPerWin = 0;
    public int CurrencyPerTop1 = 0;
    public int CurrencyPerTop2 = 0;
    public int CurrencyPerTop3 = 0;






    public long getID() {
        return ID;
    }
    public String getName() {
        return Name;
    }
    public String getDescription() {
        return Description;
    }
    public String getFlag() {
        if (NationalityName == null) return "";
        Nationality Nat = Nationality.get(NationalityName);
        if (Nat == null) return "";
        return Nat.getFlag().getFormatted() + " ";
    }
    public String getIconUrl() {
        return IconUrl;
    }
    public String getOwnerID() {
        return OwnerID;
    }
    public boolean isPublic() {
        return isPublic;
    }
    private transient User owner;
    public User getOwner() {
        return owner == null ? owner = getUserByID(getOwnerID()) : owner;
    }
    public String getWebsiteURL() {
        return WebsiteURL;
    }
    public String getTwitterURL() {
        return TwitterURL;
    }
    public String getTwitchURL() {
        return TwitchURL;
    }
    public String getYouTubeURL() {
        return YouTubeURL;
    }
    public String getInstagramURL() {
        return InstagramURL;
    }
    public String getTiktokURL() {
        return TiktokURL;
    }
    public Nationality getNationality() {
        return NationalityName == null ? null : Nationality.get(NationalityName);
    }
    public Color getColor() {
        return Color.decode(DominantColorcode);
    }
    public int getToleranceLevel() {
        return ToleranceLevel;
    }
    public boolean isAdminAcceptOnly() {
        return AdminAcceptOnly;
    }
    public boolean isAreScoresAllowed() {
        return areScoresAllowed;
    }
    public boolean isAreTournamentsAllowed() {
        return areTournamentsAllowed;
    }
    public boolean isAreWinnerRolesAllowed() {
        return areWinnerRolesAllowed;
    }
    public boolean isAreClanRolesAllowed() {
        return areClanRolesAllowed;
    }
    public boolean isAreClanTagsAllowed() {
        return areClanTagsAllowed;
    }
    public int getTournamentCount() {
        return TournamentCount;
    }
    public int getCurrencyPerWin() {
        return CurrencyPerWin;
    }
    public int getCurrencyPerTop1() {
        return CurrencyPerTop1;
    }
    public int getCurrencyPerTop2() {
        return CurrencyPerTop2;
    }
    public int getCurrencyPerTop3() {
        return CurrencyPerTop3;
    }
    public String getPermanentInviteLink() {
        return PermanentInviteLink == null ? getGuild() != null ? getGuild().getVanityUrl() : PermanentInviteLink : PermanentInviteLink;
    }
    public String getHistory() {
        return History;
    }
    public boolean hasLeaderboardModifiers() {
        return Ranking().hasPrivateRanking() || showBoardMembersOnly;
    }
    public int getMatchesCount() {
        return Math.toIntExact(Count(MatchLog_S.class, "ServerID = ?", getID()));
    }

    public boolean isGameRankAllowed(Game game) {
        return checkBoard(game, areGalaxyRanksAllowed, areCSRanksAllowed, areGO1RanksAllowed, areIE3RanksAllowed, areIE2RanksAllowed, areIE1RanksAllowed, areVRBetaRanksAllowed, areVROHRanksAllowed, areStrikersRanksAllowed, areXtremeRanksAllowed, areSDRanksAllowed);
    }
    public boolean isGameBoardAllowed(Game game) {
        return checkBoard(game, isGalaxyBoardAllowed, isCSBoardAllowed, isGO1BoardAllowed, isIE3BoardAllowed, isIE2BoardAllowed, isIE1BoardAllowed, isVRBetaBoardAllowed, isVROHBoardAllowed, isStrikersBoardAllowed, isXtremeBoardAllowed, isSDBoardAllowed);
    }
    private boolean checkBoard(Game game, boolean isGalaxyBoardAllowed, boolean isCSBoardAllowed, boolean isGO1BoardAllowed, boolean isIE3BoardAllowed, boolean isIE2BoardAllowed, boolean isIE1BoardAllowed, boolean isVRBetaBoardAllowed, boolean isVROHBoardAllowed, boolean isStrikersBoardAllowed, boolean isXtremeBoardAllowed, boolean isSDBoardAllowed) {
        return switch (game.getCode()) {
            case "IEGOGLX" -> isGalaxyBoardAllowed;
            case "IEGOCS" -> isCSBoardAllowed;
            case "IEGO1" -> isGO1BoardAllowed;
            case "IE3" -> isIE3BoardAllowed;
            case "IE2" -> isIE2BoardAllowed;
            case "IE1" -> isIE1BoardAllowed;
            case "IEVRBETA" -> isVRBetaBoardAllowed;
            case "IEVR" -> isVROHBoardAllowed;
            case "IEGOSTR" -> isStrikersBoardAllowed;
            case "IEGOSTRXTR" -> isXtremeBoardAllowed;
            case "IESD" -> isSDBoardAllowed;
            default -> false;
        };
    }

    public EmbedBuilder getServerEmbed() {
        EmbedBuilder E = new EmbedBuilder();
        E.setAuthor(" • " + getGuild().getName(), null, getGuild().getIconUrl());
        E.setThumbnail(getGuild().getIconUrl());
        E.setColor(getColor());
        return E;
    }
    public WebhookMessageBuilder getServerWebhook() {
        return new WebhookMessageBuilder().setUsername(getGuild().getName()).setAvatarUrl(getGuild().getIconUrl());
    }


    public void setID(long id) {
        ID = id;
    }
    public void setName(String name) {
        Name = name;
    }
    public void setOwnerID(String ID) {
        OwnerID = ID;
    }
    public void setDescription(String description) {
        Description = StopString(description, 1024);
    }
    public void setIconUrl(String iconUrl) {
        IconUrl = iconUrl;
    }
    public void setNationalityName(Nationality nat) {
        NationalityName = nat.getName();
    }
    public void setDominantColorcode(String dominantColorcode) {
        DominantColorcode = dominantColorcode;
    }
    public void setPermanentInviteLink(String permanentInviteLink) {
        if (permanentInviteLink != null && permanentInviteLink.length() >= 24 && permanentInviteLink.length() <= 32) PermanentInviteLink = permanentInviteLink;
    }
    public void setMemberCount(int memberCount) {
        MemberCount = memberCount;
    }
    public void setHistory(String history) {
        History = StopString(history, 1024);
    }
    public void setIsPublic(boolean aPublic) {
        isPublic = aPublic;
    }
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
    public void setAdminAcceptOnly(boolean adminAcceptOnly) {
        AdminAcceptOnly = adminAcceptOnly;
    }
    public void setAreScoresAllowed(boolean areScoresAllowed) {
        this.areScoresAllowed = areScoresAllowed;
    }
    public void setAreTournamentsAllowed(boolean areTournamentsAllowed) {
        this.areTournamentsAllowed = areTournamentsAllowed;
    }
    public void setAreWinnerRolesAllowed(boolean areWinnerRolesAllowed) {
        this.areWinnerRolesAllowed = areWinnerRolesAllowed;
    }
    public void setAreClanRolesAllowed(boolean areClanRolesAllowed) {
        this.areClanRolesAllowed = areClanRolesAllowed;
    }
    public void setAreClanTagsAllowed(boolean areClanTagsAllowed) {
        this.areClanTagsAllowed = areClanTagsAllowed;
    }

    public void setAreGlobalRankAllowed(boolean areGlobalRankAllowed) {
        this.areGlobalRankAllowed = areGlobalRankAllowed;
    }
    public void setGameRanksAllowed(boolean isAllowed, Game G) {
        switch (G.getCode()) {
            case "IEGOGLX" -> this.areGalaxyRanksAllowed = isAllowed;
            case "IEGOCS" -> this.areCSRanksAllowed = isAllowed;
            case "IEGOGO1" -> this.areGO1RanksAllowed = isAllowed;
            case "IE1" -> this.areIE1RanksAllowed = isAllowed;
            case "IE2" -> this.areIE2RanksAllowed = isAllowed;
            case "IE3" -> this.areIE3RanksAllowed = isAllowed;
            case "IEGOSTR" -> this.areStrikersRanksAllowed = isAllowed;
            case "IEGOSTRXTR" -> this.areXtremeRanksAllowed = isAllowed;
            case "IEVR" -> this.areVROHRanksAllowed = isAllowed;
            case "IEVRBETA" -> this.areVRBetaRanksAllowed = isAllowed;
            case "IESD" -> this.areSDRanksAllowed = isAllowed;
        }
    }
    public void setGameBoardAllowed(boolean isAllowed, Game G) {
        switch (G.getCode()) {
            case "IEGOGLX" -> this.isGalaxyBoardAllowed = isAllowed;
            case "IEGOCS" -> this.isCSBoardAllowed = isAllowed;
            case "IEGO1" -> this.isGO1BoardAllowed = isAllowed;
            case "IE1" -> this.isIE1BoardAllowed = isAllowed;
            case "IE2" -> this.isIE2BoardAllowed = isAllowed;
            case "IE3" -> this.isIE3BoardAllowed = isAllowed;
            case "IEGOSTR" -> this.isStrikersBoardAllowed = isAllowed;
            case "IEGOSTRXTR" -> this.isXtremeBoardAllowed = isAllowed;
            case "IEVR" -> this.isVROHBoardAllowed = isAllowed;
            case "IEVRBETA" -> this.isVRBetaBoardAllowed = isAllowed;
            case "IESD" -> this.isSDBoardAllowed = isAllowed;
        }
    }
    public void setShowBoardMembersOnly(boolean showBoardMembersOnly) {
        this.showBoardMembersOnly = showBoardMembersOnly;
    }

    public void setWebsiteURL(String websiteURL) {
        WebsiteURL = CutString(websiteURL, 256);
    }
    public void setTwitterURL(String twitterURL) {
        TwitterURL = CutString(twitterURL, 256);
    }
    public void setTwitchURL(String twitchURL) {
        TwitchURL = CutString(twitchURL, 256);
    }
    public void setYouTubeURL(String youTubeURL) {
        YouTubeURL = CutString(youTubeURL, 256);
    }
    public void setInstagramURL(String instagramURL) {
        InstagramURL = CutString(instagramURL, 256);
    }
    public void setTiktokURL(String tiktokURL) {
        TiktokURL = CutString(tiktokURL, 256);
    }


    public SChallonge_Tournament getChallonge(long challongeId) {
        SChallonge_Tournament T = SChallonge_Tournament.get(this, challongeId);
        if (T != null) T.I = this;
        return T;
    }
    public List<SChallonge_Tournament> getChallonges(boolean completedOnly) {
        if (completedOnly) {
            List<SChallonge_Tournament> T = getAllWhere(SChallonge_Tournament.class,"ServerID = ? AND State = ? ORDER BY ID DESC", getID(), TournamentState.COMPLETE.toString());
            for (SChallonge_Tournament CT : T) CT.I = this;
            return T;
        } else {
            return SChallonge_Tournament.ofServer(this);
        }
    }
    public List<SChallonge_Tournament> getActiveChallonges() {
        List<SChallonge_Tournament> CTS = getAllWhere(SChallonge_Tournament.class,"ServerID = ? AND NOT State = ? AND (StartedAtTimeEpochSecond = 0 OR StartedAtTimeEpochSecond > ?)", getID(), TournamentState.COMPLETE.toString(), Instant.now().minus(30, ChronoUnit.DAYS).getEpochSecond());
        for (SChallonge_Tournament ct : CTS) ct.I = this;
        return CTS;
    }
    public SChallonge_Tournament getTournamentOfMatch(long userid, long opponentid, Game game, boolean completed) {
        for (SChallonge_Tournament T : getActiveChallonges()) {
            if (T.getGame().equals(game) && (T.isUnderway() || T.isGroupStageUnderway())) {
                if (T.getVSAmount() > 1) {
                    for (SChallonge_Match M : T.getMatches()) if (M.getSubMatch(userid, opponentid) != null) return T;
                } else {
                    SChallonge_Participant P = T.getParticipantById(userid);
                    if (P != null && P.getMatchWithOpponent(opponentid, completed) != null) return T;
                }
            }
        }
        return null;
    }
    public SChallonge_Tournament getTournamentOfMatchLog(long matchlogId) {
        return getActiveChallonges().stream().filter(T -> T.getMatchByLog(matchlogId) != null && (T.isUnderway() || T.isGroupStageUnderway())).findFirst().orElse(null);
    }


    public List<Item> listItems() {
        return AllItems == null ? AllItems = Item.getAllWhere(Item.class, "ServerID = ? AND NOT TYPE = 'CURRENCY'", getID()) : AllItems;
    }
    public Item getCurrency() {
        return cacheService.getCachedCurrency(getID());
    }
    public Item getItem(long id) {
        return Item.get(id);
    }
    public Item getItem(String name) {
        return cacheService.getCachedItemByName(name, getID());
    }
    public List<Item> getItems(Item.ItemType type) {
        return cacheService.getCachedItemByType(type, null);
    }
    public void setCurrencyPerWin(int currencyPerWin) {
        CurrencyPerWin = currencyPerWin;
    }
    public void setCurrencyPerTop1(int currencyPerTop1) {
        CurrencyPerTop1 = currencyPerTop1;
    }
    public void setCurrencyPerTop2(int currencyPerTop2) {
        CurrencyPerTop2 = currencyPerTop2;
    }
    public void setCurrencyPerTop3(int currencyPerTop3) {
        CurrencyPerTop3 = currencyPerTop3;
    }


    public List<SChallonge_Tournament> getTournamentsOfResultChannel(long channelid) {
        List<SChallonge_Tournament> L = new ArrayList<>();
        for (SChallonge_Tournament T : getActiveChallonges()) {
            try {
                if (T.getMatchResultChannel().getIdLong() == channelid) {
                    if (T.isPending() || T.isGroupStageFinalized()) {
                        T.ResyncChallonge();
                    }
                    L.add(T);
                }
            } catch (Exception ignored) {}
        }
        return L;
    }
    public List<SChallonge_Tournament> getTournamentsOfUser(String userid) {
        List<SChallonge_Tournament> L = new ArrayList<>();
        for (SChallonge_Tournament T : getActiveChallonges()) {
            if (T.getParticipantById(userid) != null) {
                L.add(T);
            }
        }
        return L;
    }


    public EmbedBuilder getBlacklistMessage() {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TLG(this,"Blacklist"));
        E.setDescription(TLG(this,"blacklist-description") + "\n\n`                                                `\n\n");
        E.setColor(Color.red);
        String vroh = "";
        String strikers = "";
        String galaxy = "";
        String cs = "";
        String go1 = "";
        String all = "";
        for (Blacklist B : getBlacklists()) {
            String input = "• " + B.getUser().getAsMention() + " (" + TLG(this, "End") + ": " + B.getTournamentsLeftTLG() + ")\n└ " + B.Reason + "\n";
            if (B.getGame() == null) {
                if (all.length() + input.length() > 1024) {
                    E.addField(TLG(this,"All"), all, false);
                    all = "";
                }
                all = all + input;
            } else if (B.getGame().isStrikers()) {
                if (all.length() + input.length() > 1024) {
                    E.addField(Game.get("Xtreme").getName(), all, false);
                    strikers = "";
                }
                strikers = strikers + input;
            } else if (B.getGame().getCode().equals("IEGOGLX")) {
                if (all.length() + input.length() > 1024) {
                    E.addField(Game.get("Galaxy").getName(), all, false);
                    galaxy = "";
                }
                galaxy = galaxy + input;
            } else if (B.getGame().getCode().equals("IEGOCS")) {
                if (all.length() + input.length() > 1024) {
                    E.addField(Game.get("CS").getName(), all, false);
                    cs = "";
                }
                cs = cs + input;
            } else if (B.getGame().getCode().equals("IEGO1")) {
                if (all.length() + input.length() > 1024) {
                    E.addField(Game.get("GO1").getName(), all, false);
                    go1 = "";
                }
                go1 = go1 + input;
            } else if (B.getGame().isVR()) {
                if (all.length() + input.length() > 1024) {
                    E.addField(Game.get("VR").getName(), all, false);
                    vroh = "";
                }
                vroh = vroh + input;
            }
        }
        if (all.length() > 1) {
            E.addField(TLG(this,"All"), all, false);
        }
        if (vroh.length() > 1) {
            E.addField(Game.get("VR").getName(), vroh, false);
        }
        if (strikers.length() > 1) {
            E.addField(Game.get("Xtreme").getName(), strikers, false);
        }
        if (galaxy.length() > 1) {
            E.addField(Game.get("Galaxy").getName(), galaxy, false);
        }
        if (cs.length() > 1) {
            E.addField(Game.get("CS").getName(), cs, false);
        }
        if (go1.length() > 1) {
            E.addField(Game.get("GO1").getName(), go1, false);
        }
        return E;
    }
    public List<Blacklist> getBlacklists() {
        return Blacklists == null ? Blacklists = Blacklist.get(this) : Blacklists;
    }
    public List<Blacklist> getBlacklists(Game game) {
        return Blacklist.get(this, game);
    }
    public Blacklist getBlacklist(User user, Game game) {
        return Blacklist.get(this, user, game);
    }
    public Blacklist addBlacklist(User user, int Fortournaments, Game game, String reason) {
        return new Blacklist(this, user, game, Fortournaments, reason);
    }
    public void ReductionBlacklist(Game game) {
        for (Blacklist B : Blacklist.get(this, game)) B.decrementTournamentLeft();
        for (Blacklist B : Blacklist.get(this, null)) B.decrementTournamentLeft();
    }


    public List<ServerInfo_Matchmaking> getMatchmakings() {
        return Matchmakings == null ? Matchmakings = ServerInfo_Matchmaking.get(this) : Matchmakings;
    }
    public ServerInfo_Matchmaking getMatchmaking(Game game) {
        return ServerInfo_Matchmaking.get(this, game);
    }


    public synchronized ServerInfo_Roles Roles() {
        return Roles == null ? Roles = ServerInfo_Roles.get(this) : Roles;
    }
    public synchronized ServerInfo_Channels Channels() {
        return Channels == null ? Channels = ServerInfo_Channels.get(this) : Channels;
    }
    public synchronized ServerInfo_Ranking Ranking() {
        return Ranking == null ? Ranking = ServerInfo_Ranking.get(getID()) : Ranking;
    }


    private ServerInfo() {}
    public ServerInfo(Guild guild) {
        if (guild == null) throw new NullPointerException("No Guild with given id.");
        if (!guild.isDetached()) {
            this.Guild = guild;
            this.ID = guild.getIdLong();
            this.OwnerID = guild.getOwnerId();
            this.IconUrl = guild.getIconUrl();
            this.Name = guild.getName();
            this.MemberCount = guild.getMemberCount();
            Write();
        }
    }
    public ServerInfo(long serverid) {
        this.ID = serverid;
        Write();
    }
    public static ServerInfo get(Guild guild) {
        if (guild == null) return null;
        return getById(ServerInfo.class, guild.getIdLong()).orElseGet(() -> new ServerInfo(guild));
    }
    public static ServerInfo get(String serverId) {
        return getById(ServerInfo.class, serverId).orElseGet(() -> new ServerInfo(DiscordAccount.getGuildById(serverId)));
    }
    public static ServerInfo get(Long serverId) {
        return get(String.valueOf(serverId));
    }
    public static List<ServerInfo> find(String like) {
        if (like.length() < 3) return new ArrayList<>();
        List<ServerInfo> S = getAllWhere(ServerInfo.class,"Name LIKE ? OR Description LIKE ? OR History LIKE ? OR Nationality LIKE ? AND PermanentInviteLink IS NOT NULL LIMIT 10", "%" + like + "%", "%" + like + "%", "%" + like + "%", "%" + like + "%");
        if (S.isEmpty()) {
            List<Row> TR = doQueryAll("SELECT ID, Name FROM inazuma_competitive.serverinfo WHERE PermanentInviteLink IS NOT NULL;");
            for (DatabaseObject.Row row : TR) {
                if (similarity(row.getAsString("Name"), like) > 60) {
                    S.add(ServerInfo.get(row.getAsLong("ID")));
                }
            }
        }
        return S;
    }
    public static List<ServerInfo> list() {
        return getAll(ServerInfo.class);
    }
    public static List<ServerInfo> list(boolean isInGuild) {
        List<ServerInfo> S = list();
        if (isInGuild) S.removeIf(s -> s.getGuild() == null);
        return S;
    }

    public Guild getGuild() {
        return Guild == null ? Guild = DiscordAccount.getGuildById(ID) : Guild;
    }

    public void LogSlash(String string) {
        if (getGuild() != null && Constants.LogChannel != null) {
            try {
                Constants.LogChannel.sendMessage("**[" + getGuild().getName() + "/" + getGuild().getId() + "]** " + string).queue();
                if (Channels().getLogChannel() != null && Channels().getLogChannel().canTalk()) {
                    Channels().getLogChannel().sendMessage(string).queue();
                } else {
                    Channels().setLogChannel(null);
                }
            } catch (Exception ignored) {}
        }
        string = string.replaceAll("\\*", "");
        string = string.replaceAll("_", "");
        System.out.println("[" + getHHmmss(Instant.now()) + "][" + getGuild().getName() + "] " + string);
    }




    public synchronized void RefreshGuildInformation() {
        if (getGuild() != null) {
            CleanMatchmakingChannels();
            setName(getGuild().getName());
            setDescription(getGuild().getDescription());
            setOwnerID(getGuild().getOwner().getUser().getId());
            try {
                if (getGuild().getFeatures().contains("COMMUNITY") && NationalityName == null || !NationalityName.equals("International")) {
                    setNationalityName(Nationality.get(getGuild().getLocale().getLanguageName()));
                }
                setDominantColorcode(getHexValue(getDominantColor(getGuild().getIconUrl())));
                setIconUrl(getIconUrl());
                TournamentCount = Count(SChallonge_Tournament.class, "ServerID = ?", getID());
            } catch (IOException ignored) {
            }
            setMemberCount(getGuild().getMemberCount());
            Update();
        }
    }
    public void CleanMatchmakingChannels() {
        if (Guild != null && !getMatchmakings().isEmpty()) {
            ZonedDateTime parisDateTime = Instant.now().minus(8, ChronoUnit.HOURS).atZone(ProgramZoneId);
            for (ServerInfo_Matchmaking M : getMatchmakings()) {
                TextChannel CHANNEL = M.getChannel();
                try {
                    if (hasPermissionInChannel(null, CHANNEL, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) {
                        CHANNEL.getHistory().retrievePast(50).queue(t -> {
                            for (Message m : t) {
                                if (m.getAuthor().equals(DiscordAccount.getSelfUser())) {
                                    if (m.getTimeCreated().isBefore(OffsetDateTime.from(parisDateTime))) {
                                        if (m.getContentDisplay().toLowerCase().contains("mention") || m.getContentDisplay().toLowerCase().contains("igno") || m.getContentDisplay().toLowerCase().contains("matchmaking")) {
                                            m.delete().queue();
                                        }
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception ignored) {}
            }
        }
    }










    public static class ServerInfo_Channels extends DatabaseObject<ServerInfo_Channels> {
        private transient ServerInfo I;

        private transient TextChannel LogChannel;
        private transient TextChannel NotificationChannel;
        private transient TextChannel ClanUpdatesChannel;
        private transient TextChannel TournamentUpdatesChannel;
        private transient ChannelMessage ClanlistCM;
        private transient ChannelMessage LeaderboardCM;
        private transient ChannelMessage BlacklistCM;
        private transient ChannelMessage ClanInfoCM;

        public long ID;
        public Long LogChannelID;
        public Long NotificationChannelID;
        public Long ClanUpdatesChannelID;
        public Long TournamentUpdatesChannelID;

        public Long ClanlistChannelID;
        public Long ClanlistMessageID;
        public Long ClanlistMessageID2;
        public Long LeaderboardChannelID;
        public Long LeaderboardMessageID;
        public Long LeaderboardMessageID2;
        public Long LeaderboardMessageID3;
        public Long ClanInfoChannelID;
        public Long ClanInfoMessageID;
        public Long ClanInfoMessageID2;
        public Long ClanInfoMessageID3;
        public Long BlacklistChannelID;
        public Long BlacklistMessageID;

        public void setLogChannel(Channel channel) {
            LogChannelID = channel == null ? null : channel.getIdLong();
            LogChannel = null;
        }
        public void setNotificationChannel(Channel channel) {
            NotificationChannelID = channel == null ? null : channel.getIdLong();
            NotificationChannel = null;
        }
        public void setClanUpdatesChannel(Channel channel) {
            ClanUpdatesChannelID = channel == null ? null : channel.getIdLong();
            ClanUpdatesChannel = null;
        }
        public void setTournamentUpdatesChannel(Channel channel) {
            TournamentUpdatesChannelID = channel == null ? null : channel.getIdLong();
            TournamentUpdatesChannel = null;
        }
        public void setClanlistCM(TextChannel channel) {
            ClanlistChannelID = channel == null ? null : channel.getIdLong();
            ClanlistMessageID = channel == null ? null : ClanlistMessageID;
            ClanlistMessageID2 = channel == null ? null : ClanlistMessageID2;
            ClanlistCM = null;
        }
        public void setLeaderboardCM(TextChannel channel) {
            LeaderboardChannelID = channel == null ? null : channel.getIdLong();
            LeaderboardMessageID = channel == null ? null : LeaderboardMessageID;
            LeaderboardMessageID2 = channel == null ? null : LeaderboardMessageID2;
            LeaderboardMessageID3 = channel == null ? null : LeaderboardMessageID3;
            LeaderboardCM = null;
        }
        public void setBlacklistCM(TextChannel channel) {
            BlacklistChannelID = channel == null ? null : channel.getIdLong();
            BlacklistMessageID = channel == null ? null : BlacklistMessageID;
            BlacklistCM = null;
        }
        public void setClanInfoCM(TextChannel channel) {
            ClanInfoChannelID = channel == null ? null : channel.getIdLong();
            ClanInfoMessageID = channel == null ? null : ClanInfoMessageID;
            ClanInfoMessageID2 = channel == null ? null : ClanInfoMessageID2;
            ClanInfoMessageID3 = channel == null ? null : ClanInfoMessageID3;
            ClanInfoCM = null;
        }



        public TextChannel getLogChannel() {
            if (LogChannel == null) {
                try {
                    LogChannel = I.getGuild().getTextChannelById(LogChannelID);
                } catch (IllegalArgumentException | NullPointerException ignored) {}
            }
            return LogChannel;
        }
        public TextChannel getNotificationChannel() {
            if (NotificationChannel == null) {
                try {
                    NotificationChannel = I.getGuild().getTextChannelById(NotificationChannelID);
                } catch (IllegalArgumentException | NullPointerException ignored) {}
            }
            return NotificationChannel;
        }
        public TextChannel getClanUpdatesChannel() {
            if (ClanUpdatesChannel == null) {
                try {
                    ClanUpdatesChannel = I.getGuild().getTextChannelById(ClanUpdatesChannelID);
                } catch (IllegalArgumentException | NullPointerException ignored) {}
            }
            return ClanUpdatesChannel;
        }
        public TextChannel getTournamentUpdatesChannel() {
            if (TournamentUpdatesChannel == null) {
                try {
                    TournamentUpdatesChannel = I.getGuild().getTextChannelById(TournamentUpdatesChannelID);
                } catch (IllegalArgumentException | NullPointerException ignored) {}
            }
            return TournamentUpdatesChannel;
        }
        public ChannelMessage getClanlist() {
            return ClanlistCM == null ? ClanlistCM = new ChannelMessage(ID, ClanlistChannelID, ClanlistMessageID, ClanlistMessageID2) : ClanlistCM;
        }
        public ChannelMessage getLeaderboard() {
            return LeaderboardCM == null ? LeaderboardCM = new ChannelMessage(ID, LeaderboardChannelID, LeaderboardMessageID, LeaderboardMessageID2, LeaderboardMessageID3) : LeaderboardCM;
        }
        public ChannelMessage getClanInfo() {
            return ClanInfoCM == null ? ClanInfoCM = new ChannelMessage(ID, ClanInfoChannelID, ClanInfoMessageID, ClanInfoMessageID2) : ClanInfoCM;
        }
        public ChannelMessage getBlacklist() {
            return BlacklistCM == null ? BlacklistCM = new ChannelMessage(ID, BlacklistChannelID, BlacklistMessageID) : BlacklistCM;
        }

        private ServerInfo_Channels() {}
        public ServerInfo_Channels(ServerInfo I) {
            this.ID = I.getID();
            this.I = I;
            Write();
        }

        public static ServerInfo_Channels get(ServerInfo I) {
            ServerInfo_Channels S = getById(ServerInfo_Channels.class, I.getID()).orElseGet(() -> new ServerInfo_Channels(I));
            S.I = I;
            return S;
        }
    }
    public static class ServerInfo_Roles extends DatabaseObject<ServerInfo_Roles> {
        private transient ServerInfo I;

        private transient Role ClanCaptainRole = null;
        private transient Role WinnerRole1 = null;
        private transient Role WinnerRole2 = null;
        private transient Role WinnerRole3 = null;

        public long ID;
        public Long ClanCaptainRoleID;
        public Long WinnerRoleID1;
        public Long WinnerRoleID2;
        public Long WinnerRoleID3;

        public Role getClanCaptainRole() {
            if (I.areClanRolesAllowed && ClanCaptainRole == null) {
                try {
                    ClanCaptainRole = I.getGuild().getRoleById(ClanCaptainRoleID);
                    if (ClanCaptainRole == null) {
                        ClanCaptainRoleID = createRole("Clan Captain", Emoji.fromUnicode("U+1f935").getFormatted(), Color.cyan, null).getIdLong();
                        UpdateOnly("ClanCaptainRoleID");
                    }
                } catch (Exception ignored) {}
            }
            return ClanCaptainRole;
        }
        public Role getWinnerRole1() {
            if (I.areWinnerRolesAllowed && WinnerRole1 == null) {
                try {
                    WinnerRole1 = I.getGuild().getRoleById(WinnerRoleID1);
                    if (WinnerRole1 == null) {
                        WinnerRoleID1 = createRole("Winner 1", "", Color.orange, null).getIdLong();
                        UpdateOnly("WinnerRoleID1");
                    }
                } catch (Exception ignored) {}
            }
            return WinnerRole1;
        }
        public Role getWinnerRole2() {
            if (I.areWinnerRolesAllowed && WinnerRole2 == null) {
                try {
                    WinnerRole2 = I.getGuild().getRoleById(WinnerRoleID2);
                    if (WinnerRole2 == null) {
                        WinnerRoleID2 = createRole("Winner 2", "", Color.orange, null).getIdLong();
                        UpdateOnly("WinnerRoleID2");
                    }
                } catch (Exception ignored) {}
            }
            return WinnerRole2;
        }
        public Role getWinnerRole3() {
            if (I.areWinnerRolesAllowed && WinnerRole3 == null) {
                try {
                    WinnerRole3 = I.getGuild().getRoleById(WinnerRoleID3);
                    if (WinnerRole3 == null) {
                        WinnerRoleID3 = createRole("Winner 3", "", Color.orange, null).getIdLong();
                        UpdateOnly("WinnerRoleID3");
                    }
                } catch (Exception ignored) {}
            }
            return WinnerRole3;
        }


        private transient List<Row> ClanRoles;
        private List<Row> getClanRolesTR() {
            return ClanRoles == null ? ClanRoles = doQueryAll("SELECT RoleID, ClanID FROM serverinfo_clanrole WHERE ServerID = ?", ID) : ClanRoles;
        }
        private transient List<Row> LeagueRoles;
        private List<Row> getLeagueRolesTR() {
            return LeagueRoles == null ? LeagueRoles = doQueryAll("SELECT RoleID, Tier FROM serverinfo_rankrole WHERE ServerID = ? AND GameCode IS NULL", ID) : LeagueRoles;
        }
        private transient List<Row> LeagueGRoles;
        private List<Row> getLeagueGRolesTR() {
            return LeagueGRoles == null ? LeagueGRoles = doQueryAll("SELECT RoleID, Tier, GameCode FROM serverinfo_rankrole WHERE ServerID = ? AND NOT GameCode IS NULL", ID): LeagueGRoles;
        }



        public Role getClanRole(Clan clan, boolean create) {
            if (I.areClanRolesAllowed && clan.isEligibleForRole()) {
                DatabaseObject.Row TR = getClanRolesTR().stream().filter(tr -> tr.getAsLong("ClanID") == clan.getId()).findAny().orElse(null);
                Role R = null;
                if (TR != null) {
                    try {R = I.getGuild().getRoleById(TR.getAsLong("RoleID"));
                        if (R == null) {
                            doUpdate("DELETE FROM serverinfo_clanrole WHERE ServerID = ? AND ClanID = ?", ID, clan.getId());
                            TR = null;
                        }
                    } catch (Exception ignored) {}
                }
                if (R == null) {
                    try {R = I.getGuild().getRolesByName(clan.getName(), false).getFirst();
                    } catch (Exception ignored) {}
                }
                if (R == null && create && clan.areMembersInGuild(I.getGuild())) R = createRole(clan.getName(), clan.getEmojiFormatted(), clan.getColor(), clan.hasEmblem() ? Icon.from(clan.getEmblem()) : null);
                if (TR == null && R != null) doUpdate("INSERT INTO serverinfo_clanrole (ID, ServerID, ClanID, RoleID) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE RoleID = ?", Instant.now().toEpochMilli(), ID, clan.getId(), R.getIdLong(), R.getIdLong());
                return R;
            }
            return null;
        }
        public Role getLeagueRole(League.League_Tier league) {
            if (I.areGlobalRankAllowed) {
                DatabaseObject.Row TR = getLeagueRolesTR().stream().filter(tr -> tr.getAsString("Tier").equals(league.getName())).findAny().orElse(null);
                Role R = null;
                if (TR != null) {
                    try {R = I.getGuild().getRoleById(TR.getAsLong("RoleID"));
                        if (R == null) {
                            doUpdate("DELETE FROM serverinfo_rankrole WHERE Tier = ? AND ServerID = ? AND GameCode IS NULL", league.getName(), ID);
                            TR = null;
                        }
                    } catch (Exception ignored) {}
                }
                if (R == null) R = createRole(league.getName() + " League", league.getTierEmojiFormatted(), league.getColor(), league.getTierIcon());
                if (TR == null && R != null) doUpdate("INSERT INTO serverinfo_rankrole (ID, ServerID, Tier, GameCode, RoleID) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE RoleID = ?", Instant.now().toEpochMilli(), ID, league.getName(), null, R.getIdLong(), R.getIdLong());
                return R;
            }
            return null;
        }
        public Role getLeagueRole(League.League_Tier league, Game game) {
            if (I.isGameRankAllowed(game)) {
                DatabaseObject.Row TR = getLeagueGRolesTR().stream().filter(tr -> tr.getAsString("Tier").equals(league.getName()) && tr.getAsString("GameCode").equals(game.getCode())).findAny().orElse(null);
                Role R = null;
                if (TR != null) {
                    try {R = I.getGuild().getRoleById(TR.getAsLong("RoleID"));
                        if (R == null) {
                            doUpdate("DELETE FROM serverinfo_rankrole WHERE Tier = ? AND ServerID = ? AND GameCode = ?", league.getName(), ID, game.getCode());
                            TR = null;
                        }
                    } catch (Exception ignored) {}
                }
                if (R == null) R = createRole(league.getName() + " League (" + game.getCode() + ")", league.getTierEmojiFormatted(), league.getColor(), league.getTierIcon());
                if (TR == null && R != null) doUpdate("INSERT INTO serverinfo_rankrole (ID, ServerID, Tier, GameCode, RoleID) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE RoleID = ?", Instant.now().toEpochMilli(), ID, league.getName(), game.getCode(), R.getIdLong(), R.getIdLong());
                return R;
            }
            return null;
        }

        public List<Role> getClanRoles() {
            List<Role> R = new ArrayList<>();
            if (I.areClanRolesAllowed) {
                for (DatabaseObject.Row TR : getClanRolesTR()) {
                    Role r = I.getGuild().getRoleById(TR.getAsLong("RoleID"));
                    if (r != null) R.add(r);
                }
            }
            R.sort(Comparator.comparingInt(Role::getPosition).reversed());
            return R;
        }
        public List<Role> getLeagueRoles() {
            List<Role> R = new ArrayList<>();
            if (I.areGlobalRankAllowed) {
                for (DatabaseObject.Row TR : getLeagueRolesTR()) {
                    Role r = I.getGuild().getRoleById(TR.getAsLong("RoleID"));
                    if (r != null) R.add(r);
                }
                R.sort(Comparator.comparingInt(Role::getPosition).reversed());
            }
            return R;
        }
        public List<Role> getLeagueRoles(Game game) {
            List<Role> R = new ArrayList<>();
            if (I.isGameRankAllowed(game)) {
                for (DatabaseObject.Row TR : getLeagueGRolesTR()) {
                    Role r = I.getGuild().getRoleById(TR.getAsLong("RoleID"));
                    if (r != null) R.add(r);
                }
                R.sort(Comparator.comparingInt(Role::getPosition).reversed());
            }
            return R;
        }

        private synchronized Role createRole(String name, String roleemoji, Color color, Icon icon) {
            try {
                if (I.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    if (I.getGuild().getRolesByName(name, false).isEmpty()) {
                        Role newrole = I.getGuild().createRole()
                                .setName(name)
                                .setColor(color)
                                .setHoisted(true)
                                .submit().orTimeout(10, TimeUnit.SECONDS).get();
                        I.LogSlash(TLG(I, "role-create", "__**" + roleemoji + " " + name + "**__"));
                        setRoleIcon(newrole, icon, roleemoji, false);
                        return newrole;
                    } else {
                        Role newrole = I.getGuild().getRolesByName(name, false).getFirst();
                        setRoleIcon(newrole, icon, roleemoji, false);
                        return newrole;
                    }
                } else {
                    I.setAreClanRolesAllowed(false);
                    I.setAreGlobalRankAllowed(false);
                    for (Game G : Game.values()) I.setGameRanksAllowed(false, G);
                    I.Update();
                    //I.LogSlash(TLG(I,"role-create-permission-fail",  roleemoji + " **__" + name + "__**"));
                }
            } catch (Exception ignored) {}
            return null;
        }
        public synchronized void setRoleIcon(Role role, Icon icon, String roleemoji, boolean replace) {
            try {
                if (icon != null && I.getGuild().getFeatures().contains("ROLE_ICONS")) {
                    if (I.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                        if (role.getIcon() == null || replace) { // if not exist, then true, and true if replace too
                            if (I.getGuild().getSelfMember().canInteract(role)) {
                                role.getManager().setIcon(icon).queue(R -> I.LogSlash(TLG(I, "role-icon-success", roleemoji + " **__" + role.getName() + "__**")));
                            } else {
                                //I.LogSlash(TLG(I,"role-icon-permission-fail", roleemoji + " **__" + role.getName() + "__**"));
                            }
                        }
                    } else {
                        I.setAreClanRolesAllowed(false);
                        I.setAreGlobalRankAllowed(false);
                        for (Game G : Game.values()) I.setGameRanksAllowed(false, G);
                        I.Update();
                    }
                }
            } catch (Exception ignored) {}
        }
        public synchronized void setRoleColor(Role role, Color color, String roleemoji) {
            try {
                if (!getHexValue(role.getColor()).equals(getHexValue(color))) {
                    if (I.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                        if (I.getGuild().getSelfMember().canInteract(role)) {
                            role.getManager().setColor(color).queue(R -> I.LogSlash(TLG(I, "role-recolor-success", roleemoji + " **__" + role.getName() + "__**", "**" + getHexValue(color) + "**")));
                        } else {
                            //I.LogSlash(TLG(I, "role-recolor-interact-fail", roleemoji + " **__" + role.getName() + "__**"));
                        }
                    } else {
                        I.setAreClanRolesAllowed(false);
                        I.setAreGlobalRankAllowed(false);
                        for (Game G : Game.values()) I.setGameRanksAllowed(false, G);
                        I.Update();
                        //I.LogSlash(TLG(I, "role-recolor-permission-fail", roleemoji + " **__" + role.getName() + "__**"));
                    }
                }
            } catch (Exception ignored) {}
        }
        public synchronized void renameRole(Role role, String newname, String roleemoji) {
            try {
                if (I.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    if (I.getGuild().getSelfMember().canInteract(role)) {
                        String oldname = role.getName();
                        role.getManager().setName(newname).queue(R -> I.LogSlash(TLG(I,"role-rename-success", "**" + oldname + "**", roleemoji + " **__" + newname + "__**")));
                    } else {
                        //I.LogSlash(TLG(I,"role-rename-interact-fail", roleemoji + " **__" + role.getName() + "__**"));
                    }
                } else {
                    I.setAreClanRolesAllowed(false);
                    I.setAreGlobalRankAllowed(false);
                    for (Game G : Game.values()) I.setGameRanksAllowed(false, G);
                    I.Update();
                    //I.LogSlash(TLG(I,"role-rename-permission-fail", roleemoji + " **__" + role.getName() + "__**"));
                }
            } catch (Exception ignored) {}
        }
        public synchronized void deleteRole(Role role, String roleemoji) {
            try {
                if (I.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    if (I.getGuild().getSelfMember().canInteract(role)) {
                        role.delete().queue(R -> I.LogSlash(TLG(I,"role-delete-success", roleemoji + " **__" + role.getName() + "__**")));
                    } else {
                        //I.LogSlash(TLG(I,"role-delete-interact-fail", roleemoji + " **__" + role.getName() + "__**"));
                    }
                } else {
                    I.setAreClanRolesAllowed(false);
                    I.setAreGlobalRankAllowed(false);
                    for (Game G : Game.values()) I.setGameRanksAllowed(false, G);
                    I.Update();
                    //I.LogSlash(TLG(I,"role-delete-permission-fail", roleemoji + " **__" + role.getName() + "__**"));
                }
            } catch (Exception ignored) {}
        }
        public synchronized void AddRoleToMember(Role role, String roleemoji, Member member) {
            try {
                if (member != null && member.getRoles().stream().noneMatch(R -> R.getIdLong() == role.getIdLong())) {
                    if (I.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                        if (I.getGuild().getSelfMember().canInteract(role)) {
                            I.getGuild().removeRoleFromMember(member, role).queue();
                            I.getGuild().addRoleToMember(member, role).queue(R -> I.LogSlash(TLG(I, "role-add-success", roleemoji + " **__" + role.getName() + "__**", "**" + member.getEffectiveName() + "**")));
                        } else {
                            //if (!isStartup) I.LogSlash(TLG(I, "role-add-interact-fail", roleemoji + " **__" + role.getName() + "__**", "**" + member.getEffectiveName() + "**"));
                        }
                    } else {
                        I.setAreClanRolesAllowed(false);
                        I.setAreGlobalRankAllowed(false);
                        for (Game G : Game.values()) I.setGameRanksAllowed(false, G);
                        I.Update();
                        //if (!isStartup) I.LogSlash(TLG(I, "role-add-permission-fail", roleemoji + " **__" + role.getName() + "__**", "**" + member.getEffectiveName() + "**"));
                    }
                }
            } catch (Exception ignored) {}
        }
        public synchronized void RemoveRoleFromMember(Role role, String roleemoji, Member member) {
            try {
                if (member != null && member.getRoles().stream().anyMatch(R -> R.getIdLong() == role.getIdLong())) {
                    if (I.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                        if (I.getGuild().getSelfMember().canInteract(role)) {
                            I.getGuild().addRoleToMember(member, role).queue();
                            I.getGuild().removeRoleFromMember(member, role).queue(R -> I.LogSlash(TLG(I, "role-remove-success", roleemoji + " **__" + role.getName() + "__**", "**" + member.getEffectiveName() + "**")));
                        } else {
                            //I.LogSlash(TLG(I, "role-remove-interact-fail", roleemoji + " **__" + role.getName() + "__**", "**" + member.getEffectiveName() + "**"));
                        }
                    } else {
                        I.setAreClanRolesAllowed(false);
                        I.setAreGlobalRankAllowed(false);
                        for (Game G : Game.values()) I.setGameRanksAllowed(false, G);
                        I.Update();
                        //I.LogSlash(TLG(I, "role-remove-permission-fail", roleemoji + " **__" + role.getName() + "__**", "**" + member.getEffectiveName() + "**"));
                    }
                }
            } catch (Exception ignored) {}
        }

        private transient RoleOrderAction RoleOrderManager = null;
        public void ArrangeClanRoles() {
            if (I.areClanRolesAllowed) {
                try {
                    if (RoleOrderManager == null) RoleOrderManager = I.getGuild().modifyRolePositions();
                    List<Role> R = getClanRoles();
                    for (int i = 0; i < R.size(); i++) {
                        RoleOrderManager.selectPosition(getClanRoles().get(i+1)).moveTo(R.getFirst().getPosition()-1).queue();
                    }
                } catch (IndexOutOfBoundsException ignored) {}
            }
        }
        public void ArrangeRankRoles() {
            if (I.areGlobalRankAllowed) {
                try {
                    if (RoleOrderManager == null) RoleOrderManager = I.getGuild().modifyRolePositions();
                    List<Role> R = getLeagueRoles();
                    for (int i = 0; i < R.size(); i++) {
                        RoleOrderManager.selectPosition(getLeagueRoles().get(i+1)).moveTo(R.getFirst().getPosition()-1).queue();
                    }
                } catch (IndexOutOfBoundsException ignored) {}
            }
        }
        public void ArrangeGamesRankRoles(Game G) {
            if (I.isGameRankAllowed(G)) {
                try {
                    if (RoleOrderManager == null) RoleOrderManager = I.getGuild().modifyRolePositions();
                    List<Role> R = getLeagueRoles(G);
                    for (int i = 0; i < R.size(); i++) {
                        RoleOrderManager.selectPosition(getLeagueRoles(G).get(i+1)).moveTo(R.getFirst().getPosition()-1).queue();
                    }
                } catch (IndexOutOfBoundsException ignored) {}
            }
        }

        private ServerInfo_Roles() {}
        public ServerInfo_Roles(ServerInfo I) {
            this.ID = I.ID;
            this.I = I;
            Write();
        }

        public static ServerInfo_Roles get(ServerInfo I) {
            ServerInfo_Roles S = getById(ServerInfo_Roles.class, I.getID()).orElseGet(() -> new ServerInfo_Roles(I));
            S.I = I;
            return S;
        }
    }
    public static class ServerInfo_Matchmaking extends DatabaseObject<ServerInfo_Matchmaking> {
        private transient ServerInfo I;
        private transient Game g;

        private long ID;
        private long ServerID;
        private Long ChannelID;
        private Long RoleID;
        private String GameCode;

        private transient TextChannel Channel = null;
        private transient Role Role = null;

        private ServerInfo_Matchmaking() {}
        public ServerInfo_Matchmaking(ServerInfo I, Long channelId, Long roleId, Game game) {
            ID = Instant.now().toEpochMilli();
            ServerID = I.getID();
            ChannelID = channelId;
            RoleID = roleId;
            GameCode = game.getCode();
            Write();
        }

        public long getId() {
            return ID;
        }
        public Game getGame() {
            return g == null ? g = Game.get(GameCode) : g;
        }
        public TextChannel getChannel() {
            if (Channel == null) {
                try {
                    Channel = I.getGuild().getTextChannelById(ChannelID);
                } catch (IllegalArgumentException | NullPointerException ignored) {}
            }
            return Channel;
        }
        public Role getRole() {
            if (Role == null) {
                try {
                    Role = I.getGuild().getRoleById(RoleID);
                } catch (IllegalArgumentException | NullPointerException ignored) {}
            }
            return Role;
        }

        public Long getChannelID() {
            return ChannelID;
        }
        public Long getRoleID() {
            return RoleID;
        }

        public void setChannelID(Long channelID) {
            ChannelID = channelID;
        }
        public void setRoleID(Long roleID) {
            RoleID = roleID;
        }

        public static ServerInfo_Matchmaking get(ServerInfo I, Game game) {
            ServerInfo_Matchmaking S = getWhere(ServerInfo_Matchmaking.class, "ServerID = ? AND GameCode = ?", I.getID(), game.getCode()).orElse(new ServerInfo_Matchmaking(I, null, null, game));
            if (S == null) return null;
            S.I = I;
            return S;
        }
        public static List<ServerInfo_Matchmaking> get(ServerInfo I) {
            List<ServerInfo_Matchmaking> S = getAllWhere(ServerInfo_Matchmaking.class, "ServerID = ?", I.getID());
            for (ServerInfo_Matchmaking SR : S) SR.I = I;
            return S;
        }
        public static List<ServerInfo_Matchmaking> get(Game game) throws Exception {
            List<ServerInfo_Matchmaking> S = getAllWhere(ServerInfo_Matchmaking.class, "GameCode = ?", game.getCode());
            for (ServerInfo_Matchmaking SM : S) SM.I = ServerInfo.get(SM.ServerID);
            return S;
        }
    }
    public static class ServerInfo_Ranking extends DatabaseObject<ServerInfo_Ranking> {
        public transient List<League.League_Tier> PrivateTiers;
        public transient List<League> PrivateLeagues;

        private long ID;
        private double WinPts = 50;
        private double TiePts = 5;
        private double LosePts = -25;
        private double MinRNG = -1;
        private double MaxRNG = 1;
        private double LeagueDiffPts = 5;
        private boolean hasPrivateRanking = false;
        private boolean hasPrivateLeagues = false;

        public boolean hasPrivateRanking() {
            return hasPrivateRanking;
        }
        public boolean hasPrivateLeagues() {
            return hasPrivateLeagues;
        }
        public double getWinPts() {
            return WinPts;
        }
        public double getTiePts() {
            return TiePts;
        }
        public double getLosePts() {
            return LosePts;
        }
        public double getMinRNG() {
            return MinRNG;
        }
        public double getMaxRNG() {
            return MaxRNG;
        }
        public double getLeagueDiffPts() {
            return LeagueDiffPts;
        }

        public void setPrivateRanking(boolean hasPrivateRanking) {
            if (this.hasPrivateRanking != hasPrivateRanking) {
                this.hasPrivateRanking = hasPrivateRanking;
                RefreshAllLeaderboards();
            }
        }
        public void setPrivateLeagues(boolean hasPrivateRankRoles) {
            if (this.hasPrivateLeagues != hasPrivateRankRoles) {
                this.hasPrivateLeagues = hasPrivateRankRoles;
                RefreshAllPrivateCMDs();
            }
        }
        public void setWinPts(double winPts) {
            WinPts = winPts;
        }
        public void setTiePts(double tiePts) {
            TiePts = tiePts;
        }
        public void setLosePts(double losePts) {
            LosePts = losePts;
        }
        public void setMinRNG(double minRNG) {
            MinRNG = minRNG;
        }
        public void setMaxRNG(double maxRNG) {
            MaxRNG = maxRNG;
        }
        public void setLeagueDiffPts(double leagueDiffPts) {
            LeagueDiffPts = leagueDiffPts;
        }


        public List<League> getLeagues() {
            return PrivateLeagues == null ? PrivateLeagues = getAllWhere(League.class, "ServerID = ? ORDER BY Start ASC", ID) : PrivateLeagues;
        }
        public League getLeague(long id) {
            return getLeagues().stream().filter(l -> l.getId() == id).findFirst().orElse(null);
        }
        public League getLeagueByMedal(int currentMedals) {
            return getLeagues().stream().filter(l -> l.getStart() <= Math.max(0,currentMedals) && Math.max(0,currentMedals) <= l.getEnd()).findFirst().orElseGet(() -> getLeagues().isEmpty() ? League.getByMedal(0) : getLeagues().getFirst());
        }

        public List<League.League_Tier> getTiers() {
            return PrivateTiers == null ? PrivateTiers = getAllWhere(League.League_Tier.class, "ServerID = ? ORDER BY Start ASC", ID) : PrivateTiers;
        }
        public League.League_Tier getTier(long id) {
            return getTiers().stream().filter(l -> l.getId() == id).findFirst().orElse(null);
        }
        public League.League_Tier getTierByMedal(int currentMedals) {
            return getTiers().stream().filter(l -> l.getStart() <= Math.max(0,currentMedals) && Math.max(0,currentMedals) <= l.getEnd()).findFirst().orElseGet(() -> getTiers().getFirst());
        }

        public ServerInfo_Ranking(double winPts, double tiePts, double losePts, double minRNG, double maxRNG, double leagueDiffPts) {
            WinPts = winPts;
            TiePts = tiePts;
            LosePts = losePts;
            MinRNG = minRNG;
            MaxRNG = maxRNG;
            LeagueDiffPts = leagueDiffPts;
        }
        private ServerInfo_Ranking() {}
        public ServerInfo_Ranking(long serverID) {
            this.ID = serverID;
            Write();
        }

        public static ServerInfo_Ranking get(long serverID) {
            return getById(ServerInfo_Ranking.class, serverID).orElseGet(() -> new ServerInfo_Ranking(serverID));
        }
    }

    public static List<Row> getClanUpdatesChannels() {
        return doQueryAll("""
                SELECT DISTINCT CC.ID AS 'ServerID', CC.ClanUpdatesChannelID AS 'ChannelID', SS.areClanUpdatesGlobal AS 'isGlobal'
                FROM inazuma_competitive.serverinfo_channels CC
                JOIN inazuma_competitive.serverinfo SS ON SS.ID = CC.ID
                WHERE CC.ClanUpdatesChannelID IS NOT NULL;
                """);
    }
    public static List<Row> getTournamentUpdatesChannels() {
        return doQueryAll("""
                SELECT DISTINCT CC.ID AS 'ServerID', CC.TournamentUpdatesChannelID AS 'ChannelID', SS.isPublic AS 'isPublic'
                FROM inazuma_competitive.serverinfo_channels CC
                JOIN inazuma_competitive.serverinfo SS ON SS.ID = CC.ID
                WHERE CC.TournamentUpdatesChannelID IS NOT NULL;
                """);
    }
    public static List<Row> getClanlistChannels() {
        return doQueryAll("""
                SELECT DISTINCT CC.ID AS 'ServerID', CC.ClanlistChannelID AS 'ChannelID', CC.ClanlistMessageID AS 'MessageID', CC.ClanlistMessageID2 AS 'MessageID2'
                FROM inazuma_competitive.serverinfo_channels CC
                WHERE CC.ClanlistChannelID IS NOT NULL;
                """);
    }
    public static List<Row> getLeaderboardChannels() {
        return doQueryAll("""
                SELECT DISTINCT CC.ID AS 'ServerID', CC.LeaderboardChannelID AS 'ChannelID', CC.LeaderboardMessageID AS 'MessageID', CC.LeaderboardMessageID2 AS 'MessageID2', CC.LeaderboardMessageID3 AS 'MessageID3'
                FROM inazuma_competitive.serverinfo_channels CC
                WHERE CC.LeaderboardChannelID IS NOT NULL;
                """);
    }
    public static List<Row> getBlacklistChannels() {
        return doQueryAll("""
                SELECT DISTINCT CC.ID AS 'ServerID', CC.BlacklistChannelID AS 'ChannelID', CC.BlacklistMessageID AS 'MessageID'
                FROM inazuma_competitive.serverinfo_channels CC
                WHERE CC.BlacklistChannelID IS NOT NULL;
                """);
    }
    public static List<Row> getWinnerRolesServ() {
        return doQueryAll("""
                SELECT DISTINCT CC.ID AS 'ServerID', S.areWinnerRolesAllowed, CC.WinnerRoleID1 AS 'WinnerRoleID1', CC.WinnerRoleID2 AS 'WinnerRoleID2', CC.WinnerRoleID3 AS 'WinnerRoleID3'
                FROM inazuma_competitive.serverinfo_roles CC
                JOIN inazuma_competitive.serverinfo S ON S.ID = CC.ID
                WHERE S.areWinnerRolesAllowed;
                """);
    }
    public static List<ServerInfo> getClanRolesAndTagServers() {
        return getAllWhere(ServerInfo.class, "areClanRolesAllowed OR areClanTagsAllowed");
    }
    public static List<ServerInfo> getRankRolesServers() {
        return getAllWhere(ServerInfo.class, "areGlobalRankAllowed OR areGalaxyRanksAllowed OR areCSRanksAllowed OR areGO1RanksAllowed OR areStrikersRanksAllowed OR areXtremeRanksAllowed OR areVRBetaRanksAllowed OR areVROHRanksAllowed OR areIE3RanksAllowed OR areIE2RanksAllowed OR areIE1RanksAllowed OR areSDRanksAllowed");
    }
    public static List<ServerInfo> getServersNeedingCommands() {
        return doQueryAll(ServerInfo.class, """
                SELECT DISTINCT S.*
                FROM inazuma_competitive.serverinfo S
                LEFT JOIN inazuma_competitive.clan C ON C.ClanServerID = S.ID
                LEFT JOIN inazuma_competitive.challonge_tournament CT ON CT.ServerID = S.ID AND NOT CT.State = 'complete'
                LEFT JOIN inazuma_competitive.serverinfo_ranking SR ON SR.ID = S.ID AND SR.hasPrivateLeagues
                WHERE C.ID IS NOT NULL OR CT.ID IS NOT NULL OR SR.ID IS NOT NULL;
                """);
    }

    public static void CleanServers() {
        for (ServerInfo S : getAll(ServerInfo.class)) {
            if (S.getGuild() != null && S.getGuild().getMemberCount() >= 50) {
                if (S.Channels().getLogChannel() == null) S.Channels().setLogChannel(null);
                if (S.Channels().getClanUpdatesChannel() == null) S.Channels().setClanUpdatesChannel(null);
                if (S.Channels().getNotificationChannel() == null) S.Channels().setNotificationChannel(null);
                if (S.Channels().getTournamentUpdatesChannel() == null) S.Channels().setTournamentUpdatesChannel(null);
                if (S.Channels().getClanlist() == null) S.Channels().setClanlistCM(null);
                if (S.Channels().getLeaderboard() == null) S.Channels().setLeaderboardCM(null);
                if (S.Channels().getBlacklist() == null) S.Channels().setBlacklistCM(null);
                if (S.Channels().getClanlist() == null) S.Channels().setClanlistCM(null);
                S.Channels().Update();

                if (S.Roles().getClanCaptainRole() == null) S.Roles().ClanCaptainRoleID = null;
                if (S.Roles().getWinnerRole1() == null) S.Roles().WinnerRole1 = null;
                if (S.Roles().getWinnerRole2() == null) S.Roles().WinnerRole2 = null;
                if (S.Roles().getWinnerRole3() == null) S.Roles().WinnerRole3 = null;
                S.Roles().Update();
            }
        }
    }


    public boolean isSDBoardAllowed() {
        return isSDBoardAllowed;
    }

    public void setSDBoardAllowed(boolean SDBoardAllowed) {
        isSDBoardAllowed = SDBoardAllowed;
    }

    public boolean isIE1BoardAllowed() {
        return isIE1BoardAllowed;
    }

    public void setIE1BoardAllowed(boolean IE1BoardAllowed) {
        isIE1BoardAllowed = IE1BoardAllowed;
    }

    public boolean isIE2BoardAllowed() {
        return isIE2BoardAllowed;
    }

    public void setIE2BoardAllowed(boolean IE2BoardAllowed) {
        isIE2BoardAllowed = IE2BoardAllowed;
    }

    public boolean isIE3BoardAllowed() {
        return isIE3BoardAllowed;
    }

    public void setIE3BoardAllowed(boolean IE3BoardAllowed) {
        isIE3BoardAllowed = IE3BoardAllowed;
    }

    public boolean isVROHBoardAllowed() {
        return isVROHBoardAllowed;
    }

    public void setVROHBoardAllowed(boolean VROHBoardAllowed) {
        isVROHBoardAllowed = VROHBoardAllowed;
    }

    public boolean isVRBetaBoardAllowed() {
        return isVRBetaBoardAllowed;
    }

    public void setVRBetaBoardAllowed(boolean VRBetaBoardAllowed) {
        isVRBetaBoardAllowed = VRBetaBoardAllowed;
    }

    public boolean isXtremeBoardAllowed() {
        return isXtremeBoardAllowed;
    }

    public void setXtremeBoardAllowed(boolean xtremeBoardAllowed) {
        isXtremeBoardAllowed = xtremeBoardAllowed;
    }

    public boolean isStrikersBoardAllowed() {
        return isStrikersBoardAllowed;
    }

    public void setStrikersBoardAllowed(boolean strikersBoardAllowed) {
        isStrikersBoardAllowed = strikersBoardAllowed;
    }

    public boolean isGO1BoardAllowed() {
        return isGO1BoardAllowed;
    }

    public void setGO1BoardAllowed(boolean GO1BoardAllowed) {
        isGO1BoardAllowed = GO1BoardAllowed;
    }

    public boolean isCSBoardAllowed() {
        return isCSBoardAllowed;
    }

    public void setCSBoardAllowed(boolean CSBoardAllowed) {
        isCSBoardAllowed = CSBoardAllowed;
    }

    public boolean isGalaxyBoardAllowed() {
        return isGalaxyBoardAllowed;
    }

    public void setGalaxyBoardAllowed(boolean galaxyBoardAllowed) {
        isGalaxyBoardAllowed = galaxyBoardAllowed;
    }

    public boolean isShowBoardMembersOnly() {
        return showBoardMembersOnly;
    }

    public boolean isAreIE1RanksAllowed() {
        return areIE1RanksAllowed;
    }

    public void setAreIE1RanksAllowed(boolean areIE1RanksAllowed) {
        this.areIE1RanksAllowed = areIE1RanksAllowed;
    }

    public boolean isAreIE2RanksAllowed() {
        return areIE2RanksAllowed;
    }

    public void setAreIE2RanksAllowed(boolean areIE2RanksAllowed) {
        this.areIE2RanksAllowed = areIE2RanksAllowed;
    }

    public boolean isAreIE3RanksAllowed() {
        return areIE3RanksAllowed;
    }

    public void setAreIE3RanksAllowed(boolean areIE3RanksAllowed) {
        this.areIE3RanksAllowed = areIE3RanksAllowed;
    }

    public boolean isAreSDRanksAllowed() {
        return areSDRanksAllowed;
    }

    public void setAreSDRanksAllowed(boolean areSDRanksAllowed) {
        this.areSDRanksAllowed = areSDRanksAllowed;
    }

    public boolean isAreVROHRanksAllowed() {
        return areVROHRanksAllowed;
    }

    public void setAreVROHRanksAllowed(boolean areVROHRanksAllowed) {
        this.areVROHRanksAllowed = areVROHRanksAllowed;
    }

    public boolean isAreVRBetaRanksAllowed() {
        return areVRBetaRanksAllowed;
    }

    public void setAreVRBetaRanksAllowed(boolean areVRBetaRanksAllowed) {
        this.areVRBetaRanksAllowed = areVRBetaRanksAllowed;
    }

    public boolean isAreXtremeRanksAllowed() {
        return areXtremeRanksAllowed;
    }

    public void setAreXtremeRanksAllowed(boolean areXtremeRanksAllowed) {
        this.areXtremeRanksAllowed = areXtremeRanksAllowed;
    }

    public boolean isAreStrikersRanksAllowed() {
        return areStrikersRanksAllowed;
    }

    public void setAreStrikersRanksAllowed(boolean areStrikersRanksAllowed) {
        this.areStrikersRanksAllowed = areStrikersRanksAllowed;
    }

    public boolean isAreGO1RanksAllowed() {
        return areGO1RanksAllowed;
    }

    public void setAreGO1RanksAllowed(boolean areGO1RanksAllowed) {
        this.areGO1RanksAllowed = areGO1RanksAllowed;
    }

    public boolean isAreCSRanksAllowed() {
        return areCSRanksAllowed;
    }

    public void setAreCSRanksAllowed(boolean areCSRanksAllowed) {
        this.areCSRanksAllowed = areCSRanksAllowed;
    }

    public boolean isAreGalaxyRanksAllowed() {
        return areGalaxyRanksAllowed;
    }

    public void setAreGalaxyRanksAllowed(boolean areGalaxyRanksAllowed) {
        this.areGalaxyRanksAllowed = areGalaxyRanksAllowed;
    }

    public boolean isAreGlobalRankAllowed() {
        return areGlobalRankAllowed;
    }

}
