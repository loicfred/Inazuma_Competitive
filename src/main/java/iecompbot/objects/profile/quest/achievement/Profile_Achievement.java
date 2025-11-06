package iecompbot.objects.profile.quest.achievement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.quest.BaseQuest;
import iecompbot.objects.profile.quest.Objective;
import iecompbot.objects.profile.quest.Rewards;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.time.Instant;
import java.util.List;

import static iecompbot.L10N.TL;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static my.utilities.util.Utilities.GenerateRandomNumber;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile_Achievement extends BaseQuest<Profile_Achievement> {
    protected transient Achievement_Objective objectives = null;

    public Achievement_Objective getObjectives() {
        return objectives == null ? objectives = Achievement_Objective.ofAchievement(getId()) : objectives;
    }

    public EmbedBuilder getEmbed() {
        EmbedBuilder E = new EmbedBuilder();
        try {
            E.setDescription("# :medal: " + TL(getProfile(), "Achievement") + "\n:white_check_mark: " + TL(getProfile(), "achievements-complete", getProfile().getUser().getAsMention(), "**" + getEmojiFormatted() + " " + Name + "**") + "\n> " + Description);
            E.setColor(Color.GREEN);
            E.setTimestamp(getCompletedTime());
            String s = "";
            for (Objective O : getObjectives().list()) {
                if (O.isComplete()) s = s + "> ~~" + O.getDescription() + " **[" + O.getProgress() + "/" + O.getRequiredProgress() + "]**~~\n";
                else s = s + "> " + O.getDescription() + " **[" + O.getProgress() + "/" + O.getRequiredProgress() + "]**\n";
            }
            E.addField(":small_orange_diamond: " + TL(getProfile(), "Objectives"), s, false);
            s = "";
            for (Item.Item_Count<?> R : getRewards().list()) {
                s = s + "> - " +  R.getEmojiFormatted() + " " + R.getName() + " **×" + R.Amount + "**\n";
            }
            E.addField(TL(getProfile(), "Rewards"), s, false);
        } catch (Exception ignored) {}
        return E;
    }

    public void setProgress(String Type, int amount) {
        try {
            if (!isComplete()) {
                getObjectives().setProgress(Type, amount);
                if (isComplete()) {
                    CompletedTimeEpochSecond = Instant.now().getEpochSecond();
                    Update();
                    for (Item.Item_Count<?> R : getRewards().list()) getProfile().addItem(R.getId(), R.Amount);
                    sendPrivateMessage(getProfile().getUser(), new MessageCreateBuilder().setEmbeds(getEmbed().build()));
                }
            }
        } catch (Exception ignored) {}
    }
    public boolean isComplete() {
        return getObjectives() != null && getObjectives().areComplete();
    }

    private Profile_Achievement() {}
    public Profile_Achievement(String name, String description, Profile p, BotEmoji emoji, Rewards rwds) {
        ID = Instant.now().toEpochMilli() + GenerateRandomNumber(1,999);
        UserID = p.getId();
        RewardsID = rwds.getId();
        EmojiID = emoji.getIdLong();
        Name = name;
        Description = description;
        Write();
    }
    public Profile_Achievement(String name, String description, Profile p, BotEmoji emoji, long rwdsid) {
        ID = Instant.now().toEpochMilli() + GenerateRandomNumber(1,999);
        UserID = p.getId();
        RewardsID = rwdsid;
        EmojiID = emoji.getIdLong();
        Name = name;
        Description = description;
        Write();
    }

    public static Profile_Achievement get(long id) {
        return getById(Profile_Achievement.class, id).orElse(null);
    }
    public static List<Profile_Achievement> OfUser(long userID) {
        return getAllWhere(Profile_Achievement.class, "UserID = ? ORDER BY RewardsID ASC", userID);
    }

}
