package iecompbot.objects.profile.item;

import iecompbot.objects.BotEmoji;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static iecompbot.Constants.PRICEDECIMAL;
import static iecompbot.L10N.TL;
import static iecompbot.Main.DefaultURL;
import static iecompbot.springboot.config.AppConfig.cacheService;

@Component
public class Item extends DatabaseObject<Item> {
    private transient Price Prices = null;

    private long ID;
    private long EmojiID;
    private Long PriceID;
    private Long ServerID;
    private String Name;
    private String Description;
    private String Type;
    private byte[] Image1 = null;
    private byte[] Image2 = null;
    private byte[] Image3 = null;

    private Item() {}
    public Item(long serverID, String name, String description, ItemType type, long emojiID) {
        ID = Instant.now().getEpochSecond();
        ServerID = serverID;
        Name = name;
        Description = description;
        Type = type.name();
        EmojiID = emojiID;
        Write();
    }

    public long getId() {
        return ID;
    }
    public String getName() {
        return Name;
    }
    public Long getServerId() {
        return ServerID;
    }
    public String getDescription() {
        return Description;
    }
    public byte[] getImage() {
        return Image1;
    }
    public byte[] getImage2() {
        return Image2;
    }
    public byte[] getImage3() {
        return Image3;
    }
    public String getImageURL() {
        return DefaultURL + "/api/img/item/" + getId() + ".png?i=1";
    }
    public String getImageURL2() {
        return DefaultURL + "/api/img/item/" + getId() + ".png?i=2";
    }
    public String getImageURL3() {
        return DefaultURL + "/api/img/item/" + getId() + ".png?i=3";
    }
    public ItemType getType() {
        return ItemType.valueOf(Type);
    }
    public Price getPrice() {
        return PriceID != null && Prices == null ? Prices = Price.get(PriceID) : Prices;
    }
    public BotEmoji getEmoji() {
        return BotEmoji.get(ServerID, EmojiID);
    }
    public String getEmojiFormatted() {
        return getEmoji().getFormatted();
    }


    public void setType(ItemType type) {
        Type = type.name();
    }
    public void setDescription(String description) {
        Description = description;
    }
    public void setName(String name) {
        Name = name;
    }
    public void setPrice(long currencyId, int amount) {
        PriceID = Price.of(Item_Count.of(currencyId, amount)).getId();
    }
    public void setEmojiID(long emojiID) {
        EmojiID = emojiID;
    }
    public void setImage(byte[] bytes) {
        Image1 = bytes;
    }
    public void setImage2(byte[] bytes) {
        Image2 = bytes;
    }
    public void setImage3(byte[] bytes) {
        Image3 = bytes;
    }


    public static Item get(long id) {
        return cacheService.getCachedItem(id);
    }
    public static Item get(String name) {
        return cacheService.getCachedItemByName(name, null);
    }
    public static List<Item> get(ItemType type) {
        return cacheService.getCachedItemByType(type, null);
    }

    public String getPriceAsString() {
        String S = "";
        for (Item_Count<?> I : getPrice().list()) S = S + " • " + I + (I.getType().equals(ItemType.RARE_ITEMS) ? " (Only own)" : "");
        return S.replaceFirst(" • ", "");
    }


    @Override
    public int Update() {
        return ServerID != null ? super.Update() : 0;
    }
    @Override
    public Optional<Item> UpsertThenReturn() {
        return ServerID != null ? super.UpsertThenReturn() : Optional.empty();
    }
    @Override
    public Optional<Item> WriteThenReturn() {
        return ServerID != null ? super.WriteThenReturn() : Optional.empty();
    }

    public enum ItemType {
        CURRENCY("Currency"),
        RARE_ITEMS("Rare-Items"),
        BOOSTERS_COIN("Boosters"),
        BOOSTERS_XP("Boosters"),
        COSMETICS_FRAME("Cosmetics"),
        COSMETICS_BOARD("Cosmetics"),
        LICENSE_BG("License BG"),
        LICENSE_FG("License FG"),
        LICENSE_RY("License RY"),
        LICENSE_ST("License ST"),
        MATERIALS("Materials"),
        INSTANT_USE("Instant Use");

        final public String name;
        ItemType(String name) {
            this.name = name;
        }
        public String getName(InteractionHook M) {
            return TL(M, name);
        }
    }
    public static class Item_Count<T> extends DatabaseObject<T> {
        public long ItemID;
        public int Amount;

        public Item getItem() {
            return Item.get(ItemID);
        }
        public long getId() {
            return getItem().getId();
        }
        public String getName() {
            return getItem().getName();
        }
        public String getDescription() {
            return getItem().getDescription();
        }
        public ItemType getType() {
            return getItem().getType();
        }
        public BotEmoji getEmoji() {
            return getItem().getEmoji();
        }
        public String getEmojiFormatted() {
            return getEmoji().getFormatted();
        }

        private Item_Count() {}
        public static Item_Count<?> of(long itemid, int amount) {
            Item_Count<?> I = new Item_Count<>();
            I.ItemID = itemid;
            I.Amount = amount;
            return I;
        }
        public static Item_Count<?> of(String name, int amount) {
            Item_Count<?> I = new Item_Count<>();
            I.ItemID = Item.get(name).getId();
            I.Amount = amount;
            return I;
        }

        @Override
        public String toString() {
            return getEmojiFormatted() + " " + PRICEDECIMAL.format(Amount);
        }

    }

    public static class Item_To_Profile extends Item_Count<Item_To_Profile> {
        public long UserID;

        private Item_To_Profile() {}
        public Item_To_Profile(long userid, long itemid, int amount) {
            this.ItemID = itemid;
            this.UserID = userid;
            this.Amount = amount;
            Write();
        }

        @Override
        protected List<String> IDFields() {
            return List.of("ItemID", "UserID");
        }
    }
}
