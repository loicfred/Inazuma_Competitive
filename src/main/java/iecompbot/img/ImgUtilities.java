package iecompbot.img;

import iecompbot.Main;
import iecompbot.interaction.Automation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static iecompbot.Main.MainDirectory;
import static iecompbot.Main.TempDirectory;
import static iecompbot.Utility.takeOnlyInts;
import static iecompbot.Utility.takeOnlyNumberStr;
import static iecompbot.interaction.Automation.Wait;
import static my.utilities.util.Utilities.takeOnlyDigits;

public class ImgUtilities {


    public static BufferedImage roundCorners(BufferedImage img, int radius) {
        BufferedImage roundedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TRANSLUCENT);
        Graphics2D g2d = roundedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setClip(new RoundRectangle2D.Float(0, 0, img.getWidth(), img.getHeight(), radius, radius));
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return roundedImage;
    }
    public static double getWidthOfAttributedString(Graphics2D graphics2D, AttributedString attributedString) {
        AttributedCharacterIterator characterIterator = attributedString.getIterator();
        FontRenderContext fontRenderContext = graphics2D.getFontRenderContext();
        LineBreakMeasurer lbm = new LineBreakMeasurer(characterIterator, fontRenderContext);
        TextLayout textLayout = lbm.nextLayout(Integer.MAX_VALUE);
        return textLayout.getBounds().getWidth();
    }

    public static String mixColors(Color color1, Color color2) {
        return mixColors(getHexValue(color1), getHexValue(color2));
    }
    public static String mixColors(String color1, String color2) {
        // Parse color1 and color2 into RGB values
        color1 = color1.replaceAll("#", "");
        color2 = color2.replaceAll("#", "");
        int r1 = Integer.parseInt(color1.substring(0, 2), 16);
        int g1 = Integer.parseInt(color1.substring(2, 4), 16);
        int b1 = Integer.parseInt(color1.substring(4, 6), 16);

        int r2 = Integer.parseInt(color2.substring(0, 2), 16);
        int g2 = Integer.parseInt(color2.substring(2, 4), 16);
        int b2 = Integer.parseInt(color2.substring(4, 6), 16);

        // Mix the RGB values
        int mixedR = (r1 + r2) / 2;
        int mixedG = (g1 + g2) / 2;
        int mixedB = (b1 + b2) / 2;

        // Convert the mixed RGB values back to Hex
        String mixedColor = String.format("%02X%02X%02X", mixedR, mixedG, mixedB);

        return "#" + mixedColor;
    }

    public static InputStream smallImage(InputStream stream, double multiplier) {
        try {
            BufferedImage image = ImageIO.read(stream);

            BufferedImage newImg = new BufferedImage((int) (image.getWidth(null) * multiplier), (int) (image.getHeight(null) * multiplier), BufferedImage.TRANSLUCENT);
            Graphics2D g2d = newImg.createGraphics();
            g2d.drawImage(image, 0, 0, (int) (image.getWidth(null) * multiplier), (int) (image.getHeight(null) * multiplier), null);
            g2d.dispose();

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(newImg, "PNG", baos);
                try (InputStream inputStream = new ByteArrayInputStream(baos.toByteArray())) {
                    return inputStream;
                } catch (IOException e) {
                    Automation.handleException(e);
                }
            } catch (IOException e) {
                Automation.handleException(e);
            }
        } catch (IOException e) {
            Automation.handleException(e);
        }
        return stream;
    }

    public static File fillPNG(BufferedImage image, Color fillColor, int blackAmount) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();

            int red = fillColor.getRed();
            int green = fillColor.getGreen();
            int blue = fillColor.getBlue();

            // Subtract the black amount from each RGB component
            red = Math.max(0, red - blackAmount);
            green = Math.max(0, green - blackAmount);
            blue = Math.max(0, blue - blackAmount);

            // Create a new color with the adjusted RGB values
            fillColor = new Color(red, green, blue);


            // Iterate through each pixel and fill non-transparent pixels
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = image.getRGB(x, y);
                    int alpha = (pixel >> 24) & 0xFF; // Extract the alpha channel

                    double transparencyPercentage = (alpha / 255.0) * 100.0;

                    if (transparencyPercentage > 20) { // more than 20% transparent
                        // Set the pixel color to the desired fill color
                        image.setRGB(x, y, fillColor.getRGB());
                    }
                }
            }

            // Save the modified image to a new PNG file
            ImageIO.write(image, "PNG", new File(MainDirectory + "/temp/filltransvytvh.png"));
        } catch (IOException e) {
            Automation.handleException(e);
        }
        return new File(MainDirectory + "/temp/filltransvytvh.png");
    }
    public static File fillPNG(Image img, Color fillColor, int blackAmount) {
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        // Load the PNG image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(img, 0,0,null);
        g2d.dispose();
        return fillPNG(image, fillColor, blackAmount);
    }


    public static Image replaceColor(BufferedImage image, Color oldColor, Color newColor) {
        // Load the PNG image

        // Get the width and height of the image
        int width = image.getWidth();
        int height = image.getHeight();

        // Iterate through each pixel and fill non-transparent pixels
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = image.getRGB(x, y);

                if (pixel == oldColor.getRGB()) { // more than 20% transparent
                    // Set the pixel color to the desired fill color
                    image.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return image;
    }

    public static File fillPNGWhiteBlack(Image input, Color white, Color black) {
        try {
            // Load the PNG image
            BufferedImage image = new BufferedImage(input.getWidth(null), input.getHeight(null), BufferedImage.TRANSLUCENT);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(input, 0, 0, input.getWidth(null), input.getHeight(null), null);
            g2d.dispose();
            // Get the width and height of the image
            int width = image.getWidth();
            int height = image.getHeight();
            if (black.getRed() + black.getBlue() + black.getGreen() > 500) {
                black = new Color((int) Math.max(0, black.getRed() * 0.9 - 80), (int) Math.max(0, black.getGreen() * 0.9 - 80), (int) Math.max(0, black.getBlue() * 0.9 - 80));
            } else if (black.getRed() + black.getBlue() + black.getGreen() > 400) {
                black = new Color((int) Math.max(0, black.getRed() * 0.9 - 65), (int) Math.max(0, black.getGreen() * 0.9 - 65), (int) Math.max(0, black.getBlue() * 0.9 - 65));
            } else if (black.getRed() + black.getBlue() + black.getGreen() > 300) {
                black = new Color((int) Math.max(0, black.getRed() * 0.9 - 50), (int) Math.max(0, black.getGreen() * 0.9 - 50), (int) Math.max(0, black.getBlue() * 0.9 - 50));
            } else if (black.getRed() + black.getBlue() + black.getGreen() > 200) {
                black = new Color((int) Math.max(0, black.getRed() * 0.9 - 35), (int) Math.max(0, black.getGreen() * 0.9 - 35), (int) Math.max(0, black.getBlue() * 0.9 - 35));
            }

            // Iterate through each pixel and fill non-transparent pixels
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = image.getRGB(x, y);
                    int alpha = (pixel >> 24) & 0xFF; // Extract the alpha channel

                    double transparencyPercentage = (alpha / 255.0) * 100.0;

                    if (transparencyPercentage > 20) { // more than 20% transparent
                        Color color = new Color(image.getRGB(x, y));
                        if (color.getBlue() == 0 && color.getRed() == 0 && color.getGreen() == 0) {
                            image.setRGB(x, y, black.getRGB());
                        } else if (color.getBlue() == 255 && color.getRed() == 255 && color.getGreen() == 255) {
                            image.setRGB(x, y, white.getRGB());
                        }
                     }
                }
            }

            // Save the modified image to a new PNG file
            ImageIO.write(image, "PNG", new File(MainDirectory + "/temp/filltrans.png"));
         } catch (IOException e) {
            Automation.handleException(e);
        }
        return new File(MainDirectory + "/temp/filltrans.png");
    }

    public static File fillPNG(File input, int addred, int addgreen, int addblue) {
        try {
            // Load the PNG image
            BufferedImage image = ImageIO.read(input);


            // Create a new color with the adjusted RGB values

            int whiteThreshold = 200;

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    // Get the color of the current pixel
                    Color pixelColor = new Color(image.getRGB(x, y));
                    // Check if the pixel color is close to white based on threshold
                    int red = pixelColor.getRed() - 40;
                    int green = pixelColor.getGreen() - 40;
                    int blue = pixelColor.getBlue() - 40;
                    if (red != 255 && green != 255 && blue != 255) {
                        // Leave white pixels unchanged
                        int pixel = image.getRGB(x, y);
                        int alpha = (pixel >> 24) & 0xFF; // Extract the alpha channel

                        // Subtract the black amount from each RGB component
                        red = Math.max(0, red + addred);
                        green = Math.max(0, green + addgreen);
                        blue = Math.max(0, blue + addblue);
                        if (red > 255) {
                            red = 255;
                        }
                        if (green > 255) {
                            green = 255;
                        }
                        if (blue > 255) {
                            blue = 255;
                        }
                        double transparencyPercentage = (alpha / 255.0) * 100.0;


                        if (transparencyPercentage >= 100) { // more than 20% transparent

                            pixelColor = new Color(red, green, blue);

                            // Replace non-white pixels with the specified color
                            image.setRGB(x, y, pixelColor.getRGB());
                        }
                    }
                }
            }

            // Save the modified image to a new PNG file
            ImageIO.write(image, "PNG", new File(MainDirectory + "/temp/img.png"));
            Wait(100);
        } catch (IOException e) {
            Automation.handleException(e);
        }
        return new File(MainDirectory + "/temp/img.png");
    }
    private static boolean isWhiteish(Color color, int threshold) {
        // Check if the color is close to white based on the threshold
        return (color.getRed() > threshold && color.getGreen() > threshold && color.getBlue() > threshold);
    }
    public static AttributedString AddText(Graphics2D g2d, String Text, Color color, Font font, int x, int y, float size, boolean underLined, boolean fromBack) {
        AttributedString ClanName = new AttributedString(Text);
        if (font != null) {
            ClanName.addAttribute(TextAttribute.FONT, font.deriveFont(size));
        } else {
            ClanName.addAttribute(TextAttribute.SIZE, size);
        }
        if (underLined) {
            ClanName.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, Text.length());
        }
        TextLayout textlayout = new TextLayout(ClanName.getIterator(), g2d.getFontRenderContext());
        Shape shape;
        if (fromBack) {
            shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x - getWidthOfAttributedString(g2d, ClanName), y));
        } else {
            shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x, y));
        }
        g2d.setColor(color);
        g2d.fill(shape);
        return ClanName;
    }
    public static AttributedString AddText(Graphics2D g2d, String Text, Color color, Font font, int x, int y, float size, boolean underLined, boolean fromBack, boolean outline, float outlineSize, Color outlineColor) {
        AttributedString ClanName = new AttributedString(Text);
        if (font != null) {
            ClanName.addAttribute(TextAttribute.FONT, font.deriveFont(size));
        } else {
            ClanName.addAttribute(TextAttribute.SIZE, size);
        }
        if (underLined) {
            ClanName.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, Text.length());
        }
        TextLayout textlayout = new TextLayout(ClanName.getIterator(), g2d.getFontRenderContext());
        Shape shape;
        if (fromBack) {
            shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x - getWidthOfAttributedString(g2d, ClanName), y));
        } else {
            shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x, y));
        }

        Rectangle bounds = shape.getBounds();
        int padding = 2;  // Adjust the padding to make the gradient more prominent
        int gradientRadius = Math.max(bounds.width + padding, bounds.height + padding);  // Ensure the gradient covers the full width and height
         Point2D center = new Point2D.Float(x + bounds.width / 2f, y - bounds.height / 2f);
        float[] fractions = {0f, 1f};
        Color[] colors = {color.equals(Color.white) ? new Color(0, 0, 0, 40) : new Color(255, 255, 255, 40), new Color(0, 0, 0, 0)};  // Black to transparent
        RadialGradientPaint gradientPaint = new RadialGradientPaint(center, gradientRadius, fractions, colors);
        g2d.setPaint(gradientPaint);
        g2d.fill(new Rectangle2D.Double(bounds.getX() - (double) (padding / 2), bounds.getY() - (double) (padding / 2), bounds.getWidth() + padding, bounds.getHeight() + padding));


        if (outline) {
            g2d.setColor(outlineColor);
            g2d.setStroke(new BasicStroke(outlineSize));
            g2d.draw(shape);
        }
        g2d.setColor(color);
        g2d.fill(shape);
        return ClanName;
    }
    public static AttributedString AddTextCentered(Graphics2D g2d, String Text, Color color, Font font, int x, int y, float size) {
        AttributedString ClanName = new AttributedString(Text);
        if (font != null) {
            ClanName.addAttribute(TextAttribute.FONT, font.deriveFont(size));
        } else {
            ClanName.addAttribute(TextAttribute.SIZE, size);
        }
        TextLayout textlayout = new TextLayout(ClanName.getIterator(), g2d.getFontRenderContext());
        Shape shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x - (getWidthOfAttributedString(g2d, ClanName) / 2), y));
        g2d.setColor(color);
        g2d.fill(shape);
        return ClanName;
    }
    public static AttributedString AddTextCentered(Graphics2D g2d, String Text, Color color, Font font, int x, int y, float size, boolean underLined, boolean fromBack, boolean outline, float outlineSize, Color outlineColor) {
        AttributedString ClanName = new AttributedString(Text);
        if (font != null) {
            ClanName.addAttribute(TextAttribute.FONT, font.deriveFont(size));
        } else {
            ClanName.addAttribute(TextAttribute.SIZE, size);
        }
        if (underLined) {
            ClanName.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, Text.length());
        }
        TextLayout textlayout = new TextLayout(ClanName.getIterator(), g2d.getFontRenderContext());
        Shape shape;
        if (fromBack) {
            shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x - getWidthOfAttributedString(g2d, ClanName), y));
        } else {
            shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x - (getWidthOfAttributedString(g2d, ClanName) / 2), y));
        }

        Rectangle bounds = shape.getBounds();
        int padding = 2;  // Adjust the padding to make the gradient more prominent
        int gradientRadius = Math.max(bounds.width + padding, bounds.height + padding);  // Ensure the gradient covers the full width and height
        Point2D center = new Point2D.Float(x + bounds.width / 2f, y - bounds.height / 2f);
        float[] fractions = {0f, 1f};
        Color[] colors = {color.equals(Color.white) ? new Color(0, 0, 0, 40) : new Color(255, 255, 255, 40), new Color(0, 0, 0, 0)};  // Black to transparent
        RadialGradientPaint gradientPaint = new RadialGradientPaint(center, gradientRadius, fractions, colors);
        g2d.setPaint(gradientPaint);
        g2d.fill(new Rectangle2D.Double(bounds.getX() - (double) (padding / 2), bounds.getY() - (double) (padding / 2), bounds.getWidth() + padding, bounds.getHeight() + padding));


        if (outline) {
            g2d.setColor(outlineColor);
            g2d.setStroke(new BasicStroke(outlineSize));
            g2d.draw(shape);
        }
        g2d.setColor(color);
        g2d.fill(shape);
        return ClanName;
    }

    public static BufferedImage CutDiagonalTopLeft(BufferedImage originalImage) {

        // Calculate the width and height of the cropped image
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Create a new BufferedImage object to hold the cropped image
        BufferedImage croppedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Get the Graphics2D object of the cropped image
        Graphics2D g2d = croppedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Define a polygon that represents the transparent triangle to cut out of the image
        Polygon polygon = new Polygon();
        polygon.addPoint(0, 0); // Top Left
        polygon.addPoint(0, height); // Bottom Left
        polygon.addPoint((width / 2), (8 + height) / 2);
        polygon.addPoint((width / 2) - 20, (8 + height) / 2);

        polygon.addPoint(width - 32, 0); //Top Right

        // Clip the graphics context to the polygon
        g2d.setClip(polygon);

        // Draw the original image onto the clipped graphics context
        g2d.drawImage(originalImage, 0, 0, null);

        // Save the cropped image to a file

        g2d.dispose();
        return croppedImage;
    }
    public static BufferedImage CutDiagonalBottomRight(BufferedImage originalImage) {

        // Calculate the width and height of the cropped image
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Create a new BufferedImage object to hold the cropped image
        BufferedImage croppedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Get the Graphics2D object of the cropped image
        Graphics2D g2d = croppedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Define a polygon that represents the transparent triangle to cut out of the image
        Polygon polygon = new Polygon();
        polygon.addPoint(width, height); // Bottom Left 500 500
        polygon.addPoint(32, height); // Bottom Left
        polygon.addPoint((width / 2) + 20,(-8 + height) / 2);
        polygon.addPoint((width / 2), (-8 + height) / 2);

        polygon.addPoint(width, 0); //Top Right

        // Clip the graphics context to the polygon
        g2d.setClip(polygon);

        // Draw the original image onto the clipped graphics context
        g2d.drawImage(originalImage, 0, 0, null);

        // Save the cropped image to a file

        g2d.dispose();
        return croppedImage;
    }

    public static BufferedImage CutTransparentBorders(BufferedImage image) {
        if (image == null || image.getWidth() == 0 || image.getHeight() == 0) {
            throw new IllegalArgumentException("Invalid image.");
        }

        int boxWidth = image.getWidth();
        int boxHeight = image.getHeight();

        int x1 = boxWidth, y1 = boxHeight, x2 = 0, y2 = 0;
        boolean hasOpaque = false;

        for (int y = 0; y < boxHeight; y++) {
            for (int x = 0; x < boxWidth; x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
                if (alpha != 0) {
                    if (x < x1) x1 = x;
                    if (x > x2) x2 = x;
                    if (y < y1) y1 = y;
                    if (y > y2) y2 = y;
                    hasOpaque = true;
                }
            }
        }

        if (!hasOpaque) {
            // No opaque pixels found, return transparent image of same size
            return new BufferedImage(boxWidth, boxHeight, BufferedImage.TYPE_INT_ARGB);
        }

        int newWidth = x2 - x1 + 1;
        int newHeight = y2 - y1 + 1;

        BufferedImage cropped = image.getSubimage(x1, y1, newWidth, newHeight);

        // Scale the cropped image to fit into original box size
        BufferedImage scaled = new BufferedImage(boxWidth, boxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        double ratio = Math.min((double) boxWidth / newWidth, (double) boxHeight / newHeight);
        int scaledWidth = (int) (newWidth * ratio);
        int scaledHeight = (int) (newHeight * ratio);

        int xOffset = (boxWidth - scaledWidth) / 2;
        int yOffset = (boxHeight - scaledHeight) / 2;

        g.drawImage(cropped, xOffset, yOffset, scaledWidth, scaledHeight, null);
        g.dispose();

        return scaled;
    }
    public static BufferedImage CutTransparentBorders(File input) throws IOException {
        BufferedImage image = ImageIO.read(input);
        return CutTransparentBorders(image);
    }
    public static BufferedImage CutTransparentBorders(byte[] imageBytes) throws IOException {
        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(is);
            return CutTransparentBorders(image);
        }
    }

    public static void CutTransparentBorders(File input, File output, int boxWidth, int boxHeight) {
        try {
            BufferedImage image = ImageIO.read(input);
            // Determine the new dimensions of the cropped image
            int x1 = 0, y1 = 0, x2 = image.getWidth() - 1, y2 = image.getHeight() - 1;
            boolean foundOpaquePixel = false;

            // Find the leftmost opaque pixel column
            while (!foundOpaquePixel && x1 <= x2) {
                for (int y = y1; y <= y2; y++) {
                    if ((image.getRGB(x1, y) >> 24) != 0x00) {
                        foundOpaquePixel = true;
                        break;
                    }
                }
                if (!foundOpaquePixel) {
                    x1++;
                }
            }

            // Find the rightmost opaque pixel column
            foundOpaquePixel = false;
            while (!foundOpaquePixel && x2 >= x1) {
                for (int y = y1; y <= y2; y++) {
                    if ((image.getRGB(x2, y) >> 24) != 0x00) {
                        foundOpaquePixel = true;
                        break;
                    }
                }
                if (!foundOpaquePixel) {
                    x2--;
                }
            }

            // Find the topmost opaque pixel row
            foundOpaquePixel = false;
            while (!foundOpaquePixel && y1 <= y2) {
                for (int x = x1; x <= x2; x++) {
                    if ((image.getRGB(x, y1) >> 24) != 0x00) {
                        foundOpaquePixel = true;
                        break;
                    }
                }
                if (!foundOpaquePixel) {
                    y1++;
                }
            }

            // Find the bottommost opaque pixel row
            foundOpaquePixel = false;
            while (!foundOpaquePixel && y2 >= y1) {
                for (int x = x1; x <= x2; x++) {
                    if ((image.getRGB(x, y2) >> 24) != 0x00) {
                        foundOpaquePixel = true;
                        break;
                    }
                }
                if (!foundOpaquePixel) {
                    y2--;
                }
            }

            // Crop the image to the new dimensions
            int newWidth = x2 - x1 + 1;
            int newHeight = y2 - y1 + 1;
            BufferedImage croppedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = croppedImage.createGraphics();
            g.drawImage(image, 0, 0, newWidth, newHeight, x1, y1, x2 + 1, y2 + 1, null);
            g.dispose();

            // Scale the cropped image to fit exactly in the box
            int scaledWidth, scaledHeight;
            if ((double)newWidth / boxWidth > (double)newHeight / boxHeight) {
                scaledWidth = boxWidth;
                scaledHeight = (int)((double)newHeight / newWidth * boxWidth);
            } else {
                scaledWidth = (int)((double)newWidth / newHeight * boxHeight);
                scaledHeight = boxHeight;
            }
            BufferedImage scaledImage = new BufferedImage(boxWidth, boxHeight, BufferedImage.TYPE_INT_ARGB);
            g = scaledImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(croppedImage, (boxWidth - scaledWidth) / 2, (boxHeight - scaledHeight) / 2, scaledWidth, scaledHeight, null);
            g.dispose();
            ImageIO.write(scaledImage, "png", output);
        } catch (IOException e) {
            Automation.handleException(e);
        }
    }


    public static File ResizeImage(File input, double multiplier) {
        try {
            ImageIO.write(ResizeImage(ImageIO.read(input),multiplier), "png", new File(TempDirectory + "/" + input.getName()));
        } catch (IOException e) {
            Automation.handleException(e);
        }
        return new File(TempDirectory + "/" + input.getName());
    }
    public static void ResizeImage(File input, File output, double multiplier) {
        try {
            ImageIO.write(ResizeImage(ImageIO.read(input),multiplier), "png", output);
        } catch (IOException e) {
            Automation.handleException(e);
        }
    }
    public static BufferedImage ResizeImage(BufferedImage img, double multiplier) {
        BufferedImage scaledImage = new BufferedImage((int) (img.getWidth() * multiplier), (int) (img.getHeight() * multiplier), BufferedImage.TRANSLUCENT);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(img, 0, 0, (int) (img.getWidth() * multiplier), (int) (img.getHeight() * multiplier), null);
        g.dispose();
        return scaledImage;
    }
    public static boolean ImageHasTransparentPixel(File input) {
        try {
            BufferedImage image = ImageIO.read(input);
            int width = image.getWidth();
            int height = image.getHeight();

            // Iterate through all pixels
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Get the RGB values of the pixel
                    int rgb = image.getRGB(x, y);

                    // Check if the pixel is transparent
                    if ((rgb & 0xFF000000) == 0) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            Automation.handleException(e);
        }
        return false;
    }

    public static Color getDominantColor(File file) throws IOException {
        if (file.exists()) {
            BufferedImage image = ImageIO.read(file);
            return getDominantColorFromImage(image);
        } else {
            return Color.black;
        }
    }
    public static Color getDominantColor(String url) throws IOException {
        BufferedImage image = ImageIO.read(URI.create(url).toURL());
        return getDominantColorFromImage(image);
    }
    public static Color getDominantColor(byte[] imageBytes) throws IOException {
        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(is);
            return getDominantColorFromImage(image);
        }
    }

    private static Color getDominantColorFromImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Could not open or find the image");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        Map<Integer, Integer> colorCount = new HashMap<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xFF;
                if (pixel != -16777216 && pixel != -1 && alpha > 0) {
                    colorCount.put(pixel, colorCount.getOrDefault(pixel, 0) + 1);
                }
            }
        }

        int maxCount = 0;
        int dominantColorRGB = 0;
        for (Map.Entry<Integer, Integer> entry : colorCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantColorRGB = entry.getKey();
            }
        }

        Color color = new Color(dominantColorRGB);
        if (color.getRed() + color.getGreen() + color.getBlue() > 400) {
            color = adjustColor(color);
        }

        return color;
    }
    private static Color adjustColor(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        if (r > g && r > b) {
            return g > b ? new Color(Math.max(1, r - 10), Math.max(1, g - 10), Math.max(1, b - 60))
                    : new Color(Math.max(1, r - 10), Math.max(1, g - 60), Math.max(1, b - 10));
        } else if (g > r && g > b) {
            return r > b ? new Color(Math.max(1, r - 10), Math.max(1, g - 10), Math.max(1, b - 60))
                    : new Color(Math.max(1, r - 60), Math.max(1, g - 10), Math.max(1, b - 10));
        } else if (b > r && b > g) {
            return r > g ? new Color(Math.max(1, r - 10), Math.max(1, g - 60), Math.max(1, b - 10))
                    : new Color(Math.max(1, r - 60), Math.max(1, g - 10), Math.max(1, b - 10));
        } else {
            return new Color(Math.max(1, r - 25), Math.max(1, g - 25), Math.max(1, b - 25));
        }
    }

    private static int countOccurrences(Color color, BufferedImage image) {
        int count = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (color.equals(new Color(image.getRGB(x, y)))) {
                    count++;
                }
            }
        }
        return count;
    }

    public static String getHexValue(Color color) {
        if (color == null) {
            return "#808080";
        }
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        // Convert RGB values to hexadecimal

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public static BufferedImage CreateNumber(int Number) {
        Image Img0 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/0.png"))).getImage();
        Image Img1 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/1.png"))).getImage();
        Image Img2 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/2.png"))).getImage();
        Image Img3 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/3.png"))).getImage();
        Image Img4 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/4.png"))).getImage();
        Image Img5 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/5.png"))).getImage();
        Image Img6 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/6.png"))).getImage();
        Image Img7 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/7.png"))).getImage();
        Image Img8 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/8.png"))).getImage();
        Image Img9 = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/img/score/9.png"))).getImage();

        int Width = 0;
        Image[] img = new Image[10];
        int i = 0;
        for (char c : String.valueOf(Number).toCharArray()) {
            if (c == '0') {
                img[i] = Img0;
                Width = Width + Img0.getWidth(null);
            } else if (c == '1') {
                img[i] = Img1;
                Width = Width + Img1.getWidth(null);
            } else if (c == '2') {
                img[i] = Img2;
                Width = Width + Img2.getWidth(null);
            } else if (c == '3') {
                img[i] = Img3;
                Width = Width + Img3.getWidth(null);
            } else if (c == '4') {
                img[i] = Img4;
                Width = Width + Img4.getWidth(null);
            } else if (c == '5') {
                img[i] = Img5;
                Width = Width + Img5.getWidth(null);
            } else if (c == '6') {
                img[i] = Img6;
                Width = Width + Img6.getWidth(null);
            } else if (c == '7') {
                img[i] = Img7;
                Width = Width + Img7.getWidth(null);
            } else if (c == '8') {
                img[i] = Img8;
                Width = Width + Img8.getWidth(null);
            } else if (c == '9') {
                img[i] = Img9;
                Width = Width + Img9.getWidth(null);
            }
            Width = Width + 2;
            i++;
        }
        BufferedImage croppedImage = new BufferedImage(Width, 52, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = croppedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int drawwidth = 0;
        for (Image im : img) {
            if (im != null) {
                g2d.drawImage(im, drawwidth, 0, im.getWidth(null), im.getHeight(null), null);
                drawwidth = drawwidth + im.getWidth(null) + 2;
            }
        }
        g2d.dispose();
        return croppedImage;
    }
    public static BufferedImage CreateNumberStr(int Number) throws IOException {
        BufferedImage Numbers = ImageIO.read(Objects.requireNonNull(Main.class.getResource("/img/score/StrNumber.png")));
        int Width = 0;
        Image[] img = new Image[10];
        int i = 0;
        for (char c : String.valueOf(Number).toCharArray()) {
            if (Character.isDigit(c)) {
                if (c == '0') {
                    img[i] = Numbers.getSubimage(0, 0, 100, 116);
                } else if (c == '1') {
                    img[i] = Numbers.getSubimage(100, 0, 100, 116);
                } else if (c == '2') {
                    img[i] = Numbers.getSubimage(100 * 2, 0, 100, 116);
                } else if (c == '3') {
                    img[i] = Numbers.getSubimage(100 * 3, 0, 100, 116);
                } else if (c == '4') {
                    img[i] = Numbers.getSubimage(100 * 4, 0, 100, 116);
                } else if (c == '5') {
                    img[i] = Numbers.getSubimage(100 * 5, 0, 100, 116);
                } else if (c == '6') {
                    img[i] = Numbers.getSubimage(100 * 6, 0, 100, 116);
                } else if (c == '7') {
                    img[i] = Numbers.getSubimage(100 * 7, 0, 100, 116);
                } else if (c == '8') {
                    img[i] = Numbers.getSubimage(100 * 8, 0, 100, 116);
                } else if (c == '9') {
                    img[i] = Numbers.getSubimage(100 * 9, 0, 100, 116);
                }
                Width = Width + 100 - 5;
                i++;
            }
        }
        BufferedImage croppedImage = new BufferedImage(Width, 116, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = croppedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int drawwidth = 0;
        for (Image im : img) {
            if (im != null) {
                g2d.drawImage(im, drawwidth, 0, im.getWidth(null), im.getHeight(null), null);
                drawwidth = drawwidth + im.getWidth(null) - 20;
            }
        }
        g2d.dispose();
        return croppedImage;
    }
    public static BufferedImage CreateNumberVR(int Number) throws IOException {
        BufferedImage Numbers = ImageIO.read(Objects.requireNonNull(Main.class.getResource("/img/score/VRNumber.png")));
        int Width = 0;
        Image[] img = new Image[10];
        int i = 0;
        for (char c : String.valueOf(Number).toCharArray()) {
            if (Character.isDigit(c)) {
                if (c == '0') {
                    img[i] = Numbers.getSubimage(0, 0, 131, 150);
                } else if (c == '1') {
                    img[i] = Numbers.getSubimage(131, 0, 131, 150);
                } else if (c == '2') {
                    img[i] = Numbers.getSubimage(131 * 2, 0, 131, 150);
                } else if (c == '3') {
                    img[i] = Numbers.getSubimage(131 * 3, 0, 131, 150);
                } else if (c == '4') {
                    img[i] = Numbers.getSubimage(131 * 4, 0, 131, 150);
                } else if (c == '5') {
                    img[i] = Numbers.getSubimage(131 * 5, 0, 131, 150);
                } else if (c == '6') {
                    img[i] = Numbers.getSubimage(131 * 6, 0, 131, 150);
                } else if (c == '7') {
                    img[i] = Numbers.getSubimage(131 * 7, 0, 131, 150);
                } else if (c == '8') {
                    img[i] = Numbers.getSubimage(131 * 8, 0, 131, 150);
                } else if (c == '9') {
                    img[i] = Numbers.getSubimage(131 * 9, 0, 131, 150);
                }
                Width = Width + 131;
                i++;
            }
        }
        BufferedImage croppedImage = new BufferedImage(Width, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = croppedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int drawwidth = 0;
        for (Image im : img) {
            if (im != null) {
                g2d.drawImage(im, drawwidth, 0, im.getWidth(null), im.getHeight(null), null);
                drawwidth = drawwidth + im.getWidth(null) - 22;
            }
        }
        g2d.dispose();
        return croppedImage;
    }
    public static BufferedImage CreateNumberMini(int Number, int color, int Sign) throws IOException {
        BufferedImage MiniNumbers = ImageIO.read(Objects.requireNonNull(Main.class.getResource("/img/MiniNumbers.png")));
        int Width = 0;
        Image[] img = new Image[10];
        int i = 0;

        int H = 0;
        if (color == UColor.YELLOW) {
            H = 0;
        } else if (color == UColor.GREEN) {
            H = 12;
        } else if (color == UColor.BLUE) {
            H = 24;
        } else if (color == UColor.WHITE) {
            H = 36;
        } else if (color == UColor.PURPLE) {
            H = 48;
        } else if (color == UColor.PINK) {
            H = 60;
        }
        for (char c : String.valueOf(Number).toCharArray()) {
            if (Character.isDigit(c)) {
                if (c == '0') {
                    img[i] = MiniNumbers.getSubimage(0, H, 8, 12);
                } else if (c == '1') {
                    img[i] = MiniNumbers.getSubimage(8, H, 8, 12);
                } else if (c == '2') {
                    img[i] = MiniNumbers.getSubimage(16, H, 8, 12);
                } else if (c == '3') {
                    img[i] = MiniNumbers.getSubimage(24, H, 8, 12);
                } else if (c == '4') {
                    img[i] = MiniNumbers.getSubimage(32, H, 8, 12);
                } else if (c == '5') {
                    img[i] = MiniNumbers.getSubimage(40, H, 8, 12);
                } else if (c == '6') {
                    img[i] = MiniNumbers.getSubimage(48, H, 8, 12);
                } else if (c == '7') {
                    img[i] = MiniNumbers.getSubimage(56, H, 8, 12);
                } else if (c == '8') {
                    img[i] = MiniNumbers.getSubimage(64, H, 8, 12);
                } else if (c == '9') {
                    img[i] = MiniNumbers.getSubimage(72, H, 8, 12);
                }
                Width = Width + 8;
                i++;
            }
        }
        if (Sign == USign.ADD || Sign == USign.LESS) {
            Width = Width + 12;
        }
        BufferedImage croppedImage = new BufferedImage(Width, 12, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = croppedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int drawwidth = 12;
        if (Sign == USign.ADD) {
            g2d.drawImage(MiniNumbers.getSubimage(80, H,12,12), 0, 0, 12, 12, null);
        } else if (Sign == USign.LESS) {
            g2d.drawImage(MiniNumbers.getSubimage(92, H,12,12), 0, 0, 12, 12, null);
        } else {
            drawwidth = 0;
        }
        for (Image im : img) {
            if (im != null) {
                g2d.drawImage(im, drawwidth, 0, im.getWidth(null), im.getHeight(null), null);
                drawwidth = drawwidth + im.getWidth(null);
            }
        }
        g2d.dispose();
        return croppedImage;
    }


    public static BufferedImage CreateNumberMiniScript(String script) throws IOException {
        BufferedImage MiniNumbers = ImageIO.read(Objects.requireNonNull(Main.class.getResource("/img/MiniNumbers.png")));

        String[] split = script.split(" ");
        int Width = 0;
        int H = 0;

        for (String Script : split) {
            int sign = USign.NONE;
            int colour = UColor.WHITE;
            if (Script.contains("+")) {
                sign = USign.ADD;
            } else if (Script.contains("-")) {
                sign = USign.LESS;
            }
            if (Script.contains("[Y]")) {
                colour = UColor.YELLOW;
            } else if (Script.contains("[B]")) {
                colour = UColor.BLUE;
            } else if (Script.contains("[R]")) {
                colour = UColor.PINK;
            } else if (Script.contains("[P]")) {
                colour = UColor.PURPLE;
            } else if (Script.contains("[G]")) {
                colour = UColor.GREEN;
            } else if (Script.contains("[W]")) {
                colour = UColor.WHITE;
            }
            if (takeOnlyNumberStr(Script).length() > 0) {
                Width = Width + CreateNumberMini((int) takeOnlyDigits(Script), colour, sign).getWidth();
            } else if (Script.contains("To")) {
                Width = Width + 19;
            } else if (Script.contains("On")) {
                Width = Width + 12;
            } else if (Script.contains("Esp")) {
                Width = Width + 12;
            }
        }

        BufferedImage croppedImage = new BufferedImage(Width, 12, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = croppedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        Width = 0;
        for (String Script : split) {
            int sign = USign.NONE;
            int colour = UColor.WHITE;
            if (Script.contains("+")) {
                sign = USign.ADD;
            } else if (Script.contains("-")) {
                sign = USign.LESS;
            }
            if (Script.contains("[Y]")) {
                colour = UColor.YELLOW;
                H = 0;
            } else if (Script.contains("[B]")) {
                colour = UColor.BLUE;
                H = 24;
            } else if (Script.contains("[R]")) {
                colour = UColor.PINK;
                H = 60;
            } else if (Script.contains("[P]")) {
                colour = UColor.PURPLE;
                H = 48;
            } else if (Script.contains("[G]")) {
                colour = UColor.GREEN;
                H = 12;
            } else if (Script.contains("[W]")) {
                colour = UColor.WHITE;
                H = 36;
            }
            if (takeOnlyNumberStr(Script).length() > 0) {
                BufferedImage imgg = CreateNumberMini(takeOnlyInts(Script), colour, sign);
                g2d.drawImage(imgg, Width, 0, imgg.getWidth(null), imgg.getHeight(null), null);
                Width = Width + imgg.getWidth();
            } else if (Script.contains("To")) {
                Width = Width + 2;
                g2d.drawImage(MiniNumbers.getSubimage(104, H,15,12), Width, 0, 15, 12, null);
                Width = Width + 17;
            } else if (Script.contains("On")) {
                g2d.drawImage(MiniNumbers.getSubimage(119, H,12,12), Width, 0, 12,12, null);
                Width = Width + 12;
            } else if (Script.contains("Esp")) {
                Width = Width + 12;
            }
        }
        g2d.dispose();
        return croppedImage;
    }

    public static BufferedImage convertToWhite(Image image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        BufferedImage whiteImage = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        bufferedImage.getGraphics().drawImage(image, 0, 0, null);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = bufferedImage.getRGB(x, y);
                int alpha = (rgba >> 24) & 0xFF;

                if (alpha == 0) {
                    // Transparent pixel, copy as is
                    whiteImage.setRGB(x, y, rgba);
                } else {
                    // Non-transparent pixel, convert to white
                    int whiteRGB = (255 << 24) | (255 << 16) | (255 << 8) | 255;
                    whiteImage.setRGB(x, y, whiteRGB);
                }
            }
        }

        return whiteImage;
    }
    public static BufferedImage replaceColorWithImage(BufferedImage mainImage, Color colorToReplace, BufferedImage replacementImage) {

        // Create a BufferedImage with the same dimensions as the main image
        int width = mainImage.getWidth();
        int height = mainImage.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Get the color of the current pixel in the main image
                Color pixelColor = new Color(mainImage.getRGB(x, y));

                // Check if the pixel color matches the color to replace
                if (pixelColor.equals(colorToReplace)) {
                    // Get the corresponding pixel from the replacement image
                    int replacementPixel = replacementImage.getRGB(x % replacementImage.getWidth(), y % replacementImage.getHeight());
                    resultImage.setRGB(x, y, replacementPixel);
                } else {
                    // Keep the original pixel color
                    resultImage.setRGB(x, y, mainImage.getRGB(x, y));
                }
            }
        }

        return resultImage;
    }
    public static BufferedImage CircleAnImage(BufferedImage image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D resultImage = mask.createGraphics();
        resultImage.setColor(Color.white);
        resultImage.fillOval(0, 0, width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Get the color of the current pixel in the main image
                Color pixelColor = new Color(mask.getRGB(x, y));

                // Check if the pixel color matches the color to replace
                if (pixelColor.equals(Color.white)) {
                    // Get the corresponding pixel from the replacement image
                    int replacementPixel = image.getRGB(x, y);
                    mask.setRGB(x, y, replacementPixel);
                }
            }
        }

        return mask;
    }
    public static BufferedImage CutInsideImage(Image image, int x, int y) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        int HeightDiff = Math.max(0, x - y);
        int WidthDiff = Math.max(0, y - x);

        BufferedImage mask = new BufferedImage(x, y, BufferedImage.TRANSLUCENT);
        Graphics2D resultImage = mask.createGraphics();
        resultImage.drawImage(image, -(WidthDiff/2), -(HeightDiff/2), x + WidthDiff, y + HeightDiff,  null);
        return mask;
    }
    public static BufferedImage MakeOpacity(Image image, float alpha) {
        BufferedImage mask = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TRANSLUCENT);
        Graphics2D resultImage = mask.createGraphics();
        resultImage.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        resultImage.drawImage(image, 0, 0, null);
        return mask;
    }
    public static BufferedImage MakeRoundCorner(Image image, int cornerradius) {
        BufferedImage mask = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TRANSLUCENT);
        Graphics2D resultImage = mask.createGraphics();
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(0, 0, image.getWidth(null), image.getHeight(null), cornerradius, cornerradius); // Adjust the corner radius as needed
        resultImage.setClip(roundedRect);
        resultImage.drawImage(image, 0, 0, null);
        return mask;
    }

    public static Image createRightFadeGradient(Image originalImage, int amount, int withWidth, int withHeight) {
        if (amount <= 0) return originalImage;
        BufferedImage fadedImage = new BufferedImage(withWidth, withHeight, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = fadedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, withWidth, withHeight, null);
        GradientPaint gradient = new GradientPaint(
                withWidth - amount, 0, new Color(255, 255, 255, 255),
                withWidth, 0, new Color(255, 255, 255, 0),
                false
        );

        g2d.setComposite(AlphaComposite.DstIn);
        g2d.setPaint(gradient);
        g2d.fillRect(withWidth - amount, 0, amount, withHeight);
        g2d.dispose();

        return fadedImage;
    }

    public interface UColor {
        int YELLOW = 0;
        int GREEN = 1;
        int BLUE = 2;
        int WHITE = 3;
        int PURPLE = 4;
        int PINK = 5;
    }
    public interface USign {
        int NONE = 0;
        int ADD = 1;
        int LESS = 2;
    }
}
