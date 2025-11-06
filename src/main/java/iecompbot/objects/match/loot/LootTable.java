package iecompbot.objects.match.loot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public record LootTable(List<DropEntry> entries) {

    public String pickRandomItem() {
        int totalWeight = entries().stream().mapToInt(DropEntry::weight).sum();
        int rand = new Random().nextInt(totalWeight);
        int current = 0;
        for (DropEntry entry : entries()) {
            current += entry.weight();
            if (rand < current) return entry.itemName();
        }
        return null;
    }

    public record DropEntry(String itemName, int weight) {
    }

    public static final Map<String, LootTable> GAME_LOOT_TABLES = new HashMap<>();

    static {
        GAME_LOOT_TABLES.put("IEGOGLX", new LootTable(List.of(
                new DropEntry("Solar Fragment", 25),
                new DropEntry("Ocean Crystal", 25),
                new DropEntry("Crimson Firestone", 25),
                new DropEntry("Fluorescent Stone", 25)
        )));

        GAME_LOOT_TABLES.put("IEGOCS", new LootTable(List.of(
                new DropEntry("Red C.Stone", 15),
                new DropEntry("Blue C.Stone", 15),
                new DropEntry("Green C.Stone", 15),
                new DropEntry("Orange C.Stone", 15),
                new DropEntry("Purple C.Stone", 15),
                new DropEntry("Mysterious Ampoule", 10),
                new DropEntry("Zanark Motorcycle", 5)
        )));

        GAME_LOOT_TABLES.put("IEGO1", new LootTable(List.of(
                new DropEntry("White Scarf", 50),
                new DropEntry("Fifth Sector Insignia", 50)
        )));

        GAME_LOOT_TABLES.put("IE2", new LootTable(List.of(
                new DropEntry("Alius Black Ball", 30),
                new DropEntry("White Scarf", 30),
                new DropEntry("Emperor Penguin", 40)
        )));

        GAME_LOOT_TABLES.put("IE3", new LootTable(List.of(
                new DropEntry("Dark and Light Figurines", 40),
                new DropEntry("Ogres Shield", 30),
                new DropEntry("Emperor Penguin", 30)
        )));

        GAME_LOOT_TABLES.put("IEGOSTR", new LootTable(List.of(
                new DropEntry("Red C.Stone", 10),
                new DropEntry("Blue C.Stone", 10),
                new DropEntry("Green C.Stone", 10),
                new DropEntry("Orange C.Stone", 10),
                new DropEntry("Purple C.Stone", 10),
                new DropEntry("Fifth Sector Insignia", 10),
                new DropEntry("Alius Black Ball", 10),
                new DropEntry("White Scarf", 10),
                new DropEntry("Dark and Light Figurines", 10),
                new DropEntry("Ogres Shield", 10),
                new DropEntry("Emperor Penguin", 10)
        )));

        GAME_LOOT_TABLES.put("IEVR", new LootTable(List.of(
                new DropEntry("Red C.Stone", 6),
                new DropEntry("Blue C.Stone", 6),
                new DropEntry("Green C.Stone", 6),
                new DropEntry("Orange C.Stone", 6),
                new DropEntry("Purple C.Stone", 6),
                new DropEntry("Fifth Sector Insignia", 6),
                new DropEntry("Alius Black Ball", 6),
                new DropEntry("White Scarf", 6),
                new DropEntry("Dark and Light Figurines", 6),
                new DropEntry("Ogres Shield", 6),
                new DropEntry("Inspirational Quotes", 6),
                new DropEntry("Shadow Plans", 6),
                new DropEntry("Manager Notes", 6),
                new DropEntry("Old Notebook", 4),
                new DropEntry("Inazuma CD", 4),
                new DropEntry("Emperor Penguin", 10)
        )));
    }

}

