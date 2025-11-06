package iecompbot.interaction.cmdbreakdown.profile;

import iecompbot.interaction.cmdbreakdown.IDCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterCommand extends IDCommand {
    public List<String> Filter = new ArrayList<>();

    public FilterCommand(String command) {
        super(command);
        if (command.split("/").length > 2) {
            Filter = parseList(command.split("/")[2]);
        }
    }
    public FilterCommand(long id, String command) {
        super(command);
        this.ID = id;
    }

    public String toCommand() {
        return super.Command + "/" + ID + "/" + Filter + "/";
    }

    public static List<String> parseList(String str) {
        if (str.equals("[]")) return new ArrayList<>();
        str = str.substring(1, str.length() - 1);
        List<String> elements = new ArrayList<>();
        Collections.addAll(elements, str.split(",\\s*"));
        return elements;
    }

    public Long getServerID() {
        return Filter.stream().filter(s -> s.startsWith("S")).findFirst().map(s -> Long.parseLong(s.substring(1))).orElse(null);
    }
}
