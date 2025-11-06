package iecompbot.interaction.listeners.calcs;

public class VR {

//    public static void FilterVR(SlashCommandInteractionEvent event, InteractionHook M) {
//        if (event.getName().contains("players")) {
//            String FILTER = "**" + TL(M,"Filter") + ":**\n";
//            String Script = "";
//            VRPercentages PS1 = null;
//            VRPassiveCondition PSC1 = null;
//            VRAffectedAreas PSA1 = null;
//            if (event.getOption("priority-stat") != null) {
//                String statprio = event.getOption("priority-stat").getAsString();
//                Script = Script + "[P1]" + statprio;
//                FILTER = FILTER + "- Must be high in this stat: **" + statprio + "**\n";
//            }
//            if (event.getOption("passive") != null) {
//                String statprio = event.getOption("passive").getAsString();
//                Script = Script + ",[PS1]" + VRPercentages.get(statprio);
//                FILTER = FILTER + "- Must have team passives boosting: **" + statprio + "**\n";
//                PS1 = VRPercentages.get(statprio);
//            }
//            if (event.getOption("element") != null) {
//                Script = Script + "," + event.getOption("element").getAsString();
//                FILTER = FILTER + "- Must be of this element: **" + event.getOption("element").getAsString() + "**\n";
//            }
//            if (event.getOption("gender") != null) {
//                Script = Script + "," + event.getOption("gender").getAsString();
//                FILTER = FILTER + "- Must be of this gender: **" + event.getOption("gender").getAsString() + "**\n";
//            }
//            if (event.getOption("passive-condition") != null && event.getOption("passive") != null) {
//                String statprio = event.getOption("passive-condition").getAsString();
//                Script = Script + ",[PSC1]" + VRPassiveCondition.get(statprio);
//                FILTER = FILTER + "└ With Condition **" + statprio + "**\n";
//                PSC1 = VRPassiveCondition.get(statprio);
//            }
//            if (event.getOption("passive-target") != null && event.getOption("passive") != null) {
//                String statprio = event.getOption("passive-target").getAsString();
//                Script = Script + ",[PSA1]" + VRAffectedAreas.get(statprio);
//                FILTER = FILTER + "└ With as Target **" + statprio + "**\n";
//                PSA1 = VRAffectedAreas.get(statprio);
//            }
//            if (event.getOption("minimum-kick") != null) {
//                Script = Script + ",[Min]Kick" + event.getOption("minimum-kick").getAsInt();
//                FILTER = FILTER + "└ With minimum Kick: **" + event.getOption("minimum-kick").getAsString() + "**\n";
//            }
//            if (event.getOption("minimum-control") != null) {
//                Script = Script + ",[Min]Control" + event.getOption("minimum-control").getAsInt();
//                FILTER = FILTER + "└ With minimum Control: **" + event.getOption("minimum-control").getAsString() + "**\n";
//            }
//            if (event.getOption("minimum-pressure") != null) {
//                Script = Script + ",[Min]Pressure" + event.getOption("minimum-pressure").getAsInt();
//                FILTER = FILTER + "└ With minimum Pressure: **" + event.getOption("minimum-pressure").getAsString() + "**\n";
//            }
//            if (event.getOption("minimum-physical") != null) {
//                Script = Script + ",[Min]Physical" + event.getOption("minimum-physical").getAsInt();
//                FILTER = FILTER + "└ With minimum Physical: **" + event.getOption("minimum-physical").getAsString() + "**\n";
//            }
//            if (event.getOption("minimum-technique") != null) {
//                Script = Script + ",[Min]Technique" + event.getOption("minimum-technique").getAsInt();
//                FILTER = FILTER + "└ With minimum Technique: **" + event.getOption("minimum-technique").getAsString() + "**\n";
//            }
//            if (event.getOption("minimum-agility") != null) {
//                Script = Script + ",[Min]Agility" + event.getOption("minimum-agility").getAsInt();
//                FILTER = FILTER + "└ With minimum Agility: **" + event.getOption("minimum-agility").getAsString() + "**\n";
//            }
//            if (event.getOption("minimum-intelligence") != null) {
//                Script = Script + ",[Min]Intelligence" + event.getOption("minimum-intelligence").getAsInt();
//                FILTER = FILTER + "└ With minimum Intelligence: **" + event.getOption("minimum-intelligence").getAsString() + "**\n";
//            }
//            if (event.getOption("include-equipment") != null) {
//                if (event.getOption("include-equipment").getAsBoolean()) {
//                    Script = Script + ",Equip";
//                }
//            } else {
//                Script = Script + ",Equip";
//            }
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
//                    List<String> list2 = java.util.List.of(list.split(","));
//                    for (String each : list2) {
//                        for (VRPlayer P : getPlayers()) {
//                            if (P.getName().toLowerCase().contains(each) || P.getNameJP().toLowerCase().contains(each) || P.getNameJP().toLowerCase().contains(each)) {
//                                exclude.add(P.getName());
//                            }
//                        }
//                    }
//                } else {
//                    for (VRPlayer P : getPlayers()) {
//                        if (P.getName().toLowerCase().contains(list) || P.getNameJP().toLowerCase().contains(list) || P.getNameJP().toLowerCase().contains(list)) {
//                            exclude.add(P.getName());
//                        }
//                    }
//                }
//            }
//            if (true) {
//                try {
//                    java.util.List<MessageEmbed> embeds = new ArrayList<>();
//                    List<FileUpload> uploads = new ArrayList<>();
//                    M.editOriginal(FILTER).queue();
//                    System.out.println(Script);
//                    for (int i = 0; i != 5; i++) {
//                        VRPlayer P = filterPlayers(Script, exclude);
//                        if (P != null) {
//                            exclude.add(P.getName());
//                            EmbedBuilder embedBuilder = new EmbedBuilder();
//                            embedBuilder.setTitle(P.getName());
//                            embedBuilder.setAuthor(P.getNameJP());
//                            embedBuilder.setColor((
//                                    P.getElement().equals(Element.FIRE) ? Color.decode("#b42002") :
//                                            P.getElement().equals(Element.WIND) ? Color.decode("#45c9e7") :
//                                                    P.getElement().equals(Element.WOOD) ? Color.decode("#2dc457") :
//                                                            P.getElement().equals(Element.EARTH) ? Color.decode("#e19b18") :
//                                                                    Color.decode("#bb0077")));
//                            embedBuilder.addField(BotEmoji.get(P.getElement().getName()) + " Element:", "**• " + P.getElement().getName() + "**", true);
//                            embedBuilder.addField(BotEmoji.get(P.getPosition()) + " Position:", "**• " + P.getPosition() + "**", true);
//                            embedBuilder.addField(BotEmoji.get(P.getGender().getName()) + " Gender:", "**• " + P.getGender() + "**", true);
//                            embedBuilder.setThumbnail("attachment://avatar" + i + ".png");
//
//                            embedBuilder.addField("__Stats:__ (Legendary)",
//                                    "**" + TL(M,"Kick") + ":** " + P.getKick(true) + "\n" +
//                                            "**" + TL(M,"Control") + ":** " + P.getControl(true) + "\n" +
//                                            "**" + TL(M,"Pressure") + ":** " + P.getPressure(true) + "\n" +
//                                            "**" + TL(M,"Physical") + ":** " + P.getPhysical(true) + "\n" +
//                                            "**" + TL(M,"Agility") + ":** " + P.getAgility(true) + "\n" +
//                                            "**" + TL(M,"Intelligence") + ":** " + P.getIntelligence(true) + "\n" +
//                                            "**" + TL(M,"Technique") + ":** " + P.getTechnique(true)
//                                    , false);
//
//                            {
//                                VRPercentages finalPS = PS1;
//                                VRPassiveCondition finalPSC = PSC1;
//                                VRAffectedAreas finalPSA = PSA1;
//                                embedBuilder.addField("__Team Passives:__",
//                                        P.getTeamPassives().stream().map(ps -> "- " +
//                                                (finalPS != null && ps.getStat() == finalPS && (finalPSC == null || ps.getCondition() == finalPSC) && (finalPSA == null || ps.getAffectedAreas() == finalPSA) ? "**" : "")
//                                                + ps.getName() +
//                                                (finalPS != null && ps.getStat() == finalPS && (finalPSC == null || ps.getCondition() == finalPSC) && (finalPSA == null || ps.getAffectedAreas() == finalPSA) ? "**" : "")
//                                        ).collect(Collectors.joining("\n"))
//                                        , false);
//                            }
//                            embeds.add(embedBuilder.build());
//                            if (P.getImage() != null) {
//                                uploads.add(FileUpload.fromData(P.getImage(), "avatar" + i + ".png"));
//                                M.editOriginalEmbeds(embeds).setFiles(uploads).queue();
//                            }
//                        }
//                    }
//
//
//                    if (embeds.isEmpty()) {
//                        if (uploads.isEmpty()) {
//                            M.editOriginal(FILTER +"> Couldn't find anything...").queue();
//                        }
//                    } else {
//                        M.editOriginalEmbeds(embeds).queue();
//                    }
//                } catch (Exception e) {
//                    M.editOriginal(TL(event,"script-fail-2")).queue();
//                }
//            } else {
//                M.editOriginal(TL(event,"script-fail")).queue();
//            }
//        }
//
//    }

}
