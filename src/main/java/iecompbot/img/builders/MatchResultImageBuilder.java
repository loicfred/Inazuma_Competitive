package iecompbot.img.builders;

import com.vdurmont.emoji.EmojiParser;
import iecompbot.Main;
import iecompbot.objects.match.League;
import iecompbot.objects.match.MatchLog;
import iecompbot.objects.match.MatchLog_S;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.profile.item.Item;
import iecompbot.objects.profile.item.Scoreboard;
import iecompbot.objects.profile.profile_game.BasePG;
import iecompbot.objects.server.ServerInfo;
import iecompbot.objects.server.tournament.challonge.BaseCMatch;
import iecompbot.objects.server.tournament.challonge.BaseCTournament;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.text.AttributedString;
import java.util.Objects;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;
import static iecompbot.Main.DiscordAccount;
import static iecompbot.Main.newRodinPro;
import static iecompbot.img.ImgUtilities.*;
import static iecompbot.objects.UserAction.sendPrivateMessage;
import static my.utilities.util.Utilities.*;

public class MatchResultImageBuilder extends ImageBuilder {

    private final Profile P1;
    private final Profile P2;
    private final String name1;
    private final String name2;
    private final ServerInfo I;
    private final MatchLog R;
    private final MatchLog_S R2;

    private BaseCTournament<?,?,?> T = null;
    private Image BoardImage = null;
    private Scoreboard CurrentScoreboard = null;

    public boolean isPreview = false;
    public Item CustomBoardItem = null;

    private final Image ImgBlur = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/effect_blur.png"))).getImage();
    private final Image ImgArrowUp = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/up_arrow.png"))).getImage();
    private final Image ImgArrowDown = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/down_arrow.png"))).getImage();
    private final Image ImgUltraBlur = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/effect_ultra_blur.png"))).getImage();
    private final Image ImgAnime = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/Spike.png"))).getImage();
    private final Image ImgDarkener = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/Black50.png"))).getImage();

    private final Image ImgXP = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/XP.png"))).getImage();
    private final Image ImgInaCoin = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/InaCoin.png"))).getImage();
    private final Image ImgRainbowCoin = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/RainbowCoin.png"))).getImage();
    private final Image ImgMiniMedal = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/tiny_medal2.png"))).getImage();

    public MatchResultImageBuilder(User user1, User user2, MatchLog Log, MatchLog_S Log2, ServerInfo I) {
        this.R = Log;
        this.R2 = Log2;
        this.I = I;
        this.P1 = Profile.get(user1);
        this.P2 = Profile.get(user2);
        this.name1 = StopString(CharFix(EmojiParser.removeAllEmojis(I != null && I.getGuild().getMemberById(P1.getID()) != null ? I.getGuild().getMemberById(P1.getID()).getEffectiveName() : user1.getEffectiveName())), 18);
        this.name2 = StopString(CharFix(EmojiParser.removeAllEmojis(I != null && I.getGuild().getMemberById(P2.getID()) != null ? I.getGuild().getMemberById(P2.getID()).getEffectiveName() : user2.getEffectiveName())), 18);
    }

