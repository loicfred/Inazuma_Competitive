package iecompbot.objects.server.tournament.challonge.server;

import at.stefangeyer.challonge.model.query.MatchQuery;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.img.builders.DualPictureBuilder;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.ChannelMessage;
import iecompbot.objects.server.tournament.SubMatch;
import iecompbot.objects.server.tournament.challonge.BaseCMatch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.time.Instant;
import java.util.List;

import static iecompbot.Constants.ChallongeLogoURL;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.ChallongeAccount;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.interaction.Automation.getFileUrl;
import static iecompbot.interaction.Automation.getWebhookOfChannel;
import static iecompbot.interaction.listeners.MatchFeatures.MakePrediction;
import static iecompbot.objects.UserAction.sendPrivateMessage;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class SChallonge_Match extends BaseCMatch<SChallonge_Tournament, SChallonge_Match, SChallonge_Participant> {
    private transient ChannelMessage PredictionChannel = null;

    public Long PredictionChannelID = null;
    public Long PredictionMessageID = null;

    public ChannelMessage getPrediction(long serverid) {
        return PredictionChannel == null ? PredictionChannel = new ChannelMessage(serverid, PredictionChannelID, PredictionMessageID) : PredictionChannel;
    }
    public void setPredictionMessage(Message m) {
        this.PredictionChannelID = m.getChannelIdLong();
        this.PredictionMessageID = m.getIdLong();
    }
    public void setVotesFromMessage() {
        try {
            if (getPrediction(T.ServerID) != null) {
                Message m = getPrediction(T.ServerID).getMessage();
                if (m != null && m.getReactions().size() == 2) {
                    M = ChallongeAccount.updateMatch(getMatch(), MatchQuery.builder().votesForPlayer1(P1Votes).votesForPlayer2(P2Votes).build());
                    P1Votes = m.getReactions().getFirst().getCount() - 1;
                    P2Votes = m.getReactions().get(1).getCount() - 1;
                    UpdateOnly("P1Votes", "P2Votes");
                }
            }
        } catch (Exception ignored) {}
    }

    public WebhookMessageBuilder getDQMessage(Guild guild, String reason) {
        User U1 = getPlayer1().getLeader();
        User U2 = getPlayer2().getLeader();
        String dualPic;
        try (DualPictureBuilder IMG = new DualPictureBuilder(U1, U2, T.getGame())){
            dualPic = U1 != null && U2 != null ? getFileUrl(IMG.GeneratePicturePNG().DownloadPNGToFile(), "pfp.png") : DiscordAccount.getSelfUser().getEffectiveAvatarUrl();
        }
        String message = ":white_check_mark: " + TLG(guild, "tournament-match-validated", "**" + getPlayer1().getName() + " " + T.getGame().getVSEmojiFormatted() + " " + getPlayer2().getName() + "**"
                , "**" + (getWinnerId().equals(getParticipantID2()) ? "DQ" : P1Score) + " - " + (getWinnerId().equals(getParticipantID1()) ? "DQ" : P2Score) + "**") + "\n";
        message = message + "> **Reason:** " + reason;
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setAuthor(new WebhookEmbed.EmbedAuthor(T.getName(), ChallongeLogoURL, T.getFullChallongeURL()));
        embed.setColor(T.getGame().getColor().getRGB());
        embed.setFooter(new WebhookEmbed.EmbedFooter("• " + guild.getName(), null));
        embed.setTimestamp(Instant.now());
        embed.setDescription(message + "\n`                                                        `");
        return new WebhookMessageBuilder()
                .setUsername(TLG(guild,"Match-Result"))
                .setAvatarUrl(dualPic)
                .addEmbeds(embed.build());
    }
    public WebhookMessageBuilder getTeamWinMessage(Guild guild) {
        User U1 = getPlayer1().getLeader();
        User U2 = getPlayer2().getLeader();
        String dualPic;
        try (DualPictureBuilder IMG = new DualPictureBuilder(U1, U2, T.getGame())) {
            dualPic = U1 != null && U2 != null ? getFileUrl(IMG.GeneratePicturePNG().DownloadPNGToFile(), "pfp.png") : DiscordAccount.getSelfUser().getEffectiveAvatarUrl();
        }
        String m1 = "";
        String score = "";
        String m2 = "";
        for (SubMatch CSM : getSubMatches()) {
            if (CSM == getSubMatches().getLast()) {
                score = score + "__" + CSM.getP1Score() + " : " + CSM.getP2Score() + "__\n";
            } else {
                score = score + CSM.getP1Score() + " : " + CSM.getP2Score() + "\n";
            }
            m1 = m1 + CSM.getP1().getAsMention() + "\n";
            m2 = m2 + CSM.getP2().getAsMention() + "\n";
        }
        m1 = m1 + "**" + TLG(guild,"Total") + ":**";
        score = score + "**" + getTeam1GoalsSum() + " : " + getTeam2GoalsSum() + "**";
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setAuthor(new WebhookEmbed.EmbedAuthor(T.getName(), ChallongeLogoURL, T.getFullChallongeURL()));
        embed.setColor(T.getGame().getColor().getRGB());
        embed.setFooter(new WebhookEmbed.EmbedFooter("• " + guild.getName(), null));
        embed.setTimestamp(Instant.now());
        embed.setDescription(":white_check_mark: " + TLG(guild, "tournament-match-2-validated"));
        embed.addField(new WebhookEmbed.EmbedField(true, getPlayer1().getName(), m1));
        embed.addField(new WebhookEmbed.EmbedField(true, T.getGame().getVSEmojiFormatted(), score));
        embed.addField(new WebhookEmbed.EmbedField(true, getPlayer2().getName(), m2));
        return new WebhookMessageBuilder()
                .setUsername(TLG(guild,"Match-Result"))
                .setAvatarUrl(dualPic)
                .addEmbeds(embed.build());
    }


    public void NotifyStart() {
        EmbedBuilder E = T.getTournamentEmbed();
        if (getPlayer1() != null && getPlayer2() != null) {
            if (T.getVSAmount() > 1) {
                for (SubMatch CSM : getSubMatches()) {
                    EmbedBuilder E2 = new EmbedBuilder(E);
                    E2.setDescription(BotEmoji.get("icon_fan") + TL(Profile.get(CSM.getP1()), "challonge-manage-deadline-start",
                            CSM.getP2().getAsMention() + " **(@" + CSM.getP2().getName() + ")**"));
                    sendPrivateMessage(CSM.getP1(), new MessageCreateBuilder().setContent(CSM.getP1().getAsMention()).setEmbeds(E2.build()));

                    E2 = new EmbedBuilder(E);
                    E2.setDescription(BotEmoji.get("icon_fan") + TL(Profile.get(CSM.getP2()), "challonge-manage-deadline-start",
                            CSM.getP1().getAsMention() + " **(@" + CSM.getP1().getName() + ")**"));
                    sendPrivateMessage(CSM.getP2(), new MessageCreateBuilder().setContent(CSM.getP2().getAsMention()).setEmbeds(E2.build()));
                    setPredictionMessage(MakePrediction(T.I.getGuild()
                            , CSM.getP1()
                            , CSM.getP2()
                            , T.getPredictionChannel()));
                }
            } else if (T.getVSAmount() == 1) {
                EmbedBuilder E2 = new EmbedBuilder(E);
                E2.setDescription(BotEmoji.get("icon_fan") + TL(getPlayer1().getLeaderPf(), "challonge-manage-deadline-start",
                        getPlayer2().getLeader().getAsMention() + " **(@" + getPlayer2().getLeader().getName() + ")**"));
                sendPrivateMessage(getPlayer1().getLeader(), new MessageCreateBuilder().setContent(getPlayer1().getLeader().getAsMention()).setEmbeds(E2.build()));

                E2 = new EmbedBuilder(E);
                E2.setDescription(BotEmoji.get("icon_fan") + TL(getPlayer2().getLeaderPf(), "challonge-manage-deadline-start",
                        getPlayer1().getLeader().getAsMention() + " **(@" + getPlayer1().getLeader().getName() + ")**"));
                sendPrivateMessage(getPlayer2().getLeader(), new MessageCreateBuilder().setContent(getPlayer2().getLeader().getAsMention()).setEmbeds(E2.build()));
                setPredictionMessage(MakePrediction(T.I.getGuild()
                        , getPlayer1().getLeader()
                        , getPlayer2().getLeader()
                        , T.getPredictionChannel()));
            }
            UpdateOnly("PredictionChannelID", "PredictionMessageID");
        }
    }
    public void NotifyNextOpponent() {
        EmbedBuilder E = T.getTournamentEmbed();
        if (getPlayer1() != null && getPlayer2() != null) {
            if (T.getVSAmount() > 1) {
                for (SubMatch CSM : getSubMatches()) {
                    EmbedBuilder E2 = new EmbedBuilder(E);
                    E2.setDescription(BotEmoji.get("icon_fan") + TL(Profile.get(CSM.getP1()), "challonge-next-opponent", CSM.getP2().getAsMention()+ " **(@" + CSM.getP2().getName() + ")**"));
                    sendPrivateMessage(CSM.getP1(), new MessageCreateBuilder().setContent(CSM.getP1().getAsMention()).setEmbeds(E2.build()));

                    E2 = new EmbedBuilder(E);
                    E2.setDescription(BotEmoji.get("icon_fan") + TL(Profile.get(CSM.getP2()), "challonge-next-opponent", CSM.getP1().getAsMention()+ " **(@" + CSM.getP1().getName() + ")**"));
                    sendPrivateMessage(CSM.getP2(), new MessageCreateBuilder().setContent(CSM.getP2().getAsMention()).setEmbeds(E2.build()));
                    setPredictionMessage(MakePrediction(T.I.getGuild()
                            , CSM.getP1()
                            , CSM.getP2()
                            , T.getPredictionChannel()));
                }
            } else if (T.getVSAmount() == 1) {
                EmbedBuilder E2 = new EmbedBuilder(E);
                E2.setDescription(BotEmoji.get("icon_fan") + TL(getPlayer1().getLeaderPf(), "challonge-next-opponent", getPlayer2().getLeader().getAsMention()+ " **(@" + getPlayer2().getLeader().getName() + ")**"));
                sendPrivateMessage(getPlayer1().getLeader(), new MessageCreateBuilder().setContent(getPlayer1().getLeader().getAsMention()).setEmbeds(E2.build()));

                E2 = new EmbedBuilder(E);
                E2.setDescription(BotEmoji.get("icon_fan") + TL(getPlayer2().getLeaderPf(), "challonge-next-opponent", getPlayer1().getLeader().getAsMention()+ " **(@" + getPlayer1().getLeader().getName() + ")**"));
                sendPrivateMessage(getPlayer2().getLeader(), new MessageCreateBuilder().setContent(getPlayer2().getLeader().getAsMention()).setEmbeds(E2.build()));
                setPredictionMessage(MakePrediction(T.I.getGuild()
                        , getPlayer1().getLeader()
                        , getPlayer2().getLeader()
                        , T.getPredictionChannel()));
            }
            UpdateOnly("PredictionChannelID", "PredictionMessageID");
        }
    }

    @Override
    protected void afterValidateTeam() {
        getWebhookOfChannel(T.getMatchResultChannel(), CC -> {
            CC.send(getTeamWinMessage(T.I.getGuild()).build());
        });
        for (SChallonge_Match M1 : getPlayer1().getUnplayedMatches()) M1.NotifyNextOpponent();
        for (SChallonge_Match M2 : getPlayer2().getUnplayedMatches()) M2.NotifyNextOpponent();
    }


    public SChallonge_Match() {}
    public SChallonge_Match(SChallonge_Tournament T, at.stefangeyer.challonge.model.Match M) {
        this.T = T;
        this.M = M;
        this.ID = M.getId();
        this.TournamentID = T.getId();
        Write();
        RefreshMatch(M);
    }


    public static SChallonge_Match get(SChallonge_Tournament T, long matchId) {
        SChallonge_Match P = getWhere(SChallonge_Match.class, "TournamentID = ? AND ID = ?", T.getId(), matchId).orElse(null);
        if (P == null) return null;
        P.T = T;
        return P;
    }
    public static SChallonge_Match getByMatchLog(long matchLog) {
        SChallonge_Match P = getWhere(SChallonge_Match.class, "MatchLogID = ?", matchLog).orElse(null);
        if (P == null) return null;
        P.T = SChallonge_Tournament.get(P.TournamentID);
        return P;
    }
    public static List<SChallonge_Match> get(SChallonge_Tournament T) {
        List<SChallonge_Match> P = getAllWhere(SChallonge_Match.class,"TournamentID = ?", T.getId());
        for (SChallonge_Match CM : P) CM.T = T;
        return P;
    }
    public static List<SChallonge_Match> getByGroup(SChallonge_Tournament T, long GroupID) {
        List<SChallonge_Match> P = getAllWhere(SChallonge_Match.class,"TournamentID = ? AND GroupID = ?", T.getId(), GroupID);
        for (SChallonge_Match CM : P) CM.T = T;
        return P;
    }

    @Override
    public SChallonge_Tournament getTournament() {
        return T == null ? T = SChallonge_Tournament.get(getTournamentID()) : T;
    }
}
