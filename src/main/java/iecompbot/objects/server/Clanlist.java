package iecompbot.objects.server;

import iecompbot.Constants;
import iecompbot.objects.match.Game;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.util.List;
import java.util.Objects;

import static iecompbot.Constants.POWERDECIMAL;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.objects.BotManagers.isPowerDisabled;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.springboot.data.DatabaseObject.doQueryAll;
import static my.utilities.util.Utilities.getClock;

public class Clanlist extends EmbedBuilder {
    private String clanlist = "";
    private String captains = "";
    private String powerlist = "";

    private String clanlist2 = "";
    private String captains2 = "";
    private String powerlist2 = "";

    private String clanlist3 = "";
    private String captains3 = "";
    private String powerlist3 = "";

    private final List<DatabaseObject.Row> TRs;
    public Clanlist(Game game) {
        TRs = doQueryAll("CALL DisplayClanlist(?)", game != null ? game.getCode() : null);

        for (int i = 0; i < TRs.size(); i++) {
            String flag = Emoji.fromUnicode(TRs.get(i).getAsString("Flag")).getFormatted() + " ";
            String tag = TRs.get(i).getAsString("Tag");
            String name = TRs.get(i).getAsString("Name");
            String username = "@" + getUserByID(TRs.get(i).getAsString("Captain")).getName();
            String power = POWERDECIMAL.format(TRs.get(i).getAsDouble("Power"));
            if (i < 15) {
                clanlist = clanlist + flag + "**" + tag + "** - `" + name + "`\n";
                captains = captains + "`" + username + "`\n";
                powerlist = powerlist + "`" + power + "`\n";
            } else if (i < 30) {
                clanlist2 = clanlist2 + flag + "**" + tag + "** - `" + name + "`\n";
                captains2 = captains2 + "`" + username + "`\n";
                powerlist2 = powerlist2 + "`" + power + "`\n";
            } else if (i < 45) {
                clanlist3 = clanlist3 + flag + "**" + tag + "** - `" + name + "`\n";
                captains3 = captains3 + "`" + username + "`\n";
                powerlist3 = powerlist3 + "`" + power + "`\n";
            }
        }
        addFields();
    }

    public Clanlist(long serverId, Game game) {
        TRs = DatabaseObject.doQueryAll("CALL DisplayClanlist(?)", game != null ? game.getCode() : null);

        for (int i = 0; i < TRs.size(); i++) {
            String flag = Emoji.fromUnicode(TRs.get(i).getAsString("Flag")).getFormatted() + " ";
            String tag = TRs.get(i).getAsString("Tag");
            String name = TRs.get(i).getAsString("Name");
            String username = "@" + getUserByID(TRs.get(i).getAsString("Captain")).getName();
            String power = isPowerDisabled(serverId) ? "[" + TRs.get(i).getAsInt("MemberCount") + "/50]" : POWERDECIMAL.format(TRs.get(i).getAsDouble("Power"));
            if (i < 15) {
                clanlist = clanlist + flag + "**" + tag + "** - `" + name + "`\n";
                captains = captains + "`" + username + "`\n";
                powerlist = powerlist + "`" + power + "`\n";
            } else if (i < 30) {
                clanlist2 = clanlist2 + flag + "**" + tag + "** - `" + name + "`\n";
                captains2 = captains2 + "`" + username + "`\n";
                powerlist2 = powerlist2 + "`" + power + "`\n";
            } else if (i < 45) {
                clanlist3 = clanlist3 + flag + "**" + tag + "** - `" + name + "`\n";
                captains3 = captains3 + "`" + username + "`\n";
                powerlist3 = powerlist3 + "`" + power + "`\n";
            }
        }
        addFields();
    }

    private void addFields() {
        if (!clanlist.isEmpty()) {
            addField(" ", clanlist, true);
            addField(" ", captains, true);
            addField(" ", powerlist, true);
        }
        if (!clanlist2.isEmpty()) {
            addField(" ", clanlist2, true);
            addField(" ", captains2, true);
            addField(" ", powerlist2, true);
        }
        if (!clanlist3.isEmpty()) {
            addField(" ", clanlist3, true);
            addField(" ", captains3, true);
            addField(" ", powerlist3, true);
        }
    }


    public EmbedBuilder getEmbed(ServerInfo I) {
        EmbedBuilder E = new EmbedBuilder(this);
        E.setTitle(TLG(I,"clan-list"));
        E.setDescription(":small_blue_diamond: **" + TLG(I,"why-join-clan") + "**\n" + TLG(I,"clan-list-description") + "\n\n" + ":small_blue_diamond: **" + TLG(I,"clan-available", String.valueOf(TRs.size())) + "**");
        E.setColor(Color.decode(Constants.DiscordColorcode));
        E.clearFields();
        if (!getFields().isEmpty()) {
            E.addField(":european_castle: " + TLG(I,"Clan"), Objects.requireNonNull(getFields().getFirst().getValue()), true);
            E.addField(":star: " + TLG(I,"Clan-Captain"), Objects.requireNonNull(getFields().get(1).getValue()), true);
            E.addField(isPowerDisabled(I) ? TLG(I,"Members") : TLG(I,"Power"), Objects.requireNonNull(getFields().get(2).getValue()), true);
        }
        addAdditionalFields(E);
        E.setFooter(" • " + TLG(I,"updated-on-time", getClock() + " (GMT+2)"), I.getGuild().getIconUrl());
        return E;
    }
    public EmbedBuilder getEmbed(InteractionHook M, ServerInfo I) {
        EmbedBuilder E = new EmbedBuilder(this);
        E.setTitle(TL(M,"clan-list"));
        E.setDescription(":small_blue_diamond: **" + TL(M,"why-join-clan") + "**\n" + TL(M,"clan-list-description") + "\n\n" + ":small_blue_diamond: **" + TL(M,"clan-available", String.valueOf(TRs.size())) + "**");
        E.setColor(Color.decode(Constants.DiscordColorcode));
        E.clearFields();
        if (!getFields().isEmpty()) {
            E.addField(":european_castle: " + TL(M,"Clan"), Objects.requireNonNull(getFields().getFirst().getValue()), true);
            E.addField(":star: " + TL(M,"Clan-Captain"), Objects.requireNonNull(getFields().get(1).getValue()), true);
            E.addField(isPowerDisabled(I) ? TL(M,"Members") : TL(M,"Power"), Objects.requireNonNull(getFields().get(2).getValue()), true);
        }
        addAdditionalFields(E);
        return E;
    }

    private void addAdditionalFields(EmbedBuilder e) {
        if (getFields().size() > 3) {
            e.addField(getFields().get(3));
            e.addField(getFields().get(4));
            e.addField(getFields().get(5));
        }
        if (getFields().size() > 6) {
            e.addField(getFields().get(6));
            e.addField(getFields().get(7));
            e.addField(getFields().get(8));
        }
    }
}