package iecompbot.interaction.cmdbreakdown.clan;

import iecompbot.objects.clan.Clan;
import iecompbot.interaction.cmdbreakdown.CommandBreakdown;

public class ClanCommand extends CommandBreakdown {
    public long ClanID = 0;

    protected Clan clan = null;

    public ClanCommand(String command) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1) {
            ClanID = Long.parseLong(command.split("/")[1]);
        }
    }

    public ClanCommand(Clan clan) {
        super("");
        this.clan = clan;
        if (clan != null) {
            this.ClanID = clan.getID();
        }
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public Clan getTargetClan() {
        if (clan == null) {
            clan = Clan.get(ClanID);
        }
        return clan;
    }

    public String toCommand() {
        return super.Command + "/" + ClanID + "/";
    }
}