package iecompbot.objects.clan;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.clan.items.ClanPermission;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static iecompbot.L10N.TL;
import static iecompbot.objects.BotManagers.isClanManager;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ClanRole extends DatabaseObject<ClanRole> {

    public long ID;
    public long ClanID;
    public String Name;
    public String Unicode = "U+1F464";
    public int Position = 0;
    public String Permission1 = null;
    public String Permission2 = null;
    public String Permission3 = null;
    public String Permission4 = null;
    public String Permission5 = null;
    protected boolean isBuiltin = false;

    public long getId() {
        return ID;
    }
    public String getName() {
        return Name;
    }
    public int getPosition() {
        return Position;
    }
    public ClanPermission getPermission1() {
        return Permission1 == null ? null : ClanPermission.valueOf(Permission1);
    }
    public ClanPermission getPermission2() {
        return Permission2 == null ? null : ClanPermission.valueOf(Permission2);
    }
    public ClanPermission getPermission3() {
        return Permission3 == null ? null : ClanPermission.valueOf(Permission3);
    }
    public ClanPermission getPermission4() {
        return Permission4 == null ? null : ClanPermission.valueOf(Permission4);
    }
    public ClanPermission getPermission5() {
        return Permission5 == null ? null : ClanPermission.valueOf(Permission5);
    }
    public List<ClanPermission> getPermissions() {
        List<ClanPermission> perms = new ArrayList<>();
        if (getPermission1() != null) perms.add(getPermission1());
        if (getPermission2() != null) perms.add(getPermission2());
        if (getPermission3() != null) perms.add(getPermission3());
        if (getPermission4() != null) perms.add(getPermission4());
        if (getPermission5() != null) perms.add(getPermission5());
        return perms;
    }
    public boolean isBuiltin() {
        return isBuiltin;
    }
    public boolean isBuiltin(InteractionHook M) {
        if (isBuiltin() && M != null) M.editOriginal(TL(M, "clan-role-edit-built-in")).queue();
        return isBuiltin();
    }
    public boolean isBuiltin(IReplyCallback M) {
        if (isBuiltin() && M != null) M.reply(TL(M, "clan-role-edit-built-in")).setEphemeral(true).queue();
        return isBuiltin();
    }

    public void setName(String name) {
        Name = name;
    }
    public void setEmoji(String unicode) {
        Unicode = unicode;
    }
    public SelectOption getSelectOption(ClanMember member) {
        return SelectOption.of(getName(), getId() + "").withEmoji(getEmoji()).withDefault(member.getClanRoles().stream().anyMatch(r -> r.getId() == getId()));
    }

    public boolean canGiveOrRemove(ClanMember CM) {
        try {
            if (isBuiltin() && getName().equals("Clan Captain")) {
                return false;
            }
            if ((CM.isCaptain() && ClanID == CM.getClanID()) || isClanManager(CM.getUserID())) {
                return true;
            }
            if (ClanID != CM.getClanID()) {
                return false;
            }
            if (CM.getPermissions().contains(ClanPermission.ADMINISTRATOR)) {
                return !getPermissions().contains(ClanPermission.ADMINISTRATOR) || CM.getHighestRolePosition() < getPosition();
            }
            return CM.getPermissions().contains(ClanPermission.MANAGE_MEMBER) && !getPermissions().contains(ClanPermission.ADMINISTRATOR) && (!getPermissions().contains(ClanPermission.MANAGE_MEMBER) || CM.getHighestRolePosition() < getPosition());
        } catch (Exception ignored) {
            return false;
        }
    }

    public void setPosition(int position) {
        ClanRole RoleToMoveDown = getWhere(ClanRole.class, "ClanID = ? AND Position ?", ClanID, position).orElse(null);
        if (RoleToMoveDown != null) {
            RoleToMoveDown.setPosition(position+1);
            RoleToMoveDown.UpdateOnly("Position");
        }
        Position = position;
        UpdateOnly("Position");
    }
    public void emptyPermissions() {
        Permission1 = null;
        Permission2 = null;
        Permission3 = null;
        Permission4 = null;
        Permission5 = null;
    }

    public void setPermission(ClanPermission permission, int i) {
        if (i == 1) setPermission1(permission);
        else if (i == 2) setPermission2(permission);
        else if (i == 3) setPermission3(permission);
        else if (i == 4) setPermission4(permission);
        else if (i == 5) setPermission5(permission);
    }

    public void setPermission1(ClanPermission permission1) {
        Permission1 = permission1.toString();
    }
    public void setPermission2(ClanPermission permission2) {
        Permission2 = permission2.toString();
    }
    public void setPermission3(ClanPermission permission3) {
        Permission3 = permission3.toString();
    }
    public void setPermission4(ClanPermission permission4) {
        Permission4 = permission4.toString();
    }
    public void setPermission5(ClanPermission permission5) {
        Permission5 = permission5.toString();
    }
    public void setBuiltin() {
        isBuiltin = true;
        Update();
    }

    public Emoji getEmoji() {
        return Emoji.fromUnicode(Unicode);
    }
    public String getEmojiFormatted() {
        return getEmoji().getFormatted();
    }

    private ClanRole() {}
    public ClanRole(long clanID, String name) {
        ID = Instant.now().toEpochMilli();
        ClanID = clanID;
        Name = name;
        isBuiltin = true;
        Position = Count(ClanRole.class, "ClanID = ?", clanID) + 1;
        Write();
    }
    public ClanRole(long clanID, String name, String unicode) {
        ID = Instant.now().toEpochMilli();
        ClanID = clanID;
        Name = name;
        Unicode = unicode;
        isBuiltin = false;
        Position = Count(ClanRole.class, "ClanID = ?", ClanID) + 1;
        Write();
    }

    public List<ClanMember> getMembers() {
        return doQueryAll(ClanMember.class,"SELECT CM.* FROM inazuma_competitive.clanmember CM " +
                "JOIN inazuma_competitive.clanmember_to_clanrole CC ON CC.ClanMemberID = CM.ID " +
                "JOIN inazuma_competitive.clanrole CR ON CC.ClanRoleID = CR.ID " +
                "WHERE CR.ID = ?", getId());
    }
    public static ClanRole get(long id) {
        return getById(ClanRole.class, id).orElse(null);
    }
    public static List<ClanRole> of(long clanid) {
        return getAllWhere(ClanRole.class, "ClanID = ?", clanid);
    }
    public static List<ClanRole> of(Clan clan) {
        return of(clan.getID());
    }

}
