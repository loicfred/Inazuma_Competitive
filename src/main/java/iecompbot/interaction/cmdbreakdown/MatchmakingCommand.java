package iecompbot.interaction.cmdbreakdown;

import iecompbot.objects.match.Game;
import iecompbot.objects.server.ServerInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;

public class MatchmakingCommand extends CommandBreakdown {
    public Game Game = null;
    public Long ChannelID = null;
    public Long RoleID = null;


    public Channel Channel = null;
    public Role Role = null;

    public MatchmakingCommand(String command, Guild guild) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1 && !command.split("/")[1].equals("null")) {
            Game = iecompbot.objects.match.Game.get(command.split("/")[1]);
        }
        if (command.split("/").length > 2) {
            ChannelID = !command.split("/")[2].equals("null") ? Long.valueOf(command.split("/")[2]) : null;
            if (ChannelID != null) Channel = guild.getTextChannelById(ChannelID);
        }
        if (command.split("/").length > 3) {
            RoleID = !command.split("/")[3].equals("null") ? Long.valueOf(command.split("/")[3]) : null;
            if (RoleID != null) Role = guild.getRoleById(RoleID);
        }
    }
    public MatchmakingCommand(Game game) {
        super("");
        this.Game = game;
    }
    public MatchmakingCommand(ServerInfo.ServerInfo_Matchmaking I) {
        super("");
        this.Game = I.getGame();
        this.ChannelID = I.getChannelID();
        this.Channel = I.getChannel();
        this.RoleID = I.getRoleID();
        this.Role = I.getRole();
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public String toCommand() {
        return super.Command + "/" + Game + "/" + ChannelID + "/" + RoleID + "/";
    }
}