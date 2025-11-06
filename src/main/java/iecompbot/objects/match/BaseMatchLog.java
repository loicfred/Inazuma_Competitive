package iecompbot.objects.match;

import java.time.Instant;

public class BaseMatchLog<T> extends BaseDuel<T> {
    protected transient Game g;

    public int P1Medals = 0;
    public int P2Medals = 0;
    public Long ServerID;
    public long ChannelID = 0;
    public long MessageID = 0;

    public Instant getTimeCreated() {
        return Instant.ofEpochMilli(getId());
    }

    public int getP1Medals() {
        return P1Medals;
    }

    public int getP2Medals() {
        return P2Medals;
    }

    public Long getServerID() {
        return ServerID;
    }

    public long getChannelID() {
        return ChannelID;
    }

    public long getMessageID() {
        return MessageID;
    }
}
