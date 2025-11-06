package iecompbot.interaction.cmdbreakdown.clan;

import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ClanMemberInteractCommand extends ClanCommand {

    public long MyID = 0;
    public long MemberID = 0;

    private ClanMember member = null;
    private ClanMember you = null;

    public ClanMemberInteractCommand(String command) {
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
    }
    public ClanMemberInteractCommand(Clan clan) {
        super("");
        this.clan = clan;
        if (clan != null) {this.ClanID = clan.getId();}
    }
    public ClanMemberInteractCommand(SlashCommandInteractionEvent event) {
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
        if (member == null) {
            member = getTargetClan().getMemberById(MemberID);
            member.parentClan = getTargetClan();
        }
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


    public String toCommand() {
        return super.Command + "/" + ClanID + "/" + MyID + "/" + MemberID;
    }
}

