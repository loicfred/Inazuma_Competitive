package iecompbot.interaction.cmdbreakdown.clan;

import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.clan.ClanRole;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ClanRoleManager extends ClanCommand {

    public long MyID = 0;
    public long ClanRoleID = 0;

    private ClanMember you = null;

    public ClanRoleManager(String command) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1) {
            ClanID = Long.parseLong(command.split("/")[1]);
        }
        if (command.split("/").length > 2) {
            MyID = Long.parseLong(command.split("/")[2]);
        }
        if (command.split("/").length > 3) {
            ClanRoleID = Long.parseLong(command.split("/")[3]);
        }
    }
    public ClanRoleManager(Clan clan) {
        super("");
        this.clan = clan;
        if (clan != null) {this.ClanID = clan.getId();}
    }
    public ClanRoleManager(Clan clan, User me, ClanRole CR) {
        super("");
        this.clan = clan;
        this.MyID = me.getIdLong();
        this.ClanRoleID = CR.getId();
        if (clan != null) {this.ClanID = clan.getId();}
    }
    public ClanRoleManager(SlashCommandInteractionEvent event) {
        super("");
        this.MyID = event.getUser().getIdLong();
        this.clan = Clan.getClanOfUser(this.MyID);
        if (clan != null) {this.ClanID = clan.getId();}
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public ClanMember getMe() {
        if (you == null) {you = getTargetClan().getMemberById(MyID);}
        return you;
    }
    public String toCommand() {
        return super.Command + "/" + ClanID + "/" + MyID  + "/" + ClanRoleID + "/";
    }
}
