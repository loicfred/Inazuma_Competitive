package iecompbot.ai.info;

import iecompbot.objects.BotEmoji;
import iecompbot.objects.BotManagers;
import iecompbot.objects.match.Game;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.clan.Clan_Trophy;
import iecompbot.objects.event.Event;
import iecompbot.objects.event.Event_Organiser;
import iecompbot.objects.event.Event_Team;
import iecompbot.objects.match.MatchLog;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.Profile_PastClan;
import iecompbot.objects.profile.Profile_Total;
import iecompbot.objects.profile.Profile_Trophy;
import iecompbot.objects.profile.profile_game.BasePG;
import iecompbot.objects.profile.profile_game.Profile_Game;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Participant;
import iecompbot.springboot.data.DatabaseObject;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static iecompbot.Constants.POWERDECIMAL;
import static iecompbot.objects.BotManagers.STAFFLIST;
import static my.utilities.util.Utilities.ShutdownWithTimeout;

public class AI_Profile {

    public DatabaseObject.Row ActivityDetails = null;
    public AI_ProfileTotal TotalStats = null;
    public List<AI_ProfileGame> GamesStats = null;
    public List<AI_ProfileTournament> MyTournaments = null;
    public List<AI_ClanMember> ClansYouAreInWithYourInfos;
    public List<AI_EventOrganiser> EventsWhichUserOrganised;
    public List<AI_EventPlayer> EventsWhereUserParticipated;
    public List<AI_Match> RecentMatches;
    public List<AI_PastClans> PreviousClans;


    public String RoleOrContributionForTheBot = "";

    public String Nationality;
    public String FullName;
    public String Signature;
    public Long BirthdayEpochSecond;
    public String FriendCodeOfStrikers2013Game;
    public String FriendCodeOfStrikersXtremeGame;
    public String FriendCodeOfHisNintendoSwitch;

    public String Background;
    public String WebsiteURL = null;
    public String TwitterURL = null;
    public String TwitchURL = null;
    public String YouTubeURL = null;
    public String InstagramURL = null;
    public String DiscordURL = null;
    public String TiktokURL = null;

    public String Power = "0";

    public int FirstPlaceMedals;
    public int SecondPlaceMedals;
    public int ThirdPlaceMedals;

    private transient ExecutorService E = Executors.newFixedThreadPool(50);

