package de.matrix44.musictoolbox

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import de.matrix44.musictoolbox.ScoreView.Stave





/**
 * TODO: document your custom view class.
 * https://medium.com/@dbottillo/creating-android-custom-view-6d8d46122cf5
 * https://developer.android.com/guide/topics/ui/custom-components
 * https://medium.com/@rygel/stop-repeating-yourself-and-create-custom-views-on-android-with-kotlin-f5b3fc581c0e
 *
 * https://medium.com/@ssaurel/creating-a-virtual-piano-for-android-b6d3ac05d961
 */
class ScoreView : View {

    /**
     * This class represents a single glyph on the stave.
     *
     * This glyph is just a small container for a single character along with it's position on the
     * stave lines.
     */
    internal class StaveGlyph(
        private val glyph: String,
        private val x: Float,
        private val y: Float,
        private val width: Float
    ) {

        /**
         * Calculate the width of this glyph when painted.
         *
         *
         */
        fun measure(painter: TextPaint): Float {
            return painter.measureText(glyph)
        }

        fun draw(canvas: Canvas, x: Float, y: Float, painter: TextPaint?) {
            canvas.drawText(glyph, this.x + x, this.y + y, painter!!)
        }
    }

    internal class Stave(context: Context, view: View) {

        fun draw(
            canvas: Canvas,
            x: Float,
            y: Float,
            width: Float
        ) {

            // Update layout if needed:
            if (mInvalidated) updateLayout()

            // Start with the lines:
            drawStaveLines(canvas, x, y, width)

            // Draw static glyphs:
            for (glyph in mStaticGlyphs) {
                glyph.draw(canvas, x, y, mTextPaint)
            }
        }

        private fun drawStaveLines(
            canvas: Canvas,
            x: Float,
            y: Float,
            width: Float
        ) {

            // All stave lines combined are "textSize" pixels high so the distance between two stave
            // lines is 1/4 of that:
            val lineHeight = mTextSize / 4

            // Draw the lines:
            for (i in 0..4) {
                val iy = (y - i * lineHeight).toInt() + 0.5f
                canvas.drawLine(x, iy, x + width, iy, mLinePaint)
            }
        }

        private fun updateLayout() {

            // Start fresh:
            mStaticGlyphs.clear()

            // First up is a bit of empty space:
            val margin = mTextSize * mMargin
            var x = margin

            // Add the clef:
            x += addClef(x)
            x += margin
            x += addKey(x)
            x += margin
            x += addTimeSignature(x)
            x += margin

            // Save width for later:
            mStaticGlyphsWidth = x

            // Flag valid:
            mInvalidated = false
        }

        private fun invalidate() {

            // Flag invalid:
            mInvalidated = false
        }

        internal enum class Clef {
            G, F, C, N
        }

        private val mClef = Clef.G
        private val mClefOctave = 0
        private fun addClef(x: Float): Float {
            var textString = ""
            var trans = 0

            // Extract glyph for the clef:
            if (mClef == Clef.G) {
                trans = 2
                textString =
                    if (mClefOctave == -2) "\uE051" else if (mClefOctave == -1) "\uE052" else if (mClefOctave == 1) "\uE053" else if (mClefOctave == 2) "\uE053" else "\uE050"
            } else if (mClef == Clef.F) {
                trans = 6
                textString =
                    if (mClefOctave == -2) "\uE063" else if (mClefOctave == -1) "\uE064" else if (mClefOctave == 1) "\uE065" else if (mClefOctave == 2) "\uE066" else "\uE062"
            } else if (mClef == Clef.C) {
                trans = 4
                textString = if (mClefOctave == -1) "\uE05D" else "\uE05C"
            } else if (mClef == Clef.N) {
                trans = 4
                textString = "\uE069"
            }
            val offset = mTextSize * trans / 8
            val width = mTextPaint.measureText(textString)
            val newGlyph = StaveGlyph(textString, x, -offset, width)
            mStaticGlyphs.add(newGlyph)
            return width
        }

        private val mUpperTimeSignature = 4
        private val mLowerTimeSignature = 4
        private val mUseTimeSymbols = true
        private fun addTimeSignature(x: Float): Float {

            // Use symbols instead of numbers?
            if (mUseTimeSymbols && mUpperTimeSignature == mLowerTimeSignature && (mUpperTimeSignature == 2 || mUpperTimeSignature == 4)) {

                // Find symbol:
                var textString = ""
                textString = if (mUpperTimeSignature == 4) "\uE08A" else "\uE08B"
                val width = mTextPaint.measureText(textString)
                val newGlyph = StaveGlyph(textString, x, -mTextSize / 2, width)
                mStaticGlyphs.add(newGlyph)
                return width
            }

            // The unicode characters for the time signature numbers (0-9):
            val chars = arrayOf(
                "\uE080", "\uE081", "\uE082", "\uE083", "\uE084",
                "\uE085", "\uE086", "\uE087", "\uE088", "\uE089"
            )

            // Convert upper part to a string:
            var upperText = ""
            var upper = mUpperTimeSignature.toFloat()
            while (upper >= 0.5) {
                upperText = chars[upper.toInt() % 10] + upperText
                upper = Math.floor(upper / 10.toDouble()).toFloat()
            }

            // Convert lower part to a string:
            var lowerText = ""
            var lower = mLowerTimeSignature.toFloat()
            while (lower >= 0.5) {
                lowerText = chars[lower.toInt() % 10] + lowerText
                lower = Math.floor(lower / 10.toDouble()).toFloat()
            }

            // Measure texts:
            val upperSize = mTextPaint.measureText(upperText)
            val lowerSize = mTextPaint.measureText(lowerText)

            // Center text:
            var upperX = 0f
            var lowerX = 0f
            if (upperSize > lowerSize) lowerX = (upperSize - lowerSize) / 2 else upperX =
                (lowerSize - upperSize) / 2

            // Add the glyphs:
            val upperGlyph =
                StaveGlyph(upperText, x + upperX, -(mTextSize * 3) / 4, upperSize)
            mStaticGlyphs.add(upperGlyph)
            val lowerGlyph =
                StaveGlyph(lowerText, x + lowerX, -mTextSize / 4, lowerSize)
            mStaticGlyphs.add(lowerGlyph)

            // Return text width:
            return if (upperSize > lowerSize) upperSize else lowerSize
        }

