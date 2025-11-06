package iecompbot.objects.profile.item;

import iecompbot.springboot.data.DatabaseObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BasePrice<T> extends DatabaseObject<T> {
    public long ID;
    public Long ItemID1;
    public Long ItemID2;
    public Long ItemID3;
    public Long ItemID4;
    public Long ItemID5;
    public Integer Amount1;
    public Integer Amount2;
    public Integer Amount3;
    public Integer Amount4;
    public Integer Amount5;

    public long getId() {
        return ID;
    }

    public List<Item.Item_Count<?>> list() {
        List<Item.Item_Count<?>> O = new ArrayList<>();
        if (ItemID1 != null) O.add(Item.Item_Count.of(ItemID1, Amount1));
        if (ItemID2 != null) O.add(Item.Item_Count.of(ItemID2, Amount2));
        if (ItemID3 != null) O.add(Item.Item_Count.of(ItemID3, Amount3));
        if (ItemID4 != null) O.add(Item.Item_Count.of(ItemID4, Amount4));
        if (ItemID5 != null) O.add(Item.Item_Count.of(ItemID5, Amount5));
        return O;
    }
    protected void init(List<Item.Item_Count<?>> i) {
        ID = Instant.now().toEpochMilli();
        for (Item.Item_Count<?> item : i) {
            if (ItemID1 == null) {
                ItemID1 = item.getId();
                Amount1 = item.Amount;
            } else if (ItemID2 == null) {
                ItemID2 = item.getId();
                Amount2 = item.Amount;
            } else if (ItemID3 == null) {
                ItemID3 = item.getId();
                Amount3 = item.Amount;
            } else if (ItemID4 == null) {
                ItemID4 = item.getId();
                Amount4 = item.Amount;
            } else if (ItemID5 == null) {
                ItemID5 = item.getId();
                Amount5 = item.Amount;
            }
        }
        Write();
    }
}
