package iecompbot.springboot.controller;

import iecompbot.objects.Retrieval;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.event.Event;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.BaseTournament;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.Utils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static iecompbot.Main.MainDirectory;
import static iecompbot.objects.BotManagers.BOTSTAFF;
import static iecompbot.springboot.data.DatabaseObject.doQuery;
import static iecompbot.springboot.data.DatabaseObject.getAllWhere;

@Controller
@CrossOrigin(origins = "*")
public class HomeController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OAuth2User principal) throws Exception {
        if (principal != null) model.addAttribute("discordUser", principal.getAttributes());
        model.addAttribute("utils", new Utils());
        model.addAttribute("tournaments", SChallonge_Tournament.getActiveChallonges(false).stream().filter(C -> C.getParticipantCount() >= 8 && C.getInscriptionChannelInviteLink() != null).toList());
        return "index";
    }
    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal OAuth2User principal) throws Exception {
        return index(model, principal);
    }
    @GetMapping("/help")
    public String getHelp(Model model) {
        return "help";
    }
    @GetMapping("/leaderboard")
    public String getLeaderboard() {
        return "leaderboard";
    }
    @GetMapping("/error")
    public String error() throws Exception {
        return "error";
    }
    @GetMapping("/about")
    public String about(Model model) throws Exception {
        model.addAttribute("staff", BOTSTAFF);
        model.addAttribute("utils", new Retrieval());
        return "about";
    }

    @GetMapping("/events")
    public String events(Model model) throws Exception {
        model.addAttribute("wc", Event.getWCs());
        model.addAttribute("cc", Event.getCCs());
        model.addAttribute("eu", Event.getEUs());
        model.addAttribute("can", Event.getCANs());
        model.addAttribute("utils", new Utils());
        return "list/events";
    }
    @GetMapping("/clans")
    public String clans(Model model) throws Exception {
        model.addAttribute("openclans", Clan.listOpen());
        model.addAttribute("pausedclans", Clan.listPaused());
        model.addAttribute("utils", new Utils());
        return "list/clans";
    }
    @GetMapping("/tournaments")
    public String tournaments(Model model) throws Exception {
        List<SChallonge_Tournament> Ts1 = SChallonge_Tournament.getActiveChallonges(true);
        List<SChallonge_Tournament> Ts2 = SChallonge_Tournament.list(false);
        model.addAttribute("opentournaments", Ts1.stream().sorted(Comparator.comparingLong(BaseTournament::getStartAtTimeEpochSecond)).filter(SChallonge_Tournament::isPending).toList());
        model.addAttribute("ongoingtournaments", Ts1.stream().filter(SChallonge_Tournament::isUnderway).toList());
        model.addAttribute("lastmonthtournaments", Ts2.stream().filter(T -> T.getCompletedAtTime().isAfter(Instant.now().minus(30, ChronoUnit.DAYS))).toList());
        model.addAttribute("lastyeartournaments", Ts2.stream().filter(T -> T.getCompletedAtTime().isBefore(Instant.now().minus(30, ChronoUnit.DAYS)) && T.getCompletedAtTime().isAfter(Instant.now().minus(365, ChronoUnit.DAYS))).toList());
        model.addAttribute("utils", new Utils());
        return "list/tournaments";
    }
    @GetMapping("/servers")
    public String servers(Model model) throws Exception {
        List<ServerInfo> I = ServerInfo.list().stream().filter(S -> S.getPermanentInviteLink() != null && S.getGuild() != null).sorted(Comparator.comparingInt((ServerInfo S) -> S.TournamentCount).reversed()).toList();
        model.addAttribute("servers", I);
        model.addAttribute("utils", new Utils());
        return "list/servers";
    }

    @GetMapping("/p/{id}")
    public String profile(@PathVariable String id, Model model) throws Exception {
        Profile P = Profile.get(id);
        P.RefreshProfileInformation(null);
        model.addAttribute("utils", new Utils());
        model.addAttribute("profile", P);
        model.addAttribute("AvatarURL", P.getUser().getEffectiveAvatarUrl());
        model.addAttribute("ColorTheme", P.ColorCode);
        return "item/p";
    }
    @GetMapping("/c/{id}")
    public String clan(@PathVariable long id, Model model) {
        Clan C = Clan.get(id);
        C.getClanMembers().sort(Comparator.comparingInt(ClanMember::getHighestRolePosition));
        model.addAttribute("utils", new Utils());
        model.addAttribute("clan", C);
        model.addAttribute("AvatarURL", C.getEmblemURL());
        model.addAttribute("ColorTheme", C.Colorcode);
        return "item/c";
    }
    @GetMapping("/s/{id}")
    public String servers(@PathVariable long id, Model model) throws Exception {
        ServerInfo S = ServerInfo.get(id);
        S.RefreshGuildInformation();
        model.addAttribute("server", S);
        model.addAttribute("act", doQuery("CALL DisplayServerActivity(?,?,?,?)", id, null, 30, 3).orElse(null));
        model.addAttribute("utils", new Utils());
        return "item/s";
    }
    @GetMapping("/t/{id}")
    public String tournaments(@PathVariable long id, Model model) throws Exception {
        SChallonge_Tournament T = SChallonge_Tournament.get(id);
        model.addAttribute("tournament", T);
        model.addAttribute("server", ServerInfo.get(T.ServerID));
        model.addAttribute("utils", new Utils());
        return "item/t";
    }
    @GetMapping("/e/{id}")
    public String getEvent(@PathVariable String id, Model model) {
        File[] Images = new File(MainDirectory + "/assets/img/event/" + id + "/").listFiles();
        model.addAttribute("e", Event.getEvent(id));
        model.addAttribute("img", Images != null ? Arrays.stream(Images).map((File f) -> f.getName().replace(".png", "")).sorted(Comparator.comparing((String s) -> s)).toList() : new ArrayList<>());
        model.addAttribute("utils", new Utils());
        return "item/e";
    }




    @GetMapping("/search")
    public String search(Model model, @RequestParam("s") String search) throws Exception {
        List<Event> E = search != null ? getAllWhere(Event.class, """
                        Name LIKE ? OR Description LIKE ? OR ID = ?
                        ORDER BY CASE
                           WHEN ID = ? THEN 1
                           WHEN Name = ? THEN 2
                           WHEN Name LIKE ? THEN 3
                           WHEN Description LIKE ? THEN 4
                           ELSE 5
                        END ASC LIMIT 100""",
                "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%") : new ArrayList<>();
        List<ServerInfo> I = search != null ? getAllWhere(ServerInfo.class, """
                        Name LIKE ? OR Description LIKE ?
                        ORDER BY CASE
                           WHEN Name = ? THEN 1
                           WHEN Name LIKE ? THEN 2
                           WHEN Description LIKE ? THEN 3
                           ELSE 4
                        END ASC LIMIT 100"""
                , "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%").stream().filter(S -> S.getPermanentInviteLink() != null && S.getGuild() != null).toList() : new ArrayList<>();

        List<Clan> C = search != null ? getAllWhere(Clan.class, """
                        (Name LIKE ? OR Description LIKE ? OR Tag LIKE ?) AND NOT Status = 'Closed'
                        ORDER BY CASE
                           WHEN Tag = ? THEN 1
                           WHEN Tag LIKE ? THEN 2
                           WHEN Name = ? THEN 3
                           WHEN Name LIKE ? THEN 4
                           WHEN Description LIKE ? THEN 5
                           ELSE 6
                        END ASC LIMIT 100"""
                        , "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%") : new ArrayList<>();
        List<SChallonge_Tournament> T = search != null ? getAllWhere(SChallonge_Tournament.class, """
                        (Name LIKE ? OR Description LIKE ? OR URL LIKE ?)
                        ORDER BY CASE
                           WHEN Name = ? THEN 1
                           WHEN Name LIKE ? THEN 2
                           WHEN URL = ? THEN 3
                           WHEN URL LIKE ? THEN 4
                           WHEN Description LIKE ? THEN 5
                           ELSE 6
                        END ASC LIMIT 100
                        """
                        , "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%") : new ArrayList<>();
        List<Profile> P = search != null ? getAllWhere(Profile.class, """
                        Name LIKE ? OR FullName LIKE ? OR Signature LIKE ? OR ID = ?
                        ORDER BY CASE
                           WHEN ID = ? THEN 1
                           WHEN Name LIKE ? THEN 2
                           WHEN FullName LIKE ? THEN 3
                           WHEN Signature LIKE ? THEN 4
                           ELSE 5
                        END ASC LIMIT 100;
                        """
                , "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%", "%" + search + "%") : new ArrayList<>();
        model.addAttribute("search", search != null ? search : "Search");
        model.addAttribute("events", E);
        model.addAttribute("servers", I);
        model.addAttribute("clans", C);
        model.addAttribute("tournaments", T);
        model.addAttribute("profiles", P);
        model.addAttribute("utils", new Utils());
        return "list/search";
    }
}