        private val mKey = 2
        private fun addKey(x: Float): Float {

            // The C key has no sharps or flats:
            var x = x
            if (mKey == 0) return 0.0f

            // Get glyph to draw and calc sizes:
            val keyString = if (mKey < 0) "\u266D" else "\u266F"
            val width = mTextPaint.measureText(keyString)
            val totalWidth = Math.abs(mKey) * width

            // Get sharp and flat positions:
            var sharps: IntArray? = null
            var flats: IntArray? = null
            if (mClef == Clef.G) {
                sharps = intArrayOf(8, 5, 9, 6, 3, 7, 4)
                flats = intArrayOf(4, 7, 3, 6, 2, 5, 1)
            } else if (mClef == Clef.F) {
                sharps = intArrayOf(6, 3, 7, 4, 1, 5, 3)
                flats = intArrayOf(2, 5, 1, 4, 0, 3, -1)
            } else if (mClef == Clef.C) {
                sharps = intArrayOf(7, 4, 8, 5, 2, 6, 3)
                flats = intArrayOf(3, 6, 2, 5, 1, 4, 0)
            } else return 0.0f

            // Finally add the glyphs:
            val positions: IntArray = if (mKey < 0) flats else sharps
            for (i in 0 until Math.abs(mKey)) {

                // Add the glyph:
                val newGlyph =
                    StaveGlyph(keyString, x, -(mTextSize * positions[i]) / 8, width)
                mStaticGlyphs.add(newGlyph)

                // Advance to next glyph:
                x += width
            }
            return totalWidth
        }

        private val mTextPaint: TextPaint
        private val mTextSize = 100.0f
        private val mMargin = 0.25f
        private val mStaticGlyphs = ArrayList<StaveGlyph>()
        private var mStaticGlyphsWidth = 0f
        private val mLinePaint: Paint
        private var mInvalidated = true

        init {

            // Create text painter for the score symbols:
            mTextPaint = TextPaint()
            mTextPaint.flags = Paint.ANTI_ALIAS_FLAG
            mTextPaint.textAlign = Paint.Align.LEFT
            if (!view.isInEditMode)
                mTextPaint.typeface = Typeface.createFromAsset(context?.assets, "fonts/Bravura.otf")
            mTextPaint.textSize = mTextSize

            // Create normal painter for the stave lines:
            mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mLinePaint.strokeWidth = 2f
        }
    }

    private var _exampleString: String? = null // TODO: use a default from R.string...
    private var _exampleColor: Int = Color.RED // TODO: use a default from R.color...
    private var _exampleDimension: Float = 0f // TODO: use a default from R.dimen...

    private var textPaint: TextPaint? = null
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f

    /**
     * The text to draw
     */
    var exampleString2: String?
        get() = _exampleString
        set(value) {
            _exampleString = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * The font color
     */
    var exampleColor2: Int
        get() = _exampleColor
        set(value) {
            _exampleColor = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this dimension is the font size.
     */
    var exampleDimension2: Float
        get() = _exampleDimension
        set(value) {
            _exampleDimension = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this drawable is drawn above the text.
     */
    var exampleDrawable2: Drawable? = null

    private var mStave: Stave? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ScoreView, defStyle, 0
        )

        _exampleString = a.getString(
            R.styleable.ScoreView_exampleString2
        )
        _exampleColor = a.getColor(
            R.styleable.ScoreView_exampleColor2,
            exampleColor2
        )
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        _exampleDimension = a.getDimension(
            R.styleable.ScoreView_exampleDimension2,
            exampleDimension2
        )

        if (a.hasValue(R.styleable.ScoreView_exampleDrawable2)) {
            exampleDrawable2 = a.getDrawable(
                R.styleable.ScoreView_exampleDrawable2
            )
            exampleDrawable2?.callback = this
        }

        a.recycle()

        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()

        mStave = Stave(context, this);
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint?.let {
            it.textSize = exampleDimension2
            it.color = exampleColor2
            textWidth = it.measureText("Hallo")
            textHeight = it.fontMetrics.bottom
        }
    }

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas)

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        exampleString2?.let {
            // Draw the text.
            canvas.drawText(
                it,
                paddingLeft + (contentWidth - textWidth) / 2,
                paddingTop + (contentHeight + textHeight) / 2,
                textPaint!!
            )
        }
//
//        // Draw the example drawable on top of the text.
//        exampleDrawable?.let {
//            it.setBounds(
//                paddingLeft, paddingTop,
//                paddingLeft + contentWidth, paddingTop + contentHeight
//            )
//            it.draw(canvas)
//        }
        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.

        // TODO: consider storing these as member variables to reduce

        val x = paddingLeft.toFloat()
        val y = paddingTop + contentHeight / 2.toFloat()

        mStave!!.draw(canvas, x, y, contentWidth.toFloat())
    }
}
