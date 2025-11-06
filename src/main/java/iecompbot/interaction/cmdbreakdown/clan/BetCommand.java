package iecompbot.interaction.cmdbreakdown.clan;

import iecompbot.objects.match.Bet;
import iecompbot.interaction.cmdbreakdown.CommandBreakdown;

public class BetCommand extends CommandBreakdown {
    public long ID = 0;
    public int Min = 0;

    protected Bet bet = null;

    public BetCommand(String command) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1) {
            ID = Long.parseLong(command.split("/")[1]);
        }
        if (command.split("/").length > 2) {
            Min = Integer.parseInt(command.split("/")[2]);
        }
    }

    public BetCommand(Bet bet) {
        super("");
        this.bet = bet;
        if (bet != null) {
            this.ID = bet.getId();
        }
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }


    public String toCommand() {
        return super.Command + "/" + ID + "/" + Min + "/";
    }
}