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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.View
import kotlin.math.absoluteValue

/**
 * Virtual piano keyboard view.
 *
 * This view is a virtual piano keyboard with an overview area that also acts as navigation bar
 * for the big keyboard. This view is highly customizable in color and shape. It has two modes of
 * operation: A regular mode where the user can swipe through the key range as he likes and an
 * octave mode where there is only one octave visible. This is useful if you have to choose a
 * specific note (eg in a flash card game).
 *
 * There is no feedback for hit keys by default. You'll have to call noteOn/noteOff in your piano
 * key message handler or set localOff to true. This separation is done to be able to also show
 * notes played from other sources on the keyboard (eg from a sequencer or note checker).
 *
 * This class is a regular view with properties, events and so on. To react on key messages you'll
 * have to implement the PianoKeyListener interface like:
 *
 * val keys: PianoKeys = root.findViewById(R.id.pianoKeys)
 * keys.pianoKeyListener = object : PianoKeys.PianoKeyListener {
 *     override fun onPianoKeyDown(noteNumber: Int) {
 *         keys.noteOn(noteNumber)
 *         // Do something with the key here...
 *     }
 *     override fun onPianoKeyUp(noteNumber: Int) {
 *         keys.noteOff(noteNumber)
 *         // Do something with the key here...
 *     }
 * }
 */
class PianoKeys : View {

    /**
     * Ratio between key width and key height.
     *
     * This stretches or narrows the keys if set to a different value. The default is using a
     * usual grand piano ratio.
     */
    private var _keySizeRatio: Float = 15.0f / 2.3f
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
    var drawBigKeyOutline: Boolean
        get() = _drawBigKeyOutline
        set(value) {
            _drawBigKeyOutline = value
            invalidate()
        }

    /**
     * Adds a dot at middle C on the small keyboard.
     */
    private var _markMiddleC: Boolean = true
    @Suppress("unused")
    var markMiddleC: Boolean
        get() = _markMiddleC
        set(value) {
            _markMiddleC = value
            invalidate()
        }

    /**
     * Marks all Cs on the big keyboard.
     */
    private var _markAllCs: Boolean = true
    @Suppress("unused")
    var markAllCs: Boolean
        get() = _markAllCs
        set(value) {
            _markAllCs = value
            invalidate()
        }

    /**
     * Controls the visibility of the the small keyboard.
     */
    private var _smallKeysVisible: Boolean = true
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
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
    @Suppress("unused")
    var keyDownKeyShadeColor: Int
        get() = _keyDownKeyShadeColor
        set(value) {
            _keyDownKeyShadeColor = value
            keyDownKeyShadePaint?.color = _keyDownKeyShadeColor
            invalidate()
        }

    /**
     * Turn octave mode on or off.
     */
    private var _octaveMode = false
    @Suppress("unused")
    var octaveMode: Boolean
        get() = _octaveMode
        set(value) {
            _octaveMode = value
            invalidateMeasurements()
            invalidate()
        }

    /**
     * The current octave if we are in octave mode.
     *
     * The default octave is 4, starting at middle C.
     */
    private var _currentOctave = 4
    @Suppress("unused")
    var currentOctave: Int
        get() = _currentOctave
        set(value) {
            _currentOctave = value
            invalidateMeasurements()
            invalidate()
        }

    /**
     * Turn local keyboard feedback on or off.
     */
    private var _localOff = true
    @Suppress("unused")
    var localOff: Boolean
        get() = _localOff
        set(value) {
            _localOff = value
            invalidate()
        }

    /**
     * Implement this interface to receive key messages from this view.
     */
    interface PianoKeyListener {
        /**
         * A key was pressed.
         *
         * @param noteNumber The key that was pressed
         */
        fun onPianoKeyDown(noteNumber: Int)

        /**
         * A key was released.
         *
         * @param noteNumber The key that was released
         */
        fun onPianoKeyUp(noteNumber: Int)
    }

    /**
     * The listener that we send our key messages to.
     */
    private var _pianoKeyListener: PianoKeyListener? = null
    @Suppress("unused")
    var pianoKeyListener: PianoKeyListener?
        get() = _pianoKeyListener
        set(value) {
            _pianoKeyListener = value
            invalidate()
        }

