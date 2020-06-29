/*
 * Copyright (c) 2020 by Rolf Meyerhoff <rm@matrix44.de>
 *
 * License:
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,  either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; see
 * the file COPYING. If not, see http://www.gnu.org/licenses/ or write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package de.matrix44.musictoolbox

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.floor

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
        /**
         * The glyph to draw.
         */
        val glyph: String,

        /**
         * Relative position of this glyph inside the stave, x component.
         */
        val x: Float,

        /**
         * Relative position of this glyph inside the stave, w component.
         */
        val y: Float,

        /**
         * Width of this glyph as reported by measureText().
         */
        val width: Float
    ) {

        /**
         * Draw this glyph.
         *
         * @param canvas The target canvas to draw on.
         * @param staveX Stave X position.
         * @param staveY Stave Y position.
         * @param painter The painter to use for drawing.
         */
        fun draw(canvas: Canvas, staveX: Float, staveY: Float, painter: TextPaint) {
            canvas.drawText(glyph, staveX + x, staveY + y, painter)
        }
    }

    internal class Stave {

        private var glyphSize = 100.0f
        private var glyphMargin = 0.25f

        private var _clefVisible = true
        @Suppress("unused")
        var clefVisible: Boolean
            get() = _clefVisible
            set(value) {
                _clefVisible = value
                invalidate()
            }

        internal enum class Clef {
            G, F, C, N
        }

        private val mClef = Clef.G
        private val mClefOctave = 0

        private var _keyVisible = true
        @Suppress("unused")
        var keyVisible: Boolean
            get() = _keyVisible
            set(value) {
                _keyVisible = value
                invalidate()
            }

        private val key = 2

        private var _timeSignatureVisible = true
        @Suppress("unused")
        var timeSignatureVisible: Boolean
            get() = _timeSignatureVisible
            set(value) {
                _timeSignatureVisible = value
                invalidate()
            }

        private val upperTimeSignature = 4
        private val lowerTimeSignature = 4
        private val useTimeSymbols = true

        constructor(context: Context?, view: View) {

            // Create text painter for the score symbols:
            glyphPaint.flags = Paint.ANTI_ALIAS_FLAG
            glyphPaint.textAlign = Paint.Align.LEFT
            glyphPaint.textSize = glyphSize
            if (!view.isInEditMode)
                glyphPaint.typeface = Typeface.createFromAsset(context!!.assets, "fonts/Bravura.otf")

            // Create normal painter for the stave lines:
            linePaint.flags = Paint.ANTI_ALIAS_FLAG
            linePaint.strokeWidth = 2f
        }

        fun draw(canvas: Canvas, x: Float, y: Float, width: Float) {

            // Update layout if needed:
            if (invalidated)
                updateLayout()

            // Start with the lines:
            drawStaveLines(canvas, x, y, width)

            // Draw static glyphs:
            for (glyph in staticGlyphs)
                glyph.draw(canvas, x, y, glyphPaint)
        }

        private fun drawStaveLines(canvas: Canvas, x: Float, y: Float, width: Float) {

            // All stave lines combined are "textSize" pixels high so the distance between two stave
            // lines is 1/4 of that:
            val lineHeight = glyphSize / 4

            // Draw the lines:
            for (i in 0..4) {
                val iy = (y - i * lineHeight).toInt() + 0.5f
                canvas.drawLine(x, iy, x + width, iy, linePaint)
            }
        }

        private fun invalidate() {

            // Flag invalid:
            invalidated = false
        }

        private fun updateLayout() {

            // Start fresh:
            staticGlyphs.clear()

            // First up is a bit of empty space:
            val margin = glyphSize * glyphMargin
            var x = margin

            // Add the clef and all the other stuff at the start:
            if (_clefVisible) {
                x += updateClef(x)
                x += margin
            }
            if (_keyVisible) {
                x += updateKey(x)
                x += margin
            }
            if (_timeSignatureVisible) {
                x += updateTimeSignature(x)
                x += margin
            }

            // Save width for later:
            staticGlyphsWidth = x

            // Flag valid:
            invalidated = true
        }

        private fun updateClef(x: Float): Float {
            var textString = ""
            var trans = 0

            // Extract glyph for the clef:
            if (mClef == Clef.G) {
                trans = 2
                textString = if (mClefOctave == -2)
                                 "\uE051"
                             else if (mClefOctave == -1)
                                 "\uE052"
                             else if (mClefOctave == 1)
                                 "\uE053"
                             else if (mClefOctave == 2)
                                 "\uE053"
                             else
                                 "\uE050"
            } else if (mClef == Clef.F) {
                trans = 6
                textString = if (mClefOctave == -2)
                                 "\uE063"
                             else if (mClefOctave == -1)
                                 "\uE064"
                             else if (mClefOctave == 1)
                                 "\uE065"
                             else if (mClefOctave == 2)
                                 "\uE066"
                             else
                                 "\uE062"
            } else if (mClef == Clef.C) {
                trans = 4
                textString = if (mClefOctave == -1) "\uE05D" else "\uE05C"
            } else if (mClef == Clef.N) {
                trans = 4
                textString = "\uE069"
            }
            val offset = glyphSize * trans / 8
            val width = glyphPaint.measureText(textString)
            val newGlyph = StaveGlyph(textString, x, -offset, width)
            staticGlyphs.add(newGlyph)
            return width
        }

        private fun updateTimeSignature(x: Float): Float {

            // Use symbols instead of numbers?
            if (useTimeSymbols && upperTimeSignature == lowerTimeSignature && (upperTimeSignature == 2 || upperTimeSignature == 4)) {

                // Find symbol:
                var textString = ""
                textString = if (upperTimeSignature == 4) "\uE08A" else "\uE08B"
                val width = glyphPaint.measureText(textString)
                val newGlyph = StaveGlyph(textString, x, -glyphSize / 2, width)
                staticGlyphs.add(newGlyph)
                return width
            }

            // The unicode characters for the time signature numbers (0-9):
            val chars = arrayOf(
                "\uE080", "\uE081", "\uE082", "\uE083", "\uE084",
                "\uE085", "\uE086", "\uE087", "\uE088", "\uE089"
            )

            // Convert upper part to a string:
            var upperText = ""
            var upper = upperTimeSignature.toFloat()
            while (upper >= 0.5f) {
                upperText = chars[upper.toInt() % 10] + upperText
                upper = floor(upper * 0.1f)
            }

            // Convert lower part to a string:
            var lowerText = ""
            var lower = lowerTimeSignature.toFloat()
            while (lower >= 0.5f) {
                lowerText = chars[lower.toInt() % 10] + lowerText
                lower = floor(lower * 0.1f)
            }

            // Measure texts:
            val upperSize = glyphPaint.measureText(upperText)
            val lowerSize = glyphPaint.measureText(lowerText)

            // Center text:
            var upperX = 0f
            var lowerX = 0f
            if (upperSize > lowerSize) lowerX = (upperSize - lowerSize) / 2 else upperX =
                (lowerSize - upperSize) / 2

            // Add the glyphs:
            val upperGlyph =
                StaveGlyph(upperText, x + upperX, -(glyphSize * 3) / 4, upperSize)
            staticGlyphs.add(upperGlyph)
            val lowerGlyph =
                StaveGlyph(lowerText, x + lowerX, -glyphSize / 4, lowerSize)
            staticGlyphs.add(lowerGlyph)

            // Return text width:
            return if (upperSize > lowerSize) upperSize else lowerSize
        }

        private fun updateKey(x: Float): Float {

            // The C key has no sharps or flats:
            if (key == 0)
                return 0.0f

            // Get glyph to draw and calc sizes:
            val keyString = if (key < 0) "\u266D" else "\u266F"
            val width = glyphPaint.measureText(keyString)
            val totalWidth = abs(key) * width

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
            val positions: IntArray = if (key < 0) flats else sharps
            var glyphX = x
            for (i in 0 until abs(key)) {

                // Add the glyph:
                val newGlyph = StaveGlyph(keyString, glyphX, -(glyphSize * positions[i]) / 8, width)
                staticGlyphs.add(newGlyph)

                // Advance to next glyph:
                glyphX += width
            }
            return totalWidth
        }

        private val glyphPaint: TextPaint = TextPaint()
        private val linePaint: Paint = Paint()
        private val staticGlyphs = ArrayList<StaveGlyph>()
        private var staticGlyphsWidth = 0.0f
        private var invalidated = true
    }

    private var textPaint: TextPaint? = null
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f

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
        val a = context.obtainStyledAttributes(attrs, R.styleable.ScoreView, defStyle, 0)
        //...
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
