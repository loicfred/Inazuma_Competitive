package iecompbot.objects.match;

import at.stefangeyer.challonge.model.enumeration.TournamentState;
import iecompbot.Constants;
import iecompbot.img.builders.PredictionImageBuilder;
import iecompbot.interaction.cmdbreakdown.clan.BetCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.img.ImgUtilities.mixColors;
import static iecompbot.objects.Retrieval.getMessage;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.clan.Clan.getClanOfUser;

public class Bet extends DatabaseObject<Bet> {
    private transient Game g;
    private transient List<Bet_Voter> voters;

    public long ID;

    public long P1ID;
    public long P2ID;
    public long EndTimeEpochSecond;
    public long WinnerID;
    public String Phase = TournamentState.UNDERWAY.toString();
    public String GameCode;
    public String ImageURL;



    public Bet(Message M, User p1, User p2, Instant endtime, Game game) {
        this.ID = M.getIdLong();
        this.P1ID = p1.getIdLong();
        this.P2ID = p2.getIdLong();
        this.EndTimeEpochSecond = endtime.getEpochSecond();
        this.GameCode = game.getCode();
        this.g = game;
        Write();
    }

    public long getId() {
        return ID;
    }
    public long getP1ID() {
        return P1ID;
    }
    public long getP2ID() {
        return P2ID;
    }
    public Instant getEndTime() {
        return Instant.ofEpochSecond(EndTimeEpochSecond);
    }
    public Long getWinnerID() {
        if (!getState().equals(TournamentState.COMPLETE)) return null;
        return WinnerID;
    }
    public TournamentState getState() {
        return TournamentState.valueOf(Phase.toUpperCase().replaceAll(" ", "_"));
    }
    public Game getGame() {
        return g == null ? g = Game.get(GameCode) : g;
    }
    public String getImageURL() {
        if (ImageURL == null) {
            try (PredictionImageBuilder IMG = new PredictionImageBuilder(Profile.get(P1ID), Profile.get(P2ID), null);
                 FileUpload F = FileUpload.fromData(IMG.GenerateCardPNG().DownloadPNGToFile(), "image.png")){
                Message M = Constants.TempChannel.sendFiles(F).submit().orTimeout(3, TimeUnit.SECONDS).get();
                setImageURL(M.getAttachments().getFirst().getUrl());
                Update();
            } catch (Exception ignored) {
                return "attachment://logo.png";
            }
        }
        return ImageURL;

    }

