package iecompbot.interaction.cmdbreakdown.server;

import iecompbot.interaction.cmdbreakdown.CommandBreakdown;

public class ChallongeCommand extends CommandBreakdown {

    public long ChallongeID = 0;
    public long ParticipantID = 0;
    public long MatchID = 0;
    public int Round = 0;
    public int GroupID = 0;

    public ChallongeCommand(String command) {
        super(command.split("/")[0]);
        command = command.replaceAll("info/1/", "info/");
        if (command.split("/").length > 1) {
            ChallongeID = Long.parseLong(command.split("/")[1]);
        }
        if (command.split("/").length > 2) {
            ParticipantID = Long.parseLong(command.split("/")[2]);
        }
        if (command.split("/").length > 3) {
            MatchID = Long.parseLong(command.split("/")[3]);
        }
        if (command.split("/").length > 4) {
            Round = Integer.parseInt(command.split("/")[4]);
        }
        if (command.split("/").length > 5) {
            GroupID = Integer.parseInt(command.split("/")[5]);
        }
    }
    public ChallongeCommand(long challongeId) {
        super("");
        ChallongeID = challongeId;
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public String toCommand() {
        return super.Command + "/" + ChallongeID + "/" + ParticipantID + "/" + MatchID + "/" + Round + "/" + GroupID;
    }
}