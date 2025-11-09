package iecompbot.objects.clan;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.img.builders.CardImageBuilder;
import iecompbot.interaction.cmdbreakdown.clan.ClanMemberInteractCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.clan.items.ClanPermission;
import iecompbot.objects.profile.Profile;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.Constants.ImageGenerationTimerEpochSecond;
import static iecompbot.Constants.POWERDECIMAL;
import static iecompbot.L10N.TL;
import static iecompbot.Utility.formatRelativeTimeTLG;
import static iecompbot.interaction.Automation.getCardUrl;
import static iecompbot.interaction.Automation.replyException;
import static iecompbot.objects.BotManagers.isClanManager;
import static iecompbot.objects.Retrieval.getUserByID;
import static my.utilities.var.Constants.ProgramZoneId;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ClanMember extends DatabaseObject<ClanMember> {

    public transient int Authority;
    public transient List<ClanPermission> Permissions;

    public transient Profile P;
    public transient Clan parentClan;

    public transient User user = null;
    public transient List<ClanRole> ClanRoles = null;

    public long ID;

    public long ClanID;
    public long UserID;
    public String Number = "00";
    public String Nickname = null;
    public String CardPNGURL = null;
    public String CardGIFURL = null;
    public Long EndOfContractEpochSecond = null;
    public boolean isMainClan = true;
    public boolean hasClanTag = true;


    public long getID() {
        return ID;
    }
    public long getUserID() {
        return UserID;
    }
    public long getClanID() {
        return ClanID;
    }
    public int getHighestRolePosition() {
        return !getClanRoles().isEmpty() ? getClanRoles().getFirst().Position : 99999;
    }
    public String getNumber() {
        return Number;
    }
    public String getNickname() {
        return Nickname;
    }
    public String getCardPNGURL() {
        return CardPNGURL;
    }
    public String getCardGIFURL() {
        return CardGIFURL;
    }
    public Instant getTimeJoined() {
        return Instant.ofEpochMilli(getID());
    }
    public String getTimeJoined(String pattern) {
        return getTimeJoined().atZone(ProgramZoneId).format(DateTimeFormatter.ofPattern(pattern));
    }
    public Instant getEndOfContract() {
        if (EndOfContractEpochSecond == null) return null;
        return Instant.ofEpochMilli(EndOfContractEpochSecond);
    }
    public boolean isMainClan() {
        return isMainClan;
    }
    public List<ClanRole> getClanRoles() {
        return ClanRoles == null ? ClanRoles = doQueryAll(ClanRole.class, "SELECT CR.* FROM inazuma_competitive.clanrole CR " +
                "JOIN inazuma_competitive.clanmember_to_clanrole CC ON CC.ClanRoleID = CR.ID " +
                "JOIN inazuma_competitive.clanmember CM ON CC.ClanMemberID = CM.ID " +
                "WHERE CM.ID = ? " +
                "ORDER BY Position ASC", getID()) : ClanRoles;
    }

    public void setNumber(String number) {
        while (number.contains("00") && number.length() > 2) number = number.replaceFirst("0", "");
        if (number.length() == 1) number = 0 + number;
        Number = number;
        resetCards();
    }
    public void setNickname(String nickname) {
        Nickname = nickname;
    }

    public void updateMainClan(boolean mainClan) {
        isMainClan = mainClan;
        Update();
    }
    public void setCardPNGURL(String cardPNGURL) {
        if (cardPNGURL == null || cardPNGURL.length() > 10) CardPNGURL = cardPNGURL;
    }
    public void setCardGIFURL(String cardGIFURL) {
        if (cardGIFURL == null || cardGIFURL.length() > 10) CardGIFURL = cardGIFURL;
    }
    public void setEndOfContractEpochSecond(long endOfContractEpochSecond) {
        EndOfContractEpochSecond = endOfContractEpochSecond;
    }

    public void addRole(ClanRole CR) {
        doUpdate("INSERT INTO clanmember_to_clanrole (ClanMemberID, ClanRoleID) VALUES (?, ?)", ID, CR.getId());
        getClanRoles().add(CR);
    }
    public void removeRole(ClanRole CR) {
        doUpdate("DELETE FROM clanmember_to_clanrole WHERE ClanMemberID = ? AND ClanRoleID = ?", ID, CR.getId());
        getClanRoles().remove(CR);
    }

    public ClanMember() {}
    public ClanMember(long clanid, long userid) {
        this.ID = 1;
        this.ClanID = clanid;
        this.UserID = userid;
    }
    public ClanMember(long clanid, long userid, String number, String Nickname, List<ClanRole> clanRoles, Long endofcontract) {
        this.ID = Instant.now().toEpochMilli();
        this.ClanID = clanid;
        this.UserID = userid;
        this.Number = number;
        this.Nickname = Nickname;
        this.EndOfContractEpochSecond = endofcontract;
        this.ClanRoles = clanRoles;
        this.isMainClan = Clan.getClanOfUser(userid) == null;
        Write();
        for (ClanRole CR : clanRoles) {
            doUpdate("INSERT INTO clanmember_to_clanrole (ClanMemberID, ClanRoleID) VALUES (?, ?)", ID, CR.getId());
        }
    }

    public boolean isReinforcement() {
        return !isMainClan;
    }

    public String getNationEmoji() {
        return getProfile().getNationality().getFlag().getFormatted() + " ";
    }

    public boolean isContractActive() {
        return EndOfContractEpochSecond != null && Instant.now().isBefore(getEndOfContract());
    }

    public boolean hasPermission(ClanPermission permission) {
        try {
            if (isClanManager(UserID) || getPermissions().contains(ClanPermission.ADMINISTRATOR)) return true;
            return getPermissions().contains(permission) || getPermissions().contains(ClanPermission.ADMINISTRATOR);
        } catch (Exception ignored) {
            return false;
        }
    }
    public boolean hasPermission(InteractionHook M, ClanPermission... permission) {
        try {
            if (isClanManager(UserID) || getPermissions().contains(ClanPermission.ADMINISTRATOR)) return true;
            List<ClanPermission> MissingPerms = new ArrayList<>();
            for (ClanPermission perm : permission) {
                if (!getPermissions().contains(perm)) {
                    MissingPerms.add(perm);
                }
            }
            if (MissingPerms.isEmpty()) return true;
            if (M != null) M.editOriginal(TL(M, "clan-manage-fail-permission", MissingPerms.stream().map(P -> ">- " + P).collect(Collectors.joining(", ")))).queue();
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }
    public boolean hasPermission(IReplyCallback event, ClanPermission... permission) {
        try {
            if (isClanManager(UserID) || getPermissions().contains(ClanPermission.ADMINISTRATOR)) return true;
            List<ClanPermission> MissingPerms = new ArrayList<>();
            for (ClanPermission perm : permission) {
                if (!getPermissions().contains(perm)) {
                    MissingPerms.add(perm);
                }
            }
            if (MissingPerms.isEmpty()) return true;
            if (event != null) event.reply(TL(event, "clan-manage-fail-permission", MissingPerms.stream().map(P -> ">- " + P).collect(Collectors.joining(", ")))).setEphemeral(true).queue();
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean isCaptain() {
        return getClanRoles().stream().anyMatch(C -> C.Name.equals("Clan Captain") && C.isBuiltin());
    }
    public boolean isCaptain(InteractionHook M){
        if (getClanRoles().stream().anyMatch(C -> C.Name.equals("Clan Captain") && C.isBuiltin())) {
            return true;
        } else {
            M.editOriginal(TL(M, "clan-not-captain")).setReplace(true).queue();
            return false;
        }
    }

    public List<ClanPermission> getPermissions() {
        if (this.Permissions == null) {
            this.Permissions = new ArrayList<>();
            for (List<ClanPermission> CRL : getClanRoles().stream().map(ClanRole::getPermissions).toList()) {
                for (ClanPermission CR : CRL) {
                    if (!this.Permissions.contains(CR)) {
                        this.Permissions.add(CR);
                    }
                }
            }
            if (isClanManager(UserID)) this.Permissions.add(ClanPermission.ADMINISTRATOR);
        }
        return this.Permissions;
    }
    public List<ClanPermission> getPermissions(boolean refresh) {
        if (refresh) {
            this.Permissions = new ArrayList<>();
            for (List<ClanPermission> CRL : getClanRoles().stream().map(ClanRole::getPermissions).toList()) {
                for (ClanPermission CR : CRL) {
                    if (!this.Permissions.contains(CR)) {
                        this.Permissions.add(CR);
                    }
                }
            }
        }
        return this.Permissions;
    }

    public String getSince(Guild G) {
        return formatRelativeTimeTLG(G, Duration.between(getTimeJoined(), Instant.now()));
    }

    public String listTasksFirstLast() {
        String role = "• ";
        if (getClanRoles().size() == 1) {
            role = role + getClanRoles().getFirst().getName() + "\n";
            return role;
        }
        for (ClanRole task : getClanRoles()) {
            if (task.equals(getClanRoles().getFirst())) {
                if (task.getName().equals("Clan Captain")) {
                    role = role + "├─ "  + task.getName() + " :ghost:\n";
                } else {
                    role = role + "├─ "  + task.getName() + "\n";
                }
            } else if (task.equals(getClanRoles().get(getClanRoles().size() - 1))) {
                if (task.getName().equals("Clan Captain")) {
                    role = role + "└─ "  + task.getName() + " :ghost:\n";
                } else {
                    role = role + "└─ "  + task.getName() + "\n";
                }
            } else {
                if (task.getName().equals("Clan Captain")) {
                    role = role + "├─ "  + task.getName() + " :ghost:\n";
                } else {
                    role = role + "├─ "  + task.getName() + "\n";
                }
            }
        }
        return role;
    }
    public int getAuthority() {
        return Authority;
    }


    public String get1st() {
        if (!getClanRoles().isEmpty()) {
            if (getClanRoles().getFirst().getName().equals("Clan Captain")) {
                return "• :crown: " + getClanRoles().getFirst().getName();
            } else {
                return "• " + getClanRoles().getFirst().getName();
            }
        } else {
            return "• Member";
        }
    }
    public int count2by2() {
        if (getClanRoles().size() == 1) {
            return 1;
        }
        int times = 0;
        int i = 1;
        for (ClanRole task : getClanRoles()) {
            if (times == 2) {
                i++;
                times = 0;
            }
            times++;
        }
        return i;
    }
    public String listTasksOneLine() {
        String role = "";
        if (getClanRoles().size() == 1) {
            return getClanRoles().getFirst().getName();
        }
        for (ClanRole task : getClanRoles()) {
            if (role.length() < 80) {
                if (task.equals(getClanRoles().get(getClanRoles().size() - 2))) {
                    role = role + task.getName() + " & ";
                } else if (task.equals(getClanRoles().get(getClanRoles().size() - 1))) {
                    role = role + task.getName();
                } else {
                    role = role + task.getName() + ", ";
                }
            }
        }
        return role;
    }

    public String listTasksAndGame() {
        return "> -# └ " + listTasksOneLine();
    }




    public double getPower(Long serverid, String gamecodes) {
        try {
            return isReinforcement() ? 0 : getProfile().getPower(serverid, gamecodes);
        } catch (Exception e) {
            return 0;
        }
    }
    public DatabaseObject.Row getActivity(Long serverid, String gamecodes) {
        try {
            return isReinforcement() ? null : getProfile().getActivity(serverid, gamecodes);
        } catch (Exception e) {
            return null;
        }
    }

    public void regenerateClanCard(InteractionHook M) {
        if (Instant.now().isAfter(Instant.ofEpochSecond(ImageGenerationTimerEpochSecond))) {
            ImageGenerationTimerEpochSecond = Instant.now().plus(15, ChronoUnit.SECONDS).getEpochSecond();
            try {
                if (getProfile().getCharacter().exists() && getClan() != null) {
                    try (CardImageBuilder CIB = new CardImageBuilder(getProfile(), getClan())) {
                        if (M != null) M.editOriginal("**[PNG]** " + TL(M, TL(M,"generating-cards-of", "**" + getClan().getEmojiFormatted() + " " + getClan().getName() + "**"))).queue();
                        CIB.GenerateCardPNG();
                        String CardPNGURL = getCardUrl(CIB.DownloadPNGToFile(), "card.png");
                        setCardPNGURL(CardPNGURL != null && CardPNGURL.contains("?") ? CardPNGURL.split("\\?")[0] : CardPNGURL);
                        if (getProfile().isHasGIF()) {
                            ImageGenerationTimerEpochSecond = Instant.now().plus(15, ChronoUnit.SECONDS).getEpochSecond();
                            if (M != null) M.editOriginal("**[GIF]** " + TL(M, TL(M,"generating-cards-of", "**" + getClan().getEmojiFormatted() + " " + getClan().getName() + "**"))).queue();
                            CIB.GenerateCardGIF(55, 0.5);
                            String CardGIFURL = getCardUrl(CIB.DownloadGIFToFile(), "card.gif");
                            setCardGIFURL(CardGIFURL != null && CardGIFURL.contains("?") ? CardGIFURL.split("\\?")[0] : CardGIFURL);
                        }
                        UpdateOnly("CardPNGURL", "CardGIFURL");
                        ImageGenerationTimerEpochSecond = 0;
                    }
                }
            } catch (Exception e) {
                replyException(M, e);
            }
        }
    }

    public boolean hasClanCard() {
        return CardPNGURL != null && (!getProfile().isHasGIF() || CardGIFURL != null);
    }
    public String getClanCard() {
        return getProfile().isHasGIF() ? CardGIFURL : CardPNGURL;
    }
    public void resetCards() {
        setCardGIFURL(null);
        setCardPNGURL(null);
    }

    public synchronized Clan getClan() {
        return parentClan == null ? parentClan = Clan.get(getClanID()) : parentClan;
    }
    public synchronized User getUser() {
        return user == null ? user = getUserByID(UserID) : user;
    }
    public synchronized Profile getProfile() {
        return P == null ? P = Profile.get(getUserID()) : P;
    }


    public static ClanMember get(long id) {
        return getById(ClanMember.class, id).orElse(null);
    }
    public static List<ClanMember> ofClan(long clanId) {
        return doQueryAll(ClanMember.class,"""
                SELECT CM.* FROM inazuma_competitive.clanmember CM
                JOIN inazuma_competitive.profile P ON P.ID = CM.UserID
                WHERE CM.ClanID = ?;
                """, clanId);
    }

    public static List<ClanMember> OfUser(long userId) {
        return doQueryAll(ClanMember.class, """
                SELECT CM.* FROM inazuma_competitive.clanmember CM
                JOIN inazuma_competitive.profile P ON P.ID = CM.UserID
                WHERE CM.UserID = ?
                ORDER BY CM.isMainClan DESC;
                """, userId);
    }
    public static List<ClanMember> OfUser(Profile pf) {
        List<ClanMember> CM = doQueryAll(ClanMember.class, """
                SELECT CM.* FROM inazuma_competitive.clanmember CM
                JOIN inazuma_competitive.profile P ON P.ID = CM.UserID
                WHERE CM.UserID = ?
                ORDER BY CM.isMainClan DESC;
                """, pf.getID());
        for (ClanMember C : CM) C.P = pf;
        return CM;
    }
    public static ClanMember MainOfUser(long userId) {
        return doQuery(ClanMember.class, """
                SELECT CM.* FROM inazuma_competitive.clanmember CM
                JOIN inazuma_competitive.profile P ON P.ID = CM.UserID
                WHERE CM.UserID = ? AND CM.isMainClan = ?;
                """, userId, true).orElse(null);
    }
    public static ClanMember MainOfUser(Profile pf) {
        return doQuery(ClanMember.class, """
                SELECT CM.* FROM inazuma_competitive.clanmember CM
                JOIN inazuma_competitive.profile P ON P.ID = CM.UserID
                WHERE CM.UserID = ? AND CM.isMainClan = ?;
                """, pf.getID(), true).orElse(null);
    }

    public static ClanMember ofClan(long clanId, long userId) {
        return doQuery(ClanMember.class, """
                SELECT CM.* FROM inazuma_competitive.clanmember CM
                JOIN inazuma_competitive.profile P ON P.ID = CM.UserID
                WHERE CM.ClanID = ? AND (CM.UserID = ? OR CM.ID = ?);
                """, clanId, userId, userId).orElse(null);
    }

    public void ManageMemberUI(InteractionHook M, ClanMemberInteractCommand CMD) {
        if (getCardPNGURL() == null) regenerateClanCard(M);

        List<ActionRow> rows = new ArrayList<>();
        List<Button> btn = new ArrayList<>();
        btn.add(Button.secondary(CMD.Command("clan-member-manage-number"), TL(M, "clan-number")).withDisabled(isCaptain() && !CMD.getMe().hasPermission(ClanPermission.MANAGE_MEMBER)));
        btn.add(Button.secondary(CMD.Command("clan-member-manage-nickname"), TL(M, "nickname")).withDisabled(isCaptain() && !CMD.getMe().hasPermission(ClanPermission.MANAGE_MEMBER)));
        btn.add(Button.danger(CMD.Command("clan-member-manage-remove"), "Kick").withDisabled(isCaptain()));
        btn.add(Button.danger(CMD.Command("clan-member-manage-transfer-ownership"), "Transfer Ownership").withDisabled(!isCaptain() && !(CMD.getMe().isCaptain() || isClanManager(CMD.MyID))));
        rows.add(ActionRow.of(btn));

        String roles = "";
        String permissions = "";
        if (!getPermissions().isEmpty()) {
            for (ClanPermission permission : getPermissions()) {
                permissions = permissions + "• `" + permission + "`\n";
            }
        } else {
            permissions = "• " + TL(M, "None");
        }
        int Max = 5;
        for (ClanRole task : getClanRoles()) {
            if (!task.canGiveOrRemove(CMD.getMe())) {
                roles = roles + ":lock: [" + task.getName() + "]\n";
                Max = Max - 1;
            } else {
                roles = roles + "• " + task.getName() + "\n";
            }
        }
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(Number + " • " + getUser().getEffectiveName() + " ~ " + BotEmoji.get("POW") + " " + POWERDECIMAL.format(getPower(null, null)));
        embed.setAuthor(" • " + TL(M, "clan-member-manager"), null, CMD.getTargetClan().getEmblemURL());
        embed.setFooter(TL(M, "Member_Since") + " " + getTimeJoined("dd MMMM yyyy"));
        embed.setColor(CMD.getTargetClan().getColor());
        embed.setDescription(TL(M, "clan-edit-member", getUser().getEffectiveName()));
        embed.setThumbnail(getUser().getAvatarUrl());
        embed.setImage(getCardPNGURL());
        embed.addField(TL(M, "current-roles"), roles, true);
        embed.addField(TL(M, "current-permissions"), permissions, false);

        if (!isReinforcement()) {
            DatabaseObject.Row TR = getActivity(null, null);
            embed.addField(":chart_with_upwards_trend: " + TL(M, "Member-Activity"),
                    "> **" + TL(M, "Last_Month") + ": " + TR.get("Match Activity") + "**\n" +
                            "> **" + TL(M, "Last_Months_Average", "3") + ": " + POWERDECIMAL.format(TR.getAsDouble("Average Match Activity")) + "**\n",
                    false);
            embed.addField(":chart_with_upwards_trend: " + TL(M, "Tournament-Participation-Rate"),
                    "> **" + TL(M, "Last_Month") + ": " + TR.get("Tournament Activity") + "**\n" +
                            "> **" + TL(M, "Last_Months_Average", "3") + ": " + POWERDECIMAL.format(TR.getAsDouble("Average Tournament Activity")) + "**\n",
                    false);
        }

        if (getEndOfContract() != null && isContractActive()) {
            embed.addField(":scroll: " + TL(M, "Contract"), TL(M, "Ending_on") + ": <t:" + EndOfContractEpochSecond + ":d> (<t:" + EndOfContractEpochSecond + ":R>)", false);
        }

        List<SelectOption> Options = CMD.getTargetClan().getClanRoles().stream().filter(r -> r.canGiveOrRemove(CMD.getMe())).map(r -> r.getSelectOption(this)).collect(Collectors.toList());
        if (!Options.isEmpty()) {
            rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-member-manage-roles"))
                    .setPlaceholder(TL(M, "select-roles", java.lang.String.valueOf(Max)))
                    .addOptions(Options)
                    .setRequiredRange(0, Math.max(Max, 0))
                    .build()).withDisabled(!CMD.getMe().hasPermission(ClanPermission.MANAGE_MEMBER)));
        } else {
            rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-member-manage-roles"))
                    .setPlaceholder(TL(M, "select-roles", java.lang.String.valueOf(Max)))
                    .addOptions(List.of(SelectOption.of("no", "no")))
                    .build().asDisabled()));
        }
        M.editOriginalEmbeds(embed.build()).setComponents(rows).setReplace(true).queue();
    }


    public boolean isHasClanTag() {
        return hasClanTag;
    }
    public void setHasClanTag(boolean hasClanTag) {
        this.hasClanTag = hasClanTag;
    }

    public void setMainClan(boolean mainClan) {
        isMainClan = mainClan;
    }
    public Long getEndOfContractEpochSecond() {
        return EndOfContractEpochSecond;
    }
    public void setEndOfContractEpochSecond(Long endOfContractEpochSecond) {
        EndOfContractEpochSecond = endOfContractEpochSecond;
    }

    public void setUserID(long userID) {
        UserID = userID;
    }
    public void setClanID(long clanID) {
        ClanID = clanID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
}
