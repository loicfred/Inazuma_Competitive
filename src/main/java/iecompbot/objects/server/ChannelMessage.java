package iecompbot.objects.server;

import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static iecompbot.Main.DiscordAccount;
import static iecompbot.interaction.Automation.getWebhookOfChannel;

public class ChannelMessage {
    private transient Guild Guild;
    private transient TextChannel C = null;
    private transient Message M1 = null;
    private transient Message M2 = null;
    private transient Message M3 = null;
    private transient Message M4 = null;

    public long ServerID;
    public Long ChannelID = null;
    public Long MessageID = null;
    public Long MessageID2 = null;
    public Long MessageID3 = null;
    public Long MessageID4 = null;



    public ChannelMessage(long serverid, Long channelid) {
        this.ServerID = serverid;
        this.ChannelID = channelid;
    }
    public ChannelMessage(long serverid, Long channelid, Long messageID) {
        this.ServerID = serverid;
        this.ChannelID = channelid;
        this.MessageID = messageID;
    }
    public ChannelMessage(long serverid, Long channelid, Long messageID, Long messageID2) {
        this.ServerID = serverid;
        this.ChannelID = channelid;
        this.MessageID = messageID;
        this.MessageID2 = messageID2;
    }
    public ChannelMessage(long serverid, Long channelid, Long messageID, Long messageID2, Long messageID3) {
        this.ServerID = serverid;
        this.ChannelID = channelid;
        this.MessageID = messageID;
        this.MessageID2 = messageID2;
        this.MessageID3 = messageID3;
    }
    public ChannelMessage(long serverid, long channelid, long messageID, long messageID2, long messageID3, long messageID4) {
        this.ServerID = serverid;
        this.ChannelID = channelid;
        this.MessageID = messageID;
        this.MessageID2 = messageID2;
        this.MessageID3 = messageID3;
        this.MessageID4 = messageID4;
    }

    public Guild getGuild() {
        return Guild == null ? Guild = DiscordAccount.getGuildById(ServerID) : Guild;
    }

    public TextChannel getChannel() {
        if (C == null) {
            try {
                C = getGuild().getTextChannelById(ChannelID);
            } catch (Exception ignored) {}
        }
        return C;
    }
    public Message getMessage() {
        if (M1 == null) {
            try {
                M1 = getChannel().retrieveMessageById(MessageID).submit().orTimeout(10, TimeUnit.SECONDS).get();
            } catch (Exception ignored) {}
        }
        return M1;
    }
    public Message getMessage2() {
        if (M2 == null) {
            try {
                M2 = getChannel().retrieveMessageById(MessageID2).submit().orTimeout(10, TimeUnit.SECONDS).get();
            } catch (Exception ignored) {}
        }
        return M2;
    }
    public Message getMessage3() {
        if (M3 == null) {
            try {
                M3 = getChannel().retrieveMessageById(MessageID3).submit().orTimeout(10, TimeUnit.SECONDS).get();
            } catch (Exception ignored) {}
        }
        return M3;
    }
    public Message getMessage4() {
        if (M4 == null) {
            try {
                M4 = getChannel().retrieveMessageById(MessageID4).submit().orTimeout(10, TimeUnit.SECONDS).get();
            } catch (Exception ignored) {}
        }
        return M4;
    }

    public Message getMessageElseCreate() {
        if (getMessage() == null) {
            try {
                M1 = getChannel().sendMessage("Waiting...").submit().orTimeout(10, TimeUnit.SECONDS).get();
                MessageID = M1.getIdLong();
            } catch (Exception ignored) {}
        }
        return getMessage();
    }
    public Message getMessage2ElseCreate() {
        if (getMessage2() == null) {
            try {
                M2 = getChannel().sendMessage("Waiting...").submit().orTimeout(10, TimeUnit.SECONDS).get();
                MessageID2 = M2.getIdLong();
            } catch (Exception ignored) {}
        }
        return getMessage2();
    }
    public Message getMessage3ElseCreate() {
        if (getMessage3() == null) {
            try {
                M3 = getChannel().sendMessage("Waiting...").submit().orTimeout(10, TimeUnit.SECONDS).get();
                MessageID3 = M3.getIdLong();
            } catch (Exception ignored) {}
        }
        return getMessage3();
    }
    public Message getMessage4ElseCreate() {
        if (getMessage4() == null) {
            try {
                M4 = getChannel().sendMessage("Waiting...").submit().orTimeout(10, TimeUnit.SECONDS).get();
                MessageID4 = M4.getIdLong();
            } catch (Exception ignored) {}
        }
        return getMessage4();
    }

