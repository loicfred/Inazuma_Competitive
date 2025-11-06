package iecompbot.objects;

import iecompbot.Main;
import iecompbot.interaction.Automation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static iecompbot.Constants.BotStaffGuild;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.Main.MainDirectory;
import static iecompbot.Utility.takeOnlyDigits;
import static iecompbot.interaction.Automation.Wait;

public class Retrieval {

    public synchronized static Message getMessageByLink(String link) {
        try {
            link = link.replaceAll("https://discord.com/channels/", "");
            Guild G = DiscordAccount.getGuildById(link.split("/")[1]);
            TextChannel channel = G != null ? G.getTextChannelById(link.split("/")[1]) : null;
            return getMessage(channel, link.split("/")[2]);
        } catch (Exception ignored) {
            return null;
        }
    }

    public synchronized static Message getMessage(MessageChannel channel, long messageid) {
        try {
            return channel.retrieveMessageById(messageid).submit().orTimeout(5, TimeUnit.SECONDS).get();
        } catch (Exception ex) {
            return null;
        }
    }

    public synchronized static Message getMessage(MessageChannel channel, String messageid) {
        try {
            return channel.retrieveMessageById(takeOnlyDigits(messageid)).submit().orTimeout(5, TimeUnit.SECONDS).get();
        } catch (Exception ex) {
            return null;
        }
    }
    public synchronized static User getUserByID(String userid) {
        try {
            return DiscordAccount.retrieveUserById(takeOnlyDigits(userid)).submit().orTimeout(5, TimeUnit.SECONDS).get();
        } catch (Exception ex) {
            return new User() {
                @Override
                public @NotNull String getName() {
                    return "???";
                }

                @Override
                public @Nullable String getGlobalName() {
                    return "???";
                }

                @Override
                public @NotNull String getEffectiveName() {
                    return User.super.getEffectiveName();
                }

                @Override
                public @NotNull String getDiscriminator() {
                    return "???";
                }

                @Override
                public @Nullable String getAvatarId() {
                    return "";
                }

                @Override
                public @Nullable String getAvatarUrl() {
                    return User.super.getAvatarUrl();
                }

                @Override
                public @Nullable ImageProxy getAvatar() {
                    return User.super.getAvatar();
                }

                @Override
                public @NotNull String getEffectiveAvatarUrl() {
                    return User.super.getEffectiveAvatarUrl();
                }

                @Override
                public @NotNull ImageProxy getEffectiveAvatar() {
                    return User.super.getEffectiveAvatar();
                }

                @Override
                public @NotNull CacheRestAction<Profile> retrieveProfile() {
                    return null;
                }

                @Override
                public @NotNull String getAsTag() {
                    return "";
                }

                @Override
                public boolean hasPrivateChannel() {
                    return false;
                }

                @Override
                public @NotNull CacheRestAction<PrivateChannel> openPrivateChannel() {
                    return null;
                }

                @Override
                public @NotNull @Unmodifiable List<Guild> getMutualGuilds() {
                    return List.of();
                }

                @Override
                public boolean isBot() {
                    return false;
                }

                @Override
                public boolean isSystem() {
                    return false;
                }

                @Override
                public @NotNull JDA getJDA() {
                    return null;
                }

                @Override
                public @NotNull EnumSet<UserFlag> getFlags() {
                    return null;
                }

                @Override
                public int getFlagsRaw() {
                    return 0;
                }

                @Override
                public @Nullable PrimaryGuild getPrimaryGuild() {
                    return null;
                }

                @Override
                public @NotNull String getDefaultAvatarId() {
                    return "";
                }

                @Override
                public @NotNull String getDefaultAvatarUrl() {
                    return User.super.getDefaultAvatarUrl();
                }

                @Override
                public @NotNull ImageProxy getDefaultAvatar() {
                    return User.super.getDefaultAvatar();
                }

                @Override
                public @NotNull String getAsMention() {
                    return "<@974675718975946853>";
                }

                @Override
                public void formatTo(Formatter formatter, int flags, int width, int precision) {
                    User.super.formatTo(formatter, flags, width, precision);
                }

                @Override
                public @NotNull String getId() {
                    return User.super.getId();
                }

                @Override
                public long getIdLong() {
                    return 974675718975946853L;
                }

                @Override
                public @NotNull OffsetDateTime getTimeCreated() {
                    return User.super.getTimeCreated();
                }
            };
        }
    }
    public synchronized static User getUserByID(long userid) {
        try {
            return DiscordAccount.retrieveUserById(userid).submit().orTimeout(5, TimeUnit.SECONDS).get();
        } catch (Exception e) {
            return new User() {
                @Override
                public @NotNull String getName() {
                    return "???";
                }

                @Override
                public @Nullable String getGlobalName() {
                    return "???";
                }

                @Override
                public @NotNull String getEffectiveName() {
                    return User.super.getEffectiveName();
                }

                @Override
                public @NotNull String getDiscriminator() {
                    return "???";
                }

                @Override
                public @Nullable String getAvatarId() {
                    return "";
                }

                @Override
                public @Nullable String getAvatarUrl() {
                    return User.super.getAvatarUrl();
                }

                @Override
                public @Nullable ImageProxy getAvatar() {
                    return User.super.getAvatar();
                }

                @Override
                public @NotNull String getEffectiveAvatarUrl() {
                    return User.super.getEffectiveAvatarUrl();
                }

                @Override
                public @NotNull ImageProxy getEffectiveAvatar() {
                    return User.super.getEffectiveAvatar();
                }

                @Override
                public @NotNull CacheRestAction<Profile> retrieveProfile() {
                    return null;
                }

                @Override
                public @NotNull String getAsTag() {
                    return "";
                }

                @Override
                public boolean hasPrivateChannel() {
                    return false;
                }

                @Override
                public @NotNull CacheRestAction<PrivateChannel> openPrivateChannel() {
                    return null;
                }

                @Override
                public @NotNull @Unmodifiable List<Guild> getMutualGuilds() {
                    return List.of();
                }

                @Override
                public boolean isBot() {
                    return false;
                }

                @Override
                public boolean isSystem() {
                    return false;
                }

                @Override
                public @NotNull JDA getJDA() {
                    return null;
                }

                @Override
                public @NotNull EnumSet<UserFlag> getFlags() {
                    return null;
                }

                @Override
                public int getFlagsRaw() {
                    return 0;
                }

                @Override
                public @Nullable PrimaryGuild getPrimaryGuild() {
                    return null;
                }

                @Override
                public @NotNull String getDefaultAvatarId() {
                    return "";
                }

                @Override
                public @NotNull String getDefaultAvatarUrl() {
                    return User.super.getDefaultAvatarUrl();
                }

                @Override
                public @NotNull ImageProxy getDefaultAvatar() {
                    return User.super.getDefaultAvatar();
                }

                @Override
                public @NotNull String getAsMention() {
                    return "<@974675718975946853>";
                }

                @Override
                public void formatTo(Formatter formatter, int flags, int width, int precision) {
                    User.super.formatTo(formatter, flags, width, precision);
                }

                @Override
                public @NotNull String getId() {
                    return User.super.getId();
                }

                @Override
                public long getIdLong() {
                    return 974675718975946853L;
                }

                @Override
                public @NotNull OffsetDateTime getTimeCreated() {
                    return User.super.getTimeCreated();
                }
            };
        }
    }
    public static Member getMember(Guild guild, String memberid) {
        try {
            return guild.retrieveMemberById(takeOnlyDigits(memberid)).submit().orTimeout(3, TimeUnit.SECONDS).get();
        } catch (Exception ex) {
            return getMember(guild, memberid);
        }
    }
    public static void AddTemporaryUserEmoji(User user, Message message, int num) {
        try {
            File img = new File(MainDirectory + "/temp/" + user.getId() + ".png");
            ImageIO.write(ImageIO.read(URI.create(user.getEffectiveAvatarUrl()).toURL()), "png", img);
            if (img.exists()) {
                BotStaffGuild.createEmoji("Vote" + num, Icon.from(img))
                        .flatMap(richCustomEmoji -> message.addReaction(Emoji.fromCustom(richCustomEmoji))
                                .flatMap(Void -> richCustomEmoji.delete())).queue();
            } else {
                try (InputStream is = Main.class.getResourceAsStream("/img/AvatarDefault.png")) {
                    BotStaffGuild.createEmoji("Vote" + num, Icon.from(is))
                            .flatMap(richCustomEmoji -> message.addReaction(Emoji.fromCustom(richCustomEmoji))
                                    .flatMap(Void -> richCustomEmoji.delete())).queue();
                } catch (Exception ignored) {}
            }
            Wait(100);
        } catch (IOException | NullPointerException e) {
            Automation.handleException(e);
        }
    }
}
