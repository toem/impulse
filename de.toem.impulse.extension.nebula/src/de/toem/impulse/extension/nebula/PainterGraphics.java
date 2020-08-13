package de.toem.impulse.extension.nebula;
import java.util.Stack;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.TextLayout;

import de.toem.basics.core.Utils;
import de.toem.eclipse.toolkits.tlk.ITlkPainter;

public class PainterGraphics extends Graphics {

    ITlkPainter.Serialization serialization;

    public PainterGraphics(int width, int height) {
        this.serialization = new ITlkPainter.Serialization(width, height);
        initStack();
    }

    public byte[] getBytes() {
        return this.serialization.getBytes();
    }

    @Override
    public void dispose() {
    }

    void log(Object... logs) {
        // if (!logs[0].toString().startsWith("##"))
//        Utils.log(logs);
    }

    // ========================================================================================================================
    // Settings state stack
    // ========================================================================================================================

    Stack<Settings> stack;
    Settings current;

    class Settings {

        // status
        Color foreground;
        Color background;
        Integer alpha;
        Font font;

        LineAttributes lineAttributes;

        Rectangle rectClip;

        public Settings() {
        }

        public Settings(Settings parent) {
            this.foreground = parent.foreground;
            this.background = parent.background;
            this.alpha = parent.alpha;
            this.font = parent.font;
            this.lineAttributes = parent.lineAttributes;
            this.rectClip = parent.rectClip;
        }
    }

    void initStack() {
        stack = new Stack<>();
        current = new Settings();
        stack.push(current);
    }

    // ========================================================================================================================
    // Settings management
    // ========================================================================================================================

    @Override
    public void popState() {
        if (stack == null || stack.size() <= 1)
            return;
        stack.pop();
        current = stack.peek();
        serialization.write(ITlkPainter.Serialization.POP_STATE);
        log("POP_STATE");
    }

    @Override
    public void pushState() {
        if (stack == null) {
            initStack();
        }
        stack.push(current = new Settings(current));
        serialization.write(ITlkPainter.Serialization.PUSH_STATE);
        log("PUSH_STATE");
    }

    @Override
    public void restoreState() {
        if (stack == null || stack.size() <= 1)
            return;
        Settings previous = stack.pop();
        current = stack.peek();
        stack.push(current = new Settings(current));
        serialization.write(ITlkPainter.Serialization.RESTORE_STATE);
        log("RESTORE_STATE");
    }
    // ========================================================================================================================
    // Color/Font settings
    // ========================================================================================================================

    @Override
    public Color getBackgroundColor() {
        log("getBackgroundColor");
        return stack.peek().background;
    }

    @Override
    public void setBackgroundColor(Color color) {
        if (color.isDisposed())
            return;
        stack.peek().background = color;
        serialization.write(ITlkPainter.Serialization.SET_BACKGROUND, (byte) color.getRed(), (byte) color.getGreen(),
                (byte) color.getBlue(), (byte) color.getAlpha());
        log("setBackgroundColor", color.getRGBA());
    }

    @Override
    public Color getForegroundColor() {
        log("getForegroundColor");
        return stack.peek().foreground;
    }

    @Override
    public void setForegroundColor(Color color) {
        if (color.isDisposed())
            return;
        stack.peek().foreground = color;
        serialization.write(ITlkPainter.Serialization.SET_FOREGROUND, (byte) color.getRed(), (byte) color.getGreen(),
                (byte) color.getBlue(), (byte) color.getAlpha());
        log("setForegroundColor", color.getRGBA());
    }

    @Override
    public Font getFont() {
        log("getFont");
        return stack.peek().font;
    }

    @Override
    public void setFont(Font font) {
        if (font.isDisposed())
            return;
        stack.peek().font = font;
        serialization.write(ITlkPainter.Serialization.SET_FONT, (byte) 0);
        log("setFont", 0);
    }

    @Override
    public void setAlpha(int alpha) {
        stack.peek().alpha = alpha;
        serialization.write(ITlkPainter.Serialization.SET_ALPHA, (byte) alpha);
        log("setAlpha");
    }

    @Override
    public void setBackgroundPattern(Pattern pattern) {
        log("## setBackgroundPattern");
    }

