package iecompbot.interaction.listeners;

import iecompbot.interaction.cmdbreakdown.server.ChallongeCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.match.Game;
import iecompbot.objects.match.MatchLog;
import iecompbot.objects.match.MatchLog_S;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.Blacklist;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.SubMatch;
import iecompbot.objects.server.tournament.challonge.BaseCMatch;
import iecompbot.objects.server.tournament.challonge.BaseCParticipant;
import iecompbot.objects.server.tournament.challonge.BaseCTournament;
import iecompbot.objects.server.tournament.challonge.global.GChallonge_Tournament;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Match;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Participant;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static iecompbot.Constants.LogChannel;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.ChallongeAccount;
import static iecompbot.Main.Prefs;
import static iecompbot.interaction.Automation.*;
import static iecompbot.interaction.listeners.MatchFeatures.MakePrediction;
import static iecompbot.objects.BotManagers.isTournamentManager;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static my.utilities.util.Utilities.*;

public class ChallongeFeatures extends ListenerAdapter {

    private static long RegistrationTimeout = 0;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                if (event.getName().startsWith("idl-")) {
                    BaseCTournament<?,?,?> T = GChallonge_Tournament.getActiveChallonge();
                    if (T != null) {
                        ChallongeCommand CMD = new ChallongeCommand(T.getId());
                        if (event.getName().startsWith("idl-message_opponent")) {
                            String message = event.getOption("message").getAsString();
                            BaseCParticipant<?, ?, ?> me = T.getTeamByMyId(event.getUser().getIdLong());
                            if (me != null) {
                                List<BaseCMatch<?, ?, ?>> CMs = me.getUnplayedMatches().stream().filter(M -> M.getPlayer1() != null && M.getPlayer2() != null).collect(Collectors.toList());
                                if (!CMs.isEmpty()) {
                                    Label Message = Label.of(event.getUser().getName(), TextInput.create("message", TextInputStyle.PARAGRAPH)
                                            .setPlaceholder("...").setValue(message).setRequiredRange(0, 2).build());
                                    if (T.getVSAmount() > 1) {
                                        Label Opponent = Label.of(TL(event, "Opponent"), StringSelectMenu.create("opponent")
                                                .addOptions(CMs.stream()
                                                        .flatMap(CM -> CM.getSubMatches().stream().filter(SM -> !SM.isFinished()))
                                                        .filter(SM -> SM.getOpponent(event.getUser().getIdLong()) != null)
                                                        .map(SM -> SelectOption.of(SM.getOpponent(event.getUser().getIdLong()).getName(), String.valueOf(SM.getId())))
                                                        .toList()).setRequiredRange(1, 1).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-send-dm"), "Send a message to your opponent")
                                                .addComponents(Message, Opponent).build()).queue();
                                    } else {
                                        Label Opponent = Label.of(TL(event, "Opponent"), StringSelectMenu.create("opponent")
                                                .addOptions(CMs.stream().map(CM -> SelectOption.of(CM.getOpponent(me.getId()).getName(), String.valueOf(CM.getId()))).collect(Collectors.toList()))
                                                .setRequiredRange(1, 1).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-send-dm"), "Send a message to your opponent")
                                                .addComponents(Message, Opponent).build()).queue();
                                    }
                                } else {
                                    event.reply(TL(event, "tournament-add-score-fail-no-opp")).setEphemeral(true).queue();
                                }
                            } else {
                                event.reply(TL(event, "tournament-unregister-fail", "**" + T.getName() + "**")).setEphemeral(true).queue();
                            }
                        } else if (event.getName().startsWith("idl-score")) {
                            int myscore = event.getOption("myscore").getAsInt();
                            int opponentscore = event.getOption("opponentscore").getAsInt();
                            BaseCParticipant<?, ?, ?> me = T.getTeamByMyId(event.getUser().getIdLong());
                            if (me != null) {
                                LogChannel.sendMessage("**[Proof] " + event.getUser().getEffectiveName() + " (" + event.getUser().getIdLong() + "):**").setFiles(FileUpload.fromData(event.getOption("proof").getAsAttachment().getWaveform(), "proof.png")).queue();
                                List<BaseCMatch<?, ?, ?>> CMs = me.getUnplayedMatches().stream().filter(M -> M.getPlayer1() != null && M.getPlayer2() != null).collect(Collectors.toList());
                                if (!CMs.isEmpty()) {
                                    Label P1Score = Label.of(event.getUser().getName(), TextInput.create("score-1", TextInputStyle.SHORT)
                                            .setPlaceholder("0").setValue(String.valueOf(myscore)).setRequiredRange(0, 2)
                                            .setRequired(false).build());
                                    Label P2Score = Label.of(TL(event, "Opponent") + " (Score)", TextInput.create("score-2", TextInputStyle.SHORT)
                                            .setPlaceholder("0").setValue(String.valueOf(opponentscore)).setRequiredRange(0, 2)
                                            .setRequired(false).build());
                                    if (T.getVSAmount() > 1) {
                                        Label Opponent = Label.of(TL(event, "Opponent"), StringSelectMenu.create("opponent")
                                                .addOptions(CMs.stream()
                                                        .flatMap(CM -> CM.getSubMatches().stream().filter(SM -> !SM.isFinished()))
                                                        .filter(SM -> SM.getOpponent(event.getUser().getIdLong()) != null)
                                                        .map(SM -> SelectOption.of(SM.getOpponent(event.getUser().getIdLong()).getName(), String.valueOf(SM.getId())))
                                                        .toList()).setRequiredRange(1, 1).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-add-score"), "Set score of Match")
                                                .addComponents(P1Score, P2Score, Opponent).build()).queue();
                                    } else {
                                        Label Opponent = Label.of(TL(event, "Opponent"), StringSelectMenu.create("opponent")
                                                .addOptions(CMs.stream().map(CM -> SelectOption.of(CM.getOpponent(me.getId()).getName(), String.valueOf(CM.getId()))).collect(Collectors.toList()))
                                                .setRequiredRange(1, 1).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-add-score"), "Set score of Match")
                                                .addComponents(P1Score, P2Score, Opponent).build()).queue();
                                    }
                                } else {
                                    event.reply(TL(event, "tournament-add-score-fail-no-opp")).setEphemeral(true).queue();
                                }
                            } else {
                                event.reply(TL(event, "tournament-unregister-fail", "**" + T.getName() + "**")).setEphemeral(true).queue();
                            }
                        } else {
                            event.deferReply(true).queue(M -> {
                                try {
                                    if (event.getName().startsWith("idl-info")) {
                                        slashInfo(M, T);
                                    } else if (event.getName().startsWith("idl-rules")) {
                                        slashRules(M, T);
                                    } else if (event.getName().startsWith("idl-register")) {
                                        JoinTournament(M, T);
                                    } else if (event.getName().startsWith("idl-unregister")) {
                                        UnregisterTournament(M, T);
                                    } else if (event.getName().startsWith("idl-manage")) {
                                        T.TournamentManageUI(M, new ChallongeCommand(T.getId()));
                                    } else if (event.getName().startsWith("idl-start")) {
                                        StartTournament(M, T);
                                    } else if (event.getName().startsWith("idl-complete")) {
                                        CompleteTournament(M, T);
                                    }
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                    } else {
                        event.reply(TL(event, "tournament-gb-not-found")).setEphemeral(true).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("challonge")) {
                        ChallongeCommand CMD = new ChallongeCommand(event.getComponentId());
                        BaseCTournament<?,?,?> T = BaseCTournament.get(CMD.ChallongeID);
                        if (T instanceof SChallonge_Tournament CT) CT.I = ServerInfo.get(CT.ServerID);
                        if (event.getComponentId().contains("register")) {
                            if (event.getComponentId().startsWith("challonge-register")) {
                                event.deferReply(event.isFromGuild()).queue(M -> JoinTournament(M, T));
                            }
                            else if (event.getComponentId().startsWith("challonge-unregister")) {
                                event.deferReply(event.isFromGuild()).queue(M -> UnregisterTournament(M, T));
                            }
                        } else if (event.getComponentId().contains("challonge-teammate")) {
                            if (event.getComponentId().startsWith("challonge-teammate-accept")) {
                                event.deferEdit().setComponents(ActionRow.of(Button.secondary("nothing", TL(event, "yes")).withEmoji(Emoji.fromUnicode("U+2705")).asDisabled())).queue(M -> {
                                    try {
                                        BaseCParticipant<?,?,?> P = T.getParticipantById(CMD.ParticipantID);
                                        JoinTeam(M, T, P);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().startsWith("challonge-teammate-refuse")) {
                                event.deferEdit().setComponents(ActionRow.of(Button.secondary("nothing", TL(event, "no")).withEmoji(Emoji.fromUnicode("U+274C")).asDisabled())).queue(M -> {
                                    try {
                                        BaseCParticipant<?,?,?> P = T.getParticipantById(CMD.ParticipantID);
                                        if (T.getVSAmount() > 1 && P != null) sendPrivateMessage(P.getLeader(), TL(P.getLeaderPf(), "clan-invite-deny-success", event.getUser().getAsMention(), "**" + P.getName() + "**"));
                                        M.deleteOriginal().queue();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                        }
                        else if (event.getComponentId().startsWith("challonge-info")) {
                            event.deferReply(true).queue(M -> {
                                slashInfo(M, T);
                            });
                        }
                        else if (event.getComponentId().startsWith("challonge-rules")) {
                            event.deferReply(true).queue(M -> {
                                slashRules(M, T);
                            });
                        }
                        else {
                            if (event.getComponentId().startsWith("challonge-panel")) {
                                if (event.getComponentId().startsWith("challonge-panel-view-info")) {
                                    event.deferReply(true).queue(M -> {
                                        try {
                                            User U = event.getUser();
                                            BaseCParticipant<?,?,?> me = T.getTeamByMyId(U.getId());
                                            if (me != null) {
                                                EmbedBuilder E = T.getTournamentEmbed();
                                                String standing = me.getPosition() != 0 ? TL(event, "tournament-view-progress-standing", "**" + me.getPosition() + "**") + "\n" : "";
                                                String seed = !T.isPending() ? TL(event, "tournament-view-progress-seed", "**" + me.getSeed() + "**") + "\n" : "";
                                                E.setDescription(TL(event, "tournament-view-progress-description") + "\n" + standing + seed);

                                                String VS = T.getGame().getVSEmojiFormatted();
                                                for (BaseCMatch<?,?,?> Match : me.getMatches()) {
                                                    String round = Match.getRoundLong();
                                                    String icon = !Match.isCompleted() ? ":dart: " : ":white_check_mark: ";
                                                    String score = "TBA";

                                                    if (Match.getPlayer1() != null && Match.getPlayer2() != null) {
                                                        if (Match.getPlayer1().getAllTeammatesIDs().contains(U.getIdLong()) && Match.getP1Score() < Match.getP2Score()) {
                                                            icon = ":x: ";
                                                        }
                                                        else if (Match.getPlayer2().getAllTeammatesIDs().contains(U.getIdLong()) && Match.getP1Score() > Match.getP2Score()) {
                                                            icon = ":x: ";
                                                        }
                                                        else if (Match.isCompleted() && Match.getP1Score() == Match.getP2Score() && Match.getWinner() == null) {
                                                            icon = ":large_orange_diamond: ";
                                                        }
                                                    }
                                                    if (Match.isCompleted()) {
                                                        score = Match.getP1Score() + " - " + Match.getP2Score();
                                                        if (T.getVSAmount() > 1) {
                                                            score = "";
                                                            for (SubMatch CSM : Match.getSubMatches()) {
                                                                if (CSM.isFinished()) {
                                                                    score = score + CSM.getP1().getAsMention() + " ";
                                                                    if (CSM == Match.getSubMatches().get(Match.getSubMatches().size() - 1)) {
                                                                        score = score + "__" + CSM.getP1Score() + " : " + CSM.getP2Score() + "__ ";
                                                                    } else {
                                                                        score = score + CSM.getP1Score() + " : " + CSM.getP2Score() + " ";
                                                                    }
                                                                    score = score + CSM.getP2().getAsMention() + "\n";
                                                                }
                                                            }
                                                            score = score + "**" + TL(M,"Total") + ": " + Match.getTeam1GoalsSum() + " : " + Match.getTeam2GoalsSum() + "**";
                                                        } else {
                                                            if (Match.getP1Score() <= 0 && Match.getP2Score() <= 0) {
                                                                if (Match.getWinner() == Match.getPlayer1()) {
                                                                    score = Match.getP1Score() + " - DQ";
                                                                } else if (Match.getWinner() == Match.getPlayer2()) {
                                                                    score = "DQ - " + Match.getP2Score();
                                                                }
                                                            }
                                                        }
                                                    }
                                                    E.addField(icon + "__" + round + ":__ " + (Match.getPlayer1() != null ? Match.getPlayer1().getName() : "`???`") + " " + VS + " " + (Match.getPlayer2() != null ? Match.getPlayer2().getName() : "`???`")
                                                            + (Match.getDeadline() != null && icon.contains("dart") ? "\nDeadline: <t:" + Match.getDeadlineEpochSecond() + ":R>" : ""), score, false);
                                                }
                                                if (T.isComplete()) {
                                                    E.addField(TL(M, "Name"), "**" + me.getPosition() + ")** " + U.getAsMention(), true);
                                                    E.addField(TL(M, "Wins") + " | " + TL(M, "Defeats") + " | " + TL(M, "Ties"), me.getWins() + " | " + me.getLoses() + " | " + me.getTies(), true);
                                                    E.addField(TL(M, "GoalsScoredTaken"), "+" + (Math.max(me.getGoalsScored(), 0)) + "/-" + (Math.max(me.getGoalsTaken(), 0)), true);
                                                }
                                                List<ActionRow> ARs = new ArrayList<>();
                                                if (T.getVSAmount() > 1) {
                                                    ARs.add(ActionRow.of(Button.secondary(CMD.Command("challonge-panel-edit-team"), TL(M, "manage-team-name")).withDisabled(me.getLeaderID() != U.getIdLong())));
                                                    M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
                                                } else {
                                                    M.editOriginalEmbeds(E.build()).queue();
                                                }
                                                if (T instanceof SChallonge_Tournament CT) CT.RefreshPanelMessage();
                                            } else {
                                                M.editOriginal(TL(M, "tournament-view-progress-fail")).queue();
                                            }
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    });
                                }
                                else if (event.getComponentId().startsWith("challonge-panel-report-score")) {
                                    if (T.isOrganiser(event.getUser())) {
                                        event.deferReply(true).queue(M -> {
                                            T.AddScoreSelectionUI(M, CMD);
                                        });
                                    } else {
                                        BaseCParticipant<?,?,?> me = T.getTeamByMyId(event.getUser().getId());
                                        if (me != null) {
                                            List<BaseCMatch<?,?,?>> CMs = me.getUnplayedMatches().stream().filter(M -> M.getPlayer1() != null && M.getPlayer2() != null).collect(Collectors.toList());
                                            if (!CMs.isEmpty()) {
                                                Label P1Score = Label.of(event.getUser().getName(), TextInput.create("score-1", TextInputStyle.SHORT)
                                                        .setPlaceholder("0").setRequiredRange(0, 2)
                                                        .setRequired(false).build());
                                                Label P2Score = Label.of(TL(event, "Opponent") + " (Score)", TextInput.create("score-2", TextInputStyle.SHORT)
                                                        .setPlaceholder("0").setRequiredRange(0, 2)
                                                        .setRequired(false).build());
                                                if (T.getVSAmount() > 1) {
                                                    Label Opponent = Label.of(TL(event, "Opponent"), StringSelectMenu.create("opponent")
                                                            .addOptions(CMs.stream()
                                                                    .flatMap(CM -> CM.getSubMatches().stream().filter(SM -> !SM.isFinished()))
                                                                    .filter(SM -> SM.getOpponent(event.getUser().getIdLong()) != null)
                                                                    .map(SM -> SelectOption.of(SM.getOpponent(event.getUser().getIdLong()).getName(), String.valueOf(SM.getId())))
                                                                    .toList()).setRequiredRange(1, 1).build());
                                                    event.replyModal(Modal.create(CMD.Command("challonge-manage-add-score"), "Set score of Match")
                                                            .addComponents(P1Score, P2Score, Opponent).build()).queue();
                                                } else {
                                                    Label Opponent = Label.of(TL(event, "Opponent"), StringSelectMenu.create("opponent")
                                                            .addOptions(CMs.stream().map(CM -> SelectOption.of(CM.getOpponent(me.getId()).getName(), String.valueOf(CM.getId()))).collect(Collectors.toList()))
                                                            .setRequiredRange(1, 1).build());
                                                    event.replyModal(Modal.create(CMD.Command("challonge-manage-add-score"), "Set score of Match")
                                                            .addComponents(P1Score, P2Score, Opponent).build()).queue();
                                                }
                                            } else {
                                                event.reply(TL(event, "tournament-add-score-fail-no-opp")).setEphemeral(true).queue();
                                            }
                                        } else {
                                            event.reply(TL(event, "tournament-add-score-fail-no-opp")).setEphemeral(true).queue();
                                        }
                                    }
                                }
                                else if (event.getComponentId().startsWith("challonge-panel-refresh")) {
                                    event.deferEdit().queue(M -> {
                                        try {
                                            T.ResyncChallonge();
                                            T.CleanTournament();
                                            if (T instanceof SChallonge_Tournament CT) {
                                                CT.AddRoleToEveryone();
                                                CT.RefreshInscriptionMessage();
                                                if (CT.isUnderway() || CT.isGroupStageUnderway()) {
                                                    for (SChallonge_Match MM : CT.getMatches()) {
                                                        MM.setVotesFromMessage();
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    });
                                }
                                else if (event.getComponentId().startsWith("challonge-panel-manage-info")) {
                                    event.deferReply(true).queue(M -> {
                                        T.TournamentManageUI(M, CMD);
                                    });
                                }
                                else if (event.getComponentId().startsWith("challonge-panel-manage-deadline")) {
                                    event.deferReply(true).queue(M -> {
                                        if (T.isOrganiser(event.getUser())) {
                                            List<SelectOption> Op = new ArrayList<>();
                                            List<SelectOption> Op2 = new ArrayList<>();
                                            for (long G : T.getGroupIDs()) {
                                                for (int R : T.getRounds()) {
                                                    if (T.areThereMatchesLeft(R, G)) {
                                                        BaseCMatch<?,?,?> MM = T.getMatches(R, G).getFirst();
                                                        if (R >= 0) {
                                                            if (MM.getDeadline() != null) {
                                                                Op.add(SelectOption.of((G > 0 ? T.getGroupName(G) + ": " : "") + MM.getRoundLong(), R + "/" + G)
                                                                        .withDescription(EpochSecondToPattern(MM.getDeadline().getEpochSecond(), "dd/MM/yyyy - HH:mm")));
                                                            } else {
                                                                Op.add(SelectOption.of((G > 0 ? T.getGroupName(G) + ": " : "") + MM.getRoundLong(), R + "/" + G)
                                                                        .withDescription("--/--/---- - --:--"));
                                                            }
                                                        } else {
                                                            if (MM.getDeadline() != null) {
                                                                Op2.add(SelectOption.of((G > 0 ? T.getGroupName(G) + ": " : "") + MM.getRoundLong(), R + "/" + G)
                                                                        .withDescription(EpochSecondToPattern(MM.getDeadline().getEpochSecond(), "dd/MM/yyyy - HH:mm")));
                                                            } else {
                                                                Op2.add(SelectOption.of((G > 0 ? T.getGroupName(G) + ": " : "") + MM.getRoundLong(), R + "/" + G)
                                                                        .withDescription("--/--/---- - --:--"));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            EmbedBuilder E = T.getTournamentEmbed();
                                            E.setTitle("Deadline");
                                            List<ActionRow> rows = new ArrayList<>();
                                            if (!Op.isEmpty()) {
                                                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("challonge-manage-deadline-round-1"))
                                                        .setPlaceholder(Op.getFirst().getLabel())
                                                        .setRequiredRange(1, 1)
                                                        .addOptions(Op)
                                                        .build();
                                                rows.add(ActionRow.of(menu));
                                            }
                                            if (!Op2.isEmpty()) {
                                                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("challonge-manage-deadline-round-2"))
                                                        .setPlaceholder(Op2.getFirst().getLabel())
                                                        .setRequiredRange(1, 1)
                                                        .addOptions(Op2)
                                                        .build();
                                                rows.add(ActionRow.of(menu));
                                            }
                                            if (!rows.isEmpty()) {
                                                E.setDescription(TL(event, "challonge-manage-deadline-description"));
                                                M.editOriginalEmbeds(E.build()).setComponents(rows).queue();
                                            } else {
                                                E.setDescription(TL(event, "challonge-manage-deadline-fail"));
                                                M.editOriginalEmbeds(E.build()).queue();
                                            }
                                        } else {
                                            M.editOriginal(TL(event,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).queue();
                                        }
                                    });
                                }
                                else if (event.getComponentId().startsWith("challonge-panel-manage-complete")) {
                                    event.deferReply(true).queue(M -> {
                                        CompleteTournament(M, T);
                                    });
                                }
                                else if (event.getComponentId().startsWith("challonge-panel-manage-start")) {
                                    event.deferReply(true).queue(M -> {
                                        if (T.isOrganiser(event.getUser())) {
                                            if (T.getParticipants().stream().allMatch(BaseCParticipant::isTeamFull) && T.getParticipantCount() > 1) {
                                                EmbedBuilder E = T.getTournamentEmbed();
                                                E.setDescription(":red_circle: " + TL(M, "challonge-manage-start-description-confirm-window"));
                                                Button START = Button.danger("challonge-panel-confirm-start/" + T.getId(), TL(M, "Start"));
                                                M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(START)).queue();
                                            } else {
                                                M.editOriginal(TL(event, "challonge-manage-start-fail")).queue();
                                            }
                                        } else {
                                            M.editOriginal(TL(event,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).queue();
                                        }
                                    });
                                }
                                else if (event.getComponentId().startsWith("challonge-panel-confirm-start")) {
                                    event.deferEdit().setComponents(ActionRow.of(Button.danger("nothing", TL(event,"Processing")).asDisabled())).queue(M -> {
                                        if (T.isOrganiser(event.getUser())) {
                                            if (T.getParticipants().stream().allMatch(BaseCParticipant::isTeamFull) && T.getParticipantCount() > 1) {
                                                StartTournament(M, T);
                                            } else {
                                                M.editOriginal(TL(event, "challonge-manage-start-fail")).queue();
                                            }
                                        } else {
                                            M.editOriginal(TL(event,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).queue();
                                        }
                                    });
                                }
                                else if (event.getComponentId().startsWith("challonge-panel-edit-team")) {
                                    BaseCParticipant<?,?,?> MyTeam = T.getParticipantById(event.getUser().getIdLong());
                                    if (MyTeam != null) {
                                        CMD.ParticipantID = MyTeam.getId();
                                        Label Name = Label.of(TL(event,"Name"), TextInput.create("name", TextInputStyle.SHORT)
                                                .setPlaceholder(MyTeam.getName())
                                                .setRequiredRange(1, 64)
                                                .setRequired(true)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-edit-team-name"), TL(event, "Name"))
                                                .addComponents(Name).build()).queue();
                                    } else event.reply(TL(event, "tournament-cant-find-you")).setEphemeral(true).queue();
                                }
                            }
                            else if (event.getComponentId().startsWith("challonge-manage")) {
                                if (T.isOrganiser(event.getUser())) {
                                    if (event.getComponentId().startsWith("challonge-manage-deadline")) {
                                        if (event.getComponentId().startsWith("challonge-manage-deadline-warn-players")) {
                                            event.deferEdit().queue(M -> {
                                                M.editOriginalComponents(ActionRow.of(Button.secondary(CMD.Command("challonge-manage-deadline-warn-players"), TL(M,"challonge-manage-deadline-warn-players"))
                                                        .withEmoji(Emoji.fromUnicode("U+1F514")).withDisabled(true))).queue();
                                                for (BaseCMatch<?,?,?> Match : T.getMatches(CMD.Round, CMD.GroupID)) {
                                                    Match.NotifyDeadline();
                                                }
                                            });
                                        }
                                        else if (event.getComponentId().startsWith("challonge-manage-deadline-add-1-day")) {
                                            if (T.getMatches(CMD.Round, CMD.GroupID).getFirst().getDeadline() != null) {
                                                event.deferEdit().queue(M -> {
                                                    for (BaseCMatch<?,?,?> Match : T.getMatches(CMD.Round, CMD.GroupID)) {
                                                        Match.setDeadline(Match.getDeadline().plus(1, ChronoUnit.DAYS));
                                                        Match.Update();
                                                    }
                                                    String dl = EpochSecondToPattern(T.getMatches(CMD.Round, CMD.GroupID).getFirst().getDeadline().plus(1, ChronoUnit.DAYS).getEpochSecond(), "dd/MM/yyyy - HH:mm");
                                                    EmbedBuilder E = T.getTournamentEmbed();
                                                    E.setTitle("Deadline - " + T.getRoundLong(CMD.Round, CMD.GroupID));
                                                    E.setDescription(TL(M,"deadline-description-when", "`" + T.getRoundLong(CMD.Round, CMD.GroupID) + "`", "`" + EpochSecondToPattern(Instant.now().getEpochSecond(), "dd/MM/yyyy - HH:mm") + "`\nDeadline: `" + dl + "`"));
                                                    M.editOriginal(TL(M,"deadline-add-day-success", T.getRoundLong(CMD.Round, CMD.GroupID))).setEmbeds(E.build()).queue();
                                                });
                                            } else {
                                                event.reply(TL(event,"deadline-add-fail")).queue();
                                            }
                                        }
                                        else if (event.getComponentId().startsWith("challonge-manage-deadline-add-1-hour")) {
                                            if (T.getMatches(CMD.Round, CMD.GroupID).getFirst().getDeadline() != null) {
                                                event.deferEdit().queue(M -> {
                                                    for (BaseCMatch<?,?,?> Match : T.getMatches(CMD.Round, CMD.GroupID)) {
                                                        Match.setDeadline(Match.getDeadline().plus(1, ChronoUnit.HOURS));
                                                        Match.Update();
                                                    }
                                                    String dl = EpochSecondToPattern(T.getMatches(CMD.Round, CMD.GroupID).getFirst().getDeadline().plus(1, ChronoUnit.HOURS).getEpochSecond(), "dd/MM/yyyy - HH:mm");
                                                    EmbedBuilder E = T.getTournamentEmbed();
                                                    E.setTitle("Deadline - " + T.getRoundLong(CMD.Round, CMD.GroupID));
                                                    E.setDescription(TL(M,"deadline-description-when", "`" + T.getRoundLong(CMD.Round, CMD.GroupID) + "`", "`" + EpochSecondToPattern(Instant.now().getEpochSecond(), "dd/MM/yyyy - HH:mm") + "`\nDeadline: `" + dl + "`"));
                                                    M.editOriginal(TL(M,"deadline-add-hour-success", T.getRoundLong(CMD.Round, CMD.GroupID))).setEmbeds(E.build()).queue();
                                                });
                                            } else {
                                                event.reply(TL(event,"deadline-add-fail")).queue();
                                            }
                                        }
                                    }
                                } else {
                                    event.reply(TL(event,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).setEphemeral(true).queue();
                                }
                            }
                            else if (event.getComponentId().startsWith("challonge-edit-participant")) {
                                if (event.getComponentId().startsWith("challonge-edit-participant-name")) {
                                    if (T.getParticipantById(CMD.ParticipantID) != null) {
                                        Label input = Label.of(TL(event,"Name"), TextInput.create("name", TextInputStyle.SHORT)
                                                .setPlaceholder(event.getUser().getEffectiveName())
                                                .setRequiredRange(1, 64)
                                                .setRequired(true)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-edit-participant-name"), TL(event, "Name"))
                                                .addComponents(input).build()).queue();
                                    } else event.reply(TL(event, "tournament-cant-find-you")).setEphemeral(true).queue();
                                }
                                else if (event.getComponentId().startsWith("challonge-edit-participant-seed")) {
                                    if (T.getParticipantById(CMD.ParticipantID) != null) {
                                        Label input = Label.of("Seed", TextInput.create("seed", TextInputStyle.SHORT)
                                                .setPlaceholder(event.getMessage().getEmbeds().getFirst().getFields().get(2).getValue())
                                                .setRequiredRange(1, 3)
                                                .setRequired(true)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-edit-participant-seed"), "Enter new seed of " + T.getParticipantById(CMD.ParticipantID).getName())
                                                .addComponents(input).build()).queue();
                                    } else event.reply(TL(event, "tournament-cant-find-you")).setEphemeral(true).queue();
                                }
                                else if (event.getComponentId().startsWith("challonge-edit-participant-id")) {
                                    if (T.getParticipantById(CMD.ParticipantID) != null) {
                                        Label input = Label.of("ID", TextInput.create("id", TextInputStyle.SHORT)
                                                .setPlaceholder(event.getMessage().getEmbeds().getFirst().getFields().get(2).getValue())
                                                .setRequiredRange(1, 30)
                                                .setRequired(true)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-edit-participant-id"), "Discord ID")
                                                .addComponents(input).build()).queue();
                                    } else event.reply(TL(event, "tournament-cant-find-you")).setEphemeral(true).queue();
                                }
                                else if (event.getComponentId().startsWith("challonge-edit-participant-delete")) {
                                    if (T.getParticipantById(CMD.ParticipantID) != null) {
                                        event.deferEdit().queue(M -> {
                                            try {
                                                BaseCParticipant<?,?,?> P = T.DeleteParticipant(CMD.ParticipantID);
                                                M.editOriginal(":white_check_mark: " + TL(M,"challonge-manage-participant-delete-success", "**" + P.getName() + "**")).setReplace(true).queue();
                                                if (T instanceof SChallonge_Tournament CT) CT.I.LogSlash(TLG(CT.I, "log-tournament-participant-4", "**" + event.getUser().getEffectiveName() + "**", "**" + P.getName() + "**"));
                                            } catch (Exception e) {
                                                replyException(M, e);
                                            }
                                        });
                                    } else event.reply(TL(event, "tournament-cant-find-you")).setEphemeral(true).queue();
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
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (event.isFromGuild() && !event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("challonge")) {
                        List<String> InteractionIDs = event.getValues();
                        ServerInfo I = ServerInfo.get(event.getGuild());
                        if (event.getComponentId().startsWith("challonge-visualise")) {
                            BaseCTournament<?,?,?> T = BaseCTournament.get(Long.parseLong(InteractionIDs.getFirst()));
                            if (T instanceof SChallonge_Tournament CT) CT.I = ServerInfo.get(CT.ServerID);
                            if (T != null) {
                                event.deferReply().queue(M -> {
                                    ChallongeCommand CMD = new ChallongeCommand(event.getComponentId());
                                    CMD.ChallongeID = T.getId();
                                    T.TournamentManageUI(M, CMD);
                                });
                            } else {
                                event.deferReply(true).setContent(TL(event, "challonge-access-fail")).queue();
                            }
                        }

                        else {
                            ChallongeCommand CMD = new ChallongeCommand(event.getComponentId());
                            BaseCTournament<?,?,?> T = BaseCTournament.get(CMD.ChallongeID);
                            if (T instanceof SChallonge_Tournament CT) CT.I = ServerInfo.get(CT.ServerID);
                            if (T.isOrganiser(event.getUser())) {
                                if (event.getComponentId().startsWith("challonge-manage-config")) {
                                    if (InteractionIDs.getFirst().equals("add-match")) {
                                        event.deferReply(true).queue(M -> {
                                            if (!T.isComplete()) {
                                                T.AddScoreSelectionUI(M, CMD);
                                            } else {
                                                M.editOriginal(TL(event, "tournament-manage-fail-already-end")).queue();
                                            }
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-match")) {
                                        event.deferReply(true).queue(M -> {
                                            if (!T.isComplete()) {
                                                T.EditMatchSelectionUI(CMD, M);
                                            } else {
                                                M.editOriginal(TL(event, "tournament-manage-fail-already-end")).queue();
                                            }
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("random-match")) {
                                        event.deferReply(true).queue(M -> {
                                            if (!T.isComplete()) {
                                                T.RerandomMatchSelectionUI(CMD, M);
                                            } else {
                                                M.editOriginal(TL(event, "tournament-manage-fail-already-end")).queue();
                                            }
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-participants")) {
                                        event.deferReply(true).queue(M -> {
                                            T.EditParticipantSelectionUI(CMD, M);
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("add-prediction")) {
                                        event.deferReply(true).queue(M -> {
                                            if (!T.isComplete()) {
                                                T.AddPredictionSelectionUI(M, CMD);
                                            } else {
                                                M.editOriginal(TL(event, "tournament-manage-fail-already-end")).queue();
                                            }
                                        });
                                    }

                                    else if (InteractionIDs.getFirst().equals("edit-name")) {
                                        Label input = Label.of(TL(event,"Name"), TextInput.create("name", TextInputStyle.SHORT)
                                                .setPlaceholder(T.getName()).setRequiredRange(8, 64)
                                                .setRequired(true).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-name"), TL(event,"challonge-edit-name-desc"))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-desc")) {
                                        String desc = StopString(T.getDescription(), 100);
                                        Label input = Label.of(TL(event,"Description"), TextInput.create("description", TextInputStyle.PARAGRAPH)
                                                .setPlaceholder(desc.isEmpty() ? TL(event,"Description") : desc).setRequiredRange(1,1024).setRequired(true).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-desc"), TL(event,"challonge-edit-desc-desc"))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-game")) {
                                        Label input = Label.of(TL(event,"Game"), TextInput.create("game", TextInputStyle.SHORT)
                                                .setPlaceholder(T.getGame().getName()).setRequiredRange(1, 50)
                                                .setRequired(true).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-game"), TL(event,"challonge-edit-game-desc"))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-start_time")) {
                                        Label input = Label.of(TL(event,"start_time"), TextInput.create("epoch-second", TextInputStyle.SHORT)
                                                .setPlaceholder(Instant.now().getEpochSecond() + "").setRequiredRange(1, 12)
                                                .setRequired(false).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-start-time"), TL(event,"challonge-edit-start_time-desc"))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-signup_cap")) {
                                        Label input = Label.of(TL(event,"signup_cap"), TextInput.create("signup-cap", TextInputStyle.SHORT)
                                                .setPlaceholder("128").setRequiredRange(1, 3)
                                                .setRequired(false).build());
                                        event.replyModal(Modal.create(CMD.Command("challonge-manage-signup-cap"), TL(event,"challonge-edit-signup_cap-desc"))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-third-place")) {
                                        try {
                                            EmbedBuilder E = T.getTournamentEmbed();
                                            E.setTitle(TL(event,"tournament-manager"));
                                            T.setThirdPlace(!T.hasThirdPlace());
                                            E.setDescription(":white_check_mark: " + TL(event,"challonge-manage-third-place-success", "**" + T.hasThirdPlace() + "**"));
                                            LogSlash(I, TLG(event.getGuild(), "log-tournament-third-place", "**" + event.getUser().getEffectiveName() + "**", ":**white_check_mark: TRUE**"));
                                            event.replyEmbeds(E.build()).setEphemeral(true).queue();
                                        } catch (Exception e) {
                                            replyException(event, e);
                                        }
                                    }
                                    else if (InteractionIDs.getFirst().equals("download-tournament")) {
                                        event.deferReply(true).queue(M -> {
                                            M.editOriginal(TL(M,"file-ready")).setFiles(FileUpload.fromData(T.getJSON(), T.getId() + ".json")
                                                    , FileUpload.fromData(T.getCSV(), T.getId() + ".csv")).queue();
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("delete-tournament")) {
                                        event.deferEdit().queue(M -> {
                                            M.editOriginal(TL(M,"no")).queue();
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("refresh")) {
                                        event.deferReply(true).queue(Message -> {
                                            try {
                                                T.ResyncChallonge();
                                                T.Update();
                                                if (T instanceof SChallonge_Tournament ST) ST.RefreshPanelMessage();
                                                Message.editOriginal("There you go, now pls let me rest. This tournament is a lot of work...").queue();
                                            } catch (Exception e) {
                                                replyException(Message, e);
                                            }
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().contains("seed")) {
                                        event.reply(TL(event, "Processing")).setEphemeral(true).queue(M -> {
                                            try {
                                                switch (InteractionIDs.getFirst()) {
                                                    case "seed-random" -> ChallongeAccount.randomizeParticipants(T.getTournament());
                                                    case "seed-power" -> T.SeedByPower();
                                                    case "seed-activity" -> T.SeedByActivity();
                                                }
                                                EmbedBuilder E = T.getTournamentEmbed();
                                                E.setTitle(TL(M,"tournament-manager"));
                                                E.setDescription(":white_check_mark: " + TL(M,"seeding-success"));
                                                M.editOriginalEmbeds(E.build()).setReplace(true).queue();
                                            } catch (Exception e) {
                                                replyException(M, e);
                                            }
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-invite") && T instanceof SChallonge_Tournament ST) {
                                        Label input = Label.of(TL(event,"Link"), TextInput.create("invite-link", TextInputStyle.SHORT)
                                                .setPlaceholder(ST.getInscriptionChannelInviteLink() != null ? ST.getInscriptionChannelInviteLink() : "https://discord.gg/Q86CRCNmcX").setRequiredRange(24, 32)
                                                .setRequired(false).build());
                                        event.replyModal( Modal.create(CMD.Command("challonge-manage-invite-link"), TL(event,"challonge-edit-invite-desc"))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-channels") && T instanceof SChallonge_Tournament ST) {
                                        event.deferReply(true).queue(M -> {
                                            ST.TournamentChannelsSelectMenu(M, CMD);
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("edit-roles") && T instanceof SChallonge_Tournament ST) {
                                        event.deferReply(true).queue(M -> {
                                            ST.TournamentRolesSelectMenu(M, CMD);
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("log-advertisement") && T instanceof SChallonge_Tournament ST) {
                                        event.deferEdit().queue(M -> {
                                            try {
                                                ST.I = ServerInfo.get(ST.ServerID);
                                                ST.LogTournamentAnnouncement(true);
                                                M.editOriginal(TL(M,"yes")).queue();
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("log-results") && T instanceof SChallonge_Tournament ST) {
                                        event.deferEdit().queue(M -> {
                                            try {
                                                ST.I = ServerInfo.get(ST.ServerID);
                                                ST.LogTournamentResult(true);
                                                M.editOriginal(TL(M,"yes")).queue();
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("dm-advertisement") && T instanceof SChallonge_Tournament ST) {
                                        event.deferEdit().queue(M -> {
                                            ST.NotifyAllByDM(null);
                                            M.editOriginal(TL(M,"yes")).queue();
                                        });
                                    }
                                    else if (InteractionIDs.getFirst().equals("list-raw") && T instanceof SChallonge_Tournament ST) {
                                        event.deferReply(true).queue(Message -> {
                                            for (long G : T.getGroupIDs()) {
                                                for (int R : T.getRounds(G)) {
                                                    String S = "```" + ST.listUncompletedMatchesOfRound(R, G) + "```";
                                                    if (S.length() > 10 && S.length() < 1024) {
                                                        sendPrivateMessage(event.getUser(), S);
                                                    }
                                                }
                                            }
                                            Message.editOriginal("There you go, now pls let me rest. This tournament is a lot of work...").queue();
                                        });
                                    }
                                }

                                else if (event.getComponentId().startsWith("challonge-manage-deadline")) {
                                    if (event.getComponentId().startsWith("challonge-manage-deadline-round")) {
                                        event.deferReply(true).queue(M -> {
                                            CMD.Round = Integer.parseInt(InteractionIDs.getFirst().split("/")[0]);
                                            CMD.GroupID = Integer.parseInt(InteractionIDs.getFirst().split("/")[1]);
                                            EmbedBuilder E = T.getTournamentEmbed();
                                            E.setTitle("Deadline - " + event.getSelectedOptions().getFirst().getLabel());
                                            E.setFooter("Round ID: " + InteractionIDs.getFirst());

                                            List<Button> l = new ArrayList<>();
                                            if (!T.getMatches(CMD.Round, CMD.GroupID).isEmpty()) {
                                                BaseCMatch<?,?,?> Match = T.getMatches(CMD.Round, CMD.GroupID).getFirst();
                                                E.setDescription(TL(M,"deadline-description-when", "`" + Match.getRoundLong() + "`", "`" + EpochSecondToPattern(Instant.now().getEpochSecond(), "dd/MM/yyyy - HH:mm") + "`"));
                                                if (Match.getDeadline() != null) {
                                                    Button addDay = Button.success(CMD.Command("challonge-manage-deadline-add-1-day"), "+1 " + TL(M,"day"));
                                                    Button addHour = Button.success(CMD.Command("challonge-manage-deadline-add-1-hour"), "+1 " + TL(M,"hour"));
                                                    Button btn = Button.danger(CMD.Command("challonge-manage-deadline-warn-players"), TL(M,"challonge-manage-deadline-warn-players")).withEmoji(Emoji.fromUnicode("U+1F514"));
                                                    l.add(addDay);
                                                    l.add(addHour);
                                                    l.add(btn);
                                                    E.setDescription(TL(M,"deadline-description-when", "`" + Match.getRoundLong() + "`", "`" + EpochSecondToPattern(Instant.now().getEpochSecond(), "dd/MM/yyyy - HH:mm") + "`"
                                                            + "\nDeadline: `" + EpochSecondToPattern(Match.getDeadline().getEpochSecond(), "dd/MM/yyyy - HH:mm") + "`"));
                                                }
                                            }

                                            List<SelectOption> Op = new ArrayList<>();

                                            Op.add(SelectOption.of("Custom", "Custom").withDescription(TL(M,"deadline-custom")));

                                            Instant instant = Instant.now();
                                            String time = String.valueOf(PatternToEpochSecond(getDDMMYYYY(instant) + " - 23:59", "dd/MM/yyyy - HH:mm"));
                                            Op.add(SelectOption.of(TL(M,"Tonight"), time)
                                                    .withDescription(TL(M,"deadline-time", EpochSecondToPattern(Long.parseLong(time), "dd/MM/yyyy - HH:mm"))));

                                            instant = Instant.now().plus(1, ChronoUnit.DAYS);
                                            time = String.valueOf(PatternToEpochSecond(getDDMMYYYY(instant) + " - 23:59", "dd/MM/yyyy - HH:mm"));
                                            Op.add(SelectOption.of(TL(M,"Tomorrow"), time)
                                                    .withDescription(TL(M,"deadline-time", EpochSecondToPattern(Long.parseLong(time), "dd/MM/yyyy - HH:mm"))));

                                            instant = Instant.now().plus(2, ChronoUnit.DAYS);
                                            time = String.valueOf(PatternToEpochSecond(getDDMMYYYY(instant) + " - 23:59", "dd/MM/yyyy - HH:mm"));
                                            Op.add(SelectOption.of(getDayOfTheWeek(instant), time)
                                                    .withDescription(TL(M,"deadline-time", EpochSecondToPattern(Long.parseLong(time), "dd/MM/yyyy - HH:mm"))));

                                            instant = Instant.now().plus(3, ChronoUnit.DAYS);
                                            time = String.valueOf(PatternToEpochSecond(getDDMMYYYY(instant) + " - 23:59", "dd/MM/yyyy - HH:mm"));
                                            Op.add(SelectOption.of(getDayOfTheWeek(instant), time)
                                                    .withDescription(TL(M,"deadline-time", EpochSecondToPattern(Long.parseLong(time), "dd/MM/yyyy - HH:mm"))));

                                            instant = Instant.now().plus(4, ChronoUnit.DAYS);
                                            time = String.valueOf(PatternToEpochSecond(getDDMMYYYY(instant) + " - 23:59", "dd/MM/yyyy - HH:mm"));
                                            Op.add(SelectOption.of(getDayOfTheWeek(instant), time)
                                                    .withDescription(TL(M,"deadline-time", EpochSecondToPattern(Long.parseLong(time), "dd/MM/yyyy - HH:mm"))));

                                            instant = Instant.now().plus(5, ChronoUnit.DAYS);
                                            time = String.valueOf(PatternToEpochSecond(getDDMMYYYY(instant) + " - 23:59", "dd/MM/yyyy - HH:mm"));
                                            Op.add(SelectOption.of(getDayOfTheWeek(instant), time)
                                                    .withDescription(TL(M,"deadline-time", EpochSecondToPattern(Long.parseLong(time), "dd/MM/yyyy - HH:mm"))));

                                            instant = Instant.now().plus(6, ChronoUnit.DAYS);
                                            time = String.valueOf(PatternToEpochSecond(getDDMMYYYY(instant) + " - 23:59", "dd/MM/yyyy - HH:mm"));
                                            Op.add(SelectOption.of(getDayOfTheWeek(instant), time)
                                                    .withDescription(TL(M,"deadline-time", EpochSecondToPattern(Long.parseLong(time), "dd/MM/yyyy - HH:mm"))));

                                            StringSelectMenu menu = StringSelectMenu.create(CMD.Command("challonge-manage-deadline-time"))
                                                    .setPlaceholder(Op.get(1).getLabel()).setRequiredRange(1, 1).addOptions(Op).build();
                                            if (!l.isEmpty()) {
                                                M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu), ActionRow.of(l)).queue();
                                            } else {
                                                M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu)).queue();
                                            }
                                        });
                                    }
                                    if (event.getComponentId().startsWith("challonge-manage-deadline-time")) {
                                        if (InteractionIDs.getFirst().equals("Custom")) {
                                            Label input = Label.of("Deadline", TextInput.create("deadline", TextInputStyle.SHORT)
                                                    .setPlaceholder("dd/MM/yyyy - HH:mm")
                                                    .setRequiredRange(18, 18)
                                                    .setRequired(false)
                                                    .build());
                                            event.replyModal(Modal.create(CMD.Command("challonge-manage-deadline-set-custom"), "Set Deadline of " + T.getRoundLong(CMD.Round, CMD.GroupID))
                                                    .addComponents(input).build()).queue();
                                        } else {
                                            event.deferReply(true).queue(M -> {
                                                EmbedBuilder E = T.getTournamentEmbed();
                                                for (BaseCMatch<?,?,?> Match : T.getMatches(CMD.Round, CMD.GroupID)) {
                                                    Match.setDeadline(Instant.ofEpochSecond(Long.parseLong(InteractionIDs.getFirst())));
                                                    Match.Update();
                                                }
                                                E.setTitle("Deadline");
                                                E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-deadline-success", "**" + T.getRoundLong(CMD.Round, CMD.GroupID) + "**", "<t:" + InteractionIDs.getFirst() + ":R>"));
                                                Button btn = Button.success(CMD.Command("challonge-manage-deadline-warn-players"), TL(M,"challonge-manage-deadline-warn-players")).withEmoji(Emoji.fromUnicode("U+1F514"));
                                                M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(btn)).queue();
                                            });
                                        }
                                    }
                                }

                                else if (event.getComponentId().startsWith("challonge-manage-add")) {
                                    if (event.getComponentId().startsWith("challonge-manage-add-score")) {
                                        if (T.getVSAmount() > 1) {
                                            SubMatch CSM = SubMatch.get(Long.parseLong(InteractionIDs.getFirst()));
                                            Label P1Score = Label.of(CSM.getP1().getEffectiveName(), TextInput.create("score-1", TextInputStyle.SHORT)
                                                    .setPlaceholder("0")
                                                    .setRequiredRange(0, 2)
                                                    .setRequired(false)
                                                    .build());
                                            Label P2Score = Label.of(CSM.getP2().getEffectiveName(), TextInput.create("score-2", TextInputStyle.SHORT)
                                                    .setPlaceholder("0")
                                                    .setRequiredRange(0, 2)
                                                    .setRequired(false)
                                                    .build());
                                            CMD.MatchID = CSM.getId();
                                            event.replyModal(Modal.create(CMD.Command("challonge-manage-add-score"), "Set score of Match")
                                                    .addComponents(P1Score, P2Score).build()).queue();
                                        } else {
                                            BaseCMatch<?,?,?> M = T.getMatchById(Long.parseLong(InteractionIDs.getFirst().split("/")[0]));
                                            Label P1Score = Label.of(M.getPlayer1().getName(), TextInput.create("score-1", TextInputStyle.SHORT)
                                                    .setPlaceholder("0")
                                                    .setRequiredRange(0, 2)
                                                    .setRequired(false)
                                                    .build());
                                            Label P2Score = Label.of(M.getPlayer2().getName(), TextInput.create("score-2", TextInputStyle.SHORT)
                                                    .setPlaceholder("0")
                                                    .setRequiredRange(0, 2)
                                                    .setRequired(false)
                                                    .build());
                                            CMD.MatchID = M.getId();
                                            event.replyModal(Modal.create(CMD.Command("challonge-manage-add-score"), "Set score of Match")
                                                    .addComponents(P1Score, P2Score).build()).queue();
                                        }
                                    }
                                    else if (event.getComponentId().startsWith("challonge-manage-add-predi") && T instanceof SChallonge_Tournament CT) {
                                        SChallonge_Match M = CT.getMatchById(Long.parseLong(InteractionIDs.getFirst()));
                                        SChallonge_Participant P1 = M.getPlayer1();
                                        SChallonge_Participant P2 = M.getPlayer2();
                                        if (P1 != null && P2 != null && P1.getLeader() != null && P2.getLeader() != null) {
                                            TextChannel PredictionChannel = CT.getPredictionChannel();
                                            if (PredictionChannel != null) {
                                                event.deferReply(true).queue(Message -> {
                                                    try {
                                                        M.setPredictionMessage(MakePrediction(event.getGuild()
                                                                , P1.getLeader()
                                                                , P2.getLeader()
                                                                , PredictionChannel));
                                                        M.Update();
                                                        Message.editOriginal(TL(Message, "tournament-add-predi-success", "**" + M.getPlayer1().getName() + " vs. " + M.getPlayer2().getName() + "**")).queue();
                                                    } catch (Exception ignored) {   }
                                                });
                                            } else {
                                                event.reply(TL(event, "tournament-add-predi-fail-no-channel")).setEphemeral(true).queue();
                                            }
                                        } else {
                                            event.reply(TL(event, "tournament-add-predi-fail-no-players")).setEphemeral(true).queue();
                                        }
                                    }
                                }
                                else if (event.getComponentId().startsWith("challonge-edit")) {
                                    if (event.getComponentId().startsWith("challonge-edit-score")) {
                                        BaseCMatch<?,?,?> M = T.getMatchById(Long.parseLong(InteractionIDs.getFirst()));
                                        Label P1Score = Label.of(M.getPlayer1().getName(), TextInput.create("score-1", TextInputStyle.SHORT)
                                                .setPlaceholder("0")
                                                .setRequiredRange(0, 2)
                                                .setRequired(false)
                                                .build());
                                        Label P2Score = Label.of(M.getPlayer2().getName(), TextInput.create("score-2", TextInputStyle.SHORT)
                                                .setPlaceholder("0")
                                                .setRequiredRange(0, 2)
                                                .setRequired(false)
                                                .build());
                                        CMD.MatchID = M.getId();
                                        event.replyModal(Modal.create(CMD.Command("challonge-edit-score"), "Modify score of a Match")
                                                .addComponents(P1Score, P2Score).build()).queue();
                                    }
                                    else if (event.getComponentId().startsWith("challonge-edit-random")) {
                                        BaseCMatch<?,?,?> CM = T.getMatchById(Long.parseLong(InteractionIDs.getFirst()));
                                        event.deferReply(true).queue(M -> {
                                            CM.GenerateSubMatchesIfNeeded(true);
                                            if (T instanceof SChallonge_Tournament CT) CT.RefreshPanelMessage();
                                            M.editOriginal(TL(M, "Done") + "!").queue();
                                        });
                                    }
                                    else if (event.getComponentId().startsWith("challonge-edit-participant")) {
                                        event.deferReply(true).queue(M -> {
                                            CMD.ParticipantID = Long.parseLong(InteractionIDs.getFirst());
                                            BaseCParticipant<?,?,?> P = T.getParticipantById(CMD.ParticipantID);
                                            Button btn1 = Button.success(CMD.Command("challonge-edit-participant-name"), TL(M,"Name"));
                                            Button btn2 = Button.success(CMD.Command("challonge-edit-participant-seed"), "Seed").withDisabled(!T.isPending());
                                            Button btn3 = Button.success(CMD.Command("challonge-edit-participant-delete"), TL(M,"Delete")).withDisabled(!T.isPending());
                                            Button btn4 = Button.success(CMD.Command("challonge-edit-participant-id"), TL(M,"account-by-id"));
                                            EntitySelectMenu menu = EntitySelectMenu.create(CMD.Command("challonge-edit-participant"), EntitySelectMenu.SelectTarget.USER)
                                                    .setRequiredRange(1, 1).setPlaceholder(event.getUser().getEffectiveName()).build();
                                            M.editOriginalEmbeds(P.getModificationUI(M).build()).setComponents(ActionRow.of(btn1, btn2, btn3, btn4), ActionRow.of(menu)).queue();
                                        });
                                    }
                                }
                            } else {
                                event.reply(TL(event,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).setEphemeral(true).queue();
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
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        if (event.isFromGuild() && !event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("challonge")) {
                        ChallongeCommand CMD = new ChallongeCommand(event.getComponentId());
                        BaseCTournament<?,?,?> T = BaseCTournament.get(CMD.ChallongeID);
                        if (T instanceof SChallonge_Tournament CT) CT.I = ServerInfo.get(CT.ServerID);
                        if (event.getComponentId().startsWith("challonge-register-teammates")) {
                            if (T.getVSAmount() > 1) {
                                if (event.getMentions().getUsers().stream().noneMatch(u -> u.getIdLong() == event.getUser().getIdLong())) {
                                    event.deferEdit().queue(M -> SendTeamRequests(event, M, T));
                                } else {
                                    event.reply(TL(event, "user-other-than-yourself", T.getVSAmount()-1)).setEphemeral(true).queue();
                                }
                            }
                        }
                        else if (T.isOrganiser(event.getUser())) {
                            if (event.getComponentId().startsWith("challonge-manage-add-participant")) {
                                event.deferEdit().queue(M -> {
                                    try {
                                        String names = "";
                                        for (Member m : event.getMentions().getMembers()) {
                                            if (!m.getUser().isBot()) {
                                                T.AddParticipantFromInscription(null, m.getUser(), true);
                                                names = names + ", " + m.getUser().getAsMention();
                                            }
                                        }
                                        M.editOriginal(":white_check_mark: " + TL(M, "challonge-manage-participant-add-success", "**" + names.replaceFirst(", ", "") + "**")).queue();
                                        if (T instanceof SChallonge_Tournament CT) CT.I.LogSlash(TLG(event.getGuild(), "log-tournament-participant-5", "**" + event.getUser().getEffectiveName() + "**", "**" + names.replaceFirst(", ", "") + "**"));
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().startsWith("challonge-edit-participant")) {
                                event.deferEdit().queue(M -> {
                                    try {
                                        BaseCParticipant<?,?,?> P = T.getParticipantById(CMD.ParticipantID);
                                        P.setLeaderID(event.getMentions().getUsers().getFirst().getIdLong());
                                        M.editOriginalEmbeds(P.getModificationUI(M).build()).setContent(":white_check_mark: " + TL(M, "challonge-manage-participant-discord-success", event.getMentions().getUsers().getFirst().getAsMention())).queue();
                                        if (T instanceof SChallonge_Tournament CT) CT.I.LogSlash(TLG(event.getGuild(), "log-tournament-participant-2", "**" + event.getUser().getEffectiveName() + "**", P.getName(), "**" + event.getMentions().getUsers().getFirst().getEffectiveName() + "**"));
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().startsWith("challonge-select") && T instanceof SChallonge_Tournament CT) {
                                event.deferEdit().queue(M -> {
                                    try {
                                        Permission[] Ps = {Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_HISTORY};

                                        if (event.getComponentId().contains("panel-channel")) {
                                            if (isChannelOfType(M, event.getMentions().getChannels(TextChannel.class).getFirst(), ChannelType.TEXT) && hasPermissionInChannel(M, event.getMentions().getChannels(TextChannel.class).getFirst(), Ps)) {
                                                CT.setPanelChannel(event.getMentions().getChannels(TextChannel.class).getFirst());
                                            }
                                        } else if (event.getComponentId().contains("result-channel")) {
                                            if (isChannelOfType(M, event.getMentions().getChannels(TextChannel.class).getFirst(), ChannelType.TEXT) && hasPermissionInChannel(M, event.getMentions().getChannels(TextChannel.class).getFirst(), Ps)) {
                                                CT.setResultChannel(event.getMentions().getChannels(TextChannel.class).getFirst());
                                            }
                                        } else if (event.getComponentId().contains("rules-channel")) {
                                            CT.setRulesChannel(event.getMentions().getChannels(TextChannel.class).getFirst());
                                        } else if (event.getComponentId().contains("prediction-channel")) {
                                            if (isChannelOfType(M, event.getMentions().getChannels(TextChannel.class).getFirst(), ChannelType.TEXT) && hasPermissionInChannel(M, event.getMentions().getChannels(TextChannel.class).getFirst(), Ps)) {
                                                CT.setPredictionChannel(event.getMentions().getChannels(TextChannel.class).getFirst());
                                            }
                                        } else if (event.getComponentId().contains("organiser-role")) {
                                            CT.setOrganizerRole(event.getMentions().getRoles().getFirst());
                                        } else if (event.getComponentId().contains("participant-role")) {
                                            if (CT.I.getGuild().getMembersWithRoles(event.getMentions().getRoles().getFirst()).isEmpty()) {
                                                if (hasPermissionOverRole(M, event.getMentions().getRoles().getFirst())) {
                                                    CT.setParticipantRole(event.getMentions().getRoles().getFirst());
                                                }
                                            } else {
                                                M.editOriginal(":x: " + TL(M, "tournament-role-fail")).queue();
                                            }
                                        }
                                        CT.Update();
                                        CT.TournamentChannelsSelectMenu(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                        } else {
                            event.reply(TL(event,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).setEphemeral(true).queue();
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
        if (event.isFromGuild() && !event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getModalId().startsWith("challonge")) {
                        ServerInfo I = ServerInfo.get(event.getGuild());
                        ChallongeCommand CMD = new ChallongeCommand(event.getModalId());
                        BaseCTournament<?,?,?> T = BaseCTournament.get(CMD.ChallongeID);
                        if (T instanceof SChallonge_Tournament CT) CT.I = ServerInfo.get(CT.ServerID);
                        if (event.getModalId().startsWith("challonge-create-score-reporting")) {
                            event.deferReply().queue(M -> {
                                BaseCMatch<?,?,?> CM = T.getMatchById(CMD.MatchID);
                                int P1Score = Integer.parseInt(event.getValue("score-1").getAsString());
                                int P2Score = Integer.parseInt(event.getValue("score-2").getAsString());
                                if (T.getVSAmount() > 1) {
                                    SubMatch CSM = CM.getSubMatch(event.getUser().getIdLong());
                                    if (event.getUser().getIdLong() == CSM.getP2ID()) {
                                        createChallongeScore(M, T, CSM.getP2(), P2Score, P1Score, CSM.getP1());
                                    } else {
                                        createChallongeScore(M, T, CSM.getP1(), P1Score, P2Score, CSM.getP2());
                                    }
                                } else {
                                    if (event.getUser().getIdLong() == CM.getPlayer2().getLeaderID()) {
                                        createChallongeScore(M, T, CM.getPlayer2().getLeader(), P2Score, P1Score, CM.getPlayer1().getLeader());
                                    } else {
                                        createChallongeScore(M, T, CM.getPlayer1().getLeader(), P1Score, P2Score, CM.getPlayer2().getLeader());
                                    }
                                }
                            });
                        }

                        else if (event.getModalId().startsWith("challonge-manage")) {
                            if (T.isOrganiser(event.getUser())) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        if (event.getModalId().contains("challonge-manage-deadline-set-custom")) {
                                            EmbedBuilder E = new EmbedBuilder();
                                            for (BaseCMatch<?,?,?> Match : T.getMatches( CMD.Round, CMD.GroupID)) {
                                                Match.setDeadline(Instant.ofEpochSecond(PatternToEpochSecond(event.getValue("deadline").getAsString(), "dd/MM/yyyy - HH:mm")));
                                                Match.Update();
                                            }
                                            E.setTitle("Deadline");
                                            E.setColor(I.getColor());
                                            E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-deadline-success", "**" + T.getRoundLong(CMD.Round, CMD.GroupID) + "**", "<t:" + PatternToEpochSecond(event.getValue("deadline").getAsString(), "dd/MM/yyyy - HH:mm") + ":R>"));
                                            Button btn = Button.success(CMD.Command("challonge-manage-deadline-warn-players"), TL(M,"challonge-manage-deadline-warn-players")).withEmoji(Emoji.fromUnicode("U+1F514"));
                                            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(btn)).queue();
                                        }
                                        else if (event.getModalId().startsWith("challonge-manage-send-dm")) {
                                            String message = "**[" + T.getName() + "] " + event.getUser().getEffectiveName() + ":** " + event.getValue("message").getAsString();
                                            CMD.MatchID = Long.parseLong(event.getValue("opponent").getAsString());
                                            BaseCParticipant<?,?,?> P = T.getTeamByMyId(event.getUser().getIdLong());
                                            if (T.getVSAmount() > 1) {
                                                SubMatch CSM = SubMatch.get(CMD.MatchID);
                                                sendPrivateMessage(CSM.getOpponent(event.getUser().getIdLong()), message);
                                            } else {
                                                BaseCMatch<?,?,?> CM = T.getMatchById(CMD.MatchID);
                                                sendPrivateMessage(CM.getOpponent(P.getId()).getLeader(), message);
                                            }
                                        }
                                        else if (event.getModalId().startsWith("challonge-manage-add")) {
                                            if (event.getModalId().startsWith("challonge-manage-add-score")) {
                                                int P1Score = Integer.parseInt(event.getValue("score-1").getAsString());
                                                int P2Score = Integer.parseInt(event.getValue("score-2").getAsString());
                                                CMD.MatchID = event.getValue("opponent") != null ? Long.parseLong(event.getValue("opponent").getAsString()) : CMD.MatchID;
                                                if (T.getVSAmount() > 1) {
                                                    SubMatch CSM = SubMatch.get(CMD.MatchID);
                                                    if (event.getUser().getIdLong() == CSM.getP2ID()) {
                                                        createChallongeScore(M, T, CSM.getP2(), P2Score, P1Score, CSM.getP1());
                                                    } else {
                                                        createChallongeScore(M, T, CSM.getP1(), P1Score, P2Score, CSM.getP2());
                                                    }
                                                } else {
                                                    BaseCMatch<?,?,?> CM = T.getMatchById(CMD.MatchID);
                                                    if (event.getUser().getIdLong() == CM.getPlayer2().getLeaderID()) {
                                                        createChallongeScore(M, T, CM.getPlayer2().getLeader(), P2Score, P1Score, CM.getPlayer1().getLeader());
                                                    } else {
                                                        createChallongeScore(M, T, CM.getPlayer1().getLeader(), P1Score, P2Score, CM.getPlayer2().getLeader());
                                                    }
                                                }
                                            }
                                        }
                                        else if (event.getModalId().startsWith("challonge-manage-name")) {
                                            T.setName(event.getValue("name").getAsString());
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(TL(M,"tournament-manager"));
                                            E.setColor(I.getColor());
                                            E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-name-success", "**" + T.getName() + "**"));
                                            M.editOriginalEmbeds(E.build()).queue();
                                            I.LogSlash(TLG(event.getGuild(), "log-tournament-rename", "**" + event.getUser().getEffectiveName() + "**", "**" + T.getName() + "**"));

                                        }
                                        else if (event.getModalId().startsWith("challonge-manage-desc")) {
                                            T.setDescription(event.getValue("description").getAsString());
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(TL(M,"tournament-manager"));
                                            E.setColor(I.getColor());
                                            E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-desc-success"));
                                            M.editOriginalEmbeds(E.build()).queue();
                                            I.LogSlash(TLG(event.getGuild(), "log-tournament-desc", "**" + event.getUser().getEffectiveName() + "**", "**" + StopString(T.getDescription(), 500) + "**"));

                                        }
                                        else if (event.getModalId().startsWith("challonge-manage-game")) {
                                            T.setGameName(Game.get(event.getValue("game").getAsString()));
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(TL(M,"tournament-manager"));
                                            E.setColor(I.getColor());
                                            E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-game-success", "**" + event.getValue("game").getAsString() + "**"));
                                            M.editOriginalEmbeds(E.build()).queue();
                                            I.LogSlash(TLG(event.getGuild(), "log-tournament-game", "**" + event.getUser().getEffectiveName() + "**", "**" + T.getGame().getFullName() + "**"));

                                        }
                                        else if (event.getModalId().startsWith("challonge-manage-signup-cap")) {
                                            T.setSignupCap(Integer.parseInt(takeOnlyNumberStr(event.getValue("signup-cap").getAsString())));
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(TL(M,"tournament-manager"));
                                            E.setColor(I.getColor());
                                            E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-cap-success", "**" + T.getSignupCap() + "**"));
                                            M.editOriginalEmbeds(E.build()).queue();
                                            I.LogSlash(TLG(event.getGuild(), "log-tournament-cap", "**" + event.getUser().getEffectiveName() + "**", "**" + T.getSignupCap() + "**"));

                                        }
                                        else if (event.getModalId().startsWith("challonge-manage-start-time")) {
                                            if (takeOnlyDigits(event.getValue("epoch-second").getAsString()) > Instant.now().getEpochSecond()) {
                                                T.setStartAtTime(Instant.ofEpochSecond(takeOnlyDigits(event.getValue("epoch-second").getAsString())));
                                                EmbedBuilder E = new EmbedBuilder();
                                                E.setTitle(TL(M, "tournament-manager"));
                                                E.setColor(I.getColor());
                                                E.setDescription(":white_check_mark: " + TL(M, "challonge-manage-registration-success", "**<t:" + T.getStartAtTime().getEpochSecond() + ":R>**"));
                                                M.editOriginalEmbeds(E.build()).queue();
                                                I.LogSlash(TLG(event.getGuild(), "log-tournament-registration-end", "**" + event.getUser().getEffectiveName() + "**", "**<t:" + T.getStartAtTime().getEpochSecond() + ":R>**"));
                                            } else {
                                                M.editOriginal(TL(M, "tournament-registration-fail-epoch-second")).queue();
                                            }
                                        }
                                        else if (event.getModalId().startsWith("challonge-manage-invite-link") && T instanceof SChallonge_Tournament CT) {
                                            if (event.getValue("invite-link").getAsString().contains("https://discord.gg/")) {
                                                CT.setInscriptionChannelInviteLink(event.getValue("invite-link").getAsString());
                                                EmbedBuilder E = new EmbedBuilder();
                                                E.setTitle(TL(M,"tournament-manager"));
                                                E.setColor(I.getColor());
                                                E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-invite-success", "**" + CT.getInscriptionChannelInviteLink() + "**"));
                                                M.editOriginalEmbeds(E.build()).queue();
                                                I.LogSlash(TLG(event.getGuild(), "log-tournament-invite", "**" + event.getUser().getEffectiveName() + "**", "**" + CT.getInscriptionChannelInviteLink() + "**"));
                                            } else {
                                                M.editOriginal(TL(M,"invalid-invite-link")).queue();
                                            }
                                        }
                                        T.Update();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else {
                                event.reply(TL(event,"reply-failed-not-enough-permission-you", "ADMINISTRATOR, ORGANISER")).setEphemeral(true).queue();
                            }
                        }

                        else if (event.getModalId().startsWith("challonge-edit")) {
                            if (event.getModalId().startsWith("challonge-edit-participant")) {
                                event.deferEdit().queue(M -> {
                                    try {
                                        BaseCParticipant<?,?,?> P = T.getParticipantById(CMD.ParticipantID);
                                        if (event.getModalId().startsWith("challonge-edit-participant-name")) {
                                            String oldname = P.getName();
                                            P.setName(event.getValue("name").getAsString());
                                            I.LogSlash(TLG(event.getGuild(), "log-tournament-participant-1", "**" + event.getUser().getEffectiveName() + "**", "**" + oldname + "**", "**" + P.getName() + "**"));
                                            M.editOriginalEmbeds(P.getModificationUI(M).build())
                                                    .setContent(":white_check_mark: " + TL(M,"challonge-manage-participant-name-success", "**" + P.getName() + "**"))
                                                    .queue();
                                        }
                                        else if (event.getModalId().startsWith("challonge-edit-participant-seed")) {
                                            if (isNumeric(event.getValue("seed").getAsString())) {
                                                int seed = Integer.parseInt(event.getValue("seed").getAsString());
                                                if (seed > 0 && seed <= T.getParticipantCount()) {
                                                    P.setSeed(seed);
                                                    I.LogSlash(TLG(event.getGuild(), "log-tournament-participant-3", "**" + event.getUser().getEffectiveName() + "**", "**" + P.getName() + "**", "**" + seed + "**"));
                                                    M.editOriginalEmbeds(P.getModificationUI(M).build())
                                                            .setContent(":white_check_mark: " + TL(M,"challonge-manage-participant-seed-success", "**" + seed + "**"))
                                                            .queue();
                                                } else {
                                                    M.editOriginal(TL(M, "seeding-fail", T.getParticipantCount())).queue();
                                                }
                                            } else {
                                                M.editOriginal(TL(M, "seeding-fail", T.getParticipantCount())).queue();
                                            }
                                        }
                                        else if (event.getModalId().contains("challonge-edit-participant-id")) {
                                            if (isNumeric(event.getValue("id").getAsString()) && getUserByID(event.getValue("id").getAsString()) != null) {
                                                P.setLeaderID(Long.parseLong(event.getValue("id").getAsString()));
                                                I.LogSlash(TLG(event.getGuild(), "log-tournament-participant-2", "**" + event.getUser().getEffectiveName() + "**", P.getName(), "**" + P.getLeader().getEffectiveName() + "**"));
                                                M.editOriginalEmbeds(P.getModificationUI(M).build())
                                                        .setContent(":white_check_mark: " + TL(M,"challonge-manage-participant-discord-success", P.getLeader().getEffectiveName()))
                                                        .queue();
                                            } else {
                                                M.editOriginal(TL(event, "seeding-fail", T.getParticipantCount())).queue();
                                            }
                                        }
                                        P.Update();
                                        T.ResyncChallonge();
                                        if (T instanceof SChallonge_Tournament CT) CT.RefreshPanelMessage();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            if (event.getModalId().startsWith("challonge-edit-team")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        BaseCParticipant<?,?,?> P = T.getParticipantById(CMD.ParticipantID);
                                        if (event.getModalId().startsWith("challonge-edit-team-name")) {
                                            String oldname = P.getName();
                                            P.setName(event.getValue("name").getAsString());
                                            I.LogSlash(TLG(event.getGuild(), "log-tournament-participant-1", "**" + event.getUser().getEffectiveName() + "**", "**" + oldname + "**", "**" + P.getName() + "**"));
                                            M.editOriginal(":white_check_mark: " + TL(M, "challonge-manage-participant-name-success", "**" + P.getName() + "**")).queue();
                                        }
                                        T.ResyncChallonge();
                                        if (T instanceof SChallonge_Tournament CT) CT.RefreshPanelMessage();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getModalId().startsWith("challonge-edit-match")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        BaseCMatch<?,?,?> Match = T.getMatchById(CMD.MatchID);
                                        int Score1 = takeOnlyInts(event.getValue("score-1").getAsString());
                                        int Score2 = takeOnlyInts(event.getValue("score-2").getAsString());
                                        Match.AddScore(Score1, Score2, null);
                                        Match.Update();
                                        EmbedBuilder E = new EmbedBuilder();
                                        E.setTitle(TL(M,"tournament-manager"));
                                        E.setColor(I.getColor());
                                        E.setDescription(":white_check_mark: " + TL(M,"challonge-manage-score-success-2", "**" + Match.getPlayer1().getName() + " vs. " + Match.getPlayer2().getName() + "**", "**" + Match.getP1Score() + "-" + Match.getP2Score() + "**"));
                                        M.editOriginalEmbeds(E.build()).queue();
                                        T.ResyncChallonge();
                                        if (T instanceof SChallonge_Tournament CT) CT.RefreshPanelMessage();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
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
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (false)
        if (event.isFromGuild() && event.getUser() != null && !isStartup && !Prefs.TestMode || isTournamentManager(event.getUser())) {
            if (hasPermissionInChannelNoLog(event.getGuildChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION)) {
                event.retrieveMessage().queue(M -> {
                    ExecutorService E = Executors.newCachedThreadPool();
                    ShutdownAfterAction(E, 1, "Registration", E.submit(() -> {
                        try {
                            if (M != null && (!M.getReactions().isEmpty())) {
                                if (Instant.now().isAfter(Instant.ofEpochSecond(RegistrationTimeout))) {
                                    SChallonge_Tournament T = DatabaseObject.doQuery(SChallonge_Tournament.class,"SELECT CT.* FROM inazuma_competitive.challonge_tournament CT " +
                                            "JOIN inazuma_competitive.serverinfo S ON S.ID = ? WHERE CT.InscriptionMessageID = ? LIMIT 1;", event.getGuild().getId(), event.getMessageId()).orElse(null);
                                    if (T != null) {
                                        ServerInfo I = ServerInfo.get(event.getGuild());
                                        RegistrationTimeout = Instant.now().plus(10, ChronoUnit.SECONDS).getEpochSecond();
                                        T.I = I;
                                        Profile P = Profile.get(event.getUser());
                                        if (T.isRegistrationOpen()) {
                                            if (T.isAccountOldEnough(event.getUser())) {
                                                if (I.getBlacklist(event.getUser(), T.getGame()) == null && I.getBlacklist(event.getUser(), null) == null) {
                                                    T.getInscriptionChannel().getMessage().getReactions().getFirst().retrieveUsers().takeWhileAsync(ignored -> true).whenComplete((users, throwable) -> {
                                                        if (users.size() >= 10) {
                                                            M.removeReaction(M.getReactions().getFirst().getEmoji()).queue();
                                                        }
                                                        for (User u : users) {
                                                            T.AddParticipantFromInscription(null, u, false);
                                                        }
                                                        T.RefreshInscriptionMessage();
                                                        T.RefreshPanelMessage();
                                                    });
                                                } else {
                                                    event.getReaction().removeReaction(event.getUser()).queue();
                                                    Blacklist BL = I.getBlacklist(event.getUser(), null);
                                                    if (BL == null) BL = I.getBlacklist(event.getUser(), T.getGame());
                                                    if (BL != null) sendPrivateMessage(event.getUser(), TL(P, "blacklist-attempt", "**" + BL.TournamentsLeft + "**", "**" + BL.getGame().getEmojiFormatted() + " " + BL.getGame().getName() + "**") + "\n- " + BL.Reason);
                                                }
                                            } else {
                                                event.getReaction().removeReaction(event.getUser()).queue();
                                                sendPrivateMessage(event.getUser(), TL(P, "join-tournament-fail", "`7`"));
                                            }
                                        } else if (hasPermissionInChannel(null, event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
                                            event.getReaction().removeReaction(event.getUser()).queue();
                                            sendPrivateMessage(event.getUser(), TL(P, "tournament-set-no-longer-pending"));
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }));
                });
            }
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.isFromGuild() && event.getUser() != null && !isStartup && !Prefs.TestMode || isTournamentManager(event.getUser())) {
            if (hasPermissionInChannelNoLog(event.getGuildChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION)) {
                event.retrieveMessage().queue(M -> {
                    ExecutorService E = Executors.newCachedThreadPool();
                    ShutdownAfterAction(E, 1, "Unregistration", E.submit(() -> {
                        try {
                            if (M != null && (!M.getReactions().isEmpty())) {
                                SChallonge_Tournament T = DatabaseObject.doQuery(SChallonge_Tournament.class, "SELECT CT.* FROM inazuma_competitive.challonge_tournament CT " +
                                        "JOIN inazuma_competitive.serverinfo S ON S.ID = ? WHERE CT.InscriptionMessageID = ? LIMIT 1;", event.getGuild().getId(), event.getMessageId()).orElse(null);
                                if (T != null) {
                                    ServerInfo I = ServerInfo.get(event.getGuild());
                                    if (T.isRegistrationOpen()) {
                                        T.I = I;
                                        T.DeleteParticipantFromInscription(null, event.getUser());
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }));
                });
            }
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (!isStartup && !Prefs.TestMode || isTournamentManager(event.getUser())) {
            try {
                ServerInfo I = ServerInfo.get(event.getGuild());
                User Target = event.getUser();
                for (SChallonge_Tournament T : I.getTournamentsOfUser(Target.getId())) {
                    T.CleanTournament();
                }
            } catch (Exception ignored) {}
        }
    }

    private static synchronized void JoinTournament(InteractionHook M, BaseCTournament<?,?,?> T) {
        if (T.getVSAmount() > 1) {
            T.AddTeamFromInscription(M, M.getInteraction().getUser());
        } else {
            T.AddParticipantFromInscription(M, M.getInteraction().getUser(), false);
        }
    }
    private static synchronized void JoinTeam(InteractionHook M, BaseCTournament<?,?,?> T, BaseCParticipant<?,?,?> P) {
        if (T.getVSAmount() > 1) {
            if (P != null && T.getParticipants().stream().noneMatch(Pp -> Pp.getAllTeammatesIDs().contains(M.getInteraction().getUser().getIdLong()))) {
                if (!P.isTeamFull()) {
                    P.AddTeammateID(M.getInteraction().getUser().getIdLong());
                    P.Update();
                    if (T instanceof SChallonge_Tournament CT) {
                        CT.I.Roles().AddRoleToMember(CT.getParticipantRole(), BotEmoji.get("icon_fan").getFormatted(), CT.I.getGuild().getMemberById(M.getInteraction().getUser().getIdLong()));
                        CT.RefreshInscriptionMessage();
                        CT.RefreshPanelMessage();
                    }
                    sendPrivateMessage(P.getLeader(), TL(P.getLeaderPf(), "clan-invite-confirm-success", M.getInteraction().getUser().getAsMention(), "**" + P.getName() + "**"));
                } else M.editOriginal(TL(M, "tournament-register-teammate-select-fail")).queue();
            } else M.editOriginal(TL(M, "clan-invite-confirm-expired")).setReplace(true).queue();
        } else M.deleteOriginal().queue();
    }
    private static synchronized void SendTeamRequests(@NotNull EntitySelectInteractionEvent event, InteractionHook M, BaseCTournament<?,?,?> T) {
        T.SendTeamRequests(M, event.getMentions().getUsers());
    }
    private static synchronized void UnregisterTournament(InteractionHook M, BaseCTournament<?,?,?> T) {
        T.DeleteParticipantFromInscription(M, M.getInteraction().getUser());
    }

    private static synchronized void StartTournament(InteractionHook M, BaseCTournament<?,?,?> T) {
        T.Start(M);
    }
    private static synchronized void CompleteTournament(InteractionHook M, BaseCTournament<?,?,?> T) {
        T.Complete(M);
    }


    private static void slashInfo(InteractionHook M, BaseCTournament<?,?,?> T) {
        EmbedBuilder E = new EmbedBuilder();


        M.editOriginalEmbeds(E.build()).queue();
    }
    private static void slashRules(InteractionHook M, BaseCTournament<?,?,?> T) {
        EmbedBuilder E = new EmbedBuilder();


        M.editOriginalEmbeds(E.build()).queue();
    }

    public static void createChallongeScore(InteractionHook M, BaseCTournament<?,?,?> T, User user, int myscore, int opponentscore, User opponent) {
        Button Confirmation = Button.success("match-rslt-conf", TL(M, "yes"));
        Button Decline = Button.danger("match-rslt-deny", TL(M, "no"));
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(T.getGame().getColor());
        embed.setTitle(TL(M,"score-reply-title"));
        embed.setDescription(TL(M, "score-reply-success-match", T.getGame().getName()) + " " + user.getAsMention() + " **" + myscore + "** " + T.getGame().getVSEmojiFormatted() + " **" + opponentscore + "** " + opponent.getAsMention()
                + "\n" + opponent.getAsMention() + ", " + TL(M, "score-reply-success-score-confirm") + "\n\n" +
                "**" + TL(M, "Tournament") + ":** " + T.getGame().getEmojiFormatted() + " " + T.getName());
        if (T instanceof GChallonge_Tournament) {
            opponent.openPrivateChannel().queue(chan -> {
                chan.sendMessageEmbeds(embed.build()).setContent(user.getAsMention() + " **" + myscore + "** " + T.getGame().getVSEmojiFormatted() + " **" + opponentscore + "** " + opponent.getAsMention()).setComponents(ActionRow.of(Confirmation, Decline)).queue(Mm -> {
                    MatchLog ML = new MatchLog(T.getGame(), user.getIdLong(), opponent.getIdLong(), myscore, opponentscore, Mm, null);
                    BaseCParticipant<?,?,?> P = T.getTeamByMyId(user.getIdLong());
                    if (P != null) P.getMatchWithOpponent(opponent.getIdLong(), false).setMatchLog(ML);
                    M.editOriginal(TL(M, "tournament-add-score-fail-dm-success")).queue();
                    LogChannel.sendMessage(MessageCreateBuilder.fromMessage(Mm).setContent("**[DM]** Sent to " + opponent.getEffectiveName() + " (" + opponent.getIdLong() + ") : " + Mm.getContentRaw()).build()).queue();
                }, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, error -> {
                    M.editOriginal(TL(M, "tournament-add-score-fail-dm", opponent.getAsMention() + " (" + opponent.getEffectiveName() + ")")).queue();
                    LogSlash("**[DM]** Failed to send score to " + opponent.getEffectiveName() + " (" + opponent.getId() + ") for " + T.getName());
                }));
            });
        } else if (T instanceof SChallonge_Tournament CT) {
            CT.getMatchResultChannel().sendMessageEmbeds(embed.build()).setContent(user.getAsMention() + " **" + myscore + "** " + T.getGame().getVSEmojiFormatted() + " **" + opponentscore + "** " + opponent.getAsMention()).setComponents(ActionRow.of(Confirmation, Decline)).queue(Mm -> {
                MatchLog ML = new MatchLog(T.getGame(), user.getIdLong(), opponent.getIdLong(), myscore, opponentscore, Mm, null);
                new MatchLog_S(T.getGame(), user.getIdLong(), opponent.getIdLong(), myscore, opponentscore, Mm);
                BaseCParticipant<?,?,?> P = T.getTeamByMyId(user.getIdLong());
                if (P != null) P.getMatchWithOpponent(opponent.getIdLong(), false).setMatchLog(ML);
                M.editOriginal(TL(M, "tournament-add-score-fail-dm-success")).queue();
            });
        }
    }
}