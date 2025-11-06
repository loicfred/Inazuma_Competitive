package iecompbot.interaction.cmdbreakdown;

import iecompbot.objects.match.Game;
import iecompbot.objects.Nationality;
import net.dv8tion.jda.api.entities.Guild;

import static my.utilities.util.Utilities.replaceLast;

public class MercatoCommand extends CommandBreakdown {
    public int Page = 1;
    public Game Game = null;
    public Nationality Nationality = null;
    public boolean IncludeClan = false;
    public String PlayerData;

    public Guild G = null;

    public MercatoCommand(String command) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1) {
            Page = Integer.parseInt(command.split("/")[1]);
        }
        if (command.split("/").length > 2 && !command.split("/")[2].equals("null")) {
            Game = iecompbot.objects.match.Game.get(command.split("/")[2]);
        }
        if (command.split("/").length > 3 && !command.split("/")[3].equals("null")) {
            Nationality = iecompbot.objects.Nationality.get(command.split("/")[3]);
        }
        if (command.split("/").length > 4) {
            IncludeClan = Boolean.parseBoolean(command.split("/")[4]);
        }
        if (command.split("/").length > 5) {
            PlayerData = command.split("/")[5];
        }
    }

    public String getGameCode() {
        return Game == null ? null : Game.getCode();
    }

    public String getNationality() {
        return Nationality == null ? null : Nationality.getName();
    }

    public String listFilter() {
        String f = "";
        if (Nationality != null) {
            f = f + ", " + Nationality;
        }
        if (IncludeClan) {
            f = f + ", InClan";
        }
        if (Game != null) {
            f = f + ", " + Game;
        }
        return replaceLast(f.length() > 2 ? f.substring(2) : f, ", ", " &");
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public String toCommand() {
        return super.Command + "/" + Page + "/" + Game + "/" + Nationality + "/" + IncludeClan + "/" + PlayerData + "/";
    }
}