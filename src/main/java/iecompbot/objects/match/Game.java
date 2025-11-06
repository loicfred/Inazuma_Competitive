package iecompbot.objects.match;

import iecompbot.Constants;
import iecompbot.Main;
import iecompbot.objects.BotEmoji;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.net.URL;
import java.util.List;

import static iecompbot.L10N.TL;

public class Game {

    private String Code;
    private String Name;
    private String FullName;
    private long EmojiID;
    private long VSEmojiID;
    private String Colorcode;
    private String ImageURL;
    private int ScoreCooldown;


    public BotEmoji getEmoji() {
        return BotEmoji.get(EmojiID);
    }
    public BotEmoji getVSEmoji() {
        return BotEmoji.get(VSEmojiID);
    }
    public String getEmojiFormatted() {
        return getEmoji().getFormatted();
    }
    public String getVSEmojiFormatted() {
        return getVSEmoji().getFormatted();
    }


    public String getCode() {
        return Code;
    }
    public String getColorcode() {
        return Colorcode;
    }
    public String getName() {
        return Name;
    }
    public String getFullName() {
        return FullName;
    }
    public Color getColor() {
        return Color.decode(Colorcode);
    }
    public URL getLogoURL() {
        return Main.class.getResource("/static/img/games/" + getCode() + ".png");
    }
    public String getImageUrl() {
        return ImageURL;
    }
    public int getScoreCooldownMinutes() {
        return ScoreCooldown;
    }

    public static List<Game> values() {
        return Constants.allGame.isEmpty() ? Constants.allGame = DatabaseObject.getAll(Game.class) : Constants.allGame;
    }
    public static List<SelectOption> getSelectOptions(InteractionHook M, List<Game> defaults) {
        return getSelectOptions(M.getInteraction(), defaults);
    }
    public static List<SelectOption> getSelectOptions(Interaction M, List<Game> defaults) {
        return values().stream().map(G -> SelectOption.of(G.getCode(), G.getCode()).withDescription(TL(M, "view-of", G.getFullName())).withDefault(defaults != null && defaults.contains(G)).withEmoji(G.getEmoji().retrieve())).toList();
    }

    public boolean is3DS() {
        return this.Code.equals("IEGO1") || this.Code.equals("IEGOCS") || this.Code.equals("IEGOGLX");
    }
    public boolean isNDS() {
        return this.Code.equals("IE1") || this.Code.equals("IE2") || this.Code.equals("IE3");
    }
    public boolean isVR() {
        return this.Code.equals("IEVR") || this.Code.equals("IEVRBETA");
    }
    public boolean isStrikers() {
        return this.Code.equals("IEGOSTRXTR") || this.Code.equals("IEGOSTR");
    }

    public static Game get(String game) {
        if (game != null) {
            if (game.toLowerCase().contains("sd") || game.toLowerCase().contains("sd") || game.toLowerCase().contains("inazuma eleven sd")) {
                return getByCode("IESD");
            }
            if (game.toLowerCase().contains("ievrbeta") || game.toLowerCase().contains("beta")) {
                return getByCode("IEVRBETA");
            }
            if (game.toLowerCase().contains("ievr") || game.toLowerCase().contains("victory") || game.toLowerCase().contains("vr") || game.toLowerCase().contains("vroh") || game.toLowerCase().contains("victory road")) {
                return getByCode("IEVR");
            }
            if (game.toLowerCase().contains("iegostrxtr") || game.toLowerCase().contains("xtreme") || game.toLowerCase().contains("xtr") || game.toLowerCase().contains("strikers xtreme") || game.toLowerCase().contains("inazuma eleven go strikers 2013 xtreme")) {
                return getByCode("IEGOSTRXTR");
            }
            if (game.toLowerCase().contains("iegostr") || game.toLowerCase().contains("striker") || game.toLowerCase().contains("2013") || game.toLowerCase().contains("inazuma eleven go strikers 2013")) {
                return getByCode("IEGOSTR");
            }
            if (game.toLowerCase().contains("iegoglx") || game.toLowerCase().contains("galaxy") || game.toLowerCase().contains("glx") || game.toLowerCase().contains("inazuma eleven go galaxy")) {
                return getByCode("IEGOGLX");
            }
            if (game.toLowerCase().contains("iegocs") || game.toLowerCase().contains("cs") || game.toLowerCase().contains("chrono") || game.contains("stone") || game.toLowerCase().contains("inazuma eleven go chrono stone")) {
                return getByCode("IEGOCS");
            }
            if (game.toLowerCase().contains("iego1") || game.toLowerCase().contains("go1") || game.toLowerCase().contains("inazuma eleven go")) {
                return getByCode("IEGO1");
            }
            if (game.toLowerCase().contains("ie3") || game.toLowerCase().contains("ie 3") || game.toLowerCase().contains("inazuma eleven 3")) {
                return getByCode("IE3");
            }
            if (game.toLowerCase().contains("ie2") || game.toLowerCase().contains("ie 2") || game.toLowerCase().contains("inazuma eleven 2")) {
                return getByCode("IE2");
            }
        }
        return getByCode("IE1");
    }

    @Override
    public String toString() {
        return Code;
    }

    private Game() {}

    public static Game getByCode(String code) {
        return values().stream().filter(l -> l.getCode().equals(code)).findFirst().orElse(null);
    }

}