package iecompbot.objects.clan;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.profile.item.Item;
import iecompbot.springboot.data.DatabaseObject;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Clan_License extends DatabaseObject<Clan_License> {
    public long ID;
    public long CardBackground = 40001;
    public long CardForeground = 50001;
    public long CardRay = 60001;
    public long CardStrike = 70001;
    public long Sponsor = 871133534184681523L;

    public long getID() {
        return ID;
    }
    public long getSponsor() {
        return Sponsor;
    }

    public Item getCardBackgroundItem() {
        return Item.get(CardBackground);
    }
    public Item getCardForegroundItem() {
        return Item.get(CardForeground);
    }
    public Item getCardRayItem() {
        return Item.get(CardRay);
    }
    public Item getCardStrikeItem() {
        return Item.get(CardStrike);
    }

    public void setCardBackground(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_BG)) CardBackground = i.getId();
    }
    public void setCardRay(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_RY)) CardRay = i.getId();
    }
    public void setCardForeground(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_FG)) CardForeground = i.getId();
    }
    public void setCardStrike(Item i) {
        if (i.getType().equals(Item.ItemType.LICENSE_ST)) CardStrike = i.getId();
    }
    public void setSponsor(long sponsor) {
        Sponsor = sponsor;
    }


    private Clan_License() {}
    public Clan_License(long clanID){
        this.ID = clanID;
        Write();
    }
    public static Clan_License get(long id) {
        return getWhere(Clan_License.class, "ID = ?", id).orElseGet(() -> new Clan_License(id));
    }

}
