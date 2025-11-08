package iecompbot.interaction.listeners;

import at.stefangeyer.challonge.model.Tournament;
import at.stefangeyer.challonge.model.enumeration.TournamentState;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import iecompbot.Constants;
import iecompbot.img.builders.CardImageBuilder;
import iecompbot.img.builders.MatchResultImageBuilder;
import iecompbot.interaction.Automation;
import iecompbot.interaction.cmdbreakdown.InterclanCommand;
import iecompbot.interaction.cmdbreakdown.server.ChallongeCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.CommunityServer;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.clan.interclan.Interclan;
import iecompbot.objects.match.Game;
import iecompbot.objects.match.MatchLog;
import iecompbot.objects.match.MatchLog_S;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.Profile_Trophy;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.BaseCParticipant;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static iecompbot.Constants.POWERDECIMAL;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.*;
import static iecompbot.img.ImgUtilities.CircleAnImage;
import static iecompbot.img.ImgUtilities.fillPNG;
import static iecompbot.interaction.Automation.*;
import static iecompbot.interaction.GuildReady.*;
import static iecompbot.interaction.listeners.AdminFeatures.cleanChallongeURL;
import static iecompbot.objects.BotEmoji.CleanEmojis;
import static iecompbot.objects.BotManagers.*;
import static iecompbot.objects.Retrieval.getMessage;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.clan.Clan.*;
import static iecompbot.objects.clan.Clan.get;
import static iecompbot.objects.profile.quest.quest.Profile_Quest.*;
import static iecompbot.objects.server.ServerInfo.getClanUpdatesChannels;
import static my.utilities.util.Utilities.*;

public class MDFFeatures extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            try {
                if (event.getName().equals("support")) {
                    event.deferReply(true).queue(M -> {
                        Guild G = DiscordAccount.getGuildById("930718276542136400");
                        if (G != null) {
                            TextChannel Support = G.getTextChannelById("1269346946649297062");
                            String type = event.getOption("type-of-problem").getAsString();
                            String issue = event.getOption("issue").getAsString();
                            EmbedBuilder E = new EmbedBuilder();
                            E.setAuthor(event.getUser().getEffectiveName() + "'s Support Request");
                            E.setTitle(type + " Issue");
                            E.setThumbnail(event.getUser().getEffectiveAvatarUrl());
                            switch (type) {
                                case "Tournament" -> E.setColor(Color.ORANGE);
                                case "Profile" -> E.setColor(Color.CYAN);
                                case "Clan" -> E.setColor(Color.BLUE);
                                case "Complaint" -> E.setColor(Color.RED);
                                case "Other" -> E.setColor(Color.WHITE);
                            }
                            E.setDescription(issue);

                            E.addField("User ID", event.getUser().getId(), true);
                            E.addField("User Tag", "@" + event.getUser().getName(), true);

                            Button BTN =Button.success("respond-support/" + event.getUser().getId(), "Respond");
                            Support.sendMessageEmbeds(E.build()).setComponents(ActionRow.of(BTN)).queue();
                            M.editOriginal(TL(event, "Done")).queue();
                        }
                    });
                }
                if (event.getName().equals("report")) {
                    event.deferReply(true).queue(M -> {
                        Guild G = DiscordAccount.getGuildById("930718276542136400");
                        if (G != null) {
                            TextChannel Support = G.getTextChannelById("1269346946649297062");
                            String type = event.getOption("type-of-problem").getAsString();
                            User abuser = event.getOption("abuser").getAsUser();
                            String note = event.getOption("note").getAsString();
                            EmbedBuilder E = new EmbedBuilder();
                            E.setAuthor(event.getUser().getEffectiveName() + "'s Report");
                            E.setTitle(type + " Issue");
                            E.setThumbnail(event.getUser().getEffectiveAvatarUrl());
                            E.setColor(Color.RED);
                            E.setDescription("> " + note);

                            E.addField("Abuser ID", abuser.getId(), true);
                            E.addField("Abuser Tag", "@" + abuser.getName(), true);

                            E.addField("Reporter ID", event.getUser().getId(), true);
                            E.addField("Reporter Tag", "@" + event.getUser().getName(), true);

                            Button BTN = Button.success("respond-support/" + event.getUser().getId(), "Respond");
                            Support.sendMessageEmbeds(E.build()).setComponents(ActionRow.of(BTN)).queue();
                            M.editOriginal(TL(event, "Done")).queue();
                        }
                    });
                }
                if (event.getName().equals("suggestion")) {
                    event.deferReply(true).queue(M -> {
                        Guild G = DiscordAccount.getGuildById("930718276542136400");
                        if (G != null) {
                            TextChannel Support = G.getTextChannelById("1269346946649297062");
                            String suggestion = event.getOption("suggestion").getAsString();
                            EmbedBuilder E = new EmbedBuilder();
                            E.setAuthor(event.getUser().getEffectiveName() + "'s Report");
                            E.setTitle("Suggestion");
                            E.setThumbnail(event.getUser().getEffectiveAvatarUrl());
                            E.setColor(Color.RED);
                            E.setDescription("> " + suggestion);

                            E.addField("Reporter ID", event.getUser().getId(), true);
                            E.addField("Reporter Tag", "@" + event.getUser().getName(), true);

                            Support.sendMessageEmbeds(E.build()).queue();
                            M.editOriginal(TL(event, "Done")).queue();
                        }
                    });
                }

