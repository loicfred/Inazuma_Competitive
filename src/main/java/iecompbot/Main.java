package iecompbot;

import at.stefangeyer.challonge.Challonge;
import at.stefangeyer.challonge.model.Credentials;
import at.stefangeyer.challonge.rest.retrofit.RetrofitRestClient;
import at.stefangeyer.challonge.serializer.gson.GsonSerializer;
import iecompbot.ai.Aimlapi;
import iecompbot.interaction.Automation;
import iecompbot.interaction.listeners.*;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import static iecompbot.interaction.Automation.Wait;
import static iecompbot.interaction.Automation.outputBotPerformances;
import static iecompbot.interaction.GuildReady.*;
import static my.utilities.util.Utilities.StopString;
import static my.utilities.util.Utilities.takeOnlyDigits;

@EnableCaching
@EnableScheduling
@SpringBootApplication(scanBasePackages = "iecompbot.springboot")
public class Main {
    public static boolean VPSMODE = !new File("D:/bot-database/").exists(); // Will launch the bot on ROOT without UI

    public static Preferences Prefs = Preferences.Load();

    public static String MainDirectory = "D:/bot-database/ie-comp-bot"; // Default data directory on TERMINAL
    public static String ROOT = "./bot-database/ie-comp-bot"; // Default data directory on VPS
    public static String DefaultURL = "http://vps-359db294.vps.ovh.net:8080";

    public static String TempDirectory = MainDirectory + "/temp";
    public static File ServerDirectory = new File(MainDirectory + "/server/");
    public static File InterclanDirectory = new File(MainDirectory + "/storage/interclans/");
    public static File UserDirectory = new File(MainDirectory + "/user/");
    public static File ClansDirectory = new File(MainDirectory + "/clans/");

    public static Font ConsoleFont;
    public static Font newRodinPro;
    public static Font ReggaeStdB;
    public static Font Dotum;
    public static Font FlagFont;

    public static Challonge ChallongeAccount;
    public static JDA DiscordAccount;

