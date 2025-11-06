package iecompbot.springboot.controller;

import iecompbot.objects.match.Game;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.Profile_PastClan;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.Utils;
import iecompbot.springboot.data.DatabaseObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static iecompbot.springboot.data.DatabaseObject.doQueryAll;

@Controller
@CrossOrigin(origins = "*")
public class FragmentController {

    @GetMapping("/p/tournament-list")
    public String getProfileTournaments(@RequestParam("p") String id, @RequestParam(value = "g", required = false) String games, Model model) throws Exception {
        Profile P = Profile.get(id);
        model.addAttribute("tournaments", doQueryAll("CALL DisplayUserTournaments(?,?,?,?,?,?);", P.getId(), null,  games, 1, 999, true));
        return "fragments/TournamentList_Profile";
    }
    @GetMapping("/p/match-log")
    public String getProfileMatches(@RequestParam("p") String id, @RequestParam(value = "g", required = false) String games, Model model) throws Exception {
        Profile P = Profile.get(id);
        model.addAttribute("myid", P.getId());
        model.addAttribute("game", Game.get("IE1"));
        model.addAttribute("logs", doQueryAll("CALL DisplayUserHistory(?,?,?,?,?);", P.getId(), null, games, 1, 250)); // userid, serverid, gamelist, page, amountPerPage
        model.addAttribute("utils", new Utils());
        return "fragments/MatchLogs_Profile";
    }
    @GetMapping("/p/clan-log")
    public String getProfileClanLogs(@RequestParam("p") String id, Model model) {
        Profile P = Profile.get(id);
        model.addAttribute("logs1", Profile_PastClan.ofUser(P.getId(), true));
        model.addAttribute("logs2", Profile_PastClan.ofUser(P.getId(), false));
        model.addAttribute("utils", new Utils());
        return "fragments/ProfileClanLogs";
    }



    @GetMapping("/c/tournament-list")
    public String getClanTournaments(@RequestParam("c") String id, @RequestParam(value = "g", required = false) String games, Model model) {
        model.addAttribute("tournaments", doQueryAll("CALL DisplayClanTournaments(?,?,?,?,?,?);", id, null,  games, 1, 999, true));
        return "fragments/TournamentList_Clan";
    }
    @GetMapping("/c/match-log")
    public String getClanMatches(@RequestParam("p") String id, @RequestParam(value = "g", required = false) String games, Model model) {
        model.addAttribute("myid", Long.parseLong(id));
        model.addAttribute("game", Game.get("IE1"));
        model.addAttribute("logs", doQueryAll("CALL DisplayClanHistory(?,?,?,?,?);", id, null, games, 1, 500)); // clanid, serverid, gamelist, page, amountPerPage
        model.addAttribute("utils", new Utils());
        return "fragments/MatchLogs_Clan";
    }



    @GetMapping("/s/tournament-list")
    public String getServerTournaments(@RequestParam("s") String id, @RequestParam(value = "g", required = false) String games, Model model) {
        model.addAttribute("tournaments", SChallonge_Tournament.ofServer(Long.parseLong(id), games));
        return "fragments/TournamentList_Server";
    }
    @GetMapping("/s/match-log")
    public String getServerMatches(@RequestParam("p") String id, @RequestParam(value = "g", required = false) String games, Model model) {
        model.addAttribute("game", Game.get("IE1"));
        model.addAttribute("logs", doQueryAll("CALL DisplayServerHistory(?,?,?,?);", id, games, 1, 500)); // serverid, gamelist, page, amountPerPage
        model.addAttribute("utils", new Utils());
        return "fragments/MatchLogs_Server";
    }
    @GetMapping("/s/tournament-stats")
    public String getStatsFragment(@RequestParam("s") String id, @RequestParam(value = "g", required = false) String games, Model model) {
        model.addAttribute("stats", doQueryAll("CALL DisplayTournamentStats(?,?);", id, games));
        return "fragments/TournamentStatsTable";
    }


    @GetMapping("/LeaderboardData")
    public String getLeaderboardData(@RequestParam(value = "g", required = false) String games, Model model) {
        int i = 1;
        List<DatabaseObject.Row> TRs = doQueryAll("CALL inazuma_competitive.DisplayAllStats(?);", games, null, 1, 1000);
        for (DatabaseObject.Row TR : TRs) TR.rows.put("Count", i++);
        model.addAttribute("players", TRs);
        return "fragments/LeaderboardData";
    }

}
