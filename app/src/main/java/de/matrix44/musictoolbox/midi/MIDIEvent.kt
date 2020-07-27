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

/**
 * Helper class that encapsulates a MIDI message along with the originating interface and a time
 * stamp. This is what is passed around throughout the application.
 *
 * @param message The actual MIDI data.
 * @param ticks Time stamp when this message was received. This is in ticks from the start of the
 *              current context (recording buffer, clips, tracks etc). For MIDI files it's the delta
 *              from the previous event.
 * @param origin ID of the interface that created this message. While recording this is the hardware
 *               number. Otherwise it's the number of the source track e.g. when routing messages
 *               around. The value -1 is reserved for unknown origins.
 */
class MIDIEvent constructor(
    val message: MIDIMessage,
    var ticks: Int,
    var origin: Int
) {}
