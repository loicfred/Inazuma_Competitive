package iecompbot;


import iecompbot.objects.clan.Clan;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.ServerInfo;
import my.utilities.lang.Nationality;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class L10N extends my.utilities.lang.L10N {
    public static ResourceBundle RBG;
    public static ResourceBundle MRB;

    public static ResourceBundle getLanguageBundle(String nat) {
        return getLanguageBundle(Nationality.getNationality(nat));
    }
    public static ResourceBundle getLanguageBundle(Nationality nat) {
        Locale locale;
        if (nat.equals(Nationality.French) || nat.equals(Nationality.Swiss) || nat.equals(Nationality.Moroccan) || nat.equals(Nationality.Tunisian) || nat.equals(Nationality.Guadeloupean) || nat.equals(Nationality.Belgian)) {
            locale = Locale.of("fr", "FR");
        } else if (nat.equals(Nationality.Italian)) {
            locale = Locale.of("it", "IT");
        } else if (nat.equals(Nationality.Spanish) || nat.equals(Nationality.Mexican)) {
            locale = Locale.of("es", "ES");
        } else if (nat.equals(Nationality.German) || nat.equals(Nationality.Australian)) {
            locale = Locale.of("de", "DE");
        } else if (nat.equals(Nationality.Brazilian) || nat.equals(Nationality.Portuguese)) {
            locale = Locale.of("pt", "PT");
        } else {
            locale = Locale.of("en", "US");
        }
        return ResourceBundle.getBundle("lang/texts", locale, Main.class.getClassLoader());
    }
    public static ResourceBundle getLanguageBundle(DiscordLocale lang) {
        Locale locale;
        if (lang.getLanguageName().contains(DiscordLocale.FRENCH.getLanguageName())) {
            locale = Locale.of("fr", "FR");
        } else if (lang.getLanguageName().contains(DiscordLocale.ITALIAN.getLanguageName())) {
            locale = Locale.of("it", "IT");
        } else if (lang.getLanguageName().contains(DiscordLocale.SPANISH.getLanguageName())) {
            locale = Locale.of("es", "ES");
        } else if (lang.getLanguageName().contains(DiscordLocale.GERMAN.getLanguageName())) {
            locale = Locale.of("de", "DE");
        } else if (lang.getLanguageName().contains(DiscordLocale.PORTUGUESE_BRAZILIAN.getLanguageName())) {
            locale = Locale.of("pt", "PT");
        } else {
            locale = Locale.of("en", "US");
        }
        return ResourceBundle.getBundle("lang/texts", locale, Main.class.getClassLoader());
    }

    private static String TL(String key) {
        try {
            String s = RB.getString(key.replaceAll(" ", "-"));
            return s.replaceAll("<br>", "\n");
        } catch (MissingResourceException | NullPointerException | IllegalArgumentException e) {
            return key;
        }
    }
    private static String TL(String key, Object... var) {
        try {
            String s = TL(key);
            if (var.length > 0) s = s.replaceAll("<v1>", removeRegex(var[0] + ""));
            if (var.length > 1) s = s.replaceAll("<v2>", removeRegex(var[1] + ""));
            if (var.length > 2) s = s.replaceAll("<v3>", removeRegex(var[2] + ""));
            if (var.length > 3) s = s.replaceAll("<v4>", removeRegex(var[3] + ""));
            if (var.length > 4) s = s.replaceAll("<v5>", removeRegex(var[4] + ""));
            if (var.length > 5) s = s.replaceAll("<v6>", removeRegex(var[5] + ""));
            return s;
        } catch (MissingResourceException | NullPointerException | IllegalArgumentException e) {
            return key;
        }
    }

    public static String TL(InteractionHook m, String key) {
        RB = getLanguageBundle(m.getInteraction().getUserLocale());
        return TL(key);
    }
    public static String TL(InteractionHook m, String key, Object... var) {
        RB = getLanguageBundle(m.getInteraction().getUserLocale());
        return TL(key, var);
    }

    public static String TL(Interaction event, String key) {
        RB = getLanguageBundle(event.getUserLocale());
        return TL(key);
    }
    public static String TL(Interaction event, String key, Object... var) {
        RB = getLanguageBundle(event.getUserLocale());
        return TL(key, var);
    }

    public static String TL(Profile P, String key) {
        RB = getLanguageBundle(P.getLanguage());
        return TL(key);
    }
    public static String TL(Profile P, String key, Object... var) {
        RB = getLanguageBundle(P.getLanguage());
        return TL(key, var);
    }

    public static String TL(Clan C, String key) {
        RB = getLanguageBundle(C.getNationality().toString());
        return TL(key);
    }
    public static String TL(Clan C, String key, Object... var) {
        RB = getLanguageBundle(C.getNationality().toString());
        return TL(key, var);
    }


    public static String TLG(Guild G, String key) {
        try {
            RBG = getLanguageBundle(G == null ? DiscordLocale.ENGLISH_UK : G.getLocale());
            String s = RBG.getString(key.replaceAll(" ", "-"));
            return s.replaceAll("<br>", "\n");
        } catch (MissingResourceException | NullPointerException | IllegalArgumentException e) {
            return key;
        }
    }
    public static String TLG(Guild G, String key, Object... var) {
        try {
            RBG = getLanguageBundle(G == null ? DiscordLocale.ENGLISH_UK : G.getLocale());
            String s = RBG.getString(key.replaceAll(" ", "-"));
            if (var.length > 0) s = s.replaceAll("<v1>", removeRegex(var[0] + ""));
            if (var.length > 1) s = s.replaceAll("<v2>", removeRegex(var[1] + ""));
            if (var.length > 2) s = s.replaceAll("<v3>", removeRegex(var[2] + ""));
            if (var.length > 3) s = s.replaceAll("<v4>", removeRegex(var[3] + ""));
            if (var.length > 4) s = s.replaceAll("<v5>", removeRegex(var[4] + ""));
            if (var.length > 5) s = s.replaceAll("<v6>", removeRegex(var[5] + ""));
            return s.replaceAll("<br>", "\n");
        } catch (MissingResourceException | NullPointerException | IllegalArgumentException e) {
            return key;
        }
    }
    public static String TLG(ServerInfo I, String key) {
        return TLG(I == null ? null : I.getGuild(), key);
    }
    public static String TLG(ServerInfo I, String key, Object... var) {
        return TLG(I == null ? null : I.getGuild(), key, var);
    }
}