    public AI_Profile(Profile profile) {
        try {
            this.FullName = profile.FullName == null ? "" : profile.FullName;
            this.Nationality = profile.getNationality().getName();
            this.Signature = profile.Signature;
            this.BirthdayEpochSecond = profile.BirthdayEpochSecond;
            this.FriendCodeOfStrikers2013Game = profile.StrikersFriendcode;
            this.FriendCodeOfStrikersXtremeGame = profile.XtremeFriendcode;
            this.FriendCodeOfHisNintendoSwitch = profile.SwitchFriendcode;
            this.Background = profile.History;

            this.WebsiteURL = profile.WebsiteURL;
            this.TwitterURL = profile.TwitterURL;
            this.TwitchURL = profile.TwitchURL;
            this.YouTubeURL = profile.YouTubeURL;
            this.DiscordURL = profile.DiscordURL;
            this.InstagramURL = profile.InstagramURL;
            this.TiktokURL = profile.TiktokURL;

//            E.submit(() -> {
//                FirstPlaceMedals = (int) DBM.countRows("Challonge_Participant", "Position = ? AND (DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ?)", 1, profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId());
//            });
//            E.submit(() -> {
//                SecondPlaceMedals = (int) DBM.countRows("Challonge_Participant", "Position = ? AND (DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ?)", 2, profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId());
//            });
//            E.submit(() -> {
//                ThirdPlaceMedals = (int) DBM.countRows("Challonge_Participant", "Position = ? AND (DiscordID = ? OR DiscordID2 = ? OR DiscordID3 = ? OR DiscordID4 = ? OR DiscordID5 = ? OR DiscordID6 = ? OR DiscordID7 = ? OR DiscordID8 = ?)", 3, profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId(), profile.getId());
//            });
//            E.submit(() -> {
//                try {
//                    ActivityDetails = DBM.processQuery("CALL DisplayUserActivity(?,?,?,?,?)", profile.ID, null, null, 30, 3).getFirst();
//                } catch (SQLException ignored) {}
//            });
//            E.submit(() -> {
//                try {
//                    MyTournaments = DBM.processQuery("CALL DisplayUserTournaments(?,?,?,?,?,?);", profile.ID, null, null, 1, 999, true).stream().map((DatabaseService.Row TR) -> new AI_ProfileTournament(E, TR)).collect(Collectors.toList());
//                } catch (SQLException ignored) {}
//            });

            E.submit(() -> {
                double power = profile.getPower(null, null);
                this.Power = BotEmoji.get("POW").getFormatted() + " " + POWERDECIMAL.format(power);
                this.Power = this.Power + (power < 1 ? " (Beginner)" : power < 5 ? " (Improving player)" : power < 20 ? " (Great player)" :  " (Very powerful player)");
            });

            E.submit(() -> {
                try {
                    GamesStats = Profile_Game.ofUser(profile.ID).stream().filter(PG -> PG.getMatchesCount() > 0).map(AI_ProfileGame::new).sorted(Comparator.comparingInt((AI_ProfileGame PG) -> PG.Medals).reversed()).collect(Collectors.toList());
                } catch (Exception ignored) {}
            });
            E.submit(() -> {
                try {
                    TotalStats = new AI_ProfileTotal(profile.Totals());
                } catch (Exception ignored) {}
            });
            E.submit(() -> {
                ClansYouAreInWithYourInfos = ClanMember.OfUser(profile.ID).stream().map(AI_ClanMember::new).toList();
            });
//            E.submit(() -> {
//                try {
//                    EventsWhichUserOrganised = Event_Organiser.ofUser(profile.ID).stream().map(AI_EventOrganiser::new).toList();
//                } catch (Exception ignored) {}
//            });
            E.submit(() -> {
                try {
                    EventsWhereUserParticipated = Event_Team.ofUser(profile.ID).stream().map(AI_EventPlayer::new).toList();
                } catch (Exception ignored) {}
            });
            E.submit(() -> {
                PreviousClans = profile.getClanLogs(true).stream().map(AI_PastClans::new).toList();
            });
            E.submit(() -> {
                RecentMatches = MatchLog.getMatchesOf(profile.ID, null, null, 1, 50).stream().map((MatchLog M) -> new AI_Match(E, M, profile)).toList();
            });
            E.submit(() -> {
                for (BotManagers.Staff S : STAFFLIST) {
                    if (S.UserID == profile.getID()) {
                        RoleOrContributionForTheBot = RoleOrContributionForTheBot + S.Role + " -> " + S.Description + "\n";
                    }
                }
            });
            ShutdownWithTimeout(E,1, "AI Profile");
            if (RoleOrContributionForTheBot.length() < 5) RoleOrContributionForTheBot = null;
            if (EventsWhichUserOrganised.isEmpty()) EventsWhichUserOrganised = null;
            if (EventsWhereUserParticipated.isEmpty()) EventsWhereUserParticipated = null;
            if (MyTournaments.isEmpty()) MyTournaments = null;
            if (ClansYouAreInWithYourInfos.isEmpty()) ClansYouAreInWithYourInfos = null;
            if (RecentMatches.isEmpty()) RecentMatches = null;
            if (PreviousClans.isEmpty()) PreviousClans = null;
            E = null;
        } catch (Exception ignored) {}
    }

    public static class AI_ProfileTotal {
        public String Wins;
        public String Ties;
        public String Loses;
        public String GoalsScored;
        public String GoalsTaken;
        public String Medals;
        public int Level;
        public int TotalTournaments;
        public String LeagueName;

        public AI_ProfileTotal(Profile_Total profile_total) {
            Wins = profile_total.getTotalStats().get("Wins").toString();
            Ties = profile_total.getTotalStats().get("Ties").toString();
            Loses = profile_total.getTotalStats().get("Loses").toString();
            GoalsScored = profile_total.getTotalStats().get("GoalsScored").toString();
            GoalsTaken = profile_total.getTotalStats().get("GoalsTaken").toString();
            Medals = profile_total.getTotalStats().get("Medals").toString();
            Level = profile_total.Level;
            //TotalTournaments = (int) DBM.countRows(Challonge_Participant.class, "DiscordID = ?", profile_total.UserID);
            LeagueName = profile_total.getLeague().getEmojiFormatted() + " " + profile_total.getLeague().getName();
        }
    }
    public static class AI_ProfileTournament {
        public String Name;
        public String GameEmoji;
        public String GameName;
        public String FullChallongeURL;
        public int Wins;
        public int Ties;
        public int Loses;
        public int GoalsScored;
        public int GoalsTaken;
        public int Position;
        public int ParticipantCount;

        public List<AI_Tournament.AI_Participant> OtherParticipants;

        public AI_ProfileTournament(ExecutorService E, DatabaseObject.Row TR) {
            E.execute(() -> {
                this.Name = TR.getAsString("Name");
                this.GameName = Game.get(TR.getAsString("GameCode")).getFullName();
                this.GameEmoji = Game.get(TR.getAsString("GameCode")).getEmojiFormatted();
                this.FullChallongeURL = TR.getAsString("FullChallongeURL");
                this.Wins = TR.getAsInt("Wins");
                this.Ties = TR.getAsInt("Ties");
                this.Loses = TR.getAsInt("Loses");
                this.GoalsScored = TR.getAsInt("GoalsScored");
                this.GoalsTaken = TR.getAsInt("GoalsTaken");
                this.Position = TR.getAsInt("Position");
                this.ParticipantCount = TR.getAsInt("ParticipantCount");
                this.OtherParticipants = SChallonge_Participant.ofTournament(TR.getAsLong("ID")).stream().filter(P -> !P.getName().equals(this.Name)).map(AI_Tournament.AI_Participant::new).collect(Collectors.toList());
            });
       }
    }