    /**
     * Public function that turns on a specific note on the keyboard.
     *
     * @param note The note to turn on.
     */
    fun noteOn(note: Int) {

        // Add to active notes:
        if (!activeKeys.contains(note))
            activeKeys.add(note)

        // Redraw:
        invalidate()
    }

    /**
     * Public function that turns on a specific note on the keyboard.
     *
     * @param note The note to turn on.
     */
    fun noteOff(note: Int) {

        // Remove active notes:
        if (activeKeys.contains(note))
            activeKeys.remove(note)

        // Redraw:
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
        _markMiddleC          = a.getBoolean(R.styleable.PianoKeys_markMiddleC,         _markMiddleC)
        _markAllCs            = a.getBoolean(R.styleable.PianoKeys_markAllCs,           _markAllCs)
        _octaveMode           = a.getBoolean(R.styleable.PianoKeys_octaveMode,          _octaveMode)
        _localOff             = a.getBoolean(R.styleable.PianoKeys_localOff,            _localOff)
        _lineColor            = a.getColor(R.styleable.PianoKeys_lineColor,            _lineColor)
        _lineShadeColor       = a.getColor(R.styleable.PianoKeys_lineShadeColor,       _lineShadeColor)
        _whiteKeyColor        = a.getColor(R.styleable.PianoKeys_whiteKeyColor,        _whiteKeyColor)
        _whiteKeyShadeColor   = a.getColor(R.styleable.PianoKeys_whiteKeyShadeColor,   _whiteKeyShadeColor)
        _blackKeyColor        = a.getColor(R.styleable.PianoKeys_blackKeyColor,        _blackKeyColor)
        _blackKeyShadeColor   = a.getColor(R.styleable.PianoKeys_blackKeyShadeColor,   _blackKeyShadeColor)
        _keyDownKeyColor      = a.getColor(R.styleable.PianoKeys_keyDownKeyColor,      _keyDownKeyColor)
        _keyDownKeyShadeColor = a.getColor(R.styleable.PianoKeys_keyDownKeyShadeColor, _keyDownKeyShadeColor)
        _currentOctave        = a.getInt(R.styleable.PianoKeys_currentOctave, _currentOctave)
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
        textPaint            = TextPaint(Paint.ANTI_ALIAS_FLAG)

        // Set initial colors:
        linePaint!!.color            = _lineColor
        lineShadePaint!!.color       = _lineShadeColor
        whiteKeyPaint!!.color        = _whiteKeyColor
        whiteKeyShadePaint!!.color   = _whiteKeyShadeColor
        blackKeyPaint!!.color        = _blackKeyColor
        blackKeyShadePaint!!.color   = _blackKeyShadeColor
        keyDownKeyPaint!!.color      = _keyDownKeyColor
        keyDownKeyShadePaint!!.color = _keyDownKeyShadeColor

        textPaint!!.color     = _lineColor
        textPaint!!.textAlign = Paint.Align.LEFT
        textPaint!!.textSize  = 20.0f
        //linePaint!!.strokeWidth = 2f

        // Create keys:
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

        // Update visuals:
        updateVisibleArea()
    }

