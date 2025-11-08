package iecompbot.objects.clan.interclan;

import at.stefangeyer.challonge.model.enumeration.TournamentState;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import iecompbot.Constants;
import iecompbot.img.builders.DualPictureBuilder;
import iecompbot.img.builders.InterclanImageBuilder;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.match.Game;
import iecompbot.objects.server.ServerInfo;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static iecompbot.L10N.TLG;
import static iecompbot.img.ImgUtilities.mixColors;
import static iecompbot.interaction.Automation.*;
import static my.utilities.util.Utilities.EpochSecondToPattern;

public class Interclan extends DatabaseObject<Interclan> {
    private transient List<Interclan_Duel> Duels;
    private transient Clan C1;
    private transient Clan C2;

    public long ID;
    public long HosterID;
    public long JoinerID;
    public String DominantColor;
    public String LogoURL;
    public String ImageURL;
    public String State;
    public String MatchingRule;
    public int AmountRule;
    public long CompletedTimeEpochSecond;

    public Interclan() {}
    public Interclan(long ID, Clan hoster, Clan joiner, String matchingRule, int amountRule) {
        this.ID = ID;
        HosterID = hoster.getID();
        JoinerID = joiner.getID();
        State = TournamentState.UNDERWAY.toString();
        MatchingRule = matchingRule;
        AmountRule = amountRule;
        CompletedTimeEpochSecond = 0;
        DominantColor = mixColors(hoster.Colorcode, joiner.Colorcode);
        Write();
    }

