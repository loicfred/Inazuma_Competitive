package iecompbot.interaction.cmdbreakdown.clan;

import iecompbot.objects.clan.Clan;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ClanInviteCommand extends ClanMemberInteractCommand {

    public long Contract = 0;
    public boolean isPrivate = true;
    public boolean isReinforcement = false;

    public ClanInviteCommand(String command) {
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
            Contract = Long.parseLong(command.split("/")[4]);
        }
        if (command.split("/").length > 5) {
            isPrivate = Boolean.parseBoolean(command.split("/")[5]);
        }
        if (command.split("/").length > 6) {
            isReinforcement = Boolean.parseBoolean(command.split("/")[6]);
        }
    }
    public ClanInviteCommand(Clan clan) {
        super("");
        this.clan = clan;
        if (clan != null) {this.ClanID = clan.getID();}
    }
    public ClanInviteCommand(SlashCommandInteractionEvent event) {
        super("");
        MemberID = event.getOption("user").getAsUser().getIdLong();
        isPrivate =  event.getOption("private-message").getAsBoolean();
        isReinforcement = event.getOption("is-reinforcement") != null && event.getOption("is-reinforcement").getAsBoolean();
        if (event.getOption("contract") != null) {
            switch (event.getOption("contract").getAsString()) {
                case "1W" -> Contract = Instant.now().plus(7, ChronoUnit.DAYS).getEpochSecond();
                case "2W" -> Contract = Instant.now().plus(14, ChronoUnit.DAYS).getEpochSecond();
                case "1M" -> Contract = Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond();
                case "2M" -> Contract = Instant.now().plus(60, ChronoUnit.DAYS).getEpochSecond();
                case "3M" -> Contract = Instant.now().plus(90, ChronoUnit.DAYS).getEpochSecond();
            }
        }
        this.MyID = event.getUser().getIdLong();
        this.clan = Clan.getClanOfUser(event.getUser());
        if (clan != null) {this.ClanID = clan.getID();}
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public String toCommand() {
        return super.Command + "/" + ClanID + "/" + MyID + "/" + MemberID + "/" + Contract + "/" + isPrivate + "/" + isReinforcement;
    }
}

