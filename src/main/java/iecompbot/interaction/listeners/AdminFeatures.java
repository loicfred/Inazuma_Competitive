package iecompbot.interaction.listeners;

import at.stefangeyer.challonge.exception.DataAccessException;
import at.stefangeyer.challonge.model.Tournament;
import at.stefangeyer.challonge.model.enumeration.TournamentState;
import iecompbot.Utility;
import iecompbot.interaction.GuildReady;
import iecompbot.interaction.cmdbreakdown.IDCommand;
import iecompbot.interaction.cmdbreakdown.MatchmakingCommand;
import iecompbot.interaction.cmdbreakdown.server.ChallongeCommand;
import iecompbot.interaction.cmdbreakdown.server.RankingCommand;
import iecompbot.interaction.cmdbreakdown.server.DoubleIDCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.match.Game;
import iecompbot.objects.match.League;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.profile_game.Profile_Game;
import iecompbot.objects.profile.profile_game.Profile_Game_S;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.SubMatch;
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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.*;
import static iecompbot.img.ImgUtilities.getHexValue;
import static iecompbot.interaction.Automation.*;
import static iecompbot.interaction.GuildReady.RefreshAllClanMembers;
import static iecompbot.interaction.GuildReady.RefreshPOTY;
import static iecompbot.objects.BotManagers.isBotOwner;
import static iecompbot.objects.BotManagers.isTournamentManager;
import static iecompbot.objects.Retrieval.getMessageByLink;
import static my.utilities.util.Utilities.*;
import static my.utilities.var.Constants.ProgramZoneId;

public class AdminFeatures extends ListenerAdapter {

