package iecompbot.interaction;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import iecompbot.Constants;
import iecompbot.Terminal;
import iecompbot.ai.info.*;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.event.Event;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.Criminal;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.*;
import static iecompbot.Utility.ShutdownWithTimeout;
import static iecompbot.Utility.getHHmmss;
import static iecompbot.img.ImgUtilities.getHexValue;
import static iecompbot.interaction.GuildReady.LoadingTheCommands;
import static iecompbot.interaction.GuildReady.setBotVariables;
import static iecompbot.interaction.listeners.MDFFeatures.ClearClanTags;
import static iecompbot.objects.BotManagers.*;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static my.utilities.util.Utilities.*;

@Component
public class Automation extends ListenerAdapter {

    public static boolean isStartup = true;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LogBot(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        LogBot(event);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        LogBot(event);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        LogBot(event);
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        LogBot(event);
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        LogBot(event);
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        LogBot(event);
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.isFromGuild()) {
            if (isBotOwner(event.getUserIdLong())) {
                try {
                    if (event.getReaction().getEmoji().asUnicode().getAsCodepoints().contains("U+2754")) {
                        event.retrieveMessage().queue(M -> {
                            String s = "Message ID: " + M.getId();
                            s = s + "Author ID: " + M.getAuthor().getId() + "\n";

                            s = s + "Content: " + M.getContentRaw() + "\n";
                            s = s + "\n**Attachments:** " + M.getAttachments().size();
                            for (Message.Attachment A : M.getAttachments()) {
                                s = s + "\n" + A.getFileName() + ": " + A.getUrl();
                            }
                            s = s + "\n**Embeds:** " + M.getEmbeds().size();
                            for (MessageEmbed E : M.getEmbeds()) {
                                s = s + "\nSize: " + E.getLength();
                                s = s + "\nTitle: " + E.getTitle();
                                s = s + "\nDescription: " + E.getDescription();
                                s = s + "\nColor: " + getHexValue(E.getColor());
                                s = s + "\nFooter: " + (E.getFooter() != null ? E.getFooter().getText() : null);
                                s = s + "\nTimestamp: " + (E.getTimestamp() != null ? E.getTimestamp().toInstant().getEpochSecond() : null);
                                s = s + "\nThumbnail: " + (E.getThumbnail() != null ? E.getThumbnail().getUrl() : null);
                                s = s + "\nImage: " + (E.getImage() != null ? E.getImage().getUrl() : null);
                                s = s + "\nFields: " + E.getFields().size();
                            }
                            System.out.println(s);
                            sendPrivateMessage(getUserByID(Constants.BotOwnerID), s);
                        });
                    }
                    else if (event.getReaction().getEmoji().asUnicode().getAsCodepoints().contains("U+1f4c2")) {
                        Desktop.getDesktop().open(new File(MainDirectory + "/server/" + event.getGuild().getId() + "/"));
                    }
                    else if (event.getGuild().getId().equals("930718276542136400")) {
                        sendPrivateMessage(event.getUser(), event.getReaction().getEmoji().asUnicode().getAsCodepoints());
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            ExecutorService SingleThread = Executors.newCachedThreadPool();
            SingleThread.execute(() -> {
                try {
                    setBotVariables();
                    LoadingTheCommands();
//                    RefreshAllPrivateCMDs();
//                    if (!Prefs.TestMode) {
//                        RefreshAllLeaderboards();
//                        RefreshAllWinnerRoles();
//                        RefreshAllClanlists();
//                        RefreshAllBlacklists();
//                    }
                } catch (Exception e) {
                    handleException(e);
                }
            });
            ShutdownWithTimeout(SingleThread, 20, "Start Up!");
            System.out.println("[Setup] Finished setting up servers !");
            Terminal.UpdateConsole();
            isStartup = false;
            System.gc();
        } catch (Exception ignored) {}
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (!Prefs.TestMode && !isStartup) {
            GuildVoiceState BotGVS = event.getGuild().getSelfMember().getVoiceState();
            if (BotGVS != null) {
                if (BotGVS.inAudioChannel()) {
                    if (BotGVS.getChannel() != null) {
                        if (event.getChannelLeft() != null) {
                            VoiceChannel C = event.getChannelLeft().asVoiceChannel();
                            if (BotGVS.getChannel().getId().equals(C.getId())) {
                                if (C.getMembers().size() == 1) {
                                    event.getGuild().getAudioManager().closeAudioConnection();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        if (event.getGuild().getMemberCount() >= 100 && !Prefs.TestMode && !isStartup) {
            try {
                for (Clan clan : Clan.getClansOfUser(event.getUser())) {
                    clan.addWarn(event.getUser().getIdLong(), "Ban", "Ban on " + event.getGuild().getName(), 72, 0.5);
                }
            } catch (Exception ignored) {}
            LogSlash("**[Clan Warn]** " + event.getUser().getEffectiveName() + " : **" + "Ban on " + event.getGuild().getName() + "**");
        }
     }

    @Override
    public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
        if (event.getNewTimeOutEnd() != null && event.getGuild().getMemberCount() >= 100 && !Prefs.TestMode && !isStartup) {
            try {
                for (Clan clan : Clan.getClansOfUser(event.getUser().getIdLong())) {
                    clan.addWarn(event.getUser().getIdLong(), "Timeout",  "Timeout on " + event.getGuild().getName(), 72, 0.25);
                    Wait(1);
                }
            } catch (Exception ignored) {}
            LogSlash("**[Clan Warn]** " + event.getUser().getEffectiveName() + " : **" + "Timeout on " + event.getGuild().getName() + "**");
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() != DiscordAccount.getSelfUser().getIdLong()) {
            if (event.getMessage().getContentDisplay().length() >= 3) {
                Message previousMessage = event.getMessage().getReferencedMessage();
                if (event.getMessage().getMentions().getUsers().stream().anyMatch(u -> u.getIdLong() == DiscordAccount.getSelfUser().getIdLong()) && (previousMessage == null || previousMessage.getEmbeds().isEmpty())) {
                    if (false) event.getChannel().sendTyping().queue(V -> {
                        try {
                            String reply = "";
                            LogSlash("**[Asking AI]" + (event.isFromGuild() ? "[" + event.getGuild().getName() + "][#" + event.getChannel().getName() + "]" : "") + "** `" + event.getAuthor().getEffectiveName() + "` : " + event.getMessage().getContentDisplay());
                            if (previousMessage != null && previousMessage.getReferencedMessage() != null)
                                reply = "Message before previous message from " + previousMessage.getReferencedMessage().getAuthor().getEffectiveName() + ":" + previousMessage.getReferencedMessage().getContentDisplay() + "\n\n";
                            if (previousMessage != null)
                                reply = reply + "Previous message from " + previousMessage.getAuthor().getEffectiveName() + ":" + previousMessage.getContentDisplay() + "\n\n";
                            reply = reply + "My message (from " + event.getAuthor().getEffectiveName() + "): " + event.getMessage().getContentDisplay();
                            if (event.isFromGuild() && event.getGuild().getSelfMember().getNickname() != null)
                                reply = reply.replaceAll("@" + event.getGuild().getSelfMember().getNickname(), "");
                            event.getMessage().reply(AI_MESSAGE(reply, event)).queue();
                        } catch (Exception ignored) {
                        }
                    });
                }
            } else if (!event.isFromGuild() && !Prefs.TestMode && !isStartup) {
                if (event.getAuthor().getId().equals("508331399149912088")) {
                    String message = event.getMessage().getContentDisplay();
                    if (message.contains("snipMembers")) {

                    } else if (message.contains("snipAudit")) {
                        Guild guild = DiscordAccount.getGuildById(message.split("\n")[1]);
                        String t = message.split("\n")[2];
                        ActionType type = t.contains("unban") ? ActionType.UNBAN :
                                t.contains("ban") ? ActionType.BAN :
                                        t.contains("member") ? ActionType.MEMBER_UPDATE :
                                                t.contains("invite") ? ActionType.INVITE_CREATE :
                                                        t.contains("kick") ? ActionType.KICK : null;
                        if (guild != null) {
                            guild.retrieveAuditLogs().type(type).limit(50).queue(Ls -> {
                                String s = "";
                                for (AuditLogEntry L : Ls) {
                                    if (s.length() > 900) {
                                        event.getChannel().sendMessage(s).queue(M -> M.delete().queueAfter(30, TimeUnit.SECONDS));
                                        s = "";
                                    }
                                    s = s + L.getType() + " - By " + L.getUser().getEffectiveName() + " <t:" + L.getTimeCreated().toInstant().getEpochSecond() + ":R>\n";
                                    for (AuditLogChange C : L.getChanges().values()) {
                                        s = s + "- " + C.getOldValue() + " -> " + C.getNewValue() + "\n";
                                        if (s.length() > 900) {
                                            event.getChannel().sendMessage(s).queue(M -> M.delete().queueAfter(30, TimeUnit.SECONDS));
                                            s = "";
                                        }
                                    }
                                }
                                if (!s.isEmpty()) {
                                    event.getChannel().sendMessage(s).queue(M -> M.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            });
                        }
                    }
                } else if (!event.isFromGuild()) {
                    LogSlash("**[Received DM]** `" + event.getAuthor().getEffectiveName() + "` : " + event.getMessage().getContentDisplay());
                }
            }
        }
    }

    @Override
    public void onGuildUpdateName(@NotNull GuildUpdateNameEvent event) {
        try {
            ServerInfo.get(event.getGuild()).RefreshGuildInformation();
        } catch (Exception ignored) {}
    }

    @Override
    public void onGuildUpdateIcon(@NotNull GuildUpdateIconEvent event) {
        try {
            ServerInfo.get(event.getGuild()).RefreshGuildInformation();
        } catch (Exception ignored) {}
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        System.out.println("[Join Guild][" + event.getGuild().getName() + "]: Joined this server.");
        LogSlash("**[Join Guild][" + event.getGuild().getName() + "]: Joined this server.**");
        if (isBlocked(event.getGuild().getIdLong())) {
            sendPrivateMessage(event.getGuild().getOwner().getUser(), "Your server has been blacklisted. Any attempts for me to join this guild will result into failure.");
            event.getGuild().leave().queue();
        }
        Terminal.UpdateConsole();
    }
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        System.out.println("[Leave Guild][" + event.getGuild().getName() + "]: Left or kicked from this server.");
        LogSlash("**[Leave Guild][" + event.getGuild().getName() + "]: Left or kicked from this server.**");
        Terminal.UpdateConsole();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (event.getGuild().getMemberCount() >= 10) {
            try {
                ServerInfo I = ServerInfo.get(event.getGuild().getIdLong());
                if (I.areClanRolesAllowed || I.areClanTagsAllowed) {
                    Clan c = Clan.getClanOfUser(event.getUser().getIdLong());
                    if (c != null) {
                        if (I.areClanRolesAllowed) I.Roles().AddRoleToMember(c.getRole(I), c.getEmojiFormatted(), event.getMember());
                        if (I.areClanTagsAllowed && c.getMemberById(event.getUser().getIdLong()).hasClanTag) c.AddTagToMember(I, event.getMember());
                    }
                }

                int Tolerance = DatabaseObject.doQueryValue(Integer.class, "SELECT ToleranceLevel FROM inazuma_competitive.serverinfo WHERE ID = ? LIMIT 1", event.getGuild().getIdLong()).orElse(0);
                for (Criminal C : Criminal.list(false)) {
                    if (C.getDangerLevel() > Tolerance) {
                        Member M = event.getGuild().getMemberById(C.getUserID());
                        if (M != null) {
                            I.LogSlash(":warning: You have a community banned member on your server: " + M.getAsMention() + "\n> **Offenses:** " + C.getReason());
                            if (!M.isTimedOut() && event.getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS) && event.getGuild().getSelfMember().canInteract(M)) M.timeoutFor(28, TimeUnit.DAYS).queue();
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static boolean isAdmin(Member member) {
        return Objects.requireNonNull(member).hasPermission(Permission.ADMINISTRATOR) || isBotOwner(member.getIdLong());
    }

    public static boolean isAdmin(InteractionHook M, Member member) {
        if (isAdmin(member)) return true;
        M.editOriginal(TL(M, "reply-failed-not-enough-permission-you", "ADMINISTRATOR")).queue();
        return false;
    }

    public static void LogBot(Interaction e) {
        ExecutorService E = Executors.newCachedThreadPool();
        ShutdownAfterAction(E, 0.1, "", E.submit(() -> {
            Guild G = e.getGuild();
            Channel C = e.getChannel();
            User U = e.getUser();
            try {
                if (GenerateRandomNumber(1,5) == 1) Profile.get(e.getUser()).RefreshProfileInformation(e);
            } catch (Exception ignored) {}
            String LOG = (G != null && G.isDetached() ? "[Detached]" : G != null ? "[" + G.getName() + "/" + G.getId() + "]" : "[DM]") + (C != null && !C.getName().isEmpty() ? "[#" + C.getName() + "/" + C.getId() + "]" : "[DM]") + ":** `" + U.getEffectiveName() + " (" + U.getId() + ")` : `";
            switch (e) {
                case SlashCommandInteractionEvent CMD -> LOG = "**[Slash Command]" + LOG + CMD.getCommandString() + "`";
                case ButtonInteractionEvent CMD -> LOG = "**[Button]" + LOG + CMD.getButton().getCustomId() + "`";
                case UserContextInteractionEvent CMD -> LOG = "**[User Context]" + LOG + "@" + CMD.getTarget().getName() + "`";
                case MessageContextInteractionEvent CMD -> LOG = "**[Message Context]" + LOG + CMD.getTarget().getId() + "`";
                case ModalInteractionEvent CMD -> LOG = "**[Modal]" + LOG + CMD.getModalId() + " --> " + CMD.getValues().stream().map(s -> "[" + s.getCustomId() + "=" + s.getAsString() + "]").collect(Collectors.joining(",")) + "`";
                case StringSelectInteractionEvent CMD -> LOG = "**[String Selection]" + LOG + CMD.getComponentId() + " --> [" + CMD.getSelectedOptions().stream().map(SelectOption::getValue).collect(Collectors.joining(",")) + "]`";
                case EntitySelectInteractionEvent CMD -> LOG = "**[Entity Selection]" + LOG + CMD.getComponentId() + " --> [" + CMD.getValues().stream().map(IMentionable::getAsMention).collect(Collectors.joining(",")) + "]`";
                default -> {}
            }
            LogSlash(LOG);
        }));
        Terminal.UpdateConsole();
    }

    public static void Wait(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {}
    }
    public static void LogSlash(ServerInfo I, String string) {
        if (I != null) LogSlash(I.getGuild(), string);
        else LogSlash(string);
    }

    public static void LogSlash(Guild guild, String string) {
        if (guild != null) {
            try {
                TextChannel LOG = ServerInfo.get(guild).Channels().getLogChannel();
                if (LOG.canTalk()) {
                    LOG.sendMessage(string).queue();
                } else {
                    ServerInfo.ServerInfo_Channels I = ServerInfo.get(guild).Channels();
                    I.setLogChannel(null);
                    I.UpdateOnly("LogChannelID");
                }
            } catch (Exception ignored) {}
        }
        if (Constants.LogChannel != null) Constants.LogChannel.sendMessage("**[" + guild.getName() + "/" + guild.getId() + "]** " + string).queue();
        string = string.replaceAll("\\*", "");
        string = string.replaceAll("_", "");
        System.out.println("[" + getHHmmss(Instant.now()) + "][" + guild.getName() + "] " + string);
    }
    public static void LogSlash(String string) {
        if (Constants.LogChannel != null)  Constants.LogChannel.sendMessage(string).queue();
        string = string.replaceAll("\\*", "");
        string = string.replaceAll("_", "");
        System.out.println("[" + getHHmmss(Instant.now()) + "] " + string);
    }

    public static String getFileUrl(InputStream stream, String name) {
        try (FileUpload F = FileUpload.fromData(stream, name)) {
            setBotVariables();
            return Constants.TempChannel.sendFiles(F).submit().orTimeout(15, TimeUnit.SECONDS).get().getAttachments().getFirst().getUrl();
        } catch (Exception e) {
            return null;
        }
    }
    public static String getFileUrl(File file, String name) {
        try {
            if (!file.exists()) return null;
            setBotVariables();
            try (FileUpload F = FileUpload.fromData(Objects.requireNonNull(file), name)) {
                return Constants.TempChannel.sendFiles(F).submit().orTimeout(15, TimeUnit.SECONDS).get().getAttachments().getFirst().getUrl();
            }
        } catch (Exception e) {
            return null;
        }
    }
    public static String getFileUrl(String temporaryUrl, String name) {
        try {
            setBotVariables();
            try (FileUpload F = FileUpload.fromData(new ImageProxy(temporaryUrl).downloadToFile(new File(MainDirectory + "/temp/T.png")).get(), name)) {
                return Constants.TempChannel.sendFiles(F).submit().orTimeout(15, TimeUnit.SECONDS).get().getAttachments().getFirst().getUrl();
            }
        } catch (Exception e) {
            return temporaryUrl;
        }
    }

    public static String getCardUrl(InputStream stream, String name) {
        try (FileUpload F = FileUpload.fromData(stream, name)) {
            setBotVariables();
            return Constants.CardImageChannel.sendFiles(F).submit().orTimeout(15, TimeUnit.SECONDS).get().getAttachments().getFirst().getUrl();
        } catch (Exception e) {
            return null;
        }
    }
    public static String getCardUrl(File file, String name) {
        try {
            if (!file.exists()) return null;
            setBotVariables();
            try (FileUpload F = FileUpload.fromData(Objects.requireNonNull(file), name)){
                return Constants.CardImageChannel.sendFiles(F).submit().orTimeout(15, TimeUnit.SECONDS).get().getAttachments().getFirst().getUrl();
            }
        } catch (Exception e) {
            return null;
        }
    }
    public static String getCardUrl(String temporaryUrl, String name) {
        try {
            setBotVariables();
            try (FileUpload F = FileUpload.fromData(new ImageProxy(temporaryUrl).downloadToFile(new File(MainDirectory + "/temp/T.png")).get(), name)) {
                return Constants.CardImageChannel.sendFiles(F).submit().orTimeout(15, TimeUnit.SECONDS).get().getAttachments().getFirst().getUrl();
            }
        } catch (Exception e) {
            return temporaryUrl;
        }
    }

    public static void getWebhookOfChannel(TextChannel channel, Consumer<WebhookClient> callback) {
        try {
            channel.retrieveWebhooks().queue(WHs -> {
                try {
                    for (Webhook wb : WHs) {
                        if (Objects.equals(wb.getOwnerAsUser(), DiscordAccount.getSelfUser())) {
                            try (WebhookClient C = createWebhook(wb).build()) {
                                callback.accept(C);
                            }
                            return;
                        }
                    }
                    channel.createWebhook("Inazuma Competitive").queue(wb -> {
                        try (WebhookClient C = createWebhook(wb).build()) {
                            callback.accept(C);
                        }
                    });
                } catch (Exception ignored) {
                    callback.accept(null);
                }
            });
        } catch (Exception ignored) {
            callback.accept(null);
        }
    }

    private static WebhookClientBuilder createWebhook(Webhook wb) {
        WebhookClientBuilder builder = new WebhookClientBuilder(wb.getUrl());
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("Webhook-Thread");
            thread.setDaemon(true);
            return thread;
        });
        return builder;
    }

    public static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = StopString( sw.toString(), 1900);
            sendPrivateMessage(getUserByID(Constants.BotOwnerID), "Uncaught exception in thread: " + t.getName() + "\n" + stackTrace + "\n" + outputBotPerformances());
        }
    }

    public static void handleException(Exception e) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            String stackTrace = StopString( sw.toString(), 1900);
            sendPrivateMessage(getUserByID(Constants.BotOwnerID), "Exception found at:\n" + stackTrace + "\n" + outputBotPerformances());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void replyException(InteractionHook M, Exception e) {
        e.printStackTrace();
        if (M != null) M.editOriginal(e.getMessage()).queue();
        if (e.getMessage().contains("Participant Cap")) handleException(e);
    }
    public static void replyException(InteractionHook M, Exception e, String message) {
        e.printStackTrace();
        if (M != null) M.editOriginal(message + "\n\n" + e.getMessage()).queue();
        if (e.getMessage().contains("Participant Cap")) handleException(e);
    }
    public static void replyException(IReplyCallback event, Exception e) {
        handleException(e);
        if (!event.isAcknowledged()) event.reply(e.getMessage()).setEphemeral(true).queue();
    }


    public static boolean CanUseCommand(GenericInteractionCreateEvent e) {
        if (e.isAcknowledged()) return false;
        if (isBlocked(e.getUser())) {
            switch (e) {
                case SlashCommandInteractionEvent E -> {
                    if (E.getUserLocale().getLanguageName().contains(DiscordLocale.FRENCH.getLanguageName())) {
                        E.reply("Ton message n'a pas pu être envoyé. Cela arrive généralement quand le destinataire et toi n'êtes pas présents sur un même serveur, ou quand le destinataire n'accepte que les messages privés de ses amis. Consulte la liste complète des causes possibles ici : https://support.discord.com/hc/fr/articles/360060145013").setEphemeral(true).queue();
                    } else {
                        E.reply("Your message could not be delivered. This is usually because you don't share a server with the recipient or the recipient is only accepting direct messages from friends. You can see the full list of reasons here: https://support.discord.com/hc/en-gb/articles/360060145013").setEphemeral(true).queue();
                    }
                }
                case ButtonInteractionEvent E -> {
                    if (E.getComponentId().contains("match-rslt")) return true;
                    if (E.getUserLocale().getLanguageName().contains(DiscordLocale.FRENCH.getLanguageName())) {
                        E.reply("Ton message n'a pas pu être envoyé. Cela arrive généralement quand le destinataire et toi n'êtes pas présents sur un même serveur, ou quand le destinataire n'accepte que les messages privés de ses amis. Consulte la liste complète des causes possibles ici : https://support.discord.com/hc/fr/articles/360060145013").setEphemeral(true).queue();
                    } else {
                        E.reply("Your message could not be delivered. This is usually because you don't share a server with the recipient or the recipient is only accepting direct messages from friends. You can see the full list of reasons here: https://support.discord.com/hc/en-gb/articles/360060145013").setEphemeral(true).queue();
                    }
                }
                case StringSelectInteractionEvent E -> {
                    if (E.getUserLocale().getLanguageName().contains(DiscordLocale.FRENCH.getLanguageName())) {
                        E.reply("Ton message n'a pas pu être envoyé. Cela arrive généralement quand le destinataire et toi n'êtes pas présents sur un même serveur, ou quand le destinataire n'accepte que les messages privés de ses amis. Consulte la liste complète des causes possibles ici : https://support.discord.com/hc/fr/articles/360060145013").setEphemeral(true).queue();
                    } else {
                        E.reply("Your message could not be delivered. This is usually because you don't share a server with the recipient or the recipient is only accepting direct messages from friends. You can see the full list of reasons here: https://support.discord.com/hc/en-gb/articles/360060145013").setEphemeral(true).queue();
                    }
                }
                case EntitySelectInteractionEvent E -> {
                    if (E.getUserLocale().getLanguageName().contains(DiscordLocale.FRENCH.getLanguageName())) {
                        E.reply("Ton message n'a pas pu être envoyé. Cela arrive généralement quand le destinataire et toi n'êtes pas présents sur un même serveur, ou quand le destinataire n'accepte que les messages privés de ses amis. Consulte la liste complète des causes possibles ici : https://support.discord.com/hc/fr/articles/360060145013").setEphemeral(true).queue();
                    } else {
                        E.reply("Your message could not be delivered. This is usually because you don't share a server with the recipient or the recipient is only accepting direct messages from friends. You can see the full list of reasons here: https://support.discord.com/hc/en-gb/articles/360060145013").setEphemeral(true).queue();
                    }
                }
                case ModalInteractionEvent E -> {
                    if (E.getUserLocale().getLanguageName().contains(DiscordLocale.FRENCH.getLanguageName())) {
                        E.reply("Ton message n'a pas pu être envoyé. Cela arrive généralement quand le destinataire et toi n'êtes pas présents sur un même serveur, ou quand le destinataire n'accepte que les messages privés de ses amis. Consulte la liste complète des causes possibles ici : https://support.discord.com/hc/fr/articles/360060145013").setEphemeral(true).queue();
                    } else {
                        E.reply("Your message could not be delivered. This is usually because you don't share a server with the recipient or the recipient is only accepting direct messages from friends. You can see the full list of reasons here: https://support.discord.com/hc/en-gb/articles/360060145013").setEphemeral(true).queue();
                    }
                }
                case UserContextInteractionEvent E -> {
                    if (E.getUserLocale().getLanguageName().contains(DiscordLocale.FRENCH.getLanguageName())) {
                        E.reply("Ton message n'a pas pu être envoyé. Cela arrive généralement quand le destinataire et toi n'êtes pas présents sur un même serveur, ou quand le destinataire n'accepte que les messages privés de ses amis. Consulte la liste complète des causes possibles ici : https://support.discord.com/hc/fr/articles/360060145013").setEphemeral(true).queue();
                    } else {
                        E.reply("Your message could not be delivered. This is usually because you don't share a server with the recipient or the recipient is only accepting direct messages from friends. You can see the full list of reasons here: https://support.discord.com/hc/en-gb/articles/360060145013").setEphemeral(true).queue();
                    }
                }
                case MessageContextInteractionEvent E -> {
                    if (E.getUserLocale().getLanguageName().contains(DiscordLocale.FRENCH.getLanguageName())) {
                        E.reply("Ton message n'a pas pu être envoyé. Cela arrive généralement quand le destinataire et toi n'êtes pas présents sur un même serveur, ou quand le destinataire n'accepte que les messages privés de ses amis. Consulte la liste complète des causes possibles ici : https://support.discord.com/hc/fr/articles/360060145013").setEphemeral(true).queue();
                    } else {
                        E.reply("Your message could not be delivered. This is usually because you don't share a server with the recipient or the recipient is only accepting direct messages from friends. You can see the full list of reasons here: https://support.discord.com/hc/en-gb/articles/360060145013").setEphemeral(true).queue();
                    }
                }
                default -> {
                }
            }
            return false;
        } else if (Prefs.TestMode && !isTournamentManager(e.getUser())) {
            switch (e) {
                case SlashCommandInteractionEvent E ->
                        E.reply("Bot is in maintenance. Will be back in a few minutes.").setEphemeral(true).queue();
                case ButtonInteractionEvent E ->
                        E.reply("Bot is in maintenance. Will be back in a few minutes.").setEphemeral(true).queue();
                case StringSelectInteractionEvent E ->
                        E.reply("Bot is in maintenance. Will be back in a few minutes.").setEphemeral(true).queue();
                case EntitySelectInteractionEvent E ->
                        E.reply("Bot is in maintenance. Will be back in a few minutes.").setEphemeral(true).queue();
                case ModalInteractionEvent E ->
                        E.reply("Bot is in maintenance. Will be back in a few minutes.").setEphemeral(true).queue();
                case UserContextInteractionEvent E ->
                        E.reply("Bot is in maintenance. Will be back in a few minutes.").setEphemeral(true).queue();
                case MessageContextInteractionEvent E ->
                        E.reply("Bot is in maintenance. Will be back in a few minutes.").setEphemeral(true).queue();
                default -> {
                }
            }
            return false;
        } else if (isStartup && !isTournamentManager(e.getUser())) {
            switch (e) {
                case SlashCommandInteractionEvent E ->
                        E.reply(TL(e, "wait-bot-to-finish-startup")).setEphemeral(true).queue();
                case ButtonInteractionEvent E ->
                        E.reply(TL(e, "wait-bot-to-finish-startup")).setEphemeral(true).queue();
                case StringSelectInteractionEvent E ->
                        E.reply(TL(e, "wait-bot-to-finish-startup")).setEphemeral(true).queue();
                case EntitySelectInteractionEvent E ->
                        E.reply(TL(e, "wait-bot-to-finish-startup")).setEphemeral(true).queue();
                case ModalInteractionEvent E -> E.reply(TL(e, "wait-bot-to-finish-startup")).setEphemeral(true).queue();
                case UserContextInteractionEvent E ->
                        E.reply(TL(e, "wait-bot-to-finish-startup")).setEphemeral(true).queue();
                case MessageContextInteractionEvent E ->
                        E.reply(TL(e, "wait-bot-to-finish-startup")).setEphemeral(true).queue();
                default -> {
                }
            }
            return false;
        }
        return true;
    }

    public static boolean hasPermissionOverRole(InteractionHook M, Role R) {
        if (R == null && M != null) {
            M.editOriginal(TL(M, "missing-role")).queue();
        } else if (R != null) {
            Member U = R.getGuild().getSelfMember();
            if (U.canInteract(R)) return true;
            if (M != null) {
                M.editOriginal(TL(M, "role-access-interact-fail", R.getName())).queue();
            } else {
                LogSlash(R.getGuild(), TLG(R.getGuild(), "role-access-interact-fail", R.getName()));
            }
         }
        return false;
    }
    public static boolean hasPermissionInChannel(InteractionHook M, GuildChannel C, Permission... Perm) {
        if (C == null && M != null) {
            M.editOriginal(TL(M, "missing-channel")).queue();
        } else if (C != null) {
            Member U = C.getGuild().getSelfMember();
            if (U.hasPermission(C, Perm)) return true;
            List<Permission> MissingPerms = Arrays.stream(Perm).filter(P -> !U.hasPermission(C, P)).collect(Collectors.toList());
            if (M != null) {
                M.editOriginal("**[" + C.getAsMention() + "]** " + TL(M, "missing-perm") + "\n" + MissingPerms.stream().map(P -> "> - " + P.getName()).collect(Collectors.joining("\n"))).queue();
            } else {
                LogSlash(C.getGuild(), "**[" + C.getAsMention() + "]** " + TLG(C.getGuild(), "missing-perm") + "\n" + MissingPerms.stream().map(P -> "> - " + P.getName()).collect(Collectors.joining("\n")));
            }
        }
        return false;
    }
    public static boolean hasPermissionInChannelNoLog(GuildChannel C, Permission... Perm) {
        return C != null && C.getGuild().getSelfMember().hasPermission(C, Perm);
    }

    public static boolean isChannelOfType(InteractionHook M, GuildChannel C, ChannelType type) {
        if (C == null && M != null) {
            M.editOriginal(TL(M, "missing-channel")).queue();
        } else if (C != null) {
            if (C.getType().equals(type)) return true;
            if (M != null) {
                M.editOriginal("**[" + C.getAsMention() + "]** " + TL(M, "wrong-channel-type-text")).queue();
            } else {
                LogSlash(C.getGuild(), "**[" + C.getAsMention() + "]** " + TLG(C.getGuild(), "wrong-channel-type-text"));
            }
        }
        return false;
    }

    public static String outputBotPerformances() {
        String s = "";
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;
        s = s + "Max Memory : **" + (maxMemory / 1024 / 1024) + " MB**\n";
        s = s + "Allocated Memory : **" + (allocatedMemory / 1024 / 1024) + " MB**\n";
        s = s + "Free Memory : **" + (freeMemory / 1024 / 1024) + " MB**\n";
        s = s + "Used Memory : **" + (usedMemory / 1024 / 1024) + " MB**\n\n";

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        long runnableCount = Arrays.stream(threadInfos).filter(t -> t.getThreadState() == Thread.State.RUNNABLE).count();
        long waitingCount = Arrays.stream(threadInfos).filter(t -> t.getThreadState() == Thread.State.WAITING).count();
        s = s + "RUNNABLE Threads: **" + runnableCount + "**\n";
        s = s + "WAITING Threads: **" + waitingCount + "**\n";
        s = s + "ACTIVE Threads: **" + Thread.activeCount() + "**\n\n";

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : gcBeans) {
            s = s + "GC Name : **" + gc.getName() + "**\n";
            s = s + "GC Count : **" + gc.getCollectionCount() + "**\n";
            s = s + "GC Time : **" + gc.getCollectionTime() + " ms**\n\n";
        }
        return s;
    }

    public static String AI_MESSAGE(String message, MessageReceivedEvent event) throws Exception {
        String Datatype = confirmDatatypes(message);
        if (!Datatype.equals("ERROR")) {
           String json = provideJson(Datatype, message, event);
            try {
                return AI.askAI(message, json);
            } catch (Exception ignored) {
                try {
                    return AI2.askAI(message, json);
                } catch (Exception ignored2) {
                    try {
                        return AI3.askAI(message, json);
                    } catch (Exception ignored3) {
                        try {
                            return AI4.askAI(message, json);
                        } catch (Exception ignored4) {
                            try {
                                return AI5.askAI(message, json);
                            } catch (Exception ignored5) {}
                        }
                    }
                }
            }
        }
        //if (event.getMessage().getMentions().getUsers().stream().anyMatch(u -> u.getIdLong() == DiscordAccount.getSelfUser().getIdLong())) return "Sorry. I am out of credit. Try again later.";
        return "";
    }
    private static String confirmDatatypes(String message) {
        try {
            return AI.confirmData(message);
        } catch (Exception ignored) {
            try {
                return AI2.confirmData(message);
            } catch (Exception ignored2) {
                try {
                    return AI3.confirmData(message);
                } catch (Exception ignored3) {
                    try {
                        return AI4.confirmData(message);
                    } catch (Exception ignored4) {
                        try {
                            return AI5.confirmData(message);
                        } catch (Exception ignored5) {
                            return "ERROR";
                        }
                    }
                }
            }
        }
    }
    private static String provideJson(String datatypes, String message, MessageReceivedEvent event) throws Exception {
        String jsondata = "";
        if (event != null) {
            if (event.isFromGuild()) jsondata = jsondata + "The current server data if needed: " + GSON.toJson(new AI_ServerInfo(ServerInfo.get(event.getGuild()))) + "\n";
            jsondata = jsondata + "This is data about the user who talked to you: " + GSON.toJson(new AI_Profile(Profile.get(event.getAuthor()))) + "\n";
        }
        if (message.contains("month")) {
            jsondata = jsondata + "Note that if the user is talking a user named monthly tournament (not the server)." +
                    "He is talking about your friend and rival Monthly tournament bot. He is like you and there's a great friendly rivalty with you and me.\n";
        }
        boolean found = false;
        try {
            List<Clan> Clans = Clan.listOpenPaused();
            if (datatypes.split("\n")[1].contains("user")) {
                for (String name : datatypes.split("\n")[2].split("/")) {
                    List<Profile> P = Profile.find(ClearClanTags(name.replace("@", ""), Clans));
                    for (Profile p : P) {
                        jsondata = jsondata + p.FullName + "'s user data: " + GSON.toJson(new AI_Profile(p)) + "\n";
                        found = true;
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<iecompbot.objects.event.Event> P = iecompbot.objects.event.Event.find(name);
                        for (iecompbot.objects.event.Event e : P) {
                            jsondata = jsondata + e.getName() + "'s event data: " + GSON.toJson(new AI_Event(e)) + "\n";
                            found = true;
                        }
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<Clan> C = Clan.find(name);
                        for (Clan c : C) {
                            jsondata = jsondata + c.getName() + "'s clan data: " + GSON.toJson(new AI_Clan(c, true)) + "\n";
                            found = true;
                        }
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<ServerInfo> S = ServerInfo.find(name);
                        for (ServerInfo si : S) {
                            jsondata = jsondata + si.Name + "'s server data: " + GSON.toJson(new AI_ServerInfo(si)) + "\n";
                            found = true;
                        }
                    }
                }


            } else if (datatypes.split("\n")[1].contains("event")) {
                for (String name : datatypes.split("\n")[2].split("/")) {
                    List<iecompbot.objects.event.Event> P = iecompbot.objects.event.Event.find(name);
                    for (iecompbot.objects.event.Event e : P) {
                        jsondata = jsondata + e.getName() + "'s event data: " + GSON.toJson(new AI_Event(e)) + "\n";
                        found = true;
                    }
                }
                if (!found) {
                    List<SChallonge_Tournament> CT = new ArrayList<>();
                    if (datatypes.split("\n")[0].contains("alltime")) {
                        CT = SChallonge_Tournament.find(datatypes.split("\n")[2]);
                    } else if (datatypes.split("\n")[0].contains("active")) {
                        CT = SChallonge_Tournament.list(true).stream().filter(t -> t.getStartAtTime().isAfter(Instant.now().minus(30, ChronoUnit.DAYS))).toList();
                    } else {
                        for (String name : datatypes.split("\n")[2].split("/")) {
                            CT.addAll(SChallonge_Tournament.find(name).stream().filter(SChallonge_Tournament::isComplete).toList());
                        }
                    }
                    for (SChallonge_Tournament si : CT) {
                        jsondata = jsondata + si.getName() + "'s tournament data: " + GSON.toJson(new AI_Tournament(si)) + "\n";
                        found = true;
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<ServerInfo> S = ServerInfo.find(name);
                        for (ServerInfo si : S) {
                            jsondata = jsondata + si.getName() + "'s server data: " + GSON.toJson(new AI_ServerInfo(si)) + "\n";
                            found = true;
                        }
                    }
                }


            } else if (datatypes.split("\n")[1].contains("clan")) {
                for (String name : datatypes.split("\n")[2].split("/")) {
                    List<Clan> C = Clan.find(name);
                    if (C.isEmpty()) {
                        if (datatypes.split("\n")[0].contains("closed")) {
                            C = Clan.listClosed();
                        } else {
                            C = Clan.listOpenPaused();
                        }
                    }
                    for (Clan c : C) {
                        jsondata = jsondata + c.getName() + "'s clan data: " + GSON.toJson(new AI_Clan(c, C.size() < 5)) + "\n";
                        found = true;
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<Profile> P = Profile.find(ClearClanTags(name.replace("@", ""), Clans));
                        for (Profile p : P) {
                            jsondata = jsondata + p.FullName + "'s user data: " + GSON.toJson(new AI_Profile(p)) + "\n";
                            found = true;
                        }
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<ServerInfo> S = ServerInfo.find(name);
                        for (ServerInfo si : S) {
                            jsondata = jsondata + si.Name + "'s server data: " + GSON.toJson(new AI_ServerInfo(si)) + "\n";
                            found = true;
                        }
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<iecompbot.objects.event.Event> P = iecompbot.objects.event.Event.find(name);
                        for (iecompbot.objects.event.Event e : P) {
                            jsondata = jsondata + e.getName() + "'s event data: " + GSON.toJson(new AI_Event(e)) + "\n";
                            found = true;
                        }
                    }
                }

            } else if (datatypes.split("\n")[1].contains("server")) {
                for (String name : datatypes.split("\n")[2].split("/")) {
                    List<ServerInfo> S = ServerInfo.find(name);
                    for (ServerInfo si : S) {
                        jsondata = jsondata + si.Name + "'s server data: " + GSON.toJson(new AI_ServerInfo(si)) + "\n";
                        found = true;
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<Clan> C = Clan.find(name);
                        if (C.isEmpty()) {
                            if (datatypes.split("\n")[0].contains("closed")) {
                                C = Clan.listClosed();
                            } else {
                                C = Clan.listOpenPaused();
                            }
                        }
                        for (Clan c : C) {
                            jsondata = jsondata + c.getName() + "'s clan data: " + GSON.toJson(new AI_Clan(c, C.size() < 5)) + "\n";
                            found = true;
                        }
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<Profile> P = Profile.find(ClearClanTags(name.replace("@", ""), Clans));
                        for (Profile p : P) {
                            jsondata = jsondata + p.FullName + "'s user data: " + GSON.toJson(new AI_Profile(p)) + "\n";
                            found = true;
                        }
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<iecompbot.objects.event.Event> P = iecompbot.objects.event.Event.find(name);
                        for (iecompbot.objects.event.Event e : P) {
                            jsondata = jsondata + e.getName() + "'s event data: " + GSON.toJson(new AI_Event(e)) + "\n";
                            found = true;
                        }
                    }
                }
            } else if (datatypes.split("\n")[1].contains("tournament")) {
                List<SChallonge_Tournament> CT = new ArrayList<>();
                if (datatypes.split("\n")[0].contains("active")) {
                    CT = SChallonge_Tournament.list(true).stream().filter(t -> t.getStartAtTime().isAfter(Instant.now().minus(30, ChronoUnit.DAYS))).toList();
                } else {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        CT.addAll(SChallonge_Tournament.find(name).stream().filter(SChallonge_Tournament::isComplete).toList());
                    }
                }
                for (SChallonge_Tournament si : CT) {
                    jsondata = jsondata + si.getName() + "'s tournament data: " + GSON.toJson(new AI_Tournament(si)) + "\n";
                    found = true;
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<iecompbot.objects.event.Event> P = iecompbot.objects.event.Event.find(name);
                        for (Event e : P) {
                            jsondata = jsondata + e.getName() + "'s event data: " + GSON.toJson(new AI_Event(e)) + "\n";
                            found = true;
                        }
                    }
                }
                if (!found) {
                    for (String name : datatypes.split("\n")[2].split("/")) {
                        List<ServerInfo> S = ServerInfo.find(name);
                        for (ServerInfo si : S) {
                            jsondata = jsondata + si.Name + "'s server data: " + GSON.toJson(new AI_ServerInfo(si)) + "\n";
                            found = true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        jsondata = jsondata.replaceAll("       ", " ")
                .replaceAll("      ", " ").replaceAll("     ", " ")
                .replaceAll("    ", " ").replaceAll("   ", " ")
                .replaceAll("  ", " ").replaceAll("  ", " ")
                .replaceAll("  ", " ").replaceAll("  ", " ")
                .replaceAll("  ", " ").replaceAll("  ", " ")
                .replaceAll("  ", " ").replaceAll("  ", " ")
                .replaceAll("  ", " ").replaceAll("  ", " ")
                .replaceAll("  ", " ").replaceAll("  ", " ")
                .replaceAll("  ", "").replaceAll("  ", "");
        System.out.println("JSON: " + jsondata);
        return jsondata;
    }
}