    /**
     * Update the dimensions of the small key area and the keys inside the area.
     *
     * The area of the small keyboard defines the available space for the big keys. The width of the
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

            // Get key to update:
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

        // Count white keys:
        var whiteKeyCnt = 0
        for (key in smallKeys) {
            if (!keyLayout[key.noteNumber % 12].isBlack)
                whiteKeyCnt++
        }

        // Update the area of the big keyboard to fill up the view area:
        bigKeysRect.left   = paddingLeft.toFloat()
        bigKeysRect.top    = smallKeysRect.bottom
        bigKeysRect.right  = width - paddingRight.toFloat()
        bigKeysRect.bottom = height - paddingBottom.toFloat()
        val octaveHeight  = bigKeysRect.height()
        val whiteKeyWidth = if (_octaveMode) bigKeysRect.width() / 7.0f else octaveHeight / _keySizeRatio
        val octaveWidth   = whiteKeyWidth * 7.0f

        // Calc total width and scroll offset:
        bigKeyboardWidth = whiteKeyWidth * whiteKeyCnt

        // Calc where the starting octave begins in relation to the zero position:
        var octaveStart = -keyLayout[smallKeys[0].noteNumber % 12].r.left * octaveWidth

        // Loop through all notes:
        var octaveX = 0.0f
        for (key in bigKeys) {

            // Get key to update:
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

            // Found current visible C?
            if (key.noteNumber % 12 == 0 && key.noteNumber / 12 == (_currentOctave + 1))
               octaveX = key.r.left
        }

        // Update text size to be half a key in width:
        val maxTextWidth = whiteKeyWidth * 0.5f
        textPaint!!.textSize = 100.0f
        val tw = textPaint!!.measureText("C4")
        textPaint!!.textSize = maxTextWidth * 100.0f / tw

        // Update scroll position if we are in octave mode:
        if (_octaveMode) {
            scrollPosition = octaveX / (bigKeyboardWidth - bigKeysRect.width())
        }
    }

    /**
     * Internal helper that calculates the visible area of the big keyboard.
     */
    private fun updateVisibleArea() {

        // Octave mode is fixed:
        if (_octaveMode) {
            lowestVisibleKey = (_currentOctave + 1) * 12
            highestVisibleKey = lowestVisibleKey + 11
            return
        }

        // Calc scroll offset:
        val xOffset = scrollPosition * (bigKeyboardWidth - bigKeysRect.width())

        // Reset visible area:
        lowestVisibleKey = -1
        highestVisibleKey = -1

        // Loop through all notes:
        for (key in bigKeys) {

            // Update left/right delimiter:
            val left = key.r.left - xOffset
            val right = key.r.right - xOffset
            if (lowestVisibleKey < 0 && right > bigKeysRect.left)
                lowestVisibleKey = key.noteNumber
            if (left < bigKeysRect.right)
                highestVisibleKey = key.noteNumber
            else
                break
        }
    }