    public static String cleanChallongeURL(String url) {
        url = url.replaceAll("/fr/", "/").replaceAll("/es/", "/");
        url = url.replaceAll("/de/", "/").replaceAll("/en/", "/");
        url = url.replaceAll("/jp/", "/").replaceAll("/it/", "/");
        url = url.replaceAll("https://challonge.com/", "").replaceAll("challonge.com/", "");
        url = url.replaceAll("/participants", "").replaceAll("/standings", "");
        url = url.replaceAll("/announcements", "").replaceAll("/log", "");
        url = url.replaceAll("/stations", "").replaceAll("/settings", "").replaceAll("/module", "");
        return url;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.isFromGuild() && !event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getName().startsWith("adm")) {
                        ServerInfo I = ServerInfo.get(event.getGuild());
                        switch (event.getName()) {
                            case "adm-server_manage" -> {
                                event.deferReply(true).queue(M -> {
                                    slashAdmServerManage(M, I);
                                });
                            }
                            case "adm-ranking_manage" -> {
                                event.deferReply(true).queue(M -> {
                                    slashAdmRankingManage(M, I);
                                });
                            }
                            case "adm-new_tier" -> {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        if (isAdmin(M, event.getMember())) {
                                            String name = event.getOption("name").getAsString();
                                            String colorcode = event.getOption("colorcode").getAsString();
                                            if (Utility.isColorcodeValid(M, colorcode)) {
                                                BotEmoji emoji = BotEmoji.from(event.getGuild().retrieveEmojiById(event.getOption("emojiid").getAsLong()).submit().get(), event.getGuild().getIdLong());
                                                if (emoji != null) {
                                                    int start_medals = event.getOption("start-medals").getAsInt();
                                                    int end_medals = event.getOption("end-medals").getAsInt();
                                                    Message.Attachment icon = event.getOption("icon-image").getAsAttachment();

                                                    if (start_medals < end_medals && start_medals > -1) {
                                                        if (I.Ranking().getTierByMedal(start_medals) == null && I.Ranking().getTierByMedal(end_medals) == null) {
                                                            League.League_Tier L = new League.League_Tier(I.getID(), name, colorcode, start_medals, end_medals, emoji.getIdLong());
                                                            EmbedBuilder E = new EmbedBuilder();
                                                            E.setTitle(TL(M, "ranking-manager"));
                                                            E.setDescription(TL(M, "ranking-manager-leagues-description"));
                                                            E.setAuthor(L.getName());
                                                            E.setColor(L.getColor());
                                                            File Download = new File(TempDirectory + "/" + L.hashCode() + ".png");
                                                            icon.getProxy().downloadToFile(Download).whenComplete((file, throwable) -> {
                                                                try {
                                                                    if (ImageIO.read(file) != null) {
                                                                        L.setImage(Files.readAllBytes(file.toPath()));
                                                                        E.setThumbnail(L.getImageURL());
                                                                        M.editOriginalEmbeds(E.build()).queue();
                                                                        GuildReady.RefreshAllPrivateCMDs();
                                                                    } else {
                                                                        M.editOriginal(TL(M,"image-fail-extension")).queue();
                                                                    }
                                                                } catch (Exception e) {
                                                                    M.editOriginal(TL(M,"image-fail-extension")).queue();
                                                                }
                                                            });
                                                        } else {
                                                            M.editOriginal(TL(M, "ranking-error-2")).queue();
                                                        }
                                                    } else {
                                                        M.editOriginal(TL(M, "ranking-error-1")).queue();
                                                    }
                                                } else {
                                                    M.editOriginal(TL(M, "error-invalid-emoji")).queue();
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            case "adm-new_league" -> {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        if (isAdmin(M, event.getMember())) {
                                            String name = event.getOption("name").getAsString();
                                            BotEmoji emoji = BotEmoji.from(event.getGuild().retrieveEmojiById(event.getOption("emojiid").getAsString()).submit().get(), event.getGuild().getIdLong());
                                            if (emoji != null) {
                                                long tier = event.getOption("tier").getAsLong();
                                                int start_medals = event.getOption("start-medals").getAsInt();
                                                int end_medals = event.getOption("end-medals").getAsInt();
                                                double power = event.getOption("private-power") != null ? event.getOption("private-power").getAsDouble() : 0;
                                                Message.Attachment icon = event.getOption("icon-image").getAsAttachment();

                                                if (start_medals >= end_medals && start_medals > -1 && end_medals > 0) {
                                                    if (I.Ranking().getLeagueByMedal(start_medals) == null && I.Ranking().getLeagueByMedal(end_medals) == null) {
                                                        League L = new League(tier, I.getID(), name, start_medals, end_medals, power, emoji.getIdLong());
                                                        EmbedBuilder E = new EmbedBuilder();
                                                        E.setTitle(TL(M, "ranking-manager"));
                                                        E.setDescription(TL(M, "ranking-manager-leagues-description"));
                                                        E.setAuthor(L.getName());
                                                        E.setColor(L.getTier().getColor());
                                                        File Download = new File(TempDirectory + "/" + L.hashCode() + ".png");
                                                        icon.getProxy().downloadToFile(Download).whenComplete((file, throwable) -> {
                                                            try {
                                                                if (ImageIO.read(file) != null) {
                                                                    L.setImage(Files.readAllBytes(file.toPath()));
                                                                    L.Update();
                                                                    E.setThumbnail(L.getImageURL());
                                                                    M.editOriginalEmbeds(E.build()).queue();
                                                                } else {
                                                                    M.editOriginal(TL(M,"image-fail-extension")).queue();
                                                                }
                                                            } catch (Exception e) {
                                                                M.editOriginal(TL(M,"image-fail-extension")).queue();
                                                            }
                                                        });
                                                    } else {
                                                        M.editOriginal(TL(M, "ranking-error-2")).queue();
                                                    }
                                                } else {
                                                    M.editOriginal(TL(M, "ranking-error-1")).queue();
                                                }
                                            } else {
                                                M.editOriginal(TL(M, "error-invalid-emoji")).queue();
                                            }
                                        }
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            case "adm-blacklist_add" -> {
                                event.deferReply().queue(M -> {
                                    if (isAdmin(M, event.getMember())) {
                                        try {
                                            User culprit = event.getOption("user").getAsUser();
                                            int time = event.getOption("end-time").getAsInt();
                                            Game game = !event.getOption("game").getAsString().equals("All") ? Game.get(event.getOption("game").getAsString()) : null;
                                            String reason = event.getOption("reason").getAsString();
                                            slashAddBlacklist(M, I, game, time, culprit, reason);
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    }
                                });
                            }
                            case "adm-blacklist_remove" -> {
                                event.deferReply().queue(M -> {
                                    if (isAdmin(M, event.getMember())) {
                                        try {
                                            User culprit = event.getOption("user").getAsUser();
                                            Game game = !event.getOption("game").getAsString().equals("All") ? Game.get(event.getOption("game").getAsString()) : null;
                                            slashRemoveBlacklist(M, I, game, culprit);
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    }
                                });
                            }
                            case "adm-inscription" -> {
                                event.deferReply(true).queue(M -> {
                                    if (isAdmin(M, event.getMember())) {
                                        try {
                                            Game game = Game.get(event.getOption("game").getAsString());
                                            String name = event.getOption("tournament-name").getAsString();
                                            long start = event.getOption("start-date").getAsLong();
                                            String url = event.getOption("challonge-link").getAsString();
                                            String messageurl = event.getOption("custom-inscription-message") != null ? event.getOption("custom-inscription-message").getAsString() : "N/A";
                                            Message.Attachment optionalImage = event.getOption("tournament-banner") != null ? event.getOption("tournament-banner").getAsAttachment() : null;
                                            String optionalMessage = event.getOption("custom-message") != null ? event.getOption("custom-message").getAsString() : null;
                                            boolean notify = event.getOption("advertise").getAsBoolean();

                                            TextChannel InscriptionChannel = event.getOption("inscription-channel").getAsChannel().asTextChannel();
                                            TextChannel PanelChannel = event.getOption("panel-channel").getAsChannel().asTextChannel();
                                            TextChannel RulesChannel = event.getOption("rules-channel").getAsChannel().asTextChannel();
                                            TextChannel ResultChannel = event.getOption("result-channel").getAsChannel().asTextChannel();
                                            TextChannel PredictionChannel = (event.getOption("prediction-channel") != null ? event.getOption("prediction-channel").getAsChannel().asTextChannel() : null);
                                            Role ParticipantRole = event.getOption("participant-role").getAsRole();
                                            Role OrganizerRole = event.getOption("organizer-role").getAsRole();

                                            int vstype = event.getOption("vs-type") != null ? event.getOption("vs-type").getAsInt() : 1;
                                            Long customStartTime = event.getOption("custom-start-time") != null ? takeOnlyDigits(event.getOption("custom-start-time").getAsString()) : null;

                                            int cap = event.getOption("signup-cap") != null ? event.getOption("signup-cap").getAsInt() : 0;
                                            slashInscription(M, I, game, name, start, vstype, url, messageurl, optionalImage, optionalMessage, InscriptionChannel, PanelChannel, RulesChannel, ResultChannel, PredictionChannel, ParticipantRole, OrganizerRole, cap, notify, customStartTime);
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    }
                                });
                            }
                            case "adm-view_tourney" -> {
                                event.deferReply(true).queue(M -> {
                                    if (isAdmin(M, event.getMember())) {
                                        String url = event.getOption("tournament-link") != null ? event.getOption("tournament-link").getAsString() : null;
                                        slashViewTourney(M, I, url);
                                    }
                                });
                            }
                            case "adm-tourney_image" -> {
                                event.deferReply().queue(M -> {
                                    try {
                                        String Challonge = event.getOption("challonge").getAsString();
                                        Message.Attachment Image = event.getOption("image").getAsAttachment();
                                        slashChallongeImage(M, Challonge, Image);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }

                            case "adm-set_currency" -> {
                                event.deferReply().queue(M -> {
                                    try {
                                        if (isAdmin(M, event.getMember())) {
                                            String name = event.getOption("name").getAsString();
                                            String description = event.getOption("description").getAsString();
                                            BotEmoji emoji = BotEmoji.from(event.getGuild().retrieveEmojiById(event.getOption("emojiid").getAsString()).submit().get(), event.getGuild().getIdLong());
                                            if (I.Channels().getLogChannel() == null)
                                                M.editOriginal(TL(M, "server-log-channel-required")).queue();
                                            else if (emoji == null)
                                                M.editOriginal(TL(M, "error-invalid-emoji")).queue();
                                            else {
                                                I.setCurrencyPerWin(event.getOption("amount-per-win").getAsInt());
                                                I.setCurrencyPerTop1(event.getOption("amount-per-top1").getAsInt());
                                                I.setCurrencyPerTop2(event.getOption("amount-per-top2").getAsInt());
                                                I.setCurrencyPerTop3(event.getOption("amount-per-top3").getAsInt());
                                                I.Update();
                                                Item IT = I.getCurrency();
                                                if (IT == null) {
                                                    IT = new Item(I.getID(), name, description, Item.ItemType.CURRENCY, emoji.getIdLong());
                                                } else {
                                                    IT.setName(name);
                                                    IT.setDescription(description);
                                                    IT.setEmojiID(emoji.getIdLong());
                                                    IT.Update();
                                                }
                                                EmbedBuilder E = I.getServerEmbed();
                                                E.setTitle(TL(M, "Economy"));
                                                E.setDescription(TL(M, "server-economy-currency-create", "**" + IT.getEmojiFormatted() + " " + IT.getName() + "**"));
                                                M.editOriginalEmbeds(E.build()).queue();
                                            }
                                        }
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            case "adm-add_shop" -> {
                                event.deferReply().queue(M -> {
                                    if (isAdmin(M, event.getMember())) {
                                        try {
                                            String name = event.getOption("name").getAsString();
                                            String description = event.getOption("description").getAsString();
                                            String type = event.getOption("type").getAsString();
                                            int price = event.getOption("price").getAsInt();
                                            BotEmoji emoji = BotEmoji.from(event.getGuild().retrieveEmojiById(event.getOption("emojiid").getAsString()).submit().get(), event.getGuild().getIdLong());
                                            if (I.Channels().getLogChannel() == null) M.editOriginal(TL(M, "server-log-channel-required")).queue();
                                            else if (I.listItems().size() >= 100) M.editOriginal(TL(M, "server-economy-items-max")).queue();
                                            else if (emoji == null) M.editOriginal(TL(M, "error-invalid-emoji")).queue();
                                            else if (I.getCurrency() == null) M.editOriginal(TL(M, "server-economy-item-add-error")).queue();
                                            else if ((type.contains("LICENSE") || type.contains("COSMETIC")) && event.getOption("img-asset") == null && !event.getOption("img-asset").getAsAttachment().isImage()) M.editOriginal(TL(M, "server-economy-item-add-error-2")).queue();
                                            else {
                                                if (type.contains("LICENSE")) {
                                                    Message.Attachment img = event.getOption("img-asset").getAsAttachment();
                                                    if (img.getWidth() % 512 != 0 && img.getHeight() % 300 != 0) M.editOriginal(TL(M, "server-economy-item-add-error-3", 1024, 600)).queue();
                                                }
                                                else if (Item.ItemType.valueOf(type) == Item.ItemType.COSMETICS_FRAME) {
                                                    Message.Attachment img = event.getOption("img-asset").getAsAttachment();
                                                    if (img.getWidth() != 450 && img.getHeight() != 450) M.editOriginal(TL(M, "server-economy-item-add-error-3", 450, 450)).queue();
                                                }
                                                Item IT = new Item(I.getID(), name, description, Item.ItemType.valueOf(type), emoji.getIdLong());
                                                IT.setPrice(I.getCurrency().getId(), price);
                                                IT.Update();
                                                EmbedBuilder E = I.getServerEmbed();
                                                E.setTitle(TL(M,"Economy"));
                                                E.setDescription(TL(M, "server-economy-item-add", "**" + IT.getEmojiFormatted() + " " + IT.getName() + "**", "**" + I.getCurrency().getEmojiFormatted() + " " + price + "**"));
                                                if (event.getOption("img-asset") != null) {
                                                    Message.Attachment img = event.getOption("img-asset").getAsAttachment();
                                                    File Download = new File(TempDirectory + "/" + IT.hashCode() + ".png");
                                                    img.getProxy().downloadToFile(Download).whenComplete((file, throwable) -> {
                                                        try {
                                                            if (ImageIO.read(file) != null) {
                                                                IT.setImage(Files.readAllBytes(file.toPath()));
                                                                IT.Update();
                                                                E.setImage(IT.getImageURL());
                                                                M.editOriginalEmbeds(E.build()).queue();
                                                            } else {
                                                                M.editOriginal(TL(M,"image-fail-extension")).queue();
                                                            }
                                                        } catch (Exception e) {
                                                            M.editOriginal(TL(M,"image-fail-extension")).queue();
                                                        }
                                                    });
                                                } else M.editOriginalEmbeds(E.build()).queue();
                                            }
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    }
                                });
                            }
                            case "adm-shop_manage" -> {
                                event.deferReply().queue(M -> {
                                    try {
                                        slashAdmShopManage(M, I);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                        }
                    }
                    else if (event.getName().startsWith("dq")) {
                        event.deferReply().queue(M -> {
                            try {
                                ServerInfo I = ServerInfo.get(event.getGuild());
                                SChallonge_Tournament T = I.getChallonge(event.getOption("tournament").getAsLong());
                                User user = event.getOption("player") != null ? event.getOption("player").getAsUser() : event.getUser();
                                String reason = event.getOption("reason").getAsString();
                                slashDQ(M, I, T, user, reason);
                            } catch (Exception e) {
                                replyException(M, e);
                            }
                        });
                    }
                    else if (event.getName().startsWith("server-info")) {
                        event.deferReply().queue(M -> {
                            try {
                                ServerInfo I = event.getOption("name") == null ? ServerInfo.get(event.getGuild()) : null;
                                if (event.getOption("name") != null) {
                                    for (ServerInfo II :  ServerInfo.find(event.getOption("name").getAsString())) {
                                        if (II.getGuild() == null) continue;
                                        I = II;
                                        break;
                                    }
                                }
                                EmbedBuilder E = new EmbedBuilder();
                                E.setTitle(I.getGuild().getName());
                                E.setThumbnail(I.getGuild().getIconUrl());
                                E.setDescription(I.getGuild().getDescription());
                                E.addField("Owner:", "- <@" + I.getOwnerID() + ">", true);
                                E.addField(TL(M,"Tournament") + ":", "- " + I.getTournamentCount(), true);
                                if (I.getPermanentInviteLink() != null) E.addField(TL(M, "invite_link") + ":", "- " + I.getPermanentInviteLink(), false);
                                E.addField(" ", "`                                   `", false);
                                for (SChallonge_Tournament CT : I.getActiveChallonges()) {
                                    if (CT.isPending()) {
                                        E.addField(BotEmoji.get("icon_fan") + " " + CT.getName().replace("Inazuma Eleven", "IE"),
                                                "> " + TL(M, "Participants") + ": **" + CT.getParticipantCount() + " ~ " + CT.getType().toString().toUpperCase() + " ~ " + CT.getVSAmount() + "v" + CT.getVSAmount() + "**\n"
                                                        + "> " + TL(M, "Register") + ": " + CT.getInscriptionChannel().getMessageLink1() + "\n"
                                                        + "> " + TL(M, "Panel") + ": " + CT.getPanelChannel().getMessageLink1() + "\n", false);
                                    } else {
                                        E.addField(":yellow_circle: " + CT.getName().replace("Inazuma Eleven", "IE"),
                                                "> " + TL(M, "Participants") + ": **" + CT.getParticipantCount() + " ~ " + CT.getType().toString().toUpperCase() + " ~ " + CT.getVSAmount() + "v" + CT.getVSAmount() + "**\n"
                                                        + "> " + TL(M, "Panel") + ": " + CT.getPanelChannel().getMessageLink1() + "\n", false);
                                    }
                                }
                                E.setTimestamp(I.getGuild().getTimeCreated().toInstant());
                                E.setFooter(TL(M,"Members") + ": " + I.getGuild().getMemberCount());
                                M.editOriginalEmbeds(E.build()).queue();
                            } catch (Exception e) {
                                M.editOriginal(TL(M, "server-info-not-found")).queue();
                            }
                        });
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isAcknowledged() && event.isFromGuild()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("match")) {
                        if (event.getComponentId().startsWith("matchmaking-remove")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    MatchmakingCommand CMD = new MatchmakingCommand(event.getComponentId(), event.getGuild());
                                    ServerInfo I = ServerInfo.get(event.getGuild());
                                    ServerInfo.ServerInfo_Matchmaking MM = I.getMatchmaking(CMD.Game);
                                    if (MM != null) MM.Delete();
                                    CMD.ChannelID = null;
                                    CMD.RoleID = null;
                                    MatchmakingSetupMenu(M, I, CMD);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                    }
                    else if (event.getComponentId().startsWith("adm-ranking-manage-user")) {
                        RankingCommand CMD = new RankingCommand(event.getComponentId());
                        Profile_Game_S PGS = Profile_Game_S.get(CMD.UserID, CMD.ID, CMD.getGame());
                        if (event.getComponentId().contains("wtl")) {
                            Label Win = Label.of(TL(event, "Wins"), TextInput.create("win", TextInputStyle.SHORT)
                                    .setPlaceholder(PGS.getWins() + "")
                                    .setRequiredRange(1, 5).setRequired(false).build());
                            Label Ties = Label.of(TL(event, "Ties"), TextInput.create("tie", TextInputStyle.SHORT)
                                    .setPlaceholder(PGS.getTies() + "")
                                    .setRequiredRange(1, 5).setRequired(false).build());
                            Label Loses = Label.of(TL(event, "Defeats"), TextInput.create("lose", TextInputStyle.SHORT)
                                    .setPlaceholder(PGS.getLoses() + "")
                                    .setRequiredRange(1, 5).setRequired(false).build());
                            Label GoalsScored = Label.of(TL(event, "GoalsScored"), TextInput.create("goalsscored", TextInputStyle.SHORT)
                                    .setPlaceholder(PGS.getGoalsScored() + "")
                                    .setRequiredRange(1, 5).setRequired(false).build());
                            Label GoalsTaken = Label.of(TL(event, "GoalsTaken"), TextInput.create("goalstaken", TextInputStyle.SHORT)
                                    .setPlaceholder(PGS.getGoalsTaken() + "")
                                    .setRequiredRange(1, 5).setRequired(false).build());
                            event.replyModal(Modal.create(CMD.Command("adm-ranking-manage-user"), StopString(TL(event, "ranking-manage-user-select"), 45))
                                    .addComponents(Win, Ties, Loses, GoalsScored, GoalsTaken).build()).queue();
                        }
                        else if (event.getComponentId().contains("medalstr")) {
                            Label Medals = Label.of(TL(event, "Medals"), TextInput.create("medal", TextInputStyle.SHORT)
                                    .setPlaceholder(PGS.getMedals() + "")
                                    .setRequiredRange(1, 5).setRequired(false).build());
                            Label WinStreak = Label.of(TL(event, "Win_Streak"), TextInput.create("winstreak", TextInputStyle.SHORT)
                                    .setPlaceholder(PGS.getWinStreak() + "")
                                    .setRequiredRange(1, 5).setRequired(false).build());
                            Label BestWinStreak = Label.of(TL(event, "Best_Win_Streak"), TextInput.create("bestwinstreak", TextInputStyle.SHORT)
                                    .setPlaceholder(PGS.getHighestWinStreak() + "")
                                    .setRequiredRange(1, 5).setRequired(false).build());
                            event.replyModal(Modal.create(CMD.Command("adm-ranking-manage-user"), StopString(TL(event, "ranking-manage-user-select"), 45))
                                    .addComponents(Medals, WinStreak, BestWinStreak).build()).queue();
                        }
                    }
                    else if (event.getComponentId().startsWith("adm-ranking-tier")) {
                        DoubleIDCommand CMD = new DoubleIDCommand(event.getComponentId());
                        League.League_Tier L = League.League_Tier.get(CMD.ObjectID);
                        if (event.getComponentId().startsWith("adm-ranking-tier-manage")) {
                            if (event.getComponentId().contains("name")) {
                                Label name = Label.of(TL(event,"Name"), TextInput.create("name", TextInputStyle.SHORT)
                                        .setPlaceholder(L.getName()).setRequiredRange(5, 48).setRequired(false)
                                        .build());
                                Label emoji = Label.of("Emoji ID", TextInput.create("emojiid", TextInputStyle.SHORT)
                                        .setPlaceholder("Server Emoji ID").setRequiredRange(5, 24).setRequired(false)
                                        .build());
                                Label colorcode = Label.of(TL(event, "Color"), TextInput.create("colorcode", TextInputStyle.SHORT)
                                        .setPlaceholder(getHexValue(L.getColor())).setRequiredRange(7, 7).setRequired(false)
                                        .build());
                                event.replyModal(Modal.create(CMD.Command("adm-ranking-tier-manage"), L.getName())
                                        .addComponents(name, emoji, colorcode).build()).queue();
                            }
                            else if (event.getComponentId().contains("medals")) {
                                Label m1 = Label.of(TL(event,"Medals") + " (Start)", TextInput.create("start", TextInputStyle.SHORT)
                                        .setPlaceholder(L.getStart() + "").setRequiredRange(1, 8).setRequired(false)
                                        .build());
                                Label m2 = Label.of(TL(event,"Medals") + " (End)", TextInput.create("end", TextInputStyle.SHORT)
                                        .setPlaceholder(L.getEnd() + "").setRequiredRange(1, 8).setRequired(false)
                                        .build());
                                event.replyModal(Modal.create(CMD.Command("adm-ranking-tier-manage"), L.getName())
                                        .addComponents(m1, m2).build()).queue();
                            }
                            else if (event.getComponentId().contains("delete")) {
                                event.deferEdit().queue(M -> {
                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setTitle(TL(M, "ranking-manager"));
                                    E.setAuthor(L.getName());
                                    E.setColor(L.getColor());
                                    Button BTN = Button.danger(CMD.Command("adm-ranking-tier-manage-confirm-delete"), TL(M, "Delete"));
                                    M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(BTN)).queue();
                                });
                            }
                            else if (event.getComponentId().contains("confirm-delete")) {
                                event.deferEdit().queue(M -> {
                                    L.Delete();
                                    M.editOriginal(TL(M,"clanrole-delete-success")).queue();
                                });
                            }
                        }
                    }
                    else if (event.getComponentId().startsWith("adm-ranking-league")) {
                        DoubleIDCommand CMD = new DoubleIDCommand(event.getComponentId());
                        League L = League.get(CMD.ObjectID);
                        if (event.getComponentId().startsWith("adm-ranking-league-manage")) {
                            if (event.getComponentId().contains("name")) {
                                Label name = Label.of(TL(event,"Name"), TextInput.create("name", TextInputStyle.SHORT)
                                        .setPlaceholder(L.getName()).setRequiredRange(5, 48).setRequired(false)
                                        .build());
                                Label emoji = Label.of("Emoji ID", TextInput.create("emojiid", TextInputStyle.SHORT)
                                        .setPlaceholder("Server Emoji ID").setRequiredRange(5, 24).setRequired(false)
                                        .build());
                                event.replyModal(Modal.create(CMD.Command("adm-ranking-league-manage"), L.getName())
                                        .addComponents(name, emoji).build()).queue();
                            }
                            else if (event.getComponentId().contains("medals")) {
                                Label m1 = Label.of(TL(event,"Medals") + " (Start)", TextInput.create("start", TextInputStyle.SHORT)
                                        .setPlaceholder(L.getStart() + "").setRequiredRange(1, 8).setRequired(false)
                                        .build());
                                Label m2 = Label.of(TL(event,"Medals") + " (End)", TextInput.create("end", TextInputStyle.SHORT)
                                        .setPlaceholder(L.getEnd() + "").setRequiredRange(1, 8).setRequired(false)
                                        .build());
                                Label m3 = Label.of(TL(event,"Power"), TextInput.create("power", TextInputStyle.SHORT)
                                        .setPlaceholder(L.getEnd() + "").setRequiredRange(1, 8).setRequired(false)
                                        .build());
                                event.replyModal(Modal.create(CMD.Command("adm-ranking-league-manage"), L.getName())
                                        .addComponents(m1, m2, m3).build()).queue();
                            }
                            else if (event.getComponentId().contains("delete")) {
                                event.deferEdit().queue(M -> {
                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setTitle(TL(M, "ranking-manager"));
                                    E.setAuthor(L.getName());
                                    E.setColor(L.getTier().getColor());
                                    Button BTN = Button.danger(CMD.Command("adm-ranking-league-manage-confirm-delete"), TL(M, "Delete"));
                                    M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(BTN)).queue();
                                });
                            }
                            else if (event.getComponentId().contains("confirm-delete")) {
                                event.deferEdit().queue(M -> {
                                    L.Delete();
                                    M.editOriginal(TL(M,"clanrole-delete-success")).queue();
                                });
                            }
                        }
                    }
                    else if (event.getComponentId().startsWith("adm-shop-item")) {
                        ServerInfo I = ServerInfo.get(event.getGuild());
                        DoubleIDCommand CMD = new DoubleIDCommand(event.getComponentId());
                        Item IT = I.getItem(CMD.ObjectID);
                        if (event.getComponentId().startsWith("adm-shop-item-manage")) {
                            if (event.getComponentId().contains("details")) {
                                Label name = Label.of(TL(event,"Name"), TextInput.create("name", TextInputStyle.SHORT)
                                        .setPlaceholder(IT.getName()).setRequiredRange(5, 48).setRequired(false)
                                        .build());
                                Label description = Label.of(TL(event,"Description"), TextInput.create("description", TextInputStyle.SHORT)
                                        .setPlaceholder(IT.getName()).setRequiredRange(16, 128).setRequired(false)
                                        .build());
                                Label emoji = Label.of("Emoji ID", TextInput.create("emojiid", TextInputStyle.SHORT)
                                        .setPlaceholder("Server Emoji ID").setRequiredRange(5, 24).setRequired(false)
                                        .build());
                                Label cost = Label.of(TL(event,"Cost"), TextInput.create("price", TextInputStyle.SHORT)
                                        .setPlaceholder("5").setRequiredRange(1, 3).setRequired(false)
                                        .build());
                                event.replyModal(Modal.create(CMD.Command("adm-shop-item-manage"), IT.getName())
                                        .addComponents(name, description, emoji, cost).build()).queue();
                            }
                            else if (event.getComponentId().contains("delete")) {
                                event.deferEdit().queue(M -> {
                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setTitle(TL(M, "shop-manager"));
                                    E.setAuthor(IT.getName());
                                    E.setColor(I.getColor());
                                    Button BTN = Button.danger(CMD.Command("adm-shop-item-manage-confirm-delete"), TL(M, "Delete"));
                                    M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(BTN)).queue();
                                });
                            }
                            else if (event.getComponentId().contains("confirm-delete")) {
                                event.deferEdit().queue(M -> {
                                    IT.Delete();
                                    M.editOriginal(TL(M,"clanrole-delete-success")).queue();
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
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.isAcknowledged() && event.isFromGuild()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("adm")) {
                        IDCommand CMD = new IDCommand(event.getComponentId());
                        ServerInfo I = ServerInfo.get(CMD.ID);
                        if (event.getComponentId().startsWith("adm-server-manage-toggler")) {
                            event.deferEdit().queue(MM -> {
                                try {
                                    if (isBotOwner(event.getUser())) {
                                        I.setAreScoresAllowed(event.getValues().contains("Match-Result"));
                                        I.setAreTournamentsAllowed(event.getValues().contains("Tournament"));
                                    }
                                    I.setAreClanRolesAllowed(event.getValues().contains("Clan-Roles"));
                                    I.setAreClanTagsAllowed(event.getValues().contains("Clan-Tags"));
                                    I.setAreWinnerRolesAllowed(event.getValues().contains("Winner-Roles"));
                                    I.Update();
                                    slashAdmServerManage(MM, I);
                                    if (I.areClanRolesAllowed || I.areClanTagsAllowed) RefreshAllClanMembers(Clan.listOpen());
                                } catch (Exception e) {
                                    replyException(MM, e);
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("adm-server-manage-advanced")) {
                            switch (event.getValues().getFirst()) {
                                case "invite-link" -> {
                                    Label Link = Label.of(TL(event, "invite_link"), TextInput.create("link-input", TextInputStyle.SHORT)
                                            .setPlaceholder(I.getPermanentInviteLink() != null ? I.getPermanentInviteLink() : "https://discord.gg/Q86CRCNmcX").setRequiredRange(24, 32)
                                            .setRequired(false).build());
                                    event.replyModal(Modal.create("adm-server-manage-invite", StopString(TL(event,"server-invite-link-description"), 45))
                                            .addComponents(Link).build()).queue();
                                }
                                case "Matchmaking" -> event.deferReply(true).queue(M -> MatchmakingSetupMenuChoice(M, I));
                                case "Rank" -> event.deferReply(true).queue(M -> RankSetupMenu(M, I));
                                case "Leaderboard" -> event.deferReply(true).queue(M -> LeaderboardChannelMenu(M, I));
                                case "log-channels" -> event.deferReply(true).queue(M -> LogChannelMenu(M, I));
                                case "clan-channels" -> event.deferReply(true).queue(M -> ClanChannelsMenu(M, I));
                                case "tournament-channels" -> event.deferReply(true).queue(M -> TournamentChannelsMenu(M, I));
                            }
                            if (event.getValues().getFirst().startsWith("media-")) {
                                TextInput.Builder link = TextInput.create("link-input", TextInputStyle.SHORT).setRequiredRange(8, 128).setRequired(false);
                                switch (event.getValues().getFirst()) {
                                    case "media-twitter" -> {
                                        link.setPlaceholder("https://twitter.com/" + I.getName().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("adm-server-manage-twitter"), "Twitter").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    }
                                    case "media-website" -> {
                                        link.setPlaceholder("https://" + I.getName().replaceAll(" ", "").toLowerCase() + ".com/");
                                        event.replyModal(Modal.create(CMD.Command("adm-server-manage-website"), "Website").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    }
                                    case "media-youtube" -> {
                                        link.setPlaceholder("https://www.youtube.com/@" + I.getName().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("adm-server-manage-youtube"), "YouTube").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    }
                                    case "media-twitch" -> {
                                        link.setPlaceholder("https://www.twitch.tv/" + I.getName().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("adm-server-manage-twitch"), "Twitch").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    }
                                    case "media-instagram" -> {
                                        link.setPlaceholder("https://www.instagram.com/@" + I.getName().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("adm-server-manage-instagram"), "Instagram").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    }
                                    case "media-tiktok" -> {
                                        link.setPlaceholder("https://www.tiktok.com/" + I.getName().toLowerCase().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("adm-server-manage-tiktok"), "Tiktok").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    }
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("adm-ranking")) {
                            if (event.getComponentId().startsWith("adm-ranking-manage-toggler")) {
                                event.deferEdit().queue(MM -> {
                                    try {
                                        I.Ranking().setPrivateRanking(event.getValues().contains("Ranking"));
                                        I.Ranking().setPrivateLeagues(event.getValues().contains("Leagues"));
                                        I.Update();
                                        slashAdmRankingManage(MM, I);
                                    } catch (Exception e) {
                                        replyException(MM, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().startsWith("adm-ranking-manage-advanced")) {
                                if (event.getComponentId().contains("tier")) {
                                    event.deferReply(true).queue(M -> {
                                        try {
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(TL(M,"ranking-manager"));
                                            E.setDescription(TL(M, "ranking-manager-leagues-description"));
                                            E.setAuthor(" • " + I.getGuild().getName(), null, I.getGuild().getIconUrl());
                                            E.setThumbnail(I.getGuild().getIconUrl());
                                            E.setColor(I.getColor());

                                            List<ActionRow> ARs = new ArrayList<>();
                                            List<SelectOption> O = new ArrayList<>();
                                            for (League.League_Tier L : I.Ranking().getTiers()) {
                                                O.add(SelectOption.of(L.getName(), String.valueOf(L.getId())));
                                                if (O.size() % 25 == 0) {
                                                    ARs.add(ActionRow.of(StringSelectMenu.create("adm-ranking-tier-" + ARs.size())
                                                            .setPlaceholder(O.getFirst().getLabel())
                                                            .addOptions(O).setRequiredRange(1, 1).build()));
                                                    O = new ArrayList<>();
                                                }
                                            }
                                            if (!O.isEmpty()) ARs.add(ActionRow.of(StringSelectMenu.create("adm-ranking-tier-" + ARs.size())
                                                    .setPlaceholder(O.getFirst().getLabel()).addOptions(O).setRequiredRange(1, 1).build()));
                                            M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    });
                                }
                                else if (event.getComponentId().contains("wtl")) {
                                    Label Win = Label.of(TL(event, "Wins"), TextInput.create("win", TextInputStyle.SHORT)
                                            .setPlaceholder(I.Ranking().getWinPts() + "")
                                            .setRequiredRange(1, 5).setRequired(false).build());
                                    Label Ties = Label.of(TL(event, "Ties"), TextInput.create("tie", TextInputStyle.SHORT)
                                            .setPlaceholder(I.Ranking().getTiePts() + "")
                                            .setRequiredRange(1, 5).setRequired(false).build());
                                    Label Loses = Label.of(TL(event, "Defeats"), TextInput.create("lose", TextInputStyle.SHORT)
                                            .setPlaceholder(I.Ranking().getLosePts() + "")
                                            .setRequiredRange(1, 5).setRequired(false).build());
                                    Label LeagueDiff = Label.of(TL(event, "League") + " Diff", TextInput.create("league", TextInputStyle.SHORT)
                                            .setPlaceholder(I.Ranking().getLeagueDiffPts() + "")
                                            .setRequiredRange(1, 5).setRequired(false).build());
                                    event.replyModal(Modal.create("adm-ranking-manage", StopString(TL(event, "ranking-manager-select"), 45))
                                            .addComponents(Win, Ties, Loses, LeagueDiff).build()).queue();
                                }
                                else if (event.getComponentId().contains("rng")) {
                                    Label Win = Label.of("Min", TextInput.create("min", TextInputStyle.SHORT)
                                            .setPlaceholder(I.Ranking().getMinRNG() + "")
                                            .setRequiredRange(1, 5).setRequired(false).build());
                                    Label Ties = Label.of("Max", TextInput.create("max", TextInputStyle.SHORT)
                                            .setPlaceholder(I.Ranking().getMaxRNG() + "")
                                            .setRequiredRange(1, 5).setRequired(false).build());
                                    event.replyModal(Modal.create("adm-ranking-manage", StopString(TL(event, "ranking-manager-select"), 45))
                                            .addComponents(Win, Ties).build()).queue();
                                }
                                else if (event.getComponentId().contains("delete-all")) {
                                    event.deferEdit().queue(M -> {
                                        for (Game G : Game.values()) {
                                            for (Profile_Game_S PGS : Profile_Game_S.list(event.getGuild().getIdLong(), G)) {
                                                PGS.Delete();
                                            }
                                        }
                                        slashAdmRankingManage(M, ServerInfo.get(event.getGuild()));
                                    });
                                }
                                else if (event.getComponentId().contains("sync-all")) {
                                    event.deferEdit().queue(M -> {
                                        try (ExecutorService E = Executors.newFixedThreadPool(30)) {
                                            for (Game G : Game.values()) {
                                                for (Profile_Game_S PGS : Profile_Game_S.list(event.getGuild().getIdLong(), G)) {
                                                    E.execute(() -> {
                                                        try {
                                                            PGS.syncWith(Profile_Game.get(PGS.UserID, PGS.getGame()));
                                                            PGS.Update();
                                                        } catch (Exception e) {
                                                            replyException(M, e);
                                                        }
                                                    });
                                                }
                                            }
                                            slashAdmRankingManage(M, ServerInfo.get(event.getGuild()));
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    });
                                }
                            }
                            else if (event.getComponentId().startsWith("adm-ranking-manage-tier")) {
                                event.deferReply(true).queue(M -> {
                                    DoubleIDCommand CMD2 = new DoubleIDCommand(I.getID(), Long.parseLong(event.getValues().getFirst()));
                                    EditLeagueTier(CMD2, M, League.League_Tier.get(CMD2.ObjectID));
                                });
                            }
                            else if (event.getComponentId().startsWith("adm-ranking-manage-league")) {
                                event.deferReply(true).queue(M -> {
                                    DoubleIDCommand CMD2 = new DoubleIDCommand(I.getID(), Long.parseLong(event.getValues().getFirst()));
                                    EditLeague(CMD2, M, League.get(CMD2.ObjectID));
                                });
                            }
                            else if (event.getComponentId().startsWith("adm-ranking-manage-user-game")) {
                                RankingCommand CMD2 = new RankingCommand(event.getComponentId());
                                event.deferEdit().queue(M -> {
                                    CMD2.GameCode = event.getValues().getFirst();
                                    ManageUserRanking(M, CMD2);
                                });
                            }
                        }
                        else if (event.getComponentId().startsWith("adm-shop-manage-item")) {
                            event.deferReply(true).queue(M -> {
                                DoubleIDCommand CMD2 = new DoubleIDCommand(I.getID(), Long.parseLong(event.getValues().getFirst()));
                                EditShopItem(CMD2, M, I, I.getItem(CMD2.ObjectID));
                            });
                        }
                        else if (event.getComponentId().startsWith("adm-leaderboard")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    if (event.getComponentId().startsWith("adm-leaderboard-type")) {
                                        for (Game G : Game.values()) I.setGameBoardAllowed(event.getValues().contains(G.getCode()), G);
                                    }
                                    else if (event.getComponentId().startsWith("adm-leaderboard-config")) {
                                        I.setShowBoardMembersOnly(event.getValues().contains("show-server-members-only"));
                                    }
                                    I.Update();
                                    LeaderboardChannelMenu(M, I);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("adm-rank")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    if (event.getComponentId().startsWith("adm-rank-type")) {
                                        I.setAreGlobalRankAllowed(event.getValues().contains("Global"));
                                        for (Game G : Game.values()) I.setGameRanksAllowed(event.getValues().contains(G.getCode()), G);
                                    }
                                    I.Update();
                                    RankSetupMenu(M, I);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("adm-matchmaking")) {
                            event.deferReply(true).queue(M -> {
                                MatchmakingCommand CMD2 = new MatchmakingCommand(Game.get(event.getValues().getFirst()));
                                ServerInfo.ServerInfo_Matchmaking MM = I.getMatchmaking(CMD2.Game);
                                if (MM != null) CMD2 = new MatchmakingCommand(MM);
                                MatchmakingSetupMenu(M, I, CMD2);
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
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getModalId().startsWith("adm")) {
                        ServerInfo I = ServerInfo.get(event.getGuild());
                        if (event.getModalId().startsWith("adm-server-manage")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    if (isURLValid(event.getValue("link-input").getAsString())) {
                                        if (event.getModalId().startsWith("adm-server-manage-invite")) {
                                            if (event.getValue("link-input").getAsString().contains("https://discord.gg/")) {
                                                I.setPermanentInviteLink(event.getValue("link-input").getAsString());
                                                slashAdmServerManage(M, I);
                                            } else {
                                                M.editOriginal(TL(M,"invalid-invite-link")).queue();
                                            }
                                        } else if (event.getModalId().contains("adm-server-manage-twitter")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && (event.getValue("link-input").getAsString().contains("twitter.com") || event.getValue("link-input").getAsString().contains("x.com"))) {
                                                I.setTwitterURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                slashAdmServerManage(M, I);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("adm-server-manage-twitch")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("twitch.tv")) {
                                                I.setTwitchURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                slashAdmServerManage(M, I);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("adm-server-manage-website")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("https://")) {
                                                I.setWebsiteURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                slashAdmServerManage(M, I);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("adm-server-manage-youtube")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("youtu")) {
                                                I.setYouTubeURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                slashAdmServerManage(M, I);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("adm-server-manage-instagram")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("instagram.com")) {
                                                I.setInstagramURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                slashAdmServerManage(M, I);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("adm-server-manage-tiktok")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("tiktok.com")) {
                                                I.setTiktokURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                slashAdmServerManage(M, I);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        }
                                        I.Update();
                                    } else {
                                        M.editOriginal(TL(M,"invalid-invite-link")).queue();
                                    }
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getModalId().startsWith("adm-ranking-manage-user")) {
                            event.deferEdit().queue(M -> {
                                RankingCommand CMD = new RankingCommand(event.getModalId());
                                try {
                                    Profile_Game_S PGS = Profile_Game_S.get(CMD.UserID, CMD.ID, CMD.getGame());
                                    if (event.getValue("win") != null && !event.getValue("win").getAsString().isEmpty()) PGS.setWins(Integer.parseInt(event.getValue("win").getAsString()));
                                    if (event.getValue("tie") != null && !event.getValue("tie").getAsString().isEmpty()) PGS.setTies(Integer.parseInt(event.getValue("tie").getAsString()));
                                    if (event.getValue("lose") != null && !event.getValue("lose").getAsString().isEmpty()) PGS.setLoses(Integer.parseInt(event.getValue("lose").getAsString()));
                                    if (event.getValue("goalsscored") != null && !event.getValue("goalsscored").getAsString().isEmpty()) PGS.setGoalsScored(Integer.parseInt(event.getValue("goalsscored").getAsString()));
                                    if (event.getValue("goalstaken") != null && !event.getValue("goalstaken").getAsString().isEmpty()) PGS.setGoalsTaken(Integer.parseInt(event.getValue("goalstaken").getAsString()));
                                    if (event.getValue("medal") != null && !event.getValue("medal").getAsString().isEmpty()) PGS.setMedals(Integer.parseInt(event.getValue("medal").getAsString()));
                                    if (event.getValue("winstreak") != null && !event.getValue("winstreak").getAsString().isEmpty()) PGS.setWinStreak(Integer.parseInt(event.getValue("winstreak").getAsString()));
                                    if (event.getValue("bestwinstreak") != null && !event.getValue("bestwinstreak").getAsString().isEmpty()) PGS.setHighestWinStreak(Integer.parseInt(event.getValue("bestwinstreak").getAsString()));
                                    PGS.Update();
                                    ManageUserRanking(M, CMD);
                                } catch (Exception e) {
                                    replyException(event, e);
                                }
                            });
                        }
                        else if (event.getModalId().startsWith("adm-ranking-manage")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    if (event.getValue("win") != null && !event.getValue("win").getAsString().isEmpty()) I.Ranking().setWinPts(Double.parseDouble(event.getValue("win").getAsString()));
                                    if (event.getValue("tie") != null && !event.getValue("tie").getAsString().isEmpty()) I.Ranking().setTiePts(Double.parseDouble(event.getValue("tie").getAsString()));
                                    if (event.getValue("lose") != null && !event.getValue("lose").getAsString().isEmpty()) I.Ranking().setLosePts(Double.parseDouble(event.getValue("lose").getAsString()));
                                    if (event.getValue("league") != null && !event.getValue("league").getAsString().isEmpty()) I.Ranking().setLeagueDiffPts(Double.parseDouble(event.getValue("league").getAsString()));
                                    if (event.getValue("min") != null && !event.getValue("min").getAsString().isEmpty()) I.Ranking().setMinRNG(Double.parseDouble(event.getValue("min").getAsString()));
                                    if (event.getValue("max") != null && !event.getValue("max").getAsString().isEmpty()) I.Ranking().setMaxRNG(Double.parseDouble(event.getValue("max").getAsString()));
                                    I.Ranking().Update();
                                    slashAdmRankingManage(M, I);
                                } catch (Exception e) {
                                    replyException(event, e);
                                }
                            });
                        }
                        else if (event.getModalId().startsWith("adm-ranking-tier-manage")) {
                            DoubleIDCommand CMD = new DoubleIDCommand(event.getModalId());
                            League.League_Tier T = League.League_Tier.get(CMD.ObjectID);
                            if (T == null) event.deferEdit().queue(M -> M.deleteOriginal().queue());
                            else if (event.getModalId().startsWith("adm-ranking-tier-manage")) {
                                event.deferEdit().queue(M -> {
                                    if (T.getStart() < T.getEnd() && T.getStart() > -1) {
                                        League.League_Tier StartCopy = I.Ranking().getTierByMedal(T.getStart());
                                        League.League_Tier EndCopy = I.Ranking().getTierByMedal(T.getEnd());
                                        if ((StartCopy == null || StartCopy.getId() == T.getId()) && (EndCopy == null || EndCopy.getId() == T.getId())) {
                                            if (event.getValue("name") != null && !event.getValue("name").getAsString().isEmpty()) T.setName(event.getValue("name").getAsString());
                                            if (event.getValue("emojiid") != null && !event.getValue("emojiid").getAsString().isEmpty()) {
                                                if (event.getGuild().getEmojiById(event.getValue("emojiid").getAsString()) != null) {
                                                    T.setEmojiID(Long.parseLong(event.getValue("emojiid").getAsString()));
                                                } else if (event.getValue("emojiid").getAsString().toLowerCase().contains("u+")) {
                                                    T.setEmojiID(BotEmoji.get(event.getValue("emojiid").getAsString()).getIdLong());
                                                }
                                            }
                                            if (event.getValue("colorcode") != null && !event.getValue("colorcode").getAsString().isEmpty() && Utility.isColorcodeValid(M, event.getValue("colorcode").getAsString())) T.setColorcode(event.getValue("colorcode").getAsString());
                                            if (event.getValue("start") != null && !event.getValue("start").getAsString().isEmpty()) T.setStart(Integer.parseInt(event.getValue("start").getAsString()));
                                            if (event.getValue("end") != null && !event.getValue("end").getAsString().isEmpty()) T.setEnd(Integer.parseInt(event.getValue("end").getAsString()));
                                            T.Update();
                                            EditLeagueTier(CMD, M, T);
                                        } else {
                                            M.editOriginal(TL(M,"ranking-error-2")).queue();
                                        }
                                    } else {
                                        M.editOriginal(TL(M,"ranking-error-1")).queue();
                                    }
                                });
                            }
                        }
                        else if (event.getModalId().startsWith("adm-ranking-league-manage")) {
                            DoubleIDCommand CMD = new DoubleIDCommand(event.getModalId());
                            League T = League.get(CMD.ObjectID);
                            if (T == null) event.deferEdit().queue(M -> M.deleteOriginal().queue());
                            else if (event.getModalId().startsWith("adm-ranking-league-manage")) {
                                event.deferEdit().queue(M -> {
                                    if (T.getStart() < T.getEnd() && T.getStart() > -1) {
                                        League.League_Tier StartCopy = I.Ranking().getTierByMedal(T.getStart());
                                        League.League_Tier EndCopy = I.Ranking().getTierByMedal(T.getEnd());
                                        if ((StartCopy == null || StartCopy.getId() == T.getId()) && (EndCopy == null || EndCopy.getId() == T.getId())) {
                                            if (event.getValue("name") != null && !event.getValue("name").getAsString().isEmpty()) T.setName(event.getValue("name").getAsString());
                                            if (event.getValue("emojiid") != null && !event.getValue("emojiid").getAsString().isEmpty()) {
                                                if (event.getGuild().getEmojiById(event.getValue("emojiid").getAsString()) != null) {
                                                    T.setEmojiID(Long.parseLong(event.getValue("emojiid").getAsString()));
                                                } else if (event.getValue("emojiid").getAsString().toLowerCase().contains("u+")) {
                                                    T.setEmojiID(BotEmoji.get(event.getValue("emojiid").getAsString()).getIdLong());
                                                }
                                            }
                                            if (event.getValue("start") != null && !event.getValue("start").getAsString().isEmpty()) T.setStart(Integer.parseInt(event.getValue("start").getAsString()));
                                            if (event.getValue("end") != null && !event.getValue("end").getAsString().isEmpty()) T.setEnd(Integer.parseInt(event.getValue("end").getAsString()));
                                            if (event.getValue("power") != null && !event.getValue("power").getAsString().isEmpty()) T.setPower(Double.parseDouble(event.getValue("power").getAsString()));
                                            T.Update();
                                            EditLeague(CMD, M, T);
                                        } else {
                                            M.editOriginal(TL(M,"ranking-error-2")).queue();
                                        }
                                    } else {
                                        M.editOriginal(TL(M,"ranking-error-1")).queue();
                                    }
                                });
                            }
                        }
                        else if (event.getModalId().startsWith("adm-shop-item-manage")) {
                            DoubleIDCommand CMD = new DoubleIDCommand(event.getModalId());
                            Item T = Item.get(CMD.ObjectID);
                            if (T == null) event.deferEdit().queue(M -> M.deleteOriginal().queue());
                            else if (event.getModalId().startsWith("adm-shop-item-manage")) {
                                event.deferEdit().queue(M -> {
                                    if (event.getValue("name") != null && !event.getValue("name").getAsString().isEmpty()) T.setName(event.getValue("name").getAsString());
                                    if (event.getValue("description") != null && !event.getValue("description").getAsString().isEmpty()) T.setDescription(event.getValue("description").getAsString());
                                    if (event.getValue("emojiid") != null && !event.getValue("emojiid").getAsString().isEmpty()) T.setEmojiID(Long.parseLong(event.getValue("emojiid").getAsString()));
                                    if (event.getValue("price") != null && !event.getValue("price").getAsString().isEmpty()) T.setPrice(I.getCurrency().getId(), Integer.parseInt(event.getValue("price").getAsString()));
                                    T.Update();
                                    EditShopItem(CMD, M, I, T);
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
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        if (!event.isAcknowledged() && event.isFromGuild()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("adm")) {
                        if (event.getComponentId().startsWith("adm-ranking-manage-user")) {
                            event.deferReply(true).queue(M -> {
                                RankingCommand CMD = new RankingCommand(event.getGuild(), event.getMentions().getUsers().getFirst());
                                CMD.GameCode = "IEVRBETA";
                                ManageUserRanking(M, CMD);
                            });
                        }
                    }
                    else if (event.getComponentId().startsWith("server")) {
                        Mentions Interactions = event.getMentions();
                        ServerInfo I = ServerInfo.get(event.getGuild());
                        if (event.getComponentId().startsWith("server-channel-select")) {
                            TextChannel channel = Interactions.getChannels(TextChannel.class).getFirst();
                            event.deferEdit().queue(M -> {
                                try {
                                    if (isAdmin(M, event.getMember()) && hasPermissionInChannel(M, channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MANAGE_WEBHOOKS)) {
                                        if (event.getComponentId().contains("log")) {
                                            I.Channels().setLogChannel(channel);
                                            LogChannelMenu(M, I);
                                        } else if (event.getComponentId().contains("notif")) {
                                            I.Channels().setNotificationChannel(channel);
                                            LogChannelMenu(M, I);
                                        } else if (event.getComponentId().contains("clan-updates")) {
                                            I.Channels().setClanUpdatesChannel(channel);
                                            ClanChannelsMenu(M, I);
                                        } else if (event.getComponentId().contains("tournament-updates")) {
                                            I.Channels().setTournamentUpdatesChannel(channel);
                                            TournamentChannelsMenu(M, I);
                                        } else if (event.getComponentId().contains("leaderboard")) {
                                            I.Channels().setLeaderboardCM(channel);
                                            LeaderboardChannelMenu(M, I);
                                        } else if (event.getComponentId().contains("clanlist")) {
                                            I.Channels().setClanlistCM(channel);
                                            ClanChannelsMenu(M, I);
                                        } else if (event.getComponentId().contains("blacklist")) {
                                            I.Channels().setBlacklistCM(channel);
                                            TournamentChannelsMenu(M, I);
                                        } else if (event.getComponentId().contains("poty")) {
                                            RefreshPOTY();
                                        }
                                        I.Channels().Update();
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

    public static void slashAdmServerManage(InteractionHook M, ServerInfo I) {
        try {
            if (isAdmin(M, M.getInteraction().getMember())) {
                IDCommand CMD = new IDCommand(I.getID());

                EmbedBuilder E = I.getServerEmbed();
                E.setTitle(TL(M,"server-manager"));
                E.setDescription(TL(M,"server-manager-description") + "\n"
                        + "> Clan Captain Role: " + (I.Roles().getClanCaptainRole() != null ? I.Roles().getClanCaptainRole().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Winner Role 1: " + (I.Roles().getWinnerRole1() != null ? I.Roles().getWinnerRole1().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Winner Role 2: " + (I.Roles().getWinnerRole2() != null ? I.Roles().getWinnerRole2().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Winner Role 3: " + (I.Roles().getWinnerRole3() != null ? I.Roles().getWinnerRole3().getAsMention() : "`" + TL(M, "None") + "`") + "\n\n"

                        + "> Log Channel: " + (I.Channels().getLogChannel() != null ? I.Channels().getLogChannel().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Notification Channel: " + (I.Channels().getNotificationChannel() != null ? I.Channels().getNotificationChannel().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Clan Updates Channel: " + (I.Channels().getClanUpdatesChannel() != null ? I.Channels().getClanUpdatesChannel().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Tournament Updates Channel: " + (I.Channels().getTournamentUpdatesChannel() != null ? I.Channels().getTournamentUpdatesChannel().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Leaderboard Message: " + (I.Channels().getLeaderboard().getChannel() != null ? I.Channels().getLeaderboard().getChannel().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Clanlist Message: " + (I.Channels().getClanlist().getChannel() != null ? I.Channels().getClanlist().getChannel().getAsMention() : "`" + TL(M, "None") + "`") + "\n"
                        + "> Blacklist Message: " + (I.Channels().getBlacklist().getChannel() != null ? I.Channels().getBlacklist().getChannel().getAsMention() : "`" + TL(M, "None") + "`") + "\n\n"
                        + "> " + TL(M, "invite_link") +  ": " + (I.getPermanentInviteLink() != null ? I.getPermanentInviteLink() : "`" + TL(M, "None") + "`") + "\n\n"
                        + "> Tiktok: `" + I.getTiktokURL() + "`\n"
                        + "> Twitch: `" + I.getTwitchURL() + "`\n"
                        + "> Twitter: `" + I.getTwitterURL() + "`\n"
                        + "> YouTube: `" + I.getYouTubeURL() + "`\n"
                        + "> Website: `" + I.getWebsiteURL() + "`\n"
                        + "> Instagram: `" + I.getInstagramURL());

                List<ActionRow> ARs = new ArrayList<>();
                List<SelectOption> Toggles = new ArrayList<>();
                if (isTournamentManager(M.getInteraction().getUser())) {
                    Toggles.add(SelectOption.of(TL(M,"Match-Result"), "Match-Result").withDescription("Allow people to report scores on the server.").withDefault(I.areScoresAllowed));
                    Toggles.add(SelectOption.of(TL(M,"Tournament"), "Tournament").withDescription("Allow Tournaments to be counted on the server.").withDefault(I.areTournamentsAllowed));
                }
                Toggles.add(SelectOption.of(TL(M,"Clan-Roles"), "Clan-Roles").withDefault(I.areClanRolesAllowed));
                Toggles.add(SelectOption.of(TL(M,"Clan-Tags"), "Clan-Tags").withDefault(I.areClanTagsAllowed));
                Toggles.add(SelectOption.of(TL(M,"Winner-Roles"), "Winner-Roles").withDefault(I.areWinnerRolesAllowed));
                ARs.add(ActionRow.of(StringSelectMenu.create(CMD.Command("adm-server-manage-toggler"))
                        .setPlaceholder(TL(M, "server-manager-select-1"))
                        .addOptions(Toggles).setRequiredRange(0,10).build()));

                List<SelectOption> Adv = new ArrayList<>();
                Adv.add(SelectOption.of(TL(M,"invite_link"), "invite-link").withEmoji(Emoji.fromUnicode("U+1F4E7")).withDescription(TL(M, "server-invite-link-description")));
                Adv.add(SelectOption.of(TL(M,"Matchmaking") + "...", "Matchmaking").withEmoji(BotEmoji.get("VS3").retrieve()).withDescription(TL(M, "server-matchmaking-description")));
                Adv.add(SelectOption.of(TL(M, "Rank-Roles") + "...", "Rank").withEmoji(League.get(15).getEmoji().retrieve()).withDescription(TL(M, "server-rank-roles-description")));
                Adv.add(SelectOption.of(TL(M,"Leaderboard") + "...", "Leaderboard").withEmoji(Emoji.fromUnicode("U+1F947")).withDescription(TL(M, "server-leaderboard-description")));
                Adv.add(SelectOption.of(TL(M,"server-log-channels"), "log-channels").withEmoji(Emoji.fromUnicode("U+1F4CB")).withDescription(TL(M, "server-log-channels-description")));
                Adv.add(SelectOption.of(TL(M,"server-clan-channels"), "clan-channels").withEmoji(BotEmoji.get("Members").retrieve()).withDescription(TL(M, "server-clan-channels-description")));
                Adv.add(SelectOption.of(TL(M,"server-tournament-channels"), "tournament-channels").withEmoji(Emoji.fromUnicode("U+1F4C6")).withDescription(TL(M, "server-tournament-channels-description")));
                Adv.add(SelectOption.of("Twitch", "media-twitch").withDescription(TL(M, "edit-media-description", "Twitch")).withEmoji(Emoji.fromUnicode("U+1F47E")));
                Adv.add(SelectOption.of("Twitter", "media-twitter").withDescription(TL(M, "edit-media-description", "Twitter")).withEmoji(Emoji.fromUnicode("U+1F426")));
                Adv.add(SelectOption.of("Website", "media-website").withDescription(TL(M, "edit-media-description", "Website")).withEmoji(Emoji.fromUnicode("U+1F4BB")));
                Adv.add(SelectOption.of("YouTube", "media-youtube").withDescription(TL(M, "edit-media-description", "YouTube")).withEmoji(Emoji.fromUnicode("U+25B6 U+FE0F")));
                Adv.add(SelectOption.of("Instagram", "media-instagram").withDescription(TL(M, "edit-media-description", "Instagram")).withEmoji(Emoji.fromUnicode("U+1F4F7")));
                Adv.add(SelectOption.of("Tiktok", "media-tiktok").withDescription(TL(M, "edit-media-description", "Tiktok")).withEmoji(Emoji.fromUnicode("U+1F4F7")));
                ARs.add(ActionRow.of(StringSelectMenu.create(CMD.Command("adm-server-manage-advanced"))
                        .setPlaceholder(TL(M, "server-manager-select-2"))
                        .addOptions(Adv).setRequiredRange(1, 1).build()));
                M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
                I.RefreshGuildInformation();
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public static void slashAdmRankingManage(InteractionHook M, ServerInfo I) {
        try {
            if (isAdmin(M, M.getInteraction().getMember())) {
                IDCommand CMD = new IDCommand(I.getID());

                EmbedBuilder E = I.getServerEmbed();
                E.setTitle(TL(M,"ranking-manager"));
                E.setDescription(TL(M,"ranking-manager-description") + "\n"
                        + "> Points/" + TL(M, "Wins") + ": `" + PlusMinusSignWithNum(I.Ranking().getWinPts()) + "`\n"
                        + "> Points/" + TL(M, "Ties") + ": `" + PlusMinusSignWithNum(I.Ranking().getTiePts()) + "`\n"
                        + "> Points/" + TL(M, "Defeats") + ": `" + PlusMinusSignWithNum(I.Ranking().getLosePts()) + "`\n\n"
                        + "> Points/RNG Min: `" + PlusMinusSignWithNum(I.Ranking().getMinRNG()) + "`\n"
                        + "> Points/RNG Max: `" + PlusMinusSignWithNum(I.Ranking().getMaxRNG()) + "`\n\n"
                        + "> Points/League Diff: `" + I.Ranking().getLeagueDiffPts() + "`\n");
                List<ActionRow> ARs = new ArrayList<>();
                ARs.add(ActionRow.of(StringSelectMenu.create(CMD.Command("adm-ranking-manage-toggler")).setPlaceholder(TL(M,"None")).addOptions(
                        SelectOption.of(TL(M, "ranking-manager-enable-ranking"), "Ranking").withDescription(TL(M, "ranking-manager-enable-ranking-description")).withDefault(I.Ranking().hasPrivateRanking()),
                        SelectOption.of(TL(M, "ranking-manager-enable-leagues"), "Leagues").withDescription(TL(M, "ranking-manager-enable-leagues-description")).withDefault(I.Ranking().hasPrivateLeagues())).setRequiredRange(0,10).build()));

                ARs.add(ActionRow.of(StringSelectMenu.create(CMD.Command("adm-ranking-manage-advanced")).setPlaceholder(TL(M, "Wins") + "/" + TL(M, "Ties") + "/" + TL(M, "Defeats") + "/League")
                        .addOptions(SelectOption.of(TL(M, "Wins") + "/" + TL(M, "Ties") + "/" + TL(M, "Defeats") + "/League", "wtl"),
                                SelectOption.of("RNG Min/Max", "rng"),
                                SelectOption.of("Syncronise with global ranking", "sync-all"),
                                SelectOption.of("Reset server ranking", "delete-all")).setDisabled(!I.Ranking().hasPrivateRanking()).setRequiredRange(0, 1).build()));

                List<SelectOption> O = I.Ranking().getTiers().stream().map(T -> SelectOption.of(T.getName(), String.valueOf(T.getId())).withDescription(T.getStart() + " -> " + T.getEnd()).withEmoji(T.getTierEmoji().retrieve())).toList();
                if (!O.isEmpty()) ARs.add(ActionRow.of(StringSelectMenu.create(CMD.Command("adm-ranking-manage-tier")).setPlaceholder("Manage: " + O.getFirst().getLabel()).addOptions(O).setDisabled(!I.Ranking().hasPrivateLeagues()).setRequiredRange(0, 1).build()));

                ARs.add(ActionRow.of(EntitySelectMenu.create(CMD.Command("adm-ranking-manage-user"), EntitySelectMenu.SelectTarget.USER).setRequiredRange(1, 1).setPlaceholder(TL(M, "ranking-manage-user-select")).setDisabled(!I.Ranking().hasPrivateRanking()).build()));
                M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public static void slashAddBlacklist(InteractionHook M, ServerInfo I, Game game, int amount, User culprit, String reason) throws Exception {
        if (I.getBlacklist(culprit, game) != null) I.getBlacklist(culprit, game).Delete();
        I.addBlacklist(culprit, amount, game, reason.replaceAll("[*~_>-]", ""));
        M.editOriginal(":white_check_mark: " + TL(M, "blacklist-success-1", "**" + culprit.getEffectiveName() + "**", amount, "**" + (game == null ? TL(M,"All") : game.getEmojiFormatted() + " " + game.getName()) + "**") + "\n**" + TL(M, "Reason") + ":** *" + reason + "*").queue();
        I.LogSlash("**" + M.getInteraction().getUser().getEffectiveName() + ":** " + TL(M, "blacklist-success-1", "**" + culprit.getEffectiveName() + "**", amount, "**" + (game == null ? TL(M,"All") : game.getEmojiFormatted() + " " + game.getName()) + "**") + "\n**" + TL(M, "Reason") + ":** *" + reason + "*");
    }
    public static void slashRemoveBlacklist(InteractionHook M, ServerInfo I, Game game, User culprit) throws Exception {
        if (I.getBlacklist(culprit, game) != null) I.getBlacklist(culprit, game).Delete();
        M.editOriginal(":white_check_mark: " + TL(M, "blacklist-success-2", "**" + culprit.getEffectiveName() + "**", "**" + (game == null ? TL(M,"All") : game.getEmojiFormatted() + " " + game.getName()) + "**")).queue();
        I.LogSlash("**" + M.getInteraction().getUser().getEffectiveName() + ":** " + TL(M, "blacklist-success-2", "**" + culprit.getEffectiveName() + "**", "**" + (game == null ? TL(M,"All") : game.getEmojiFormatted() + " " + game.getName()) + "**"));
    }
    public static void slashInscription(InteractionHook M, ServerInfo I, Game game, String name, long start, int vstype, String url, String messageurl, Message.Attachment optionalImage, String optionalMessage
            , TextChannel InscriptionChannel, TextChannel PanelChannel, TextChannel RulesChannel, TextChannel ResultChannel, TextChannel PredictionChannel, Role ParticipantRole, Role OrganizerRole, int CAP, boolean notify, Long customStartTime) throws Exception {
        Tournament T = retrieveFromAccessedTournaments(url, name, false);
        ServerInfo IsOwnedBy = retrieveServerOwnedFrom(T);
        if (IsOwnedBy != null && IsOwnedBy.getID() != I.getID()) {
            M.editOriginal(TL(M, "tournament-set-not-belong-this-server", "**" + IsOwnedBy.getName() + "**")).queue();
        } else {
            // Check if tournament is eligible for Inscriptions
            if (T.getState().equals(TournamentState.PENDING))  {
                // Check if channels are accessible
                Permission[] Ps = {Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_HISTORY};
                if (isChannelOfType(M, InscriptionChannel, ChannelType.TEXT) && hasPermissionInChannel(M, InscriptionChannel, Ps)) {
                    if (isChannelOfType(M, PanelChannel, ChannelType.TEXT) && hasPermissionInChannel(M, PanelChannel, Ps)) {
                        if (isChannelOfType(M, ResultChannel, ChannelType.TEXT) && hasPermissionInChannel(M, ResultChannel, Ps)) {
                            if (PredictionChannel == null || (isChannelOfType(M, PredictionChannel, ChannelType.TEXT) && hasPermissionInChannel(M, PredictionChannel, Ps))) {
                                if (customStartTime == null || (customStartTime > Instant.now().getEpochSecond())) {
                                    if (hasPermissionOverRole(M, ParticipantRole)) {
                                        if (I.getGuild().getMembersWithRoles(ParticipantRole).isEmpty()) {
                                            // Check if tournament is accessible
                                            SChallonge_Tournament C = new SChallonge_Tournament(I, T);
                                            C.setSignupCap(Math.max(0, CAP));
                                            C.setVSAmount(vstype);
                                            C.setGameName(game);
                                            C.setPublic(notify);
                                            C.setRegistrationStartTime(Instant.now());
                                            C.setParticipantRole(ParticipantRole);
                                            C.setOrganizerRole(OrganizerRole);
                                            C.setResultChannel(ResultChannel);
                                            C.setRulesChannel(RulesChannel);
                                            C.setPanelChannel(PanelChannel);
                                            C.setPredictionChannel(PredictionChannel);
                                            C.setStartAtTime(Instant.ofEpochSecond(customStartTime != null ? customStartTime : start));
                                            C.setSignupImageURL(optionalImage != null && optionalImage.isImage() ? getFileUrl(optionalImage.getProxy().downloadToFile(new File(TempDirectory + "/banner.png")).get(), "banner.png") : null);
                                            if (!C.getDescription().toLowerCase().contains("Inazuma Competitive".toLowerCase())) C.setDescription(C.getDescription() + "\n\nPowered by Inazuma Competitive");

                                            C.RefreshPanelMessage();

                                            try {
                                                Message InscriptionMessage = getMessageByLink(messageurl);
                                                C.setInscriptionChannel(InscriptionMessage);
                                                C.setInscriptionByBot(false);
                                            } catch (Exception ignored) {
                                                C.setInscriptionChannel(InscriptionChannel);
                                                C.setInscriptionByBot(true);
                                            }
                                            C.RefreshInscriptionMessage();
                                            C.setInscriptionChannelInviteLink(I.getGuild().getVanityUrl() != null ? I.getGuild().getVanityUrl() : I.getPermanentInviteLink() != null ? I.getPermanentInviteLink() : I.getGuild().getSelfMember().hasPermission(InscriptionChannel, Permission.CREATE_INSTANT_INVITE) ? C.getInscriptionChannel().getChannel().createInvite().setMaxAge(7L, TimeUnit.DAYS).submit().orTimeout(3, TimeUnit.SECONDS).get().getUrl() : null);

                                            try {
                                                if (M.getInteraction().getMember().hasPermission(Permission.MANAGE_EVENTS)) {
                                                    I.getGuild().createScheduledEvent(name, I.getGuild().getName(), OffsetDateTime.ofInstant(C.getStartAtTime(), ProgramZoneId), OffsetDateTime.ofInstant(Instant.ofEpochSecond(start).plus(7, ChronoUnit.DAYS), ProgramZoneId))
                                                            .setImage(optionalImage != null && optionalImage.isImage() ? Icon.from(optionalImage.getProxy().download().get()) : null)
                                                            .setDescription(TLG(I, "tournament-event-desc", name, start + ""))
                                                            .queue();
                                                }
                                            } catch (IOException ignored) {}

                                            M.editOriginal(TL(M, "Done") + " !").queue();
                                            if (notify && C.getInscriptionChannelInviteLink() != null && !C.getInscriptionChannelInviteLink().isEmpty()) {
                                                C.LogTournamentAnnouncement(false);
                                                M.editOriginal(TL(M, "tournament-register-dm", C.NotifyAllByDM(optionalMessage) + "")).queue();
                                            }
                                            C.Update();
                                        } else {
                                            M.editOriginal(":x: " + TL(M, "tournament-role-fail")).queue();
                                        }
                                    }
                                } else {
                                    M.editOriginal(TL(M, "tournament-registration-fail-epoch-second")).queue();
                                }
                            }
                        }
                    }
                }
            } else {
                M.editOriginal(TL(M, "tournament-set-no-longer-pending")).queue();
            }
        }
    }
    public static void slashViewTourney(InteractionHook M, ServerInfo I, String url) {
        if (url == null) {
            List<SelectOption> options = new ArrayList<>();
            List<ActionRow> row = new ArrayList<>();
            List<SChallonge_Tournament> T = I.getChallonges(false);
            for (SChallonge_Tournament MM : T) {
                options.add(SelectOption.of(MM.getName(), MM.getId() + "")
                        .withDescription(TL(M,"Players") + ": " + MM.getParticipantCount() + " | " + MM.getType() + " | " + MM.getState().name())
                        .withEmoji(MM.getGame().getEmoji().retrieve()));
                if (options.size() % 25 == 0) {
                    row.add(ActionRow.of(StringSelectMenu.create("challonge-visualise-" + row.size())
                            .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                            .addOptions(options).build()));
                    options = new ArrayList<>();
                }
            }
            if (!options.isEmpty()) {
                row.add(ActionRow.of(StringSelectMenu.create("challonge-visualise-" + row.size())
                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1)
                        .addOptions(options).build()));
            }
            EmbedBuilder E = I.getServerEmbed();
            E.setTitle(TL(M,"tournament-manager"));
            E.setDescription(TL(M,"tournament-manager-description") + "\n" + TL(M,"Tournaments") + ": **" + T.size() + "**");
            M.editOriginalEmbeds(E.build()).setComponents(row).queue();
        } else {
            try {
                Tournament T = retrieveFromAccessedTournaments(url, url, true);
                ServerInfo IsOwnedBy = retrieveServerOwnedFrom(T);
                if (IsOwnedBy == null || IsOwnedBy.getID() == I.getID() || isTournamentManager(M.getInteraction().getUser())) {
                    SChallonge_Tournament CT = I.getChallonge(T.getId());
                    if (CT != null) {
                        ChallongeCommand CMD = new ChallongeCommand("null");
                        CMD.ChallongeID = T.getId();
                        CT.TournamentManageUI(M, CMD);
                    }
                } else {
                    M.editOriginal(TL(M, "tournament-set-not-belong-this-server", "**" + IsOwnedBy.getName() + "**")).queue();
                }
            } catch (Exception e) {
                replyException(M, e);
            }
        }
    }
    public static void slashDQ(InteractionHook M, ServerInfo I, SChallonge_Tournament T, User Target, String reason) {
        if (T != null && (T.isUnderway() || T.isGroupStageUnderway())) {
            if (Target == M.getInteraction().getUser() || T.isOrganiser(M.getInteraction().getUser())) {
                SChallonge_Participant DQParticipant = T.getTeamByMyId(Target.getId());
                if (DQParticipant != null) {
                    EmbedBuilder E = T.getTournamentEmbed();
                    getWebhookOfChannel(T.getMatchResultChannel(), CC -> {
                        try {
                            for (SChallonge_Match Match : DQParticipant.getUnplayedMatches()) {
                                if (T.getVSAmount() > 1) {
                                    if (Match.canTeamDQ(DQParticipant)) {
                                        SubMatch CSM = Match.getSubMatch(Target.getIdLong());
                                        if (CSM.getP1ID() == Target.getIdLong()) {
                                            CSM.AddScore(0, 3);
                                        } else if (CSM.getP2ID() == Target.getIdLong()) {
                                            CSM.AddScore(3, 0);
                                        }
                                        Match.validateTeamScores(Match.getOpponent(DQParticipant.getId()).getId());
                                        Match.Update();
                                        T.RefreshPanelMessage();
                                    } else {
                                        E.setDescription(":x: " + TL(M, "challonge-manage-dq-fail-abuse"));
                                        M.editOriginalEmbeds(E.build()).queue();
                                        return;
                                    }
                                } else {
                                    if (Match.getPlayer1() != null && Target.getIdLong() == Match.getPlayer1().getLeaderID()) {
                                        Match.AddScore(0, 0, Match.getParticipantID2());
                                    } else if (Match.getPlayer2() != null && Target.getIdLong() == Match.getPlayer2().getLeaderID()) {
                                        Match.AddScore(0, 0, Match.getParticipantID1());
                                    }
                                    Match.Update();
                                    T.ResyncChallonge();
                                    T.Update();
                                    for (SChallonge_Match M1 : Match.getPlayer1().getUnplayedMatches()) M1.NotifyNextOpponent();
                                    for (SChallonge_Match M2 : Match.getPlayer2().getUnplayedMatches()) M2.NotifyNextOpponent();
                                }
                                CC.send(Match.getDQMessage(I.getGuild(), reason).build());
                                T.CleanTournament();
                            }
                        } catch (Exception ignored) { }
                    });
                    if (Target.equals(M.getInteraction().getUser())) {
                        E.setDescription(":white_check_mark: " + TL(M, "challonge-manage-success-3"));
                    } else {
                        E.setDescription(":white_check_mark: " + TL(M, "challonge-manage-success-3-other", "**" + Target.getEffectiveName() + "**"));
                    }
                    M.editOriginalEmbeds(E.build()).queue();
                } else {
                    M.editOriginal(TL(M, "tournament-cant-find-you")).queue();
                }
            } else {
                M.editOriginal(TL(M, "tournament-dq-fail-organiser")).queue();
            }
        } else {
            M.editOriginal(TL(M, "tournament-dq-fail")).queue();
        }
    }
    public static void slashChallongeImage(InteractionHook M, String Challonge, Message.Attachment Image) throws DataAccessException {
        Tournament T = ChallongeAccount.getTournament(cleanChallongeURL(Challonge), true, true);
        if (T != null) {
            SChallonge_Tournament CT =  SChallonge_Tournament.get(T.getId());
            if (CT != null && (isTournamentManager(M.getInteraction().getUser()) || (isAdmin(M.getInteraction().getMember()) && CT.ServerID == M.getInteraction().getGuild().getIdLong()))) {
                if (Image.isImage()) {
                    new File(MainDirectory + "/server/" + CT.getServerID() + "/img/tournaments/").mkdirs();
                    Image.getProxy().downloadToFile(new File(MainDirectory + "/server/" + CT.getServerID() + "/img/tournaments/" + T.getId() + ".png")).whenComplete(((file, throwable) -> {
                        try (FileUpload FU = FileUpload.fromData(file, "image.png")) {
                            M.editOriginal(T.getFullChallongeUrl()).setAttachments(FU).queue();
                        } catch (IOException e) {
                            replyException(M, e);
                        }
                    }));
                } else {
                    M.editOriginal(TL(M, "image-fail-extension")).queue();
                }
            } else {
                M.editOriginal("Can't find tournament in bot data...").queue();
            }
        } else {
            M.editOriginal("Can't find tournament on server...").queue();
        }
    }
    public static void slashAdmShopManage(InteractionHook M, ServerInfo I) {
        try {
            if (isAdmin(M, M.getInteraction().getMember())) {
                if (I.Channels().getLogChannel() == null) M.editOriginal(TL(M, "server-log-channel-required")).queue();
                else if (I.getCurrency() == null) M.editOriginal(TL(M, "server-economy-item-add-error")).queue();
                else {
                    List<ActionRow> ARs = new ArrayList<>();
                    List<SelectOption> Toggles = new ArrayList<>();
                    Item Currency = I.getCurrency();
                    String s = "> Currency: " + (Currency != null ? Currency.getEmojiFormatted() + " " + Currency.getName() : "`" + TL(M, "None") + "`") + "\n";
                    for (Item IT : I.listItems()) {
                        s += "> " + IT.getEmojiFormatted() + " " + IT.getName() + " (" + IT.getPriceAsString() + " " + (Currency != null ? Currency.getEmojiFormatted() : "") + ")\n";
                        Toggles.add(SelectOption.of(IT.getName(), IT.getId() + "").withEmoji(IT.getEmoji().retrieve()));
                        if (Toggles.size() % 25 == 0 && ARs.size() < 5) {
                            ARs.add(ActionRow.of(StringSelectMenu.create("adm-shop-manage-item")
                                    .setPlaceholder(Toggles.getFirst().getLabel())
                                    .addOptions(Toggles).setRequiredRange(1, 1).build()));
                            Toggles = new ArrayList<>();
                        }
                    }
                    s += "\n> " + TL(M, "Items") + ": " + I.listItems().size();
                    if (!Toggles.isEmpty() && ARs.size() < 5)
                        ARs.add(ActionRow.of(StringSelectMenu.create("adm-shop-manage-item")
                                .setPlaceholder(Toggles.getFirst().getLabel())
                                .addOptions(Toggles).setRequiredRange(1, 1).build()));

                    EmbedBuilder E = I.getServerEmbed();
                    E.setTitle(TL(M, "shop-manager"));
                    E.setDescription(TL(M, "shop-manager-description") + "\n" + s);
                    if (!ARs.isEmpty()) M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
                    else M.editOriginalEmbeds(E.build()).queue();
                }
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }

    public static Tournament retrieveFromAccessedTournaments(String url, String name, boolean acceptFinished) throws DataAccessException {
        url = cleanChallongeURL(url);
        Tournament T = ChallongeAccount.getTournament(url);
        if (T == null) {
            for (Tournament Ts : ChallongeAccount.getTournaments()) {
                if (acceptFinished || Ts.getState().equals(TournamentState.PENDING)) {
                    try {
                        if (Ts.getUrl().equals(url) || similarity(Ts.getName(), name, true) > 95) {
                            break;
                        } else if (Ts.getId() == Long.parseLong(url) || Ts.getId() == Long.parseLong(name)) {
                            T = Ts;
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return T;
    }
    private static ServerInfo retrieveServerOwnedFrom(Tournament T) {
        return DatabaseObject.doQuery(ServerInfo.class,"""
                SELECT S.* FROM inazuma_competitive.serverinfo S
                JOIN inazuma_competitive.challonge_tournament CT ON CT.ID = ? AND CT.ServerID = S.ID
                """, T.getId()).orElse(null);
    }


    public static void MatchmakingSetupMenuChoice(InteractionHook M, ServerInfo I) {
        EmbedBuilder E = I.getServerEmbed();
        E.setTitle(TL(M,"server-manager"));
        E.setDescription(TL(M,"server-matchmaking-description"));
        List<SelectOption> optionsRank = new ArrayList<>();
        for (Game G : Game.values()) {
            ServerInfo.ServerInfo_Matchmaking MM = I.getMatchmaking(G);
            if (MM != null) {
                TextChannel channel = MM.getChannel();
                Role role = MM.getRole();
                if (channel != null && role != null) optionsRank.add(SelectOption.of(G.getFullName(), G.getCode()).withDescription("#" + channel.getName() + " /// @" + role.getName()).withEmoji(G.getEmoji().retrieve()));
                else optionsRank.add(SelectOption.of(G.getFullName(), G.getCode()).withDescription("-").withEmoji(G.getEmoji().retrieve()));
            } else optionsRank.add(SelectOption.of(G.toString(), G.toString()).withDescription("-").withEmoji(G.getEmoji().retrieve()));
        }
        StringSelectMenu menu1 = StringSelectMenu.create("adm-matchmaking")
                .setPlaceholder("Matchmakings...").addOptions(optionsRank)
                .setRequiredRange(1, 1).build();
        M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu1)).queue();
    }
    public static void RankSetupMenu(InteractionHook M, ServerInfo I) {
        EmbedBuilder E = I.getServerEmbed();
        E.setTitle(TL(M,"server-manager"));
        E.setDescription(TL(M,"server-rank-roles-description") + "\n\n" + Game.values().stream().map(g -> g.getName() + ": " + (I.isGameRankAllowed(g) ? ":white_check_mark:" : ":x:")).collect(Collectors.joining("\n")));

        List<SelectOption> Games = new ArrayList<>();
        Games.add(SelectOption.of("Global", "Global"));
        for (Game G : Game.values()) Games.add(SelectOption.of(G.getCode(), G.getCode()).withDescription(TL(M, "view-of", G.getFullName())).withDefault(I.isGameRankAllowed(G)).withEmoji(G.getEmoji().retrieve()));
        StringSelectMenu menu1 = StringSelectMenu.create("adm-rank-type")
                .setPlaceholder("Ranks...").addOptions(Games)
                .setRequiredRange(0,25).build();
        M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu1)).queue();
    }
    public static void LeaderboardChannelMenu(InteractionHook M, ServerInfo I) {
        try {
            EmbedBuilder E = I.getServerEmbed();
            E.setTitle(TL(M,"server-manager"));
            E.setDescription(TL(M,"server-leaderboard-description") + "\n\n" + Game.values().stream().map(g -> g.getName() + ": " + (I.isGameBoardAllowed(g) ? ":white_check_mark:" : ":x:")).collect(Collectors.joining("\n")));

            GuildChannel leaderboardchannel = I.Channels().getLeaderboard() != null ? I.Channels().getLeaderboard().getChannel() : null;
            E.addField(TL(M, "Leaderboard"), leaderboardchannel != null ? leaderboardchannel.getAsMention() : "N/A", false);

            EntitySelectMenu.Builder menu = EntitySelectMenu.create("server-channel-select-leaderboard", EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(leaderboardchannel != null ? leaderboardchannel.getName() : "-")
                    .setChannelTypes(ChannelType.TEXT)
                    .setRequiredRange(1, 1);
            if (leaderboardchannel != null) menu.setDefaultValues(EntitySelectMenu.DefaultValue.channel(leaderboardchannel.getId()));

            List<SelectOption> Games = new ArrayList<>();
            for (Game G : Game.values()) Games.add(SelectOption.of(G.getCode(), G.getCode()).withDescription(TL(M, "view-of", G.getFullName())).withDefault(I.isGameBoardAllowed(G)).withEmoji(G.getEmoji().retrieve()));
            StringSelectMenu menu2 = StringSelectMenu.create("adm-leaderboard-type")
                    .setPlaceholder("Leaderboards...").addOptions(Games)
                    .setRequiredRange(0, 25).build();
            List<SelectOption> optionsActivate = new ArrayList<>();
            optionsActivate.add(SelectOption.of(TL(M, "server-leaderboard-show-serv-member-only"), "show-server-members-only").withDefault(I.showBoardMembersOnly));
            StringSelectMenu menu3 = StringSelectMenu.create("adm-leaderboard-config")
                    .setPlaceholder(optionsActivate.getFirst().getLabel()).addOptions(optionsActivate)
                    .setRequiredRange(0, 25).build();
            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu.build()), ActionRow.of(menu2), ActionRow.of(menu3)).queue();
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public static void LogChannelMenu(InteractionHook M, ServerInfo I) {
        try {
            EmbedBuilder E = I.getServerEmbed();
            E.setTitle(TL(M,"server-manager"));
            E.setDescription(TL(M,"server-log-channels-description"));

            GuildChannel logchannel = I.Channels().getLogChannel();
            E.addField(TL(M,"Log"), logchannel != null ? logchannel.getAsMention() : "N/A", false);
            GuildChannel notificationChannel = I.Channels().getNotificationChannel();
            E.addField(TL(M,"Notification"), notificationChannel != null ? notificationChannel.getAsMention() : "N/A", false);

            EntitySelectMenu.Builder menu = EntitySelectMenu.create("server-channel-select-log", EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(logchannel != null ? logchannel.getName() : "-")
                    .setChannelTypes(ChannelType.TEXT)
                    .setRequiredRange(1, 1);
            if (logchannel != null) menu.setDefaultValues(EntitySelectMenu.DefaultValue.channel(logchannel.getId()));
            EntitySelectMenu.Builder menu2 = EntitySelectMenu.create("server-channel-select-notif", EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(notificationChannel != null ? notificationChannel.getName() : "-")
                    .setChannelTypes(ChannelType.TEXT)
                    .setRequiredRange(1, 1);
            if (notificationChannel != null) menu2.setDefaultValues(EntitySelectMenu.DefaultValue.channel(notificationChannel.getId()));

            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu.build()), ActionRow.of(menu2.build())).queue();
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public static void ClanChannelsMenu(InteractionHook M, ServerInfo I) {
        try {
            EmbedBuilder E = new EmbedBuilder();
            E.setTitle(TL(M,"server-manager"));
            E.setAuthor(" • " + I.getGuild().getName(), null, I.getGuild().getIconUrl());
            E.setThumbnail(I.getGuild().getIconUrl());
            E.setColor(I.getColor());
            E.setDescription(TL(M,"server-clan-channels-description"));

            GuildChannel clanupdateschannel = I.Channels().getClanUpdatesChannel();
            E.addField(TL(M,"clan-updates"), clanupdateschannel != null ? clanupdateschannel.getAsMention() : "N/A", false);
            GuildChannel clanlistchannel = I.Channels().getClanlist() != null ? I.Channels().getClanlist().getChannel() : null;
            E.addField(TL(M,"clan-list"), clanlistchannel != null ? clanlistchannel.getAsMention() : "N/A", false);

            EntitySelectMenu.Builder menu1 = EntitySelectMenu.create("server-channel-select-clan-updates", EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(clanupdateschannel != null ? clanupdateschannel.getName() : "-")
                    .setChannelTypes(ChannelType.TEXT)
                    .setRequiredRange(1, 1);
            if (clanupdateschannel != null) menu1.setDefaultValues(EntitySelectMenu.DefaultValue.channel(clanupdateschannel.getId()));

            EntitySelectMenu.Builder menu2 = EntitySelectMenu.create("server-channel-select-clanlist", EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(clanlistchannel != null ? clanlistchannel.getName() : "-")
                    .setChannelTypes(ChannelType.TEXT)
                    .setRequiredRange(1, 1);
            if (clanlistchannel != null) menu2.setDefaultValues(EntitySelectMenu.DefaultValue.channel(clanlistchannel.getId()));

            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu1.build()), ActionRow.of(menu2.build())).queue();
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public static void TournamentChannelsMenu(InteractionHook M, ServerInfo I) {
        try {
            EmbedBuilder E = I.getServerEmbed();
            E.setTitle(TL(M,"server-manager"));
            E.setDescription(TL(M,"server-tournament-channels-description"));

            GuildChannel tournamentupdatechannel = I.Channels().getTournamentUpdatesChannel();
            E.addField(TL(M,"tournament-updates"), tournamentupdatechannel != null ? tournamentupdatechannel.getAsMention() : "N/A", false);
            GuildChannel blacklistchannel = I.Channels().getBlacklist() != null ? I.Channels().getBlacklist().getChannel() : null;
            E.addField(TL(M,"Blacklist"), blacklistchannel != null ? blacklistchannel.getAsMention() : "N/A", false);
            GuildChannel potychannel = null;
            E.addField(TL(M,"POTY"), potychannel != null ? potychannel.getAsMention() : "N/A", false);

            EntitySelectMenu.Builder menu1 = EntitySelectMenu.create("server-channel-select-tournament-updates", EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(tournamentupdatechannel != null ? tournamentupdatechannel.getName() : "-")
                    .setChannelTypes(ChannelType.TEXT).setRequiredRange(1, 1);
            if (tournamentupdatechannel != null) menu1.setDefaultValues(EntitySelectMenu.DefaultValue.channel(tournamentupdatechannel.getId()));
            EntitySelectMenu.Builder menu2 = EntitySelectMenu.create("server-channel-select-blacklist", EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(blacklistchannel != null ? blacklistchannel.getName() : "-")
                    .setChannelTypes(ChannelType.TEXT).setRequiredRange(1, 1);
            if (blacklistchannel != null) menu2.setDefaultValues(EntitySelectMenu.DefaultValue.channel(blacklistchannel.getId()));
            EntitySelectMenu.Builder menu3 = EntitySelectMenu.create("server-channel-select-poty", EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(potychannel != null ? potychannel.getName() : "-")
                    .setChannelTypes(ChannelType.TEXT).setRequiredRange(1, 1);
            if (potychannel != null) menu3.setDefaultValues(EntitySelectMenu.DefaultValue.channel(potychannel.getId()));

            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu1.build()), ActionRow.of(menu2.build()), ActionRow.of(menu3.build().asDisabled())).queue();
        } catch (Exception e) {
            replyException(M, e);
        }
    }

    public static void MatchmakingSetupMenu(InteractionHook M, ServerInfo I, MatchmakingCommand CMD) {
        try {
            EmbedBuilder E = I.getServerEmbed();
            E.setTitle(TL(M,"server-manager"));
            E.setDescription(TL(M, "matchmaking-manage-description"));
            E.setFooter(CMD.Game.getFullName());

            E.addField(TL(M,"Channel") + ":", CMD.Channel != null ? CMD.Channel.getAsMention() : "N/A", false);
            E.addField(TL(M,"Role") + ":", CMD.Role != null ? CMD.Role.getAsMention() : "N/A", false);
            EntitySelectMenu.Builder menu = EntitySelectMenu.create(CMD.Command("matchmaking-select-channel"), EntitySelectMenu.SelectTarget.CHANNEL)
                    .setPlaceholder(CMD.Channel != null ? CMD.Channel.getName() : "#matchmaking-channel")
                    .setChannelTypes(ChannelType.TEXT)
                    .setRequiredRange(1, 1);
            EntitySelectMenu.Builder menu2 = EntitySelectMenu.create(CMD.Command("matchmaking-select-role"), EntitySelectMenu.SelectTarget.ROLE)
                    .setPlaceholder(CMD.Role != null ? CMD.Role.getName() : "@Matchmaking")
                    .setRequiredRange(1, 1);
            if (CMD.ChannelID != null) menu.setDefaultValues(EntitySelectMenu.DefaultValue.channel(CMD.ChannelID));
            if (CMD.RoleID != null) menu2.setDefaultValues(EntitySelectMenu.DefaultValue.role(CMD.RoleID));
            Button btn = Button.danger(CMD.Command("matchmaking-remove"), TL(M,"Delete")).withDisabled(CMD.Channel == null && CMD.Role == null);
            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu.build()), ActionRow.of(menu2.build()), ActionRow.of(btn)).queue();
        } catch (Exception e) {
            replyException(M, e);
        }
    }

    public static void ManageUserRanking(InteractionHook M, RankingCommand CMD) {
        try {
            List<ActionRow> ARs = new ArrayList<>();
            ARs.add(ActionRow.of(StringSelectMenu.create(CMD.Command("adm-ranking-manage-user-game"))
                    .setPlaceholder(TL(M,"None")).addOptions(Game.getSelectOptions(M, List.of(CMD.getGame()))).setRequiredRange(1,1).build()));

            ServerInfo I = ServerInfo.get(CMD.ID);
            EmbedBuilder E = I.getServerEmbed();
            E.setTitle(TL(M,"ranking-manager"));
            if (CMD.getGame() != null) {
                Profile_Game_S PG = Profile_Game_S.get(CMD.UserID, CMD.ID, CMD.getGame());
                E.setDescription(TL(M, "ranking-manage-user-description") + "\n"
                        + "**" + PG.getGame().getEmojiFormatted() + " " + PG.getGame().getFullName() + "**\n"
                        + "> :medal: " + TL(M, "Medals") + ": `" + PG.getMedals() + "`\n"
                        + "> " + TL(M, "Wins") + ": `" + PG.getWins() + "`\n"
                        + "> " + TL(M, "Ties") + ": `" + PG.getTies() + "`\n"
                        + "> " + TL(M, "Defeats") + ": `" + PG.getLoses() + "`\n"
                        + "> " + TL(M, "GoalsScored") + ": `" + PG.getGoalsScored() + "`\n"
                        + "> " + TL(M, "GoalsTaken") + ": `" + PG.getGoalsTaken() + "`\n"
                        + "> " + TL(M, "Win_Streak") + ": `" + PG.getWinStreak() + "`\n"
                        + "> " + TL(M, "Best_Win_Streak") + ": `" + PG.getHighestWinStreak() + "`\n");
                ARs.add(ActionRow.of(Button.secondary(CMD.Command("adm-ranking-manage-user-wtl"), "Edit Win/Tie/Lose/GoalsScored/GoalsTaken"), Button.secondary(CMD.Command("adm-ranking-manage-user-medalstr"), "Edit Medals/Streak")));
            }
            M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
        } catch (Exception e) {
            replyException(M, e);
        }
    }


    public void EditLeagueTier(DoubleIDCommand CMD, InteractionHook M, League.League_Tier T) {
        List<ActionRow> ARs = new ArrayList<>();
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M, "ranking-manager"));
        E.setAuthor(T.getName());
        E.setDescription(TL(M, "ranking-manager-leagues-description"));
        E.setColor(T.getColor());
        E.setThumbnail(T.getImageURL());

        E.addField(TL(M, "Name"), T.getName(), true);
        E.addField(TL(M, "Emoji"), T.getTierEmojiFormatted(), true);
        E.addField(TL(M, "Details"), T.getStart() + " -> " + T.getEnd(), false);

        Button BTN1 = Button.success(CMD.Command("adm-ranking-tier-manage-name"), TL(M,"Name") + "/Emoji/" + TL(M,"Color"));
        Button BTN2 = Button.success(CMD.Command("adm-ranking-tier-manage-medals"), TL(M,"Medals") + "/" + TL(M,"Power"));
        Button BTN3 = Button.danger(CMD.Command("adm-ranking-tier-manage-delete"), TL(M,"Delete"));
        ARs.add(ActionRow.of(BTN1, BTN2, BTN3));
        List<SelectOption> O = new ArrayList<>();
        for (League L : T.getLeagues()) {
            O.add(SelectOption.of(L.getName(), String.valueOf(L.getId())));
            if (O.size() % 25 == 0 && ARs.size() < 5) {
                ARs.add(ActionRow.of(StringSelectMenu.create("adm-ranking-manage-league-" + ARs.size())
                        .setPlaceholder(O.getFirst().getLabel())
                        .addOptions(O).setRequiredRange(1, 1).build()));
                O = new ArrayList<>();
            }
        }
        if (!O.isEmpty() && ARs.size() < 5) ARs.add(ActionRow.of(StringSelectMenu.create("adm-ranking-manage-league-" + ARs.size())
                .setPlaceholder(O.getFirst().getLabel()).addOptions(O).setRequiredRange(1, 1).build()));
        M.editOriginalEmbeds(E.build()).setComponents(ARs).queue();
    }
    public void EditLeague(DoubleIDCommand CMD, InteractionHook M, League L) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M, "ranking-manager"));
        E.setAuthor(L.getName());
        E.setDescription(TL(M, "ranking-manager-leagues-description"));
        E.setColor(L.getTier().getColor());
        E.setThumbnail(L.getImageURL());

        E.addField(TL(M, "Name"), L.getName(), true);
        E.addField(TL(M, "Emoji"), L.getEmojiFormatted(), true);
        E.addField(TL(M, "Details"), L.getStart() + " -> " + L.getEnd() + " | " + BotEmoji.get("POW").getFormatted() + " " + L.getPower(), false);

        Button BTN1 = Button.success(CMD.Command("adm-ranking-league-manage-name"), TL(M,"Name") + "/Emoji");
        Button BTN2 = Button.success(CMD.Command("adm-ranking-league-manage-medals"), TL(M,"Medals") + "/" + TL(M,"Power"));
        Button BTN3 = Button.danger(CMD.Command("adm-ranking-league-manage-delete"), TL(M,"Delete"));
        M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(BTN1, BTN2, BTN3)).queue();
    }
    public void EditShopItem(DoubleIDCommand CMD, InteractionHook M, ServerInfo I, Item IT) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M, "shop-manager"));
        E.setAuthor(IT.getName());
        E.setDescription(TL(M, "shop-manager-description"));
        E.setColor(I.getColor());
        E.setImage(IT.getImageURL());

        E.addField(TL(M, "Name"), IT.getEmojiFormatted() + " " + IT.getName(), true);
        E.addField(TL(M, "Description"), IT.getDescription(), true);
        E.addField(TL(M, "Cost"), IT.getPriceAsString(), true);
        
        Button BTN1 = Button.success(CMD.Command("adm-shop-item-manage-details"), TL(M,"Details"));
        Button BTN2 = Button.danger(CMD.Command("adm-shop-item-manage-delete"), TL(M,"Delete"));
        M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(BTN1, BTN2)).queue();
    }

}
