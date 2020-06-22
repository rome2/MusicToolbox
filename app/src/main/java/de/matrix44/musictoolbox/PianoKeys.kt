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
     * Enables the drawing of the outline around the big keyboard.
     */
    private var _drawBigKeyOutline: Boolean = false
    var drawBigKeyOutline: Boolean
        get() = _drawBigKeyOutline
        set(value) {
            _drawBigKeyOutline = value
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
     * This color is used for all lines.
     */
    private var _lineColor: Int = Color.BLACK
    var lineColor: Int
    get() = _lineColor
        set(value) {
            _lineColor = value
            linePaint?.color = _lineColor
            invalidate()
        }

    /**
     * This color is used for all lines that are in the shade of the small keyboard area.
     */
    private var _lineShadeColor: Int = Color.DKGRAY
    var lineShadeColor: Int
        get() = _lineShadeColor
        set(value) {
            _lineShadeColor = value
            lineShadePaint?.color = _lineShadeColor
            invalidate()
        }

    /**
     * This color is used for all white keys.
     */
    private var _whiteKeyColor: Int = Color.WHITE
    var whiteKeyColor: Int
        get() = _whiteKeyColor
        set(value) {
            _whiteKeyColor = value
            whiteKeyPaint?.color = _whiteKeyColor
            invalidate()
        }

    /**
     * This color is used for all white keys that are in the shade of the small keyboard area.
     */
    private var _whiteKeyShadeColor: Int = Color.LTGRAY
    var whiteKeyShadeColor: Int
        get() = _whiteKeyShadeColor
        set(value) {
            _whiteKeyShadeColor = value
            whiteKeyShadePaint?.color = _whiteKeyShadeColor
            invalidate()
        }

    /**
     * This color is used for all black keys.
     */
    private var _blackKeyColor: Int = Color.BLACK
    var blackKeyColor: Int
        get() = _blackKeyColor
        set(value) {
            _blackKeyColor = value
            blackKeyPaint?.color = _blackKeyColor
            invalidate()
        }

    /**
     * This color is used for all black keys that are in the shade of the small keyboard area.
     */
    private var _blackKeyShadeColor: Int = Color.DKGRAY
    var blackKeyShadeColor: Int
        get() = _blackKeyShadeColor
        set(value) {
            _blackKeyShadeColor = value
            blackKeyShadePaint?.color = _blackKeyShadeColor
            invalidate()
        }

    /**
     * This color is used for all pressed keys,
     */
    private var _keyDownKeyColor: Int = Color.RED
    var keyDownKeyColor: Int
        get() = _keyDownKeyColor
        set(value) {
            _keyDownKeyColor = value
            keyDownKeyPaint?.color = _keyDownKeyColor
            invalidate()
        }

    /**
     * This color is used for all pressed keys that are in the shade of the small keyboard area.
     */
    private var _keyDownKeyShadeColor: Int = Color.rgb(128, 0, 0)
    var keyDownKeyShadeColor: Int
        get() = _keyDownKeyShadeColor
        set(value) {
            _keyDownKeyShadeColor = value
            keyDownKeyShadePaint?.color = _keyDownKeyShadeColor
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
        _keySizeRatio         = a.getFloat(R.styleable.PianoKeys_keySizeRatio, _keySizeRatio)
        _smallKeysVisible     = a.getBoolean(R.styleable.PianoKeys_smallKeysVisible,    _smallKeysVisible)
        _drawKeyDivider       = a.getBoolean(R.styleable.PianoKeys_drawKeyDivider,      _drawKeyDivider)
        _drawSmallKeyOutline  = a.getBoolean(R.styleable.PianoKeys_drawSmallKeyOutline, _drawSmallKeyOutline)
        _drawBigKeyOutline    = a.getBoolean(R.styleable.PianoKeys_drawBigKeyOutline,   _drawBigKeyOutline)
        _lineColor            = a.getColor(R.styleable.PianoKeys_lineColor,            _lineColor)
        _lineShadeColor       = a.getColor(R.styleable.PianoKeys_lineShadeColor,       _lineShadeColor)
        _whiteKeyColor        = a.getColor(R.styleable.PianoKeys_whiteKeyColor,        _whiteKeyColor)
        _whiteKeyShadeColor   = a.getColor(R.styleable.PianoKeys_whiteKeyShadeColor,   _whiteKeyShadeColor)
        _blackKeyColor        = a.getColor(R.styleable.PianoKeys_blackKeyColor,        _blackKeyColor)
        _blackKeyShadeColor   = a.getColor(R.styleable.PianoKeys_blackKeyShadeColor,   _blackKeyShadeColor)
        _keyDownKeyColor      = a.getColor(R.styleable.PianoKeys_keyDownKeyColor,      _keyDownKeyColor)
        _keyDownKeyShadeColor = a.getColor(R.styleable.PianoKeys_keyDownKeyShadeColor, _keyDownKeyShadeColor)
        a.recycle()

        // Create all of our painters:
        linePaint            = Paint(Paint.ANTI_ALIAS_FLAG)
        lineShadePaint       = Paint(Paint.ANTI_ALIAS_FLAG)
        whiteKeyPaint        = Paint(Paint.ANTI_ALIAS_FLAG)
        whiteKeyShadePaint   = Paint(Paint.ANTI_ALIAS_FLAG)
        blackKeyPaint        = Paint(Paint.ANTI_ALIAS_FLAG)
        blackKeyShadePaint   = Paint(Paint.ANTI_ALIAS_FLAG)
        keyDownKeyPaint      = Paint(Paint.ANTI_ALIAS_FLAG)
        keyDownKeyShadePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Set initial colors:
        linePaint!!.color            = _lineColor
        lineShadePaint!!.color       = _lineShadeColor
        whiteKeyPaint!!.color        = _whiteKeyColor
        whiteKeyShadePaint!!.color   = _whiteKeyShadeColor
        blackKeyPaint!!.color        = _blackKeyColor
        blackKeyShadePaint!!.color   = _blackKeyShadeColor
        keyDownKeyPaint!!.color      = _keyDownKeyColor
        keyDownKeyShadePaint!!.color = _keyDownKeyShadeColor

        linePaint!!.strokeWidth = 2f

        // Create keys:
        smallKeys = arrayListOf<KeyRect>()
        for (i in 21..108) {
            smallKeys.add(KeyRect(RectF(0.0f, 0.0f, 0.0f, 0.0f), false, i))
            bigKeys.add(KeyRect(RectF(0.0f, 0.0f, 0.0f, 0.0f), false, i))
        }

        // Update measurements:
        invalidateMeasurements()
    }

    /**
     * Internal helper function to update all sizes and measurements of the keyboard(s).
     */
    private fun invalidateMeasurements() {

        // Update keys:
        updateSmallKeysMeasurements()
        updateBigKeysMeasurements()
    }

    /**
     * Update the dimensions of the small key area and the keys inside the area.
     *
     * The area of the small keyboard defines the available space for the big keys. The width of
     * area is divided into 52 white keys and the height is determined by the key width scaled by
     * the keySizeRatio property value.
     */
    private fun updateSmallKeysMeasurements() {

        // Count white keys:
        var whiteKeyCnt = 0
        for (key in smallKeys) {
            if (!keyLayout[key.noteNumber % 12].isBlack)
                whiteKeyCnt++
        }

        // Update the area of the small keyboard:
        smallKeysRect.left   = paddingLeft.toFloat()
        smallKeysRect.top    = paddingTop.toFloat()
        smallKeysRect.right  = width - paddingRight.toFloat()
        val whiteKeyWidth    = smallKeysRect.width() /whiteKeyCnt          // 52 white keys + 36 black keys = 88 keys
        val octaveWidth      = smallKeysRect.width() * 7.0f / whiteKeyCnt   // 7 white keys in an octave
        val octaveHeight     = whiteKeyWidth * _keySizeRatio
        smallKeysRect.bottom = paddingTop + octaveHeight

        // If we are not visible then set rect to zero height:
        if (!_smallKeysVisible) {
            smallKeysRect.bottom = smallKeysRect.top
            return
        }

        // Calc where the starting octave begins in relation to the rect:
        var octaveStart = smallKeysRect.left - keyLayout[smallKeys[0].noteNumber % 12].r.left * octaveWidth

        // Loop through all notes:
        for (key in smallKeys) {

            // Get key to draw:
            val k = keyLayout[key.noteNumber % 12]

            // Map coordinates into current octave:
            key.r.left   = octaveStart + k.r.left * octaveWidth
            key.r.right  = octaveStart + k.r.right * octaveWidth
            key.r.top    = smallKeysRect.top + k.r.top * octaveHeight
            key.r.bottom = smallKeysRect.top + k.r.bottom * octaveHeight
            key.isBlack  = k.isBlack

            // Switch to next octave?
            if ((key.noteNumber + 1) % 12 == 0)
                octaveStart += octaveWidth
        }
    }

    /**
     * Update the dimensions of the big key area and the keys inside the area.
     *
     * The area of the big keyboard uses up the remaining space after the small keyboard area
     * has been calculated.
     */
    private fun updateBigKeysMeasurements() {

        // Update the area of the big keyboard to fill up the view area:
        bigKeysRect.left   = paddingLeft.toFloat()
        bigKeysRect.top    = smallKeysRect.bottom
        bigKeysRect.right  = width - paddingRight.toFloat()
        bigKeysRect.bottom = height - paddingBottom.toFloat()

        val octaveHeight  = bigKeysRect.height()
        val whiteKeyWidth = octaveHeight / _keySizeRatio
        val octaveWidth   = whiteKeyWidth * 7.0f

        // Calc where the starting octave begins in relation to the rect:
        var octaveStart = bigKeysRect.left - keyLayout[smallKeys[0].noteNumber % 12].r.left * octaveWidth

        // Loop through all notes:
        for (key in bigKeys) {

            // Get key to draw:
            val k = keyLayout[key.noteNumber % 12]

            // Map coordinates into current octave:
            key.r.left   = octaveStart + k.r.left * octaveWidth
            key.r.right  = octaveStart + k.r.right * octaveWidth
            key.r.top    = bigKeysRect.top + k.r.top * octaveHeight
            key.r.bottom = bigKeysRect.top + k.r.bottom * octaveHeight
            key.isBlack  = k.isBlack

            // Switch to next octave?
            if ((key.noteNumber + 1) % 12 == 0)
                octaveStart += octaveWidth
        }
    }

    /**
     * Called when the size of the view has changed.
     *
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Resize our components:
        invalidateMeasurements()
    }

    /**
     * Called when the view should render its content.
     *
     * @param canvas The canvas on which the view should be drawn.
     */
    override fun onDraw(canvas: Canvas) {

        // Base class fills the background with the styled background color:
        super.onDraw(canvas)

        // Draw the keyboards:
        drawBigKeyboard(canvas)
        drawSmallKeyboard(canvas)
    }

    /**
     * Draws the small overview keyboard (if visible).
     *
     * @param canvas The canvas on which the keys should be drawn.
     */
    private fun drawSmallKeyboard(canvas: Canvas) {

        // Don't draw if we are not visible at all:
        if (!_smallKeysVisible)
            return

        // Fill background with hidden area:
        canvas.drawRect(smallKeysRect, whiteKeyShadePaint!!)

        // Draw white keys:
        var firstKey = true
        for (key in smallKeys) {

            // Get pressed state:
            val isActive = key.noteNumber in lowestVisibleKey..highestVisibleKey

            // Draw active key background:
            if (isActive)
                canvas.drawRect(key.r, whiteKeyPaint!!)

            // Draw the active rect:
            if (key.noteNumber in activeKeys) {
                if (isActive)
                    canvas.drawRect(key.r.left, key.r.top, key.r.right + 1, key.r.bottom + 1, keyDownKeyPaint!!)
                else
                    canvas.drawRect(key.r.left, key.r.top, key.r.right + 1, key.r.bottom + 1, keyDownKeyShadePaint!!)
            }

            // Draw the key divider line:
            if (!key.isBlack && !firstKey) {
                if (!isActive)
                    canvas.drawLine(key.r.left + 0.5f, key.r.top, key.r.left + 0.5f, key.r.bottom, lineShadePaint!!)
                else
                    canvas.drawLine(key.r.left + 0.5f, key.r.top, key.r.left + 0.5f, key.r.bottom, linePaint!!)
            }

            // The first line is not drawn:
            firstKey = false
        }

        // Draw black keys:
        for (key in smallKeys) {

            // Get pressed state:
            val isActive = key.noteNumber in lowestVisibleKey..highestVisibleKey

            // Draw the key:
            if (key.isBlack) {

                // Active keys need a frame:
                if (key.noteNumber in activeKeys) {
                    if (isActive) {
                        canvas.drawRect(key.r.left, key.r.top, key.r.right + 1, key.r.bottom + 1, keyDownKeyPaint!!)
                        canvas.drawLine(key.r.left + 0.5f, key.r.top, key.r.left + 0.5f, key.r.bottom, linePaint!!)
                        canvas.drawLine(key.r.right + 0.5f, key.r.top, key.r.right + 0.5f, key.r.bottom, linePaint!!)
                        canvas.drawLine(key.r.left + 0.5f, key.r.bottom, key.r.right + 0.5f, key.r.bottom, linePaint!!)
                    } else {
                        canvas.drawRect(key.r.left, key.r.top, key.r.right + 1, key.r.bottom + 1, keyDownKeyShadePaint!!)
                        canvas.drawLine(key.r.left + 0.5f, key.r.top, key.r.left + 0.5f, key.r.bottom, lineShadePaint!!)
                        canvas.drawLine(key.r.right + 0.5f, key.r.top, key.r.right + 0.5f, key.r.bottom, lineShadePaint!!)
                        canvas.drawLine(key.r.left + 0.5f, key.r.bottom, key.r.right + 0.5f, key.r.bottom, lineShadePaint!!)
                    }
                }
                else {
                    // Solid rectangle for normal black keys:
                    if (isActive)
                        canvas.drawRect(key.r.left, key.r.top, key.r.right + 1, key.r.bottom + 1, blackKeyPaint!!)
                    else
                        canvas.drawRect(key.r.left, key.r.top, key.r.right + 1, key.r.bottom + 1, blackKeyShadePaint!!)
                }
            }
        }

        // Border top, left and right lines around the small keyboard:
        if (_drawSmallKeyOutline) {
            canvas.drawLine(smallKeysRect.left + 0.5f,  smallKeysRect.top, smallKeysRect.left + 0.5f,  smallKeysRect.bottom, linePaint!!)
            canvas.drawLine(smallKeysRect.right + 0.5f, smallKeysRect.top, smallKeysRect.right + 0.5f, smallKeysRect.bottom, linePaint!!)
            canvas.drawLine(smallKeysRect.left + 0.5f,  smallKeysRect.top, smallKeysRect.right + 0.5f, smallKeysRect.top,    linePaint!!)
        }

        // Divider line between the keyboards:
        if (_drawKeyDivider)
            canvas.drawLine(smallKeysRect.left + 0.5f, smallKeysRect.bottom, smallKeysRect.right + 0.5f, smallKeysRect.bottom, linePaint!!)
    }

    /**
     * Draws the big keyboard.
     *
     * @param canvas The canvas on which the keys should be drawn.
     */
    private fun drawBigKeyboard(canvas: Canvas) {

        // Fill background:
        canvas.drawRect(bigKeysRect, whiteKeyPaint!!)


        // Draw white keys:
        for (key in bigKeys) {

            // Skip black keys:
            if (key.isBlack)
                continue;

            // Is this key to the left of the view?
            if (key.r.right < bigKeysRect.left)
                continue

            // Did we leave the drawing area?
            if (key.r.left > bigKeysRect.right)
                break

            // Draw the key:
            if (key.noteNumber in activeKeys) {

                // Clip key to the viewing area:
                val r = RectF()
                r.setIntersect(key.r, bigKeysRect)

                // Draw it:
                canvas.drawRect(r.left, r.top, r.right + 1, r.bottom + 1, keyDownKeyPaint!!)
            }

            // Draw the line between the keys:
            if (key.r.left - 0.5f > bigKeysRect.left && key.r.left + 0.5f < bigKeysRect.right)
                canvas.drawLine(key.r.left + 0.5f, key.r.top, key.r.left + 0.5f, key.r.bottom, linePaint!!)
        }

        // Draw black keys:
        for (key in bigKeys) {

            // Skip black keys:
            if (!key.isBlack)
                continue;

            // Is this key to the left of the view?
            if (key.r.right < bigKeysRect.left)
                continue

            // Did we leave the drawing area?
            if (key.r.left > bigKeysRect.right)
                break

            // Clip black key to the viewing area:
            val r = RectF()
            r.setIntersect(key.r, bigKeysRect)

            // Draw it:
            if (key.noteNumber in activeKeys)
                canvas.drawRect(r.left, r.top, r.right + 1, r.bottom + 1, keyDownKeyPaint!!)
            else
                canvas.drawRect(r.left, r.top, r.right + 1, r.bottom + 1, blackKeyPaint!!)

            // Add outline if visible:
            if (key.r.left - 0.5f > bigKeysRect.left && key.r.left + 0.5f < bigKeysRect.right)
                canvas.drawLine(r.left + 0.5f, r.top, r.left + 0.5f, r.bottom, linePaint!!)
            if (key.r.right - 0.5f > bigKeysRect.left && key.r.right + 0.5f < bigKeysRect.right)
                canvas.drawLine(r.right + 0.5f, r.top, r.right + 0.5f, r.bottom, linePaint!!)
            canvas.drawLine(r.left + 0.5f, r.bottom, r.right + 0.5f, r.bottom, linePaint!!)
        }

        // Border bottom, left and right lines around the big keyboard:
        if (_drawBigKeyOutline) {
            canvas.drawLine(bigKeysRect.left + 0.5f,  bigKeysRect.top,    bigKeysRect.left + 0.5f,  bigKeysRect.bottom, linePaint!!)
            canvas.drawLine(bigKeysRect.right + 0.5f, bigKeysRect.top,    bigKeysRect.right + 0.5f, bigKeysRect.bottom, linePaint!!)
            canvas.drawLine(bigKeysRect.left + 0.5f,  bigKeysRect.bottom, bigKeysRect.right + 0.5f, bigKeysRect.bottom, linePaint!!)

            // Top line only if needed:
            if (!_smallKeysVisible)
                canvas.drawLine(smallKeysRect.left + 0.5f, smallKeysRect.bottom, smallKeysRect.right + 0.5f, smallKeysRect.bottom, linePaint!!)
        }
    }

    /**
     * Painter for all lines.
     */
    private var linePaint: Paint? = null

    /**
     * Painter for all shaded lines.
     */
    private var lineShadePaint: Paint? = null

    /**
     * Painter for all white keys.
     */
    private var whiteKeyPaint: Paint? = null

    /**
     * Painter for all shaded white keys.
     */
    private var whiteKeyShadePaint: Paint? = null

    /**
     * Painter for all black keys.
     */
    private var blackKeyPaint: Paint? = null

    /**
     * Painter for all shaded black keys.
     */
    private var blackKeyShadePaint: Paint? = null

    /**
     * Painter for all pressed keys.
     */
    private var keyDownKeyPaint: Paint? = null

    /**
     * Painter for all shaded pressed keys.
     */
    private var keyDownKeyShadePaint: Paint? = null

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
        KeyRect(RectF(0.086905f, 0.0f, 0.170238f, 0.666667f), true,  1),  // C#
        KeyRect(RectF(0.142857f, 0.0f, 0.285714f, 1.0f),      false, 2),  // D
        KeyRect(RectF(0.258333f, 0.0f, 0.341667f, 0.666667f), true,  3),  // D#
        KeyRect(RectF(0.285714f, 0.0f, 0.428571f, 1.0f),      false, 4),  // E
        KeyRect(RectF(0.428571f, 0.0f, 0.571429f, 1.0f),      false, 5),  // F
        KeyRect(RectF(0.509354f, 0.0f, 0.592687f, 0.666667f), true,  6),  // F#
        KeyRect(RectF(0.571429f, 0.0f, 0.714286f, 1.0f),      false, 7),  // G
        KeyRect(RectF(0.672619f, 0.0f, 0.755952f, 0.666667f), true,  8),  // G#
        KeyRect(RectF(0.714286f, 0.0f, 0.857143f, 1.0f),      false, 9),  // A
        KeyRect(RectF(0.835885f, 0.0f, 0.919218f, 0.666667f), true,  10), // Bb
        KeyRect(RectF(0.857143f, 0.0f, 1.000000f, 1.0f),      false, 11)) // B

    /**
     * Local coordinates of the small keyboard keys.
     */
    private var smallKeys = ArrayList<KeyRect>(88);

    /**
     * Local coordinates of the big keyboard keys.
     */
    private var bigKeys = ArrayList<KeyRect>(88);

    /**
     * The enclosing rectangle of the small key's area.
     */
    private var smallKeysRect = RectF()

    /**
     * The enclosing rectangle of the big key's area.
     */
    private var bigKeysRect = RectF()

    /**
     * These keys are currently pressed.
     */
    private var activeKeys = ArrayList<Int>(88);

    /**
     * Current scrolling position of the big keyboard.
     */
    private var scrollPosition = 0.5f

    private var lowestVisibleKey = 60
    private var highestVisibleKey = 71
}
