package de.matrix44.musictoolbox.ui.musictheory

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.matrix44.musictoolbox.R

class MusicTheoryFragment : Fragment() {

    companion object {
        fun newInstance() = MusicTheoryFragment()
    }

    private lateinit var viewModel: MusicTheoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music_theory, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MusicTheoryViewModel::class.java)
        // TODO: Use the ViewModel
    }

}