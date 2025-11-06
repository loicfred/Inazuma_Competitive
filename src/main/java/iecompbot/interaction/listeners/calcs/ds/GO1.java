package iecompbot.interaction.listeners.calcs.ds;

import ie.enums.BuildType;
import ie.enums.Element;
import ie.enums.Position;
import ie.games.ds.go1.*;
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

public class GO1 {

    public static void GO1_LoadCommands(SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getName().contains("-go1")) {
                        event.deferReply().queue(M -> {
                            switch (event.getName()) {
                                case "build-go1-player" -> BuildGO1(event, M);
                                case "filter-go1-players" -> FilterGO1(event, M);
                                case "library-go1" -> LibraryGO1(event, M);
                            }
                        });

                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    public static void FilterGO1(SlashCommandInteractionEvent event, InteractionHook M) {
        if (event.getName().contains("players")) {
            GOFilter CSB = new GOFilter(event.getOption("as-position").getAsString().equals("FW") ? BuildType.FW
                    : event.getOption("as-position").getAsString().equals("FW_RECONVERT") ? BuildType.FW_RECONVERT
                    : event.getOption("as-position").getAsString().equals("MF") ? BuildType.MF
                    : event.getOption("as-position").getAsString().equals("MF_RECONVERT") ? BuildType.MF_RECONVERT
                    : event.getOption("as-position").getAsString().equals("DF") ? BuildType.DF
                    : event.getOption("as-position").getAsString().equals("DF_RECONVERT") ? BuildType.DF_RECONVERT
                    : event.getOption("as-position").getAsString().equals("GK") ? BuildType.GK
                    : BuildType.GK_RECONVERT
            );

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
    }

    public static void BuildGO1(SlashCommandInteractionEvent event, InteractionHook M) {
        try {
            if (event.getName().contains("player")) {
                GOPlayer P = GO1Utils.getPlayersByName(event.getOption("player-name").getAsString()).getFirst();
                GOBuilder CSB = new GOBuilder(P, BuildType.get(event.getOption("as-position").getAsString()));
                if (event.getOption("excluded-players") != null) {
                    String list = event.getOption("excluded-players").getAsString().toLowerCase()
                            .replaceAll("     ", "").replaceAll("    ", "").replaceAll("   ", "")
                            .replaceAll("  ", "").replaceAll("  ", "").replaceAll("  ", "")
                            .replaceAll(", ", ",").replaceAll(" ,", ",");
                    CSB.setExclusions(Arrays.stream(list.split(",")).toList());
                }
                DisplayPlayer(M, CSB.Build());
            }
        } catch (Exception ignored) {
            M.editOriginal(TL(M,"script-fail-3")).queue();
        }
    }
    
    private static void DisplayPlayer(InteractionHook M, GOPlayer P) {
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
                E.addField(BotEmoji.get("GP").getFormatted() + " " + P.getMaxGP(true), " ", true);
                E.addField(BotEmoji.get("TP").getFormatted() + " " + P.getMaxTP(true), " ", true);
                E.setThumbnail("attachment://avatar.png");

                String kickreal = P.getKick(true, true) + " (" + P.getFreedomSpentKick() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.KICK) + "]`";
                String drireal = P.getDribble(true, true) + " (" + P.getFreedomSpentDribble() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.DRIBBLE) + "]`";
                String defreal = P.getBlock(true, true) + " (" + P.getFreedomSpentBlock() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.BLOCK) + "]`";
                String catchreal = P.getCatch( true, true) + " (" + P.getFreedomSpentCatch() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.CATCH) + "]`";
                String techreal = P.getTechnique(true, true) + " (" + P.getFreedomSpentTechnique() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.TECHNIQUE) + "]`";
                String speedreal = P.getSpeed(true, true) + " (" + P.getFreedomSpentSpeed() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.SPEED) + "]`";
                String stareal = P.getStamina(true, true) + " (" + P.getFreedomSpentStamina() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.STAMINA) + "]`";
                String luckreal = P.getLuck(true, true) + " (" + P.getFreedomSpentLuck() + ") `[" + P.getEquipmentBoost(ie.enums.DSStats.LUCK) + "]`";


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
                    if (SM instanceof GOSpecialMove S) {
                        moveset = moveset + BotEmoji.get(S.getMoveType().getName()) + BotEmoji.get(S.getElement().getName()) + S.getName() + " **Lvl." + S.getLevel() + "** (" +  S.getPower() + ") `[" + S.getCost() + " TP]`\n";
                    } else if (SM instanceof Talent T) {
                        moveset = moveset + BotEmoji.get("SKILL") + " " + T.getName() + "\n";
                    }
                }
                moveset = moveset + "\n";
                if (P.getSummon() != null) {
                    moveset = moveset + BotEmoji.get(P.getSummon().getType()) + BotEmoji.get(P.getSummon().getElement().getName()) + P.getSummon().getName() + "\n";
                }
                E.addField("Moveset:", moveset, false);

                String eqp = "";
                if (P.getBoots() != null) eqp = eqp + ":athletic_shoe: " + P.getBoots().getName() + "\n";
                if (P.getPendant() != null) eqp = eqp + ":grinning:  " + P.getPendant().getName() + "\n";
                if (P.getBracelet() != null) eqp = eqp + ":hand_splayed: " + P.getBracelet().getName() + "\n";
                if (P.getGloves() != null) eqp = eqp + ":gloves: " + P.getGloves().getName();

                if (eqp.length() > 1) E.addField("Equipments", eqp, false);

                M.editOriginalEmbeds(E.build()).setFiles(FileUpload.fromData(P.getImage(), "avatar.png")
                        , FileUpload.fromData(P.ExportToNFFMFile(new File(TempDirectory + "/" + takeOnlyLetters(P.getName()) + ".INZ4")),  P.getName() + ".INZ4")
                ).setReplace(true).queue();
            } else {
                M.editOriginal(TL(M,"script-fail-3")).queue();
            }
        } catch (IOException e) {
            replyException(M, e);
        }
    }

    public static void LibraryGO1(SlashCommandInteractionEvent event, InteractionHook M) {
        if (event.getOption("type").getAsString().equals("Player")) {
            ViewPlayer(M, GO1Utils.getPlayersByName(event.getOption("name").getAsString()));
        } else if (event.getOption("type").getAsString().equals("Move")) {
            ViewMove(M, java.util.List.of((GOSpecialMove) GO1Utils.getMoveByName(event.getOption("name").getAsString())));
        } else if (event.getOption("type").getAsString().equals("Spirit")) {
            ViewSpirit(M, java.util.List.of(GO1Utils.getSpiritByName(event.getOption("name").getAsString())));
        }
    }
    public static void ViewPlayer(InteractionHook M, java.util.List<GOPlayer> Players) {
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
                        if (SM instanceof GOSpecialMove S) {
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
    public static void ViewMove(InteractionHook M, java.util.List<GOSpecialMove> Moves) {
        try {
            if (!Moves.isEmpty()) {
                java.util.List<MessageEmbed> Es = new ArrayList<>();
                java.util.List<FileUpload> FU = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    GOSpecialMove S = Moves.get(i);
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
                    E.addField("TP Cost:", "**• " + S.getCost() + "**", true);
                    E.addField("Difficulty:", "**• " + S.getDifficulty() + "**", true);
                    E.addField("Max Level:", "**• " + S.getMaxLevel() + "**", true);

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
    public static void ViewSpirit(InteractionHook M, java.util.List<GOSpirit> Spirits) {
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
}
