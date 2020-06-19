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
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * TODO: document your custom view class.
 */
class PianoKeys : View {

    /**
     * Ratio between key width and key height.
     *
     * This stretches or narrows the keys if set to a different value. The default is using a
     * usual grand piano ratio.
     */
    private var _keySizeRatio: Float = 15.0f / 2.3f
    var keySizeRatio: Float
        get() = _keySizeRatio
        set(value) {
            _keySizeRatio = value
            invalidateMeasurements()
            invalidate()
        }

    /**
     * Enables the drawing of the line between both keyboards.
     */
    private var _drawKeyDivider: Boolean = true
    var drawKeyDivider: Boolean
        get() = _drawKeyDivider
        set(value) {
            _drawKeyDivider = value
            invalidate()
        }

    /**
     * Enables the drawing of the outline around the small keyboard.
     */
    private var _drawSmallKeyOutline: Boolean = true
    var drawSmallKeyOutline: Boolean
        get() = _drawSmallKeyOutline
        set(value) {
            _drawSmallKeyOutline = value
            invalidate()
        }

    /**
     * Controls the visibility of the the small keyboard.
     */
    private var _smallKeysVisible: Boolean = true
    var smallKeysVisible: Boolean
        get() = _smallKeysVisible
        set(value) {
            _smallKeysVisible = value
            invalidateMeasurements()
            invalidate()
        }

    /**
     * Code constructor
     *
     * This constructor can be called by a programmer directly from code to create a new instance
     * of the view.
     */
    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    /**
     * Basic XML constructor.
     *
     * This constructor is called when the view is build via xml.
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    /**
     * Extended XML constructor.
     *
     * This constructor is supposed to be called by superclasses to choose a specific style when
     * the view is build via xml.
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    /**
     * Internal function that initializes the state of the view.
     */
    private fun init(attrs: AttributeSet?, defStyle: Int) {

        // Extract attributes:
        val a = context.obtainStyledAttributes(attrs, R.styleable.PianoKeys, defStyle, 0)
        _keySizeRatio = a.getFloat(R.styleable.PianoKeys_keySizeRatio, _keySizeRatio)
        _smallKeysVisible = a.getBoolean(R.styleable.PianoKeys_smallKeysVisible, _smallKeysVisible)
        _drawKeyDivider = a.getBoolean(R.styleable.PianoKeys_drawKeyDivider, _drawKeyDivider)
        _drawSmallKeyOutline = a.getBoolean(R.styleable.PianoKeys_drawSmallKeyOutline, _drawSmallKeyOutline)
        a.recycle()

        // Create all required painters:
        linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linePaintShade = Paint(Paint.ANTI_ALIAS_FLAG)
        whitePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        whitePaintShade = Paint(Paint.ANTI_ALIAS_FLAG)
        blackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        blackPaintShade = Paint(Paint.ANTI_ALIAS_FLAG)
        keyDownPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        keyDownPaintShade = Paint(Paint.ANTI_ALIAS_FLAG)

        linePaint!!.color = Color.BLACK
        linePaintShade!!.color = Color.DKGRAY
        whitePaint!!.color = Color.WHITE
        whitePaintShade!!.color = Color.LTGRAY
        blackPaint!!.color = Color.BLACK
        blackPaintShade!!.color = Color.DKGRAY
        keyDownPaint!!.color = Color.RED
        keyDownPaintShade!!.color = Color.RED

        // Create keys:
        smallKeys = arrayListOf<KeyRect>()
        for (i in 0..87)
            smallKeys.add(KeyRect(RectF(0.0f, 0.0f, 0.0f, 0.0f), false, i + 21))

        // Update measurements:
        invalidateMeasurements()
    }

    /**
     * Internal helper function to update all sizes and measurements of the keyboard(s).
     */
    private fun invalidateMeasurements() {

        // Update small keys:
        updateSmallKeysMeasurements()
    }

