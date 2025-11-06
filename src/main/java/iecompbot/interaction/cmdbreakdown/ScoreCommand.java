package iecompbot.interaction.cmdbreakdown;

import iecompbot.objects.match.Game;
import iecompbot.objects.match.MatchLog;

public class ScoreCommand extends CommandBreakdown {
    public long RequesterID = 0;
    public long P1ID = 0;
    public long P2ID = 0;
    public int P1Score = 0;
    public int P2Score = 0;
    public Game Game;

    public ScoreCommand(String command) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1) {
            RequesterID = Long.parseLong(command.split("/")[1]);
        }
        if (command.split("/").length > 2) {
            P1ID = Long.parseLong(command.split("/")[2]);
        }
        if (command.split("/").length > 3) {
            P2ID = Long.parseLong(command.split("/")[3]);
        }
        if (command.split("/").length > 4) {
            P1Score = Integer.parseInt(command.split("/")[4]);
        }
        if (command.split("/").length > 5) {
            P2Score = Integer.parseInt(command.split("/")[5]);
        }
        if (command.split("/").length > 6) {
            Game = iecompbot.objects.match.Game.get(command.split("/")[6]);
        }
    }
    public ScoreCommand(MatchLog log) {
        super("");
        P1ID = log.getP1ID();
        P2ID = log.getP2ID();
        P1Score = log.getP1Score();
        P2Score = log.getP2Score();
        Game = log.getGame();
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public String toCommand() {
        return super.Command + "/" + RequesterID + "/" + P1ID + "/" + P2ID + "/" + P1Score + "/" + P2Score + "/" + Game;
    }
}