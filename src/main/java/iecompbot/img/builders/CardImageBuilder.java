package iecompbot.img.builders;

import com.vdurmont.emoji.EmojiParser;
import iecompbot.Main;
import iecompbot.img.MyAttributedString;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static iecompbot.Main.*;
import static iecompbot.img.ImgUtilities.*;
import static my.utilities.util.Utilities.*;

public class CardImageBuilder extends ImageBuilder {

    private Clan clan;
    private final Profile P;

    private final Color TEXTCOLOR = Color.WHITE;
    private final Color OUTLINECOLOR = Color.BLACK;

    private Item BG = Item.get("40001");
    private Item FG = Item.get("50001");
    private Item RY = Item.get("60001");
    private Item ST = Item.get("70001");


    private String Number = "00";
    private long sponsor = 871133534184681523L;
    private Image BGIMG;
    private final Image Sponsor = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/Sponsor.png"))).getImage();
    private final Image Blurr = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/effect_blur_low.png"))).getImage();
    private final Image Captain = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/Captain.png"))).getImage();

    public CardImageBuilder(Profile p) {
        P = p;
        try {
            BG = P.getCardBackgroundItem();
            FG = P.getCardForegroundItem();
            RY = P.getCardRayItem();
            ST = P.getCardStrikeItem();
            sponsor = P.getSponsor();
        } catch (Exception ignored) {}
     }
    public CardImageBuilder(Profile p, Clan c) {
        P = p;
        clan = c;
        try {
            BG = c.getCardBackgroundItem();
            FG = c.getCardForegroundItem();
            RY = c.getCardRayItem();
            ST = c.getCardStrikeItem();
            sponsor = c.getSponsor();
            Number = clan.getMemberById(p.getId()).getNumber();
        } catch (Exception ignored) {}
    }

