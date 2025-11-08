package iecompbot.objects.profile;

import at.stefangeyer.challonge.exception.DataAccessException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.img.builders.CardImageBuilder;
import iecompbot.interaction.cmdbreakdown.PageViewerCommand;
import iecompbot.interaction.cmdbreakdown.profile.FilterCommand;
import iecompbot.interaction.cmdbreakdown.profile.GamesCommand;
import iecompbot.interaction.cmdbreakdown.profile.ProfileCommand;
import iecompbot.interaction.custom.BuiltMessageE;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.Nationality;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.match.Game;
import iecompbot.objects.match.League;
import iecompbot.objects.match.MatchLog;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.item.Scoreboard;
import iecompbot.objects.profile.profile_game.Profile_Game;
import iecompbot.objects.profile.profile_game.Profile_Game_S;
import iecompbot.objects.profile.quest.Objective;
import iecompbot.objects.profile.quest.QuestCategory;
import iecompbot.objects.profile.quest.achievement.Profile_Achievement;
import iecompbot.objects.profile.quest.quest.Profile_Quest;
import iecompbot.objects.profile.quest.quest.Quest_Objective;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Match;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.Constants.*;
import static iecompbot.L10N.TL;
import static iecompbot.Main.DefaultURL;
import static iecompbot.Main.MainDirectory;
import static iecompbot.img.ImgUtilities.getDominantColor;
import static iecompbot.img.ImgUtilities.getHexValue;
import static iecompbot.interaction.Automation.*;
import static iecompbot.objects.BotManagers.*;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.clan.Clan.getClansOfUser;
import static my.utilities.util.Utilities.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Profile extends DatabaseObject<Profile> {
    private transient Nationality Nat = null;
    private transient User user = null;
    private transient Profile_Total Total;

    private transient List<Item.Item_To_Profile> Items;
    private transient List<Profile_Booster> Boosters;
    private transient List<Profile_Achievement> Achievements;
    private transient List<Profile_Quest> Quests;
    private transient List<ClanMember> ClanMembers;


    public long ID;
    public String Name;
    public String FullName;
    public String AvatarURL;
    public String NationalityName = "International";
    public String ColorCode = "#808080";
    public String Signature = "Hey there, I am a new player!";
    public String Language = DiscordLocale.ENGLISH_UK.getLanguageName();
    public Long BirthdayEpochSecond;
    public String StrikersFriendcode;
    public String XtremeFriendcode;
    public String SwitchFriendcode;
    public String CardPNGURL;
    public String CardGIFURL;
    public boolean hasMatchmakingNotification = true;
    public boolean hasTournamentNotification = true;
    public boolean hasPrivateProfile = false;
    public boolean hasClanTag = true;
    public boolean hasGIF = false;
    public boolean isAlive = true;
    public long ScoreTimeoutEpochSecond = 0;
    public long MatchmakingTimeoutEpochSecond = 0;
    public long BirthdayTimeoutEpochSecond = 0;
    public long NextDailyReceiveTime = 0;
    public long NextWeeklyReceiveTime = 0;
    public long NextMonthlyReceiveTime = 0;
    public String History = "-";
    public String WebsiteURL = null;
    public String TwitterURL = null;
    public String TwitchURL = null;
    public String YouTubeURL = null;
    public String InstagramURL = null;
    public String DiscordURL = null;
    public String TiktokURL = null;

    public long getID() {
        return ID;
    }
    public String getNationalityName() {
        return NationalityName;
    }
    public Nationality getNationality() {
        return Nat == null ? Nat = Nationality.get(NationalityName) : Nat;
    }
    public Color getColor() {
        return Color.decode(getColorCode());
    }
    public String getColorCode() {
        return ColorCode;
    }
    public String getLanguage() {
        return Language;
    }
    public String getSignature() {
        return Signature;
    }
    public String getHistory() {
        return History;
    }
    public Long getBirthdayEpochSecond() {
        return BirthdayEpochSecond;
    }
    public Instant getBirthday() {
        if (BirthdayEpochSecond == null) return null;
        return Instant.ofEpochSecond(BirthdayEpochSecond);
    }
    public String getStrikersFriendcode() {
        return StrikersFriendcode;
    }
    public String getXtremeFriendcode() {
        return XtremeFriendcode;
    }
    public String getSwitchFriendcode() {
        return SwitchFriendcode;
    }
    public String getCardPNGURL() {
        return CardPNGURL;
    }
    public String getCardGIFURL() {
        return CardGIFURL;
    }
    public boolean isHasMatchmakingNotification() {
        return hasMatchmakingNotification;
    }
    public boolean isHasTournamentNotification() {
        return hasTournamentNotification;
    }
    public boolean isHasPrivateProfile() {
        return hasPrivateProfile;
    }
    public boolean isHasClanTag() {
        return hasClanTag;
    }
    public boolean isHasGIF() {
        return hasGIF && getItem("Shiny Card").Amount >= 1;
    }
    public Instant getScoreTimeout() {
        return Instant.ofEpochSecond(ScoreTimeoutEpochSecond);
    }
    public Instant getMatchmakingTimeout() {
        return Instant.ofEpochSecond(MatchmakingTimeoutEpochSecond);
    }
    public Instant getBirthdayTimeout() {
        return Instant.ofEpochSecond(BirthdayTimeoutEpochSecond);
    }

    public String getWebsiteURL() {
        return WebsiteURL;
    }
    public String getTwitterURL() {
        return TwitterURL;
    }
    public String getTwitchURL() {
        return TwitchURL;
    }
    public String getYouTubeURL() {
        return YouTubeURL;
    }
    public String getInstagramURL() {
        return InstagramURL;
    }
    public String getDiscordURL() {
        return DiscordURL;
    }
    public String getTiktokURL() {
        return TiktokURL;
    }

    public void setWebsiteURL(String websiteURL) {
        WebsiteURL = CutString(websiteURL, 256);
    }
    public void setTwitterURL(String twitterURL) {
        TwitterURL = CutString(twitterURL, 256);
    }
    public void setTwitchURL(String twitchURL) {
        TwitchURL = CutString(twitchURL, 256);
    }
    public void setYouTubeURL(String youTubeURL) {
        YouTubeURL = CutString(youTubeURL, 256);
    }
    public void setInstagramURL(String instagramURL) {
        InstagramURL = CutString(instagramURL, 256);
    }
    public void setDiscordURL(String discordURL) {
        DiscordURL = CutString(discordURL, 256);
    }
    public void setTiktokURL(String tiktokURL) {
        TiktokURL = CutString(tiktokURL, 256);
    }

    public User getUser() {
        return user == null ? user = getUserByID(getID()) : user;
    }

    public Profile_Booster addBooster(Item.Item_To_Profile i) {
        if (i.Amount > 0) {
            if (removeItem(i.getId(), 1) != 0) {
                return new Profile_Booster(getID()
                        , Instant.now().plus(i.getItem().getName().contains("48") ? 48 : 24, ChronoUnit.HOURS).toEpochMilli()
                        , i.getName().contains("Coin") ? "Coin" : "XP"
                        , i.getName()
                        , i.getName().contains("x2.5") ? 2.5 : i.getItem().getName().contains("x2") ? 2.0 : 1.5);
            }
        }
        return null;
    }
    public List<Profile_Booster> getBoosters() {
        return Boosters == null ? Boosters = Profile_Booster.get(getID()) : Boosters;
    }


    public void addItem(long itemid, int amount) {
        Item.Item_To_Profile TR = getItem(itemid);
        TR.Amount += amount;
        TR.Update();
    }
    public void setItem(long itemid, int amount) {
        Item.Item_To_Profile TR = getItem(itemid);
        TR.Amount = amount;
        TR.Update();
    }
    public int removeItem(long itemid, int amount) {
        Item.Item_To_Profile TR = getItem(itemid);
        TR.Amount -= amount;
        return TR.Update();
    }
    public List<Item.Item_To_Profile> getItems() {
        return Items == null ? Items = getAllWhere(Item.Item_To_Profile.class,"UserID = ?", getID()) : Items;
    }
    public List<Item.Item_To_Profile> getItems(Item.ItemType type) {
        return getItems().stream().filter(i -> i.getType().equals(type)).collect(Collectors.toList());
    }
    public Item.Item_To_Profile getItem(long itemid) {
        Item.Item_To_Profile TR = getItems().stream().filter(tr -> tr.ItemID == itemid).findFirst().orElse(null);
        if (TR == null) {
            TR = new Item.Item_To_Profile(getID(), itemid, 0);
            getItems().add(TR);
        }
        return TR;
    }
    public Item.Item_To_Profile getItem(String name) {
        Item.Item_To_Profile TR = getItems().stream().filter(tr -> tr.getItem().getName().equals(name)).findFirst().orElse(null);
        if (TR == null) {
            TR = new Item.Item_To_Profile(getID(), Item.get(name).getId(), 0);
            getItems().add(TR);
        }
        return TR;
    }


    public long CardBackground = 40001;
    public long CardForeground = 50001;
    public long CardRay = 60001;
    public long CardStrike = 70001;
    public long Sponsor = 871133534184681523L;

    public Long CustomFrame = null;
    public Long CustomBoard = null;

    public long getSponsor() {
        return Sponsor;
    }

    public Item getCustomFrameItem() {
        if (CustomFrame == null) return null;
        return getItem(CustomFrame).getItem();
    }
    public Item getCustomBoardItem() {
        if (CustomBoard == null) return null;
        return getItem(CustomBoard).getItem();
    }
    public Item getCardBackgroundItem() {
        Item.Item_To_Profile I = getItem(CardBackground);
        if (I == null) return null;
        return getItem(CardBackground).getItem();
    }
    public Item getCardForegroundItem() {
        return getItem(CardForeground).getItem();
    }
    public Item getCardRayItem() {
        return getItem(CardRay).getItem();
    }
    public Item getCardStrikeItem() {
        return getItem(CardStrike).getItem();
    }

    public void setCardBackground(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_BG)) CardBackground = i.getId();
    }
    public void setCardRay(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_RY)) CardRay = i.getId();
    }
    public void setCardForeground(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_FG)) CardForeground = i.getId();
    }
    public void setCardStrike(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_ST)) CardStrike = i.getId();
    }
    public void setSponsor(long id) {
        Sponsor = id;
    }
    public void setCustomFrame(Item i) {
        if (i == null) CustomFrame = null;
        else if (i.getType().equals(Item.ItemType.COSMETICS_FRAME)) CustomFrame = i.getId();
    }
    public void setCustomBoard(Item i) {
        if (i == null) CustomBoard = null;
        else if (i.getType().equals(Item.ItemType.COSMETICS_BOARD)) CustomBoard = i.getId();
    }
    public Scoreboard getScoreboard() {
        return new Scoreboard(getCustomBoardItem());
    }



    public Profile_PastClan AddClanLog(ClanMember member) {
        return new Profile_PastClan(getID(), member.getClanID(), member.getId(), Instant.now().toEpochMilli(), member.isMainClan());
    }
    public List<Profile_PastClan> getClanLogs(boolean mainClan) {
        return Profile_PastClan.ofUser(getID(), mainClan);
    }

    public List<MatchLog> getMatchLogs(Long serverid, List<Game> games, int pages, int amountperpages) {
        return MatchLog.getMatchesOf(getID(), serverid, games, pages, amountperpages);
    }

    public void setNationalityName(String nationalityName) {
        NationalityName = Nationality.get(nationalityName).getName();
    }
    public void setNationality(Nationality nationality) {
        NationalityName = nationality.getName();
    }
    public void setColorCode(String colorCode) {
        ColorCode = colorCode;
    }
    public void setColor(Color color) {
        ColorCode = getHexValue(color);
    }
    public void setSignature(String signature) {
        Signature = CutString(signature, 128);
    }
    public void setBirthday(long birthday) {
        BirthdayEpochSecond = birthday;
    }
    public void setStrikersFriendcode(String strikersFriendcode) {
        StrikersFriendcode = CutString(strikersFriendcode, 12);
    }
    public void setXtremeFriendcode(String xtremeFriendcode) {
        XtremeFriendcode = CutString(xtremeFriendcode, 12);
    }
    public void setSwitchFriendcode(String switchFriendcode) {
        SwitchFriendcode = CutString(switchFriendcode, 12);
    }
    public void setHistory(String history) {
        History = StopString(history, 1024);
    }
    public void setCardPNGURL(String cardPNGURL) {
        if (cardPNGURL == null || cardPNGURL.length() > 10) CardPNGURL = cardPNGURL;
    }
    public void setCardGIFURL(String cardGIFURL) {
        if (cardGIFURL == null || cardGIFURL.length() > 10) CardGIFURL = cardGIFURL;
    }
    public void setHasMatchmakingNotification(boolean hasMatchmakingNotification) {
        this.hasMatchmakingNotification = hasMatchmakingNotification;
    }
    public void setHasTournamentNotification(boolean hasTournamentNotification) {
        this.hasTournamentNotification = hasTournamentNotification;
    }
    public void setHasPrivateProfile(boolean hasPrivateProfile) {
        this.hasPrivateProfile = hasPrivateProfile;
    }
    public void setHasGIF(boolean hasGIF) {
        this.hasGIF = hasGIF && getItem("Shiny Card").Amount >= 1;
    }
    public void RefreshScoreTimeout(Instant i) {
        ScoreTimeoutEpochSecond = i.getEpochSecond();
        UpdateOnly("ScoreTimeoutEpochSecond");
    }
    public void RefreshMatchmakingTimeout(Instant i) {
        MatchmakingTimeoutEpochSecond = i.getEpochSecond();
        UpdateOnly("MatchmakingTimeoutEpochSecond");
    }
    public void RefreshBirthdayTimeout(Instant i) {
        BirthdayTimeoutEpochSecond = i.getEpochSecond();
        UpdateOnly("BirthdayTimeoutEpochSecond");
    }



    public Profile_Total Totals() {
        return Total == null ? Total = Profile_Total.get(this) : Total;
    }


    public File getCharacter() {
        new File(MainDirectory + "/user/" + getID() + "/").mkdirs();
        return new File(MainDirectory + "/user/" + getID() + "/character.png");
    }
    public File getEmblem() {
        new File(MainDirectory + "/user/" + getID() + "/").mkdirs();
        return new File(MainDirectory + "/user/" + getID() + "/emblem.png");
    }
    public void RefreshProfileInformation(Interaction event) {
        Name = getUser().getName();
        FullName = getUser().getEffectiveName();
        AvatarURL = getUser().getEffectiveAvatarUrl();
        if (event != null && getID() == event.getUser().getIdLong()) {
            try {
                Language = event.getUserLocale().getLanguageName();
                if (NationalityName.equals("International")) NationalityName = event.getUserLocale().getLanguageName();
                if (ColorCode.equals("#808080")) ColorCode = getHexValue(getDominantColor(getUser().getAvatarUrl()));
            } catch (IOException ignored) {}
        }
        if (getUser().getEffectiveName().startsWith("deleted_user")) isAlive = false;
        UpdateOnly("Name", "FullName", "AvatarURL", "Language", "NationalityName", "ColorCode", "isAlive");
    }


    public void resetCards() {
        CardPNGURL = null;
        CardGIFURL = null;
    }
    public void resetClanCards() {
        resetCards();
        for (ClanMember M : ClanMember.OfUser(this)) M.resetCards();
    }

    public void regeneratePersonalCard(InteractionHook M) {
        if (Instant.now().isAfter(Instant.ofEpochSecond(ImageGenerationTimerEpochSecond))) {
            ImageGenerationTimerEpochSecond = Instant.now().plus(15, ChronoUnit.SECONDS).getEpochSecond();
            if (getCharacter().exists()) {
                try (CardImageBuilder CIB = new CardImageBuilder(this)) {
                    if (M != null) M.editOriginal("**[PNG]** " + TL(M, TL(M,"generating-cards"))).queue();
                    CIB.GenerateCardPNG();
                    String CardPNGURL = getCardUrl(CIB.DownloadPNGToFile(), "card.png");
                    setCardPNGURL(CardPNGURL != null && CardPNGURL.contains("?") ? CardPNGURL.split("\\?")[0] : CardPNGURL);
                    if (isHasGIF()) {
                        ImageGenerationTimerEpochSecond = Instant.now().plus(15, ChronoUnit.SECONDS).getEpochSecond();
                        if (M != null) M.editOriginal("**[GIF]** " + TL(M, TL(M,"generating-cards"))).queue();
                        CIB.GenerateCardGIF(55, 0.5);
                        String CardGIFURL = getCardUrl(CIB.DownloadGIFToFile(), "card.gif");
                        setCardGIFURL(CardGIFURL != null && CardGIFURL.contains("?") ? CardGIFURL.split("\\?")[0] : CardGIFURL);
                    }
                    UpdateOnly("CardPNGURL", "CardGIFURL");
                    ImageGenerationTimerEpochSecond = 0;
                } catch (Exception e) {
                    replyException(M, e);
                }
            }
        }
    }

    public synchronized String getPersonalCardPNG() {
        return getCardPNGURL();
    }
    public synchronized String getPersonalCardGIF() {
        if (isHasGIF() && getCardGIFURL() != null) return getCardGIFURL();
        return getPersonalCardPNG();
    }
    public synchronized String getClanCardPNG(Clan clan) {
        if (clan != null && clan.getMemberById(getID()).getCardPNGURL() != null) return clan.getMemberById(getID()).getCardPNGURL();
        return getPersonalCardGIF();
    }
    public synchronized String getClanCardGIF(Clan clan) {
        if (isHasGIF() && clan != null && clan.getMemberById(getID()).getCardGIFURL() != null) return clan.getMemberById(getID()).getCardGIFURL();
        return getClanCardPNG(clan);
    }


    public boolean hasPersonalCard() {
        return CardPNGURL != null && (!isHasGIF() || CardGIFURL != null);
    }
    public List<Profile_Trophy> getTrophies() {
        return Profile_Trophy.get(getID());
    }

    public boolean hasDailies() {
        return getUnderwayQuests().stream().anyMatch(q -> q.getName().contains("Daily"));
    }
    public boolean hasWeeklies() {
        return getUnderwayQuests().stream().anyMatch(q -> q.getName().contains("Weekly"));
    }
    public boolean hasMonthlies() {
        return getUnderwayQuests().stream().anyMatch(q -> q.getName().contains("Monthly"));
    }

    public synchronized List<Profile_Quest> getCompletedQuests() {
        try {
            return getQuests().stream().filter(Profile_Quest::isComplete).collect(Collectors.toList());
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }
    public synchronized List<Profile_Quest> getUnderwayQuests() {
        try {
            return getQuests().stream().filter(Q -> !Q.isComplete()).collect(Collectors.toList());
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }
    public synchronized List<Profile_Achievement> getCompletedAchievements() {
        return getAchievements().stream().filter(Profile_Achievement::isComplete).collect(Collectors.toList());
    }
    public synchronized List<Profile_Achievement> getUnderwayAchievements() {
        return getAchievements().stream().filter(Q -> !Q.isComplete()).collect(Collectors.toList());
    }
    public synchronized List<Profile_Achievement> getAchievements() {
        return Achievements == null ? Achievements = Profile_Achievement.OfUser(getID()) : Achievements;
    }
    public synchronized List<Profile_Quest> getQuests() {
        if (Quests == null) {
            try {
                Quests = Profile_Quest.OfUser(getID());
                if (Instant.now().isAfter(Instant.ofEpochSecond(NextDailyReceiveTime))) {
                    NextDailyReceiveTime = getTomorrowMidnight().getEpochSecond();
                    if (Quests.stream().noneMatch(a -> a.getName().startsWith("Daily Training"))) {
                        Profile_Quest QQ = new Profile_Quest("Daily Training", "Got to keep away the rust huh.", this, BotEmoji.get("U+1F3C3 U+200D U+2642 U+FE0F"), QuestCategory.DAILY, NextDailyReceiveTime, 101);
                        new Quest_Objective(QQ.getId(), new Objective("Win 1 match of any game.", "WIN_MATCH/null", 1));
                        Quests.add(QQ);
                    }
                }
                if (Instant.now().isAfter(Instant.ofEpochSecond(NextWeeklyReceiveTime))) {
                    NextWeeklyReceiveTime = getNextMondayMidnight().getEpochSecond();
                    if (Quests.stream().noneMatch(a -> a.getName().startsWith("Weekly Matching"))) {
                        Profile_Quest QQ = new Profile_Quest("Weekly Matching", "Time for some matchmaking this week.", this, BotEmoji.get("Soccer"), QuestCategory.WEEKLY, NextWeeklyReceiveTime, 103);
                        new Quest_Objective(QQ.getId(), new Objective("Play 10 matches of any game.", "PLAY_MATCH/null", 10), new Objective("Win 3 matches of any game.", "WIN_MATCH/null", 3));
                        Quests.add(QQ);
                    }
                    if (Quests.stream().noneMatch(a -> a.getName().startsWith("Weekly Scoring"))) {
                        Profile_Quest QQ = new Profile_Quest("Weekly Scoring", "This week's scoring will be good.", this, BotEmoji.get("Soccer"), QuestCategory.WEEKLY, NextWeeklyReceiveTime, 102);
                        new Quest_Objective(QQ.getId(), new Objective("Score 30 goals of any game.", "SCORE_GOAL/null", 30));
                        Quests.add(QQ);
                    }
                }
                if (Instant.now().isAfter(Instant.ofEpochSecond(NextMonthlyReceiveTime))) {
                    NextMonthlyReceiveTime = getNextMonthStartMidnight().getEpochSecond();
                    if (Quests.stream().noneMatch(a -> a.getName().startsWith("Monthly Clashing"))) {
                        Profile_Quest QQ = new Profile_Quest("Monthly Clashing", "Time to destroy everyone!", this, BotEmoji.get("U+1F525"), QuestCategory.MONTHLY, NextMonthlyReceiveTime, 105);
                        new Quest_Objective(QQ.getId(), new Objective("Play 50 matches of any game.", "PLAY_MATCH/null", 50), new Objective("Win 15 matches of any game.", "WIN_MATCH/null", 15));
                        Quests.add(QQ);
                    }
                    if (Quests.stream().noneMatch(a -> a.getName().startsWith("Monthly Scoring"))) {
                        Profile_Quest QQ = new Profile_Quest("Monthly Scoring", "Time to score the month!", this, BotEmoji.get("U+1F525"), QuestCategory.MONTHLY, NextMonthlyReceiveTime, 103);
                        new Quest_Objective(QQ.getId(), new Objective("Score 150 goals on any game.", "SCORE_GOAL/null", 150));
                        Quests.add(QQ);
                    }
                    if (Quests.stream().noneMatch(a -> a.getName().startsWith("Monthly Tournaments"))) {
                        Profile_Quest QQ = new Profile_Quest("Monthly Tournaments", "Time for some monthly tournaments!", this, BotEmoji.get("U+1F3C6"), QuestCategory.MONTHLY, NextMonthlyReceiveTime, 102);
                        new Quest_Objective(QQ.getId(), new Objective("Participate and play in 1 tournament of any game.", "TOURNAMENT_PARTICIPATE/null", 1));
                        Quests.add(QQ);
                    }
                }
            } catch (Exception ignored) {}
        }
        return Quests;
    }

    public double getPower(Long serverid, String gamecodes) {
        if (serverid == null) {
            return Double.parseDouble(PowerSQL(gamecodes).getAsString("Total Power"));
        } else {
            return Double.parseDouble(PowerSQL(serverid, gamecodes).getAsString("Total Power"));
        }
    }
    private transient DatabaseObject.Row activity;
    public DatabaseObject.Row getActivity(Long serverid, String gamecodes) {
        return activity == null ? activity = doQuery("CALL DisplayUserActivity(?,?,?,?,?)", getID(), serverid, gamecodes, 30, 3).orElse(null) : activity;
    }

    public void ViewProfile(InteractionHook M) {
        Guild G = M.getInteraction().getGuild();

        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        E.setAuthor(TL(M,"profile-of-user", getUser().getEffectiveName(), DefaultURL + "/p/" + Name));
        String power = POWERDECIMAL.format(getPower(null, null));
        E.setDescription("\"*" + getSignature().replaceAll("<br>", "\n") + "*\"\n" +
                (!isPowerDisabled(G) ? "**Average Power: " + BotEmoji.get("POW") + power + "**" : ""));

        if (getSwitchFriendcode() != null && getXtremeFriendcode() != null) {
            E.addField(BotEmoji.get("NintendoSwitch") + "Switch Code", getSwitchFriendcode(), true);
            E.addField("Xtreme Code", getXtremeFriendcode(), true);
            E.addBlankField(true);
        } else if (getSwitchFriendcode() != null) {
            E.addField(BotEmoji.get("NintendoSwitch") + "Switch Code", getSwitchFriendcode(), false);
        } else if (getXtremeFriendcode() != null) {
            E.addField("Xtreme Code", getXtremeFriendcode(), false);
        }
        Row Totals = Totals().getTotalStats();
        E.addField(getNationality().getFlag().getFormatted() + " " + TL(M,"Level") + " " + Totals().Level, BotEmoji.get("XP") + ": **" + Totals().CurrentXP + "/" + Totals().XPForNextLevel + "**\n"
                + BotEmoji.get("InaCoin") + ": **" + PRICEDECIMAL.format(getItem(1).Amount) + "** \n"
                + TL(M,"MatchDiff") + ": **(+" + Totals.get("Wins") + "/" + Totals.get("Ties") + "/-" + Totals.get("Loses") + ")**\n"
                + TL(M,"TotalGoalsDiff") + ": **(+" + Totals.get("GoalsScored") + "/-" + Totals.get("GoalsTaken") + ")**\n"
                + TL(M, "Completed-Quests") + ": **" + Totals().TotalQuests + "**", true);

        E.addField(Totals().getLeague().getEmojiFormatted() + " " + TL(M,"Matches"),
                TL(M,"TotalMedals") + " : **" + Totals.get("Medals") + "**\n"
                + "All Tournaments: **" + Totals.get("TCount") + "**\n"
                + "Top 1s: **" + Totals.get("T1") + "**\n"
                + "Top 2s: **" + Totals.get("T2") + "**\n"
                + "Top 3s: **" + Totals.get("T3") + "**", true);

        List<ClanMember> clans = ClanMember.OfUser(this);
        for (ClanMember CM : clans) {
            if (CM.isMainClan()) {
                E.addField(CM.getClan().getEmojiFormatted() + " " + CM.getClan().getName(), TL(M,"PlayerBelongsToXXXClan", "__" + CM.getClan().getName() + "__"), false);
                if (!CM.hasClanCard()) CM.regenerateClanCard(M);
            }
            else E.addField(CM.getClan().getEmojiFormatted() + " " + CM.getClan().getName(), TL(M,"PlayerReinforceToXXXClan", "__" + CM.getClan().getName() + "__"), false);
        }
        if (clans.isEmpty() && !hasPersonalCard()) regeneratePersonalCard(M);



        listTrophies(E);
        if (getSignature().equals("Hey I am a new player !")) {
            E.setFooter(TL(M,"profile-tip-signature"));
        } else if (getHexValue(getColor()).equals("#808080")) {
            E.setFooter(TL(M,"profile-tip-colorcode"));
        }
        if (!getUser().equals(M.getInteraction().getUser())) {
            E.setFooter(null);
        }
        Clan clan = Clan.getClanOfUser(getID());
        E.setImage(getClanCardGIF(clan));

        List<Button> BTN = new ArrayList<>();
        if (getWebsiteURL() != null) BTN.add(Button.link(getWebsiteURL(), TL(M,"Website")));
        if (getTwitchURL() != null) BTN.add(Button.link(getTwitchURL(), "Twitch"));
        if (getTwitterURL() != null) BTN.add(Button.link(getTwitterURL(), "X"));
        if (getTiktokURL() != null) BTN.add(Button.link(getTiktokURL(), "Tiktok"));
        if (getYouTubeURL() != null) BTN.add(Button.link(getYouTubeURL(), "YouTube"));
        if (getDiscordURL() != null && BTN.size() < 5) BTN.add(Button.link(getDiscordURL(), "Discord"));
        if (getInstagramURL() != null && BTN.size() < 5) BTN.add(Button.link(getInstagramURL(), "Instagram"));
        if (BTN.isEmpty()) {
            M.editOriginalEmbeds(E.build()).setReplace(true).queue();
        } else {
            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(BTN)).setReplace(true).queue();
        }
    }
    public void ViewGamesStats(InteractionHook M) {
        Guild G = M.getInteraction().getGuild();
        EmbedBuilder profile = new EmbedBuilder();
        profile.setTitle(TL(M,"profile-game-of-user", getUser().getEffectiveName()));
        profile.setDescription(TL(M,"profile-game-description"));
        profile.setThumbnail(getUser().getEffectiveAvatarUrl());
        profile.setColor(getColor());
        for (Profile_Game P : Profile_Game.ofUser(getID())) {
            if (P.Wins > 0 || P.Ties > 0 || P.Loses > 0) {
                int Rank = P.getRank();
                String emoji = Rank == 1 ? "(:first_place:)" : Rank == 2 ? "(:second_place:)" : Rank == 3 ? "(:third_place:)" : "";
                String endth = "th";
                if (Rank == 1) endth = "st";
                else if (Rank == 2) endth = "nd";
                else if (Rank == 3) endth = "rd";
                profile.addField(P.getGame().getEmoji() + " " + P.getGame().getCode(),
                        P.getLeague().getEmojiFormatted() + " **" + TL(M, "Rank") + ": " + Rank + endth + "** " + emoji + "\n"
                                + TL(M,"Medals") + ": **" + P.getMedals() + "**\n"
                                + TL(M,"Wins") + ": **" + P.getWins() + "**\n"
                                + TL(M,"Ties") + ": **" + P.getTies() + "**\n"
                                + TL(M,"Defeats") + ": **" + P.getLoses() + "**\n"
                                + TL(M,"GoalsScored") + ": **" + P.getGoalsScored() + "**\n"
                                + TL(M,"GoalsTaken") + ": **" + P.getGoalsTaken() + "**\n"
                                + TL(M,"Win_Streak") + ": **" + P.getWinStreak() + "**\n"
                                + TL(M,"Best_Win_Streak") + ": **" + P.getHighestWinStreak() + "**", true);
            }
        }
        if (G != null) {
            ServerInfo I = ServerInfo.get(G);
            if (I.Ranking().hasPrivateRanking()) {
                profile.addField(G.getName() + " ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", " ", false);
                for (Profile_Game_S P : Profile_Game_S.ofUser(getID(), G.getIdLong())) {
                    if (P.Wins > 0 || P.Ties > 0 || P.Loses > 0) {
                        int Rank = P.getRank();
                        String emoji = Rank == 1 ? "(:first_place:)" : Rank == 2 ? "(:second_place:)" : Rank == 3 ? "(:third_place:)" : "";
                        String endth = "th";
                        if (Rank == 1) endth = "st";
                        else if (Rank == 2) endth = "nd";
                        else if (Rank == 3) endth = "rd";

                        League L = I.Ranking().getLeagueByMedal(P.getMedals());
                        profile.addField(P.getGame().getEmoji() + " " + P.getGame().getCode(),
                                L.getEmojiFormatted() + " **" + TL(M, "Rank") + ": " + Rank + endth + "** " + emoji + "\n"
                                        + TL(M, "Medals") + ": **" + P.getMedals() + "**\n"
                                        + TL(M, "Wins") + ": **" + P.getWins() + "**\n"
                                        + TL(M, "Ties") + ": **" + P.getTies() + "**\n"
                                        + TL(M, "Defeats") + ": **" + P.getLoses() + "**\n"
                                        + TL(M, "GoalsScored") + ": **" + P.getGoalsScored() + "**\n"
                                        + TL(M, "GoalsTaken") + ": **" + P.getGoalsTaken() + "**\n"
                                        + TL(M, "Win_Streak") + ": **" + P.getWinStreak() + "**\n"
                                        + TL(M, "Best_Win_Streak") + ": **" + P.getHighestWinStreak() + "**", true);
                    }
                }
            }
        }
        M.editOriginalEmbeds(profile.build()).queue();
    }
    public void ViewRPG(InteractionHook M) {
        EmbedBuilder profile = new EmbedBuilder();
        profile.setThumbnail(getUser().getAvatarUrl());
        profile.setColor(getColor());
        profile.setAuthor(TL(M,"profile-stats-title", getUser().getEffectiveName()));
        String Element = BotEmoji.get(Totals().getElement().toString()).getFormatted();
        String Position = BotEmoji.get(Totals().getPosition().toString()).getFormatted();
        String Gender = BotEmoji.get(Totals().getGender().toString()).getFormatted();
        profile.addField(TL(M,"Statistics"),
                TL(M,"Element") + ": **" + Element + "**\n" +
                        TL(M,"Position") + ": **" + Position + "**\n" +
                        TL(M,"Gender") + ": **" + Gender + "**\n" +
                        "• " + TL(M,"Kick") + ": **" + Totals().getKick() + "**\n" +
                        "• " + TL(M,"Dribble") + ": **" + Totals().getDribble() + "**\n" +
                        "• " + TL(M,"Block") + ": **" + Totals().getBlock() + "**\n" +
                        "• " + TL(M,"Catch") + ": **" + Totals().getCatch() + "**\n" +
                        "• " + TL(M,"Technique") + ": **" + Totals().getTechnique() + "**\n" +
                        "• " + TL(M,"Speed") + ": **" + Totals().getSpeed() + "**\n" +
                        "• " + TL(M,"Stamina") + ": **" + Totals().getStamina() + "**\n" +
                        "• " + TL(M,"Luck") + ": **" + Totals().getLuck() + "**"
                , true);
        M.editOriginalEmbeds(profile.build()).queue();
    }
    public void ViewPowerDetails(InteractionHook M, FilterCommand FILCMD, GamesCommand GMCMD) {
        if (!isPowerDisabled(M.getInteraction().getGuild())) {
            EmbedBuilder E = new EmbedBuilder();
            E.setThumbnail(getUser().getEffectiveAvatarUrl());
            E.setColor(getColor());
            E.setTitle(TL(M, "power-details-of", getUser().getEffectiveName()));
            E.setDescription(TL(M,"power-details-description"));
            E.setFooter(TL(M,"Filter") + ": " + FILCMD.Filter);

            double INPUT = 0.1;
            E.addField(TL(M, "Member"),
                    BotEmoji.get("POW") + " **+" + POWERDECIMAL.format(INPUT) + "**",
                    true);

            DatabaseObject.Row TR = PowerSQL(FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")));
            INPUT = TR.getAsDouble("POW: League");
            E.addField(TL(M, "League") + " (**x" + TR.get("Amount: League") + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("POW: Trophies");
            E.addField(TL(M, "Trophies") + " (**x" + TR.get("Amount: Trophies") + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("POW: Event");
            E.addField(TL(M, "Event") + " (**x" + TR.get("Amount: Event") + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "**",
                    true);
            INPUT = TR.getAsDouble("POW: Tournament");
            E.addField(TL(M, "Tournaments") + " (**x" + TR.get("Amount: Tournament") + "**)",
                    BotEmoji.get("POW") + " **" + PlusMinusSign(INPUT) + POWERDECIMAL.format(INPUT) + "** (x" + TR.get("Polyvalence Multiplier") + ")",
                    true);
            INPUT = TR.getAsDouble("Total Power");
            E.addField(TL(M, "Total"),
                    BotEmoji.get("POW") + " **" + POWERDECIMAL.format(INPUT) + "**",
                    false);

            {
                TR = getActivity(FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")));
                E.addField(":chart_with_upwards_trend: " + TL(M,"Activity"),
                        "> **" + TL(M, "Last_Month") + ": " + TR.get("Match Activity") + "**\n" +
                                "> **" + TL(M, "Last_Months_Average", "3") + ": " + POWERDECIMAL.format(TR.getAsDouble("Average Match Activity")) + "**\n",
                        false);
                E.addField(":chart_with_upwards_trend: " + TL(M,"Tournament-Participation-Rate"),
                        "> **" + TL(M, "Last_Month") + ": " + TR.get("Tournament Activity") + "**\n" +
                                "> **" + TL(M, "Last_Months_Average", "3") + ": " + POWERDECIMAL.format(TR.getAsDouble("Average Tournament Activity")) + "**\n",
                        false);
            }

            BuiltMessageE MSG = new BuiltMessageE(M);
            MSG.addEmbeds(E.build());

            if (M.getInteraction().getGuild() != null && !M.getInteraction().getGuild().isDetached()) {
                MSG.EnableFilter(FILCMD, 0, 3,
                        SelectOption.of(M.getInteraction().getGuild().getName(), "S" + M.getInteraction().getGuild().getId()).withDescription(TL(M,"view-only-this-server")).withDefault(FILCMD.getServerID() != null)
                );
            }

            MSG.EnableGames(GMCMD, 0, 3);

            M.editOriginal(MSG.build()).queue();
        } else {
            M.editOriginal(TL(M, "Power_Disabled_Server")).queue();
        }
    }
    public void ViewTournaments(InteractionHook M, PageViewerCommand PGCMD, FilterCommand FILCMD, GamesCommand GMCMD) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        E.setTitle(TL(M,"tournament-list-of", getUser().getEffectiveName()));
        E.setDescription(TL(M,"tournament-list-description"));
        E.setFooter(TL(M,"Filter") + ": " + FILCMD.Filter);
        Game G;
        String medal;
        String name;
        String power;
        String clan;
        int AmountPerPages = 15;
        List<DatabaseObject.Row> TRs = DatabaseObject.doQueryAll("CALL DisplayUserTournaments(?,?,?,?,?,?);", getID(), FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")), PGCMD.Page, AmountPerPages, true); // userid, serverid, gamelist, page, amountPerPage, private
        for (DatabaseObject.Row TR : TRs) {
            G = Game.get(TR.getAsString("GameCode"));
            medal = TR.getAsInt("Position") == 1 ? ":first_place:" : TR.getAsInt("Position") == 2 ? ":second_place:" : TR.getAsInt("Position") == 3 ? ":third_place:" : TR.getAsInt("Position") <= 0 ? ":Out:" : TR.getAsInt("Position") + "th";
            name = TR.getAsString("Name");
            clan = "";
            power = "";
            Clan CC = getClanAtTime(Instant.ofEpochSecond(TR.getAsLong("CompletedAtTimeEpochSecond")));
            if (CC != null) clan = " | " + TL(M,"Clan") + ": **" + CC.getEmojiFormatted() + " " + CC.getName() + "**";

            if (!isPowerDisabled(M.getInteraction().getGuild()) && TR.getAsBoolean("isPublic")) {
                power = " | " + TL(M,"Power") + ": **" + BotEmoji.get("POW") + " +" + POWERDECIMAL.format(TR.getAsDouble("Power")) + "**";
            }
            E.addField((!TR.getAsBoolean("isPublic") ? "[:lock:] " : "") + "(" + G.getEmoji() + ") (" + medal + ") " + name.replaceAll("(?i)VICTORY ROAD", "VR").replaceAll("(?i)INAZUMA ELEVEN GO", "IEGO").replaceAll("(?i)INAZUMA ELEVEN", "IE"),
                    (clan + power).replaceFirst(" \\|", "> ") + "\n" +
                            "> " + TL(M,"Wins") + ": **" + TR.getAsInt("Wins") + "** | " + TL(M,"Defeats") + ": **" + TR.getAsInt("Loses") + "** | " + TL(M,"TotalGoalsDiff") + ": **+" + TR.getAsInt("GoalsScored") + "/-" + TR.getAsInt("GoalsTaken") + "**\n" +
                            "> " + TL(M,"Date") + ": <t:" + TR.getAsLong("CompletedAtTimeEpochSecond") + ":d> | " + TL(M,"Players") + ": **" + (TR.getAsInt("ParticipantCount") * TR.getAsInt("VSAmount")) + "** | [" + TL(M,"View_Bracket") + "](" + TR.getAsString("FullChallongeURL") + ")"
                    , false);
        }

        BuiltMessageE MSG = new BuiltMessageE(M);
        MSG.addEmbeds(E.build());

        if (M.getInteraction().getGuild() != null && !M.getInteraction().getGuild().isDetached()) {
            MSG.EnableFilter(FILCMD, 0, 3,
                    SelectOption.of(M.getInteraction().getGuild().getName(), "S" + M.getInteraction().getGuild().getId()).withDescription(TL(M,"view-only-this-server")).withDefault(FILCMD.getServerID() != null)
            );
        }

        MSG.EnableGames(GMCMD, 0, 3);
        MSG.EnablePagination(PGCMD, 25, doQueryValue(Integer.class, "SELECT GetUserTournamentsCount(?,?,?,?) AS 'Count'", getID(), FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(",")), true).orElse(0));

        M.editOriginal(MSG.build()).queue();
    }
    public void ViewHistory(InteractionHook M, PageViewerCommand PGCMD, FilterCommand FILCMD, GamesCommand GMCMD) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        E.setAuthor(TL(M,"profile-match-history-title", getUser().getEffectiveName()));
        E.setDescription(TL(M, "time-displayed"));
        E.setFooter(TL(M,"Filter") + ": " + FILCMD.Filter);

        int P1Score, P2Score;
        String emoji;
        String P1NameSurround, P2NameSurround;
        String Name1, Name2;
        String newLog, newTime;
        String servername;
        String newTournament;

        int AmountPerPages = 12;
        for (MatchLog log : getMatchLogs(FILCMD.getServerID(), GMCMD.Games, PGCMD.Page, AmountPerPages)) {
            if (E.getFields().size() < 12) {
                Name1 = getUser().getName();
                Name2 = getID() == log.getP1ID() ? log.getP2().getName() : log.getP1().getName();
                P1Score = getID() == log.getP1ID() ? log.getP1Score() : log.getP2Score();
                P2Score = getID() == log.getP1ID() ? log.getP2Score() : log.getP1Score();
                P1NameSurround = P1Score > P2Score ? "" : P1Score < P2Score ? "~~" : "__";
                P2NameSurround = P1Score > P2Score ? "~~" : P1Score < P2Score ? "" : "__";
                emoji = P1Score > P2Score ? BotEmoji.get("Win").getFormatted() : P1Score < P2Score ? BotEmoji.get("Lose").getFormatted() : BotEmoji.get("Tie").getFormatted();
                newLog = P1NameSurround + "`" + Name1 + "`" + P1NameSurround + ": " + P1Score + " " + log.getGame().getVSEmoji() + " " + P2Score + " :" + P2NameSurround + "`" + Name2 + "`" + P2NameSurround + " (" + emoji + ")";

                newTime = "> ├ <t:" + log.getTimeCreated().getEpochSecond() + ":R> (<t:" + log.getTimeCreated().getEpochSecond() + ":f>)\n";

                servername = "> ├ " + (log.getServerID() != null ? StopString(ServerInfo.get(log.getServerID()).getName(), 48) : " Direct Message") + "\n";

                SChallonge_Match CM = SChallonge_Match.getByMatchLog(log.getId());
                try {
                    newTournament = CM != null ? "> ├ " + CM.getRoundShort() + ": " + (CM.T.getTournament().getName().length() > 40 ? CM.T.getTournament().getName().replaceAll("(?i)VICTORY ROAD", "VR").replaceAll("(?i)INAZUMA ELEVEN GO", "IEGO").replaceAll("(?i)INAZUMA ELEVEN", "IE") : CM.T.getTournament().getName()) + "\n" : "";
                } catch (DataAccessException e) {
                    newTournament = "";
                }

                E.addField(log.getGame().getEmojiFormatted() + " " + newLog,
                        replaceLast(newTournament + servername + newTime, "├", "└"), false);
            }
        }


        BuiltMessageE MSG = new BuiltMessageE(M);
        MSG.addEmbeds(E.build());

        if (M.getInteraction().getGuild() != null && !M.getInteraction().getGuild().isDetached()) {
            MSG.EnableFilter(FILCMD, 0, 3,
                    SelectOption.of(M.getInteraction().getGuild().getName(), "S" + M.getInteraction().getGuild().getId()).withDescription(TL(M,"view-only-this-server")).withDefault(FILCMD.getServerID() != null)
            );
        }

        MSG.EnableGames(GMCMD, 0, 3);
        MSG.EnablePagination(PGCMD, 25, doQueryValue(Integer.class, "SELECT GetUserHistoryCount(?,?,?) AS 'Count'", getID(), FILCMD.getServerID(), GMCMD.Games.isEmpty() ? null : GMCMD.Games.stream().map(Game::getCode).collect(Collectors.joining(","))).orElse(0));

        M.editOriginal(MSG.build()).queue();
    }
    public void ViewClanHistory(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        E.setAuthor(TL(M,"profile-clan-history-title", getUser().getEffectiveName()));
        E.setDescription(TL(M,"profile-clan-history-description"));
        ArrangeClanLogs();
        String S = "";

        Clan clan = Clan.getClanOfUser(getID());
        if (clan != null) S = S + "> **" + clan.getEmojiFormatted() + " " + clan.getName() + ":** " + TL(M,"Member_Since") + " __<t:" + clan.getMemberById(getID()).getTimeJoined().getEpochSecond() + ":d>__\n";
        Clan clan2 = Clan.getReinforcementOfUser(getID());
        if (clan2 != null) S = S + "> **" + clan2.getEmojiFormatted() + " " + clan2.getName() + ":** " + TL(M,"Reinforcing_Since") + " __<t:" + clan2.getMemberById(getID()).getTimeJoined().getEpochSecond() + ":d>__";
        if (!S.isEmpty()) E.addField(TL(M,"Current-Clan"), S, false);

        List<Profile_PastClan> CLs = getClanLogs(true);
        boolean isFirst = true;
        S = "";
        if (!CLs.isEmpty()) {
            for (Profile_PastClan log : CLs) {
                if (log.getClan().getStatus().equals("Open"))
                    S = S + "> **" + log.getClan().getEmojiFormatted() + " " + log.getClan().getName() + ":** __<t:" + log.getTimeJoined().getEpochSecond() + ":d>__ - __<t:" + log.getTimeLeft().getEpochSecond() + ":d>__\n";
                else
                    S = S + "> **~~" + log.getClan().getEmojiFormatted() + " " + log.getClan().getName() + "~~:** __<t:" + log.getTimeJoined().getEpochSecond() + ":d>__ - __<t:" + log.getTimeLeft().getEpochSecond() + ":d>__\n";
                S = S + "> └ " + log.getDuration(M) + "\n";
                if (S.length() > 800 && S.length() + E.length() < 5800) {
                    E.addField(isFirst ? ":small_blue_diamond: **Main Clan**" : " ", S, false);
                    S = "";
                    isFirst = false;
                }
            }
            if (!S.isEmpty() && S.length() + E.length() < 5800) {
                E.addField(isFirst ? ":small_blue_diamond: **Main Clan**" : " ", S, false);
            }
        }
        CLs = getClanLogs(false);
        isFirst = true;
        S = "";
        if (!CLs.isEmpty()) {
            for (Profile_PastClan log : CLs) {
                if (log.getClan().getStatus().equals("Open"))
                    S = S + "> **" + log.getClan().getEmojiFormatted() + " " + log.getClan().getName() + ":** __<t:" + log.getTimeJoined().getEpochSecond() + ":d>__ - __<t:" + log.getTimeLeft().getEpochSecond() + ":d>__\n";
                else
                    S = S + "> **~~" + log.getClan().getName() + "~~:** __<t:" + log.getTimeJoined().getEpochSecond() + ":d>__ - __<t:" + log.getTimeLeft().getEpochSecond() + ":d>__\n";
                S = S + "> └ " + log.getDuration(M) + "\n";
                if (S.length() > 800 && S.length() + E.length() < 5990) {
                    E.addField(isFirst ? ":small_orange_diamond: **" + TL(M, "Reinforcement") + "**" : " ", S, false);
                    S = "";
                    isFirst = false;
                }
            }
            if (!S.isEmpty() && S.length() + E.length() < 5990) {
                E.addField(isFirst ? ":small_orange_diamond: **" + TL(M, "Reinforcement") + "**" : " ", S, false);
            }
        }
        M.editOriginalEmbeds(E.build()).queue();
    }
    public void ViewInventory(InteractionHook M, PageViewerCommand PGCMD) {
        setItem(1001, doQueryValue(Integer.class,"""
                SELECT Count(*) AS Count FROM challonge_participant CP
                JOIN challonge_tournament CT ON CT.ID = CP.TournamentID
                WHERE Position = ? AND CT.isPublic AND (DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ?);
                """,1, getID(), getID(), getID(), getID(), getID(), getID(), getID(), getID()).orElse(0));
        setItem(1002, doQueryValue(Integer.class,"""
                SELECT Count(*) AS Count FROM challonge_participant CP
                JOIN challonge_tournament CT ON CT.ID = CP.TournamentID
                WHERE Position = ? AND CT.isPublic AND (DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ?);
                """,2, getID(), getID(), getID(), getID(), getID(), getID(), getID(), getID()).orElse(0));
        setItem(1003, doQueryValue(Integer.class,"""
                SELECT Count(*) AS Count FROM challonge_participant CP
                JOIN challonge_tournament CT ON CT.ID = CP.TournamentID
                WHERE Position = ? AND CT.isPublic AND (DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ?);
                """,3, getID(), getID(), getID(), getID(), getID(), getID(), getID(), getID()).orElse(0));

        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        E.setAuthor(TL(M,"profile-inventory-title", getUser().getEffectiveName()));
        E.setDescription(TL(M,"profile-inventory-description"));
        E.addField(Item.get(1).getEmojiFormatted() + " " + PRICEDECIMAL.format(getItem(1).Amount), " ", true);
        E.addField(Item.get(2).getEmojiFormatted() + " " + PRICEDECIMAL.format(getItem(2).Amount), " ", true);
        String ActiveBooster = "";
        for (Profile_Booster boosters : getBoosters()) ActiveBooster = BotEmoji.get("XPBoost") + boosters.getName() + " **--> " + TL(M,"End") + ":** <t:" + boosters.getTimeFinished().getEpochSecond() + ":R>\n";
        if (ActiveBooster.length() > 1) E.addField(TL(M, "Active_Boosters"), ActiveBooster, false);
        E.addField(" ", " ", false);

        String itemList = "", previousType = "", typename, servername;
        for (DatabaseObject.Row I : doQueryAll("""
                SELECT * FROM inazuma_competitive.profile_inventory
                WHERE UserID = ? AND NOT Type = 'CURRENCY'
                LIMIT 25 OFFSET ?;""", getID(), 25 * (PGCMD.Page - 1))) {
            servername = I.getAsString("ServerName");
            typename = Item.ItemType.valueOf(I.getAsString("Type")).getName(M);
            if (servername != null) {
                if (!previousType.equals(servername)) {
                    previousType = servername;
                    itemList += "\n**" + previousType + "**\n";
                }
            } else {
                if (!previousType.equals(typename)) {
                    previousType = typename;
                    itemList += "\n**" + previousType + "**\n";
                }
            }

            itemList += I.getAsString("Formatted") + " " + I.getAsString("Name") + " **×" + I.getAsInt("Amount") + (servername != null ? " (" + typename + ")" : "") + "**\n";
            if (itemList.length() > 900) {
                E.addField(" ", itemList, false);
                itemList = "";
            }
        }
        if (Range(itemList.length(), 1, 1000)) {
            E.addField(" ", itemList, true);
        }

        List<SelectOption> cosmeticsframe = new ArrayList<>();
        List<SelectOption> cosmeticsboard = new ArrayList<>();
        List<SelectOption> usebooster = new ArrayList<>();
        for (Item.Item_To_Profile ITEM : getItems()) {
            if (ITEM.Amount > 0) {
                if (ITEM.getType().equals(Item.ItemType.COSMETICS_FRAME) || ITEM.getType().equals(Item.ItemType.COSMETICS_BOARD)) {
                    if (ITEM.getType().equals(Item.ItemType.COSMETICS_FRAME)) cosmeticsframe.add(SelectOption.of(ITEM.getName(), ITEM.getId() + "").withDescription(ITEM.getDescription()).withEmoji(ITEM.getEmoji().retrieve()).withDefault(CustomFrame != null && CustomFrame == ITEM.getId()));
                    if (ITEM.getType().equals(Item.ItemType.COSMETICS_BOARD)) cosmeticsboard.add(SelectOption.of(ITEM.getName(), ITEM.getId() + "").withDescription(ITEM.getDescription()).withEmoji(ITEM.getEmoji().retrieve()).withDefault(CustomBoard  != null && CustomBoard == ITEM.getId()));
                } else if (ITEM.getType().equals(Item.ItemType.BOOSTERS_XP) || ITEM.getType().equals(Item.ItemType.BOOSTERS_COIN)) {
                    usebooster.add(SelectOption.of(ITEM.getName(), ITEM.getId() + "").withDescription(ITEM.getDescription()).withEmoji(ITEM.getEmoji().retrieve()));
                }
            }
        }

        BuiltMessageE MSG = new BuiltMessageE(M);
        MSG.addEmbeds(E.build());
        if (getID() == M.getInteraction().getUser().getIdLong()) {
            ProfileCommand CMD = new ProfileCommand(this);
            if (!cosmeticsframe.isEmpty()) {
                cosmeticsframe.add(SelectOption.of(TL(M,"Default"), "0").withDescription("Default design.").withDefault( getCustomFrameItem() == null));
                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("pf-manage-equip-frame")).setPlaceholder(TL(M, "profile-inventory-equip-cosmetic")).setRequiredRange(1, 1).addOptions(cosmeticsframe).build();
                MSG.addComponents(ActionRow.of(menu));
            }
            if (!cosmeticsboard.isEmpty()) {
                cosmeticsboard.add(SelectOption.of(TL(M,"Default"), "0").withDescription("Default design.").withDefault(getCustomBoardItem() == null));
                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("pf-manage-equip-board")).setPlaceholder(TL(M, "profile-inventory-equip-cosmetic")).setRequiredRange(1, 1).addOptions(cosmeticsboard).build();
                MSG.addComponents(ActionRow.of(menu));
            }
            if (!usebooster.isEmpty()) {
                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("pf-manage-equip-booster")).setPlaceholder(TL(M, "profile-inventory-use-booster")).setRequiredRange(1, 1).addOptions(usebooster).build();
                MSG.addComponents(ActionRow.of(menu));
            }
        }
        MSG.EnablePagination(PGCMD, 25, getItems().stream().filter(I -> !I.getType().equals(Item.ItemType.CURRENCY)).toList().size());
        M.editOriginal(MSG.build()).queue();
    }
    public void ViewQuests(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        E.setTitle(TL(M, "profile-quests-title", getUser().getEffectiveName()));
        E.setDescription(TL(M, "quests-description") + "\n"
                + TL(M, "Total") + ": " + Totals().TotalQuests + "\n"
                + TL(M, "Total") + " (Daily): " + Totals().TotalDailies);
        E.addField(":small_orange_diamond:" + TL(M, "Active-Quests") + " ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", " ", false);
        if (!getUnderwayQuests().isEmpty()) {
            if (!hasDailies()) {
                E.addField(" ", "> Next Daily: <t:" + NextDailyReceiveTime + ":R>", false);
            }
            if (!hasWeeklies()) {
                E.addField(" ", "> Next Weekly: <t:" + NextWeeklyReceiveTime + ":R>", false);
            }
            if (!hasMonthlies()) {
                E.addField(" ", "> Next Monthly: <t:" + NextMonthlyReceiveTime + ":R>", false);
            }
            for (Profile_Quest Q : getUnderwayQuests()) {
                if (E.getFields().size() < 25) {
                    String objList = Q.getObjectives().list().stream().map(s -> "> " + (s.isComplete() ? "~~" : "") + s.getDescription() + " **[" + s.getProgress() + "/" + s.getRequiredProgress() + "]**" + (s.isComplete() ? "~~" : "")).collect(Collectors.joining("\n"));
                    String rewards = Q.getRewards().list().stream().map(s -> s.getEmojiFormatted() + s.Amount).collect(Collectors.joining(" • "));
                    if (!Q.getRewards().list().isEmpty() && Q.getRewards().list().getFirst().getItem().equals(Item.get(1))) {
                        rewards = BotEmoji.get("XP") + " " + (int) (Q.getRewards().list().getFirst().Amount * 1.25) + " • " + rewards;
                    }
                    E.addField(Q.getEmojiFormatted() + " • " + Q.getName() + (Q.getDeadline() != null ? " • Deadline: <t:" + Q.getDeadline().getEpochSecond() + ":R>" : "")
                            , "> Rewards: **" + rewards + "**\n" + objList, false);
                }
            }
        } else {
            E.addField(" ", "> Next Daily: <t:" + NextDailyReceiveTime + ":R>", false);
            E.addField(" ", "> Next Weekly: <t:" + NextWeeklyReceiveTime + ":R>", false);
            E.addField(" ", "> Next Monthly: <t:" + NextMonthlyReceiveTime + ":R>", false);
        }
        E.addField(":small_blue_diamond: " + TL(M, "Completed-Quests") + " ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", " ", false);
        if (!getCompletedQuests().isEmpty()) {
            for (Profile_Quest Q : getCompletedQuests()) {
                if (E.getFields().size() < 25 && E.getFields().stream().noneMatch(F -> F.getName() == null || F.getName().contains(Q.getName()))) {
                    long matches = getCompletedQuests().stream().filter(QQ -> QQ.getName().equals(Q.getName())).count();
                    String amount = matches > 1 ? " (x" + matches + ")" : "";
                    String time = " • Completed at <t:" + Q.getCompletedTime().getEpochSecond() + ":R>";
                    String objList = Q.getObjectives().list().stream().map(s -> "> - " + (s.isComplete() ? "~~" : "") + s.getDescription() + " **[" + s.getProgress() + "/" + s.getRequiredProgress() + "]**" + (s.isComplete() ? "~~" : "")).collect(Collectors.joining("\n"));
                    E.addField(":ballot_box_with_check: • " + Q.getName() + amount + time, objList, false);
                }
            }
        } else {
            E.addField(" ", "- " + TL(M, "None"), false);
        }
        M.editOriginalEmbeds(E.build()).queue();
    }
    public void ViewAchievements(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        E.setTitle(TL(M, "profile-achievements-title", getUser().getEffectiveName()));
        E.setDescription(TL(M, "achievements-description"));
        UpdateAchievement();
        for (Profile_Achievement Q : getUnderwayAchievements()) {
            if (E.getFields().size() < 25) {
                String objList = Q.getObjectives().list().stream().map(s -> "> " + (s.isComplete() ? "~~" : "") + s.getDescription() + " **[" + s.getProgress() + "/" + s.getRequiredProgress() + "]**" + (s.isComplete() ? "~~" : "")).collect(Collectors.joining("\n"));
                String rewards = Q.getRewards().list().stream().map(s -> s.getEmojiFormatted() + s.Amount).collect(Collectors.joining(" • "));
                if (!Q.getRewards().list().isEmpty() && Q.getRewards().list().getFirst().getItem().equals(Item.get(1))) {
                    rewards = BotEmoji.get("XP") + " " + (int) (Q.getRewards().list().getFirst().Amount * 1.25) + " • " + rewards;
                }
                E.addField(Q.getEmojiFormatted() + " • " + Q.getName(), "> Rewards: **" + rewards + "**\n" + objList, false);
            }
        }
        for (Profile_Achievement Q : getCompletedAchievements()) {
            if (E.getFields().size() < 25) {
                String objList = Q.getObjectives().list().stream().map(s -> "> - " + (s.isComplete() ? "~~" : "") + s.getDescription() + " **[" + s.getProgress() + "/" + s.getRequiredProgress() + "]**" + (s.isComplete() ? "~~" : "")).collect(Collectors.joining("\n"));
                E.addField(":ballot_box_with_check: • " + Q.getName(), objList, false);
            }
        }
        M.editOriginalEmbeds(E.build()).queue();
    }
    public void ViewTrophies(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        E.setTitle(TL(M, "Trophies"));
        E.setDescription(TL(M,"trophies-description"));
        listTrophies(E);
        M.editOriginalEmbeds(E.build()).queue();
    }
    public void ViewLicenses(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M,"profile-license-title", getUser().getEffectiveName()));
        E.setDescription(TL(M, "profile-license-description"));
        E.setThumbnail(getUser().getAvatarUrl());
        E.setColor(getColor());
        List<Button> BTNs = new ArrayList<>();
        if (getCharacter().exists()) {
            List<MessageEmbed> Es = new ArrayList<>();
            E.setImage(getCardPNGURL());
            if (!E.isEmpty()) Es.add(E.build());
            if (isHasGIF()) {
                E = new EmbedBuilder();
                E.setColor(getColor());
                E.setImage(getCardGIFURL());
                if (!E.isEmpty()) Es.add(E.build());
            }
            BTNs.add(Button.secondary(new ProfileCommand(this).Command("pf-refresh-cards-priv"), TL(M,"Refresh") + ": Personal"));
            for (ClanMember C : ClanMember.OfUser(this)) {
                if (getCharacter().exists()) {
                    E = new EmbedBuilder();
                    E.setImage(C.getCardPNGURL());
                    E.setColor(C.getClan().getColor());
                    if (!E.isEmpty()) Es.add(E.build());
                    if (isHasGIF()) {
                        E = new EmbedBuilder();
                        E.setImage(C.getCardGIFURL());
                        E.setColor(C.getClan().getColor());
                        if (!E.isEmpty()) Es.add(E.build());
                    }
                    BTNs.add(Button.secondary(new ProfileCommand(this).Command("pf-refresh-cards-" + C.getClanID()), TL(M,"Refresh") + ": " + C.getClan().getName()));
                }
            }
            M.editOriginalEmbeds(Es).setComponents(ActionRow.of(BTNs)).setReplace(true).queue();
        } else {
            M.editOriginalEmbeds(E.build()).setReplace(true).queue();
        }
    }


    public void ManageProfileUI(InteractionHook M, ProfileCommand CMD) {
        List<ActionRow> ARs = new ArrayList<>();
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M,"profile-manager"));
        E.setAuthor(" • " + getUser().getEffectiveName(), null, getUser().getAvatarUrl());
        E.setThumbnail(getUser().getAvatarUrl());
        E.setImage(getPersonalCardGIF());
        E.setColor(getColor());
        E.setDescription(TL(M,"profile-manager-description") + "\n"
                + "> " + TL(M,"Color") + ": `" + getHexValue(getColor()) + "`\n"
                + "> " + TL(M,"Nationality") + ": **" + getNationality().getNamePlusFlag() + "**\n"
                + "> " + TL(M,"Birthday") + ": " + (getBirthday() != null ? "<t:" + getBirthday().getEpochSecond() + ":d>" : "`N/A`") + "\n"
                + "> Strikers Code: `" + getStrikersFriendcode() + "`\n"
                + "> Xtreme Code: `" + getXtremeFriendcode() + "`\n"
                + "> Switch Code: `" + getSwitchFriendcode() + "`\n\n"
                + "> Tiktok: `" + getTiktokURL() + "`\n"
                + "> Twitch: `" + getTwitchURL() + "`\n"
                + "> Twitter: `" + getTwitterURL() + "`\n"
                + "> YouTube: `" + getYouTubeURL() + "`\n"
                + "> Discord: `" + getDiscordURL() + "`\n"
                + "> Website: `" + getWebsiteURL() + "`\n"
                + "> Instagram: `" + getInstagramURL() + "`\n\n"
                + "> " + TL(M,"Signature") + ": `" + getSignature() + "`");

        List<SelectOption> options = new ArrayList<>();
        for (Clan C : getClansOfUser(CMD.ID)) {
            options.add(SelectOption.of(TL(M,"clan-number"), "pf-manage-number-" + C.getID()).withDescription(TL(M,"edit-clan-number-description", C.getName())).withEmoji(C.getEmoji().retrieve()));
        }
        options.add(SelectOption.of(TL(M,"Color"), "pf-manage-color").withDescription(TL(M,"edit-colorcode-description")).withEmoji(Emoji.fromUnicode("U+1F3A8")));
        options.add(SelectOption.of(TL(M,"Signature"), "pf-manage-bio").withDescription(TL(M,"edit-signature-description")).withEmoji(Emoji.fromUnicode("U+1F4DD")));
        options.add(SelectOption.of(TL(M,"Character-Image"), "pf-manage-character").withDescription(TL(M,"edit-character-description") + " [/profile-character]").withEmoji(BotEmoji.get("Members").retrieve()));
        options.add(SelectOption.of(TL(M,"Birthday"), "pf-manage-birthday").withDescription(TL(M,"edit-birthday-description")).withEmoji(Emoji.fromUnicode("U+1F4C5")));
        options.add(SelectOption.of(TL(M,"Nationality"), "pf-manage-nationality").withDescription(TL(M,"edit-nationality-description")).withEmoji(Emoji.fromUnicode("U+1F1FA U+1F1F3")));
        options.add(SelectOption.of(TL(M,"Friendcodes"), "pf-manage-friendcode").withDescription(TL(M,"edit-friendcode-description")).withEmoji(Emoji.fromUnicode("U+26A1")));
        options.add(SelectOption.of("Twitch", "pf-manage-media-twitch").withDescription(TL(M, "edit-media-description", "Twitch")).withEmoji(Emoji.fromUnicode("U+1F47E")));
        options.add(SelectOption.of("Twitter", "pf-manage-media-twitter").withDescription(TL(M, "edit-media-description", "Twitter")).withEmoji(Emoji.fromUnicode("U+1F426")));
        options.add(SelectOption.of("Website", "pf-manage-media-website").withDescription(TL(M, "edit-media-description", "Website")).withEmoji(Emoji.fromUnicode("U+1F4BB")));
        options.add(SelectOption.of("Discord", "pf-manage-media-discord").withDescription(TL(M, "edit-media-description", "Discord")).withEmoji(Emoji.fromUnicode("U+1F4F1")));
        options.add(SelectOption.of("YouTube", "pf-manage-media-youtube").withDescription(TL(M, "edit-media-description", "YouTube")).withEmoji(Emoji.fromUnicode("U+25B6 U+FE0F")));
        options.add(SelectOption.of("Instagram", "pf-manage-media-instagram").withDescription(TL(M, "edit-media-description", "Instagram")).withEmoji(Emoji.fromUnicode("U+1F4F7")));
        options.add(SelectOption.of("Tiktok", "pf-manage-media-tiktok").withDescription(TL(M, "edit-media-description", "Tiktok")).withEmoji(Emoji.fromUnicode("U+1F4F7")));
        StringSelectMenu Advanced = StringSelectMenu.create(CMD.Command("pf-manage-menu"))
                .setPlaceholder(TL(M, "Signature") + ", " + TL(M, "Nationality") + ", " + TL(M, "Birthday") + "...")
                .setRequiredRange(1, 1).addOptions(options).build();


        List<SelectOption> options2 = new ArrayList<>();
        if (getItem("Shiny Card").Amount >= 1) options2.add(SelectOption.of(TL(M,"edit-toggle-card-gif"), "pf-toggle-card-gif").withDescription(TL(M,"edit-toggle-card-gif-description")).withEmoji(Emoji.fromUnicode("U+2699")).withDefault(isHasGIF()));
        options2.add(SelectOption.of(TL(M,"edit-toggle-matchmaking-notif"), "pf-toggle-matchmaking").withDescription(TL(M,"edit-toggle-matchmaking-notif-description")).withEmoji(Emoji.fromUnicode("U+2699")).withDefault(isHasMatchmakingNotification()));
        options2.add(SelectOption.of(TL(M,"edit-toggle-tournament-notif"), "pf-toggle-tournament").withDescription(TL(M,"edit-toggle-tournament-notif-description")).withEmoji(Emoji.fromUnicode("U+2699")).withDefault(isHasTournamentNotification()));
        options2.add(SelectOption.of(TL(M,"edit-toggle-profile-private"), "pf-toggle-private").withDescription(TL(M,"edit-toggle-profile-private-description")).withEmoji(Emoji.fromUnicode("U+2699")).withDefault(isHasPrivateProfile()));
        StringSelectMenu Togglers = StringSelectMenu.create(CMD.Command("pf-manage-toggle"))
                .setPlaceholder(options2.getFirst().getLabel())
                .setRequiredRange(0, 25).addOptions(options2)
                .build();
        
        Button btn1 = Button.secondary(CMD.Command("pf-manage-license-bg"), "License BG");
        Button btn2 = Button.secondary(CMD.Command("pf-manage-license-fg"), "License FG");
        Button btn3 = Button.secondary(CMD.Command("pf-manage-license-ry"), "License RY").asDisabled();
        Button btn4 = Button.secondary(CMD.Command("pf-manage-license-st"), "License ST").asDisabled();
        Button btn5 = Button.secondary(CMD.Command("pf-manage-license-sp"), "License SP");

        ARs.add(ActionRow.of(Advanced));
        ARs.add(ActionRow.of(Togglers));
        List<ClanMember> CMs = ClanMember.OfUser(CMD.ID);
        if (CMs.size() > 1) {
            List<SelectOption> MyClans = new ArrayList<>();
            for (ClanMember CM : CMs) {
                MyClans.add(SelectOption.of((CM.isMainClan() ? "Main" : TL(M, "Reinforcement")) + ": " + CM.getClan().getTag() + " • " + CM.getClan().getName(), CM.getClanID() + "")
                        .withDescription(CM.getClan().getNationality().getName() + " | [" + CM.getClan().getMemberCount() + "/50] | " + CM.getClan().getPowerAsString())
                        .withEmoji(CM.getClan().getEmoji().retrieve())
                        .withDefault(CM.isMainClan()));
            }
            StringSelectMenu Main = StringSelectMenu.create(CMD.Command("pf-manage-set-main-clan"))
                    .setPlaceholder(TL(M, "edit-set-main-clan"))
                    .setRequiredRange(0, 1).addOptions(MyClans)
                    .build();
            ARs.add(ActionRow.of(Main));
        }
        ARs.add(ActionRow.of(btn1, btn2, btn3, btn4, btn5));
        if (isTournamentManager(M.getInteraction().getUser())) ARs.add(ActionRow.of(Button.secondary("pf-edit-other-user-by-id", "[ADMIN] Click here to modify another user by ID.")));
        M.editOriginalEmbeds(E.build()).setComponents(ARs).setReplace(true).queue();
    }
    public void LicenseManageUI(InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M,"license-manager"));
        E.setDescription(TL(M, "license-manager-description"));
        E.setThumbnail(getUser().getEffectiveAvatarUrl());
        E.setColor(getColor());
        E.addField(TL(M,"background"), getCardBackgroundItem().getName(), false);
        E.addField(TL(M,"foreground"), getCardForegroundItem().getName(), false);
        E.addField(TL(M,"ray"), getCardRayItem().getName(), false);
        E.addField(TL(M,"strike"), getCardStrikeItem().getName(), false);
        E.addField(TL(M,"sponsor"), getSponsor() + "", false);
        try (CardImageBuilder CIB = new CardImageBuilder(Profile.get(M.getInteraction().getUser()))) {
            E.setImage(getFileUrl(CIB.GenerateCardPNG().DownloadPNGToFile(), "card.png"));
        }
        M.editOriginalEmbeds(E.build()).queue();
    }

    public Clan getClanAtTime(Instant atTime) {
        for (Profile_PastClan PC : getClanLogs(true)) {
            if (PC.getTimeJoined().isBefore(atTime) && atTime.isBefore(PC.getTimeLeft())) {
                return PC.getClan();
            }
        }
        ClanMember c = ClanMember.MainOfUser(this);
        if (c != null && c.getTimeJoined().isBefore(atTime)) return c.getClan();
        return null;
    }

    public void ArrangeClanLogs() {

    }

    public void IncrementProgress(String Type, int amount) {
        if (amount > 0) for (Profile_Quest PQ : getUnderwayQuests()) {
            PQ.IncrementProgress(Type, amount);
        }
    }
    public void UpdateAchievement() {
        List<Profile_Game> MyPGS = Profile_Game.ofUser(getID());
        UpdateAchievement("TOTAL_MATCH", Integer.parseInt(Totals().getTotalStats().get("Wins").toString()) + Integer.parseInt(Totals().getTotalStats().get("Ties").toString()) + Integer.parseInt(Totals().getTotalStats().get("Loses").toString()));
        UpdateAchievement("TOTAL_WIN_MATCH", Integer.parseInt(Totals().getTotalStats().get("Wins").toString()));
        UpdateAchievement("TOTAL_POLY_5_GAMES", (int) MyPGS.stream().filter(PG -> PG.Wins >= 5).count());
        UpdateAchievement("TOTAL_POLY_20_GAMES", (int) MyPGS.stream().filter(PG -> PG.Wins >= 20).count());
        UpdateAchievement("TOTAL_POLY_50_GAMES", (int) MyPGS.stream().filter(PG -> PG.Wins >= 50).count());
        UpdateAchievement("TOTAL_TOURNAMENT", getItem(1001).Amount + getItem(1002).Amount + getItem(1003).Amount);
        UpdateAchievement("TOTAL_TOP1", getItem(1001).Amount);
    }
    private void UpdateAchievement(String Type, int amount) {
        if (amount > 0) for (Profile_Achievement PA : getUnderwayAchievements()) {
            try {
                PA.setProgress(Type, amount);
            } catch (Exception ignored) {}
        }
    }
    public Profile() {}
    public Profile(long id) {
        this.ID = id;
        Write();
    }

    public static List<Profile> list() {
        return getAll(Profile.class);
    }
    public static Profile get(User user) {
        return get(user.getIdLong());
    }
    public static Profile get(long id) {
        return DatabaseObject.getById(Profile.class, id).orElseGet(() -> new Profile(id));
    }
    public static Profile get(String id) {
        return DatabaseObject.getWhere(Profile.class, "ID = ? OR Name = ?", id, id).orElse(null);
    }

    public static List<Profile> find(String like) {
        if (like.length() < 3) return new ArrayList<>();
        List<Profile> P = getAllWhere(Profile.class, "ID = ? OR Name LIKE ? OR FullName LIKE ? OR Nationality LIKE ? OR History LIKE ?", like, "%" + like + "%", "%" + like + "%", "%" + like + "%", "%" + like + "%");
        if (P.isEmpty()) {
            List<Row> TR = doQueryAll("SELECT ID, FullName, Name FROM inazuma_competitive.profile;");
            for (DatabaseObject.Row row : TR) {
                if (similarity(row.getAsString("Name"), like) > 50 || similarity(row.getAsString("FullName"), like) > 50) {
                    P.add(Profile.get(row.getAsLong("ID")));
                }
            }
        }
        return P;
    }
    public Profile_Game getPG(Game game) {
        Profile_Game P = Profile_Game.get(getID(), game);
        P.pf = this;
        P.g = game;
        return P;
    }
    public Profile_Game_S getPG(Game game, long serverid) {
        Profile_Game_S P = Profile_Game_S.get(getID(), serverid, game);
        P.pf = this;
        P.g = game;
        return P;
    }
    public List<Profile_Game> getPGs() {
        List<Profile_Game> P = Profile_Game.ofUser(getID());
        for (Profile_Game PG : P) PG.pf = this;
        return P;
    }
    public List<Profile_Game_S> getPGs(long serverid) {
        List<Profile_Game_S> P = Profile_Game_S.ofUser(getID(), serverid);
        for (Profile_Game_S PG : P) PG.pf = this;
        return P;
    }

    public List<ClanMember> getClanMembers() {
        return ClanMembers == null ? ClanMembers = ClanMember.OfUser(ID) : ClanMembers;
    }

    public DatabaseObject.Row PowerSQL(String gamecodes) {
        return PowerSQL(null, gamecodes);
    }
    public DatabaseObject.Row PowerSQL(Long serverId, String gamecodes) {
        return doQuery("CALL DisplayUserPower(?,?,?)", getID(), serverId, gamecodes).orElse(null);
    }

    private void listTrophies(EmbedBuilder e) {
        for (Profile_Trophy T : getTrophies()) {
            e.addField(T.getEmoji() + " " + T.getName(), "*" + T.getDescription() + "*", false);
        }
        for (Staff S : STAFFLIST) {
            if (S.UserID == getID()) {
                if (S.Role.equals("Graphic")) e.addField(":art: Graphic Designer", "*" + S.Description + "*", false);
                if (S.Role.equals("Translator")) e.addField(":magic_wand: Translator", "*" + S.Description + "*", false);
                if (S.Role.equals("Clan Manager")) e.addField(":small_blue_diamond: Clan Manager", "*" + S.Description + "*", false);
                if (S.Role.equals("Tournament Manager")) e.addField(":small_orange_diamond: Tournament Manager", "*" + S.Description + "*", false);
            }
        }
        for (DatabaseObject.Row TR : doQueryAll("CALL DisplayEvents(?,?)", getID(), null)) {
            if (TR.getAsInt("Position") > 0 || (TR.getAsString("Type").equals("WC") || TR.getAsString("Position").equals("EU"))) {
                String emoji = TR.getAsInt("Position") == 1 ? ":first_place:" : TR.getAsInt("Position") == 2 ? ":second_place:" : TR.getAsInt("Position") == 3 ? ":third_place:" : ":medal:";
                e.addField(emoji + " " + TR.getAsString("Name") + " Player", "*A player for " + TR.getAsString("Team") + " in the " + TR.getAsString("Name") + ".*", false);
            }
        }
    }
}
