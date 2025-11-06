package iecompbot.interaction.cmdbreakdown;

public class IDCommand extends CommandBreakdown {
    public Long ID;

    public IDCommand(String command) {
        super(command.split("/")[0]);
        if (command.split("/").length > 1) {
            ID = Long.parseLong(command.split("/")[1]);
        }
    }
    public IDCommand(long userid) {
        super("");
        this.ID = userid;
    }

    public String toCommand() {
        return super.Command + "/" + ID + "/";
    }
}