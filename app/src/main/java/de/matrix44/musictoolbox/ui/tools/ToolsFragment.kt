package de.matrix44.musictoolbox.ui.tools

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import de.matrix44.musictoolbox.R

/**
 * A simple [Fragment] subclass.
 */
class ToolsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        val button = view.findViewById<Button>(R.id.button_tempo_table)

        button.setOnClickListener(fun(it: View) {

            val nav = view.findNavController()
            nav.navigate(R.id.nav_tempo_table)
        })
        return view
    }
}
