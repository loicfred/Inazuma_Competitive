package iecompbot.interaction.listeners;

import iecompbot.Constants;
import iecompbot.Utility;
import iecompbot.annotations.Command;
import iecompbot.interaction.cmdbreakdown.clan.ClanInviteCommand;
import iecompbot.interaction.cmdbreakdown.clan.ClanMemberInteractCommand;
import iecompbot.interaction.cmdbreakdown.PageViewerCommand;
import iecompbot.interaction.cmdbreakdown.profile.FilterCommand;
import iecompbot.interaction.cmdbreakdown.profile.GamesCommand;
import iecompbot.interaction.cmdbreakdown.profile.ProfileCommand;
import iecompbot.objects.BotEmoji;
import iecompbot.objects.Nationality;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.clan.items.ClanPermission;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.Profile_Booster;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.server.ServerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.getLanguageBundle;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.Utility.getComponentFullID;
import static iecompbot.img.ImgUtilities.*;
import static iecompbot.interaction.Automation.CanUseCommand;
import static iecompbot.interaction.Automation.replyException;
import static iecompbot.objects.BotManagers.isTournamentManager;
import static iecompbot.objects.clan.Clan.*;
import static my.utilities.lang.L10N.RB;
import static my.utilities.util.Utilities.*;

public class ProfileFeatures extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getName().startsWith("profile")) {
                        switch (event.getName()) {
                            case "profile" -> {
                                String option = (event.getOption("profile-info") != null ? event.getOption("profile-info").getAsString() : "Basic");
                                Profile pf = Profile.get(event.getOption("player") != null ? event.getOption("player").getAsUser() : event.getUser());
                                event.deferReply(option.equals("Inventory") || pf.hasPrivateProfile()).queue(M -> {
                                    slashProfile(M, pf, option);
                                    pf.RefreshProfileInformation(event);
                                });
                            }
                            case "profile-character" -> {
                                event.deferReply(true).queue(M -> {
                                    slashProfileCharacter(M, event.getUser(), event.getOption("character").getAsAttachment());
                                });
                            }
                            case "profile-manage" -> {
                                event.deferReply(true).queue(ProfileFeatures::slashProfileManage);
                            }
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("pf-edit-other-user-by-id")) {
                        Label input = Label.of("User ID:", TextInput.create("id", TextInputStyle.SHORT)
                                        .setPlaceholder(event.getUser().getId()).setMaxLength(20).build());
                        event.replyModal(Modal.create("pf-edit-other-user-by-id", "Modify other User")
                                .addComponents(TextDisplay.of("Select a user by their ID."), input)
                                .build()).queue();
                    }
                    else if (event.getComponentId().startsWith("pf-edit-friendcode")) {
                        event.replyModal(OpenFriendcodeUI(event, new ProfileCommand(event.getUser()))).queue();
                    }
                    else if (event.getComponentId().startsWith("pf-edit-nationality")) {
                        event.replyModal(OpenNationalityUI(event, new ProfileCommand(event.getUser()))).queue();
                    }
                    else if (event.getComponentId().startsWith("pf-refresh")) {
                        ProfileCommand CMD = new ProfileCommand(event.getComponentId());
                        Profile P = CMD.getProfile();
                        if (event.getComponentId().contains("pf-refresh-cards")) {
                            event.deferEdit().setComponents(ActionRow.of(Button.secondary("refresh", TL(event, "Processing")).asDisabled())).queue(M -> {
                                try {
                                    if (event.getComponentId().contains("priv")) {
                                        P.regeneratePersonalCard(M);
                                    } else {
                                        ClanMember CM = ClanMember.ofClan(takeOnlyDigits(CMD.Command), P.getId());
                                        if (CM != null) CM.regenerateClanCard(M);
                                    }
                                    System.err.println("Done Load");
                                    P.ViewLicenses(M);
                                } catch (Exception e) {
                                    replyException(event, e);
                                }
                            });
                        }
                    }

