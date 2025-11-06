package iecompbot.objects.server;

import iecompbot.objects.clan.Clan;
import iecompbot.objects.match.Game;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.interaction.listeners.MDFFeatures.ClearClanTags;
import static iecompbot.objects.Retrieval.getUserByID;
import static my.utilities.util.Utilities.getClock;

public class Leaderboard {

    private final List<LeaderboardG> Boards = new ArrayList<>();

    public Leaderboard(List<Clan> CLS, int page, int amount) {
        for (Game G : Game.values()) Boards.add(new LeaderboardOfGame(CLS, G, null, page, amount));
        Boards.sort(Comparator.comparingInt((LeaderboardG L) -> L.HighestMedal).reversed());
    }
    public Leaderboard(List<Clan> CLS, Game G, int page, int amount) {
        Boards.add(new LeaderboardOfGame(CLS, G, null, page, amount));
        Boards.sort(Comparator.comparingInt((LeaderboardG L) -> L.HighestMedal).reversed());
    }
    public Leaderboard(ServerInfo I, List<Clan> CLS, int page, int amount) {
        for (Game G : Game.values()) Boards.add(new LeaderboardOfGame(CLS, G, I, page, amount));
        Boards.sort(Comparator.comparingInt((LeaderboardG L) -> L.HighestMedal).reversed());
    }
    public Leaderboard(ServerInfo I, List<Clan> CLS, Game G, int page, int amount) {
        Boards.add(new LeaderboardOfGame(CLS, G, I, page, amount));
        Boards.sort(Comparator.comparingInt((LeaderboardG L) -> L.HighestMedal).reversed());
    }

    public List<EmbedBuilder> getEmbeds(ServerInfo I) {
        List<EmbedBuilder> Es = new ArrayList<>();
        for (LeaderboardG values : new ArrayList<>(Boards)) {
            if (I != null && I.isGameBoardAllowed(values.G) && !values.getFields().isEmpty() && values.HighestMedal > 0) {
                EmbedBuilder E = new EmbedBuilder(values);
                E.clearFields();
                if (!values.getFields().isEmpty()) {
                    E.addField(TLG(I, "Rank"), Objects.requireNonNull(values.getFields().getFirst().getValue()), true);
                    E.addField(TLG(I, "Medals"), Objects.requireNonNull(values.getFields().get(1).getValue()), true);
                    E.addField(TLG(I, "Name"), Objects.requireNonNull(values.getFields().get(2).getValue()), true);
                }
                if (values.getFields().size() > 3) {
                    E.addField(values.getFields().get(3));
                    E.addField(values.getFields().get(4));
                    E.addField(values.getFields().get(5));
                }
                if (values.getFields().size() > 6) {
                    E.addField(values.getFields().get(6));
                    E.addField(values.getFields().get(7));
                    E.addField(values.getFields().get(8));
                }
                E.setDescription(TLG(I, "leaderboard-description"));
                Es.add(E);
            }
        }
        if (!Es.isEmpty()) Es.getLast().setFooter(" • " + TLG(I,"updated-on-time", getClock() + " (GMT+2)"), I.getGuild().getIconUrl());
        return Es;
    }

    public List<EmbedBuilder> getEmbeds(InteractionHook M) {
        List<EmbedBuilder> Es = new ArrayList<>();
        for (LeaderboardG values : new ArrayList<>(Boards)) {
            EmbedBuilder E = new EmbedBuilder(values);
            E.clearFields();
            if (!values.getFields().isEmpty()) {
                E.addField(TL(M, "Rank"), Objects.requireNonNull(values.getFields().getFirst().getValue()), true);
                E.addField(TL(M, "Medals"), Objects.requireNonNull(values.getFields().get(1).getValue()), true);
                E.addField(TL(M, "Name"), Objects.requireNonNull(values.getFields().get(2).getValue()), true);
            }
            if (values.getFields().size() > 3) {
                E.addField(values.getFields().get(3));
                E.addField(values.getFields().get(4));
                E.addField(values.getFields().get(5));
            }
            if (values.getFields().size() > 6) {
                E.addField(values.getFields().get(6));
                E.addField(values.getFields().get(7));
                E.addField(values.getFields().get(8));
            }
            Es.add(E);
        }
        return Es;
    }

