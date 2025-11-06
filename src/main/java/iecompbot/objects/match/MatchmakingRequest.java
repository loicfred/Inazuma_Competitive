package iecompbot.objects.match;

import iecompbot.objects.profile.Profile;
import my.utilities.json.JSONItem;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.Main.MainDirectory;
import static iecompbot.objects.Retrieval.getMessage;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static my.utilities.util.Utilities.GenerateRandomNumber;

public class MatchmakingRequest extends JSONItem {
    public Game Game;
    public boolean isAnonymous;
    public long ID;
    public long RequesterID;
    public String Rule;
    public List<RequestData> MatchmakingRequests = new ArrayList<>();

    private transient User U = null;
    private transient Profile P = null;
    public MatchmakingRequest(long ID, long requesterID, boolean isAnonymous, String rule, Game game) {
        this.ID = ID;
        this.RequesterID = requesterID;
        this.isAnonymous = isAnonymous;
        this.Rule = rule;
        this.Game = game;
    }
    public synchronized User getUser() throws Exception {
        if (U == null) U = getPf().getUser();
        return U;
    }
    public synchronized Profile getPf() throws Exception {
        if (P == null) P = Profile.get(RequesterID);
        return P;
    }

    public synchronized static MatchmakingRequest retrieveMatchmakingRequest(String id) {
        try (FileReader reader = new FileReader(MainDirectory + "/VS/matchmaking/" + id + ".json", StandardCharsets.UTF_8)) {
            MatchmakingRequest REQ = GSON.fromJson(reader, MatchmakingRequest.class);
            for (RequestData RD : REQ.MatchmakingRequests) RD.REQ = REQ;
            return REQ;
        } catch (Exception e) {
            return null;
        }
    }
    public synchronized void Save() {
        if (!MatchmakingRequests.isEmpty()) Save(MainDirectory + "/VS/matchmaking/" + ID + ".json");
    }
    public synchronized boolean Delete() {
        return new File(MainDirectory + "/VS/matchmaking/" + ID + ".json").delete();
    }
    public static class RequestData {
        private transient MatchmakingRequest REQ;

        public long ServerID;
        public String ChannelID;
        public String RoleID;
        public String MessageID;

        public RequestData(long serverID, String channelID, String roleID, String messageID) {
            ServerID = serverID;
            ChannelID = channelID;
            RoleID = roleID;
            MessageID = messageID;
        }

        private transient Guild G = null;
        public Guild getGuild() {
            if (G == null) G =  DiscordAccount.getGuildById(ServerID);
            return G;
        }
        private transient TextChannel C = null;
        public TextChannel getChannel() {
            if (C == null) C = getGuild().getTextChannelById(ChannelID);
            return C;
        }

        public void Accept(User accepter) throws Exception {
            getMessage(getChannel(), MessageID).delete().queue();
            getChannel().sendMessage(TL(REQ.getPf(), "matchmaking-match-accept-success", REQ.getUser().getAsMention(), accepter.getEffectiveName())).queue();
            REQ.getPf().RefreshMatchmakingTimeout(Instant.now().plus(5, ChronoUnit.MINUTES));
            REQ.Delete();
            if (REQ.Game.is3DS() || REQ.Game.isVR()) {
                if (GenerateRandomNumber(1, 2) == 1) {
                    getChannel().sendMessage(TL(REQ.getPf(),"matchmaking-random-host", REQ.getUser().getEffectiveName())).queue();
                } else {
                    getChannel().sendMessage(TL(REQ.getPf(),"matchmaking-random-host", accepter.getEffectiveName())).queue();
                }
            }
            sendPrivateMessage(REQ.getUser(), TL(REQ.getPf(),"matchmaking-someone-accepted-dm", accepter.getAsMention(), REQ.Game.getName()));
        }
        public void AcceptedElseWhere(User accepter) throws Exception {
            Message M = getMessage(getChannel(), MessageID);
            if (REQ.isAnonymous) {
                M.editMessage(TLG(getGuild(), "matchmaking-reply-accepted-ano", REQ.Game.getName(), "**" + accepter.getEffectiveName() + "**")).setReplace(true).queue();
            } else {
                M.editMessage(TLG(getGuild(), "matchmaking-reply-accepted", REQ.getUser().getEffectiveName(), REQ.Game.getName(), "**" + accepter.getEffectiveName() + "**")).setReplace(true).queue();
            }
        }
        public void Canceled() throws Exception {
            Message M = getMessage(getChannel(), MessageID);
            if (REQ.isAnonymous) {
                M.editMessage(TLG(getGuild(), "matchmaking-reply-cancel-ano")).setReplace(true).queue();
            } else {
                M.editMessage(TLG(getGuild(), "matchmaking-reply-cancel", REQ.getUser().getEffectiveName())).setReplace(true).queue();
            }
        }

    }
}
