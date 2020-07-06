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
package de.matrix44.musictoolbox.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import de.matrix44.musictoolbox.R

/**
 * A simple [Fragment] that displays a list of tempo markings.
 *
 * The data used for this list can be found in the /res/values/arrays.xml file under the
 * tempo_definitions id.
 */
class TempoTableFragment : Fragment() {

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * Just prepares the list for the list view.
     *
     * @param inflater This object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Used when being re-constructed from a previous saved state.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment:
        val view = inflater.inflate(R.layout.fragment_tempo_table, container, false)

        // Our tempo definitions are organized as header/description pairs in this array:
        val tempo = resources.getStringArray(R.array.tempo_definitions)

        // Convert the definitions into a map that can be understood by the list view item:
        val tempoData = ArrayList<Map<String, String?>>()
        for (item in 0 until (tempo.size / 2)) {
            val datum: MutableMap<String, String> = HashMap(2)
            datum["name"]  = tempo[item * 2]
            datum["tempo"] = tempo[item * 2 + 1]
            tempoData.add(datum)
        }

        // Create the adapter for the grid:
        val adapter = SimpleAdapter(view.context, tempoData, android.R.layout.simple_list_item_2,
            arrayOf("name", "tempo"), intArrayOf(android.R.id.text1, android.R.id.text2))

        // Attach adapter to our grid:
        val gridView = view.findViewById(R.id.tempo_table) as GridView
        gridView.adapter = adapter

        // Done:
        return view
    }
}
