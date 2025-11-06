package iecompbot.objects;

import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Formatter;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static iecompbot.Main.DiscordAccount;
import static iecompbot.Main.Prefs;
import static iecompbot.springboot.config.AppConfig.cacheService;

@Component
public class BotEmoji extends DatabaseObject<BotEmoji> implements CustomEmoji {

    public long ID;
    public Long ServerID = null;
    public String Name;
    public String Formatted;

    private BotEmoji() {}
    public BotEmoji(ApplicationEmoji emoji) {
        Name = emoji.getName();
        ID = emoji.getIdLong();
        Formatted = emoji.getAsMention();
        if (!Prefs.TestMode) Write();
    }
    public BotEmoji(CustomEmoji emoji, Long serverID) {
        ID = emoji.getIdLong();
        Name = emoji.getName();
        Formatted = emoji.getAsMention();
        ServerID = serverID;
        Write();
    }
    public BotEmoji(String unicode) {
        if (unicode.toLowerCase().startsWith("u+")) {
            ID = Instant.now().toEpochMilli();
            Name = unicode;
            Formatted = Emoji.fromUnicode(unicode).getFormatted();
            Write();
        }
    }
    public static BotEmoji from(Emoji emoji, Long serverID) {
        BotEmoji i = cacheService.getCachedEmojiByFormatted(emoji.getFormatted());
        if (i != null && (serverID == null || serverID.equals(i.ServerID))) return i;
        if (emoji instanceof UnicodeEmoji E) {
            return new BotEmoji(E.getAsCodepoints());
        } else if (emoji instanceof CustomEmoji E) {
            return new BotEmoji(E, serverID);
        }
        return null;
    }

    public static BotEmoji get(long emojiId) {
        try {
            if (Prefs.TestMode) return from(DiscordAccount.retrieveApplicationEmojiById(1342608664770641980L).submit().orTimeout(3, TimeUnit.SECONDS).get(), null);
            BotEmoji E = cacheService.getCachedEmojiById(emojiId);
            if (E == null) E = from(DiscordAccount.retrieveApplicationEmojiById(emojiId).submit().orTimeout(3, TimeUnit.SECONDS).get(), null);
            return E;
        } catch (Exception ignored) {
            return get("Empty");
        }
    }
    public static BotEmoji get(String name) {
        try {
            if (Prefs.TestMode) return from(DiscordAccount.retrieveApplicationEmojiById(1342608664770641980L).submit().orTimeout(3, TimeUnit.SECONDS).get(), null);
            BotEmoji E = cacheService.getCachedEmojiByName(name);
            if (E == null) E = from(Objects.requireNonNull(DiscordAccount.retrieveApplicationEmojis().submit().orTimeout(3, TimeUnit.SECONDS).get().stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null)), null);
            return E;
        } catch (Exception ignored) {
            return get("Empty");
        }
    }

    public static BotEmoji get(Long serverid, long emojiId) {
        try {
            if (serverid == null) return get(emojiId);
            BotEmoji E = cacheService.getCachedEmojiById(emojiId);
            if (E == null && Prefs.TestMode) return from(DiscordAccount.retrieveApplicationEmojiById(1342608664770641980L).submit().orTimeout(3, TimeUnit.SECONDS).get(), null);
            if (E == null) {
                Guild G = DiscordAccount.getGuildById(serverid);
                E = from(G.retrieveEmojiById(emojiId).submit().orTimeout(3, TimeUnit.SECONDS).get(), serverid);
            }
            return E;
        } catch (Exception ignored) {
            try {
                return get("Empty");
            } catch (Exception ignored2) {}
            return null;
        }
    }
    public static BotEmoji get(Long serverid, String name) {
        try {
            if (serverid == null) return get(name);
            BotEmoji E = cacheService.getCachedServerEmojiByName(name, serverid);
            if (E == null && Prefs.TestMode) return from(DiscordAccount.retrieveApplicationEmojiById(1342608664770641980L).submit().orTimeout(3, TimeUnit.SECONDS).get(), null);
            if (E == null) {
                Guild G = DiscordAccount.getGuildById(serverid);
                E = from(Objects.requireNonNull(G.retrieveEmojis().submit().orTimeout(3, TimeUnit.SECONDS).get().stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null)), serverid);
            }
            return E;
        } catch (Exception ignored) {
            return get("Empty");
        }
    }









    @NotNull
    @Override
    public String getFormatted() {
        if (getName().equals("Empty")) return "";
        return Formatted;
    }
    @NotNull
    @Override
    public Emoji.Type getType() {
        return retrieve().getType();
    }

    @Override
    public boolean isAnimated() {
        if (retrieve() instanceof CustomEmoji E) {
            return E.isAnimated();
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public String getImageUrl() {
        if (retrieve() instanceof CustomEmoji E) {
            return E.getImageUrl();
        } else {
            return null;
        }
    }

    @NotNull
    @Override
    public ImageProxy getImage() {
        if (retrieve() instanceof CustomEmoji E) {
            return E.getImage();
        } else {
            return null;
        }
    }

    @NotNull
    @Override
    public String getAsMention() {
        return Formatted;
    }

    @NotNull
    @Override
    public String getName() {
        return Name;
    }

    @NotNull
    @Override
    public String getAsReactionCode() {
        return retrieve().getAsReactionCode();
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        retrieve().formatTo(formatter, flags, width, precision);
    }

    @NotNull
    @Override
    public String getId() {
        return String.valueOf(ID);
    }

    @Override
    public long getIdLong() {
        return ID;
    }

    @NotNull
    @Override
    public OffsetDateTime getTimeCreated() {
        if (retrieve() instanceof CustomEmoji E) {
            return E.getTimeCreated();
        } else {
            return OffsetDateTime.from(Instant.ofEpochSecond(0));
        }
    }

    @NotNull
    @Override
    public DataObject toData() {
        return retrieve().toData();
    }

    private transient Emoji emoji;
    public Emoji retrieve() {
        if (emoji == null) {
            try {
                if (Name.toLowerCase().startsWith("u+")) emoji = Emoji.fromUnicode(Name);
                else if (ServerID != null) emoji = DiscordAccount.getGuildById(ServerID).getEmojiById(ID);
                else emoji = DiscordAccount.retrieveApplicationEmojiById(ID).submit().orTimeout(4, TimeUnit.SECONDS).get();
            } catch (Exception e) {
                try {
                    emoji = DiscordAccount.retrieveApplicationEmojiById("1305202726015336579").submit().orTimeout(3, TimeUnit.SECONDS).get();
                } catch (InterruptedException | ExecutionException ex) {
                    return null;
                }
            }
        } return emoji;
    }

    @Override
    public String toString() {
        return Formatted;
    }


    public static void CleanEmojis() {
        if (!Prefs.TestMode) for (BotEmoji E : getAll(BotEmoji.class)) {
            if (E.retrieve() == null) {
                E.Delete();
                System.out.println("Deleted emoji " + E.Formatted);
            }
        }
        cacheService.clearCache("obj_emoji");
    }
}
