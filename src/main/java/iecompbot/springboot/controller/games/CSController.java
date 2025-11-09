package iecompbot.springboot.controller.games;

import ie.enums.DuelType;
import ie.enums.Element;
import ie.enums.Gender;
import ie.games.ds.cs.*;
import ie.games.ds.object.slotmove.summon.Spirit;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ie.Constants.InazumaCSDB;
import static iecompbot.Constants.POWERDECIMAL;
import static my.utilities.json.JSONItem.GSON;

@Tag(name = "Inazuma Eleven GO Chrono Stones", description = "Endpoints to access data from Chrono Stones database.")
@RestController
@CrossOrigin(origins = "*")
public class CSController {

    @Operation(summary = "Get a list of all CS players")
    @GetMapping("/cs/players.json")
    public List<CSPlayer> GetCSPlayers() {
        return InazumaCSDB.retrieveItems("Player").mapAllTo(CSPlayer.class);
    }
    @Operation(summary = "Get a list of a CS player by ID")
    @GetMapping("/cs/players/{id}.json")
    public CSPlayer GetCSPlayerByID(@PathVariable String id) {
        return InazumaCSDB.retrieveItems("Player").where("ID = ?", id).mapFirstTo(CSPlayer.class);
    }
    @Operation(summary = "Get a list of all CS special moves")
    @GetMapping("/cs/moves.json")
    public List<CSSpecialMove> GetCSMoves() {
        return InazumaCSDB.retrieveItems("Move").mapAllTo(CSSpecialMove.class);
    }
    @Operation(summary = "Get a list of a CS special move by ID")
    @GetMapping("/cs/moves/{id}.json")
    public CSSpecialMove GetCSMoveByID(@PathVariable String id) {
        return InazumaCSDB.retrieveItems("Move").where("ID = ?", id).mapFirstTo(CSSpecialMove.class);
    }
    @Operation(summary = "Get a list of all CS fighting spirits")
    @GetMapping("/cs/spirits.json")
    public List<CSSpirit> GetCSSpirits() {
        return InazumaCSDB.retrieveItems("Spirit").mapAllTo(CSSpirit.class);
    }
    @Operation(summary = "Get a list of a CS fighting spirit by ID")
    @GetMapping("/cs/spirits/{id}.json")
    public CSSpirit GetCSSpiritByID(@PathVariable String id) {
        return InazumaCSDB.retrieveItems("Spirit").where("ID = ?", id).mapFirstTo(CSSpirit.class);
    }

    @Hidden
    @PutMapping("/calculator/cs/dualtype")
    public String CSCalculatorSetDualType(@RequestBody Map<String, Object> payload) {
        PlayerMoveSpiritConfig CONFIG = new PlayerMoveSpiritConfig();
        try {
            if (!payload.get("duelType").toString().equals(" ")) {
                if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.SHOT_CATCH)) {
                    CONFIG.p1Move.addAll(InazumaCSDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'SHOT'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p1Stat = "Kick";

                    CONFIG.p2Move.addAll(InazumaCSDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'CATCH'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p2Stat = "Catch";
                }
                else if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.DRIBBLE_BLOCK)) {
                    CONFIG.p1Move.addAll(InazumaCSDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'DRIBBLE'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                     CONFIG.p1Stat = "Dribble";

                    CONFIG.p2Move.addAll(InazumaCSDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'BLOCK'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                     CONFIG.p2Stat = "Block";
                }
                else if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.SHOT_SHOTBLOCK)) {
                    CONFIG.p1Move.addAll(InazumaCSDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'SHOT'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                     CONFIG.p1Stat = "Kick";

                    CONFIG.p2Move.addAll(InazumaCSDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'SHOT' AND OtherType LIKE 'Shoot Block'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p2Stat = "Block";
                }
            }
        } catch (Exception ignored) {}
        return GSON.toJson(CONFIG);
    }

