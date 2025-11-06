package iecompbot.interaction.cmdbreakdown;

public abstract class CommandBreakdown {
    public String Command;

    public CommandBreakdown(String c) {
        Command = c;
    }

    public String Command(String cmd) {
        Command = cmd;
        return toCommand();
    }

    public abstract String toCommand();
}
