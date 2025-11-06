package iecompbot.objects.match;

import iecompbot.Constants;
import iecompbot.objects.BotEmoji;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.Icon;

import java.awt.*;
import java.time.Instant;
import java.util.List;

import static iecompbot.Main.DefaultURL;
import static iecompbot.springboot.config.AppConfig.cacheService;

public class League extends DatabaseObject<League> {

    private long ID;
    private Long ServerID;
    private long EmojiID;
    private long TierID;
    private int Start;
    private int End;
    private double Power;
    private Double Multiplier;
    private String Name;
    public byte[] Image;

    private League() {}
    public League(long tierId, long serverId, String name, int start, int end, double power, long emojiID) {
        this.ID = Instant.now().toEpochMilli();
        ServerID = serverId;
        Name = name;
        Start = start;
        End = end;
        Power = power;
        EmojiID = emojiID;
        TierID = tierId;
        Write();
    }

    public long getId() {
        return ID;
    }
    public long getTierID() {
        return TierID;
    }
    public Long getServerID() {
        return ServerID;
    }
    public byte[] getImage() {
        return Image;
    }
    public String getName() {
        return Name;
    }
    public int getStart() {
        return Start;
    }
    public int getEnd() {
        return End;
    }
    public double getPower() {
        return Power;
    }
    public double getMultiplier() {
        return Multiplier;
    }
    public String getImageURL() {
        return DefaultURL + "/api/img/league/" + getId() + ".png";
    }

    public void setImage(byte[] file) {
        Image = file;
    }
    public void setStart(int start) {
        Start = start;
    }
    public void setEmojiID(long emojiID) {
        EmojiID = emojiID;
    }
    public void setName(String name) {
        Name = name;
    }
    public void setEnd(int end) {
        End = end;
    }
    public void setPower(double pow) {
        Power = pow;
    }

    public String getEmojiFormatted() {
        return getEmoji().getFormatted();
    }

    public BotEmoji getEmoji() {
        return BotEmoji.get(ServerID, EmojiID);
    }

    public League_Tier getTier() {
        return cacheService.getCachedLeagueTier(TierID);
    }
    public static League get(long id) {
        return cacheService.getCachedLeague(id);
    }
    public static League getByMedal(int currentMedals) {
        return listGlobal().stream().filter(l -> l.getStart() <= Math.max(0,currentMedals) && Math.max(0,currentMedals) <= l.getEnd()).findFirst().orElseGet(() -> listGlobal().getFirst());
    }
    public static List<League> listGlobal() {
        return Constants.GlobalLeagues.isEmpty() ? Constants.GlobalLeagues = getAllWhere(League.class, "ServerID IS NULL ORDER BY Start ASC") : Constants.GlobalLeagues;
    }

    @Override
    public String toString() {
        return getEmojiFormatted();
    }

    public static class League_Tier extends DatabaseObject<League_Tier> {
        private transient List<League> leagues = null;

        private long ID;
        private Long ServerID;
        private long EmojiID;
        private int Start;
        private int End;
        private String Name;
        private String Colorcode;
        public byte[] Image;

        private League_Tier() {}
        public League_Tier(long serverId, String name, String colorcode, int start, int end, long emojiID) {
            ID = Instant.now().toEpochMilli();
            ServerID = serverId;
            Name = name;
            Colorcode = colorcode;
            Start = start;
            End = end;
            EmojiID = emojiID;
            Write();
        }

        public long getId() {
            return ID;
        }
        public Long getServerID() {
            return ServerID;
        }
        public String getName() {
            return Name;
        }
        public byte[] getImage() {
            return Image;
        }
        public Color getColor() {
            return Color.decode(Colorcode);
        }
        public int getStart() {
            return Start;
        }
        public int getEnd() {
            return End;
        }
        public String getImageURL() {
            return DefaultURL + "/api/img/league/tier/" + getId() + ".png";
        }


        public void setStart(int start) {
            Start = start;
        }
        public void setEmojiID(long emojiID) {
            EmojiID = emojiID;
        }
        public void setImage(byte[] file) {
            Image = file;
        }
        public void setName(String name) {
            Name = name;
        }
        public void setEnd(int end) {
            End = end;
        }
        public void setColorcode(String colorcode) {
            Colorcode = colorcode;
        }

        public Icon getTierIcon() {
            return Icon.from(Image);
        }
        public String getTierEmojiFormatted() {
            return getTierEmoji().getFormatted();
        }
        public BotEmoji getTierEmoji() {
            return BotEmoji.get(ServerID, EmojiID);
        }

        public List<League> getLeagues() {
            return leagues == null ? leagues = getAllWhere(League.class, "TierID = ? ORDER BY Start ASC", getId()) : leagues;
        }

        public static League_Tier get(long id) {
            return cacheService.getCachedLeagueTier(id);
        }
        public static League_Tier getByMedal(int currentMedals) {
            return list().stream().filter(l -> l.Start <= Math.max(0,currentMedals) && Math.max(0,currentMedals) <= l.End).findFirst().orElseGet(() -> list().getFirst());
        }
        public static List<League_Tier> list() {
            return Constants.GlobalLeagueTiers.isEmpty() ? Constants.GlobalLeagueTiers = getAllWhere(League_Tier.class, "ServerID IS NULL ORDER BY Start ASC") : Constants.GlobalLeagueTiers;
        }
    }
}