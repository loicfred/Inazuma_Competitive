package iecompbot.interaction.listeners.calcs;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StrikersXtr {

    public static void Xtreme_LoadCommands(SlashCommandInteractionEvent event) {

//        if (event.getName().contains("valcalc-xtr")) {
//            event.deferReply().queue(M -> {
//                String dueltype = event.getOption("duel-type").getAsString();
//                int stat = event.getOption("player-stat").getAsInt();
//                Element Element = ie.enums.Element.get(event.getOption("player-element").getAsString());
//                Element MoveElement = ie.enums.Element.get(event.getOption("player-move-element").getAsString());
//                int movepower = 0;
//                boolean isKeshinArmed1 = false;
//                boolean isKeshinArmed2 = false;
//                String MoveName1 = TL(M,"Move") + " \\(" + TL(M,"Power") + "\\)";
//                String MoveName2 = TL(M,"Move") + " \\(" + TL(M,"Power") + "\\)";
//                if (isNumeric(event.getOption("player-move").getAsString())) {
//                    movepower = event.getOption("player-move").getAsInt();
//                } else {
//                    if (dueltype.equals(DuelType.SHOT_CATCH)) {
//                        Shot MM = XtremeUtils.getShotByName(event.getOption("player-move").getAsString());
//                        movepower = MM.getTruePower();
//                        isKeshinArmed1 = (MM.isKeshin() || MM.isArmed());
//                        MoveName1 = MM.getName();
//                        MoveElement = MM.getElement();
//                    } else if (dueltype.equals(DuelType.CATCH_SHOT)) {
//                        Catch MM = XtremeUtils.getCatchByName(event.getOption("player-move").getAsString());
//                        movepower = MM.getTruePower();
//                        isKeshinArmed1 = (MM.isKeshin() || MM.isArmed());
//                        MoveName1 = MM.getName();
//                        MoveElement = MM.getElement();
//                    } else if (dueltype.equals(DuelType.DRIBBLE_BLOCK)) {
//                        Dribble MM = XtremeUtils.getDribbleByName(event.getOption("player-move").getAsString());
//                        movepower = MM.getTruePower();
//                        isKeshinArmed1 = (MM.isKeshin() || MM.isArmed());
//                        MoveName1 = MM.getName();
//                        MoveElement = MM.getElement();
//                    } else if (dueltype.equals(DuelType.BLOCK_DRIBBLE)) {
//                        Block MM = XtremeUtils.getBlockByName(event.getOption("player-move").getAsString());
//                        movepower = MM.getTruePower();
//                        isKeshinArmed1 = (MM.isKeshin() || MM.isArmed());
//                        MoveName1 = MM.getName();
//                        MoveElement = MM.getElement();
//                    }
//                }
//
//                int stat2 = 0;
//                Element Element2 = ie.enums.Element.VOID;
//                int movepower2 = 0;
//                Element MoveElement2 = ie.enums.Element.VOID;
//                if (event.getOption("opponent-stat") != null) {
//                    stat2 = event.getOption("opponent-stat").getAsInt();
//                }
//                if (event.getOption("opponent-element") != null) {
//                    Element2 = ie.enums.Element.get(event.getOption("opponent-element").getAsString());
//                }
//                if (event.getOption("opponent-move-element") != null) {
//                    MoveElement2 = ie.enums.Element.get(event.getOption("opponent-move-element").getAsString());
//                }
//                if (event.getOption("opponent-move") != null) {
//                    if (isNumeric(event.getOption("opponent-move").getAsString())) {
//                        movepower2 = event.getOption("opponent-move").getAsInt();
//                    } else {
//                        if (dueltype.equals(DuelType.SHOT_CATCH)) {
//                            Catch MM = XtremeUtils.getCatchByName(event.getOption("opponent-move").getAsString());
//                            movepower2 = MM.getTruePower();
//                            isKeshinArmed2 = (MM.isKeshin() || MM.isArmed());
//                            MoveName2 = MM.getName();
//                            MoveElement2 = MM.getElement();
//                        } else if (dueltype.equals(DuelType.CATCH_SHOT)) {
//                            Shot MM = XtremeUtils.getShotByName(event.getOption("opponent-move").getAsString());
//                            movepower2 = MM.getTruePower();
//                            isKeshinArmed2 = (MM.isKeshin() || MM.isArmed());
//                            MoveName2 = MM.getName();
//                            MoveElement2 = MM.getElement();
//                        } else if (dueltype.equals(DuelType.DRIBBLE_BLOCK)) {
//                            Block MM = XtremeUtils.getBlockByName(event.getOption("opponent-move").getAsString());
//                            movepower2 = MM.getTruePower();
//                            isKeshinArmed2 = (MM.isKeshin() || MM.isArmed());
//                            MoveName2 = MM.getName();
//                            MoveElement2 = MM.getElement();
//                        } else if (dueltype.equals(DuelType.BLOCK_DRIBBLE)) {
//                            Dribble MM = XtremeUtils.getDribbleByName(event.getOption("opponent-move").getAsString());
//                            movepower2 = MM.getTruePower();
//                            isKeshinArmed2 = (MM.isKeshin() || MM.isArmed());
//                            MoveName2 = MM.getName();
//                            MoveElement2 = MM.getElement();
//                        }
//                    }
//                }
//
//                String k1 = TL(M,"None");
//                String k2 = TL(M,"None");
//                Key key1 = null;
//                Key key2 = null;
//                if (event.getOption("player-keys") != null) {
//                    key1 = new Key(event.getOption("player-keys").getAsString()
//                            , takeOnlyInts(event.getOption("player-keys").getAsString()));
//                    k1 = event.getOption("player-keys").getAsString();
//                }
//                if (event.getOption("opponent-keys") != null) {
//                    key2 = new Key(event.getOption("opponent-keys").getAsString()
//                            , takeOnlyInts(event.getOption("opponent-keys").getAsString()));
//                    k2 = event.getOption("opponent-keys").getAsString();
//                }
//                List<Shot> S = new ArrayList<>();
//                S.add(new Shot("", "", "", "", 0, 0, movepower, 0, MoveElement,
//                        0, 0, false, false, false, false, false, false, false, false));
//                List<Dribble> D = new ArrayList<>();
//                D.add(new Dribble("", "", "", "", 0, 0, movepower, 0, MoveElement,
//                        0, 0, false, false, false, false, false));
//                List<Block> B = new ArrayList<>();
//                B.add(new Block("", "", "", "", 0, 0, movepower, 0, MoveElement,
//                        0, 0, false, false, false, false, false, false));
//                List<Catch> C = new ArrayList<>();
//                C.add(new Catch("", "", "", "", 0, 0, movepower, 0, MoveElement,
//                        0, 0, false, false, false, false, false, false, false));
//                Player Init = new Player("Initiator", "", "", "", MoveElement,
//                        ie.enums.Gender.Male, "", 200, stat, stat, stat, stat, stat, stat, "", "", 0
//                        , "", "", "", S, D, B, C);
//
//
//
//
//                S = new ArrayList<>();
//                S.add(new Shot("", "", "", "", 0, 0, movepower2, 0, MoveElement2,
//                        0, 0, false, false, false, false, false, false, false, false));
//                D = new ArrayList<>();
//                D.add(new Dribble("", "", "", "", 0, 0, movepower2, 0, MoveElement2,
//                        0, 0, false, false, false, false, false));
//                B = new ArrayList<>();
//                B.add(new Block("", "", "", "", 0, 0, movepower2, 0, MoveElement2,
//                        0, 0, false, false, false, false, false, false));
//                C = new ArrayList<>();
//                C.add(new Catch("", "", "", "", 0, 0, movepower2, 0, MoveElement2,
//                        0, 0, false, false, false, false, false, false, false));
//                Player Opponent = new Player("Opponent", "", "", "", MoveElement2,
//                        ie.enums.Gender.Male, "", 200, stat2, stat2, stat2, stat2, stat2, stat2, "", "", 0
//                        , "", "", "", S, D, B, C);
//
//
//
//                DuelBuilder BUILDER = new DuelBuilder(Init, Opponent, dueltype);
//                BUILDER.setArmedOrSummon1(isKeshinArmed1);
//                BUILDER.setArmedOrSummon1(isKeshinArmed2);
//                BUILDER.setInitiatorKeys(key1, null);
//                BUILDER.setOpponentKeys(key2, null);
//                try {
//                    EmbedBuilder Embed = new EmbedBuilder();
//                    Embed.setTitle(TL(M,"calculator-of-game", Game.get("IEGOSTRXTR").getName()));
//                    Embed.setThumbnail(Game.get("IEGOSTRXTR").getImageUrl());
//                    Embed.setColor(Game.get("IEGOSTRXTR").getColor());
//                    switch (dueltype) {
//                        case DuelType.SHOT_CATCH -> {
//                            Embed.addField(BotEmoji.get(Element.getName()) + TL(M,"Kick"), stat + "", true);
//                            Embed.addField(BotEmoji.get(MoveElement.getName()) + MoveName1, movepower + "", true);
//                            Embed.addField(":key: Keys", k1, true);
//                            Embed.addField(BotEmoji.get(Element2.getName()) + TL(M,"Catch"), stat2 + " \\(" + TL(M,"Opponent") + "\\)", true);
//                            Embed.addField(BotEmoji.get(MoveElement2.getName()) + MoveName2, movepower2 + " \\(" + TL(M,"Opponent") + "\\)", true);
//                            Embed.addField(":key: Keys (Opp)", k2, true);
//                            Duel Duel = BUILDER.build(Init.getShots().getFirst(), Opponent.getCatchs().getFirst());
//
//                            String Calc1 = "`(" + stat + " + " + Duel.getKeyBonus(Init, Stats.KICK) + ") / 2` (Kick + Key Bonus)\n"
//                                    + "`+" + movepower + "` (Move Power)\n"
//                                    + (!isKeshinArmed1 ? "`+20` (Hold)\n" : "`+0` (Hold) (Armed/Keshin)\n")
//                                    + "`+19` (Random Power)\n";
//                            int PowerCalculated = Duel.getTotalKick(Init);
//                            String Calc2 = "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + movepower;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + (!isKeshinArmed1 ? 20 : 0);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + 19;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            Embed.addField("Calculation (P1)", Calc1, true);
//                            Embed.addField(TL(M,"Total"), Calc2, true);
//
//
//                            Embed.addField(TL(M,"Total"), (Duel.CalculateShotValue(Init) - 19) + " / " + Duel.CalculateShotValue(Init) + " (RNG) "
//                                    + BotEmoji.get("VS2") + (Duel.CalculateCatchValue(Opponent) - 19) + " / " + Duel.CalculateCatchValue(Opponent) + " (RNG)", false);
//
//
//                            Calc1 = "`(" + stat2 + " + " + Duel.getKeyBonus(Opponent, Stats.CATCH) + ") / 2` (Catch + Key Bonus)\n"
//                                    + "`+" + movepower2 + "` (Move Power)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getGKElementAvantage(Opponent)) + "` (Init Element Adv/Disadv)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getGKElementMoveAvantage(Opponent)) + "` (Init Move Adv/Disadv)\n"
//                                    + "`+20` (Flame)\n"
//                                    + "`+19` (Random Power)\n";
//                            PowerCalculated = Duel.getTotalCatch(Opponent);
//                            Calc2 = "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + movepower2;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getGKElementAvantage(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getGKElementMoveAvantage(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + 20;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + 19;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            Embed.addField("Calculation (P2)", Calc1, true);
//                            Embed.addField(TL(M,"Total"), Calc2, true);
//
//                            M.editOriginalEmbeds(Embed.build()).queue();
//
//                            try {
//                                if (!MoveName1.contains(TL(M,"Move") + " \\(" + TL(M,"Power") + "\\)")) {
//                                    String s = "";
//                                    List<String> exclude = new ArrayList<>();
//                                    for (int i = 0; i != 5; i++) {
//                                        Player P = XtremeUtils.filterPlayers("[P1]Kick, Player " + Element + ", Move " + MoveElement + ",[Incl]" + MoveName1 + ",[Min]Kick" + stat, exclude);
//                                        if (P != null) {
//                                            exclude.add(P.getName());
//                                            s = s + "- " + P.getName() + "\n";
//                                        }
//                                    }
//                                    if (s.length() > 1) {
//                                        Embed.addField(TL(M,"Recommended"), s, false);
//                                    }
//                                } else {
//                                    String s = "";
//                                    List<String> exclude = new ArrayList<>();
//                                    for (int i = 0; i != 5; i++) {
//                                        Player P = XtremeUtils.filterPlayers("[P1]Kick, Player " + Element + ", Move " + MoveElement + ",[Min]Move" + movepower, exclude);
//                                        if (P != null) {
//                                            exclude.add(P.getName());
//                                            s = s + "- " + P.getName() + "\n";
//                                        }
//                                    }
//                                    if (s.length() > 1) {
//                                        Embed.addField(TL(M,"Recommended"), s, false);
//                                    }
//                                }
//                            } catch (IntruderPresenceException | DuplicateEntityException ignored) {}
//                        }
//                        case DuelType.CATCH_SHOT -> {
//                            Embed.addField(BotEmoji.get(Element.getName()) + TL(M,"Catch"), stat + "", true);
//                            Embed.addField(BotEmoji.get(MoveElement.getName()) + MoveName1, movepower + "", true);
//                            Embed.addField(":key: Keys", k1, true);
//                            Embed.addField(BotEmoji.get(Element2.getName()) + TL(M,"Kick"), stat2 + " \\(" + TL(M,"Opponent") + "\\)", true);
//                            Embed.addField(BotEmoji.get(MoveElement2.getName()) + MoveName2, movepower2 + " \\(" + TL(M,"Opponent") + "\\)", true);
//                            Embed.addField(":key: Keys (Opp)", k2, true);
//                            Duel Duel = BUILDER.build(Init.getCatchs().getFirst(), Opponent.getShots().getFirst());
//
//                            String Calc1 = "`(" + stat + " + " + Duel.getKeyBonus(Init, Stats.CATCH) + ") / 2` (Catch + Key Bonus)\n"
//                                    + "`+" + movepower + "` (Move Power)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getGKElementAvantage(Init)) + "` (Init Element Adv/Disadv)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getGKElementMoveAvantage(Init)) + "` (Init Move Adv/Disadv)\n"
//                                    + "`+20` (Flame)\n"
//                                    + "`+19` (Random Power)\n";
//                            int PowerCalculated = Duel.getTotalCatch(Init);
//                            String Calc2 = "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + movepower;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getGKElementAvantage(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getGKElementMoveAvantage(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + 20;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + 19;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            Embed.addField("Calculation (P1)", Calc1, true);
//                            Embed.addField(TL(M,"Total"), Calc2, true);
//
//
//                            Embed.addField(TL(M,"Total"), (Duel.CalculateCatchValue(Init) - 19) + " / " + Duel.CalculateCatchValue(Init) + " (RNG) "
//                                    + BotEmoji.get("VS2") + (Duel.CalculateShotValue(Opponent) - 19) + " / " + Duel.CalculateShotValue(Opponent) + " (RNG)", true);
//
//
//                            Calc1 = "`(" + stat2 + " + " + Duel.getKeyBonus(Opponent, Stats.KICK) + ") / 2` (Kick + Key Bonus)\n"
//                                    + "`+" + movepower2 + "` (Move Power)\n"
//                                    + (!isKeshinArmed2 ? "`+20` (Hold)\n" : "`+0` (Hold) (Armed/Keshin)\n")
//                                    + "`+19` (Random Power)\n";
//                            PowerCalculated = Duel.getTotalKick(Opponent);
//                            Calc2 = "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + movepower2;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + (!isKeshinArmed2 ? 20 : 0);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + 19;
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            Embed.addField("Calculation (P2)", Calc1, true);
//                            Embed.addField(TL(M,"Total"), Calc2, true);
//                            M.editOriginalEmbeds(Embed.build()).queue();
//
//                            try {
//                                if (!MoveName1.contains(TL(M,"Move") + " \\(" + TL(M,"Power") + "\\)")) {
//                                    String s = "";
//                                    List<String> exclude = new ArrayList<>();
//                                    for (int i = 0; i != 5; i++) {
//                                        Player P = XtremeUtils.filterPlayers("[P1]Catch, Player " + Element + ", Move " + MoveElement + ",[Incl]" + MoveName1 + ",[Min]Catch" + stat, exclude);
//                                        if (P != null) {
//                                            exclude.add(P.getName());
//                                            s = s + "- " + P.getName() + "\n";
//                                        }
//                                    }
//                                    if (s.length() > 1) {
//                                        Embed.addField(TL(M,"Recommended"), s, false);
//                                    }
//                                } else {
//                                    String s = "";
//                                    List<String> exclude = new ArrayList<>();
//                                    for (int i = 0; i != 5; i++) {
//                                        Player P = XtremeUtils.filterPlayers("[P1]Catch, Player " + Element + ", Move " + MoveElement + ",[Min]Move" + movepower, exclude);
//                                        if (P != null) {
//                                            exclude.add(P.getName());
//                                            s = s + "- " + P.getName() + "\n";
//                                        }
//                                    }
//                                    if (s.length() > 1) {
//                                        Embed.addField(TL(M,"Recommended"), s, false);
//                                    }
//                                }
//                            } catch (IntruderPresenceException | DuplicateEntityException ignored) { }
//                        }
//                        case DuelType.DRIBBLE_BLOCK -> {
//                            Embed.addField(BotEmoji.get(Element.getName()) + TL(M,"Dribble"), stat + "", true);
//                            Embed.addField(BotEmoji.get(MoveElement.getName()) + MoveName1, movepower + "", true);
//                            Embed.addField(":key: Keys", k1, true);
//                            Embed.addField(BotEmoji.get(Element2.getName()) + TL(M,"Block"), stat2 + " \\(" + TL(M,"Opponent") + "\\)", true);
//                            Embed.addField(BotEmoji.get(MoveElement2.getName()) + MoveName2, movepower2 + " \\(" + TL(M,"Opponent") + "\\)", true);
//                            Embed.addField(":key: Keys (Opp)", k2, true);
//                            Duel Duel = BUILDER.build(Init.getDribbles().getFirst(), Opponent.getBlocks().getFirst());
//                            String Calc1 = "`" + Duel.getInitiatorMove().getTier() + " - " + Duel.getOpponentMove().getTier() + "` (Init M.Tier - Opp M.Tier)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFStatDifference(Init)) + "` (Diff Init Body/Opp Guard)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFMovePowerDifference(Init)) + "` (Diff Init Move/Opp Move)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFElementAdvantage(Init)) + "` (Init Element Adv/Disadv)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFElementMoveAdvantage(Init)) + "` (Init Move Adv/Disadv)\n";
//                            int PowerCalculated = Duel.getMFDFTierDifference(Init);
//                            String Calc2 = "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFStatDifference(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFMovePowerDifference(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFElementAdvantage(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFElementMoveAdvantage(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            Embed.addField("Calculation (P1)", Calc1, true);
//                            Embed.addField(TL(M,"Total"), Calc2, true);
//
//                            if (Duel.getDuelWinner().getName().equals(Init.getName())) {
//                                Embed.addField(TL(M,"Total"), "Player 1 win the duel.", false);
//                            } else {
//                                Embed.addField(TL(M,"Total"), "Opponent win the duel.", false);
//                            }
//
//                            Calc1 = "`" + Duel.getOpponentMove().getTier() + " - " + Duel.getInitiatorMove().getTier() + "` (Opp M Tier - Init.M.Tier)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFStatDifference(Opponent)) + "` (Diff Opp Guard/Init Body)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFMovePowerDifference(Opponent)) + "` (Diff Opp Move/Init Move)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFElementAdvantage(Opponent)) + "` (Opp Element Adv/Disadv)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFElementMoveAdvantage(Opponent)) + "` (Opp Move Adv/Disadv)\n";
//                            PowerCalculated = Duel.getMFDFTierDifference(Opponent);
//                            Calc2 = "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFStatDifference(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFMovePowerDifference(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFElementAdvantage(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFElementMoveAdvantage(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            Embed.addField("Calculation (P2)", Calc1, true);
//                            Embed.addField(TL(M,"Total"), Calc2, true);
//                            M.editOriginalEmbeds(Embed.build()).queue();
//
//                            try {
//                                if (!MoveName1.contains(TL(M,"Move") + " \\(" + TL(M,"Power") + "\\)")) {
//                                    String s = "";
//                                    List<String> exclude = new ArrayList<>();
//                                    for (int i = 0; i != 5; i++) {
//                                        Player P = XtremeUtils.filterPlayers("[P1]Body, Player " + Element + ", Move " + MoveElement + ",[Incl]" + MoveName1 + ",[Min]Body" + stat, exclude);
//                                        if (P != null) {
//                                            exclude.add(P.getName());
//                                            s = s + "- " + P.getName() + "\n";
//                                        }
//                                    }
//                                    if (s.length() > 1) {
//                                        Embed.addField(TL(M,"Recommended"), s, false);
//                                    }
//                                } else {
//                                    String s = "";
//                                    List<String> exclude = new ArrayList<>();
//                                    for (int i = 0; i != 5; i++) {
//                                        Player P = XtremeUtils.filterPlayers("[P1]Body, Player " + Element + ", Move " + MoveElement + ",[Min]Move" + movepower, exclude);
//                                        if (P != null) {
//                                            exclude.add(P.getName());
//                                            s = s + "- " + P.getName() + "\n";
//                                        }
//                                    }
//                                    if (s.length() > 1) {
//                                        Embed.addField(TL(M,"Recommended"), s, false);
//                                    }
//                                }
//                            } catch (IntruderPresenceException | DuplicateEntityException ignored) {}
//                        }
//                        case DuelType.BLOCK_DRIBBLE -> {
//                            Embed.addField(BotEmoji.get(Element.getName()) + TL(M,"Block"), stat + "", true);
//                            Embed.addField(BotEmoji.get(MoveElement.getName()) + MoveName1, movepower + "", true);
//                            Embed.addField(":key: Keys", k1, true);
//                            Embed.addField(BotEmoji.get(Element.getName()) + TL(M,"Dribble"), stat2 + " \\(" + TL(M,"Opponent") + "\\)", true);
//                            Embed.addField(BotEmoji.get(MoveElement2.getName()) + MoveName2, movepower2 + " \\(" + TL(M,"Opponent") + "\\)", true);
//                            Embed.addField(":key: Keys (Opp)", k2, true);
//                            Duel Duel = BUILDER.build(Init.getBlocks().getFirst(), Opponent.getDribbles().getFirst());
//                            String Calc1 = "`" + Duel.getInitiatorMove().getTier() + " - " + Duel.getOpponentMove().getTier() + "` (Init M.Tier - Opp M.Tier)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFStatDifference(Init)) + "` (Diff Init Guard/Opp Body)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFMovePowerDifference(Init)) + "` (Diff Init Move/Opp Move)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFElementAdvantage(Init))+ "` (Init Element Adv/Disadv)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFElementMoveAdvantage(Init)) + "` (Init Move Adv/Disadv)\n";
//                            int PowerCalculated = Duel.getMFDFTierDifference(Init);
//                            String Calc2 = "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFStatDifference(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFMovePowerDifference(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFElementAdvantage(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFElementMoveAdvantage(Init);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            Embed.addField("Calculation (P1)", Calc1, true);
//                            Embed.addField(TL(M,"Total"), Calc2, true);
//
//                            if (Duel.getDuelWinner().getName().equals(Init.getName())) {
//                                Embed.addField(TL(M,"Total"), "Player 1 win the duel.", false);
//                            } else {
//                                Embed.addField(TL(M,"Total"), "Opponent win the duel.", false);
//                            }
//
//                            Calc1 = "`" + Duel.getOpponentMove().getTier() + " - " + Duel.getInitiatorMove().getTier() + "` (Opp M Tier - Init.M.Tier)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFStatDifference(Opponent)) + "` (Diff Opp Body/Init Guard)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFMovePowerDifference(Opponent)) + "` (Diff Opp Move/Init Move)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFElementAdvantage(Opponent)) + "` (Opp Element Adv/Disadv)\n"
//                                    + "`" + PlusMinusSignWithNum(Duel.getMFDFElementMoveAdvantage(Opponent)) + "` (Opp Move Adv/Disadv)\n";
//                            PowerCalculated = Duel.getMFDFTierDifference(Opponent);
//                            Calc2 = "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFStatDifference(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFMovePowerDifference(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFElementAdvantage(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            PowerCalculated = PowerCalculated + Duel.getMFDFElementMoveAdvantage(Opponent);
//                            Calc2 = Calc2 + "`" + PowerCalculated + "`\n";
//                            Embed.addField("Calculation (P2)", Calc1, true);
//                            Embed.addField(TL(M,"Total"), Calc2, true);
//                            M.editOriginalEmbeds(Embed.build()).queue();
//
//                            try {
//                                if (!MoveName1.contains(TL(M,"Move") + " \\(" + TL(M,"Power") + "\\)")) {
//                                    String s = "";
//                                    List<String> exclude = new ArrayList<>();
//                                    for (int i = 0; i != 5; i++) {
//                                        Player P = XtremeUtils.filterPlayers("[P1]Guard, Player " + Element + ", Move " + MoveElement + ",[Incl]" + MoveName1 + ",[Min]Guard" + stat, exclude);
//                                        if (P != null) {
//                                            exclude.add(P.getName());
//                                            s = s + "- " + P.getName() + "\n";
//                                        }
//                                    }
//                                    if (s.length() > 1) {
//                                        Embed.addField(TL(M,"Recommended"), s, false);
//                                    }
//                                } else {
//                                    String s = "";
//                                    List<String> exclude = new ArrayList<>();
//                                    for (int i = 0; i != 5; i++) {
//                                        Player P = XtremeUtils.filterPlayers("[P1]Guard, Player " + Element + ", Move " + MoveElement + ",[Min]Move" + movepower, exclude);
//                                        if (P != null) {
//                                            exclude.add(P.getName());
//                                            s = s + "- " + P.getName() + "\n";
//                                        }
//                                    }
//                                    if (s.length() > 1) {
//                                        Embed.addField(TL(M,"Recommended"), s, false);
//                                    }
//                                }
//                            } catch (IntruderPresenceException | DuplicateEntityException ignored) {}
//                        }
//                    }
//                    M.editOriginalEmbeds(Embed.build()).queue();
//                } catch (EntityNotFoundException | WrongTypeException e) {
//                    Automation.handleException(e);
//                }
//            });
//        }

    }

//    public static void LibraryXtr(SlashCommandInteractionEvent event, String name, InteractionHook M) throws IOException {
//        if (event.getSubcommandGroup().equals("xtr")) {
//            if (event.getSubcommandName().equals("players")) {
//                ie.strikers.object.Player P = XtremeUtils.getPlayerByName(name);
//                if (P != null) {
//                    EmbedBuilder embedBuilder = new EmbedBuilder();
//                    embedBuilder.setTitle(P.getName());
//                    embedBuilder.setColor((
//                            P.getElement().equals(Element.FIRE) ? Color.decode("#b42002") :
//                                    P.getElement().equals(Element.WIND) ? Color.decode("#45c9e7") :
//                                            P.getElement().equals(Element.WOOD) ? Color.decode("#2dc457") :
//                                                    P.getElement().equals(Element.EARTH) ? Color.decode("#e19b18") :
//                                                            Color.decode("#bb0077")));
//                    embedBuilder.addField(BotEmoji.get(P.getElement().getName()) + "Element:", "**• " + P.getElement() + "**", true);
//                    embedBuilder.addField(BotEmoji.get(P.getPosition()) + "Position:", "**• " + P.getPosition() + "**", true);
//                    embedBuilder.addField(BotEmoji.get(P.getGender().getName()) + "Gender:", "**• " + P.getGender() + "**", true);
//                    embedBuilder.addField("Stats:",
//                            "**" + TL(M,"TP") + ": **" + P.getTP() + "\n" +
//                                    "**" + TL(M,"Kick") + ": **" + P.getKick() + "\n" +
//                                    "**" + TL(M,"Body") + ": **" + P.getBody() + "\n" +
//                                    "**" + TL(M,"Guard") + ": **" + P.getGuard() + "\n" +
//                                    "**" + TL(M,"Catch") + ": **" + P.getCatch() + "\n" +
//                                    "**" + TL(M,"Control") + ": **" + P.getControl() + "\n" +
//                                    "**" + TL(M,"Speed") + ": **" + P.getSpeed()
//                            , true);
//
//                    embedBuilder.addField("`                                                        `", " ", false);
//
//                    embedBuilder.addField("Charge Profile: ", P.getChargeProfile(), true);
//                    embedBuilder.addField("Key Bonus: ", P.getKeyBonus(), true);
//                    embedBuilder.addField("Tactical Action: ", P.getTacticalAction(), true);
//                    embedBuilder.addField("Charge Time (with Ball): ", P.getChargeTimeBall(), true);
//                    embedBuilder.addField("Charge Time: ", P.getChargeTimeNoBall(), true);
//
//                    embedBuilder.addField("`                                                        `", " ", false);
//
//                    String s1 = "";
//                    String s12 = "";
//                    String s2 = "";
//                    String s22 = "";
//                    String s3 = "";
//                    String s32 = "";
//                    for (ie.strikers.object.moves.Shot SS : P.getShots()) {
//                        if (SS != null) {
//                            if (SS.getTier() == 1) {
//                                if (s1.length() < 920) {
//                                    s1 = s1 + "**• " + BotEmoji.get("ShotMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isChain() ? "(C)" : SS.isLongShoot() ? "(L)" : SS.isCounterShoot() ? "(B)" : "") + "\n";
//                                } else {
//                                    s12 = s12 + "**• " + BotEmoji.get("ShotMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isChain() ? "(C)" : SS.isLongShoot() ? "(L)" : SS.isCounterShoot() ? "(B)" : "") + "\n";
//                                }
//                            } else if (SS.getTier() == 2) {
//                                if (s2.length() < 920) {
//                                    s2 = s2 + "**• " + BotEmoji.get("ShotMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isChain() ? "(C)" : SS.isLongShoot() ? "(L)" : SS.isCounterShoot() ? "(B)" : "") + "\n";
//                                } else {
//                                    s22 = s22 + "**• " + BotEmoji.get("ShotMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isChain() ? "(C)" : SS.isLongShoot() ? "(L)" : SS.isCounterShoot() ? "(B)" : "") + "\n";
//                                }
//                            } else if (SS.getTier() == 3) {
//                                if (s3.length() < 920) {
//                                    s3 = s3 + "**• " + BotEmoji.get("ShotMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isChain() ? "(C)" : SS.isLongShoot() ? "(L)" : SS.isCounterShoot() ? "(B)" : "") + "\n";
//                                } else {
//                                    s32 = s32 + "**• " + BotEmoji.get("ShotMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isChain() ? "(C)" : SS.isLongShoot() ? "(L)" : SS.isCounterShoot() ? "(B)" : "") + "\n";
//                                }
//                            }
//                        }
//                    }
//                    if (s1.length() > 1 || s2.length() > 1 || s3.length() > 1) {
//                        embedBuilder.addField("__All Shots__", " ", false);
//                        if (s3.length() > 1) {
//                            embedBuilder.addField("Tier 3", s3, true);
//                        }
//                        if (s2.length() > 1) {
//                            embedBuilder.addField("Tier 2", s2, true);
//                        }
//                        if (s1.length() > 1) {
//                            embedBuilder.addField("Tier 1", s1, true);
//                        }
//                        if (s32.length() > 1) {
//                            embedBuilder.addField("Tier 3 (Part 2)", s32, false);
//                        }
//                        if (s22.length() > 1) {
//                            embedBuilder.addField("Tier 2 (Part 2)", s22, false);
//                        }
//                        if (s12.length() > 1) {
//                            embedBuilder.addField("Tier 1 (Part 2)", s12, false);
//                        }
//                    }
//                    s1 = "";
//                    s12 = "";
//                    s2 = "";
//                    s22 = "";
//                    s3 = "";
//                    s32 = "";
//                    for (ie.strikers.object.moves.Dribble SS : P.getDribbles()) {
//                        if (SS != null) {
//                            if (SS.getTier() == 1) {
//                                if (s1.length() < 920) {
//                                    s1 = s1 + "**• " + BotEmoji.get("DribbleMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + "\n";
//                                } else {
//                                    s12 = s12 + "**• " + BotEmoji.get("DribbleMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + "\n";
//                                }
//                            } else if (SS.getTier() == 2) {
//                                if (s2.length() < 920) {
//                                    s2 = s2 + "**• " + BotEmoji.get("DribbleMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + "\n";
//                                } else {
//                                    s22 = s22 + "**• " + BotEmoji.get("DribbleMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + "\n";
//                                }
//                            } else if (SS.getTier() == 3) {
//                                if (s3.length() < 920) {
//                                    s3 = s3 + "**• " + BotEmoji.get("DribbleMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + "\n";
//                                } else {
//                                    s32 = s32 + "**• " + BotEmoji.get("DribbleMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + "\n";
//                                }
//                            }
//                        }
//                    }
//                    if (s1.length() > 1 || s2.length() > 1 || s3.length() > 1) {
//                        embedBuilder.addField("__All Dribbles__", " ", false);
//                        if (s3.length() > 1) {
//                            embedBuilder.addField("Tier 3", s3, true);
//                        }
//                        if (s2.length() > 1) {
//                            embedBuilder.addField("Tier 2", s2, true);
//                        }
//                        if (s1.length() > 1) {
//                            embedBuilder.addField("Tier 1", s1, true);
//                        }
//                        if (s32.length() > 1) {
//                            embedBuilder.addField("Tier 3 (Part 2)", s32, false);
//                        }
//                        if (s22.length() > 1) {
//                            embedBuilder.addField("Tier 2 (Part 2)", s22, false);
//                        }
//                        if (s12.length() > 1) {
//                            embedBuilder.addField("Tier 1 (Part 2)", s12, false);
//                        }
//                    }
//                    s1 = "";
//                    s12 = "";
//                    s2 = "";
//                    s22 = "";
//                    s3 = "";
//                    s32 = "";
//                    for (ie.strikers.object.moves.Block SS : P.getBlocks()) {
//                        if (SS != null) {
//                            if (SS.getTier() == 1) {
//                                if (s1.length() < 920) {
//                                    s1 = s1 + "**• " + BotEmoji.get("BlockMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isShootBlock() ? "(B)" : "") + "\n";
//                                } else {
//                                    s12 = s12 + "**• " + BotEmoji.get("BlockMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isShootBlock() ? "(B)" : "") + "\n";
//                                }
//                            } else if (SS.getTier() == 2) {
//                                if (s2.length() < 920) {
//                                    s2 = s2 + "**• " + BotEmoji.get("BlockMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isShootBlock() ? "(B)" : "") + "\n";
//                                } else {
//                                    s22 = s22 + "**• " + BotEmoji.get("BlockMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isShootBlock() ? "(B)" : "") + "\n";
//                                }
//                            } else if (SS.getTier() == 3) {
//                                if (s3.length() < 920) {
//                                    s3 = s3 + "**• " + BotEmoji.get("BlockMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isShootBlock() ? "(B)" : "") + "\n";
//                                } else {
//                                    s32 = s32 + "**• " + BotEmoji.get("BlockMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isShootBlock() ? "(B)" : "") + "\n";
//                                }
//                            }
//                        }
//                    }
//                    if (s1.length() > 1 || s2.length() > 1 || s3.length() > 1) {
//                        embedBuilder.addField("__All Blocks__", " ", false);
//                        if (s3.length() > 1) {
//                            embedBuilder.addField("Tier 3", s3, true);
//                        }
//                        if (s2.length() > 1) {
//                            embedBuilder.addField("Tier 2", s2, true);
//                        }
//                        if (s1.length() > 1) {
//                            embedBuilder.addField("Tier 1", s1, true);
//                        }
//                        if (s32.length() > 1) {
//                            embedBuilder.addField("Tier 3 (Part 2)", s32, false);
//                        }
//                        if (s22.length() > 1) {
//                            embedBuilder.addField("Tier 2 (Part 2)", s22, false);
//                        }
//                        if (s12.length() > 1) {
//                            embedBuilder.addField("Tier 1 (Part 2)", s12, false);
//                        }
//                    }
//                    s1 = "";
//                    s12 = "";
//                    s2 = "";
//                    s22 = "";
//                    s3 = "";
//                    s32 = "";
//                    for (ie.strikers.object.moves.Catch SS : P.getCatchs()) {
//                        if (SS != null) {
//                            if (SS.getTier() == 1) {
//                                if (s1.length() < 920) {
//                                    s1 = s1 + "**• " + BotEmoji.get("SaveMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isPunch() ? "(P)" : "") + "\n";
//                                } else {
//                                    s12 = s12 + "**• " + BotEmoji.get("SaveMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isPunch() ? "(P)" : "") + "\n";
//                                }
//                            } else if (SS.getTier() == 2) {
//                                if (s2.length() < 920) {
//                                    s2 = s2 + "**• " + BotEmoji.get("SaveMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isPunch() ? "(P)" : "") + "\n";
//                                } else {
//                                    s22 = s22 + "**• " + BotEmoji.get("SaveMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isPunch() ? "(P)" : "") + "\n";
//                                }
//                            } else if (SS.getTier() == 3) {
//                                if (s3.length() < 920) {
//                                    s3 = s3 + "**• " + BotEmoji.get("SaveMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isPunch() ? "(P)" : "") + "\n";
//                                } else {
//                                    s32 = s32 + "**• " + BotEmoji.get("SaveMove") + BotEmoji.get(SS.getElement().getName()) + " **" + SS.getName() + " " + (SS.isPunch() ? "(P)" : "") + "\n";
//                                }
//                            }
//                        }
//                    }
//                    if (s1.length() > 1 || s2.length() > 1 || s3.length() > 1) {
//                        embedBuilder.addField("__All Catches__", " ", false);
//                        if (s3.length() > 1) {
//                            embedBuilder.addField("Tier 3", s3, true);
//                        }
//                        if (s2.length() > 1) {
//                            embedBuilder.addField("Tier 2", s2, true);
//                        }
//                        if (s1.length() > 1) {
//                            embedBuilder.addField("Tier 1", s1, true);
//                        }
//                        if (s32.length() > 1) {
//                            embedBuilder.addField("Tier 3 (Part 2)", s32, false);
//                        }
//                        if (s22.length() > 1) {
//                            embedBuilder.addField("Tier 2 (Part 2)", s22, false);
//                        }
//                        if (s12.length() > 1) {
//                            embedBuilder.addField("Tier 1 (Part 2)", s12, false);
//                        }
//                    }
//
//                    M.editOriginalEmbeds(embedBuilder.build()).queue();
//                } else {
//                    M.editOriginal("Can't find the player.").queue();
//                }
//            }
//            else if (event.getSubcommandName().equals("moves")) {
//                String type = event.getOption("type").getAsString();
//                if (type.equals("Shot")) {
//                    ie.strikers.object.moves.Shot move = XtremeUtils.getShotByName(name);
//                    if (move != null) {
//                        EmbedBuilder embedBuilder = new EmbedBuilder();
//                        embedBuilder.setTitle(move.getName());
//                        embedBuilder.setColor((
//                                move.getElement().equals(Element.FIRE) ? Color.decode("#b42002") :
//                                        move.getElement().equals(Element.WIND) ? Color.decode("#45c9e7") :
//                                                move.getElement().equals(Element.WOOD) ? Color.decode("#2dc457") :
//                                                        move.getElement().equals(Element.EARTH) ? Color.decode("#e19b18") :
//                                                                Color.decode("#bb0077")));
//                        embedBuilder.addField(BotEmoji.get(move.getElement().getName()) + "Element:", "**• " + move.getElement() + "**", true);
//                        embedBuilder.addField(BotEmoji.get("TP") + "Cost:",  "**• " + move.getTP() + "**", true);
//                        embedBuilder.addField(":up: Tier:", "**• " + move.getTier() + "** (**" + move.getRealTier() + ")**", true);
//                        embedBuilder.addField("Kakusei:", "**• " + move.getKakusei() + "**", true);
//                        embedBuilder.addField("Shot Type:",  "**• " + (move.isChain() ? "Chain" : move.isLongShoot() ? "Long Shoot"  : move.isCounterShoot() ? "Counter Shoot" : "Normal Shoot")+ "**", true);
//                        embedBuilder.addField("Move Is a:",  "**• " + (move.isDuo() ? "Duo Move" : move.isTrio() ? "Trio Move" : "Solo Move") + "**", true);
//                        embedBuilder.addField(":boom: True Power:", "**• " + move.getTruePower() + "**", false);
//                        embedBuilder.addField(":red_circle: Knockout Range:", "**• " + move.getKnockoutRange() + "**", true);
//                        embedBuilder.addField(":blue_circle: Range:", "**• " + move.getRange() + "**", true);
//                        embedBuilder.setThumbnail("attachment://move.png");
//
//                        M.editOriginalEmbeds(embedBuilder.build()).queue();
//
//                    } else {
//                        M.editOriginal("Can't find the move.").queue();
//                    }
//                }
//                else if (type.equals("Dribble")) {
//                    ie.strikers.object.moves.Dribble move = XtremeUtils.getDribbleByName(name);
//                    if (move != null) {
//                        EmbedBuilder embedBuilder = new EmbedBuilder();
//                        embedBuilder.setTitle(move.getName());
//                        embedBuilder.setColor((
//                                move.getElement().equals(Element.FIRE) ? Color.decode("#b42002") :
//                                        move.getElement().equals(Element.WIND) ? Color.decode("#45c9e7") :
//                                                move.getElement().equals(Element.WOOD) ? Color.decode("#2dc457") :
//                                                        move.getElement().equals(Element.EARTH) ? Color.decode("#e19b18") :
//                                                                Color.decode("#bb0077")));
//                        embedBuilder.addField(BotEmoji.get(move.getElement().getName()) + "Element:", "**• " + move.getElement() + "**", true);
//                        embedBuilder.addField(BotEmoji.get("TP") + "Cost:",  "**• " + move.getTP() + "**", true);
//                        embedBuilder.addField(":up: Tier:", "**• " + move.getTier() + "** (**" + move.getRealTier() + ")**", true);
//                        embedBuilder.addField("Kakusei:", "**• " + move.getKakusei() + "**", true);
//                        embedBuilder.addField("Shot Type:",  "**• Normal Dribble" + "**", true);
//                        embedBuilder.addField("Move Is a:",  "**• " + (move.isDuo() ? "Duo Move" : move.isTrio() ? "Trio Move" : "Solo Move") + "**", true);
//                        embedBuilder.addField(":boom: True Power:", "**• " + move.getTruePower() + "**", false);
//                        embedBuilder.addField(":red_circle: Knockout Range:", "**• " + move.getKnockoutRange() + "**", true);
//                        embedBuilder.addField(":blue_circle: Range:", "**• " + move.getRange() + "**", true);
//                        embedBuilder.setThumbnail("attachment://move.png");
//
//                        M.editOriginalEmbeds(embedBuilder.build()).queue();
//
//                    } else {
//                        M.editOriginal("Can't find the move.").queue();
//                    }
//                }
//                else if (type.equals("Block")) {
//                    ie.strikers.object.moves.Block move = XtremeUtils.getBlockByName(name);
//                    if (move != null) {
//                        EmbedBuilder embedBuilder = new EmbedBuilder();
//                        embedBuilder.setTitle(move.getName());
//                        embedBuilder.setColor((
//                                move.getElement().equals(Element.FIRE) ? Color.decode("#b42002") :
//                                        move.getElement().equals(Element.WIND) ? Color.decode("#45c9e7") :
//                                                move.getElement().equals(Element.WOOD) ? Color.decode("#2dc457") :
//                                                        move.getElement().equals(Element.EARTH) ? Color.decode("#e19b18") :
//                                                                Color.decode("#bb0077")));
//                        embedBuilder.addField(BotEmoji.get(move.getElement().getName()) + "Element:", "**• " + move.getElement() + "**", true);
//                        embedBuilder.addField(BotEmoji.get("TP") + "Cost:",  "**• " + move.getTP() + "**", true);
//                        embedBuilder.addField(":up: Tier:", "**• " + move.getTier() + "** (**" + move.getRealTier() + ")**", true);
//                        embedBuilder.addField("Kakusei:", "**• " + move.getKakusei() + "**", true);
//                        embedBuilder.addField("Shot Type:",  "**• " + (move.isShootBlock() ? "Shoot Block" : "Normal Block")+ "**", true);
//                        embedBuilder.addField("Move Is a:",  "**• " + (move.isDuo() ? "Duo Move" : move.isTrio() ? "Trio Move" : "Solo Move") + "**", true);
//                        embedBuilder.addField(":boom: True Power:", "**• " + move.getTruePower() + "**", false);
//                        embedBuilder.addField(":red_circle: Knockout Range:", "**• " + move.getKnockoutRange() + "**", true);
//                        embedBuilder.addField(":blue_circle: Range:", "**• " + move.getRange() + "**", true);
//                        embedBuilder.setThumbnail("attachment://move.png");
//
//                        M.editOriginalEmbeds(embedBuilder.build()).queue();
//                    } else {
//                        M.editOriginal("Can't find the move.").queue();
//                    }
//                }
//                else if (type.equals("Catch")) {
//                    ie.strikers.object.moves.Catch move = XtremeUtils.getCatchByName(name);
//                    if (move != null) {
//                        EmbedBuilder embedBuilder = new EmbedBuilder();
//                        embedBuilder.setTitle(move.getName());
//                        embedBuilder.setColor((
//                                move.getElement().equals(Element.FIRE) ? Color.decode("#b42002") :
//                                        move.getElement().equals(Element.WIND) ? Color.decode("#45c9e7") :
//                                                move.getElement().equals(Element.WOOD) ? Color.decode("#2dc457") :
//                                                        move.getElement().equals(Element.EARTH) ? Color.decode("#e19b18") :
//                                                                Color.decode("#bb0077")));
//                        embedBuilder.addField(BotEmoji.get(move.getElement().getName()) + "Element:", "**• " + move.getElement() + "**", true);
//                        embedBuilder.addField(BotEmoji.get("TP") + "Cost:",  "**• " + move.getTP() + "**", true);
//                        embedBuilder.addField(":up: Tier:", "**• " + move.getTier() + "** (**" + move.getRealTier() + ")**", true);
//                        embedBuilder.addField("Kakusei:", "**• " + move.getKakusei() + "**", true);
//                        embedBuilder.addField("Shot Type:",  "**• " + (move.isPunch() ? "Punch Catch" : "Normal Catch")+ "**", true);
//                        embedBuilder.addField("Move Is a:",  "**• " + (move.isDuo() ? "Duo Move" : move.isTrio() ? "Trio Move" : "Solo Move") + "**", true);
//                        embedBuilder.addField(":boom: True Power:", "**• " + move.getTruePower() + "**", false);
//                        embedBuilder.addField(":red_circle: Knockout Range:", "**• " + move.getKnockoutRange() + "**", true);
//                        embedBuilder.addField(":blue_circle: Range:", "**• " + move.getRange() + "**", true);
//                        embedBuilder.setThumbnail("attachment://move.png");
//
//                        M.editOriginalEmbeds(embedBuilder.build()).queue();
//
//                    } else {
//                        M.editOriginal("Can't find the move.").queue();
//                    }
//                }
//            }
//
//        }
//    }
//
//    public static void FilterXtr(SlashCommandInteractionEvent event, InteractionHook M) {
//        if (event.getName().contains("players")) {
//            String Script = "";
//            String prio = "";
//            String Mtype = "";
//            String Melement = "";
//            if (event.getOption("priority-stat") != null) {
//                Script = Script + "[P1]" + event.getOption("priority-stat").getAsString();
//                prio = event.getOption("priority-stat").getAsString();
//            }
//            if (event.getOption("player-element") != null) {
//                Script = Script + ", Player " + event.getOption("player-element").getAsString();
//            }
//            if (event.getOption("move-element") != null) {
//                Script = Script + ", Move " + event.getOption("move-element").getAsString();
//                Melement = event.getOption("move-element").getAsString();
//            }
//            if (event.getOption("move-type") != null) {
//                Script = Script + ", [MType] " + event.getOption("move-type").getAsString();
//                Mtype = event.getOption("move-type").getAsString();
//            }
//            if (event.getOption("gender") != null) {
//                Script = Script + ", " + event.getOption("gender").getAsString();
//            }
//            if (event.getOption("min-tp") != null) {
//                Script = Script + ", [Min] TP " + event.getOption("min-tp").getAsInt();
//            }
//            if (event.getOption("minimum-kick") != null) {
//                Script = Script + ", [Min]Kick " + event.getOption("minimum-kick").getAsInt();
//            }
//            if (event.getOption("minimum-body") != null) {
//                Script = Script + ", [Min]Body " + event.getOption("minimum-body").getAsInt();
//            }
//            if (event.getOption("minimum-control") != null) {
//                Script = Script + ", [Min]Control " + event.getOption("minimum-control").getAsInt();
//            }
//            if (event.getOption("minimum-guard") != null) {
//                Script = Script + ", [Min]Guard " + event.getOption("minimum-guard").getAsInt();
//            }
//            if (event.getOption("minimum-speed") != null) {
//                Script = Script + ", [Min]Speed " + event.getOption("minimum-speed").getAsInt();
//            }
//            if (event.getOption("minimum-catch") != null) {
//                Script = Script + ", [Min]Catch " + event.getOption("minimum-catch").getAsInt();
//            }
//            if (event.getOption("dash-type") != null) {
//                Script = Script + ", [Tact]" + event.getOption("dash-type").getAsString();
//            }
//            if (event.getOption("charge-time-with-ball") != null) {
//                Script = Script + ", [CB]" + event.getOption("charge-time-with-ball").getAsString();
//            }
//            if (event.getOption("charge-time-without-ball") != null) {
//                Script = Script + ", [CwB]" + event.getOption("charge-time-without-ball").getAsString();
//            }
//            //System.out.println(Script);
//            java.util.List<String> exclude = new ArrayList<>();
//            if (event.getOption("excluded-players") != null) {
//                String list = event.getOption("excluded-players").getAsString().toLowerCase()
//                        .replaceAll("     ", "")
//                        .replaceAll("    ", "")
//                        .replaceAll("   ", "")
//                        .replaceAll("  ", "")
//                        .replaceAll("  ", "")
//                        .replaceAll(", ", "")
//                        .replaceAll(" ,", "");
//                if (list.contains(",")) {
//                    java.util.List<String> list2 = java.util.List.of(list.split(","));
//                    for (String each : list2) {
//                        for (Player P : getPlayers()) {
//                            if (P.getName().toLowerCase().contains(each) || P.getNameJP().toLowerCase().contains(each) || P.getNameEU().toLowerCase().contains(each)) {
//                                exclude.add(P.getName());
//                            }
//                        }
//                    }
//                } else {
//                    for (Player P : getPlayers()) {
//                        if (P.getName().toLowerCase().contains(list) || P.getNameJP().toLowerCase().contains(list) || P.getNameEU().toLowerCase().contains(list)) {
//                            exclude.add(P.getName());
//                        }
//                    }
//                }
//            }
//            try {
//                java.util.List<MessageEmbed> embeds = new ArrayList<>();
//                List<FileUpload> uploads = new ArrayList<>();
//
//                for (int i = 0; i != 5; i++) {
//                    Player P = filterPlayers(Script, exclude);
//                    if (P != null) {
//                        exclude.add(P.getName());
//                        EmbedBuilder embedBuilder = new EmbedBuilder();
//                        embedBuilder.setTitle(P.getName());
//                        embedBuilder.setAuthor(P.getNameEU());
//                        embedBuilder.setColor((
//                                P.getElement().equals(Element.FIRE) ? Color.decode("#b42002") :
//                                        P.getElement().equals(Element.WIND) ? Color.decode("#45c9e7") :
//                                                P.getElement().equals(Element.WOOD) ? Color.decode("#2dc457") :
//                                                        P.getElement().equals(Element.EARTH) ? Color.decode("#e19b18") :
//                                                                Color.decode("#bb0077")));
//                        embedBuilder.addField(BotEmoji.get(P.getElement().getName()) + "Element:", "**• " + P.getElement() + "**", true);
//                        embedBuilder.addField(BotEmoji.get(P.getPosition()) + "Position:", "**• " + P.getPosition() + "**", true);
//                        embedBuilder.addField(BotEmoji.get(P.getGender().getName()) + "Gender:", "**• " + P.getGender() + "**", true);
//                        embedBuilder.addField("Charge Time (with Ball):", P.getChargeTimeBall(), true);
//                        embedBuilder.addField("Charge Time:", P.getChargeTimeNoBall(), true);
//                        embedBuilder.addBlankField(true);
//                        embedBuilder.setThumbnail("attachment://avatar" + i + ".png");
//
//
//                        String kickreal = P.getKick() + " \\(" + getStatRank(P.getKick()) + "\\)";
//                        String bodyreal = P.getBody() + " \\(" + getStatRank(P.getBody()) + "\\)";
//                        String controlreal = P.getControl() + " \\(" + getStatRank(P.getControl()) + "\\)";
//                        String guardreal = P.getGuard() + " \\(" + getStatRank(P.getGuard()) + "\\)";
//                        String speedreal = P.getSpeed() + " \\(" + getStatRank(P.getSpeed()) + "\\)";
//                        String catchreal = P.getCatch() + " \\(" + getStatRank(P.getCatch()) + "\\)";
//                        embedBuilder.addField("Stats:", BotEmoji.get("TP").getFormatted() + " " + P.getTP() + "\n" +
//                                "**" + TL(M,"Kick") + ":** " + kickreal + "\n" +
//                                        "**" + TL(M,"Body") + ":** " + bodyreal + "\n" +
//                                        "**" + TL(M,"Control") + ":** " + controlreal + "\n" +
//                                        "**" + TL(M,"Guard") + ":** " + guardreal + "\n" +
//                                        "**" + TL(M,"Speed") + ":** " + speedreal + "\n" +
//                                        "**" + TL(M,"Catch") + ":** " + catchreal + "\n"
//                                , true);
//                        embedBuilder.addField("Dash:", P.getTacticalAction() + "\n\n**Keys:**\n" + P.getKeyBonus().replace("/", "\n"), true);
//                        String Moveset = "";
//                        String MoveType = "";
//                        switch (prio) {
//                            case Stats.KICK -> {
//                                MoveType = TL(M,"Shoots");
//                                for (Shot MM : P.getShots()) {
//                                    if (((Mtype.equals("Solo") && (!MM.isDuo() && !MM.isTrio()) ||
//                                            (Mtype.equals("Duo") && MM.isDuo()) ||
//                                            (Mtype.equals("Trio") && MM.isTrio()) ||
//                                            (Mtype.equals("Move Solo") && MM.isKeshin() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Move Duo") && MM.isKeshin() && MM.isDuo()) ||
//                                            (Mtype.equals("Move Trio") && MM.isKeshin() && MM.isTrio()) ||
//                                            (Mtype.equals("Keshin Solo") && MM.isKeshin() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Keshin Duo") && MM.isKeshin() && MM.isDuo()) ||
//                                            (Mtype.equals("Keshin Trio") && MM.isKeshin() && MM.isTrio()) ||
//                                            (Mtype.equals("Armed Solo") && MM.isArmed() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Armed Duo") && MM.isArmed() && MM.isDuo()) ||
//                                            (Mtype.equals("Armed Trio") && MM.isArmed() && MM.isTrio())))) {
//                                        Moveset = Moveset + "**" + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + ")**\n";
//                                    } else if (MM.getElement().equals(Melement)) {
//                                        Moveset = Moveset + "**" + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + ")**\n";
//                                    } else {
//                                        Moveset = Moveset + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + "\\)\n";
//                                    }
//                                }
//                            }
//                            case Stats.BODY -> {
//                                MoveType = TL(M,"Dribbles");
//                                for (Dribble MM : P.getDribbles()) {
//                                    if (((Mtype.equals("Solo") && (!MM.isDuo() && !MM.isTrio()) ||
//                                            (Mtype.equals("Duo") && MM.isDuo()) ||
//                                            (Mtype.equals("Trio") && MM.isTrio()) ||
//                                            (Mtype.equals("Move Solo") && MM.isKeshin() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Move Duo") && MM.isKeshin() && MM.isDuo()) ||
//                                            (Mtype.equals("Move Trio") && MM.isKeshin() && MM.isTrio()) ||
//                                            (Mtype.equals("Keshin Solo") && MM.isKeshin() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Keshin Duo") && MM.isKeshin() && MM.isDuo()) ||
//                                            (Mtype.equals("Keshin Trio") && MM.isKeshin() && MM.isTrio()) ||
//                                            (Mtype.equals("Armed Solo") && MM.isArmed() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Armed Duo") && MM.isArmed() && MM.isDuo()) ||
//                                            (Mtype.equals("Armed Trio") && MM.isArmed() && MM.isTrio())))) {
//                                        Moveset = Moveset + "**" + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + ")**\n";
//                                    } else if (MM.getElement().equals(Melement)) {
//                                        Moveset = Moveset + "**" + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + ")**\n";
//                                    } else {
//                                        Moveset = Moveset + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + "\\)\n";
//                                    }
//                                }
//                            }
//                            case Stats.GUARD -> {
//                                MoveType = TL(M,"Blocks");
//                                for (Block MM : P.getBlocks()) {
//                                    if (((Mtype.equals("Solo") && (!MM.isDuo() && !MM.isTrio()) ||
//                                            (Mtype.equals("Duo") && MM.isDuo()) ||
//                                            (Mtype.equals("Trio") && MM.isTrio()) ||
//                                            (Mtype.equals("Move Solo") && MM.isKeshin() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Move Duo") && MM.isKeshin() && MM.isDuo()) ||
//                                            (Mtype.equals("Move Trio") && MM.isKeshin() && MM.isTrio()) ||
//                                            (Mtype.equals("Keshin Solo") && MM.isKeshin() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Keshin Duo") && MM.isKeshin() && MM.isDuo()) ||
//                                            (Mtype.equals("Keshin Trio") && MM.isKeshin() && MM.isTrio()) ||
//                                            (Mtype.equals("Armed Solo") && MM.isArmed() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Armed Duo") && MM.isArmed() && MM.isDuo()) ||
//                                            (Mtype.equals("Armed Trio") && MM.isArmed() && MM.isTrio())))) {
//                                        Moveset = Moveset + "**" + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + ")**\n";
//                                    } else if (MM.getElement().equals(Melement)) {
//                                        Moveset = Moveset + "**" + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + ")**\n";
//                                    } else {
//                                        Moveset = Moveset + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + "\\)\n";
//                                    }
//                                }
//                            }
//                            case Stats.CATCH -> {
//                                MoveType = TL(M,"Catches");
//                                for (Catch MM : P.getCatchs()) {
//                                    if (((Mtype.equals("Solo") && (!MM.isDuo() && !MM.isTrio()) ||
//                                            (Mtype.equals("Duo") && MM.isDuo()) ||
//                                            (Mtype.equals("Trio") && MM.isTrio()) ||
//                                            (Mtype.equals("Move Solo") && MM.isKeshin() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Move Duo") && MM.isKeshin() && MM.isDuo()) ||
//                                            (Mtype.equals("Move Trio") && MM.isKeshin() && MM.isTrio()) ||
//                                            (Mtype.equals("Keshin Solo") && MM.isKeshin() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Keshin Duo") && MM.isKeshin() && MM.isDuo()) ||
//                                            (Mtype.equals("Keshin Trio") && MM.isKeshin() && MM.isTrio()) ||
//                                            (Mtype.equals("Armed Solo") && MM.isArmed() && (!MM.isDuo() && !MM.isTrio())) ||
//                                            (Mtype.equals("Armed Duo") && MM.isArmed() && MM.isDuo()) ||
//                                            (Mtype.equals("Armed Trio") && MM.isArmed() && MM.isTrio())))) {
//                                        Moveset = Moveset + "**" + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + ")**\n";
//                                    } else if (MM.getElement().equals(Melement)) {
//                                        Moveset = Moveset + "**" + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + ")**\n";
//                                    } else {
//                                        Moveset = Moveset + BotEmoji.get(MM.getElement().getName()) + MM.getName().replace(" (Armed)","").replace(" (Keshin)","") + " \\(" + MM.getTruePower() + ") (" + MM.getRealTier() + "\\)\n";
//                                    }
//                                }
//                            }
//                        }
//                        if (Moveset.length() > 1) {
//                            embedBuilder.addField("Moveset (" + MoveType + "\\)", Moveset, false);
//                        }
//                        embeds.add(embedBuilder.build());
//                        if (P.getImage() != null) {
//                            uploads.add(FileUpload.fromData(P.getImage(), "avatar" + i + ".png"));
//                        }
//                        M.editOriginalEmbeds(embeds).setFiles(uploads).setReplace(true).queue();
//                    }
//                }
//
//
//                if (embeds.isEmpty()) {
//                    if (uploads.isEmpty()) {
//                        M.editOriginal(TL(event,"script-fail-3")).queue();
//                    }
//                }
//            } catch (IntruderPresenceException | DuplicateEntityException e) {
//                M.editOriginal(TL(event,"script-fail-2")).queue();
//            }
//        }
//
//    }
//    public static String getStatRank(int stat) {
//        if (stat == 110) {
//            return "S+";
//        } else if (stat == 100) {
//            return "S";
//        } else if (stat == 90) {
//            return "A+";
//        } else if (stat == 80) {
//            return "A";
//        } else if (stat == 70) {
//            return "B+";
//        } else if (stat == 60) {
//            return "B";
//        } else if (stat == 50) {
//            return "C+";
//        } else if (stat == 40) {
//            return "C";
//        } else if (stat == 30) {
//            return "D+";
//        } else if (stat == 20) {
//            return "D";
//        } else if (stat == 10) {
//            return "E+";
//        }
//        return "E";
//    }
}
