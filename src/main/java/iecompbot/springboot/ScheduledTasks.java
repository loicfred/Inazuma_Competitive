package iecompbot.springboot;

import iecompbot.interaction.GuildReady;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.CacheService;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static iecompbot.Main.DiscordAccount;
import static iecompbot.interaction.Automation.LogSlash;
import static iecompbot.interaction.Automation.handleException;
import static my.utilities.util.Utilities.GenerateRandomNumber;
import static my.utilities.util.Utilities.Range;

@Component
public class ScheduledTasks {

    private final CacheService cacheService;
    public ScheduledTasks(CacheService cacheService) {
        this.cacheService = cacheService;
    }


    private int prevStatus = 1;
    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000 * 60 * 3)
    public void each10min() {
        changeStatusRandomly();
    }
    private void changeStatusRandomly() {
        try {
            int status = GenerateRandomNumber(1, 15);
            while (status == prevStatus) status = GenerateRandomNumber(1, 15);
            switch (status) {
                case 1 -> {
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("⚽ Competing with " + DatabaseObject.Count(Profile.class, "Name IS NOT NULL") + " players on Inazuma Eleven.")
                    );
                }
                case 2 -> {
                    int which = GenerateRandomNumber(1,3);
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("⚡ " + (which == 1 ? "Chaining" : which == 2 ? "Dashing" : "Nomashing") + " on Inazuma Eleven GO Strikers 2013 !")
                    );
                }
                case 3 -> {
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("🏆 Managing " + DatabaseObject.Count(SChallonge_Tournament.class) + " tourneys in " + DiscordAccount.getGuilds().size() + " servers.")
                    );
                }
                case 4 -> {
                    int which = GenerateRandomNumber(1,5);
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("\uD83D\uDC7E " + (which == 1 ? "Armoring" : which == 2 ? "Jading" : which == 3 ? "Offsiding" : which == 4 ? "Pausing" : "Miximaxing") + " on Inazuma Eleven GO Chrono Stones !")
                    );
                }
                case 5 -> {
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("\uD83C\uDF10 Watching over " + DatabaseObject.Count(Clan.class, "NOT Status = 'Closed'") + " clans with " + DatabaseObject.Count(ClanMember.class, "isMainClan") + " members.")
                    );
                }
                case 6 -> {
                    int which = GenerateRandomNumber(1,3);
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("\uD83D\uDCA5 " + (which == 1 ? "Breaching" : which == 2 ? "Focusing" : "Scrambling") + " on Inazuma Eleven Victory Road !")
                    );
                }
                case 7 -> {
                    int which = GenerateRandomNumber(1,5);
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("\uD83C\uDF20 " + (which == 1 ? "Summoning" : which == 2 ? "Critting" : which == 3 ? "Offsiding" : which == 4 ? "Pausing" : "Running") + " on Inazuma Eleven GO Galaxy !")
                    );
                }
                case 8 -> {
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("\uD83D\uDCAD Playing Rock-Paper-Scissors on Victory Road...")
                    );
                }
                case 9 -> {
                    int which = GenerateRandomNumber(1,5);
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("✍\uFE0F Building a team on Inazuma Eleven " + (which == 1 ? "GO Galaxy" : which == 2 ? "GO Chrono Stones" : which == 3 ? "GO" : which == 4 ? "GO Strikers 2013" : "Victory Road") + " !")
                    );
                }
                case 10 -> {
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("\uD83C\uDFD7\uFE0F Building my city on Inazuma Eleven Victory Road !")
                    );
                }
                case 11 -> {
                    int which = GenerateRandomNumber(1,7);
                    DiscordAccount.getPresence().setActivity(
                            Activity.customStatus("\uD83E\uDEA7 Scouting players on Inazuma Eleven " + (which == 1 ? "GO Galaxy" : which == 2 ? "GO Chrono Stones" : which == 3 ? "GO" : which == 4 ? "2" : which == 5 ? "3" : which == 6 ? "Victory Road" : "") + " !")
                    );
                }
                default -> {
                    List<SChallonge_Tournament> CTs = SChallonge_Tournament.getPendingChallonges(true);
                    if (!CTs.isEmpty()) {
                        SChallonge_Tournament CT =  CTs.get(GenerateRandomNumber(0, CTs.size()-1));
                        String text = fixMessage(CT);
                        if (Range(text.length(), 1, 128)) {
                            DiscordAccount.getPresence().setActivity(Activity.customStatus(text));
                        } else changeStatusRandomly();
                    } else changeStatusRandomly();
                }
            }
            prevStatus = status;
        } catch (Exception e) {
            handleException(e);
        }
    }
    private String fixMessage(SChallonge_Tournament CT) {
        String text = "🏆 Register now to " + CT.getName() + " | " +  (CT.getInscriptionChannelInviteLink() != null ? CT.getInscriptionChannelInviteLink() : "").replace("https://", "");
        text = text.replaceAll("Inazuma Eleven", "IE").replaceAll("INAZUMA ELEVEN", "IE");
        text = text.replaceAll("Victory Road", "VR").replaceAll("VICTORY ROAD", "VR");
        text = text.replaceAll("Chrono Stone", "CS").replaceAll("CHRONO STONE", "CS");
        text = text.replaceAll("Chrono Stones", "CS").replaceAll("CHRONO STONES", "CS");
        return text;
    }


    @Scheduled(fixedRate = 1000 * 60 * 60 * 3, initialDelay = 1000 * 60 * 60)
    public void each3h() {
        try {
            GuildReady.RefreshAllClanlists();
            GuildReady.RefreshAllBlacklists();
        } catch (Exception e) {
            handleException(e);
        }
    }


    @Scheduled(fixedRate = 1000 * 60 * 60 * 6, initialDelay = 1000 * 60 * 60)
    public void each6h() {
        try {
            GuildReady.RefreshAllPrivateCMDs();
            GuildReady.RefreshAllLeaderboards();
            //GuildReady.RefreshAllWinnerRoles();
        } catch (Exception e) {
            handleException(e);
        }
    }


    @Scheduled(fixedRate = 1000 * 60 * 60 * 24 * 7, initialDelay = 1000 * 60 * 60)
    public void each7d() {
        try {
            GuildReady.RefreshAllRankMembers();
            GuildReady.RefreshAllClanMembers(Clan.listOpen());
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void midNight() {
        LogSlash("**[Schedule]** Cleaning up " +  cacheService.clearAllCaches() + " cache lists...");
        System.gc();
    }


}