package iecompbot.interaction.listeners.calcs.ds;

import ie.enums.BuildType;
import ie.enums.Element;
import ie.enums.Position;
import ie.games.ds.galaxy.*;
import ie.games.ds.object.Player;
import ie.games.ds.object.SlotMove;
import ie.games.ds.object.slotmove.skill.Talent;
import ie.games.ds.object.slotmove.summon.Spirit;
import iecompbot.objects.BotEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static iecompbot.L10N.TL;
import static iecompbot.Main.TempDirectory;
import static iecompbot.interaction.Automation.CanUseCommand;
import static iecompbot.interaction.Automation.replyException;
import static my.utilities.util.Utilities.takeOnlyLetters;

public class Galaxy {


    public static void Galaxy_LoadCommands(SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getName().contains("-glx")) {
                        event.deferReply().queue(M -> {
                            switch (event.getName()) {
                                case "build-glx-player" -> BuildGalaxy(event, M);
                                case "filter-glx-players" -> FilterGalaxy(event, M);
                                case "library-glx" -> LibraryGLX(event, M);
                            }
                        });

                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    public static void FilterGalaxy(SlashCommandInteractionEvent event, InteractionHook M) {
        try {
            if (event.getName().contains("players")) {
                GalaxyFilter CSB = new GalaxyFilter(BuildType.get(event.getOption("as-position").getAsString()));

                if (event.getOption("miximax-name") != null) {
                    CSB.setMixi(GalaxyUtils.getPlayersByName(event.getOption("miximax-name").getAsString()).getFirst());
                }
                else if (event.getOption("search-mixi") != null) {
                    CSB.withMixiSearch();
                }

                if (event.getOption("of-element") != null) {
                    CSB.setElementNeeded(Element.valueOf(event.getOption("of-element").getAsString()));
                }
                if (event.getOption("of-position") != null) {
                    CSB.setPositionNeeded(Position.valueOf(event.getOption("of-position").getAsString()));
                }
                if (event.getOption("of-gender") != null) {
                    CSB.setGenderNeeded(ie.enums.Gender.valueOf(event.getOption("of-gender").getAsString()));
                }

                if (event.getOption("min-gp") != null) {
                    CSB.setMinGP(event.getOption("min-gp").getAsInt());
                }
                if (event.getOption("min-tp") != null) {
                    CSB.setMinTP(event.getOption("min-tp").getAsInt());
                }
                if (event.getOption("max-gp") != null) {
                    CSB.setMaxGP(event.getOption("max-gp").getAsInt());
                }
                if (event.getOption("max-tp") != null) {
                    CSB.setMaxTP(event.getOption("max-tp").getAsInt());
                }

                if (event.getOption("minimum-kick") != null) {
                    CSB.setMinKick(event.getOption("minimum-kick").getAsInt());
                }
                if (event.getOption("minimum-dribble") != null) {
                    CSB.setMinDribble(event.getOption("minimum-dribble").getAsInt());
                }
                if (event.getOption("minimum-block") != null) {
                    CSB.setMinBlock(event.getOption("minimum-block").getAsInt());
                }
                if (event.getOption("minimum-catch") != null) {
                    CSB.setMinCatch(event.getOption("minimum-catch").getAsInt());
                }
                if (event.getOption("minimum-technique") != null) {
                    CSB.setMinTechnique(event.getOption("minimum-technique").getAsInt());
                }
                if (event.getOption("minimum-speed") != null) {
                    CSB.setMinSpeed(event.getOption("minimum-speed").getAsInt());
                }
                if (event.getOption("minimum-stamina") != null) {
                    CSB.setMinStamina(event.getOption("minimum-stamina").getAsInt());
                }
                if (event.getOption("minimum-luck") != null) {
                    CSB.setMinLuck(event.getOption("minimum-luck").getAsInt());
                }

                if (event.getOption("excluded-players") != null) {
                    String list = event.getOption("excluded-players").getAsString().toLowerCase()
                            .replaceAll("     ", "").replaceAll("    ", "").replaceAll("   ", "")
                            .replaceAll("  ", "").replaceAll("  ", "").replaceAll("  ", "")
                            .replaceAll(", ", ",").replaceAll(" ,", ",");
                    CSB.setExclusions(Arrays.stream(list.split(",")).toList());
                }
                DisplayPlayer(M, CSB.Filter());
            }
        } catch (Exception ignored) {
            M.editOriginal(TL(M,"script-fail-3")).queue();
        }
    }

    public static void BuildGalaxy(SlashCommandInteractionEvent event, InteractionHook M) {
        if (event.getName().contains("player")) {
            GalaxyPlayer P = GalaxyUtils.getPlayersByName(event.getOption("player-name").getAsString()).getFirst();
            GalaxyBuilder CSB = new GalaxyBuilder(P, BuildType.get(event.getOption("as-position").getAsString()));

            if (event.getOption("miximax-name") != null) {
                CSB.setMixi(GalaxyUtils.getPlayersByName(event.getOption("miximax-name").getAsString()).getFirst());
            }
            else if (event.getOption("search-mixi") != null) {
                CSB.withMixiSearch();
            }

            if (event.getOption("min-gp") != null) {
                CSB.setMinGP(event.getOption("min-gp").getAsInt());
            }
            if (event.getOption("min-tp") != null) {
                CSB.setMinTP(event.getOption("min-tp").getAsInt());
            }
            if (event.getOption("max-gp") != null) {
                CSB.setMaxGP(event.getOption("max-gp").getAsInt());
            }
            if (event.getOption("max-tp") != null) {
                CSB.setMaxTP(event.getOption("max-tp").getAsInt());
            }

            if (event.getOption("minimum-kick") != null) {
                CSB.setMinKick(event.getOption("minimum-kick").getAsInt());
            }
            if (event.getOption("minimum-dribble") != null) {
                CSB.setMinDribble(event.getOption("minimum-dribble").getAsInt());
            }
            if (event.getOption("minimum-block") != null) {
                CSB.setMinBlock(event.getOption("minimum-block").getAsInt());
            }
            if (event.getOption("minimum-catch") != null) {
                CSB.setMinCatch(event.getOption("minimum-catch").getAsInt());
            }
            if (event.getOption("minimum-technique") != null) {
                CSB.setMinTechnique(event.getOption("minimum-technique").getAsInt());
            }
            if (event.getOption("minimum-speed") != null) {
                CSB.setMinSpeed(event.getOption("minimum-speed").getAsInt());
            }
            if (event.getOption("minimum-stamina") != null) {
                CSB.setMinStamina(event.getOption("minimum-stamina").getAsInt());
            }
            if (event.getOption("minimum-luck") != null) {
                CSB.setMinLuck(event.getOption("minimum-luck").getAsInt());
            }
            if (event.getOption("excluded-players") != null) {
                String list = event.getOption("excluded-players").getAsString().toLowerCase()
                        .replaceAll("     ", "").replaceAll("    ", "").replaceAll("   ", "")
                        .replaceAll("  ", "").replaceAll("  ", "").replaceAll("  ", "")
                        .replaceAll(", ", ",").replaceAll(" ,", ",");
                CSB.setExclusions(Arrays.stream(list.split(",")).toList());
            }
            DisplayPlayer(M, CSB.Build());
        }
    }

    private static void DisplayPlayer(InteractionHook M, GalaxyPlayer P) {
        try {
            if (P != null) {
                EmbedBuilder E = new EmbedBuilder();
                E.setTitle(P.getName());
                E.setAuthor(P.getNameJP());
                E.setColor((P.getElement().equals(Element.FIRE) ? Color.decode("#b42002")
                        : P.getElement().equals(Element.WIND) ? Color.decode("#45c9e7")
                        : P.getElement().equals(Element.WOOD) ? Color.decode("#2dc457")
                        : P.getElement().equals(Element.EARTH) ? Color.decode("#e19b18")
                        : Color.decode("#bb0077")));

                E.addField(BotEmoji.get(P.getElement().getName()) + " Element:", "**• " + P.getElement() + "**", true);
                E.addField(BotEmoji.get(P.getPosition().getName()) + " Position:", "**• " + P.getPosition() + "**", true);
                E.addField(BotEmoji.get(P.getGender().getName()) + " Gender:", "**• " + P.getGender() + "**", true);
                String gpreal = (P.getAura() != null ? " (" + P.getMaxGP(true, true) + ")" : "");
                String tpreal = (P.getAura() != null ? " (" + P.getMaxTP(true, true) + ")" : "");
                E.addField(BotEmoji.get("GP").getFormatted() + " " + P.getMaxGP(true) + gpreal, " ", true);
                E.addField(BotEmoji.get("TP").getFormatted() + " " + P.getMaxTP(true) + tpreal, " ", true);
                E.setThumbnail("attachment://avatar.png");

                String kickreal = P.getKick(true, true, true) + " (" + P.getFreedomSpentKick() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.KICK) + "]`";
                String drireal = P.getDribble(true, true, true) + " (" + P.getFreedomSpentDribble() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.DRIBBLE) + "]`";
                String defreal = P.getBlock(true, true, true) + " (" + P.getFreedomSpentBlock() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.BLOCK) + "]`";
                String catchreal = P.getCatch(true, true, true) + " (" + P.getFreedomSpentCatch() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.CATCH) + "]`";
                String techreal = P.getTechnique(true, true, true) + " (" + P.getFreedomSpentTechnique() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.TECHNIQUE) + "]`";
                String speedreal = P.getSpeed(true, true, true) + " (" + P.getFreedomSpentSpeed() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.SPEED) + "]`";
                String stareal = P.getStamina(true, true, true) + " (" + P.getFreedomSpentStamina() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.STAMINA) + "]`";
                String luckreal = P.getLuck(true, true, true) + " (" + P.getFreedomSpentLuck() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.LUCK) + "]`";


                E.addField("Stats:",
                        "**" + TL(M,"Kick") + ":** " + kickreal + "\n" +
                                "**" + TL(M,"Dribble") + ":** " + drireal + "\n" +
                                "**" + TL(M,"Block") + ":** " + defreal + "\n" +
                                "**" + TL(M,"Catch") + ":** " + catchreal + "\n" +
                                "**" + TL(M,"Technique") + ":** " + techreal + "\n" +
                                "**" + TL(M,"Speed") + ":** " + speedreal + "\n" +
                                "**" + TL(M,"Stamina") + ":** " + stareal + "\n" +
                                "**" + TL(M,"Luck") + ":** " + luckreal + "\n" +
                                "**" + TL(M,"Freedom") + ":** `[" + P.getFreedom() + "/" + P.getMaxFreedom() + "]`"
                        , false);

                String moveset = "";
                for (SlotMove SM : P.getMoves()) {
                    if (SM instanceof GalaxySpecialMove S) {
                        moveset = moveset + BotEmoji.get(S.getMoveType().getName()) + BotEmoji.get(S.getElement().getName()) + S.getName() + " **Lvl." + S.getLevel() + "** (" +  S.getPower() + ") `[" + S.getCost() + " TP]`\n";
                    } else if (SM instanceof Talent T) {
                        moveset = moveset + BotEmoji.get("SKILL") + " " + T.getName() + "\n";
                    }
                }
                moveset = moveset + "\n";
                if (P.getSummon() != null) {
                    moveset = moveset + BotEmoji.get(P.getSummon().getType()) + BotEmoji.get(P.getSummon().getElement().getName()) + P.getSummon().getName() + "\n";
                }
                if (P.getMixiSummon() != null) {
                    moveset = moveset + BotEmoji.get(P.getAura().getSummon().getType()) + BotEmoji.get(P.getAura().getSummon().getElement().getName()) + P.getAura().getSummon().getName() + "\n";
                }
                E.addField("Moveset:", moveset, false);

                String eqp = "";
                if (P.getBoots() != null) eqp = eqp + ":athletic_shoe: " + P.getBoots().getName() + "\n";
                if (P.getPendant() != null) eqp = eqp + ":grinning:  " + P.getPendant().getName() + "\n";
                if (P.getBracelet() != null) eqp = eqp + ":hand_splayed: " + P.getBracelet().getName() + "\n";
                if (P.getGloves() != null) eqp = eqp + ":gloves: " + P.getGloves().getName();

                if (eqp.length() > 1) E.addField("Equipments", eqp, false);

                if (P.getAura() != null) E.addField("Miximax:", P.getAura().getName() + " \\(" + P.getAura().getNameJP() + "\\)", false);
                M.editOriginalEmbeds(E.build()).setFiles(FileUpload.fromData(P.getImage(), "avatar.png")
                        , FileUpload.fromData(P.ExportToNFFMFile(new File(TempDirectory + "/" + takeOnlyLetters(P.getName()) + ".INZ6")), P.getName() + (P.getAura() != null ? "_" + P.getAura().getName() : "") + ".INZ6")
                ).setReplace(true).queue();
            } else {
                M.editOriginal(TL(M,"script-fail-3")).queue();
            }
        } catch (IOException e) {
            replyException(M, e);
        }
    }

    public static void LibraryGLX(SlashCommandInteractionEvent event, InteractionHook M) {
        if (event.getOption("type").getAsString().equals("Player")) {
            ViewPlayer(M, GalaxyUtils.getPlayersByName(event.getOption("name").getAsString()));
        } else if (event.getOption("type").getAsString().equals("Move")) {
            ViewMove(M, java.util.List.of((GalaxySpecialMove) GalaxyUtils.getMoveByName(event.getOption("name").getAsString())));
        } else if (event.getOption("type").getAsString().equals("Spirit")) {
            ViewSpirit(M, java.util.List.of(GalaxyUtils.getSpiritByName(event.getOption("name").getAsString())));
        } else if (event.getOption("type").getAsString().equals("Totem")) {
            ViewTotem(M, java.util.List.of(GalaxyUtils.getTotemByName(event.getOption("name").getAsString())));
        }
    }
    public static void ViewPlayer(InteractionHook M, java.util.List<GalaxyPlayer> Players) {
        try {
            if (!Players.isEmpty()) {
                java.util.List<MessageEmbed> Es = new ArrayList<>();
                java.util.List<FileUpload> FU = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    Player P = Players.get(i);
                    EmbedBuilder E = new EmbedBuilder();
                    E.setTitle(P.getName());
                    E.setAuthor(P.getNameJP());
                    E.setColor((P.getElement().equals(Element.FIRE) ? Color.decode("#b42002")
                            : P.getElement().equals(Element.WIND) ? Color.decode("#45c9e7")
                            : P.getElement().equals(Element.WOOD) ? Color.decode("#2dc457")
                            : P.getElement().equals(Element.EARTH) ? Color.decode("#e19b18")
                            : Color.decode("#bb0077")));

                    E.addField(BotEmoji.get(P.getElement().getName()) + " Element:", "**• " + P.getElement() + "**", true);
                    E.addField(BotEmoji.get(P.getPosition().getName()) + " Position:", "**• " + P.getPosition() + "**", true);
                    E.addField(BotEmoji.get(P.getGender().getName()) + " Gender:", "**• " + P.getGender() + "**", true);
                    E.addField(BotEmoji.get("GP").getFormatted() + " " + P.getMaxGP(true), " ", true);
                    E.addField(BotEmoji.get("TP").getFormatted() + " " + P.getMaxTP(true), " ", true);
                    E.setThumbnail("attachment://" + i + ".png");

                    E.addField("Stats:",
                            "**" + TL(M, "Kick") + ":** " + P.getKick(true, true) + "\n" +
                                    "**" + TL(M, "Dribble") + ":** " + P.getDribble(true, true) + "\n" +
                                    "**" + TL(M, "Block") + ":** " + P.getBlock(true, true) + "\n" +
                                    "**" + TL(M, "Catch") + ":** " + P.getCatch(true, true) + "\n" +
                                    "**" + TL(M, "Technique") + ":** " + P.getTechnique(true, true) + "\n" +
                                    "**" + TL(M, "Speed") + ":** " + P.getSpeed(true, true) + "\n" +
                                    "**" + TL(M, "Stamina") + ":** " + P.getStamina(true, true) + "\n" +
                                    "**" + TL(M, "Luck") + ":** " + P.getLuck(true, true) + "\n" +
                                    "**" + TL(M, "Freedom") + ":** " + P.getMaxFreedom()
                            , false);

                    String moveset = "";
                    for (SlotMove SM : P.getMoves()) {
                        if (SM instanceof GalaxySpecialMove S) {
                            moveset = moveset + BotEmoji.get(S.getMoveType().getName()) + BotEmoji.get(S.getElement().getName()) + S.getName() + " **Lvl." + S.getLevel() + "** (" + S.getPower() + ") `[" + S.getCost() + " TP]`\n";
                        } else if (SM instanceof Talent T) {
                            moveset = moveset + BotEmoji.get("SKILL") + " " + T.getName() + "\n";
                        }
                    }
                    moveset = moveset + "\n";
                    if (P.getSummon() != null) {
                        moveset = moveset + BotEmoji.get(P.getSummon().getType()) + BotEmoji.get(P.getSummon().getElement().getName()) + P.getSummon().getName() + "\n";
                    }
                    E.addField("Moveset:", moveset, false);
                    Es.add(E.build());
                    try (FileUpload fu = FileUpload.fromData(P.getImage(), i + ".png")) {
                        FU.add(fu);
                    } catch (Exception ignored) {}
                }
                M.editOriginalEmbeds(Es).setFiles(FU).setReplace(true).queue();
            } else {
                M.editOriginal(TL(M, "script-fail-3")).queue();
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public static void ViewMove(InteractionHook M, java.util.List<GalaxySpecialMove> Moves) {
        try {
            if (!Moves.isEmpty()) {
                java.util.List<MessageEmbed> Es = new ArrayList<>();
                java.util.List<FileUpload> FU = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    GalaxySpecialMove S = Moves.get(i);
                    EmbedBuilder E = new EmbedBuilder();
                    E.setImage("attachment://" + i + ".png");
                    E.setTitle(S.getName());
                    E.setColor((S.getElement().equals(Element.FIRE) ? Color.decode("#b42002")
                            : S.getElement().equals(Element.WIND) ? Color.decode("#45c9e7")
                            : S.getElement().equals(Element.WOOD) ? Color.decode("#2dc457")
                            : S.getElement().equals(Element.EARTH) ? Color.decode("#e19b18")
                            : Color.decode("#bb0077")));

                    E.addField(BotEmoji.get(S.getElement().getName()) + " Element:", "**• " + S.getElement() + "**", true);
                    E.addField(BotEmoji.get(S.getMoveType().getName()) + " Type:", "**• " + S.getMoveType() + "**", true);
                    E.addField("Power:", "**• " + S.getPower() + "**", true);
                    E.addField("Stun:", "**• " + S.getStun() + "**", true);
                    E.addField("TP Cost:", "**• " + S.getCost() + "**", true);
                    E.addField("Difficulty:", "**• " + S.getDifficulty() + "**", true);
                    E.addField("Max Level:", "**• " + S.getMaxLevel() + "**", true);
                    S.setLevel(6);
                    E.addField("Max Power:", "**• " + S.getPower() + "**", true);
                    E.addField("Max Stun:", "**• " + S.getStun() + "**", true);

                    Es.add(E.build());
                    try (FileUpload fu = FileUpload.fromData(S.getSplashName(), i + ".png")) {
                        FU.add(fu);
                    } catch (Exception ignored) {}
                }
                M.editOriginalEmbeds(Es).setFiles(FU).setReplace(true).queue();
            } else {
                M.editOriginal(TL(M, "script-fail-3")).queue();
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public static void ViewSpirit(InteractionHook M, java.util.List<GalaxySpirit> Spirits) {
        try {
            if (!Spirits.isEmpty()) {
                java.util.List<MessageEmbed> Es = new ArrayList<>();
                List<FileUpload> FU = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    Spirit S = Spirits.get(i);
                    EmbedBuilder E = new EmbedBuilder();
                    E.setImage("attachment://" + i + ".png");
                    E.setTitle(S.getName());
                    E.setColor((S.getElement().equals(Element.FIRE) ? Color.decode("#b42002")
                            : S.getElement().equals(Element.WIND) ? Color.decode("#45c9e7")
                            : S.getElement().equals(Element.WOOD) ? Color.decode("#2dc457")
                            : S.getElement().equals(Element.EARTH) ? Color.decode("#e19b18")
                            : Color.decode("#bb0077")));

                    E.addField(BotEmoji.get(S.getElement().getName()) + " Element:", "**• " + S.getElement() + "**", true);
                    E.addField(BotEmoji.get(S.getType()) + " Type:", "**• " + S.getType() + "**", true);

                    StringBuilder statsLine = new StringBuilder();
                    StringBuilder statsLine2 = new StringBuilder();
                    for (int level = 1; level <= 6; level++) {
                        S.setLevel(level);
                        statsLine.append(S.getFSP());
                        statsLine2.append(S.getAttack());
                        if (level < 6) statsLine.append(" → ");
                        if (level < 6) statsLine2.append(" → ");
                    }
                    E.addField("FSP:", statsLine.toString(), false);
                    E.addField("Attack:", statsLine2.toString(), false);

                    E.addField(" ", "`                       `", false);
                    E.addField(BotEmoji.get(S.getMove().getElement().getName()) + " " + S.getMove().getName(), "**• " + S.getMove().getPower() + " Power**", true);

                    Es.add(E.build());
                    try (FileUpload fu = FileUpload.fromData(S.getSplashName(), i + ".png")) {
                        FU.add(fu);
                    } catch (Exception ignored) {
                    }
                }
                M.editOriginalEmbeds(Es).setFiles(FU).setReplace(true).queue();
            } else {
                M.editOriginal(TL(M, "script-fail-3")).queue();
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }
    public static void ViewTotem(InteractionHook M, java.util.List <Totem> Totems) {
        try {
            if (!Totems.isEmpty()) {
                java.util.List<MessageEmbed> Es = new ArrayList<>();
                List<FileUpload> FU = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    Totem T = Totems.get(i);
                    EmbedBuilder E = new EmbedBuilder();
                    E.setImage("attachment://" + i + ".png");
                    E.setTitle(T.getName());
                    E.setColor((T.getElement().equals(Element.FIRE) ? Color.decode("#b42002")
                            : T.getElement().equals(Element.WIND) ? Color.decode("#45c9e7")
                            : T.getElement().equals(Element.WOOD) ? Color.decode("#2dc457")
                            : T.getElement().equals(Element.EARTH) ? Color.decode("#e19b18")
                            : Color.decode("#bb0077")));

                    E.addField(BotEmoji.get(T.getElement().getName()) + " Element:", "**• " + T.getElement() + "**", true);
                    E.addField("Type:", "**• TOTEM**", true);

                    StringBuilder tpLine = new StringBuilder();
                    for (int level = 1; level <= 5; level++) {
                        T.setLevel(level);
                        tpLine.append(T.getTP());
                        if (level < 5) tpLine.append(" → ");
                    }
                    E.addField("TP:", tpLine.toString(), false);

                    String bonuses = "**Bonus 1:** " + T.getBonus1().getName() + "\n" +
                            "**Bonus 2:** " + T.getBonus2().getName() + "\n" +
                            "**Bonus 3:** " + T.getBonus3().getName() + "\n" +
                            "**Bonus 4:** " + T.getBonus4().getName() + "\n" +
                            "**Bonus 5:** " + T.getBonus5().getName() + "\n" +
                            "**Bonus 6:** " + T.getBonus6().getName();
                    E.addField("Bonuses:", bonuses, false);

                    Es.add(E.build());
                    try (FileUpload fu = FileUpload.fromData(T.getImage(), i + ".png")) {
                        FU.add(fu);
                    } catch (Exception ignored) {}
                }
                M.editOriginalEmbeds(Es).setFiles(FU).setReplace(true).queue();
            } else {
                M.editOriginal(TL(M, "script-fail-3")).queue();
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }
}
