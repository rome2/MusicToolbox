package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

class StaveGlyph {

    public StaveGlyph(String glyph, float x, float y, float width) {
        this.glyph = glyph;
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public float measure(TextPaint painter) {
        return painter.measureText(glyph);
    }

    public void draw(Canvas canvas, float x, float y, TextPaint painter) {
        canvas.drawText(glyph, this.x + x, this.y + y, painter);
    }

    private String glyph;
    private float x;
    private float y;
    private float width;
}

class Stave {

    public Stave(Context context) {

        // Create text painter for the score symbols:
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Bravura.otf");
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTypeface(typeface);
        mTextPaint.setTextSize(mTextSize);

        // Create normal painter for the stave lines:
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(2);
    }

    public void draw(Canvas canvas, float x, float y, float width) {

        // Update layout if needed:
        if (mInvalidated)
            updateLayout();

        // Start with the lines:
        drawStaveLines(canvas, x, y, width);

        // Draw static glyphs:
        for (StaveGlyph glyph : mStaticGlyphs) {
            glyph.draw(canvas, x, y, mTextPaint);
        }
    }

    private void drawStaveLines(Canvas canvas, float x, float y, float width) {

        // All stave lines combined are "textSize" pixels high so the distance between two stave
        // lines is 1/4 of that:
        float lineHeight = mTextSize / 4;

        // Draw the lines:
        for (int i = 0; i < 5; i++) {
            float iy = (int)(y - (i * lineHeight)) + 0.5f;
            canvas.drawLine(x, iy, x + width, iy, mLinePaint);
        }
    }

    private void updateLayout() {

        // Start fresh:
        mStaticGlyphs.clear();

        // First up is a bit of empty space:
        float margin = mTextSize * mMargin;
        float x = margin;

        // Add the clef:
        x += addClef(x);
        x += margin;
        x += addKey(x);
        x += margin;
        x += addTimeSignature(x);
        x += margin;

        // Save width for later:
        mStaticGlyphsWidth = x;

        // Flag valid:
        mInvalidated = false;
    }

    private void invalidate() {

        // Flag invalid:
        mInvalidated = false;
    }

    enum Clef {
        G,
        F,
        C,
        N
    }

    private Clef mClef = Clef.G;
    private int mClefOctave = 0;

    private float addClef(float x) {

        String textString = "";
        int trans = 0;

        // Extract glyph for the clef:
        if (mClef == Clef.G) {
            trans = 2;
            if (mClefOctave == -2)
                textString ="\uE051";
            else if (mClefOctave == -1)
                textString ="\uE052";
            else if (mClefOctave == 1)
                textString ="\uE053";
            else if (mClefOctave == 2)
                textString ="\uE053";
            else
                textString ="\uE050";
        } else if (mClef == Clef.F) {
            trans = 6;
            if (mClefOctave == -2)
                textString ="\uE063";
            else if (mClefOctave == -1)
                textString ="\uE064";
            else if (mClefOctave == 1)
                textString ="\uE065";
            else if (mClefOctave == 2)
                textString ="\uE066";
            else
                textString ="\uE062";
        } else if (mClef == Clef.C) {
            trans = 4;
            if (mClefOctave == -1)
                textString ="\uE05D";
            else
                textString ="\uE05C";
        } else if (mClef == Clef.N) {
            trans = 4;
            textString ="\uE069";
        }

        float offset = (mTextSize * trans) / 8;
        float width = mTextPaint.measureText(textString);

        StaveGlyph newGlyph = new StaveGlyph(textString, x, -offset, width);
        mStaticGlyphs.add(newGlyph);

        return width;
    }

    private int mUpperTimeSignature = 4;
    private int mLowerTimeSignature = 4;
    private boolean mUseTimeSymbols = true;