    @Override
    public void setFillRule(int rule) {
        log("## setFillRule");
    }

    @Override
    public void setForegroundPattern(Pattern pattern) {
        log("## setForegroundPattern");
    }

    @Override
    public FontMetrics getFontMetrics() {
        log("## getFontMetrics");
        return null;
    }

    // ========================================================================================================================
    // Line settings
    // ========================================================================================================================

    @Override
    public void setLineAttributes(LineAttributes attributes) {
        stack.peek().lineAttributes = attributes;
        serialization.write(ITlkPainter.Serialization.SET_LINEWIDTH,  attributes.width);
        serialization.write(ITlkPainter.Serialization.SET_LINESTYLE, attributes.style);
        log("setLineAttributes");
    }
    
    @Override
    public int getLineStyle() {
        log("getLineStyle");
        return (int) (stack.peek().lineAttributes != null ?stack.peek().lineAttributes.style:0);
    }

    @Override
    public void setLineStyle(int style) {
        if (stack.peek().lineAttributes == null)
            stack.peek().lineAttributes = new LineAttributes((float) 1.0);
        stack.peek().lineAttributes.style = style;
        serialization.write(ITlkPainter.Serialization.SET_LINESTYLE, style);
        log("setLineStyle", style);
    }

    @Override
    public int getLineWidth() {
        log("getLineWidth");
        return (int) (stack.peek().lineAttributes != null ?stack.peek().lineAttributes.width:0);
    }

    @Override
    public void setLineWidth(int width) {
        if (stack.peek().lineAttributes == null)
            stack.peek().lineAttributes = new LineAttributes((float) 1.0);
        stack.peek().lineAttributes.width = width;
        serialization.write(ITlkPainter.Serialization.SET_LINEWIDTH,  (float)width);
        log("setLineWidth", width);
    }

    @Override
    public float getLineWidthFloat() {
        log("getLineWidthFloat");
        return (stack.peek().lineAttributes != null ?stack.peek().lineAttributes.width:0);
    }
    
    @Override
    public void setLineWidthFloat(float width) {
        if (stack.peek().lineAttributes == null)
            stack.peek().lineAttributes = new LineAttributes((float) 1.0);
        stack.peek().lineAttributes.width = width;
        serialization.write(ITlkPainter.Serialization.SET_LINEWIDTH, (float) width);
        log("setLineWidthFloat", width);
    }

    @Override
    public void setLineMiterLimit(float miterLimit) {
        log("## setLineMiterLimit");
    }

    @Override
    public LineAttributes getLineAttributes() {
        log("#### getLineAttributes");
        return null;
    }

    @Override
    public int getLineCap() {
        log("#### getLineCap");
        return SWT.CAP_FLAT;
    }

    @Override
    public int getLineJoin() {
        log("#### getLineJoin");
        return SWT.JOIN_MITER;
    }

    @Override
    public float getLineMiterLimit() {
        log("#### getLineMiterLimit");
        return 0;
    }


    @Override
    public void setLineCap(int cap) {
        log("#### setLineCap");
    }

    @Override
    public void setLineDash(int dash[]) {
        log("#### setLineDash");
    }

    @Override
    public void setLineDash(float[] value) {
        log("#### setLineDash");
    }

    @Override
    public void setLineDashOffset(float value) {
        log("#### setLineDashOffset");
    }

    @Override
    public void setLineJoin(int join) {
        log("#### setLineJoin");
    }

    // ========================================================================================================================
    // Clipping settings
    // ========================================================================================================================

    @Override
    public void setClip(Rectangle r) {
        serialization.write(ITlkPainter.Serialization.SET_RECT_CLIP, r.x, r.y, r.width, r.height);
        stack.peek().rectClip = r.getCopy();
        log("setClip", r.x, r.y, r.width, r.height);
    }

    @Override
    public void clipRect(Rectangle r) {
        serialization.write(ITlkPainter.Serialization.CLIP_RECT, r.x, r.y, r.width, r.height);
        if (stack.peek().rectClip != null)
            stack.peek().rectClip = stack.peek().rectClip.intersect(r);
        else
            stack.peek().rectClip = r.getCopy();
        log("clipRect", r.x, r.y, r.width, r.height);
    }

