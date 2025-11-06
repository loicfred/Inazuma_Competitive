package iecompbot.objects.server.tournament.challonge.global;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.tournament.SubMatch;
import iecompbot.objects.server.tournament.challonge.BaseCMatch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.List;

import static iecompbot.L10N.TL;
import static iecompbot.objects.UserAction.sendPrivateMessage;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class GChallonge_Match extends BaseCMatch<GChallonge_Tournament, GChallonge_Match, GChallonge_Participant> {

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
            }
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
                }
            } else if (T.getVSAmount() == 1) {
                EmbedBuilder E2 = new EmbedBuilder(E);
                E2.setDescription(BotEmoji.get("icon_fan") + TL(getPlayer1().getLeaderPf(), "challonge-next-opponent", getPlayer2().getLeader().getAsMention()+ " **(@" + getPlayer2().getLeader().getName() + ")**"));
                sendPrivateMessage(getPlayer1().getLeader(), new MessageCreateBuilder().setContent(getPlayer1().getLeader().getAsMention()).setEmbeds(E2.build()));

                E2 = new EmbedBuilder(E);
                E2.setDescription(BotEmoji.get("icon_fan") + TL(getPlayer2().getLeaderPf(), "challonge-next-opponent", getPlayer1().getLeader().getAsMention()+ " **(@" + getPlayer1().getLeader().getName() + ")**"));
                sendPrivateMessage(getPlayer2().getLeader(), new MessageCreateBuilder().setContent(getPlayer2().getLeader().getAsMention()).setEmbeds(E2.build()));
            }
        }
    }

    @Override
    protected void afterValidateTeam() {
        if (!getPlayer1().getUnplayedMatches().isEmpty()) {
            GChallonge_Match M1 = getPlayer1().getUnplayedMatches().getFirst();
            if (!T.hasModifierNoReveal()) M1.NotifyNextOpponent();
        }
        if (!getPlayer2().getUnplayedMatches().isEmpty()) {
            GChallonge_Match M1 = getPlayer2().getUnplayedMatches().getFirst();
            M1.NotifyNextOpponent();
        }
    }


    public GChallonge_Match() {}
    public GChallonge_Match(GChallonge_Tournament T, at.stefangeyer.challonge.model.Match M) {
        this.T = T;
        this.M = M;
        this.ID = M.getId();
        this.TournamentID = T.getId();
        Write();
        RefreshMatch(M);
    }


    public static GChallonge_Match get(GChallonge_Tournament T, long matchId) {
        GChallonge_Match P = getWhere(GChallonge_Match.class, "TournamentID = ? AND ID = ?", T.getId(), matchId).orElse(null);
        if (P == null) return null;
        P.T = T;
        return P;
    }
    public static GChallonge_Match getByMatchLog(long matchLog) {
        GChallonge_Match P = getWhere(GChallonge_Match.class, "MatchLogID = ?", matchLog).orElse(null);
        if (P == null) return null;
        P.T = GChallonge_Tournament.get(P.TournamentID);
        return P;
    }
    public static List<GChallonge_Match> get(GChallonge_Tournament T) {
        List<GChallonge_Match> P = getAllWhere(GChallonge_Match.class,"TournamentID = ?", T.getId());
        for (GChallonge_Match CM : P) CM.T = T;
        return P;
    }
    public static List<GChallonge_Match> getByGroup(GChallonge_Tournament T, long GroupID) {
        List<GChallonge_Match> P = getAllWhere(GChallonge_Match.class,"TournamentID = ? AND GroupID = ?", T.getId(), GroupID);
        for (GChallonge_Match CM : P) CM.T = T;
        return P;
    }

    @Override
    public GChallonge_Tournament getTournament() {
        return T == null ? T = GChallonge_Tournament.get(getTournamentID()) : T;
    }
}