    /**
     * Called when the size of the view has changed.
     *
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldWidth Old width of this view.
     * @param oldHeight Old height of this view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(w, h, oldWidth, oldHeight)

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
        canvas.drawRect(smallKeysRect.left, smallKeysRect.top, smallKeysRect.right + 1.0f, smallKeysRect.bottom + 1.0f, whiteKeyShadePaint!!)

        // Draw white keys:
        var firstKey = true
        for (key in smallKeys) {

            // Draw the key?
            if (key.isBlack)
                continue

            // Get pressed state:
            val isActive = key.noteNumber in lowestVisibleKey..highestVisibleKey

            // The draw function does not include the right and bottom border:
            val drawRect = RectF(key.r.left, key.r.top, key.r.right + 1.0f, key.r.bottom + 1.0f)

            // Draw active key background:
            if (isActive)
                canvas.drawRect(drawRect, whiteKeyPaint!!)

            // Draw the active rect:
            if (key.noteNumber in activeKeys) {
                if (isActive)
                    canvas.drawRect(drawRect, keyDownKeyPaint!!)
                else
                    canvas.drawRect(drawRect, keyDownKeyShadePaint!!)
            }

            // Draw the key divider line only if it's not the first line:
            if (!firstKey) {
                if (!isActive)
                    canvas.drawLine(key.r.left, key.r.top, key.r.left, key.r.bottom, lineShadePaint!!)
                else
                    canvas.drawLine(key.r.left, key.r.top, key.r.left, key.r.bottom, linePaint!!)
            }
            firstKey = false

            // Mark middle C:
            if (_markMiddleC && key.noteNumber == 60) {
                val m = key.r.width() * 0.2f
                val w = key.r.width() - (m * 2.0f)
                val r = RectF(key.r.left + m + 1, key.r.bottom - m - w + 1.0f, key.r.right - m + 1, key.r.bottom - m + 1.0f)
                if (!isActive)
                    canvas.drawOval(r, lineShadePaint!!)
                else
                    canvas.drawOval(r, linePaint!!)
            }

        }

        // Draw black keys:
        for (key in smallKeys) {

            // Draw the key?
            if (!key.isBlack)
                continue

            // Get pressed state:
            val isActive = key.noteNumber in lowestVisibleKey..highestVisibleKey

            // The draw function does not include the right and bottom border:
            val drawRect = RectF(key.r.left, key.r.top, key.r.right + 1.0f, key.r.bottom + 1.0f)

            // Active keys need a frame:
            if (key.noteNumber in activeKeys) {
                if (isActive) {
                    canvas.drawRect(drawRect, keyDownKeyPaint!!)
                    canvas.drawLine(key.r.left, key.r.top, key.r.left, key.r.bottom, linePaint!!)
                    canvas.drawLine(key.r.right, key.r.top, key.r.right, key.r.bottom, linePaint!!)
                    canvas.drawLine(key.r.left, key.r.bottom, key.r.right, key.r.bottom, linePaint!!)
                } else {
                    canvas.drawRect(drawRect, keyDownKeyShadePaint!!)
                    canvas.drawLine(key.r.left, key.r.top, key.r.left, key.r.bottom, lineShadePaint!!)
                    canvas.drawLine(key.r.right, key.r.top, key.r.right, key.r.bottom, lineShadePaint!!)
                    canvas.drawLine(key.r.left, key.r.bottom, key.r.right, key.r.bottom, lineShadePaint!!)
                }
            }
            else {
                // Solid rectangle for normal black keys:
                if (isActive)
                    canvas.drawRect(drawRect, blackKeyPaint!!)
                else
                    canvas.drawRect(drawRect, blackKeyShadePaint!!)
            }
        }

        // Border top, left and right lines around the small keyboard:
        if (_drawSmallKeyOutline) {
            canvas.drawLine(smallKeysRect.left,  smallKeysRect.top, smallKeysRect.left,  smallKeysRect.bottom, linePaint!!)
            canvas.drawLine(smallKeysRect.right, smallKeysRect.top, smallKeysRect.right, smallKeysRect.bottom, linePaint!!)
            canvas.drawLine(smallKeysRect.left,  smallKeysRect.top, smallKeysRect.right, smallKeysRect.top,    linePaint!!)
        }

        // Divider line between the keyboards:
        if (_drawKeyDivider)
            canvas.drawLine(smallKeysRect.left, smallKeysRect.bottom, smallKeysRect.right, smallKeysRect.bottom, linePaint!!)
    }

    /**
     * Draws the big keyboard.
     *
     * @param canvas The canvas on which the keys should be drawn.
     */
    private fun drawBigKeyboard(canvas: Canvas) {

        // Fill background:
        canvas.drawRect(bigKeysRect.left, bigKeysRect.top, bigKeysRect.right + 1.0f, bigKeysRect.bottom + 1.0f, whiteKeyPaint!!)

        // Calc scroll offset:
        val xOffset =  scrollPosition * (bigKeyboardWidth - bigKeysRect.width())

        // Draw white keys:
        for (key in bigKeys) {

            // Skip black keys:
            if (key.isBlack)
                continue

            // The drawRect function does not include the right and bottom border:
            val left = bigKeysRect.left + key.r.left - xOffset
            val right = bigKeysRect.left + key.r.right - xOffset
            val drawRect = RectF(left, key.r.top, right + 1.0f, key.r.bottom + 1.0f)

            // Is this key to the left of the view?
            if (right < bigKeysRect.left)
                continue

            // Did we leave the drawing area?
            if (left > bigKeysRect.right)
                break

            // Draw the key:
            if (key.noteNumber in activeKeys) {

                // Clip key to the viewing area:
                val r = RectF()
                r.setIntersect(key.r, bigKeysRect)

                // Draw it:
                canvas.drawRect(drawRect, keyDownKeyPaint!!)
            }

            // Draw the line between the keys:
            if (drawRect.left - 0.5f > bigKeysRect.left && drawRect.left + 0.5f < bigKeysRect.right)
                canvas.drawLine(left, key.r.top, left, key.r.bottom, linePaint!!)

            // Mark Cs:
            if (_markAllCs && key.noteNumber % 12 == 0) {
                val o = (key.noteNumber / 12) - 1
                val m = (right - left) * 0.1f
                if (right - m < bigKeysRect.right && left + m > bigKeysRect.left)
                    canvas.drawText("C$o", left + m, key.r.bottom - m, textPaint!!)
            }
        }

        // Draw black keys:
        for (key in bigKeys) {

            // Skip black keys:
            if (!key.isBlack)
                continue

            // The draw function does not include the right and bottom border:
            val left = bigKeysRect.left + key.r.left - xOffset
            val right = bigKeysRect.left + key.r.right - xOffset
            val drawRect = RectF(left, key.r.top, right + 1.0f, key.r.bottom + 1.0f)

            // Is this key to the left of the view?
            if (right < bigKeysRect.left)
                continue

            // Did we leave the drawing area?
            if (left > bigKeysRect.right)
                break

            // Clip black key to the viewing area:
            val r = RectF()
            r.setIntersect(drawRect, bigKeysRect)

            // Draw it:
            if (key.noteNumber in activeKeys) {
                canvas.drawRect(drawRect, keyDownKeyPaint!!)

                // Add outline if visible:
                if (left - 0.5f > bigKeysRect.left && left + 0.5f < bigKeysRect.right)
                    canvas.drawLine(left, key.r.top, left, key.r.bottom, linePaint!!)
                if (right - 0.5f > bigKeysRect.left && right + 0.5f < bigKeysRect.right)
                    canvas.drawLine(right, key.r.top, right, key.r.bottom, linePaint!!)
                canvas.drawLine(left, key.r.bottom, right, key.r.bottom, linePaint!!)
            } else
                canvas.drawRect(r, blackKeyPaint!!)
        }

        // Border bottom, left and right lines around the big keyboard:
        if (_drawBigKeyOutline) {
            canvas.drawLine(bigKeysRect.left,  bigKeysRect.top,    bigKeysRect.left,  bigKeysRect.bottom, linePaint!!)
            canvas.drawLine(bigKeysRect.right, bigKeysRect.top,    bigKeysRect.right, bigKeysRect.bottom, linePaint!!)
            canvas.drawLine(bigKeysRect.left,  bigKeysRect.bottom, bigKeysRect.right, bigKeysRect.bottom, linePaint!!)

            // Top line only if needed:
            if (!_smallKeysVisible)
                canvas.drawLine(smallKeysRect.left, smallKeysRect.bottom, smallKeysRect.right, smallKeysRect.bottom, linePaint!!)
        }
    }

