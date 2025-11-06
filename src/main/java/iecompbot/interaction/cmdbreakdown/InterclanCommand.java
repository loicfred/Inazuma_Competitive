package iecompbot.interaction.cmdbreakdown;

import iecompbot.objects.clan.interclan.Interclan;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class InterclanCommand extends CommandBreakdown {

    public Interclan I = null;
    private ClanMember you = null;
    private Clan Host = null;
    private Clan Join = null;

    public int Page = 1;
    public long InterclanID = 0;
    public long MyID = 0;
    public long HostClan = 0;
    public long JoinClan = 0;
    public int RuleMatches = 0;
    public String RuleDistribution = null;


    public InterclanCommand(String command) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1) {
            Page = Integer.parseInt(command.split("/")[1]);
        }
        if (command.split("/").length > 2) {
            I = Interclan.get(InterclanID = Long.parseLong(command.split("/")[2]));
        }
        if (command.split("/").length > 3) {
            MyID = Long.parseLong(command.split("/")[3]);
        }
        if (command.split("/").length > 4) {
            Host = Clan.get(HostClan = Long.parseLong(command.split("/")[4]));
        }
        if (command.split("/").length > 5) {
            Join = Clan.get(JoinClan = Long.parseLong(command.split("/")[5]));
        }
        if (command.split("/").length > 6) {
            RuleMatches = Integer.parseInt(command.split("/")[6]);
        }
        if (command.split("/").length > 7) {
            RuleDistribution = command.split("/")[7];
        }
    }
    public InterclanCommand(long interclanid) {
        super("");
        InterclanID = interclanid;
        I = Interclan.get(InterclanID);
    }
    public InterclanCommand(SlashCommandInteractionEvent event) {
        super("");
        this.MyID = event.getUser().getIdLong();
    }

    public InterclanCommand(SlashCommandInteractionEvent event, long interclanid) {
        super("");
        this.MyID = event.getUser().getIdLong();
        InterclanID = interclanid;
        I = Interclan.get(InterclanID);
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public Clan getHostClan() {
        if (Host == null) Host = Clan.get(HostClan);
        return Host;
    }
    public Clan getJoinClan() {
        if (Join == null) Join = Clan.get(JoinClan);
        return Join;
    }
    public ClanMember getMe() {
        if (you == null) {you = Host.getMemberById(MyID);}
        return you;
    }
    public ClanMember getMe(long userid) {
        MyID = userid;
        if (you == null) {you = Host.getMemberById(MyID);}
        return you;
    }

    public String toCommand() {
        return super.Command + "/" + Page + "/" + InterclanID + "/" + HostClan + "/" + JoinClan + "/" + RuleMatches + "/" + RuleDistribution;
    }
}
