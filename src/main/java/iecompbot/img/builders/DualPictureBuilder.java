package iecompbot.img.builders;

import iecompbot.Main;
import iecompbot.objects.match.Game;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import static iecompbot.img.ImgUtilities.CutDiagonalBottomRight;
import static iecompbot.img.ImgUtilities.CutDiagonalTopLeft;

public class DualPictureBuilder extends ImageBuilder {

    private final Game game;
    private BufferedImage C1;
    private BufferedImage C2;

    public DualPictureBuilder(User u1, User u2, Game g) {
        game = g;
        try {if (u1 != null) C1 = CutDiagonalTopLeft(ImageIO.read(URI.create(u1.getEffectiveAvatarUrl().replace(".gif", ".png")).toURL()));
        } catch (IOException ignored) {}
        try {if (u2 != null) C2 = CutDiagonalBottomRight(ImageIO.read(URI.create(u2.getEffectiveAvatarUrl().replace(".gif", ".png")).toURL()));
        } catch (IOException ignored) {}
    }
    public DualPictureBuilder(byte[] u1, byte[] u2, Game g) {
        game = g;
        try (InputStream is = new ByteArrayInputStream(u1)) {
            C1 = CutDiagonalTopLeft(ImageIO.read(is));
        } catch (IOException ignored) {}
        try (InputStream is = new ByteArrayInputStream(u2)) {
            C2 = CutDiagonalBottomRight(ImageIO.read(is));
        } catch (IOException ignored) {}
    }

    public synchronized DualPictureBuilder GeneratePicturePNG() {
        PNG = new BufferedImage(500, 500, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = PNG.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(0, 0, PNG.getWidth(), PNG.getHeight());

        try {g2d.drawImage(C1,0,0,500,500, null);
        } catch (Exception ignored) {}
        try {g2d.drawImage(C2,0,0,500,500, null);
        } catch (Exception ignored) {}

        Image ImgVS = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/static/img/match/VS.png"))).getImage();
        if (game.isStrikers()) {
            ImgVS = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/static/img/match/VS2.png"))).getImage();
        } else if (game.isVR()) {
            ImgVS = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/static/img/match/VS3.png"))).getImage();
        }
        g2d.drawImage(ImgVS, (500 / 2) - 184, 250 - 115, 368, 230, null);
        g2d.dispose();
        return this;
    }
}