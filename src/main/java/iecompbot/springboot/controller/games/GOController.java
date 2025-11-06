package iecompbot.springboot.controller.games;

import ie.games.ds.go1.GOPlayer;
import ie.games.ds.go1.GOSpecialMove;
import ie.games.ds.go1.GOSpirit;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import static ie.Constants.InazumaGODB;
import static my.utilities.json.JSONItem.GSON;

@RestController
@CrossOrigin(origins = "*")
public class GOController {

    @PutMapping("/go1/players")
    public String GetGO1Players() {
        return GSON.toJson(InazumaGODB.retrieveItems("Player").mapAllTo(GOPlayer.class).getFirst());
    }
    @PutMapping("/go1/players/{id}")
    public String GetGO1PlayerByID(@PathVariable String id) {
        return GSON.toJson(InazumaGODB.retrieveItems("Player").where("ID = ?", id).mapAllTo(GOPlayer.class));
    }
    @PutMapping("/go1/moves")
    public String GetGO1Moves() {
        return GSON.toJson(InazumaGODB.retrieveItems("Move").mapAllTo(GOSpecialMove.class));
    }
    @PutMapping("/go1/moves/{id}")
    public String GetGO1MoveByID(@PathVariable String id) {
        return GSON.toJson(InazumaGODB.retrieveItems("Move").where("ID = ?", id).mapAllTo(GOSpecialMove.class));
    }
    @PutMapping("/go1/spirits")
    public String GetGO1Spirits() {
        return GSON.toJson(InazumaGODB.retrieveItems("Spirit").mapAllTo(GOSpirit.class));
    }
    @PutMapping("/go1/spirits/{id}")
    public String GetGO1SpiritByID(@PathVariable String id) {
        return GSON.toJson(InazumaGODB.retrieveItems("Spirit").where("ID = ?", id).mapAllTo(GOSpirit.class));
    }

}
