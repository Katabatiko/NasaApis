package com.gonpas.nasaapis.ui.epic

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.EpicsAnimationBinding
import com.gonpas.nasaapis.domain.DomainEpic
import java.util.*

private const val TAG = "xxEsf"

class EpicSliderFragment : Fragment(), ViewPagerAdapter.ConditionViewPager {

    lateinit var imageList: List<DomainEpic>
    lateinit var coleccion: String
    lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<EpicsAnimationBinding>(inflater, R.layout.epics_animation,  container, false)

        viewPager = binding.viewPager

        // recogiendo la lista de los argumentos
        imageList = EpicSliderFragmentArgs.fromBundle(requireArguments()).epicsList.toList()
        coleccion = EpicSliderFragmentArgs.fromBundle(requireArguments()).coleccion

        viewPager.adapter = ViewPagerAdapter(imageList, coleccion,this)
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.offscreenPageLimit = 2
        viewPager.setPageTransformer(AlphaPageTransformer())

        //
        var currentPage: Int = 0
        val handler = Handler()
        val update = Runnable {
            if(currentPage == imageList.size)       currentPage = 0
            viewPager.setCurrentItem(currentPage++, true)
        }
        Timer().schedule(object : TimerTask(){
            override fun run() {
                handler.post(update)
            }
        }, 1000, 3000)

        return binding.root
    }


    override fun condition(position: Int, fullSize: Int) {
        if (position == fullSize){
            Log.d(TAG,"posicion final: $position")
        }
    }
}

private const val MIN_SCALE = 0.5f

class AlphaPageTransformer : ViewPager2.PageTransformer {
    /** During the depth animation, the default animation (a screen slide) still takes place,
     * so you must counteract the screen slide with a negative X translation. For example:
     * view.translationX = -1 * view.width * position
     */
    override fun transformPage(page: View, position: Float) {
        page.apply{
            val pageWidth = width
            when{
                position < -1 -> {// [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 0 -> {// [-1,0]
                    // Use the default slide transition when moving to the left page
                    alpha = 1 - position
                    translationX = pageWidth * -position
                    translationY = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> {// [0,1]
                    // Fade the page out.
                    alpha = 1 - position
                    // Counteract the default slide transition
                    translationX = pageWidth * -position
                    // Move it behind the left page
                    translationY = -1f
                    // Scale the page down (between MIN_SCALE and 1)
                    /*val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor*/
                }
                else -> {// [1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}

/*
@RequiresApi(21)
class DepthPageTransformer : ViewPager2.PageTransformer {
    */
/** During the depth animation, the default animation (a screen slide) still takes place,
     * so you must counteract the screen slide with a negative X translation. For example:
     * view.translationX = -1 * view.width * position
     *//*

    override fun transformPage(page: View, position: Float) {
        page.apply{
            val pageWidth = width
            when{
                position < -1 -> {// [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 0 -> {// [-1,0]
                // Use the default slide transition when moving to the left page
                    alpha = 1f
                    translationX = 0f
                    translationY = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> {// [0,1]
                    // Fade the page out.
                    alpha = 1 - position
                    // Counteract the default slide transition
                    translationX = pageWidth * -position
                    // Move it behind the left page
                    translationY = -1f
                    // Scale the page down (between MIN_SCALE and 1)
                    val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> {// [1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}*/
