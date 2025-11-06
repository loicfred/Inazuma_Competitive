package iecompbot.objects.profile.quest.quest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.Constants;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.match.Game;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.profile_game.Profile_Game;
import iecompbot.objects.profile.quest.BaseQuest;
import iecompbot.objects.profile.quest.Objective;
import iecompbot.objects.profile.quest.QuestCategory;
import iecompbot.objects.profile.quest.Rewards;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static my.utilities.util.Utilities.GenerateRandomNumber;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile_Quest extends BaseQuest<Profile_Quest> {
    private transient Quest_Objective objectives = null;

    public String Category;
    public Long DMMessageID;
    public Long DeadlineEpochSecond;

    public QuestCategory getCategory() {
        return QuestCategory.valueOf(Category);
    }
    public Quest_Objective getObjectives() {
        return objectives == null ? objectives = Quest_Objective.ofQuest(getId()) : objectives;
    }
    public Instant getDeadline() {
        if (DeadlineEpochSecond == null) return null;
        return Instant.ofEpochSecond(DeadlineEpochSecond);
    }
    public void IncrementProgress(String Type, int amount) {
        try {
            if (amount != 0 && !isComplete()) {
                getObjectives().IncrementProgress(Type, amount);
                if (isComplete()) {
                    CompletedTimeEpochSecond = Instant.now().getEpochSecond();
                    Update();
                    for (Item.Item_Count<?> R : getRewards().list()) getProfile().addItem(R.getId(), R.Amount);
                    getProfile().Totals().addTotalQuests();
                    if (getCategory().equals(QuestCategory.DAILY)) getProfile().Totals().addTotalDailies();
                    if (getCategory().equals(QuestCategory.MONTHLY)) sendPrivateMessage(getProfile().getUser(), new MessageCreateBuilder().setEmbeds(getEmbed().build()));
                }
                if (DMMessageID != null) getProfile().getUser().openPrivateChannel().flatMap(C -> C.retrieveMessageById(DMMessageID).flatMap(M -> M.editMessageEmbeds(getEmbed().build()))).queue();
            }
        } catch (Exception ignored) {}
    }
    public boolean isComplete() {
        return getObjectives() != null && getObjectives().areComplete();
    }
    public EmbedBuilder getEmbed() {
        EmbedBuilder E = new EmbedBuilder();
        try {
            if (isComplete()) {
                E.setDescription("# :scroll: " + TL(getProfile(), "Completed") + "\n:white_check_mark: " + TL(getProfile(), "quest-completed", "**" + getEmojiFormatted() + " " + Name + "**") + "\n> " + Description);
                E.setColor(Color.GREEN);
                E.setTimestamp(getCompletedTime());
            } else {
                E.setDescription("# :scroll: " + TL(getProfile(), "New-Quest") + "\n" + TL(getProfile(), "quest-received", "**" + getEmojiFormatted() + " " + Name + "**") + "\n> " + Description + "\n"
                + (getDeadline() != null && getDeadline().isAfter(Instant.now()) ? "**" + TL(getProfile(), "Deadline") + "**: <t:" + getDeadline().getEpochSecond() + ":R>" : ""));
                E.setColor(Color.ORANGE);
            }
            String s = "";
            for (Objective O : getObjectives().list()) {
                if (O.isComplete()) s = s + "> ~~" + O.getDescription() + " **[" + O.getProgress() + "/" + O.getRequiredProgress() + "]**~~\n";
                else s = s + "> " + O.getDescription() + " **[" + O.getProgress() + "/" + O.getRequiredProgress() + "]**\n";
            }
            E.addField(":small_blue_diamond: " + TL(getProfile(), "Objectives"), s, false);
            s = "";
            for (Item.Item_Count<?> R : getRewards().list()) {
                s = s + "> - " +  R.getEmojiFormatted() + " " + R.getName() + " **×" + R.Amount + "**\n";
            }
            E.addField(TL(getProfile(), "Rewards"), s, false);
        } catch (Exception ignored) {}
        return E;
    }

    public static void GenerateRandomQuestForUser(Guild Guild, Game G, Profile_Game Me) {
        List<Item.Item_Count<?>> drops = new ArrayList<>();
        Item Mats = DatabaseObject.getRandom(Item.class,"Type = ?", Item.ItemType.MATERIALS.name());
        if (GenerateRandomNumber(1,2) == 1) {
            List<Profile_Game> PG = getAllWhere(Profile_Game.class, "NOT UserID = ? AND", Me.getUserID());
            PG.removeIf(P -> !P.getLeague().getTier().getName().equals(Me.getLeague().getTier().getName()));
            PG.removeIf(P -> P.getLastTimePlayed() != null && !P.getLastTimePlayed().isAfter(Instant.now().minus(28, ChronoUnit.DAYS)));
            PG.removeIf(P -> Guild.getMemberById(P.getUserID()) == null);
            if (PG.size() >= 5) {
                Profile_Game PGChosen = PG.get(GenerateRandomNumber(0, PG.size() - 1));
                double POW = PGChosen.getPower();
                if (POW >= 20) {
                    drops.add(Item.Item_Count.of("InaCoin", 1500));
                    drops.add(Item.Item_Count.of("RainbowCoin", 250));
                    drops.add(Item.Item_Count.of(Mats.getId(), 5));
                } else if (POW >= 15) {
                    drops.add(Item.Item_Count.of("InaCoin", 1000));
                    drops.add(Item.Item_Count.of("RainbowCoin", 100));
                    drops.add(Item.Item_Count.of(Mats.getId(), 5));
                } else if (POW >= 10) {
                    drops.add(Item.Item_Count.of("InaCoin", 750));
                    drops.add(Item.Item_Count.of("RainbowCoin", 75));
                    drops.add(Item.Item_Count.of(Mats.getId(), 4));
                } else if (POW >= 5) {
                    drops.add(Item.Item_Count.of("InaCoin", 500));
                    drops.add(Item.Item_Count.of("RainbowCoin", 50));
                    drops.add(Item.Item_Count.of(Mats.getId(), 3));
                } else if (POW >= 1) {
                    drops.add(Item.Item_Count.of("InaCoin", 250));
                    drops.add(Item.Item_Count.of("RainbowCoin", 25));
                    drops.add(Item.Item_Count.of(Mats.getId(), 2));
                } else {
                    drops.add(Item.Item_Count.of("InaCoin", 150));
                    drops.add(Item.Item_Count.of("RainbowCoin", 15));
                    drops.add(Item.Item_Count.of(Mats.getId(), 1));
                }
                User opponent = getUserByID(PGChosen.getUserID());
                if (GenerateRandomNumber(1,2) == 1) {
                    for (Item.Item_Count<?> I : drops) {
                        I.Amount = (int) (I.Amount * 1.25);
                    }
                    AddWinDuelQuest(Me.getProfile(), opponent, G, drops);
                } else {
                    AddDuelQuest(Me.getProfile(), opponent, G, drops);
                }
            }
        } else {
            Profile P = Me.getProfile();
            P.getUser().openPrivateChannel().queue(C -> C.sendMessage(TL(P, "Processing")).queue(M -> {
                try {
                    if (GenerateRandomNumber(1,2) == 1) {
                        Profile_Quest QQ = new Profile_Quest("Matchmaking Day " + (Profile_Quest.OfUser(Me.getUserID(), "Matchmaking Day").size()+1), "Time for some matchmaking.", Me.getProfile(), BotEmoji.get("U+26BD"), QuestCategory.NORMAL, Instant.now().plus(3, ChronoUnit.DAYS).getEpochSecond(), Rewards.of(Item.Item_Count.of("InaCoin", 150), Item.Item_Count.of(Mats.getId(), GenerateRandomNumber(1, 3))).getId(), M.getIdLong());
                        new Quest_Objective(QQ.getId(), new Objective("Play 5 match of any game.", "PLAY_MATCH/null", 5));
                        M.editMessageEmbeds(QQ.getEmbed().build()).setReplace(true).queue();
                        Constants.LogChannel.sendMessageEmbeds(QQ.getEmbed().build()).setContent("[DM] Sent to " + P.getUser().getEffectiveName() + " (" + P.getUser().getIdLong() + "):").queue();
                    } else {
                        Profile_Quest QQ = new Profile_Quest("Goal a gogo " + (Profile_Quest.OfUser(Me.getUserID(), "Goal a gogo").size()+1), "Time for some matchmaking.", Me.getProfile(), BotEmoji.get("U+26BD"), QuestCategory.NORMAL, Instant.now().plus(3, ChronoUnit.DAYS).getEpochSecond(), Rewards.of(Item.Item_Count.of("InaCoin", 150), Item.Item_Count.of(Mats.getId(), GenerateRandomNumber(1, 3))).getId(), M.getIdLong());
                        new Quest_Objective(QQ.getId(), new Objective("Score 20 goals of any game.", "SCORE_GOAL/null", 20));
                        M.editMessageEmbeds(QQ.getEmbed().build()).setReplace(true).queue();
                        Constants.LogChannel.sendMessageEmbeds(QQ.getEmbed().build()).setContent("[DM] Sent to " + P.getUser().getEffectiveName() + " (" + P.getUser().getIdLong() + "):").queue();
                    }
                 } catch (Exception ignored) {}
            }));
        }
    }
    public static void AddWinDuelQuest(Profile Me, User opponent, Game G, List<Item.Item_Count<?>> drops) {
        Me.getUser().openPrivateChannel().queue(C -> C.sendMessage(TL(Me, "Processing")).queue(M -> {
            try {
                String gameemoji = G != null ? " (" + G.getEmoji() + ")" : "";
                Profile_Quest QQ = new Profile_Quest("Bounty on " + opponent.getEffectiveName() + " " + (Profile_Quest.OfUser(Me.getId(), "Bounty on " + opponent.getEffectiveName()).size()+1), "Time to clash with " + opponent.getEffectiveName() + ".", Me, BotEmoji.get("U+2694 U+FE0F"), QuestCategory.NORMAL, Instant.now().plus(3, ChronoUnit.DAYS).getEpochSecond(), Rewards.of(drops).getId(), M.getIdLong());
                new Quest_Objective(QQ.getId(), new Objective("Defeat **" + opponent.getEffectiveName() + "** in a match!" + gameemoji, "WIN_DUEL_" + opponent.getId() + "/" + G, 1));
                M.editMessageEmbeds(QQ.getEmbed().build()).setReplace(true).queue();
                Constants.LogChannel.sendMessageEmbeds(QQ.getEmbed().build()).setContent("[DM] Sent to " + Me.getUser().getEffectiveName() + " (" + Me.getUser().getIdLong() + "):").queue();
            } catch (Exception ignored) {}
        }));
    }
    public static void AddLoseDuelQuest(Profile Me, User opponent, Game G, List<Item.Item_Count<?>> drops) {
        Me.getUser().openPrivateChannel().queue(C -> C.sendMessage(TL(Me, "Processing")).queue(M -> {
            try {
                String gameemoji = G != null ? " (" + G.getEmoji() + ")" : "";
                Profile_Quest QQ = new Profile_Quest("Loss on " + opponent.getEffectiveName() + " " + (Profile_Quest.OfUser(Me.getId(), "Loss on " + opponent.getEffectiveName()).size()+1), "Time to clash with " + opponent.getEffectiveName() + ".", Me, BotEmoji.get("U+2694 U+FE0F"), QuestCategory.NORMAL, Instant.now().plus(3, ChronoUnit.DAYS).getEpochSecond(), Rewards.of(drops).getId(), M.getIdLong());
                new Quest_Objective(QQ.getId(), new Objective("Lose against **" + opponent.getEffectiveName() + "** in a match!" + gameemoji, "LOSE_DUEL_" + opponent.getId() + "/" + G, 1));
                M.editMessageEmbeds(QQ.getEmbed().build()).setReplace(true).queue();
                Constants.LogChannel.sendMessageEmbeds(QQ.getEmbed().build()).setContent("[DM] Sent to " + Me.getUser().getEffectiveName() + " (" + Me.getUser().getIdLong() + "):").queue();
            } catch (Exception ignored) {}
        }));
    }
    public static void AddTieDuelQuest(Profile Me, User opponent, Game G, List<Item.Item_Count<?>> drops) {
        Me.getUser().openPrivateChannel().queue(C -> C.sendMessage(TL(Me, "Processing")).queue(M -> {
            try {
                String gameemoji = G != null ? " (" + G.getEmoji() + ")" : "";
                Profile_Quest QQ = new Profile_Quest("Tie on " + opponent.getEffectiveName() + " " + (Profile_Quest.OfUser(Me.getId(), "Tie on " + opponent.getEffectiveName()).size()+1), "Time to clash with " + opponent.getEffectiveName() + ".", Me, BotEmoji.get("U+2694 U+FE0F"), QuestCategory.NORMAL, Instant.now().plus(3, ChronoUnit.DAYS).getEpochSecond(), Rewards.of(drops).getId(), M.getIdLong());
                new Quest_Objective(QQ.getId(), new Objective("Tie a match with **" + opponent.getEffectiveName() + "**!" + gameemoji, "TIE_DUEL_" + opponent.getId() + "/" + G, 1));
                M.editMessageEmbeds(QQ.getEmbed().build()).setReplace(true).queue();
                Constants.LogChannel.sendMessageEmbeds(QQ.getEmbed().build()).setContent("[DM] Sent to " + Me.getUser().getEffectiveName() + " (" + Me.getUser().getIdLong() + "):").queue();
            } catch (Exception ignored) {}
        }));
    }
    public static void AddDuelQuest(Profile Me, User opponent, Game G, List<Item.Item_Count<?>> drops) {
        Me.getUser().openPrivateChannel().queue(C -> C.sendMessage(TL(Me, "Processing")).queue(M -> {
            try {
                String gameemoji = G != null ? " (" + G.getEmoji() + ")" : "";
                Profile_Quest QQ = new Profile_Quest("Duel with " + opponent.getEffectiveName() + " " + (Profile_Quest.OfUser(Me.getId(), "Duel with " + opponent.getEffectiveName()).size()+1), "Time to clash with " + opponent.getEffectiveName() + ".", Me, BotEmoji.get("U+2694 U+FE0F"), QuestCategory.NORMAL, Instant.now().plus(3, ChronoUnit.DAYS).getEpochSecond(), Rewards.of(drops).getId(), M.getIdLong());
                new Quest_Objective(QQ.getId(), new Objective("Play against **" + opponent.getEffectiveName() + "** in a match!" + gameemoji, "DUEL_" + opponent.getId() + "/" + G, 1));
                M.editMessageEmbeds(QQ.getEmbed().build()).setReplace(true).queue();
                Constants.LogChannel.sendMessageEmbeds(QQ.getEmbed().build()).setContent("[DM] Sent to " + Me.getUser().getEffectiveName() + " (" + Me.getUser().getIdLong() + "):").queue();
            } catch (Exception ignored) {}
        }));
    }

    private Profile_Quest() {}
    public Profile_Quest(String name, String description, Profile p, BotEmoji emoji, QuestCategory category, Long deadlineEpochSecond, long rwdsid, long dmMessageID) {
        ID = Instant.now().toEpochMilli();
        UserID = p.getId();
        RewardsID = rwdsid;
        EmojiID = emoji.getIdLong();
        Name = name;
        Description = description;
        Category = category.toString();
        DeadlineEpochSecond = deadlineEpochSecond;
        DMMessageID = dmMessageID;
        Write();
    }
    public Profile_Quest(String name, String description, Profile p, BotEmoji emoji, QuestCategory category, Long deadlineEpochSecond, long rwdsid) {
        ID = Instant.now().toEpochMilli();
        UserID = p.getId();
        RewardsID = rwdsid;
        EmojiID = emoji.getIdLong();
        Name = name;
        Description = description;
        Category = category.toString();
        DeadlineEpochSecond = deadlineEpochSecond;
        Write();
    }

    public static Profile_Quest get(long id) {
        Profile_Quest PQ = getById(Profile_Quest.class, id).orElse(null);
        return PQ != null ? PQ.getOrElseDelete() : null;
    }
    public static List<Profile_Quest> OfUser(long userID) {
        List<Profile_Quest> L = new ArrayList<>();
        for (Profile_Quest l : getAllWhere(Profile_Quest.class, "UserID = ?", userID)) {
            Profile_Quest PQ = l.getOrElseDelete();
            if (PQ != null) L.add(PQ);
        }
        return L;
    }
    public static List<Profile_Quest> OfUser(long userID, String name) {
        return OfUser(userID).stream().filter(P -> P.getName().contains(name)).collect(Collectors.toList());
    }

    private Profile_Quest getOrElseDelete() {
        if (DeadlineEpochSecond != null && (!getObjectives().areComplete() || getCategory().equals(QuestCategory.DAILY) || getCategory().equals(QuestCategory.WEEKLY) || getCategory().equals(QuestCategory.MONTHLY)) && Instant.now().isAfter(getDeadline())) {
            Delete();
            return null;
        } return this;
    }


    @Override
    public int Delete() {
        if (DMMessageID != null) getProfile().getUser().openPrivateChannel().flatMap(C -> C.retrieveMessageById(DMMessageID).flatMap(Message::delete)).queue();
        return super.Delete();
    }
}
