package iecompbot.objects.profile.quest;

import iecompbot.objects.profile.item.BasePrice;
import iecompbot.objects.profile.item.Item;

import java.util.ArrayList;
import java.util.List;

public class Rewards extends BasePrice<Rewards> {

    private Rewards() {}
    public Rewards(List<Item.Item_Count<?>> i) {
        init(i);
    }

    public static Rewards get(long id) {
        return getById(Rewards.class, id).orElse(null);
    }
    public static Rewards of(List<Item.Item_Count<?>> inv) {
        StringBuilder query = new StringBuilder();
        List<Object> o = new ArrayList<>();
        int i = 1;
        for (Item.Item_Count<?> I : inv) {
            query.append("AND ItemID").append(i).append(" = ? AND Amount").append(i).append(" = ? ");
            o.add(I.getId());
            o.add(I.Amount);
            i++;
        }
        return getWhere(Rewards.class, query.toString().replaceFirst("AND ", ""), o.toArray()).orElseGet(() -> new Rewards(inv));
    }
    public static Rewards of(Item.Item_Count<?>... inv) {
        return of(List.of(inv));
    }
}
