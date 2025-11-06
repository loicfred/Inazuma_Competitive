package iecompbot.springboot.controller.games;

import ie.enums.DuelType;
import ie.enums.Element;
import ie.enums.Gender;
import ie.games.ds.galaxy.*;
import ie.games.ds.object.slotmove.summon.Spirit;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static ie.Constants.InazumaGLXDB;
import static iecompbot.Constants.POWERDECIMAL;
import static my.utilities.json.JSONItem.GSON;

@RestController
@CrossOrigin(origins = "*")
public class GalaxyController {

    @PutMapping("/galaxy/players")
    public String GetGalaxyPlayers() {
        return GSON.toJson(InazumaGLXDB.retrieveItems("Player").mapAllTo(GalaxyPlayer.class));
    }
    @PutMapping("/galaxy/players/{id}")
    public String GetGalaxyPlayerByID(@PathVariable String id) {
        return GSON.toJson(InazumaGLXDB.retrieveItems("Player").where("ID = ?", id).mapAllTo(GalaxyPlayer.class));
    }
    @PutMapping("/galaxy/moves")
    public String GetGalaxyMoves() {
        return GSON.toJson(InazumaGLXDB.retrieveItems("Move").mapAllTo(GalaxySpecialMove.class));
    }
    @PutMapping("/galaxy/moves/{id}")
    public String GetGalaxyMoveByID(@PathVariable String id) {
        return GSON.toJson(InazumaGLXDB.retrieveItems("Move").where("ID = ?", id).mapAllTo(GalaxySpecialMove.class));
    }
    @PutMapping("/galaxy/spirits")
    public String GetGalaxySpirits() {
        return GSON.toJson(InazumaGLXDB.retrieveItems("Spirit").mapAllTo(GalaxySpirit.class));
    }
    @PutMapping("/galaxy/spirits/{id}")
    public String GetGalaxySpiritByID(@PathVariable String id) {
        return GSON.toJson(InazumaGLXDB.retrieveItems("Spirit").where("ID = ?", id).mapAllTo(GalaxySpirit.class));
    }
    @PutMapping("/galaxy/totems")
    public String GetGalaxyTotems() {
        return GSON.toJson(InazumaGLXDB.retrieveItems("Totem").mapAllTo(Totem.class));
    }
    @PutMapping("/galaxy/totems/{id}")
    public String GetGalaxyTotemByID(@PathVariable String id) {
        return GSON.toJson(InazumaGLXDB.retrieveItems("Totem").where("ID = ?", id).mapAllTo(Totem.class));
    }

