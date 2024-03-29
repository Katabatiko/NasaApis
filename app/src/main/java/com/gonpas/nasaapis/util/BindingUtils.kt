package com.gonpas.nasaapis.util

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.domain.DomainEpic
import com.gonpas.nasaapis.ui.apods.NasaApiStatus
import com.ravikoradiya.zoomableimageview.ZoomableImageView


@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String){
   // Log.d("xxBu", imgUrl)
    imgUrl.let {
        // para convertir la URL en URI
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        // invocar glide para cargar la imagen
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}
@BindingAdapter("zoomImageUrl")
fun bindZoomImage(zoomImage: ZoomableImageView, imgUrl: String){
    imgUrl.let {
        // para convertir la URL en URI
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        // invocar glide para cargar la imagen
        Glide.with(zoomImage.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation_big)
                    .error(R.drawable.ic_broken_image))
            .into(zoomImage)
    }
}

@BindingAdapter("setThumb")
fun setThumbnail(imgView: ImageView, url: String){

    url.let {
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
}

@BindingAdapter( "setBigImage", "collection" )
fun setBigImage(imgView: ImageView, epic: DomainEpic, coleccion: String){
    var url = "https://epic.gsfc.nasa.gov/archive/%s/%s/%s/%s/png/%s.png"
    val fecha = epic.date.split(" ")[0].split("-")
    val fullUrl = url.format(coleccion, fecha[0], fecha[1],fecha[2], epic.imageName)
//     Log.d("xxBu", "bigImage: $fullUrl")
    fullUrl.let {
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

}

@BindingAdapter("capitalize")
fun TextView.capitalize(rover: String){
    text = rover.uppercase()
}

@BindingAdapter("setRover", "setStatus")
fun TextView.setRover(rover: String, status: String){
    val template = "%s   %s"
    val roverStatus = when(status){
        "active" -> "(activo)"
        "complete" -> "(finalizado)"
        else -> ""
    }
    text = String.format(template, rover.uppercase(), roverStatus)
}

@BindingAdapter("toString")
fun TextView.intToString(int: Int){
    text = int.toString()
}

@BindingAdapter("dateFormat")
fun TextView.bindDateFormated(date: String){
    var partes = date.split("-")
    partes = partes.reversed()
    val template = "%s-%s-%s"
    text = String.format(template, partes[0], partes[1], partes[2])
}

@BindingAdapter("extractTime")
fun TextView.extractAndSetTime(date: String){
    var partes = date.split(" ")
    text = partes[1]
}

@BindingAdapter("setTextColor")
fun TextView.setTextColor(blanco: Boolean){
    if(blanco) this.setTextColor(Color.parseColor("#FFFFFF"))
    else    this.setTextColor(Color.parseColor("#FF0000"))
}

@BindingAdapter("nasaApiStatus")
fun bindStatusImage(statusImgView: ImageView, status: NasaApiStatus){
    when(status){
        NasaApiStatus.LOADING -> {
            statusImgView.visibility = View.VISIBLE
            statusImgView.setImageResource(R.drawable.loading_animation)
        }
        NasaApiStatus.ERROR -> {
            statusImgView.visibility = View.VISIBLE
            statusImgView.setImageResource(R.drawable.ic_broken_image)
        }
        NasaApiStatus.DONE -> {
            statusImgView.visibility = View.GONE
        }
    }
}

@BindingAdapter("statusErrorMsg")
fun bindStatusMsg(statusTextView: TextView, status: NasaApiStatus){
    when(status) {
        NasaApiStatus.ERROR -> {
            statusTextView.visibility = View.VISIBLE
        }
        else -> statusTextView.visibility = View.GONE
    }
}
