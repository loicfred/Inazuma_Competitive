package iecompbot.springboot.controller.games;

import ie.games.ds.go1.GOPlayer;
import ie.games.ds.go1.GOSpecialMove;
import ie.games.ds.go1.GOSpirit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static ie.Constants.InazumaGODB;

@Tag(name = "Inazuma Eleven GO", description = "Endpoints to access data from GO1 database.")
@RestController
@CrossOrigin(origins = "*")
public class GOController {

    @Operation(summary = "Get a list of all GO1 players")
    @GetMapping("/go1/players.json")
    public List<GOPlayer> GetGO1Players() {
        return InazumaGODB.retrieveItems("Player").mapAllTo(GOPlayer.class);
    }
    @Operation(summary = "Get a list of a GO1 player by ID")
    @GetMapping("/go1/players/{id}.json")
    public GOPlayer GetGO1PlayerByID(@PathVariable String id) {
        return InazumaGODB.retrieveItems("Player").where("ID = ?", id).mapFirstTo(GOPlayer.class);
    }
    @Operation(summary = "Get a list of all GO1 special moves")
    @GetMapping("/go1/moves.json")
    public List<GOSpecialMove> GetGO1Moves() {
        return InazumaGODB.retrieveItems("Move").mapAllTo(GOSpecialMove.class);
    }
    @Operation(summary = "Get a list of a GO1 special move by ID")
    @GetMapping("/go1/moves/{id}.json")
    public GOSpecialMove GetGO1MoveByID(@PathVariable String id) {
        return InazumaGODB.retrieveItems("Move").where("ID = ?", id).mapFirstTo(GOSpecialMove.class);
    }
    @Operation(summary = "Get a list of all GO1 fighting spirits")
    @GetMapping("/go1/spirits.json")
    public List<GOSpirit> GetGO1Spirits() {
        return InazumaGODB.retrieveItems("Spirit").mapAllTo(GOSpirit.class);
    }
    @Operation(summary = "Get a list of a GO1 fighting spirit by ID")
    @GetMapping("/go1/spirits/{id}.json")
    public GOSpirit GetGO1SpiritByID(@PathVariable String id) {
        return InazumaGODB.retrieveItems("Spirit").where("ID = ?", id).mapFirstTo(GOSpirit.class);
    }

}
