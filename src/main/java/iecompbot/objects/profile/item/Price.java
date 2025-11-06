package iecompbot.objects.profile.item;

import java.util.ArrayList;
import java.util.List;

import static iecompbot.springboot.config.AppConfig.cacheService;

public class Price extends BasePrice<Price> {

    private Price() {}
    public Price(List<Item.Item_Count<?>> i) {
        init(i);
    }

    public static Price get(long id) {
        return cacheService.getCachedPrice(id);
    }
    public static Price of(List<Item.Item_Count<?>> inv) {
        StringBuilder query = new StringBuilder();
        List<Object> o = new ArrayList<>();
        int i = 1;
        for (Item.Item_Count<?> I : inv) {
            query.append("AND ItemID").append(i).append(" = ? AND Amount").append(i).append(" = ? ");
            o.add(I.getId());
            o.add(I.Amount);
            i++;
        }
        return getWhere(Price.class, query.toString().replaceFirst("AND ", ""), o.toArray()).orElseGet(() -> new Price(inv));
    }
    public static Price of(Item.Item_Count<?>... inv) {
        return of(List.of(inv));
    }
}
