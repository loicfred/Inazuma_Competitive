package iecompbot.interaction.listeners;

import iecompbot.img.builders.CosmeticsPreviewBuilder;
import iecompbot.img.builders.MatchResultImageBuilder;
import iecompbot.interaction.cmdbreakdown.MercatoCommand;
import iecompbot.interaction.cmdbreakdown.profile.ItemCommand;
import iecompbot.interaction.cmdbreakdown.PageViewerCommand;
import iecompbot.objects.match.Game;
import iecompbot.objects.Nationality;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.match.MatchLog;
import iecompbot.objects.match.MatchLog_S;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.server.ServerInfo;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static iecompbot.Constants.POWERDECIMAL;
import static iecompbot.Constants.PRICEDECIMAL;
import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.interaction.Automation.*;
import static iecompbot.interaction.listeners.MDFFeatures.ClearClanTags;
import static iecompbot.objects.BotManagers.isPowerDisabled;
import static iecompbot.objects.Retrieval.getUserByID;
import static iecompbot.springboot.data.DatabaseObject.*;
import static my.utilities.util.Utilities.*;

public class ShopFeatures extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getName().startsWith("shop")) {
                        event.deferReply(true).queue(M -> {
                            try {
                                slashShop(M, new ItemCommand(event.getUser()), event.getOption("article").getAsString());
                            } catch (Exception e) {
                                replyException(M, e);
                            }
                        });
                    }
                    else if (event.getName().startsWith("mercato")) {
                        event.deferReply().queue(M -> {
                            MercatoCommand CMD = new MercatoCommand("");
                            CMD.G = event.getGuild();
                            CMD.PlayerData = event.getOption("player-data").getAsString();
                            if (event.getOption("include-clan-members") != null) {
                                CMD.IncludeClan = event.getOption("include-clan-members").getAsBoolean();
                            }
                            if (event.getOption("game") != null) {
                                CMD.Game = Game.get(event.getOption("game").getAsString());
                            }
                            if (event.getOption("nation") != null) {
                                CMD.Nationality = Nationality.get(event.getOption("nation").getAsString());
                            }
                            slashMercato(M, CMD);
                        });
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
                    if (event.getComponentId().startsWith("shop")) {
                        ItemCommand CMD = new ItemCommand(event.getComponentId());
                        if (event.getComponentId().startsWith("shop-confirm-buy")) {
                            if (CMD.canAfford()) {
                                event.deferEdit().setComponents(ActionRow.of(Button.secondary("buy", TL(event, "Buy")).asDisabled())).queue(M -> {
                                    try {
                                        CMD.Purchase();
                                        EmbedBuilder E = new EmbedBuilder();
                                        ServerInfo I = ServerInfo.get(event.getGuild());
                                        if (I != null) E = I.getServerEmbed();
                                        E.setColor(CMD.getProfile().getColor());
                                        E.setFooter(CMD.getItem().getName());
                                        switch (CMD.getItem().getType()) {
                                            case COSMETICS_FRAME, COSMETICS_BOARD, BOOSTERS_COIN, BOOSTERS_XP -> {
                                                E.setDescription(":white_check_mark: " + TL(M, "shop-buy-success-use", "**" + CMD.getItem().getEmojiFormatted() + " " + CMD.getItem().getName() + "**"));
                                                Button btn = Button.success(CMD.Command("shop-immediate-equip"), TL(M, "yes"));
                                                M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(btn)).setReplace(true).queue();
                                            }
                                            default -> {
                                                E.setDescription(":white_check_mark: " + TL(M, "shop-buy-success", "**" + CMD.getItem().getEmojiFormatted() + " " + CMD.getItem().getName() + "**") + "\n" + (CMD.getItem().getServerId() != null ? TL(M, "server-economy-buy-success-adm") : ""));
                                                M.editOriginalEmbeds(E.build()).setReplace(true).queue();
                                                if (CMD.getItem().getId() == 1101) { // Shiny Card
                                                    CMD.getProfile().setHasGIF(true);
                                                    CMD.getProfile().UpdateOnly("hasGIF");
                                                }
                                            }
                                        }
                                        LogSlash(I,"**[Shop][" + event.getUser().getEffectiveName() + "]** " + TLG(I,"server-economy-buy-success", "(" + event.getUser().getName() + ")", "**" + CMD.getItem().getEmojiFormatted() + " " + CMD.getItem().getName() + " (" + CMD.getItem().getType() + ")** -> " + CMD.getItem().getPriceAsString()) + "\n"
                                        + "Current Balance: " + CMD.getYourBudget());
                                    } catch (Exception e) {
                                        replyException(M, e);
                                    }
                                });
                            } else {
                                event.reply(TL(event, "shop-buy-cant-afford")).setEphemeral(true).queue();
                            }
                        }
                        else if (event.getComponentId().startsWith("shop-immediate-equip")) {
                            event.deferReply(true).queue(M -> {
                                try {
                                    Profile P = CMD.getProfile();
                                    Item EquippingItem = CMD.getItem();
                                    EmbedBuilder E = new EmbedBuilder();
                                    E.setColor(P.getColor());
                                    E.setAuthor(TL(M, "Cosmetics"), null, event.getUser().getAvatarUrl());
                                    E.setDescription(":white_check_mark: " + TL(M, "profile-cosmetics-success", "**" + EquippingItem.getEmojiFormatted() + " " + EquippingItem.getName() + "**"));
                                    if (event.getComponentId().contains("frame")) {
                                        P.setCustomFrame(EquippingItem);
                                    } else if (event.getComponentId().contains("board")) {
                                        P.setCustomBoard(EquippingItem);
                                    } else if (event.getComponentId().contains("booster")) {
                                        E.setAuthor(TL(M, "booster-activated"));
                                        String boosttype = "";
                                        String boosttime = "";
                                        String multiplier = "**x2.0**";
                                        if (EquippingItem.getName().contains("Coin")) {
                                            boosttype = "**InaCoin**";
                                        } else if (EquippingItem.getName().contains("XP")) {
                                            boosttype = "**XP**";
                                        }
                                        if (EquippingItem.getName().contains("24h")) {
                                            boosttime = "**24h**";
                                        } else if (EquippingItem.getName().contains("48h")) {
                                            boosttime = "**48h**";
                                        }
                                        P.addBooster(P.getItem(EquippingItem.getId()));
                                        E.setDescription(":white_check_mark: " + TL(M, "profile-boost-use-success", boosttype, multiplier, boosttime));
                                    }
                                    M.editOriginalEmbeds(E.build()).queue();
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }
                        else if (event.getComponentId().startsWith("shop-view-inventory")) {
                            event.deferReply(true).queue(M -> {
                                try {
                                    Profile P = Profile.get(event.getUser());
                                    P.ViewInventory(M, new PageViewerCommand(P.getID(), "pf-inv-cp"));
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
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.isAcknowledged()) {
            if (CanUseCommand(event)) {
                try {
                    if (event.getComponentId().startsWith("shop")) {
                        if (event.getComponentId().startsWith("shop-select-product")) {
                            event.deferReply(true).queue(M -> {
                                try {
                                    ItemCommand CMD = new ItemCommand(event.getComponentId());
                                    CMD.ItemID = Long.parseLong(event.getValues().getFirst());
                                    CMD.ID = event.getUser().getIdLong();
                                    EmbedBuilder E = new EmbedBuilder();
                                    String cost = "**" + TL(event, "Cost") + ":** " + CMD.getItem().getPriceAsString() + "\n" +
                                            "**" + TL(event, "Inventory") + ":** " + CMD.getYourBudget();
                                    switch (CMD.getItem().getType()) {
                                        case COSMETICS_FRAME -> {
                                            if (CMD.getAmountOwned() > 0) {
                                                E.setDescription(TL(M, "shop-buy-already-own"));
                                                M.editOriginalEmbeds(E.build()).queue();
                                            } else {
                                                E.setTitle(TL(M, "Preview"));
                                                E.setDescription(TL(M, "shop-preview-description") + "\n\n" + cost);
                                                E.setColor(CMD.getProfile().getColor());
                                                E.setImage("attachment://preview.png");
                                                E.setFooter(CMD.getItem().getName());
                                                Button btn = Button.success(CMD.Command("shop-confirm-buy"), TL(M, "Buy")).withStyle(CMD.canAfford() ? ButtonStyle.SUCCESS : ButtonStyle.DANGER);
                                                M.editOriginalEmbeds(E.build()).setFiles(FileUpload.fromData(new CosmeticsPreviewBuilder(event.getUser(), CMD.getItem()).GenerateCardPNG().DownloadPNGToFile(), "preview.png")).setComponents(ActionRow.of(btn)).queue();
                                            }
                                        }
                                        case COSMETICS_BOARD -> {
                                            if (CMD.getAmountOwned() > 0) {
                                                E.setDescription(TL(M, "shop-buy-already-own"));
                                                M.editOriginalEmbeds(E.build()).queue();
                                            } else {
                                                E.setTitle(TL(M, "Preview"));
                                                E.setDescription(TL(M, "shop-preview-description") + "\n\n" + cost);
                                                E.setColor(CMD.getProfile().getColor());
                                                E.setImage("attachment://preview.png");
                                                E.setFooter(CMD.getItem().getName());
                                                Button btn = Button.success(CMD.Command("shop-confirm-buy"), TL(M, "Buy")).withStyle(CMD.canAfford() ? ButtonStyle.SUCCESS : ButtonStyle.DANGER);

                                                try (MatchResultImageBuilder MRIB = new MatchResultImageBuilder(event.getUser(), DiscordAccount.getSelfUser(), MatchLog.getRandom(), MatchLog_S.getRandom(), ServerInfo.get(event.getGuild()))) {
                                                    MRIB.isPreview = true;
                                                    MRIB.CustomBoardItem = CMD.getItem();
                                                    MRIB.GenerateMatchResultPNG();
                                                    M.editOriginalEmbeds(E.build()).setFiles(FileUpload.fromData(MRIB.DownloadPNGToFile(), "preview.png")).setComponents(ActionRow.of(btn)).queue();
                                                }
                                            }
                                        }
                                        case RARE_ITEMS -> {
                                            if (CMD.getAmountOwned() > 0) {
                                                E.setDescription(TL(M, "shop-buy-already-own"));
                                                M.editOriginalEmbeds(E.build()).queue();
                                            } else {
                                                E.setTitle(TL(M, "Buy"));
                                                E.setDescription(TL(M, "shop-buy-confirm", "**" + CMD.getItem().getEmojiFormatted() + " " + CMD.getItem().getName() + "**") + "\n\n" + cost);
                                                E.setColor(CMD.getProfile().getColor());
                                                E.setFooter(CMD.getItem().getName());
                                                Button btn = Button.success(CMD.Command("shop-confirm-buy"), TL(M, "Buy")).withStyle(CMD.canAfford() ? ButtonStyle.SUCCESS : ButtonStyle.DANGER);
                                                M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(btn)).queue();
                                            }
                                        }
                                        default -> {
                                            E.setTitle(TL(M, "Buy"));
                                            E.setDescription(TL(M, "shop-buy-confirm", "**" + CMD.getItem().getEmojiFormatted() + " " + CMD.getItem().getName() + "**") + "\n\n" + cost);
                                            E.setColor(CMD.getProfile().getColor());
                                            E.setFooter(CMD.getItem().getName());
                                            Button btn = Button.success(CMD.Command("shop-confirm-buy"), TL(M, "Buy")).withStyle(CMD.canAfford() ? ButtonStyle.SUCCESS : ButtonStyle.DANGER);
                                            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(btn)).queue();
                                        }
                                    }
                                } catch (Exception e) {
                                    replyException(M, e);
                                }
                            });
                        }

                    } else if (event.getComponentId().startsWith("mercato")) {
                        if (event.getComponentId().startsWith("mercato-change-page")) {
                            event.deferEdit().queue(M -> {
                                MercatoCommand CMD = new MercatoCommand(event.getComponentId());
                                CMD.G = event.getGuild();
                                CMD.Page = Integer.parseInt(event.getValues().getFirst());
                                slashMercato(M, CMD);
                            });
                        }
                    }
                } catch (Exception e) {
                    replyException(event, e);
                }
            }
        }
    }



    public static void slashShop(InteractionHook M, ItemCommand CMD, String MO) throws Exception {
        EmbedBuilder Shop = new EmbedBuilder();
        Shop.setTitle("InaShop");
        Shop.setColor(Color.orange);
        Shop.setDescription(TL(M, "shop-description") + "\nInaCoins: " + Item.get(1).getEmojiFormatted() + " " + PRICEDECIMAL.format(CMD.getProfile().getItem(1).Amount));
        List<SelectOption> options = new ArrayList<>();
        List<ActionRow> rows = new ArrayList<>();
        switch (MO) {
            case "Server" -> {
                if (!M.getInteraction().isFromGuild() || !M.getInteraction().isFromAttachedGuild()) M.editOriginal(TL(M, "shop-no-items")).queue();
                else {
                    ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
                    if (I.Channels().getLogChannel() == null) M.editOriginal(TL(M, "server-log-channel-required")).queue();
                    else if (I.getCurrency() == null || I.listItems().isEmpty()) M.editOriginal(TL(M, "shop-no-items")).queue();
                    else {
                        Shop.setDescription(TL(M, "shop-description") + "\n" + I.getCurrency().getName() + ": " + I.getCurrency().getEmojiFormatted() + " " + PRICEDECIMAL.format(CMD.getProfile().getItem(I.getCurrency().getId()).Amount));
                        Shop.setFooter("• " + I.getName(), I.getIconUrl());
                        for (Item IT : I.listItems()) options = addItemToShopEmbed(M, CMD, Shop, options, rows, IT);
                        if (Shop.getFields().size() < 25) Shop.addField("More Coming Soon", "> ...", false);
                    }
                }
            }
            case "Cosmetics (Frame)" -> {
                Shop.setFooter("• Credits to " + getUserByID("546096014789181440").getName() + ", " + getUserByID("456475741857513493").getName() + " and " + getUserByID("447095620872699936").getName());
                for (Item I : Item.get(Item.ItemType.COSMETICS_FRAME)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
                if (Shop.getFields().size() < 25) Shop.addField("More Coming Soon", "> ...", false);
            }
            case "Cosmetics (Board)" -> {
                for (Item I : Item.get(Item.ItemType.COSMETICS_BOARD)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
                if (Shop.getFields().size() < 25) Shop.addField("More Coming Soon", "> ...", false);
            }
            case "Materials" -> {
                for (Item I : Item.get(Item.ItemType.MATERIALS)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
            }
            case "Unique Items" -> {
                for (Item I : Item.get(Item.ItemType.RARE_ITEMS)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
            }
            case "Boosters" -> {
                for (Item I : Item.get(Item.ItemType.BOOSTERS_XP)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
                for (Item I : Item.get(Item.ItemType.BOOSTERS_COIN)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
            }
            case "License BG" -> {
                for (Item I : Item.get(Item.ItemType.LICENSE_BG)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
            }
            case "License FG" -> {
                for (Item I : Item.get(Item.ItemType.LICENSE_FG)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
            }
            case "License RY" -> {
                for (Item I : Item.get(Item.ItemType.LICENSE_RY)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
            }
            case "License ST" -> {
                for (Item I : Item.get(Item.ItemType.LICENSE_ST)) {
                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
                }
            }
        }
        if (!options.isEmpty()) rows.add(ActionRow.of(StringSelectMenu.create(CMD.Command("shop-select-product-" + rows.size()))
                .setPlaceholder(TL(M,"Preview")).setRequiredRange(1, 1).addOptions(options).build()));


        rows.add(ActionRow.of(Button.secondary("shop-view-inventory", TL(M, "shop-view-inventory"))));
        M.editOriginalEmbeds(Shop.build()).setComponents(rows).queue();
    }
//    public static void slashShopV2(InteractionHook M, ItemCommand CMD, String MO) throws Exception {
//
//        List<Section> sections = new ArrayList<>();
//        switch (MO) {
//            case "Server" -> {
//                if (!M.getInteraction().isFromGuild() || !M.getInteraction().isFromAttachedGuild()) M.editOriginal(TL(M, "shop-no-items")).queue();
//                else {
//                    ServerInfo I = ServerInfo.get(M.getInteraction().getGuild());
//                    if (I.Channels().getLogChannel() == null) M.editOriginal(TL(M, "server-log-channel-required")).queue();
//                    else if (I.getCurrency() == null || I.listItems().isEmpty()) M.editOriginal(TL(M, "shop-no-items")).queue();
//                    else {
//                        for (Item IT : I.listItems()) {
//                            if (IT.getPrice() != null && sections.size() < 10) {
//                                CMD.I = IT;
//                                sections.add(Section.of(
//                                        Button.success(CMD.Command("shop-buy-" + IT.getId()), TL(M, "Preview")),
//                                        TextDisplay.of(IT.getEmojiFormatted() + " " + IT.getName() + ": **" + IT.getPriceAsString() + (CMD.canAfford() ? " :exclamation:" : "") + "**")
//                                ));
//                            }
//                        }
//                    }
//                }
//            }
//            case "Cosmetics (Frame)" -> {
//                for (Item IT :  Item.get(Item.ItemType.COSMETICS_FRAME)) {
//                    if (IT.getPrice() != null && sections.size() < 10) {
//                        CMD.I = IT;
//                        if (CMD.getProfile().getItem(IT.getId()).Amount == 0) {
//                            sections.add(Section.of(
//                                    Button.success(CMD.Command("shop-buy-" + IT.getId()), TL(M, "Preview")),
//                                    TextDisplay.of(IT.getEmojiFormatted() + " " + IT.getName() + ": **" + IT.getPriceAsString() + (CMD.canAfford() ? " :exclamation:" : "") + "**")
//                            ));
//                        }
//                    }
//                }
//            }
//            case "Cosmetics (Board)" -> {
//                for (Item I : Item.get(Item.ItemType.COSMETICS_BOARD)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//                if (Shop.getFields().size() < 25) Shop.addField("More Coming Soon", "> ...", false);
//            }
//            case "Materials" -> {
//                for (Item I : Item.get(Item.ItemType.MATERIALS)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//            }
//            case "Unique Items" -> {
//                for (Item I : Item.get(Item.ItemType.RARE_ITEMS)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//            }
//            case "Boosters" -> {
//                for (Item I : Item.get(Item.ItemType.BOOSTERS_XP)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//                for (Item I : Item.get(Item.ItemType.BOOSTERS_COIN)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//            }
//            case "License BG" -> {
//                for (Item I : Item.get(Item.ItemType.LICENSE_BG)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//            }
//            case "License FG" -> {
//                for (Item I : Item.get(Item.ItemType.LICENSE_FG)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//            }
//            case "License RY" -> {
//                for (Item I : Item.get(Item.ItemType.LICENSE_RY)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//            }
//            case "License ST" -> {
//                for (Item I : Item.get(Item.ItemType.LICENSE_ST)) {
//                    options = addItemToShopEmbed(M, CMD, Shop, options, rows, I);
//                }
//            }
//        }
//        Container container = Container.of(
//                TextDisplay.of("## InaShop"),
//                TextDisplay.of(TL(M, "shop-description") + "\nInaCoins: " + Item.get(1).getEmojiFormatted() + " " + PRICEDECIMAL.format(CMD.getProfile().getItem(1).Amount)),
//                TextDisplay.of("• Credits to " + getUserByID("546096014789181440").getName() + ", " + getUserByID("456475741857513493").getName() + " and " + getUserByID("447095620872699936").getName()),
//
//                Separator.createDivider(Separator.Spacing.SMALL)
//        );
//        M.editOriginalComponents(container).queue();
//    }

    private static List<SelectOption> addItemToShopEmbed(InteractionHook m, ItemCommand CMD, EmbedBuilder shop, List<SelectOption> options, List<ActionRow> rows, Item I) throws Exception {
        if (I.getPrice() != null) {
            CMD.I = I;
            if (shop.getFields().size() < 25) shop.addField(I.getEmojiFormatted() + " " + I.getName() + ": **" + I.getPriceAsString() + (CMD.canAfford() ? " :exclamation:" : "") + "**", "> " + I.getDescription(), false);
            options.add(SelectOption.of(I.getName(), I.getId() + "").withDescription("• " + I.getDescription()).withEmoji(I.getEmoji().retrieve()));
            if (options.size() == 25) {
                StringSelectMenu menu = StringSelectMenu.create(CMD.Command("shop-select-product-" + rows.size()))
                        .setPlaceholder(TL(m, "Preview")).setRequiredRange(1, 1).addOptions(options).build();
                rows.add(ActionRow.of(menu));
                options = new ArrayList<>();
            }
        }
        return options;
    }

    public static void slashMercato(InteractionHook M, MercatoCommand CMD) {
        try {
            switch (CMD.PlayerData) {
                case "Power" -> ListOfClanlessPowerfulMembers(M, CMD);
                case "Activity" -> ListOfClanlessActiveMembers(M, CMD);
                // case "Age" -> ListOfClanlessAgedMembers(M, CMD);
            }
        } catch (Exception e) {
            replyException(M, e);
        }
    }


    public synchronized static void ListOfClanlessPowerfulMembers(InteractionHook M, MercatoCommand CMD) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle("Mercato");
        E.setColor(Color.orange);
        E.setDescription(TL(M,"mercato-description"));
        E.setFooter(CMD.listFilter().length() > 1 ? TL(M, "Filter") + ": " + CMD.listFilter() : null);
        List<Clan> cls = Clan.listOpenPaused();
        String s1 = "";
        String s2 = "";
        String s3 = "";

        List<DatabaseObject.Row> TRs = DatabaseObject.doQueryAll("CALL DisplayMercatoPower(?,?,?,?)", CMD.getGameCode(), CMD.getNationality(), CMD.IncludeClan, CMD.Page);
        String name;
        String tag;
        String nationality;
        int i = 1 + (50 * (CMD.Page-1));
        for (DatabaseObject.Row TR : TRs) {
            try {
                name = StopString(ClearClanTags(getUserByID(TR.getAsLong("UserID")).getEffectiveName(), cls), 14);
                tag = TR.get("Tag") != null ? "**" + TR.getAsString("Tag") + "**" : "";
                nationality = Nationality.get(TR.getAsString("Nationality")).getFlag().getFormatted();

                s1 = s1 + "`" + i + ")` " + nationality + " " + tag + " | " + name + "\n";
                s2 = s2 + "`" + POWERDECIMAL.format(TR.getAsDouble("Power")) + "`\n";
                s3 = s3 + "`" + TR.getAsInt("Activity") + " (" + POWERDECIMAL.format(TR.getAsDouble("Avg Activity")) + " g/m)`\n";
                i++;
                if ((i-1) % 10 == 0) {
                    E.addField(TL(M,"Name"), s1, true);
                    if (!isPowerDisabled(CMD.G)) E.addField(TL(M,"Power"), s2, true);
                    else E.addBlankField(true);

                    E.addField(TL(M,"Activity"), s3, true);
                    s1 = "";
                    s2 = "";
                    s3 = "";
                }
            } catch (Exception ignored) {
                break;
            }
        }
        if (!s1.isEmpty()) {
            E.addField(TL(M,"Name"), s1, true);
            if (!isPowerDisabled(CMD.G)) E.addField(TL(M,"Power"), s2, true);
            else E.addBlankField(true);
            E.addField(TL(M,"Activity"), s3, true);
        }

        List<SelectOption> options = new ArrayList<>();
        double totalAmount = Math.min(1250, doQueryValue(Integer.class,"SELECT GetMercatoAmount(?,?) AS 'Count'", CMD.getNationality(), CMD.IncludeClan).orElse(0));
        for (i = 0; i < (int) (totalAmount/50); i++) {
            if (options.size() < 25) {
                options.add(SelectOption.of(TL(M,"Page") + " " + (i + 1) + "...", "" + (i + 1))
                        .withDescription("[" + ((i * 50) + 1) + "-" + Math.min(((i + 1) * 50), totalAmount) + "/" + (int) totalAmount + "] " + TL(M,"view-more", "" + (i + 1))));
            }
        }
        if (!options.isEmpty()) {
            StringSelectMenu menu = StringSelectMenu.create(CMD.Command("mercato-change-page"))
                    .setPlaceholder(options.get(CMD.Page - 1).getLabel())
                    .setRequiredRange(1, 1)
                    .addOptions(options)
                    .build();
            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu)).queue();
        } else {
            M.editOriginalEmbeds(E.build()).queue();
        }
    }
    public synchronized static void ListOfClanlessActiveMembers(InteractionHook M, MercatoCommand CMD) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle("Mercato");
        E.setColor(Color.orange);
        E.setDescription(TL(M, "mercato-description-2"));
        E.setFooter(CMD.listFilter().length() > 1 ? TL(M, "Filter") + ": " + CMD.listFilter() : null);
        List<Clan> cls = Clan.listOpenPaused();
        String s1 = "";
        String s2 = "";
        String s3 = "";
        List<DatabaseObject.Row> TRs = DatabaseObject.doQueryAll("CALL DisplayMercatoActivity(?,?,?,?)", CMD.getGameCode(), CMD.getNationality(), CMD.IncludeClan, CMD.Page);
        String name;
        String tag;
        String nationality;
        int i = 1 + (50 * (CMD.Page-1));
        for (DatabaseObject.Row TR : TRs) {
            try {
                name = StopString(ClearClanTags(getUserByID(TR.getAsLong("UserID")).getEffectiveName(), cls), 14);
                tag = TR.get("Tag") != null ? "**" + TR.getAsString("Tag") + "**" : "";
                nationality = Nationality.get(TR.getAsString("Nationality")).getFlag().getFormatted();

                s1 = s1 + "`" + i + ")` " + nationality + " " + tag + " | " + name + "\n";
                s2 = s2 + "`" + TR.getAsInt("Activity") + " (" + POWERDECIMAL.format(TR.getAsDouble("Avg Activity")) + " g/m)`\n";
                s3 = s3 + "`" + POWERDECIMAL.format(TR.getAsDouble("Power")) + "`\n";
                i++;
                if ((i-1) % 10 == 0) {
                    E.addField(TL(M,"Name"), s1, true);
                    E.addField(TL(M,"Activity"), s2, true);
                    if (!isPowerDisabled(CMD.G)) E.addField(TL(M,"Power"), s3, true);
                    else E.addBlankField(true);
                    s1 = "";
                    s2 = "";
                    s3 = "";
                }
            } catch (Exception ignored) {
                break;
            }
        }
        if (!s1.isEmpty()) {
            E.addField(TL(M,"Name"), s1, true);
            E.addField(TL(M,"Activity"), s2, true);
            if (!isPowerDisabled(CMD.G)) E.addField(TL(M,"Power"), s3, true);
            else E.addBlankField(true);
        }

        List<SelectOption> options = new ArrayList<>();
        double totalAmount = Math.min(1250, doQueryValue(Integer.class,"SELECT GetMercatoAmount(?,?) AS 'Count'", CMD.getNationality(), CMD.IncludeClan).orElse(0));
        for (i = 0; i < (int) (totalAmount/50); i++) {
            if (options.size() < 25) {
                options.add(SelectOption.of(TL(M,"Page") + " " + (i + 1) + "...", "" + (i + 1))
                        .withDescription("[" + ((i * 50) + 1) + "-" + Math.min(((i + 1) * 50), totalAmount) + "/" + (int) totalAmount + "] " + TL(M,"view-more", "" + (i + 1))));
            }
        }
        if (!options.isEmpty()) {
            StringSelectMenu menu = StringSelectMenu.create(CMD.Command("mercato-change-page"))
                    .setPlaceholder(options.get(CMD.Page - 1).getLabel())
                    .setRequiredRange(1, 1).addOptions(options).build();
            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu)).queue();
        } else {
            M.editOriginalEmbeds(E.build()).queue();
        }
    }
    public synchronized static void ListOfClanlessAgedMembers(MercatoCommand CMD, InteractionHook M) {
        EmbedBuilder E = new EmbedBuilder();
        E.setTitle("Mercato");
        E.setColor(Color.orange);
        E.setDescription(TL(M,"mercato-description-3"));
        E.setFooter(CMD.listFilter().length() > 1 ? TL(M, "Filter") + ": " + CMD.listFilter() : null);
        List<Clan> cls = Clan.listOpenPaused();
        String s1 = "";
        String s2 = "";
        String s3 = "";
        Clan clan;
        String isInClan = "";
        int TPerPage = 50;


        int i = 1 + (TPerPage * (CMD.Page-1));
        for (Profile P : doQueryAll(Profile.class,"SELECT * FROM profile WHERE BirthdayEpochSecond IS NOT NULL LIMIT 50 OFFSET " + (50 * CMD.Page-1))) {
            try {
                String name = StopString(ClearClanTags(P.getUser().getEffectiveName(), cls), 14);
                if (CMD.IncludeClan) {
                    clan = Clan.getClanOfUser(P.getID());
                    isInClan = (clan != null ? "**" + clan.getTag() + " • **" : "");
                }
                s1 = s1 + "`" + i + ")` " + P.getNationality().getFlag().getFormatted() + " " + isInClan + name + "\n";
                s2 = s2 + "`" + EpochSecondToPattern(P.getBirthday().getEpochSecond(), "dd/MM/yyyy") + "`\n";
                s3 = s3 + "`" + getTimeBetweenNow(P.getBirthday()).getYears() + " " + TL(M,"years") + "`\n";
                i++;
                if ((i-1) % 10 == 0) {
                    E.addField(TL(M,"Name") + (CMD.Game != null ? " (" + CMD.Game.getName() + ")" : ""), s1, true);
                    E.addField("Birthday", s2, true);
                    E.addField("Age", s3, true);
                }
                s1 = "";
                s2 = "";
                s3 = "";
            } catch (Exception ignored) {
                break;
            }
        }

        if (!s1.isEmpty()) {
            E.addField(TL(M,"Name") + (CMD.Game != null ? " (" + CMD.Game.getName() + ")" : ""), s1, true);
            E.addField("Birthday", s2, true);
            E.addField("Age", s3, true);
        }

        List<SelectOption> options = new ArrayList<>();
        int totalAmount = Count(Profile.class);
        for (i = 0; i < Math.ceil((double) totalAmount / TPerPage); i++) {
            if (options.size() < 25) {
                options.add(SelectOption.of(TL(M,"Page") + " " + (i + 1) + "...", "" + (i + 1))
                        .withDescription("[" + ((i * TPerPage) + 1) + "-" + Math.min(((i + 1) * TPerPage), totalAmount) + "/" + totalAmount + "] " + TL(M,"view-more", "" + (i + 1))));
            }
        }
        if (!options.isEmpty()) {
            StringSelectMenu menu = StringSelectMenu.create(CMD.Command("mercato-change-page"))
                    .setPlaceholder(options.get(CMD.Page - 1).getLabel())
                    .setRequiredRange(1, 1)
                    .addOptions(options)
                    .build();
            M.editOriginalEmbeds(E.build()).setComponents(ActionRow.of(menu)).queue();
        } else {
            M.editOriginalEmbeds(E.build()).queue();
        }
    }
}
