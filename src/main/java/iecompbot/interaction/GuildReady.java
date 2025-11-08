package iecompbot.interaction;

import ie.enums.*;
import iecompbot.Constants;
import iecompbot.Main;
import iecompbot.Terminal;
import iecompbot.objects.match.Game;
import iecompbot.objects.match.League;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.clan.interclan.Interclan;
import iecompbot.objects.match.MatchLog;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.Profile_Total;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.profile_game.BasePG;
import iecompbot.objects.profile.profile_game.Profile_Game;
import iecompbot.objects.profile.profile_game.Profile_Game_S;
import iecompbot.objects.server.*;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static iecompbot.Constants.clans;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.*;
import static iecompbot.Utility.parseEmbedBuilders;
import static iecompbot.interaction.Automation.*;
import static iecompbot.objects.BotManagers.UpdateStaffs;
import static iecompbot.objects.BotManagers.isPowerDisabled;
import static iecompbot.objects.Retrieval.getMessage;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.server.ServerInfo.*;
import static my.utilities.util.Utilities.*;


public class GuildReady {

    public static List<CommandData> AllCommands = new ArrayList<>();
    public static EmbedBuilder Page4 = new EmbedBuilder();
    public static EmbedBuilder Page5 = new EmbedBuilder();

    public static OptionData getGames(boolean required) {
        OptionData PlayableGamesRequired = new OptionData(OptionType.STRING, "game", "Select the game being played.", required);
        for (Game G : Game.values()) PlayableGamesRequired.addChoice(G.getFullName(), G.getCode());
        return PlayableGamesRequired;
    }
    public static OptionData getPlayableGames(boolean required) {
        OptionData PlayableGamesRequired = new OptionData(OptionType.STRING, "game", "Select the game being played.", required);
        for (Game G : Game.values()) {
            if (!G.getCode().equals("IEVRBETA") && !G.getCode().equals("IESD") && !G.getCode().equals("IE1")) {
                PlayableGamesRequired.addChoice(G.getFullName(), G.getCode());
            }
        }
        return PlayableGamesRequired;
    }

