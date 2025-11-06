package iecompbot.objects.server.tournament.challonge.server;

import at.stefangeyer.challonge.exception.DataAccessException;
import at.stefangeyer.challonge.model.Match;
import at.stefangeyer.challonge.model.Participant;
import at.stefangeyer.challonge.model.Tournament;
import at.stefangeyer.challonge.model.enumeration.TournamentState;
import club.minnced.discord.webhook.DiscordEmoji;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.Utility;
import iecompbot.interaction.cmdbreakdown.IDCommand;
import iecompbot.interaction.cmdbreakdown.server.ChallongeCommand;
import iecompbot.interaction.cmdbreakdown.server.DoubleIDCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.Nationality;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.profile_game.Profile_Game;
import iecompbot.objects.server.Blacklist;
import iecompbot.objects.server.ChannelMessage;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.Challonge_WaitingList;
import iecompbot.objects.server.tournament.SubMatch;
import iecompbot.objects.server.tournament.challonge.BaseCTournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static iecompbot.Constants.ChallongeLogoURL;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.Utility.*;
import static iecompbot.interaction.Automation.*;
import static iecompbot.objects.BotManagers.isTournamentManager;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.server.ServerInfo.getTournamentUpdatesChannels;
import static my.utilities.util.Utilities.getClock;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class SChallonge_Tournament extends BaseCTournament<SChallonge_Tournament, SChallonge_Match, SChallonge_Participant> {
    public transient ServerInfo I;
    protected transient ChannelMessage PanelChannel;
    protected transient ChannelMessage InscriptionChannel;
    protected transient Role OrganizerRole = null;
    protected transient Role ParticipantRole = null;
    protected transient TextChannel PredictionChannel = null;
    protected transient TextChannel MatchResultChannel = null;
    protected transient TextChannel RulesChannel = null;

    public long ServerID;

    public Long PanelChannelID;
    public Long PanelMessageID;
    public Long InscriptionChannelID;
    public Long InscriptionMessageID;

    public long OrganizerRoleID;
    public long ParticipantRoleID;
    public long PredictionChannelID;
    public long MatchResultChannelID;
    public long RulesChannelID;

    public String InscriptionChannelInviteLink = null;

    public long getServerID() {
        return ServerID;
    }
    public long getOrganizerRoleID() {
        return OrganizerRoleID;
    }
    public long getParticipantRoleID() {
        return ParticipantRoleID;
    }
    public long getMatchResultChannelID() {
        return MatchResultChannelID;
    }
    public long getPredictionChannelID() {
        return PredictionChannelID;
    }
    public ChannelMessage getPanelChannel() {
        return PanelChannel == null ? PanelChannel = new ChannelMessage(ServerID, PanelChannelID, PanelMessageID) : PanelChannel;
    }
    public ChannelMessage getInscriptionChannel() {
        return InscriptionChannel == null ? InscriptionChannel = new ChannelMessage(ServerID, InscriptionChannelID, InscriptionMessageID) : InscriptionChannel;
    }
    public Role getOrganizerRole() {
        return OrganizerRole == null ? OrganizerRole = I.getGuild().getRoleById(OrganizerRoleID) : OrganizerRole;
    }
    public Role getParticipantRole() {
        return ParticipantRole == null ? ParticipantRole = I.getGuild().getRoleById(ParticipantRoleID) : ParticipantRole;
    }
    public TextChannel getPredictionChannel() {
        return PredictionChannel == null ? PredictionChannel = I.getGuild().getTextChannelById(PredictionChannelID) : PredictionChannel;
    }
    public TextChannel getMatchResultChannel() {
        return MatchResultChannel == null ? MatchResultChannel = I.getGuild().getTextChannelById(MatchResultChannelID) : MatchResultChannel;
    }
    public TextChannel getRulesChannel() {
        return RulesChannel == null ? RulesChannel = I.getGuild().getTextChannelById(RulesChannelID) : RulesChannel;
    }
    public String getInscriptionChannelInviteLink() {
        return InscriptionChannelInviteLink;
    }
    public void setInscriptionChannelInviteLink(String InscriptionChannelInviteLink) {
        this.InscriptionChannelInviteLink = InscriptionChannelInviteLink;
    }
    public void setResultChannel(TextChannel channel) {
        if (channel != null) this.MatchResultChannelID = channel.getIdLong();
    }
    public void setRulesChannel(TextChannel channel) {
        if (channel != null) this.RulesChannelID = channel.getIdLong();
    }
    public void setPredictionChannel(TextChannel channel) {
        if (channel != null) this.PredictionChannelID = channel.getIdLong();
    }
    public void setPanelChannel(TextChannel channel) {
        this.PanelChannelID = channel.getIdLong();
        PanelChannel = new ChannelMessage(ServerID, PanelChannelID);
    }
    public void setInscriptionChannel(TextChannel channel) {
        if (channel != null) this.InscriptionChannelID = channel.getIdLong();
    }
    public void setInscriptionChannel(Message message) {
        this.InscriptionChannelID = message.getChannelIdLong();
        this.InscriptionMessageID = message.getIdLong();
        InscriptionChannel = new ChannelMessage(ServerID, InscriptionChannelID, InscriptionMessageID);
    }
    public void setOrganizerRole(Role role) {
        this.OrganizerRoleID = role.getIdLong();
    }
    public void setParticipantRole(Role role) {
        this.ParticipantRoleID = role.getIdLong();
    }

    @Override
    public boolean isOrganiser(User user) {
        Member M = I.getGuild().getMemberById(user.getId());
        return M != null && M.getRoles().contains(getOrganizerRole()) || super.isOrganiser(user);
    }

    public void RefreshPanelMessage() {
        RefreshPanelOneAtATime(this);
    }
    public void RefreshInscriptionMessage() {
        try {
            if (isRegistrationOpen() || getInscriptionChannel().getMessage() != null) {
                if (!isInscriptionByBot() && getInscriptionChannel().getMessage().getReactions().isEmpty()) {
                    getInscriptionChannel().getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue();
                    InscriptionMessageID = getInscriptionChannel().MessageID;
                }
                else if (isInscriptionByBot()) {
                    WebhookEmbedBuilder E = new WebhookEmbedBuilder();
                    E.setAuthor(new WebhookEmbed.EmbedAuthor(getName(), ChallongeLogoURL, null));
                    E.setThumbnailUrl(getGame().getImageUrl());
                    E.setColor(I.getColor().getRGB());
                    E.setFooter(new WebhookEmbed.EmbedFooter("• " + I.getGuild().getName(), I.getGuild().getIconUrl()));
                    E.setImageUrl(getSignupImageURL());

                    if (I.getCurrency() != null) {
                        String s = "";
                        if (I.getCurrencyPerTop1() > 0) s += ":first_place: = **" + I.getCurrency().getEmojiFormatted() + " ×" + I.getCurrencyPerTop1() + "**\n";
                        if (I.getCurrencyPerTop2() > 0) s += ":second_place: = **" + I.getCurrency().getEmojiFormatted() + " ×" + I.getCurrencyPerTop2() + "**\n";
                        if (I.getCurrencyPerTop3() > 0) s += ":third_place: = **" + I.getCurrency().getEmojiFormatted() + " ×" + I.getCurrencyPerTop3() + "**\n";
                        if (!s.isEmpty()) E.addField(new WebhookEmbed.EmbedField(false, TLG(I,"Rewards"), s));
                    }

                    String participantsOrTeam = TLG(I,getVSAmount() > 1 ? "Teams" : "Participants");
                    String Cap = "";
                    String spot = participantsOrTeam + ": **" + getParticipantCount() + "** | **" + getType().toString().toUpperCase() + "** | **" + getVSAmount() + "v" + getVSAmount() + "**\n> __" + TLG(I, "only-no-spot") + "__";
                    if (!isRegistrationOpen()) {
                        E.setDescription(TLG(I, "tournament-inscription-desc", "**:white_check_mark: " + TLG(I, "Register") + "**", "<t:" + getStartAtTime().getEpochSecond() + ":R> (<t:" + getStartAtTime().getEpochSecond() + ":d>)") + "\n" + ":no_entry: __" + TLG(I, "registrations-closed") + "__ :no_entry:");
                    } else {
                        if (getSignupCap() > 0) {
                            int spotleft = getSignupCap() - getParticipantCount();
                            Cap = "**[" + getParticipantCount() + "/" + getSignupCap() + "]:** ";
                            spot = participantsOrTeam + ": **" + getParticipantCount() + "** | **" + getType().toString().toUpperCase() + "** | **" + getVSAmount() + "v" + getVSAmount() + "**\n> __" + TLG(I, "zero-spot-left") + "__";
                            if (spotleft > 1) {
                                spot = participantsOrTeam + ": **" + getParticipantCount() + "** | **" + getType().toString().toUpperCase() + "** | **" + getVSAmount() + "v" + getVSAmount() + "**\n> __" + TLG(I, "only-spot-left", spotleft) + "__";
                            } else if (spotleft == 1) {
                                spot = participantsOrTeam + ": **" + getParticipantCount() + "** | **" + getType().toString().toUpperCase() + "** | **" + getVSAmount() + "v" + getVSAmount() + "**\n> __" + TLG(I, "only-one-spot-left") + "__";
                            } else if (!getWaitingList().isEmpty()) {
                                String l = getWaitingList().stream().map(s -> "- <@" + s.getUserId() + ">").collect(Collectors.joining("\n"));
                                E.addField(new WebhookEmbed.EmbedField(false, TLG(I, "Waiting-list"), StopString(l, 950)));
                            }
                        }
                        E.setDescription(TLG(I, "tournament-inscription-desc","**:white_check_mark: " + TLG(I, "Register") + "**", "<t:" + getStartAtTime().getEpochSecond() + ":R> (<t:" + getStartAtTime().getEpochSecond() + ":d>)") + "\n> " + Cap + spot);
                    }
                    IDCommand CMD = new IDCommand(getId());
                    club.minnced.discord.webhook.send.component.button.Button BTN1 = new club.minnced.discord.webhook.send.component.button.Button(club.minnced.discord.webhook.send.component.button.Button.Style.SECONDARY, CMD.Command("challonge-register")).setLabel(TLG(I, "Register")).setEmoji(DiscordEmoji.unicode("\u2705"));
                    club.minnced.discord.webhook.send.component.button.Button BTN2 = new club.minnced.discord.webhook.send.component.button.Button(club.minnced.discord.webhook.send.component.button.Button.Style.SECONDARY, CMD.Command("challonge-unregister")).setLabel(TLG(I, "Unregister")).setEmoji(DiscordEmoji.unicode("\u274C"));
                    club.minnced.discord.webhook.send.component.button.Button BTN3 = new club.minnced.discord.webhook.send.component.button.Button(club.minnced.discord.webhook.send.component.button.Button.Style.LINK, getRulesChannel() != null ? getRulesChannel().getJumpUrl() : getInscriptionChannel().getChannel().getJumpUrl()).setLabel(TLG(I, "Read-the-rules")).setEmoji(DiscordEmoji.unicode("\uD83D\uDCD5"));
                    getInscriptionChannel().ModifyWebhookMessageElseCreate(I.getServerWebhook().addComponents(club.minnced.discord.webhook.send.component.layout.ActionRow.of(BTN1, BTN2, BTN3)).addEmbeds(E.build()), msg -> {
                        if (!Objects.equals(InscriptionMessageID, msg.getId())) {
                            InscriptionMessageID = msg.getId();
                            UpdateOnly("InscriptionMessageID");
                        }
                    });
                }
            }
        } catch (Exception ignored) {}
    }
    public List<MessageEmbed> getChallongeBracket() {
        List<MessageEmbed> Es = new ArrayList<>();
        try {
            String playerLocale = getVSAmount() > 1 ? "Teams" : "Players";
            EmbedBuilder E = new EmbedBuilder();
            E.setColor(I.getColor());
            E.setThumbnail(getGame().getImageUrl());
            if (isPending()) {
                E.setAuthor("REGISTRATION PHASE", null, ChallongeLogoURL);
                E.setTitle(getName());
                String description = "";
                if (getOrganizerRole() != null) description = "> " + TLG(I, "tournament-bracket-desc-2", getOrganizerRole().getAsMention()) + "\n";
                if (getRulesChannel() != null) description = description + "> " + TLG(I, "Read-the-rules") + ": " + getRulesChannel().getAsMention() + "\n";
                if (getSignupCap() == 0) {
                    description = description + "> " + TLG(I,playerLocale) + ": **" + getParticipantCount() + "**\n";
                } else {
                    description = description + "> " + TLG(I,playerLocale) + ": **[" + getParticipantCount() + "/" + getSignupCap() + "]" + "**\n";
                }
                E.setDescription(description);

                Map<Nationality, List<SChallonge_Participant>> Langs = new HashMap<>();
                for (SChallonge_Participant P : getParticipants()) {
                    Profile PP = P.getLeaderPf();
                    if (PP != null && Langs.get(PP.getNationality()) == null) {
                        Langs.put(PP.getNationality(), getParticipants().stream().filter(p -> {
                            try {
                                return p.getLeaderPf().getNationality() == PP.getNationality();
                            } catch (Exception e) {
                                return false;
                            }
                        }).toList());
                    }
                }

                if (getVSAmount() == 1) {
                    List<List<SChallonge_Participant>> CPs = new ArrayList<>(Langs.values().stream().toList());
                    CPs.sort(Comparator.comparingInt((List<SChallonge_Participant> cp) -> cp.size()).reversed());

                    for (List<SChallonge_Participant> X : CPs) {
                        String list = "";
                        boolean first = true;
                        for (SChallonge_Participant Y : X) {
                            String newLine = "";
                            try {
                                newLine = newLine + "- " + I.getGuild().getMemberById(Y.getLeaderID()).getAsMention() + "\n";
                            } catch (Exception ignored) {
                                newLine = newLine + "- " + Y.getName() + "\n";
                            }
                            if (list.length() + newLine.length() > 1024 && E.length() < 5500 && E.getFields().size() < 25) {
                                E.addField(first ? X.getFirst().getLeaderPf().getNationality().getNamePlusFlag() : " ", list, false);
                                list = newLine;
                                first = false;
                            } else {
                                list = list + newLine;
                            }
                        }
                        if (!list.isEmpty() && E.length() < 5500 && E.getFields().size() < 25) {
                            try {
                                E.addField(first ? X.getFirst().getLeaderPf().getNationality().getNamePlusFlag() : " ", list, false);
                            } catch (Exception ignored) {}
                        }
                    }
                    int count = 0;
                    String list = "";
                    boolean first = true;
                    for (Challonge_WaitingList ID : getWaitingList()) {
                        String newLine = "";
                        try {
                            count++;
                            newLine = newLine + count + ". " + Profile.get(ID.getUserId()).getNationality().getFlag().getFormatted() + " " + I.getGuild().getMemberById(ID.getUserId()).getAsMention() + "\n";
                        } catch (Exception ignored) {
                            try {
                                newLine = newLine + count + ". " + Profile.get(ID.getUserId()).getNationality().getFlag().getFormatted() + " " + "<@" + ID.getUserId() + ">\n";
                            } catch (Exception ignored2) {
                                newLine = newLine + count + ". ???\n";
                            }
                        }
                        if (list.length() + newLine.length() > 1024 && E.length() < 5500 && E.getFields().size() < 25) {
                            E.addField(first ? TLG(I, "Waiting-list") : " ", list, false);
                            list = newLine;
                            first = false;
                        } else {
                            list = list + newLine;
                        }
                    }
                    if (!list.isEmpty() && E.length() < 5500 && E.getFields().size() < 25) {
                        E.addField(first ? TLG(I, "Waiting-list") : " ", list, false);
                    }
                }

                else {
                    String list = "";
                    for (SChallonge_Participant CP : getParticipants()) {
                        String line = "- **" + CP.getName() + " [" + CP.getAllTeammatesIDs().size() + "/" + getVSAmount() + "]**\n";
                        for (long id : CP.getAllTeammatesIDs()) {
                            try {
                                line = line + "├ " + Profile.get(id).getNationality().getFlag().getFormatted() + (id == CP.getLeaderID() ? " :crown:" : "") + " <@" + id + "> " +"\n";
                            } catch (Exception ignored) {
                                line = line + "- " + CP.getName() + "\n";
                            }
                        }
                        line = replaceLast(line, "├", "└");
                        if (list.length() + line.length() > 1024 && E.length() < 5500 && E.getFields().size() < 25) {
                            E.addField(" ", list, false);
                            list = line;
                        } else list = list + line;
                    }
                    if (!list.isEmpty()&& E.length() < 5500 && E.getFields().size() < 25) E.addField(" ", list, false);
                }
                E.addField("`                                                                           `", " ", false);
            }
            else if (isAwaitingReview()) {
                E.setAuthor("AWAITS CONFIRMATION", null, ChallongeLogoURL);
                E.setTitle(getName(), getFullChallongeURL());
                E.setDescription("> " + TLG(I, "tournament-bracket-awaiting-review", "**[" + TLG(I,"Complete") + "]**"));
                E.addField("`                                                                           `", " ", false);
            }
            else if (isGroupStageFinalized()) {
                E.setAuthor("START OF FINAL PHASE", null, ChallongeLogoURL);
                E.setTitle(getName(), getFullChallongeURL());
                String description = TLG(I, "tournament-bracket-group-start") + "\n";
                description = description + "> " + TLG(I, "tournament-bracket-desc-1", getFullChallongeURL()) + "\n";
                if (getRulesChannel() != null) description = description + "> " + TLG(I, "Read-the-rules") + ": " + getRulesChannel().getAsMention() + "\n";
                description = description + "> " + TLG(I,playerLocale) + ": **" + getParticipantCount() + "**";
                E.setDescription(description);
            }
            else if (isGroupStageUnderway() || isUnderway() || !getUnplayedMatches().isEmpty()) {
                E.setTitle(getName(), getFullChallongeURL());
                if (getUnplayedMatches().isEmpty()) {
                    E.setAuthor("END OF GROUP PHASE", null, ChallongeLogoURL);
                    String description = TLG(I, "tournament-bracket-group-end") + "\n";
                    if (getRulesChannel() != null) description = description + "> " + TLG(I, "Read-the-rules") + ": " + getRulesChannel().getAsMention() + "\n";
                    description = description + "> " + TLG(I, "tournament-bracket-desc-1", getFullChallongeURL()) + "\n";
                    description = description + "> " + TLG(I,"Matches") + ": **[" + getMatches(true).size() + "/" + getMatches().size() + "]** | " + TLG(I,playerLocale) + ": **" + getParticipantCount() + "**";
                    E.setDescription(description);
                } else {
                    E.setAuthor("PLAYING PHASE", null, ChallongeLogoURL);
                    String description = "";
                    if (getRulesChannel() != null) description = description + "> " + TLG(I, "Read-the-rules") + ": " + getRulesChannel().getAsMention() + "\n";
                    description = description + "> " + TLG(I, "tournament-bracket-desc-1", getFullChallongeURL()) + "\n";
                    description = description + "> " + TLG(I,"Matches") + ": **[" + getMatches(true).size() + "/" + getMatches().size() + "]** | " + TLG(I,playerLocale) + ": **" + getParticipantCount() + "**";
                    if (getMatchResultChannel() != null) description = "> " + TLG(I, "tournament-bracket-desc-3", getMatchResultChannel().getAsMention()) + "\n" + description;
                    if (getOrganizerRole() != null) description = "> " + TLG(I, "tournament-bracket-desc-2", getOrganizerRole().getAsMention()) + "\n" + description;

                    E.setDescription(description);
                    if (isGroupStageUnderway()) {
                        for (long G : getGroupIDs()) {
                            E.addField(":large_blue_diamond: __" + getGroupName(G) + "__", " ", false);
                            for (int R : getRounds()) {
                                List<String> str = listUncompletedMatchesOfRound(R, G);
                                for (String S : str) {
                                    if (S.length() > 10 && E.length() + S.length() < 5000) {
                                        E.addField(S.equals(str.getFirst()) ? (R >= 0 ? ":small_blue_diamond:" : ":small_orange_diamond:") + " __" + getRoundLong(R, G) + "__  (" + getRoundProgress(R, G) + ")" + (getDeadline(R, G) != null ? "  **Deadline: <t:" + getDeadline(R, G).getEpochSecond() + ":R>**" : "") : " ", S, false);
                                    }
                                }
                            }
                        }
                    } else {
                        for (int R : getRounds()) {
                            List<String> str = listUncompletedMatchesOfRound(R, 0);
                            for (String S : str) {
                                if (S.length() > 10 && E.length() + S.length() < 5000) {
                                    E.addField(S.equals(str.getFirst()) ? (R >= 0 ? ":small_blue_diamond:" : ":small_orange_diamond:") + " __" + getRoundLong(R, 0) + "__  (" + getRoundProgress(R, 0) + ")" + (getDeadline(R, 0) != null ? "  **Deadline: <t:" + getDeadline(R, 0).getEpochSecond() + ":R>**" : "") : " ", S, false);
                                }
                            }
                        }
                    }
                    E.addField("`                                                                           `", " ", false);
                }
            }
            else if (isComplete()) {
                E.setAuthor("ENDING PHASE", null, ChallongeLogoURL);
                E.setTitle(getName(), getFullChallongeURL());
                String description = "";
                if (getOrganizerRole() != null) description = "> " + TLG(I, "tournament-bracket-desc-2", getOrganizerRole().getAsMention()) + "\n";
                description = description + "> " + TLG(I,"Matches") + ": **[" + getMatches(true).size() + "/" + getMatches().size() + "]** | " + TLG(I,playerLocale) + ": **" + getParticipantCount() + "**";
                E.setDescription(description);
                int i = 0;
                String s = "";
                String s2 = "";
                String s3 = "";
                E.addField(TLG(I,"Name"), " ", true);
                E.addField(TLG(I,"Wins") + " | " + TLG(I,"Defeats") + " | " + TLG(I,"Ties"), " ", true);
                E.addField(TLG(I,"GoalsScoredTaken"), " ", true);
                List<SChallonge_Participant> LP = new ArrayList<>(getParticipants());
                LP.sort(Comparator.comparingInt(SChallonge_Participant::getPosition));
                for (SChallonge_Participant P : LP) {
                    if (P.getPosition() > 0) {
                        String name = StopString(P.getName(), 12);
                        s = s + "**" + P.getPosition() + ")** " + name + "\n";
                        s2 = s2 + P.getWins() + " | " + P.getLoses() + " | " + P.getTies() + "\n";
                        s3 = s3 + "+" + (Math.max(P.getGoalsScored(), 0)) + "/-" + (Math.max(P.getGoalsTaken(), 0)) + "\n";
                        i++;
                        if (i == 64 && (E.length() + s.length() + s2.length() + s3.length()) < 5500) {
                            E.addField(" ", s, true);
                            E.addField(" ", s2, true);
                            E.addField(" ", s3, true);
                            s = "";
                            s2 = "";
                            s3 = "";
                        } else if (i == 48 && (E.length() + s.length() + s2.length() + s3.length()) < 5500) {
                            E.addField(" ", s, true);
                            E.addField(" ", s2, true);
                            E.addField(" ", s3, true);
                            s = "";
                            s2 = "";
                            s3 = "";
                        } else if (i == 32 && (E.length() + s.length() + s2.length() + s3.length()) < 5500) {
                            E.addField(" ", s, true);
                            E.addField(" ", s2, true);
                            E.addField(" ", s3, true);
                            s = "";
                            s2 = "";
                            s3 = "";
                        } else if (i == 16 && (E.length() + s.length() + s2.length() + s3.length()) < 5500) {
                            E.addField(" ", s, true);
                            E.addField(" ", s2, true);
                            E.addField(" ", s3, true);
                            s = "";
                            s2 = "";
                            s3 = "";
                        } else if (i == 8) {
                            E.addField(" ", s
                                    .replaceAll("1\\)", ":first_place:)")
                                    .replaceAll("2\\)", ":second_place:)")
                                    .replaceAll("3\\)", ":third_place:)"), true);
                            E.addField(" ", s2, true);
                            E.addField(" ", s3, true);
                            s = "";
                            s2 = "";
                            s3 = "";
                        }
                    }
                }
                if (s.length() > 1 &&  (E.length() + s.length() + s2.length() + s3.length()) < 5500) {
                    if (s.length() < 800) {
                        E.addField(" ", s, true);
                        E.addField(" ", s2, true);
                        E.addField(" ", s3, true);
                    }
                }
                int totaldq = 0;
                String dqs = "";
                for (SChallonge_Match Matches : getMatches()) {
                    if (Matches.getP1Score() == -1 || Matches.getP2Score() == -1 || Matches.isForfeited()) {
                        totaldq++;
                    }
                }
                if (totaldq > 0) {
                    E.addField(TLG(I,"Disqualifications") + " (DQs)", dqs + "- **" + TLG(I,"Total") + ":** " + totaldq, true);
                }
            }
            E.setFooter(" • " + TLG(I,"updated-on-time", getClock() + " (GMT+2)"), I.getGuild().getIconUrl());
            Es.add(E.build());
            if (isGroupStageUnderway() || isUnderway() || !getUnplayedMatches().isEmpty()) {
                EmbedBuilder EE = new EmbedBuilder();
                EE.setTitle("Deadlines");
                String s = "";
                String s2 = "";
                for (long G : getGroupIDs()) {
                    if (isGroupStageUnderway()) {
                        s = s + "\n__" + getGroupName(G) + "__\n";
                    }
                    for (int R : getRounds()) {
                        if (getDeadline(R, G) != null && getDeadline(R, G).isAfter(Instant.now().minus(2, ChronoUnit.DAYS))) {
                            String isComplete = "";
                            if (getMatches(R, G, true).size() == getMatches(R, G).size()) {
                                isComplete = "~~";
                            }
                            if (getRoundLong(R, G).contains("Losers")) {
                                if (getDeadline(R, G).isAfter(Instant.now().plus(6, ChronoUnit.HOURS))) {
                                    s2 = s2 + ":green_circle: " + isComplete + getRoundLong(R, G) + " : <t:" + getDeadline(R, G).getEpochSecond() + ":d> (<t:" + getDeadline(R, G).getEpochSecond() + ":R>)" + isComplete + "\n";
                                } else if (getDeadline(R, G).isAfter(Instant.now().plus(3, ChronoUnit.HOURS))) {
                                    s2 = s2 + ":yellow_circle: " + isComplete + getRoundLong(R, G) + " : <t:" + getDeadline(R, G).getEpochSecond() + ":d> (<t:" + getDeadline(R, G).getEpochSecond() + ":R>)" + isComplete + "\n";
                                } else if (getDeadline(R, G).isAfter(Instant.now())) {
                                    s2 = s2 + ":orange_circle: " + isComplete + getRoundLong(R, G) + " : <t:" + getDeadline(R, G).getEpochSecond() + ":d> (<t:" + getDeadline(R, G).getEpochSecond() + ":R>)" + isComplete + "\n";
                                } else {
                                    s2 = s2 + ":red_circle: " + isComplete + getRoundLong(R, G) + " : <t:" + getDeadline(R, G).getEpochSecond() + ":d> (<t:" + getDeadline(R, G).getEpochSecond() + ":R>)" + isComplete + "\n";
                                }
                            } else {
                                if (getDeadline(R, G).isAfter(Instant.now().plus(6, ChronoUnit.HOURS))) {
                                    s = s + ":green_circle: " + isComplete + getRoundLong(R, G) + " : <t:" + getDeadline(R, G).getEpochSecond() + ":d> (<t:" + getDeadline(R, G).getEpochSecond() + ":R>)" + isComplete + "\n";
                                } else if (getDeadline(R, G).isAfter(Instant.now().plus(3, ChronoUnit.HOURS))) {
                                    s = s + ":yellow_circle: " + isComplete + getRoundLong(R, G) + " : <t:" + getDeadline(R, G).getEpochSecond() + ":d> (<t:" + getDeadline(R, G).getEpochSecond() + ":R>)" + isComplete + "\n";
                                } else if (getDeadline(R, G).isAfter(Instant.now())) {
                                    s = s + ":orange_circle: " + isComplete + getRoundLong(R, G) + " : <t:" + getDeadline(R, G).getEpochSecond() + ":d> (<t:" + getDeadline(R, G).getEpochSecond() + ":R>)" + isComplete + "\n";
                                } else {
                                    s = s + ":red_circle: " + isComplete + getRoundLong(R, G) + " : <t:" + getDeadline(R, G).getEpochSecond() + ":d> (<t:" + getDeadline(R, G).getEpochSecond() + ":R>)" + isComplete + "\n";
                                }
                            }
                        }
                    }
                }
                if (s.contains("circle")) {
                    EE.setColor(I.getColor());
                    EE.setDescription(TLG(I, "tournament-bracket-deadline") + "\n\n" + s + "\n`                                                                           `\n\n" + s2);
                    Es.add(EE.build());
                }
            }
        } catch (Exception e) {
            sendPrivateMessage(getUserByID(508331399149912088L), getId() + "");
            handleException(e);
        }
        return Es;
    }
    public List<String> listUncompletedMatchesOfRound(int round, long groupid) {
        List<String> matches = new ArrayList<>();
        String List = "";
        for (SChallonge_Match M : getMatches(round, groupid)) {
            if (M.getPlayer1() != null || M.getPlayer2() != null && !M.isOptional()) {
                String P1Mention = "`???`";
                String P2Mention = "`???`";
                try {
                    if (getVSAmount() == 1 && I.getGuild().getMemberById(M.getPlayer1().getLeaderID()) != null) {
                        P1Mention = I.getGuild().getMemberById(M.getPlayer1().getLeaderID()).getAsMention();
                    } else {
                        P1Mention = "**" + M.getPlayer1().getName() + "**";
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (getVSAmount() == 1 && I.getGuild().getMemberById(M.getPlayer2().getLeaderID()) != null) {
                        P2Mention = I.getGuild().getMemberById(M.getPlayer2().getLeaderID()).getAsMention();
                    } else {
                        P2Mention = "**" + M.getPlayer2().getName() + "**";
                    }
                } catch (Exception ignored) {
                }
                if (!M.isCompleted()) {
                    String VS = " vs. ";
                    if (getGame().isStrikers()) {
                        VS = " " + BotEmoji.get("VS2") + " ";
                        if (M.getDeadline() != null && M.getDeadline().isBefore(Instant.now())) {
                            VS = " " + BotEmoji.get("VS2Hard") + " ";
                        }
                    } else if (getGame().isVR()) {
                        VS = " " + BotEmoji.get("VS3") + " ";
                        if (M.getDeadline() != null && M.getDeadline().isBefore(Instant.now())) {
                            VS = " " + BotEmoji.get("VS3Hard") + " ";
                        }
                    } else if (getGame().is3DS()) {
                        VS = " " + BotEmoji.get("VS1") + " ";
                        if (M.getDeadline() != null && M.getDeadline().isBefore(Instant.now())) {
                            VS = " " + BotEmoji.get("VS1Hard") + " ";
                        }
                    }
                    String Line = "- ";
                    if (groupid == 0) {
                        Line = Line + P1Mention + VS + P2Mention;
                    } else {
                        Line = Line + M.getRoundShort() + ": " + P1Mention + VS + P2Mention;
                    }
                    if (getVSAmount() > 1 && !M.getSubMatches().isEmpty()) Line = Line + "  **(" + M.getMatchProgress() + ")**";
                    Line = Line + "\n";
                    if (getVSAmount() > 1) {
                        for (SubMatch CSM : M.getSubMatches()) {
                            if (!CSM.isFinished())
                                Line = Line + "> " + CSM.getP1().getAsMention() + " " + VS + " " + CSM.getP2().getAsMention() + "\n";
                        }
                    }
                    if (List.length() + Line.length() > 1000) {
                        matches.add(List);
                        List = Line;
                    } else {
                        List = List + Line;
                    }
                }
            }
        }
        matches.add(List);
        return matches;
    }
    public synchronized static void RefreshPanelOneAtATime(SChallonge_Tournament CT) {
        try {
            if (CT.getPanelChannel() != null && hasPermissionInChannel(null, CT.getPanelChannel().getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EMBED_LINKS)) {
                Button btn = CT.isPending() && CT.getVSAmount() > 1 ? Button.success("challonge-panel-edit-team/" + CT.getId(), TLG(CT.I,"manage-team-name")) : Button.success("challonge-panel-view-info/" + CT.getId(), CT.getVSAmount() > 1 ? TLG(CT.I,"manage-team-name") : TLG(CT.I,"view-your-progress")).withDisabled(CT.isPending());
                Button btn2 = Button.success("challonge-panel-report-score/" + CT.getId(), "Add Score");
                Button btn3 = Button.secondary("challonge-panel-refresh/" + CT.getId(), "Refresh");
                if (CT.isPending()) {
                    btn2 = Button.danger("challonge-panel-manage-start/" + CT.getId(), TLG(CT.I,"Start"));
                } else if (CT.isGroupStageUnderway() && CT.getUnplayedMatches().isEmpty()) {
                    btn2 = btn2.asDisabled();
                } else if (CT.isGroupStageFinalized()) {
                    btn2 = btn2.asDisabled();
                } else if (CT.isAwaitingReview() || CT.isComplete()) {
                    btn2 = Button.danger("challonge-panel-manage-complete/" + CT.getId(), TLG(CT.I,"Complete")).withDisabled(CT.isComplete());
                    if (CT.isAwaitingReview()) btn3 = btn3.asDisabled();
                } else if (CT.isUnderway() || !CT.getUnplayedMatches().isEmpty()) {
                    btn2 = Button.success("challonge-panel-report-score/" + CT.getId(), "Add Score");
                }
                Button btn4 = Button.secondary("challonge-panel-manage-info/" + CT.getId(), "Manage");
                Button btn5 = Button.secondary("challonge-panel-manage-deadline/" + CT.getId(), "Deadline").withDisabled(CT.isComplete() || CT.isPending() || CT.isAwaitingReview());

                CT.getPanelChannel().ModifyWebhookMessageElseCreate(CT.I.getServerWebhook().addEmbeds(parseEmbedBuilders(CT.getChallongeBracket())).addComponents(parseComponentBuilder(btn, btn2, btn3, btn4, btn5)), msg -> {
                    if (!Objects.equals(CT.PanelMessageID, msg.getId())) {
                        CT.PanelMessageID = msg.getId();
                        CT.UpdateOnly("PanelMessageID");
                    }
                });
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public List<SChallonge_Match> getMatches() {
        if (Matches == null) {
            Matches = SChallonge_Match.get(this);
            for (SChallonge_Match P : Matches) P.T = this;
        }
        return Matches;
    }

    @Override
    public List<SChallonge_Participant> getParticipants() {
        if (Participants == null) {
            Participants = SChallonge_Participant.ofTournament(this);
            for (SChallonge_Participant P : Participants) P.T = this;
        }
        return Participants;
    }

    @Override
    public SChallonge_Participant AddParticipant(User member) throws Exception {
        SChallonge_Participant participant = super.AddParticipant(member);
        if (participant != null) I.Roles().AddRoleToMember(getParticipantRole(), BotEmoji.get("icon_fan").getFormatted(), I.getGuild().getMemberById(member.getIdLong()));
        return participant;
    }
    private SChallonge_Participant AddParticipant(Member member) throws Exception {
        SChallonge_Participant participant = super.AddParticipant(member.getUser());
        if (participant != null) I.Roles().AddRoleToMember(getParticipantRole(), BotEmoji.get("icon_fan").getFormatted(), member);
        return participant;
    }
    @Override
    public SChallonge_Participant DeleteParticipant(long participantId) throws DataAccessException {
        SChallonge_Participant P = super.DeleteParticipant(participantId);
        if (P != null) I.Roles().RemoveRoleFromMember(getParticipantRole(), BotEmoji.get("icon_fan").getFormatted(), I.getGuild().getMemberById(P.getLeaderID()));
        return P;
    }


    @Override
    public void AddParticipantFromInscription(InteractionHook M, User u, boolean force) {
        if (getVSAmount() == 1) {
            EmbedBuilder E = getTournamentEmbed();
            try {
                Member m = I.getGuild().getMemberById(u.getId());
                if (isRegistrationOpen() || force) {
                    if (m != null) {
                        if (!u.isBot() && getParticipantById(Profile.get(u).getId()) == null) {
                            if (I.getBlacklist(u, getGame()) == null || force) {
                                if (isAccountOldEnough(u) || force) {
                                    if (getSignupCap() > getParticipantCount() || getSignupCap() == 0) {
                                        AddParticipant(m);
                                        if (M != null) {
                                            E.setDescription(":white_check_mark: " + TL(M, "tournament-register-success", "**" + getName() + "**"));
                                            M.editOriginalEmbeds(E.build()).queue();
                                            RefreshPanelMessage();
                                            RefreshInscriptionMessage();
                                        }
                                    } else if (getWaitingList().stream().noneMatch(W -> W.getUserId() == m.getIdLong())) {
                                        I.LogSlash(TLG(I, "tournament-waiting-list-success", ":**hourglass: " + m.getEffectiveName() + "**", "**" + getName() + "**"));
                                        AddToWaitingList(m.getIdLong());
                                        if (M != null) {
                                            E.setDescription(":white_check_mark: " + TL(M, "tournament-waiting-list-success", "**" + m.getUser().getEffectiveName() + "**", "**" + getName() + "**"));
                                            M.editOriginalEmbeds(E.build()).queue();
                                            RefreshPanelMessage();
                                            RefreshInscriptionMessage();
                                        }
                                    } else if (M != null) {
                                        E.setDescription(":x: " + TL(M, "tournament-waiting-list-fail", "**" + getName() + "**"));
                                        M.editOriginalEmbeds(E.build()).queue();
                                    }
                                } else if (M != null) {
                                    E.setDescription(":x: " + TL(M, "join-tournament-fail", "`7`"));
                                    M.editOriginalEmbeds(E.build()).queue();
                                }
                            } else if (M != null) {
                                Blacklist BL = I.getBlacklist(u, null);
                                if (BL == null) BL = I.getBlacklist(u, getGame());
                                if (BL != null) {
                                    E.setDescription(":x: " + TL(M, "blacklist-attempt", "**" + BL.TournamentsLeft + "**", "**" + BL.getGame().getEmojiFormatted() + " " + BL.getGame().getName() + "**") + "\n> **" + TL(M, "Reason") + ":** " + BL.Reason);
                                    M.editOriginalEmbeds(E.build()).queue();
                                }
                            }
                        } else if (M != null) {
                            E.setDescription(":x: " + TL(M, "tournament-register-fail", "**" + getName() + "**"));
                            M.editOriginalEmbeds(E.build()).queue();
                        }
                    }
                } else if (M != null) {
                    E.setDescription(":x: " + TL(M, "tournament-set-no-longer-pending"));
                    M.editOriginalEmbeds(E.build()).queue();
                }
            } catch (Exception ignored) {
            }
        }
    }
    @Override
    public void DeleteParticipantFromInscription(InteractionHook M, User u) {
        try {
            EmbedBuilder E = getTournamentEmbed();
            if (isRegistrationOpen()) {
                if (getVSAmount() > 1) for (SChallonge_Participant p : getParticipants()) {
                    if (p.RemoveTeammate(u.getIdLong()) && p.getLeaderID() != u.getIdLong()) {
                        p.Update();
                        E.setDescription(":white_check_mark: " + TL(M, "tournament-unregister-success", "**" + getName() + "**"));
                        M.editOriginalEmbeds(E.build()).queue();
                        sendPrivateMessage(p.getLeader(), TL(M, "clan-leave-confirm-success", "**" + u.getEffectiveName() + "**", "**" + p.getName() + "**"));
                        I.Roles().RemoveRoleFromMember(getParticipantRole(), BotEmoji.get("icon_fan").getFormatted(), I.getGuild().getMemberById(u.getIdLong()));
                        RefreshPanelMessage();
                        break;
                    }
                }

                SChallonge_Participant P = getParticipantById(u.getIdLong());
                if (P != null) {
                    if (getVSAmount() > 1) {
                        for (User U : P.getTeammates()) {
                            if (U.getIdLong() != u.getIdLong()) {
                                E.setDescription(TL(Profile.get(U), "tournament-unregister-team-success", "**" + P.getName() + "**"));
                                sendPrivateMessage(U, new MessageCreateBuilder().setEmbeds(E.build()));
                            }
                        }
                    }
                    if (DeleteParticipant(u.getIdLong()) != null) {
                        if (M != null) {
                            E.setDescription(":white_check_mark: " + TL(M, "tournament-unregister-success", "**" + getName() + "**"));
                            M.editOriginalEmbeds(E.build()).queue();
                            RemoveFromWaitingList(u.getIdLong());
                        }
                    } else if (RemoveFromWaitingList(u.getIdLong())) {
                        I.LogSlash(TLG(I, "tournament-waitinglist-remove", ":**unlock: " + u.getEffectiveName() + "**", "**" + getName() + "**"));
                        if (M != null) {
                            E.setDescription(":white_check_mark: " + TL(M, "tournament-waitinglist-remove", "**" + u.getEffectiveName() + "**", "**" + getName() + "**"));
                            M.editOriginalEmbeds(E.build()).queue();
                        }
                    } else if (M != null) {
                        E.setDescription(":x: " + TL(M, "tournament-unregister-fail", "**" + getName() + "**"));
                        M.editOriginalEmbeds(E.build()).queue();
                    }
                    while (getSignupCap() > getParticipantCount()) {
                        if (!getWaitingList().isEmpty()) {
                            Member m = I.getGuild().getMemberById(RemoveFromWaitingList().getUserId());
                            if (m != null) AddParticipant(m);
                        } else break;
                    }
                    RefreshInscriptionMessage();
                    RefreshPanelMessage();
                } else if (M != null) {
                    E.setDescription(":x: " + TL(M, "tournament-unregister-fail", "**" + getName() + "**"));
                    M.editOriginalEmbeds(E.build()).queue();
                }
            } else if (M != null) {
                E.setDescription(":x: " + TL(M, "tournament-set-no-longer-pending"));
                M.editOriginalEmbeds(E.build()).queue();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void AddTeamFromInscription(InteractionHook M, User u) {
        if (getVSAmount() > 1) {
            EmbedBuilder E = getTournamentEmbed();
            try {
                SChallonge_Participant MyTeam = getParticipantById(Profile.get(u).getId());
                Member m = I.getGuild().getMemberById(u.getId());
                if (isRegistrationOpen() || MyTeam != null) {
                    if (m != null) {
                        if (!u.isBot() && getParticipants().stream().noneMatch(PP -> PP.getLeaderID() != u.getIdLong() && PP.getAllTeammatesIDs().contains(u.getIdLong()))) {
                            if (I.getBlacklist(u, getGame()) == null) {
                                if (isAccountOldEnough(u)) {
                                    if (getSignupCap() > getParticipantCount() || getSignupCap() == 0 || MyTeam != null) {
                                        ChallongeCommand CMD = new ChallongeCommand(getId());
                                        List<ActionRow> ARs = new ArrayList<>();
                                        if (MyTeam != null && MyTeam.isTeamFull()) {
                                            E.setDescription(TL(M, "tournament-register-teammate-select-fail"));
                                            M.editOriginalEmbeds(E.build()).queue();
                                        } else {
                                            int chooseAmount = getVSAmount() - 1 - (MyTeam != null ? (MyTeam.getAllTeammatesIDs().size() - 1) : 0);
                                            E.setDescription(TL(M, "tournament-register-teammate-select") + "\n"
                                                    + (MyTeam != null ? TL(M, "Members") + ": **[" + MyTeam.getAllTeammatesIDs().size() + "/" + getVSAmount() + "]**\n" + MyTeam.getTeammates().stream().map(U -> "- " + (U.getIdLong() == MyTeam.getLeaderID() ? ":crown: " : "") + U.getAsMention()).collect(Collectors.joining("\n")) : ""));
                                            ARs.add(ActionRow.of(EntitySelectMenu.create(CMD.Command("challonge-register-teammates"), EntitySelectMenu.SelectTarget.USER)
                                                    .setRequiredRange(1, chooseAmount).setPlaceholder(chooseAmount + " " + TL(M, "Teammates") + "...").build()));
                                            M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
                                            RefreshPanelMessage();
                                            RefreshInscriptionMessage();
                                        }
                                    } else if (M != null) {
                                        E.setDescription(":x: " + TL(M, "zero-spot-left"));
                                        M.editOriginalEmbeds(E.build()).queue();
                                    }
                                } else if (M != null) {
                                    E.setDescription(":x: " + TL(M, "join-tournament-fail", "`7`"));
                                    M.editOriginalEmbeds(E.build()).queue();
                                }
                            } else if (M != null) {
                                Blacklist BL = I.getBlacklist(u, null);
                                if (BL == null) BL = I.getBlacklist(u, getGame());
                                if (BL != null) {
                                    E.setDescription(":x: " + TL(M, "blacklist-attempt", "**" + BL.TournamentsLeft + "**", "**" + BL.getGame().getEmojiFormatted() + " " + BL.getGame().getName() + "**") + "\n> **" + TL(M, "Reason") + ":** " + BL.Reason);
                                    M.editOriginalEmbeds(E.build()).queue();
                                }
                            }
                        } else if (M != null) {
                            E.setDescription(":x: " + TL(M, "tournament-register-fail", "**" + getName() + "**"));
                            M.editOriginalEmbeds(E.build()).queue();
                        }
                    }
                } else if (M != null) {
                    E.setDescription(":x: " + TL(M, "tournament-set-no-longer-pending"));
                    M.editOriginalEmbeds(E.build()).queue();
                }
            } catch (Exception ignored) {
            }
        }
    }
    @Override
    public void SendTeamRequests(InteractionHook M, List<User> teammates) {
        if (getVSAmount() > 1) {
            EmbedBuilder E = getTournamentEmbed();
            try {
                if (teammates.stream().allMatch(u -> u != null && !u.isBot() && I.getGuild().getMemberById(u.getId()) != null)) {
                    if (teammates.stream().allMatch(u -> I.getBlacklist(u, getGame()) == null)) {
                        if (teammates.stream().allMatch(TT -> getParticipants().stream().noneMatch(PP -> PP.getAllTeammatesIDs().contains(TT.getIdLong())))) {
                            if (teammates.stream().allMatch(this::isAccountOldEnough)) {
                                SChallonge_Participant MyTeam = getParticipantById(Profile.get(M.getInteraction().getUser().getIdLong()).getId());
                                if (getSignupCap() > getParticipantCount() || getSignupCap() == 0 || MyTeam != null) {
                                    if (MyTeam == null) {
                                        MyTeam = AddParticipant(M.getInteraction().getMember());
                                        MyTeam.setName(M.getInteraction().getUser().getEffectiveName() + "'s Team");
                                    }
                                    ChallongeCommand CMD2 = new ChallongeCommand(getId());
                                    CMD2.ParticipantID = MyTeam.getId();
                                    for (User U : teammates) {
                                        Profile P = Profile.get(U);
                                        Button BTN1 = Button.secondary(CMD2.Command("challonge-teammate-accept"), TL(P, "yes"));
                                        Button BTN2 = Button.secondary(CMD2.Command("challonge-teammate-refuse"), TL(P, "no"));
                                        EmbedBuilder EE = new EmbedBuilder(E);
                                        EE.setTitle(TL(P, "Teammate-Request"));
                                        EE.setDescription(TL(P, "Teammate-Request-description", M.getInteraction().getUser().getAsMention(), "**" + getName() + "**"));
                                        sendPrivateMessage(U, new MessageCreateBuilder().setEmbeds(EE.build()).addComponents(ActionRow.of(BTN1, BTN2)));
                                    }
                                    M.editOriginal(TL(M, "clan-invite-send", "**" + replaceLast(teammates.stream().map(User::getEffectiveName).collect(Collectors.joining(", ")), ", ", " & ") + "**")).queue();
                                    RefreshPanelMessage();
                                    RefreshInscriptionMessage();
                                } else {
                                    E.setDescription(":x: " + TL(M, "zero-spot-left"));
                                    M.editOriginalEmbeds(E.build()).queue();
                                }
                            } else {
                                E.setDescription(":x: " + TL(M, "join-tournament-fail", "`7`"));
                                M.editOriginalEmbeds(E.build()).queue();
                            }
                        } else {
                            E.setDescription(":x: " + TL(M, "tournament-register-team-fail"));
                            M.editOriginalEmbeds(E.build()).queue();
                        }
                    } else {
                        String bl = "";
                        for (Blacklist BL : teammates.stream().map(u -> {
                            Blacklist BL = I.getBlacklist(u, null);
                            if (BL == null) BL = I.getBlacklist(u, getGame());
                            return BL;
                        }).toList()) {
                            bl = bl + ":x: " + TL(M, "blacklist-attempt", "**" + BL.TournamentsLeft + "**", "**" + BL.getGame().getEmojiFormatted() + " " + BL.getGame().getName() + "**") + "\n> **" + TL(M, "Reason") + ":** " + BL.Reason + "\n\n";
                        }
                        E.setDescription(bl);
                        M.editOriginalEmbeds(E.build()).queue();
                    }
                }
            } catch (Exception e) {
                replyException(M, e);
            }
        }
    }

    @Override
    public void CleanTournament() throws Exception {
        for (SChallonge_Participant P : new ArrayList<>(getParticipants())) {
            RemoveOrDQNonServerParticipant(P.getLeaderID());
        }
        for (SChallonge_Participant P : new ArrayList<>(getParticipants())) {
            ClearDuplicateParticipants(P);
        }
        if (isPending() && getInscriptionChannel().getMessage() != null && !getInscriptionChannel().getMessage().getReactions().isEmpty()) {
            getInscriptionChannel().getMessage().getReactions().getFirst().retrieveUsers().takeWhileAsync(ignored -> true).whenComplete((users, throwable) -> {
                if (users.size() >= 10) getInscriptionChannel().getMessage().removeReaction(getInscriptionChannel().getMessage().getReactions().getFirst().getEmoji()).queue();
                for (User u : users) AddParticipantFromInscription(null, u, false);
                RefreshNamesAndClans();
            });
        } else {
            RefreshNamesAndClans();
        }
        validateAllTeamsScores();
        Participants = null;
        RefreshPanelMessage();
    }

    public void AddRoleToEveryone() {
        if (getParticipantRole() != null && !isComplete()) {
            for (SChallonge_Participant P : getParticipants()) {
                for (Long ID : P.getAllTeammatesIDs()) {
                    I.Roles().AddRoleToMember(getParticipantRole(), BotEmoji.get("icon_fan").getFormatted(), I.getGuild().getMemberById(ID));
                }
            }
        }
    }
    public void RemoveRoleFromEveryone() {
        if (getParticipantRole() != null && isComplete()) {
            for (SChallonge_Participant P : getParticipants()) {
                for (Long ID : P.getAllTeammatesIDs()) {
                    I.Roles().RemoveRoleFromMember(getParticipantRole(), BotEmoji.get("icon_fan").getFormatted(), I.getGuild().getMemberById(ID));
                }
            }
        }
    }
    public void RefreshNamesAndClans() {
        if (getVSAmount() == 1) for (SChallonge_Participant P : getParticipants()) {
            try {
                if (!P.getName().equals(P.getLeader().getEffectiveName())) {
                    P.setName(P.getLeader().getEffectiveName());
                }
            } catch (Exception ignored) {}
        }
    }
    public void RemoveOrDQNonServerParticipant(Long TargetID) {
        if (TargetID != null && I.getGuild().getMemberById(TargetID) == null) {
            if (isUnderway() || isGroupStageUnderway()) {
                SChallonge_Participant DQ = getParticipantById(TargetID);
                getWebhookOfChannel(getMatchResultChannel(), CC -> {
                    try {
                        for (SChallonge_Match Match : getParticipantById(TargetID).getUnplayedMatches()) {
                            if (getVSAmount() > 1) {
                                if (Match.canTeamDQ(DQ)) {
                                    SubMatch CSM = Match.getSubMatch(TargetID);
                                    if (CSM.getP1ID() == TargetID) {
                                        CSM.AddScore(0, 3);
                                    } else if (CSM.getP2ID() == TargetID) {
                                        CSM.AddScore(3, 0);
                                    }
                                    Match.validateTeamScores(Match.getOpponent(DQ.getId()).getId());
                                    Match.Update();
                                    RefreshPanelMessage();
                                }
                            } else {
                                if (TargetID == Match.getPlayer1().getLeaderID()) {
                                    Match.AddScore(0, 0, Match.getParticipantID2());
                                } else if (TargetID == Match.getPlayer2().getLeaderID()) {
                                    Match.AddScore(0, 0, Match.getParticipantID1());
                                }
                                Match.Update();
                                ResyncChallonge();
                                Update();
                                for (SChallonge_Match M1 : Match.getPlayer1().getUnplayedMatches())
                                    M1.NotifyNextOpponent();
                                for (SChallonge_Match M2 : Match.getPlayer2().getUnplayedMatches())
                                    M2.NotifyNextOpponent();
                            }
                            CC.send(Match.getDQMessage(I.getGuild(), "Left the server.").build());
                        }
                    } catch (Exception ignored) {}
                });
            } else if (isPending()) {
                RemoveFromWaitingList(TargetID);
                DeleteParticipantFromInscription(null, getUserByID(TargetID));
                Message M = getInscriptionChannel().getMessage();
                M.removeReaction(M.getReactions().getFirst().getEmoji(), getUserByID(TargetID)).queue();
                Update();
            }
        }
    }

    public int NotifyAllByDM(String optionalMessage) {
        int i = 0;
        EmbedBuilder EE = new EmbedBuilder();
        EE.setTitle(getName());
        EE.setThumbnail(getGame().getImageUrl());
        EE.setTimestamp(getStartAtTime());
        EE.setImage(getSignupImageURL() != null ? getSignupImageURL() : null);
        EE.setAuthor(I.getGuild().getName(), getInscriptionChannelInviteLink(), I.getGuild().getIconUrl());
        EE.setColor(I.getColor());
        DoubleIDCommand CMD = new DoubleIDCommand(getServerID(), getId());
        for (Profile_Game PG : Profile_Game.list(getGame())) {
            try {
                Member member = I.getGuild().getMemberById(PG.getUserID());
                if (member == null) continue;
                Profile P = Profile.get(member.getIdLong());
                if (P.hasTournamentNotification() && PG.hasEverPlayed()) {
                    i++;
                    MessageCreateBuilder MSG = new MessageCreateBuilder();
                    EmbedBuilder E = new EmbedBuilder(EE);
                    E.setDescription(optionalMessage != null ? optionalMessage.replaceAll("<br>", "\n") : TL(P, "hope-to-see-you-there") + " ! :wink:");
                    E.addField(":closed_book: " + TL(P, "Details"), "> " + getType().toString().toUpperCase() + "\n> " + getVSAmount() + "v" + getVSAmount(), false);
                    E.addField(":small_orange_diamond: " + TL(P, "Date"), "> <t:" + getStartAtTimeEpochSecond() + ":d> (<t:" + getStartAtTimeEpochSecond() + ":R>)", false);
                    E.addField(":small_orange_diamond: " + TL(P, "tournament-register-now"), "> " + getInscriptionChannel().getChannel().getAsMention(), false);
                    E.addField(":small_blue_diamond: " + "Notification", "> " + TL(P, "edit-profile-notif"), false);
                    Button btn1 = Button.link(getInscriptionChannel().getChannel().getJumpUrl(), TL(P, "Register")).withEmoji(Emoji.fromUnicode("U+2705"));
                    Button btn2 = Button.secondary(CMD.Command("server-unregister"), TL(P, "Unregister")).withEmoji(Emoji.fromUnicode("U+274C"));
                    Button btn3 = Button.link(getRulesChannel() != null ? getRulesChannel().getJumpUrl() : getInscriptionChannel().getChannel().getJumpUrl(), TL(P, "Read-the-rules")).withEmoji(Emoji.fromUnicode("U+1F4D5"));
                    Button btn4 = Button.link(getInscriptionChannelInviteLink(), TL(P, "Join"));
                    MSG.setContent(":wave: Hey " + member.getUser().getAsMention() + ", " + TL(P, "tournament-dm-description", "**" + getGame().getFullName() + "**") + " **" + I.getGuild().getName() + "**");
                    MSG.setComponents(ActionRow.of(btn1, btn2, btn3, btn4));
                    MSG.setEmbeds(E.build());
                    sendPrivateMessage(member.getUser(), MSG);
                }
            } catch (Exception ignored) {
            }
        }
        return i;
    }

    public void LogTournamentAnnouncement(boolean force) {
        if (force || (I.isPublic && I.getGuild().getMemberCount() > 30 && (getSignupCap() >= 16 || getSignupCap() <= 0))) {
            if (isPending()) {
                for (DatabaseObject.Row TR : getTournamentUpdatesChannels()) {
                    try {
                        Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                        if (G == null) continue;
                        TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                        if (C == null) continue;
                        if (TR.getAsBoolean("isPublic")) {
                            if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                                getWebhookOfChannel(C, client -> {
                                    String description = "# " + BotEmoji.get("icon_fan") + TLG(G, "tournament-news-announcement");
                                    if (getInscriptionChannelInviteLink() != null || getInscriptionChannelInviteLink().length() > 1)
                                        description = description + "\n> " + TLG(G, "tournament-news-announcement-description", I.getFlag() + "**[" + getName() + "](" + getInscriptionChannelInviteLink() + ")**");
                                    else
                                        description = description + "\n> " + TLG(G, "tournament-news-announcement-description", "**" + getName() + "**");

                                    description = description + "\n> " + TLG(G, "Starting") + ": **<t:" + getStartAtTimeEpochSecond() + ":R> (<t:" + getStartAtTimeEpochSecond() + ":d>)**";
                                    if (getSignupCap() > 3)
                                        description = description + " | Max " + TLG(G, "Participants") + ": **" + getSignupCap() + "**";
                                    description = description + "\n> " + TLG(G, "Type") + ": **" + getType().name().replace("_", " ") + " ~ " + getVSAmount() + "v" + getVSAmount() + "**";

                                    description = description + "\n`                                                       `\n";

                                    WebhookEmbedBuilder E = new WebhookEmbedBuilder();
                                    E.setDescription(description);
                                    E.setThumbnailUrl(getGame().getImageUrl());
                                    E.setColor(I.getColor().getRGB());
                                    E.setFooter(new WebhookEmbed.EmbedFooter(I.getGuild().getName() + " | " + I.getGuild().getMemberCount() + " " + TLG(G, "Members"), I.getGuild().getIconUrl()));
                                    E.setTimestamp(Instant.now());
                                    List<Component> BTNs = new ArrayList<>();
                                    if (I.getPermanentInviteLink() != null) BTNs.add(Button.link(I.getPermanentInviteLink(), TLG(I,"Join")));
                                    if (I.getTwitterURL() != null) BTNs.add(Button.link(I.getTwitterURL(), "X"));
                                    if (I.getWebsiteURL() != null) BTNs.add(Button.link(I.getWebsiteURL(), "Website"));
                                    if (I.getTwitchURL() != null) BTNs.add(Button.link(I.getTwitchURL(), "Twitch"));
                                    if (I.getTiktokURL() != null) BTNs.add(Button.link(I.getTiktokURL(), "Tiktok"));
                                    if (I.getYouTubeURL() != null && BTNs.size() < 5) BTNs.add(Button.link(I.getYouTubeURL(), "YouTube"));
                                    client.send(I.getServerWebhook().addEmbeds(E.build()).addComponents(Utility.parseComponentBuilder(BTNs)).build());
                                });
                            } else {
                                new ServerInfo(G).Channels().setTournamentUpdatesChannel(null);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }
    public void LogTournamentResult(boolean force) {
        if (force || (I.isPublic && I.getGuild().getMemberCount() > 30 && (getSignupCap() >= 16 || getSignupCap() <= 0))) {
            if (isComplete()) {
                for (DatabaseObject.Row TR : getTournamentUpdatesChannels()) {
                    try {
                        Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                        if (G == null) continue;
                        TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                        if (C == null) continue;
                        if (TR.getAsBoolean("isPublic")) {
                            if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                                getWebhookOfChannel(C, client -> {
                                    String description = "# :trophy: " + TLG(G, "tournament-news-result") + "\n";
                                    description = description + "> " + TLG(G, "tournament-news-result-description", "**" + I.getFlag() + getName() + "**") + "\n";
                                    description = description + " > :first_place: **" + getParticipantByRank(1).getFirst().getName() + "**\n";
                                    description = description + " > :second_place: **" + getParticipantByRank(2).getFirst().getName() + "**\n";
                                    description = description + " > :third_place: **" + getParticipantByRank(3).stream().map(SChallonge_Participant::getName).collect(Collectors.joining(" / ")) + "**\n";
                                    description = description + " > **" + BotEmoji.get("Members").getFormatted() + " " + TLG(G, getVSAmount() > 1 ? "Teams" : "Participants") + ": " + getParticipantCount() + "**\n";
                                    description = description + " > - **[" + TLG(G, "View_Bracket") + "](" + getFullChallongeURL() + ")**\n";
                                    description = description + "`                                                       `\n";

                                    WebhookEmbedBuilder E = new WebhookEmbedBuilder();
                                    E.setDescription(description);
                                    E.setThumbnailUrl(getGame().getImageUrl());
                                    E.setColor(I.getColor().getRGB());
                                    E.setFooter(new WebhookEmbed.EmbedFooter(I.getGuild().getName() + " | " + I.getGuild().getMemberCount() + " " + TLG(G, "Members"), I.getGuild().getIconUrl()));
                                    E.setTimestamp(Instant.now());
                                    List<Component> BTNs = new ArrayList<>();
                                    if (I.getPermanentInviteLink() != null) BTNs.add(Button.link(I.getPermanentInviteLink(), TLG(I,"Join")));
                                    if (I.getTwitterURL() != null) BTNs.add(Button.link(I.getTwitterURL(), "X"));
                                    if (I.getWebsiteURL() != null) BTNs.add(Button.link(I.getWebsiteURL(), "Website"));
                                    if (I.getTwitchURL() != null) BTNs.add(Button.link(I.getTwitchURL(), "Twitch"));
                                    if (I.getTiktokURL() != null) BTNs.add(Button.link(I.getTiktokURL(), "Tiktok"));
                                    if (I.getYouTubeURL() != null && BTNs.size() < 5) BTNs.add(Button.link(I.getYouTubeURL(), "YouTube"));
                                    client.send(I.getServerWebhook().addEmbeds(E.build()).addComponents(Utility.parseComponentBuilder(BTNs)).build());
                                });
                            } else {
                                ServerInfo I = ServerInfo.get(G);
                                I.Channels().setTournamentUpdatesChannel(null);
                                I.Channels().Update();
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }


    public SChallonge_Tournament() {}
    public SChallonge_Tournament(ServerInfo I, Tournament T) throws Exception {
        super();
        this.I = I;
        this.T = T;
        this.ID = T.getId();
        this.URL = T.getUrl();
        this.ServerID = I.getId();
        ResyncChallonge();
        Upsert();
    }


    @Override
    public SChallonge_Participant createParticipant(Participant P, Long discordID) {
        return new SChallonge_Participant(this, P, discordID);
    }
    @Override
    public SChallonge_Match createMatch(Match M) {
        return new SChallonge_Match(this, M);
    }

    public static List<SChallonge_Tournament> find(String like) {
        return getAllWhere(SChallonge_Tournament.class, "Name LIKE ? OR Description LIKE ? OR FullChallongeURL LIKE ? OR State LIKE ? ORDER BY ID DESC", "%" + like + "%", "%" + like + "%", "%" + like + "%", "%" + like + "%");
    }
    public static SChallonge_Tournament get(long id) {
        return getById(SChallonge_Tournament.class, id).orElse(null);
    }
    public static List<SChallonge_Tournament> list(boolean withServ) {
        List<SChallonge_Tournament> T = getAllWhere(SChallonge_Tournament.class, "TRUE ORDER BY ID DESC");
        if (withServ) for (SChallonge_Tournament CT : T) CT.I = ServerInfo.get(CT.ServerID);
        return T;
    }
    public static List<SChallonge_Tournament> getActiveChallonges(boolean withServ) {
        List<SChallonge_Tournament> T = getAllWhere(SChallonge_Tournament.class,"NOT State = ? AND StartAtTimeEpochSecond > ? ORDER BY StartAtTimeEpochSecond DESC", TournamentState.COMPLETE.toString(), Instant.now().minus(90, ChronoUnit.DAYS).getEpochSecond());
        if (withServ) for (SChallonge_Tournament CT : T) CT.I = ServerInfo.get(CT.ServerID);
        return T;
    }
    public static List<SChallonge_Tournament> getPendingChallonges(boolean withServ) {
        List<SChallonge_Tournament> T = getAllWhere(SChallonge_Tournament.class,"State = ? AND isPublic AND StartAtTimeEpochSecond > ? ORDER BY StartAtTimeEpochSecond DESC", TournamentState.PENDING.toString(), Instant.now().minus(90, ChronoUnit.DAYS).getEpochSecond());
        if (withServ) for (SChallonge_Tournament CT : T) CT.I = ServerInfo.get(CT.ServerID);
        return T;
    }

    public static SChallonge_Tournament get(ServerInfo I, long id) {
        SChallonge_Tournament T = getById(SChallonge_Tournament.class, id).orElse(null);
        if (T == null) return null;
        T.I = I;
        return T;
    }
    public static List<SChallonge_Tournament> ofServer(long serverid) {
        return getAllWhere(SChallonge_Tournament.class, "ServerID = ? ORDER BY ID DESC", serverid);
    }
    public static List<SChallonge_Tournament> ofServer(long serverid, String games) {
        return getAllWhere(SChallonge_Tournament.class, "ServerID = ? AND (? IS NULL OR FIND_IN_SET(GameCode,?) > 0) ORDER BY ID DESC", serverid, games, games);
    }
    public static List<SChallonge_Tournament> ofServer(ServerInfo I) {
        List<SChallonge_Tournament> T = ofServer(I.getId());
        for (SChallonge_Tournament CT : T) CT.I = I;
        return T;
    }

    @Override
    public EmbedBuilder getTournamentEmbed() {
        EmbedBuilder E = new EmbedBuilder();
        E.setAuthor(getName(), !isPending() ? getFullChallongeURL() : null, ChallongeLogoURL);
        E.setColor(I.getColor());
        E.setThumbnail(getGame().getImageUrl());
        E.setFooter("• " + I.getGuild().getName(), I.getGuild().getIconUrl());
        return E;
    }

    @Override
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
            E.addField(TL(M,"Channels") + ", " + TL(M,"Roles") + " & " + TL(M,"Other") + " ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                    "Panel Channel: " + (getPanelChannel().getChannel() != null ? getPanelChannel().getChannel().getAsMention() : "`N/A`") + "\n" +
                            "Match Result: " + (getMatchResultChannel() != null ? getMatchResultChannel().getAsMention() : "`N/A`") + "\n" +
                            "Prediction: " + (getPredictionChannel() != null ? getPredictionChannel().getAsMention() : "`N/A`") + "\n" +
                            "Tournament Organiser: " + (getOrganizerRole() != null ? getOrganizerRole().getAsMention() : "`N/A`") + "\n" +
                            "Tournament Participant: " + (getParticipantRole() != null ? getParticipantRole().getAsMention() : "`N/A`") + "\n" +
                            "Start Time: " + (getStartAtTimeEpochSecond() != 0 ? " <t:" + getStartAtTimeEpochSecond() + ":R>" : "`" + TL(M,"None") + "`") + "\n" +
                            "Invite Link: " + (getInscriptionChannelInviteLink() != null ? getInscriptionChannelInviteLink() : "`N/A`") + "\n" +
                            "Signup Cap: " + (getSignupCap() != 0 ? getSignupCap() : "`" + TL(M,"None")  + "`") + "\n", false);
            E.setDescription(TL(M, "tournament-manage-description") + "\n" + TL(M, "tournament-bracket-desc-1", getFullChallongeURL()));
            M.editOriginalEmbeds(E.build()).setComponents(rows).queue();
        } else {
            M.editOriginal(TL(M,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).queue();
        }
    }

    public void TournamentChannelsSelectMenu(InteractionHook M, ChallongeCommand CMD) {
        EmbedBuilder E = getTournamentEmbed();
        E.setTitle(TL(M,"tournament-manager"));
        E.setDescription(TL(M, "tournament-manager-description"));
        E.addField(TL(M,"Channels"),
                "Panel: " + (getPanelChannel().getChannel() != null ? getPanelChannel().getChannel().getAsMention() : "`N/A`") + "\n" +
                        "Rules: " + (getRulesChannel() != null ? getRulesChannel().getAsMention() : "`N/A`") + "\n" +
                        "Match Result: " + (getMatchResultChannel() != null ? getMatchResultChannel().getAsMention() : "`N/A`") + "\n" +
                        "Prediction: " + (getPredictionChannel() != null ? getPredictionChannel().getAsMention() : "`N/A`")
                ,false);
        EntitySelectMenu.Builder Panel = EntitySelectMenu.create(CMD.Command("challonge-select-panel-channel"), EntitySelectMenu.SelectTarget.CHANNEL)
                .setPlaceholder("Control Panel...")
                .setChannelTypes(ChannelType.TEXT)
                .setRequiredRange(1, 1);
        EntitySelectMenu.Builder Result = EntitySelectMenu.create(CMD.Command("challonge-select-result-channel"), EntitySelectMenu.SelectTarget.CHANNEL)
                .setPlaceholder("Match Results...")
                .setChannelTypes(ChannelType.TEXT)
                .setRequiredRange(1, 1);
        EntitySelectMenu.Builder Rules = EntitySelectMenu.create(CMD.Command("challonge-select-rules-channel"), EntitySelectMenu.SelectTarget.CHANNEL)
                .setPlaceholder("Rules...")
                .setChannelTypes(ChannelType.TEXT)
                .setRequiredRange(1, 1);
        EntitySelectMenu.Builder Prediction = EntitySelectMenu.create(CMD.Command("challonge-select-prediction-channel"), EntitySelectMenu.SelectTarget.CHANNEL)
                .setPlaceholder("Prediction...")
                .setChannelTypes(ChannelType.TEXT)
                .setRequiredRange(1, 1);
        if (getPanelChannel().getChannel() != null) Panel.setDefaultValues(EntitySelectMenu.DefaultValue.channel(getPanelChannel().getChannel().getId()));
        if (getMatchResultChannel() != null) Result.setDefaultValues(EntitySelectMenu.DefaultValue.channel(getMatchResultChannel().getId()));
        if (getRulesChannel() != null) Rules.setDefaultValues(EntitySelectMenu.DefaultValue.channel(getRulesChannel().getId()));
        if (getPredictionChannel() != null) Prediction.setDefaultValues(EntitySelectMenu.DefaultValue.channel(getPredictionChannel().getId()));
        M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(Panel.build()), ActionRow.of(Result.build()), ActionRow.of(Rules.build()), ActionRow.of(Prediction.build())).queue();
    }
    public void TournamentRolesSelectMenu(InteractionHook M, ChallongeCommand CMD) {
        EmbedBuilder E = getTournamentEmbed();
        E.setTitle(TL(M,"tournament-manager"));
        E.setDescription(TL(M, "tournament-manager-description"));
        E.addField(TL(M,"Roles"),
                "Tournament Organiser: " + (getOrganizerRole() != null ? getOrganizerRole().getAsMention() : "`N/A`") + "\n" +
                        "Tournament Participant: " + (getParticipantRole() != null ? getParticipantRole().getAsMention() : "`N/A`")
                ,false);
        EntitySelectMenu.Builder Organizer = EntitySelectMenu.create(CMD.Command("challonge-select-organiser-role"), EntitySelectMenu.SelectTarget.ROLE)
                .setPlaceholder("Organiser Role...")
                .setRequiredRange(1, 1);
        EntitySelectMenu.Builder Participant = EntitySelectMenu.create(CMD.Command("challonge-select-participant-role"), EntitySelectMenu.SelectTarget.ROLE)
                .setPlaceholder("Participant Role...")
                .setRequiredRange(1, 1);
        if (getOrganizerRole() != null) Organizer.setDefaultValues(EntitySelectMenu.DefaultValue.role(getOrganizerRole().getId()));
        if (getParticipantRole() != null) Participant.setDefaultValues(EntitySelectMenu.DefaultValue.role(getParticipantRole().getId()));
        M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(Organizer.build()), ActionRow.of(Participant.build())).queue();
    }

}