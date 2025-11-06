package iecompbot.img.builders;

import com.vdurmont.emoji.EmojiParser;
import iecompbot.Main;
import iecompbot.objects.clan.interclan.Interclan;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import static iecompbot.Main.ReggaeStdB;
import static iecompbot.Main.newRodinPro;
import static iecompbot.img.ImgUtilities.AddText;
import static iecompbot.img.ImgUtilities.AddTextCentered;

public class InterclanImageBuilder extends ImageBuilder {
    private final Interclan I;

    private final Image Top = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/Interclan/InterclanTop.png"))).getImage();
    private final Image Middle = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/Interclan/InterclanMiddle.png"))).getImage();
    private final Image Bottom = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/Interclan/InterclanBottom.png"))).getImage();
    private final Image ImgDefaultAvatar = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/AvatarDefault.png"))).getImage();

    public InterclanImageBuilder(Interclan I) {
        this.I = I;
    }

    public synchronized InterclanImageBuilder GeneratePicturePNG() {
        try {
            int TotalHeight = Top.getHeight(null) + Bottom.getHeight(null) + (Middle.getHeight(null) * I.getDuels().size());
            PNG = new BufferedImage(900, TotalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = PNG.createGraphics();
            g2d.drawImage(Top, 0, 0, Top.getWidth(null), Top.getHeight(null), null);
            for (int i = 0; i != I.getDuels().size(); i++) {
                int Y = Top.getHeight(null) + (Middle.getHeight(null) * i);
                g2d.drawImage(Middle, 0, Y, Middle.getWidth(null), Middle.getHeight(null), null);
                User P1 = I.getDuels().get(i).getP1();
                User P2 = I.getDuels().get(i).getP2();
                g2d.drawImage(ImageIO.read(I.getDuels().get(i).getGame().getLogoURL()), 400, Y, 102, 75, null);
                String name = P1.getEffectiveName();
                String name2 = P2.getEffectiveName();
                if (name.length() > 12) {
                    name = name.substring(0, 11) + "...";
                }
                if (name2.length() > 12) {
                    name2 = name2.substring(0, 11) + "...";
                }
                AddText(g2d, EmojiParser.removeAllEmojis(name), Color.BLACK, ReggaeStdB, 100, Y + 45, 22f, false, false);
                AddText(g2d, EmojiParser.removeAllEmojis(name2), Color.BLACK, ReggaeStdB, 800, Y + 45, 22f, false, true);
                if (P1.getAvatarUrl() != null) {
                    Image avatar = ImageIO.read(URI.create(P1.getAvatarUrl().replace(".gif", ".png")).toURL());
                    g2d.drawImage(avatar, 13, Y, 75, 75, null);
                } else {
                    g2d.drawImage(ImgDefaultAvatar, 13, Y, 75, 75, null);
                }
                if (P2.getAvatarUrl() != null) {
                    Image avatar = ImageIO.read(URI.create(P2.getAvatarUrl().replace(".gif", ".png")).toURL());
                    g2d.drawImage(avatar, 812, Y, 75, 75, null);
                } else {
                    g2d.drawImage(ImgDefaultAvatar, 812, Y, 75, 75, null);
                }

                AddTextCentered(g2d, String.valueOf(I.getDuels().get(i).getP1Score()), Color.BLACK, newRodinPro, 341, Y + 50, 36f);
                AddTextCentered(g2d, String.valueOf(I.getDuels().get(i).getP2Score()), Color.BLACK, newRodinPro, 559, Y + 50, 36f);
            }
            g2d.drawImage(Bottom, 0, Top.getHeight(null) + (Middle.getHeight(null) * I.getDuels().size()), Bottom.getWidth(null), Bottom.getHeight(null), null);
            AddTextCentered(g2d, String.valueOf(I.getHostTotalScore()), Color.BLACK, newRodinPro, 366, Top.getHeight(null) + (Middle.getHeight(null) * I.getDuels().size()) + 50, 42f);
            AddTextCentered(g2d, String.valueOf(I.getJoinTotalScore()), Color.BLACK, newRodinPro, 534, Top.getHeight(null) + (Middle.getHeight(null) * I.getDuels().size()) + 50, 42f);

            try (InputStream is = new ByteArrayInputStream(I.getHoster().getEmblem())) {
                g2d.setColor(I.getHoster().getColor());
                g2d.fillRect(13,13, 264, 264);
                g2d.drawImage(ImageIO.read(is), 13,13, 264, 264, null);
            } catch (Exception ignored) {}

            try (InputStream is = new ByteArrayInputStream(I.getJoiner().getEmblem())) {
                g2d.setColor(I.getJoiner().getColor());
                g2d.fillRect(623,13, 264, 264);
                g2d.drawImage(ImageIO.read(is), 623,13, 264, 264, null);
            } catch (Exception ignored) {}

            g2d.dispose();
        } catch (Exception ignored) {}
        return this;
    }



}
