package iecompbot.objects.clan.items;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;

public enum MemberType {
    CAPTAIN("Clan Captain"),
    CO_CAPTAIN("Co-Captain"),
    BROADCASTER("Broadcaster"),
    DESIGNER("Graphic Designer"),
    MANAGER("Manager"),
    MODDER("Modder"),
    SCOUTER("Scouter"),
    TRAINER("Trainer"),
    SPEEDRUNNER("Speedrunner"),
    CHEERLEADER("Cheerleader"),
    THEORYCRAFTER("Theorycrafter"),
    UNOFFICIAL("Ghost"),
    MEMBER("Member");

    private final String name;

    public String getEmoji() {
        if (this.equals(CAPTAIN)) {
            return "U+1F451";
        } else if (this.equals(CO_CAPTAIN)) {
            return "U+2B50";
        } else if (this.equals(MANAGER)) {
            return "U+1F9D1 U+200D U+1F4BC";
        } else if (this.equals(BROADCASTER)) {
            return "U+1F3A5";
        } else if (this.equals(MODDER)) {
            return "U+1F4BB";
        } else if (this.equals(DESIGNER)) {
            return "U+1F9D1 U+200D U+1F3A8";
        } else if (this.equals(SCOUTER)) {
            return "U+1F575 U+FE0F";
        } else if (this.equals(TRAINER)) {
            return "U+1F468 U+200D U+1F3EB";
        } else if (this.equals(SPEEDRUNNER)) {
            return "U+1F3C3";
        } else if (this.equals(CHEERLEADER)) {
            return "U+1F46F";
        } else if (this.equals(THEORYCRAFTER)) {
            return "U+1F4DA";
        } else if (this.equals(UNOFFICIAL)) {
            return "U+1F47B";
        } else {
            return "U+1F464";
        }
    }
    public String getName() {
        return name;
    }
    public String getTL() {
        return TL((InteractionHook) null, name);
    }
    public String getTLG(Guild G) {
        return TLG(G, name);
    }

    public static MemberType get(String name) {
        for (MemberType v : values()) {
            if (v.name.equals(name)) {
                return v;
            }
        }
        return null;
    }
    MemberType(String name) {
        this.name = name;
    }
}