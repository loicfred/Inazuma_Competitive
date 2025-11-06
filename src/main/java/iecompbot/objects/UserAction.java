package iecompbot.objects;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.time.Instant;

import static iecompbot.Constants.LogChannel;
import static iecompbot.Utility.getHHmmss;
import static iecompbot.interaction.Automation.LogSlash;
import static my.utilities.util.Utilities.CutString;
import static my.utilities.util.Utilities.StopString;

@SuppressWarnings("all")
public class UserAction {
    public static void sendPrivateMessage(User user, String message) {
        sendPrivateMessage(user, new MessageCreateBuilder().addContent(StopString(message, 2000)));
    }
    public static void sendPrivateMessage(User user, MessageCreateBuilder M) {
        try {
            if (user != null) {
                user.openPrivateChannel().queue(channel -> { // MY DM
                    channel.sendMessage(M.build()).queue(message2 -> {
                        MessageCreateAction MM = LogChannel.sendMessage("**[DM]** Sent to " + user.getEffectiveName() + " (" + user.getId() + "):\n- " + CutString(M.getContent(), 1800));
                        if (!M.getEmbeds().isEmpty()) MM.setEmbeds(M.getEmbeds());
                        if (!M.getAttachments().isEmpty()) MM.addFiles(M.getAttachments());
                        if (!M.getComponents().isEmpty()) MM.setComponents(M.getComponents());
                        MM.queue();
                    }, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, error -> {
                        System.out.println("[" + getHHmmss(Instant.now()) + "] Failed to message " + user.getEffectiveName());
                        LogSlash("**[DM]** Failed to send to " + user.getEffectiveName() + " (" + user.getId() + "):\n- " + M.getContent());
                    }));
                });
            }
        } catch (Exception ignored) {}
    }

}
