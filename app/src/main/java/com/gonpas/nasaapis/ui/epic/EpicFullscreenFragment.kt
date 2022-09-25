package com.gonpas.nasaapis.ui.epic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.FragmentFullscreenEpicBinding

private const val TAG = "xxEff"

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class EpicFullscreenFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentFullscreenEpicBinding>(inflater, R.layout.fragment_fullscreen_epic, container, false)

        val args = EpicFullscreenFragmentArgs.fromBundle(requireArguments())
        val bigEpicUrl = args.epicUrl

        binding.time.text = args.epicTime
        val imgView =  binding.fullEpic

//      Log.d("xxBu", "thumbUrl: $fullUrl")
        bigEpicUrl.let {
            // para convertir la URL en URI
            val imgUri = it.toUri().buildUpon().scheme("https").build()
            // invocar glide para cargar la imagen
            Glide.with(imgView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image))
                .into(imgView)
        }


        return binding.root
    }
}