    private Image getBoardToUse(){
        if (CustomBoardItem != null && CustomBoardItem.getType().equals(Item.ItemType.COSMETICS_BOARD)) {
            CurrentScoreboard = new Scoreboard(CustomBoardItem);
            return CurrentScoreboard.WinBoard;
        }
        if (R.getP1Score() > R.getP2Score()) {
            if (P1.getScoreboard().WinBoard != null) {
                CurrentScoreboard = P1.getScoreboard();
                return CurrentScoreboard.WinBoard;
            } else if (P2.getScoreboard().LoseBoard != null) {
                CurrentScoreboard = P2.getScoreboard();
                return CurrentScoreboard.LoseBoard;
            }
        }
        else if (R.getP2Score() > R.getP1Score()) {
            if (P2.getScoreboard().WinBoard != null) {
                CurrentScoreboard = P2.getScoreboard();
                return CurrentScoreboard.WinBoard;
            } else if (P1.getScoreboard().LoseBoard != null) {
                CurrentScoreboard = P1.getScoreboard();
                return CurrentScoreboard.LoseBoard;
            }
        }
        else {
            if (GenerateRandomNumber(1,2) == 1) {
                if (P1.getScoreboard().TieBoard != null) {
                    CurrentScoreboard = P1.getScoreboard();
                    return CurrentScoreboard.TieBoard;
                } else if (P2.getScoreboard().TieBoard != null) {
                    CurrentScoreboard = P2.getScoreboard();
                    return CurrentScoreboard.TieBoard;
                }
            } else {
                if (P2.getScoreboard().TieBoard != null) {
                    CurrentScoreboard = P2.getScoreboard();
                    return CurrentScoreboard.TieBoard;
                }  else if (P1.getScoreboard().TieBoard != null) {
                    CurrentScoreboard = P1.getScoreboard();
                    return CurrentScoreboard.TieBoard;
                }
            }
        }

        if (R.getGame().isVR()) {
            CurrentScoreboard = new Scoreboard(Item.get("VR Scoreboard"));
        } else if (R.getGame().getCode().equals("IEGOCS")) {
            CurrentScoreboard = new Scoreboard(Item.get("CS Scoreboard"));
        } else if (R.getGame().getCode().equals("IEGO1")) {
            CurrentScoreboard = new Scoreboard(Item.get("GO1 Scoreboard"));
        } else if (R.getGame().isStrikers()) {
            CurrentScoreboard = new Scoreboard(Item.get("Strikers Scoreboard"));
        } else {
            CurrentScoreboard = new Scoreboard(Item.get("Galaxy Scoreboard"));
        }
        return CurrentScoreboard.WinBoard;
    }

    public void addChallonge(BaseCTournament<?,?,?> T) {
        this.T = T;
    }