                if (event.getName().startsWith("mdf") || event.getName().startsWith("mdt") || event.getName().startsWith("mdc") || event.getName().startsWith("img")) {
                    UpdateStaffs();
                    if (event.getName().equals("mdf-points_calc")) {
                        event.reply("Wait...").queue(M -> {
                            String s = "";
                            double ThirdPoints = 100;
                            double LastPoints = 5;
                            double Participants = event.getOption("participants").getAsInt();

                            double PointsDifference = (ThirdPoints - LastPoints) / (Participants - 3);

                            s = s + ("1st. 300 (" + 300 * (Participants / 32) + "\\)") + "\n";
                            s = s + ("2st. 200 (" + 200 * (Participants / 32) + "\\)") + "\n";
                            s = s + ("3rd. 100 (" + 100 * (Participants / 32) + "\\)") + "\n";
                            int amountOfTimes = 1;
                            for (int i = 4; i < Participants + 1; i++) {
                                double Points = ThirdPoints - (PointsDifference * amountOfTimes);
                                double TruePoints = (ThirdPoints - (PointsDifference * amountOfTimes)) * (Participants / 32);
                                String Pts = POWERDECIMAL.format(Points);
                                String TPts = POWERDECIMAL.format(TruePoints);
                                s = s + (i + "th. " + Pts + " \\(" + TPts + "\\)") + "\n";
                                if (s.length() > 1950) {
                                    event.getChannel().sendMessage("```" + s + "```").queue();
                                    s = "";
                                }
                                amountOfTimes++;
                            }
                            if (s.length() > 0) {
                                event.getChannel().sendMessage("```" + s + "```").queue();
                            }
                        });
                    }
                    else if (event.getName().equals("mdf-url_test")) {
                        event.reply("Done!").queue(M -> {
                            String url = event.getOption("url").getAsString();
                            EmbedBuilder E = new EmbedBuilder();
                            E.setDescription(url);
                            E.addField("Is Valid?", (isURLValid(url) ? ":white_check_mark:" : ":x:"), false);
                            M.editOriginalEmbeds(E.build()).queue();
                        });
                    }

                    else if (event.getName().equals("img-batch-convert")) {
                        event.reply("Wait...").queue(m -> {
                            event.getOption("image").getAsAttachment().getProxy().downloadToFile(new File(MainDirectory + "/temp/dl.png")).whenComplete((file, throwable) -> {
                                event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, 100, 0, 100), "img.png")).queue(e1 -> {
                                    event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, 0, 10, 200), "img.png")).queue(e3 -> {
                                        event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, -200, 150, 10), "img.png")).queue(e4 -> {
                                            event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, -200, 0, 200), "img.png")).queue(e5 -> {
                                                event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, 200, 180, 40), "img.png")).queue(e6 -> {
                                                    event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, 200, 50, -50), "img.png")).queue(e7 -> {
                                                        event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, -200, -50, 50), "img.png")).queue(e8 -> {
                                                            event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, -220, 140, 180), "img.png")).queue(e9 -> {
                                                                event.getChannel().sendMessage("Done!").setFiles(FileUpload.fromData(fillPNG(file, -160, -10, 60), "img.png")).queue(e10 -> {
                                                                    m.editOriginal("Done !").queue();
                                                                });
                                                            });
                                                        });
                                                    });
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    }
                    else if (event.getName().equals("img-circle")) {
                        event.reply("Wait...").queue(m -> {
                            try {
                                File f = new File(TempDirectory + "/t.png");
                                if (event.getOption("image") == null) {
                                    ImageIO.write(CircleAnImage(ImageIO.read(URI.create(event.getGuild().getIconUrl()).toURL())), "png", f);
                                    m.editOriginal("Done !").setFiles(FileUpload.fromData(f, "file.png")).queue();
                                } else {
                                    if (event.getOption("image").getAsAttachment().getFileExtension().contains("png")
                                            || event.getOption("image").getAsAttachment().getFileExtension().contains("jpg")) {
                                        ImageIO.write(CircleAnImage(ImageIO.read(URI.create(event.getOption("image").getAsAttachment().getUrl()).toURL())), "png", f);
                                        m.editOriginal("Done !").setFiles(FileUpload.fromData(f, "file.png")).queue();
                                    }
                                }
                            } catch (IOException e) {
                                Automation.handleException(e);
                            }
                        });
                    }
                    else if (event.getName().equals("img-simulate_score")) {
                        event.reply("Okay !").queue(e -> {
                            MatchLog ML = new MatchLog();
                            ML.P1ID = event.getOption("user-1").getAsUser().getIdLong();
                            ML.P2ID = event.getOption("user-2").getAsUser().getIdLong();
                            ML.P1Score = event.getOption("score-1").getAsInt();
                            ML.P2Score = event.getOption("score-2").getAsInt();
                            ML.GameCode = Game.get(event.getOption("game").getAsString()).getCode();
                            MatchLog_S ML2 = new MatchLog_S();
                            ML2.P1ID = event.getOption("user-1").getAsUser().getIdLong();
                            ML2.P2ID = event.getOption("user-2").getAsUser().getIdLong();
                            ML2.P1Score = event.getOption("score-1").getAsInt();
                            ML2.P2Score = event.getOption("score-2").getAsInt();
                            ML2.GameCode = Game.get(event.getOption("game").getAsString()).getCode();
                            try (MatchResultImageBuilder MRIB = new MatchResultImageBuilder(event.getOption("user-1").getAsUser(), event.getOption("user-2").getAsUser(), ML, ML2, ServerInfo.get(event.getGuild()))) {
                                MRIB.GenerateMatchResultPNG();
                                FileUpload FU = FileUpload.fromData(MRIB.DownloadPNGToFile(), "score.png");
                                e.editOriginalAttachments(FU).queue();
                                FU.close();
                            } catch (Exception ignored) {}
                        });
                    }

                    if (isClanManager(event.getUser())) {
                        if (event.getName().equals("mdc-clan_news")) {
                            Clan clan = Clan.get(event.getOption("clan").getAsString());
                            User user = event.getOption("user").getAsUser();
                            event.deferReply().queue(M -> {
                                if (clan != null) {
                                    ClanMember CM = clan.getMemberById(user);
                                    CM.isMainClan = !event.getOption("is-reinforcement").getAsBoolean();
                                    switch (event.getOption("type").getAsString()) {
                                        case "New Member" -> clan.LogClanUpdatesNewMember(CM);
                                        case "Lose Member" -> clan.LogClanUpdatesKickMember(CM);
                                    }
                                }
                                M.editOriginal("Done!").queue();
                            });

                        }
                        else if (event.getName().equals("mdc-clan_card")) {
                            event.reply("Done!").setEphemeral(true).queue(M -> {
                                try {
                                    Clan clan = get(event.getOption("clan").getAsString());
                                    if (clan == null) clan = Clan.list().getFirst();
                                    User user = event.getOption("user").getAsUser();
                                    Profile P = Profile.get(user);

                                    try (CardImageBuilder CIB = new CardImageBuilder(P, clan)) {
                                        CIB.GenerateCardPNG();
                                        CIB.GenerateCardGIF(40, 40f);
                                        File PNG = new File(TempDirectory + "/png" + user.getId() + ".png");
                                        File GIF = new File(TempDirectory + "/png" + user.getId() + ".gif");

                                        event.getChannel().sendFiles(FileUpload.fromData(CIB.DownloadPNGToFile(PNG), "card.png")).queue();
                                        event.getChannel().sendFiles(FileUpload.fromData(CIB.DownloadPNGToFile(GIF), "card.gif")).queue();

                                        String pfpurl = getFileUrl(user.getEffectiveAvatarUrl(), "pfp.png");

                                        {
                                            Clan finalClan = clan;
                                            getWebhookOfChannel(event.getGuild().getTextChannelById(event.getChannel().getId()), client -> {
                                                String description = "# " + BotEmoji.get("Members").getFormatted() + " " + TLG(event.getGuild(), "clan-news-member") + "\n";
                                                description = description + "> " + TLG(event.getGuild(), "clan-news-member-description", "**" + user.getEffectiveName() + "**", "**" + finalClan.getEmojiFormatted() + " " + finalClan.getName() + "**") + "\n";
                                                description = description + "> " + TLG(event.getGuild(), "clan-news-member-members", String.valueOf(finalClan.getClanMembers().size())) + "\n";
                                                description = description + "`                                                       `\n";

                                                WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                                                embed.setDescription(description);
                                                embed.setThumbnailUrl(pfpurl);
                                                embed.setColor(finalClan.getColor().getRGB());
                                                embed.setFooter(new WebhookEmbed.EmbedFooter(finalClan.getName(), null));
                                                embed.setTimestamp(Instant.now());

                                                WebhookMessageBuilder builde = new WebhookMessageBuilder()
                                                        .setUsername(finalClan.getName())
                                                        .setAvatarUrl(finalClan.getEmblemURL())
                                                        .addEmbeds(embed.build());
                                                client.send(builde.build());


                                                description = "# :door: " + TLG(event.getGuild(), "clan-news-member-leave") + "\n";
                                                description = description + "> " + TLG(event.getGuild(), "clan-news-member-leave-description", "**" + P.getNationality().getFlag().getFormatted() + " " + user.getEffectiveName() + "**", "**" + finalClan.getEmojiFormatted() + " " + finalClan.getName() + "**") + "\n";
                                                description = description + "> " + TLG(event.getGuild(), "clan-news-member-members", String.valueOf(finalClan.getClanMembers().size())) + "\n";
                                                description = description + "`                                                       `\n";


                                                embed = new WebhookEmbedBuilder();
                                                embed.setDescription(description);
                                                embed.setThumbnailUrl(pfpurl);
                                                embed.setColor(finalClan.getColor().getRGB());
                                                embed.setFooter(new WebhookEmbed.EmbedFooter(finalClan.getName(), null));
                                                embed.setTimestamp(Instant.now());

                                                builde = new WebhookMessageBuilder()
                                                        .setUsername(finalClan.getName())
                                                        .setAvatarUrl(finalClan.getEmblemURL())
                                                        .addEmbeds(embed.build());
                                                client.send(builde.build());
                                            });
                                        }
                                    }
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                    }
                    if (isTournamentManager(event.getUser())) {
                        if (event.getName().equals("mdt-score")) {
                            event.deferReply().queue(M -> {
                                User user = event.getOption("p1").getAsUser();
                                User opponent = event.getOption("p2").getAsUser();
                                int myscore = event.getOption("p1-score").getAsInt();
                                int opponentscore = event.getOption("p2-score").getAsInt();
                                Game game = Game.get(event.getOption("game").getAsString());

                                createForcedScore(M, game, user, myscore, opponentscore, opponent, event.getChannel().asTextChannel());
                                M.deleteOriginal().queue();
                            });
                        }
                        else if (event.getName().equals("mdt-revert-score")) {
                            User user = event.getOption("p1").getAsUser();
                            User opponent = event.getOption("p2").getAsUser();
                            int myscore = event.getOption("p1-score").getAsInt();
                            int opponentscore = event.getOption("p2-score").getAsInt();
                            Game game = Game.get(event.getOption("game").getAsString());

                            MatchLog Log = MatchLog.getLog(user.getIdLong(), opponent.getIdLong(), myscore, opponentscore, game);
                            MatchLog_S Log2 = MatchLog_S.getLog(user.getIdLong(), opponent.getIdLong(), myscore, opponentscore, game);
                            if (Log != null) {
                                event.deferReply().addComponents(ActionRow.of(Button.secondary("nothing", TL(event, "Processing")).asDisabled())).queue(M -> {
                                    String p1name = user.getEffectiveName();
                                    String p2name = opponent.getEffectiveName();

                                    Log.removeRewards();
                                    Log.Delete();

                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setColor(Log.getGame().getColor());
                                    E.setThumbnail(Log.getGame().getImageUrl());
                                    E.addField(p1name, BotEmoji.get("XP") + "**__" + -Log.getP1XP() + "__**\n" +
                                            ":medal: **__" + -Log.getP1Medals() + "__**\n" +
                                            BotEmoji.get("InaCoin") + " **__" + -Log.getP1Coins() + "__**", true);
                                    E.addField(p2name, BotEmoji.get("XP") + "**__" + -Log.getP2XP() + "__**\n" +
                                            ":medal: **__" + -Log.getP2Medals() + "__**\n" +
                                            BotEmoji.get("InaCoin") + " **__" + -Log.getP2Coins() + "__**", true);

                                    if (Log2 != null) {
                                        Log2.removeRewards();
                                        Log2.Delete();
                                        E.addField(ServerInfo.get(Log2.getServerID()).getName(), "P1: **" + -Log2.getP1Medals() + "** :medal:\nP2: **" + -Log2.getP2Medals() + "** :medal:", false);
                                    }
                                    E.setDescription(TL(M, "result-reverted", "**" + p1name + "**", "**" + p2name + "**"));
                                    E.setTitle(p1name + " " + Log.getP1Score() + " " + Log.getGame().getVSEmojiFormatted() + " " + Log.getP2Score() + " " + p2name);
                                    M.editOriginalEmbeds(E.build()).setReplace(true).queue();
                                });
                            } else {
                                event.reply(TL(event, "result-reverted-fail")).queue();
                            }
                        }
                        else if (event.getName().equals("mdt-new_interclan")) {
                            event.reply("Done!").queue(M -> {
                                Clan C1 = Clan.get(event.getOption("clan-1").getAsLong());
                                Clan C2 = Clan.get(event.getOption("clan-2").getAsLong());
                                Interclan IC = new Interclan(Instant.now().toEpochMilli(), C1, C2, "N/A", 3);
                                IC.setState(TournamentState.COMPLETE);
                                IC.Update();
                                InterclanCommand CMD = new InterclanCommand(event);
                                CMD.HostClan = C1.getID();
                                CMD.JoinClan = C2.getID();
                                InterclanViewer(IC, M);
                                Button BTN1 = Button.primary(CMD.Command("interclan-add-duel"), "Add Duel");
                                Button BTN2 = Button.primary(CMD.Command("interclan-set-date"), "Set Date");
                                M.editOriginalComponents(ActionRow.of(BTN1, BTN2)).queue();
                            });
                        }
                        else if (event.getName().equals("mdt-cooldown_remove")) {
                            event.reply("Okay !").queue(M -> {
                                User u = event.getOption("user").getAsUser();
                                Profile P = Profile.get(u);
                                P.RefreshScoreTimeout(Instant.now());
                                P.RefreshMatchmakingTimeout(Instant.now());
                            });
                        }
                        else if (event.getName().equals("mdt-add_reward")) {
                            event.reply("Okay !").queue(M -> {
                                User u = event.getOption("user").getAsUser();
                                String emoji = event.getOption("emoji").getAsString();
                                String name = event.getOption("name").getAsString();
                                String desc = event.getOption("description").getAsString();
                                String game = event.getOption("game").getAsString();
                                double power = event.getOption("power").getAsDouble();
                                new Profile_Trophy(u.getIdLong(), emoji, name, desc, Game.get(game), power);
                            });
                        }
                        else if (event.getName().equals("mdt-challonge_refresh")) {
                            event.reply(TL(event,"Done")).queue(M -> {
                                try {
                                    Tournament T = ChallongeAccount.getTournament(cleanChallongeURL(event.getOption("server").getAsString()), true, true);
                                    if (T != null) {
                                        SChallonge_Tournament CT = SChallonge_Tournament.get(T.getId());
                                        if (CT != null) {
                                            CT.I = ServerInfo.get(event.getOption("serverid").getAsString());
                                            CT.ResyncChallonge();
                                        } else {
                                            CT = new SChallonge_Tournament(ServerInfo.get(event.getOption("serverid").getAsString()),T);
                                        }
                                        CT.TournamentManageUI(M, new ChallongeCommand(CT.getId()));
                                    } else {
                                        M.editOriginal("Can't find tournament...").queue();
                                    }
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getName().equals("mdt-give_match_quest")) {
                            event.deferReply(true).queue(M -> {
                                User user = event.getOption("user").getAsUser();
                                User opponent = event.getOption("opponent").getAsUser();
                                String type = event.getOption("type") != null ? event.getOption("type").getAsString() : "PLAY";
                                Game G = event.getOption("game") != null ? Game.get(event.getOption("game").getAsString()) : null;
                                int coins = event.getOption("coins-reward") != null ? event.getOption("coins-reward").getAsInt() : 1;

                                try {
                                    Profile P = Profile.get(user);
                                    switch (type) {
                                        case "PLAY" -> AddDuelQuest(P, opponent, G, List.of(Item.Item_Count.of(1, coins)));
                                        case "WIN" -> AddWinDuelQuest(P, opponent, G, List.of(Item.Item_Count.of(1, coins)));
                                        case "TIE" -> AddTieDuelQuest(P, opponent, G, List.of(Item.Item_Count.of(1, coins)));
                                        case "LOSE" -> AddLoseDuelQuest(P, opponent, G, List.of(Item.Item_Count.of(1, coins)));
                                    }
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                                M.deleteOriginal().queue();
                            });
                        }
                    }

                    if (isBotOwner(event.getUser())) {
                        if (event.getName().equals("mdf-list_data")) {
                            event.deferReply().queue(m -> {
                                int i = 0;
                                switch (event.getOption("data-type").getAsString()) {
                                    case "Tournaments" -> {
                                        for (ServerInfo I : ServerInfo.list(false)) {
                                            if (event.getOption("specific-guildid") != null) {
                                                if (I.getID() != event.getOption("specific-guildid").getAsLong()) {
                                                    continue;
                                                }
                                            }
                                            boolean hasTourney = false;
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(":blue_circle: " + I.getName() + " - " + I.getID());
                                            try {
                                                E.setColor(I.getColor());
                                            } catch (Exception ignored) {
                                            }
                                            String S = "";
                                            for (SChallonge_Tournament T : I.getChallonges(true)) {
                                                S = S + "> ";
                                                if (T.isPublic()) {
                                                    S = S + "[" + BotEmoji.get("POW") + "] ";
                                                } else {
                                                    S = S + "[:low_battery:] ";
                                                }
                                                if (T.areAllParticipantsLinked()) {
                                                    S = S + "[:white_check_mark:] ";
                                                } else {
                                                    S = S + "[:x:] ";
                                                }
                                                S = S + "[All] " + T.getName() + " \\(" + T.getId() + "\\)\n";
                                                hasTourney = true;
                                                i++;
                                                if (S.length() > 3500) {
                                                    E.setDescription(S);
                                                    event.getChannel().sendMessageEmbeds(E.build()).queue();
                                                    S = "";
                                                }
                                            }
                                            E.setDescription(S);
                                            if (hasTourney) {
                                                event.getChannel().sendMessageEmbeds(E.build()).queue();
                                            }
                                        }
                                        m.editOriginal("There are the " + i + " tournaments saved on the bot !").queue();
                                    }
                                    case "Interclans" -> {
                                        String message = "";
                                        for (Interclan IC : Interclan.listOnGoing()) {
                                            message = message + IC.getId() + ": " + IC.getCompletedTime() + " - " + IC.getHoster().getName() + " vs " + IC.getJoiner().getName() + "\n";
                                            if (message.length() > 900) {
                                                event.getChannel().sendMessage(message).queue();
                                                message = "";
                                            }
                                            i++;
                                        }
                                        if (message.length() > 10) {
                                            event.getChannel().sendMessage(message).queue();
                                        }
                                        m.editOriginal("There are the " + i + " interclans saved on the bot !").queue();
                                    }
                                    case "Guilds" -> {
                                        String s = "";
                                        for (Guild G : DiscordAccount.getGuilds()) {
                                            s = s + "- " + G.getName() + "\n";
                                            if (s.length() > 900) {
                                                event.getChannel().sendMessage(s).queue();
                                                s = "";
                                            }
                                        }
                                        if (s.length() > 0) {
                                            event.getChannel().sendMessage(s).queue();
                                        }
                                        m.editOriginal("Done").queue();
                                    }
                                    case "Emojis" -> {
                                        for (ServerInfo G : ServerInfo.list(true)) {
                                            if (event.getOption("specific-guildid") != null) {
                                                if (G.getID() != event.getOption("specific-guildid").getAsLong()) {
                                                    continue;
                                                }
                                            }
                                            boolean hasEmojis = false;
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(":blue_circle: " + G.getName() + " - " + G.getID());
                                            E.setColor(G.getColor());
                                            String S = "";
                                            for (Emoji T : G.getGuild().getEmojis()) {
                                                S = S + "> " + T.getFormatted() + " : `" + T.getFormatted() + "`\n";
                                                hasEmojis = true;
                                                i++;
                                                if (S.length() > 3000) {
                                                    E.setDescription(S);
                                                    event.getChannel().sendMessageEmbeds(E.build()).queue();
                                                    S = "";
                                                }
                                            }
                                            E.setDescription(S);
                                            if (hasEmojis) {
                                                event.getChannel().sendMessageEmbeds(E.build()).queue();
                                            }
                                        }
                                        m.editOriginal("There are the " + i + " emojis available for the bot !").queue();
                                    }
                                    case "Roles" -> {
                                        for (ServerInfo G : ServerInfo.list(true)) {
                                            if (event.getOption("specific-guildid") != null) {
                                                if (G.getID() != event.getOption("specific-guildid").getAsLong()) {
                                                    continue;
                                                }
                                            }
                                            boolean hasRoles = false;
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(":blue_circle: " + G.getName() + " - " + G.getID());
                                            E.setColor(G.getColor());
                                            String S = "";
                                            for (Role T : G.getGuild().getRoles()) {
                                                S = S + "> " + " (@" + T.getName() + "): `" + T.getId() + "`\n";
                                                hasRoles = true;
                                                i++;
                                                if (S.length() > 3000) {
                                                    E.setDescription(S);
                                                    event.getChannel().sendMessageEmbeds(E.build()).queue();
                                                    S = "";
                                                }
                                            }
                                            E.setDescription(S);
                                            if (hasRoles) {
                                                event.getChannel().sendMessageEmbeds(E.build()).queue();
                                            }
                                        }
                                        m.editOriginal("There are the " + i + " roles available for the bot !").queue();
                                    }
                                    case "Channels" -> {
                                        for (ServerInfo G : ServerInfo.list(true)) {
                                            if (event.getOption("specific-guildid") != null) {
                                                if (G.getID() != event.getOption("specific-guildid").getAsLong()) {
                                                    continue;
                                                }
                                            }
                                            boolean hasChannels = false;
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(":blue_circle: " + G.getName() + " - " + G.getID());
                                            E.setColor(G.getColor());
                                            String S = "";
                                            for (Channel T : G.getGuild().getChannels()) {
                                                S = S + "> " + T.getAsMention() + " (#" + T.getName() + "): `" + T.getId() + "`\n";
                                                if (T instanceof TextChannel TC) {
                                                    try {
                                                        S = S + "L--> Last message by __" + TC.getHistory().retrievePast(1).submit().orTimeout(15, TimeUnit.SECONDS).get().getFirst().getAuthor().getEffectiveName() + "__ at __" + EpochSecondToPattern(TC.getHistory().retrievePast(1).submit().orTimeout(15, TimeUnit.SECONDS).get().getFirst().getTimeCreated().toInstant().getEpochSecond(), "dd/MM/yyyy HH:mm") + "__\n";
                                                    } catch (Exception ignored) {
                                                        S = S + "L--> Last message by ??? at ???.\n";
                                                    }
                                                }
                                                hasChannels = true;
                                                i++;
                                                if (S.length() > 3000) {
                                                    E.setDescription(S);
                                                    event.getChannel().sendMessageEmbeds(E.build()).queue();
                                                    S = "";
                                                }
                                            }
                                            E.setDescription(S);
                                            if (hasChannels) {
                                                event.getChannel().sendMessageEmbeds(E.build()).queue();
                                            }
                                        }
                                        m.editOriginal("There are the " + i + " channels available for the bot !").queue();
                                    }
                                    case "Birthdays" -> {
                                        EmbedBuilder E = new EmbedBuilder();
                                        E.setTitle("Birthdays");
                                        E.setColor(Color.orange);
                                        java.util.List<Profile> p = new ArrayList<>(Profile.list());
                                        p.removeIf(PP -> PP.getBirthday() == null);
                                        p.sort(Comparator.comparingLong((Profile obj) -> DaysUntilDayOfNextYear(obj.getBirthday())));
                                        String s1 = "";
                                        String s2 = "";
                                        String s3 = "";
                                        java.util.List<Clan> cls = new ArrayList<>(Clan.list());
                                        for (int list = 0; list < 5; list++) {
                                            for (int x = (list * 10); x < 10 + (list * 10); x++) {
                                                try {
                                                    String name = ClearClanTags(getUserByID(p.get(x).getID()).getEffectiveName(), cls);
                                                    if (name.length() > 14) {
                                                        name = name.substring(0, 12) + "...";
                                                    }
                                                    Clan clan = getClanOfUser(p.get(x).getID());
                                                    String isInClan = (clan != null ? "**" + clan.getTag() + " • **" : "");
                                                    s1 = s1 + "`" + (x + 1) + ")` " + p.get(x).getNationality().getFlag() + " " + isInClan + name + "\n";
                                                    s2 = s2 + "`" + EpochSecondToPattern(p.get(x).getBirthday().getEpochSecond(), "dd/MM/yyyy")  + "`\n";
                                                    s3 = s3 + "`" + DaysUntilDayOfNextYear(p.get(x).getBirthday()) + " " + TL(event, "days") + "`\n";
                                                } catch (Exception ignored) {
                                                    break;
                                                }
                                            }
                                            if (s1.length() > 5) {
                                                if (list == 0) {
                                                    E.addField(TL(event, "Name"), s1, true);
                                                    E.addField("Birthday", s2, true);
                                                    E.addField("Days left", s3, true);
                                                } else {
                                                    E.addField(" ", s1, true);
                                                    E.addField(" ", s2, true);
                                                    E.addField(" ", s3, true);
                                                }
                                            }
                                            s1 = "";
                                            s2 = "";
                                            s3 = "";
                                        }
                                        m.editOriginalEmbeds(E.build()).queue();
                                    }
                                }
                            });
                        }
                        else if (event.getName().equals("mdf-status")) {
                            event.deferReply().queue(M -> {
                                String activity = event.getOption("activity").getAsString();
                                String status = event.getOption("status").getAsString();
                                switch (activity) {
                                    case "Playing":
                                        DiscordAccount.getPresence().setActivity(Activity.playing(status));
                                        break;
                                    case "Listening":
                                        DiscordAccount.getPresence().setActivity(Activity.listening(status));
                                        break;
                                    case "Watching":
                                        DiscordAccount.getPresence().setActivity(Activity.watching(status));
                                        break;
                                    case "Competing":
                                        DiscordAccount.getPresence().setActivity(Activity.competing(status));
                                        break;
                                    case "Streaming":
                                        if (event.getOption("url") != null) {
                                            DiscordAccount.getPresence().setActivity(Activity.streaming(status, event.getOption("url").getAsString()));
                                        }
                                        break;
                                }
                                M.deleteOriginal().queue();
                            });
                        }
                        else if (event.getName().equals("mdf-leave_serv")) {
                            event.deferReply().queue(M -> {
                                String guildid = event.getOption("guildid").getAsString();
                                DiscordAccount.getGuildById(guildid).leave().queue();
                                M.editOriginal("Done!").queue();
                            });
                        }
                        else if (event.getName().equals("mdf-perms")) {
                            MessagePollBuilder MPB = new MessagePollBuilder("Register here !");
                            MPB.setLayout(MessagePoll.LayoutType.DEFAULT);
                            MPB.setDuration(3, TimeUnit.DAYS);
                            MPB.setMultiAnswer(false);
                            MPB.addAnswer("Register !");
                            EmbedBuilder E = new EmbedBuilder();
                            E.setTitle("Hey");
                            E.setDescription("This is an embed with a poll !");
                            E.setThumbnail(event.getGuild().getIconUrl());
                            event.getChannel().sendMessageEmbeds(E.build()).setPoll(MPB.build()).queue();

                            event.deferReply().queue(M -> {
                                String Perms = "";
                                for (Guild G : DiscordAccount.getGuilds()) {
                                    String S = "";
                                    if (G.getSelfMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                                        S = S + ", ADM";
                                    }
                                    if (G.getSelfMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                                        S = S + ", BAN";
                                    }
                                    if (G.getSelfMember().getPermissions().contains(Permission.KICK_MEMBERS)) {
                                        S = S + ", KICK";
                                    }
                                    if (S.length() > 3) {
                                        Perms = Perms + G.getName() + ": `" + S.replaceFirst(", ", "") + "`\n";
                                    }
                                }
                                M.editOriginal(Perms).queue();
                            });
                        }
                        else if (event.getName().equals("mdf-nickname")) {
                            event.deferReply().queue(M -> {
                                String guildid = event.getOption("guildid").getAsString();
                                User user = event.getOption("user").getAsUser();
                                Guild G = DiscordAccount.getGuildById(guildid);
                                try {
                                    G.modifyNickname(Objects.requireNonNull(G.getMemberById(user.getId())), event.getOption("nickname").getAsString()).queue();
                                    M.editOriginal("Successfully set my nickname on **" + G.getName() + "** by **" + event.getOption("nickname").getAsString() + "**").queue();
                                } catch (Exception e) {
                                    M.editOriginal("Failed to rename myself. No perms i guess.").queue();
                                }
                            });
                        }
                        else if (event.getName().equals("mdf-delete_message")) {
                            event.deferReply().queue(M -> {
                                String messageurl = event.getOption("message").getAsString().replaceAll("https://discord.com/channels/", "");
                                try {
                                    Guild guild = DiscordAccount.getGuildById(messageurl.split("/")[0]);
                                    TextChannel C = guild.getTextChannelById(messageurl.split("/")[1]);
                                    Message message = getMessage(C, messageurl.split("/")[2]);
                                    message.delete().queue();
                                    M.editOriginal("Done !").queue();
                                } catch (NullPointerException ignored) {
                                }
                            });
                        }
                        else if (event.getName().equals("mdf-message")) {
                            event.deferReply(true).queue(m -> {
                                if (event.getOption("message").getAsString().equals("FCAdd")) {
                                    m.editOriginal("Done !").queue();
                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setTitle("Friendcode");
                                    E.setDescription("Click this button below to add your friendcodes!");
                                    E.setColor(Color.red);
                                    E.setThumbnail(event.getGuild().getIconUrl());
                                    event.getChannel().sendMessageEmbeds(E.build()).setComponents(ActionRow.of(Button.success("profile-manage-friendcode", "Add Friendcode"))).queue();
                                } else if (event.getOption("message").getAsString().equals("NationalityAdd")) {
                                    m.editOriginal("Done !").queue();
                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setTitle("Nationality");
                                    E.setDescription("""
                                            Click this button below set your nationality!
                                            If the language isn't found, notify us. (Do not write the country name)
                                            :flag_fr: :flag_es: :flag_de: :flag_it: :flag_mu: :flag_be: :flag_br: :flag_pt: :flag_gb:""");
                                    E.setColor(Color.cyan);
                                    E.setThumbnail(event.getGuild().getIconUrl());
                                    event.getChannel().sendMessageEmbeds(E.build()).setComponents(ActionRow.of(Button.success("profile-manage-nationality", "Set Nationality"))).queue();
                                } else if (event.getOption("message").getAsString().equals("AddScore")) {
                                    m.editOriginal("Done !").queue();
                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setTitle("Add Score");
                                    E.setDescription("Click this button below to add your tournament score!");
                                    E.setColor(Color.orange);
                                    E.setThumbnail(event.getGuild().getIconUrl());
                                    event.getChannel().sendMessageEmbeds(E.build()).setComponents(ActionRow.of(Button.success("server-report-score/1/" + event.getOption("misc").getAsString(), "Add Score"))).queue();
                                }
                            });
                        }
                        else if (event.getName().equals("mdf-shutdown")) {
                            DiscordAccount.shutdownNow();
                            Wait(10000);
                            System.exit(0);
                        }
                        else if (event.getName().equals("mdf-invite_serv")) {
                            event.reply("Wait...").queue(M -> {
                                Guild guild = DiscordAccount.getGuildById(event.getOption("guildid").getAsString());
                                if (guild != null) {
                                    guild.getTextChannels().getFirst().createInvite().setMaxAge(7L, TimeUnit.DAYS).queue(invite -> {
                                        M.editOriginal("https://discord.com/invite/" + invite.getCode()).queue();
                                    });
                                } else {
                                    M.editOriginal("I am not on the server.").queue();
                                }
                            });
                        }
                        else if (event.getName().equals("mdf-refresh")) {
                            event.deferReply(true).setContent("Done !").queue();
                            ExecutorService E = Executors.newCachedThreadPool();
                            E.execute(() -> {
                                Constants.RefreshCooldownOfClanRoleRefresh = 0;
                                Constants.RefreshCooldownOfRankRoleRefresh = 0;
                                Constants.RefreshCooldownOfLeaderboard = 0;
                                try {
                                    switch (event.getOption("data-type").getAsString()) {
                                        case "Tournaments" -> {
                                            for (ServerInfo I : ServerInfo.list(true)) {
                                                for (SChallonge_Tournament T : I.getActiveChallonges()) {
                                                    try {
                                                        T.ResyncChallonge();
                                                        T.RefreshPanelMessage();
                                                        T.Update();
                                                    } catch (Exception ignored) {}
                                                }
                                            }
                                        }
                                        case "Server Roles" -> {
                                            RefreshAllClanMembers(Clan.listOpenPaused());
                                            RefreshAllRankMembers();
                                        }
                                        case "Leaderboards" -> {
                                            RefreshAllLeaderboards();
                                            RefreshAllClanlists();
                                        }
                                        case "Remind Scores" -> {
                                            RemindScores();
                                        }
                                        case "Clean" -> {
                                            Constants.BotStaffGuild = null;
                                            setBotVariables();
                                            CleanEmojis();
                                        }
                                    }
                                } catch (Exception e) {
                                    handleException(e);
                                }
                            });
                            ShutdownWithTimeout(E, 1, "");
                        }
                        else if (event.getName().equals("mdf-changelog")) {
                            event.deferReply(true).setContent("Done !").queue();
                            String features = "\n> **Features:**\n";
                            String bugfix = "\n> **Bug Fixes:**\n";
                            for (OptionMapping o : event.getOptions()) {
                                if (o.getName().contains("feature")) {
                                    features = features + "> - " + o.getAsString() + "\n";
                                } else {
                                    bugfix = bugfix + "> - " + o.getAsString() + "\n";
                                }
                            }
                            if (features.equals("\n> **Features:**\n")) {
                                features = "";
                            }
                            if (bugfix.equals("\n> **Bug Fixes:**\n")) {
                                bugfix = "";
                            }
                            LogBotChangelog(features, bugfix);
                        }
                        else if (event.getName().equals("mdf-announcement")) {
                            event.deferReply(true).setContent("Done !").queue();
                            String description = event.getOption("announcement").getAsString();
                            LogBotAnnouncement(description);
                        }
                        else if (event.getName().equals("mdf-send_dm")) {
                            event.reply("Wait...").queue(M -> {
                                String text = event.getOption("text").getAsString();
                                User user = event.getOption("user").getAsUser();
                                sendPrivateMessage(user, text.replaceAll("<br>", "\n"));
                                M.editOriginal("Sent!").queue();
                            });
                        }
                        else if (event.getName().equals("mdf-send_message")) {
                            event.reply("Wait...").setEphemeral(true).queue(M -> {
                                try {
                                    String message = event.getOption("text").getAsString().replaceAll("<br>", "\n");
                                    Guild guild;
                                    String guildid = event.getOption("guildid").getAsString();
                                    if (isNumeric(guildid)) {
                                        guild = DiscordAccount.getGuildById(guildid);
                                    } else {
                                        java.util.List<Guild> Gs = new ArrayList<>(DiscordAccount.getGuilds());
                                        Gs.sort(Comparator.comparingDouble((Guild G) -> similarity(G.getName(), guildid, true)).reversed());
                                        guild = Gs.getFirst();
                                    }

                                    TextChannel chan = event.getChannel().asTextChannel();
                                    if (isNumeric(event.getOption("channelid").getAsString())) {
                                        String channelid = event.getOption("channelid").getAsString();
                                        if (isNumeric(channelid)) {
                                            chan = guild.getTextChannelById(channelid);
                                        } else {
                                            java.util.List<TextChannel> Gs = new ArrayList<>(guild.getTextChannels());
                                            Gs.sort(Comparator.comparingDouble((TextChannel G) -> similarity(G.getName(), channelid, true)).reversed());
                                            chan = Gs.getFirst();
                                        }
                                    }
                                    TextChannel finalChan = chan;
                                    if (event.getOption("user") == null) {
                                        if (event.getOption("reply-messageid") != null) {
                                            Message mess = getMessage(chan, event.getOption("reply-messageid").getAsString());
                                            if (mess != null) {
                                                mess.reply(message).queue(mm -> {
                                                    if (event.getOption("button-label-1") != null) {
                                                        String btntx = event.getOption("button-label-1").getAsString();
                                                        String btnid = event.getOption("button-id-1").getAsString();
                                                        mm.editMessageComponents(ActionRow.of(Button.success(btnid, btntx))).queue();
                                                    }
                                                    M.editOriginal("Replied to " + mess.getAuthor().getAsMention() + " in **" + guild.getName() + "** in #" + finalChan.getName() + ".").queue();
                                                });
                                            }
                                        } else {
                                            chan.sendMessage(message).queue(mm -> {
                                                if (event.getOption("button-label-1") != null) {
                                                    String btntx = event.getOption("button-label-1").getAsString();
                                                    String btnid = event.getOption("button-id-1").getAsString();
                                                    mm.editMessageComponents(ActionRow.of(Button.success(btnid, btntx))).queue();
                                                }
                                                M.editOriginal("Sent in **" + guild.getName() + "** in #" + finalChan.getName() + ".").queue();
                                            });
                                        }
                                    } else {
                                        User usurpator = event.getOption("user").getAsUser();
                                        TextChannel finalChan1 = chan;
                                        getWebhookOfChannel(guild.getTextChannelById(chan.getId()), WB -> {
                                            WebhookMessageBuilder MessageSender = new WebhookMessageBuilder()
                                                    .setUsername(usurpator.getEffectiveName())
                                                    .setContent(message)
                                                    .setAvatarUrl(usurpator.getEffectiveAvatarUrl());
                                            if (event.getOption("button-label-1") != null) {
                                                String btntx = event.getOption("button-label-1").getAsString();
                                                String btnid = event.getOption("button-id-1").getAsString();
                                                club.minnced.discord.webhook.send.component.button.Button BTN2
                                                        = new club.minnced.discord.webhook.send.component.button.Button(club.minnced.discord.webhook.send.component.button.Button.Style.SUCCESS, btnid).setLabel(btntx);
                                                club.minnced.discord.webhook.send.component.layout.ActionRow R = club.minnced.discord.webhook.send.component.layout.ActionRow.of(BTN2);
                                                MessageSender.addComponents(R);
                                            }
                                            WB.send(MessageSender.build());
                                            M.editOriginal("Sent in **" + guild.getName() + "** in #" + finalChan1.getName() + ".").queue();
                                        });
                                    }
                                } catch (Exception ignored) {
                                    M.editOriginal("Failed sending...").queue();
                                }
                            });
                        }
                        else if (event.getName().equals("mdf-react_message")) {
                            event.reply("Wait...").queue(M -> {
                                try {
                                    String message = event.getOption("emoji").getAsString();

                                    Guild guild;
                                    String guildid = event.getOption("guildid").getAsString();
                                    if (isNumeric(guildid)) {
                                        guild = DiscordAccount.getGuildById(guildid);
                                    } else {
                                        java.util.List<Guild> Gs = new ArrayList<>(DiscordAccount.getGuilds());
                                        Gs.sort(Comparator.comparingDouble((Guild G) -> similarity(G.getName(), guildid, true)).reversed());
                                        guild = Gs.getFirst();
                                    }

                                    TextChannel chan;
                                    String channelid = event.getOption("channelid").getAsString();
                                    if (isNumeric(channelid)) {
                                        chan = guild.getTextChannelById(channelid);
                                    } else {
                                        List<TextChannel> Gs = new ArrayList<>(guild.getTextChannels());
                                        Gs.sort(Comparator.comparingDouble((TextChannel G) -> similarity(G.getName(), channelid, true)).reversed());
                                        chan = Gs.getFirst();
                                    }

                                    Message mess = getMessage(chan, event.getOption("messageid").getAsString());
                                    if (mess != null) {
                                        mess.addReaction(Emoji.fromFormatted(message)).queue(MM -> {
                                            M.editOriginal("Replied to " + mess.getAuthor().getAsMention() + " in **" + guild.getName() + "** in #" + chan.getName() + ".").queue();
                                        });
                                    }
                                } catch (Exception ignored) {
                                    M.editOriginal("Failed sending...").queue();
                                }
                            });
                        }
                        else if (event.getName().equals("mdf-super_timeout")) {
                            event.reply("Wait...").setEphemeral(true).queue(M -> {
                                try {
                                    Guild guild = DiscordAccount.getGuildById(event.getOption("guildid").getAsString());
                                    User user = event.getOption("user").getAsUser();
                                    Member m = guild.getMemberById(user.getId());

                                    int amount = takeOnlyInts(event.getOption("time").getAsString());
                                    TimeUnit unit = event.getOption("time").getAsString().contains("M") ? TimeUnit.MINUTES : TimeUnit.DAYS;
                                    m.timeoutFor(amount, unit).queue();
                                    M.editOriginal("Done !").queue();
                                } catch (Exception ignored) {
                                    M.editOriginal("Failed timeouting...").queue();
                                }
                            });
                        }
                        else if (event.getName().equals("mdf-untimeout")) {
                            event.reply("Wait...").setEphemeral(true).queue(M -> {
                                try {
                                    Guild guild = DiscordAccount.getGuildById(event.getOption("guildid").getAsString());
                                    User user = event.getOption("user").getAsUser();
                                    guild.getMemberById(user.getId()).removeTimeout().queue();
                                    M.editOriginal("Done !").queue();
                                } catch (Exception ignored) {
                                    M.editOriginal("Failed untimeouting...").queue();
                                }
                            });
                        }
                    } else {
                        if (!event.isAcknowledged()) event.reply("You are not Loic....!").queue();
                    }
                }
            } catch(Exception e){
                replyException(event, e);
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isAcknowledged()) {
            try {
                if (event.getComponentId().contains("respond-support")) {
                    Label input = Label.of("Response", TextInput.create("response", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Hello, the solution to your problem is:")
                            .setRequired(true)
                            .build());
                    event.replyModal(Modal.create(event.getComponentId(), "Respond to the user.")
                            .addComponents(input).build()).queue();
                }
                if (event.getComponentId().contains("jtmloic")) {
                    event.reply("Ouiiiiiiii " + event.getUser().getEffectiveName() + " est fan de Loic !!!").queue();
                }
                if (event.getComponentId().contains("piegeto")) {
                    event.getMember().timeoutFor(1, TimeUnit.MINUTES).queue();
                }


                if (event.getComponentId().equals("server-french")) {
                    // Message message = event.getMessage();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setAuthor(TL(event,"Community"));
                    embed.setColor(Color.cyan);
                    embed.setDescription("The community of Inazuma Eleven is made of several servers, all around different nationalities and group of people. " +
                            "This is a list of all Discord servers we support ! \n" +
                            "When there are icons near the server It means:\n" +
                            "• :signal_strength: Bot is available there.\n" +
                            "• :trophy: Make tournaments.");

                    String s = "";
                    for (CommunityServer.Server S : new CommunityServer("French").Servers) {
                        if (DiscordAccount.getGuildById(S.id) != null) {
                            if (true) {
                                s = s + "└ • :signal_strength: :trophy: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            } else {
                                s = s + "└ • :signal_strength: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            }
                        } else {
                            s = s + "└ • " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                        }
                    }
                    embed.addField("French",
                            s, false);

                    ActionRow row = ActionRow.of(Button.secondary("server-international", "International").withEmoji(Emoji.fromFormatted("U+1F1FA U+1F1F3"))
                            , Button.secondary("server-spanish", "Spanish").withEmoji(Emoji.fromFormatted("U+1F1EA U+1F1F8"))
                            , Button.secondary("server-italian", "Italian").withEmoji(Emoji.fromFormatted("U+1F1EE U+1F1F9"))
                            , Button.secondary("server-brazilian", "Brazilian").withEmoji(Emoji.fromFormatted("U+1F1E7 U+1F1F7"))
                    );
                    event.replyEmbeds(embed.build()).setComponents(row).setEphemeral(true).queue();
                }
                if (event.getComponentId().equals("server-spanish")) {
                    // Message message = event.getMessage();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setAuthor(TL(event,"Community"));
                    embed.setColor(Color.cyan);
                    embed.setDescription("The community of Inazuma Eleven is made of several servers, all around different nationalities and group of people. " +
                            "This is a list of all Discord servers we support ! \n" +
                            "When there are icons near the server It means:\n" +
                            "• :signal_strength: Bot is available there.\n" +
                            "• :trophy: Make tournaments.");

                    String s = "";
                    for (CommunityServer.Server S : new CommunityServer("Spanish").Servers) {
                        if (DiscordAccount.getGuildById(S.id) != null) {
                            if (true) {
                                s = s + "└ • :signal_strength: :trophy: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            } else {
                                s = s + "└ • :signal_strength: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            }
                        } else {
                            s = s + "└ • " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                        }
                    }
                    embed.addField("Spanish",
                            s, false);

                    ActionRow row = ActionRow.of(Button.secondary("server-international", "International").withEmoji(Emoji.fromFormatted("U+1F1FA U+1F1F3"))
                            , Button.secondary("server-french", "French").withEmoji(Emoji.fromFormatted("U+1F1EB U+1F1F7"))
                            , Button.secondary("server-italian", "Italian").withEmoji(Emoji.fromFormatted("U+1F1EE U+1F1F9"))
                            , Button.secondary("server-brazilian", "Brazilian").withEmoji(Emoji.fromFormatted("U+1F1E7 U+1F1F7"))
                    );
                    event.replyEmbeds(embed.build()).setComponents(row).setEphemeral(true).queue();
                }
                if (event.getComponentId().equals("server-italian")) {
                    // Message message = event.getMessage();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setAuthor(TL(event,"Community"));
                    embed.setColor(Color.cyan);
                    embed.setDescription("The community of Inazuma Eleven is made of several servers, all around different nationalities and group of people. " +
                            "This is a list of all Discord servers we support ! \n" +
                            "When there are icons near the server It means:\n" +
                            "• :signal_strength: Bot is available there.\n" +
                            "• :trophy: Make tournaments.");

                    String s = "";
                    for (CommunityServer.Server S : new CommunityServer("Italian").Servers) {
                        if (DiscordAccount.getGuildById(S.id) != null) {
                            if (true) {
                                s = s + "└ • :signal_strength: :trophy: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            } else {
                                s = s + "└ • :signal_strength: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            }
                        } else {
                            s = s + "└ • " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                        }
                    }
                    embed.addField("Italian",
                            s, false);

                    ActionRow row = ActionRow.of(Button.secondary("server-international", "International").withEmoji(Emoji.fromFormatted("U+1F1FA U+1F1F3"))
                            , Button.secondary("server-french", "French").withEmoji(Emoji.fromFormatted("U+1F1EB U+1F1F7"))
                            , Button.secondary("server-spanish", "Spanish").withEmoji(Emoji.fromFormatted("U+1F1EA U+1F1F8"))
                            , Button.secondary("server-brazilian", "Brazilian").withEmoji(Emoji.fromFormatted("U+1F1E7 U+1F1F7"))
                    );
                    event.replyEmbeds(embed.build()).setComponents(row).setEphemeral(true).queue();
                }
                if (event.getComponentId().equals("server-brazilian")) {
                    //  Message message = event.getMessage();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setAuthor(TL(event,"Community"));
                    embed.setColor(Color.cyan);
                    embed.setDescription("The community of Inazuma Eleven is made of several servers, all around different nationalities and group of people. " +
                            "This is a list of all Discord servers we support ! \n" +
                            "When there are icons near the server It means:\n" +
                            "• :signal_strength: Bot is available there.\n" +
                            "• :trophy: Make tournaments.");

                    String s = "";
                    for (CommunityServer.Server S : new CommunityServer("Brazilian").Servers) {
                        if (DiscordAccount.getGuildById(S.id) != null) {
                            if (true) {
                                s = s + "└ • :signal_strength: :trophy: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            } else {
                                s = s + "└ • :signal_strength: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            }
                        } else {
                            s = s + "└ • " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                        }
                    }
                    embed.addField("Brazilian",
                            s, false);

                    ActionRow row = ActionRow.of(Button.secondary("server-international", "International").withEmoji(Emoji.fromFormatted("U+1F1FA U+1F1F3"))
                            , Button.secondary("server-french", "French").withEmoji(Emoji.fromFormatted("U+1F1EB U+1F1F7"))
                            , Button.secondary("server-spanish", "Spanish").withEmoji(Emoji.fromFormatted("U+1F1EA U+1F1F8"))
                            , Button.secondary("server-italian", "Italian").withEmoji(Emoji.fromFormatted("U+1F1EE U+1F1F9"))
                    );
                    event.replyEmbeds(embed.build()).setComponents(row).setEphemeral(true).queue();
                }
                if (event.getComponentId().equals("server-international")) {
                    //  Message message = event.getMessage();

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setAuthor(TL(event,"Community"));
                    embed.setColor(Color.cyan);
                    embed.setDescription("The community of Inazuma Eleven is made of several servers, all around different nationalities and group of people. " +
                            "This is a list of all Discord servers we support ! \n" +
                            "When there are icons near the server It means:\n" +
                            "• :signal_strength: Bot is available there.\n" +
                            "• :trophy: Make tournaments.");

                    String s = "";
                    for (CommunityServer.Server S : new CommunityServer("International").Servers) {
                        if (DiscordAccount.getGuildById(S.id) != null) {
                            if (true) {
                                s = s + "└ • :signal_strength: :trophy: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            } else {
                                s = s + "└ • :signal_strength: " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                            }
                        } else {
                            s = s + "└ • " + S.flag + " [" + S.name + "](" + S.link + "\\)\n";
                        }
                    }
                    embed.addField("International",
                            s, false);

                    ActionRow row = ActionRow.of(Button.secondary("server-french", "French").withEmoji(Emoji.fromFormatted("U+1F1EB U+1F1F7"))
                            , Button.secondary("server-spanish", "Spanish").withEmoji(Emoji.fromFormatted("U+1F1EA U+1F1F8"))
                            , Button.secondary("server-italian", "Italian").withEmoji(Emoji.fromFormatted("U+1F1EE U+1F1F9"))
                            , Button.secondary("server-brazilian", "Brazilian").withEmoji(Emoji.fromFormatted("U+1F1E7 U+1F1F7"))
                    );
                    event.replyEmbeds(embed.build()).setComponents(row).setEphemeral(true).queue();
                }

                if (event.getComponentId().equals("player-filter-search-galaxy")) {
                    Label player = Label.of("SCRIPT", TextInput.create("filter-input", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("[P1]Block, [P2]Speed, Fire, Female, [Min]190 TP")
                            .setMaxLength(256)
                            .build());
                    event.replyModal(Modal.create("player-filter-script-galaxy", "Filtering Players")
                            .addComponents(player).build()).queue();
                }
                if (event.getComponentId().equals("delete-message")) {
                    event.getMessage().delete().queue();
                }
            } catch (Exception e){
                replyException(event, e);
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.isAcknowledged()) {
            try {
                if (event.getModalId().startsWith("respond-support")) {
                    event.deferReply().queue(M -> {
                        String response = event.getValue("response").getAsString();
                        EmbedBuilder E = new EmbedBuilder();
                        E.setTitle("Support Response");
                        E.setDescription(response);
                        E.setThumbnail(DiscordAccount.getSelfUser().getEffectiveAvatarUrl());
                        if (response.toLowerCase().contains("clan")) {
                            E.setColor(Color.blue);
                        } else if (response.toLowerCase().contains("tourn")) {
                            E.setColor(Color.orange);
                        } else if (response.toLowerCase().contains("profile")) {
                            E.setColor(Color.cyan);
                        } else if (response.toLowerCase().contains("abuser")) {
                            E.setColor(Color.red);
                        } else {
                            E.setColor(Color.white);
                        }
                        E.addField("Staff", event.getUser().getEffectiveName(), false);
                        User u = getUserByID(event.getModalId());
                        sendPrivateMessage(u, new MessageCreateBuilder().setContent("Hey " + u.getAsMention() + ", you received a response for your support request!").setEmbeds(E.build()));
                        M.editOriginal("**" + event.getUser().getEffectiveName() + "** has responded:\n" + response).queue();
                    });
                }
            } catch (Exception e) {
                replyException(event, e);
            }
        }
    }

    public static void createForcedScore(InteractionHook M, Game game, User user, int myscore, int opponentscore, User opponent, MessageChannel channel) {
        Button Confirmation = Button.success("match-rslt-conf", TL(M, "yes"));
        Button Decline = Button.danger("match-rslt-deny", TL(M, "no"));
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(game.getColor());
        embed.setTitle(TL(M,"score-reply-title"));

        ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
        SChallonge_Tournament T = I != null ? I.getTournamentOfMatch(user.getIdLong(), opponent.getIdLong(), game, false) : null;

        embed.setDescription(TL(M, "score-reply-success-match", game.getName()) + " " + user.getAsMention() + " **" + myscore + "** " + game.getVSEmojiFormatted() + " **" + opponentscore + "** " + opponent.getAsMention()
                + "\n" + opponent.getAsMention() + ", " + TL(M, "score-reply-success-score-confirm")+ "\n\n" +
                (T != null && T.getMatchResultChannelID() == M.getInteraction().getChannelIdLong() ? "**" + TL(M,"Tournament") + ":** " + game.getEmojiFormatted() + " " + T.getName() : ""));
        channel.sendMessageEmbeds(embed.build()).setContent(user.getAsMention() + " **" + myscore + "** " + game.getVSEmojiFormatted() + " **" + opponentscore + "** " + opponent.getAsMention()).setComponents(ActionRow.of(Confirmation, Decline)).queue(Mm -> {
            MatchLog ML = new MatchLog(game, user.getIdLong(), opponent.getIdLong(), myscore, opponentscore, Mm, null);
            if (Mm.isFromGuild()) new MatchLog_S(game, user.getIdLong(), opponent.getIdLong(), myscore, opponentscore, Mm);
            if (T != null && T.getMatchResultChannelID() == M.getInteraction().getChannelIdLong()) {
                BaseCParticipant<?,?,?> P = T.getTeamByMyId(user.getIdLong());
                if (P != null) P.getMatchWithOpponent(opponent.getIdLong(), false).setMatchLog(ML);
            }
        });
    }

    public static void LogBotChangelog(String features, String bugfix) {
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                    getWebhookOfChannel(C, client -> {
                        String description = "# :arrows_counterclockwise: New Update\n";
                        description = description + features + bugfix + "\n";
                        description = description + "`                                                       `\n";

                        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                        embed.setDescription(description);
                        embed.setColor(Color.WHITE.getRGB());
                        embed.setFooter(new WebhookEmbed.EmbedFooter("• V." + EpochSecondToPattern(Instant.now().getEpochSecond(), "yyyy.MM.dd"), DiscordAccount.getSelfUser().getEffectiveAvatarUrl()));
                        embed.setTimestamp(Instant.now());
                        WebhookMessageBuilder builde = new WebhookMessageBuilder()
                                .setUsername("Bot Update")
                                .setAvatarUrl(DiscordAccount.getSelfUser().getEffectiveAvatarUrl())
                                .addEmbeds(embed.build());
                        client.send(builde.build());
                    });
                }
            } catch (Exception ignored) {}
        }
    }
    public static void LogBotAnnouncement(String announcement) {
        for (DatabaseObject.Row TR : getClanUpdatesChannels()) {
            try {
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                if (G == null) continue;
                TextChannel C = G.getTextChannelById(TR.getAsLong("ChannelID"));
                if (C == null) continue;
                if (hasPermissionInChannel(null, C, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                    getWebhookOfChannel(C, client -> {
                        String description = "# :rotating_light: ANNOUNCEMENT !\n";
                        description = description + announcement + "\n";
                        description = description + "`                                                       `\n";

                        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                        embed.setDescription(description);
                        embed.setColor(Color.WHITE.getRGB());
                        embed.setFooter(new WebhookEmbed.EmbedFooter("• Inazuma Competitive", DiscordAccount.getSelfUser().getEffectiveAvatarUrl()));
                        embed.setTimestamp(Instant.now());
                        WebhookMessageBuilder builde = new WebhookMessageBuilder()
                                .setUsername("New Update")
                                .setAvatarUrl(DiscordAccount.getSelfUser().getEffectiveAvatarUrl())
                                .addEmbeds(embed.build());
                        client.send(builde.build());
                    });
                }
            } catch (Exception ignored) {}
        }
    }

    public static String ClearClanTags(String name, List<Clan> clans) {
        for (Clan c : clans) {
            if (name.contains(c.getTag())) {
                name = name.replaceFirst(c.getName(), "");
                name = name.replaceFirst(" \\| ", "");
                name = name.replaceFirst("\\| ", "");
                name = name.replaceFirst(" \\|", "");
            }
        }
        return name;
    }
}