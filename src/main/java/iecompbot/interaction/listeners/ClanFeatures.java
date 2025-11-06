package iecompbot.interaction.listeners;

import at.stefangeyer.challonge.model.enumeration.TournamentState;
import iecompbot.Constants;
import iecompbot.Utility;
import iecompbot.interaction.GuildReady;
import iecompbot.interaction.cmdbreakdown.InterclanCommand;
import iecompbot.interaction.cmdbreakdown.PageViewerCommand;
import iecompbot.interaction.cmdbreakdown.clan.*;
import iecompbot.interaction.cmdbreakdown.profile.FilterCommand;
import iecompbot.interaction.cmdbreakdown.profile.GamesCommand;
import iecompbot.interaction.cmdbreakdown.profile.ProfileCommand;
import iecompbot.objects.Nationality;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.clan.ClanRole;
import iecompbot.objects.clan.interclan.Interclan;
import iecompbot.objects.clan.interclan.Interclan_Duel;
import iecompbot.objects.clan.items.ClanPermission;
import iecompbot.objects.match.Game;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.server.Clanlist;
import iecompbot.objects.server.ServerInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.getLanguageBundle;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.Main.MainDirectory;
import static iecompbot.Utility.getComponentFullID;
import static iecompbot.img.ImgUtilities.CutTransparentBorders;
import static iecompbot.img.ImgUtilities.getHexValue;
import static iecompbot.interaction.Automation.*;
import static iecompbot.interaction.GuildReady.*;
import static iecompbot.objects.BotManagers.*;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static iecompbot.objects.clan.Clan.*;
import static iecompbot.objects.server.ServerInfo.getClanRolesAndTagServers;
import static my.utilities.lang.L10N.RB;
import static my.utilities.util.Utilities.*;

