package it.filo.maggioliebook.android.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.filo.maggioliebook.android.R
import it.filo.maggioliebook.android.databinding.FragmentAboutBinding

class AboutFragment: Fragment() {

    private var _binding: FragmentAboutBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }
}