    @Override
    public Rectangle getClip(Rectangle rect) {
        if (stack.peek().rectClip != null) {
            rect.x = stack.peek().rectClip.x;
            rect.y = stack.peek().rectClip.y;
            rect.width = stack.peek().rectClip.width;
            rect.height = stack.peek().rectClip.height;
        }else {
            rect.x = 0;
            rect.y = 0;
            rect.width = serialization.getWidth();
            rect.height = serialization.getHeight();  
        }
        log("getClip");
        rect.x =Short.MIN_VALUE;
        rect.y = Short.MIN_VALUE;
        rect.width = 2*Short.MAX_VALUE;
        rect.height = 2*Short.MAX_VALUE;  
        return rect;
    }

    @Override
    public void clipPath(Path path) {
        log("#### clipPath");
    }

    @Override
    public void setClip(Path path) {
        log("#### setClip");
    }
    // ========================================================================================================================
    // Transform settings
    // ========================================================================================================================

    @Override
    public void translate(int dx, int dy) {
        serialization.write(ITlkPainter.Serialization.TRANSLATE, (float)dx,(float) dy);
        log("translate", dx, dy);
    }

    @Override
    public void translate(float dx, float dy) {
        serialization.write(ITlkPainter.Serialization.TRANSLATE, (float) dx, (float) dy);
        log("translate2", dx, dy);
    }

    @Override
    public void scale(double amount) {
        serialization.write(ITlkPainter.Serialization.SCALE, (float) amount, (float) amount);
        log("scale", amount);
    }

    @Override
    public void scale(float horizontal, float vertical) {
        serialization.write(ITlkPainter.Serialization.SCALE, (float)horizontal,(float) vertical);
        log("scale",horizontal,vertical);
    }

    @Override
    public void rotate(float degrees) {
        serialization.write(ITlkPainter.Serialization.ROTATE, (float)degrees);
        log("scale",degrees);
    }

    @Override
    public void shear(float horz, float vert) {
        log("#### shear");
    }

    // ========================================================================================================================
    // Other settings
    // ========================================================================================================================

    @Override
    public void setXORMode(boolean b) {
        log("## setXORMode");
    }

    @Override
    public boolean getXORMode() {
        log("## getXORMode");
        return false;
    }

    @Override
    public void setAdvanced(boolean advanced) {
        log("#### setAdvanced");
    }

    @Override
    public void setAntialias(int antialias) {
        log("#### setAntialias");
    }

    @Override
    public boolean getAdvanced() {
        log("#### getAdvanced");
        return false;
    }

    @Override
    public int getAntialias() {
        log("#### getAntialias");
        return SWT.DEFAULT;
    }

    @Override
    public int getFillRule() {
        log("#### getFillRule");
        return 0;
    }

    @Override
    public int getInterpolation() {
        log("#### getInterpolation");
        return 0;
    }

    @Override
    public int getTextAntialias() {
        log("#### getTextAntialias");
        return SWT.DEFAULT;
    }

    @Override
    public void setInterpolation(int interpolation) {
        log("#### setInterpolation");
    }

    @Override
    public void setTextAntialias(int value) {
        log("#### setTextAntialias");
    }
    // ========================================================================================================================
    // Draw
    // ========================================================================================================================

    @Override
    public void drawArc(int x, int y, int w, int h, int offset, int length) {
        serialization.write(ITlkPainter.Serialization.DRAW_ARC, x, y, w, h, offset, length);
        log("drawArc", x, y, w, h, offset, length);
    }

    @Override
    public void drawFocus(int x, int y, int w, int h) {
        serialization.write(ITlkPainter.Serialization.DRAW_FOCUS, x, y, w, h);
        log("drawFocus", x, y, w, h);
    }

    @Override
    public void drawImage(Image image, int x, int y) {
        serialization.write(ITlkPainter.Serialization.DRAW_IMAGE, x, y, image);
        log("drawImage", x, y);
    }

