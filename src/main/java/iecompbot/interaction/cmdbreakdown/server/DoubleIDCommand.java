package iecompbot.interaction.cmdbreakdown.server;

import iecompbot.interaction.cmdbreakdown.IDCommand;

public class DoubleIDCommand extends IDCommand {
    public Long ObjectID;

    public DoubleIDCommand(String command) {
        super(command);
        if (command.split("/").length > 2) {
            ObjectID = Long.parseLong(command.split("/")[2]);
        }
    }
    public DoubleIDCommand(long Id, long objId) {
        super("");
        ID = Id;
        ObjectID = objId;
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public String toCommand() {
        return super.Command + "/" + ID + "/" + ObjectID + "/";
    }
}