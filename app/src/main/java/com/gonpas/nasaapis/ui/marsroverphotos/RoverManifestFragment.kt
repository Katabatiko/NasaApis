package com.gonpas.nasaapis.ui.marsroverphotos

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.databinding.DataBindingUtil
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.RoverManifestBinding

/**
 * A simple [Fragment] subclass.
 * Use the [RoverManifestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "xxRmf"

class RoverManifestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<RoverManifestBinding>(inflater, R.layout.rover_manifest, container, false)
        val rover = RoverManifestFragmentArgs.fromBundle(requireArguments()).rover
        binding.rover = rover

        Log.d(TAG,"rover name: ${rover.name}")
        val url = when(rover.name){
            "Perseverance" -> "https://mars.nasa.gov/layout/embed/model/?s=6"
            "Spirit" -> "https://mars.nasa.gov/layout/embed/model/?s=3&rotate=true"
            "Opportunity" -> "https://mars.nasa.gov/layout/embed/model/?s=3&rotate=true"
            "Curiosity" -> "https://mars.nasa.gov/gltf_embed/24584"
            else -> ""
        }

        val webView = binding.webView
        loadWebView(webView, url)

        return binding.root
    }

    private fun loadWebView(webView: WebView, url: String) = with(webView){
        settings.javaScriptEnabled = true

        webChromeClient = WebChromeClient()
        Log.d(TAG,"url: $url")

        //loadUrl(url)
        loadData(htmlString(url), "text/html", "utf-8")
    }
    private fun htmlString(url: String): String{
        //https://mars.nasa.gov/layout/embed/model/?s=6
//        Log.d(TAG,"url: $url")
        return """
            <iframe src='$url' width='100%' height='100%' scrolling='no' frameborder='0' allowfullscreen></iframe>
          """

        // curiosity <iframe src="https://mars.nasa.gov/gltf_embed/24584" width="100%" height="450px" frameborder="0" />
        // perseverance <iframe src='https://mars.nasa.gov/layout/embed/model/?s=6' width='800' height='450' scrolling='no' frameborder='0' allowfullscreen></iframe>
        // spirit y opportunity <iframe src='https://mars.nasa.gov/layout/embed/model/?s=3&rotate=true' width='800' height='450' scrolling='no' frameborder='0' allowfullscreen></iframe>
    }

}