package iecompbot.img.builders;

import com.vdurmont.emoji.EmojiParser;
import iecompbot.Main;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.server.ServerInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Objects;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.newRodinPro;
import static iecompbot.img.ImgUtilities.*;
import static my.utilities.util.Utilities.CharFix;
import static my.utilities.util.Utilities.StopString;

public class PredictionImageBuilder extends ImageBuilder {

    private final ServerInfo I;
    private final Profile P1;
    private final Profile P2;


    public PredictionImageBuilder(Profile P1, Profile P2, ServerInfo I) {
        this.P1 = P1;
        this.P2 = P2;
        this.I = I;
    }

    public synchronized PredictionImageBuilder GenerateCardPNG() {
         try {
            PNG = new BufferedImage(1600, 900, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = PNG.createGraphics();
            Image ImgBackground = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/BgRaimonGO.jpg"))).getImage();
            Image ImgDarkener = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/Black50.png"))).getImage();
            Image ImgAnime = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/Spike.png"))).getImage();
            Image ImgMiddleStrike = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/MiddleStrike.png"))).getImage();

            g2d.drawImage(ImgBackground, 0, 0,1600,900, null);
            g2d.drawImage(ImgDarkener, 0, 0,1600,900, null);
            g2d.drawImage(ImgAnime, 0, 0,1600,900, null);
            g2d.drawImage(ImgMiddleStrike, 800 - (ImgMiddleStrike.getWidth(null) * 3 / 2), 0, ImgMiddleStrike.getWidth(null) * 3,900, null);

            AddProfile1(g2d);
            AddProfile2(g2d);

            g2d.setColor(Color.white);

            WriteNames(g2d);
            WriteClans(g2d);
            AddWhoWillWin(g2d);

            Stroke stroke1 = new BasicStroke(26f);
            g2d.setColor(I != null ? I.getColor() : Color.orange);
            g2d.setStroke(stroke1);
            g2d.drawRect(0,0, 1600, 900);
            g2d.dispose();
        } catch (Exception ignored) {}
        return this;
    }
    public synchronized PredictionImageBuilder GenerateCardGIF(double sizemultiplier) {
        if (PNG != null) {
            Point2D.Float glintPos = new Point2D.Float(0, (int) (200 * sizemultiplier));
            BufferedImage IMG = ResizeImage(PNG, sizemultiplier);

            java.util.List<BufferedImage> frames = new ArrayList<>(); // Adjust this to your desired frame count
            while (glintPos.y < IMG.getHeight()) {
                // Move toward the target (corner-to-corner movement)
                float speed = (int) (10f * sizemultiplier);
                if (glintPos.x > 0) glintPos.x -= speed; // Move left
                if (glintPos.y < IMG.getHeight()) glintPos.y += speed; // Move down

                // Ensure the glint doesn't go out of bounds
                glintPos.x = Math.max(glintPos.x, 0);
                glintPos.y = Math.min(glintPos.y, IMG.getHeight());
            }
            GIF = frames;
        }
        return this;
    }


    private void AddProfile1(Graphics2D g2d) throws Exception {
        Image avatar = ImageIO.read(URI.create(P1.getUser().getEffectiveAvatarUrl().replace(".gif", ".png")).toURL());
        g2d.drawImage(avatar, 170, (900 / 2) - 170, 250, 250, null);
        Stroke stroke1 = new BasicStroke(10f);
        g2d.setColor(P1.getColor());
        g2d.setStroke(stroke1);
        g2d.drawRect(170 - 5, (900 / 2) - 175, 260, 260);
        Item cosmetics = P1.getCustomFrameItem();
        if (cosmetics != null) g2d.drawImage(ImageIO.read(URI.create(cosmetics.getImageURL()).toURL()), 170 - 80, (900 / 2) - 170 - 80, 410, 410, null);
    }
    private void AddProfile2(Graphics2D g2d) throws Exception {
        Image avatar = ImageIO.read(URI.create(P2.getUser().getEffectiveAvatarUrl().replace(".gif", ".png")).toURL());
        g2d.drawImage(avatar, 1600 - 170 - 250, (900 / 2) - 170, 250, 250, null);
        Stroke stroke1 = new BasicStroke(10f);
        g2d.setColor(P2.getColor());
        g2d.setStroke(stroke1);
        g2d.drawRect(1600 - 170 - 250 - 5, (900 / 2) - 175, 260, 260);
        Item cosmetics = P2.getCustomFrameItem();
        if (cosmetics != null) g2d.drawImage(ImageIO.read(URI.create(cosmetics.getImageURL()).toURL()), 1600 - 170 - 250 - 80, (900 / 2) - 170 - 80, 410, 410, null);
    }
    private void WriteClans(Graphics2D g2d) throws IOException {
        Image ImgZapLeft = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/ZapLeft.png"))).getImage();
        Image ImgZapRight = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/ZapRight.png"))).getImage();
        Clan c1 = Clan.getClanOfUser(P1.getId());
        if (c1 != null) {
            g2d.drawImage(ImgZapLeft, 10, 0, 125 * 4 ,48 * 4, null);
            if (c1.hasEmblem()) g2d.drawImage(CutTransparentBorders(c1.getEmblem()),10, 20, 140, 140, null);
            AddText(g2d,c1.getName(),c1.getColor(), newRodinPro,150,80,35f,false,false,true,2f,Color.black);
            AddText(g2d, "No. " + c1.getMemberById(P1.getId()).Number,Color.WHITE, newRodinPro,150,120,32f,false,false,true,2f,Color.black);

            Stroke stroke1 = new BasicStroke(11f);
            g2d.setColor(c1.getColor());
            g2d.setStroke(stroke1);
            g2d.setColor(Color.decode("#808080"));
        }
        Clan c2 = Clan.getClanOfUser(P2.getId());
        if (c2 != null) {
            g2d.drawImage(ImgZapRight,  1600 - (125 * 4) - 10, 0, 125 * 4 ,48 * 4, null);
            if (c2.hasEmblem()) g2d.drawImage(CutTransparentBorders(c2.getEmblem()),  1600 - 140 - 10, 20, 140,140, null);
            AddText(g2d, c2.getName(),c2.getColor(),newRodinPro,1600 - 150,80,35f,false,true,true,2f,Color.black);
            AddText(g2d, "No. " + c2.getMemberById(P2.getId()).Number,Color.WHITE,newRodinPro,1600 - 150,120,32f,false,true,true,2f,Color.black);

            Stroke stroke1 = new BasicStroke(11f);
            g2d.setColor(c2.getColor());
            g2d.setStroke(stroke1);
            g2d.setColor(Color.decode("#808080"));
        }

    }
    private void WriteNames(Graphics2D g2d) {
        String name1 = StopString(CharFix(EmojiParser.removeAllEmojis(I != null && I.getGuild().getMemberById(P1.getId()) != null ? I.getGuild().getMemberById(P1.getId()).getEffectiveName() : P1.getUser().getEffectiveName())), 20);
        String name2 = StopString(CharFix(EmojiParser.removeAllEmojis(I != null && I.getGuild().getMemberById(P2.getId()) != null ? I.getGuild().getMemberById(P2.getId()).getEffectiveName() : P2.getUser().getEffectiveName())), 20);

        AttributedString as1 = new AttributedString(name1);
        as1.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(40f));
        g2d.drawString(as1.getIterator(), (int) (300 - (getWidthOfAttributedString(g2d, as1) / 2)), (900 / 2) + 190);

        AttributedString as2 = new AttributedString(name2);
        as2.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(40f));
        g2d.drawString(as2.getIterator(), (int) (1300 - (getWidthOfAttributedString(g2d, as2) / 2)), (900 / 2) + 190);

        Image IE1Logo = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/static/img/games/IE1.png"))).getImage();
        Image ImgVS = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/static/img/match/VS.png"))).getImage();
        g2d.drawImage(IE1Logo, (1600 / 2) - 256, -100, 512, 512, null);
        g2d.drawImage(ImgVS, (int) ((1600 / 2) - (400 * 0.7)), (int) ((900 / 2) - (250 * 0.7)), (int) (ImgVS.getWidth(null) * 0.7) * 2, (int) (ImgVS.getHeight(null)  * 0.7) * 2, null);
    }
    private void AddWhoWillWin(Graphics2D g2d) {
        AttributedString Title = new AttributedString(I != null ? TLG(I,"Who-will-win-the-match") : TL(P1,"Who-will-win-the-match"));
        Title.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(60f));
        Title.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);
        g2d.drawString(Title.getIterator(), (int) (800 - (getWidthOfAttributedString(g2d, Title) / 2)), 760);
    }


}