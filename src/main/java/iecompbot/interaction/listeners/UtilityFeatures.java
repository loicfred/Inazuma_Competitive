package iecompbot.interaction.listeners;

import iecompbot.Constants;
import iecompbot.interaction.music.PlayerManager;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.event.Event;
import iecompbot.objects.event.Event_Team;
import iecompbot.objects.profile.Profile;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.interaction.Automation.*;
import static iecompbot.interaction.GuildReady.Page4;
import static iecompbot.interaction.GuildReady.Page5;
import static iecompbot.interaction.listeners.calcs.StrikersXtr.Xtreme_LoadCommands;
import static iecompbot.interaction.listeners.calcs.ds.CS.CS_LoadCommands;
import static iecompbot.interaction.listeners.calcs.ds.GO1.GO1_LoadCommands;
import static iecompbot.interaction.listeners.calcs.ds.Galaxy.Galaxy_LoadCommands;
import static iecompbot.objects.BotManagers.*;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.event.Event.*;
import static my.utilities.util.Utilities.EpochSecondToPattern;
import static my.utilities.util.Utilities.StopString;

public class UtilityFeatures extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            try {
                if (event.getName().equals("events")) {
                    event.deferReply().queue(M -> {
                        List<SelectOption> options = new ArrayList<>();
                        EmbedBuilder E = new EmbedBuilder();
                        E.setTitle("Event Selection");
                        E.setDescription("Select the event of your choice:");
                        switch (event.getOption("type").getAsString()) {
                            case "WC" -> {
                                for (Event f : getWCs()) {
                                    options.add(SelectOption.of(f.getName(), f.getId())
                                            .withDescription(StopString(f.getDescription(), 100))
                                            .withEmoji(f.getGame().getEmoji().retrieve()));
                                }
                            }
                            case "EU" -> {
                                for (Event f : getEUs()) {
                                    options.add(SelectOption.of(f.getName(), f.getId())
                                            .withDescription(StopString(f.getDescription(), 100))
                                            .withEmoji(f.getGame().getEmoji().retrieve()));
                                }
                            }
                            case "CAN" -> {
                                for (Event f : getCANs()) {
                                    options.add(SelectOption.of(f.getName(), f.getId())
                                            .withDescription(StopString(f.getDescription(), 100))
                                            .withEmoji(f.getGame().getEmoji().retrieve()));
                                }
                            }
                            case "CC" -> {
                                for (Event f : getCCs()) {
                                    options.add(SelectOption.of(f.getName(), f.getId())
                                            .withDescription(StopString(f.getDescription(), 100))
                                            .withEmoji(f.getGame().getEmoji().retrieve()));
                                }
                            }
                        }
                        if (!options.isEmpty()) {
                            StringSelectMenu menu = StringSelectMenu.create("event-choice").setPlaceholder(options.getFirst().getLabel()).addOptions(options).build();
                            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu)).queue();
                        } else {
                            M.editOriginalEmbeds(E.build()).queue();
                        }
                    });
                }
                else if (event.getName().equals("help")) {
                    String page = event.getOption("section").getAsString();
                    if (page.equals("1")) {
                        EmbedBuilder E = new EmbedBuilder();
                        E.setColor(Color.red);
                        E.setTitle("Profile Manager");
                        E.setDescription("# Profile Viewing\n" +
                                "\n" +
                                "In the bot, each user has a global profile which can be accessed through </profile:1122156891313803316>, you can view the profile of anyone with the user parameter.\n" +
                                "You have different options.\n" +
                                "1. **Basic Information**\n" +
                                "> The first one is the default one if you don't select an info type. It display your levels, tournament tops, all-games match difference and goal difference. It also display the player license cards if the user is in a clan.\n" +
                                "2. **Games Statistics**\n" +
                                "> The second one shows the statistics on individual games. Such as goals scored, taken, match won, lost and tied and the ranks.\n" +
                                "3. **Tournaments & Medals**\n" +
                                "> The third one shows a list of your tournament top 1, 2 and 3. Including the tournament, your clan at that time, the link to the tournament, the amount of participants.\n" +
                                "4. **Inventory**\n" +
                                "> The forth one shows your inventory, all the items you can drop through matches or bought with the coins. These include cosmetics, boosters, and medals.\n" +
                                "5. **Match History**\n" +
                                "> The last one is your match history, it will show you all the matches you played recently, with the game, opponent and scores, the history keeps only 100 of your past matches, and beyond that are deleted.");

                        EmbedBuilder E2 = new EmbedBuilder();
                        E2.setColor(Color.red);
                        E2.setDescription("# Profile Editing\n" +
                                "\n" +
                                "You can edit your profile with </profile-manage:1122156891313803317>. Which will show you a window with different options.\n" +
                                "You can select any of them.\n" +
                                "1. **Embed Color**\n" +
                                "> *The color of your `/profile` embed.*\n" +
                                "2. **Signature**\n" +
                                "> *The text of your license card.*\n" +
                                "3. **Nationality**\n" +
                                "> *Used be used for flag icons on your profile or various windows.*\n" +
                                "4. **Date of Birth**\n" +
                                "> *The date of birth that will appear on your license card.*\n" +
                                "5. **Character**\n" +
                                "> *The URL of the PNG of the character that will appear on your license card.*\n" +
                                "6. **Clan Number**\n" +
                                "> *The uniform number that will appear on your license card and in various places.*\n" +
                                "7. **Toggle Matchmaking DMs**\n" +
                                "> *Whether you want to receive DM notifications if someone accepts your matchmakings.*\n" +
                                "8. **Toggle Tournament DMs**\n" +
                                "> *Whether you want to receive DM notifications if someone launch a tournament.*\n" +
                                "9. **Toggle Clan Tag DMs**\n" +
                                "> *Whether you want to have clan tags on various servers.*\n" +
                                "\n" +
                                "This will open a window (modal) where you can write the appropriate information.\n" +
                                "\n" +
                                "You can also set the character with </profile-character:1151237520835756145> directly.");

                        EmbedBuilder E3 = new EmbedBuilder();
                        E3.setColor(Color.red);
                        E3.setDescription("# Profile Editing - Cosmetics\n" +
                                "\n" +
                                "1. **The Shop**\n" +
                                "> By using </shop:1122156892811169906>, you are able to buy boosters, materials, and cosmetics. \n" +
                                "> - The Boosters are used to boost the amount of XP or Coins you earn while reporting a match.\n" +
                                "> - Cosmetics are used to customise your match results and prediction images.\n" +
                                "> - Materials are used for **Coming soon**.\n" +
                                "\n" +
                                "Note that you cannot sell or trade cosmetics. Once you buy them. It's no refund.");



                        event.replyEmbeds(E.build(), E2.build(), E3.build()).queue();
                    } else if (page.equals("2")) {
                        EmbedBuilder E = new EmbedBuilder();
                        E.setColor(Color.cyan);
                        E.setTitle("Tournament Manager");
                        E.setDescription("""
                            # View Clan Information

                            You can any clan's information with </clan-info:1122156891842297981>. And several kind of information can be viewed.
                            1. **Basic Information**
                            > It shows every little detail and information of a clan, including name, tag, server role, description, join requirements, primary language, power, creation date, logo and list of members.
                            2. **Member Information**
                            > It is like a condensed version of </profile:1122156891313803316> for each member. It will show the tournament won, the ranks, their nationality, clan number and their license card.
                            3. **Trophies & Rewards**
                            > These are achievements that clans earn by achieving things in the community. They are given by the owner and are very rare. Maybe your clan will earn one someday.
                            4. **Tournament List**
                            > The list of tournaments won by the clan members while they were in the clan. They stay even if the members leave the clan.
                            5. **Performances Information**
                            > These are tournaments statistics of the clan. It shows how much tournaments you won in total, how much you won in the last 3 months, and how much you won compared to the previous 3 months. As well as providing information about the best player of the clan and the most profitable server for tournaments.
                            6. **Vault**
                            > The wealth of the clan (more Coming soon)
                            7. **Power Details**
                            > Information from where does the clan achieve most of it's power.
                            8. **Interclans History**
                            > The history of all interclans (clan battles) of the clan.
                            9. **Activity Logs**
                            > Logs about every modification happening in the clans. Including who invited who, who edited what, etc...
                            10. **Warn Notices**
                            > Information about the behaviour of the clan members. These include which server they were timeout, banned, etc...

                            You can also view a leaderboard of all clans with </clan-list:1122156891146043440>.""");

                        EmbedBuilder E2 = new EmbedBuilder();
                        E2.setColor(Color.cyan);
                        E2.setDescription("# Clan Power\n" +
                                "\n" +
                                "A numeric value which determines the power of the clan. The power is purely speculative. But it is calculated based on many factors.\n" +
                                "1. **Members**\n" +
                                "> Each member bring +0.1 Power.\n" +
                                "2. **Ranks/League**\n" +
                                "> The league of each member provide +0.1 Power per tier. For example a Bronze V gives 0.1 and a Bronze IV gives 0.2.\n" +
                                "3. **Trophies & Rewards**\n" +
                                "> The power a trophy provide may vary. But they may give a huge amount.\n" +
                                "4. **Interclan Results**\n" +
                                "> The power given from an interclan depend of the power of the opposing clan at the time you challenged them. Better challenge big clans when they're still big !\n" +
                                "5. **Months of Existence**\n" +
                                "> Each 2 months since the creation of clan provides +0.1 Power.\n" +
                                "> The older the clan, the wiser the clan.\n" +
                                "6. **Tournament Results**\n" +
                                "> The main source of power of a clan. The amount of power received depend of the ranking of the members and the size of the tournament (participants amount). The bigger and the higher the members. The more power the tournament will bring !");

                        EmbedBuilder E3 = new EmbedBuilder();
                        E3.setColor(Color.cyan);
                        E3.setDescription("# Editing the Clan Information\n" +
                                "\n" +
                                "You can manage and edit your clan information and member information by using </clan-manage:1122156891842297979>. This command will provide you with options to edit:\n" +
                                "- Clan name\n" +
                                "- Clan description\n" +
                                "- Clan color\n" +
                                "- Clan join requirements\n" +
                                "- Clan social media links\n" +
                                "- Clan logo\n" +
                                "- Member number, roles, games and nickname.\n" +
                                "- License customisation\n" +
                                "\n" +
                                "Editing the 6 first options are as simple as writing text in a field on a window.\n" +
                                "For the clan logo, you can also use </clan-logo:1151237520835756144> which will request you to upload a PNG image.\n" +
                                "\n" +
                                "Editing members and license will covered in the message later.\n" +
                                "\n" +
                                "**Disbanding a clan**\n" +
                                "\n" +
                                "You can also disband a clan with </clan-disband:1122156891527729220>.\n" +
                                "This command is final. You cannot rollback a deleted clan.\n" +
                                "So be careful.");
                        event.replyEmbeds(E.build(), E2.build(), E3.build()).queue();

                        E.setDescription("# Adding, Removing and Managing Clan Members\n" +
                                "\n" +
                                "A clan needs members, once you made target of your next member.\n" +
                                "You can add them using </clan-add_member:1122156891527729221> or in theApps shortcut of your clan server. This will add them to your clan member list and you are now free to manage them.\n" +
                                "The clan members will also now benefit from having the **clan role and tag** on the servers.\n" +
                                "\n" +
                                "In </clan-manage:1122156891842297979> after selecting `Managing a member` or the Apps shortcut in your clan server. You can manage several things:\n" +
                                "1. **Roles & Games**\n" +
                                "2. **Number**\n" +
                                "3. **Nickname**\n" +
                                "And additionally:\n" +
                                "4. **Kick**\n" +
                                "5. **Transfer Ownership**\n" +
                                "\n" +
                                "Kicking a member from your clan can be done also in the Apps shortcut in your clan server.\n" +
                                "But members can also leave by themselves with </clan-leave:1122156891842297978>.\n" +
                                "Which will remove their **clan role and tag** on the servers.\n" +
                                "\n" +
                                "If you are the clan captain, you can also transfer the clan ownership to a member. Which will provide them with the captain clan role and remove it from you, as there can only be 1 owner. Beware of who you trust.");

                        E2.setDescription("# Clan Hierarchy and Roles\n" +
                                "\n" +
                                "Managing a clan by yourself can be quite tough. It's why you can provide roles to clan members !\n" +
                                "As said above, you can give several roles and games roles to your clan members. The games include all Inazuma Eleven games.\n" +
                                "But roles include:\n" +
                                "- Clan Captain\n" +
                                "> Can manage everything.\n" +
                                "- Co-Captain\n" +
                                "> Can manage everything, except disbanding and transfering of ownership.\n" +
                                "- Manager\n" +
                                "> Can manage the clan information, member information, interclans, adding members, removing members.\n" +
                                "- Scouter\n" +
                                "> Can add members\n" +
                                "- Trainer\n" +
                                "- Cheerleader\n" +
                                "- Broadcaster\n" +
                                "- Speedrunner\n" +
                                "- Designer\n" +
                                "> Just visual roles.\n" +
                                "- Ghost\n" +
                                "> The member having this role is in the clan list, but is not considered a real member of the clan. Which means the bot will not give him role and tag on servers. Nor count his contribution for the clan power.\n" +
                                "\n" +
                                "Now you can also edit the **shirt-number** of the members. Which will be displayed in their license cards.\n" +
                                "\n" +
                                "You can also edit the **nickname** of the member. Which is often used as character names for everyone.\n" +
                                "There may be more roles to come in the future.");

                        E3.setDescription("# Clan Licenses\n" +
                                "\n" +
                                "Those able to manage information in a clan can build the clan's license.\n" +
                                "Right now they are able to choose 3 options to customise their card.\n" +
                                "1. **Card Background**\n" +
                                "> The color of the background of the card. The color is very always dark toned and there is little choice for now.\n" +
                                "2. **Card Foreground**\n" +
                                "> A variety of shiny colors of many shapes. There will be more available overtime.\n" +
                                "3. **Sponsor**\n" +
                                "> A transparent logo of a discord server of the community. For now there is little choice due to how small our community is.\n" +
                                "\n" +
                                "These changes will be visible to the licenses of all the clan members, so edit them with consent.");



                        event.getChannel().sendMessageEmbeds(E.build(), E2.build(), E3.build()).queue();
                    } else if (page.equals("3")) {
                        EmbedBuilder E = new EmbedBuilder();
                        E.setColor(Color.ORANGE);
                        E.setTitle("Tournament Manager");
                        E.setImage("https://media.discordapp.net/attachments/1246502760048758815/1246502822652936342/Screenshot_2024-06-01_191012.png?ex=665c9fb7&is=665b4e37&hm=0560a4c6f7d4eab93b542b6e178d2eaaaf536d6e85c3b90ae51b29462b8ef0c9&=&format=webp&quality=lossless&width=1267&height=670");
                        E.setDescription(TL(event,"tournament-manual-1"));
                        EmbedBuilder E2 = new EmbedBuilder();
                        E2.setColor(Color.ORANGE);
                        E2.setDescription(TL(event,"tournament-manual-2"));
                        event.replyEmbeds(E.build(), E2.build()).queue();
                    } else if (page.equals("clan")) {
                        Page4.setAuthor("List of Commands: Page 4");
                        Page4.setColor(Color.yellow);
                        event.replyEmbeds(Page4.build()).queue();
                    } else if (page.equals("event")) {
                        Page5.setAuthor("List of Commands: Page 5");
                        Page5.setColor(Color.yellow);
                        event.replyEmbeds(Page5.build()).queue();
                    } else if (page.equals("event")) {
                        Page5.setAuthor("List of Commands: Page 6");
                        Page5.setColor(Color.yellow);
                        event.replyEmbeds(Page5.build()).queue();
                    } else if (page.equals("event")) {
                        Page5.setAuthor("List of Commands: Page 5");
                        Page5.setColor(Color.yellow);
                        event.replyEmbeds(Page5.build()).queue();
                    }
                }
                else if (event.getName().equals("bot-info")) {
                    event.deferReply(true).queue(M -> {
                        EmbedBuilder E = new EmbedBuilder();
                        E.setTitle(TL(M,"inazuma-competitive-info"));
                        E.setColor(Color.BLUE);
                        E.setThumbnail(DiscordAccount.getSelfUser().getAvatarUrl());

                        E.addField(TL(M,"team-information"), TL(M,"bot-creator") + ": <@" + Constants.BotOwnerID + ">\n", false);

                        E.addField("Match & Tournament Managers", BOTSTAFF.TournamentManagers.stream().map(s -> "- <@" + s + ">").collect(Collectors.joining("\n")), false);
                        E.addField("Clan & Interclan Managers", BOTSTAFF.ClanManagers.stream().map(s -> "- <@" + s + ">").collect(Collectors.joining("\n")), false);
                        E.addField("Graphic Contributors", BOTSTAFF.GraphicDesigners.stream().map(s -> "- <@" + s + ">").collect(Collectors.joining("\n")), false);
                        E.addField("Translators", BOTSTAFF.Translators.stream().map(s -> "- <@" + s + ">").collect(Collectors.joining("\n")), false);

                        E.addField(TL(M,"general-information"),
                                TL(M,"active-servers") + ": **" + DiscordAccount.getGuilds().size() + "**\n" +
                                        TL(M,"profiles-created") + ": **" + Profile.Count(Profile.class) + "**\n" +
                                        TL(M,"clans-created") + ": **" + DatabaseObject.Count(Clan.class) + "**\n", false);
                        if (isBotOwner(event.getUser()) || isTournamentManager(event.getUser())) {
                            E.addField("Score ban", BOTSTAFF.ScoreBan.stream().map(s -> "<@" + s + ">").collect(Collectors.joining("\n")), false);
                            E.addField("Bot block", BOTSTAFF.BlockList.stream().map(s -> "<@" + s + ">").collect(Collectors.joining("\n")), false);
                        }
                        M.editOriginalEmbeds(E.build()).queue();
                        E.addField("", "[Privacy Policy](https://docs.google.com/document/d/1WpDHF7QPCqphVD36NUqp_MEFrhP9RzF_)", false);
                        E.addField("", "[Terms of Service](https://docs.google.com/document/d/13mG3VEML1UY2sq3L5e-DGldBmbJvsOPf)", false);
                        if (isBotOwner(event.getUser())) {
                            System.gc();
                            int requests = DiscordAccount.cancelRequests();
                            E.addField("Details", "Canceled Requests: **" + requests + "**\n" + outputBotPerformances(), false);
                            User U = getUserByID("508331399149912088");
                            for (ThreadInfo info : ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)) {
                                String stacktrace = Arrays.stream(info.getStackTrace()).map(StackTraceElement::toString).filter(string -> string.contains("iecompbot") || string.contains("dv8tion")).collect(Collectors.joining("\n"));
                                String s = "-----------------------------------------------\nID: " + info.getThreadId() + " - Prio: " + info.getPriority() + " - " + info.getThreadState() + " - " + info.getThreadName() + "\n";
                                s = s + StopString(stacktrace, 1600);
                                sendPrivateMessage(U, s);
                            }
                            sendPrivateMessage(U, "Canceled Requests: **" + requests + "**\n" + outputBotPerformances());
                        }
                        M.editOriginalEmbeds(E.build()).queue();
                    });
                }
                else if (event.getName().equals("list-permissions")) {
                    event.deferReply().queue(M -> {
                        if (event.isFromGuild()) {
                            Member bot = event.getGuild().getSelfMember();
                            EmbedBuilder E = new EmbedBuilder();
                            E.setTitle(TL(M,"list-of-permissions"));
                            E.setDescription(TL(M,"list-of-permissions-description"));
                            E.setColor(Color.green);
                            if (bot.hasPermission(Permission.VIEW_CHANNEL)) {
                                E.addField("View Channels : :white_check_mark:", TL(M,"Allowthebottofunctionproperly"), false);
                            } else {
                                E.addField("View Channels : :x:", ":warning: " +  TL(M,"Thebotmaynotfunctionproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MESSAGE_EXT_EMOJI)) {
                                E.addField("Use External Emojis : :white_check_mark:", TL(M,"Allowthebottofunctionproperly"), false);
                            } else {
                                E.addField("Use External Emojis : :x:", ":warning: " +  TL(M,"Thebotmaynotfunctionproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MESSAGE_EXT_STICKER)) {
                                E.addField("Use External Stickers : :white_check_mark:", TL(M,"Allowthebottofunctionproperly"), false);
                            } else {
                                E.addField("Use External Stickers : :x:", ":warning: " +  TL(M,"Thebotmaynotfunctionproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
                                E.addField("Embed Links : :white_check_mark:", TL(M,"Allowthebottofunctionproperly"), false);
                            } else {
                                E.addField("Embed Links : :x:", ":warning: " +  TL(M,"Thebotmaynotfunctionproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MESSAGE_SEND)) {
                                E.addField("Manage Nicknames : :white_check_mark:", TL(M,"Allowthebottofunctionproperly"), false);
                            } else {
                                E.addField("Manage Nicknames : :x:", ":warning: " +  TL(M,"Thebotmaynotfunctionproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MANAGE_ROLES)) {
                                E.addField("Manage Roles : :white_check_mark:", TL(M,"Allowthebottofunctionproperly"), false);
                            } else {
                                E.addField("Manage Roles : :x:", ":warning: " +  TL(M,"Thebotmaynotfunctionproperly"), false);
                            }
                            if (bot.hasPermission(Permission.NICKNAME_MANAGE)) {
                                E.addField("Manage Nicknames : :white_check_mark:", TL(M,"Allowthebottofunctionproperly"), false);
                            } else {
                                E.addField("Manage Nicknames : :x:", ":warning: " +  TL(M,"Thebotmaynotfunctionproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MANAGE_EVENTS)) {
                                E.addField("Manage Events : :white_check_mark:", ":grey_exclamation: " + TL(M,"Unneededpermission"), false);
                            } else {
                                E.addField("Manage Events : :x:", ":warning: " +  TL(M,"Thebotmaynotfunctionproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MANAGE_THREADS)) {
                                E.addField("Manage Threads : :white_check_mark:", ":grey_exclamation: " + TL(M,"Unneededpermission"), false);
                            } else {
                                E.addField("Manage Threads : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS)) {
                                E.addField("Manage Emojis & Stickers : :white_check_mark:", ":grey_exclamation: " + TL(M,"Unneededpermission"), false);
                            } else {
                                E.addField("Manage Emojis & Stickers : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MESSAGE_MENTION_EVERYONE)) {
                                E.addField("Mention Everyone : :white_check_mark:", ":grey_exclamation: " + TL(M,"Unneededpermission"), false);
                            } else {
                                E.addField("Mention Everyone : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MANAGE_CHANNEL)) {
                                E.addField("Manage Channels : :white_check_mark:", ":warning: " + TL(M,"DangerouspermissionallowedCouldbeathreattotheserver"), false);
                                E.setColor(Color.red);
                            } else {
                                E.addField("Manage Channels : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MESSAGE_MANAGE)) {
                                E.addField("Manage Messages : :white_check_mark:", ":warning: " + TL(M,"DangerouspermissionallowedCouldbeathreattotheserver"), false);
                                E.setColor(Color.red);
                            } else {
                                E.addField("Manage Messages : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MANAGE_WEBHOOKS)) {
                                E.addField("Manage Webhooks : :white_check_mark:", ":warning: " + TL(M,"DangerouspermissionallowedCouldbeathreattotheserver"), false);
                                E.setColor(Color.red);
                            } else {
                                E.addField("Manage Webhooks : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MANAGE_SERVER)) {
                                E.addField("Manage Server : :white_check_mark:", ":warning: " + TL(M,"DangerouspermissionallowedCouldbeathreattotheserver"), false);
                                E.setColor(Color.red);
                            } else {
                                E.addField("Manage Server : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.BAN_MEMBERS)) {
                                E.addField("Ban Members : :white_check_mark:", ":warning: " + TL(M,"DangerouspermissionallowedCouldbeathreattotheserver"), false);
                                E.setColor(Color.red);
                            } else {
                                E.addField("Ban Members : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.KICK_MEMBERS)) {
                                E.addField("Kick Members : :white_check_mark:", ":warning: " + TL(M,"DangerouspermissionallowedCouldbeathreattotheserver"), false);
                                E.setColor(Color.red);
                            } else {
                                E.addField("Kick Members : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.MODERATE_MEMBERS)) {
                                E.addField("Moderate Members : :white_check_mark:", ":warning: " + TL(M,"DangerouspermissionallowedCouldbeathreattotheserver"), false);
                                E.setColor(Color.red);
                            } else {
                                E.addField("Moderate Members : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }
                            if (bot.hasPermission(Permission.ADMINISTRATOR)) {
                                E.addField("Administrator : :white_check_mark:", ":warning: " + TL(M,"DangerouspermissionallowedCouldbeathreattotheserver"), false);
                                E.setColor(Color.red);
                            } else {
                                E.addField("Administrator : :x:", TL(M,"Doesntpreventthebotfromfunctioningproperly"), false);
                            }

                            M.editOriginalEmbeds(E.build()).queue();
                        } else {
                            M.editOriginal("[Error] DM").queue();
                        }
                    });
                }
                else if (event.getName().contains("snd-play") || event.getName().contains("samba")) {
                    event.deferReply().queue(M -> {
                        GuildVoiceState MyGVS = event.getMember().getVoiceState();
                        GuildVoiceState BotGVS = event.getGuild().getSelfMember().getVoiceState();
                        if (MyGVS != null && BotGVS != null) {
                            if (hasPermissionInChannel(M, MyGVS.getChannel(), Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
                                Message.Attachment OST = event.getOption("sound-file").getAsAttachment();
                                if (MyGVS.inAudioChannel()) {
                                    if (!BotGVS.inAudioChannel()) {
                                        event.getGuild().getAudioManager().openAudioConnection(MyGVS.getChannel());
                                    } else if (MyGVS.getChannel() != BotGVS.getChannel()) {
                                        event.getGuild().getAudioManager().closeAudioConnection();
                                        Wait(100);
                                        event.getGuild().getAudioManager().openAudioConnection(MyGVS.getChannel());
                                    }
                                    if (OST.getFileExtension() != null) {
                                        if (OST.getFileExtension().toLowerCase().contains("mp3")
                                                || OST.getFileExtension().toLowerCase().contains("mp4")
                                                || OST.getFileExtension().toLowerCase().contains("wav")
                                                || OST.getFileExtension().toLowerCase().contains("ogg")) {
                                            PlayerManager.getInstance().loadAndPlay(M, OST.getUrl(), OST.getFileName(), event.getOption("repeat-times"));
                                        } else {
                                            M.editOriginal(TL(event, "music-manager-load-fail-ex")).queue();
                                        }
                                    } else {
                                        M.editOriginal(TL(event, "music-manager-load-fail-ex")).queue();
                                    }
                                } else {
                                    M.editOriginal(TL(event, "music-manager-load-fail-channel")).queue();
                                }
                            }
                        }
                    });
                }

                Galaxy_LoadCommands(event);
                Xtreme_LoadCommands(event);
                CS_LoadCommands(event);
                GO1_LoadCommands(event);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("event-choice")) {
                        event.deferReply().queue(M -> {
                            showEvent(M, Event.getEvent(event.getValues().getFirst()));
                        });
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    private static void showEvent(InteractionHook M, Event EVT) {
        EmbedBuilder E = new EmbedBuilder();
        E.setAuthor(EVT.getName());
        E.setColor(EVT.getColor());
        E.setDescription(EVT.getDescription() + "\n**Organizers:**\n" + EVT.getOrganisers().stream().map(p -> "> " + p.getUser().getAsMention()).collect(Collectors.joining("\n")));
        for (Event_Team ET : EVT.getTeams()) {
            Clan C = ET.getClanID() == null ? null : Clan.get(ET.getClanID());
            String emoji = ET.getPosition() == 1 ? "(:first_place:)" : ET.getPosition() == 2 ? "(:second_place:)" : ET.getPosition() == 3 ? "(:third_place:)" : "(:medal:)";
            E.addField(emoji + " " + (C == null ? ET.getName() : C.getEmojiFormatted() + " " + C.getName()) + " (" + BotEmoji.get("POW") + ET.getPower() + ")",
                    ET.getUsers().stream().map(p -> "> **" + (p == null ? null : p.getAsMention() + " (" + p.getEffectiveName() + ")") + "**").collect(Collectors.joining("\n")) + "\n" +
                            ET.getUnknown().stream().map(p -> "> **" + p + "**").collect(Collectors.joining("\n")), false);
        }
        E.setFooter("• " + EpochSecondToPattern(EVT.getStartedAtTimeEpochSecond(), "dd/MM/yyyy") + " - ");
        E.setTimestamp(Instant.ofEpochSecond(EVT.getCompletedAtTimeEpochSecond()));
        E.setThumbnail(EVT.getGame().getImageUrl());
        M.editOriginalEmbeds(E.build()).queue();
    }
}
