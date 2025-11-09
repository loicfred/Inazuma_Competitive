package iecompbot.objects.clan;

import at.stefangeyer.challonge.model.enumeration.TournamentState;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.img.builders.CardImageBuilder;
import iecompbot.interaction.cmdbreakdown.InterclanCommand;
import iecompbot.interaction.cmdbreakdown.PageViewerCommand;
import iecompbot.interaction.cmdbreakdown.clan.ClanInviteCommand;
import iecompbot.interaction.cmdbreakdown.clan.ClanManager;
import iecompbot.interaction.cmdbreakdown.clan.ClanMemberInteractCommand;
import iecompbot.interaction.cmdbreakdown.clan.ClanRoleManager;
import iecompbot.interaction.cmdbreakdown.profile.FilterCommand;
import iecompbot.interaction.cmdbreakdown.profile.GamesCommand;
import iecompbot.interaction.custom.BuiltMessageE;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.Nationality;
import iecompbot.objects.clan.interclan.Interclan;
import iecompbot.objects.clan.interclan.Interclan_Duel;
import iecompbot.objects.clan.items.ClanPermission;
import iecompbot.objects.match.Game;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Participant;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static iecompbot.Constants.POWERDECIMAL;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.*;
import static iecompbot.Utility.parseEmbedBuilders;
import static iecompbot.img.ImgUtilities.*;
import static iecompbot.interaction.Automation.*;
import static iecompbot.interaction.GuildReady.RefreshAllClanMembers;
import static iecompbot.objects.BotManagers.isClanManager;
import static iecompbot.objects.BotManagers.isPowerDisabled;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.server.ServerInfo.getClanRolesAndTagServers;
import static iecompbot.objects.server.ServerInfo.getClanUpdatesChannels;
import static iecompbot.springboot.config.AppConfig.cacheService;
import static my.utilities.util.Utilities.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Clan extends DatabaseObject<Clan> {
    private transient Nationality Nat = null;
    private transient List<ClanRole> ClanRoles = null;
    private transient List<ClanMember> ClanMembers = null;
    private transient BotEmoji emoji = null;

    public Long ID;

    public String Name = "N/A";
    public String Tag = "N/A";
    public String Colorcode = "#808080";
    public String NationalityName = "International";
    public String Status = "Open";

    public byte[] Emblem = null;

    public Long EmojiID = null;

    public Long ClanServerID = null;
    public String Description = null;
    public String Requirements = null;

    public long DateDeletedEpochSecond = 0;
    public long DerelictTimeEpochSecond = Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond();
    public long DerelictReminderEpochSecond = Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond();
    public long MailSendTimerEpochSecond = Instant.now().getEpochSecond();
    public int MemberCount = 0;
    public String History = "-";

    public String WebsiteURL = null;
    public String TwitterURL = null;
    public String TwitchURL = null;
    public String YouTubeURL = null;
    public String InstagramURL = null;
    public String DiscordURL = null;
    public String TiktokURL = null;

    public long CardBackground = 40001;
    public long CardForeground = 50001;
    public long CardRay = 60001;
    public long CardStrike = 70001;
    public long Sponsor = 871133534184681523L;


    public long getID() {
        return ID;
    }
    public String getName() {
        return Name;
    }
    public String getTag() {
        return Tag;
    }
    public String getColorCode() {
        return Colorcode;
    }
    public Color getColor() {
        return Color.decode(Colorcode);
    }
    public String getNationalityName() {
        return NationalityName;
    }
    public Nationality getNationality() {
        return Nat == null ? Nat = Nationality.get(NationalityName) : Nat;
    }
    public String getHistory() {
        return History;
    }
    public String getStatus() {
        return Status;
    }
    public Long getClanServerID() {
        return ClanServerID;
    }
    public String getDescription() {
        return Description;
    }
    public String getRequirements() {
        return Requirements;
    }
    public Instant getTimeCreated() {
        return Instant.ofEpochMilli(getID());
    }
    public long getDateDeletedEpochSecond() {
        return DateDeletedEpochSecond;
    }
    public long getDerelictTimeEpochSecond() {
        return DerelictTimeEpochSecond;
    }
    public long getDerelictReminderEpochSecond() {
        return DerelictReminderEpochSecond;
    }
    public long getMailSendTimerEpochSecond() {
        return MailSendTimerEpochSecond;
    }
    public int getMemberCount() {
        return MemberCount;
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
    public String getDiscordURL() {
        return DiscordURL;
    }
    public String getTiktokURL() {
        return TiktokURL;
    }
    public byte[] getEmblem() {
        return Emblem;
    }

    public List<ClanRole> getClanRoles() {
        return ClanRoles == null ? ClanRoles = ClanRole.of(this) : ClanRoles;
    }

    public void setName(String oldname, String name, User u) throws Exception {
        if (name.length() > 8) {
            Name = CutString(name, 32);
            for (ServerInfo I : ServerInfo.getClanRolesAndTagServers()) {
                if (hasClanRole(I)) I.Roles().renameRole(getRole(I), name, getEmojiFormatted() + " ");
            }
            if (u != null) AddClanLog(u, "Name", "Renamed the clan to **" + getName() + "**.");
            LogClanUpdatesName(oldname, getName());
        }
    }
    public void setTag(String oldtag, String tag, User u) {
        if (!tag.isEmpty()) {
            Tag = CutString(tag, 8);
            if (u != null) AddClanLog(u, "Tag", "Modified the clan's tag to **" + getTag() + "**.");
            RefreshAllClanMembers(List.of(this));
            LogClanUpdatesTag(oldtag, tag);
        }
    }
    public void setColor(Color color) {
        Colorcode = getHexValue(color);
    }
    public void setNationalityName(String nationality) {
        Nat = Nationality.get(nationality);
        NationalityName = Nat.getName();
    }
    public void setNationality(Nationality nationality) {
        NationalityName = nationality.getName();
        Nat = nationality;
    }
    public void setStatus(String status) {
        Status = status;
    }
    public void setHistory(String history) {
        History = StopString(history, 1024);
    }
    public void setEmblem(byte[] file) {
        Emblem = file;
    }
    public void setClanServerID(Long clanServerID) {
        ClanServerID = clanServerID;
    }
    public void setDescription(String description) {
        Description = CutString(description, 256);
    }
    public void setRequirements(String requirements) {
        Requirements = CutString(requirements, 256);
    }
    public void setDateDeletedEpochSecond(long dateDeletedEpochSecond) {
        DateDeletedEpochSecond = dateDeletedEpochSecond;
    }
    public void setDerelictTimeEpochSecond(long derelictTimeEpochSecond) {
        DerelictTimeEpochSecond = derelictTimeEpochSecond;
    }
    public void setDerelictReminderEpochSecond(long derelictReminderEpochSecond) {
        DerelictReminderEpochSecond = derelictReminderEpochSecond;
    }
    public void setMailSendTimerEpochSecond(long mailSendTimerEpochSecond) {
        MailSendTimerEpochSecond = mailSendTimerEpochSecond;
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
    public void setDiscordURL(String discordURL) {
        DiscordURL = CutString(discordURL, 256);
    }
    public void setTiktokURL(String tiktokURL) {
        TiktokURL = CutString(tiktokURL, 256);
    }
    public int updateMemberCount() {
        MemberCount = Count(ClanMember.class, "ClanID = ?", getID());
        UpdateOnly("MemberCount");
        return MemberCount;
    }



    public long getSponsor() {
        return Sponsor;
    }

    public Item getCardBackgroundItem() {
        return Item.get(CardBackground);
    }
    public Item getCardForegroundItem() {
        return Item.get(CardForeground);
    }
    public Item getCardRayItem() {
        return Item.get(CardRay);
    }
    public Item getCardStrikeItem() {
        return Item.get(CardStrike);
    }

    public void setCardBackground(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_BG)) CardBackground = i.getId();
    }
    public void setCardRay(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_RY)) CardRay = i.getId();
    }
    public void setCardForeground(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_FG)) CardForeground = i.getId();
    }
    public void setCardStrike(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_ST)) CardStrike = i.getId();
    }
    public void setSponsor(long sponsor) {
        Sponsor = sponsor;
    }



    public Clan() {}
    public Clan(User captain, String name, String tag, String description, String colorcode, Nationality nationality, String status, List<User> members, MessageEmbed.Thumbnail emblem) throws Exception {
        ID = Instant.now().toEpochMilli();
        Name = name;
        Tag = tag;
        Colorcode = colorcode;
        Description = description;
        Requirements = "On Request";
        Nat = nationality;
        NationalityName = nationality.getName();
        Status = status;
        Clan createdClan = WriteThenReturn().orElse(null);

        if (emblem != null && emblem.getProxyUrl() != null) {
            emblem.getProxy().downloadToFile(new File(TempDirectory + "/emblem.png"),350,350).whenComplete((file, throwable) -> {
                try {
                    Emblem = Files.readAllBytes(file.toPath());
                    UpdateOnly("Emblem");
                } catch (Exception ignored) {}
            });
            Wait(8000);
        }


        ClanRole Captain = new ClanRole(createdClan.getID(), "Clan Captain");
        Captain.setPermission1(ClanPermission.ADMINISTRATOR);
        Captain.setBuiltin();
        Captain.Update();

        ClanRole CoCap = new ClanRole(createdClan.getID(), "Co-Captain");
        CoCap.setPermission1(ClanPermission.ADMINISTRATOR);
        CoCap.setBuiltin();
        CoCap.Update();


        Profile Capt = Profile.get(captain);
        new ClanMember(createdClan.getID(), Capt.getID(), "00", captain.getEffectiveName(), List.of(Captain), null);
        new File(MainDirectory + "/clans/" + ID + "/").mkdirs();
        LogClanUpdatesClanCreate(captain);

        Wait(1000);
        List<ClanMember> clanMembers = members.stream().map(U -> new ClanMember(createdClan.getID(), Profile.get(U).getID(), "00", U.getEffectiveName(), new ArrayList<>(), null)).collect(Collectors.toList());
        LogClanUpdatesNewMembers(clanMembers);

        sendPrivateMessage(captain, TL(Capt, "clan-register-accept"));
    }


    public List<Interclan> getOnGoingInterclanS() {
        return Interclan.getOngoing(this);
    }
    public List<Interclan> getInterclans() {
        List<Interclan> L = new ArrayList<>();
        for (Interclan I : Interclan.get(this)) {
            if (I.getState().equals(TournamentState.COMPLETE)) {
                L.add(I);
            }
        }
        return L;
    }
    public List<Interclan> getInterclans(boolean win, boolean tied, boolean lost) {
        List<Interclan> L = new ArrayList<>();
        for (Interclan I : getInterclans()) {
            if (win && (I.getWinner() != null && Name.equals(I.getWinner()))) {
                L.add(I);
            }
            if (tied && I.getWinner() == null) {
                L.add(I);
            }
            if (lost && (I.getWinner() != null && !Name.equals(I.getWinner()))) {
                L.add(I);
            }
        }
        return L;
    }

    public boolean areMembersInGuild(Guild g) {
        if (g == null) return false;
        return getClanMembers().stream().anyMatch(cm -> g.getMemberById(cm.getUserID()) != null);
    }

    // Logging in all Channels
    public WebhookMessageBuilder getClanWebhook() {
        return new WebhookMessageBuilder().setUsername(getName()).setAvatarUrl(getEmblemURL());
    }
    public void LogClanUpdatesNewMember(ClanMember M) {
        if (M.isMainClan()) {
            String pfpurl = getFileUrl(M.getUser().getEffectiveAvatarUrl(), "pfp.png");
            double hisPower = M.getPower(null, null);
            int MemberCount = updateMemberCount();
            for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
                try {
                    Wait(100);
                    if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                    Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                    if (G == null) continue;
                    TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                    if (C == null) continue;
                    boolean isGlobal = (boolean) TR.get("isGlobal");
                    if (isGlobal || G.getMemberById(M.getUser().getId()) != null || areMembersInGuild(G)) {
                        if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                            getWebhookOfChannel(C, client -> {
                                String description = "# " + BotEmoji.get("Members") + " " + TLG(G, "clan-news-member") + "\n";
                                if (M.isContractActive()) {
                                    String time = "???";
                                    if (M.getEndOfContract().isAfter(Instant.now().plus(80, ChronoUnit.DAYS))) {
                                        time = 3 + " " + TLG(G, "months");
                                    } else if (M.getEndOfContract().isAfter(Instant.now().plus(50, ChronoUnit.DAYS))) {
                                        time = 2 + " " + TLG(G, "months");
                                    } else if (M.getEndOfContract().isAfter(Instant.now().plus(20, ChronoUnit.DAYS))) {
                                        time = 1 + " " + TLG(G, "month");
                                    } else if (M.getEndOfContract().isAfter(Instant.now().plus(12, ChronoUnit.DAYS))) {
                                        time = 2 + " " + TLG(G, "weeks");
                                    } else if (M.getEndOfContract().isAfter(Instant.now().plus(5, ChronoUnit.DAYS))) {
                                        time = 1 + " " + TLG(G, "week");
                                    }
                                    description = description + "> " + TLG(G, "clan-news-member-description-2", "**" + M.getNationEmoji() + M.getUser().getEffectiveName() + "**", "**" + getEmojiFormatted() + " " + getName() + "**", ":scroll: **" + time + "**") + "\n";
                                } else {
                                    description = description + "> " + TLG(G, "clan-news-member-description", "**" + M.getNationEmoji() + M.getUser().getEffectiveName() + "**", "**" + getEmojiFormatted() + " " + getName() + "**") + "\n";
                                }
                                description = description + "> " + TLG(G, "clan-news-member-members", MemberCount) + "\n";
                                description = description + "`                                                       `\n";

                                if (!isPowerDisabled(G)) {
                                    String powerString = POWERDECIMAL.format(hisPower);
                                    if (hisPower >= 0.5) {
                                        description = description + "> " + TLG(G, "clan-news-member-power-gain", "**" + M.getUser().getEffectiveName() + "**", BotEmoji.get("POW") + " **+" + powerString + "**") + "\n";
                                    }
                                    if (hisPower >= 15.0) {
                                        description = description + "> " + TLG(G, "cnmpg-4");
                                    } else if (hisPower >= 7.5) {
                                        description = description + "> " + TLG(G, "cnmpg-3");
                                    } else if (hisPower >= 5.0) {
                                        description = description + "> " + TLG(G, "cnmpg-2");
                                    } else if (hisPower >= 0.5) {
                                        description = description + "> " + TLG(G, "cnmpg-1");
                                    }
                                }
                                WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                                embed.setDescription(description);
                                embed.setThumbnailUrl(pfpurl);
                                embed.setColor(getColor().getRGB());
                                embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                                embed.setTimestamp(Instant.now());
                                client.send(getClanWebhook().addEmbeds(embed.build()).build());
                            });
                        } else {
                            ServerInfo I = ServerInfo.get(G);
                            I.Channels().setClanUpdatesChannel(null);
                            I.Channels().Update();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }
    public void LogClanUpdatesNewMembers(List<ClanMember> Ms) {
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                    getWebhookOfChannel(C, client -> {
                        int i = 1;
                        for (ClanMember M : Ms) {
                            if (M.isMainClan()) {
                                i++;
                                if (isGlobal || G.getMemberById(M.getUser().getId()) != null || areMembersInGuild(G)) {
                                    double hisPower = M.getPower(null, null);
                                    String description = "# " + BotEmoji.get("Members") + " " + TLG(G, "clan-news-member") + "\n";
                                    if (M.isContractActive()) {
                                        String time = "???";
                                        if (M.getEndOfContract().isAfter(Instant.now().plus(80, ChronoUnit.DAYS))) {
                                            time = 3 + " " + TLG(G, "months");
                                        } else if (M.getEndOfContract().isAfter(Instant.now().plus(50, ChronoUnit.DAYS))) {
                                            time = 2 + " " + TLG(G, "months");
                                        } else if (M.getEndOfContract().isAfter(Instant.now().plus(20, ChronoUnit.DAYS))) {
                                            time = 1 + " " + TLG(G, "month");
                                        } else if (M.getEndOfContract().isAfter(Instant.now().plus(12, ChronoUnit.DAYS))) {
                                            time = 2 + " " + TLG(G, "weeks");
                                        } else if (M.getEndOfContract().isAfter(Instant.now().plus(5, ChronoUnit.DAYS))) {
                                            time = 1 + " " + TLG(G, "week");
                                        }
                                        description = description + "> " + TLG(G, "clan-news-member-description-2", "**" + M.getNationEmoji() + M.getUser().getEffectiveName() + "**", "**" + getEmojiFormatted() + " " + getName() + "**", ":scroll: **" + time + "**") + "\n";
                                    } else {
                                        description = description + "> " + TLG(G, "clan-news-member-description", "**" + M.getNationEmoji() + M.getUser().getEffectiveName() + "**", "**" + getEmojiFormatted() + " " + getName() + "**") + "\n";
                                    }
                                    description = description + "> " + TLG(G, "clan-news-member-members", i) + "\n";
                                    description = description + "`                                                       `\n";

                                    if (!isPowerDisabled(G)) {
                                        String powerString = POWERDECIMAL.format(hisPower);
                                        if (hisPower >= 0.5) {
                                            description = description + "> " + TLG(G, "clan-news-member-power-gain", "**" + M.getUser().getEffectiveName() + "**", BotEmoji.get("POW") + " **+" + powerString + "**") + "\n";
                                        }
                                        if (hisPower >= 15.0) {
                                            description = description + "> " + TLG(G, "cnmpg-4");
                                        } else if (hisPower >= 7.5) {
                                            description = description + "> " + TLG(G, "cnmpg-3");
                                        } else if (hisPower >= 5.0) {
                                            description = description + "> " + TLG(G, "cnmpg-2");
                                        } else if (hisPower >= 0.5) {
                                            description = description + "> " + TLG(G, "cnmpg-1");
                                        }
                                    }
                                    WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                                    embed.setDescription(description);
                                    embed.setThumbnailUrl(M.getUser().getEffectiveAvatarUrl());
                                    embed.setColor(getColor().getRGB());
                                    embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                                    embed.setTimestamp(Instant.now());
                                    client.send(getClanWebhook().addEmbeds(embed.build()).build());
                                }
                            }
                        }
                    });
                } else {
                    ServerInfo I = ServerInfo.get(G);
                    I.Channels().setClanUpdatesChannel(null);
                    I.Channels().Update();
                }
            } catch (Exception ignored) {}
        }
    }
    public void LogClanUpdatesKickMember(ClanMember M) {
        String pfpurl = getFileUrl(M.getUser().getEffectiveAvatarUrl(), "pfp.png");
        double hisPower = M.getPower(null, null);
        int MemberCount = updateMemberCount();
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                Wait(100);
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (isGlobal || G.getMemberById(M.getUser().getId()) != null || areMembersInGuild(G)) {
                    if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(C, client -> {
                            String description = "# :door: " + TLG(G, "clan-news-member-leave") + "\n";
                            description = description + "> " + TLG(G, "clan-news-member-leave-description", "**" + M.getNationEmoji() + M.getUser().getEffectiveName() + "**", "**" + getEmojiFormatted() + " " + getName() + "**") + "\n";
                            description = description + "> " + TLG(G, "clan-news-member-members", MemberCount) + "\n";
                            description = description + "`                                                       `\n";

                            if (!isPowerDisabled(G)) {
                                String powerString = POWERDECIMAL.format(hisPower);
                                if (hisPower >= 0.5) {
                                    description = description + "> " + TLG(G, "clan-news-member-power-lose", "**" + M.getUser().getEffectiveName() + "**", BotEmoji.get("POW") + " **-" + powerString + "**") + "\n";
                                }
                                if (hisPower >= 10.0) {
                                    description = description + "> " + TLG(G, "cnmlpg-4");
                                } else if (hisPower >= 7.5) {
                                    description = description + "> " + TLG(G, "cnmlpg-3");
                                } else if (hisPower >= 5.0) {
                                    description = description + "> " + TLG(G, "cnmlpg-2");
                                } else if (hisPower >= 0.5) {
                                    description = description + "> " + TLG(G, "cnmlpg-1");
                                }
                            }

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setThumbnailUrl(pfpurl);
                            embed.setColor(getColor().getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(TLG(G, "clan-news-member-leave-time", M.getUser().getEffectiveName(), M.getSince(G)), null));
                            client.send(getClanWebhook().addEmbeds(embed.build()).build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }
    public void LogClanUpdatesNewCaptain(User newmember) {
        String pfpurl = getFileUrl(newmember.getEffectiveAvatarUrl(), "pfp.png");
        Profile P = Profile.get(newmember.getIdLong());
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (isGlobal || areMembersInGuild(G)) {
                    if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(C, client -> {
                            String description = "# " + iecompbot.objects.profile.item.Item.get("XP Boost").getEmojiFormatted() + TLG(G, "clan-news-transfer-captain") + "\n";
                            description = description + "> " + TLG(G, "clan-news-transfer-captain-description", "**" + P.getNationality().getFlag().getFormatted() + " " + newmember.getEffectiveName() + "**", "**" + getEmojiFormatted() + " " + getName() + "**") + "\n";
                            description = description + "`                                                       `\n";

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setThumbnailUrl(pfpurl);
                            embed.setColor(getColor().getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                            embed.setTimestamp(Instant.now());
                            client.send(getClanWebhook().addEmbeds(embed.build()).build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }
    public void LogClanUpdatesColorChange(String colorcode) {
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (isGlobal || areMembersInGuild(G)) {
                    if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(C, client -> {
                            String description = "# :art: " + TLG(G, "clan-news-change-color") + "\n";
                            description = description + "> " + TLG(G, "clan-news-change-color-description", "**" + getEmojiFormatted() + " " + getName() + "**", "**" + colorcode + "**") + "\n";
                            description = description + "`                                                       `\n";

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setThumbnailUrl(getEmblemURL());
                            embed.setColor(Color.decode(colorcode).getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                            embed.setTimestamp(Instant.now());
                            client.send(getClanWebhook().addEmbeds(embed.build()).build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }
    public void LogClanUpdatesName(String oldname, String newname) {
        if (!oldname.equals(newname)) for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (isGlobal || areMembersInGuild(G)) {
                    if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(C, client -> {
                            String description = "# :placard: " + TLG(G, "clan-news-name") + "\n";
                            description = description + "> " + TLG(G, "clan-news-name-description", "**" + getEmojiFormatted() + " " + oldname + "**", "**" + getEmojiFormatted() + " " + newname + "**") + "\n";
                            description = description + "`                                                       `\n";

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setThumbnailUrl(getEmblemURL());
                            embed.setColor(getColor().getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                            embed.setTimestamp(Instant.now());
                            client.send(getClanWebhook().addEmbeds(embed.build()).build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }
    public void LogClanUpdatesTag(String oldtag, String newtag) {
        if (!oldtag.equals(newtag)) for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (isGlobal || areMembersInGuild(G)) {
                    if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(C, client -> {
                            String description = "# :label: " + TLG(G, "clan-news-tag") + "\n";
                            description = description + "> " + TLG(G, "clan-news-tag-description", "**" + getEmojiFormatted() + " " + getName() + "**", "**" + oldtag + "**", "**" + newtag + "**") + "\n";
                            description = description + "`                                                       `\n";

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setThumbnailUrl(getEmblemURL());
                            embed.setColor(getColor().getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                            embed.setTimestamp(Instant.now());
                            client.send(getClanWebhook().addEmbeds(embed.build()).build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }
    public void LogClanUpdatesLogo() {
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (isGlobal || areMembersInGuild(G)) {
                    if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(C, client -> {
                            String description = "# " + getEmojiFormatted() + " " + TLG(G, "clan-news-logo") + "\n";
                            description = description + "> " + TLG(G, "clan-news-logo-description", "**" + getEmojiFormatted() + " " + getName() + "**") + "\n";
                            description = description + "`                                                       `\n";

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setThumbnailUrl(getEmblemURL());
                            embed.setColor(getColor().getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                            embed.setTimestamp(Instant.now());
                            client.send(getClanWebhook().addEmbeds(embed.build()).build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }
    public void LogClanUpdatesClanCreate(User captain) {
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (isGlobal || areMembersInGuild(G)) {
                    if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(C, client -> {
                            String description = "# :new: " + TLG(G, "clan-news-create") + "\n";
                            description = description + "> " + TLG(G, "clan-news-create-description",
                                    getNationality().getFlag().getFormatted() + " __" + getName() + "__",
                                    "**" + captain.getEffectiveName().replaceAll("[*_]", "") + "**",
                                    "`" + captain.getName() + "`") + "\n";
                            description = description + "`                                                       `\n";

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setColor(getColor().getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                            embed.setThumbnailUrl(captain.getEffectiveAvatarUrl());
                            embed.setTimestamp(Instant.now());
                            client.send(getClanWebhook().addEmbeds(embed.build()).build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }
    public void LogClanUpdatesClanDisband() {
        setDateDeletedEpochSecond(Instant.now().getEpochSecond());
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                if (Prefs.TestMode && TR.getAsLong("ServerID") != 930718276542136400L) continue;
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                boolean isGlobal = (boolean) TR.get("isGlobal");
                if (isGlobal || areMembersInGuild(G)) {
                    if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(C, client -> {
                            String description = "# :rose: " + TLG(G, "clan-news-disband") + "\n";
                            description = description + "> " + TLG(G, "clan-news-disband-description", "**" + getEmojiFormatted() + " " + getName() + "**") + "\n";
                            description = description + "`                                                       `\n";

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setThumbnailUrl(getEmblemURL());
                            embed.setColor(getColor().getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(getName(), null));
                            embed.setTimestamp(Instant.now());
                            client.send(getClanWebhook().addEmbeds(embed.build()).build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean isDerelictDue() {
        return Instant.now().isAfter(Instant.ofEpochSecond(DerelictTimeEpochSecond));
    }
    public boolean isDerelictReminderDue() {
        return Instant.now().isAfter(Instant.ofEpochSecond(DerelictReminderEpochSecond));
    }
    public void RefreshDerelict() {
        try {
            DatabaseObject.Row ACT = ClanActivitySQL(null, null);
            double MatchAct = ACT.getAsDouble("Average Match Activity");
            double TournAct = ACT.getAsDouble("Average Tournament Activity");
            if (isDerelictReminderDue() && getClanMembers().size() < 5) {
                setDerelictReminderEpochSecond(Instant.now().plus(7, ChronoUnit.DAYS).getEpochSecond());
                sendPrivateMessage(getCaptain().getUser(), TL(Profile.get(getCaptain().getUser()),"clan-derelict-warning", "**" + getEmojiFormatted() + " " + getName() + "**", "<t:" + DerelictTimeEpochSecond + ":R>"));
            }
            if (MatchAct > 0 || TournAct > 0) {
//                if (!getStatus().equals("Open"))
//                    sendPrivateMessage(getCaptain().getUser(), ":chart_with_upwards_trend: " + TL(Profile.get(getCaptain().getUser()),"clan-derelict-open", "**" + getEmojiFormatted() + " " + getName() + "**"));
                setStatus("Open");
                setDerelictTimeEpochSecond(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond());
                setDerelictReminderEpochSecond(Instant.now().plus(7, ChronoUnit.DAYS).getEpochSecond());
            } else if (isDerelictDue()) {
                if (getClanMembers().size() >= 5) {
//                    if (!getStatus().equals("Pause"))
//                        sendPrivateMessage(getCaptain().getUser(), ":chart_with_downwards_trend: " + TL(Profile.get(getCaptain().getUser()),"clan-derelict-pause", "**" + getEmojiFormatted() + " " + getName() + "**", "**Pause**"));
                    setStatus("Pause");
                } else {
                    if (!getStatus().equals("Closed")) {
                        for (ServerInfo I : getClanRolesAndTagServers()) {
                            if (areMembersInGuild(I.getGuild())) {
                                for (ClanMember m : getClanMembers()) {
                                    RemoveTagFromMember(I, I.getGuild().getMemberById(m.UserID));
                                }
                                if (hasClanRole(I)) {
                                    I.Roles().deleteRole(getRole(I), getEmojiFormatted() + " ");
                                }
                            }
                        }
                        for (ClanMember CM : getClanMembers()) {
                            sendPrivateMessage(CM.getUser(), TL(CM.getProfile(), "clan-disband-success-dm", "**" + getEmojiFormatted() + " " + getName() + "**"));
                            CM.getProfile().AddClanLog(CM);
                            CM.Delete();
                        }
                        setStatus("Closed");
                        sendPrivateMessage(getCaptain().getUser(), TL(Profile.get(getCaptain().getUser()), "clan-derelict-done", "**" + getEmojiFormatted() + " " + getName() + "**"));
                        LogClanUpdatesClanDisband();
                    }
                }
            }
            UpdateOnly("DerelictReminderEpochSecond", "DerelictTimeEpochSecond", "Status");
        } catch (Exception ignored) {}
    }

    // Warn
    public transient List<Clan_Warn> ClanWarns = null;
    public List<Clan_Warn> getClanWarns() {
        return ClanWarns == null ?  ClanWarns = Clan_Warn.get(getID()) : ClanWarns;
    }
    public Clan_Warn addWarn(long userID, String name, String description, int hours, double powerloss) {
        return new Clan_Warn(getID(), userID, name, description, hours, powerloss);
    }

    public transient List<Clan_Log> ClanLogs = null;
    public List<Clan_Log> getClanLogs(int page, int amountperpage) {
        return ClanLogs == null ? ClanLogs = Clan_Log.get(getID(), page, amountperpage) : ClanLogs;
    }
    public Clan_Log AddClanLog(User editor, String name, String description) {
        return new Clan_Log(getID(), editor.getIdLong(), name, description);
    }


    // Clan Emoji
    public synchronized void AddEmoji(boolean replace) throws IOException, ExecutionException, InterruptedException {
        if (hasEmblem() && (replace || EmojiID == null)) {
            if (replace && EmojiID != null) {
                try {
                    BotEmoji.get(EmojiID).Delete();
                } catch (Exception ignored) {}
            }
            Files.write(new File(TempDirectory + "/temp.png").toPath(), getEmblem());
            File temp = new File(TempDirectory + "/temp.png");
            File F = temp.length() / 1024 > 512 ? ResizeImage(temp, 0.5) : temp.length() / 1024 > 256 ? ResizeImage(temp, 0.75) : temp;
            ApplicationEmoji emoji = DiscordAccount.createApplicationEmoji(Instant.now().toEpochMilli() + "", Icon.from(F)).submit().orTimeout(3, TimeUnit.SECONDS).get();
            EmojiID = emoji != null ? new BotEmoji(emoji).ID : null;
            Update();
        }
    }
    public synchronized BotEmoji getEmoji() {
        if (emoji == null) {
            try {
                AddEmoji(false);
                if (EmojiID != null) emoji = BotEmoji.get(EmojiID);
                if (emoji == null) emoji = BotEmoji.get("Empty");
            } catch (Exception ignored) {}
        }
        return emoji;
    }
    public synchronized String getEmojiFormatted() {
        return getEmoji() == null ? getNationality().getFlag().getFormatted() : getEmoji().getFormatted();
    }


    // Roles & Tags
    public void AddTagToMember(ServerInfo I, Member M) {
        try {
            Member member = I.getGuild().getMemberById(M.getIdLong());
            if (I.areClanTagsAllowed) {
                if (member != null && !member.getEffectiveName().contains(getTag()) && !member.getUser().getEffectiveName().startsWith(getTag())) {
                    if (I.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
                        if (I.getGuild().getSelfMember().canInteract(member)) {
                            String oldname = member.getEffectiveName();
                            String newname = member.getUser().getEffectiveName().contains(getTag()) ? member.getUser().getEffectiveName() :
                                    getTag() + " | " + member.getUser().getEffectiveName();
                            if (newname.length() <= 32) {
                                member.modifyNickname(newname).submit().orTimeout(3, TimeUnit.SECONDS).whenComplete((m, t) -> {
                                    I.LogSlash(TLG(I,"nickname-modify-success", "**" + oldname + "**", "**" + newname + "**"));
                                });
                            }
                        } else if (!isStaffMember(member) && !isStartup) {
                            //I.LogSlash(TLG(I,"nickname-modify-interact-fail", "**" + member.getEffectiveName() + "**"));
                        }
                    } else {
                        I.setAreClanTagsAllowed(false);
                        I.Update();
                        //if (!isStartup) I.LogSlash(TLG(I,"nickname-modify-permission-fail", "**" + member.getEffectiveName() + "**"));
                    }
                }
            }
        } catch (Exception ignored) {}
    }
    public void RemoveTagFromMember(ServerInfo I, Member member) {
        try {
            if (member != null && member.getEffectiveName().startsWith(getTag()) && !member.getUser().getEffectiveName().startsWith(getTag())) {
                if (I.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
                    if (I.getGuild().getSelfMember().canInteract(member)) {
                        String oldname = member.getEffectiveName();
                        member.modifyNickname(member.getUser().getEffectiveName()).submit().whenComplete((m, t) -> {
                            I.LogSlash(TLG(I, "nickname-modify-success", "**" + oldname + "**", "**" + member.getUser().getEffectiveName() + "**"));
                        });
                    } else if (!isStaffMember(member)) {
                        //I.LogSlash(TLG(I, "nickname-modify-interact-fail", "**" + member.getEffectiveName() + "**", getTag()));
                    }
                } else {
                    I.setAreClanTagsAllowed(false);
                    I.Update();
                    //I.LogSlash(TLG(I, "nickname-modify-permission-fail", "**" + member.getEffectiveName() + "**", getTag()));
                }
            }
        } catch (Exception ignored) {}
    }
    public Role getRole(ServerInfo I){
        if (I.areClanRolesAllowed && I.getGuild() != null && areMembersInGuild(I.getGuild()) && isEligibleForRole()) {
            return I.Roles().getClanRole(this, true);
        } return null;
    }
    public boolean hasClanRole(ServerInfo I) {
        if (I.areClanRolesAllowed && I.getGuild() != null) {
            return I.Roles().getClanRole(this, false) != null;
        } return false;
    }
    public static boolean isStaffMember(Member member) {
        for (Role role : member.getRoles()) {
            if (role.getName().toLowerCase().contains("admin")
                    || role.getName().toLowerCase().contains("moderat")
                    || role.getName().toLowerCase().contains("modérat")
                    || role.getName().toLowerCase().contains("owner")
                    || role.getName().toLowerCase().contains("organi")) {
                return true;
            }
        }
        return false;
    }

    public boolean isEligibleForRole() {
        return MemberCount >= 5;
    }


    public int getReinforcementCount() {
        return Count(ClanMember.class, "ClanID = ? AND NOT isMainClan", getID());
    }
    public List<ClanMember> getClanMembers() {
        return ClanMembers == null ? ClanMembers = ClanMember.ofClan(getID()) : ClanMembers;
    }
    public ClanMember getMemberById(User user) {
        ClanMember M = getMemberById(user.getIdLong());
        M.user = user;
        return M;
    }
    public ClanMember getMemberById(long id) {
        ClanMember M = ClanMember.ofClan(getID(), id);
        return M != null ? M : new ClanMember(getID(), id);
    }

    public ClanMember getCaptain() {
        return cacheService.getCachedCaptain(getID());
    }
    public ClanRole getCaptainRole() {
        return getWhere(ClanRole.class,"ClanID = ? AND Name = ? AND isBuiltin = ?", getID(), "Clan Captain", true).orElse(null);
    }

    public List<Clan_Trophy> getTrophies() {
        return Clan_Trophy.get(getID());
    }
    public List<DatabaseObject.Row> getAllTrophies(int page, int amountperpage) {
        return doQueryAll("CALL DisplayClanTrophies(?,?,?);", getID(), page, amountperpage);
    }

    public boolean hasEmblem() {
        return getEmblem() != null;
    }

    public String getEmblemURL() {
        return DefaultURL + "/api/img/clan/" + getID() + "/emblem.png";
    }
    public static MessageCreateData InviteMember(User receiver, User inviter, ClanInviteCommand CMD) {
        try {
            Profile P = Profile.get(receiver.getIdLong());
            Button Yes = Button.success(CMD.Command("clan-inv-confirm"), TL(P,"Join"));
            Button No = Button.secondary(CMD.Command("clan-inv-deny"), TL(P,"refuse"));
            EmbedBuilder E = new EmbedBuilder();
            E.setThumbnail(CMD.getTargetClan().getEmblemURL());
            E.setTitle(TL(P, CMD.isReinforcement ? "clan-invitation-2" : "clan-invitation"));
            E.setColor(CMD.getTargetClan().getColor());
            E.setDescription(TL(P, CMD.isReinforcement ? "clan-invite-2" : "clan-invite", inviter.getAsMention(), "**" + CMD.getTargetClan().getEmojiFormatted() + " " + CMD.getTargetClan().getName() + "**"));
            E.setFooter("• " + inviter.getEffectiveName());
            if (CMD.Contract > 0) {
                E.addField(":scroll: " + TL(P,"Contract"), "> " + TL(P,"clan-invite-contract-time", "<t:" + CMD.Contract + ":d>", "<t:" + CMD.Contract + ":R>") , false);
            }
            E.addField("Inviter", inviter.getAsMention(), false);
            return new MessageCreateBuilder()
                    .setContent("Hey " + receiver.getAsMention() + " !")
                    .addComponents(ActionRow.of(Yes, No))
                    .setEmbeds(E.build())
                    .build();
        } catch (Exception ignored) {
            return new MessageCreateBuilder().setContent("Error").build();
        }
    }
    public boolean isClanServerTaken(Guild g) {
        return Count(Clan.class,"ClanServerID = ?", g.getId()) > 0;
    }

    public static List<Clan> getClansOfUser(Long userid) {
        if (userid == null) return null;
        return doQueryAll(Clan.class,"SELECT c.* FROM inazuma_competitive.clan c JOIN inazuma_competitive.clanmember m ON c.ID = m.ClanID WHERE m.UserID = ?", userid);
    }
    public static List<Clan> getClansOfUser(User u) {
        return doQueryAll(Clan.class,"SELECT c.* FROM inazuma_competitive.clan c JOIN inazuma_competitive.clanmember m ON c.ID = m.ClanID WHERE m.UserID = ?", u.getId());
    }
    public static Clan getClanOfUser(Long userid) {
        if (userid == null) return null;
        ClanMember CM = getWhere(ClanMember.class,"UserID = ? AND isMainClan = ?", userid, true).orElse(null);
        return CM != null ? CM.getClan() : null;
    }
    public static Clan getReinforcementOfUser(Long userid) {
        if (userid == null) return null;
        ClanMember CM = getWhere(ClanMember.class,"UserID = ? AND isMainClan = ?", userid, false).orElse(null);
        return CM != null ? CM.getClan() : null;
    }
    public static Clan getClanOfUser(User u) {
        ClanMember CM = getWhere(ClanMember.class,"UserID = ? AND isMainClan = ?", u.getIdLong(), true).orElse(null);
        return CM != null ? CM.getClan() : null;
    }
    public static Clan pickRandomClan() {
        return getRandom(Clan.class);
    }
    public static Clan getClanOfGuild(String guildid) {
        return getWhere(Clan.class,"ClanServerID = ?", guildid).orElse(null);
    }

    public void ViewClan(InteractionHook M) {
        ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
        EmbedBuilder clanprofile = new EmbedBuilder();
        clanprofile.setThumbnail(getEmblemURL());
        clanprofile.setColor(getColor());
        clanprofile.setDescription(getDescription());

        clanprofile.setTitle((":white_check_mark: ") + getTag() + " | " + getName(), DefaultURL + "/c/" + getID());


        String flag = getNationality().getFlag().getFormatted();
        clanprofile.addField(flag + " " + TL(M,"Nationality") + ":", "- " + getNationality().getName(), true);
        clanprofile.addField(":calendar_spiral: " + TL(M,"Date_Created") + ":", "- <t:" + getTimeCreated().getEpochSecond() + ":d>", true);
        if (!isPowerDisabled(I)) {
            clanprofile.addField(BotEmoji.get("POW") + " " + TL(M,"Clan_Power") + ":", "- " +  getPowerAsString(), true);
        }

        if (I != null && I.getGuild() != null && !I.getGuild().isDetached() && getRole(I) != null) {
            clanprofile.addField(getEmojiFormatted() + " " + TL(M,"Clan-Server-Role") + ": ", "- " + getRole(I).getAsMention(), true);
        }
        clanprofile.addField(":people_hugging: " + TL(M,"Members") + ": ", "- (" + getMemberCount() + "/50)", true);
        clanprofile.addField(":clipboard: " + TL(M,"Join_Requirements") + ":", "- " + getRequirements(), false);


        String memberstring = "";
        String memberstring2 = "";
        String reinforcementstring = "";
        List<ClanMember> CMs = getClanMembers();
        CMs.sort(Comparator.comparingInt(ClanMember::getHighestRolePosition));
        for (ClanMember CM : CMs) {
            String roles = CM.listTasksOneLine();
            if (CM.isMainClan()) {
                if (memberstring.length() > 850) {
                    memberstring2 = memberstring2 + CM.getNationEmoji() + (CM.isCaptain() ? ":crown: " : "") + CM.Number + " • " + CM.getUser().getEffectiveName() + (getClanMembers().size() <= 30 ? "  ~  *(@" + CM.getUser().getName() + ")*" : "") + "\n";
                    if (roles.length() > 1) memberstring2 = memberstring2 + "> -# └ " + roles + "\n";
                } else {
                    memberstring = memberstring + CM.getNationEmoji() + (CM.isCaptain() ? ":crown: " : "") + CM.Number + " • " + CM.getUser().getEffectiveName() + (getClanMembers().size() <= 30 ? "  ~  *(@" + CM.getUser().getName() + ")*" : "") + "\n";
                    if (roles.length() > 1) memberstring = memberstring + "> -# └ " + roles + "\n";
                }
            } else {
                reinforcementstring = reinforcementstring + CM.getNationEmoji() + (CM.isCaptain() ? ":crown: " : "") + CM.Number + " • " + CM.getUser().getEffectiveName() + (getClanMembers().size() <= 30 ? "  ~  *(@" + CM.getUser().getName() + ")*" : "") + "\n";
                if (roles.length() > 1) reinforcementstring = reinforcementstring + "> -# └ " + roles + "\n";
                if (reinforcementstring.length() > 800 && clanprofile.length() + reinforcementstring.length() < 6000) {
                    clanprofile.addField(" ", reinforcementstring, false);
                    reinforcementstring = "";
                }
            }
        }
        if (!memberstring.isEmpty()) clanprofile.addField(":small_blue_diamond: " + BotEmoji.get("Members").getFormatted() + " **" + TL(M,"Members") + "**",  memberstring, false);
        if (!memberstring2.isEmpty()) clanprofile.addField(" ", memberstring2, false);
        if (!reinforcementstring.isEmpty()) clanprofile.addField(":small_orange_diamond: " + BotEmoji.get("Members").getFormatted() + " **" + TL(M,"Reinforcements") + "**",  reinforcementstring, false);


        M.editOriginalEmbeds(clanprofile.build()).queue();
        List<ActionRow> ARs = new ArrayList<>();

        ARs.add(ActionRow.of(StringSelectMenu.create("pf-view")
                .setPlaceholder(getClanMembers().getFirst().getUser().getEffectiveName() + " (" + getClanMembers().getFirst().getUser().getName() + ")").setRequiredRange(1,1)
                .addOptions(getClanMembers().stream().limit(25).map(CM -> SelectOption.of(CM.getUser().getEffectiveName() + " (" + CM.getUser().getName() + ")", String.valueOf(CM.getUserID())).withDescription(TL(M, "Member_Since") + " " + CM.getTimeJoined("dd/MM/yyyy") + ".")).toList())
                .build()));
        if (getClanMembers().size() > 25) ARs.add(ActionRow.of(StringSelectMenu.create("pf-view-2")
                .setPlaceholder(getClanMembers().get(25).getUser().getEffectiveName() + " (" + getClanMembers().get(25).getUser().getName() + ")").setRequiredRange(1,1)
                .addOptions(getClanMembers().stream().skip(25).limit(25).map(CM -> SelectOption.of(CM.getUser().getEffectiveName() + " (" + CM.getUser().getName() + ")", String.valueOf(CM.getUserID())).withDescription(TL(M, "Member_Since") + " " + CM.getTimeJoined("dd/MM/yyyy") + ".")).toList())
                .build()));

        List<Button> BTN = new ArrayList<>();
        List<Button> BTN2 = new ArrayList<>();
        if (getWebsiteURL() != null) BTN.add(Button.link(getWebsiteURL(), TL(M,"Website")));
        if (getTwitchURL() != null) BTN.add(Button.link(getTwitchURL(), "Twitch"));
        if (getTwitterURL() != null) BTN.add(Button.link(getTwitterURL(), "X"));
        if (getTiktokURL() != null) BTN.add(Button.link(getTiktokURL(), "Tiktok"));
        if (getYouTubeURL() != null) BTN.add(Button.link(getYouTubeURL(), "YouTube"));
        if (getDiscordURL() != null) if (BTN.size() < 5) BTN.add(Button.link(getDiscordURL(), "Discord"));
        else BTN2.add(Button.link(getDiscordURL(), "Discord"));
        if (getInstagramURL() != null) if (BTN.size() < 5) BTN.add(Button.link(getInstagramURL(), "Instagram"));
        else BTN2.add(Button.link(getInstagramURL(), "Instagram"));
        if (!BTN.isEmpty()) ARs.add(ActionRow.of(BTN));
        if (!BTN2.isEmpty()) ARs.add(ActionRow.of(BTN2));

        M.editOriginalEmbeds(clanprofile.build()).setComponents(ARs).queue();
    }
    public void ViewPowerDetails(InteractionHook M, FilterCommand FILCMD, GamesCommand GMCMD) {
        if (!isPowerDisabled(M.getInteraction().getGuild())) {
            EmbedBuilder E = new EmbedBuilder();
            E.setThumbnail(getEmblemURL());
            E.setColor(getColor());
            E.setTitle(TL(M, "power-details-of", getName()));
            E.setDescription(TL(M,"power-details-description"));
            E.setFooter(TL(M,"Filter") + ": " + FILCMD.Filter);

            DatabaseObject.Row TR = ClanPowerSQL(FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")));
            double INPUT = TR.getAsDouble("POW: Member");
            E.addField(TL(M,"Members") + " (**x" + TR.get("Amount: Member") + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("POW: Months");
            E.addField(TL(M,"Months") + " (**x" + TR.get("Amount: Months") + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("POW: League");
            E.addField(TL(M,"League") + " (**x" + TR.get("Amount: League") + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("POW: P.Trophies") + TR.getAsDouble("POW: C.Trophies");
            E.addField(TL(M,"Trophies") + " (**x" + (TR.getAsDouble("Amount: P.Trophies") + TR.getAsDouble("Amount: C.Trophies")) + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("POW: P.Event") + TR.getAsDouble("POW: C.Event");
            E.addField(TL(M,"Event") + " (**x" + (TR.getAsDouble("Amount: P.Event") + TR.getAsDouble("Amount: C.Event")) + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = 0;
            E.addField("Interclans" + " (**x" + 0 + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("POW: Tournament");
            E.addField(TL(M,"Tournaments") + " (**x" + TR.get("Amount: Tournament") + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("Total Power");
            E.addField(TL(M,"Total"),
                    BotEmoji.get("POW") + " **" + POWERDECIMAL.format(INPUT) + "**",
                    false);
            {
                TR = ClanActivitySQL(FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")));
                E.addField(":chart_with_upwards_trend: " + TL(M,"Member-Activity"),
                        "> **" + TL(M, "Last_Month") + ": " + TR.get("Match Activity") + "**\n" +
                                "> **" + TL(M, "Last_Months_Average", "3") + ": " + POWERDECIMAL.format(TR.getAsDouble("Average Match Activity")) + "**\n",
                        false);
                E.addField(":chart_with_upwards_trend: " + TL(M,"Tournament-Participation-Rate"),
                        "> **" + TL(M, "Last_Month") + ": " + TR.get("Tournament Activity") + "**\n" +
                                "> **" + TL(M, "Last_Months_Average", "3") + ": " + POWERDECIMAL.format(TR.getAsDouble("Average Tournament Activity")) + "**\n",
                        false);

            }
            DatabaseObject.Row MostPowerful = doQuery("CALL DisplayMostPowerful(?,?,?);", getID(), FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(","))).orElse(null);
            DatabaseObject.Row MostActive = doQuery("CALL DisplayMostActive(?,?,?,?,?);", getID(), FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")), 30, 3).orElse(null);

            E.addField(TL(M, "Players"),
                    "> **" + TL(M,"most-powerful") + ": " + (MostPowerful.getAsDouble("Power") < 0.2 ? TL(M, "None") : getUserByID(MostPowerful.getAsLong("UserID")).getAsMention() + " (" + BotEmoji.get("POW") + " " + POWERDECIMAL.format(MostPowerful.getAsDouble("Power")) + ")") + "**\n" +
                            "> **" + TL(M,"most-active") + ": " + (MostActive.getAsDouble("Activity") == 0 ? TL(M, "None") : getUserByID(MostActive.getAsLong("UserID")).getAsMention() + " (:chart_with_upwards_trend: " + POWERDECIMAL.format(MostActive.getAsDouble("Activity")) + ")") + "**", false);

            BuiltMessageE MSG = new BuiltMessageE(M);
            MSG.addEmbeds(E.build());

            if (M.getInteraction().getGuild() != null && !M.getInteraction().getGuild().isDetached()) {
                MSG.EnableFilter(FILCMD, 0, 3,
                        SelectOption.of(M.getInteraction().getGuild().getName(), "S" + M.getInteraction().getGuild().getId()).withDescription(TL(M,"view-only-this-server")).withDefault(FILCMD.getServerID() != null)
                );
            }

            MSG.EnableGames(GMCMD, 0, 3);

            M.editOriginal(MSG.build()).queue();
        } else {
            M.editOriginal(TL(M, "Power_Disabled_Server")).queue();
        }
    }
    public void ViewTournaments(InteractionHook M, PageViewerCommand PGCMD, FilterCommand FILCMD, GamesCommand GMCMD) throws Exception {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M,"tournament-list-of", getName()));
        E.setDescription(TL(M,"tournament-list-description"));
        E.setThumbnail(getEmblemURL());
        E.setColor(getColor());
        E.setFooter(TL(M,"Filter") + ": " + FILCMD.Filter);
        Game G;
        Clan C;
        String medal;
        String name;
        String power;
        String players;
        String playersLine;
        SChallonge_Tournament CT;

        int AmountPerPages = 10;
        List<DatabaseObject.Row> TRs = DatabaseObject.doQueryAll("CALL DisplayClanTournaments(?,?,?,?,?,?);", getID(), FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")), PGCMD.Page, AmountPerPages, true); // userid, serverid, gamelist, page, amountPerPage, allow private
        for (DatabaseObject.Row TR : TRs) {
            G = Game.get(TR.getAsString("GameCode"));
            name = TR.getAsString("Name");
            players = "";
            playersLine = "> ";
            power = "";
            if (!isPowerDisabled(M.getInteraction().getGuild()) && TR.getAsBoolean("isPublic")) {
                power = " | " + TL(M,"Power") + ": **" + BotEmoji.get("POW") + " +" + POWERDECIMAL.format(TR.getAsDouble("Power")) + "**";
            }

            CT = SChallonge_Tournament.get(TR.getAsLong("ID"));
            for (SChallonge_Participant CP : SChallonge_Tournament.get(TR.getAsLong("ID")).getParticipants()) {
                if (players.length() < 300) {
                    if (CP.getLeaderID() != null) {
                        if (CP.getPosition() == 1) medal = "(:first_place:) ";
                        else if (CP.getPosition() == 2) medal = "(:second_place:) ";
                        else if (CP.getPosition() == 3) medal = "(:third_place:) ";
                        else if (CP.getPosition() == 0) medal = "(Out) ";
                        else medal = "(" + CP.getPosition() + "th) ";
                        C = CP.getLeaderPf().getClanAtTime(CT.getCompletedAtTime());
                        if (C != null && C.getID() == getID()) {
                            playersLine = playersLine + (playersLine.length() < 5 ? "└" : "─");
                            C = getClanOfUser(CP.getLeaderID());
                            if (C != null && C.getID() == getID()) {
                                playersLine = playersLine + medal + CP.getLeader().getEffectiveName();
                            } else {
                                playersLine = playersLine + "~~" + medal + CP.getLeader().getEffectiveName() + "~~";
                            }
                        }
                        if (playersLine.length() > 48) {
                            players = players + playersLine + "\n";
                            playersLine = "> ";
                        }
                    }
                } else break;
            }
            E.addField((!TR.getAsBoolean("isPublic") ? "[:lock:] " : "") + "(" + G.getEmoji() + ") " + name.replaceAll("(?i)VICTORY ROAD", "VR").replaceAll("(?i)INAZUMA ELEVEN GO", "IEGO").replaceAll("(?i)INAZUMA ELEVEN", "IE"),
                    "> " + TL(M,"Date") + ": <t:" + TR.getAsLong("CompletedAtTimeEpochSecond") + ":d>" + power.replaceFirst(" \\|", "") + "\n" +
                    "> " + TL(M,"Players") + ": **" + (TR.getAsInt("ParticipantCount") * TR.getAsInt("VSAmount")) + "** | [" + TL(M,"View_Bracket") + "](" + TR.getAsString("FullChallongeURL") + ")\n" +
                    players, false);
        }

        BuiltMessageE MSG = new BuiltMessageE(M);
        MSG.addEmbeds(E.build());

        if (M.getInteraction().getGuild() != null && !M.getInteraction().getGuild().isDetached()) {
            MSG.EnableFilter(FILCMD, 0, 3,
                    SelectOption.of(M.getInteraction().getGuild().getName(), "S" + M.getInteraction().getGuild().getId()).withDescription(TL(M,"view-only-this-server")).withDefault(FILCMD.getServerID() != null)
            );
        }

        MSG.EnableGames(GMCMD, 0, 3);
        MSG.EnablePagination(PGCMD, 25, doQueryValue(Integer.class, "SELECT GetClanTournamentsCount(?,?,?,?) AS 'Count'", getID(), FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")), true).orElse(0));

        M.editOriginal(MSG.build()).queue();
    }
    public void ViewClanLogs(InteractionHook M, PageViewerCommand CMD) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getEmblemURL());
        E.setColor(getColor());
        E.setAuthor(TL(M,"clan-log-title", getName()), null, getEmblemURL());
        E.setDescription(TL(M, "clan-log-description"));

        int AmountPerPages = 25;
        for (Clan_Log log : getClanLogs(CMD.Page, AmountPerPages)) {
            if (E.getFields().size() < 25) {
                E.addField(log.getName() + ": <t:" + log.getEpochSecond() + ":f>", "> " + log.getEditor().getEffectiveName() + ": " + log.getDescription(), false);
            }
        }

        List<ActionRow> ARs = new ArrayList<>();
        List<SelectOption> Pages = new ArrayList<>();
        int totalAmount = Math.min(1250, doQueryValue(Integer.class,"SELECT GetClanLogsCount(?) AS 'Count'", getID()).orElse(0));
        for (int i = 0; i < Math.ceil((double) totalAmount/AmountPerPages); i++) {
            if (Pages.size() < 25) {
                Pages.add(SelectOption.of(TL(M,"Page") + " " + (i + 1) + "...", "" + (i + 1))
                        .withDescription("[" + ((i * AmountPerPages) + 1) + "-" + Math.min(((i + 1) * AmountPerPages), totalAmount) + "/" + totalAmount + "] " + TL(M,"view-more", "" + (i + 1))));
            }
        }
        if (Pages.size() > 1) ARs.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-logs-cp")).setPlaceholder(Pages.get(CMD.Page-1).getLabel()).setRequiredRange(1, 1).addOptions(Pages).build()));

        M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
    }
    public void ViewClanWarn(InteractionHook M, ClanManager CMD) {
        if (CMD.getMe().hasPermission(M, ClanPermission.MANAGE_INFORMATION)) {
            EmbedBuilder E = new EmbedBuilder();
            E.setThumbnail(getEmblemURL());
            E.setColor(getColor());
            E.setAuthor(TL(M,"clan-warn-title", getName()), null, getEmblemURL());
            E.setDescription(TL(M, "clan-warn-description"));
            for (Clan_Warn log : getClanWarns()) {
                if (E.getFields().size() < 25) {
                    E.addField((log.isCompleted() ? ":white_check_mark: " : "") + log.getName() + ": <t:" + log.getEpochSecond() + ":f>", "> " + log.getUser().getEffectiveName() + ": " + log.getDescription(), false);
                }
            }
            M.editOriginalEmbeds(E.build()).queue();
        } else {
            M.editOriginal(TL(M, "clan-manage-fail-permission", "MANAGE_INFORMATION")).queue();
        }
    }
    public void ViewTrophies(InteractionHook M, PageViewerCommand CMD) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getEmblemURL());
        E.setColor(getColor());
        E.setTitle(TL(M, "Trophies"));
        E.setDescription(TL(M,"trophies-description"));

        int AmountPerPages = 25;
        for (DatabaseObject.Row TR : getAllTrophies(CMD.Page, AmountPerPages)) {
            if (E.getFields().size() < 25) {
                String power = !isPowerDisabled(M.getInteraction().getGuild()) ? " (" + BotEmoji.get("POW") + " " + PlusMinusSignWithNum(POWERDECIMAL.format(TR.getAsDouble("POW"))) + ")" : "";
                E.addField(TR.getAsString("Emoji") + " " + TR.getAsString("Name") + " `x" + TR.getAsString("Count") + "`" + power, "*" + TR.getAsString("Description") + "*", false);
            }
        }

        List<ActionRow> ARs = new ArrayList<>();
        List<SelectOption> Pages = new ArrayList<>();
        int totalAmount = Math.min(1250, doQueryValue(Integer.class,"SELECT GetClanTrophiesCount(?) AS 'Count'", getID()).orElse(0));
        for (int i = 0; i < Math.ceil((double) totalAmount/AmountPerPages); i++) {
            if (Pages.size() < 25) {
                Pages.add(SelectOption.of(TL(M,"Page") + " " + (i + 1) + "...", "" + (i + 1))
                        .withDescription("[" + ((i * AmountPerPages) + 1) + "-" + Math.min(((i + 1) * AmountPerPages), totalAmount) + "/" + totalAmount + "] " + TL(M,"view-more", "" + (i + 1))));
            }
        }
        if (Pages.size() > 1) ARs.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-troph-cp")).setPlaceholder(Pages.get(CMD.Page-1).getLabel()).setRequiredRange(1, 1).addOptions(Pages).build()));

        M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
    }
    public void ViewInterclan(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getEmblemURL());
        E.setColor(getColor());
        E.setTitle(TL(M,"interclans-of-clan", getName()));
        List<SelectOption> options = new ArrayList<>();
        String S = "";
        for (Interclan IC : getInterclans()) {
            Clan opposingClan = IC.getOpposingClanOf(this);
            if (opposingClan != null) {
                options.add(SelectOption.of(getName() + " vs. " + opposingClan.getName(), IC.getId() + "")
                        .withDescription(IC.getCompletedTime("dd/MM/yyyy") + " | " + IC.getRuleVSString() + " | " + IC.getMatchingRule())
                        .withEmoji(opposingClan.getEmoji().retrieve()));
            } else {
                options.add(SelectOption.of(getName() + " vs. " + IC.getOpposingClanOf(this).getName(), IC.getId() + "")
                        .withDescription(IC.getCompletedTime("dd/MM/yyyy") + " | " + IC.getRuleVSString() + " | " + IC.getMatchingRule()));
            }
            if (IC.getHoster().getID() == getID()) {
                S = S + "__" + IC.getCompletedTime("dd/MM/yyyy") + ":__ **" + IC.getHoster().getName() + "** vs. **" + IC.getJoiner().getName() + "**\n";
            } else if (IC.getJoiner().getID() == getID()) {
                S = S + "__" + IC.getCompletedTime("dd/MM/yyyy") + ":__ **" + IC.getJoiner().getName() + "** vs. **" + IC.getHoster().getName() + "**\n";
            }
        }
        if (E.getFields().isEmpty() && options.isEmpty()) {
            M.editOriginalEmbeds(E.build()).queue();
        } else {
            E.setDescription(S);
            StringSelectMenu menu = StringSelectMenu.create("interclan-select-view")
                    .setPlaceholder(options.getFirst().getLabel())
                    .setRequiredRange(1, 1)
                    .addOptions(options)
                    .build();
            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu)).queue();
        }
    }
    public void ViewVault(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getEmblemURL());
        E.setColor(getColor());
        E.setTitle(TL(M,"clan-vault", getName()));
        E.setDescription(TL(M,"clan-vault-description"));
        int medals = 0;
        for (ClanMember m : getClanMembers()) {
            if (!m.isReinforcement()) {
                medals = medals + m.getProfile().Totals().getTotalMedals();
            }
        }
        E.addField(TL(M,"TotalMedals"), ":medal: **" + medals + "**", true);
        M.editOriginalEmbeds(E.build()).queue();
        Update();
    }


    public static Clan get(String name) {
        List<Clan> C = Clan.listOpenPaused();
        if (name.length() >= 5) {
            for (Clan clanfile : C) {
                if (clanfile.getName().toLowerCase().contains(name.toLowerCase())) {
                    return clanfile;
                } else if (name.toLowerCase().contains(clanfile.getName().toLowerCase())) {
                    return clanfile;
                }
            }
            C.sort(Comparator.comparingDouble((Clan obj) -> similarity(obj.Name, name, true)).reversed());
            if (similarity(C.getFirst().Name, name, true) >= 70) return C.getFirst();
            return null;
        } else {
            C.sort(Comparator.comparingDouble((Clan obj) -> similarity(obj.Tag, name, true)).reversed());
            if (similarity(C.getFirst().Tag, name, true) >= 70) return C.getFirst();
            else return null;
        }
    }
    public static List<Clan> find(String name) {
        List<Clan> result = new ArrayList<>();
        List<Clan> C = Clan.listOpenPaused();
        if (name.length() >= 5) {
            for (Clan clanfile : C) {
                if (clanfile.getName().toLowerCase().contains(name.toLowerCase())) {
                    result.add(clanfile);
                } else if (name.toLowerCase().contains(clanfile.getName().toLowerCase())) {
                    result.add(clanfile);
                }
            }
            C.sort(Comparator.comparingDouble((Clan obj) -> similarity(obj.Name, name, true)).reversed());
            for (Clan c : C) if (similarity(C.getFirst().Name, name, true) >= 70) result.add(c);
        } else {
            C.sort(Comparator.comparingDouble((Clan obj) -> similarity(obj.Tag, name, true)).reversed());
            for (Clan c : C) if (similarity(C.getFirst().Tag, name, true) >= 70) result.add(c);
        }
        return result;
    }
    public static Clan get(long id) {
        return getById(Clan.class, id).orElse(null);
    }


    public static List<Clan> list() {
        return getAll(Clan.class);
    }
    public static List<Clan> listOpen() {
        return getAllWhere(Clan.class,"Status = ?", "Open");
    }
    public static List<Clan> listPaused() {
        return getAllWhere(Clan.class,"Status = ?", "Pause");
    }
    public static List<Clan> listClosed() {
        return getAllWhere(Clan.class,"Status = ?", "Closed");
    }
    public static List<Clan> listOpenPaused() {
        return getAllWhere(Clan.class,"Status = ? OR Status = ?", "Open", "Pause");
    }

    public void ClanInfoUI(InteractionHook M, ClanManager CMD) throws Exception {
        CMD.MyID = M.getInteraction().getUser().getIdLong();
        switch (CMD.InfoType) {
            case "Basic Information":
                ViewClan(M);
                break;
            case "Power & Activity":
                ViewPowerDetails(M, new FilterCommand(getID(), "clan-power-filter"), new GamesCommand(getID(), "clan-power-game"));
                break;
            case "Tournament List":
                ViewTournaments(M, new PageViewerCommand(getID(), "clan-tourn-cp"), new FilterCommand(getID(), "clan-tourn-filter"), new GamesCommand(getID(), "clan-tourn-game"));
                break;
            case "Vault":
                ViewVault(M);
                break;
            case "Interclans History":
                ViewInterclan(M);
                break;
            case "Clan Rewards":
                ViewTrophies(M, new PageViewerCommand(getID(), "clan-troph-cp"));
                break;
            case "Clan Logs":
                if (CMD.getMe().hasPermission(M, ClanPermission.MANAGE_INFORMATION)) {
                    ViewClanLogs(M, new PageViewerCommand(getID(), "clan-logs-cp"));
                } else {
                    M.editOriginal(TL(M, "clan-manage-fail-permission", "MANAGE_INFORMATION")).queue();
                }
                break;
            case "Clan Warns":
                ViewClanWarn(M, CMD);
                break;
        }
    }
    public void ManageClanUI(InteractionHook M, ClanManager CMD) {
        try {
            Guild G = ClanServerID != null ? DiscordAccount.getGuildById(ClanServerID) : null;
            EmbedBuilder E = new EmbedBuilder();
            E.setAuthor(" • " + getName(), null, getEmblemURL());
            E.setThumbnail(getEmblemURL());
            E.setTitle(TL(M, "clan-manager"));
            E.setColor(getColor());
            E.setDescription(TL(M, "clan-manager-description", getName()) + "\n"
                    + "> " + TL(M, "Name") + ": `" + getName() + "`\n"
                    + "> " + TL(M, "Clan-Tag") + ": `" + getTag() + "`\n"
                    + "> " + TL(M, "Color") + ": `" + getHexValue(getColor()) + "`\n"
                    + "> " + TL(M, "Nationality") + ": " + getNationality().getNamePlusFlag() + "\n"
                    + "> " + TL(M, "Birthday") + ": <t:" + getTimeCreated().getEpochSecond() + ":d>\n"
                    + "> " + TL(M, "clan-server") + ": **" + (G != null ? G.getName() : "N/A") + "**\n\n"
                    + "> " + TL(M, "Description") + ": `" + getDescription() + "`\n"
                    + "> " + TL(M, "Join_Requirements") + ": `" + getRequirements() + "`\n\n"
                    + "> Tiktok: `" + getTiktokURL() + "`\n"
                    + "> Twitch: `" + getTwitchURL() + "`\n"
                    + "> Twitter: `" + getTwitterURL() + "`\n"
                    + "> YouTube: `" + getYouTubeURL() + "`\n"
                    + "> Website: `" + getWebsiteURL() + "`\n"
                    + "> Discord: `" + getDiscordURL() + "`\n"
                    + "> Instagram: `" + getInstagramURL() + "`\n");

            List<ActionRow> Rows = new ArrayList<>();
            List<SelectOption> options = new ArrayList<>();
            if (CMD.getMe().hasPermission(ClanPermission.ADMINISTRATOR)) {
                options.add(SelectOption.of(TL(M, "Tag"), "tag").withDescription(TL(M, "clan-edit-tag-description")).withEmoji(Emoji.fromUnicode("U+1F3F7")));
                options.add(SelectOption.of(TL(M, "Name"), "name").withDescription(TL(M, "clan-edit-name-description")).withEmoji(Emoji.fromUnicode("U+1F4DD")));
                if (M.getInteraction().isFromGuild()) {
                    options.add(SelectOption.of(TL(M, "clan-server"), "server").withDescription(TL(M, "clan-edit-clan-server-description")).withEmoji(Emoji.fromUnicode("U+1F3F0")));
                }
            }
            if (CMD.getMe().hasPermission(ClanPermission.MANAGE_INFORMATION)) {
                options.add(SelectOption.of(TL(M, "Logo URL"), "logo").withDescription(TL(M, "clan-edit-logo-description")).withEmoji(getEmoji().retrieve()));
                options.add(SelectOption.of(TL(M, "Color"), "color").withDescription(TL(M, "clan-edit-colorcode-description")).withEmoji(Emoji.fromUnicode("U+1F3A8")));
                options.add(SelectOption.of(TL(M, "Description"), "description").withDescription(TL(M, "clan-edit-description-description")).withEmoji(Emoji.fromUnicode("U+1F4DD")));
                options.add(SelectOption.of(TL(M, "Nationality"), "nationality").withDescription(TL(M, "clan-edit-nationality-description")).withEmoji(Emoji.fromUnicode("U+1F1FA U+1F1F3")));
                options.add(SelectOption.of(TL(M, "Join_Requirements"), "requirements").withDescription(TL(M, "clan-edit-join_req-description")).withEmoji(Emoji.fromUnicode("U+1F4D5")));
                options.add(SelectOption.of("Twitch", "media-twitch").withDescription(TL(M, "edit-media-description", "Twitch")).withEmoji(Emoji.fromUnicode("U+1F47E")));
                options.add(SelectOption.of("Twitter", "media-twitter").withDescription(TL(M, "edit-media-description", "Twitter")).withEmoji(Emoji.fromUnicode("U+1F426")));
                options.add(SelectOption.of("Website", "media-website").withDescription(TL(M, "edit-media-description", "Website")).withEmoji(Emoji.fromUnicode("U+1F4BB")));
                options.add(SelectOption.of("Discord", "media-discord").withDescription(TL(M, "edit-media-description", "Discord")).withEmoji(Emoji.fromUnicode("U+1F4F1")));
                options.add(SelectOption.of("YouTube", "media-youtube").withDescription(TL(M, "edit-media-description", "YouTube")).withEmoji(Emoji.fromUnicode("U+25B6 U+FE0F")));
                options.add(SelectOption.of("Instagram", "media-instagram").withDescription(TL(M, "edit-media-description", "Instagram")).withEmoji(Emoji.fromUnicode("U+1F4F7")));
                options.add(SelectOption.of("Tiktok", "media-tiktok").withDescription(TL(M, "edit-media-description", "Tiktok")).withEmoji(Emoji.fromUnicode("U+1F4F7")));
            }
            if (isClanManager(CMD.MyID)) options.add(SelectOption.of("[ADMIN] Force Add Member", "add-member").withDescription("Forcefully add a member to this clan.").withEmoji(BotEmoji.get("Members").retrieve()));
            if (!options.isEmpty()) Rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-manage-config"))
                        .setPlaceholder(TL(M, "Description") + "...")
                        .setRequiredRange(1, 1).addOptions(options).build()));



            {
                List<SelectOption> MemberOptions1 = new ArrayList<>();
                List<SelectOption> MemberOptions2 = new ArrayList<>();
                ClanMemberInteractCommand CMD2 = new ClanMemberInteractCommand(this);
                CMD2.MyID = CMD.MyID;
                getClanMembers().sort(Comparator.comparingInt(ClanMember::getHighestRolePosition));
                for (ClanMember member : getClanMembers()) {
                    if (MemberOptions1.size() < 25) {
                        MemberOptions1.add(SelectOption.of(member.Number + " • " + member.getUser().getEffectiveName(), member.getID() + "")
                                .withDescription(member.listTasksOneLine())
                                .withEmoji(member.getClanRoles().isEmpty() ? Emoji.fromUnicode("U+1f464") : member.getClanRoles().getFirst().getEmoji()));
                    } else {
                        MemberOptions2.add(SelectOption.of(member.Number + " • " + member.getUser().getEffectiveName(), member.getID() + "")
                                .withDescription(member.listTasksOneLine())
                                .withEmoji(member.getClanRoles().isEmpty() ? Emoji.fromUnicode("U+1f464") : member.getClanRoles().getFirst().getEmoji()));
                    }
                }
                if (!MemberOptions1.isEmpty()) Rows.add(ActionRow.of(StringSelectMenu.create(CMD2.Command("clan-manage-member-1"))
                            .setPlaceholder(TL(M, "clan-manager-manage-member") + " 1: " + MemberOptions1.getFirst().getLabel()).setRequiredRange(1, 1)
                            .addOptions(MemberOptions1).build().withDisabled(!CMD.getMe().hasPermission(ClanPermission.MANAGE_MEMBER))));
                if (!MemberOptions2.isEmpty()) Rows.add(ActionRow.of(StringSelectMenu.create(CMD2.Command("clan-manage-member-2"))
                            .setPlaceholder(TL(M, "clan-manager-manage-member") + " 2: " + MemberOptions2.getFirst().getLabel()).setRequiredRange(1, 1)
                            .addOptions(MemberOptions2).build().withDisabled(!CMD.getMe().hasPermission(ClanPermission.MANAGE_MEMBER))));

            }
            List<SelectOption> O = new ArrayList<>();
            O.add(SelectOption.of("[New] " + TL(M, "Clan-Role"), "new-role"));
            O.addAll(getClanRoles().stream().map(CR -> SelectOption.of(CR.getName(), CR.getId() + "").withDescription(CR.getPermissions().stream().map(Objects::toString).collect(Collectors.joining(", "))).withEmoji(CR.getEmoji())).toList());
            Rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-clanrole-manage-select"))
                            .setPlaceholder(TL(M, "clan-manager-manage-clan-role") + ": " + getClanRoles().getFirst().getName()).setRequiredRange(1, 1)
                            .addOptions(O).build()).withDisabled(!CMD.getMe().hasPermission(ClanPermission.MANAGE_ROLE)));

            Button btn1 = Button.secondary(CMD.Command("clan-license-manage-bg"), "License BG [" + Item.get(Item.ItemType.LICENSE_BG).size() + "]").withDisabled(!CMD.getMe().hasPermission(ClanPermission.MANAGE_INFORMATION));
            Button btn2 = Button.secondary(CMD.Command("clan-license-manage-fg"), "License FG [" + Item.get(Item.ItemType.LICENSE_FG).size() + "]").withDisabled(!CMD.getMe().hasPermission(ClanPermission.MANAGE_INFORMATION));
            Button btn3 = Button.secondary(CMD.Command("clan-license-manage-ry"), "License RY [" + Item.get(Item.ItemType.LICENSE_RY).size() + "]").asDisabled();
            Button btn4 = Button.secondary(CMD.Command("clan-license-manage-st"), "License ST [" + Item.get(Item.ItemType.LICENSE_ST).size() + "]").asDisabled();
            Button btn5 = Button.secondary(CMD.Command("clan-license-manage-sp"), "License SP").withDisabled(!CMD.getMe().hasPermission(ClanPermission.MANAGE_INFORMATION));
            Rows.add(ActionRow.of(btn1, btn2, btn3, btn4, btn5));
            M.editOriginalEmbeds(E.build()).setComponents(Rows).setReplace(true).queue();
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public void InviteMemberUI(InteractionHook M, ClanInviteCommand CMD) {
        if (CMD.getMe().hasPermission(M, ClanPermission.INVITE_MEMBER)) {
            if (getMemberCount() < 50) {
                if (!CMD.isReinforcement || getReinforcementCount() < 5) {
                    if ((Clan.getClanOfUser(CMD.MemberID) == null && !CMD.isReinforcement) || (Clan.getReinforcementOfUser(CMD.MemberID) == null && CMD.isReinforcement)) {
                        if (CMD.isPrivate) {
                            User Target = CMD.getTargetMember().getUser();
                            try (MessageCreateData Mess = InviteMember(Target, CMD.getMe().getUser(), CMD)) {
                                Target.openPrivateChannel().queue(channel -> { // MY DM
                                    channel.sendMessage(Mess).queue(m -> {
                                        M.editOriginal(TL(M, "clan-invite-send", CMD.getTargetMember().getUser().getEffectiveName())).queue();
                                    }, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, error -> {
                                        M.editOriginal(TL(M, "clan-invite-send-fail", "**" + Target.getEffectiveName() + "**")).queue();
                                    }));
                                });
                            } catch (Exception e) {
                                replyException(M, e);
                            }
                        } else if (hasPermissionInChannel(M, M.getInteraction().getGuildChannel(), Permission.MESSAGE_SEND)) {
                            M.getInteraction().getMessageChannel().sendMessage(InviteMember(CMD.getTargetMember().getUser(), CMD.getMe().getUser(), CMD)).queue();
                            M.editOriginal(TL(M, "clan-invite-send", CMD.getTargetMember().getUser().getEffectiveName())).queue();
                        }
                    } else {
                        M.editOriginal(TL(M, "clan-invite-send-fail-already-clan", "**" + CMD.getTargetMember().getUser().getEffectiveName() + "**")).queue();
                    }
                } else {
                    M.editOriginal(TL(M, "clan-reinforcement-limit")).queue();
                }
            } else {
                M.editOriginal(TL(M,"clan-member-limit")).queue();
            }
        }
    }
    public void ClanLeaveUI(InteractionHook M, ClanMemberInteractCommand CMD) {
        if (!CMD.getMe().isCaptain()) {
            Button Yes = Button.success(CMD.Command("clan-leave-confirm"), TL(M,"yes"));
            Button No = Button.danger(CMD.Command("clan-leave-deny"), TL(M,"no"));
            M.editOriginal(CMD.getMe().getUser().getAsMention() + ", " + TL(M,"clan-leave", "**" + getEmojiFormatted() + " " + getName() + "**")).setComponents(ActionRow.of(Yes, No)).queue();
        } else {
            M.editOriginal(TL(M,"clan-leave-fail-captain")).queue();
        }
    }
    public void ClanDisbandUI(InteractionHook M, ClanManager CMD) {
        if (CMD.getMe().isCaptain(M)) {
            Button Yes = Button.success(CMD.Command("clan-disband-confirm"), TL(M,"yes"));
            Button No = Button.danger(CMD.Command("clan-disband-deny"), TL(M,"no"));
            M.editOriginal(CMD.getMe().getUser().getAsMention() + ", " + TL(M,"clan-disband", "**" + getEmojiFormatted() + " " + getName() + "**")).setComponents(ActionRow.of(Yes, No)).queue();
        }
    }
    public void InterclanRequestUI(InteractionHook M, InterclanCommand CMD) {
        if (CMD.getMe().hasPermission(M, ClanPermission.MANAGE_INTERCLAN)) {
            if (Interclan.getOngoing(this, CMD.getJoinClan()) == null) {
                try {
                    User oppCaptain = CMD.getJoinClan().getCaptain().getUser();
                    Profile P = Profile.get(oppCaptain);
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle(TL(P,"interclan-request"));
                    embed.setDescription(TL(P,"interclan-invite", oppCaptain.getAsMention(), "**" + CMD.getMe().getUser().getEffectiveName() + "**"));
                    embed.setFooter("• " + getName());
                    embed.setColor(Color.decode(mixColors(getColor(), CMD.getJoinClan().getColor())));
                    Button btn = Button.success(CMD.Command("interclan-accept"), TL(P,"accept"));
                    Button btn2 = Button.secondary(CMD.Command("interclan-refuse"), TL(P,"refuse"));
                    M.editOriginalEmbeds(embed.build()).setContent(oppCaptain.getAsMention()).setComponents(ActionRow.of(btn, btn2)).queue();
                    AddClanLog(CMD.getMe().getUser(),"Interclan Request", "Requested an Interclan against **" + CMD.getJoinClan().getName() + "**.");
                } catch (Exception e) {
                    replyException(M, e);
                }
            } else {
                M.editOriginal(TL(M,"interclan-invite-fail-2")).queue();
            }
        }
    }

    public void EditClanRoleUI(ClanRoleManager CMD, InteractionHook M, ClanRole CR) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M, "clanrole-manager"));
        E.setAuthor(CR.getName());
        E.setDescription(TL(M, "clanrole-manager-description"));
        E.setThumbnail(getEmblemURL());
        E.setColor(getColor());

        E.addField(TL(M, "Name"), CR.getName(), true);
        E.addField(TL(M, "Emoji"), CR.getEmojiFormatted(), true);
        E.addField(TL(M, "Permissions"), CR.getPermissions().stream().map(s -> "`" + s + "`").collect(Collectors.joining("\n")), false);

        Button BTN1 = Button.success(CMD.Command("clan-clanrole-manage-name"), TL(M,"Name"));
        Button BTN2 = Button.success(CMD.Command("clan-clanrole-manage-emoji"), "Emoji (Unicode)");
        Button BTN3 = Button.danger(CMD.Command("clan-clanrole-manage-delete"), TL(M,"Delete")).withDisabled(CR.isBuiltin());
        StringSelectMenu menu = StringSelectMenu.create(CMD.Command("clan-clanrole-manage-permissions"))
                .setPlaceholder(ClanPermission.MANAGE_INFORMATION.name() + ", " + ClanPermission.INVITE_MEMBER.name() + "...").setRequiredRange(0, 5)
                .addOptions(Stream.of(ClanPermission.values()).map(P -> SelectOption.of(P.name(), P.name()).withDefault(CR.getPermissions().contains(P))).collect(Collectors.toList())).build();
        M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(BTN1, BTN2, BTN3), ActionRow.of(menu)).queue();
    }
    public void LicenseManageUI(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M,"license-manager"));
        E.setDescription(TL(M, "license-manager-description"));
        E.setThumbnail(getEmblemURL());
        E.setColor(getColor());
        E.addField(TL(M,"background"), getCardBackgroundItem().getName(), false);
        E.addField(TL(M,"foreground"), getCardForegroundItem().getName(), false);
        E.addField(TL(M,"ray"), getCardRayItem().getName(), false);
        E.addField(TL(M,"strike"), getCardStrikeItem().getName(), false);
        E.addField(TL(M,"sponsor"), getSponsor() + "", false);
        try (CardImageBuilder CIB = new CardImageBuilder(Profile.get(M.getInteraction().getUser()), this)) {
            E.setImage(getFileUrl(CIB.GenerateCardPNG().DownloadPNGToFile(), "card.png"));
        }
        M.editOriginalEmbeds(E.build()).queue();
    }

    public static void InterclanViewer(Interclan IC, InteractionHook M) {
        IC.EndIfPossible();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("Interclan");
        embedBuilder.setTitle(IC.getHoster().getName() + " vs " + IC.getJoiner().getName());
        embedBuilder.setColor(IC.getColor());
        embedBuilder.setFooter("ID: " + IC.getId());
        embedBuilder.setThumbnail(IC.getLogoURL());

        embedBuilder.setDescription("Interclan Phase: :**red_circle: " + IC.getState() + "**\n"
                + "Completed: <t:" + IC.getCompletedTime().getEpochSecond() + ":d> (<t:" + IC.getCompletedTime().getEpochSecond() + ":R>)\n"
                + ":closed_book: VS Rule: " + IC.getRuleVSString());

        if (!IC.getDuels().isEmpty()) {
            embedBuilder.addField(" ", " ", false);
            String P1 = "";
            String duals = "";
            String P2 = "";
            for (Interclan_Duel D : IC.getDuels()) {
                P1 = P1 + D.getGame().getEmoji() + " " + D.getP1().getEffectiveName() + "\n";
                duals = duals + "**" + D.P1Score + " " + D.getGame().getVSEmoji() + " " + D.P2Score + "**\n";
                P2 = P2 + D.getP2().getEffectiveName() + "\n";
            }
            duals = duals + "**" + IC.getHostTotalScore() + " " + Game.get("IEVR").getVSEmoji() + IC.getJoinTotalScore() + "**";
            embedBuilder.addField(IC.getHoster().getName(), P1, true);
            embedBuilder.addField(TL(M,"Matches"), duals, true);
            embedBuilder.addField(IC.getJoiner().getName(), P2, true);
            embedBuilder.setImage(IC.getImagePNG());
            M.editOriginalEmbeds(embedBuilder.build()).queue();
        } else {
            M.editOriginalEmbeds(embedBuilder.build()).queue();
        }
    }
    public static void InterclanManager(InterclanCommand CMD, InteractionHook M) {
        InterclanViewer(CMD.I, M);
        Button BTN1 = Button.success(CMD.Command("interclan-manage-add-duel"), "Add Duel");
        Button BTN2 = Button.secondary(CMD.Command("interclan-manage-generate-random"), "Generate Duel (Random)").asDisabled();
        Button BTN3 = Button.secondary(CMD.Command("interclan-manage-generate-balanced"), "Generate Duel (Balanced)").asDisabled();
        List<ActionRow> R = new ArrayList<>();
        R.add(ActionRow.of(BTN1, BTN2, BTN3));
        List<SelectOption> L = new ArrayList<>();
        for (Interclan_Duel D : CMD.I.getDuels()) {
            if (L.size() < 25) {
                L.add(SelectOption.of(D.getP1().getEffectiveName() + " vs " + D.getP2().getEffectiveName(), D.getId() + "").withDescription(D.getGame() + ": " + D.getP1Score() + " - " + D.getP2Score()).withEmoji(D.getGame().getEmoji()));
            }
        }
        if (!L.isEmpty()) {
            StringSelectMenu Menu = StringSelectMenu.create(CMD.Command("interclan-duel-select"))
                    .setPlaceholder("Duel: " + L.getFirst().getLabel()) // shows the placeholder indicating what this menu is for
                    .setRequiredRange(1, 1) // exactly one must be selected
                    .addOptions(L)
                    .build();
            R.add(ActionRow.of(Menu));
        }
        M.editOriginalComponents(R).queue();
    }

    public void changeLogo(ClanManager CMD, InteractionHook M, String url) {
        if (CMD.getMe().hasPermission(M, ClanPermission.ADMINISTRATOR)) {
            try {
                File Downloaded = new File(TempDirectory + "/" + url.hashCode() + ".png");
                new ImageProxy(url).downloadToFile(Downloaded).whenComplete((file, throwable) -> {
                    try {
                        if (ImageIO.read(file) != null) {
                            CutTransparentBorders(file, file, 350, 350);
                            setEmblem(Files.readAllBytes(file.toPath()));
                            AddEmoji(true);
                            ManageClanUI(M, CMD);
                            for (ClanMember CM : getClanMembers()) CM.resetCards();
                            for (ServerInfo I : ServerInfo.getClanRolesAndTagServers()) {
                                if (areMembersInGuild(I.getGuild())) {
                                    I.Roles().setRoleIcon(getRole(I), Icon.from(getEmblem()), getEmojiFormatted(), true);
                                }
                            }
                            LogClanUpdatesLogo();
                        } else {
                            M.editOriginal(TL(M,"image-fail-extension")).queue();
                        }
                    } catch (Exception e) {
                        M.editOriginal(TL(M,"image-fail-extension")).queue();
                    }
                });
            } catch (IllegalArgumentException e) {
                M.editOriginal(TL(M,"image-fail-extension")).queue();
            }
        }
    }

    private synchronized List<EmbedBuilder> getYourClanInfo(ServerInfo I) {
        List<EmbedBuilder> Es = new ArrayList<>();
        try {
            {
                EmbedBuilder E = new EmbedBuilder();
                E.setColor(getColor());
                E.setDescription(getDescription());
                E.setThumbnail(getEmblemURL());
                E.setTitle((":white_check_mark: ") +
                        getTag() + " | " + getName());

                E.addField(getNationality().getFlag().getFormatted() + " " + TL(this,"Nationality") + ":", "- " + getNationality().toString(), true);
                E.addField(":calendar_spiral: " + TL(this,"Date_Created") + ":", "- <t:" + getTimeCreated().getEpochSecond() + ":R>", true);
                E.addField(":people_hugging: " + TL(this,"Members") + ": ", "- (" + getClanMembers().size() + "/50)", true);

                if (I != null && getRole(I) != null) {
                    E.addField(getEmojiFormatted() + " " + TL(this,"Clan-Server-Role") + ": ", "- " + getRole(I).getAsMention(), true);
                }
                E.addField(":crown: " + TL(this,"Clan-Captain") + ": ", "- " + getCaptain().getUser().getAsMention(), true);
                E.addField("`                                                                           `", " ", false);

                DatabaseObject.Row TR;
                if (!isPowerDisabled(I)) {
                    TR = ClanPowerSQL(null, null);
                    String INPUT = POWERDECIMAL.format(TR.getAsDouble("POW: Member"));
                    E.addField(TL(this,"Members") + " (**x" + TR.get("Amount: Member") + "**)",
                            BotEmoji.get("POW") + " **" + PlusMinusSignWithNum(INPUT) + INPUT + "**",
                            true);
                    INPUT = POWERDECIMAL.format(TR.getAsDouble("POW: Months"));
                    E.addField(TL(this,"Months") + " (**x" + TR.get("Amount: Months") + "**)",
                            BotEmoji.get("POW") + " **" + PlusMinusSignWithNum(INPUT) + INPUT + "**",
                            true);
                    INPUT = POWERDECIMAL.format(TR.getAsDouble("POW: League"));
                    E.addField(TL(this,"League") + " (**x" + TR.get("Amount: League") + "**)",
                            BotEmoji.get("POW") + " **" + PlusMinusSignWithNum(INPUT) + INPUT + "**",
                            true);
                    INPUT = POWERDECIMAL.format(TR.getAsDouble("POW: Trophies"));
                    E.addField(TL(this,"Trophies") + " (**x" + TR.get("Amount: Trophies") + "**)",
                            BotEmoji.get("POW") + " **" + PlusMinusSignWithNum(INPUT) + INPUT + "**",
                            true);
                    INPUT = POWERDECIMAL.format(0);
                    E.addField("Interclans" + " (**x" + 0 + "**)",
                            BotEmoji.get("POW") + " **" + PlusMinusSignWithNum(INPUT) + INPUT + "**",
                            true);
                    INPUT = POWERDECIMAL.format(TR.getAsDouble("POW: Tournament"));
                    E.addField(TL(this,"Tournaments") + " (**x" + TR.get("Amount: Tournament") + "**)",
                            BotEmoji.get("POW") + " **" + PlusMinusSignWithNum(INPUT) + INPUT + "**",
                            true);
                    INPUT = POWERDECIMAL.format(TR.getAsDouble("Total Power"));
                    E.addField(TL(this,"Total"),
                            BotEmoji.get("POW") + " **" + INPUT + "**",
                            false);
                    E.addField("`                                                                           `", " ", false);
                }
                {
                    TR = ClanActivitySQL(null, null);
                    E.addField(":chart_with_upwards_trend: " + TL(this, "Member-Activity"),
                            "> **" + TL(this, "Last_Month") + ": " + TR.get("Match Activity") + "**\n" +
                                    "> **" + TL(this, "Last_Months_Average", "3") + ": " + POWERDECIMAL.format(TR.getAsDouble("Average Match Activity")) + "**\n",
                            false);
                    E.addField(":chart_with_upwards_trend: " + TL(this, "Tournament-Participation-Rate"),
                            "> **" + TL(this, "Last_Month") + ": " + TR.get("Tournament Activity") + "**\n" +
                                    "> **" + TL(this, "Last_Months_Average", "3") + ": " + POWERDECIMAL.format(TR.getAsDouble("Average Tournament Activity")) + "**\n",
                            false);
                    E.addField("`                                                                           `", " ", false);
                }
                Es.add(E);
            }
            {
                EmbedBuilder E = new EmbedBuilder();
                E.setColor(getColor());
                E.setThumbnail(getEmblemURL());
                String s = "# " + BotEmoji.get("Members").getFormatted() + " " + TL(this,"Members") + "\n";
                for (ClanMember M : getClanMembers()) {
                    s = s + M.getNationEmoji() + (M.isCaptain() ? ":crown: " : "") + M.Number + " • *" + M.getUser().getAsMention() + (getClanMembers().size() <= 35 ? " ~ (@" + M.getUser().getName() + ")" : "") + "*\n";
                    s = s + "> -# └ " + M.listTasksOneLine() + "\n";
                }
                E.setDescription(s);
                E.addField("`                                                                           `", " ", false);
                Es.add(E);
            }

            Es.getLast().setFooter(" • " + TLG(I,"updated-on-time", getClock() + " (GMT+2)"), I.getGuild().getIconUrl());
        } catch (Exception e) {
            handleException(e);
        }
        return Es;
    }
    public synchronized void RefreshInfoChannel() {
        try {
            if (getClanServerID() > 0) {
                ServerInfo I = ServerInfo.get(getClanServerID());
                if (I.Channels().getClanInfo() != null && hasPermissionInChannel(null, I.Channels().getClanInfo().getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_HISTORY)) {
                    club.minnced.discord.webhook.send.component.button.Button.Style S = club.minnced.discord.webhook.send.component.button.Button.Style.LINK;
                    club.minnced.discord.webhook.send.component.layout.ActionRow ROW = club.minnced.discord.webhook.send.component.layout.ActionRow.of(
                            (isURLValid(getTwitterURL()) ? new club.minnced.discord.webhook.send.component.button.Button(S, getTwitterURL()).setLabel("X") : new club.minnced.discord.webhook.send.component.button.Button(S, "https://x.com").setLabel("X").setDisabled(true)),
                            (isURLValid(getTwitchURL()) ? new club.minnced.discord.webhook.send.component.button.Button(S, getTwitchURL()).setLabel("Twitch") : new club.minnced.discord.webhook.send.component.button.Button(S, "https://x.com").setLabel("Twitch").setDisabled(true)),
                            (isURLValid(getYouTubeURL()) ? new club.minnced.discord.webhook.send.component.button.Button(S, getYouTubeURL()).setLabel("YouTube") : new club.minnced.discord.webhook.send.component.button.Button(S, "https://x.com").setLabel("YouTube").setDisabled(true)),
                            (isURLValid(getInstagramURL()) ? new club.minnced.discord.webhook.send.component.button.Button(S, getInstagramURL()).setLabel("Instagram") : new club.minnced.discord.webhook.send.component.button.Button(S, "https://x.com").setLabel("Instagram").setDisabled(true)),
                            (isURLValid(getWebsiteURL()) ? new club.minnced.discord.webhook.send.component.button.Button(S, getWebsiteURL()).setLabel("Website") : new club.minnced.discord.webhook.send.component.button.Button(S, "https://x.com").setLabel("Website").setDisabled(true))
                    );
                    List<EmbedBuilder> Es = getYourClanInfo(I);
                    if (Es.size() >= 3) {
                        I.Channels().getClanInfo().ModifyWebhookMessageElseCreate(
                                getClanWebhook().addEmbeds(parseEmbedBuilders(Es.getFirst().build())), msg -> {
                                    if (!Objects.equals(I.Channels().ClanInfoMessageID, msg.getId())) {
                                        I.Channels().ClanInfoMessageID = msg.getId();
                                        I.Channels().UpdateOnly("ClanInfoMessageID");
                                    }
                                });
                        I.Channels().getClanInfo().ModifyWebhookMessage2ElseCreate(
                                getClanWebhook().addEmbeds(parseEmbedBuilders(Es.get(1).build())), msg -> {
                                    if (!Objects.equals(I.Channels().ClanInfoMessageID2, msg.getId())) {
                                        I.Channels().ClanInfoMessageID2 = msg.getId();
                                        I.Channels().UpdateOnly("ClanInfoMessageID2");
                                    }
                                });
                        I.Channels().getClanInfo().ModifyWebhookMessage3ElseCreate(
                                getClanWebhook().addComponents(ROW).addEmbeds(parseEmbedBuilders(Es.get(2).build())), msg -> {
                                    if (!Objects.equals(I.Channels().ClanInfoMessageID3, msg.getId())) {
                                        I.Channels().ClanInfoMessageID3 = msg.getId();
                                        I.Channels().UpdateOnly("ClanInfoMessageID3");
                                    }
                                });
                    } else if (Es.size() == 2) {
                        I.Channels().getClanInfo().ModifyWebhookMessageElseCreate(
                                getClanWebhook().addEmbeds(parseEmbedBuilders(Es.getFirst().build())), msg -> {
                                    if (!Objects.equals(I.Channels().ClanInfoMessageID, msg.getId())) {
                                        I.Channels().ClanInfoMessageID = msg.getId();
                                        I.Channels().UpdateOnly("ClanInfoMessageID");
                                    }
                                });
                        I.Channels().getClanInfo().ModifyWebhookMessage2ElseCreate(
                                getClanWebhook().addComponents(ROW).addEmbeds(parseEmbedBuilders(Es.get(1).build())), msg -> {
                                    if (!Objects.equals(I.Channels().ClanInfoMessageID2, msg.getId())) {
                                        I.Channels().ClanInfoMessageID2 = msg.getId();
                                        I.Channels().UpdateOnly("ClanInfoMessageID2");
                                    }
                                });
                    } else {
                        I.Channels().getClanInfo().ModifyWebhookMessageElseCreate(
                                getClanWebhook().addComponents(ROW).addEmbeds(parseEmbedBuilders(Es.getFirst().build())), msg -> {
                                    if (!Objects.equals(I.Channels().ClanInfoMessageID, msg.getId())) {
                                        I.Channels().ClanInfoMessageID = msg.getId();
                                        I.Channels().UpdateOnly("ClanInfoMessageID");
                                    }
                                });
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    public String getPowerAsString() {
        return POWERDECIMAL.format(ClanPowerSQL(null,null).getAsDouble("Total Power"));
    }
    public DatabaseObject.Row ClanPowerSQL(Long serverId, String gamecode) {
        return doQuery("CALL DisplayClanPower(?,?,?)", getID(), serverId, gamecode).orElse(null);
    }

    private transient DatabaseObject.Row activity;
    public DatabaseObject.Row ClanActivitySQL(Long serverId, String gamecode) {
        return activity == null ? activity = doQuery("CALL DisplayClanActivity(?,?,?,?,?)", getID(), serverId, gamecode, 30, 3).orElse(null) : activity;
    }

    public int getTournamentsAmount() {
        return doQueryAll("CALL DisplayClanTournaments(?,?,?,?,?,?);", ID, null,  null, 1, 999, false).size();
    }


    private void setID(Long ID) {
        this.ID = ID;
    }
    private void setName(String name) {
        Name = name;
    }
    private void setTag(String tag) {
        Tag = tag;
    }
    private String getColorcode() {
        return Colorcode;
    }
    private void setColorcode(String colorcode) {
        Colorcode = colorcode;
    }
}
