package iecompbot.objects.profile.quest;

import iecompbot.objects.BotEmoji;
import iecompbot.objects.profile.Profile;
import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;

public class BaseQuest<T> extends DatabaseObject<T> {
    protected transient Rewards rewards = null;
    protected transient Profile profile = null;
    protected transient BotEmoji emojiformatted = null;

    protected long ID;
    protected long UserID;
    protected long RewardsID;
    protected long EmojiID;
    protected String Name;
    protected String Description;
    protected long CompletedTimeEpochSecond = 0;

    public long getId() {
        return ID;
    }
    public String getName() {
        return Name;
    }
    public String getDescription() {
        return Description;
    }
    public Rewards getRewards() {
        return rewards == null ? rewards = Rewards.get(RewardsID) : rewards;
    }
    public Profile getProfile() {
        return profile == null ? profile = Profile.get(UserID) : profile;
    }
    public BotEmoji getEmoji() {
        return emojiformatted == null ? emojiformatted = BotEmoji.get(EmojiID) : emojiformatted;
    }
    public String getEmojiFormatted() {
        return getEmoji().getFormatted();
    }
    public Instant getCompletedTime() {
        return Instant.ofEpochSecond(CompletedTimeEpochSecond);
    }
}
