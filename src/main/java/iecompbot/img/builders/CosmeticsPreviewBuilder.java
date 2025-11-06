package iecompbot.img.builders;

import com.vdurmont.emoji.EmojiParser;
import iecompbot.interaction.Automation;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.text.AttributedString;

import static iecompbot.Main.newRodinPro;
import static iecompbot.img.ImgUtilities.CreateNumber;
import static iecompbot.img.ImgUtilities.getWidthOfAttributedString;
import static iecompbot.objects.Retrieval.getUserByID;

public class CosmeticsPreviewBuilder extends ImageBuilder {

    private final User user1;
    private final Item cosmetic;

    public CosmeticsPreviewBuilder(User user1, Item cosmetic) {
        this.user1 = user1;
        this.cosmetic = cosmetic;
    }

    public synchronized CosmeticsPreviewBuilder GenerateCardPNG() {
        try {
            if (cosmetic.getType().equals(Item.ItemType.COSMETICS_FRAME)) {
                PNG = new BufferedImage(450, 450, BufferedImage.TRANSLUCENT);
                Graphics2D g2d = PNG.createGraphics();

                Image avatar = ImageIO.read(URI.create(user1.getEffectiveAvatarUrl().replace(".gif", ".png")).toURL());
                g2d.drawImage(avatar, 75, 75, 300, 300, null);

                Profile P = Profile.get(user1);
                Stroke stroke1 = new BasicStroke(12f);
                g2d.setColor(P.getColor());
                g2d.setStroke(stroke1);
                g2d.drawRect(75 - 6, 75 - 6, 312, 312);
                g2d.setColor(Color.white);
                g2d.drawImage(ImageIO.read(URI.create(cosmetic.getImageURL()).toURL()), 0, 0, 450, 450, null);
            } else if (cosmetic.getType().equals(Item.ItemType.COSMETICS_BOARD)) {
                PNG = new BufferedImage(900, 540, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = PNG.createGraphics();
                User user2 = getUserByID("974675718975946853");
                int Score1 = 3;
                int Score2 = 0;

                g2d.drawImage(ImageIO.read(URI.create(cosmetic.getImageURL()).toURL()), 0, 0, 900, 540, null);


                String name1 = user1.getEffectiveName();
                String name2 = user2.getEffectiveName();
                name1 = EmojiParser.removeAllEmojis(name1.replaceAll("Đ", "D"));
                name2 = EmojiParser.removeAllEmojis(name2.replaceAll("Đ", "D"));

                if (name1.length() > 11) {
                    name1 = name1.substring(0, 10) + "...";
                }
                AttributedString as1 = new AttributedString(name1);
                as1.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(20f));
                g2d.drawString(as1.getIterator(), (int) (350 - (getWidthOfAttributedString(g2d, as1) / 2)), 130);

                if (name2.length() > 11) {
                    name2 = name2.substring(0, 10) + "...";
                }

                AttributedString as2 = new AttributedString(name2);
                as2.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(20f));
                g2d.drawString(as2.getIterator(), (int) (900 - 345 - (getWidthOfAttributedString(g2d, as2) / 2)), 130);



                BufferedImage number1 = CreateNumber(Score1);
                g2d.drawImage(number1, 365 - (int) (number1.getWidth() * 1.5 / 1.5), 190, (int) (number1.getWidth() * 1.5), (int) (number1.getHeight() * 1.5), null);
                BufferedImage number2 = CreateNumber(Score2);
                g2d.drawImage(number2, (900 - 335) - (int) (number2.getWidth() * 1.5 / 1.5), 190, (int) (number2.getWidth() * 1.5), (int) (number2.getHeight() * 1.5), null);
                g2d.dispose();
            }
        } catch (Exception e) {
            Automation.handleException(e);
        }
        return this;
    }
}