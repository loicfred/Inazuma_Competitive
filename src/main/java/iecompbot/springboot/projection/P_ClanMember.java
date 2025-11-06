package iecompbot.springboot.projection;

import iecompbot.objects.Nationality;
import iecompbot.objects.clan.ClanMember;
import iecompbot.springboot.data.DatabaseObject;

import static my.utilities.util.Utilities.replaceLast;

public class P_ClanMember extends ClanMember {

    public String Crown = "";
    public String Name;
    public String FullName;
    public String AvatarURL;
    public String Signature;
    public String ColorCode;
    public Nationality Nationality;

    public String Roles;
    public int Level;
    public boolean hasLogo = false;

    // Constructor to copy values from ClanMember object
    public P_ClanMember(DatabaseObject.Row TR) {
        this.ID = TR.getAsLong("ID");
        this.UserID = TR.getAsLong("UserID");
        this.Number = TR.getAsString("Number");
        this.Nickname = TR.getAsString("Nickname");
        this.EndOfContractEpochSecond = TR.getAsLong("EndOfContractEpochSecond");
        this.isMainClan = TR.getAsBoolean("isMainClan");
        this.Name = TR.getAsString("Name");
        this.FullName = TR.getAsString("FullName");
        this.AvatarURL = TR.getAsString("AvatarURL");
        this.Signature = TR.getAsString("Signature");
        this.ColorCode = TR.getAsString("ColorCode");
        this.Nationality = iecompbot.objects.Nationality.get(TR.getAsString("NationalityName"));
        this.Level = TR.getAsInt("Level");
        this.Roles = replaceLast(TR.getAsString("Roles"), ", ", " & ");
        this.Roles = Roles.isEmpty() ? " " : Roles;
    }
    public P_ClanMember(ClanMember TR) {
        this.ID = TR.getId();
        this.Number = TR.getNumber();
        this.Nickname = TR.getNickname();
        this.EndOfContractEpochSecond = TR.EndOfContractEpochSecond;
        this.isMainClan = TR.isMainClan();
        this.Name = TR.getClan().getName();
        this.ClanID = TR.getClan().getId();
        this.hasLogo = TR.getClan().hasEmblem();
    }

}