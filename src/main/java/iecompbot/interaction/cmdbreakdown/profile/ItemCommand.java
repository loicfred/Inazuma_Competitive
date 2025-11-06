package iecompbot.interaction.cmdbreakdown.profile;

import iecompbot.objects.profile.item.Item;
import net.dv8tion.jda.api.entities.User;

public class ItemCommand extends ProfileCommand {
    public long ItemID;
    public Item I;

    public ItemCommand(String command) {
        super(command);
        if (command.split("/").length > 2) {
            ItemID = Long.parseLong(command.split("/")[2]);
        }
    }
    public ItemCommand(User u) {
        super("");
        ID = u.getIdLong();
    }

    public String getYourBudget() {
        String S = "";
        for (Item.Item_Count<?> I : getItem().getPrice().list()) S = S + " • " + I.getEmojiFormatted() + " " + getProfile().getItem(I.getId()).Amount;
        return S.replaceFirst(" • ", "");
    }

    public boolean canAfford() {
        return getItem().getPrice().list().stream().anyMatch(C -> C.Amount > getProfile().getItem(C.getId()).Amount);
    }
    public void Purchase() {
        if (canAfford()) {
            for (Item.Item_Count<?> C : getItem().getPrice().list()) {
                if (!C.getType().equals(Item.ItemType.RARE_ITEMS)) {
                    getProfile().removeItem(C.getId(), C.Amount);
                }
            }
            getProfile().addItem(ItemID, 1);
        }
    }
    public int getAmountOwned() {
        return getProfile().getItem(ItemID).Amount;
    }

    public Item getItem() {
        return I == null ? I = Item.get(0) : I;
    }

    public String toCommand() {
        try {
            return super.Command + "/" + ID + "/" + ItemID + "/";
        } catch (NullPointerException ignored) {}
        return super.Command + "/" + ID + "/" + null + "/";
    }
}