    public synchronized CardImageBuilder GenerateCardPNG() {
        try {
            BGIMG = ImageIO.read(URI.create(BG.getImageURL()).toURL());
            Image FGIMG = ImageIO.read(URI.create(FG.getImageURL()).toURL());
            Image RYIMG = ImageIO.read(URI.create(RY.getImageURL()).toURL());
            Image STIMG = ImageIO.read(URI.create(ST.getImageURL()).toURL());

            PNG = new BufferedImage(BGIMG.getWidth(null), BGIMG.getHeight(null), BufferedImage.TRANSLUCENT);
            Graphics2D g2d = PNG.createGraphics();
            g2d.drawImage(BGIMG, 0, 0, BGIMG.getWidth(null), BGIMG.getHeight(null), null);
            g2d.drawImage(RYIMG, 0, 0, BGIMG.getWidth(null), BGIMG.getHeight(null), null);
            g2d.drawImage(FGIMG, 0, 0, BGIMG.getWidth(null), BGIMG.getHeight(null), null);


            if (clan != null) {
                Color dominant = getDominantColor(clan.getEmblem());
                Color roleColor = clan.getColor();
                Color white;
                Color black;
                if (roleColor.getRed() + roleColor.getGreen() + roleColor.getBlue() > dominant.getRed() + dominant.getGreen() + dominant.getBlue()) {
                    white = roleColor;
                    black = dominant;
                } else {
                    white = dominant;
                    black = roleColor;
                }
                g2d.drawImage(ImageIO.read(fillPNGWhiteBlack(STIMG, white, black)), 0, 0, BGIMG.getWidth(null), BGIMG.getHeight(null), null);
            } else {
                Color dominant = getDominantColor(P.getUser().getEffectiveAvatarUrl());
                Color roleColor = P.getColor();
                Color white;
                Color black;
                if (roleColor.getRed() + roleColor.getGreen() + roleColor.getBlue() > dominant.getRed() + dominant.getGreen() + dominant.getBlue()) {
                    white = roleColor;
                    black = dominant;
                } else {
                    white = dominant;
                    black = roleColor;
                }
                g2d.drawImage(ImageIO.read(fillPNGWhiteBlack(STIMG, white, black)), 0, 0, BGIMG.getWidth(null), BGIMG.getHeight(null), null);
            }


            try {
                if (P.getCharacter().exists()) {
                    Image Chara = CutTransparentBorders(P.getCharacter());
                    Image Dark = ImageIO.read(fillPNG(Chara, Color.black, 0));
                    Image Shade = clan != null ? ImageIO.read(fillPNG(Chara, clan.getColor(), 100)) :
                            ImageIO.read(fillPNG(Chara, P.getColor(), 100));

                    double Width = Chara.getWidth(null) *  (1200 / (double) Chara.getHeight(null));
                    g2d.drawImage(createRightFadeGradient(Shade, (int) (Width > 700 ? (Width - 500) / 2 : 0), (int) Width, 1200), (int) (500 - (Width / 2)) + 24, 24, (int) Width, 1200, null);
                    g2d.drawImage(createRightFadeGradient(Dark, (int) (Width > 700 ? (Width - 500) / 2 : 0), (int) Width, 1200), (int) (500 - (Width / 2)), 0, (int) Width, 1200, null);
                    g2d.drawImage(createRightFadeGradient(Chara, (int) (Width > 700 ? (Width - 500) / 2 : 0), (int) Width, 1200), (int) (500 - (Width / 2)), 0, (int) Width, 1200, null);
                }
            } catch (Exception ignored) {}


            try {
                if (clan != null) {
                    if (clan.getCaptain().getUser().equals(P.getUser())) {
                        g2d.drawImage(Captain, 0, 0, BGIMG.getWidth(null), BGIMG.getHeight(null), null);
                    }
                    if (clan.hasEmblem()) {
                        try (InputStream is = new ByteArrayInputStream(clan.getEmblem())) {
                            BufferedImage ClanIcon = ImageIO.read(is);

                            g2d.drawImage(Blurr, 1770 - 200, 560 - 200, 400, 400, null);
                            g2d.drawImage(ClanIcon, 1770 - 150, 560 - 150, 300, 300, null);
                            if (clan.getName().length() >= 16) {
                                new MyAttributedString(clan.getName(), TEXTCOLOR, 1770, 744, 52f)
                                        .withFont(Dotum, true).withOutline(OUTLINECOLOR, 12f, true).drawCentered(g2d);
                            } else {
                                new MyAttributedString(clan.getName(), TEXTCOLOR, 1770, 744, 58f)
                                        .withFont(Dotum, true).withOutline(OUTLINECOLOR, 12f, true).drawCentered(g2d);
                            }
                        }
                    }
                    new MyAttributedString("uniform no.", TEXTCOLOR, 870, 740, 56f)
                            .withFont(Dotum, true).withOutline(OUTLINECOLOR, 10f, true).drawFromFront(g2d);
                    new MyAttributedString(Number, TEXTCOLOR, 1230, 740, 164f)
                            .withFont(Dotum, true).withOutline(OUTLINECOLOR, 16f, true).drawFromFront(g2d);
                } else {
                    String flag = P.getNationality().getUnicode().toLowerCase().replace("u+", "").replace(" ", "-");
                    g2d.drawImage(Blurr, 1770 - 200, 560 - 200, 400, 400, null);
                    File FF = new File(MainDirectory + "/assets/flag/" + flag + ".png");
                    if (FF.exists()) {
                        g2d.drawImage(ImageIO.read(FF), 1770 - 150, 560 - 150, 300, 300, null);
                        new MyAttributedString(P.getNationality().getCountry() + " FC", TEXTCOLOR, 1770, 744, 52f)
                                .withFont(Dotum, true).withOutline(OUTLINECOLOR, 0.4f, true).drawCentered(g2d);
                    }
                }
            } catch (Exception ignored) {}

            AddPosition(g2d);

            AddBirthdayNationality(g2d);

            AddDescription(g2d);

            AddStars(g2d);

            AddName(g2d);

            // League
            try (InputStream IS = new ByteArrayInputStream(P.Totals().getLeague().getTier().getImage())) {
                g2d.drawImage(ImageIO.read(IS), 40, 40, 150, 150, null);
            } catch (Exception ignored) {}

            try {
                g2d.drawImage(Sponsor, 0, 0, BGIMG.getWidth(null), BGIMG.getHeight(null), null);
                Image ServLogo = ImageIO.read(URI.create(DiscordAccount.getGuildById(sponsor).getIconUrl()).toURL());
                g2d.drawImage(MakeRoundCorner(MakeOpacity(CutInsideImage(ServLogo, 254, 170), 0.75f), 45), 1814 - 127, 915, 254, 170, null);
            } catch (Exception ignored) {
                Image ServLogo = ImageIO.read(URI.create(DiscordAccount.getSelfUser().getEffectiveAvatarUrl()).toURL());
                g2d.drawImage(MakeRoundCorner(MakeOpacity(CutInsideImage(ServLogo, 254, 170), 0.75f), 45), 1814 - 127, 915, 254, 170, null);
            }


            if (P.Totals().getTotalMedals() > 0) {
                g2d.drawImage(ImageIO.read(Objects.requireNonNull(Main.class.getResource("/img/Level.png"))), 1050, 950, 88, 88, null);
                new MyAttributedString("LVL.", Color.WHITE, 1150, 1020, 40f)
                        .withFont(Dotum, true).withOutline(OUTLINECOLOR, 4f, true).drawFromFront(g2d);
                new MyAttributedString(P.Totals().Level + "", TEXTCOLOR, 1230, 1020, 55f)
                        .withFont(Dotum, true).withOutline(OUTLINECOLOR, 4f, true).drawFromFront(g2d);

                g2d.drawImage(ImageIO.read(Objects.requireNonNull(Main.class.getResource("/img/tiny_medal2.png"))), 1054, 1040, 80, 80, null);
                new MyAttributedString("MEDALS.", Color.WHITE, 1150, 1100, 40f)
                        .withFont(Dotum, true).withOutline(OUTLINECOLOR, 4f, true).drawFromFront(g2d);
                new MyAttributedString(P.Totals().getTotalMedals() + "", TEXTCOLOR, 1340, 1100, 55f)
                        .withFont(Dotum, true).withOutline(OUTLINECOLOR, 4f, true).drawFromFront(g2d);
            }

            g2d.dispose();
            PNG = roundCorners(PNG, 150);
        } catch (Exception ignored) {}
        return this;
    }
    public synchronized CardImageBuilder GenerateCardGIF(int timeBetweenFrame, double percentage) {
        if (PNG == null) GenerateCardPNG();
        GIFFrameTime = timeBetweenFrame;
        Point2D.Float glintPos = new Point2D.Float(0, 0);
        BufferedImage IMG = ResizeImage(PNG, 0.5);
        int speedW = (int) Math.round(((double) IMG.getWidth() / 100) * percentage);
        int speedH = (int) Math.round(((double) IMG.getHeight() / 100) * percentage);
        java.util.List<BufferedImage> frames = new ArrayList<>();
        while (glintPos.y < IMG.getHeight() * 0.20) {
            frames.add(applyGlint(IMG, (int) glintPos.x, (int) glintPos.y, Color.LIGHT_GRAY));
            if (glintPos.x < IMG.getWidth()) glintPos.x += speedW;
            if (glintPos.y < IMG.getHeight()) glintPos.y += speedH;
        }
        GIF = frames;
        return this;
    }


