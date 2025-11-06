package iecompbot.img;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedString;

import static iecompbot.img.ImgUtilities.getWidthOfAttributedString;

public class MyAttributedString extends AttributedString {
    protected final String text;
    protected Color color;
    protected int x;
    protected int y;
    protected float size;

    protected Color outlineColor = null;
    protected float outlineSize = 0;
    protected boolean gradient = false;

    public MyAttributedString(String text, Color color, int x, int y, float size) {
        super(text);
        this.text = text;
        this.color = color;
        this.x = x;
        this.y = y;
        this.size = size;
        addAttribute(TextAttribute.SIZE, size);
    }

    public MyAttributedString withFont(Font font, float size) {
        addAttribute(TextAttribute.FONT, font.deriveFont(size));
        return this;
    }
    public MyAttributedString withFont(Font font, boolean bold) {
        if (bold) {
            addAttribute(TextAttribute.FONT, font.deriveFont(Font.BOLD, size));
        } else {
            addAttribute(TextAttribute.FONT, font.deriveFont(size));
        }
        return this;
    }
    public MyAttributedString withUnderlined() {
        addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, text.length());
        return this;
    }
    public MyAttributedString withOutline(Color outlineColor, float outlineSize) {
        this.outlineColor = outlineColor;
        this.outlineSize = outlineSize;
        return this;
    }
    public MyAttributedString withOutline(Color outlineColor, float outlineSize, boolean gradient) {
        this.gradient = gradient;
        return withOutline(outlineColor, outlineSize);
    }

    public PostDrawing drawCentered(Graphics2D g2d) {
        TextLayout textlayout = new TextLayout(getIterator(), g2d.getFontRenderContext());
        Shape shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x - (getWidthOfAttributedString(g2d, this) / 2), y));
        g2d.setColor(color);
        g2d.fill(shape);
        drawOutline(g2d, shape);
        g2d.setColor(color);
        g2d.fill(shape);
        return new PostDrawing(g2d, shape,this);
    }
    public PostDrawing drawFromBehind(Graphics2D g2d) {
        TextLayout textlayout = new TextLayout(getIterator(), g2d.getFontRenderContext());
        Shape shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x - getWidthOfAttributedString(g2d, this), y));
        g2d.setColor(color);
        g2d.fill(shape);
        drawOutline(g2d, shape);
        g2d.setColor(color);
        g2d.fill(shape);
        return new PostDrawing(g2d, shape,this);
    }
    public PostDrawing drawFromFront(Graphics2D g2d) {
        TextLayout textlayout = new TextLayout(getIterator(), g2d.getFontRenderContext());
        Shape shape = textlayout.getOutline(AffineTransform.getTranslateInstance(x, y));
        g2d.setColor(color);
        g2d.fill(shape);
        drawOutline(g2d, shape);
        g2d.setColor(color);
        g2d.fill(shape);
        return new PostDrawing(g2d, shape,this);
    }

    private void drawOutline(Graphics2D g2d, Shape shape) {
        if (outlineColor != null) {
            if (gradient) {
                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.10)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.90)));
                g2d.draw(shape);

                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.20)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.80)));
                g2d.draw(shape);

                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.30)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.70)));
                g2d.draw(shape);

                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.40)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.60)));
                g2d.draw(shape);

                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.50)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.50)));
                g2d.draw(shape);

                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.60)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.40)));
                g2d.draw(shape);

                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.70)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.30)));
                g2d.draw(shape);

                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.80)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.20)));
                g2d.draw(shape);

                g2d.setColor(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (outlineColor.getAlpha() * 0.90)));
                g2d.setStroke(new BasicStroke((float) (outlineSize * 0.10)));
                g2d.draw(shape);
            } else {
                g2d.setColor(outlineColor);
                g2d.setStroke(new BasicStroke(outlineSize));
                g2d.draw(shape);
            }
        }
    }

    public static class PostDrawing extends MyAttributedString {
        protected Graphics2D g2d;
        protected Shape shape;
        public PostDrawing(Graphics2D g2d, Shape shape, MyAttributedString s) {
            super(s.text, s.color, s.x, s.y, s.size);
            this.shape = shape;
            this.g2d = g2d;
        }
    }
}