    @Hidden
    @PutMapping("/calculator/cs/move1")
    public String CSCalculatorSetMove1(@RequestBody Map<String, Object> payload) {
        MoveConfig moveConfig = new MoveConfig();
        try {
            if (!payload.get("p1_move").toString().equals(" ")) {
                CSSpecialMove S = (CSSpecialMove) CSUtils.getMoveByName(payload.get("p1_move").toString());
                moveConfig.level = S.getMaxLevel() + "";
            }
        } catch (Exception ignored) {}
        return GSON.toJson(moveConfig);
    }
    @Hidden
    @PutMapping("/calculator/cs/move2")
    public String CSCalculatorSetMove2(@RequestBody Map<String, Object> payload) {
        MoveConfig moveConfig = new MoveConfig();
        try {
            if (!payload.get("p2_move").toString().equals(" ")) {
                CSSpecialMove S = (CSSpecialMove) CSUtils.getMoveByName(payload.get("p2_move").toString());
                moveConfig.level = S.getMaxLevel() + "";
            }
        } catch (Exception ignored) {}
        return GSON.toJson(moveConfig);
    }

    @Hidden
    @PutMapping("/calculator/cs/spirit1")
    public String CSCalculatorSetSpirit1(@RequestBody Map<String, Object> payload) {
        SpiritConfig spiritConfig = new SpiritConfig();
        try {
            if (!payload.get("p1_spirit").toString().equals(" ")) {
                Spirit S = CSUtils.getSpiritByName(payload.get("p1_spirit").toString());
                S.setLevel(Integer.parseInt(payload.get("p1_spirit_lvl").toString()));
                spiritConfig.fsp = S.getFSP() + "";
                spiritConfig.hasSpirit = true;
                if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.SHOT_CATCH) && S.getType().toLowerCase().contains("shot")) {
                    spiritConfig.canUseMove = true;
                }
                if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.DRIBBLE_BLOCK) && S.getType().toLowerCase().contains("dribble")) {
                    spiritConfig.canUseMove = true;
                }
                if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.SHOT_SHOTBLOCK) && S.getType().toLowerCase().contains("shot")) {
                    spiritConfig.canUseMove = true;
                }
            }
        } catch (Exception ignored) {}
        return GSON.toJson(spiritConfig);
    }
    @Hidden
    @PutMapping("/calculator/cs/spirit2")
    public String CSCalculatorSetSpirit2(@RequestBody Map<String, Object> payload) {
        SpiritConfig spiritConfig = new SpiritConfig();
        try {
            if (!payload.get("p2_spirit").toString().equals(" ")) {
                Spirit S = CSUtils.getSpiritByName(payload.get("p2_spirit").toString());
                S.setLevel(Integer.parseInt(payload.get("p2_spirit_lvl").toString()));
                spiritConfig.fsp = S.getFSP() + "";
                spiritConfig.hasSpirit = true;
                if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.SHOT_CATCH) && S.getType().toLowerCase().contains("catch")) {
                    spiritConfig.canUseMove = true;
                }
                if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.DRIBBLE_BLOCK) && S.getType().toLowerCase().contains("block")) {
                    spiritConfig.canUseMove = true;
                }
                if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.SHOT_SHOTBLOCK) && S.getType().toLowerCase().contains("block")) {
                    spiritConfig.canUseMove = true;
                }
            }
        } catch (Exception ignored) {}
        return GSON.toJson(spiritConfig);
    }

    @Hidden
    @PutMapping("/calculator/cs/c")
    public String CSCalculator(@RequestBody Map<String, Object> payload) {
        try {
            int Move1 = 1;
            int Move2 = 1;
            CSPlayer P1 = new CSPlayer(Element.get(payload.get("p1_element").toString()), Gender.get(payload.get("p1_gender").toString()));
            P1.setKick(Integer.parseInt(payload.get("p1_K/D/B/C").toString()));
            P1.setDribble(Integer.parseInt(payload.get("p1_K/D/B/C").toString()));
            P1.setBlock(Integer.parseInt(payload.get("p1_K/D/B/C").toString()));
            P1.setCatch(Integer.parseInt(payload.get("p1_K/D/B/C").toString()));
            P1.setTechnique(Integer.parseInt(payload.get("p1_Technique").toString()));
            P1.setGP(Integer.parseInt(payload.get("p1_MaxGP").toString()));
            P1.setCurrentGP(Integer.parseInt(payload.get("p1_GP").toString()));
            P1.setTP(Integer.parseInt(payload.get("p1_MaxTP").toString()));
            P1.setCurrentTP(Integer.parseInt(payload.get("p1_TP").toString()));

            if (Boolean.parseBoolean(payload.get("p1_mixi").toString())) {
                CSPlayer Mixi = new CSPlayer(Element.VOID, Gender.Genderless);
                P1.setAura(Mixi, 0,0);
            }

            if (!payload.get("p1_spirit").toString().equals(" ")) {
                Spirit S = CSUtils.getSpiritByName(payload.get("p1_spirit").toString());
                S.setLevel(Integer.parseInt(payload.get("p1_spirit_lvl").toString()));
                P1.setSummon(S);
                if (Boolean.parseBoolean(payload.get("p1_armored").toString())) {
                    P1.summonSpirit(1);
                    S.Armoufy();
                }
            }

            if (!payload.get("p1_move").toString().equals("SPIRIT MOVE") && !payload.get("p1_move").equals(" ")) {
                if (CSUtils.getMoveByName(payload.get("p1_move").toString()) instanceof CSSpecialMove M) {
                    M.setLevel(Integer.parseInt(payload.get("p1_move_lvl").toString()));
                    P1.setMove1(M);
                }
            } else {
                P1.summonSpirit(1);
                Move1 = -1;
            }
            if (!payload.get("p1_talent1").toString().equals(" ")) P1.setMove2(CSUtils.getMoveByName(payload.get("p1_talent1").toString()));
            if (!payload.get("p1_talent2").toString().equals(" ")) P1.setMove3(CSUtils.getMoveByName(payload.get("p1_talent2").toString()));
            if (!payload.get("p1_talent3").toString().equals(" ")) P1.setMove4(CSUtils.getMoveByName(payload.get("p1_talent3").toString()));
            if (!payload.get("p1_talent4").toString().equals(" ")) P1.setMove5(CSUtils.getMoveByName(payload.get("p1_talent4").toString()));

            CSPlayer P2 = new CSPlayer(Element.get(payload.get("p2_element").toString()), Gender.get(payload.get("p2_gender").toString()));
            P2.setKick(Integer.parseInt(payload.get("p2_K/D/B/C").toString()));
            P2.setDribble(Integer.parseInt(payload.get("p2_K/D/B/C").toString()));
            P2.setBlock(Integer.parseInt(payload.get("p2_K/D/B/C").toString()));
            P2.setCatch(Integer.parseInt(payload.get("p2_K/D/B/C").toString()));
            P2.setTechnique(Integer.parseInt(payload.get("p2_Technique").toString()));
            P2.setGP(Integer.parseInt(payload.get("p2_MaxGP").toString()));
            P2.setCurrentGP(Integer.parseInt(payload.get("p2_GP").toString()));
            P2.setTP(Integer.parseInt(payload.get("p2_MaxTP").toString()));
            P2.setCurrentTP(Integer.parseInt(payload.get("p2_TP").toString()));

            if (Boolean.parseBoolean(payload.get("p2_mixi").toString())) {
                CSPlayer Mixi = new CSPlayer(Element.VOID, Gender.Genderless);
                P2.setAura(Mixi, 0,0);
            }

            if (!payload.get("p2_spirit").toString().equals(" ")) {
                Spirit S = CSUtils.getSpiritByName(payload.get("p2_spirit").toString());
                S.setLevel(Integer.parseInt(payload.get("p2_spirit_lvl").toString()));
                P2.setSummon(S);
                if (Boolean.parseBoolean(payload.get("p1_armored").toString())) {
                    P2.summonSpirit(1);
                    S.Armoufy();
                }
            }

            if (!payload.get("p2_move").toString().equals("SPIRIT MOVE") && !payload.get("p2_move").equals(" ")) {
                if (CSUtils.getMoveByName(payload.get("p2_move").toString()) instanceof CSSpecialMove M) {
                    M.setLevel(Integer.parseInt(payload.get("p2_move_lvl").toString()));
                    P2.setMove1(M);
                }
            } else {
                P2.summonSpirit(1);
                Move2 = -1;
            }
            if (!payload.get("p2_talent1").toString().equals(" ")) P2.setMove2(CSUtils.getMoveByName(payload.get("p2_talent1").toString()));
            if (!payload.get("p2_talent2").toString().equals(" ")) P2.setMove3(CSUtils.getMoveByName(payload.get("p2_talent2").toString()));
            if (!payload.get("p2_talent3").toString().equals(" ")) P2.setMove4(CSUtils.getMoveByName(payload.get("p2_talent3").toString()));
            if (!payload.get("p2_talent4").toString().equals(" ")) P2.setMove5(CSUtils.getMoveByName(payload.get("p2_talent4").toString()));

            CSDuel D = new CSDuel(P1, P2, Move1, Move2, DuelType.get(payload.get("duelType").toString()));
            D.setP1Armored(Boolean.parseBoolean(payload.get("p1_armored").toString()));
            if (!payload.get("p1_support1").toString().equals(" ")) D.setP1Support1(CSUtils.getSpiritByName(payload.get("p1_support1").toString()).getTalent());
            if (!payload.get("p1_support2").toString().equals(" ")) D.setP1Support2(CSUtils.getSpiritByName(payload.get("p1_support2").toString()).getTalent());
            if (!payload.get("p1_support3").toString().equals(" ")) D.setP1Support3(CSUtils.getSpiritByName(payload.get("p1_support3").toString()).getTalent());

            D.setP2Armored(Boolean.parseBoolean(payload.get("p2_armored").toString()));
            if (!payload.get("p2_support1").toString().equals(" ")) D.setP1Support1(CSUtils.getSpiritByName(payload.get("p2_support1").toString()).getTalent());
            if (!payload.get("p2_support2").toString().equals(" ")) D.setP1Support2(CSUtils.getSpiritByName(payload.get("p2_support2").toString()).getTalent());
            if (!payload.get("p2_support3").toString().equals(" ")) D.setP1Support3(CSUtils.getSpiritByName(payload.get("p2_support3").toString()).getTalent());

            if (Boolean.parseBoolean(payload.get("p1_volley").toString())) D.setVolley(true);

            return POWERDECIMAL.format(D.CalculateRealValueP1()) + " /// " + POWERDECIMAL.format(D.CalculateRealValueP2()) + " /// " + POWERDECIMAL.format(D.CalculateBaseValueP1()) + " /// " + POWERDECIMAL.format(D.CalculateBaseValueP2()) + " /// " + POWERDECIMAL.format(D.CalculateRealMoveValueP1()) + " /// " + POWERDECIMAL.format(D.CalculateRealMoveValueP2()) + " /// " + D.CalculateTotalValueP1() + " /// " + D.CalculateTotalValueP2()
                    + " /// " + D.RVFormula1 + " /// " + D.RVFormula2 + " /// " + D.BVFormula1 + " /// " + D.BVFormula2 + " /// " + D.RMVFormula1 + " /// " + D.RMVFormula2 + " /// " + D.TVFormula1 + " /// " + D.TVFormula2;
        } catch (Exception ignored) {
            return 0 + " /// " + 0 + " /// " + 0 + " /// " + 0 + " /// " + 0 + " /// " + 0 + " /// " + 0 + " /// " + 0 + " /// "
                    + " " + " /// "  + " " + " /// "  + " " + " /// " + " " + " /// "  + " " + " /// " + " " + " /// "  + " " + " /// " + " ";
        }
    }


    private static class PlayerMoveSpiritConfig {
        public List<String> p1Move = new ArrayList<>(List.of(" ", "SPIRIT MOVE"));
        public String p1Stat = "K/D/B/C";
        public List<String> p2Move = new ArrayList<>(List.of(" ", "SPIRIT MOVE"));
        public String p2Stat = "K/D/B/C";

        public PlayerMoveSpiritConfig() {}
    }
    private static class SpiritConfig {
        public String fsp = "0";
        public boolean hasSpirit = false;
        public boolean canUseMove = false;
    }
    private static class MoveConfig {
        public String level = "1";
    }

}
