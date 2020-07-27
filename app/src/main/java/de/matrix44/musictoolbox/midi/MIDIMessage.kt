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
package de.matrix44.musictoolbox.midi

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.roundToInt

// https://blog.stylingandroid.com/midipad-midi-events/

/**
 *
 * The message type can not be changed during the lifetime of this object as this defines the size
 * of the data package.
 *
 * @param _type  The MIDI message type for the data.
 * @param _channel The MIDI channel for the data.
 * @param _data Optional payload of this message as variable argument list.
 */
class MIDIMessage constructor(
    private val _type: MessageType,
    private var _channel: Byte,
    private vararg var _data: Byte) {

    /**
     * The MIDI message type for the data.
     */
    val message: MessageType
        get() = _type

    /**
     * The MIDI channel for the data.
     */
    var channel: Int
        get() = _channel.toInt()
        set(value) {
            _channel = value.toByte()
        }

    /**
     * The first message parameter.
     *
     * There is no range checking here so please make sure that this message has enough room for at
     * least 1 parameter in the data section.
     */
    var parameter1 : Int
        get() = _data[0].toInt()
        set(value) {
            _data[0] = value.toByte()
        }

    /**
     * The second message parameter.
     *
     * There is no range checking here so please make sure that this message has enough room for at
     * least 2 parameters in the data section.
     */
    var parameter2 : Int
        get() = _data[1].toInt()
        set(value) {
            _data[1] = value.toByte()
        }

    /**
     * The available MIDI message types.
     *
     *  see https://www.midi.org/specifications-old/item/table-1-summary-of-midi-message for a very
     *  detailed description of the MIDI messages below.
     */
    enum class MessageType(val msg: Byte) {

        /**
         * A key was pressed. Additional parameters are channel, note number and velocity.
         */
        NoteOff(0x80.toByte()),

        /**
         * A key was released. Additional parameters are channel, note number and velocity.
         */
        NoteOn(0x90.toByte()),

        /**
         * A key was pressed after the initial hit (aftertouch). Additional parameters are channel,
         * note number and pressure.
         */
        PolyphonicKeyPressure(0xA0.toByte()),

        /**
         * A controller was moved. Additional parameters are channel, controller ID and pressure.
         */
        ControlChange(0xB0.toByte()),

        /**
         * The sound was switched to a different program. Additional parameters are channel and
         * program number.
         */
        ProgramChange(0xC0.toByte()),

        /**
         * A key was pressed after the initial hit (aftertouch). Additional parameters are channel
         * and pressure. This is monophonic aftertouch.
         */
        ChannelPressure(0xD0.toByte()),

        /**
         * The pitch bend lever was moved. Additional parameters are channel and the pitch amount as
         * a 14 bit value.
         */
        PitchBend(0xE0.toByte()),

        /**
         * System exclusive binary data.
         */
        SystemExclusive(0xF0.toByte())
    }

    /**
     * Convert this event to a byte array that can be sent to actual MIDI hardware.
     */
    fun asBytes(): ByteArray {

        // Build new array:
        return ByteArray(_data.size + 1) {

            // Iterate through the contents:
            when (it) {
                // Combine message and channel into first byte:
                0 -> (_type.msg and 0x0F.toByte()) or (_channel and 0xF0.toByte())

                // Just copy the rest (if any):
                else -> _data[it - 1]
            }
        }
    }

    /**
     * Static functions.
     */
    companion object {

        /**
         * Create a Note Off message.
         *
         * This message is sent when a note is released.
         *
         * @param channel Channel to use for this message.
         * @param note Note number to use for this message.
         * @param velocity Release velocity to use for this message.
         */
        fun noteOff(channel: Int, note: Int, velocity: Int) =
            MIDIMessage(MessageType.NoteOff, channel.toByte(), note.toByte(), velocity.toByte())

        /**
         * Create a Note On message.
         *
         * This message is sent when a note is pressed.
         *
         * @param channel Channel to use for this message.
         * @param note Note number to use for this message.
         * @param velocity Velocity to use for this message.
         */
        fun noteOn(channel: Int, note: Int, velocity: Int) =
            MIDIMessage(MessageType.NoteOn, channel.toByte(), note.toByte(), velocity.toByte())

        /**
         * Create a Polyphonic Key Pressure message.
         *
         * This message is most often sent by pressing down on the key after it was pressed.
         *
         * @param channel Channel to use for this message.
         * @param note Note number to use for this message.
         * @param pressure Pressure to use for this message.
         */
        fun polyphonicKeyPressure(channel: Int, note: Int, pressure: Int) =
            MIDIMessage(MessageType.PolyphonicKeyPressure, channel.toByte(), note.toByte(), pressure.toByte())

        /**
         * Create a Control Change message.
         *
         * This message is sent when a controller value changes. Controller numbers 120 to 127 are
         * reserved as "Channel Mode Messages" (below).
         *
         * @param channel Channel to use for this message.
         * @param controller Controller number to use for this message.
         * @param value Controller value to use for this message.
         */
        fun controlChange(channel: Int, controller: Int, value: Int) =
            MIDIMessage(MessageType.ControlChange, channel.toByte(), controller.toByte(), value.toByte())

        /**
         * Create a Program Change message.
         *
         * This message is sent when a program patch changes
         *
         * @param channel Channel to use for this message.
         * @param program Program number to use for this message.
         */
        fun programChange(channel: Int, program: Int) =
            MIDIMessage(MessageType.ProgramChange, channel.toByte(), program.toByte())

        /**
         * Create a Channel Pressure (Aftertouch) message.
         *
         * This message is most often sent by pressing down on the key after it was pressed. This
         * message is usually the greatest pressure value of all currently pressed keys.
         *
         * @param channel Channel to use for this message.
         * @param pressure Pressure to use for this message.
         */
        fun channelPressure(channel: Int, pressure: Int) =
            MIDIMessage(MessageType.ChannelPressure, channel.toByte(), pressure.toByte())

        /**
         * Create a Pitch Bend message.
         *
         * This message is sent to indicate a change of the pitch bender wheel or lever. The pitch
         * bender is measured by an unsigend fourteen bit value with the center at 0x2000.
         *
         * @param channel Channel to use for this message.
         * @param msb Most significant 7 bits of the pitch bend value.
         * @param lsb Least significant 7 bits of the pitch bend value.
         */
        fun pitchBend(channel: Int, msb: Int, lsb: Int) =
            MIDIMessage(MessageType.PitchBend, channel.toByte(), msb.toByte(), lsb.toByte())

        /**
         * Create a Pitch Bend message from float.
         *
         * Convenience method to create a pitch bend message from normalized floats [-1..1].
         *
         * @param channel Channel to use for this message.
         * @param value Pitch bend value as normalized float [-1..1].
         */
        fun pitchBend(channel: Int, value: Float) : MIDIMessage {

            // Clamp to range (value.clamp() gives an error):
            val floatVal = value.coerceIn(-1.0f, 1.0f)

            // Convert to 14 bit value:
            val intVal : Int = (((floatVal * 16384.0f) + 8192.0f).roundToInt())

            // Extract bits
            val msb = (intVal shr 7) and 0x7F
            val lsb = intVal and 0x7F

            // Construct regular message:
            return pitchBend(channel, msb, lsb)
        }

        /**
         * Create a System Exclusive message from a byte array.
         *
         * @param data The system exclusive data.
         */
        fun systemExclusive(data: ByteArray) =
            MIDIMessage(MessageType.SystemExclusive, 0, *data, 0xF7.toByte())

        /**
         * Create an All Sounds Off message.
         *
         * When this message is received all sound sources have to be turned off as soon as
         * possible.
         *
         * @param channel Channel to use for this message.
         */
        fun allSoundsOff(channel: Int) =
            controlChange(120, channel, 0)

        /**
         * Create a Controller Reset message.
         *
         * When this message s is received all controller values should be reset to their default
         * values.
         *
         * @param channel Channel to use for this message.
         */
        fun controllerReset(channel: Int) =
            controlChange(121, channel, 0)

        /**
         * Create a Local Control Off message.
         *
         * When local control is off all devices should only respond to messages sent over MIDI.
         * Local keyboards and controllers should be disconnected from the sound engines.
         *
         * @param channel Channel to use for this message.
         */
        fun localControlOff(channel: Int) =
            controlChange(122, channel, 0)

        /**
         * Create a Local Control On message.
         *
         * When local control is off all devices should only respond to messages sent over MIDI.
         * Local keyboards and controllers should be disconnected from the sound engines.
         *
         * @param channel Channel to use for this message.
         */
        fun localControlOn(channel: Int) =
            controlChange(122, channel, 127)

        /**
         * Create an All Notes Off message.
         *
         * When this message is received all playing notes should be turned off.
         *
         * @param channel Channel to use for this message.
         */
        fun allNotesOff(channel: Int) =
            controlChange(123, channel, 0)

        /**
         * Create an Omni Mode Off message.
         *
         * This also implies an All Notes Off message.
         *
         * @param channel Channel to use for this message.
         */
        fun omniModeOff(channel: Int) =
            controlChange(124, channel, 0)

        /**
         *  Create an Omni Mode On message.
         *
         *  This also implies an All Notes Off message.
         *
         * @param channel Channel to use for this message.
         */
        fun omniModeOn(channel: Int) =
            controlChange(125, channel, 0)

        /**
         * Create a Mono Mode On message.
         *
         * This also implies an All Notes Off message.
         *
         * @param channel Channel to use for this message.
         */
        fun monoModeOn(channel: Int) =
            controlChange(126, channel, 0)

        /**
         * Create a Poly Mode On message.
         *
         * This also implies an All Notes Off message.
         *
         * @param channel Channel to use for this message.
         */
        fun polyModeOn(channel: Int) =
            controlChange(127, channel, 0)
    }
}
