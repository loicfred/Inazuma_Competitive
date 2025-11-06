package iecompbot.springboot.controller.games;

import ie.enums.DuelType;
import ie.enums.Element;
import ie.enums.Gender;
import ie.games.ds.cs.CSPlayer;
import ie.games.ds.cs.CSUtils;
import ie.games.ds.galaxy.GalaxyPlayer;
import ie.games.ds.galaxy.GalaxyUtils;
import ie.games.ds.go1.GO1Utils;
import ie.games.ds.go1.GOPlayer;
import iecompbot.springboot.Utils;
import my.utilities.db.DBOrder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import static ie.Constants.InazumaCSDB;
import static ie.Constants.InazumaGLXDB;

@Controller
@CrossOrigin(origins = "*")
public class GamesController {

    @GetMapping("/games/index")
    public String getIndex(Model model, @RequestParam(required = false) String s) throws IOException {
        model.addAttribute("utils", new Utils());
        List<Player> P;
        if (s == null || s.isEmpty()) P = InazumaGLXDB.retrieveItems("Player").order("ID", DBOrder.ASC).mapAllTo(Player.class);
        else P = InazumaGLXDB.retrieveItems("Player").where("Name LIKE ?", "%" + s + "%").order("ID", DBOrder.ASC).mapAllTo(Player.class);
        for (Player player : P) player.imgEncoded = Base64.getEncoder().encodeToString(player.getImage().readAllBytes());
        model.addAttribute("players", P);
        return "games/index";
    }
    @GetMapping("/games/index/{id}")
    public String getIndexPlayer(Model model, @PathVariable String id) throws IOException {
        model.addAttribute("utils", new Utils());
        GOPlayer player1 = GO1Utils.getPlayerById(id);
        CSPlayer player2 = CSUtils.getPlayerById(id);
        GalaxyPlayer player3 = GalaxyUtils.getPlayerById(id);
        model.addAttribute("GO1Data", player1);
        model.addAttribute("CSData", player2);
        model.addAttribute("GLXData", player3);
        model.addAttribute("imgEncoded", Base64.getEncoder().encodeToString(player3.getImage().readAllBytes()));
        return "games/player";
    }

    @GetMapping("/calculator/galaxy")
    public String showDuelPanelGLX(Model model) {
        List<String> S = new ArrayList<>(List.of(" "));
        S.addAll(InazumaGLXDB.retrieveItems("Spirit").select("Name").get().stream().map(TR -> TR.getAsString("Name")).toList());
        List<String> S2 = new ArrayList<>(List.of(" "));
        S2.addAll(InazumaGLXDB.retrieveItems("Skill").select("Name").where("Action LIKE '%Add%' OR Action LIKE '%Multiplier%'").get().stream().map(TR -> TR.getAsString("Name")).toList());
        S2.sort(Comparator.comparing((String s) -> s.toLowerCase().replace(" ", "")));

        model.addAttribute("talents", S2);
        model.addAttribute("moves", List.of(""));
        model.addAttribute("spirits", S);
        model.addAttribute("duelTypes", List.of(DuelType.SHOT_CATCH, DuelType.DRIBBLE_BLOCK, DuelType.SHOT_SHOTBLOCK)); // or fetch from service
        model.addAttribute("stats", List.of("MaxGP", "GP", "K/D/B/C", "Technique"));

        model.addAttribute("elements", Element.values());
        model.addAttribute("genders", Gender.values());

        return "games/calculatorglx";
    }
    @GetMapping("/calculator/cs")
    public String showDuelPanelCS(Model model) {
        List<String> S = new ArrayList<>(List.of(" "));
        S.addAll(InazumaCSDB.retrieveItems("Spirit").select("Name").get().stream().map(TR -> TR.getAsString("Name")).toList());

        List<String> S2 = new ArrayList<>(List.of(" "));
        S2.addAll(InazumaCSDB.retrieveItems("Skill").select("Name").where("Action LIKE '%Add%' OR Action LIKE '%Multiplier%'").get().stream().map(TR -> TR.getAsString("Name")).toList());
        S2.sort(Comparator.comparing((String s) -> s.toLowerCase().replace(" ", "")));

        model.addAttribute("talents", S2);
        model.addAttribute("moves", List.of(""));
        model.addAttribute("spirits", S);
        model.addAttribute("duelTypes", List.of(DuelType.SHOT_CATCH, DuelType.DRIBBLE_BLOCK, DuelType.SHOT_SHOTBLOCK)); // or fetch from service
        model.addAttribute("stats", List.of("MaxGP", "GP", "MaxTP", "TP", "K/D/B/C", "Technique"));

        model.addAttribute("elements", Element.values());
        model.addAttribute("genders", Gender.values());

        return "games/calculatorcs";
    }


    private static class Player extends GalaxyPlayer {
        public String imgEncoded;
    }
}
