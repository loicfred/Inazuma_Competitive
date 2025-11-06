package iecompbot.interaction.listeners;

import at.stefangeyer.challonge.model.enumeration.MatchState;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import iecompbot.img.builders.DualPictureBuilder;
import iecompbot.img.builders.MatchResultImageBuilder;
import iecompbot.img.builders.PredictionImageBuilder;
import iecompbot.interaction.cmdbreakdown.MatchmakingCommand;
import iecompbot.interaction.cmdbreakdown.ScoreCommand;
import iecompbot.interaction.cmdbreakdown.clan.BetCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.interclan.Interclan;
import iecompbot.objects.clan.interclan.Interclan_Duel;
import iecompbot.objects.match.*;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.profile_game.BasePG;
import iecompbot.objects.profile.profile_game.Profile_Game;
import iecompbot.objects.server.Leaderboard;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.SubMatch;
import iecompbot.objects.server.tournament.challonge.BaseCMatch;
import iecompbot.objects.server.tournament.challonge.BaseCParticipant;
import iecompbot.objects.server.tournament.challonge.BaseCTournament;
import iecompbot.objects.server.tournament.challonge.global.GChallonge_Tournament;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Match;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static iecompbot.Constants.BotOwnerID;
import static iecompbot.Constants.ChallongeLogoURL;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.img.ImgUtilities.mixColors;
import static iecompbot.interaction.Automation.*;
import static iecompbot.interaction.listeners.AdminFeatures.MatchmakingSetupMenu;
import static iecompbot.objects.BotManagers.isScoreBan;
import static iecompbot.objects.BotManagers.isTournamentManager;
import static iecompbot.objects.Retrieval.AddTemporaryUserEmoji;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.clan.Clan.getClanOfUser;
import static iecompbot.springboot.data.DatabaseObject.doQuery;
import static my.utilities.util.Utilities.*;

