package iecompbot.objects.profile.item;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URI;

public class Scoreboard {
    public String Name = null;

    public Image WinBoard = null;
    public Image LoseBoard = null;
    public Image TieBoard = null;

    public Scoreboard(Item item) {
        if (item != null && item.getType().equals(Item.ItemType.COSMETICS_BOARD)) {
            try {
                Name = item.getName();
                WinBoard = ImageIO.read(URI.create(item.getImageURL()).toURL());
                LoseBoard = ImageIO.read(URI.create(item.getImageURL2()).toURL());
                TieBoard = ImageIO.read(URI.create(item.getImageURL3()).toURL());
                if (TieBoard == null) TieBoard = WinBoard;
                if (LoseBoard == null) LoseBoard = TieBoard;
            } catch (Exception ignored) {}
        }
    }
}