    /**
     * Update the dimensions of the small key area and the keys inside the area.
     *
     * The area of the small keyboard defines the available space for the big keys. The width of
     * area is divided into 52 white keys and the height is determined by the key width scaled by
     * the keySizeRatio property value.
     */
    private fun updateSmallKeysMeasurements() {

        // Update the area of the small keyboard:
        smallKeysRect.left = paddingLeft.toFloat();
        smallKeysRect.top = paddingTop.toFloat()
        smallKeysRect.right = width - paddingRight.toFloat()
        val whiteKeyWidth = smallKeysRect.width() / 52.0f        // 52 white keys + 36 black keys = 88 keys
        val octaveWidth = smallKeysRect.width() * 7.0f / 52.0f   // 7 white keys in an octave
        val octaveHeight = whiteKeyWidth * _keySizeRatio
        smallKeysRect.bottom = paddingTop + octaveHeight

        // If we are not visible then set rect to zero height:
        if (!_smallKeysVisible) {
            smallKeysRect.bottom = smallKeysRect.top
            return
        }

        // Loop from the lowest A to the highest C:
        var octaveStart = smallKeysRect.left - 5.0f * whiteKeyWidth
        for (key in smallKeys) {

            // Get key to draw:
            val k = keyLayout[key.noteNumber % 12]

            // Map coordinates into current octave:
            key.r.left   = octaveStart + k.r.left * octaveWidth
            key.r.right  = octaveStart + k.r.right * octaveWidth
            key.r.top    = smallKeysRect.top + k.r.top * octaveHeight

            key.r.bottom = smallKeysRect.top + k.r.bottom * octaveHeight
            key.isBlack = k.isBlack

            // Switch to next octave?
            if (key.noteNumber % 12 == 0)
                octaveStart += octaveWidth
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidateMeasurements()
    }

    override fun onDraw(canvas: Canvas) {

        // Base class fills the background with the styled background color:
        super.onDraw(canvas)

        // Draw the keyboards:
        drawSmallKeyboard(canvas)
    }

    private fun drawSmallKeyboard(canvas: Canvas) {

        // Don't draw if we are not visible at all:
        if (!_smallKeysVisible)
            return

        // Fill background:
        canvas.drawRect(smallKeysRect, whitePaint!!)

        // Border lines around the small keyboard:
        if (_drawSmallKeyOutline) {
            canvas.drawLine(smallKeysRect.left, smallKeysRect.top, smallKeysRect.left, smallKeysRect.bottom, linePaint!!)
            canvas.drawLine(smallKeysRect.right, smallKeysRect.top, smallKeysRect.right, smallKeysRect.bottom, linePaint!!)
            canvas.drawLine(smallKeysRect.left, smallKeysRect.top, smallKeysRect.right, smallKeysRect.top, linePaint!!)
        }

        // Divider line between the keyboards:
        if (_drawKeyDivider) {
            canvas.drawLine(smallKeysRect.left, smallKeysRect.bottom, smallKeysRect.right, smallKeysRect.bottom, linePaint!!)
        }

        // Draw keys:
        for (key in smallKeys) {

            // Draw the key:
            if (key.isBlack) {
                canvas.drawRect(key.r.left, key.r.top, key.r.right + 1, key.r.bottom + 1, blackPaint!!)
            } else {
                canvas.drawLine(key.r.right, key.r.top, key.r.right, key.r.bottom, linePaint!!)
            }
        }
    }

    private var linePaint: Paint? = null
    private var linePaintShade: Paint? = null
    private var whitePaint: Paint? = null
    private var whitePaintShade: Paint? = null
    private var blackPaint: Paint? = null
    private var blackPaintShade: Paint? = null
    private var keyDownPaint: Paint? = null
    private var keyDownPaintShade: Paint? = null

    /**
     * Represents one key of the virtual keyboard.
     */
    internal class KeyRect (

        /**
         * Position and size of this key inside the view.
         */
        var r: RectF,

        /**
         * Is this a black key?
         */
        var isBlack: Boolean,

        /**
         * MIDI note number of this key.
         */
        var noteNumber: Int
    ) { }

    /**
     * Normalized (0..1) coordinates and sizes for a single octave's keys.
     *
     * This is the blueprint for all key drawing functions.
     */
    private val keyLayout = arrayListOf<KeyRect>(
        KeyRect(RectF(0.000000f, 0.0f, 0.142857f, 1.0f),      false, 0),  // C
        KeyRect(RectF(0.085714f, 0.0f, 0.171429f, 0.666667f), true,  1),  // C#
        KeyRect(RectF(0.142857f, 0.0f, 0.285714f, 1.0f),      false, 2),  // D
        KeyRect(RectF(0.257143f, 0.0f, 0.342857f, 0.666667f), true,  3),  // D#
        KeyRect(RectF(0.285714f, 0.0f, 0.428571f, 1.0f),      false, 4),  // E
        KeyRect(RectF(0.428571f, 0.0f, 0.571429f, 1.0f),      false, 5),  // F
        KeyRect(RectF(0.510204f, 0.0f, 0.591837f, 0.666667f), true,  6),  // F#
        KeyRect(RectF(0.571429f, 0.0f, 0.714286f, 1.0f),      false, 7),  // G
        KeyRect(RectF(0.673469f, 0.0f, 0.755102f, 0.666667f), true,  8),  // G#
        KeyRect(RectF(0.714286f, 0.0f, 0.857143f, 1.0f),      false, 9),  // A
        KeyRect(RectF(0.836735f, 0.0f, 0.918368f, 0.666667f), true,  10), // Bb
        KeyRect(RectF(0.857143f, 0.0f, 1.000000f, 1.0f),      false, 11)) // B

    /**
     * Local coordinates of the small keyboard keys.
     */
    private var smallKeys = ArrayList<KeyRect>(88);

    /**
     * The enclosing rectangle of the small key's area.
     */
    private var smallKeysRect = RectF()
}