    private void AddPosition(Graphics2D g2d) {
        try {
            Image Position = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/P_FW.png"))).getImage();
            if (P.Totals().getPosition().equals("FW")) {
                Position = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/P_FW.png"))).getImage();
            } else if (P.Totals().getPosition().equals("MF")) {
                Position = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/P_MF.png"))).getImage();
            } else if (P.Totals().getPosition().equals("DF")) {
                Position = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/P_DF.png"))).getImage();
            } else if (P.Totals().getPosition().equals("GK")) {
                Position = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/P_GK.png"))).getImage();
            }
            g2d.drawImage(Position, 0, 0, BGIMG.getWidth(null), BGIMG.getHeight(null), null);
        } catch (Exception ignored) {}
    }
    private void AddBirthdayNationality(Graphics2D g2d) {
        try {
            new MyAttributedString("date of birth", TEXTCOLOR, 870, 545, 52f)
                    .withFont(Dotum, true).withOutline(OUTLINECOLOR, 10f, true).drawFromFront(g2d);
            new MyAttributedString(P.getBirthday() != null ? EpochSecondToPattern(P.getBirthday().getEpochSecond(), "dd/MM/yyyy") : "--/--/----", TEXTCOLOR, 1250, 545, 52f)
                    .withFont(Dotum, true).withOutline(OUTLINECOLOR, 10f, true).drawFromFront(g2d);
        } catch (Exception ignored) {}
        try {
            new MyAttributedString("nationality", TEXTCOLOR, 870, 600, 52f)
                    .withFont(Dotum, true).withOutline(OUTLINECOLOR, 10f, true).drawFromFront(g2d);
            new MyAttributedString(P.getNationality().toString(), TEXTCOLOR, 1320, 600, 52f)
                    .withFont(Dotum, true).withOutline(OUTLINECOLOR, 10f, true).drawFromFront(g2d);

            new MyAttributedString("______________________", TEXTCOLOR, 1150, 755, 68f)
                    .withFont(Dotum, true).withOutline(OUTLINECOLOR, 10f, true).drawCentered(g2d);

            String flag = P.getNationality().getUnicode().toLowerCase().replace("u+", "").replace(" ", "-");
            File FF = new File(MainDirectory + "/assets/flag/" + flag + ".png");
            if (FF.exists()) g2d.drawImage(ImageIO.read(FF), 1255, 554, 60, 60, null);
        } catch (Exception ignored) {}
    }
    private void AddDescription(Graphics2D g2d) {
        try {
            String desc = EmojiParser.removeAllEmojis(P.getSignature());
            desc = stringLineChangerByLength(desc, 40);
            if (desc.contains("\n")) {
                if (countWord(desc, "\n") >= 2) {
                    new MyAttributedString(desc.split("\n")[0], TEXTCOLOR, 800, 800, 32f)
                            .withFont(Dotum, true).withOutline(OUTLINECOLOR, 8f, true).drawFromFront(g2d);
                    new MyAttributedString(desc.split("\n")[1], TEXTCOLOR, 800, 834, 32f)
                            .withFont(Dotum, true).withOutline(OUTLINECOLOR, 8f, true).drawFromFront(g2d);

                    if (countWord(desc, "\n") > 2) {
                        new MyAttributedString(desc.split("\n")[2] + "...", TEXTCOLOR, 800, 868, 32f)
                                .withFont(Dotum, true).withOutline(OUTLINECOLOR, 8f, true).drawFromFront(g2d);
                    } else {
                        new MyAttributedString(desc.split("\n")[2], TEXTCOLOR, 800, 868, 32f)
                                .withFont(Dotum, true).withOutline(OUTLINECOLOR, 8f, true).drawFromFront(g2d);
                    }
                } else if (countWord(desc, "\n") == 1) {
                    new MyAttributedString(desc.split("\n")[0], TEXTCOLOR, 800, 808, 36f)
                            .withFont(Dotum, true).withOutline(OUTLINECOLOR, 8f, true).drawFromFront(g2d);
                    new MyAttributedString(desc.split("\n")[1], TEXTCOLOR, 800, 848, 36f)
                            .withFont(Dotum, true).withOutline(OUTLINECOLOR, 8f, true).drawFromFront(g2d);
                }
            } else {
                if (desc.isEmpty()) desc = " ";
                new MyAttributedString(desc, TEXTCOLOR, 810, 832, 40f)
                        .withFont(Dotum, true).withOutline(OUTLINECOLOR, 9f, true).drawFromFront(g2d);
            }
        } catch (Exception ignored) {}
    }
    private void AddName(Graphics2D g2d) {
        try {
            String name = EmojiParser.removeAllEmojis(P.getUser().getEffectiveName()
                    .replaceAll("Đ", "D")
                    .replaceAll("ℝ", "R")
                    .replaceAll("ℂ", "C")
                    .replaceAll("Ƭ", "T"));
            if (name.contains(" ") && name.charAt(0) != ' ' && name.charAt(name.length() - 1) != ' ' && name.length() >= 12) {
                if (name.split(" ").length == 2) {
                    AddText(g2d, name.split(" ")[0], Color.white, Dotum, 880, 200, 128f, false, false, true, 10f, Color.black);
                    AddText(g2d, name.split(" ")[1], Color.white, Dotum, 880, 300, 128f, false, false, true, 10f, Color.black);
                } else {
                    AddText(g2d, name, Color.white, Dotum, 880, 260, 128f, false, false, true, 10f, Color.black);
                }
            } else if (name.contains("_") && name.charAt(0) != '_' && name.charAt(name.length() - 1) != '_' && name.length() > 12) {
                if (name.split("_").length == 2) {
                    AddText(g2d, name.split("_")[0], Color.white, Dotum, 880, 200, 128f, false, false, true, 10f, Color.black);
                    AddText(g2d, name.split("_")[1], Color.white, Dotum, 880, 300, 128f, false, false, true, 10f, Color.black);
                } else {
                    AddText(g2d, name, Color.white, Dotum, 880, 260, 128f, false, false, true, 10f, Color.black);
                }
            } else {
                if (name.length() >= 15) {
                    AddText(g2d, name, Color.white, Dotum, 880, 260, 108f, false, false, true, 8f, Color.black);
                } else {
                    AddText(g2d, name, Color.white, Dotum, 880, 260, 128f, false, false, true, 10f, Color.black);
                }
            }
            new MyAttributedString("@" + CharFix(P.getUser().getName()), TEXTCOLOR, 1180, 480, 70f)
                    .withFont(Dotum, true).withOutline(OUTLINECOLOR, 14f, true).drawCentered(g2d);
        } catch (Exception ignored) {}
    }
    private void AddStars(Graphics2D g2d) {
        try {
            int current = 0;
            int m1 = P.getItem("First Medal").Amount;
            int m2 = P.getItem("Second Medal").Amount;
            int m3 = P.getItem("Third Medal").Amount;
            if (m1 > 0) {
                Image Star = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/Star.png"))).getImage();
                for (int i = 0; i != m1; i++) {
                    g2d.drawImage(Star, 1750 - (current * 28), 70, 46, 46, null);
                    current++;
                }
            }
            if (m2 > 0) {
                Image Star2 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/Star2.png"))).getImage();
                for (int i = 0; i != m2; i++) {
                    g2d.drawImage(Star2, 1750 - (current * 28), 70, 46, 46, null);
                    current++;
                }
            }
            if (m3 > 0) {
                Image Star3 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/License/Star3.png"))).getImage();
                for (int i = 0; i != m3; i++) {
                    g2d.drawImage(Star3, 1750 - (current * 28), 70, 46, 46, null);
                    current++;
                }
            }
        } catch (Exception ignored) {}
    }


    private BufferedImage applyGlint(BufferedImage img, int x, int y, Color glintColor) {
        int width = img.getWidth();
        int height = img.getHeight();

        // Create a copy of the original image to apply the glint effect
        BufferedImage glintedImage = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = glintedImage.createGraphics();

        // Draw the original image first
        g2d.drawImage(img, 0, 0, null);

        // Set rendering hints for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a gradient for the glint effect
        GradientPaint glint = new GradientPaint(
                x, y, new Color(glintColor.getRed(), glintColor.getGreen(), glintColor.getBlue(), 1), // Transparent start
                x + Math.round(((double) img.getWidth() / 10)), y + Math.round(((double) img.getHeight() / 10)), new Color(glintColor.getRed(), glintColor.getGreen(), glintColor.getBlue(), 40), // Semi-transparent end
                true
        );

        // Apply the gradient as a glint over the image
        g2d.setPaint(glint);
        g2d.fillRect(0, 0, width, height);

        g2d.dispose();
        return glintedImage;
    }

    // Function to generate random slight movements (jitter effect)
    private static final Random random = new Random();
    private static int randomJitter(int base, int range) {
        return base + random.nextInt(range) - range / 2; // Slight random movement
    }

}