    public void ModifyWebhookMessageElseCreate(WebhookMessageBuilder E, Consumer<ReadonlyMessage> callback) {
        getWebhookOfChannel(getChannel(), CC -> {
            try {
                if (getMessage() == null || !getMessage().isWebhookMessage()) {
                    if (getMessage() != null && !getMessage().isWebhookMessage()) getMessage().delete().queue();
                    CC.send(E.build()).whenComplete((msg, ex) -> {
                        MessageID = msg.getId();
                        callback.accept(msg);
                    });
                } else CC.edit(MessageID, E.build()).whenComplete((msg, ex) -> callback.accept(msg));
            } catch (Exception ignored) {
                callback.accept(null);
            }
        });
    }
    public void ModifyWebhookMessage2ElseCreate(WebhookMessageBuilder E, Consumer<ReadonlyMessage> callback) {
        getWebhookOfChannel(getChannel(), CC -> {
            try {
                if (getMessage2() == null || !getMessage2().isWebhookMessage()) {
                    if (getMessage2() != null && !getMessage2().isWebhookMessage()) getMessage2().delete().queue();
                    CC.send(E.build()).whenComplete((msg, ex) -> {
                        MessageID2 = msg.getId();
                        callback.accept(msg);
                    });
                } else CC.edit(MessageID2, E.build()).whenComplete((msg, ex) -> callback.accept(msg));
            } catch (Exception ignored) {
                callback.accept(null);
            }
        });
    }
    public void ModifyWebhookMessage3ElseCreate(WebhookMessageBuilder E, Consumer<ReadonlyMessage> callback) {
        getWebhookOfChannel(getChannel(), CC -> {
            try {
                if (getMessage3() == null || !getMessage3().isWebhookMessage()) {
                    if (getMessage3() != null && !getMessage3().isWebhookMessage()) getMessage3().delete().queue();
                    CC.send(E.build()).whenComplete((msg, ex) -> {
                        MessageID3 = msg.getId();
                        callback.accept(msg);
                    });
                } else CC.edit(MessageID3, E.build()).whenComplete((msg, ex) -> callback.accept(msg));
            } catch (Exception ignored) {
                callback.accept(null);
            }
        });
    }
    public void ModifyWebhookMessage4ElseCreate(WebhookMessageBuilder E, Consumer<ReadonlyMessage> callback) {
        getWebhookOfChannel(getChannel(), CC -> {
            try {
                if (getMessage4() == null || !getMessage4().isWebhookMessage()) {
                    if (getMessage4() != null && !getMessage4().isWebhookMessage()) getMessage4().delete().queue();
                    CC.send(E.build()).whenComplete((msg, ex) -> {
                        MessageID4 = msg.getId();
                        callback.accept(msg);
                    });
                } else CC.edit(MessageID4, E.build()).whenComplete((msg, ex) -> callback.accept(msg));
            } catch (Exception ignored) {
                callback.accept(null);
            }
        });
    }

    public String getMessageLink1() {
        return "https://discord.com/channels/" + ServerID + "/" + ChannelID + "/" + MessageID;
    }
    public String getMessageLink2() {
        return "https://discord.com/channels/" + ServerID + "/" + ChannelID + "/" + MessageID2;
    }
    public String getMessageLink3() {
        return "https://discord.com/channels/" + ServerID + "/" + ChannelID + "/" + MessageID3;
    }
    public String getMessageLink4() {
        return "https://discord.com/channels/" + ServerID + "/" + ChannelID + "/" + MessageID4;
    }

    public void DeleteMessage1() {
        try {
            getMessage().delete().queue();
            M1 = null;
            MessageID = null;
        } catch (Exception ignored) {}
    }
    public void DeleteMessage2() {
        try {
            getMessage2().delete().queue();
            M2 = null;
            MessageID2 = null;
        } catch (Exception ignored) {}
    }
    public void DeleteMessage3() {
        try {
            getMessage3().delete().queue();
            M3 = null;
            MessageID3 = null;
        } catch (Exception ignored) {}
    }
    public void DeleteMessage4() {
        try {
            getMessage4().delete().queue();
            M4 = null;
            MessageID4 = null;
        } catch (Exception ignored) {}
    }

}
