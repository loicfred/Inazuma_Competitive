package iecompbot.objects;

import iecompbot.objects.server.ServerInfo;
import iecompbot.springboot.data.DatabaseObject;
import my.utilities.json.JSONItem;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BotManagers extends JSONItem {

    public static BotManagers BOTSTAFF = null;
    public static List<Staff> STAFFLIST = null;

    public transient List<Long> BotOwners = List.of(508331399149912088L); // Loic
    public List<Long> ClanManagers = new ArrayList<>();
    public List<Long> TournamentManagers = new ArrayList<>();
    public List<Long> Translators = new ArrayList<>();
    public List<Long> GraphicDesigners = new ArrayList<>();
    public List<Long> PowerDisabled = new ArrayList<>();


    public List<Long> ScoreBan = new ArrayList<>();
    public List<Long> ClanCaptainBan = new ArrayList<>();
    public List<Long> BlockList = new ArrayList<>();

    public static boolean isPowerDisabled(long serverid) {
        return BOTSTAFF != null && BOTSTAFF.PowerDisabled.contains(serverid);
    }
    public static boolean isPowerDisabled(Guild G) {
        return BOTSTAFF != null && G != null && BOTSTAFF.PowerDisabled.contains(G.getIdLong());
    }
    public static boolean isPowerDisabled(ServerInfo I) {
        return BOTSTAFF != null && I != null && BOTSTAFF.PowerDisabled.contains(I.getID());
    }
    public static boolean isBlocked(long userid) {return BOTSTAFF != null && BOTSTAFF.BlockList.contains(userid);}
    public static boolean isBlocked(User user) {return user != null && BOTSTAFF != null && BOTSTAFF.BlockList.contains(user.getIdLong());}
    public static boolean isScoreBan(long userid) {return BOTSTAFF != null && BOTSTAFF.ScoreBan.contains(userid);}
    public static boolean isScoreBan(User user) {return user != null && BOTSTAFF != null && BOTSTAFF.ScoreBan.contains(user.getIdLong());}
    public static boolean isClanCaptainBan(String userid) {return BOTSTAFF != null && BOTSTAFF.ClanCaptainBan.contains(Long.parseLong(userid));}
    public static boolean isClanCaptainBan(long userid) {return BOTSTAFF != null && BOTSTAFF.ClanCaptainBan.contains(userid);}
    public static boolean isClanCaptainBan(User user) {return user != null && BOTSTAFF != null && BOTSTAFF.ClanCaptainBan.contains(user.getIdLong());}

    public static boolean isClanManager(String userid) {return BOTSTAFF != null && (isBotOwner(userid) || BOTSTAFF.ClanManagers.contains(Long.parseLong(userid)));}
    public static boolean isClanManager(long userid) {return BOTSTAFF != null && (isBotOwner(userid) || BOTSTAFF.ClanManagers.contains(userid));}
    public static boolean isClanManager(User user) {return user != null && BOTSTAFF != null && (isBotOwner(user) || BOTSTAFF.ClanManagers.contains(user.getIdLong()));}
    public static boolean isTournamentManager(String userid) {return BOTSTAFF != null && (isBotOwner(userid) || BOTSTAFF.TournamentManagers.contains(Long.parseLong(userid)));}
    public static boolean isTournamentManager(long userid) {return BOTSTAFF != null && (isBotOwner(userid) || BOTSTAFF.TournamentManagers.contains(userid));}
    public static boolean isTournamentManager(User user) {return user != null && BOTSTAFF != null && (isBotOwner(user) || BOTSTAFF.TournamentManagers.contains(user.getIdLong()));}

    public static boolean isTranslator(long userid) {return BOTSTAFF != null && BOTSTAFF.Translators.contains(userid);}
    public static boolean isTranslator(User user) {return user != null && BOTSTAFF != null && BOTSTAFF.Translators.contains(user.getIdLong());}
    public static boolean isGraphicDesigner(long userid) {return BOTSTAFF != null && BOTSTAFF.GraphicDesigners.contains(userid);}
    public static boolean isGraphicDesigner(User user) {return user != null && BOTSTAFF != null && BOTSTAFF.GraphicDesigners.contains(user.getIdLong());}

    public static boolean isBotOwner(String userid) {return BOTSTAFF != null && BOTSTAFF.BotOwners.contains(Long.parseLong(userid));}
    public static boolean isBotOwner(long userid) {return BOTSTAFF != null && BOTSTAFF.BotOwners.contains(userid);}
    public static boolean isBotOwner(User user) {return user != null && BOTSTAFF != null && BOTSTAFF.BotOwners.contains(user.getIdLong());}

    public static BotManagers UpdateStaffs() {
        STAFFLIST = Staff.list();
        BOTSTAFF = new BotManagers();
        if (STAFFLIST != null) {
            BOTSTAFF.Translators = STAFFLIST.stream().filter(s -> s.Role.equals("Translator")).map(s -> s.UserID).collect(Collectors.toList());
            BOTSTAFF.GraphicDesigners = STAFFLIST.stream().filter(s -> s.Role.equals("Graphic")).map(s -> s.UserID).collect(Collectors.toList());
            BOTSTAFF.TournamentManagers = STAFFLIST.stream().filter(s -> s.Role.equals("Tournament Manager")).map(s -> s.UserID).collect(Collectors.toList());
            BOTSTAFF.ClanManagers = STAFFLIST.stream().filter(s -> s.Role.equals("Clan Manager")).map(s -> s.UserID).collect(Collectors.toList());
            BOTSTAFF.PowerDisabled = STAFFLIST.stream().filter(s -> s.Role.equals("Power Block")).map(s -> s.UserID).collect(Collectors.toList());
            BOTSTAFF.ScoreBan = STAFFLIST.stream().filter(s -> s.Role.equals("Score Ban")).map(s -> s.UserID).collect(Collectors.toList());
            BOTSTAFF.ClanCaptainBan = STAFFLIST.stream().filter(s -> s.Role.equals("Captain Ban")).map(s -> s.UserID).collect(Collectors.toList());
            BOTSTAFF.BlockList = STAFFLIST.stream().filter(s -> s.Role.equals("Block")).map(s -> s.UserID).collect(Collectors.toList());
        }
        return BOTSTAFF;
    }

    public static class Staff {
        public long ID, UserID;
        public String Role, Description;
        public static List<Staff> list() {
            return DatabaseObject.getAll(Staff.class);
        }
    }
}