    public synchronized MatchResultImageBuilder GenerateMatchResultPNG() {
        try {
            PNG = new BufferedImage(1800, 1080, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = PNG.createGraphics();

            BoardImage = getBoardToUse();
            setGameBoard(g2d);
            SetNamesAndScore(g2d);
            SetAvatar(g2d);

            if (!isPreview) {
                if (T != null) {
                    BaseCMatch<?,?,?> M = (T.getVSAmount() == 1 ? T.getParticipantById(P1.getID()).getMatchWithOpponent(P2.getID(), false) : T.getMatches().stream().filter(MM -> MM.getSubMatch(P1.getID(), P2.getID()) != null).findAny().orElse(null));
                    if (M != null) {
                        BufferedImage Icon = CircleAnImage(ImageIO.read(URI.create(I != null ? I.getGuild().getIconUrl().replace(".gif", ".png") : DiscordAccount.getSelfUser().getEffectiveAvatarUrl()).toURL()));
                        String TName = CharFix(EmojiParser.removeAllEmojis(T.getName()));
                        int isStrikers = 0;
                        int isStrikers2 = 0;
                        if (R.getGame().isStrikers()) {
                            isStrikers = 10;
                            isStrikers2 = 2;
                        } else if (R.getGame().getCode().equals("IEGOCS")) {
                            isStrikers = -10;
                        }
                        double maxlength;

                        AttributedString Name = new AttributedString(TName);
                        if (TName.length() > 62) {
                            Name.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(30f + isStrikers2));
                        } else if (TName.length() > 54) {
                            Name.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(34f + isStrikers2));
                        } else if (TName.length() > 48) {
                            Name.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(38f + isStrikers2));
                        } else if (TName.length() > 40) {
                            Name.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(42f + isStrikers2));
                        } else if (TName.length() > 34) {
                            Name.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(50f + isStrikers2));
                        } else if (TName.length() > 28) {
                            Name.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(56f + isStrikers2));
                        } else {
                            Name.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(60f + isStrikers2));
                        }
                        TextLayout textlayout = new TextLayout(Name.getIterator(), g2d.getFontRenderContext());


                        AttributedString Description = new AttributedString((M.getGroupID() != 0 ? T.getGroupName(M.getGroupID()) + " - " : "") + M.getRoundLong() + " - " + T.getType().name());
                        Description.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(35f + isStrikers2));
                        TextLayout textlayout2 = new TextLayout(Description.getIterator(), g2d.getFontRenderContext());


                        maxlength = 200 + Math.max(getWidthOfAttributedString(g2d, Name), getWidthOfAttributedString(g2d, Description)) + isStrikers;
                        {
                            g2d.drawImage(ImgBlur, (int) (870 - (maxlength / 2)), 850 - isStrikers, 240 + isStrikers, 240 + isStrikers, null);
                            g2d.drawImage(Icon, (int) (900 - (maxlength / 2)), 880 - isStrikers, 180 + isStrikers, 180 + isStrikers, null);

                            Shape shape = textlayout.getOutline(AffineTransform.getTranslateInstance(1100 + isStrikers - (maxlength / 2), 960 - isStrikers));

                            g2d.setColor(Color.black);
                            g2d.setStroke(new BasicStroke(6f + isStrikers2));
                            g2d.draw(shape);
                            g2d.setColor(Color.white);
                            g2d.fill(shape);
                        }
                        {
                            Shape shape = textlayout2.getOutline(AffineTransform.getTranslateInstance(1100 + isStrikers - (maxlength / 2), 1020 - isStrikers));
                            g2d.setColor(Color.black);
                            g2d.setStroke(new BasicStroke(3f + isStrikers2));
                            g2d.draw(shape);
                            g2d.setColor(Color.white);
                            g2d.fill(shape);
                        }
                    } else showRewards(g2d);
                } else showRewards(g2d);
            }
            else {
                AttributedString as1 = new AttributedString("This is a preview.");
                as1.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(80f));
                g2d.setColor(Color.white);
                g2d.drawString(as1.getIterator(), (int) (900 - (getWidthOfAttributedString(g2d, as1) / 2)), 960);
            }
            g2d.dispose();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return this;
    }

    private void showRewards(Graphics2D g2d) {
        SetP1Rewards(g2d);
        SetP2Rewards(g2d);
        try {
            if (I != null && I.Ranking().hasPrivateRanking() && I.getGuild().getIconUrl() != null) {
                g2d.drawImage(ImgBlur, PNG.getWidth() - (PNG.getWidth()/2) - 80, 894, 160, 160, null);
                BufferedImage Icon = CircleAnImage(ImageIO.read(URI.create(I.getGuild().getIconUrl().replace(".gif", ".png")).toURL()));
                g2d.drawImage(Icon, PNG.getWidth() - (PNG.getWidth()/2) - 70, 904, 140, 140, null);
            }
        } catch (Exception ignored) {}
    }

    private void setGameBoard(Graphics2D g2d) {
        g2d.drawImage(BoardImage, 0, 0, 1800, 1080, null);
        if (CurrentScoreboard.Name.equals("Strikers Scoreboard")) {
            g2d.drawImage(ImgAnime, 0, 0, 1800, 1080, null);
            g2d.drawImage(ImgDarkener, 0, 0, 1800, 1080, null);
            g2d.drawImage(ImgAnime, -40, -60, 1880, 1200, null);
            Image ImgStrlgtng = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/strikers/strikerslightningvs.png"))).getImage();
            g2d.drawImage(ImgStrlgtng, 900 - ImgStrlgtng.getWidth(null), -30, ImgStrlgtng.getWidth(null) * 2, ImgStrlgtng.getHeight(null) * 2, null);
        } else if (CurrentScoreboard.Name.equals("VR Scoreboard")) {
            Image ImgVRScoreground = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/VRScoreTop.png"))).getImage();
            g2d.drawImage(ImgAnime, -80, -120, 1960, 1320, null);
            g2d.drawImage(ImgVRScoreground, 0, 0, 1800, 600, null);
            Image Bar = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/VRBar.png"))).getImage();
            g2d.drawImage(Bar, 900 - Bar.getWidth(null), 280, Bar.getWidth(null) * 2, Bar.getHeight(null) * 2, null);
            Image VS = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/static/img/match/VS3.png"))).getImage();
            g2d.drawImage(VS, 900 - (VS.getWidth(null) / 2), 740, VS.getWidth(null), VS.getHeight(null), null);
        }
    }
    private void SetNamesAndScore(Graphics2D g2d) {
        try {
            if (CurrentScoreboard != null && CurrentScoreboard.Name.equals("Kappa Dance")) {
                AttributedString as1 = new AttributedString(name1);
                as1.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(name1.length() <= 16 ? 40f : 36f));
                g2d.drawString(as1.getIterator(), (int) (700 - (getWidthOfAttributedString(g2d, as1) / 2)), 260);

                AttributedString as2 = new AttributedString(name2);
                as2.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(name2.length() <= 16 ? 40f : 36f));
                g2d.drawString(as2.getIterator(), (int) (1800 - 690 - (getWidthOfAttributedString(g2d, as2) / 2)), 260);

                BufferedImage number1 = CreateNumber(R.getP1Score());
                g2d.drawImage(number1, 700 - (number1.getWidth() * 3 / 3), 400, number1.getWidth() * 3, number1.getHeight() * 3, null);
                BufferedImage number2 = CreateNumber(R.getP2Score());
                g2d.drawImage(number2, (1800 - 700) - (number2.getWidth() * 3 / 3), 400, number2.getWidth() * 3, number2.getHeight() * 3, null);
            } else {
                AttributedString as1 = new AttributedString(name1);
                if (R.getGame().isStrikers()) {
                    as1.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(50f));
                    g2d.drawString(as1.getIterator(), (int) (410 - (getWidthOfAttributedString(g2d, as1) / 2)), 770);
                } else if (R.getGame().getCode().equals("IEGO1")) {
                    as1.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(60f));
                    g2d.drawString(as1.getIterator(), (int) (410 - (getWidthOfAttributedString(g2d, as1) / 2)), 750);
                } else if (R.getGame().isVR()) {
                    as1.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(name1.length() <= 16 ? 64f : 60f));
                    g2d.drawString(as1.getIterator(), (int) (410 - (getWidthOfAttributedString(g2d, as1) / 2)), 820);
                } else {
                    as1.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(name1.length() <= 16 ? 64f : 60f));
                    g2d.drawString(as1.getIterator(), (int) (410 - (getWidthOfAttributedString(g2d, as1) / 2)), 820);
                }

                AttributedString as2 = new AttributedString(name2);
                if (R.getGame().isStrikers()) {
                    as2.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(50f));
                    g2d.drawString(as2.getIterator(), (int) (1800 - 410 - (getWidthOfAttributedString(g2d, as2) / 2)), 770);
                } else if (R.getGame().getCode().equals("IEGO1")) {
                    as2.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(60f));
                    g2d.drawString(as2.getIterator(), (int) (1800 - 410 - (getWidthOfAttributedString(g2d, as2) / 2)), 750);
                } else if (R.getGame().isVR()) {
                    as2.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(name2.length() <= 16 ? 64f : 60f));
                    g2d.drawString(as2.getIterator(), (int) (1800 - 410 - (getWidthOfAttributedString(g2d, as2) / 2)), 820);
                } else {
                    as2.addAttribute(TextAttribute.FONT, newRodinPro.deriveFont(name2.length() <= 16 ? 64f : 60f));
                    g2d.drawString(as2.getIterator(), (int) (1800 - 410 - (getWidthOfAttributedString(g2d, as2) / 2)), 820);
                }


                if (R.getGame().isStrikers()) {
                    BufferedImage number2 = CreateNumberStr(R.getP1Score());
                    g2d.drawImage(number2, 480 - number2.getWidth(), 440, number2.getWidth() * 2, number2.getHeight() * 2, null);
                } else if (R.getGame().isVR()) {
                    BufferedImage number1 = CreateNumberVR(R.getP1Score());
                    g2d.drawImage(number1, 600 - (int) (number1.getWidth() * 0.75), 200, (int) (number1.getWidth() * 1.5), (int) (number1.getHeight() * 1.5), null);
                } else if (R.getGame().getCode().equals("IEGO1")) {
                    BufferedImage number1 = CreateNumber(R.getP1Score());
                    g2d.drawImage(number1, 600 - number1.getWidth() * 2, 370, number1.getWidth() * 4, number1.getHeight() * 4, null);
                } else {
                    BufferedImage number1 = CreateNumber(R.getP1Score());
                    g2d.drawImage(number1, 600 - number1.getWidth() * 2, 350, number1.getWidth() * 4, number1.getHeight() * 4, null);
                }

                if (R.getGame().isStrikers()) {
                    BufferedImage number2 = CreateNumberStr(R.getP2Score());
                    g2d.drawImage(number2, (1800 - 480) - number2.getWidth(), 440, number2.getWidth() * 2, number2.getHeight() * 2, null);
                } else if (R.getGame().isVR()) {
                    BufferedImage number2 = CreateNumberVR(R.getP2Score());
                    g2d.drawImage(number2, (1800 - 600) - (int) (number2.getWidth() * 0.75), 200, (int) (number2.getWidth() * 1.5), (int) (number2.getHeight() * 1.5), null);
                } else if (R.getGame().getCode().equals("IEGO1")) {
                    BufferedImage number2 = CreateNumber(R.getP2Score());
                    g2d.drawImage(number2, (1800 - 600) - number2.getWidth() * 2, 370, number2.getWidth() * 4, number2.getHeight() * 4, null);
                } else {
                    BufferedImage number2 = CreateNumber(R.getP2Score());
                    g2d.drawImage(number2, (1800 - 600) - number2.getWidth() * 2, 350, number2.getWidth() * 4, number2.getHeight() * 4, null);
                }
            }
        } catch (Exception ignored) {}
    }
    private void SetP1Rewards(Graphics2D g2d) {
        int position = R.getP1XP() > 99 || R.getP1Coins() > 99 ? 290 : 250;
        try {
            // Drawing the P1 XP
            g2d.drawImage(ImgXP, 20, 900, 48, 48, null);
            BufferedImage XP = CreateNumberMini(R.getP1XP(), UColor.BLUE, USign.ADD);
            g2d.drawImage(XP, 100, 900, XP.getWidth() * 4, XP.getHeight() * 4, null);

            BufferedImage MXP = CreateNumberMiniScript(P1.Totals().CurrentXP + "[B] To " + (P1.Totals().CurrentXP+R.getP1XP()) + "[B] On " + P1.Totals().XPForNextLevel + "[B]");
            g2d.drawImage(MXP, position, 900, MXP.getWidth() * 4, MXP.getHeight() * 4, null);

        } catch (Exception ignored) {}
        try {

            BasePG<?> P1G = I != null && I.Ranking().hasPrivateRanking() ? R2.getPGP1() : R.getPGP1();
            g2d.drawImage(ImgMiniMedal, 20, 960, 48, 48, null);
            int Medal = I != null && I.Ranking().hasPrivateRanking() ? R2.getP1Medals() : R.getP1Medals();
            if (Medal > 0) {
                BufferedImage Medals = CreateNumberMini(Medal, UColor.YELLOW, USign.ADD);
                g2d.drawImage(Medals, 100, 960, Medals.getWidth() * 4, Medals.getHeight() * 4, null);
                BufferedImage MChange = CreateNumberMiniScript(P1G.getMedals() + "[Y] To " + (P1G.getMedals() + Medal) + "[Y]");
                g2d.drawImage(MChange, position, 960, MChange.getWidth() * 4, MChange.getHeight() * 4, null);
            } else if (Medal < 0) {
                BufferedImage Medals = CreateNumberMini(Medal, UColor.PINK, USign.LESS);
                g2d.drawImage(Medals, 100, 960, Medals.getWidth() * 4, Medals.getHeight() * 4, null);
                BufferedImage MChange = CreateNumberMiniScript(P1G.getMedals() + "[R] To " + (P1G.getMedals() + Medal) + "[R]");
                g2d.drawImage(MChange, position, 960, MChange.getWidth() * 4, MChange.getHeight() * 4, null);
            } else {
                BufferedImage Medals = CreateNumberMini(Medal, UColor.WHITE, USign.NONE);
                g2d.drawImage(Medals, 100, 960, Medals.getWidth() * 4, Medals.getHeight() * 4, null);
                BufferedImage MChange = CreateNumberMiniScript(P1G.getMedals() + "[W] To " + (P1G.getMedals() + Medal) + "[W]");
                g2d.drawImage(MChange, position, 960, MChange.getWidth() * 4, MChange.getHeight() * 4, null);
            }
            League OldLeague = P1G.getLeague();
            League NewLeague = I != null && I.Ranking().hasPrivateRanking() ? I.Ranking().getLeagueByMedal(P1G.getMedals() + Medal) : League.getByMedal(P1G.getMedals() + Medal);
            Image ImgP1League = ImageIO.read(URI.create(NewLeague.getImageURL()).toURL());
            if (NewLeague.getTier().getStart() > OldLeague.getTier().getStart()) {
                g2d.drawImage(ImgBlur, 164 - 20, 82 - 76, 152, 152, null);
                g2d.drawImage(ImgArrowUp, 164, 82 - 56, 112, 112, null);
                g2d.drawImage(ImgUltraBlur, -30, -30, 224, 224, null);
                if (OldLeague.getTier() != NewLeague.getTier()) {
                    P1.resetClanCards();
                    if (I != null) I.Channels().getNotificationChannel().sendMessage(TLG(I, "result-confirm-league-advance", P1.getUser().getAsMention(), "**" + OldLeague.getTier().getTierEmojiFormatted() + " " + OldLeague.getTier().getName() + " " + TLG(I, "League") + "**", "**" + NewLeague.getTier().getTierEmojiFormatted() + " " + NewLeague.getTier().getName() + " " + TLG(I, "League") + "**")).queue();
                    sendPrivateMessage(P1.getUser(), P1G.getGame().getEmojiFormatted() + " **" + P1G.getGameCode() + " :**" + TL(P1,"result-confirm-league-advance", P1.getUser().getAsMention(), "**" + OldLeague.getTier().getTierEmojiFormatted() + " " + OldLeague.getTier().getName() + " " + TL(P1, "League") + "**", "**" + NewLeague.getTier().getTierEmojiFormatted() + " " + NewLeague.getTier().getName() + " " + TL(P1, "League") + "**"));
                }
            } else if (OldLeague.getTier().getStart() > NewLeague.getTier().getStart()) {
                g2d.drawImage(ImgBlur, 164 - 20, 82 - 76, 152, 152, null);
                g2d.drawImage(ImgArrowDown, 164, 82 - 56, 112, 112, null);
                g2d.drawImage(ImgUltraBlur, -30, -30, 224, 224, null);
                if (OldLeague.getTier() != NewLeague.getTier()) {
                    P1.resetClanCards();
                    if (I != null) I.Channels().getNotificationChannel().sendMessage(TLG(I, "result-confirm-league-down", P1.getUser().getAsMention(), "**" + OldLeague.getTier().getTierEmojiFormatted() + " " + OldLeague.getTier().getName() + " " + TLG(I, "League") + "**", "**" + NewLeague.getTier().getTierEmojiFormatted() + " " + NewLeague.getTier().getName() + " " + TLG(I, "League") + "**")).queue();
                    sendPrivateMessage(P1.getUser(), P1G.getGame().getEmojiFormatted() + " **" + P1G.getGameCode() + " :**" + TL(P1,"result-confirm-league-down", P1.getUser().getAsMention(), "**" + OldLeague.getTier().getTierEmojiFormatted() + " " + OldLeague.getTier().getName() + " " + TL(P1, "League") + "**", "**" + NewLeague.getTier().getTierEmojiFormatted() + " " + NewLeague.getTier().getName() + " " + TL(P1, "League") + "**"));
                }
            }
            g2d.drawImage(ImgP1League, 0, 0, 164, 164, null);

        } catch (Exception ignored) {}
        try {
            // Drawing the P1 Coins
            g2d.drawImage(ImgInaCoin, 20, 1020, 48, 48, null);
            BufferedImage Coins = CreateNumberMini(R.getP1Coins(), UColor.YELLOW, USign.ADD);
            g2d.drawImage(Coins, 100, 1020, Coins.getWidth() * 4, Coins.getHeight() * 4, null);

            // Drawing the P1 Rainbow
            g2d.drawImage(ImgRainbowCoin, position, 1020, 48, 48, null);
            BufferedImage Coins2 = CreateNumberMini(R.getP1RainbowCoins(), UColor.PURPLE, USign.ADD);
            g2d.drawImage(Coins2, position + 80, 1020, Coins2.getWidth() * 4, Coins2.getHeight() * 4, null);
        } catch (Exception ignored) {}
    }
    private void SetP2Rewards(Graphics2D g2d) {
        int position = R.getP2XP() > 99 || R.getP2Coins() > 99 ? 290 : 250;
        try {
            // Drawing the P2 XP
            g2d.drawImage(ImgXP, 1800 - 20 - 48, 900, 48, 48, null);
            BufferedImage XP = CreateNumberMini(R.getP2XP(), UColor.BLUE, USign.ADD);
            g2d.drawImage(XP, 1800 - 100 - (XP.getWidth() * 4), 900, XP.getWidth() * 4, XP.getHeight() * 4, null);

            BufferedImage MXP = CreateNumberMiniScript(P2.Totals().CurrentXP + "[B] To " + (P2.Totals().CurrentXP + R.getP2XP()) + "[B] On " + P2.Totals().XPForNextLevel + "[B]");
            g2d.drawImage(MXP, 1800 - position - (MXP.getWidth() * 4), 900, MXP.getWidth() * 4, MXP.getHeight() * 4, null);

        } catch (Exception ignored) {}
        try {

            // Drawing the P2 Medals
            BasePG<?> P2G = I != null && I.Ranking().hasPrivateRanking() ? R2.getPGP2() : R.getPGP2();
            g2d.drawImage(ImgMiniMedal, 1800 - 20 - 48, 960, 48, 48, null);
            int Medal = I != null && I.Ranking().hasPrivateRanking() ? R2.getP2Medals() : R.getP2Medals();
            if (Medal > 0) {
                BufferedImage Medals = CreateNumberMini(Medal, UColor.YELLOW, USign.ADD);
                g2d.drawImage(Medals, 1800 - 100 - (Medals.getWidth() * 4), 960, Medals.getWidth() * 4, Medals.getHeight() * 4, null);
                BufferedImage MChange = CreateNumberMiniScript(P2G.getMedals() + "[Y] To " + (P2G.getMedals()+Medal) + "[Y]");
                g2d.drawImage(MChange, 1800 - position - (MChange.getWidth() * 4), 960, MChange.getWidth() * 4, MChange.getHeight() * 4, null);
            } else if (Medal < 0) {
                BufferedImage Medals = CreateNumberMini(Medal, UColor.PINK, USign.LESS);
                g2d.drawImage(Medals, 1800 - 100 - (Medals.getWidth() * 4), 960, Medals.getWidth() * 4, Medals.getHeight() * 4, null);
                BufferedImage MChange = CreateNumberMiniScript(P2G.getMedals() + "[R] To " + (P2G.getMedals()+Medal) + "[R]");
                g2d.drawImage(MChange, 1800 - position - (MChange.getWidth() * 4), 960, MChange.getWidth() * 4, MChange.getHeight() * 4, null);
            } else {
                BufferedImage Medals = CreateNumberMini(Medal, UColor.WHITE, USign.NONE);
                g2d.drawImage(Medals, 1800 - 100 - (Medals.getWidth() * 4), 960, Medals.getWidth() * 4, Medals.getHeight() * 4, null);
                BufferedImage MChange = CreateNumberMiniScript(P2G.getMedals() + "[W] To " + (P2G.getMedals()+Medal) + "[W]");
                g2d.drawImage(MChange, 1800 - position - (MChange.getWidth() * 4), 960, MChange.getWidth() * 4, MChange.getHeight() * 4, null);
            }

            League OldLeague = P2G.getLeague();
            League NewLeague = I != null && I.Ranking().hasPrivateRanking() ? I.Ranking().getLeagueByMedal(P2G.getMedals() + Medal) : League.getByMedal(P2G.getMedals() + Medal);
            Image ImgP2League = ImageIO.read(URI.create(NewLeague.getImageURL()).toURL());
            if (NewLeague.getTier().getStart() > OldLeague.getTier().getStart()) {
                g2d.drawImage(ImgBlur, 1800 - 164 - 132, 82 - 76, 152, 152, null);
                g2d.drawImage(ImgArrowUp,1800 - 164 - 112, 82 - 56, 112, 112, null);
                g2d.drawImage(ImgUltraBlur, 1800 - 194,-30, 224 ,224, null);
                if (OldLeague.getTier() != NewLeague.getTier()) {
                    P2.resetClanCards();
                    if (I != null) I.Channels().getNotificationChannel().sendMessage(TLG(I, "result-confirm-league-advance", P2.getUser().getAsMention(), "**" + OldLeague.getTier().getTierEmojiFormatted() + " " + OldLeague.getTier().getName() + " " + TLG(I, "League") + "**", "**" + NewLeague.getTier().getTierEmojiFormatted() + " " + NewLeague.getTier().getName() + " " + TLG(I, "League") + "**")).queue();
                    sendPrivateMessage(P2.getUser(), P2G.getGame().getEmojiFormatted() + " **" + P2G.getGameCode() + " :**" + TL(P2, "result-confirm-league-advance", P2.getUser().getAsMention(), "**" + OldLeague.getTier().getTierEmojiFormatted() + " " + OldLeague.getTier().getName() + " " + TL(P2, "League") + "**", "**" + NewLeague.getTier().getTierEmojiFormatted() + " " + NewLeague.getTier().getName() + " " + TL(P2, "League") + "**"));
                }
            } else if (OldLeague.getTier().getStart() > NewLeague.getTier().getStart()) {
                g2d.drawImage(ImgBlur, 1800 - 164 - 132, 82 - 76, 152, 152, null);
                g2d.drawImage(ImgArrowDown,1800 - 164 - 112, 82 - 56, 112, 112, null);
                g2d.drawImage(ImgUltraBlur, 1800 - 194,-30, 224 ,224, null);
                if (OldLeague.getTier() != NewLeague.getTier()) {
                    P2.resetClanCards();
                    if (I != null) I.Channels().getNotificationChannel().sendMessage(TLG(I, "result-confirm-league-down", P2.getUser().getAsMention(), "**" + OldLeague.getTier().getTierEmojiFormatted() + " " + OldLeague.getTier().getName() + " " + TLG(I, "League") + "**", "**" + NewLeague.getTier().getTierEmojiFormatted() + " " + NewLeague.getTier().getName() + " " + TLG(I, "League") + "**")).queue();
                    sendPrivateMessage(P2.getUser(), P2G.getGame().getEmojiFormatted() + " **" + P2G.getGameCode() + " :**" + TL(P2, "result-confirm-league-down", P2.getUser().getAsMention(), "**" + OldLeague.getTier().getTierEmojiFormatted() + " " + OldLeague.getTier().getName() + " " + TL(P2, "League") + "**", "**" + NewLeague.getTier().getTierEmojiFormatted() + " " + NewLeague.getTier().getName() + " " + TL(P2, "League") + "**"));
                }
            }
            g2d.drawImage(ImgP2League,1800 - 164, 0, 164, 164, null);

        } catch (Exception ignored) {}
        try {

            // Drawing the P2 Coins
            g2d.drawImage(ImgInaCoin, 1800 - 20 - 48, 1020, 48, 48, null);
            BufferedImage Coins = CreateNumberMini(R.getP2Coins(), UColor.YELLOW, USign.ADD);
            g2d.drawImage(Coins, 1800 - 100 - (Coins.getWidth() * 4), 1020, Coins.getWidth() * 4, Coins.getHeight() * 4, null);

            // Drawing the P1 Rainbow
            g2d.drawImage(ImgRainbowCoin, 1800 - position - 48, 1020, 48, 48, null);
            BufferedImage Coins2 = CreateNumberMini(R.getP2RainbowCoins(), UColor.PURPLE, USign.ADD);
            g2d.drawImage(Coins2, 1800 - position - 100 - (Coins2.getWidth() * 4), 1020, Coins2.getWidth() * 4, Coins2.getHeight() * 4, null);
        } catch (Exception ignored) {}
    }

    private void SetAvatar(Graphics2D g2d) {
        try {
            if (CurrentScoreboard.Name.equals("Strikers Scoreboard") || CurrentScoreboard.Name.equals("VR Scoreboard")) {
                drawAvatar(g2d, P1, 90, 90, 1);
                drawAvatar(g2d, P2, 1800 - 90 - 260, 90, 1);
            } else if (CurrentScoreboard.Name.equals("Kappa Dance")) {
                drawAvatar(g2d, P1, 375, 50, 0.6);
                drawAvatar(g2d, P2, (int) (1800 - 375 - (260 * 0.6)), 50, 0.6);
            } else {
                drawAvatar(g2d, P1, 110, 320, 1);
                drawAvatar(g2d, P2, 1800 - 110 - 260, 320, 1);
            }
        } catch (Exception ignored) { }
    }
    private void drawAvatar(Graphics2D g2d, Profile P, int X, int Y, double sizeMultiplier) {
        try {
            Image avatar = ImageIO.read(URI.create(P.getUser().getEffectiveAvatarUrl().replace(".gif", ".png")).toURL());
            g2d.drawImage(avatar, X, Y, (int) (260 * sizeMultiplier), (int) (260 * sizeMultiplier), null);

            Stroke stroke1 = new BasicStroke(8f);
            g2d.setColor(P.getColor());
            g2d.setStroke(stroke1);
            g2d.drawRect(X - (int) (4 * sizeMultiplier), Y - (int) (4 * sizeMultiplier), (int) (268 * sizeMultiplier), (int) (268 * sizeMultiplier));

            Item F = P.getCustomFrameItem();
            if (F != null) g2d.drawImage(ImageIO.read(URI.create(F.getImageURL()).toURL()), X - (int) (70 * sizeMultiplier), Y - (int) (70 * sizeMultiplier), (int) (400 * sizeMultiplier), (int) (400 * sizeMultiplier), null);
        } catch (Exception ignored) {}
    }
}