    public static Aimlapi AI = new Aimlapi("2c35796f7d8d4764bfeb2febfb9f9e7e");
    public static Aimlapi AI2 = new Aimlapi("dbcd96355b804558af6834ab33f8c9c8");
    public static Aimlapi AI3 = new Aimlapi("f335edfafdaf4c608ddc2b54c73a419f");
    public static Aimlapi AI4 = new Aimlapi("034259ddc8ba4089a85d923d5563714d");
    public static Aimlapi AI5 = new Aimlapi("8a80cca592f848f9b727a53e1dcbbeba");

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
        Thread.setDefaultUncaughtExceptionHandler(new Automation.MyUncaughtExceptionHandler());
        RegisterFiles();
        RegisterFonts();
        RegisterAccounts();
        RegisterConsoleCommands();
     }

    public static void RegisterFiles() {
        try {
            if (VPSMODE) MainDirectory = ROOT;
            System.out.println(MainDirectory);
            TempDirectory = MainDirectory + "/temp";
            ServerDirectory = new File(MainDirectory + "/server/");
            InterclanDirectory = new File(MainDirectory + "/storage/interclans/");
            UserDirectory = new File(MainDirectory + "/user/");
            ClansDirectory = new File(MainDirectory + "/clans/");
            File theDir = new File(MainDirectory + "/server");
            if (!theDir.exists()){
                System.out.println("[File] Creating all directories...");
                theDir.mkdirs();
            }
            File theDirUser = new File(MainDirectory + "/user");
            if (!theDirUser.exists()){
                theDirUser.mkdirs();
            }
            File theDirVS = new File(MainDirectory + "/VS");
            if (!theDirVS.exists()){
                theDirVS.mkdirs();
            }
            File theDirStorage = new File(MainDirectory + "/storage/tournament");
            if (!theDirStorage.exists()){
                theDirStorage.mkdirs();
            }
            File theDirAwards = new File(MainDirectory + "/storage/badges");
            if (!theDirAwards.exists()){
                theDirAwards.mkdirs();
            }
            File theDirC = new File(MainDirectory + "/clans");
            if (!theDirC.exists()){
                theDirC.mkdirs();
            }
            ClearTempFiles();
            //  new Thread(ie.Constants::RefreshDocs).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void RegisterFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try (InputStream is = Main.class.getResourceAsStream("/static/font/Inconsolata-Regular.ttf")) {
            Font inconsolata = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            ge.registerFont(inconsolata);
            ConsoleFont = inconsolata.deriveFont(Font.PLAIN, 12);
        } catch (Exception ignored){System.out.println("Failed loading Inconsolata-Regular.ttf...");}

        try (InputStream is = Main.class.getResourceAsStream("/static/font/IEBold.otf")) {
            Font newRodin = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            ge.registerFont(newRodin);
            newRodinPro = newRodin.deriveFont(Font.PLAIN, 16);
        } catch (Exception ignored){System.out.println("Failed loading FlagsColor-DOGLd.ttf...");}

        try (InputStream is = Main.class.getResourceAsStream("/static/font/IEStrike.otf")) {
            Font reggaeStd = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            ge.registerFont(reggaeStd);
            ReggaeStdB = reggaeStd.deriveFont(Font.PLAIN, 16);
        } catch (Exception ignored){System.out.println("Failed loading FOT-ReggaeStd-B.ttf...");}

        try (InputStream is = Main.class.getResourceAsStream("/static/font/UnDotum.ttf")) {
            Font dotum = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            ge.registerFont(dotum);
            Dotum = dotum.deriveFont(Font.PLAIN, 16);
        } catch (Exception ignored){System.out.println("Failed loading UnDotum.ttf...");}

        try (InputStream is = Main.class.getResourceAsStream("/static/font/FlagsColor-DOGLd.ttf")) {
            Font flagsFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            ge.registerFont(flagsFont);
            FlagFont = flagsFont;
        } catch (Exception ignored){System.out.println("Failed loading FlagsColor-DOGLd.ttf...");}
    }
    public static void RegisterAccounts() {
        Credentials credentials = new Credentials("Inazuma_Competitive", "d5JR3RiLAEoSxlYnp08SmSVnEgPKvSfkBMBzpmr6");
        ChallongeAccount = new Challonge(credentials, new GsonSerializer(), new RetrofitRestClient());
        if (true) DiscordAccount = getBuilder().build(); else new Terminal();
        System.out.println("Launching " + DiscordAccount.getSelfUser().getEffectiveName() + "...");
    }
    public static void RegisterConsoleCommands() {
        new Thread(() -> {
            System.out.println("Starting console commands...");
            Scanner scan = new Scanner(System.in);
            while (true) {
                try {
                    String input = scan.nextLine();
                    if (input.equalsIgnoreCase("exit")) {
                        scan.close();
                        System.gc();
                        System.exit(0);
                    } else if (input.startsWith("info")) {
                        if (input.contains("threads")) {
                            for (ThreadInfo info : ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)) {
                                String stacktrace = Arrays.stream(info.getStackTrace()).map(StackTraceElement::toString).filter(string -> string.contains("iecompbot") || string.contains("dv8tion")).collect(Collectors.joining("\n"));
                                String s = "-----------------------------------------------\nID: " + info.getThreadId() + " - Prio: " + info.getPriority() + " - " + info.getThreadState() + " - " + info.getThreadName() + "\n";
                                s = s + StopString(stacktrace,1600);
                                System.out.println(s);
                                Wait(500);
                            }
                            DiscordAccount.cancelRequests();
                        }
                        else if (input.contains("ram")) {
                            System.out.println(outputBotPerformances());
                        }
                    } else if (input.startsWith("refresh")) {
                        if (input.contains("tournament") || input.contains("server")) {
                            for (SChallonge_Tournament CT : SChallonge_Tournament.getActiveChallonges(true)) {
                                CT.RefreshInscriptionMessage();
                                CT.ResyncChallonge();
                                CT.Update();
                            }
                        }
                        else if (input.contains("server") || input.contains("guild")) {
                            if (input.contains("only")) {
                                ServerInfo I = ServerInfo.get(takeOnlyDigits(input));
                                I.RefreshGuildInformation();
                                RefreshGuildClanRole(I, Clan.listOpenPaused());
                            } else {
                                for (Guild G : DiscordAccount.getGuilds()) {
                                    ServerInfo.get(G).RefreshGuildInformation();
                                }
                                RefreshAllClanMembers(Clan.listOpenPaused());
                            }
                        }
                        else if (input.contains("profile") || input.contains("user")) {
                            Profile I = Profile.get(takeOnlyDigits(input));
                            I.RefreshProfileInformation(null);
                        }
                        else if (input.contains("command") || input.contains("cmd")) {
                            LoadingTheCommands();
                        }
                    }
                    else if (input.contains("snipBan")) {
                        Guild guild = DiscordAccount.getGuildById(takeOnlyDigits(input));
                        ActionType type = ActionType.BAN;
                        if (guild != null) {
                            guild.retrieveAuditLogs().type(type).limit(50).queue(Ls -> {
                                for (AuditLogEntry L : Ls) {
                                    System.out.println(L.getType() + " - By " + L.getUser().getEffectiveName() + " <t:" + L.getTimeCreated().toInstant().getEpochSecond() + ":R>");
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static JDABuilder getBuilder() {
        System.out.println("Starting bot...");
        JDABuilder JB = JDABuilder.createDefault(Prefs.TestMode ? Prefs.TestToken : Prefs.Token).
                setStatus(OnlineStatus.ONLINE).
                setChunkingFilter(ChunkingFilter.ALL).
                setMemberCachePolicy(MemberCachePolicy.ALL).
                enableIntents(GatewayIntent.GUILD_MEMBERS).
                enableIntents(GatewayIntent.GUILD_MESSAGES).
                enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS).
                enableIntents(GatewayIntent.GUILD_MODERATION).
                enableIntents(GatewayIntent.SCHEDULED_EVENTS).
                enableIntents(GatewayIntent.GUILD_EXPRESSIONS).
                addEventListeners(
                        new ProfileFeatures()
                        , new ShopFeatures()
                        , new ProfileFeatures()
                        , new MatchFeatures()
                        , new MDFFeatures()
                        , new AdminFeatures()
                        , new UtilityFeatures()
                        , new ChallongeFeatures()
                        , new ClanFeatures(),
                        new Automation()
                );
        if (Objects.equals(Prefs.Activity, "Playing")) JB.setActivity(Activity.playing(Prefs.ActivityStatus));
        else if (Objects.equals(Prefs.Activity, "Competing")) JB.setActivity(Activity.competing(Prefs.ActivityStatus));
        else if (Objects.equals(Prefs.Activity, "Listening")) JB.setActivity(Activity.listening(Prefs.ActivityStatus));
        else if (Objects.equals(Prefs.Activity, "Watching")) JB.setActivity(Activity.watching(Prefs.ActivityStatus));
        else JB.setActivity(Activity.customStatus(Prefs.ActivityStatus));
        return JB;
    }

    public static void ClearTempFiles() {
        File temp = new File(MainDirectory + "/temp/");
        if (temp.listFiles() != null) {
            for (File f : Objects.requireNonNull(temp.listFiles())) {
                f.delete();
            }
        }
    }
}