                    else if (event.getComponentId().startsWith("pf-manage-license")) {
                        ProfileCommand CMD = new ProfileCommand(event.getComponentId());
                        Profile P = CMD.getProfile();
                        event.deferReply(true).queue(M -> {
                            try {
                                P.LicenseManageUI(M);
                                List<ActionRow> rows = new ArrayList<>();
                                List<SelectOption> options = new ArrayList<>();
                                if (event.getComponentId().contains("bg")) {
                                    for (Item.Item_To_Profile I : P.getItems(Item.ItemType.LICENSE_BG)) {
                                        if (I.Amount > 0) {
                                            options.add(SelectOption.of(I.getName(), I.getId() + "").withEmoji(I.getEmoji().retrieve()));
                                            if (options.size() == 25 && rows.size() < 5) {
                                                rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("pf-manage-license-bg-" + rows.size()))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                options = new ArrayList<>();
                                                M.editOriginalComponents(rows).queue();
                                            }
                                            if (rows.size() == 5) break;
                                        }
                                    }
                                    if (!options.isEmpty() && rows.size() < 5) {
                                        StringSelectMenu menu = StringSelectMenu.create(CMD.Command("pf-manage-license-bg-5"))
                                                .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build();
                                        rows.add(ActionRow.of(menu));
                                    }
                                }
                                else if (event.getComponentId().contains("fg")) {
                                    for (Item.Item_To_Profile I : P.getItems(Item.ItemType.LICENSE_FG)) {
                                        if (I.Amount > 0) {
                                            options.add(SelectOption.of(I.getName(), I.getId() + "").withEmoji(I.getEmoji().retrieve()));
                                            if (options.size() % 25 == 0) {
                                                rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("pf-manage-license-fg-" + rows.size()))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                options = new ArrayList<>();
                                                M.editOriginalComponents(rows).queue();
                                            }
                                            if (rows.size() == 5) break;
                                        }
                                    }
                                    if (!options.isEmpty() && rows.size() < 5) {
                                        StringSelectMenu menu = StringSelectMenu.create(CMD.Command("pf-manage-license-fg-5"))
                                                .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build();
                                        rows.add(ActionRow.of(menu));
                                    }
                                }
                                else if (event.getComponentId().contains("ry")) {
                                    for (Item.Item_To_Profile I : P.getItems(Item.ItemType.LICENSE_RY)) {
                                        if (I.Amount > 0) {
                                            options.add(SelectOption.of(I.getName(), I.getId() + "").withEmoji(I.getEmoji().retrieve()));
                                            if (options.size() == 25 && rows.size() < 5) {
                                                rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("pf-manage-license-ry-" + rows.size()))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                options = new ArrayList<>();
                                                M.editOriginalComponents(rows).queue();
                                            }
                                            if (rows.size() == 5) break;
                                        }
                                    }
                                    if (!options.isEmpty() && rows.size() < 5) {
                                        StringSelectMenu menu = StringSelectMenu.create(CMD.Command("pf-manage-license-ry-5"))
                                                .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build();
                                        rows.add(ActionRow.of(menu));
                                    }
                                }
                                else if (event.getComponentId().contains("st")) {
                                    for (Item.Item_To_Profile I : P.getItems(Item.ItemType.LICENSE_ST)) {
                                        if (I.Amount > 0) {
                                            options.add(SelectOption.of(I.getName(), I.getId() + "").withEmoji(I.getEmoji().retrieve()));
                                            if (options.size() == 25 && rows.size() < 5) {
                                                rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("pf-manage-license-st-" + rows.size()))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                options = new ArrayList<>();
                                                M.editOriginalComponents(rows).queue();
                                            }
                                            if (rows.size() == 5) break;
                                        }
                                    }
                                    if (!options.isEmpty() && rows.size() < 5) {
                                        StringSelectMenu menu = StringSelectMenu.create(CMD.Command("pf-manage-license-st-5"))
                                                .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build();
                                        rows.add(ActionRow.of(menu));
                                    }
                                }
                                else if (event.getComponentId().contains("sp")) {
                                    for (String ID : Constants.SponsoredServers) {
                                        Guild G = DiscordAccount.getGuildById(ID);
                                        if (G != null) {
                                            options.add(SelectOption.of(G.getName(), G.getId()));
                                            if (options.size() == 25 && rows.size() < 5) {
                                                rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("pf-manage-license-sp-" + rows.size()))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                options = new ArrayList<>();
                                                M.editOriginalComponents(rows).queue();
                                            }
                                            if (rows.size() == 5) break;
                                        }
                                    }
                                    if (!options.isEmpty() && rows.size() < 5) {
                                        StringSelectMenu menu = StringSelectMenu.create(CMD.Command("pf-manage-license-sp-5"))
                                                .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build();
                                        rows.add(ActionRow.of(menu));
                                    }
                                }
                                if (rows.isEmpty()) M.editOriginal(TL(M, "profile-no-decoration")).queue();
                                else M.editOriginalComponents(rows).queue();
                            } catch (Exception e) {
                                replyException(M, e);
                            }
                        });
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("pf")) {
                        if (event.getComponentId().startsWith("pf-view")) {
                            event.deferReply(event.getMessage().isEphemeral()).queue(M -> {
                                try {
                                    slashProfile(M, Profile.get(Long.parseLong(event.getValues().getFirst())), "Basic");
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }

                        else if (event.getComponentId().startsWith("pf-manage-")) {
                            ProfileCommand CMD = new ProfileCommand(event.getComponentId());
                            Profile P = CMD.getProfile();
                            if (event.getComponentId().startsWith("pf-manage-toggle")) {
                                event.deferEdit().queue(M -> {
                                    try {
                                        P.setMatchmakingNotification(event.getValues().contains("pf-toggle-matchmaking"));
                                        P.setTournamentNotification(event.getValues().contains("pf-toggle-tournament"));
                                        P.setPrivateProfile(event.getValues().contains("pf-toggle-private"));
                                        P.setGIF(P.getItem("Shiny Card").Amount >= 1 ? event.getValues().contains("pf-toggle-card-gif") : CMD.getProfile().hasGIF());
                                        P.ManageProfileUI(M, CMD);
                                        P.Update();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().startsWith("pf-manage-license")) {
                                event.deferEdit().queue(M -> {
                                    try {
                                        List<String> SelectedOptions = event.getValues();
                                        Item I = P.getItem(Long.parseLong(SelectedOptions.getFirst())).getItem();
                                        if (event.getComponentId().contains("bg")) {
                                            P.setCardBackground(I);
                                        } else if (event.getComponentId().contains("fg")) {
                                            P.setCardForeground(I);
                                        } else if (event.getComponentId().contains("ry")) {
                                            P.setCardRay(I);
                                        } else if (event.getComponentId().contains("st")) {
                                            P.setCardStrike(I);
                                        } else if (event.getComponentId().contains("sp")) {
                                            P.setSponsor(Long.parseLong(SelectedOptions.getFirst()));
                                        }
                                        P.LicenseManageUI(M);
                                        P.resetCards();
                                        P.Update();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().startsWith("pf-manage-equip")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        EmbedBuilder E = new EmbedBuilder();
                                        E.setColor(P.getColor());
                                        E.setAuthor(TL(M,"Cosmetics"), null, event.getUser().getAvatarUrl());

                                        if (event.getValues().getFirst().equals("0")) {
                                            E.setDescription(":white_check_mark: " + TL(M,"profile-cosmetics-success", "**" + TL(M, "Default") + "**"));
                                            if (event.getComponentId().contains("frame")) {
                                                P.setCustomFrame(null);
                                            } else if (event.getComponentId().contains("board")) {
                                                P.setCustomBoard(null);
                                            }
                                        } else {
                                            Item EquippingItem = Item.get(takeOnlyDigits(event.getValues().getFirst()));
                                            E.setDescription(":white_check_mark: " + TL(M,"profile-cosmetics-success", "**" + EquippingItem.getEmojiFormatted() + " " + EquippingItem.getName() + "**"));
                                            if (event.getComponentId().contains("frame")) {
                                                P.setCustomFrame(EquippingItem);
                                            } else if (event.getComponentId().contains("board")) {
                                                P.setCustomBoard(EquippingItem);
                                            } else if (event.getComponentId().contains("booster")) {
                                                E.setAuthor(TL(M,"booster-activated"));
                                                String boosttime = "";
                                                if (EquippingItem.getName().contains("24h")) {
                                                    boosttime = "**24h**";
                                                } else if (EquippingItem.getName().contains("48h")) {
                                                    boosttime = "**48h**";
                                                }
                                                Profile_Booster pb = P.addBooster(P.getItem(EquippingItem.getId()));
                                                E.setDescription(":white_check_mark: " + TL(M,"profile-boost-use-success", pb.getBoosterType(), pb.getMultiplier(), boosttime));
                                            }
                                        }
                                        M.editOriginalEmbeds(E.build()).queue();
                                        P.Update();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().startsWith("pf-manage-set-main-clan")) {
                                event.deferEdit().queue(M -> {
                                    try {
                                        for (ClanMember CM : ClanMember.OfUser(CMD.ID)) {
                                            CM.updateMainClan(event.getValues().stream().anyMatch(s -> s.equals(String.valueOf(CM.ClanID))));
                                        }
                                        P.ManageProfileUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().startsWith("pf-manage-menu")) {
                                String Option = event.getValues().getFirst();
                                if (Option.startsWith("pf-manage-color")) {
                                    Label input = Label.of(TL(event, "Color") + ":", TextInput.create("pf-color-input", TextInputStyle.SHORT)
                                            .setPlaceholder(getHexValue(CMD.getProfile().getColor()))
                                            .setMaxLength(7)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("pf-manage-color"), StopString(TL(event, "edit-colorcode-description"), 45))
                                            .addComponents(input)
                                            .build()).queue();
                                }
                                else if (Option.startsWith("pf-manage-nationality")) {
                                    Label input = Label.of(TL(event, "Nationality") + ":", TextInput.create("pf-nationality-input", TextInputStyle.SHORT)
                                            .setPlaceholder("Spanish, German, Brazilian...")
                                            .setMaxLength(16)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("pf-manage-nationality"), StopString(TL(event, "edit-nationality-description"), 45))
                                            .addComponents(input)
                                            .build()).queue();
                                }
                                else if (Option.startsWith("pf-manage-friendcode")) {
                                    Label input = Label.of("Strikers Friendcode (Numbers only) :", TextInput.create("pf-strikers-input", TextInputStyle.SHORT)
                                            .setPlaceholder(CMD.getProfile().getStrikersFriendcode() + " (PS: Enter full `0` to remove.)")
                                            .setMinLength(12).setMaxLength(12)
                                            .setRequired(false)
                                            .build());
                                    Label input2 = Label.of("Xtreme Friendcode (Numbers only) :", TextInput.create("pf-xtreme-input", TextInputStyle.SHORT)
                                            .setPlaceholder(CMD.getProfile().getXtremeFriendcode() + " (PS: Enter full `0` to remove.)")
                                            .setMinLength(12).setMaxLength(12)
                                            .setRequired(false)
                                            .build());
                                    Label input3 = Label.of("Switch Friendcode (Numbers only) :", TextInput.create("pf-switch-input", TextInputStyle.SHORT)
                                            .setPlaceholder(CMD.getProfile().getSwitchFriendcode() + " (PS: Enter full `0` to remove.)")
                                            .setMinLength(12).setMaxLength(12)
                                            .setRequired(false)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("pf-manage-friendcode"), StopString(TL(event, "edit-friendcode-description"), 45))
                                            .addComponents(input, input2, input3)
                                            .build()).queue();
                                }
                                else if (Option.startsWith("pf-manage-number")) {
                                    ClanMemberInteractCommand CMD2 = new ClanMemberInteractCommand("clan-member-manage-number");
                                    CMD2.MyID = event.getUser().getIdLong();
                                    CMD2.MemberID = CMD.ID;
                                    CMD2.ClanID = takeOnlyDigits(Option);
                                    Label input = Label.of(TL(event, "clan-number") + ":", TextInput.create("clan-number-input", TextInputStyle.SHORT)
                                            .setPlaceholder("10").setMaxLength(3)
                                            .build());
                                    event.replyModal(Modal.create(CMD2.Command("clan-member-manage-self-num"), StopString(TL(event, "edit-clan-number-description"), 45))
                                            .addComponents(input).build()).queue();
                                }
                                else if (Option.startsWith("pf-manage-bio")) {
                                    Label input = Label.of(TL(event, "Signature"), TextInput.create("pf-bio-input", TextInputStyle.PARAGRAPH)
                                            .setPlaceholder(StopString(CMD.getProfile().getSignature(), 100))
                                            .setRequired(true).setMaxLength(128)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("pf-manage-bio"), StopString(TL(event, "edit-signature-description"), 45))
                                            .addComponents(input)
                                            .build()).queue();
                                }
                                else if (Option.startsWith("pf-manage-birthday")) {
                                    Label input = Label.of(TL(event, "Birthday") + ":", TextInput.create("pf-birthday-input", TextInputStyle.SHORT)
                                            .setPlaceholder(EpochSecondToPattern(Instant.now().getEpochSecond(), "dd/MM/yyyy"))
                                            .setRequiredRange(10, 10).setRequired(true)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("pf-manage-birthday"), StopString(TL(event, "edit-birthday-description"), 45))
                                            .addComponents(input)
                                            .build()).queue();
                                }
                                else if (Option.startsWith("pf-manage-character")) {
                                    String s = TL(event, "edit-character-description-recommendation");
                                    Label input = Label.of("URL:", TextInput.create("pf-character-input", TextInputStyle.PARAGRAPH)
                                            .setPlaceholder(StopString(s, 100))
                                            .setRequired(true).setRequiredRange(1, 512)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("pf-manage-character"), StopString(TL(event, "edit-character-description"), 45))
                                            .addComponents(input)
                                            .build()).queue();
                                }
                                else if (Option.startsWith("pf-manage-media")) {
                                    TextInput.Builder link = TextInput.create("link-input", TextInputStyle.SHORT).setRequiredRange(8, 128).setRequired(false);
                                    if (Option.contains("twitter")) {
                                        link.setPlaceholder("https://twitter.com/" + event.getUser().getName().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("pf-manage-twitter"), "Twitter").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    } else if (Option.contains("website")) {
                                        link.setPlaceholder("https://" + event.getUser().getName().replaceAll(" ", "").toLowerCase() + ".com/");
                                        event.replyModal(Modal.create(CMD.Command("pf-manage-website"), "Website").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    } else if (Option.contains("youtube")) {
                                        link.setPlaceholder("https://www.youtube.com/@" + event.getUser().getName().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("pf-manage-youtube"), "YouTube").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    } else if (Option.contains("twitch")) {
                                        link.setPlaceholder("https://www.twitch.tv/" + event.getUser().getName().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("pf-manage-twitch"), "Twitch").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    } else if (Option.contains("instagram")) {
                                        link.setPlaceholder("https://www.instagram.com/@" + event.getUser().getName().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("pf-manage-instagram"), "Instagram").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    } else if (Option.contains("discord")) {
                                        link.setPlaceholder("https://discord.gg/" + event.getUser().getName().toLowerCase().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("pf-manage-discord"), "Discord").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    } else if (Option.contains("tiktok")) {
                                        link.setPlaceholder("https://www.tiktok.com/" + event.getUser().getName().toLowerCase().replaceAll(" ", "").toLowerCase());
                                        event.replyModal(Modal.create(CMD.Command("pf-manage-tiktok"), "Tiktok").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                    }
                                }
                            }
                        }

                        else if (event.getComponentId().startsWith("pf-power")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    Message OG = M.retrieveOriginal().submit().get();
                                    FilterCommand CMD2 = new FilterCommand(getComponentFullID(OG, "pf-power-filter"));
                                    GamesCommand CMD3 = new GamesCommand(getComponentFullID(OG, "pf-power-game"));

                                    Profile P = Profile.get(CMD2.ID);
                                    if (event.getComponentId().startsWith("pf-power-filter")) {
                                        CMD2.Filter = event.getValues();
                                    } else if (event.getComponentId().startsWith("pf-power-game")) {
                                        CMD3.Games = CMD3.parseGamesToList(event.getValues().toString());
                                    }
                                    P.ViewPowerDetails(M, CMD2, CMD3);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("pf-mlog")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    Message OG = M.retrieveOriginal().submit().get();
                                    PageViewerCommand CMD1 = new PageViewerCommand(getComponentFullID(OG, "pf-mlog-cp"));
                                    FilterCommand CMD2 = new FilterCommand(getComponentFullID(OG, "pf-mlog-filter"));
                                    GamesCommand CMD3 = new GamesCommand(getComponentFullID(OG, "pf-mlog-game"));

                                    Profile P = Profile.get(CMD1.ID);
                                    CMD1.Page = 1;
                                    if (event.getComponentId().startsWith("pf-mlog-cp")) {
                                        CMD1.Page = Integer.parseInt(event.getValues().getFirst());
                                    } else if (event.getComponentId().startsWith("pf-mlog-filter")) {
                                        CMD2.Filter = event.getValues();
                                    } else if (event.getComponentId().startsWith("pf-mlog-game")) {
                                        CMD3.Games = CMD3.parseGamesToList(event.getValues().toString());
                                    }
                                    P.ViewHistory(M, CMD1, CMD2, CMD3);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("pf-tourn")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    Message OG = M.retrieveOriginal().submit().get();
                                    PageViewerCommand CMD1 = new PageViewerCommand(getComponentFullID(OG, "pf-tourn-cp"));
                                    FilterCommand CMD2 = new FilterCommand(getComponentFullID(OG, "pf-tourn-filter"));
                                    GamesCommand CMD3 = new GamesCommand(getComponentFullID(OG, "pf-tourn-game"));
                                    Profile P = Profile.get(CMD1.ID);
                                    CMD1.Page = 1;
                                    if (event.getComponentId().startsWith("pf-tourn-cp")) {
                                        CMD1.Page = Integer.parseInt(event.getValues().getFirst());
                                    } else if (event.getComponentId().startsWith("pf-tourn-filter")) {
                                        CMD2.Filter = event.getValues();
                                    } else if (event.getComponentId().startsWith("pf-tourn-game")) {
                                        CMD3.Games = CMD3.parseGamesToList(event.getValues().toString());
                                    }
                                    P.ViewTournaments(M, CMD1, CMD2, CMD3);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("pf-inv")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    Message OG = M.retrieveOriginal().submit().get();
                                    PageViewerCommand CMD1 = new PageViewerCommand(getComponentFullID(OG, "pf-inv-cp"));
                                    Profile P = Profile.get(CMD1.ID);
                                    CMD1.Page = 1;
                                    if (event.getComponentId().startsWith("pf-inv-cp")) {
                                        CMD1.Page = Integer.parseInt(event.getValues().getFirst());
                                    }
                                    P.ViewInventory(M, CMD1);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getModalId().startsWith("pf")) {
                        if (event.getModalId().startsWith("pf-edit-other-user-by-id")) {
                            event.deferEdit().queue(M -> {
                                ProfileCommand CMD = new ProfileCommand(Long.parseLong(event.getValue("id").getAsString()));
                                CMD.getProfile().ManageProfileUI(M, CMD);
                            });
                        }
                        else if (event.getModalId().startsWith("pf-manage")) {
                            ProfileCommand CMD = new ProfileCommand(event.getModalId());
                            event.deferEdit().queue(M -> {
                                Profile P = CMD.getProfile();
                                if (event.getModalId().startsWith("pf-manage-color")) {
                                    String code = event.getValue("pf-color-input").getAsString();
                                    if (Utility.isColorcodeValid(M, code)) {
                                        P.resetCards();
                                        P.setColor(Color.decode(code));
                                        P.ManageProfileUI(M, CMD);
                                    }
                                } else if (event.getModalId().startsWith("pf-manage-bio")) {
                                    String bio = event.getValue("pf-bio-input").getAsString().replaceAll("\n", ". ").replaceAll("\n", ". ");
                                    P.resetClanCards();
                                    P.setSignature(bio);
                                    P.ManageProfileUI(M, CMD);
                                } else if (event.getModalId().startsWith("pf-manage-birthday")) {
                                    String birthday = event.getValue("pf-birthday-input").getAsString();
                                    if (!birthday.contains("\n") && birthday.charAt(2) == '/' && birthday.charAt(5) == '/' && isNumeric(birthday.charAt(0)) && isNumeric(birthday.charAt(1)) && isNumeric(birthday.charAt(3)) && isNumeric(birthday.charAt(4))
                                            && isNumeric(birthday.charAt(6)) && isNumeric(birthday.charAt(7)) && isNumeric(birthday.charAt(8)) && isNumeric(birthday.charAt(9))) {
                                        P.resetClanCards();
                                        P.setBirthday(PatternToEpochSecond(birthday, "dd/MM/yyyy"));
                                        P.ManageProfileUI(M, CMD);
                                    } else {
                                        M.editOriginal(":x: " + TL(event, "error-invalid-date")).queue();
                                    }
                                } else if (event.getModalId().startsWith("pf-manage-friendcode")) {
                                    String code = event.getValue("pf-strikers-input") != null ? event.getValue("pf-strikers-input").getAsString() : null;
                                    String code2 = event.getValue("pf-xtreme-input") != null ? event.getValue("pf-xtreme-input").getAsString() : null;
                                    String code3 = event.getValue("pf-switch-input") != null ? event.getValue("pf-switch-input").getAsString() : null;
                                    if (code != null && code.length() == 12 && isNumeric(code)) {
                                        P.setStrikersFriendcode(code.equals("000000000000") ? null : code.substring(0, 4) + "-" + code.substring(4, 8) + "-" + code.substring(8, 12));
                                    }
                                    if (code2 != null && code2.length() == 12 && isNumeric(code2)) {
                                        P.setXtremeFriendcode(code2.equals("000000000000") ? null : code2.substring(0, 4) + "-" + code2.substring(4, 8) + "-" + code2.substring(8, 12));
                                    }
                                    if (code3 != null && code3.length() == 12 && isNumeric(code3)) {
                                        P.setSwitchFriendcode(code3.equals("000000000000") ? null : "SW-" + code3.substring(0, 4) + "-" + code3.substring(4, 8) + "-" + code3.substring(8, 12));
                                    }
                                    P.ManageProfileUI(M, CMD);
                                } else if (event.getModalId().startsWith("pf-manage-nationality")) {
                                    P.resetClanCards();
                                    P.setNationality(Nationality.get(event.getValue("pf-nationality-input").getAsString()));
                                    P.ManageProfileUI(M, CMD);
                                } else if (event.getModalId().startsWith("pf-manage-character")) {
                                    try {
                                        ImageProxy proxy = new ImageProxy(event.getValue("pf-character-input").getAsString());
                                        proxy.downloadToFile(P.getCharacter()).whenComplete((file, throwable) -> {
                                            try {
                                                if (ImageIO.read(file) != null) {
                                                    if (file.getName().contains(".png")) {
                                                        if (ImageHasTransparentPixel(file)) {
                                                            if (ImageIO.read(file).getHeight() > 3000) {
                                                                ResizeImage(file, file, 0.4);
                                                            } else if (ImageIO.read(file).getHeight() > 2500) {
                                                                ResizeImage(file, file, 0.5);
                                                            } else if (ImageIO.read(file).getHeight() > 2000) {
                                                                ResizeImage(file, file, 0.6);
                                                            } else if (ImageIO.read(file).getHeight() > 1500) {
                                                                ResizeImage(file, file, 0.8);
                                                            }
                                                            P.resetClanCards();
                                                            P.regeneratePersonalCard(M);
                                                            P.ManageProfileUI(M, CMD);
                                                        } else {
                                                            file.delete();
                                                            M.editOriginal(TL(M, "image-fail-extension-2")).queue();
                                                        }
                                                    } else {
                                                        file.delete();
                                                        M.editOriginal(TL(M, "image-fail-extension")).queue();
                                                    }
                                                } else {
                                                    file.delete();
                                                    M.editOriginal(TL(M, "image-fail-extension")).queue();
                                                }
                                            } catch (Exception e) {
                                                file.delete();
                                                M.editOriginal(TL(M, "image-fail-extension")).queue();
                                            }
                                        });
                                    } catch (Exception e) {
                                        M.editOriginal(TL(M, "image-fail-extension")).queue();
                                    }
                                } else if (event.getModalId().contains("pf-manage-twitter")) {
                                    if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && (event.getValue("link-input").getAsString().contains("twitter.com") || event.getValue("link-input").getAsString().contains("x.com"))) {
                                        P.setTwitterURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                        P.ManageProfileUI(M, CMD);
                                    } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                } else if (event.getModalId().contains("pf-manage-twitch")) {
                                    if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("twitch.tv")) {
                                        P.setTwitchURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                        P.ManageProfileUI(M, CMD);
                                    } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                } else if (event.getModalId().contains("pf-manage-website")) {
                                    if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("https://")) {
                                        P.setWebsiteURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                        P.ManageProfileUI(M, CMD);
                                    } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                } else if (event.getModalId().contains("pf-manage-youtube")) {
                                    if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("youtu")) {
                                        P.setYouTubeURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                        P.ManageProfileUI(M, CMD);
                                    } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                } else if (event.getModalId().contains("pf-manage-instagram")) {
                                    if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("instagram.com")) {
                                        P.setInstagramURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                        P.ManageProfileUI(M, CMD);
                                    } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                } else if (event.getModalId().contains("pf-manage-discord")) {
                                    if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("discord.gg")) {
                                        P.setDiscordURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                        P.ManageProfileUI(M, CMD);
                                    } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                } else if (event.getModalId().contains("pf-manage-tiktok")) {
                                    if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("tiktok.com")) {
                                        P.setTiktokURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                        P.ManageProfileUI(M, CMD);
                                    } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                }
                                P.Update();
                            });
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (!event.isAcknowledged()) {
            try {
                if (event.getName().equals("View clan...")) {
                    List<Clan> clans = Clan.getClansOfUser(event.getTarget());
                    event.deferReply(true).queue(M -> {
                        if (!clans.isEmpty()) {
                            List<MessageEmbed> embeds = new ArrayList<>();
                            List<ActionRow> ARs = new ArrayList<>();
                            for (Clan C : clans) {
                                try {
                                    ClanMember Target = C.getMemberById(event.getTarget());
                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setAuthor(Target.Number + " • " + event.getTarget().getEffectiveName() + " - " + Target.listTasksOneLine(),
                                            null, event.getTarget().getAvatarUrl());
                                    E.setTitle((":white_check_mark: ") + C.getName() + " | " + C.getName() + (Target.isReinforcement() ? " • Secondary Clan" : ""));

                                    E.setColor(C.getColor());
                                    E.addField(C.getNationality().getFlag().getFormatted() + " " + TL(M,"Nationality"),"- " + C.getNationality(), true);
                                    E.addField(":calendar_spiral: " + TL(M,"Date_Created"),"- <t:" + C.getTimeCreated().getEpochSecond() + ":d>", true);
                                    E.addField(BotEmoji.get("POW") + " " + TL(M,"Clan_Power"), "- " + C.getPowerAsString(), true);
                                    E.setDescription(C.getDescription());
                                    if (event.isFromGuild()) {
                                        ServerInfo I = ServerInfo.get(event.getGuild());
                                        if (I != null && C.getRole(I) != null) E.addField(C.getEmojiFormatted() + " " + TL(M,"Clan-Server-Role") + ": ","- " + C.getRole(I).getAsMention(), true);
                                    }

                                    E.addField(":crown: " + TL(M,"Clan-Captain") + ": ", "- " + C.getCaptain().getUser().getAsMention(), true);
                                    E.addField(":people_hugging: " + TL(M,"Members") + ": ", "- (" + C.getClanMembers().size() + "/50)", true);

                                    if (Target.getEndOfContract() != null && Target.isContractActive()) {
                                        E.addField(":scroll: " + TL(M,"Contract"), TL(M,"Ending_on") + ": <t:" + Target.EndOfContractEpochSecond + ":d> (<t:" + Target.EndOfContractEpochSecond + ":R>)", false);
                                    }
                                    E.setFooter("• "+ TL(M,"Member_Since") + ": " + Target.getTimeJoined("dd MMMM yyyy"));
                                    E.setImage(Target.getClanCard());
                                    E.addField(TL(M,"Join_Requirements"), "- " + C.getRequirements(), false);
                                    E.setThumbnail(C.getEmblemURL());

                                    List<Button> BTN = new ArrayList<>();
                                    List<Button> BTN2 = new ArrayList<>();
                                    if (!Target.isReinforcement()) {
                                        if (C.getWebsiteURL() != null) BTN.add(Button.link(C.getWebsiteURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + TL(M,"Website")));
                                        if (C.getTwitchURL() != null) BTN.add(Button.link(C.getTwitchURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + "Twitch"));
                                        if (C.getTwitterURL() != null) BTN.add(Button.link(C.getTwitterURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + "X"));
                                        if (C.getTiktokURL() != null) BTN.add(Button.link(C.getTiktokURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + "Tiktok"));
                                        if (C.getYouTubeURL() != null) BTN.add(Button.link(C.getYouTubeURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + "YouTube"));
                                        if (C.getDiscordURL() != null) if (BTN.size() < 5) BTN.add(Button.link(C.getDiscordURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + "Discord"));
                                        else BTN2.add(Button.link(C.getDiscordURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + "Discord"));
                                        if (C.getInstagramURL() != null) if (BTN.size() < 5) BTN.add(Button.link(C.getInstagramURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + "Instagram"));
                                        else BTN2.add(Button.link(C.getInstagramURL(), (clans.size() > 1 ? C.getTag() + " | " : "") + "Instagram"));
                                        if (!BTN.isEmpty()) ARs.add(ActionRow.of(BTN));
                                        if (!BTN2.isEmpty()) ARs.add(ActionRow.of(BTN2));
                                    }

                                    embeds.add(E.build());
                                    if (!ARs.isEmpty()) M.editOriginalEmbeds(embeds).setComponents(ARs).queue();
                                    else M.editOriginalEmbeds(embeds).queue();
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            }
                        } else {
                            M.editOriginal(TL(event,"no-information")).queue();
                        }
                    });
                }
                if (event.getName().equals("Invite to clan...")) {
                    ClanInviteCommand CMD = new ClanInviteCommand(getClanOfUser(event.getUser()));
                    if (CMD.getTargetClan() != null) {
                        if (CMD.getMe(event.getUser().getIdLong()).hasPermission(ClanPermission.INVITE_MEMBER)) {
                            if (getClanOfUser(event.getTarget()) == null) {
                                event.deferReply(true).queue(M -> {
                                    CMD.MemberID = event.getTarget().getIdLong();
                                    event.getTarget().openPrivateChannel().queue(channel -> { // MY DM
                                        channel.sendMessage(InviteMember(event.getTarget(), event.getUser(), CMD)).queue(m -> {
                                            RB = getLanguageBundle(event.getUserLocale());
                                            M.editOriginal(TL(event,"clan-invite-send", "**" + event.getTarget().getEffectiveName() + "**")).queue();
                                        }, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, error -> {
                                            RB = getLanguageBundle(event.getUserLocale());
                                            M.editOriginal(TL(event,"clan-invite-send-fail", "**" + event.getTarget().getEffectiveName() + "**")).queue();
                                        }));
                                    });
                                });
                            } else {
                                event.reply(TL(event, "clan-invite-send-fail-already-clan", "**" + event.getTarget().getEffectiveName() + "**")).setEphemeral(true).queue();
                            }
                        } else {
                            event.reply(TL(event, "clan-manage-fail-permission", "ADD_MEMBERS")).setEphemeral(true).queue();
                        }
                    } else {
                        event.reply(TL(event,"error-you-are-not-in-a-clan")).setEphemeral(true).queue();
                    }
                }


                Clan clanGuild = getClanOfGuild(event.getGuild().getId());
                if (clanGuild != null) {
                    if (event.getName().contains("Kick")) {
                        event.deferReply(true).queue(M -> {
                            ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(clanGuild);
                            CMD.MyID = event.getUser().getIdLong();
                            CMD.MemberID = event.getTarget().getIdLong();
                            EmbedBuilder E = new EmbedBuilder();
                            E.setTitle(TL(M, "clan-remove-member"));
                            E.setAuthor(event.getTarget().getEffectiveName(), null, event.getTarget().getEffectiveAvatarUrl());
                            E.setThumbnail(clanGuild.getEmblemURL());
                            E.setColor(Color.red);
                            E.setDescription(":x: " + TL(event,"clan-remove-member-confirmation", "**" + event.getTarget().getEffectiveName() + "**", "**" + CMD.getTargetClan().getName() + "**"));
                            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(Button.danger(CMD.Command("clan-member-manage-confirm-remove"), "Kick"))).queue();
                        });
                    }
                    if (event.getName().contains("Manage")) {
                        event.deferReply(true).queue(M -> {
                            ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(clanGuild);
                            CMD.MyID = event.getUser().getIdLong();
                            CMD.MemberID = event.getTarget().getIdLong();
                            CMD.getTargetMember().ManageMemberUI(M, CMD);
                        });
                    }
                }
            } catch (Exception e) {
                replyException(event, e);
            }
        }
    }

    @Command(command = "profile")
    public static void slashProfile(InteractionHook M, Profile pf, String option) {
        if (!pf.hasPrivateProfile() || pf.getUser().equals(M.getInteraction().getUser()) || isTournamentManager(M.getInteraction().getUser())) {
            switch (option) {
                case "Basic" -> {
                    pf.ViewProfile(M);
                }
                case "Games" -> {
                    pf.ViewGamesStats(M);
                }
                case "RPG" -> {
                    pf.ViewRPG(M);
                }
                case "Power & Activity" -> {
                    pf.ViewPowerDetails(M, new FilterCommand(pf.getId(), "pf-power-filter"), new GamesCommand(pf.getId(), "pf-power-game"));
                }
                case "Trophies" -> {
                    pf.ViewTrophies(M);
                }
                case "Inventory" -> {
                    pf.ViewInventory(M, new PageViewerCommand(pf.getId(), "pf-inv-cp"));
                }
                case "Quests" -> {
                    pf.ViewQuests(M);
                }
                case "Achievements" -> {
                    pf.ViewAchievements(M);
                }
                case "Clan History" -> {
                    pf.ViewClanHistory(M);
                }
                case "Match History" -> {
                    pf.ViewHistory(M, new PageViewerCommand(pf.getId(), "pf-mlog-cp"), new FilterCommand(pf.getId(), "pf-mlog-filter"), new GamesCommand(pf.getId(), "pf-mlog-game"));
                }
                case "Tournament" -> {
                    pf.ViewTournaments(M, new PageViewerCommand(pf.getId(), "pf-tourn-cp"), new FilterCommand(pf.getId(), "pf-tourn-filter"), new GamesCommand(pf.getId(), "pf-tourn-game"));
                }
                case "License" -> {
                    pf.ViewLicenses(M);
                }
            }
        } else {
            M.editOriginal(":no_entry_sign: " + TL(M, "profile-private")).queue();
        }
    }
    @Command(command = "profile-manage")
    public static void slashProfileManage(InteractionHook M) {
        ProfileCommand CMD = new ProfileCommand(M.getInteraction().getUser());
        CMD.getProfile().ManageProfileUI(M, CMD);
    }
    @Command(command = "profile-character")
    public static void slashProfileCharacter(InteractionHook M, User user, Message.Attachment Logo) {
        if (Logo.getFileExtension() != null) {
            if (Logo.getFileExtension().contains("png")) {
                Profile P = Profile.get(user);
                Logo.getProxy().downloadToFile(P.getCharacter()).whenComplete((file, throwable) -> {
                    try {
                        if (ImageIO.read(file) != null) {
                            if (ImageHasTransparentPixel(file)) {
                                if (ImageIO.read(file).getHeight() > 3000) {
                                    ResizeImage(file, file, 0.4);
                                } else if (ImageIO.read(file).getHeight() > 2500) {
                                    ResizeImage(file, file, 0.5);
                                } else if (ImageIO.read(file).getHeight() > 2000) {
                                    ResizeImage(file, file, 0.6);
                                } else if (ImageIO.read(file).getHeight() > 1500) {
                                    ResizeImage(file, file, 0.8);
                                }
                                P.resetClanCards();
                                P.regeneratePersonalCard(M);
                                P.ManageProfileUI(M, new ProfileCommand(P));
                            } else {
                                file.delete();
                                M.editOriginal(TL(M,"image-fail-png")).queue();
                            }
                        } else {
                            file.delete();
                            M.editOriginal(TL(M,"image-fail-url")).queue();
                        }
                    } catch (Exception e) {
                        file.delete();
                        M.editOriginal(TL(M,"image-fail-url")).queue();
                    }
                });
            } else {
                M.editOriginal(TL(M, "image-fail-extension")).queue();
            }
        } else {
            M.editOriginal(TL(M, "image-fail-extension")).queue();
        }
    }


    public static Modal OpenFriendcodeUI(GenericInteractionCreateEvent e, ProfileCommand CMD) {
        Label input = Label.of("Strikers Friendcode (Numbers only) :", TextInput.create("profile-strikers-input", TextInputStyle.SHORT)
                .setPlaceholder(CMD.getProfile().getStrikersFriendcode() + " (PS: Enter full `0` to remove.)")
                .setMinLength(12)
                .setMaxLength(12)
                .setRequired(false)
                .build());
        Label input2 = Label.of("Xtreme Friendcode (Numbers only) :", TextInput.create("profile-xtreme-input", TextInputStyle.SHORT)
                .setPlaceholder(CMD.getProfile().getXtremeFriendcode() + " (PS: Enter full `0` to remove.)")
                .setMinLength(12)
                .setMaxLength(12)
                .setRequired(false)
                .build());
        Label input3 = Label.of("Switch Friendcode (Numbers only) :", TextInput.create("profile-switch-input", TextInputStyle.SHORT)
                .setPlaceholder(CMD.getProfile().getSwitchFriendcode() + " (PS: Enter full `0` to remove.)")
                .setMinLength(12)
                .setMaxLength(12)
                .setRequired(false)
                .build());
        return Modal.create(CMD.Command("profile-friendcode-code"), StopString(TL(e, "edit-friendcode-description"), 45))
                .addComponents(input, input2, input3)
                .build();
    }
    public static Modal OpenNationalityUI(GenericInteractionCreateEvent e, ProfileCommand CMD) {
        Label input = Label.of(TL(e, "Nationality") + ":", TextInput.create("profile-nationality-input", TextInputStyle.SHORT)
                .setPlaceholder("Français, Spanish, Brazilian, German...")
                .setMaxLength(16)
                .build());
        return Modal.create(CMD.Command("profile-nationality"), StopString(TL(e, "edit-nationality-description"), 45))
                .addComponents(input)
                .build();
    }
    }