public class MatchFeatures extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    switch (event.getName()) {
                        case "host" -> {
                            event.deferReply().queue(M -> {
                                slashHost(M, event.getOption("p1").getAsUser(), event.getOption("p2").getAsUser());
                            });
                        }
                        case "matchmaking" -> {
                            event.deferReply(true).queue(M -> {
                                try {
                                    boolean isAnonymous = event.getOption("is-anonymous") != null && event.getOption("is-anonymous").getAsBoolean();
                                    String rule = event.getOption("rule") != null ? event.getOption("rule").getAsString() : null;
                                    boolean isGlobal = event.getOption("is-global") == null || event.getOption("is-global").getAsBoolean();
                                    slashMatchmaking(M, event.getUser(), Game.get(event.getOption("game").getAsString()), isAnonymous, rule, isGlobal);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        case "score" -> {
                            event.deferReply(true).queue(M -> {
                                if (event.isFromGuild()) {
                                    try {
                                        User winneroftie = event.getOption("winner-of-tie") != null ? event.getOption("winner-of-tie").getAsUser() : null;
                                        Message.Attachment proof = event.getOption("proof") != null ? event.getOption("proof").getAsAttachment() : null;
                                        createScoreReport(M, event.getUser().getIdLong(), event.getOption("opponent").getAsUser().getIdLong(),
                                                event.getOption("myscore").getAsInt(), event.getOption("opponentscore").getAsInt(),
                                                Game.get(event.getOption("game").getAsString()),
                                                winneroftie, event.getChannel().asTextChannel(), proof);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                } else {
                                    M.editOriginal(TL(event, "reply-failed-not-in-guild")).queue();
                                }
                            });
                        }
                        case "revert-score" -> {
                            event.deferReply().queue(M -> {
                                if (event.isFromGuild()) {
                                    try {
                                        slashRevertScore(M, event.getUser(), event.getOption("opponent").getAsUser(),
                                                event.getOption("myscore").getAsInt(), event.getOption("opponentscore").getAsInt(),
                                                Game.get(event.getOption("game").getAsString()));
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                } else {
                                    M.editOriginal(TL(event, "reply-failed-not-in-guild")).queue();
                                }
                            });
                        }
                        case "prediction" -> {
                            event.deferReply(true).queue(M -> {
                                if (event.isFromGuild()) {
                                    try {
                                        MakePrediction(M.getInteraction().getGuild(), event.getOption("p1").getAsUser(), event.getOption("p2").getAsUser(), event.getChannel().asTextChannel());
                                        M.deleteOriginal().queue();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                } else {
                                    M.editOriginal(TL(event, "reply-failed-not-in-guild")).queue();
                                }
                            });
                        }
                        case "bet" -> {
                            event.deferReply().queue(M -> {
                                if (event.isFromGuild()) {
                                    try {
                                        slashBet(M, event.getOption("user").getAsUser(), event.getOption("user-2").getAsUser(),
                                                event.getOption("minute").getAsInt(), Game.get(event.getOption("game").getAsString()));
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                } else {
                                    M.editOriginal(TL(event, "reply-failed-not-in-guild")).queue();
                                }
                            });
                        }
                        case "league" -> {
                            event.deferReply().queue(M -> {
                                try {
                                    slashLeague(M, Profile.get(event.getUser()));
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        case "active-tourney" -> {
                            event.deferReply().queue(M -> {
                                Game game = event.getOption("game") != null ? Game.get(event.getOption("game").getAsString()) : null;
                                slashActiveTourney(M, game);
                            });
                        }
                        case "leaderboard" -> {
                            event.deferReply().queue(M -> {
                                Leaderboard LB = new Leaderboard(Clan.list(), Game.get(event.getOption("game").getAsString()), event.getOption("page") == null ? 1 : event.getOption("page").getAsInt(), 30);
                                M.editOriginalEmbeds(LB.getEmbeds(M).stream().map(EmbedBuilder::build).toList()).queue();
                            });
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("match") || event.getComponentId().startsWith("bet")) {
                        if (event.getComponentId().startsWith("bet")) {
                            BetCommand CMD = new BetCommand(event.getComponentId());
                            Bet B = Bet.get(CMD.ID);
                            if (event.getComponentId().contains("bet-vote-p1")) {
                                if (Instant.now().isBefore(B.getEndTime())) {
                                    Bet_Voter me = Bet_Voter.get(B.getId(), event.getUser().getIdLong());
                                    if (me == null || me.getCandidateID() == B.P1ID) {
                                        Label input = Label.of("InaCoin", TextInput.create("vote", TextInputStyle.SHORT)
                                                .setPlaceholder("100   (Your Coins -> " + Profile.get(event.getUser()).getItem(1).Amount + ")")
                                                .setMinLength(1)
                                                .setMaxLength(5)
                                                .build());
                                        event.replyModal(Modal.create("bet-vote-p1", "Betting on " + getUserByID(B.P1ID).getEffectiveName())
                                                .addComponents(input)
                                                .build()).queue();
                                    } else {
                                        event.reply(TL(event, "bet-fail-2")).setEphemeral(true).queue();
                                    }
                                } else {
                                    event.reply(TL(event, "bet-time-end")).setEphemeral(true).queue();
                                }
                            }
                            else if (event.getComponentId().contains("bet-vote-p2")) {
                                if (Instant.now().isBefore(B.getEndTime())) {
                                    Bet_Voter me = Bet_Voter.get(B.getId(), event.getUser().getIdLong());
                                    if (me == null || me.getCandidateID() == B.P2ID) {
                                        Label input = Label.of("InaCoin", TextInput.create("vote", TextInputStyle.SHORT)
                                                .setPlaceholder("100   (Your Coins -> " + Profile.get(event.getUser()).getItem(1).Amount + ")")
                                                .setMinLength(1)
                                                .setMaxLength(5)
                                                .build());
                                        event.replyModal(Modal.create("bet-vote-p2", "Betting on " + getUserByID(B.P2ID).getEffectiveName())
                                                .addComponents(input).build()).queue();
                                    } else {
                                        event.reply(TL(event, "bet-fail-2")).setEphemeral(true).queue();
                                    }
                                } else {
                                    event.reply(TL(event, "bet-time-end")).setEphemeral(true).queue();
                                }
                            }
                            else if (event.getComponentId().contains("bet-info")) {
                                event.deferReply(true).queue(M -> {
                                    EmbedBuilder Embed = new EmbedBuilder();
                                    Embed.setColor(Color.ORANGE);
                                    Embed.setAuthor(TLG(event.getGuild(), "match-betting"), null, B.getGame().getImageUrl());
                                    Bet_Voter Me = Bet_Voter.get(B.getId(), event.getUser().getIdLong());
                                    if (Me != null) Embed.setDescription(TL(M, "bet-info-description") + "\n\n" +
                                            TL(M, "bet-info", Item.get("InaCoin").getEmojiFormatted() + "**" + Me.getAmount() + "**", "**" + B.countCoins(Me.getCandidateID()) + "**",
                                                    "**" + B.countPercentage(Me) + "**", "**" + getUserByID(Me.getCandidateID()).getEffectiveName() + "**"));
                                    else Embed.setDescription(TL(M, "bet-info-description"));

                                    M.editOriginalEmbeds(Embed.build()).queue();
                                });
                            }

                            else if (event.getComponentId().contains("bet-time-add")) {
                                if (isTournamentManager(event.getUser())) {
                                    Label input = Label.of("Minute(s)", TextInput.create("min", TextInputStyle.SHORT)
                                            .setPlaceholder("60").setMinLength(1).setMaxLength(5).build());
                                    event.replyModal(Modal.create("bet-time-add", "Adding time")
                                            .addComponents(input).build()).queue();
                                } else {
                                    event.reply("No perm for this.").setEphemeral(true).queue();
                                }
                            }
                            else if (event.getComponentId().contains("bet-time-less")) {
                                if (isTournamentManager(event.getUser())) {
                                    Label input = Label.of("Minute(s)", TextInput.create("min", TextInputStyle.SHORT)
                                            .setPlaceholder("60")
                                            .setMinLength(1)
                                            .setMaxLength(5)
                                            .build());
                                    event.replyModal(Modal.create("bet-time-less", "Adding time")
                                            .addComponents(input).build()).queue();
                                } else {
                                    event.reply("No perm for this.").setEphemeral(true).queue();
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("match-rslt")) {
                            if (event.getComponentId().startsWith("match-rslt-conf")) {
                                ServerInfo I = ServerInfo.get(event.getGuild());
                                MatchLog R1 = MatchLog.getByMessage(event.getMessageIdLong());
                                MatchLog_S R2 = MatchLog_S.getByMessage(event.getMessageIdLong());
                                if (R1 != null) {
                                    BaseCTournament<?,?,?> T = I == null ?
                                            doQuery(GChallonge_Tournament.class, """
                                                    SELECT T.* FROM inazuma_competitive.challonge_tournament T
                                                    JOIN inazuma_competitive.challonge_match M ON M.TournamentID = T.ID
                                                    WHERE M.MatchLogID = ?""", R1.getId()).orElse(null)
                                            : doQuery(SChallonge_Tournament.class, """
                                                    SELECT T.* FROM inazuma_competitive.challonge_tournament T
                                                    JOIN inazuma_competitive.challonge_match M ON M.TournamentID = T.ID
                                                    WHERE M.MatchLogID = ?""", R1.getId()).orElse(null);
                                    if (T instanceof SChallonge_Tournament CT) CT.I = I;
                                    boolean isRightUserAccepting = (event.getUser().getIdLong() == R1.getP2ID() && (I == null || !I.AdminAcceptOnly))
                                            || (I != null && I.AdminAcceptOnly && isAdmin(event.getMember()))
                                            || (T != null && T.isOrganiser(event.getUser()))
                                            || isTournamentManager(event.getUser());

                                    if (isRightUserAccepting) {
                                        event.deferEdit().setComponents(ActionRow.of(Button.secondary("nothing", TL(event, "Processing")).asDisabled())).queue(m -> {
                                            if (I != null) {
                                                processScore(m, I, R1, R2, T);
                                            } else {
                                                processScore(m, R1, T);
                                            }
                                        });
                                    } else if (event.getUser().getIdLong() == R1.getP1ID()) {
                                        event.reply(event.getUser().getEffectiveName() + ", " + TL(event, "result-confirm-fail-self-confirm")).setEphemeral(true).queue();
                                    } else if (I != null && I.AdminAcceptOnly) {
                                        event.reply("Only admins can accept here...").setEphemeral(true).queue();
                                    } else {
                                        event.reply(event.getUser().getEffectiveName() + ", " + TL(event, "result-confirm-fail-wrong-member")).setEphemeral(true).queue();
                                        LogSlash(I,TLG(event.getGuild(), "result-confirm-fail-wrong-member-log", event.getUser().getEffectiveName()) + " (" + getUserByID(R1.getP1ID()).getEffectiveName() + " vs " + getUserByID(R1.getP2ID()).getEffectiveName() + ")");
                                    }
                                }
                            }
                            else if (event.getComponentId().startsWith("match-rslt-deny")) {
                                if (event.getMessage().getMentions().getUsers().size() > 1) {
                                    MatchLog R = MatchLog.getByMessage(event.getMessageIdLong());
                                    MatchLog_S R2 = MatchLog_S.getByMessage(event.getMessageIdLong());
                                    if (R != null) {
                                        if (event.getUser().getIdLong() == R.getP1ID() || event.getUser().getIdLong() == R.getP2ID() || isTournamentManager(event.getUser())) {
                                            event.deferEdit().setContent(TL(event, "result-deny-success", event.getUser().getAsMention() + " (@" + event.getUser().getName() + ")")).setReplace(true).queue();
                                            Profile P1 = R.getProfileP1();
                                            P1.RefreshScoreTimeout(Instant.now());

                                            Profile P2 = R.getProfileP2();
                                            P2.RefreshScoreTimeout(Instant.now());

                                            R.Delete();
                                            if (R2 != null) R2.Delete();

                                            if (!event.isFromGuild()) sendPrivateMessage(R.getProfileP1().getUser(), TL(event, "result-deny-success", event.getUser().getAsMention() + " (@" + event.getUser().getName() + ")"));
                                            
                                            LogSlash(event.getGuild(),"**[Score]** Result of (" + P1.getUser().getEffectiveName() + " vs " + P2.getUser().getEffectiveName() + ")" + " has been cancelled.");
                                            LogSlash(event.getGuild(),P1.getUser().getEffectiveName() + " timer is reset !");
                                            LogSlash(event.getGuild(),P2.getUser().getEffectiveName() + " timer is reset !");
                                        } else {
                                            event.reply(TL(event, "result-deny-fail-wrong-member", R.getP2().getEffectiveName())).setEphemeral(true).queue();
                                            LogSlash(event.getGuild(), "**[Score]** " + TLG(event.getGuild(), "result-deny-fail-wrong-member-log", event.getUser().getEffectiveName()) + " (" + R.getP1().getEffectiveName() + " vs " + R.getP2().getEffectiveName() + ")");
                                        }
                                    } else {
                                        event.reply(TL(event, "result-deny-fail-not-found")).setEphemeral(true).queue();
                                        event.getMessage().delete().queue();
                                    }
                                } else {
                                    event.reply(TL(event, "result-deny-fail-not-found")).setEphemeral(true).queue();
                                    event.getMessage().delete().queue();
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("match-rev")) {
                            ScoreCommand CMD = new ScoreCommand(event.getComponentId());
                            if (event.getComponentId().startsWith("match-rev-rslt-conf")) {
                                MatchLog Log = MatchLog.getLog(CMD.P1ID, CMD.P2ID, CMD.P1Score, CMD.P2Score, CMD.Game);
                                MatchLog_S Log2 = MatchLog_S.getLog(CMD.P1ID, CMD.P2ID, CMD.P1Score, CMD.P2Score, CMD.Game);
                                if (Log != null) {
                                    if ((event.getUser().getIdLong() == CMD.P2ID && CMD.RequesterID == CMD.P1ID) || (event.getUser().getIdLong() == CMD.P1ID && CMD.RequesterID == CMD.P2ID) || isTournamentManager(event.getUser())) {
                                        event.deferEdit().setComponents(ActionRow.of(Button.secondary("nothing", TL(event, "Processing")).asDisabled())).queue(M -> {
                                            try {
                                                String p1name = getUserByID(CMD.P1ID).getEffectiveName();
                                                String p2name = getUserByID(CMD.P2ID).getEffectiveName();

                                                EmbedBuilder E = new EmbedBuilder();
                                                E.setColor(Log.getGame().getColor());
                                                E.setThumbnail(Log.getGame().getImageUrl());
                                                E.addField(p1name, BotEmoji.get("XP") + "**__" + -Log.getP1XP() + "__**\n" +
                                                        ":medal: **__" + -Log.getP1Medals() + "__**\n" +
                                                        BotEmoji.get("InaCoin") + " **__" + -Log.getP1Coins() + "__**", true);
                                                E.addField(p2name, BotEmoji.get("XP") + "**__" + -Log.getP2XP() + "__**\n" +
                                                        ":medal: **__" + -Log.getP2Medals() + "__**\n" +
                                                        BotEmoji.get("InaCoin") + " **__" + -Log.getP2Coins() + "__**", true);

                                                try {
                                                    ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
                                                    List<SChallonge_Tournament> TT = I.getTournamentsOfResultChannel(M.getInteraction().getChannelIdLong());
                                                    SChallonge_Tournament T = I.getTournamentOfMatchLog(Log.getId());
                                                    if (!TT.isEmpty()) {
                                                        if (TT.stream().anyMatch(This -> T != null && This.getId() == T.getId())) {
                                                            if (T.getMatchResultChannel().getIdLong() == M.getInteraction().getChannelIdLong()) {
                                                                SChallonge_Match CM = T.getMatchByLog(Log.getId());
                                                                if (!CM.isMatchUsedAsPrerequisite()) {
                                                                    CM.reopenMatch(CMD.P1ID, CMD.P2ID);
                                                                    E.addField(BotEmoji.get("Challonge").getFormatted() + " " + T.getName(), "**• " + p1name + " " + T.getGame().getVSEmojiFormatted() + " " + p2name + ":** REOPEN", false);
                                                                } else {
                                                                    M.editOriginal(TL(M, "tournament-revert-score-fail-too-late")).queue();
                                                                    return;
                                                                }
                                                            } else {
                                                                M.editOriginal(TL(M, "tournament-add-score-fail-wrong-channel")).queue();
                                                                return;
                                                            }
                                                        } else {
                                                            M.editOriginal(TL(M, "tournament-add-score-fail-no-opp")).queue();
                                                            return;
                                                        }
                                                    }
                                                } catch (Exception ignored) {}
                                                Log.removeRewards();
                                                Log.Delete();
                                                if (Log2 != null) {
                                                    Log2.removeRewards();
                                                    Log2.Delete();
                                                    E.addField(ServerInfo.get(Log2.getServerID()).getName(), "P1: " + -Log2.getP1Medals() + " :medal:\nP2: **" + -Log2.getP2Medals() + "** :medal:", false);
                                                }
                                                E.setDescription(TL(M, "result-reverted", "**" + p1name + "**", "**" + p2name + "**"));
                                                E.setTitle(p1name + " " + Log.getP1Score() + " " + Log.getGame().getVSEmojiFormatted() + " " + Log.getP2Score() + " " + p2name);
                                                M.editOriginalEmbeds(E.build()).setReplace(true).queue();
                                            } catch (Exception e) {
                                                replyException(M, e);
                                            }
                                        });
                                    } else if (event.getUser().getIdLong() == CMD.RequesterID) {
                                        event.reply(event.getUser().getEffectiveName() + ", " + TL(event, "result-confirm-fail-self-confirm")).setEphemeral(true).queue();
                                    } else {
                                        event.reply(event.getUser().getEffectiveName() + ", " + TL(event, "result-confirm-fail-wrong-member")).setEphemeral(true).queue();
                                    }
                                } else {
                                    event.reply(TL(event, "result-reverted-fail")).queue();
                                }
                            }
                            else if (event.getComponentId().equals("match-rev-rslt-deny")) {
                                if (event.getUser().getIdLong() == CMD.P1ID || event.getUser().getIdLong() == CMD.P2ID) {
                                    event.deferEdit().queue(M -> M.deleteOriginal().queue());
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("matchmaking")) {
                            if (event.getComponentId().contains("matchmaking-accept-")) {
                                MatchmakingRequest REQ = MatchmakingRequest.retrieveMatchmakingRequest(takeOnlyNumberStr(event.getComponentId()));
                                if (REQ != null && event.getUser() != REQ.getUser()) {
                                    event.deferEdit().setComponents(ActionRow.of(event.getButton().withDisabled(true))).queue();
                                    for (MatchmakingRequest.RequestData RD : REQ.MatchmakingRequests) {
                                        if (event.getGuild().getIdLong() == RD.ServerID) {
                                            RD.Accept(event.getUser());
                                        } else {
                                            RD.AcceptedElseWhere(event.getUser());
                                        }
                                    }
                                } else {
                                    event.reply(TL(event, "matchmaking-match-accept-fail")).setEphemeral(true).queue();
                                }
                            }
                            if (event.getComponentId().contains("matchmaking-cancel-")) {
                                User asker = getUserByID(event.getComponentId());
                                if (event.getUser() == asker || isTournamentManager(event.getUser())) {
                                    Profile P = Profile.get(event.getUser());
                                    event.deferReply(true).queue(M -> {
                                        MatchmakingRequest REQ = MatchmakingRequest.retrieveMatchmakingRequest(takeOnlyNumberStr(event.getComponentId()));
                                        if (REQ != null) for (MatchmakingRequest.RequestData RD : REQ.MatchmakingRequests) {
                                            try {
                                                RD.Canceled();
                                            } catch (Exception e) {
                                                replyException(M, e);
                                            }
                                        }
                                        if (Instant.now().isAfter(P.getMatchmakingTimeout())) {
                                            M.editOriginal(TL(event, "matchmaking-match-cancel-success-1")).queue();
                                        } else {
                                            M.editOriginal(TL(event, "matchmaking-match-cancel-success-2", "<t:" + P.getMatchmakingTimeout().getEpochSecond() + ":R> !")).queue();
                                        }
                                    });
                                } else {
                                    event.reply(TL(event, "matchmaking-match-cancel-fail")).setEphemeral(true).queue();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getModalId().startsWith("bet")) {
                        try {
                            BetCommand CMD = new BetCommand(event.getModalId());
                            Bet B = Bet.get(CMD.ID);
                            if (event.getModalId().startsWith("bet-vote")) {
                                if (event.getModalId().startsWith("bet-vote-p1")) {
                                    if (Instant.now().isBefore(B.getEndTime())) {
                                        Profile P = Profile.get(event.getUser().getIdLong());
                                        int amount = takeOnlyInts(event.getValues().getFirst().getAsString());
                                        if (P.getItem(1).Amount >= amount) {
                                            B.AddBet(event.getUser(), B.getP1ID(), amount);
                                        } else {
                                            event.reply(TL(event, "bet-fail")).setEphemeral(true).queue();
                                        }
                                    } else {
                                        event.reply(TL(event, "bet-time-end")).setEphemeral(true).queue();
                                    }
                                }
                                else if (event.getModalId().startsWith("bet-vote-p2")) {
                                    if (Instant.now().isBefore(B.getEndTime())) {
                                        Profile P = Profile.get(event.getUser().getIdLong());
                                        int amount = takeOnlyInts(event.getValues().getFirst().getAsString());
                                        if (P.getItem(1).Amount >= amount) {
                                            B.AddBet(event.getUser(), B.getP2ID(), amount);
                                        } else {
                                            event.reply(TL(event, "bet-fail")).setEphemeral(true).queue();
                                        }
                                    } else {
                                        event.reply(TL(event, "bet-time-end")).setEphemeral(true).queue();
                                    }
                                }
                            }
                            else if (event.getModalId().startsWith("bet-time-add")) {
                                if (isTournamentManager(event.getUser())) {
                                    if (Instant.now().isAfter(B.getEndTime())) {
                                        B.setEndTime(Instant.now().plus(takeOnlyInts(event.getValues().getFirst().getAsString()), ChronoUnit.MINUTES));
                                    } else {
                                        B.setEndTime(B.getEndTime().plus(takeOnlyInts(event.getValues().getFirst().getAsString()), ChronoUnit.MINUTES));
                                    }
                                }
                            }
                            else if (event.getModalId().startsWith("bet-time-less")) {
                                if (isTournamentManager(event.getUser())) {
                                    B.setEndTime(B.getEndTime().minus(takeOnlyInts(event.getValues().getFirst().getAsString()), ChronoUnit.MINUTES));
                                }
                            }
                            B.BetUI(CMD, event.getChannel().asTextChannel());
                        } catch (Exception e) {
                            replyException(event, e);
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        if (!event.isAcknowledged() && event.isFromGuild()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("match")) {
                        Mentions Interactions = event.getMentions();
                        ServerInfo I = ServerInfo.get(event.getGuild());
                        if (event.getComponentId().startsWith("matchmaking-select") && I != null) {
                            MatchmakingCommand CMD = new MatchmakingCommand(event.getComponentId(), event.getGuild());
                            event.deferEdit().queue(M -> {
                                try {
                                    if (isAdmin(M, event.getMember())) {
                                        if (event.getComponentId().startsWith("matchmaking-select-channel")) {
                                            if (hasPermissionInChannel(M, Interactions.getChannels().getFirst(), Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MANAGE_WEBHOOKS)) {
                                                CMD.Channel = Interactions.getChannels().getFirst();
                                                CMD.ChannelID = CMD.Channel.getIdLong();
                                                ServerInfo.ServerInfo_Matchmaking MM = I.getMatchmaking(CMD.Game);
                                                if (MM != null) {
                                                    MM.setChannelID(CMD.ChannelID);
                                                    MM.UpdateOnly("ChannelID");
                                                } else {
                                                    new ServerInfo.ServerInfo_Matchmaking(I, CMD.ChannelID, CMD.RoleID, CMD.Game);
                                                }
                                                MatchmakingSetupMenu(M, I, CMD);
                                            }
                                        }
                                        else if (event.getComponentId().startsWith("matchmaking-select-role")) {
                                            if (I.getMatchmakings().stream().filter(MM -> MM.getRoleID() != null).noneMatch(MM -> MM.getRoleID() == Interactions.getRoles().getFirst().getIdLong())) {
                                                CMD.Role = Interactions.getRoles().getFirst();
                                                CMD.RoleID = CMD.Role.getIdLong();
                                                ServerInfo.ServerInfo_Matchmaking MM = I.getMatchmaking(CMD.Game);
                                                if (MM != null) {
                                                    MM.setRoleID(CMD.RoleID);
                                                    MM.UpdateOnly("RoleID");
                                                } else {
                                                    new ServerInfo.ServerInfo_Matchmaking(I, CMD.ChannelID, CMD.RoleID, CMD.Game);
                                                }
                                                MatchmakingSetupMenu(M, I, CMD);
                                            } else {
                                                M.editOriginal(TL(event, "matchmaking-manage-fail")).queue();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }


    public static void AttributingChallongesToEmbed(InteractionHook M, SChallonge_Tournament CT, EmbedBuilder E) {
        String Participation = "";
        String Location = "> **Event Location:** " + CT.I.getName() + "\n";
        String Starting = "";
        String isNew = "";

        if (Instant.now().isBefore(CT.getRegistrationStart().plus(2, ChronoUnit.DAYS))) {
            isNew = ":new: ";
        }

        if (CT.isPending()) {
            String spaceleft = "`[" + CT.getParticipants().size() + " " + TL(M,"Participants") + "]`";
            if (CT.getSignupCap() > 0) {
                if (CT.getSignupCap() == 1) {
                    spaceleft = "`[" + CT.getParticipants().size() + "/" + CT.getSignupCap() + "]` __*" + TL(M,"only-one-spot-left") + "*__";
                } else {
                    spaceleft = "`[" + CT.getParticipants().size() + "/" + CT.getSignupCap() + "]` __*" + TL(M,"only-spot-left", (CT.getSignupCap() - CT.getParticipantCount())) + "*__";
                }
            }
            Participation = "> **" + TL(M, "Registration") + ":** :green_circle: Open " + spaceleft + "\n";
            Starting = "> **" + TL(M, "Starting") + ":** <t:" + CT.getStartAtTime().getEpochSecond() + ":R> (<t:" + CT.getStartAtTime().getEpochSecond() + ":d>)\n";
            if (CT.getParticipantById(M.getInteraction().getUser().getIdLong()) != null) {
                Participation = "> **" + TL(M, "Registration") + ":** :white_check_mark: *Already Registered!* " + spaceleft + "\n";
            }
        } else if (CT.isGroupStageUnderway() || CT.isUnderway()) {
            Participation = "> **" + TL(M, "Registration") + ":** :yellow_circle: Underway (" + CT.getProgressMeter() + "%) `[" + CT.getParticipantCount() + " " + TL(M,"Participants") + "]`\n";
            Starting = "> **" + TL(M, "Started") + ":** <t:" + CT.getStartedAtTime().getEpochSecond() + ":R> (<t:" + CT.getStartedAtTime().getEpochSecond() + ":d>)\n";
            if (CT.getParticipantById(M.getInteraction().getUser().getIdLong()) != null) {
                Participation = "> **" + TL(M, "Registration") + ":** " + BotEmoji.get("yellow_check_mark") + "*Already Playing! (" + CT.getProgressMeter() + "%)* `[" + CT.getParticipants().size() + " " + TL(M,"Participants") + "]`\n";
            }
        }
        if (CT.getInscriptionChannelInviteLink() != null) {
            Location = "> **Event Location:** [" + CT.I.getName() + "](" + CT.getInscriptionChannelInviteLink() + ")\n";
        }
        E.addField(CT.getGame().getEmoji() + " " + isNew + CT.getName(),
                Participation +
                        Location +
                        Starting, false);
    }


    public static void slashHost(InteractionHook M, User P1, User P2) {
        M.editOriginal(TL(M,"host-choice", P1.getEffectiveName(), P2.getEffectiveName(), GenerateRandomNumber(1,2) == 1 ? P1.getAsMention() : P2.getAsMention())).queue();
    }
    public static void slashMatchmaking(InteractionHook M, User user, Game game, boolean isAnonymous, String rule, boolean isGlobal) throws Exception {
        Profile P = Profile.get(user);
        if (Instant.now().isAfter(P.getMatchmakingTimeout())) {
            ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
            if (!user.getId().equals(BotOwnerID)) P.RefreshMatchmakingTimeout(Instant.now().plus(60, ChronoUnit.MINUTES));
            Profile_Game PG = P.getPG(game);
            MatchmakingRequest REQ = new MatchmakingRequest(Instant.now().toEpochMilli(), user.getIdLong(), isAnonymous, rule, game);
            if (isGlobal) {
                M.editOriginal(TL(M, "matchmaking-many-serv")).queue();
                for (ServerInfo II : ServerInfo.list(true)) {
                    if (II.isPublic || (I != null && II.getId() == I.getId())) {
                        if (II.Ranking().hasPrivateRanking()) {
                            sendMatchmaking(II, user, REQ, P.getPG(game, II.getId()).getLeague(II));
                        } else {
                            sendMatchmaking(II, user, REQ, PG.getLeague());
                        }
                    }
                } REQ.Save();
            } else {
                if (I != null) {
                    M.editOriginal(TL(M, "matchmaking-one-serv")).queue();
                    if (I.Ranking().hasPrivateRanking()) {
                        sendMatchmaking(I, user, REQ, P.getPG(game, I.getId()).getLeague(I));
                    } else {
                        sendMatchmaking(I, user, REQ, PG.getLeague());
                    } REQ.Save();
                } else {
                    M.editOriginal(TL(M,"reply-failed-not-guild")).queue();
                }
            }
        } else {
            M.editOriginal(TL(M,"matchmaking-reply-failed-do-next-in") + " **<t:" + P.getMatchmakingTimeout().getEpochSecond() + ":R>**").queue();
        }
    }
    public static void sendMatchmaking(ServerInfo I, User U, MatchmakingRequest req, League league) {
        if (I.getGuild().getMemberById(U.getId()) != null) {
            if (I.getMatchmaking(req.Game) != null) {
                EmbedBuilder E = new EmbedBuilder();
                E.setThumbnail(req.isAnonymous ? req.Game.getImageUrl() : U.getEffectiveAvatarUrl());
                E.setFooter("ID: " + req.ID);
                E.setColor(req.Game.getColor());
                E.setAuthor("• Matchmaking " + req.Game, null, req.Game.getImageUrl());
                Button Me = Button.success("matchmaking-accept-" + req.ID, TLG(I,"accept"));
                Button Me2 = Button.secondary("matchmaking-cancel-" + req.ID, TLG(I,"cancel"));
                Role matchrole = I.getMatchmaking(req.Game).getRole();
                TextChannel matchchannel = I.getMatchmaking(req.Game).getChannel();
                if (matchrole != null && matchchannel != null) {
                    if (matchchannel.canTalk(I.getGuild().getSelfMember())) {
                        if (req.isAnonymous) {
                            E.setDescription(":detective: " + TLG(I,"matchmaking-reply-success-2", matchrole.getAsMention()) + "\n"
                                    + TLG(I, "Hint") + ": **" + league.getTier().getTierEmojiFormatted() + " " + league.getTier().getName() + "**");
                            E.setFooter("• Anonymous", I.getGuild().getIconUrl());
                        } else {
                            E.setDescription(TLG(I,"matchmaking-reply-success", "**" + league.getTier().getTierEmojiFormatted() + " " + U.getEffectiveName() + "**", matchrole.getAsMention()) + "\n"
                                    + ":warning: __" + TLG(I,"matchmaking-accept-warning") + "__");
                            E.setFooter("• " + U.getName(), I.getGuild().getIconUrl());
                        }
                        if (req.Rule != null) {
                            E.addField("__Special Rule__ ", "- " + req.Rule, false);
                        }
                        E.setTimestamp(Instant.now());
                        try {
                            Message M = matchchannel.sendMessageEmbeds(E.build()).setContent(matchrole.getAsMention()).setComponents(ActionRow.of(Me, Me2)).submit().orTimeout(3, TimeUnit.SECONDS).get();
                            req.MatchmakingRequests.add(new MatchmakingRequest.RequestData(I.getId(),matchchannel.getId(), matchrole.getId(), M.getId()));
                        } catch (InterruptedException | ExecutionException ignored) {}

                        if (req.isAnonymous) {
                            LogSlash("**[Matchmaking] [" + I.getGuild().getName() + "]** " + TLG(I,"matchmaking-log", "||" + U.getEffectiveName() + "||", req.Game.getName()));
                        } else {
                            LogSlash("**[Matchmaking] [" + I.getGuild().getName() + "]** " + TLG(I,"matchmaking-log", U.getEffectiveName(), req.Game.getName()));
                        }
                    }
                }
            }
        }
    }
    public synchronized static void createScoreReport(InteractionHook M, long P1ID, long P2ID, int myscore, int opponentscore, Game game, User winneroftie, TextChannel channel, Message.Attachment proof) {
        ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
        List<SChallonge_Tournament> TT = I.getTournamentsOfResultChannel(channel.getIdLong());
        SChallonge_Tournament T = I.getTournamentOfMatch(P1ID, P2ID, game, false);

        Profile P1 = Profile.get(P1ID);
        Profile P2 = Profile.get(P2ID);
        if (isScoreBan(P1.getId()) && T == null) {
            M.editOriginal("**" + P1.getUser().getEffectiveName() + "** has been **banned** from **Score Reporting**.").queue();
        } else if (isScoreBan(P2.getId()) && T == null) {
            M.editOriginal("**" + P2.getUser().getEffectiveName() + "** has been **banned** from **Score Reporting**.").queue();
        } else {
            if (hasPermissionInChannel(M, channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {

                if (TT.isEmpty() || TT.stream().anyMatch(This -> T != null && This.getId() == T.getId())) {

                    if (T == null || (T.isScoreValid(myscore, opponentscore) || winneroftie != null)) {

                        if (T == null || (game.equals(T.getGame()) && T.getMatchResultChannel().getId().equals(channel.getId()))) {

                            if (!P1.getUser().isBot() && !P2.getUser().isBot()) {

                                if (I.getGuild().getMemberById(P1.getId()) != null && I.getGuild().getMemberById(P2.getId()) != null) {

                                    if ((!(myscore > 20) && !(opponentscore > 20) && !(myscore < 0) && !(opponentscore < 0)) || T != null && T.getVSAmount() > 1) {

                                        if (Instant.now().isAfter(P1.getScoreTimeout())) {

                                            if (Instant.now().isAfter(P2.getScoreTimeout())) {

                                                if (!P1.getUser().getId().equals(P2.getUser().getId())) {
                                                    EmbedBuilder E = new EmbedBuilder();
                                                    E.setColor(game.getColor());
                                                    E.setTitle(TL(P2,"score-reply-title"));
                                                    E.setThumbnail(game.getImageUrl());
                                                    E.setDescription(TL(P2,"score-reply-success-match", game.getFullName()) + ": \n" +
                                                            P1.getUser().getAsMention() + " **" + myscore + "** " + game.getVSEmoji() + " **" + opponentscore + "** " + P2.getUser().getAsMention() + "\n" +
                                                            P2.getUser().getAsMention() + ", " + TL(P2,"score-reply-success-score-confirm") + "\n" +
                                                            (myscore == opponentscore && winneroftie != null ? (winneroftie.getIdLong() == P1.getId() ? "**Winner of tie:** " + P1.getId() : winneroftie.getIdLong() == P2.getId() ? "Winner of tie: " + P2.getId() + "\n"  : "") : "") + "\n" +
                                                            (T != null ? "**" + TL(M,"Tournament") + ":** " + game.getEmojiFormatted() + " " + T.getName() : ""));
                                                    if (proof != null) {
                                                        I.LogSlash(proof.getUrl());
                                                        E.setImage(proof.getUrl());
                                                    }

                                                    Button Confirmation = Button.success("match-rslt-conf", TL(P2,"yes"));
                                                    Button Decline = Button.danger("match-rslt-deny", TL(P2,"no"));
                                                    channel.sendMessage(P1.getUser().getAsMention() + " **" + myscore + "** " + game.getVSEmojiFormatted() + " **" + opponentscore + "** " + P2.getUser().getAsMention()).setEmbeds(E.build()).addComponents(ActionRow.of(Confirmation, Decline)).queue(Mm -> {
                                                        try {
                                                            MatchLog ML = new MatchLog(game, P1.getId(), P2.getId(), myscore, opponentscore, Mm, winneroftie);
                                                            new MatchLog_S(game, P1.getId(), P2.getId(), myscore, opponentscore, Mm);
                                                            if (T != null) {
                                                                BaseCParticipant<?,?,?> P = T.getTeamByMyId(P1ID);
                                                                if (P != null) P.getMatchWithOpponent(P2ID, false).setMatchLog(ML);
                                                            }
                                                            if (!isTournamentManager(M.getInteraction().getUser())) {
                                                                P1.RefreshScoreTimeout(Instant.now().plus(game.getScoreCooldownMinutes(), ChronoUnit.MINUTES));
                                                                P2.RefreshScoreTimeout(Instant.now().plus(game.getScoreCooldownMinutes(), ChronoUnit.MINUTES));
                                                                I.LogSlash(TLG(I, "log-vs-timer", P1.getUser().getEffectiveName(), game.getScoreCooldownMinutes()-1));
                                                                I.LogSlash(TLG(I, "log-vs-timer", P2.getUser().getEffectiveName(), game.getScoreCooldownMinutes()-1));
                                                                P1.Update();
                                                                P2.Update();
                                                            }
                                                            M.editOriginal(TL(P1, "Done")).queue();
                                                        } catch (Exception e) {
                                                            replyException(M, e);
                                                        }
                                                    });
                                                } else {
                                                    M.editOriginal(TL(M,"score-reply-failed-match_yourself")).queue();
                                                }
                                            } else {
                                                M.editOriginal(TL(M,"score-reply-failed-report_score-timer", P2.getUser().getEffectiveName(), "**<t:" + P2.getScoreTimeout().getEpochSecond() + ":R>**")).queue();
                                            }
                                        } else {
                                            M.editOriginal(TL(M,"score-reply-failed-report_score-timer", P1.getUser().getEffectiveName(), "**<t:" + P1.getScoreTimeout().getEpochSecond() + ":R>**")).queue();
                                        }
                                    } else {
                                        M.editOriginal(TL(M,"score-reply-failed-fake_score")).queue();
                                    }
                                } else {
                                    M.editOriginal(TL(M,"user-not-part-of-guild", "**" + P2.getUser().getEffectiveName() + "**")).queue();
                                }
                            } else {
                                M.editOriginal(TL(M,"score-reply-failed-fake_score")).queue();
                            }
                        } else {
                            M.editOriginal(TL(M, "tournament-add-score-fail-game")).queue();
                        }
                    } else {
                        M.editOriginal(TL(M, "tournament-add-score-fail-ties")).queue();
                    }
                } else {
                    M.editOriginal(TL(M, "tournament-add-score-fail-game")).queue();
                }
            }
        }
    }
    public static void slashRevertScore(InteractionHook M, User user, User opponent, int myscore, int opponentscore, Game game) throws Exception {
        ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
        MatchLog Log = MatchLog.getLog(user.getIdLong(), opponent.getIdLong(), myscore, opponentscore, game);
        if (Log != null) {
            List<SChallonge_Tournament> TT = I.getTournamentsOfResultChannel(M.getInteraction().getChannelIdLong());
            SChallonge_Tournament T = I.getTournamentOfMatchLog(Log.getId());
            if (TT.isEmpty() || TT.stream().anyMatch(This -> T != null && This.getId() == T.getId())) {
                if (T == null || T.getMatchResultChannel().getIdLong() == M.getInteraction().getChannelIdLong()) {
                    EmbedBuilder E = new EmbedBuilder();
                    E.setColor(Color.red);
                    E.setThumbnail(game.getImageUrl());
                    E.setTitle(TL(M, "unscore-reply-title"));
                    E.setDescription(":warning: " + TL(M, "unscore-reply-description", user.getEffectiveName()) + ":\n" +
                            user.getAsMention() + " **" + myscore + "** " + game.getVSEmoji() + " **" + opponentscore + "** " + opponent.getAsMention() + "\n" +
                            TL(M, "unscore-reply-description-confirm"));
                    if (T != null) {
                        SChallonge_Match CM = T.getMatchByLog(Log.getId());
                        if (!CM.isMatchUsedAsPrerequisite()) {
                            if (T.getVSAmount() > 1) {
                                SubMatch CSM = CM.getSubMatch(Log.getId());
                                if (CSM != null) E.addField(BotEmoji.get("Challonge").getFormatted() + " " + T.getName(), "**• " + user.getEffectiveName() + " " + T.getGame().getVSEmojiFormatted() + " " + opponent.getEffectiveName() + ":** " + CSM.getP1Score() + " - " + CSM.getP2Score(), false);
                            } else {
                                E.addField(BotEmoji.get("Challonge").getFormatted() + " " + T.getName(), "**• " + user.getEffectiveName() + " " + T.getGame().getVSEmojiFormatted() + " " + opponent.getEffectiveName() + ":** " + CM.getP1Score() + " - " + CM.getP2Score(), false);
                            }
                        } else {
                            M.editOriginal(TL(M, "tournament-revert-score-fail-too-late")).queue();
                            return;
                        }
                    }

                    ScoreCommand CMD = new ScoreCommand(Log);
                    CMD.RequesterID = M.getInteraction().getUser().getIdLong();
                    Button Confirmation = Button.danger(CMD.Command("match-rev-rslt-conf"), TL(M, "yes"));
                    Button Decline = Button.secondary(CMD.Command("match-rev-rslt-deny"), TL(M, "no"));
                    M.editOriginalEmbeds(E.build()).setContent("<@" + (CMD.RequesterID == Log.getP1ID() ? Log.getP2ID() : Log.getP1ID()) + ">")
                            .setComponents(ActionRow.of(Confirmation, Decline)).queue();
                } else {
                    M.editOriginal(TL(M, "tournament-add-score-fail-wrong-channel")).queue();
                }
            } else {
                M.editOriginal(TL(M, "tournament-add-score-fail-no-opp")).queue();
            }
        } else {
            M.editOriginal(TL(M,"result-reverted-fail")).queue();
        }
    }
    public static Message MakePrediction(Guild GUILD, User u1, User u2, TextChannel channel) {
        if (channel != null && u1 != null && u2 != null) {
            EmbedBuilder Embed = new EmbedBuilder();
            Clan c1 = getClanOfUser(u1);
            Clan c2 = getClanOfUser(u2);
            String s1 = c1 != null ? c1.getEmojiFormatted() : "";
            String s2 = c2 != null ? c2.getEmojiFormatted() : "";
            String name1 = GUILD != null && GUILD.getMemberById(u1.getId()) != null ? Objects.requireNonNull(GUILD.getMemberById(u1.getId())).getEffectiveName() : u1.getEffectiveName();
            String name2 = GUILD != null && GUILD.getMemberById(u2.getId()) != null ? Objects.requireNonNull(GUILD.getMemberById(u2.getId())).getEffectiveName() : u2.getEffectiveName();
            Profile P1 = Profile.get(u1);
            Profile P2 = Profile.get(u2);

            Embed.setTitle(s1 + name1 + " " + BotEmoji.get("VS3") + " " + name2 + " " + s2);
            Embed.setColor(Color.decode(mixColors(P1.getColor(), P2.getColor())));
            try (PredictionImageBuilder IMG = new PredictionImageBuilder(P1, P2, ServerInfo.get(GUILD))) {
                Embed.setImage(getFileUrl(IMG.GenerateCardPNG().DownloadPNGToFile(), "VS.jpg"));
            }
             if (GUILD != null) {
                Embed.setAuthor(TLG(GUILD, "Prediction"));
                Embed.setDescription(TLG(GUILD, "Who-will-win-the-match"));
                Embed.setFooter("• " + GUILD.getName(), GUILD.getIconUrl());
            } else {
                Embed.setAuthor(TL(P1, "Prediction"));
                Embed.setDescription(TL(P2, "Who-will-win-the-match"));
                Embed.setFooter("• " + u1.getEffectiveName() + "/" + u2.getEffectiveName());
            }
            try {
                Message message = channel.sendMessageEmbeds(Embed.build()).submit().orTimeout(3, TimeUnit.SECONDS).get();
                AddTemporaryUserEmoji(u1, message, 1);
                AddTemporaryUserEmoji(u2, message, 2);
                return message;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    public static void slashBet(InteractionHook M, User u1, User u2, int minute, Game game) {
        M.editOriginal("Loading...").queue(message1 -> {
            try {
                Bet B = new Bet(message1, u1, u2, Instant.now().plus(minute, ChronoUnit.MINUTES), game);
                B.BetUI(new BetCommand(B), message1.getChannel().asTextChannel());
            } catch (Exception e) {
                replyException(M, e);
            }
        });
    }
    public static void slashLeague(InteractionHook M, Profile p) {
        ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
        EmbedBuilder E = new EmbedBuilder();
        StringBuilder s = new StringBuilder();
        int i = 0;
        boolean hasPrivLeagues = I != null && I.Ranking().hasPrivateLeagues() && !I.Ranking().getLeagues().isEmpty();
        for (BasePG<?> G : hasPrivLeagues ? p.getPGs(I.getId()) : p.getPGs()) {
            if (G.Medals > 0) {
                if (i % 3 == 0) s.append("\n└ ");
                else s.append(" — ");
                s.append(G.getGame().getEmoji()).append(" • **").append(G.getLeague().getEmoji()).append(" ").append(G.getMedals()).append("**");
                i++;
            }
        }
        E.setTitle(TL(M,"League"));
        E.setAuthor("• " + p.getUser().getEffectiveName(), null, p.getUser().getAvatarUrl());
        E.setDescription(TL(M,"league-description", "**" + League.listGlobal().size() + "**"));
        E.addField(TL(M,"league-description-your-league", "**" +
                (hasPrivLeagues ? I.Ranking().getLeagueByMedal(p.Totals().getTotalMedals()) + " " + p.Totals().getTotalStats(I.getId()).get("Medals").toString() : League.getByMedal(p.Totals().getTotalMedals()) + " " + p.Totals().getTotalMedals())) + "**", s
                + (hasPrivLeagues ? "\n\n:warning: This server uses custom private leagues." : ""), false);
        String league = "";
        String tier = "";
        for (League L : hasPrivLeagues ? I.Ranking().getLeagues() : League.listGlobal()) {
            if (!league.isEmpty() && !L.getTier().getName().equals(tier)) {
                E.addField(tier + " League", league, false);
                league = "";
            }
            if (L.getEnd() > 999999) {
                league = league + L.getEmoji() + " **" + L.getName() + "** ・ " + L.getStart() + "\n";
            } else if (L.getEnd() < 0) {
                league = league + L.getEmoji() + " **" + L.getName() + "** ・ " + L.getEnd() + "\n";
            } else {
                league = league + L.getEmoji() + " **" + L.getName() + "** ・ " + L.getStart() + " - " + L.getEnd() + "\n";
            }
            tier = L.getTier().getName();
        }
        if (!league.isEmpty()) E.addField(tier + " League", league, false);
        E.setColor(Color.orange);
        M.editOriginalEmbeds(E.build()).queue();
    }
    public static void slashActiveTourney(InteractionHook M, Game game) {
        try {
            EmbedBuilder E = new EmbedBuilder();
            E.setTitle(TL(M, "active-tournaments"));
            E.setDescription(TL(M, "active-tournaments-description") +
                    (game != null ? "\n**" + TL(M,"Game") + ":** " + game.getFullName() : "") + "\n"
                    + TL(M,"view-more") + ": [Inazuma Events FRANCE](https://docs.google.com/spreadsheets/d/1MhabRWj8w_1a6Lb8Y8Q-N7L16ZsuCbZ2IzQG99-F7dk)\n"
                    + "__**Upcoming**__ ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            E.setColor(Color.orange);
            List<SChallonge_Tournament> CTs = new ArrayList<>();
            List<SChallonge_Tournament> CTs2 = new ArrayList<>();
            for (SChallonge_Tournament CT : SChallonge_Tournament.getActiveChallonges(true)) {
                if (CT.I.isPublic) {
                    if (game == null || CT.getGame() == game) {
                        if (CT.isPending()) {
                            CTs.add(CT);
                        } else if (CT.isGroupStageUnderway() || CT.isUnderway()) {
                            CTs2.add(CT);
                        }
                    }
                }
            }
            for (SChallonge_Tournament CT : CTs) {
                AttributingChallongesToEmbed(M, CT, E);
            }
            E.addField(" ", "__**Ongoing**__ ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", false);
            for (SChallonge_Tournament CT : CTs2) {
                AttributingChallongesToEmbed(M, CT, E);
            }
            M.editOriginalEmbeds(E.build()).queue();
        } catch (Exception e) {replyException(M, e);}
    }


    public static synchronized void processScore(InteractionHook M, @NotNull ServerInfo I, @NotNull MatchLog R1, @NotNull MatchLog_S R2, BaseCTournament<?,?,?> T) {
        if (hasPermissionInChannel(M, M.getInteraction().getGuildChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
            getWebhookOfChannel(M.getInteraction().getGuild().getTextChannelById(R1.getChannelID()), C -> {
                try {
                    if (I.areScoresAllowed || (T instanceof GChallonge_Tournament)) R1.makeRewards(T);
                    R2.makeRewards(T);

                    long readonlyMessageId = C.send(getResultMessage(M.getInteraction().getUser(), I, R1, R2, T).build()).orTimeout(10, TimeUnit.SECONDS).get().getId();
                    if (T != null) T.validateAllTeamsScores();

                    if (I.areScoresAllowed || (T instanceof GChallonge_Tournament)) R1.giveRewards();
                    R2.giveRewards();

                    R1.complete(MatchState.COMPLETE, readonlyMessageId);
                    R2.complete(MatchState.COMPLETE, readonlyMessageId);

                    M.deleteOriginal().queue();
                    R1.confirmBet();
                    LogSlash(I,TLG(M.getInteraction().getGuild(), "log-file-deleted-match-confirm", R1.getP1().getEffectiveName() + " " + R1.getP1Score(), R1.getP2Score() + " " + R1.getP2().getEffectiveName(), M.getInteraction().getUser().getEffectiveName()));
                } catch (Exception e) {
                    replyException(M, e, TL(M, "result-confirm-fail-not-found"));
                    Button Confirmation = Button.success("match-rslt-conf", TL(R1.getProfileP1(),"yes"));
                    Button Decline = Button.danger("match-rslt-deny", TL(R1.getProfileP2(),"no"));
                    M.editOriginalComponents(ActionRow.of(Confirmation, Decline)).queue();
                }
            });
        }
    }
    public static synchronized void processScore(InteractionHook M, MatchLog R1, BaseCTournament<?,?,?> T) {
        if (!M.getInteraction().isFromGuild() || hasPermissionInChannel(M, M.getInteraction().getGuildChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
            try {
                if (T instanceof GChallonge_Tournament) R1.makeRewards(T);

                MessageCreateBuilder MSG = getResultMessage(M.getInteraction().getUser(), R1, T);
                try {
                    R1.getProfileP1().getUser().openPrivateChannel().queue(channel -> channel.sendMessage(MSG.build()).queue());
                } catch (Exception ignored) {}
                try {
                    R1.getProfileP2().getUser().openPrivateChannel().queue(channel -> channel.sendMessage(MSG.build()).queue());
                } catch (Exception ignored) {}
                if (T != null) T.validateAllTeamsScores();

                if (T instanceof GChallonge_Tournament) R1.giveRewards();

                R1.complete(MatchState.COMPLETE, 0);

                M.deleteOriginal().queue();
                R1.confirmBet();
                LogSlash(TLG(M.getInteraction().getGuild(), "log-file-deleted-match-confirm", R1.getP1().getEffectiveName() + " " + R1.getP1Score(), R1.getP2Score() + " " + R1.getP2().getEffectiveName(), M.getInteraction().getUser().getEffectiveName()));
            } catch (Exception e) {
                try {
                    replyException(M, e, TL(M, "result-confirm-fail-not-found"));
                    Button Confirmation = Button.success("match-rslt-conf", TL(R1.getProfileP1(),"yes"));
                    Button Decline = Button.danger("match-rslt-deny", TL(R1.getProfileP2(),"no"));
                    M.editOriginalComponents(ActionRow.of(Confirmation, Decline)).queue();
                } catch (Exception ignored) {}
            }
        }
    }
    public static WebhookMessageBuilder getResultMessage(@NotNull User accepter, ServerInfo I, @NotNull MatchLog ML, MatchLog_S MLS, BaseCTournament<?,?,?> T) throws Exception {
        WebhookMessageBuilder WebhookMessage = new WebhookMessageBuilder();
        WebhookMessage.setUsername(TLG(I,"Match-Result"));
        WebhookMessage.resetEmbeds();
        WebhookMessage.resetFiles();
        try (MatchResultImageBuilder MRIB = new MatchResultImageBuilder(ML.getProfileP1().getUser(), ML.getProfileP2().getUser(), ML, MLS, I)) {
            if (T != null) MRIB.addChallonge(T);
            String resultimg = getFileUrl(MRIB.GenerateMatchResultPNG().DownloadPNGToFile(), "pfp.png");

            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
            embed.setDescription(":small_orange_diamond: " + TLG(I,"result-confirm-success-1", ML.getGame().getName(), accepter.getAsMention()) + "\n" +
                    "> **" + ML.getP1().getEffectiveName() + " " + ML.P1Score + " " + ML.getGame().getVSEmoji() + " " + ML.P2Score + " " + ML.getP2().getEffectiveName() + "**");
            embed.setAuthor(new WebhookEmbed.EmbedAuthor(ML.getGame().getFullName(), ML.getGame().getImageUrl(), null));
            embed.setFooter(new WebhookEmbed.EmbedFooter("• /score", I != null ? I.getGuild().getIconUrl() : DiscordAccount.getSelfUser().getEffectiveAvatarUrl()));
            embed.setThumbnailUrl(ML.getGame().getImageUrl());
            embed.setColor(ML.getGame().getColor().getRGB());
            embed.setImageUrl(resultimg);

            {
                TextChannel notification = I != null ? I.Channels().getNotificationChannel() : null;
                String s = "**";
                if (MLS != null && MLS.P1Item1ID != null) {
                    Item INV = Item.get(MLS.P1Item1ID);
                    if (INV != null) s = s + "- " + INV.getEmojiFormatted() + " " + INV.getName() + " ×" + MLS.P1Item1Amount + "\n";
                }
                if (!ML.getP1Drops().isEmpty() || (MLS != null && MLS.P1Item1ID != null)) {
                    for (Item S : ML.getP1Drops()) s = s + "- " + S.getEmojiFormatted() + " " + TLG(I, S.getName()) + "\n";
                    if (notification != null && T != null) notification.sendMessage("**__P1 Drops:__** \n" + s + "**").queue();
                    else embed.addField(new WebhookEmbed.EmbedField(true, "P1 Drops", s + "**"));
                }
                s = "**";
                if (MLS != null && MLS.P2Item1ID != null) {
                    Item INV = Item.get(MLS.P2Item1ID);
                    if (INV != null) s = s + "- " + INV.getEmojiFormatted() + " " + INV.getName() + " ×" + MLS.P2Item1Amount + "\n";
                }
                if (!ML.getP2Drops().isEmpty() || (MLS != null && MLS.P2Item1ID != null)) {
                    for (Item S : ML.getP2Drops()) s = s + "- " + S.getEmojiFormatted() + " " + TLG(I, S.getName()) + "\n";
                    if (notification != null && T != null) notification.sendMessage("**__P2 Drops:__** \n" + s + "**").queue();
                    else embed.addField(new WebhookEmbed.EmbedField(true, "P2 Drops", s + "**"));
                }
            }
            if (ML.P1Score > ML.P2Score) {
                int streak = ML.getProfileP1().getPG(ML.getGame()).WinStreak + 1;
                if (streak > 1) embed.addField(new WebhookEmbed.EmbedField(false, ML.getP1().getEffectiveName() + "'s Win Streak", "- **" + streak + "** (**x" + Math.min(1 + (0.05 * streak), 1.5) + "** XP/Medals/Coins/Drops)"));
            } else if (ML.P1Score < ML.P2Score) {
                int streak = ML.getProfileP2().getPG(ML.getGame()).WinStreak + 1;
                if (streak > 1) embed.addField(new WebhookEmbed.EmbedField(false, ML.getP2().getEffectiveName() + "'s Win Streak", "- **" + streak + "** (**x" + Math.min(1 + (0.05 * streak), 1.5) + "** XP/Medals/Coins/Drops)"));
            }

            WebhookMessage.addEmbeds(embed.build());
            try (DualPictureBuilder IMG = new DualPictureBuilder(ML.getProfileP1().getUser(), ML.getProfileP2().getUser(), ML.getGame())){
                WebhookMessage.setAvatarUrl(getFileUrl(IMG.GeneratePicturePNG().DownloadPNGToFile(), "pfp.png"));
            }

            if (T != null) {
                if (T.isUnderway() || T.isGroupStageUnderway()) {
                    if (T.getTeamByMyId(ML.P1ID) != null && T.getTeamByMyId(ML.P2ID) != null) {
                        BaseCMatch<?,?,?> M = (T.getVSAmount() == 1 ? T.getParticipantById(ML.P1ID).getMatchWithOpponent(ML.P2ID, false) : T.getMatches().stream().filter(MM -> MM.getSubMatch(ML.P1ID, ML.P2ID) != null).findAny().orElse(null));
                        if (M != null) {
                            M.setMatchLog(ML);
                            String message = ":white_check_mark: ";
                            ExecutorService E = Executors.newCachedThreadPool();
                            if (T.getVSAmount() > 1) {
                                SubMatch CSM = M.getSubMatch(ML.P1ID, ML.P2ID);
                                if (CSM.getP1ID() == ML.P1ID && CSM.getP2ID() == ML.P2ID) {
                                    CSM.AddScore(ML.P1Score, ML.P2Score);
                                } else if (CSM.getP1ID() == ML.P2ID && CSM.getP2ID() == ML.P1ID) {
                                    CSM.AddScore(ML.P2Score, ML.P1Score);
                                }
                                message = message + TLG(I, "tournament-match-validated", "**" + CSM.getP1().getEffectiveName() + " vs. " + CSM.getP2().getEffectiveName() + "**", "**" + CSM.getP1Score() + " - " + CSM.getP2Score() + "**");
                            } else {
                                if (M.getPlayer1().getLeaderID() == ML.P1ID) {
                                    M.AddScore(ML.P1Score, ML.P2Score, ML.TiedWinner);
                                } else if (M.getPlayer2().getLeaderID() == ML.P1ID) {
                                    M.AddScore(ML.P2Score, ML.P1Score, ML.TiedWinner);
                                }
                                M.Update();
                                message = message + TLG(I, "tournament-match-validated", "**" + M.getPlayer1().getName() + " vs. " + M.getPlayer2().getName() + "**", "**" + M.getP1Score() + " - " + M.getP2Score() + "**");
                            }
                            ShutdownAfterAction(E, 1, "", E.submit(() -> {
                                try {
                                    if (T.getVSAmount() == 1) {
                                        T.ResyncChallonge();
                                        T.Update();
                                        for (BaseCMatch<?,?,?> M1 : M.getPlayer1().getUnplayedMatches()) M1.NotifyNextOpponent();
                                        for (BaseCMatch<?,?,?> M2 : M.getPlayer2().getUnplayedMatches()) M2.NotifyNextOpponent();
                                        T.CleanTournament();
                                    }
                                } catch (Exception ignored) {}
                            }));
                            ML.getProfileP1().IncrementProgress("TOURNAMENT_PARTICIPATE/null", 1);
                            ML.getProfileP2().IncrementProgress("TOURNAMENT_PARTICIPATE/null", 1);
                            WebhookEmbedBuilder embed1 = new WebhookEmbedBuilder();
                            embed1.setAuthor(new WebhookEmbed.EmbedAuthor(T.getName(), ChallongeLogoURL, T.getFullChallongeURL()));
                            embed1.setColor(ML.getGame().getColor().getRGB());
                            embed1.setFooter(new WebhookEmbed.EmbedFooter("• " + (I != null ? I.getGuild().getName() : "Inazuma Competitive"), null));
                            embed1.setTimestamp(Instant.now());
                            embed1.setDescription(message + "\n`                                                        `");
                            WebhookMessage.addEmbeds(embed1.build());
                            LogSlash(I,TLG(I, "log-tournament-add", "**" + ML.getP1().getEffectiveName() + " vs. " + ML.getP2().getEffectiveName() + "**", "**" + ML.P1Score + " - " + ML.P2Score + "**", "**" + T.getName() + "**"));
                        }
                    }
                }
            }

            Clan clan = getClanOfUser(ML.P1ID);
            if (clan != null) {
                List<Interclan> L = clan.getOnGoingInterclanS();
                for (Interclan IC : L) {
                    Interclan_Duel D = IC.getDuel(ML.P1ID, ML.P2ID);
                    if (D != null && !D.isFinished() && D.getGame() == ML.getGame()) {
                        if (D.getP1ID() == ML.P1ID && D.getP2ID() == ML.P2ID) {
                            D.AddScore(ML.P1Score, ML.P2Score);
                        } else  if (D.getP1ID() == ML.P2ID && D.getP2ID() == ML.P1ID) {
                            D.AddScore(ML.P2Score, ML.P1Score);
                        }
                        D.Update();

                        String interclan = getFileUrl(IC.getDualPic(), "interclan.png");
                        String message = ":white_check_mark: " + TLG(I, "interclan-match-validated", "**" + ML.getP1().getEffectiveName() + " vs. " + ML.getP2().getEffectiveName() + "**", "**" + ML.P1Score + " - " + ML.P2Score + "**");
                        WebhookEmbedBuilder embed1 = new WebhookEmbedBuilder();
                        embed1.setAuthor(new WebhookEmbed.EmbedAuthor(IC.getHoster().getName() + " vs " + IC.getJoiner().getName(), interclan, null));
                        embed1.setColor(ML.getGame().getColor().getRGB());
                        embed1.setFooter(new WebhookEmbed.EmbedFooter("• " + (I != null ? I.getGuild().getName() : "Inazuma Competitive"), null));
                        embed1.setTimestamp(Instant.now());
                        embed1.setDescription(message + "\n`                                                        `");
                        WebhookMessage.addEmbeds(embed1.build());
                        IC.EndIfPossible();
                    }
                }
            }
        }
        return WebhookMessage;
    }
    public static MessageCreateBuilder getResultMessage(User accepter, MatchLog ML, BaseCTournament<?,?,?> T) throws Exception {
        MessageCreateBuilder WebhookMessage = new MessageCreateBuilder();
        try (MatchResultImageBuilder MRIB = new MatchResultImageBuilder(ML.getProfileP1().getUser(), ML.getProfileP2().getUser(), ML, null, null)) {
            if (T != null) MRIB.addChallonge(T);
            String resultimg = getFileUrl(MRIB.GenerateMatchResultPNG().DownloadPNGToFile(), "pfp.png");
            ServerInfo I = null;
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription(":small_orange_diamond: " + TLG(I,"result-confirm-success-1", ML.getGame().getName(), accepter.getAsMention()) + "\n" +
                    "> **" + ML.getP1().getEffectiveName() + " " + ML.P1Score + " " + ML.getGame().getVSEmoji() + " " + ML.P2Score + " " + ML.getP2().getEffectiveName() + "**");
            embed.setAuthor(ML.getGame().getFullName(), ML.getGame().getImageUrl(), null);
            embed.setFooter("• /score", DiscordAccount.getSelfUser().getEffectiveAvatarUrl());
            embed.setThumbnail(ML.getGame().getImageUrl());
            embed.setColor(ML.getGame().getColor().getRGB());
            embed.setImage(resultimg);

            {
                String s = "**";
                if (!ML.getP1Drops().isEmpty()) {
                    for (Item S : ML.getP1Drops()) s = s + "- " + S.getEmojiFormatted() + " " + TLG(I, S.getName()) + "\n";
                    embed.addField("P1 Drops", s + "**", true);
                }
                s = "**";
                if (!ML.getP2Drops().isEmpty()) {
                    for (Item S : ML.getP2Drops()) s = s + "- " + S.getEmojiFormatted() + " " + TLG(I, S.getName()) + "\n";
                    embed.addField("P2 Drops", s + "**", true);
                }
            }
            if (ML.P1Score > ML.P2Score) {
                int streak = ML.getProfileP1().getPG(ML.getGame()).WinStreak + 1;
                if (streak > 1) embed.addField(ML.getP1().getEffectiveName() + "'s Win Streak", "- **" + streak + "** (**x" + Math.min(1 + (0.05 * streak), 1.5) + "** XP/Medals/Coins/Drops)", false);
            } else if (ML.P1Score < ML.P2Score) {
                int streak = ML.getProfileP2().getPG(ML.getGame()).WinStreak + 1;
                if (streak > 1) embed.addField(ML.getP2().getEffectiveName() + "'s Win Streak", "- **" + streak + "** (**x" + Math.min(1 + (0.05 * streak), 1.5) + "** XP/Medals/Coins/Drops)", false);
            }

            WebhookMessage.addEmbeds(embed.build());

            if (T != null) {
                if (T.isUnderway() || T.isGroupStageUnderway()) {
                    if (T.getTeamByMyId(ML.P1ID) != null && T.getTeamByMyId(ML.P2ID) != null) {
                        BaseCMatch<?,?,?> M = (T.getVSAmount() == 1 ? T.getParticipantById(ML.P1ID).getMatchWithOpponent(ML.P2ID, false) : T.getMatches().stream().filter(MM -> MM.getSubMatch(ML.P1ID, ML.P2ID) != null).findAny().orElse(null));
                        if (M != null) {
                            M.setMatchLog(ML);
                            String message = ":white_check_mark: ";
                            ExecutorService E = Executors.newCachedThreadPool();
                            if (T.getVSAmount() > 1) {
                                SubMatch CSM = M.getSubMatch(ML.P1ID, ML.P2ID);
                                if (CSM.getP1ID() == ML.P1ID && CSM.getP2ID() == ML.P2ID) {
                                    CSM.AddScore(ML.P1Score, ML.P2Score);
                                } else if (CSM.getP1ID() == ML.P2ID && CSM.getP2ID() == ML.P1ID) {
                                    CSM.AddScore(ML.P2Score, ML.P1Score);
                                }
                                message = message + TLG(I, "tournament-match-validated", "**" + CSM.getP1().getEffectiveName() + " vs. " + CSM.getP2().getEffectiveName() + "**", "**" + CSM.getP1Score() + " - " + CSM.getP2Score() + "**");
                            } else {
                                if (M.getPlayer1().getLeaderID() == ML.P1ID) {
                                    M.AddScore(ML.P1Score, ML.P2Score, ML.TiedWinner);
                                } else if (M.getPlayer2().getLeaderID() == ML.P1ID) {
                                    M.AddScore(ML.P2Score, ML.P1Score, ML.TiedWinner);
                                }
                                M.Update();
                                message = message + TLG(I, "tournament-match-validated", "**" + M.getPlayer1().getName() + " vs. " + M.getPlayer2().getName() + "**", "**" + M.getP1Score() + " - " + M.getP2Score() + "**");
                            }
                            ShutdownAfterAction(E, 1, "", E.submit(() -> {
                                try {
                                    if (T.getVSAmount() == 1) {
                                        T.ResyncChallonge();
                                        T.Update();
                                        for (BaseCMatch<?,?,?> M1 : M.getPlayer1().getUnplayedMatches()) M1.NotifyNextOpponent();
                                        for (BaseCMatch<?,?,?> M2 : M.getPlayer2().getUnplayedMatches()) M2.NotifyNextOpponent();
                                        T.CleanTournament();
                                    }
                                } catch (Exception ignored) {}
                            }));
                            ML.getProfileP1().IncrementProgress("TOURNAMENT_PARTICIPATE/null", 1);
                            ML.getProfileP2().IncrementProgress("TOURNAMENT_PARTICIPATE/null", 1);
                            EmbedBuilder embed1 = new EmbedBuilder();
                            embed1.setAuthor(T.getName(), ChallongeLogoURL, T.getFullChallongeURL());
                            embed1.setColor(ML.getGame().getColor().getRGB());
                            embed1.setFooter("• Inazuma Competitive");
                            embed1.setTimestamp(Instant.now());
                            embed1.setDescription(message + "\n`                                                        `");
                            WebhookMessage.addEmbeds(embed1.build());
                            LogSlash(TLG(I, "log-tournament-add", "**" + ML.getP1().getEffectiveName() + " vs. " + ML.getP2().getEffectiveName() + "**", "**" + ML.P1Score + " - " + ML.P2Score + "**", "**" + T.getName() + "**"));
                        }
                    }
                }
            }

            Clan clan = getClanOfUser(ML.P1ID);
            if (clan != null) {
                List<Interclan> L = clan.getOnGoingInterclanS();
                for (Interclan IC : L) {
                    Interclan_Duel D = IC.getDuel(ML.P1ID, ML.P2ID);
                    if (D != null && !D.isFinished() && D.getGame() == ML.getGame()) {
                        if (D.getP1ID() == ML.P1ID && D.getP2ID() == ML.P2ID) {
                            D.AddScore(ML.P1Score, ML.P2Score);
                        } else  if (D.getP1ID() == ML.P2ID && D.getP2ID() == ML.P1ID) {
                            D.AddScore(ML.P2Score, ML.P1Score);
                        }
                        D.Update();

                        String interclan = getFileUrl(IC.getDualPic(), "interclan.png");
                        String message = ":white_check_mark: " + TLG(I, "interclan-match-validated", "**" + ML.getP1().getEffectiveName() + " vs. " + ML.getP2().getEffectiveName() + "**", "**" + ML.P1Score + " - " + ML.P2Score + "**");
                        EmbedBuilder embed1 = new EmbedBuilder();
                        embed1.setAuthor(IC.getHoster().getName() + " vs " + IC.getJoiner().getName(), interclan);
                        embed1.setColor(ML.getGame().getColor().getRGB());
                        embed1.setFooter("• Inazuma Competitive");
                        embed1.setTimestamp(Instant.now());
                        embed1.setDescription(message + "\n`                                                        `");
                        WebhookMessage.addEmbeds(embed1.build());
                        IC.EndIfPossible();
                    }
                }
            }
        }
        return WebhookMessage;
    }

}
