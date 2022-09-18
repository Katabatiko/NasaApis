package com.gonpas.nasaapis.ui.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gonpas.nasaapis.R
import kotlinx.coroutines.delay

class IntroFragment : Fragment() {

    private lateinit var viewModel: IntroViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = this.findNavController()
        /*lifecycleScope.launchWhenCreated {
            delay(2000)
            navController.navigate(IntroFragmentDirections.actionIntroFragmentToApodFragment())
        }*/
    }
}