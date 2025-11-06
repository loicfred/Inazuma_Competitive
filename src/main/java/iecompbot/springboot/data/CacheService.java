package iecompbot.springboot.data;

import iecompbot.objects.BotEmoji;
import iecompbot.objects.match.League;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.item.Price;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static iecompbot.objects.server.ServerInfo.CleanServers;
import static iecompbot.springboot.data.DatabaseObject.doQuery;
import static iecompbot.springboot.data.DatabaseObject.getAllWhere;

@Service
public class CacheService {

    private final CacheManager cacheManager;
    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Cacheable(value = "clan_captain", key = "#clanId", unless = "#result == null")
    public ClanMember getCachedCaptain(long clanId) {
        return doQuery(ClanMember.class,"SELECT cm.* FROM inazuma_competitive.clanmember cm " +
                        "JOIN inazuma_competitive.clanmember_to_clanrole CC ON cm.ID = CC.ClanMemberID " +
                        "JOIN inazuma_competitive.clanrole cr ON cr.ID = CC.ClanRoleID " +
                        "WHERE cr.Name = 'Clan Captain' AND cr.isBuiltin = true AND cm.ClanID = ?"
                , clanId).orElse(null);
    }


    @Cacheable(value = "obj_item", key = "#id", unless = "#result == null")
    public Item getCachedItem(long id) {
        return DatabaseObject.getById(Item.class, id).orElse(null);
    }
    @Cacheable(value = "obj_item", key = "#name + '-/-' + #serverid", unless = "#result == null")
    public Item getCachedItemByName(String name, Long serverid) {
        return DatabaseObject.getWhere(Item.class, "Name = ? AND ServerID = ?", name, serverid).orElse(null);
    }
    @Cacheable(value = "obj_item", key = "#serverid + '_Currency'", unless = "#result == null")
    public Item getCachedCurrency(long serverid) {
        return Item.getWhere(Item.class, "ServerID = ? AND Type = ?", serverid, Item.ItemType.CURRENCY.name()).orElse(null);
    }
    @Cacheable(value = "obj_item_list", key = "#type + '-/-' + #serverid", unless = "#result == null")
    public List<Item> getCachedItemByType(Item.ItemType type, Long serverid) {
        return getAllWhere(Item.class, "Type = ? AND ServerID = ?", type.name(), serverid);
    }

    @Cacheable(value = "obj_price", key = "#id", unless = "#result == null")
    public Price getCachedPrice(long id) {
        return DatabaseObject.getById(Price.class, id).orElse(null);
    }


    @Cacheable(value = "obj_emoji", key = "#id", unless = "#result == null")
    public BotEmoji getCachedEmojiById(long id) {
        return DatabaseObject.getById(BotEmoji.class, id).orElse(null);
    }
    @Cacheable(value = "obj_emoji", key = "#name", unless = "#result == null")
    public BotEmoji getCachedEmojiByName(String name) {
        return DatabaseObject.getWhere(BotEmoji.class, "Name = ?", name).orElse(null);
    }
    @Cacheable(value = "obj_emoji", key = "#name", unless = "#result == null")
    public BotEmoji getCachedServerEmojiByName(String name, long serverId) {
        return DatabaseObject.getWhere(BotEmoji.class, "Name = ? AND ServerID = ?", name, serverId).orElse(null);
    }
    @Cacheable(value = "obj_emoji", key = "#formatted", unless = "#result == null")
    public BotEmoji getCachedEmojiByFormatted(String formatted) {
        return DatabaseObject.getWhere(BotEmoji.class, "Formatted = ?", formatted).orElse(null);
    }



    @Cacheable(value = "obj_league", key = "#id", unless = "#result == null")
    public League getCachedLeague(long id) {
        return DatabaseObject.getById(League.class, id).orElse(null);
    }
    @Cacheable(value = "obj_league_tier", key = "#id", unless = "#result == null")
    public League.League_Tier getCachedLeagueTier(long id) {
        return DatabaseObject.getById(League.League_Tier.class, id).orElse(null);
    }



    public void clearCache(String cache) {
        Cache c = cacheManager.getCache(cache);
        if (c != null) {
            c.invalidate();
            c.clear();
        }
    }

    public int clearAllCaches() {
        int i = 0;
        for (String cache : cacheManager.getCacheNames()) {
            Cache c = cacheManager.getCache(cache);
            i++;
            if (c != null) {
                c.invalidate();
                c.clear();
            }
        }
        CleanServers();
        return i;
    }
}