    /**
     * React to touch events.
     *
     * @param ev: Provides details for this event.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {

        when (ev.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = ev.actionIndex
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)

                // Start dragging on the small keyboard?
                if (smallKeysRect.contains(x, y) && draggingPointerId == INVALID_POINTER_ID) {
                    draggingPointerId = ev.getPointerId(0)
                    lastDraggingX = x
                }

                // Hit a key?
                if (bigKeysRect.contains(x, y)) {

                    // Calc scroll offset:
                    val xOffset =  scrollPosition * (bigKeyboardWidth - bigKeysRect.width())

                    // Scan white keys:
                    var hitKey: KeyRect? = null
                    for (key in bigKeys) {

                        // Skip white keys:
                        if (!key.isBlack)
                            continue

                        // Is this key to the left of the view?
                        val right = bigKeysRect.left + key.r.right - xOffset
                        if (right < bigKeysRect.left)
                            continue

                        // Did we leave the drawing area?
                        val left = bigKeysRect.left + key.r.left - xOffset
                        if (left > bigKeysRect.right)
                            break

                        // Check the key:
                        val checkRect = RectF(left, key.r.top, right, key.r.bottom)
                        if (checkRect.contains(x, y)) {
                            hitKey = key
                            break
                        }
                    }

                    // Scan white keys:
                    if (hitKey == null) {
                        for (key in bigKeys) {

                            // Skip black keys:
                            if (key.isBlack)
                                continue

                            // Is this key to the left of the view?
                            val right = bigKeysRect.left + key.r.right - xOffset
                            if (right < bigKeysRect.left)
                                continue

                            // Did we leave the drawing area?
                            val left = bigKeysRect.left + key.r.left - xOffset
                            if (left > bigKeysRect.right)
                                break

                            // Check the key:
                            val checkRect = RectF(left, key.r.top, right, key.r.bottom)
                            if (checkRect.contains(x, y)) {
                                hitKey = key
                                break
                            }
                        }
                    }

                    // Hit something?
                    if (hitKey != null && !activeKeys.contains(hitKey.noteNumber)) {

                        // Add key with pointer to current down list:
                        downKeys.add(DownKey(ev.getPointerId(0), hitKey.noteNumber))

                        // Fire key hit event:
                        _pianoKeyListener?.onPianoKeyDown(hitKey.noteNumber)

                        // Update display if needed:
                        if (!_localOff)
                            noteOn(hitKey.noteNumber)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {

                // Are we dragging the small keyboard?
                if (draggingPointerId != INVALID_POINTER_ID) {
                    val pointerIndex = ev.findPointerIndex(draggingPointerId)
                    val x = ev.getX(pointerIndex)

                    // Let's see how much we were moving:
                    val dx = x - lastDraggingX

                    // Convert amount into scrolling space:
                    val db = (bigKeyboardWidth - bigKeysRect.width()) / bigKeyboardWidth
                    val deltaScroll = dx / (smallKeysRect.width() * db)

                    // Octave mode jumps from octave to octave:
                    if (_octaveMode) {

                        // Calc the width of one octave in scroll space:
                        val dw = bigKeysRect.width() / bigKeyboardWidth

                        // Dragged enough to reach the next octave?
                        if (deltaScroll.absoluteValue > dw) {

                            // Calc new octave and clip it to the keyboard range:
                            val newOctave = if (deltaScroll < 0.0f) currentOctave - 1 else currentOctave + 1
                            if (newOctave in 1..7)
                                currentOctave = newOctave // The setter will invalidate the view.

                            // Start new drag towards the next octave:
                            lastDraggingX = x
                        }

                    // Regular mode moves smoothly between the keys:
                    } else {

                        // Update scroll position:
                        scrollPosition += deltaScroll
                        if (scrollPosition < 0.0f)
                            scrollPosition = 0.0f
                        if (scrollPosition > 1.0f)
                            scrollPosition = 1.0f

                        // The small keyboard needs an update of the visible keys:
                        updateVisibleArea()

                        // Redraw view:
                        invalidate()

                        // Remember current position for the next move event:
                        lastDraggingX = x
                    }
                }
            }

            // We lost control of some sort, reset active pointers:
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                // Reset active states:
                draggingPointerId = INVALID_POINTER_ID
                downKeys.clear()
                activeKeys.clear()

                // Redraw view:
                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = ev.actionIndex

                // Stopped dragging?
                if (ev.getPointerId(pointerIndex) == draggingPointerId)
                    draggingPointerId = INVALID_POINTER_ID

                // Released a key?
                else {
                    // Find our key:
                    for (downKey in downKeys) {
                        if (downKey.pointerID == ev.getPointerId(pointerIndex)) {

                            // Fire key release event:
                            _pianoKeyListener?.onPianoKeyUp(downKey.noteNumber)

                            // Remove from pressed keys:
                            downKeys.remove(downKey)

                            // Update display if needed:
                            if (!_localOff)
                                noteOff(downKey.noteNumber)

                            // Done:
                            break
                        }
                    }
                }
            }
        }
        return true
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
     * Painter for all text elements.
     */
    private var textPaint: TextPaint? = null

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
    )

    /**
     * Normalized (0..1) coordinates and sizes for a single octave's keys.
     *
     * This is the blueprint for all key drawing functions.
     */
    private val keyLayout = arrayListOf(
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
    private var smallKeys = ArrayList<KeyRect>(88)

    /**
     * Local coordinates of the big keyboard keys.
     */
    private var bigKeys = ArrayList<KeyRect>(88)

    /**
     * The enclosing rectangle of the small key's area.
     */
    private var smallKeysRect = RectF()

    /**
     * The enclosing rectangle of the big key's visible area.
     */
    private var bigKeysRect = RectF()

    /**
     * Total width of the big keyboard
     */
    private var bigKeyboardWidth = 0.0f

    /**
     * Current scrolling position of the big keyboard.
     */
    private var scrollPosition = 0.5f

    /**
     * The lowest note number that is currently visible on the big keyboard.
     */
    private var lowestVisibleKey = 60

    /**
     * The highest note number that is currently visible on the big keyboard.
     */
    private var highestVisibleKey = 71

    /**
     * ID of the finger/pointer that is used to drag the small keyboard.
     */
    private var draggingPointerId = INVALID_POINTER_ID

    /**
     * Last known position of the dragging finger on the small keyboard.
     */
    private var lastDraggingX = 0.0f

    /**
     * These keys are currently pressed.
     */
    private var activeKeys = ArrayList<Int>(88)

    /**
     * Container that ties a pointer/finger ID to a note number.
     *
     * This is used to track the currently pressed keys of the big keyboard.
     */
    internal class DownKey (

        /**
         * pointer/finger ID used.
         */
        var pointerID: Int,

        /**
         * The key that was hit.
         */
        var noteNumber: Int
    )

    /**
     * List of currently pressed keys.
     */
    private var downKeys = ArrayList<DownKey>()
}