    @PutMapping("/calculator/galaxy/dualtype")
    public String GalaxyCalculatorSetDualType(@RequestBody Map<String, Object> payload) {
        PlayerMoveSpiritConfig CONFIG = new PlayerMoveSpiritConfig();
        try {
            if (!payload.get("duelType").toString().equals(" ")) {
                if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.SHOT_CATCH)) {
                    CONFIG.p1Move.addAll(InazumaGLXDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'SHOT'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p1Stat = "Kick";

                    CONFIG.p2Move.addAll(InazumaGLXDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'CATCH'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p2Stat = "Catch";
                }
                else if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.DRIBBLE_BLOCK)) {
                    CONFIG.p1Move.addAll(InazumaGLXDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'DRIBBLE'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p1Stat = "Dribble";

                    CONFIG.p2Move.addAll(InazumaGLXDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'BLOCK'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p2Stat = "Block";
                }
                else if (DuelType.get(payload.get("duelType").toString()).equals(DuelType.SHOT_SHOTBLOCK)) {
                    CONFIG.p1Move.addAll(InazumaGLXDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'SHOT'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p1Stat = "Kick";

                    CONFIG.p2Move.addAll(InazumaGLXDB.retrieveItems("Move").select("Name").where("MoveType LIKE 'SHOT' AND OtherType LIKE 'Shoot Block'").get().stream().map(TR -> TR.getAsString("Name")).toList());
                    CONFIG.p2Stat = "Block";
                }
            }
        } catch (Exception ignored) {}
        return GSON.toJson(CONFIG);
    }

    @PutMapping("/calculator/galaxy/spirit1")
    public String GalaxyCalculatorSetSpirit1(@RequestBody Map<String, Object> payload) {
        SpiritConfig spiritConfig = new SpiritConfig();
        try {
            if (!payload.get("p1_spirit").toString().equals(" ")) {
                Spirit S = GalaxyUtils.getSpiritByName(payload.get("p1_spirit").toString());
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
    @PutMapping("/calculator/galaxy/spirit2")
    public String GalaxyCalculatorSetSpirit2(@RequestBody Map<String, Object> payload) {
        SpiritConfig spiritConfig = new SpiritConfig();
        try {
            if (!payload.get("p2_spirit").toString().equals(" ")) {
                Spirit S = GalaxyUtils.getSpiritByName(payload.get("p2_spirit").toString());
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

    @PutMapping("/calculator/galaxy/c")
    public String GalaxyCalculator(@RequestBody Map<String, Object> payload) {
        try {
            int Move1 = 1;
            int Move2 = 1;
            GalaxyPlayer P1 = new GalaxyPlayer(Element.get(payload.get("p1_element").toString()), Gender.get(payload.get("p1_gender").toString()));
            P1.setKick(Integer.parseInt(payload.get("p1_K/D/B/C").toString()));
            P1.setDribble(Integer.parseInt(payload.get("p1_K/D/B/C").toString()));
            P1.setBlock(Integer.parseInt(payload.get("p1_K/D/B/C").toString()));
            P1.setCatch(Integer.parseInt(payload.get("p1_K/D/B/C").toString()));
            P1.setTechnique(Integer.parseInt(payload.get("p1_Technique").toString()));
            P1.setGP(Integer.parseInt(payload.get("p1_MaxGP").toString()));
            P1.setCurrentGP(Integer.parseInt(payload.get("p1_GP").toString()));

            if (!payload.get("p1_spirit").toString().equals(" ")) {
                Spirit S = GalaxyUtils.getSpiritByName(payload.get("p1_spirit").toString());
                S.setLevel(Integer.parseInt(payload.get("p1_spirit_lvl").toString()));
                P1.setSummon(S);
                if (Boolean.parseBoolean(payload.get("p1_armored").toString())) {
                    P1.summonSpirit(1);
                    S.Armoufy();
                }
            }

            if (!payload.get("p1_move").toString().equals("SPIRIT MOVE") && !payload.get("p1_move").equals(" ")) {
                if (GalaxyUtils.getMoveByName(payload.get("p1_move").toString()) instanceof GalaxySpecialMove M) {
                    M.setLevel(Integer.parseInt(payload.get("p1_move_lvl").toString()));
                    P1.setMove1(M);
                }
            } else {
                P1.summonSpirit(1);
                Move1 = -1;
            }
            if (!payload.get("p1_talent1").toString().equals(" ")) P1.setMove2(GalaxyUtils.getMoveByName(payload.get("p1_talent1").toString()));
            if (!payload.get("p1_talent2").toString().equals(" "))  P1.setMove3(GalaxyUtils.getMoveByName(payload.get("p1_talent2").toString()));
            if (!payload.get("p1_talent3").toString().equals(" ")) P1.setMove4(GalaxyUtils.getMoveByName(payload.get("p1_talent3").toString()));
            if (!payload.get("p1_talent4").toString().equals(" ")) P1.setMove5(GalaxyUtils.getMoveByName(payload.get("p1_talent4").toString()));

            GalaxyPlayer P2 = new GalaxyPlayer(Element.get(payload.get("p2_element").toString()), Gender.get(payload.get("p2_gender").toString()));
            P2.setKick(Integer.parseInt(payload.get("p2_K/D/B/C").toString()));
            P2.setDribble(Integer.parseInt(payload.get("p2_K/D/B/C").toString()));
            P2.setBlock(Integer.parseInt(payload.get("p2_K/D/B/C").toString()));
            P2.setCatch(Integer.parseInt(payload.get("p2_K/D/B/C").toString()));
            P2.setTechnique(Integer.parseInt(payload.get("p2_Technique").toString()));
            P1.setGP(Integer.parseInt(payload.get("p2_MaxGP").toString()));
            P1.setCurrentGP(Integer.parseInt(payload.get("p2_GP").toString()));

            if (!payload.get("p2_spirit").toString().equals(" ")) {
                Spirit S = GalaxyUtils.getSpiritByName(payload.get("p2_spirit").toString());
                S.setLevel(Integer.parseInt(payload.get("p2_spirit_lvl").toString()));
                P2.setSummon(S);
                if (Boolean.parseBoolean(payload.get("p1_armored").toString())) {
                    P2.summonSpirit(1);
                    S.Armoufy();
                }
            }

            if (!payload.get("p2_move").toString().equals("SPIRIT MOVE") && !payload.get("p2_move").equals(" ")) {
                if (GalaxyUtils.getMoveByName(payload.get("p2_move").toString()) instanceof GalaxySpecialMove M) {
                    M.setLevel(Integer.parseInt(payload.get("p2_move_lvl").toString()));
                    P2.setMove1(M);
                }
            } else {
                P2.summonSpirit(1);
                Move2 = -1;
            }
            if (!payload.get("p2_talent1").toString().equals(" ")) P2.setMove2(GalaxyUtils.getMoveByName(payload.get("p2_talent1").toString()));
            if (!payload.get("p2_talent2").toString().equals(" ")) P2.setMove3(GalaxyUtils.getMoveByName(payload.get("p2_talent2").toString()));
            if (!payload.get("p2_talent3").toString().equals(" ")) P2.setMove4(GalaxyUtils.getMoveByName(payload.get("p2_talent3").toString()));
            if (!payload.get("p2_talent4").toString().equals(" ")) P2.setMove5(GalaxyUtils.getMoveByName(payload.get("p2_talent4").toString()));

            GalaxyDuel D = new GalaxyDuel(P1, P2, Move1, Move2, DuelType.get(payload.get("duelType").toString()));
            D.setP1Armored(Boolean.parseBoolean(payload.get("p1_armored").toString()));
            if (!payload.get("p1_support1").toString().equals(" ")) D.setP1Support1(GalaxyUtils.getSpiritByName(payload.get("p1_support1").toString()).getTalent());
            if (!payload.get("p1_support2").toString().equals(" ")) D.setP1Support2(GalaxyUtils.getSpiritByName(payload.get("p1_support2").toString()).getTalent());
            if (!payload.get("p1_support3").toString().equals(" ")) D.setP1Support3(GalaxyUtils.getSpiritByName(payload.get("p1_support3").toString()).getTalent());

            D.setP2Armored(Boolean.parseBoolean(payload.get("p2_armored").toString()));
            if (!payload.get("p2_support1").toString().equals(" ")) D.setP1Support1(GalaxyUtils.getSpiritByName(payload.get("p2_support1").toString()).getTalent());
            if (!payload.get("p2_support2").toString().equals(" ")) D.setP1Support2(GalaxyUtils.getSpiritByName(payload.get("p2_support2").toString()).getTalent());
            if (!payload.get("p2_support3").toString().equals(" ")) D.setP1Support3(GalaxyUtils.getSpiritByName(payload.get("p2_support3").toString()).getTalent());

            if (Boolean.parseBoolean(payload.get("p1_gk").toString())) D.setP1GK(true);
            if (Boolean.parseBoolean(payload.get("p2_gk").toString())) D.setP2GK(true);

            if (Boolean.parseBoolean(payload.get("p1_volley").toString())) D.setVolley(true);

            return POWERDECIMAL.format(D.CalculateBaseValueP1()) + " /// " + POWERDECIMAL.format(D.CalculateBaseValueP2()) + " /// " + D.CalculateTotalValueP1() + " /// " + D.CalculateTotalValueP2()
                    + " /// " + D.BVFormula1 + " /// " + D.BVFormula2 + " /// " + D.TVFormula1 + " /// " + D.TVFormula2;
        } catch (Exception ignored) {
            return 0 + " /// " + 0 + " /// " + 0 + " /// " + 0 + " /// "
                    + " " + " /// "  + " " + " /// "  + " " + " /// " + " ";
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
}
