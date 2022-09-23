package com.gonpas.nasaapis.ui.marsroverphotos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.RoverManifestBinding

/**
 * A simple [Fragment] subclass.
 * Use the [RoverManifestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoverManifestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<RoverManifestBinding>(inflater, R.layout.rover_manifest, container, false)
        val rover = RoverManifestFragmentArgs.fromBundle(requireArguments()).rover
        binding.rover = rover

        return binding.root
    }

}