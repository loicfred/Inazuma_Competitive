package iecompbot.objects.server.tournament.challonge.global;

import at.stefangeyer.challonge.model.Match;
import at.stefangeyer.challonge.model.Participant;
import at.stefangeyer.challonge.model.Tournament;
import at.stefangeyer.challonge.model.enumeration.TournamentState;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.Utility;
import iecompbot.interaction.cmdbreakdown.IDCommand;
import iecompbot.interaction.cmdbreakdown.server.ChallongeCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.BaseCTournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.Constants.ChallongeLogoURL;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.Utility.replaceLast;
import static iecompbot.interaction.Automation.*;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.server.ServerInfo.getTournamentUpdatesChannels;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class GChallonge_Tournament extends BaseCTournament<GChallonge_Tournament, GChallonge_Match, GChallonge_Participant> {
    protected transient List<GChallonge_Participant> Participants;
    protected transient List<GChallonge_Match> Matches;

    @Override
    public List<GChallonge_Match> getMatches() {
        if (Matches == null) {
            Matches = GChallonge_Match.get(this);
            for (GChallonge_Match P : Matches) P.T = this;
        }
        return Matches;
    }

    @Override
    public List<GChallonge_Participant> getParticipants() {
        if (Participants == null) {
            Participants = GChallonge_Participant.ofTournament(this);
            for (GChallonge_Participant P : Participants) P.T = this;
        }
        return Participants;
    }


    @Override
    public void AddParticipantFromInscription(InteractionHook M, User u, boolean force) {
        if (getVSAmount() == 1) {
            EmbedBuilder E = getTournamentEmbed();
            try {
                if (isRegistrationOpen() || force) {
                    if (u != null) {
                        if (!u.isBot() && getParticipantById(Profile.get(u).getID()) == null) {
                                if (isAccountOldEnough(u) || force) {
                                    if (getSignupCap() > getParticipantCount() || getSignupCap() == 0) {
                                        AddParticipant(u);
                                        if (M != null) {
                                            E.setDescription(":white_check_mark: " + TL(M, "tournament-register-success", "**" + getName() + "**"));
                                            M.editOriginalEmbeds(E.build()).queue();
                                        }
                                    } else if (getWaitingList().stream().noneMatch(W -> W.getUserId() == u.getIdLong())) {
                                        AddToWaitingList(u.getIdLong());
                                        if (M != null) {
                                            E.setDescription(":white_check_mark: " + TL(M, "tournament-waiting-list-success", "**" + u.getEffectiveName() + "**", "**" + getName() + "**"));
                                            M.editOriginalEmbeds(E.build()).queue();
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
                if (getVSAmount() > 1) for (GChallonge_Participant p : getParticipants()) {
                    if (p.RemoveTeammate(u.getIdLong()) && p.getLeaderID() != u.getIdLong()) {
                        E.setDescription(":white_check_mark: " + TL(M, "tournament-unregister-success", "**" + getName() + "**"));
                        M.editOriginalEmbeds(E.build()).queue();
                        sendPrivateMessage(p.getLeader(), TL(M, "clan-leave-confirm-success", "**" + u.getEffectiveName() + "**", "**" + p.getName() + "**"));
                        break;
                    }
                }

                GChallonge_Participant P = getParticipantById(u.getIdLong());
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
                            AddParticipant(u);
                        } else break;
                    }
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
                GChallonge_Participant MyTeam = getParticipantById(Profile.get(u).getID());
                if (isRegistrationOpen() || MyTeam != null) {
                    if (!u.isBot() && getParticipants().stream().noneMatch(PP -> PP.getLeaderID() != u.getIdLong() && PP.getAllTeammatesIDs().contains(u.getIdLong()))) {
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
                        E.setDescription(":x: " + TL(M, "tournament-register-fail", "**" + getName() + "**"));
                        M.editOriginalEmbeds(E.build()).queue();
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
                if (teammates.stream().allMatch(u -> u != null && !u.isBot())) {
                    if (teammates.stream().allMatch(TT -> getParticipants().stream().noneMatch(PP -> PP.getAllTeammatesIDs().contains(TT.getIdLong())))) {
                        if (teammates.stream().allMatch(this::isAccountOldEnough)) {
                            GChallonge_Participant MyTeam = getParticipantById(Profile.get(M.getInteraction().getUser().getIdLong()).getID());
                            if (getSignupCap() > getParticipantCount() || getSignupCap() == 0 || MyTeam != null) {
                                if (MyTeam == null) {
                                    MyTeam = AddParticipant(M.getInteraction().getUser());
                                    MyTeam.setName(M.getInteraction().getUser().getEffectiveName() + "'s Team");
                                }
                                IDCommand CMD2 = new IDCommand(MyTeam.getId());
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
                }
            } catch (Exception e) {
                replyException(M, e);
            }
        }
    }

    @Override
    public void CleanTournament() throws Exception {
        for (GChallonge_Participant P : new ArrayList<>(getParticipants())) {
            ClearDuplicateParticipants(P);
        }
        validateAllTeamsScores();
        Participants = null;
    }

    public void LogTournamentRegistration() {
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
                                String description = "# " + BotEmoji.get("icon_fan") + TLG(G, "tournament-gb-news-announcement");
                                description = description + "\n> " + TLG(G, "tournament-gb-news-announcement-description", "**" + getName() + "**");

                                description = description + "\n> " + TLG(G, "Starting") + ": **<t:" + getStartAtTimeEpochSecond() + ":R> (<t:" + getStartAtTimeEpochSecond() + ":d>)**";
                                if (getSignupCap() > 3) description = description + " | Max " + TLG(G, "Participants") + ": **" + getSignupCap() + "**";
                                description = description + "\n> " + TLG(G, "Type") + ": **" + getType().name().replace("_", " ") + " ~ " + getVSAmount() + "v" + getVSAmount() + "**";

                                description = description + "\n`                                                       `\n";

                                WebhookEmbedBuilder E = new WebhookEmbedBuilder();
                                E.setDescription(description);
                                E.setThumbnailUrl(getGame().getImageUrl());
                                E.setColor(Color.ORANGE.getRGB());
                                E.setFooter(new WebhookEmbed.EmbedFooter("Inazuma Competitive", DiscordAccount.getSelfUser().getEffectiveAvatarUrl()));
                                E.setTimestamp(Instant.now());
                                List<Component> BTNs = new ArrayList<>();
                                IDCommand CMD = new IDCommand(getId());
                                BTNs.add(Button.success(CMD.Command("challonge-register"), TLG(G, "Register")).withEmoji(Emoji.fromUnicode("U+2705")));
                                BTNs.add(Button.secondary(CMD.Command("challonge-unregister"), TLG(G, "Unregister")).withEmoji(Emoji.fromUnicode("U+274C")));
                                BTNs.add(Button.secondary(CMD.Command("challonge-info"), TLG(G, "Information")).withEmoji(Emoji.fromUnicode("U+1F4D8")));
                                BTNs.add(Button.secondary(CMD.Command("challonge-rules"), TLG(G, "Read-the-rules")).withEmoji(Emoji.fromUnicode("U+1F4D5")));
                                client.send(getInazumaWebhook().addEmbeds(E.build()).addComponents(Utility.parseComponentBuilder(BTNs)).build());
                            });
                        } else {
                            new ServerInfo(G).Channels().setTournamentUpdatesChannel(null);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }
    public void LogTournamentResult(boolean force) {
        if (force || isComplete()) {
            for (DatabaseObject.Row TR : getTournamentUpdatesChannels()) {
                try {
                    Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                    if (G == null) continue;
                    TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                    if (C == null) continue;
                    if (TR.getAsBoolean("isPublic")) {
                        if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                            getWebhookOfChannel(C, client -> {
                                String description = "# :trophy: " + TLG(G, "tournament-gb-news-result") + "\n";
                                description = description + "> " + TLG(G, "tournament-gb-news-result-description", "**" + getName() + "**") + "\n";
                                description = description + " > :first_place: **" + getParticipantByRank(1).getFirst().getLeader().getEffectiveName() + "**\n";
                                description = description + " > :second_place: **" + getParticipantByRank(2).getFirst().getLeader().getEffectiveName() + "**\n";
                                description = description + " > :third_place: **" + getParticipantByRank(3).stream().map((GChallonge_Participant P) -> P.getLeader().getEffectiveName()).collect(Collectors.joining(" / ")) + "**\n";
                                description = description + " > **" + BotEmoji.get("Members").getFormatted() + " " + TLG(G, getVSAmount() > 1 ? "Teams" : "Participants") + ": " + getParticipantCount() + "**\n";
                                description = description + " > - **[" + TLG(G, "View_Bracket") + "](" + getFullChallongeURL() + ")**\n";
                                description = description + "`                                                       `\n";

                                WebhookEmbedBuilder E = new WebhookEmbedBuilder();
                                E.setDescription(description);
                                E.setThumbnailUrl(getGame().getImageUrl());
                                E.setColor(Color.ORANGE.getRGB());
                                E.setFooter(new WebhookEmbed.EmbedFooter("Inazuma Competitive", DiscordAccount.getSelfUser().getEffectiveAvatarUrl()));
                                E.setTimestamp(Instant.now());
                                client.send(getInazumaWebhook().addEmbeds(E.build()).build());
                            });
                        } else {
                            new ServerInfo(G).Channels().setTournamentUpdatesChannel(null);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }
    public WebhookMessageBuilder getInazumaWebhook() {
        return new WebhookMessageBuilder().setUsername(getName()).setAvatarUrl(DiscordAccount.getSelfUser().getAvatarUrl());
    }

    public GChallonge_Tournament() {}
    public GChallonge_Tournament(Tournament T) throws Exception {
        super();
        this.T = T;
        this.ID = T.getId();
        this.URL = T.getUrl();
        ResyncChallonge();
        Write();
    }


    @Override
    public GChallonge_Participant createParticipant(Participant P, Long discordID) {
        return new GChallonge_Participant(this, P, discordID);
    }
    @Override
    public GChallonge_Match createMatch(Match M) {
        return new GChallonge_Match(this, M);
    }

    public static List<GChallonge_Tournament> find(String like) {
        return getAllWhere(GChallonge_Tournament.class, "Name LIKE ? OR Description LIKE ? OR FullChallongeURL LIKE ? OR State LIKE ? ORDER BY ID DESC", "%" + like + "%", "%" + like + "%", "%" + like + "%", "%" + like + "%");
    }
    public static GChallonge_Tournament get(long id) {
        return getById(GChallonge_Tournament.class, id).orElse(null);
    }
    public static List<GChallonge_Tournament> list() {
        return getAllWhere(GChallonge_Tournament.class, "ORDER BY ID DESC");
    }
    public static GChallonge_Tournament getActiveChallonge() {
        return getWhere(GChallonge_Tournament.class, "ServerID IS NULL AND NOT State = ? AND StartAtTimeEpochSecond > ? ORDER BY StartAtTimeEpochSecond DESC", TournamentState.COMPLETE.toString(), Instant.now().minus(90, ChronoUnit.DAYS).getEpochSecond()).orElse(null);
    }

    @Override
    public EmbedBuilder getTournamentEmbed() {
        EmbedBuilder E = new EmbedBuilder();
        E.setAuthor(getName(), !isPending() ? getFullChallongeURL() : null, ChallongeLogoURL);
        E.setColor(Color.ORANGE);
        E.setThumbnail(getGame().getImageUrl());
        E.setFooter("• " + getName(), ChallongeLogoURL);
        return E;
    }

}