    public void setEndTime(Instant end) {
        EndTimeEpochSecond = end.getEpochSecond();
        Update();
    }
    public void setWinnerID(long winnerID) {
        WinnerID = winnerID;
        Update();
    }
    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
        Update();
    }

    public long getOpponent(long candidateid) {
        return candidateid == getP1ID() ? getP2ID() : getP1ID();
    }

    public Bet_Voter AddBet(User voter, long candidateId, int Amount) {
        Bet_Voter Voter = Bet_Voter.get(getId(), voter.getIdLong());
        if (Voter != null) Voter.setAmount(Voter.getAmount() + Amount);
        else Voter = new Bet_Voter(getId(), voter.getIdLong(), candidateId, Amount);
        return Voter;
    }
    public int countBetter(long candidateid) {
        return (int) getVoters().stream().filter(b -> b.getCandidateID() == candidateid).count();
    }
    public int countCoins(long candidateid) {
        return getVoters().stream().filter(b -> b.getCandidateID() == candidateid).mapToInt(Bet_Voter::getAmount).sum();
    }
    public int countPercentage(Bet_Voter Me) {
        double totalA = 0;
        double totalB = 0;
        for (Bet_Voter b : getVoters()) {
            if (b.getCandidateID() == P1ID) {
                totalA += b.getAmount();
            } else if (b.getCandidateID() == P2ID) {
                totalB += b.getAmount();
            }
        }
        if (Me.getCandidateID() == P1ID) {
            return (int) (100 * (Me.getAmount() / totalA));
        } else if (Me.getCandidateID() == P2ID) {
            return (int) (100 * (Me.getAmount() / totalB));
        }
        return 0;
    }
    public boolean EndBet(MatchLog L) {
        if (!getGame().equals(L.getGame())) return false;

        long whowin = 0;
        if (P1ID == L.getP1ID() && P2ID == L.getP2ID()) {
            if (L.getP1Score() > L.getP2Score()) {
                whowin = P1ID;
            } else if (L.getP1Score() < L.getP2Score()) {
                whowin = P2ID;
            }
        } else if (P2ID == L.getP1ID() && P1ID == L.getP2ID()) {
            if (L.getP1Score() > L.getP2Score()) {
                whowin = P2ID;
            } else if (L.getP1Score() < L.getP2Score()) {
                whowin = P1ID;
            }
        } else {
            return false;
        }
        double totalA = 0;
        double totalB = 0;
        for (Bet_Voter b : getVoters()) {
            if (b.getCandidateID() == P1ID) {
                totalA = totalA + b.getAmount();
            } else if (b.getCandidateID() == P2ID) {
                totalB = totalB + b.getAmount();
            }
        }
        int i = 0;
        if (whowin == P1ID) {
            WinnerID = P1ID;
            for (Bet_Voter b : getVoters()) {
                Profile P = Profile.get(b.getVoterID());
                if (b.getCandidateID() == P1ID) {
                    i++;
                    double MoneyEarned = b.getAmount() + (totalB * (b.getAmount() / totalA));
                    P.addItem(1, (int) MoneyEarned);
                    sendPrivateMessage(P.getUser(), TL(P,"bet-win", iecompbot.objects.profile.item.Item.get(1).getEmojiFormatted() + " **" + (int) MoneyEarned + "**"));
                } else {
                    sendPrivateMessage(P.getUser(), TL(P,"bet-lost", iecompbot.objects.profile.item.Item.get(1).getEmojiFormatted() + " **" + b.getAmount() + "**"));
                }
            }
            if (i == 0) {
                Profile P = Profile.get(P1ID);
                P.addItem(1, (int) totalB);
                sendPrivateMessage(P.getUser(), TL(P,"bet-win", iecompbot.objects.profile.item.Item.get(1).getEmojiFormatted() + " **" + (int) totalB + "**"));
            }
        } else if (whowin == P2ID) {
            WinnerID = P2ID;
            for (Bet_Voter b : getVoters()) {
                Profile P = Profile.get(b.getVoterID());
                if (b.getCandidateID() == P2ID) {
                    i++;
                    double MoneyEarned = b.getAmount() + (totalA * (b.getAmount() / totalB));
                    P.addItem(1, (int) MoneyEarned);
                    sendPrivateMessage(P.getUser(), TL(P,"bet-win", iecompbot.objects.profile.item.Item.get(1).getEmojiFormatted() + " **" + (int) MoneyEarned + "**"));
                } else {
                    sendPrivateMessage(P.getUser(), TL(P,"bet-lost", iecompbot.objects.profile.item.Item.get(1).getEmojiFormatted() + " **" + b.getAmount() + "**"));
                }
            }
            if (i == 0) {
                Profile P = Profile.get(P2ID);
                P.addItem(1, (int) totalA);
                sendPrivateMessage(P.getUser(), TL(P,"bet-win", iecompbot.objects.profile.item.Item.get(1).getEmojiFormatted() + " **" + (int) totalA + "**"));
            }
            Phase = TournamentState.COMPLETE.toString();
            Update();
        }
        return true;

    }

    public void BetUI(BetCommand CMD, TextChannel C) {
        Message M = getMessage(C, getId());
        if (M == null) {
            Delete();
        } else {
            Guild GUILD = M.getGuild();
            User u1 = getUserByID(P1ID);
            User u2 = getUserByID(P2ID);

            Clan c1 = getClanOfUser(u1);
            Clan c2 = getClanOfUser(u2);
            String s1 = c1 != null ? c1.getEmojiFormatted() : "";
            String s2 = c2 != null ? c2.getEmojiFormatted() : "";
            String name1 = GUILD.getMemberById(u1.getId()) != null ? Objects.requireNonNull(GUILD.getMemberById(u1.getId())).getEffectiveName() : u1.getEffectiveName();
            String name2 = GUILD.getMemberById(u2.getId()) != null ? Objects.requireNonNull(GUILD.getMemberById(u2.getId())).getEffectiveName() : u2.getEffectiveName();
            String P1Color = DatabaseObject.doQuery("SELECT ColorCode FROM profile WHERE UserID = ?", u1.getId()).orElse(new Row(Map.of("ColorCode", "#FFFFFF"))).getAsString("ColorCode");
            String P2Color = DatabaseObject.doQuery("SELECT ColorCode FROM profile WHERE UserID = ?", u2.getId()).orElse(new Row(Map.of("ColorCode", "#FFFFFF"))).getAsString("ColorCode");

            EmbedBuilder Embed = new EmbedBuilder();
            Embed.setFooter("• " + GUILD.getName(), GUILD.getIconUrl());
            Embed.setTitle(s1 + name1 + " " + BotEmoji.get("VS3") + " " + name2 + " " + s2);
            Embed.setColor(Color.decode(mixColors(P1Color, P2Color)));
            Embed.setImage(getImageURL());
            Embed.setAuthor(TLG(GUILD, "match-betting"), null, getGame().getImageUrl());
            Embed.setFooter("• " + GUILD.getName(), GUILD.getIconUrl());
            Embed.setDescription(TLG(GUILD, "match-betting-description"));

            Embed.addField("__" + u1.getEffectiveName() + "__", "**Betters: " + BotEmoji.get("icon_fan") + countBetter(P1ID)
                    + "\n" + "InaCoin: " + Item.get("InaCoin").getEmojiFormatted() + countCoins(P1ID) + "**", true);

            Embed.addField("__" + u2.getEffectiveName() + "__", "**Betters: " + BotEmoji.get("icon_fan") + countBetter(P2ID)
                    + "\n" + "InaCoin: " + Item.get("InaCoin").getEmojiFormatted() + countCoins(P2ID) + "**", true);

            if (Instant.now().isBefore(getEndTime())) {
                Embed.addField(TLG(GUILD, "End") + ": <t:" + EndTimeEpochSecond + ":f> (<t:" + EndTimeEpochSecond + ":R>)", " ", false);
            } else {
                Embed.addField(TLG(GUILD, "End") + ": __" + TLG(GUILD, "Completed") + "__", " ", false);
            }

            Button btn1 = Button.success(CMD.Command("B-vote-p1"), u1.getEffectiveName());
            Button btn2 = Button.success(CMD.Command("B-vote-p2"), u2.getEffectiveName());
            Button btn3 = Button.secondary(CMD.Command("B-time-add"), "Add Time");
            Button btn4 = Button.secondary(CMD.Command("B-time-less"), "Remove Time");
            Button btn5 = Button.secondary(CMD.Command("bet-manage"), "Betting Info");

            M.editMessageEmbeds(Embed.build()).setComponents(ActionRow.of(btn1, btn2, btn3, btn4, btn5)).setReplace(true).queue();
        }
    }

    public List<Bet_Voter> getVoters() {
        return voters == null ? voters = Bet_Voter.get(getId()) : voters;
    }


    public static Bet get(long messageId) {
        return getById(Bet.class, messageId).orElse(null);
    }
    public static Bet getIncomplete(long candidate1id, long candidate2id) {
        return getWhere(Bet.class, "((P1ID = ? AND P2ID = ?) OR (P2ID = ? AND P1ID = ?) AND WinnerID IS NULL)", candidate1id, candidate2id, candidate1id, candidate2id).orElse(null);
    }

}