    @Override
    public void drawImage(Image image, int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        serialization.write(ITlkPainter.Serialization.DRAW_SOURCEIMAGE, x1, y1, w1, h1, x2, y2, w2, h2, image);
        log("drawImage2", x1, y1, w1, h1, x2, y2, w2, h2);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        serialization.write(ITlkPainter.Serialization.DRAW_LINE, x1, y1, x2, y2);
        log("drawLine", x1, y1, x2, y2);
    }

    @Override
    public void drawOval(int x, int y, int w, int h) {
        serialization.write(ITlkPainter.Serialization.DRAW_OVAL, x, y, w, h);
        log("drawOval", x, y, w, h);
    }

    @Override
    public void drawPolygon(PointList points) {
        serialization.write(ITlkPainter.Serialization.DRAW_POLYGON, points.toIntArray());
        log("drawPolygon", points.size());
    }

    @Override
    public void drawPolyline(PointList points) {
        serialization.write(ITlkPainter.Serialization.DRAW_POLYLINE, points.toIntArray());
        log("drawPolyline", points.size());
    }

    @Override
    public void drawRectangle(int x, int y, int w, int h) {
        serialization.write(ITlkPainter.Serialization.DRAW_RECT, x, y, w, h);
        log("drawRectangle", x, y, w, h);
    }

    @Override
    public void drawRoundRectangle(Rectangle r, int aw, int ah) {
        serialization.write(ITlkPainter.Serialization.DRAW_ROUNDRECT, r.x, r.y, r.width, r.height, aw, ah);
        log("drawRoundRectangle", r.x, r.y, r.width, r.height, aw, ah);
    }

    @Override
    public void drawString(String text, int x, int y) {
        serialization.write(ITlkPainter.Serialization.DRAW_STRING, x, y, text);
        log("drawString", x, y, text);
    }

    @Override
    public void drawText(String text, int x, int y) {
        serialization.write(ITlkPainter.Serialization.DRAW_TEXT, x, y, text);
        log("drawText", x, y, text);
    }

    @Override
    public void fillArc(int x, int y, int w, int h, int offset, int length) {
        serialization.write(ITlkPainter.Serialization.FILL_ARC, x, y, w, h, offset, length);
        log("fillArc", x, y, w, h, offset, length);
    }

    @Override
    public void fillGradient(int x, int y, int w, int h, boolean vertical) {
        serialization.write(ITlkPainter.Serialization.FILL_GRAD, x, y, w, h, vertical);
        log("fillGradient", x, y, w, h, vertical);
    }

    @Override
    public void fillOval(int x, int y, int w, int h) {
        serialization.write(ITlkPainter.Serialization.FILL_OVAL, x, y, w, h);
        log("fillOval", x, y, w, h);
    }

    @Override
    public void fillPolygon(PointList points) {
        serialization.write(ITlkPainter.Serialization.FILL_POLYGON, points.toIntArray());
        log("fillPolygon", points.size());
    }

    @Override
    public void fillRectangle(int x, int y, int w, int h) {
        serialization.write(ITlkPainter.Serialization.FILL_RECT, x, y, w, h);
        log("fillRectangle", x, y, w, h);
    }

    @Override
    public void fillRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        serialization.write(ITlkPainter.Serialization.FILL_ROUNDRECT, r.x, r.y, r.width, r.height, arcWidth, arcHeight);
        log("fillRoundRectangle", r.x, r.y, r.width, r.height, arcWidth, arcHeight);
    }

    @Override
    public void fillString(String text, int x, int y) {
        serialization.write(ITlkPainter.Serialization.FILL_STRING, x, y, text);
        log("fillString", x, y, text);
    }

    @Override
    public void fillText(String text, int x, int y) {
        serialization.write(ITlkPainter.Serialization.FILL_TEXT, x, y, text);
        log("fillText", x, y, text);
    }

    @Override
    public void drawPath(Path path) {
        log("#### drawPath");
    }

    @Override
    public void drawText(String s, int x, int y, int style) {
        log("#### drawText");
    }

    @Override
    public void drawTextLayout(TextLayout layout, int x, int y, int selectionStart, int selectionEnd,
            Color selectionForeground, Color selectionBackground) {
        log("#### drawTextLayout");
    }

    @Override
    public void fillPath(Path path) {
        log("#### fillPath");
    }

} 