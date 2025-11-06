package iecompbot.interaction.cmdbreakdown.clan;

import iecompbot.objects.match.Game;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ClanManager extends ClanCommand {

    public String InfoType = "null";
    public long MemberID = 0;
    public long MyID = 0;
    public String GamesType = "All";
    public long Contract = 0;

    private ClanMember member = null;
    private ClanMember you = null;

    public ClanManager(String command) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1) {
            ClanID = Long.parseLong(command.split("/")[1]);
        }
        if (command.split("/").length > 2) {
            MyID = Long.parseLong(command.split("/")[2]);
        }
        if (command.split("/").length > 3) {
            MemberID = Long.parseLong(command.split("/")[3]);
        }
        if (command.split("/").length > 4) {
            InfoType = command.split("/")[4];
        }
        if (command.split("/").length > 5) {
            GamesType = command.split("/")[5];
        }
        if (command.split("/").length > 6) {
            Contract = Long.parseLong(command.split("/")[6]);
        }
    }
    public ClanManager(Clan clan) {
        super("");
        this.clan = clan;
        if (clan != null) {this.ClanID = clan.getId();}
    }
    public ClanManager(Clan clan, User me) {
        super("");
        this.clan = clan;
        this.MyID = me.getIdLong();
        if (clan != null) {this.ClanID = clan.getId();}
    }
    public ClanManager(SlashCommandInteractionEvent event) {
        super("");
        this.MyID = event.getUser().getIdLong();
        this.clan = Clan.getClanOfUser(this.MyID);
        if (clan != null) {this.ClanID = clan.getId();}
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public ClanMember getTargetMember() {
        if (member == null) {member = getTargetClan().getMemberById(MemberID);}
        return member;
    }
    public ClanMember getMe() {
        if (you == null) {you = getTargetClan().getMemberById(MyID);}
        return you;
    }
    public ClanMember getMe(long userid) {
        MyID = userid;
        if (you == null) {you = getTargetClan().getMemberById(MyID);}
        return you;
    }

    public Game getGamesType() {
        if (GamesType == null) return null;
        return Game.get(GamesType);
    }

    public String toCommand() {
        return super.Command + "/" + ClanID + "/" + MyID + "/" + MemberID + "/" + InfoType + "/" + GamesType + "/" + Contract + "/";
    }
}
