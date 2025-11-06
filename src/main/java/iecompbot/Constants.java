package iecompbot;

import iecompbot.objects.match.Game;
import iecompbot.objects.match.League;
import iecompbot.objects.server.ServerInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final DecimalFormat POWERDECIMAL = new DecimalFormat("0.000");
    public static final DecimalFormat PRICEDECIMAL = new DecimalFormat("#,###");

    public static final String BotOwnerID = "508331399149912088";
    public static final String DiscordColorcode = "#2b2d31";

    public static OptionData awards;
    public static OptionData clans;
    public static OptionData interclans;
    public static OptionData opposingclan;
    public static OptionData eventnames;
    public static OptionData teamname;
    public static OptionData groupname;
    public static OptionData matches;

    public static final String ChallongeLogoURL = "https://media.discordapp.net/attachments/1246502760048758815/1291816129316257964/image.png";

    public static List<Game> allGame = new ArrayList<>();
    public static List<League> GlobalLeagues = new ArrayList<>();
    public static List<League.League_Tier> GlobalLeagueTiers = new ArrayList<>();

    public static ServerInfo.ServerInfo_Ranking GlobalRanking;

    public static Guild BotStaffGuild = null;
    public static TextChannel TempChannel = null;
    public static TextChannel LogChannel = null;
    public static TextChannel ClanEmblemChannel = null;
    public static TextChannel CardImageChannel = null;
    public static TextChannel ClanRequestChannel = null;
    public static VoiceChannel Battery = null;

    public static String[] SponsoredServers = {
            "1074612072010219580",
            "1072991720268111892",
            "1138604999467860129",
            "1200551639170424972",
            "1055585286320554055",
            "871133534184681523",
            "415203100287172618",
            "462219202087092225",
            "995002678830694480",
            "868031514984738847",
            "724029744634658827",
            "1265925623776280576",
            "1347960014840795137",
            "1158515204527771790",
            "916130742151548999",
            "1278149997757730948",
            "1226876193223999488"};
    public static long ImageGenerationTimerEpochSecond = 0;
    public static long RefreshCooldownOfLeaderboard = 0;
    public static long RefreshCooldownOfClanRoleRefresh = 0;
    public static long RefreshCooldownOfRankRoleRefresh = 0;
}