    private static class LeaderboardG extends EmbedBuilder {
        int HighestMedal = 0;
        private final Game G;
        public LeaderboardG(Game G) {
            this.G = G;
            setTitle(G.getEmoji() + " " + G.getName() + " Leaderboard");
            setThumbnail(G.getImageUrl());
            setColor(G.getColor());
        }
    }
    private static class LeaderboardOfGame extends LeaderboardG {
        int offset = 0;
        public LeaderboardOfGame(List<Clan> CLS, Game G, ServerInfo I, int page, int amount) {
            super(G);
            try {
                offset = amount * (page - 1);
                List<DatabaseObject.Row> TR = DatabaseObject.doQueryAll("CALL DisplayTop15(?,?,?,?)", G.getCode(), I != null && I.Ranking().hasPrivateRanking() ? I.getGuild().getId() : null, page, amount);
                if (I != null && I.showBoardMembersOnly) TR.removeIf(TRR  -> I.getGuild().getMemberById(TRR.getAsString("UserID")) == null);
                WriteLeaderboard(TR, CLS, amount);
            } catch (Exception ignored) {}
        }

        private void WriteLeaderboard(List<DatabaseObject.Row> TR, List<Clan> CLS, int max) {
            String ranks = "";
            String names = "";
            String PTS = "";

            String ranks2 = "";
            String names2 = "";
            String PTS2 = "";

            String ranks3 = "";
            String names3 = "";
            String PTS3 = "";
            for (int i = 0; i < Math.min(TR.size(), max); i++) {
                String tag = TR.get(i).getAsString("Tag");
                String league = TR.get(i).getAsString("League");
                User u = getUserByID(TR.get(i).getAsString("UserID"));
                if (u != null) {
                    if (i < 10) {
                        ranks = ranks + (i + 1 + offset) + "\n";
                        names = names + (tag != null ? tag + " | " : "") + ClearClanTags(u.getEffectiveName(), CLS) + "\n";
                        PTS = PTS + league + " "  + TR.get(i).get("Medals") + "\n";
                    } else if (i < 20) {
                        ranks2 = ranks2 + (i + 1 + offset) + "\n";
                        names2 = names2 + (tag != null ? tag + " | " : "") + ClearClanTags(u.getEffectiveName(), CLS) + "\n";
                        PTS2 = PTS2 + league + " "  + TR.get(i).get("Medals") + "\n";
                    } else {
                        ranks3 = ranks3 + (i + 1 + offset) + "\n";
                        names3 = names3 + (tag != null ? tag + " | "  : "") + ClearClanTags(u.getEffectiveName(), CLS) + "\n";
                        PTS3 = PTS3 + league + " " + TR.get(i).get("Medals") + "\n";
                    }
                }
                if (HighestMedal == 0) HighestMedal = TR.get(i).getAsInt("Medals");
            }
            if (!ranks.isEmpty()) {
                addField("Rank", ranks.replaceFirst("1", ":first_place:").replaceFirst("2", ":second_place:").replaceFirst("3", ":third_place:"), true);
                addField("Points", "**" + PTS + "**", true);
                addField("Name", names, true);
            }
            if (!ranks2.isEmpty()) {
                addField(" ", ranks2, true);
                addField(" ", "**" + PTS2 + "**", true);
                addField(" ", names2, true);
            }
            if (!ranks3.isEmpty()) {
                addField(" ", ranks3, true);
                addField(" ", "**" + PTS3 + "**", true);
                addField(" ", names3, true);
            }
        }
    }
}
