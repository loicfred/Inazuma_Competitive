package iecompbot.springboot;

import static iecompbot.Constants.POWERDECIMAL;

public class Utils {

    public String darkenColor(String hexColor, double factor) {
        hexColor = hexColor.replace("#", "");

        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);

        r = (int) Math.max(0, r * factor);
        g = (int) Math.max(0, g * factor);
        b = (int) Math.max(0, b * factor);

        return String.format("#%02X%02X%02X", r, g, b);
    }

    public String format(double num) {
        return POWERDECIMAL.format(num);
    }

    public String StopString(String input, int maxCharactersPerLine) {
        return input == null ? "" : (input.length() > maxCharactersPerLine ? input.substring(0, maxCharactersPerLine - 3) + "..." : input);
    }

    public String formatPosition(int num) {
        return num == 0 ? "OUT" : num == 1 ? "1st" : num == 2 ? "2nd" : num == 3 ? "3rd" : num + "th";
    }

}