    private float addTimeSignature(float x) {

        // Use symbols instead of numbers?
        if (mUseTimeSymbols && mUpperTimeSignature == mLowerTimeSignature && (mUpperTimeSignature == 2 || mUpperTimeSignature == 4)) {

            // Find symbol:
            String textString = "";
            if (mUpperTimeSignature == 4)
                textString = "\uE08A";
            else
                textString = "\uE08B";

            float width = mTextPaint.measureText(textString);

            StaveGlyph newGlyph = new StaveGlyph(textString, x, -mTextSize / 2, width);
            mStaticGlyphs.add(newGlyph);

            return width;
        }

        // The unicode characters for the time signature numbers (0-9):
        String[] chars = { "\uE080", "\uE081", "\uE082", "\uE083", "\uE084",
                           "\uE085", "\uE086", "\uE087", "\uE088", "\uE089" };

        // Convert upper part to a string:
        String upperText = "";
        float upper = mUpperTimeSignature;
        while (upper >= 0.5) {
            upperText = chars[(int)upper % 10] + upperText;
            upper = (float)Math.floor(upper / 10);
        }

        // Convert lower part to a string:
        String lowerText = "";
        float lower = mLowerTimeSignature;
        while (lower >= 0.5) {
            lowerText = chars[(int)lower % 10] + lowerText;
            lower = (float)Math.floor(lower / 10);
        }

        // Measure texts:
        float upperSize = mTextPaint.measureText(upperText);
        float lowerSize = mTextPaint.measureText(lowerText);

        // Center text:
        float upperX = 0;
        float lowerX = 0;
        if (upperSize > lowerSize)
            lowerX = (upperSize - lowerSize) / 2;
        else
            upperX = (lowerSize - upperSize) / 2;

        // Add the glyphs:
        StaveGlyph upperGlyph = new StaveGlyph(upperText, x + upperX, -(mTextSize * 3) / 4, upperSize);
        mStaticGlyphs.add(upperGlyph);
        StaveGlyph lowerGlyph = new StaveGlyph(lowerText, x + lowerX, -mTextSize / 4, lowerSize);
        mStaticGlyphs.add(lowerGlyph);

        // Return text width:
        if (upperSize > lowerSize)
            return upperSize;
        return lowerSize;
    }

    private int mKey = 2;

    private float addKey(float x) {

        // The C key has no sharps or flats:
        if (mKey == 0)
            return 0.0f;

        // Get glyph to draw and calc sizes:
        String keyString = mKey < 0 ? "\u266D" : "\u266F";
        float width = mTextPaint.measureText(keyString);
        float totalWidth = Math.abs(mKey) * width;

        // Get sharp and flat positions:
        int[] sharps = null;
        int[] flats = null;
        if (mClef == Clef.G) {
            sharps = new int[] { 8, 5, 9, 6, 3, 7, 4 };
            flats = new int[] { 4, 7, 3, 6, 2, 5, 1 };
        }
        else if (mClef == Clef.F) {
            sharps = new int[] { 6, 3, 7, 4, 1, 5, 3 };
            flats = new int[] { 2, 5, 1, 4, 0, 3, -1 };
        }
        else if (mClef == Clef.C) {
            sharps = new int[] { 7, 4, 8, 5, 2, 6, 3 };
            flats = new int[] { 3, 6, 2, 5, 1, 4, 0 };
        }
        else
            return 0.0f;

        // Finally add the glyphs:
        int[] positions = mKey < 0 ? flats : sharps;
        for (int i = 0; i < Math.abs(mKey); i++) {

            // Add the glyph:
            StaveGlyph newGlyph = new StaveGlyph(keyString, x, - (mTextSize * positions[i]) / 8, width);
            mStaticGlyphs.add(newGlyph);

            // Advance to next glyph:
            x += width;
        }

        return totalWidth;
    }

    private TextPaint mTextPaint;
    private float mTextSize = 100.0f;
    private float mMargin = 0.25f;

    private ArrayList<StaveGlyph> mStaticGlyphs = new ArrayList<StaveGlyph>();
    private float mStaticGlyphsWidth;

    private Paint mLinePaint;

    private boolean mInvalidated = true;
}

/**
 * TODO: document your custom view class.
 */
public class MyView extends View {

    public MyView(Context context) {
        super(context);
        init(context,null, 0);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        mStave = new Stave(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;
        float x = paddingLeft;
        float y = paddingTop + contentHeight / 2;

        mStave.draw(canvas, x, y, contentWidth);
    }

    Stave mStave;

}