    public static void createTheCommands() {
        AllCommands = new ArrayList<>(Collections.emptyList());
        System.out.println("[Setup] Creating the commands...");
        Terminal.UpdateConsole();

        OptionData name = new OptionData(OptionType.STRING, "name", "The player name.", true);
        OptionData user = new OptionData(OptionType.USER, "user", "Select a user.", true);
        OptionData user2 = new OptionData(OptionType.USER, "user-2", "Select another user.", true);

        OptionData auraString = new OptionData(OptionType.STRING, "aura", "User targeted.", true);

        // 3rd Search
        OptionData position = new OptionData(OptionType.STRING, "position", "Your position.", true);
        position.addChoice("FW", "FW");
        position.addChoice("MF", "MF");
        position.addChoice("DF", "DF");
        position.addChoice("GK", "GK");
        OptionData gender = new OptionData(OptionType.STRING, "gender", "Your gender [Male/Female].", true);
        gender.addChoice("Male", "Male");
        gender.addChoice("Female", "Female");
        OptionData kick = new OptionData(OptionType.STRING, "kick", "Your Kick Stat.", true);
        kick.addChoice("S+ (8)", "S+");
        kick.addChoice("S (7)", "S");
        kick.addChoice("A+ (6)", "A+");
        kick.addChoice("A (5)", "A");
        kick.addChoice("B (4)", "B");
        kick.addChoice("C (3)", "C");
        kick.addChoice("D (2)", "D");
        kick.addChoice("E (1)", "E");
        OptionData dribble = new OptionData(OptionType.STRING, "dribble", "Your Dribble Stat.", true);
        dribble.addChoices(kick.getChoices());
        OptionData defense = new OptionData(OptionType.STRING, "defense", "Your Defense Stat.", true);
        defense.addChoices(kick.getChoices());
        OptionData catchs = new OptionData(OptionType.STRING, "catch", "Your catch Stat.", true);
        catchs.addChoices(kick.getChoices());
        OptionData technique = new OptionData(OptionType.STRING, "technique", "Your technqiue Stat.", true);
        technique.addChoices(kick.getChoices());
        OptionData stamina = new OptionData(OptionType.STRING, "stamina", "Your stamina Stat.", true);
        stamina.addChoices(kick.getChoices());
        OptionData GP = new OptionData(OptionType.INTEGER, "gp", "Your max GP Stat.", true);
        OptionData TP = new OptionData(OptionType.INTEGER, "tp", "Your max TP Stat.", true);
        OptionData move1 = new OptionData(OptionType.STRING, "move1", "Your first move.", true);
        OptionData move2 = new OptionData(OptionType.STRING, "move2", "Your second move.", true);
        OptionData move3 = new OptionData(OptionType.STRING, "move3", "Your third move.", true);
        OptionData move4 = new OptionData(OptionType.STRING, "move4", "Your forth move.", true);
        OptionData skill1 = new OptionData(OptionType.STRING, "skill1", "Your 1st skill.", true);
        OptionData skill2 = new OptionData(OptionType.STRING, "skill2", "Your 2nd skill.", true);
        OptionData player1 = new OptionData(OptionType.STRING, "player-1", "The forward of the team.", true);
        OptionData player2 = new OptionData(OptionType.STRING, "player-2", "The midfielder of the team.", true);
        OptionData player3 = new OptionData(OptionType.STRING, "player-3", "The midfielder of the team.", true);
        OptionData player4 = new OptionData(OptionType.STRING, "player-4", "The defender of the team.", true);
        OptionData player5 = new OptionData(OptionType.STRING, "player-gk", "The goalkeeper of the team.", true);


        AllCommands.add(Commands.slash("support", "Have any problem with the bot? Contact us!").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.STRING, "type-of-problem", "What kind of issues do you have?", true)
                                .addChoice("Tournament", "Tournament")
                                .addChoice("Profile", "Profile")
                                .addChoice("Clan", "Clan")
                                .addChoice("Complaint", "Complaint")
                                .addChoice("Other", "Other"),
                        new OptionData(OptionType.STRING, "issue", "Explain your issue as detailed as possible.", true)));
        AllCommands.add(Commands.slash("report", "Find anything suspicious about people? Contact us!").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.STRING, "type-of-problem", "What kind of issues do you have?", true)
                                .addChoice("Fake-Scoring", "Fake-Scoring")
                                .addChoice("Alt-Account", "Alt-Account")
                                .addChoice("Clan Abuse", "Clan Abuse")
                                .addChoice("Other", "Other"),
                        new OptionData(OptionType.STRING, "note", "If you have something to say about them.", true),
                        new OptionData(OptionType.USER, "abuser", "The user you want to report.", false)));
        AllCommands.add(Commands.slash("suggestion", "Any idea on how to improve the bot? Contact us!").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.STRING, "description", "Tell us about it !", true)));
        AllCommands.add(Commands.slash("help", "Directions on how to use the bot.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .setNameLocalization(DiscordLocale.FRENCH, "aide").setDescriptionLocalization(DiscordLocale.FRENCH, "Directives sur comment utiliser le bot.")
                .addOptions(new OptionData(OptionType.STRING, "section", "What do you need help about ?", true)
                        .addChoice("Profile Management", "1")
                        .addChoice("Clan Management", "2")
                        .addChoice("Tournament Management", "3")
                        .addChoice("Matchmaking", "4")
                        .addChoice("Misc", "5")));
        AllCommands.add(Commands.slash("bot-info", "Directions on how to use the bot.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .setNameLocalization(DiscordLocale.FRENCH, "bot-info").setDescriptionLocalization(DiscordLocale.FRENCH, "Les informations sur le bot."));
        AllCommands.add(Commands.slash("list-permissions", "Directions on how to use the bot.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .setNameLocalization(DiscordLocale.FRENCH, "list-permissions").setDescriptionLocalization(DiscordLocale.FRENCH, "Voir toutes les permissions du bot.."));











        // Profile
        AllCommands.add(Commands.slash("shop", "Purchase Items from the coins you earned from matchmaking.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.STRING, "article", "The type of article you want to see.", true)
                        .addChoice("Server Specific (if any)", "Server")
                        .addChoice("Unique Items", "Unique Items")
                        .addChoice("Cosmetics (Frame)", "Cosmetics (Frame)")
                        .addChoice("Cosmetics (Board)", "Cosmetics (Board)")
                        .addChoice("Materials", "Materials")
                        .addChoice("Boosters", "Boosters")
                        .addChoice("License BG", "License BG")
                        .addChoice("License FG", "License FG")
                        .addChoice("License RY", "License RY")
                        .addChoice("License ST", "License ST")));
        AllCommands.add(Commands.slash("profile", "Displays your profile or someone else.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Voir votre profil ou celui de quelqu'un d'autre.")
                .addOptions(new OptionData(OptionType.STRING, "profile-info", "The type of information you want to see.", false)
                                .addChoice("Basic Information", "Basic").setDescriptionLocalization(DiscordLocale.FRENCH, "Information Basiques")
                                .addChoice("Games Statistics", "Games").setDescriptionLocalization(DiscordLocale.FRENCH, "Statistiques des Jeux")
                                .addChoice("Tournaments list", "Tournament").setDescriptionLocalization(DiscordLocale.FRENCH, "Liste de tournois")
                                .addChoice("Power & Activity", "Power & Activity").setDescriptionLocalization(DiscordLocale.FRENCH, "Puissance & Activité")
                                .addChoice("Trophies & Rewards", "Trophies").setDescriptionLocalization(DiscordLocale.FRENCH, "Trophées & Récompenses")
                                .addChoice("Inventory", "Inventory").setDescriptionLocalization(DiscordLocale.FRENCH, "Inventaire")
                                .addChoice("Quests", "Quests").setDescriptionLocalization(DiscordLocale.FRENCH, "Quêtes")
                                .addChoice("Achievements", "Achievements").setDescriptionLocalization(DiscordLocale.FRENCH, "Succès")
                                .addChoice("Match History", "Match History").setDescriptionLocalization(DiscordLocale.FRENCH, "Historique des matchs")
                                .addChoice("Clan History", "Clan History").setDescriptionLocalization(DiscordLocale.FRENCH, "Historique des clans")
                                .addChoice("All Licenses", "License").setDescriptionLocalization(DiscordLocale.FRENCH, "Vos licences"),
                        new OptionData(OptionType.USER, "player", "User you want to view.", false)));
        AllCommands.add(Commands.slash("profile-manage", "Manage your global profile or someone else.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("profile-set_stats", "Set your stats.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.STRING, "element", "Your element.", true)
                                .addChoice("Fire", "Fire")
                                .addChoice("Wind", "Wind")
                                .addChoice("Wood", "Wood")
                                .addChoice("Earth", "Earth"),
                        position,
                        new OptionData(OptionType.STRING, "gender", "Your gender.", true).addChoices(Arrays.stream(Gender.values()).map(G -> new Command.Choice(G.getName(), G.getName())).toList())
                        , kick, dribble, defense, catchs, technique, stamina));
        AllCommands.add(Commands.slash("profile-set_moveset", "Set your stats.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(GP, TP, move1, move2, move3, move4, skill1, skill2));
        AllCommands.add(Commands.slash("profile-create_team", "Create a team of 5 with the players you like.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(name, player1, player2, player3, player4, player5));
        AllCommands.add(Commands.slash("profile-do-miximax", "Make a miximax between 2 of your players.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(player1, auraString, name,
                        new OptionData(OptionType.STRING, "shared-move-1", "The one your player will receive.", true)
                                .addChoice("Move 1", "Move 1")
                                .addChoice("Move 2", "Move 2")
                                .addChoice("Move 3", "Move 3")
                                .addChoice("Move 4", "Move 4")
                                .addChoice("Skill 1", "Skill 1")
                                .addChoice("Skill 2", "Skill 2"),
                        new OptionData(OptionType.STRING, "shared-move-2", "The one your player will receive.", true)
                                .addChoice("Move 1", "Move 1")
                                .addChoice("Move 2", "Move 2")
                                .addChoice("Move 3", "Move 3")
                                .addChoice("Move 4", "Move 4")
                                .addChoice("Skill 1", "Skill 1")
                                .addChoice("Skill 2", "Skill 2")));
        AllCommands.add(Commands.slash("profile-remove-miximax", "Remove the miximax of a player.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(player1));
        AllCommands.add(Commands.slash("profile-teams", "Shows all your current teams.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("profile-buy_player", "Recruit a player.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(name));
        AllCommands.add(Commands.slash("profile-character", "Upload a PNG character for your license card.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.ATTACHMENT, "character", "Upload a PNG of a character.", true)));









        // Match
        AllCommands.add(Commands.slash("host", "Make a choice between 2 players.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.USER, "p1", "The first player.", true),
                        new OptionData(OptionType.USER, "p2", "The second player.", true)));
        AllCommands.add(Commands.slash("prediction", "Set up predictions based on a message.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.USER, "p1", "The first player.", true),
                        new OptionData(OptionType.USER, "p2", "The second player.", true)));
        AllCommands.add(Commands.slash("matchmaking", "Pings everyone with the role of the selected game.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(getPlayableGames(true),
                        new OptionData(OptionType.BOOLEAN, "is-anonymous", "Do you want your name to be masked? False by default.", false),
                        new OptionData(OptionType.BOOLEAN, "is-global", "Whether the matchmaking will be sent in all your mutual servers. False by default.", false),
                        new OptionData(OptionType.STRING, "rule", "The type of rule you want to play with.", false)
                                .addChoice("Draft", "Draft")
                                .addChoice("30min", "30min")
                                .addChoice("15min", "15min")));
        AllCommands.add(Commands.slash("league", "Shows the league.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("score", "Reports a match score, contributes to both server points, tournaments and global XP.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.INTEGER, "myscore", "Your amount of goals scored.", true),
                        new OptionData(OptionType.INTEGER, "opponentscore", "Amount of goals scored by your opponent.", true),
                        new OptionData(OptionType.USER, "opponent", "Your opponent's name.", true),
                        getPlayableGames(true),
                        new OptionData(OptionType.ATTACHMENT, "proof", "Send a proof of result, such as a screenshot.", false),
                        new OptionData(OptionType.USER, "winner-of-tie", "Determine a winner from tied home & away matches. (USABLE IN TOURNAMENTS ONLY)", false)));
        AllCommands.add(Commands.slash("revert-score", "Reverts a match score. Removes both history and points and XP.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.INTEGER, "myscore", "Your amount of goals scored.", true),
                        new OptionData(OptionType.INTEGER, "opponentscore", "Amount of goals scored by your opponent.", true),
                        new OptionData(OptionType.USER, "opponent", "Your opponent's name.", true),
                        getPlayableGames(true)));
        AllCommands.add(Commands.slash("bet", "Set up predictions with InaCoin bet based on a message.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(user, user2, getPlayableGames(true),
                        new OptionData(OptionType.INTEGER, "minute", "Number of minutes.", true)));

        AllCommands.add(Commands.slash("idl-info", "View information about the Inazuma Discord League.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("idl-rules", "View the rules of the Inazuma Discord League.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("idl-message_opponent", "View the rules of the Inazuma Discord League.").addOptions(
                new OptionData(OptionType.STRING, "message", "The message to send to the opponent.", true).setMaxLength(900)
        ).setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("idl-score", "Submit a score for your opponent of Inazuma Discord League.").addOptions(
                new OptionData(OptionType.INTEGER, "myscore", "Your amount of goals scored.", true),
                new OptionData(OptionType.INTEGER, "opponentscore", "Amount of goals scored by your opponent.", true)
        ).setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("idl-register", "Registers to the Inazuma Discord League.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("idl-unregister", "Unregisters to the Inazuma Discord League.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));

        AllCommands.add(Commands.slash("events", "Sends a list of all past events.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.STRING, "type", "The type of events you wish to see.", true)
                        .addChoice("World Cups", "WC")
                        .addChoice("Clan Cups", "CC")
                        .addChoice("Cup of Nations (CAN)", "CAN")
                        .addChoice("European Champions", "EU")));
        AllCommands.add(Commands.slash("mercato", "View a list of all clanless top players.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.STRING, "player-data", "The type of player data you want to see.", true)
                                .addChoice("Power Ranking", "Power")
                                .addChoice("Activity Ranking", "Activity")
                                .addChoice("Age", "Age"),
                        getGames(false),
                        new OptionData(OptionType.STRING, "nation", "The data about players coming fron only these specific regions. Eg. French, Belgian, Brazilian...", false),
                        new OptionData(OptionType.BOOLEAN, "include-clan-members", "Whether to also include clan members.", false)));
        AllCommands.add(Commands.slash("active-tourney", "List of all tournaments with open registrations or currently running.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(getGames(false)));

        AllCommands.add(Commands.slash("server-info", "Shows the information about the server.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the server you are looking for.", false)));
        AllCommands.add(Commands.slash("leaderboard", "Show the leaderboard of the game of your choice.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(getGames(true), new OptionData(OptionType.INTEGER, "page", "Select the page you want.", false).setRequiredRange(1, 25)));




        // Clan
        AllCommands.add(Commands.slash("clan-register", "Register your new clan with your first four members. (excluding you).").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(
                        new OptionData(OptionType.STRING, "name", "The name of the clan.", true).setRequiredLength(6, 24),
                        new OptionData(OptionType.STRING, "tag", "The tag of the clan.", true).setRequiredLength(2, 4),
                        new OptionData(OptionType.STRING, "color", "The color of the clan.", true).setRequiredLength(7, 7),
                        new OptionData(OptionType.STRING, "nationality", "The nationality of the clan.", true).setRequiredLength(5, 20),
                        new OptionData(OptionType.STRING, "description", "The description of the clan.", true).setRequiredLength(64, 256),
                        new OptionData(OptionType.USER, "member-1", "The first member of your clan.", true),
                        new OptionData(OptionType.USER, "member-2", "The second member of your clan.", true),
                        new OptionData(OptionType.USER, "member-3", "The third member of your clan.", true),
                        new OptionData(OptionType.USER, "member-4", "The fourth member of your clan.", true),
                        new OptionData(OptionType.ATTACHMENT, "logo", "The logo of the clan. (must be a backgroundless PNG)", true)));
        AllCommands.add(Commands.slash("clan-info", "Displays the clan information.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(Constants.clans, new OptionData(OptionType.STRING, "clan-info", "The type of information you want to see.", false)
                        .addChoice("Basic Information", "Basic Information").setDescriptionLocalization(DiscordLocale.FRENCH, "Information Basiques")
                        .addChoice("Power & Activity", "Power & Activity").setDescriptionLocalization(DiscordLocale.FRENCH, "Puissance & Activité")
                        .addChoice("Tournament lists", "Tournament List").setDescriptionLocalization(DiscordLocale.FRENCH, "Liste de tournois")
                        .addChoice("Trophies & Rewards", "Clan Rewards").setDescriptionLocalization(DiscordLocale.FRENCH, "Trophées & Récompenses")
                        .addChoice("Interclans History", "Interclans History").setDescriptionLocalization(DiscordLocale.FRENCH, "Historique d'Interclans")
                        .addChoice("Vault", "Vault").setDescriptionLocalization(DiscordLocale.FRENCH, "Coffrefort")
                        .addChoice("[Private] Activity Logs", "Clan Logs").setDescriptionLocalization(DiscordLocale.FRENCH, "Logs de clan")
                        .addChoice("[Private] Warn Notices", "Clan Warns").setDescriptionLocalization(DiscordLocale.FRENCH, "Avertissements de clan")));
        AllCommands.add(Commands.slash("clan-invite_member", "Invite a member to a clan. (ID works as well)").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(user,
                        new OptionData(OptionType.BOOLEAN, "private-message", "Send in private message or on the channel ?", true),
                        new OptionData(OptionType.BOOLEAN, "is-reinforcement", "Whether this member is a reinforcement or not. They won't give power or receive clan role and tag.", false),
                        new OptionData(OptionType.STRING, "contract", "How long the member is expected to stay in the clan.", false)
                                .addChoice("Contract ends after 1 week or longer.", "1W")
                                .addChoice("Contract ends after 2 weeks or longer.", "2W")
                                .addChoice("Contract ends after 1 month or longer.", "1M")
                                .addChoice("Contract ends after 2 months or longer.", "2M")
                                .addChoice("Contract ends after 3 months or longer.", "3M")));
        AllCommands.add(Commands.slash("clan-logo", "Upload a PNG logo for your clan.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(new OptionData(OptionType.ATTACHMENT, "logo", "Upload a PNG of a clan logo.", true)));
        AllCommands.add(Commands.slash("clan-disband", "Disband your clan if you are the captain").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("clan-leave", "Leave a clan by yourself.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("clan-manage", "Edit the information and member information of the clan.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL));
        AllCommands.add(Commands.slash("clan-list", "List of all clans.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(getPlayableGames(false)));






        // interclan
        AllCommands.add(Commands.slash("interclan-request", "Request a clan battle against other clans.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(Constants.clans,
                        new OptionData(OptionType.INTEGER, "rule-matches", "How many matches do you want to play?", true)
                                .addChoice("3v3", 3)
                                .addChoice("5v5", 5)
                                .addChoice("7v7", 7)
                                .addChoice("9v9", 9)
                                .addChoice("Whole Line-up vs Whole Line-up", 0),
                        new OptionData(OptionType.STRING, "rule-distribution", "How do you want the matches to be generated?", true)
                                .addChoice("Random (Random Matchups)", "Random")
                                .addChoice("Balanced (Matchups based on Power)", "Balanced"),
                        getPlayableGames(true)));
        AllCommands.add(Commands.slash("interclan-manage", "Manage your current interclan, as well as your line up.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(Constants.interclans));
        AllCommands.add(Commands.slash("interclan-info", "View information an interclan.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                .addOptions(Constants.interclans));






        // Only ME - Criminal
        AllCommands.add(Commands.slash("mdt-score", "Reports a match score with arranged score, contributes to both game points and global XP.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.USER, "p1", "P1", true),
                        new OptionData(OptionType.INTEGER, "p1-score", "P1 Score", true),
                        new OptionData(OptionType.INTEGER, "p2-score", "P2 Score", true),
                        new OptionData(OptionType.USER, "p2", "P2", true),
                        getGames(true)));
        AllCommands.add(Commands.slash("mdt-revert-score", "Revert a match score with arranged score.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.USER, "p1", "P1", true),
                        new OptionData(OptionType.INTEGER, "p1-score", "P1 Score", true),
                        new OptionData(OptionType.INTEGER, "p2-score", "P2 Score", true),
                        new OptionData(OptionType.USER, "p2", "P2", true),
                        getGames(true)));
        AllCommands.add(Commands.slash("mdf-message", "Send an interactable message.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.STRING, "message", "The type of interactable message.", true)
                                .addChoice("Friendcode Add", "FCAdd")
                                .addChoice("Nationality Add", "NationalityAdd")
                                .addChoice("Add Score", "AddScore"),
                        new OptionData(OptionType.STRING, "misc", "Misc data, such as tournament ID.", false)));
        AllCommands.add(Commands.slash("mdt-give_match_quest", "Give a quest to someone.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.USER, "user", "The user to give.", true),
                        new OptionData(OptionType.USER, "opponent", "The opponent.", true),
                        getGames(false),
                        new OptionData(OptionType.INTEGER, "coins-reward", "Amount of coin rewards.", false),
                        new OptionData(OptionType.STRING, "type", "The type of objective", false)
                                .addChoice("PLAY", "PLAY")
                                .addChoice("WIN", "WIN")
                                .addChoice("TIE", "TIE")
                                .addChoice("LOSE", "LOSE")));
        AllCommands.add(Commands.slash("mdf-send_message", "View and edit a tournament.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.STRING, "text", "What to send?", true),
                        new OptionData(OptionType.STRING, "guildid", "Enter guild id.", true),
                        new OptionData(OptionType.STRING, "channelid", "Enter channel id.", true),
                        new OptionData(OptionType.STRING, "reply-messageid", "Enter message id of which the bot is replying.", false),
                        new OptionData(OptionType.USER, "user", "Whether you want it to be a webhook representing someone.", false),
                        new OptionData(OptionType.STRING, "button-label-1", "A button with name.", false),
                        new OptionData(OptionType.STRING, "button-id-1", "A button with ID.", false)));









        // Admin
        OptionData time = new OptionData(OptionType.STRING, "start-date", "Select the date that the tournament will start.", true);
        long epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(1, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice("Tomorrow " + getDDMMYYYY(Instant.now().plus(2, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(2, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(2, ChronoUnit.DAYS))  + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(3, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(3, ChronoUnit.DAYS)) + " - 14:00 ", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(4, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(4, ChronoUnit.DAYS)) + " - 14:00 ", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(5, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(5, ChronoUnit.DAYS)) + " - 14:00 ", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(6, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(6, ChronoUnit.DAYS)) + " - 14:00 ", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(7, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(7, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(8, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(8, ChronoUnit.DAYS))  + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(9, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(9, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(10, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(10, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(11, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(11, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(12, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(12, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(13, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(13, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(14, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(14, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(15, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(15, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(16, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(16, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(17, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(17, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(18, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(18, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(19, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(19, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(20, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(20, ChronoUnit.DAYS)) + " - 14:00", epoch + "");
        epoch = PatternToEpochSecond(getDDMMYYYY(Instant.now().plus(21, ChronoUnit.DAYS)) + " - 14:00", "dd/MM/yyyy - HH:mm");
        time.addChoice(getDayOfTheWeekShort(Instant.ofEpochSecond(epoch)) + " " + getDDMMYYYY(Instant.now().plus(21, ChronoUnit.DAYS)) + " - 14:00", epoch + "");


        AllCommands.add(Commands.slash("adm-inscription", "Create an automanagable tournament.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.STRING, "challonge-link", "Paste the link of your active server.", true),
                        new OptionData(OptionType.STRING, "tournament-name", "Enter the name of the tournament. Try to keep the same name as the server tournament.", true),
                        getPlayableGames(true), time,
                        new OptionData(OptionType.BOOLEAN, "advertise", "Whether you want the tournament to be advertised on all servers.", true),
                        new OptionData(OptionType.ROLE, "participant-role", "COMPULSORY: Role for tournament participants for easier mention.", true),
                        new OptionData(OptionType.ROLE, "organizer-role", "COMPULSORY: Role for tournament organizers who can edit the tournament.", true),
                        new OptionData(OptionType.CHANNEL, "inscription-channel", "COMPULSORY: The channel where the auto-generated inscription message will be.", true).setChannelTypes(ChannelType.TEXT),
                        new OptionData(OptionType.CHANNEL, "panel-channel", "COMPULSORY: The channel where the control panel will be, with matches, participants list, etc.", true).setChannelTypes(ChannelType.TEXT),
                        new OptionData(OptionType.CHANNEL, "rules-channel", "COMPULSORY: The channel where the tournament rules are to be read.", true).setChannelTypes(ChannelType.TEXT),
                        new OptionData(OptionType.CHANNEL, "result-channel", "COMPULSORY: The channel where the match results will be sent.", true).setChannelTypes(ChannelType.TEXT),
                        new OptionData(OptionType.CHANNEL, "prediction-channel", "Optional: The channel where the predictions will be sent.", false).setChannelTypes(ChannelType.TEXT),
                        new OptionData(OptionType.INTEGER, "vs-type", "Is it a default 1v1 or more?", false)
                                .addChoice("1v1", 1).addChoice("2v2", 2).addChoice("3v3", 3).addChoice("4v4", 4)
                                .addChoice("5v5", 5).addChoice("6v6", 6).addChoice("7v7", 7).addChoice("8v8", 8),
                        new OptionData(OptionType.INTEGER, "signup-cap", "Optional: Enter the minimum number of participants you want. Default is 0 (none).The minimum is 8.", false).setRequiredRange(8, 256),
                        new OptionData(OptionType.STRING, "custom-inscription-message", "If the inscription reaction message already exist, enter the link here.", false),
                        new OptionData(OptionType.STRING, "custom-start-time", "Overrides the start time using a timestamp or epoch second.", false),
                        new OptionData(OptionType.STRING, "custom-message", "Custom message for the notification.", false),
                        new OptionData(OptionType.ATTACHMENT, "tournament-banner", "A banner for the inscription, event and notification.", false)));
        AllCommands.add(Commands.slash("adm-server_manage", "Manage server information.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD));
        AllCommands.add(Commands.slash("adm-ranking_manage", "Manage server ranking information.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD));
        AllCommands.add(Commands.slash("adm-view_tourney", "View and edit a tournament.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.STRING, "tournament-link", "The link of the tournament.", false)));
        AllCommands.add(Commands.slash("adm-tourney_image", "Add an image to a server.").addOptions(
                new OptionData(OptionType.STRING, "challonge", "Challonge ID.", true),
                new OptionData(OptionType.ATTACHMENT, "image", "Image.", true)));

        time = new OptionData(OptionType.INTEGER, "end-time", "Select the length of the blacklist.", true);
        time.addChoice("After 1 tournament", 1);
        time.addChoice("After 2 tournaments", 2);
        time.addChoice("After 3 tournaments", 3);
        time.addChoice("After 4 tournaments", 4);
        time.addChoice("After 5 tournaments", 5);
        time.addChoice("After 6 tournaments", 6);
        time.addChoice("After 7 tournaments", 7);
        time.addChoice("After 8 tournaments", 8);
        time.addChoice("After 9 tournaments", 9);
        time.addChoice("After 10 tournament", 10);
        time.addChoice("Indefinitely (Forever)", -1);
        AllCommands.add(Commands.slash("adm-blacklist_add", "Add someone to the blacklist for a number of time.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(user, time, getPlayableGames(true).addChoice("All", "All"),
                        new OptionData(OptionType.STRING, "reason", "The reason of the blacklist.", true)));
        AllCommands.add(Commands.slash("adm-blacklist_remove", "Add someone to the blacklist for a number of time.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(user, getPlayableGames(true).addChoice("All", "All")));

        AllCommands.add(Commands.slash("adm-set_currency", "Set a custom currency for this server.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the currency.", true).setRequiredLength(4,64),
                        new OptionData(OptionType.STRING, "description", "The description of the currency.", true).setRequiredLength(16,128),
                        new OptionData(OptionType.STRING, "emojiid", "The id of the server emoji of the currency.", true).setRequiredLength(10,20),
                        new OptionData(OptionType.INTEGER, "amount-per-win", "The amount you earn per match win (can be 0).", true),
                        new OptionData(OptionType.INTEGER, "amount-per-top1", "The amount you earn per top 1 (can be 0).", true),
                        new OptionData(OptionType.INTEGER, "amount-per-top2", "The amount you earn per top 2 (can be 0).", true),
                        new OptionData(OptionType.INTEGER, "amount-per-top3", "The amount you earn per top 3 (can be 0).", true)
                ));
        AllCommands.add(Commands.slash("adm-add_shop", "Add a new item to the server shop.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item.", true).setRequiredLength(4,64),
                        new OptionData(OptionType.STRING, "description", "The description of the item.", true).setRequiredLength(16,128),
                        new OptionData(OptionType.STRING, "emojiid", "The id of the server emoji of the item.", true).setRequiredLength(10,20),
                        new OptionData(OptionType.STRING, "type", "The type of item.", true).setRequiredLength(10,20)
                                .addChoice(Item.ItemType.INSTANT_USE.name(), Item.ItemType.INSTANT_USE.name())
                                .addChoice(Item.ItemType.LICENSE_BG.name(), Item.ItemType.LICENSE_BG.name())
                                .addChoice(Item.ItemType.LICENSE_FG.name(), Item.ItemType.LICENSE_FG.name())
                                .addChoice(Item.ItemType.COSMETICS_FRAME.name(), Item.ItemType.COSMETICS_FRAME.name()),
                        new OptionData(OptionType.INTEGER, "price", "How much of your currency does this item cost?", true),
                        new OptionData(OptionType.ATTACHMENT, "img-asset", "The image asset of this item if it is required.", false)
                ));
        AllCommands.add(Commands.slash("adm-shop_manage", "Manages the server shop.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD));








        // Library
        {
            AllCommands.add(Commands.slash("library-go1", "Search a GO1 player, move or spirit in the libraries.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(new OptionData(OptionType.STRING, "type", "The data type.", true).addChoice("Player", "Player").addChoice("Move", "Move").addChoice("Spirit", "Spirit"),
                            new OptionData(OptionType.STRING, "name", "The name of the data you are searching", true)));
            AllCommands.add(Commands.slash("library-cs", "Search an CS player, move or spirit in the libraries.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(new OptionData(OptionType.STRING, "type", "The data type.", true).addChoice("Player", "Player").addChoice("Move", "Move").addChoice("Spirit", "Spirit"),
                            new OptionData(OptionType.STRING, "name", "The name of the data you are searching", true)));
            AllCommands.add(Commands.slash("library-glx", "Search an Galaxy player, move or spirit in the libraries.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(new OptionData(OptionType.STRING, "type", "The data type.", true).addChoice("Player", "Player").addChoice("Move", "Move").addChoice("Spirit", "Spirit").addChoice("Totem", "Totem"),
                            new OptionData(OptionType.STRING, "name", "The name of the data you are searching", true)));
        }

        // Filter Xtreme
        {
            List<OptionData> D = new ArrayList<>();
            D.add(new OptionData(OptionType.STRING, "priority-stat", "The first stat prioritized in the filter.", true)
                    .addChoice(STRStats.KICK.getName() + " (Shot)", STRStats.KICK.getName())
                    .addChoice(STRStats.BODY.getName() + " (Dribble)", STRStats.BODY.getName())
                    .addChoice(STRStats.GUARD.getName() + " (Block)", STRStats.GUARD.getName())
                    .addChoice(STRStats.CATCH.getName() + " (Catch)", STRStats.CATCH.getName())
                    .addChoice(STRStats.CONTROL.getName(), STRStats.CONTROL.getName())
                    .addChoice(STRStats.SPEED.getName(), STRStats.SPEED.getName())
                    .addChoice(STRStats.TP.getName(), STRStats.TP.getName()));
            D.add(new OptionData(OptionType.STRING, "player-element", "The player must be of this element.", false)
                    .addChoices(Arrays.stream(ie.enums.Element.values()).map(p -> new Command.Choice(p.getName(), p.getName())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "gender", "The player must be of this gender.", false)
                    .addChoice(ie.enums.Gender.Male.getName(), ie.enums.Gender.Male.getName())
                    .addChoice(ie.enums.Gender.Female.getName(), ie.enums.Gender.Female.getName()));
            D.add(new OptionData(OptionType.STRING, "move-element", "His best move must be of this element.", false)
                    .addChoices(Arrays.stream(ie.enums.Element.values()).map(p -> new Command.Choice(p.getName(), p.getName())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "move-type", "The type of move that he will only look for.", false)
                    .addChoice("Solo", "Solo")
                    .addChoice("Duo", "Duo")
                    .addChoice("Trio", "Trio")
                    .addChoice("Move Solo", "Move Solo")
                    .addChoice("Move Duo", "Move Duo")
                    .addChoice("Move Trio", "Move Trio")
                    .addChoice("Keshin Solo", "Keshin Solo")
                    .addChoice("Keshin Duo", "Keshin Duo")
                    .addChoice("Keshin Trio", "Keshin Trio")
                    .addChoice("Armed Solo", "Armed Solo")
                    .addChoice("Armed Duo", "Armed Duo")
                    .addChoice("Armed Trio", "Armed Trio"));
            D.add(new OptionData(OptionType.STRING, "dash-type", "The type of dash this player use.", false)
                    .addChoice("Wind Break - (Lancer, Caleb IJ, etc...)", "Wind Break")
                    .addChoice("White Rush - (Slide, Arion, etc...)", "White Rush")
                    .addChoice("Red Rush - (Spectrum, Laurel, etc...)", "Red Rush")
                    .addChoice("Forward Rotation - (Big players, Jack, Minion, etc...)", "Forward Rotation")
                    .addChoice("Front Space - (Small players, JP, Bradford, etc...)", "Front Space")
                    .addChoice("3 Step Jump - (Muscular players, Kraken, SARU Mixi, etc...)", "3 Step Jump")
                    .addChoice("Loop - (Girl players, Goldie, Bellatrix, etc...)", "Loop"));
            D.add(new OptionData(OptionType.STRING, "charge-time-with-ball", "The charge time of this player when heh as the ball.", false)
                    .addChoice("S+++++++ [01:30]", "S+++++++ (11) [01:30]")
                    .addChoice("S++++++ [01:40]", "S++++++ (10) [01:40]")
                    .addChoice("S+++++ [01:51]", "S+++++ (9) [01:51]")
                    .addChoice("S++++ [02:05]", "S++++ (8) [02:05]")
                    .addChoice("S+++ [02:22]", "S+++ (7) [02:22]")
                    .addChoice("S++ [02:46]", "S++ (6) [02:46]")
                    .addChoice("S+ [03:20]", "S+ (5) [03:20]")
                    .addChoice("A [05:33]", "A (3) [05:33]")
                    .addChoice("B [08:20]", "B (2) [08:20]"));
            D.add(new OptionData(OptionType.STRING, "charge-time-without-ball", "The charge time of this player when he doesn't have the ball.", false)
                    .addChoice("S++ [02:46]", "S++ (6) [02:46]")
                    .addChoice("S+ [03:20]", "S+ (5) [03:20]")
                    .addChoice("S [04:10]", "S (4) [04:10]")
                    .addChoice("A [05:33]", "A (3) [05:33]")
                    .addChoice("B [08:20]", "B (2) [08:20]")
                    .addChoice("C [16:40]", "C (1) [16:40]"));
            D.add(new OptionData(OptionType.INTEGER, "min-tp", "The player's TP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-kick", "The player's kick must not be below this value.", false)
                    .addChoice("110 (S+)", 110)
                    .addChoice("100 (S)", 100)
                    .addChoice("90 (A+)", 90)
                    .addChoice("80 (A)", 80)
                    .addChoice("70 (B+)", 70)
                    .addChoice("60 (B)", 60)
                    .addChoice("50 (C+)", 50)
                    .addChoice("40 (C)", 40)
                    .addChoice("30 (D+)", 30)
                    .addChoice("20 (D)", 20)
                    .addChoice("10 (E+)", 10)
                    .addChoice("0 (E)", 0));
            D.add(new OptionData(OptionType.INTEGER, "minimum-body", "The player's body must not be below this value.", false)
                    .addChoice("110 (S+)", 110)
                    .addChoice("100 (S)", 100)
                    .addChoice("90 (A+)", 90)
                    .addChoice("80 (A)", 80)
                    .addChoice("70 (B+)", 70)
                    .addChoice("60 (B)", 60)
                    .addChoice("50 (C+)", 50)
                    .addChoice("40 (C)", 40)
                    .addChoice("30 (D+)", 30)
                    .addChoice("20 (D)", 20)
                    .addChoice("10 (E+)", 10)
                    .addChoice("0 (E)", 0));
            D.add(new OptionData(OptionType.INTEGER, "minimum-control", "The player's control must not be below this value.", false)
                    .addChoice("110 (S+)", 110)
                    .addChoice("100 (S)", 100)
                    .addChoice("90 (A+)", 90)
                    .addChoice("80 (A)", 80)
                    .addChoice("70 (B+)", 70)
                    .addChoice("60 (B)", 60)
                    .addChoice("50 (C+)", 50)
                    .addChoice("40 (C)", 40)
                    .addChoice("30 (D+)", 30)
                    .addChoice("20 (D)", 20)
                    .addChoice("10 (E+)", 10)
                    .addChoice("0 (E)", 0));
            D.add(new OptionData(OptionType.INTEGER, "minimum-guard", "The player's guard must not be below this value.", false)
                    .addChoice("110 (S+)", 110)
                    .addChoice("100 (S)", 100)
                    .addChoice("90 (A+)", 90)
                    .addChoice("80 (A)", 80)
                    .addChoice("70 (B+)", 70)
                    .addChoice("60 (B)", 60)
                    .addChoice("50 (C+)", 50)
                    .addChoice("40 (C)", 40)
                    .addChoice("30 (D+)", 30)
                    .addChoice("20 (D)", 20)
                    .addChoice("10 (E+)", 10)
                    .addChoice("0 (E)", 0));
            D.add(new OptionData(OptionType.INTEGER, "minimum-speed", "The player's speed must not be below this value.", false)
                    .addChoice("110 (S+)", 110)
                    .addChoice("100 (S)", 100)
                    .addChoice("90 (A+)", 90)
                    .addChoice("80 (A)", 80)
                    .addChoice("70 (B+)", 70)
                    .addChoice("60 (B)", 60)
                    .addChoice("50 (C+)", 50)
                    .addChoice("40 (C)", 40)
                    .addChoice("30 (D+)", 30)
                    .addChoice("20 (D)", 20)
                    .addChoice("10 (E+)", 10)
                    .addChoice("0 (E)", 0));
            D.add(new OptionData(OptionType.INTEGER, "minimum-catch", "The player's catch must not be below this value.", false)
                    .addChoice("110 (S+)", 110)
                    .addChoice("100 (S)", 100)
                    .addChoice("90 (A+)", 90)
                    .addChoice("80 (A)", 80)
                    .addChoice("70 (B+)", 70)
                    .addChoice("60 (B)", 60)
                    .addChoice("50 (C+)", 50)
                    .addChoice("40 (C)", 40)
                    .addChoice("30 (D+)", 30)
                    .addChoice("20 (D)", 20)
                    .addChoice("10 (E+)", 10)
                    .addChoice("0 (E)", 0));
            AllCommands.add(Commands.slash("filter-xtr-players", "Filter a list of Strikers Xtreme players, moves or spirits in the libraries.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(D));
        }

        // Builder Filter 3DS
        { // Build GO1
            List<OptionData> D = new ArrayList<>();
            D.add(new OptionData(OptionType.STRING, "player-name", "The name of the player you want to build.", true));
            D.add(new OptionData(OptionType.STRING, "as-position", "The position you want to play this player.", true)
                    .addChoices(Arrays.stream(BuildType.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            AllCommands.add(Commands.slash("build-go1-player", "Build the GO1 player of your choice and Import them to your game.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(D));
        }
        { // Build CS
            List<OptionData> D = new ArrayList<>();
            D.add(new OptionData(OptionType.STRING, "player-name", "The name of the player you want to build.", true));
            D.add(new OptionData(OptionType.STRING, "as-position", "The position you want to play this player.", true)
                    .addChoices(Arrays.stream(BuildType.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "team-element", "The element your player is maintaining in the team to decide the moveset.", true)
                    .addChoices(Arrays.stream(Element.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "miximax-name", "Name of the miximax you want.", false));
            D.add(new OptionData(OptionType.BOOLEAN, "search-mixi", "Whether to include miximax while generating the players. False by default.", false));

            D.add(new OptionData(OptionType.INTEGER, "min-gp", "The player's GP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "min-tp", "The player's TP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-gp", "The player's GP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-tp", "The player's TP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-kick", "The player's kick must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-dribble", "The player's dribble must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-defense", "The player's defense must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-catch", "The player's catch must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-technique", "The player's technique must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-speed", "The player's speed must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-stamina", "The player's stamina must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-luck", "The player's luck must not be below this value.", false));

            AllCommands.add(Commands.slash("build-cs-player", "Build the CS player of your choice and Import them to your game.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(D));
        }
        { // Build GLX
            List<OptionData> D = new ArrayList<>();
            D.add(new OptionData(OptionType.STRING, "player-name", "The name of the player you want to build.", true));
            D.add(new OptionData(OptionType.STRING, "as-position", "The position you want to play this player.", true)
                    .addChoices(Arrays.stream(BuildType.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "miximax-name", "Name of the miximax you want.", false));
            D.add(new OptionData(OptionType.BOOLEAN, "search-mixi", "Whether to include miximax while generating the players. False by default.", false));

            D.add(new OptionData(OptionType.INTEGER, "min-gp", "The player's GP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "min-tp", "The player's TP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-gp", "The player's GP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-tp", "The player's TP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-kick", "The player's kick must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-dribble", "The player's dribble must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-defense", "The player's defense must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-catch", "The player's catch must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-technique", "The player's technique must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-speed", "The player's speed must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-stamina", "The player's stamina must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-luck", "The player's luck must not be below this value.", false));

            AllCommands.add(Commands.slash("build-glx-player", "Build the Galaxy player of your choice and Import them to your game.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(D));
        }
        // Filters
        { // Filter GO1
            List<OptionData> D = new ArrayList<>();
            D.add(new OptionData(OptionType.STRING, "as-position", "The position you want to play this player.", true)
                    .addChoices(Arrays.stream(BuildType.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-position", "The position the player must be.", false)
                    .addChoices(Arrays.stream(Position.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-element", "The element the player must be.", false)
                    .addChoices(Arrays.stream(Element.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-gender", "The gender the player must be.", false)
                    .addChoices(Arrays.stream(Gender.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));

            D.add(new OptionData(OptionType.STRING, "excluded-players", "Add names of excluded players and miximax. Separated by a \",\"", false));

            D.add(new OptionData(OptionType.INTEGER, "min-gp", "The player's GP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "min-tp", "The player's TP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-gp", "The player's GP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-tp", "The player's TP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-kick", "The player's kick must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-dribble", "The player's dribble must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-defense", "The player's defense must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-catch", "The player's catch must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-technique", "The player's technique must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-speed", "The player's speed must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-stamina", "The player's stamina must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-luck", "The player's luck must not be below this value.", false));
            AllCommands.add(Commands.slash("filter-go1-players", "Filter a list of GO1 players with their moves or spirits in the libraries.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(D));
        }
        { // Filter CS
            List<OptionData> D = new ArrayList<>();
            D.add(new OptionData(OptionType.STRING, "as-position", "The position you want to play this player.", true)
                    .addChoices(Arrays.stream(BuildType.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "team-element", "The element your player is maintaining in the team to decide the moveset.", true)
                    .addChoices(Arrays.stream(Element.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-position", "The position the player must be.", false)
                    .addChoices(Arrays.stream(Position.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-element", "The element the player must be.", false)
                    .addChoices(Arrays.stream(Element.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-gender", "The gender the player must be.", false)
                    .addChoices(Arrays.stream(Gender.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "miximax-name", "Name of the miximax you want.", false));
            D.add(new OptionData(OptionType.BOOLEAN, "search-mixi", "Whether to search for a miximax while generating the players. False by default.", false));

            D.add(new OptionData(OptionType.STRING, "excluded-players", "Add names of excluded players and miximax. Separated by a \",\"", false));

            D.add(new OptionData(OptionType.INTEGER, "min-gp", "The player's GP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "min-tp", "The player's TP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-gp", "The player's GP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-tp", "The player's TP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-kick", "The player's kick must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-dribble", "The player's dribble must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-defense", "The player's defense must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-catch", "The player's catch must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-technique", "The player's technique must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-speed", "The player's speed must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-stamina", "The player's stamina must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-luck", "The player's luck must not be below this value.", false));

            D.add(new OptionData(OptionType.STRING, "with-move", "The player must have this move.", false)
                    .addChoice("SHOT - Big Bang Slash / Big Bang Slash", "Big Bang Slash")
                    .addChoice("SHOT - Final Death Zone / Last Death Zone", "Final Death Zone")
                    .addChoice("SHOT - I am the Almighty / Great Max na Ore", "I am the Almighty")
                    .addChoice("SHOT - Majestic Radiance / Glorious Ray", "Majestic Radiance")
                    .addChoice("SHOT - Nocturne / The Monsters", "Nocturne")
                    .addChoice("SHOT - Omega Assault / Omega Attack", "Omega Assault")
                    .addChoice("SHOT - Shell Burst / Shellbit Burst", "Shell Burst")
                    .addChoice("SHOT - Supermassive Shot / Chaos Meteor", "Supermassive Shot")
                    .addChoice("SHOT - Supernatural Strike / Devil Burst", "Supernatural Strike")
                    .addChoice("SHOT - Ultimate Eleven Assault / Saikyou Eleven Hadou", "Ultimate Eleven Assault")
                    .addChoice("DRIBBLE - Eye of the Storm / Storm Zone", "Eye of the Storm")
                    .addChoice("DRIBBLE - Tornado Tunnel / Kazaana Drive", "Tornado Tunnel")
                    .addChoice("DRIBBLE - Warp Step / Sprint Warp", "Warp Step")
                    .addChoice("BLOCK - Acrobotics / Muei Ranbu", "Acrobotics")
                    .addChoice("BLOCK - La Flamme / La Flamme", "La Flamme")
                    .addChoice("BLOCK - Master Stroke / Saikyou-san Shoukan", "Master Stroke")
                    .addChoice("BLOCK - Oglitteration / Kirakira Illusion", "Oglitteration")
                    .addChoice("BLOCK - Particle Accelerator / Grand Sweeper", "Particle Accelerator")
                    .addChoice("BLOCK - Side Swipe / Vanishing Cut", "Side Swipe"));
            AllCommands.add(Commands.slash("filter-cs-players", "Filter a list of Chrono Stones players with their moves or spirits in the libraries.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(D));
        }
        { // Filter GLX
            List<OptionData> D = new ArrayList<>();
            D.add(new OptionData(OptionType.STRING, "as-position", "The position you want to play this player.", true)
                    .addChoices(Arrays.stream(BuildType.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-position", "The position the player must be.", false)
                    .addChoices(Arrays.stream(Position.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-element", "The element the player must be.", false)
                    .addChoices(Arrays.stream(Element.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "of-gender", "The gender the player must be.", false)
                    .addChoices(Arrays.stream(Gender.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "miximax-name", "Name of the miximax you want.", false));
            D.add(new OptionData(OptionType.BOOLEAN, "search-mixi", "Whether to search for a miximax while generating the players. False by default.", false));

            D.add(new OptionData(OptionType.STRING, "excluded-players", "Add names of excluded players and miximax. Separated by a \",\"", false));

            D.add(new OptionData(OptionType.INTEGER, "min-gp", "The player's GP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "min-tp", "The player's TP must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-gp", "The player's GP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "max-tp", "The player's TP must not be above this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-kick", "The player's kick must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-dribble", "The player's dribble must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-defense", "The player's defense must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-catch", "The player's catch must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-technique", "The player's technique must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-speed", "The player's speed must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-stamina", "The player's stamina must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-luck", "The player's luck must not be below this value.", false));

            AllCommands.add(Commands.slash("filter-glx-players", "Filter a list of Galaxy players with their moves or spirits in the libraries.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(D));
        }

        // Builder Filter VR
        {
            List<OptionData> D = new ArrayList<>();
            D.add(new OptionData(OptionType.STRING, "priority-stat", "The first stat prioritized in the filter.", true)
                    .addChoices(Arrays.stream(VRStats.values()).map(p -> new Command.Choice(p.getName(), p.toString())).collect(Collectors.toList()))
                    .addChoice("Shot AT", "ShotAT")
                    .addChoice("Focus AT", "FocusAT")
                    .addChoice("Focus DF", "FocusDF")
                    .addChoice("Scramble AT", "ScrambleAT")
                    .addChoice("Scramble DF", "ScrambleDF")
                    .addChoice("KP Gauge", "KP"));
            D.add(new OptionData(OptionType.STRING, "element", "The player must be of this element.", false)
                    .addChoices(Arrays.stream(ie.enums.Element.values()).map(p -> new Command.Choice(p.getName(), p.getName())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "gender", "The player must be of this gender.", false)
                    .addChoice(ie.enums.Gender.Male.getName(), ie.enums.Gender.Male.getName())
                    .addChoice(ie.enums.Gender.Female.getName(), ie.enums.Gender.Female.getName())
                    .addChoice(ie.enums.Gender.Genderless.getName(), ie.enums.Gender.Genderless.getName()));
            D.add(new OptionData(OptionType.STRING, "passive", "What kind of stats his passive must target.", false)
                    .addChoices(Arrays.stream(VRPercentages.values()).map(p -> new Command.Choice(p.toString(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "passive-condition", "The condition for this passive to activate.", false)
                    .addChoices(Arrays.stream(VRPassiveCondition.values()).map(p -> new Command.Choice(p.toString(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.STRING, "passive-target", "What players does this passive target.", false)
                    .addChoices(Arrays.stream(VRAffectedAreas.values()).map(p -> new Command.Choice(p.toString(), p.toString())).collect(Collectors.toList())));
            D.add(new OptionData(OptionType.INTEGER, "minimum-control", "The player's control must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-pressure", "The player's pressure must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-physical", "The player's physical must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-intelligence", "The player's intelligence must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-agility", "The player's agility must not be below this value.", false));
            D.add(new OptionData(OptionType.INTEGER, "minimum-technique", "The player's technique must not be below this value.", false));
            D.add(new OptionData(OptionType.BOOLEAN, "include-equipment", "Whether to include equipements while generating the players. True by default.", false));
            D.add(new OptionData(OptionType.STRING, "excluded-players", "Add names of excluded players and miximax. Separated by a \",\"", false));
            AllCommands.add(Commands.slash("filter-vr-players", "Filter a list of Galaxy players, moves or spirits in the libraries.").setIntegrationTypes(IntegrationType.ALL).setContexts(InteractionContextType.ALL)
                    .addOptions(D));
        }

        // Misc
        AllCommands.add(Commands.slash("snd-play", "Play a music file in your current voice channel.").setIntegrationTypes(IntegrationType.GUILD_INSTALL).setContexts(InteractionContextType.GUILD)
                .addOptions(new OptionData(OptionType.ATTACHMENT, "sound-file", "Upload a MP3, WAV or OGG sound file.", true),
                        new OptionData(OptionType.INTEGER, "repeat-times", "Amount of times the song will repeat.", false)));

        AllCommands.add(Commands.user("View clan..."));
        AllCommands.add(Commands.message("Make Prediction"));
    }
    public static void LoadingTheCommands() {
        ExecutorService CachedPool = Executors.newCachedThreadPool();
        CachedPool.execute(() -> {
            ListAwards();
            ListClans();
            ListInterclans();

            createTheCommands();
            Terminal.UpdateConsole();
            DiscordAccount.updateCommands().addCommands(AllCommands).queue();
            System.out.println("[Setup] Loading the " + AllCommands.size() + " commands in servers...");
        });
        ShutdownWithTimeout(CachedPool, 0.5, "Loading commands...");
    }

    public synchronized static void RefreshAllLeaderboards() {
        if (Instant.now().isAfter(Instant.ofEpochSecond(Constants.RefreshCooldownOfLeaderboard))) {
            Constants.RefreshCooldownOfLeaderboard = Instant.now().plus(30, ChronoUnit.MINUTES).getEpochSecond();
            List<Clan> CLS = Clan.listOpenPaused();
            for (Clan C : Clan.listOpenPaused()) C.RefreshDerelict();
            Leaderboard LB = new Leaderboard(CLS, 1, 15);
            System.out.println("[Setup] Refresh leaderboards...");
            ExecutorService CachedPool = Executors.newFixedThreadPool(20);
            for (DatabaseObject.Row TR : getLeaderboardChannels()) {
                CachedPool.execute(() -> {
                    ChannelMessage CM = new ChannelMessage(TR.getAsLong("ServerID"), TR.getAsLong("ChannelID"), TR.getAsLong("MessageID"), TR.getAsLong("MessageID2"), TR.getAsLong("MessageID3"));
                    if (CM.getGuild() != null && CM.getChannel() != null && hasPermissionInChannel(null, CM.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_HISTORY, Permission.MANAGE_WEBHOOKS)) {
                        ServerInfo I = ServerInfo.get(CM.ServerID);
                        List<EmbedBuilder> embeds = I.hasLeaderboardModifiers() ? new Leaderboard(I, CLS, 1, 15).getEmbeds(I) : LB.getEmbeds(I);
                        if (!embeds.isEmpty()) {
                            List<MessageEmbed> E1 = new ArrayList<>();
                            List<MessageEmbed> E2 = new ArrayList<>();
                            List<MessageEmbed> E3 = new ArrayList<>();
                            for (EmbedBuilder E : embeds) {
                                if (E2.size() == 4) {
                                    E3.add(E.build());
                                } else if (E1.size() == 4) {
                                    E2.add(E.build());
                                } else {
                                    E1.add(E.build());
                                }
                            }
                            CM.ModifyWebhookMessageElseCreate(I.getServerWebhook().addEmbeds(parseEmbedBuilders(E1)), msg -> {
                                if (!Objects.equals(TR.getAsLong("MessageID"), msg.getId())) {
                                    I.Channels().LeaderboardMessageID = msg.getId();
                                    I.Channels().UpdateOnly("LeaderboardMessageID");
                                }
                            });
                            if (!E2.isEmpty()) {
                                CM.ModifyWebhookMessage2ElseCreate(I.getServerWebhook().addEmbeds(parseEmbedBuilders(E2)), msg -> {
                                    if (!Objects.equals(TR.getAsLong("MessageID2"), msg.getId())) {
                                        I.Channels().LeaderboardMessageID2 = msg.getId();
                                        I.Channels().UpdateOnly("LeaderboardMessageID2");
                                    }
                                });
                            }
                            if (!E3.isEmpty()) {
                                CM.ModifyWebhookMessage3ElseCreate(I.getServerWebhook().addEmbeds(parseEmbedBuilders(E3)), msg -> {
                                    if (TR.getAsLong("MessageID3") != msg.getId()) {
                                        I.Channels().LeaderboardMessageID3 = msg.getId();
                                        I.Channels().UpdateOnly("LeaderboardMessageID3");
                                    }
                                });
                            }
                        }
                    } else if (!Prefs.TestMode) {
                        ServerInfo I = ServerInfo.get(CM.ServerID);
                        I.Channels().LeaderboardChannelID = null;
                        I.Channels().LeaderboardMessageID = null;
                        I.Channels().LeaderboardMessageID2 = null;
                        I.Channels().LeaderboardMessageID3 = null;
                        I.Channels().UpdateOnly("LeaderboardChannelID", "LeaderboardMessageID", "LeaderboardMessageID2", "LeaderboardMessageID3");
                    }
                });
            }
            CachedPool.execute(GuildReady::CheckForBirthday);
            ShutdownWithTimeout(CachedPool, 3, "RefreshAllLeaderboards");
            Terminal.UpdateConsole();
            RefreshAllBanlist();
            Main.ClearTempFiles();
        }
    }
    public synchronized static void RefreshAllClanlists() {
        Clanlist CL = new Clanlist(null);
        System.out.println("[Setup] Refresh clan list...");
        ExecutorService CachedPool = Executors.newFixedThreadPool(20);
        for (DatabaseObject.Row TR : getClanlistChannels()) {
            CachedPool.execute(() -> {
                ChannelMessage CM = new ChannelMessage(TR.getAsLong("ServerID"), TR.getAsLong("ChannelID"), TR.getAsLong("MessageID"), TR.getAsLong("MessageID2"));
                if (CM.getGuild() != null && CM.getChannel() != null && hasPermissionInChannel(null, CM.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_HISTORY, Permission.MANAGE_WEBHOOKS)) {
                    ServerInfo I = ServerInfo.get(CM.ServerID);
                    CM.ModifyWebhookMessageElseCreate(I.getServerWebhook().addEmbeds(parseEmbedBuilders(isPowerDisabled(CM.ServerID) ? new Clanlist(CM.ServerID, null).getEmbed(I).build() : CL.getEmbed(I).build())), msg -> {
                        if (!Objects.equals(TR.getAsLong("MessageID"), msg.getId())) {
                            I.Channels().ClanlistMessageID = msg.getId();
                            I.Channels().UpdateOnly("ClanlistMessageID");
                        }
                    });
                } else if (!Prefs.TestMode) {
                    ServerInfo I = ServerInfo.get(CM.ServerID);
                    I.Channels().ClanlistChannelID = null;
                    I.Channels().ClanlistMessageID = null;
                    I.Channels().ClanlistMessageID2 = null;
                    I.Channels().UpdateOnly("ClanlistChannelID", "ClanlistMessageID", "ClanlistMessageID2");
                }
            });
        }
        ShutdownWithTimeout(CachedPool, 3, "RefreshAllClanlists");
        Terminal.UpdateConsole();
    }
    public synchronized static void RefreshAllClanMembers(List<Clan> CLS) {
        if (Instant.now().isAfter(Instant.ofEpochSecond(Constants.RefreshCooldownOfClanRoleRefresh))) {
            Constants.RefreshCooldownOfClanRoleRefresh = Instant.now().plus(30, ChronoUnit.SECONDS).getEpochSecond();
            System.out.println("[Setup] Refresh clan members...");
            ExecutorService CachedPool = Executors.newFixedThreadPool(5);
            for (Clan clan : CLS) clan.getClanMembers();
            for (ServerInfo I : getClanRolesAndTagServers()) {
                if (I.getGuild() != null) CachedPool.execute(() -> {
                    RefreshGuildClanRole(I, CLS);
                });
            }
            ShutdownWithTimeout(CachedPool, 10, "RefreshAllClanMembers");
            Terminal.UpdateConsole();
        }
    }
    public static void RefreshGuildClanRole(ServerInfo I, List<Clan> CLS) {
        for (Clan clan : CLS) {
            if (clan.areMembersInGuild(I.getGuild())) {
                Role R = clan.getRole(I);
                for (ClanMember m : clan.getClanMembers()) {
                    if (m.isReinforcement()) continue;
                    Member M = I.getGuild().getMemberById(m.getUserID());
                    I.Roles().AddRoleToMember(R, clan.getEmojiFormatted(), M);
                    if (m.hasClanTag) clan.AddTagToMember(I, M);
                }
                I.Roles().AddRoleToMember(I.Roles().getClanCaptainRole(), Emoji.fromUnicode("U+1f935").getFormatted(), I.getGuild().getMemberById(clan.getCaptain().UserID));
                if (R != null) for (Member M : I.getGuild().getMembersWithRoles(R)) {
                    if (clan.getClanMembers().stream().noneMatch(cm -> (cm.ID == M.getIdLong() || cm.UserID == M.getIdLong()))) {
                        I.Roles().RemoveRoleFromMember(R, clan.getEmojiFormatted(), M);
                        clan.RemoveTagFromMember(I, M);
                    }
                }
                I.Roles().setRoleColor(R, clan.getColor(), clan.getEmojiFormatted());
                if (clan.hasEmblem()) I.Roles().setRoleIcon(R, Icon.from(clan.getEmblem()), clan.getEmojiFormatted(), false);
            }
        }
    }

    public synchronized static void RefreshAllRankMembers() {
        if (Instant.now().isAfter(Instant.ofEpochSecond(Constants.RefreshCooldownOfRankRoleRefresh))) {
            Constants.RefreshCooldownOfRankRoleRefresh = Instant.now().plus(30, ChronoUnit.SECONDS).getEpochSecond();
            System.out.println("[Setup] Refresh ranked members...");
            ExecutorService CachedPool = Executors.newFixedThreadPool(30);
            List<League.League_Tier> tiers = League.League_Tier.list().stream().filter(t -> !t.getName().equals("Rookie")).toList();

            Map<League.League_Tier, List<DatabaseObject.Row>> LMap = new HashMap<>();
            Map<Game, Map<League.League_Tier, List<Profile_Game>>> GMap = new HashMap<>();
            for (League.League_Tier Tier : tiers)
                LMap.put(Tier, doQueryAll("SELECT UserID, SUM(Medals) AS 'Medals' FROM inazuma_competitive.profile_game GROUP BY UserID HAVING ? <= Medals AND Medals <= ?", Tier.getStart(), Tier.getEnd()));

            for (Game G : Game.values()) {
                Map<League.League_Tier, List<Profile_Game>> GM = new HashMap<>();
                for (League.League_Tier Tier : tiers) {
                    GM.put(Tier, getAllWhere(Profile_Game.class, "? <= Medals AND Medals <= ? AND GameCode = ?", Tier.getStart(), Tier.getEnd(), G.getCode()));
                }
                GMap.put(G, GM);
            }



            for (ServerInfo I : getRankRolesServers()) {
                if (I.getGuild() != null) CachedPool.execute(() -> {
                    for (League.League_Tier Tier : I.Ranking().hasPrivateLeagues() ? I.Ranking().getTiers() : tiers) {
                        if (Tier.getStart() <= 0) continue;
                        if (I.areGlobalRankAllowed) {
                            Role R = I.Roles().getLeagueRole(Tier);
                            for (DatabaseObject.Row PT : I.Ranking().hasPrivateRanking() ? doQueryAll("SELECT UserID, SUM(Medals) AS 'Medals' FROM inazuma_competitive.profile_game_s WHERE ServerID = ? GROUP BY UserID HAVING ? <= Medals AND Medals <= ?", I.getID(), Tier.getStart(), Tier.getEnd()) : LMap.get(Tier)) {
                                Member M = I.getGuild().getMemberById(PT.getAsLong("UserID"));
                                I.Roles().AddRoleToMember(R, Tier.getTierEmojiFormatted(), M);
                            }
                            if (R != null) for (Member M : I.getGuild().getMembersWithRoles(R)) {
                                League PT = I.Ranking().hasPrivateRanking() ? Profile_Total.get(M.getIdLong()).getLeague(I.getID()) : Profile_Total.get(M.getIdLong()).getLeague();
                                if (PT.getTier().getId() != Tier.getId()) {
                                    I.Roles().RemoveRoleFromMember(R, Tier.getTierEmojiFormatted(), M);
                                }
                            }
                            I.Roles().setRoleIcon(R, Tier.getTierIcon(), Tier.getTierEmojiFormatted(), false);
                        }
                        for (Game G : Game.values()) {
                            if (I.isGameRankAllowed(G)) {
                                Role R = I.Roles().getLeagueRole(Tier, G);
                                for (BasePG<?> PT : I.Ranking().hasPrivateRanking() ? getAllWhere(Profile_Game_S.class, "ServerID = ? AND ? <= Medals AND Medals <= ? AND GameCode = ?", I.getID(), Tier.getStart(), Tier.getEnd(), G.getCode()) : GMap.get(G).get(Tier)) {
                                    Member M = I.getGuild().getMemberById(PT.getUserID());
                                    I.Roles().AddRoleToMember(R, Tier.getTierEmojiFormatted(), M);
                                }
                                if (R != null) for (Member M : I.getGuild().getMembersWithRoles(R)) {
                                    BasePG<?> PT = I.Ranking().hasPrivateRanking() ? Profile_Game_S.get(M.getIdLong(), I.getID(), G) : Profile_Game.get(M.getIdLong(), G);
                                    if (PT.getLeague().getTier().getId() != Tier.getId()) {
                                        I.Roles().RemoveRoleFromMember(R, Tier.getTierEmojiFormatted(), M);
                                    }
                                }
                                I.Roles().setRoleIcon(R, Tier.getTierIcon(), Tier.getTierEmojiFormatted(), false);
                            }
                        }
                    }
                });
            }
            ShutdownWithTimeout(CachedPool, 3, "RefreshAllRankMembers");
            Terminal.UpdateConsole();
        }
    }
    public synchronized static void RefreshAllBlacklists() {
        System.out.println("[Setup] Refresh blacklists...");
        ExecutorService CachedPool = Executors.newFixedThreadPool(20);
        for (DatabaseObject.Row TR : getBlacklistChannels()) {
            CachedPool.execute(() -> {
                ChannelMessage CM = new ChannelMessage(TR.getAsLong("ServerID"), TR.getAsLong("ChannelID"), TR.getAsLong("MessageID"));
                if (CM.getGuild() != null && CM.getChannel() != null && hasPermissionInChannel(null, CM.getChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_HISTORY, Permission.MANAGE_WEBHOOKS)) {
                    ServerInfo I = ServerInfo.get(CM.ServerID);
                    CM.ModifyWebhookMessageElseCreate(I.getServerWebhook().addEmbeds(parseEmbedBuilders(I.getBlacklistMessage().setFooter(" • " + TLG(I,"updated-on-time", getClock() + " (GMT+2)"), I.getGuild().getIconUrl()).build())), msg -> {
                        if (!Objects.equals(TR.getAsLong("MessageID"), msg.getId())) {
                            I.Channels().BlacklistMessageID = msg.getId();
                            I.Channels().UpdateOnly("BlacklistMessageID");
                        }
                    });

                } else if (!Prefs.TestMode) {
                    ServerInfo I = ServerInfo.get(CM.ServerID);
                    I.Channels().BlacklistChannelID = null;
                    I.Channels().BlacklistMessageID = null;
                    I.Channels().UpdateOnly("BlacklistChannelID", "BlacklistMessageID");
                }
            });
        }
        ShutdownWithTimeout(CachedPool, 5, "RefreshAllBlacklists");
        Terminal.UpdateConsole();
    }
    public synchronized static void RefreshAllPrivateCMDs() {
        System.out.println("[Setup] Refresh clan servers...");
        ExecutorService CachedPool = Executors.newFixedThreadPool(20);
        for (ServerInfo I : getServersNeedingCommands()) {
            if (I.getGuild() != null && I.getID() != 930718276542136400L) CachedPool.execute(() -> {
                try {
                    List<CommandData> GuildCmd = new ArrayList<>();
                    try {
                        Clan clan = getWhere(Clan.class, "ClanServerID = ?", I.getID()).orElse(null);
                        if (clan != null) {
                            String tag = "";
                            for (char c : RemoveNumbers(CharFix(clan.Tag)).toCharArray()) {
                                if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                                    tag = tag + c;
                                }
                            }
                            if (tag.isEmpty()) tag = "c";
                            GuildCmd.add(Commands.user("[" + tag + "] Add"));
                            GuildCmd.add(Commands.user("[" + tag + "] Kick"));
                            GuildCmd.add(Commands.user("[" + tag + "] Manage"));

                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-info_channel", "Set the auto-update info channel of " + clan.getName() + " clan.")
                                    .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to place the info channel.", true).setChannelTypes(ChannelType.TEXT)));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_name", "Change the name of " + clan.getName() + " clan."));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_nationality", "Change the nationality of " + clan.getName() + " clan."));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_tag", "Change the tag of " + clan.getName() + " clan."));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_birthday", "Change the creation date of " + clan.getName() + " clan."));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_colorcode", "Change the colorcode of " + clan.getName() + " clan."));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_description", "Change the description of " + clan.getName() + " clan."));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_requirements", "Change the join requirements of " + clan.getName() + " clan."));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-edit-license", "Edit the license of " + clan.getName() + " clan."));
                            GuildCmd.add(Commands.slash(tag.toLowerCase() + "-info", "View the information of the " + clan.getName() + " clan only."));
                            if (clan.getName().contains("Heaven Chaos")) {
                                OptionData soundfile = new OptionData(OptionType.ATTACHMENT, "sound-file", "Upload a MP3, WAV or OGG sound file.", true);
                                OptionData repeat = new OptionData(OptionType.INTEGER, "repeat-times", "Amount of times the song will repeat.", false);
                                GuildCmd.add(Commands.slash("olé-samba", "Play a music ! Olé Sambaaaa !").addOptions(soundfile, repeat));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        if (I.Ranking().hasPrivateLeagues()) {
                            List<Command.Choice> L = I.Ranking().getTiers().stream().map(T -> new Command.Choice(T.getName() + " (" + T.getStart() + "-" + T.getEnd() + ")", T.getId() + "")).toList();
                            GuildCmd.add(Commands.slash("adm-new_tier", "Create a new league tier for this server.")
                                    .addOptions(new OptionData(OptionType.STRING, "name", "Name of the tier.", true),
                                            new OptionData(OptionType.STRING, "emojiid", "The emoji id of the tier.", true),
                                            new OptionData(OptionType.STRING, "colorcode", "The colorcode of this tier. eg.#FFFFFF", true),
                                            new OptionData(OptionType.INTEGER, "start-medals", "The start of this tier, for eg. 1000 for 1000->1999", true),
                                            new OptionData(OptionType.INTEGER, "end-medals", "The start of this tier, for eg. 1999 for 1000->1999", true),
                                            new OptionData(OptionType.ATTACHMENT, "icon-image", "The image of the tier.", true)));
                            if (!L.isEmpty()) {
                                GuildCmd.add(Commands.slash("adm-new_league", "Create a new league rank for a tier..")
                                        .addOptions(new OptionData(OptionType.STRING, "tier", "The tier of this league.", true).addChoices(L),
                                                new OptionData(OptionType.STRING, "name", "Name of the league.", true),
                                                new OptionData(OptionType.STRING, "emojiid", "The emoji id of the league.", true),
                                                new OptionData(OptionType.INTEGER, "start-medals", "The start of this league, for eg. 200 for 200->399", true),
                                                new OptionData(OptionType.INTEGER, "end-medals", "The start of this league, for eg. 399 for 200->399", true),
                                                new OptionData(OptionType.ATTACHMENT, "icon-image", "The image of the league.", true),
                                                new OptionData(OptionType.NUMBER, "private-power", "Power given to players for this server only if private power is enabled.", false)));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        List<SChallonge_Tournament> Tournaments = I.getActiveChallonges();
                        if (!Tournaments.isEmpty()) {
                            OptionData tournament = new OptionData(OptionType.STRING, "tournament", "The tournament you are targeting.", true);
                            for (SChallonge_Tournament T : Tournaments) {
                                T.ResyncChallonge();
                                T.RefreshPanelMessage();
                                T.RefreshInscriptionMessage();
                                tournament.addChoice(T.getName().replace("Inazuma Eleven", "IE"), T.getId() + "");
                            }
                            GuildCmd.add(Commands.slash("dq", "Disqualify you or someone else from the current tournament of this server.")
                                    .addOptions(tournament, new OptionData(OptionType.STRING, "reason", "Reason for withdrawal.", true).setRequiredLength(1, 128)
                                            , new OptionData(OptionType.USER, "player", "The player to be DQed. Only TOs can select other people than themselves.", false)));
                        }
                    } catch (Exception ignored) {}
                    try {
                        I.getGuild().updateCommands().addCommands(GuildCmd).queue(commands -> {
                        }, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, error -> {
                            System.out.println("Missing Access in: [" + I.getName() + "/" + I.getID() + "]");
                        }));
                    } catch (ErrorResponseException e) {
                        handleException(e);
                    }
                } catch (Exception ignored) {}
            });
        }
        CachedPool.execute(() -> AddAdminCommands(930718276542136400L));
        CachedPool.execute(() -> AddAdminCommands(594809107631046656L));
        ShutdownWithTimeout(CachedPool, 1, "RefreshClanServers");
        Terminal.UpdateConsole();
    }
    public synchronized static void RefreshAllWinnerRoles() {
        System.out.println("[Setup] Refresh winner roles...");
        ExecutorService CachedPool = Executors.newFixedThreadPool(20);
        for (DatabaseObject.Row TR : getWinnerRolesServ()) {
            CachedPool.execute(() -> {
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ServerID"));
                List<DatabaseObject.Row> Tops = doQueryAll("SELECT * FROM DisplayTopsRanking(?)", G.getId());
                for (DatabaseObject.Row Top : Tops) {
                    Member M = G.getMemberById(Top.getAsInt("DiscordID"));
                    if (M != null) {
                        if (Top.getAsInt("T1") >= 5) {
                            Role R = G.getRoleById(TR.getAsLong("WinnerRoleID3"));
                            if (R != null && !M.getRoles().contains(R)) {
                                G.addRoleToMember(M, R).queue(r -> {
                                    LogSlash(G, TLG(G, "role-add-success", ":medal: **" + R.getName() + "**", "**" + M.getEffectiveName() + "**"));
                                });
                            }
                        } else if (Top.getAsInt("T1") >= 3) {
                            Role R = G.getRoleById(TR.getAsLong("WinnerRoleID2"));
                            if (R != null && !M.getRoles().contains(R)) {
                                G.addRoleToMember(M, R).queue(r -> {
                                    LogSlash(G, TLG(G, "role-add-success", ":medal: **" + R.getName() + "**", "**" + M.getEffectiveName() + "**"));
                                });
                            }
                        } else if (Top.getAsInt("T1") >= 1) {
                            Role R = G.getRoleById(TR.getAsLong("WinnerRoleID1"));
                            if (R != null && !M.getRoles().contains(R)) {
                                G.addRoleToMember(M, R).queue(r -> {
                                    LogSlash(G, TLG(G, "role-add-success", ":medal: **" + R.getName() + "**", "**" + M.getEffectiveName() + "**"));
                                });
                            }
                        }
                    }
                }
            });
        }
        ShutdownWithTimeout(CachedPool, 5, "RefreshWinnerRoles");
        Terminal.UpdateConsole();
    }
    public synchronized static void RefreshAllBanlist() {
        System.out.println("[Setup] Refresh banlist roles...");
        ExecutorService CachedPool = Executors.newFixedThreadPool(20);
        Criminal.list(true);
        for (DatabaseObject.Row TR : doQueryAll("SELECT ID, ToleranceLevel FROM inazuma_competitive.serverinfo WHERE MemberCount >= ?", 50)) {
            CachedPool.execute(() -> {
                Guild G = DiscordAccount.getGuildById(TR.getAsLong("ID"));
                if (G != null) {
                    int Tolerance = TR.getAsInt("ToleranceLevel");
                    for (Criminal C : Criminal.list(false)) {
                        if (C.getDangerLevel() > Tolerance) {
                            Member M = G.getMemberById(C.getUserID());
                            if (M != null) {
                                LogSlash(G,":warning: You have a community banned member on your server: " + M.getAsMention() + "\n" + "> **Offenses:** " + C.getReason());
                                if (!M.isTimedOut() && G.getSelfMember().hasPermission(Permission.MODERATE_MEMBERS) && G.getSelfMember().canInteract(M)) M.timeoutFor(28, TimeUnit.DAYS).queue();
                            }
                        }
                    }
                }
            });
        }
        ShutdownWithTimeout(CachedPool, 5, "RefreshAllBanlist");
        Terminal.UpdateConsole();
    }

    private static void AddAdminCommands(long serverId) {
        try {
            ServerInfo I = ServerInfo.get(serverId);
            if (I.getGuild() != null) {
                List<CommandData> GuildCmd = new ArrayList<>();
                GuildCmd.add(Commands.slash("mdt-new_interclan", "Add a new interclan result.").addOptions(
                        new OptionData(OptionType.STRING, "clan-1", "Select a clan for host clan.", true),
                        new OptionData(OptionType.STRING, "clan-2", "Select a clan for join clan.", true)));
                GuildCmd.add(Commands.slash("mdt-add_reward", "Add a new interclan result.").addOptions(
                        new OptionData(OptionType.USER, "user", "User given.", true),
                        new OptionData(OptionType.STRING, "emoji", "Emoji given.", true),
                        new OptionData(OptionType.STRING, "name", "Name given.", true),
                        new OptionData(OptionType.STRING, "description", "Description given.", true),
                        getGames(true).addChoice("All", "All"),
                        new OptionData(OptionType.STRING, "power", "Power given.", true)));
                GuildCmd.add(Commands.slash("mdt-challonge_refresh", "Refresh a server.").addOptions(
                        new OptionData(OptionType.STRING, "server", "Challonge ID.", true),
                        new OptionData(OptionType.STRING, "serverid", "Server ID.", true)));
                GuildCmd.add(Commands.slash("mdt-cooldown_remove", "Clear all cooldowns of a user.").addOptions(
                        new OptionData(OptionType.USER, "user", "Select a user.", true)));

                GuildCmd.add(Commands.slash("idl-start", "Start the league."));
                GuildCmd.add(Commands.slash("idl-complete", "Complete the league."));
                GuildCmd.add(Commands.slash("idl-manage", "Start the league."));

                GuildCmd.add(Commands.slash("img-simulate_score", "Simulate a score.").addOptions(
                        new OptionData(OptionType.USER, "user-1", "User 1.", true),
                        new OptionData(OptionType.INTEGER, "score-1", "Score 1.", true),
                        new OptionData(OptionType.INTEGER, "score-2", "Score 2.", true),
                        new OptionData(OptionType.USER, "user-2", "User 2.", true),
                        getGames(true)));
                GuildCmd.add(Commands.slash("img-batch-convert", "Refresh boards.")
                        .addOptions(new OptionData(OptionType.ATTACHMENT, "image", "The image.", true)));
                GuildCmd.add(Commands.slash("img-circle", "Make a circle."));



                GuildCmd.add(Commands.slash("mdc-clan_card", "Make a card for a clan.").addOptions(clans,
                        new OptionData(OptionType.USER, "user", "Select a user.", true)));
                GuildCmd.add(Commands.slash("mdc-clan_news", "Send a clan news to all servers").addOptions(clans,
                        new OptionData(OptionType.USER, "user", "Select a user.", true),
                        new OptionData(OptionType.BOOLEAN, "is-reinforcement", "Is reinforcement?", true),
                        new OptionData(OptionType.STRING, "type", "The type.", true)
                                .addChoice("New Member", "New Member")
                                .addChoice("Lose Member", "Lose Member")));


                GuildCmd.add(Commands.slash("mdf-changelog", "Send changelog.").addOptions(
                        new OptionData(OptionType.STRING, "feature-1", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-2", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-3", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-4", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-5", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-6", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-7", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-8", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-9", "Features.", false),
                        new OptionData(OptionType.STRING, "feature-10", "Features.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-1", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-2", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-3", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-4", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-5", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-6", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-7", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-8", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-9", "Bug fixes.", false),
                        new OptionData(OptionType.STRING, "bug-fixes-10", "Bug fixes.", false)));
                GuildCmd.add(Commands.slash("mdf-announcement", "Send announcement.").addOptions(
                        new OptionData(OptionType.STRING, "announcement", "The announcement.", false)));
                GuildCmd.add(Commands.slash("mdf-refresh", "Refresh boards.")
                        .addOptions(new OptionData(OptionType.STRING, "data-type", "What data to refresh?", true)
                                .addChoice("Tournaments", "Tournaments")
                                .addChoice("Server Roles", "Server Roles")
                                .addChoice("Guilds Data", "Guilds Data")
                                .addChoice("Leaderboards", "Leaderboards")
                                .addChoice("Remind Scores", "Remind Scores")
                                .addChoice("Clean", "Clean")));
                GuildCmd.add(Commands.slash("mdf-list_data", "Send a list of data").addOptions(
                        new OptionData(OptionType.STRING, "data-type", "Data type you are looking for.", true)
                                .addChoice("Tournaments", "Tournaments")
                                .addChoice("Interclans", "Interclans")
                                .addChoice("Guilds", "Guilds")
                                .addChoice("Emojis", "Emojis")
                                .addChoice("Roles", "Roles")
                                .addChoice("Channels", "Channels")
                                .addChoice("Birthdays", "Birthdays"),
                        new OptionData(OptionType.STRING, "specific-guildid", "Any specific server?", false)));
                GuildCmd.add(Commands.slash("mdf-shutdown", "Shut down the bot."));

                OptionData activity = new OptionData(OptionType.STRING, "activity", "What activity ?", true);
                activity.addChoice("Playing", "Playing");
                activity.addChoice("Listening", "Listening");
                activity.addChoice("Watching", "Watching");
                activity.addChoice("Competing", "Competing");
                activity.addChoice("Streaming", "Streaming");

                GuildCmd.add(Commands.slash("mdf-status", "Set the bot status.").addOptions(activity
                        , new OptionData(OptionType.STRING, "status", "What is the status ?", true)
                        , new OptionData(OptionType.STRING, "url", "Only for streaming.", false)));
                GuildCmd.add(Commands.slash("mdf-nickname", "Set the bot nickname.").addOptions(
                        new OptionData(OptionType.USER, "user", "Select a user.", true),
                        new OptionData(OptionType.STRING, "guildid", "The guild where you want to rename him.", true)
                        , new OptionData(OptionType.STRING, "nickname", "The nickname.", true)));
                GuildCmd.add(Commands.slash("mdf-send_dm", "Sends a dm to someone.").addOptions(
                        new OptionData(OptionType.USER, "user", "Select a user.", true),
                        new OptionData(OptionType.STRING, "text", "What to send?", true)));
                GuildCmd.add(Commands.slash("mdf-react_message", "Send a reaction to a message").addOptions(
                        new OptionData(OptionType.STRING, "emoji", "What to send?", true),
                        new OptionData(OptionType.STRING, "guildid", "Enter guild id.", true),
                        new OptionData(OptionType.STRING, "channelid", "Enter channel id.", true),
                        new OptionData(OptionType.STRING, "messageid", "Enter message id of which the bot is reacting.", true)));
                GuildCmd.add(Commands.slash("mdf-invite_serv", "Create an invite to join a server.").addOptions(
                        new OptionData(OptionType.STRING, "guildid", "The guild which you want to join.", true)));
                GuildCmd.add(Commands.slash("mdf-leave_serv", "Leave a server.").addOptions(
                        new OptionData(OptionType.STRING, "guildid", "The guild which you want to leave.", true)));

                GuildCmd.add(Commands.slash("mdf-url_test", "Test a url.").addOptions(
                        new OptionData(OptionType.STRING, "url", "The URL", true)));
                GuildCmd.add(Commands.slash("mdf-super_timeout", "Gives a SUPER TIMEOUT of 1 month to someone.").addOptions(
                        new OptionData(OptionType.STRING, "guildid", "Enter guild id.", true),
                        new OptionData(OptionType.STRING, "time", "Enter time.", true)
                                .addChoice("1 Minutes", "1M")
                                .addChoice("5 Minutes", "5M")
                                .addChoice("1 Days", "1D")
                                .addChoice("7 Days", "7D")
                                .addChoice("28 Days", "28D"),
                        new OptionData(OptionType.USER, "user", "Select a user.", true)));
                GuildCmd.add(Commands.slash("mdf-untimeout", "Gives a SUPER TIMEOUT of 1 month to someone.").addOptions(
                        new OptionData(OptionType.STRING, "guildid", "Enter guild id.", true),
                        new OptionData(OptionType.USER, "user", "Select a user.", true)));
                GuildCmd.add(Commands.slash("mdf-points_calc", "Get points per participants.").addOptions(
                        new OptionData(OptionType.INTEGER, "participants", "Enter participants.", true)));
                GuildCmd.add(Commands.slash("mdf-perms", "Check which server has bad perms."));
                GuildCmd.add(Commands.slash("mdf-delete_message", "Deletes a message in a channel.").addOptions(
                        new OptionData(OptionType.STRING, "message", "The message's link.", true)));

                try {
                    Clan clan = getWhere(Clan.class, "ClanServerID = ?", I.getID()).orElse(null);
                    if (clan != null) {
                        String tag = "";
                        for (char c : RemoveNumbers(CharFix(clan.Tag)).toCharArray()) {
                            if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                                tag = tag + c;
                            }
                        }
                        if (tag.isEmpty()) tag = "c";
                        GuildCmd.add(Commands.user("[" + tag + "] Add"));
                        GuildCmd.add(Commands.user("[" + tag + "] Kick"));
                        GuildCmd.add(Commands.user("[" + tag + "] Manage"));

                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-info_channel", "Set the auto-update info channel of " + clan.getName() + " clan.")
                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to place the info channel.", true).setChannelTypes(ChannelType.TEXT)));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_name", "Change the name of " + clan.getName() + " clan."));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_nationality", "Change the nationality of " + clan.getName() + " clan."));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_tag", "Change the tag of " + clan.getName() + " clan."));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_birthday", "Change the creation date of " + clan.getName() + " clan."));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_colorcode", "Change the colorcode of " + clan.getName() + " clan."));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_description", "Change the description of " + clan.getName() + " clan."));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-set_requirements", "Change the join requirements of " + clan.getName() + " clan."));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-edit-license", "Edit the license of " + clan.getName() + " clan."));
                        GuildCmd.add(Commands.slash(tag.toLowerCase() + "-info", "View the information of the " + clan.getName() + " clan only."));
                        if (clan.getName().contains("Heaven Chaos")) {
                            OptionData soundfile = new OptionData(OptionType.ATTACHMENT, "sound-file", "Upload a MP3, WAV or OGG sound file.", true);
                            OptionData repeat = new OptionData(OptionType.INTEGER, "repeat-times", "Amount of times the song will repeat.", false);
                            GuildCmd.add(Commands.slash("olé-samba", "Play a music ! Olé Sambaaaa !").addOptions(soundfile, repeat));
                        }
                    }
                } catch (Exception ignored) {}

                try {
                    if (I.Ranking().hasPrivateLeagues()) {
                        List<Command.Choice> L = I.Ranking().getTiers().stream().map(T -> new Command.Choice(T.getName() + " (" + T.getStart() + "-" + T.getEnd() + ")", T.getId() + "")).toList();
                        GuildCmd.add(Commands.slash("adm-new_tier", "Create a new league tier for this server.")
                                .addOptions(new OptionData(OptionType.STRING, "name", "Name of the tier.", true),
                                        new OptionData(OptionType.STRING, "emojiid", "The emoji id of the tier.", true),
                                        new OptionData(OptionType.STRING, "colorcode", "The colorcode of this tier. eg.#FFFFFF", true),
                                        new OptionData(OptionType.INTEGER, "start-medals", "The start of this tier, for eg. 1000 for 1000->1999", true),
                                        new OptionData(OptionType.INTEGER, "end-medals", "The start of this tier, for eg. 1999 for 1000->1999", true),
                                        new OptionData(OptionType.ATTACHMENT, "icon-image", "The image of the league.", true)));
                        if (!L.isEmpty()) {
                            GuildCmd.add(Commands.slash("adm-new_league", "Create a new league rank for a tier..")
                                    .addOptions(new OptionData(OptionType.STRING, "tier", "The tier of this league.", true).addChoices(L),
                                            new OptionData(OptionType.STRING, "name", "Name of the league.", true),
                                            new OptionData(OptionType.STRING, "emojiid", "The emoji id of the league.", true),
                                            new OptionData(OptionType.INTEGER, "start-medals", "The start of this league, for eg. 200 for 200->399", true),
                                            new OptionData(OptionType.INTEGER, "end-medals", "The start of this league, for eg. 399 for 200->399", true),
                                            new OptionData(OptionType.NUMBER, "private-power", "Power given to players for this server only if private power is enabled.", false)));
                        }
                    }
                } catch (Exception ignored) {}

                try {
                    List<SChallonge_Tournament> Tournaments = I.getActiveChallonges();
                    if (!Tournaments.isEmpty()) {
                        OptionData tournament = new OptionData(OptionType.STRING, "tournament", "The tournament you are targeting.", true);
                        for (SChallonge_Tournament T : Tournaments) {
                            tournament.addChoice(T.getName().replace("Inazuma Eleven", "IE"), T.getId() + "");
                        }
                        GuildCmd.add(Commands.slash("dq", "Disqualify you or someone else from the current tournament of this server.")
                                .addOptions(tournament, new OptionData(OptionType.STRING, "reason", "Reason of withdrawal.", true).setRequiredLength(1, 128)
                                        , new OptionData(OptionType.USER, "player", "The player to be DQed. Only TOs can select other people than themselves.", false)));
                    }
                } catch (Exception ignored) {}

                I.getGuild().updateCommands().addCommands(GuildCmd).queue(commands -> {
                }, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, error -> {
                    System.out.println("Missing Access in: [" + I.getName() + "/" + I.getID() + "]");
                }));
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public static void RefreshPOTY() {
        Terminal.UpdateConsole();
        System.out.println("[Setup] Updating POTY in servers...");
        String servers = "";
        List<String> games = new ArrayList<>();
        List<String> games2 = new ArrayList<>();
        for (int TIMES = 0; TIMES <= 1; TIMES++) {
            Game G;
            if (TIMES == 0) {
                G = Game.get("IEVR");
            } else {
                G = Game.get("IEGOSTRXTR");
            }
            List<POTY> list = new ArrayList<>();
            for (ServerInfo I : ServerInfo.list()) {
                if (true) {
                    //if (!servers.contains(serverInfo.getName())) {
                    //    servers = servers + "- **" + serverInfo.getName() + "**\n";
                    //}
                    // for (TournamentResult T : serverInfo.getTournaments()) {
                    //                        if (!T.getName().contains("All-Stars")) {
                    //                            if (getYear(Instant.ofEpochSecond(PatternToEpochSecond(T.Date, "dd/MM/yyyy"))).equals(getYear(Instant.now()))) {
                    //                                if (T.getGame().equals(G)) {
                    //                                    boolean isNew1 = false;
                    //                                    boolean isNew2 = false;
                    //                                    boolean isNew3 = false;
                    //                                    double Multiplier = TournamentParticipantMultiplier(T.Participants);
                    //                                    for (POTY PP : list) {
                    //                                        if (PP.ID.equals(T.Top1)) {
                    //                                            PP.PTS = PP.PTS + (int) (300 * Multiplier);
                    //                                            isNew1 = true;
                    //                                        } else if (PP.ID.equals(T.Top2)) {
                    //                                            PP.PTS = PP.PTS + (int) (200 * Multiplier);
                    //                                            isNew2 = true;
                    //                                        } else if (PP.ID.equals(T.Top3)) {
                    //                                            PP.PTS = PP.PTS + (int) (100 * Multiplier);
                    //                                            isNew3 = true;
                    //                                        } else if (PP.ID.equals(T.SecondTop3)) {
                    //                                            PP.PTS = PP.PTS + (int) (100 * Multiplier);
                    //                                            isNew3 = true;
                    //                                        }
                    //                                    }
                    //                                    if (!isNew1) {
                    //                                        list.add(new POTY(T.Top1, (int) (300 * Multiplier)));
                    //                                    }
                    //                                    if (!isNew2) {
                    //                                        list.add(new POTY(T.Top2, (int) (200 * Multiplier)));
                    //                                    }
                    //                                    if (!isNew3) {
                    //                                        list.add(new POTY(T.Top3, (int) (100 * Multiplier)));
                    //                                    }
                    //                                    if (!isNew3) {
                    //                                        list.add(new POTY(T.SecondTop3, (int) (100 * Multiplier)));
                    //                                    }
                    //                                }
                    //                            }
                    //                        }
                    //                    }
                    //                    for (ChallongeTournament C : serverInfo.getChallonges(true)) {
                    //                        if (getYear(C.getCompletedAtTime()).equals(getYear(Instant.now()))) {
                    //                            if (C.getGame().equals(G)) {
                    //                                double Multiplier = TournamentParticipantMultiplier(C.getParticipantsCount());
                    //                                for (ChallongeTournament.ChallongeParticipant P : C.getParticipants()) {
                    //                                    boolean notFound = true;
                    //                                    if (!Objects.equals(P.getDiscordID(), "N/A")) {
                    //                                        for (POTY PP : list) {
                    //                                            if (P.getDiscordID().equals(PP.ID)) {
                    //                                                if (P.getPosition() == 1) {
                    //                                                    PP.PTS = PP.PTS + (int) (300 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() == 2) {
                    //                                                    PP.PTS = PP.PTS + (int) (200 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() == 3) {
                    //                                                    PP.PTS = PP.PTS + (int) (100 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() == 4) {
                    //                                                    PP.PTS = PP.PTS + (int) (75 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() <= 6) {
                    //                                                    PP.PTS = PP.PTS + (int) (50 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() <= 8) {
                    //                                                    PP.PTS = PP.PTS + (int) (25 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() <= 16) {
                    //                                                    PP.PTS = PP.PTS + (int) (20 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() <= 32) {
                    //                                                    PP.PTS = PP.PTS + (int) (15 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() <= 48) {
                    //                                                    PP.PTS = PP.PTS + (int) (10 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                } else if (P.getPosition() <= 64) {
                    //                                                    PP.PTS = PP.PTS + (int) (5 * Multiplier);
                    //                                                    notFound = false;
                    //                                                    break;
                    //                                                }
                    //                                            }
                    //                                        }
                    //                                        if (notFound) {
                    //                                            if (P.getPosition() == 1) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (300 * Multiplier)));
                    //                                            } else if (P.getPosition() == 2) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (200 * Multiplier)));
                    //                                            } else if (P.getPosition() == 3) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (100 * Multiplier)));
                    //                                            } else if (P.getPosition() == 4) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (75 * Multiplier)));
                    //                                            } else if (P.getPosition() <= 6) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (50 * Multiplier)));
                    //                                            } else if (P.getPosition() <= 8) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (25 * Multiplier)));
                    //                                            } else if (P.getPosition() <= 16) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (20 * Multiplier)));
                    //                                            } else if (P.getPosition() <= 32) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (15 * Multiplier)));
                    //                                            } else if (P.getPosition() <= 48) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (10 * Multiplier)));
                    //                                            } else if (P.getPosition() <= 64) {
                    //                                                list.add(new POTY(P.getDiscordID(), (int) (5 * Multiplier)));
                    //                                            }
                    //                                        }
                    //                                    }
                    //                                }
                    //                            }
                    //                        }
                    //                    }
                }
            }
            String name = "";
            String pts = "";
            if (!list.isEmpty()) {
                int n = list.size();
                for (int i = 0; i < n - 1; i++) {
                    for (int j = 0; j < n - i - 1; j++) {
                        if (list.get(j).PTS < list.get(j + 1).PTS) {
                            POTY temp = list.get(j);
                            list.set(j, list.get(j + 1));
                            list.set(j + 1, temp);
                        }
                    }
                }
                int i = 1;
                for (POTY PP : list) {
                    if (i <= 10) {
                        name = name + Profile.get(PP.ID).getNationality().getFlag().getFormatted() + " " + getUserByID(PP.ID).getEffectiveName() + "\n";
                        pts = pts + PP.PTS + "\n";
                        i++;
                    }
                }
            }
            games.add(name);
            games2.add(pts);
        }

        for (Guild guild : DiscordAccount.getGuilds()) {
            EmbedBuilder[] embeds = new EmbedBuilder[9];
            FileUpload[] uploads = new FileUpload[9];
            embeds[0] = new EmbedBuilder()
                    .setTitle("POTY - Player Of The Year")
                    .setColor(Color.ORANGE)
                    .setThumbnail("attachment://BallonDor.png")
                    .setDescription("Every 1st January the ranking will reset. Everyone has a chance to reach the top throughout the year from tournaments organised by the servers below."
                            + "\n"+ "\n" + "The servers which tournaments counts:"
                            + "\n" + servers
                            + "\n" + "The race will end: <t:" + DateHourToEpochSecond("31/12/" + Year.now() + " - 23:59") + ":R>"
                            + "\n" + "Below you will find the current champions:");
            try (InputStream is = Main.class.getClassLoader().getResourceAsStream("img/BallonDor.png")) {
                uploads[0] = FileUpload.fromData(is, "BallonDor.png");
            } catch (Exception ignored) {}
            String N = "**1 :star2:\n"
                    + "2\n"
                    + "3\n"
                    + "4\n"
                    + "5\n"
                    + "6\n"
                    + "7\n"
                    + "8\n"
                    + "9\n"
                    + "10**";

            embeds[1] = new EmbedBuilder()
                    .setTitle("Victory Road POTY")
                    .setColor(Game.get("VR").getColor())
                    .setThumbnail(Game.get("VR").getImageUrl())
                    .addField("Position", N, true)
                    .addField("Name", games.getFirst(), true)
                    .addField("Points", games2.getFirst(), true);


            embeds[2] = new EmbedBuilder()
                    .setTitle("Xtreme POTY")
                    .setColor(Game.get("Xtreme").getColor())
                    .setThumbnail(Game.get("Xtreme").getImageUrl())
                    .addField("Position", N, true)
                    .addField("Name", games.get(1), true)
                    .addField("Points", games2.get(1), true);

            //
            //            embeds[3] = new EmbedBuilder()
            //                    .setTitle("Chrono Stones POTY")
            //                    .setColor(Games.CS.getImageUrl())
            //                    .setThumbnail(Games.CS.getImageUrl())
            //                    .addField("Position", N, true)
            //                    .addField("Name", games.get(2), true)
            //                    .addField("Points", games2.get(2), true);
            //            uploads[3] = FileUpload.fromData(Games.CS.getLogo(), "cs.png");
        }
    }
    public static void RemindScores() {
        System.out.println("[Setup] Reminding scores...");
        Terminal.UpdateConsole();
        for (MatchLog R : MatchLog.getUncomplete()) {
            try {
                if (Instant.ofEpochMilli(R.getId()).isBefore(Instant.now().minus(7, ChronoUnit.DAYS))) R.Delete();
                getMessage(DiscordAccount.getGuildById(R.getServerID()).getTextChannelById(R.getChannelID()), R.getMessageID()).getId();
                sendPrivateMessage(R.getProfileP2().getUser(), TL(R.getProfileP2(),"score-reminder", "**" + R.getProfileP1().getUser() + "**", "https://discord.com/channels/" + R.getServerID() + "/" + R.getChannelID() + "/" + R.getMessageID()));
            } catch (Exception e) {
                R.Delete();
            }
        }
    }

    public static void setBotVariables() {
        if (Constants.BotStaffGuild == null || Constants.LogChannel == null) {
            UpdateStaffs();
            Constants.BotStaffGuild = DiscordAccount.getGuildById("930718276542136400");
            Constants.allGame = new ArrayList<>();
            Constants.allGame = Game.values();
            Constants.GlobalLeagues = new ArrayList<>();
            Constants.GlobalLeagues = League.listGlobal();
            Constants.GlobalLeagueTiers = new ArrayList<>();
            Constants.GlobalLeagueTiers = League.League_Tier.list();
            Constants.GlobalRanking = new ServerInfo.ServerInfo_Ranking(50,5,-25,-1,1,5);
            if (Constants.BotStaffGuild != null) {
                Constants.ClanRequestChannel = Constants.BotStaffGuild.getTextChannelById("1137461804822442085");
                Constants.LogChannel = Constants.BotStaffGuild.getTextChannelById("1121096901681483807");
                Constants.CardImageChannel = Constants.BotStaffGuild.getTextChannelById("1292977552700346398");
                Constants.ClanEmblemChannel = Constants.BotStaffGuild.getTextChannelById("1285728179989774489");
                Constants.TempChannel = Constants.BotStaffGuild.getTextChannelById("1102661048269541406");
                Constants.Battery = Constants.BotStaffGuild.getVoiceChannelById("1116378323333959712");
            }
            Constants.awards = new OptionData(OptionType.STRING, "badge", "The badge to add.", true);
            Constants.clans = new OptionData(OptionType.STRING, "clan", "The clan you are looking for. Select full name or enter an abbreviation.", true);
            Constants.interclans = new OptionData(OptionType.STRING, "interclan", "The interclan to select.", true);
            Constants.opposingclan = new OptionData(OptionType.STRING, "opposing-clan", "The clan you are challenging.", true);
            Constants.groupname = new OptionData(OptionType.STRING, "group-name", "The name of the group (less than 12 characters).", true);
            Constants.teamname = new OptionData(OptionType.STRING, "team-name", "The name of the team.", true);
            Constants.eventnames = new OptionData(OptionType.STRING, "event", "The event you want to select.", true);
            Constants.matches = new OptionData(OptionType.STRING, "match", "The match you want to select.", true);
         }
    }
    public static void CheckForBirthday() {
        for (Profile P : getAllWhere(Profile.class,"""
                (DAY(FROM_UNIXTIME(BirthdayEpochSecond))+1) = DAY(CURDATE())
                AND MONTH(FROM_UNIXTIME(BirthdayEpochSecond)) = MONTH(CURDATE())
                AND UNIX_TIMESTAMP() > BirthdayTimeoutEpochSecond;
                """)) {
            try {
                P.RefreshBirthdayTimeout(Instant.now().plus(7200, ChronoUnit.HOURS));
                P.addItem(1, 500);
                P.addItem(2, 100);
                P.addItem(3002, 1);
                P.addItem(3004, 1);
                sendPrivateMessage(P.getUser(), ":birthday: " + TL(P,"happy-birthday") + "\n"
                        + "- **+500 " + Item.get(1).getEmojiFormatted() + "**\n"
                        + "- **+100 " + Item.get(2).getEmojiFormatted() + "**\n"
                        + "- **+1 " + Item.get(3002).getEmojiFormatted() + " " + Item.get(3002).getName() + "**\n"
                        + "- **+1 " + Item.get(3004).getEmojiFormatted() + " " + Item.get(3004).getName() + "**\n");
            } catch (Exception ignored) {}
        }
    }

    public static void ListAwards() {
        File theDirUser = new File(MainDirectory + "/storage/badges/");
        if (theDirUser.exists()) {
            for (File file : Objects.requireNonNull(theDirUser.listFiles())) {
                if (file.getName().contains(".txt")) {
                    Constants.awards.addChoice(file.getName().replace(".txt", ""), file.getName().replace(".txt", ""));
                }
            }
        }
    }
    public static void ListClans() {
        for (Clan clan : Clan.listOpenPaused()) {
            if (Constants.clans.getChoices().size() < 25) {
                Constants.clans.addChoice(CharFix(clan.getTag()) + " - " + clan.getName(), clan.getName());
            } else {
                Constants.clans = new OptionData(OptionType.STRING, "clan", "The clan you are looking for. Select full name or enter an abbreviation.", true);
                break;
            }
        }
        Constants.opposingclan.addChoices(Constants.clans.getChoices());
    }
    public static void ListInterclans() {
        try {
            for (Interclan IC : Interclan.listOnGoing()) {
                Constants.interclans.addChoice(IC.getHoster().getName() + " vs " + IC.getJoiner().getName(), IC.getId() + "");
            }
        } catch (NullPointerException ignored) {}
    }

}
