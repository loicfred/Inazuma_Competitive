package iecompbot.objects;

import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static my.utilities.util.Utilities.similarity;

public class Nationality {
    private static List<Nationality> allnationalities = null;

    public String Name;
    public String Country;
    public String NativeName;
    public String FlagUnicode;

    public String getName() {
        return Name;
    }

    public String getCountry() {
        return Country;
    }

    public String getNativeName() {
        return NativeName;
    }

    public String getUnicode() {
        return FlagUnicode;
    }

    public Emoji getFlag() {
        return Emoji.fromUnicode(getUnicode());
    }
    public String getNamePlusFlag() {
        return getFlag().getFormatted() + " " + getName();
    }

    public String getCodepoints() {
        String[] codePoints = FlagUnicode.split(" ");
        StringBuilder flagEmoji = new StringBuilder();
        for (String codePoint : codePoints) {
            if (codePoint.startsWith("U+")) codePoint = codePoint.substring(2);
            int point = Integer.parseInt(codePoint, 16);
            flagEmoji.append(new String(Character.toChars(point)));
        }

        return flagEmoji.toString();
    }

    public static List<Nationality> getNationalities() {
        return allnationalities == null ? allnationalities = DatabaseObject.getAll(Nationality.class) : allnationalities;
    }

    public static Nationality get(String nationality) {
        List<Nationality> N = new ArrayList<>(getNationalities());
        N.sort(Comparator.comparingDouble((Nationality obj) -> getHighestSim(nationality, obj.Name, obj.Country, obj.NativeName)).reversed());
        if (getHighestSim(nationality, N.getFirst().Name, N.getFirst().Country, N.getFirst().NativeName) >= 70) {
            return N.getFirst();
        } return N.stream().filter(NN -> NN.Name.equals("International")).findFirst().orElseGet(N::getFirst);
    }

    private static double getHighestSim(String input, String compare1, String compare2, String compare3) {
        return Math.max(similarity(input, compare1, true),Math.max(similarity(input, compare2, true), similarity(input, compare3, true)));
    }


    @Override
    public String toString() {
        return Name;
    }
}
