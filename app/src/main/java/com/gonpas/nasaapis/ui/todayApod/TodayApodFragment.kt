package com.gonpas.nasaapis.ui.todayApod

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.MediaController
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.FragmentApodBinding
import com.gonpas.nasaapis.domain.DomainApod
import com.ravikoradiya.zoomableimageview.ZoomableImageView
import java.lang.Float.max
import java.lang.Float.min

private const val TAG = "xxTaf"

class TodayApodFragment : Fragment() {

    private lateinit var viewModel: TodayApodViewModel
    private lateinit var binding: FragmentApodBinding
    private lateinit var apod: DomainApod

    private lateinit var zoomView: ZoomableImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val app = requireNotNull(activity).application
        binding = DataBindingUtil.inflate<FragmentApodBinding>(inflater, R.layout.fragment_apod, container,false)
        binding.lifecycleOwner = this

        apod = TodayApodFragmentArgs.fromBundle(requireArguments()).apod
        Log.d(TAG, "recibido apod de fecha ${apod.date}")
        binding.domainApod = apod

        val viewMoelFactory = TodayApodViewMoelFactory(apod, app)
        viewModel = ViewModelProvider(this, viewMoelFactory)[TodayApodViewModel::class.java]
        binding.viewModel = viewModel


        val navController = this.findNavController()
        viewModel.navigateUp.observe(viewLifecycleOwner){
            if(it == true) {
                navController.navigateUp()
                viewModel.navigated()
            }
        }

        viewModel.launchVideo.observe(viewLifecycleOwner){
//            Log.d(TAG,"observado launchVideo = $it")
            if(it){
                playVideo()
                viewModel.videoLaunched()
            }
        }

        zoomView = binding.apod

        return binding.root
    }


    override fun onStop() {
        super.onStop()
//        Log.d(TAG,"onStop")
    }

    override fun onResume() {
        super.onResume()
//        Log.d(TAG,"onResume")
        if(requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            requireActivity().actionBar?.hide()
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            requireActivity().window.decorView.visibility = View.GONE
//            Log.d(TAG,"Orientacion landscape")
        }else {
            setHasOptionsMenu(true)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//            Log.d(TAG,"Orientacion portrait")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        Log.d(TAG,"onAttach")
    }

    override fun onDetach() {
        super.onDetach()
//        Log.d(TAG,"onDetach")
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.apod_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       // Log.d("xxTaf","item selected: ${item.itemId}")
        if (item.itemId == R.id.del_apod)   viewModel.delApod()
        return super.onOptionsItemSelected(item)
    }

    fun playVideo() {
//        Log.d(TAG,"playvideo")
        val packageManager = context?.packageManager ?: return
//        Log.d(TAG, "videoUrl: ${apod.hdurl} - videoUri: ${apod.launchUri}")
        //controlando la integridad de la url
//        Log.d(TAG, "actual apod.hdurl: ${apod.hdurl}")
        if(!apod.hdurl.startsWith("http://") && !apod.hdurl.startsWith("https://")){
            apod.hdurl = "http:${apod.hdurl}"
            Log.d(TAG, "new hdurl: ${apod.hdurl}")
        }
        // Try to generate a direct intent to the YouTube app
        var intent = Intent(Intent.ACTION_VIEW, apod.launchUri)
        if (intent.resolveActivity(packageManager) == null) {
//            Log.d(TAG,"No hay youtube app")
            // YouTube app isn't found, use the web url
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(apod.hdurl))
        }
            Log.d(TAG, "final apdo.hdurl: ${apod.hdurl}")
            startActivity(intent)
    }



    /**
     * Helper method to generate YouTube app links
     */
    private val DomainApod.launchUri: Uri
        get() {
            /*if(!hdurl.startsWith("http://") && !hdurl.startsWith("https://")){
                hdurl = "http://$hdurl"
                Log.d(TAG, "new hdurl: $hdurl")
            }*/
            val httpUri = Uri.parse(hdurl)
          //  Log.d(TAG, "httpUri.getQueryParameter: ${httpUri.getQueryParameter('v'.toString())}")
            return Uri.parse("vnd.youtube:" + httpUri.getQueryParameter("v"))
        }


}