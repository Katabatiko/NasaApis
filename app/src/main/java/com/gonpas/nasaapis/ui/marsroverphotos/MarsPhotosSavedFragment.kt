package com.gonpas.nasaapis.ui.marsroverphotos

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.asListDomainMarsPhotos
import com.gonpas.nasaapis.databinding.FragmentMarsPhotosListBinding
import com.gonpas.nasaapis.databinding.MarsPhotoSavedItemBinding
import com.gonpas.nasaapis.domain.DomainMarsPhoto

private const val TAG = "xxMplf"

class MarsPhotosListFragment : Fragment(){
    val viewModel: MarsPhotosListViewModel by lazy {
        val application = requireNotNull(activity).application
        val viewModelFactory = MarsPhotosListViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory).get(MarsPhotosListViewModel::class.java)
    }

    private lateinit var adapter: MarsFotosListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentMarsPhotosListBinding>(inflater, R.layout.fragment_mars_photos_list, container, false)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        adapter = MarsFotosListAdapter(DeleteFotoListener {
            //      Log.d(TAG,"Pulsado para borrar ${it.marsPhotoId}")
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("¿Seguro de borrar?")
                .setCancelable(false)
                .setPositiveButton("Confirmar"){ dialog, id ->
                    viewModel.removeFoto(it.marsPhotoId)
                }
                .setNegativeButton("Cancelar"){ dialog, id ->
                    dialog.dismiss()
                }
            builder.create().show()
        })

        binding.marsFotosList.adapter = adapter

        viewModel.fotosList.observe(viewLifecycleOwner){ list ->
            Log.d(TAG,"lista de fotos recibida con ${list.size} imágenes")
            viewModel.setStatusDone()
            adapter.let {
                it.submitList(list.asListDomainMarsPhotos())
            }
        }

        return binding.root
    }


    class MarsFotosListAdapter(val clickListener: DeleteFotoListener) : ListAdapter<DomainMarsPhoto, MarsFotosListAdapter.FotoViewHolder>(FotosDiffCallback) {

        var list = listOf<DomainMarsPhoto>()
            set(value) { field = value}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
            return FotoViewHolder.from(parent)
        }

        override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
            val item = getItem(position)
            holder.binding.foto = item
            holder.binding.deleteBtn.setOnClickListener {
                clickListener.onClick(item)
            }
            holder.binding.executePendingBindings()
        }


        companion object FotosDiffCallback: DiffUtil.ItemCallback<DomainMarsPhoto>(){
            override fun areItemsTheSame(
                oldItem: DomainMarsPhoto,
                newItem: DomainMarsPhoto
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: DomainMarsPhoto,
                newItem: DomainMarsPhoto
            ): Boolean {
                return oldItem.marsPhotoId == newItem.marsPhotoId
            }
        }

        class FotoViewHolder(val binding: MarsPhotoSavedItemBinding) : RecyclerView.ViewHolder(binding.root){

            companion object{
                fun from(parent: ViewGroup): FotoViewHolder{
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val binding = MarsPhotoSavedItemBinding.inflate(layoutInflater, parent, false)
                    return FotoViewHolder(binding)
                }

            }
        }

    }

    class DeleteFotoListener(val clickListener: (foto: DomainMarsPhoto) -> Unit){
        fun onClick(foto: DomainMarsPhoto) = clickListener(foto)
    }
}