    public static class AI_Trophy {
        public long ID;
        public String Emoji;
        public String Name;
        public String Description;
        public String GameCode;
        public double Power;

        public AI_Trophy(Profile_Trophy profile_Trophy) {
            ID = profile_Trophy.ID;
            Emoji = profile_Trophy.Emoji;
            Name = profile_Trophy.Name;
            Description = profile_Trophy.Description;
            GameCode = profile_Trophy.GameCode;
            Power = profile_Trophy.Power;
        }
        public AI_Trophy(Clan_Trophy profile_Trophy) {
            ID = profile_Trophy.ID;
            Emoji = profile_Trophy.Emoji;
            Name = profile_Trophy.Name;
            Description = profile_Trophy.Description;
            GameCode = profile_Trophy.GameCode;
            Power = profile_Trophy.Power;
        }
    }
    
    public static class AI_ProfileGame {

        public String GameName;
        public int Wins;
        public int Ties;
        public int Loses;
        public int GoalsScored;
        public int GoalsTaken;
        public int Medals;
        public int WinStreak;
        public int LastStreak;
        public int HighestWinStreak;

        public AI_ProfileGame(BasePG<?> profileGame) {
            this.GameName = profileGame.getGame().getEmojiFormatted() + " " + profileGame.getGame().getName();
            this.Wins = profileGame.Wins;
            this.Ties = profileGame.Ties;
            this.Loses = profileGame.Loses;
            this.GoalsScored = profileGame.GoalsScored;
            this.GoalsTaken = profileGame.GoalsTaken;
            this.Medals = profileGame.Medals;
            this.WinStreak = profileGame.WinStreak;
            this.LastStreak = profileGame.LastStreak;
            this.HighestWinStreak = profileGame.HighestWinStreak;
        }
    }
    
    public static class AI_EventOrganiser {
        public String EventName;
        public String EventRole;

        public AI_EventOrganiser(Event_Organiser event) {
            EventName = Event.getEvent(event.getEventID()).getName();
            EventRole = event.getRole();
        }
    }

    public static class AI_EventPlayer {
        public String EventName;
        public String EventTeamName;
        public int EventTeamPosition;

        public AI_EventPlayer(Event_Team event) {
            EventName = Event.getEvent(event.getEventID()).getName();
            EventTeamName = event.getName();
            EventTeamPosition = event.getPosition();
        }
    }

    public static class AI_ClanMember {

        public long TimeJoinedInEpochMilli;
        public String Number;
        public String ClanName;
        public String Roles;

        // Constructor to copy values from ClanMember object
        public AI_ClanMember(ClanMember clanMember) {
            this.TimeJoinedInEpochMilli = clanMember.getID();
            this.Number = clanMember.getNumber();
            this.ClanName = (clanMember.getClan().EmojiID != null ? clanMember.getClan().getEmojiFormatted() + " " : "") + clanMember.getClan().getName();
            this.Roles = clanMember.getClanRoles().stream().map(cm -> cm.Name).collect(Collectors.joining(", "));
        }
    }

    public static class AI_Match {
        public long TimePlayedEpochMillis;
        public String P1Name;
        public String P2Name;
        public int P1Score;
        public int P2Score;
        public String GameName;
        public AI_Match(ExecutorService E, MatchLog m, Profile me) {
            E.execute(() -> {
                try {
                    this.TimePlayedEpochMillis = m.getId();
                    this.P1Score = m.getP1Score();
                    this.P2Score = m.getP2Score();
                    this.P1Name = me.getID() == m.getP1ID() ? me.getUser().getEffectiveName() : m.getP1().getEffectiveName();
                    this.P2Name = me.getID() == m.getP2ID() ? me.getUser().getEffectiveName() : m.getP2().getEffectiveName();
                    this.GameName = m.getGame().getEmojiFormatted() + " " + m.getGame().getName();
                } catch (Exception e) {}
            });
        }
    }
    
    public static class AI_PastClans {
        public long TimeJoinedEpochSecond;
        public long TimeLeftEpochSecond;
        public String PastClanName;
        
        public AI_PastClans(Profile_PastClan pastClan) {
            TimeJoinedEpochSecond = pastClan.getTimeJoinedEpochSecond();
            TimeLeftEpochSecond = pastClan.getId();
            PastClanName = (pastClan.getClan().EmojiID != null ? pastClan.getClan().getEmojiFormatted() + " " : "") + pastClan.getClan().getName();
        }
        
    }
    
}
