package iecompbot.interaction.cmdbreakdown;

public class PageViewerCommand extends IDCommand {

    public int Page = 1;;

    public PageViewerCommand(String command) {
        super(command);
        if (command.split("/").length > 2) {
            Page = Integer.parseInt(command.split("/")[2]);
        }
    }
    public PageViewerCommand(long id, String command) {
        super(command);
        this.ID = id;
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public String toCommand() {
        return super.Command + "/" + ID + "/" + Page + "/";
    }

}
