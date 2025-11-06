package iecompbot.interaction.cmdbreakdown.server;

import iecompbot.interaction.cmdbreakdown.IDCommand;
import iecompbot.objects.match.Game;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class RankingCommand extends IDCommand {
    private Game G = null;

    public long UserID;
    public String GameCode = null;
    public RankingCommand(String command) {
        super(command);
        if (command.split("/").length > 2) {
            UserID = Long.parseLong(command.split("/")[2]);
        }
        if (command.split("/").length > 3) {
            GameCode = command.split("/")[3];
        }
    }
    public RankingCommand(Guild G, User user) {
        super("");
        this.ID = G.getIdLong();
        this.UserID = user.getIdLong();
    }

    public Game getGame() {
        if (GameCode.equals("null")) return null;
        return G == null ? G = Game.get(GameCode) : G;
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }


    public String toCommand() {
        return super.Command + "/" + ID + "/" + UserID  + "/" + GameCode;
    }
}
