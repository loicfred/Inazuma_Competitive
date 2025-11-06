package iecompbot.ai.info;

import iecompbot.objects.BotEmoji;
import iecompbot.objects.Nationality;
import iecompbot.objects.clan.Clan;
import iecompbot.springboot.data.DatabaseObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static my.utilities.util.Utilities.ShutdownWithTimeout;

public class AI_Clan {

    public DatabaseObject.Row ActivityDetails = null;
    public List<AI_ClanMember> ClanMembers;

    public String ClanName;
    public String Tag;
    public String Status;
    public Nationality Nationality;

    public String WebsiteURL;
    public String TwitterURL;
    public String TwitchURL;
    public String YouTubeURL;
    public String InstagramURL;
    public String DiscordURL;
    public String TiktokURL;

    public String Description;
    public String Requirements;
    public String Background;

    public int MemberCount;
    public String Power;
    public long CreatedAtTimeEpochSecond;

    public AI_Clan(Clan clan, boolean power) {
        try {
            this.CreatedAtTimeEpochSecond = clan.ID;
            this.ClanName = (clan.EmojiID != null ? clan.getEmojiFormatted() + " " : "") + clan.Name;
            this.Tag = clan.Tag;
            this.Nationality = clan.getNationality();
            this.Status = clan.Status;

            this.Description = clan.Description;
            this.Requirements = clan.Requirements;

            this.MemberCount = clan.MemberCount;
            this.Background = clan.History;
            this.WebsiteURL = clan.getWebsiteURL();
            this.TwitterURL = clan.getTwitterURL();
            this.TwitchURL = clan.getTwitchURL();
            this.YouTubeURL = clan.getYouTubeURL();
            this.InstagramURL = clan.getInstagramURL();
            this.DiscordURL = clan.getDiscordURL();
            this.TiktokURL = clan.getTiktokURL();

            ExecutorService E = Executors.newCachedThreadPool();
            E.execute(() -> {
                this.Power = BotEmoji.get("POW").getFormatted() + " " + clan.getPowerAsString();
            });

//            E.execute(() -> {
//                try {
//                    ActivityDetails = DBM.processQuery("CALL DisplayClanActivity(?,?,?,?,?)", clan.ID, null, null, 30, 3).getFirst();
//                } catch (SQLException ignored) {}
//            });
//            E.execute(() -> {
//                try {
//                    ClanMembers = DBM.processQuery("""
//                SELECT CM.Number, P.FullName, COALESCE(GROUP_CONCAT(CR.Name SEPARATOR ', '), "Member") AS 'Roles'  FROM inazuma_competitive.clanmember CM
//                JOIN profile P ON P.ID = CM.UserID
//                LEFT JOIN clanmember_to_clanrole CMCR ON CMCR.ClanMemberID = CM.ID
//                LEFT JOIN clanrole CR ON CR.ID = CMCR.ClanRoleID
//                WHERE CM.ClanID = ?
//                GROUP BY CM.ID
//                ORDER BY COALESCE(MIN(CR.Position), 99999) ASC, CM.Number DESC;""", clan.ID).stream().map(AI_ClanMember::new).collect(Collectors.toList());
//                } catch (SQLException ignored) {}
//            });
            ShutdownWithTimeout(E,0.25, "AI Clan");
        } catch (Exception ignored) {}
    }

    public static class AI_ClanMember {

        public long TimeJoinedInEpochMilli;
        public String Number;
        public String Name;
        public String Roles;

        // Constructor to copy values from ClanMember object
        public AI_ClanMember(DatabaseObject.Row clanMember) {
            this.TimeJoinedInEpochMilli = clanMember.getAsLong("ID");
            this.Number = clanMember.getAsString("Number");
            this.Name = clanMember.getAsString("FullName");
            this.Roles = clanMember.getAsString("Roles");
        }
    }
}
