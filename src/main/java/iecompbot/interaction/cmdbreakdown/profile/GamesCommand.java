package iecompbot.interaction.cmdbreakdown.profile;

import iecompbot.interaction.cmdbreakdown.IDCommand;
import iecompbot.objects.match.Game;

import java.util.ArrayList;
import java.util.List;

public class GamesCommand extends IDCommand {
    public List<Game> Games = new ArrayList<>();

    public GamesCommand(String command) {
        super(command);
        if (command.split("/").length > 2) {
            Games = parseGamesToList(command.split("/")[2]);
        }
    }
    public GamesCommand(long id, String command) {
        super(command);
        this.ID = id;
    }

    public String toCommand() {
        return super.Command + "/" + ID + "/" + Games + "/";
    }

    public List<Game> parseGamesToList(String str) {
        if (str.equals("[]")) return new ArrayList<>();
        // Remove the square brackets
        str = str.substring(1, str.length() - 1);

        // Split the string by the comma and trim any whitespace
        List<Game> elements = new ArrayList<>();
        for (String g : str.split(",\\s*")) {
            elements.add(Game.get(g));
        }

        // Convert the array to a list and return it
        return elements;
    }
}