    public void setDominantColor(String dominantColor) {
        DominantColor = dominantColor;
    }
    public void setState(TournamentState state) {
        State = state.toString();
    }
    public void setMatchingRule(String matchingRule) {
        MatchingRule = matchingRule;
    }
    public void setLogoURL(String logoURL) {
        LogoURL = logoURL;
    }
    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }


    public long getId() {
        return ID;
    }
    public Color getColor() {
        return Color.decode(DominantColor);
    }
    public TournamentState getState() {
        return TournamentState.valueOf(State.toUpperCase().replaceAll(" ","_"));
    }
    public String getLogoURL() {
        if (LogoURL == null) {
            try (FileUpload F = FileUpload.fromData(getDualPic(), "img/logo.png")) {
                Message M = Constants.ClanEmblemChannel.sendFiles(F).submit().orTimeout(3, TimeUnit.SECONDS).get();
                setLogoURL(M.getAttachments().getFirst().getUrl());
                Update();
            } catch (Exception ignored) {
                return "attachment://logo.png";
            }
        }
        return LogoURL;
    }
    public String getImagePNG() {
        if (ImageURL == null) {
            try (FileUpload F = FileUpload.fromData(new InterclanImageBuilder(this).GeneratePicturePNG().DownloadPNGToFile(), "image.png")) {
                Message M = Constants.ClanEmblemChannel.sendFiles(F).submit().orTimeout(3, TimeUnit.SECONDS).get();
                setImageURL(M.getAttachments().getFirst().getUrl());
                Update();
            } catch (Exception ignored) {
                return "attachment://logo.png";
            }
        }
        return ImageURL;
    }
    public String getMatchingRule() {
        return MatchingRule;
    }

    public Instant getCompletedTime() {
        return Instant.ofEpochSecond(CompletedTimeEpochSecond);
    }
    public String getCompletedTime(String pattern) {
        return EpochSecondToPattern(CompletedTimeEpochSecond, pattern);
    }

    public Clan getOpposingClanOf(Clan clan) {
        if (clan.getID() == getHoster().getID()) {
            return getJoiner();
        } else if (clan.getID() == getJoiner().getID()) {
            return getHoster();
        }
        return null;
    }

    public Clan getWinner() {
        if (getHostTotalScore() > getJoinTotalScore()) {
            return getHoster();
        } else {
            return getJoiner();
        }
    }
    public Clan getLooser() {
        if (getHostTotalScore() < getJoinTotalScore()) {
            return getHoster();
        } else {
            return getJoiner();
        }
    }

    public File getDualPic() {
        try (DualPictureBuilder IMG = new DualPictureBuilder(getHoster().getEmblem(), getJoiner().getEmblem(), Game.get("IEVR"))) {
            return IMG.GeneratePicturePNG().DownloadPNGToFile();
        }
    }


    public boolean EndIfPossible() {
        if (getState().equals(TournamentState.UNDERWAY) && !getDuels().isEmpty()) {
            if (getDuels().stream().allMatch(Interclan_Duel::isFinished)) {
                State = TournamentState.COMPLETE.toString();
                CompletedTimeEpochSecond = Instant.now().getEpochSecond();
                LogInterclanEnd();
                Update();
                return true;
            }
        } return false;
    }


    public List<Interclan_Duel> getFinishedDuels() {
        return getDuels().stream().filter(Interclan_Duel::isFinished).collect(Collectors.toList());
    }
    public List<Interclan_Duel> getUnfinishedDuels() {
        return getDuels().stream().filter(D -> !D.isFinished()).collect(Collectors.toList());
    }
    public Clan getHoster() {
        return C1 == null ? C1 = Clan.get(HosterID) : C1;
    }
    public Clan getJoiner() {
        return C2 == null ? C2 = Clan.get(JoinerID) : C2;
    }

    public List<Interclan_Duel> getDuels() {
        return Duels == null ? Duels = Interclan_Duel.get(this) : Duels;
    }

    public Interclan_Duel getDuel(long id) {
        return getDuels().stream().filter(i -> i.ID == id).findFirst().orElse(null);
    }
    public Interclan_Duel getDuel(long P1ID, long P2ID) {
        return getDuels().stream().filter(i -> (i.P1ID == P1ID && i.P2ID == P2ID) || (i.P1ID == P2ID && i.P2ID == P1ID)).findFirst().orElse(null);
    }

    public String getRuleVSString() {
        if (AmountRule == 0) {
            return getDuels().size() + " vs " + getDuels().size();
        }
        return AmountRule + "v" + AmountRule;
    }
    public int getHostTotalScore() {
        int i = 0;
        for (Interclan_Duel D : getDuels()) {
            if (D.getP1Score() > D.getP2Score()) {
                i++;
            }
        }
        return i;
    }

    public int getJoinTotalScore() {
        int i = 0;
        for (Interclan_Duel D : getDuels()) {
            if (D.getP1Score() < D.getP2Score()) {
                i++;
            }
        }
        return i;
    }
    public void LogInterclanEnd() {
        String logourl = getFileUrl(getDualPic(), "img/logo.png");
        for (ServerInfo I : ServerInfo.list(true)) {
            try {
                if (I.Channels().getClanUpdatesChannel() != null) {
                    if (hasPermissionInChannel(null, I.Channels().getClanUpdatesChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_WEBHOOKS)) {
                        getWebhookOfChannel(I.Channels().getClanUpdatesChannel(), client -> {
                            String description = "# :zap: " + TLG(I, "clan-news-interclan-end") + "\n";
                            description = description + "> " + TLG(I, "clan-news-interclan-end-description", "**" + getHoster().getEmojiFormatted() + getHoster().getName() + "**", "**" + getJoiner().getEmojiFormatted() + getJoiner().getName() + "**") + "\n";
                            description = description + "`                                                       `\n";


                            String P1 = "";
                            String duals = "";
                            String P2 = "";
                            for (Interclan_Duel D : getDuels()) {
                                P1 = P1 + D.getGame().getEmoji() + " " + D.getP1().getEffectiveName() + "\n";
                                duals = duals + "**" + D.getP1Score() + " " + D.getGame().getVSEmoji() + D.getP2Score() + "**\n";
                                P2 = P2 + D.getP2().getEffectiveName() + "\n";
                            }
                            duals = duals + "**" + getHostTotalScore() + " " + Game.get("VR").getVSEmoji() + getJoinTotalScore() + "**";

                            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                            embed.setDescription(description);
                            embed.setThumbnailUrl(logourl);
                            embed.setColor(getColor().getRGB());
                            embed.setFooter(new WebhookEmbed.EmbedFooter(getHoster().getName() + " v/s " + getJoiner().getName(), null));
                            embed.setTimestamp(Instant.now());
                            embed.addField(new WebhookEmbed.EmbedField(true, getHoster().getName(), P1));
                            embed.addField(new WebhookEmbed.EmbedField(true, TLG(I, "Matches"), duals));
                            embed.addField(new WebhookEmbed.EmbedField(true, getJoiner().getName(), P2));

                            WebhookMessageBuilder builde = new WebhookMessageBuilder()
                                    .setUsername(getHoster().getName() + " v/s " + getJoiner().getName())
                                    .setAvatarUrl(logourl)
                                    .addEmbeds(embed.build());
                            client.send(builde.build());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static List<Interclan> get(Clan C) {
        return getAllWhere(Interclan.class, "HosterID = ? OR JoinerID = ?", C.getID(), C.getID());
    }
    public static List<Interclan> listOnGoing() {
        return getAllWhere(Interclan.class, "State = ?", TournamentState.UNDERWAY.toString());
    }
    public static List<Interclan> getOngoing(Clan C) {
        return getAllWhere(Interclan.class, "State = ? AND (HosterID = ? OR JoinerID = ?)", TournamentState.UNDERWAY.toString(), C.getID(), C.getID());
    }
    public static Interclan getOngoing(Clan C1, Clan C2) {
        return getWhere(Interclan.class,"State = ? AND ((HosterID = ? AND JoinerID = ?) OR (JoinerID = ? AND HosterID = ?))", TournamentState.UNDERWAY.toString(), C1.getID(), C2.getID(), C1.getID(), C2.getID()).orElse(null);
    }
    public static Interclan get(long Id) {
        return getById(Interclan.class, Id).orElse(null);
    }

}