public class ClanFeatures extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getName().startsWith("interclan") || event.getName().startsWith("clan")) {
                        switch (event.getName()) {
                            case "clan-register" -> {
                                slashClanRegister(event, event.getUser());
                            }
                            case "clan-info" -> {
                                ClanManager CMD = new ClanManager(Clan.get(event.getOption("clan").getAsString()));
                                event.deferReply(CMD.InfoType.contains("Log") || CMD.InfoType.contains("Warn") || CMD.getTargetClan() == null).queue(M -> {
                                    try {
                                        CMD.InfoType = event.getOption("clan-info") != null ? event.getOption("clan-info").getAsString() : "Basic Information";
                                        if (CMD.getTargetClan() != null) {
                                            CMD.MyID = event.getUser().getIdLong();
                                            CMD.getTargetClan().ClanInfoUI(M, CMD);
                                        } else {
                                            M.editOriginalEmbeds(ClanChoice(M, CMD.InfoType).build()).setComponents(listClanChoices(CMD, "clan-choice-view", listOpenPaused())).queue();
                                        }
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            case "clan-manage" -> {
                                event.deferReply(true).queue(M -> {
                                    ClanManager CMD = new ClanManager(event);
                                    slashClanManage(M, CMD);
                                });
                            }
                            case "clan-invite_member" -> {
                                event.deferReply(true).queue(M -> {
                                    ClanInviteCommand CMD = new ClanInviteCommand(event);
                                    slashClanInvite(M, CMD);
                                });
                            }
                            case "clan-leave" -> {
                                event.deferReply(true).queue(M -> {
                                    ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(event);
                                    slashClanLeave(M, CMD);
                                });
                            }
                            case "clan-logo" -> {
                                event.deferReply(true).queue(M -> {
                                    setBotVariables();
                                    ClanManager CMD = new ClanManager(Clan.getClanOfUser(event.getUser()), event.getUser());
                                    slashClanLogo(M, CMD, event.getOption("logo").getAsAttachment());
                                });
                            }
                            case "clan-disband" -> {
                                event.deferReply(true).queue(M -> {
                                    ClanManager CMD = new ClanManager(event);
                                    slashClanDiscard(M, CMD);
                                });
                            }
                            case "clan-list" -> {
                                event.deferReply().queue(M -> {
                                    try {
                                        ServerInfo I = ServerInfo.get(event.getGuild());
                                        Clanlist E;
                                        if (event.getOption("game") != null) {
                                            E = new Clanlist(I != null ? I.getId() : 0, Game.get(event.getOption("game").getAsString()));
                                            E.setFooter(TL(M, "Filter") + ": " + Game.get(event.getOption("game").getAsString()));
                                        } else {
                                            E = new Clanlist(I != null ? I.getId() : 0, null);
                                        }
                                        M.editOriginalEmbeds(E.getEmbed(M, I).build()).queue();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            case "interclan-request" -> {
                                event.deferReply().queue(M -> {
                                    InterclanCommand CMD = new InterclanCommand(event);
                                    CMD.HostClan = Clan.getClanOfUser(event.getUser()).getId();
                                    CMD.JoinClan = Clan.get(event.getOption("clan").getAsString()).getId();
                                    CMD.RuleMatches = event.getOption("rule-matches").getAsInt();
                                    CMD.RuleDistribution = event.getOption("rule-distribution").getAsString();
                                    slashInterclanRequest(M, CMD);
                                });
                            }
                            case "interclan-manage" -> {
                                event.deferReply(true).queue(M -> {
                                    InterclanCommand CMD = new InterclanCommand(event, event.getOption("interclan").getAsLong());
                                    setInterclanManage(M, CMD);
                                });
                            }
                            case "interclan-info" -> {
                                event.deferReply().queue(M -> {
                                    Interclan IC = Interclan.get(event.getOption("interclan").getAsLong());
                                    if (IC != null) {
                                        InterclanViewer(IC, M);
                                    } else {
                                        M.editOriginal(TL(event, "interclan-not-exist")).queue();
                                    }
                                });
                            }
                        }
                    }
                    if (event.isFromGuild() && !event.isAcknowledged()) {
                        ClanManager CMD = new ClanManager(getClanOfGuild(event.getGuild().getId()));
                        if (CMD.getTargetClan() != null) {
                            CMD.MyID = event.getUser().getIdLong();
                            if (event.getName().contains("-info_channel")) {
                                event.deferReply().queue(M -> {
                                    if (CMD.getMe().hasPermission(M, ClanPermission.ADMINISTRATOR)) {
                                        TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
                                        if (hasPermissionInChannel(M, channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MANAGE_WEBHOOKS)) {
                                            try {
                                                ServerInfo I = ServerInfo.get(event.getGuild());
                                                I.Channels().setClanInfoCM(channel);
                                                I.Channels().Update();
                                                M.deleteOriginal().queue();
                                                CMD.getTargetClan().RefreshInfoChannel();
                                            } catch (Exception e) {
                                                replyException(M, e);
                                            }
                                        }

                                    }
                                });
                            } else if (event.getName().contains("-info")) {
                                event.deferReply().queue(M -> {
                                    try {
                                        CMD.InfoType = "Basic Information";
                                        CMD.getTargetClan().ClanInfoUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getName().contains("set")) {
                                if (event.getName().contains("-set_name")) {
                                    if (CMD.getMe().hasPermission(event, ClanPermission.ADMINISTRATOR)) {
                                        Label input = Label.of("Rename Clan:", TextInput.create("clan-name-input", TextInputStyle.SHORT)
                                                .setPlaceholder(CMD.getTargetClan().getName())
                                                .setRequiredRange(8, 20)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-name"), "[Edit] Rename " + CMD.getTargetClan().getName())
                                                .addComponents(input).build()).queue();
                                    }
                                } else if (event.getName().contains("-set_tag")) {
                                    if (CMD.getMe().hasPermission(event, ClanPermission.ADMINISTRATOR)) {
                                        Label input = Label.of("Change Tag:", TextInput.create("clan-tag-input", TextInputStyle.SHORT)
                                                .setPlaceholder(CMD.getTargetClan().getTag())
                                                .setRequiredRange(2, 6)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-tag"), "[Edit] Tag of " + CMD.getTargetClan().getName())
                                                .addComponents(input).build()).queue();
                                    }
                                } else if (event.getName().contains("-set_birthday")) {
                                    if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_INFORMATION)) {
                                        Label input = Label.of("Modify Birthday:", TextInput.create("clan-birthday-input", TextInputStyle.SHORT)
                                                .setPlaceholder(getDDMMYYYY(Instant.now()))
                                                .setRequiredRange(10, 10)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-birthday"), "[Edit] Birthday of " + CMD.getTargetClan().getName())
                                                .addComponents(input).build()).queue();
                                    }
                                } else if (event.getName().contains("-set_colorcode")) {
                                    if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_INFORMATION)) {
                                        Label input = Label.of("Modify Colorcode:", TextInput.create("clan-color-input", TextInputStyle.SHORT)
                                                .setPlaceholder(getHexValue(CMD.getTargetClan().getColor()))
                                                .setRequiredRange(7, 7)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-color"), "[Edit] Colorcode of " + CMD.getTargetClan().getName())
                                                .addComponents(input).build()).queue();
                                    }
                                } else if (event.getName().contains("-set_nationality")) {
                                    if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_INFORMATION)) {
                                        Label input = Label.of("Modify Nationality:", TextInput.create("clan-nationality-input", TextInputStyle.SHORT)
                                                .setPlaceholder("French, Brazilian, German, Spanish etc...")
                                                .setMaxLength(24)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-nationality"), "[Edit] Nationality of " + CMD.getTargetClan().getName())
                                                .addComponents(input).build()).queue();
                                    }
                                } else if (event.getName().contains("-set_requirements")) {
                                    if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_INFORMATION)) {
                                        Label input = Label.of("New Join Conditions:", TextInput.create("clan-requirements-input", TextInputStyle.PARAGRAPH)
                                                .setPlaceholder("To join " + CMD.getTargetClan().getName() + ", you must meet the following criterias:")
                                                .setMaxLength(256)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-requirements"), "[Edit] Requirements of " + CMD.getTargetClan().getName())
                                                .addComponents(input).build()).queue();
                                    }
                                } else if (event.getName().contains("-set_description")) {
                                    if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_INFORMATION)) {
                                        Label input = Label.of("New Description", TextInput.create("clan-description-input", TextInputStyle.PARAGRAPH)
                                                .setPlaceholder(StopString(CMD.getTargetClan().getDescription(), 100))
                                                .setMaxLength(256)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-description"), "[Edit] Description of " + CMD.getTargetClan().getName())
                                                .addComponents(input).build()).queue();
                                    }
                                }
                            } else if (event.getName().contains("-edit-license")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        CMD.getTargetClan().LicenseManageUI(M);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
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
                    if (event.getComponentId().startsWith("interclan") || event.getComponentId().startsWith("clan")) {
                        if (event.getComponentId().startsWith("clan-create")) {
                            if (event.getComponentId().startsWith("clan-create-accept")) {
                                if (isClanManager(event.getUser())) {
                                    MessageEmbed EM = event.getMessage().getEmbeds().getFirst();
                                    String tag = EM.getFields().getFirst().getValue();
                                    String name = EM.getFields().get(1).getValue();
                                    String nationality = EM.getFields().get(2).getValue();
                                    User captain = getUserByID(EM.getFields().get(3).getValue());
                                    String description = EM.getDescription();
                                    String color = getHexValue(EM.getColor());
                                    MessageEmbed.Thumbnail emblem = event.getMessage().getEmbeds().getFirst().getThumbnail();
                                    List<User> members = new ArrayList<>();
                                    for (String id : EM.getFields().get(4).getValue().split("\n")) members.add(getUserByID(id));
                                    if (getClanOfUser(captain) == null && members.stream().allMatch(m -> getClanOfUser(m.getIdLong()) == null)) {
                                        event.reply(":white_check_mark: **" + name + "** has been validated by " + event.getUser().getAsMention() + ".").queue();
                                        Clan C = new Clan(captain, name, tag, description, color, Nationality.get(nationality), "Open", members, emblem);
                                        GuildReady.LoadingTheCommands();
                                    } else {
                                        event.reply("This clan can't be created anymore. Some of the members probably joined a clan by then.").queue();
                                    }
                                }
                            }
                            else if (event.getComponentId().startsWith("clan-create-decline")) {
                                MessageEmbed EM = event.getMessage().getEmbeds().getFirst();
                                String name = EM.getFields().get(1).getValue();
                                User captain = getUserByID(EM.getFields().get(3).getValue());
                                event.reply(":x: **" + name + "** has been declined by " + event.getUser().getAsMention() + ".").queue();
                                if (getClanOfUser(captain) == null) sendPrivateMessage(captain, TL(event, "clan-register-decline", captain.getEffectiveName()));
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-clanrole")) {
                            ClanRoleManager CMD = new ClanRoleManager(event.getComponentId());
                            Clan C = Clan.get(CMD.ClanID);
                            if (event.getComponentId().startsWith("clan-clanrole-manage")) {
                                ClanRole CR = ClanRole.get(CMD.ClanRoleID);
                                if (!CR.isBuiltin(event)) {
                                    if (event.getComponentId().contains("name")) {
                                        Label input = Label.of(TL(event,"Name"), TextInput.create("name", TextInputStyle.SHORT)
                                                .setPlaceholder("Theorycraft")
                                                .setRequiredRange(5, 48)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-clanrole-manage"), "Create role")
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (event.getComponentId().contains("emoji")) {
                                        Label input = Label.of("Emoji", TextInput.create("emoji", TextInputStyle.SHORT)
                                                .setPlaceholder("U+1F600 or Custom Emoji ID")
                                                .setRequiredRange(5, 7)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-clanrole-manage"), "Create role")
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (event.getComponentId().contains("confirm-delete")) {
                                        event.deferEdit().queue(M -> {
                                            CR.Delete();
                                            M.editOriginal(TL(M,"clanrole-delete-success")).queue();
                                        });
                                    }
                                    else if (event.getComponentId().contains("delete")) {
                                        event.deferEdit().queue(M -> {
                                            EmbedBuilder E = new EmbedBuilder();
                                            E.setTitle(TL(M, "clanrole-manager"));
                                            E.setAuthor(CR.getName());
                                            E.setThumbnail(C.getEmblemURL());
                                            E.setColor(C.getColor());
                                            Button BTN = Button.danger(CMD.Command("clan-clanrole-manage-confirm-delete"), TL(M, "Delete")).withDisabled(CR.isBuiltin());
                                            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(BTN)).queue();
                                        });
                                    }
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-license-manage")) {
                            ClanManager CMD = new ClanManager(event.getComponentId());
                            Clan C = Clan.get(CMD.ClanID);
                            event.deferReply(true).queue(M -> {
                                if (CMD.getMe().hasPermission(M, ClanPermission.MANAGE_LICENSE)) {
                                    try {
                                        C.LicenseManageUI(M);
                                        List<ActionRow> rows = new ArrayList<>();
                                        List<SelectOption> options = new ArrayList<>();
                                        if (event.getComponentId().contains("bg")) {
                                            for (Item f : Item.get(Item.ItemType.LICENSE_BG)) {
                                                options.add(SelectOption.of(f.getName(), f.getId() + "").withEmoji(f.getEmoji().retrieve()));
                                                if (options.size() == 25 && rows.size() < 5) {
                                                    rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-license-manage-bg-" + rows.size()))
                                                            .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                    options = new ArrayList<>();
                                                    M.editOriginalComponents(rows).queue();
                                                }
                                                if (rows.size() == 5) break;
                                            }
                                            if (!options.isEmpty() && rows.size() < 5) {
                                                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("clan-license-manage-bg-5"))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build();
                                                rows.add(ActionRow.of(menu));
                                            }
                                        }
                                        else if (event.getComponentId().contains("fg")) {
                                            for (Item f : Item.get(Item.ItemType.LICENSE_FG)) {
                                                options.add(SelectOption.of(f.getName(), f.getId() + "").withEmoji(f.getEmoji().retrieve()));
                                                if (options.size() % 25 == 0) {
                                                    rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-license-manage-fg-" + rows.size()))
                                                            .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                    options = new ArrayList<>();
                                                    M.editOriginalComponents(rows).queue();
                                                }
                                                if (rows.size() == 5) break;
                                            }
                                            if (!options.isEmpty() && rows.size() < 5) {
                                                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("clan-license-manage-fg-5"))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build();
                                                rows.add(ActionRow.of(menu));
                                                M.editOriginalComponents(rows).queue();
                                            }
                                        }
                                        else if (event.getComponentId().contains("ry")) {
                                            for (Item f : Item.get(Item.ItemType.LICENSE_RY)) {
                                                options.add(SelectOption.of(f.getName(), f.getId() + "").withEmoji(f.getEmoji().retrieve()));
                                                if (options.size() == 25 && rows.size() < 5) {
                                                    rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-license-manage-ry-" + rows.size()))
                                                            .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                    options = new ArrayList<>();
                                                    M.editOriginalComponents(rows).queue();
                                                }
                                                if (rows.size() == 5) break;
                                            }
                                            if (!options.isEmpty() && rows.size() < 5) {
                                                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("clan-license-manage-ry-5"))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build();
                                                rows.add(ActionRow.of(menu));
                                            }
                                        }
                                        else if (event.getComponentId().contains("st")) {
                                            for (Item f : Item.get(Item.ItemType.LICENSE_ST)) {
                                                options.add(SelectOption.of(f.getName(), f.getId() + "").withEmoji(f.getEmoji().retrieve()));
                                                if (options.size() == 25 && rows.size() < 5) {
                                                    rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-license-manage-st-" + rows.size()))
                                                            .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                    options = new ArrayList<>();
                                                    M.editOriginalComponents(rows).queue();
                                                }
                                                if (rows.size() == 5) break;
                                            }
                                            if (!options.isEmpty() && rows.size() < 5) {
                                                rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-license-manage-st-5"))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                            }
                                        }
                                        else if (event.getComponentId().contains("sp")) {
                                            for (String ID : Constants.SponsoredServers) {
                                                Guild G = DiscordAccount.getGuildById(ID);
                                                if (G != null) {
                                                    options.add(SelectOption.of(G.getName(), G.getId()));
                                                    if (options.size() == 25 && rows.size() < 5) {
                                                        rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-license-manage-sp-" + rows.size()))
                                                                .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                                        options = new ArrayList<>();
                                                        M.editOriginalComponents(rows).queue();
                                                    }
                                                    if (rows.size() == 5) break;
                                                }
                                            }
                                            if (!options.isEmpty() && rows.size() < 5) {
                                                rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("clan-license-manage-sp-5"))
                                                        .setPlaceholder(options.getFirst().getLabel()).setRequiredRange(1, 1).addOptions(options).build()));
                                            }
                                        }
                                        if (rows.isEmpty()) M.editOriginal(TL(M, "profile-no-decoration")).queue();
                                        else M.editOriginalComponents(rows).queue();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("clan-member-manage")) {
                            ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(event.getComponentId());
                            ClanMember Target = ClanMember.ofClan(CMD.ClanID, CMD.MemberID);
                            Clan C = Clan.get(CMD.ClanID);
                            if (event.getComponentId().contains("number")) {
                                if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_MEMBER)) {
                                    Label input = Label.of(TL(event, "clan-number"), TextInput.create("number", TextInputStyle.SHORT)
                                            .setPlaceholder(Target.Number)
                                            .setMaxLength(3)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("clan-member-manage-number"), "Change Number of " + Target.getUser().getEffectiveName())
                                            .addComponents(input).build()).queue();
                                }
                            } else if (event.getComponentId().contains("nickname")) {
                                if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_MEMBER)) {
                                    Label input = Label.of(TL(event, "nickname"), TextInput.create("nickname", TextInputStyle.SHORT)
                                            .setPlaceholder(Target.getUser().getEffectiveName())
                                            .setRequiredRange(0, 32)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("clan-member-manage-nickname"), "Change Nickname of " + Target.getUser().getEffectiveName())
                                            .addComponents(input).build()).queue();
                                }

                            } else if (event.getComponentId().contains("confirm-remove")) {
                                event.deferEdit().setComponents(ActionRow.of(Button.secondary("nothing", TL(event, "Processing")))).queue(M -> {
                                    try {
                                        if (CMD.getMe().hasPermission(M, ClanPermission.KICK_MEMBER) && !Target.isCaptain()) {
                                            if (!Target.hasPermission(ClanPermission.KICK_MEMBER) || CMD.getMe().getHighestRolePosition() > Target.getHighestRolePosition()) {
                                                Target.Delete();
                                                M.editOriginal(TL(M,"clan-remove-member-success", "**" + Target.getUser().getEffectiveName() + "**", "**" + C.getEmojiFormatted() + " " + C.getName() + "**")).setReplace(true).queue();
                                                sendPrivateMessage(Target.getUser(), TL(Target.getProfile(), "clan-remove-member-success-dm", "**" + C.getEmojiFormatted() + " " + C.getName() + "**"));
                                                Target.getProfile().AddClanLog(Target);
                                                C.AddClanLog(CMD.getMe().getUser(), "[Member Kick]", "Kicked **" + Target.getUser().getEffectiveName() + "** from the clan.");
                                                if (!Target.isReinforcement()) C.LogClanUpdatesKickMember(Target);
                                                RefreshAllClanMembers(List.of(C));
                                            } else {
                                                M.editOriginal(TL(M,"clan-not-enough-authority")).queue();
                                            }
                                        }
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getComponentId().contains("remove")) {
                                event.deferReply(true).queue(M -> {
                                    if (CMD.getMe().hasPermission(event, ClanPermission.KICK_MEMBER) && !Target.isCaptain()) {
                                        EmbedBuilder E = new EmbedBuilder();
                                        E.setTitle(TL(M, "clan-remove-member"));
                                        E.setAuthor(Target.getUser().getEffectiveName(), null, Target.getUser().getEffectiveAvatarUrl());
                                        E.setThumbnail(C.getEmblemURL());
                                        E.setColor(Color.red);
                                        E.setDescription(":x: " + TL(event,"clan-remove-member-confirmation", "**" + Target.getUser().getEffectiveName() + "**", "**" + CMD.getTargetClan().getName() + "**"));
                                        M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(Button.danger(CMD.Command("clan-member-manage-confirm-remove"), "Kick"))).queue();
                                    }
                                });
                            } else if (event.getComponentId().contains("transfer")) {
                                if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_MEMBER)) {
                                    if (event.getComponentId().contains("transfer-ownership")) {
                                        event.deferReply(true).queue(M -> {
                                            if (CMD.getMe().isCaptain(M) || isClanManager(event.getUser())) {
                                                if (!isClanCaptainBan(CMD.MemberID)) {
                                                    if (!Target.isCaptain()) {
                                                        RB = getLanguageBundle(event.getUserLocale());
                                                        Button Yes = Button.success(CMD.Command("clan-member-manage-transfer-confirm"), TL(M, "yes"));
                                                        Button No = Button.danger(CMD.Command("clan-member-manage-transfer-deny"), TL(M, "no"));
                                                        M.editOriginal(CMD.getTargetClan().getCaptain().getUser().getAsMention() + ", " + TL(M, "clan-transfer-ownership", "**" + CMD.getTargetClan().getEmojiFormatted() + " " + CMD.getTargetClan().getName() + "**", Target.getUser().getAsMention())).setComponents(ActionRow.of(Yes, No)).queue();
                                                    } else {
                                                        M.editOriginal(TL(event, "clan-transfer-fail")).queue();
                                                    }
                                                } else {
                                                    M.editOriginal(TL(event, "clan-transfer-fail-ban")).queue();
                                                }
                                            }
                                        });
                                    } else {
                                        if (event.getUser() == C.getCaptain().getUser() || isClanManager(event.getUser())) {
                                            if (event.getComponentId().contains("confirm")) {
                                                event.deferEdit().setComponents(ActionRow.of(net.dv8tion.jda.api.components.buttons.Button.secondary("nothing", TL(event, "Processing")).asDisabled())).queue(M -> {
                                                    try {
                                                        ClanRole caprole = C.getCaptainRole();

                                                        C.getCaptain().removeRole(caprole);
                                                        Target.addRole(caprole);

                                                        M.editOriginal(TL(M, "clan-transfer-confirm-success", Target.getUser().getAsMention(), "**" + C.getEmojiFormatted() + " " + C.getName() + "**")).setReplace(true).queue();
                                                        sendPrivateMessage(Target.getUser(), TL(Target.getProfile(), "clan-transfer-confirm-success-2", "**" + C.getEmojiFormatted() + " " + C.getName() + "**"));
                                                        C.AddClanLog(event.getUser(), "Transfer Owner", "Transfered the ownership of the clan to **" + Target.getUser().getEffectiveName() + "**.");
                                                        C.LogClanUpdatesNewCaptain(Target.getUser());
                                                    } catch (Exception e) {
                                                        replyException(M, e);
                                                    }
                                                });
                                            }
                                            else if (event.getComponentId().contains("deny")) {
                                                event.deferEdit().flatMap(InteractionHook::deleteOriginal).queue();
                                            }
                                        } else {
                                            event.deferEdit().setContent(TL(event, "clan-not-captain")).queue();
                                        }
                                    }
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-leave")) {
                            ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(event.getComponentId());
                            Clan C = Clan.get(CMD.ClanID);
                            if (event.getUser() == CMD.getMe().getUser() || isClanManager(event.getUser())) {
                                if (event.getComponentId().contains("confirm")) {
                                    event.deferEdit().setComponents(ActionRow.of(net.dv8tion.jda.api.components.buttons.Button.secondary("nothing", TL(event, "Processing")).asDisabled())).queue(M -> {
                                        try {
                                            if (!CMD.getMe().isCaptain()) {
                                                CMD.getMe().Delete();
                                                if (!CMD.getMe().isReinforcement()) C.LogClanUpdatesKickMember(CMD.getMe());
                                                CMD.getMe().getProfile().AddClanLog(CMD.getMe());
                                                C.AddClanLog(CMD.getMe().getUser(), "[Member Leave]", TL(M,"clan-leave-confirm-success", "**" + CMD.getMe().getUser().getEffectiveName() + "**", "**" + C.getEmojiFormatted() + " " + C.getName() + "**"));
                                                M.editOriginal(TL(M,"clan-leave-confirm-success", "**" + CMD.getMe().getUser().getEffectiveName() + "**", "**" + C.getEmojiFormatted() + " " + C.getName() + "**")).setReplace(true).queue();
                                                sendPrivateMessage(C.getCaptain().getUser(), TL(Profile.get(C.getCaptain().getUser()),"clan-leave-confirm-success", "**" + CMD.getMe().getUser().getEffectiveName() + "**", "**" + C.getEmojiFormatted() + " " + C.getName() + "**"));
                                                RefreshAllClanMembers(List.of(C));
                                            } else {
                                                M.editOriginal(TL(M, "clan-leave-fail-captain")).setReplace(true).queue();
                                            }
                                        } catch (Exception e) {
                                            replyException(M, e);
                                        }
                                    });
                                }
                                else if (event.getComponentId().contains("deny")) {
                                    event.deferEdit().flatMap(InteractionHook::deleteOriginal).queue();
                                }
                            } else {
                                event.reply(TL(event, "clan-leave-fail")).setEphemeral(true).queue();
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-disband")) {
                            ClanManager CMD = new ClanManager(event.getComponentId());
                            Clan C = Clan.get(CMD.ClanID);
                            if (event.getComponentId().contains("confirm")) {
                                event.deferEdit().setComponents(ActionRow.of(net.dv8tion.jda.api.components.buttons.Button.secondary("nothing", TL(event, "Processing")).asDisabled())).queue(M -> {
                                    try {
                                        if (CMD.getMe().isCaptain(M)) {
                                            for (ServerInfo I : getClanRolesAndTagServers()) {
                                                if (C.areMembersInGuild(I.getGuild())) {
                                                    for (ClanMember m : C.getClanMembers()) C.RemoveTagFromMember(I, I.getGuild().getMemberById(m.getUserID()));
                                                    if (C.hasClanRole(I)) I.Roles().deleteRole(C.getRole(I), C.getEmojiFormatted() + " ");
                                                }
                                            }
                                            for (ClanMember CM : C.getClanMembers()) {
                                                sendPrivateMessage(CM.getUser(), TL(CM.getProfile(), "clan-disband-success-dm", "**" + C.getEmojiFormatted() + " " + C.getName() + "**"));
                                                CM.getProfile().AddClanLog(CM);
                                                CM.Delete();
                                            }
                                            C.setStatus("Closed");
                                            C.Update();
                                            C.LogClanUpdatesClanDisband();
                                            GuildReady.LoadingTheCommands();
                                            M.editOriginal(TL(M, "clan-disband-confirm-success", "**" + C.getEmojiFormatted() + " " + C.getName() + "**") + "!").setReplace(true);
                                        }
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                            else if (event.getComponentId().contains("deny")) {
                                event.deferEdit().flatMap(InteractionHook::deleteOriginal).queue();
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-inv")) {
                            ClanInviteCommand CMD = new ClanInviteCommand(event.getComponentId());
                            Clan C = Clan.get(CMD.ClanID);
                            User receiver = getUserByID(CMD.MemberID);
                            User inviter = getUserByID(CMD.MyID);
                            if (event.getUser() == receiver || isBotOwner(event.getUser())) {
                                if (Instant.now().isBefore(event.getMessage().getTimeCreated().toInstant().plus(3, ChronoUnit.DAYS))) {
                                    if (event.getComponentId().contains("clan-inv-confirm")) {
                                        if (CMD.getMe().hasPermission(event, ClanPermission.INVITE_MEMBER)) {
                                            if ((Clan.getClanOfUser(CMD.MemberID) == null && !CMD.isReinforcement) || (Clan.getReinforcementOfUser(CMD.MemberID) == null && CMD.isReinforcement)) {
                                                event.deferEdit().setComponents(ActionRow.of(net.dv8tion.jda.api.components.buttons.Button.secondary("nothing", TL(event, "Processing")).asDisabled())).queue(M -> {
                                                    try {

                                                        ClanMember CM = new ClanMember(C.getId(), CMD.MemberID, "00", receiver.getEffectiveName(), new ArrayList<>(), CMD.Contract);
                                                        CM.updateMainClan(!CMD.isReinforcement);

                                                        Profile pf = CM.getProfile();
                                                        pf.RefreshProfileInformation(event);

                                                        M.editOriginalComponents(ActionRow.of(net.dv8tion.jda.api.components.buttons.Button.secondary("nothing", TL(event, "Done")).withEmoji(Emoji.fromUnicode("U+2705")).asDisabled())).queue();
                                                        C.getClanMembers().add(CM);
                                                        C.AddClanLog(CMD.getMe().getUser(), "[Member Invite]", "Invited **" + receiver.getEffectiveName() + "** from the clan.");
                                                        sendPrivateMessage(inviter, TL(pf, "clan-invite-confirm-success", "**" + receiver.getEffectiveName() + "**", "**" + C.getEmojiFormatted() + " " + C.getName() + "**"));
                                                        C.LogClanUpdatesNewMember(CM);
                                                        if (C.getCaptain().getUserID() != CMD.MyID) sendPrivateMessage(C.getCaptain().getUser(), TL(Profile.get(C.getCaptain().getUser()), "clan-invite-confirm-success-owner", "**" + receiver.getEffectiveName() + "**", "**" + C.getEmojiFormatted() + " " + C.getName() + "**", inviter.getAsMention()));
                                                        RefreshAllClanMembers(List.of(C));
                                                    } catch (Exception e) {
                                                        replyException(M, e);
                                                    }
                                                });
                                            } else {
                                                event.reply(TL(event, "clan-invite-send-fail-already-clan", "**" + receiver.getEffectiveName() + "**")).setEphemeral(true).queue();
                                            }
                                        } else {
                                            event.reply(TL(event, "clan-invite-confirm-fail-sender-no-power")).setEphemeral(true).queue();
                                        }
                                    } else if (event.getComponentId().contains("clan-inv-deny")) {
                                        event.deferEdit().flatMap(InteractionHook::deleteOriginal).queue();
                                        sendPrivateMessage(inviter, TL(Profile.get(CMD.MyID), "clan-invite-deny-success", "**" + event.getUser().getEffectiveName() + "**", "**" + C.getEmojiFormatted() + " " + C.getName() + "**"));
                                    }
                                } else {
                                    event.deferEdit().flatMap(M -> M.editOriginal(TL(event, "clan-invite-confirm-expired"))).queue();
                                }
                            } else {
                                event.reply(TL(event, "clan-invite-deny-fail")).setEphemeral(true).queue();
                            }
                        }
                        else if (event.getComponentId().startsWith("interclan-manage")) {
                            InterclanCommand CMD = new InterclanCommand(event.getComponentId());
                            if (event.getComponentId().contains("add-duel")) {
                                Label P1 = Label.of(CMD.getHostClan().getName() + "'s User ID", TextInput.create("clan-1", TextInputStyle.SHORT)
                                        .setPlaceholder("508331399149912088")
                                        .setRequiredRange(10, 40)
                                        .build());
                                Label P2 = Label.of(CMD.getJoinClan().getName() + "'s User ID", TextInput.create("clan-2", TextInputStyle.SHORT)
                                        .setPlaceholder("363666347504173066")
                                        .setRequiredRange(10, 40)
                                        .build());
                                Label Games = Label.of("Game name", StringSelectMenu.create("game")
                                        .addOptions(Game.getSelectOptions(event, null)).build());
                                if (isTournamentManager(event.getUser())) {
                                    Label P1Score = Label.of(CMD.getHostClan().getName() + "'s Score", TextInput.create("clan-1-score", TextInputStyle.SHORT)
                                            .setPlaceholder("0")
                                            .setRequiredRange(1, 2)
                                            .build());
                                    Label P2Score = Label.of(CMD.getJoinClan().getName() + "'s Score", TextInput.create("clan-2-score", TextInputStyle.SHORT)
                                            .setPlaceholder("0")
                                            .setRequiredRange(1, 2)
                                            .build());
                                    event.replyModal(Modal.create(CMD.Command("interclan-add-duel"), "Add Duel")
                                            .addComponents(P1, P1Score, P2Score, P2, Games)
                                            .build()).queue();
                                } else {
                                    event.replyModal(Modal.create(CMD.Command("interclan-add-duel"), "Add Duel")
                                            .addComponents(P1, P2, Games)
                                            .build()).queue();
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("interclan-duel-edit")) {
                            if (event.getComponentId().contains("delete")) {
                                InterclanCommand CMD = new InterclanCommand(event.getComponentId());
                                event.deferEdit().queue(M -> {
                                    CMD.I.getDuel(CMD.Page).Delete();
                                    M.editOriginal(TL(event, "Done") + "!").queue();
                                });
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
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("interclan") || event.getComponentId().startsWith("clan")) {
                        if (event.getComponentId().startsWith("clan-choice")) {
                            if (event.getComponentId().startsWith("clan-choice-view")) {
                                event.deferReply().queue(M -> {
                                    try {
                                        ClanManager CMD = new ClanManager(event.getComponentId());
                                        CMD.ClanID = Long.parseLong(event.getValues().getFirst());
                                        Clan.get(CMD.ClanID).ClanInfoUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getComponentId().startsWith("clan-choice-manage")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        ClanManager CMD = new ClanManager(event.getComponentId());
                                        CMD.ClanID = Long.parseLong(event.getValues().getFirst());
                                        Clan.get(CMD.ClanID).ManageClanUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getComponentId().startsWith("clan-choice-disband")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        ClanManager CMD = new ClanManager(event.getComponentId());
                                        CMD.ClanID = Long.parseLong(event.getValues().getFirst());
                                        Clan.get(CMD.ClanID).ClanDisbandUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getComponentId().startsWith("clan-choice-logo")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        ClanManager CMD = new ClanManager(event.getComponentId());
                                        CMD.ClanID = Long.parseLong(event.getValues().getFirst());
                                        Clan.get(CMD.ClanID).changeLogo(CMD, M, event.getMessage().getEmbeds().getFirst().getImage().getProxyUrl());
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getComponentId().startsWith("clan-choice-inv")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        ClanInviteCommand CMD = new ClanInviteCommand(event.getComponentId());
                                        CMD.ClanID = Long.parseLong(event.getValues().getFirst());
                                        Clan.get(CMD.ClanID).InviteMemberUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getComponentId().startsWith("clan-choice-leave")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(event.getComponentId());
                                        CMD.ClanID = Long.parseLong(event.getValues().getFirst());
                                        Clan.get(CMD.ClanID).ClanLeaveUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getComponentId().startsWith("clan-choice-ic-host")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        InterclanCommand CMD = new InterclanCommand(event.getComponentId());
                                        CMD.HostClan = Long.parseLong(event.getValues().getFirst());
                                        if (CMD.getJoinClan() == null) M.editOriginalEmbeds(ClanChoice(M, "Interclaning " + CMD.getJoinClan().getName()).build()).setComponents(listClanChoices(CMD, "clan-choice-ic-join", Clan.listOpenPaused().stream().filter(c -> c.getId() != CMD.HostClan).collect(Collectors.toList()))).queue();
                                        else Clan.get(CMD.HostClan).InterclanRequestUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else if (event.getComponentId().startsWith("clan-choice-ic-join")) {
                                event.deferReply(true).queue(M -> {
                                    try {
                                        InterclanCommand CMD = new InterclanCommand(event.getComponentId());
                                        CMD.JoinClan = Long.parseLong(event.getValues().getFirst());
                                        Clan.get(CMD.HostClan).InterclanRequestUI(M, CMD);
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-manage")) {
                            if (event.getComponentId().contains("config")) {
                                ClanManager CMD = new ClanManager(event.getComponentId());
                                Clan C = Clan.get(CMD.ClanID);
                                if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_INFORMATION)) {
                                    List<String> SelectedOptions = event.getValues();
                                    if (SelectedOptions.getFirst().startsWith("name")) {
                                        Label input = Label.of(TL(event, "Name"), TextInput.create("clan-name-input", TextInputStyle.SHORT)
                                                .setPlaceholder(C.getName()).setRequiredRange(8, 32).build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-name"), StopString(TL(event, "clan-edit-name-description"), 45))
                                                .addComponents(input).build()).queue();

                                    }
                                    else if (SelectedOptions.getFirst().startsWith("add-member")) {
                                        if (isClanManager(event.getUser())) {
                                            Label input = Label.of("User ID", TextInput.create("userid", TextInputStyle.SHORT)
                                                    .setPlaceholder(event.getUser().getId()).setRequiredRange(2, 6).build());
                                            event.replyModal(Modal.create(CMD.Command("clan-manage-add-member"), "[Admin] Force Add Member")
                                                    .addComponents(input).build()).queue();
                                        }
                                    }
                                    else if (SelectedOptions.getFirst().startsWith("tag")) {
                                        Label input = Label.of(TL(event, "Tag"), TextInput.create("clan-tag-input", TextInputStyle.SHORT)
                                                .setPlaceholder(C.getTag()).setRequiredRange(2, 6).build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-tag"), StopString(TL(event, "clan-edit-tag-description"), 45))
                                                .addComponents(input).build()).queue();

                                    }
                                    else if (SelectedOptions.getFirst().startsWith("description")) {
                                        Label input = Label.of(TL(event, "Description"), TextInput.create("clan-description-input", TextInputStyle.PARAGRAPH)
                                                .setPlaceholder(StopString(C.getDescription(), 100))
                                                .setRequiredRange(1,256).build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-description"), StopString(TL(event, "clan-edit-description-description"), 45))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (SelectedOptions.getFirst().startsWith("requirements")) {
                                        Label input = Label.of(TL(event, "Join_Requirements"), TextInput.create("clan-requirements-input", TextInputStyle.PARAGRAPH)
                                                .setPlaceholder("To join " + C.getName() + ", you must meet the following criterias:")
                                                .setMaxLength(256).build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-requirements"), StopString(TL(event, "clan-edit-join_req-description"), 45))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (SelectedOptions.getFirst().startsWith("nationality")) {
                                        Label input = Label.of(TL(event, "Nationality"), TextInput.create("clan-nationality-input", TextInputStyle.SHORT)
                                                .setPlaceholder("French, Spanish, German, Brazilian etc...")
                                                .setMaxLength(32).build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-nationality"), StopString(TL(event, "clan-edit-nationality-description"), 45))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (SelectedOptions.getFirst().startsWith("color")) {
                                        Label input = Label.of(TL(event, "Color"), TextInput.create("clan-color-input", TextInputStyle.SHORT)
                                                .setPlaceholder(getHexValue(C.getColor()))
                                                .setRequiredRange(7, 7).build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-color"), StopString(TL(event, "clan-edit-colorcode-description"), 45))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (SelectedOptions.getFirst().startsWith("logo")) {
                                        Label input = Label.of("Image URL", TextInput.create("clan-logo-input", TextInputStyle.SHORT)
                                                .setPlaceholder("https://cdn.discordapp.com/attachments/1285728179989774489/1330869958481149952/logo.png")
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-manage-logo"), StopString(TL(event, "clan-edit-logo-description"), 45))
                                                .addComponents(input).build()).queue();
                                    }
                                    else if (SelectedOptions.getFirst().startsWith("server")) {
                                        if (!C.isClanServerTaken(event.getGuild())) {
                                            event.deferReply().queue(M -> {
                                                if (isAdmin(M ,event.getMember())) {
                                                    try {
                                                        if (C.getClanServerID() != null) {
                                                            Guild old = DiscordAccount.getGuildById(C.getClanServerID());
                                                            if (old != null)
                                                                old.updateCommands().addCommands().queue();
                                                        }
                                                        C.setClanServerID(event.getGuild().getIdLong());
                                                        C.ManageClanUI(M, CMD);
                                                        RefreshAllPrivateCMDs();
                                                    } catch (Exception e) {
                                                        replyException(M, e);
                                                    }
                                                }
                                            });
                                        } else {
                                            event.reply(TL(event, "clan-update-clan-server-fail")).setEphemeral(true).queue();
                                        }
                                    }
                                    else if (SelectedOptions.getFirst().startsWith("media")) {
                                        TextInput.Builder link = TextInput.create("link-input", TextInputStyle.SHORT).setRequiredRange(8, 128).setRequired(false);
                                        if (SelectedOptions.getFirst().contains("twitter")) {
                                            link.setPlaceholder("https://twitter.com/" + C.getName().replaceAll(" ", "").toLowerCase());
                                            event.replyModal(Modal.create(CMD.Command("clan-manage-twitter"), "Twitter").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                        } else if (SelectedOptions.getFirst().contains("website")) {
                                            link.setPlaceholder("https://" + C.getName().replaceAll(" ", "").toLowerCase() + ".com/");
                                            event.replyModal(Modal.create(CMD.Command("clan-manage-website"), "Website").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                        } else if (SelectedOptions.getFirst().contains("youtube")) {
                                            link.setPlaceholder("https://www.youtube.com/@" + C.getName().replaceAll(" ", "").toLowerCase());
                                            event.replyModal(Modal.create(CMD.Command("clan-manage-youtube"), "YouTube").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                        } else if (SelectedOptions.getFirst().contains("twitch")) {
                                            link.setPlaceholder("https://www.twitch.tv/" + C.getName().replaceAll(" ", "").toLowerCase());
                                            event.replyModal(Modal.create(CMD.Command("clan-manage-twitch"), "Twitch").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                        } else if (SelectedOptions.getFirst().contains("instagram")) {
                                            link.setPlaceholder("https://www.instagram.com/@" + C.getName().replaceAll(" ", "").toLowerCase());
                                            event.replyModal(Modal.create(CMD.Command("clan-manage-instagram"), "Instagram").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                        } else if (SelectedOptions.getFirst().contains("discord")) {
                                            link.setPlaceholder("https://discord.gg/" + C.getName().toLowerCase().replaceAll(" ", "").toLowerCase());
                                            event.replyModal(Modal.create(CMD.Command("clan-manage-discord"), "Discord").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                        } else if (SelectedOptions.getFirst().contains("tiktok")) {
                                            link.setPlaceholder("https://www.tiktok.com/" + C.getName().toLowerCase().replaceAll(" ", "").toLowerCase());
                                            event.replyModal(Modal.create(CMD.Command("clan-manage-tiktok"), "Tiktok").addComponents(Label.of(TL(event,"Link"), link.build())).build()).queue();
                                        }
                                    }
                                }
                            }
                            else if (event.getComponentId().contains("member")) {
                                ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(event.getComponentId());
                                if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_MEMBER, ClanPermission.KICK_MEMBER)) {
                                    event.deferReply(true).queue(M -> {
                                        CMD.MemberID = Long.parseLong(event.getValues().getFirst());
                                        CMD.getTargetMember().ManageMemberUI(M, CMD);
                                    });
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-license-manage")) {
                            ClanManager CMD = new ClanManager(event.getComponentId());
                            if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_LICENSE)) {
                                Clan C = Clan.get(CMD.ClanID);
                                List<String> SelectedOptions = event.getValues();
                                event.deferEdit().queue(M -> {
                                    if (event.getComponentId().contains("sp")) {
                                        C.setSponsor(Long.parseLong(SelectedOptions.getFirst()));
                                        C.AddClanLog(event.getUser(), "[License]", "Modified the clan's license sponsor to **" + SelectedOptions.getFirst() + "**.");
                                    } else {
                                        Item I = Item.get(Long.parseLong(SelectedOptions.getFirst()));
                                        if (event.getComponentId().contains("bg")) {
                                            C.setCardBackground(I);
                                            C.AddClanLog(event.getUser(), "[License]", "Modified the clan's license background to **" + I.getName() + "**.");
                                        } else if (event.getComponentId().contains("fg")) {
                                            C.setCardForeground(I);
                                            C.AddClanLog(event.getUser(), "[License]", "Modified the clan's license foreground to **" + I.getName() + "**.");
                                        } else if (event.getComponentId().contains("ry")) {
                                            C.setCardRay(I);
                                            C.AddClanLog(event.getUser(), "[License]", "Modified the clan's license ray to **" + I.getName() + "**.");
                                        } else if (event.getComponentId().contains("st")) {
                                            C.setCardStrike(I);
                                            C.AddClanLog(event.getUser(), "[License]", "Modified the clan's license strike to **" + I.getName() + "**.");
                                        }
                                    }
                                    C.Update();
                                    C.LicenseManageUI(M);
                                    for (ClanMember CM : C.getClanMembers()) CM.resetCards();
                                });
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-clanrole-manage")) {
                            ClanRoleManager CMD = new ClanRoleManager(event.getComponentId());
                            if (CMD.getMe().hasPermission(event, ClanPermission.MANAGE_ROLE)) {
                                Clan C = Clan.get(CMD.ClanID);
                                if (event.getComponentId().contains("permissions")) {
                                    event.deferEdit().queue(M -> {
                                        ClanRole R = ClanRole.get(CMD.ClanRoleID);
                                        R.emptyPermissions();
                                        int i = 1;
                                        for (String S : event.getValues()) {
                                            R.setPermission(ClanPermission.valueOf(S), i++);
                                        }
                                        R.Update();
                                        C.EditClanRoleUI(CMD, M, R);
                                    });
                                } else if (event.getComponentId().contains("select")){
                                    if (event.getValues().getFirst().contains("new-role")) {
                                        Label name = Label.of(TL(event,"Name"), TextInput.create("name", TextInputStyle.SHORT)
                                                .setPlaceholder("Theorycraft")
                                                .setRequiredRange(4, 48)
                                                .build());
                                        Label emoji = Label.of("Unicode Emoji", TextInput.create("emoji", TextInputStyle.SHORT)
                                                .setPlaceholder("U+1F600")
                                                .setRequiredRange(5, 7)
                                                .build());
                                        event.replyModal(Modal.create(CMD.Command("clan-clanrole-new"), "Create role")
                                                .addComponents(name, emoji).build()).queue();
                                    } else {
                                        event.deferReply(true).queue(M -> {
                                            CMD.ClanRoleID = Long.parseLong(event.getValues().getFirst());
                                            ClanRole CR = ClanRole.get(CMD.ClanRoleID);
                                            if (!CR.isBuiltin(M)) C.EditClanRoleUI(CMD, M, CR);
                                        });
                                    }
                                }
                            }
                        }
                        else if (event.getComponentId().startsWith("clan-member-manage")) {
                            ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(event.getComponentId());
                            if (event.getComponentId().contains("roles")) {
                                event.deferEdit().queue(M -> {
                                    ClanMember Target = ClanMember.ofClan(CMD.ClanID, CMD.MemberID);
                                    Clan C = Clan.get(CMD.ClanID);
                                    if (CMD.getMe().hasPermission(ClanPermission.MANAGE_MEMBER)) {
                                        for (ClanRole OldTask : new ArrayList<>(Target.getClanRoles())) {
                                            if (OldTask.canGiveOrRemove(CMD.getMe())) {
                                                Target.removeRole(OldTask);
                                            }
                                        }
                                        for (String task : event.getValues()) {
                                            ClanRole CR = ClanRole.get(Long.parseLong(task));
                                            if (CR != null && CR.canGiveOrRemove(CMD.getMe())) {
                                                Target.addRole(CR);
                                            }
                                        }
                                        C.AddClanLog(event.getUser(), "Clan Roles", "Modified the clan tasks of **" + Target.getUser().getEffectiveName());
                                        Target.ManageMemberUI(M, CMD);
                                    }
                                });
                            }
                        }
                        else if (event.getComponentId().contains("clan-power")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    Message OG = M.retrieveOriginal().submit().get();
                                    FilterCommand CMD2 = new FilterCommand(getComponentFullID(OG, "clan-power-filter"));
                                    GamesCommand CMD3 = new GamesCommand(getComponentFullID(OG, "clan-power-game"));
                                    Clan C = Clan.get(CMD2.ID);

                                    if (event.getComponentId().startsWith("clan-power-filter")) {
                                        CMD2.Filter = event.getValues();
                                    } else if (event.getComponentId().startsWith("clan-power-game")) {
                                        CMD3.Games = CMD3.parseGamesToList(event.getValues().toString());
                                    }
                                    C.ViewPowerDetails(M, CMD2, CMD3);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().contains("clan-tourn")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    Message OG = M.retrieveOriginal().submit().get();
                                    PageViewerCommand CMD1 = new PageViewerCommand(getComponentFullID(OG, "clan-tourn-cp"));
                                    FilterCommand CMD2 = new FilterCommand(getComponentFullID(OG, "clan-tourn-filter"));
                                    GamesCommand CMD3 = new GamesCommand(getComponentFullID(OG, "clan-tourn-game"));

                                    Clan C = Clan.get(CMD1.ID);
                                    CMD1.Page = 1;
                                    if (event.getComponentId().startsWith("clan-tourn-cp")) {
                                        CMD1.Page = Integer.parseInt(event.getValues().getFirst());
                                    } else if (event.getComponentId().startsWith("clan-tourn-filter")) {
                                        CMD2.Filter = event.getValues();
                                    } else if (event.getComponentId().startsWith("clan-tourn-game")) {
                                        CMD3.Games = CMD3.parseGamesToList(event.getValues().toString());
                                    }
                                    C.ViewTournaments(M, CMD1, CMD2, CMD3);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().contains("clan-logs")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    Message OG = M.retrieveOriginal().submit().get();
                                    PageViewerCommand CMD1 = new PageViewerCommand(getComponentFullID(OG, "clan-logs-cp"));
                                    Clan C = Clan.get(CMD1.ID);
                                    CMD1.Page = 1;
                                    if (event.getComponentId().startsWith("clan-logs-cp")) {
                                        CMD1.Page = Integer.parseInt(event.getValues().getFirst());
                                    }
                                    C.ViewClanLogs(M, CMD1);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().contains("clan-troph")) {
                            event.deferEdit().queue(M -> {
                                try {
                                    Message OG = M.retrieveOriginal().submit().get();
                                    PageViewerCommand CMD1 = new PageViewerCommand(getComponentFullID(OG, "clan-troph-cp"));
                                    Clan C = Clan.get(CMD1.ID);
                                    CMD1.Page = 1;
                                    if (event.getComponentId().startsWith("clan-troph-cp")) {
                                        CMD1.Page = Integer.parseInt(event.getValues().getFirst());
                                    }
                                    C.ViewTrophies(M, CMD1);
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("interclan-dual")) {
                            InterclanCommand CMD = new InterclanCommand(event.getComponentId());
                            if (event.getComponentId().startsWith("interclan-dual-select")) {
                                event.deferReply(true).queue(M -> {
                                    CMD.Page = Integer.parseInt(event.getValues().getFirst());
                                    Interclan_Duel D = CMD.I.getDuel(Long.parseLong(event.getValues().getFirst()));
                                    InterclanDuelManager(CMD, D, M);
                                });
                            } else if (event.getComponentId().startsWith("interclan-dual-edit")) {
                                event.deferEdit().queue(M -> {
                                    Interclan_Duel D = CMD.I.getDuels().get(CMD.Page);
                                    if (event.getComponentId().contains("game")) {
                                        D.setGame(Game.get(event.getValues().getFirst()));
                                    } else if (event.getComponentId().contains("p1")) {
                                        D.setP1ID(Long.parseLong(event.getValues().getFirst()));
                                    } else if (event.getComponentId().contains("p2")) {
                                        D.setP2ID(Long.parseLong(event.getValues().getFirst()));
                                    }
                                    D.Update();
                                    InterclanDuelManager(CMD, D, M);
                                });

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
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("interclan") || event.getComponentId().startsWith("clan")) {
                        if (event.getComponentId().startsWith("interclan-dual")) {
                            InterclanCommand CMD = new InterclanCommand(event.getComponentId());
                            if (event.getComponentId().startsWith("interclan-dual-edit")) {
                                event.deferEdit().queue(M -> {
                                    Interclan_Duel D = CMD.I.getDuels().get(CMD.Page);
                                    if (event.getComponentId().contains("p1")) {
                                        D.setP1ID(event.getMentions().getUsers().getFirst().getIdLong());
                                    } else if (event.getComponentId().contains("p2")) {
                                        D.setP2ID(event.getMentions().getUsers().getFirst().getIdLong());
                                    }
                                    D.Update();
                                    InterclanDuelManager(CMD, D, M);
                                });
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
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getModalId().startsWith("interclan") || event.getModalId().startsWith("clan")) {
                        if (event.getModalId().startsWith("clan-manage")) {
                            ClanManager CMD = new ClanManager(event.getModalId());
                            Clan C = Clan.get(CMD.ClanID);
                            event.deferEdit().queue(M -> {
                                if (CMD.getMe().hasPermission(M, ClanPermission.MANAGE_INFORMATION)) {
                                    try {
                                        if (event.getModalId().contains("clan-manage-name")) {
                                            if (isValidClanName(event.getValue("clan-name-input").getAsString())) {
                                                if (!event.getValue("clan-name-input").getAsString().equals(C.getName())) {
                                                    C.setName(C.getName(), event.getValue("clan-name-input").getAsString(), event.getUser());
                                                    C.ManageClanUI(M, CMD);
                                                    for (ClanMember CM : C.getClanMembers()) CM.resetCards();
                                                    GuildReady.LoadingTheCommands();
                                                } else event.deferEdit().queue();
                                            } else {
                                                M.editOriginal(":x: " + TL(event, "error-illegal-character")).queue();
                                            }
                                        } else if (event.getModalId().contains("clan-manage-tag")) {
                                            Checks.matches(event.getValue("clan-tag-input").getAsString(), Checks.ALPHANUMERIC_WITH_DASH, "Name");
                                            C.setTag(C.getTag(), event.getValue("clan-tag-input").getAsString(), event.getUser());
                                            C.ManageClanUI(M, CMD);
                                        } else if (event.getModalId().contains("clan-manage-description")) {
                                            C.setDescription(event.getValue("clan-description-input").getAsString());
                                            C.AddClanLog(event.getUser(), "Description", "Modified the clan's description.");
                                            C.ManageClanUI(M, CMD);
                                        } else if (event.getModalId().contains("clan-manage-requirements")) {
                                            C.setRequirements(event.getValue("clan-requirements-input").getAsString());
                                            C.AddClanLog(event.getUser(), "Requirements", "Modified the clan's join requirements.");
                                            C.ManageClanUI(M, CMD);
                                        } else if (event.getModalId().contains("clan-manage-nationality")) {
                                            C.setNationality(Nationality.get(event.getValue("clan-nationality-input").getAsString()));
                                            C.AddClanLog(event.getUser(), "Nationality", "Modified the clan's nationality to **" + C.getNationality() + "**.");
                                            C.ManageClanUI(M, CMD);
                                        } else if (event.getModalId().contains("clan-manage-add-member")) {
                                            if (isClanManager(event.getUser())) {
                                                M.editOriginal(TL(M, "Done") + "!").queue();
                                                User U = getUserByID(event.getValue("userid").getAsString());
                                                ClanMember CM = new ClanMember(C.getId(), U.getIdLong());
                                                C.LogClanUpdatesNewMember(CM);
                                            }
                                        } else if (event.getModalId().contains("clan-manage-color")) {
                                            if (isColorcodeValid(event.getValue("clan-color-input").getAsString())) {
                                                C.setColor(Color.decode(event.getValue("clan-color-input").getAsString()));
                                                C.AddClanLog(event.getUser(), "Colorcode", "Modified the clan's colorcode to **" + event.getValue("clan-color-input").getAsString() + "**.");
                                                C.ManageClanUI(M, CMD);
                                                for (ServerInfo I : ServerInfo.list(true)) {
                                                    if (C.areMembersInGuild(I.getGuild())) {
                                                        I.Roles().setRoleColor(C.getRole(I), C.getColor(), C.getEmojiFormatted());
                                                    }
                                                }
                                                for (ClanMember CM : C.getClanMembers()) CM.resetCards();
                                                C.LogClanUpdatesColorChange(event.getValue("clan-color-input").getAsString());
                                            }
                                        } else if (event.getModalId().contains("clan-manage-logo")) {
                                            C.changeLogo(CMD, M, event.getValue("clan-logo-input").getAsString());
                                        } else if (event.getModalId().contains("clan-manage-twitter")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && (event.getValue("link-input").getAsString().contains("twitter.com") || event.getValue("link-input").getAsString().contains("x.com"))) {
                                                C.setTwitterURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                C.AddClanLog(event.getUser(), "Twitter", "Modified the clan's Twitter link to " + C.getDiscordURL() + ".");
                                                C.ManageClanUI(M, CMD);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("clan-manage-twitch")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("twitch.tv")) {
                                                C.setTwitchURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                C.AddClanLog(event.getUser(), "Twitch", "Modified the clan's Twitch link to " + C.getDiscordURL() + ".");
                                                C.ManageClanUI(M, CMD);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("clan-manage-website")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("https://")) {
                                                C.setWebsiteURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                C.AddClanLog(event.getUser(), "Website", "Modified the clan's Website link to " + C.getDiscordURL() + ".");
                                                C.ManageClanUI(M, CMD);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("clan-manage-youtube")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("youtu")) {
                                                C.setYouTubeURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                C.AddClanLog(event.getUser(), "YouTube", "Modified the clan's YouTube link to " + C.getDiscordURL() + ".");
                                                C.ManageClanUI(M, CMD);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("clan-manage-instagram")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("instagram.com")) {
                                                C.setInstagramURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                C.AddClanLog(event.getUser(), "Instagram", "Modified the clan's Instagram link to " + C.getDiscordURL() + ".");
                                                C.ManageClanUI(M, CMD);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("clan-manage-discord")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("discord.gg")) {
                                                C.setDiscordURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                C.AddClanLog(event.getUser(), "Discord", "Modified the clan's Discord link to " + C.getDiscordURL() + ".");
                                                C.ManageClanUI(M, CMD);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        } else if (event.getModalId().contains("clan-manage-tiktok")) {
                                            if (event.getValue("link-input") == null || isURLValid(event.getValue("link-input").getAsString()) && event.getValue("link-input").getAsString().contains("tiktok.com")) {
                                                C.setTiktokURL(event.getValue("link-input") == null ? null : event.getValue("link-input").getAsString());
                                                C.AddClanLog(event.getUser(), "Tiktok", "Modified the clan's Tiktok link to " + C.getDiscordURL() + ".");
                                                C.ManageClanUI(M, CMD);
                                            } else M.editOriginal(":x: " + TL(M, "image-fail-url")).queue();
                                        }
                                        C.Update();
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                }
                            });
                        }
                        else if (event.getModalId().startsWith("clan-clanrole")) {
                            ClanRoleManager CMD = new ClanRoleManager(event.getModalId());
                            Clan C = Clan.get(CMD.ClanID);
                            if (event.getModalId().startsWith("clan-clanrole-new")) {
                                event.deferReply(true).queue(M -> {
                                    if ((isEmoji(event.getValue("emoji").getAsString()) || event.getGuild().getEmojiById(event.getValue("emoji").getAsString()) != null) && C.getClanRoles().size() < 25) {
                                        ClanRole CR = new ClanRole(C.getId(), event.getValue("name").getAsString(), event.getValue("emoji").getAsString());
                                        CMD.ClanRoleID = CR.getId();
                                        C.EditClanRoleUI(CMD, M, CR);
                                    } else {
                                        M.editOriginal("Bad emoji.").queue();
                                    }
                                });
                            } else if (event.getModalId().startsWith("clan-clanrole-manage")) {
                                event.deferEdit().queue(M -> {
                                    ClanRole CR = ClanRole.get(CMD.ClanRoleID);
                                    if (event.getValue("name") != null && !event.getValue("name").getAsString().isEmpty()) CR.setName(event.getValue("name").getAsString());
                                    if (event.getValue("emoji") != null && !event.getValue("emoji").getAsString().isEmpty() && (isEmoji(event.getValue("emoji").getAsString()) || event.getGuild().getEmojiById(event.getValue("emoji").getAsString()) != null)) CR.setEmoji(event.getValue("emoji").getAsString());
                                    CR.UpdateOnly("Name", "Unicode");
                                    C.EditClanRoleUI(CMD, M, CR);
                                });
                            }
                        }
                        else if (event.getModalId().startsWith("clan-member-manage-self-num")) {
                            ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(event.getModalId());
                            ClanMember Target = ClanMember.ofClan(CMD.ClanID, CMD.MemberID);
                            Clan C = Clan.get(CMD.ClanID);
                            event.deferReply(true).queue(M -> {
                                try {
                                    Target.setNumber(String.valueOf(Integer.parseInt(event.getValues().getFirst().getAsString())));
                                    Target.Update();
                                    Target.getProfile().ManageProfileUI(M, new ProfileCommand(Target.getUser()));
                                    C.AddClanLog(event.getUser(), "[Clan Number]", "Assigned the number **" + Target.getNumber() + "** to **" + Target.getUser().getEffectiveName() + "**.");
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getModalId().startsWith("clan-member-manage")) {
                            ClanMemberInteractCommand CMD = new ClanMemberInteractCommand(event.getModalId());
                            ClanMember Target = ClanMember.ofClan(CMD.ClanID, CMD.MemberID);
                            Clan C = Clan.get(CMD.ClanID);
                            event.deferEdit().queue(M -> {
                                if (event.getModalId().contains("number")) {
                                    Target.setNumber(String.valueOf(Integer.parseInt(event.getValues().getFirst().getAsString())));
                                    Target.resetCards();
                                    C.AddClanLog(event.getUser(), "[Clan Number]", "Assigned the number **" + Target.getNumber() + "** to **" + Target.getUser().getEffectiveName() + "**.");
                                } else if (event.getModalId().contains("nickname")) {
                                    Target.setNickname(event.getValues().getFirst().getAsString());
                                    C.AddClanLog(event.getUser(), "Nickname", "Assigned the nicname **" + Target.getNickname() + "** to **" + Target.getUser().getEffectiveName() + "**.");
                                }
                                Target.Update();
                                Target.ManageMemberUI(M, CMD);
                            });
                        }
                        else if (event.getModalId().startsWith("interclan")) {
                            InterclanCommand CMD = new InterclanCommand(event.getModalId());
                            if (event.getModalId().startsWith("interclan-add-duel")) {
                                event.deferEdit().queue(M -> {
                                    User C1 = getUserByID(event.getValue("clan-1").getAsString());
                                    User C2 = getUserByID(event.getValue("clan-2").getAsString());
                                    Game G = Game.get(event.getValue("game").getAsString());
                                    Interclan_Duel D = new Interclan_Duel(CMD.I, C1.getIdLong(), C2.getIdLong(), G);
                                    if (CMD.I.getState().equals(TournamentState.COMPLETE)) {
                                        int S1 = Integer.parseInt(event.getValue("clan-1-score").getAsString());
                                        int S2 = Integer.parseInt(event.getValue("clan-2-score").getAsString());
                                        D.AddScore(S1, S2);
                                        D.Update();
                                    }
                                    M.editOriginal("Done!").queue();
                                    InterclanViewer(CMD.I, M);
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }

    public static EmbedBuilder ClanChoice(InteractionHook M, String option) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle(TL(M,"clan-choose"));
        E.setColor(Color.orange);
        E.setDescription(TL(M,"clan-choose-description"));
        E.setFooter(option);
        return E;
    }

    public static void InterclanDuelManager(InterclanCommand CMD, Interclan_Duel D, InteractionHook M) {
        List<ActionRow> R = new ArrayList<>();

        Button BTN1 = net.dv8tion.jda.api.components.buttons.Button.danger(CMD.Command("interclan-duel-edit-delete"), TL(M,"Delete"));
        Button BTN2 = net.dv8tion.jda.api.components.buttons.Button.danger(CMD.Command("interclan-duel-edit-p1"), "P1");
        Button BTN3 = net.dv8tion.jda.api.components.buttons.Button.danger(CMD.Command("interclan-duel-edit-p2"), "P2");
        R.add(ActionRow.of(BTN1, BTN2, BTN3));

        StringSelectMenu G = StringSelectMenu.create(CMD.Command("interclan-duel-edit-game"))
                .setPlaceholder(D.getGame().getName())
                .setRequiredRange(0, 12)
                .addOptions(Game.getSelectOptions(M, new ArrayList<>()))
                .build();
        R.add(ActionRow.of(G));

        {
            EntitySelectMenu G1 = EntitySelectMenu.create(CMD.Command("interclan-duel-edit-p1"), EntitySelectMenu.SelectTarget.USER)
                    .setDefaultValues(EntitySelectMenu.DefaultValue.user(D.P1ID))
                    .setRequiredRange(1, 1)
                    .build();
            R.add(ActionRow.of(G1));
        }
        {
            EntitySelectMenu G1 = EntitySelectMenu.create(CMD.Command("interclan-duel-edit-p2"), EntitySelectMenu.SelectTarget.USER)
                    .setDefaultValues(EntitySelectMenu.DefaultValue.user(D.P1ID))
                    .setRequiredRange(1, 1)
                    .build();
            R.add(ActionRow.of(G1));
        }
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(TL(M,"interclan-manager") + " - Duel " + CMD.Page);
        embed.setAuthor(CMD.I.getHoster().getName() + " vs " + CMD.I.getJoiner().getName(), null, CMD.I.getLogoURL());
        embed.setDescription(TL(M,"interclan-manager-description"));
        embed.setColor(CMD.I.getColor());
        embed.setThumbnail(CMD.I.getLogoURL());

        String P1 = "";
        String duals = "";
        String P2 = "";
        for (Interclan_Duel DD : CMD.I.getDuels()) {
            P1 = P1 + DD.getGame().getEmoji() + " " + DD.getP1().getEffectiveName() + "\n";
            duals = duals + "**" + DD.getP1Score() + " " + DD.getGame().getVSEmoji() + " " + DD.getP2Score() + "**\n";
            P2 = P2 + DD.getP2().getEffectiveName() + "\n";
        }
        duals = duals + "**" + CMD.I.getHostTotalScore() + " " + Game.get("IEVR").getVSEmoji() + CMD.I.getJoinTotalScore() + "**";
        embed.addField(CMD.I.getHoster().getName(), P1, true);
        embed.addField(TL(M,"Matches"), duals, true);
        embed.addField(CMD.I.getJoiner().getName(), P2, true);
        M.editOriginalEmbeds(embed.build()).setComponents(R).queue();
    }



    public static List<ActionRow> listClanChoices(ClanCommand CMD, String commandname, List<Clan> choices) {
        List<ActionRow> R = new ArrayList<>();
        List<SelectOption> options = new ArrayList<>();
        for (Clan clan : choices) {
            options.add(SelectOption.of(clan.getTag() + " • " + clan.getName(), clan.getId() + "")
                    .withDescription(clan.getNationality().getName() + " | [" + clan.getMemberCount() + "/50] | " + EpochSecondToPattern(clan.getTimeCreated().getEpochSecond(), "dd/MM/yyyy"))
                    .withEmoji(clan.getEmoji() != null ? clan.getEmoji().retrieve() : null));
            if (options.size() == 25) {
                R.add(ActionRow.of(StringSelectMenu.create(CMD.Command(commandname +"-" + R.size()))
                        .setPlaceholder(options.getFirst().getLabel() + "...")
                        .setRequiredRange(1, 1)
                        .addOptions(options)
                        .build()));
                options = new ArrayList<>();
            }
        }
        if (!options.isEmpty()) {
            R.add(ActionRow.of(StringSelectMenu.create(CMD.Command(commandname +"-" + R.size()))
                    .setPlaceholder(options.getFirst().getLabel() + "...")
                    .setRequiredRange(1, 1)
                    .addOptions(options)
                    .build()));
        }
        return R;
    }
    public static List<ActionRow> listClanChoices(InterclanCommand CMD, String commandname, List<Clan> choices) {
        List<ActionRow> R = new ArrayList<>();
        List<SelectOption> options = new ArrayList<>();
        for (Clan clan : choices) {
            options.add(SelectOption.of(clan.getTag() + " • " + clan.getName(), clan.getId() + "")
                    .withDescription(clan.getNationality().getName() + " | [" + clan.getMemberCount() + "/50] | " + clan.getPowerAsString())
                    .withEmoji(clan.getEmoji().retrieve()));
            if (options.size() == 25) {
                R.add(ActionRow.of(StringSelectMenu.create(CMD.Command(commandname +"-" + R.size()))
                        .setPlaceholder(options.getFirst().getLabel() + "...")
                        .setRequiredRange(1, 1)
                        .addOptions(options)
                        .build()));
                options = new ArrayList<>();
            }
        }
        if (!options.isEmpty()) {
            StringSelectMenu menu = StringSelectMenu.create(CMD.Command(commandname +"-" + R.size()))
                    .setPlaceholder(options.getFirst().getLabel() + "...")
                    .setRequiredRange(1, 1)
                    .addOptions(options)
                    .build();
            R.add(ActionRow.of(menu));
        }
        return R;
    }

    public static void slashClanRegister(GenericCommandInteractionEvent event, User captain) {
        Set<Long> set = new HashSet<>();
        set.add(event.getOption("member-1").getAsLong());
        set.add(event.getOption("member-2").getAsLong());
        set.add(event.getOption("member-3").getAsLong());
        set.add(event.getOption("member-4").getAsLong());
        if (Clan.getClanOfUser(captain) == null && set.stream().allMatch(m -> getClanOfUser(m) == null)) {
            if (captain.getTimeCreated().toInstant().isBefore(Instant.now().minus(90, ChronoUnit.DAYS))) {
                if (!isClanCaptainBan(captain.getIdLong())) {
                    if (set.size() < 4 || set.contains(captain.getIdLong())) {
                        event.reply(TL(event, "user-other-than-yourself", 4)).queue();
                    } else if (set.stream().noneMatch(L -> getUserByID(L).isBot())) {
                        String clanname = event.getOption("name").getAsString();
                        String tag = event.getOption("tag").getAsString();
                        String code = event.getOption("color").getAsString();
                        String nationality = event.getOption("nationality").getAsString();
                        String description = event.getOption("description").getAsString();
                        event.deferReply(true).queue(M -> {
                            if (Clan.get(clanname) == null) {
                                if (Utility.isColorcodeValid(M, code)) {
                                    try {
                                        Checks.matches(tag, Checks.ALPHANUMERIC_WITH_DASH, "Name");
                                        if (Clan.getClanOfUser(event.getUser()) == null && set.stream().allMatch(m -> getClanOfUser(m) == null)) {
                                            Message.Attachment Logo = event.getOption("logo").getAsAttachment();
                                            File Downloaded = new File(MainDirectory + "/temp/" + Logo.getProxyUrl().hashCode() + ".png");
                                            new ImageProxy(Logo.getProxyUrl()).downloadToFile(Downloaded).whenComplete((file, throwable) -> {
                                                try {
                                                    if (ImageIO.read(file) != null) {
                                                        CutTransparentBorders(file, file, 350, 350);
                                                        EmbedBuilder E = new EmbedBuilder();
                                                        E.setTitle("Clan Validation Request");
                                                        E.setDescription(description);
                                                        E.setThumbnail("attachment://emblem.png");
                                                        E.setColor(Color.decode(code.replaceAll("000000", "000001")));
                                                        E.addField("Tag:", tag.replaceAll(" ", ""), true);
                                                        E.addField("Name:", clanname, true);
                                                        E.addField("Nationality:", nationality, true);
                                                        E.addField("Captain:", captain.getAsMention(), false);
                                                        E.addField("Members:", set.stream().map(CM -> "<@" + CM + ">").collect(Collectors.joining("\n")), false);
                                                        Button b1 = Button.success("clan-create-accept", "Validate");
                                                        Button b2 = Button.danger("clan-create-decline", "Decline");
                                                        try (FileUpload FU = FileUpload.fromData(file, "emblem.png")) {
                                                            Constants.ClanRequestChannel.sendMessageEmbeds(E.build()).setContent("<@&1266748490743287819>").addComponents(ActionRow.of(b1, b2)).addFiles(FU).queue();
                                                            M.editOriginal(TL(event, "clan-register")).queue();
                                                        } catch (Exception e) {
                                                            M.editOriginal(TL(M, "image-fail-extension")).queue();
                                                        }
                                                    } else {
                                                        M.editOriginal(TL(M, "image-fail-extension")).queue();
                                                    }
                                                } catch (IOException e) {
                                                    handleException(e);
                                                }
                                            });
                                        } else {
                                            M.editOriginal(TL(event, "clan-register-fail-already-in-a-clan")).queue();
                                        }
                                    } catch (IllegalArgumentException e) {
                                        M.editOriginal(TL(event, "error-illegal-character")).queue();
                                    }
                                }
                            } else {
                                M.editOriginal(TL(event, "error-already-exist-or-similar", TL(event, "Name"))).queue();
                            }
                        });
                    } else {
                        event.reply(":expressionless: ... No bot.").queue();
                    }
                } else {
                    event.reply(TL(event,"clan-create-ban")).queue();
                }
            } else {
                event.reply(TL(event,"clan-register-fail-too-new", String.valueOf(3))).queue();
            }
        } else {
            event.reply(TL(event,"clan-register-fail-already-in-a-clan")).setEphemeral(true).queue();
        }
    }
    public static void slashClanManage(InteractionHook M, ClanManager CMD) {
        List<Clan> clans = isClanManager(CMD.MyID) ? Clan.listOpenPaused() : getClansOfUser(CMD.MyID);
        if (clans.isEmpty()) M.editOriginal(TL(M,"error-you-are-not-in-a-clan")).queue();
        if (clans.size() > 1) M.editOriginalEmbeds(ClanChoice(M,"Manage").build()).setComponents(listClanChoices(CMD, "clan-choice-manage", clans)).queue();
        if (clans.size() == 1) clans.getFirst().ManageClanUI(M, CMD);
    }
    public static void slashClanInvite(InteractionHook M, ClanInviteCommand CMD) {
        List<Clan> clans = isClanManager(CMD.MyID) ? Clan.listOpenPaused() : getClansOfUser(CMD.MyID);
        if (clans.isEmpty()) M.editOriginal(TL(M,"error-you-are-not-in-a-clan")).queue();
        if (clans.size() > 1) M.editOriginalEmbeds(ClanChoice(M,"Inviting " + CMD.getTargetMember().getUser().getEffectiveName()).build()).setComponents(listClanChoices(CMD, "clan-choice-inv", clans)).queue();
        if (clans.size() == 1) clans.getFirst().InviteMemberUI(M, CMD);
    }
    public static void slashClanLeave(InteractionHook M, ClanMemberInteractCommand CMD) {
        List<Clan> clans = getClansOfUser(CMD.MyID);
        if (clans.isEmpty()) M.editOriginal(TL(M,"error-you-are-not-in-a-clan")).queue();
        if (clans.size() > 1) M.editOriginalEmbeds(ClanChoice(M,"Leaving " + CMD.getMe().getUser().getEffectiveName()).build()).setComponents(listClanChoices(CMD, "clan-choice-leave", clans)).queue();
        if (clans.size() == 1) clans.getFirst().ClanLeaveUI(M, CMD);
    }
    public static void slashClanLogo(InteractionHook M, ClanManager CMD, Message.Attachment Logo) {
        List<Clan> clans = isClanManager(CMD.MyID) ? Clan.listOpenPaused() : getClansOfUser(CMD.MyID);
        if (clans.isEmpty()) M.editOriginal(TL(M,"error-you-are-not-in-a-clan")).queue();
        if (clans.size() > 1) M.editOriginalEmbeds(ClanChoice(M,"Logoing " + CMD.getTargetClan().getName()).setImage(Logo.getUrl()).build()).setComponents(listClanChoices(CMD, "clan-choice-logo", clans)).queue();
        if (clans.size() == 1) clans.getFirst().changeLogo(CMD, M, Logo.getUrl());
    }
    public static void slashClanDiscard(InteractionHook M, ClanManager CMD) {
        List<Clan> clans = isClanManager(CMD.MyID) ? Clan.listOpenPaused() : getClansOfUser(CMD.MyID);
        if (clans.isEmpty()) M.editOriginal(TL(M,"error-you-are-not-in-a-clan")).queue();
        if (clans.size() > 1) M.editOriginalEmbeds(ClanChoice(M,"Disbanding " + CMD.getTargetClan().getName()).build()).setComponents(listClanChoices(CMD, "clan-choice-disband", clans)).queue();
        if (clans.size() == 1) clans.getFirst().ClanDisbandUI(M, CMD);
    }
    public static void slashInterclanRequest(InteractionHook M, InterclanCommand CMD) {
        List<Clan> clans = isClanManager(CMD.MyID) ? Clan.listOpenPaused() : getClansOfUser(CMD.MyID);
        if (clans.isEmpty()) M.editOriginal(TL(M,"error-you-are-not-in-a-clan")).queue();
        if (clans.size() > 1) M.editOriginalEmbeds(ClanChoice(M,"Interclaning " + CMD.getJoinClan().getName()).build()).setComponents(listClanChoices(CMD, "clan-choice-ic-host", clans)).queue();
        if (clans.size() == 1) if (CMD.getJoinClan() == null) M.editOriginalEmbeds(ClanChoice(M,"Interclaning " + CMD.getJoinClan().getName()).build()).setComponents(listClanChoices(CMD, "clan-choice-ic-join", Clan.listOpenPaused().stream().filter(c -> c.getId() != CMD.HostClan).collect(Collectors.toList()))).queue();
        else clans.getFirst().InterclanRequestUI(M, CMD);
    }


    public static void setInterclanManage(InteractionHook M, InterclanCommand CMD) {
        if (CMD.getMe().hasPermission(M, ClanPermission.MANAGE_INTERCLAN)) {

        }
    }



    public static boolean isValidClanName(String cname) {
        boolean isGood = !(similarity(cname, "Moderator, true") > 70) &&
                !(similarity(cname, "Modérateur, true") > 70) &&
                !(similarity(cname, "Community, true") > 70) &&
                !(similarity(cname, "Mod, true") > 40) &&
                !(similarity(cname, "Admin, true") > 70) &&
                !(similarity(cname, "Administrator, true") > 70) &&
                !(similarity(cname, "Tournament, true") > 70) &&
                !(similarity(cname, "Tournois, true") > 70) &&
                !(similarity(cname, "Torneo, true") > 70) &&
                !(similarity(cname, "Lead, true") > 70) &&
                !(similarity(cname, "Project, true") > 70) &&
                !(similarity(cname, "Number, true") > 70) &&
                !(similarity(cname, "Leader, true") > 70) &&
                !(similarity(cname, "Chief, true") > 70) &&
                !(similarity(cname, "Organizer, true") > 70) &&
                !(similarity(cname, "Organisateur, true") > 70) &&
                !(similarity(cname, "Captain", true) > 70) &&
                !(similarity(cname, "Clan Captain", true) > 70) &&
                !(similarity(cname, "Fuck", true) > 70) &&
                !(similarity(cname, "Merde", true) > 70) &&
                !(similarity(cname, "Bitch", true) > 70) &&
                !(similarity(cname, "Bitches", true) > 70) &&
                !(similarity(cname, "Assholes", true) > 70) &&
                !(similarity(cname, "Grosses", true) > 70) &&
                !(similarity(cname, "Partner", true) > 70) &&
                !(similarity(cname, "Teniante", true) > 70) &&
                !(similarity(cname, "Gerante", true) > 70) &&
                !(similarity(cname, "Owner", true) > 70) &&
                !(similarity(cname, "Capitano", true) > 70) &&
                !(similarity(cname, "Founder", true) > 70) &&
                !(similarity(cname, "Membres", true) > 70) &&
                !(similarity(cname, "N/A", true) > 70) &&
                !(similarity(cname, "Clan", true) > 70) &&
                !(similarity(cname, "server", true) > 70) &&
                !(similarity(cname, "choice", true) > 70) &&
                !(similarity(cname, "button", true) > 70) &&
                !(similarity(cname, "profile", true) > 70) &&
                !(similarity(cname, "manage", true) > 70) &&
                !(similarity(cname, "edit", true) > 70) &&
                !(similarity(cname, "select", true) > 70) &&
                !(similarity(cname, "Member", true) > 70);
        char[] charArray = cname.toCharArray();
        for (char c : charArray) {
            if (!Character.isAlphabetic(c) && !Character.isWhitespace(c) && !Character.isDigit(c)) {
                if (c != '#' && c != '-' && c != '!' && c != '|') {
                    isGood = false;
                }
            }
        }
        for (Clan clan : Clan.listOpenPaused()) {
            if (clan.getName().contains(cname)
                    || cname.contains(clan.getName())
                    || cname.toLowerCase().contains("server")
                    || cname.toLowerCase().contains("tournament")
                    || cname.toLowerCase().contains("edit")
                    || cname.toLowerCase().contains("manage")
                    || cname.toLowerCase().contains("profile")
                    || cname.toLowerCase().contains("tourn")
                    || cname.toLowerCase().contains("clan")
                    || cname.toLowerCase().contains("select")
                    || cname.toLowerCase().contains("member")
                    || cname.toLowerCase().contains("admin")
                    || cname.toLowerCase().contains("captain")
                    || cname.toLowerCase().contains("button")
                    || cname.toLowerCase().contains("n/a")
                    || cname.toLowerCase().contains("cosmetic")
                    || cname.toLowerCase().contains("complete")
                    || cname.toLowerCase().contains("description")
                    || cname.toLowerCase().contains("choice")
                    || cname.toLowerCase().contains("pf")
                    || cname.toLowerCase().contains("mod")
                    || cname.toLowerCase().contains("menu")
                    || cname.toLowerCase().contains("accept")
                    || cname.toLowerCase().contains("cancel")
                    || cname.toLowerCase().contains("*")
                    || cname.toLowerCase().contains("_")
                    || cname.toLowerCase().contains("~")
                    || cname.toLowerCase().contains("-")
                    || cname.toLowerCase().contains(">")
                    || cname.toLowerCase().contains("`")
                    || cname.toLowerCase().contains("|")) {
                isGood = false;
                break;
            }
        }
        return isGood;
    }
}
