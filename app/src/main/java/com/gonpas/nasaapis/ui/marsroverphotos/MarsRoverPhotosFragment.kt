package com.gonpas.nasaapis.ui.marsroverphotos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.FragmentMarsRoverPhotosBinding
import com.gonpas.nasaapis.databinding.MarsFotoItemBinding
import com.gonpas.nasaapis.domain.DomainMarsPhoto


private val CURIOSITY_LANDING_DATE = listOf("2012","05","06")
private val OPPORTUNITY_LANDING_DATE = listOf("2004","01","25")
private val SPIRIT_LANDING_DATE = listOf("2004","01","04")
private val PERSEVERANCE_LANDING_DATE = listOf("2021","02","18")

private val TAG = "xxMrpf"

/**
 * A simple [Fragment] subclass.
 * Use the [MarsRoverPhotosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MarsRoverPhotosFragment : Fragment() {

    val viewModel: MarsRoverPhotosViewModel by lazy {
        val application = requireNotNull(activity).application
        val viewModelFactory = MarsRoverPhotosViewModelFactory(application)
        ViewModelProvider(requireActivity(), viewModelFactory).get(MarsRoverPhotosViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentMarsRoverPhotosBinding>(inflater, R.layout.fragment_mars_rover_photos, container, false)

        /*val application = requireNotNull(activity).application
        val viewModelFactory = MarsRoverPhotosViewModelFactory(application)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(MarsRoverPhotosViewModel::class.java)*/

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val dia = binding.day
        val mes = binding.month
        val anno = binding.year
        val totalFotos = binding.totalFotos

        viewModel.rover.observe(viewLifecycleOwner){
            when(it){
                "perseverance" -> {
                    anno.hint = PERSEVERANCE_LANDING_DATE[0]
                    mes.hint =  PERSEVERANCE_LANDING_DATE[1]
                    dia.hint =  PERSEVERANCE_LANDING_DATE[2]
                }
                "curiosity" -> {
                    anno.hint = CURIOSITY_LANDING_DATE[0]
                    mes.hint = CURIOSITY_LANDING_DATE[1]
                    dia.hint = CURIOSITY_LANDING_DATE[2]
                }
                "opportunity" -> {
                    anno.hint = OPPORTUNITY_LANDING_DATE[0]
                    mes.hint = OPPORTUNITY_LANDING_DATE[1]
                    dia.hint = OPPORTUNITY_LANDING_DATE[2]
                }
                "spirit" -> {
                    anno.hint = SPIRIT_LANDING_DATE[0]
                    mes.hint = SPIRIT_LANDING_DATE[1]
                    dia.hint = SPIRIT_LANDING_DATE[2]
                }
            }
        }

        dia.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setDia(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) { }
        })
        mes.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setMes(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) { }
        })
        anno.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setAnno(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) { }
        })

        val adapter = MarsFotosAdapter(FotoSaveListener {
            viewModel.guardarFoto(it)
            it.saved = true
        })
        binding.marsFotos.adapter = adapter

        viewModel.photos.observe(viewLifecycleOwner){ fotos ->
//            Log.d(TAG,"photos changed")
            if (fotos != null){
                if (fotos.isNotEmpty()) {
                    adapter.let {
//                        it.datos = fotos
//                        it.notifyDataSetChanged()
                        it.submitList(fotos)
                    }
                    val template = "Fotos: %s"
                    totalFotos.text = String.format(template, fotos.size)
                }else{
                    totalFotos.text = "Fotos:"
                    Toast.makeText(context, "Fecha fuera de rango", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.navigateToRoverManifest.observe(viewLifecycleOwner){
            if(it != null){
                findNavController().navigate(MarsRoverPhotosFragmentDirections.actionMarsRoverPhotosFragmentToRoverManifestFragment(it))
                viewModel.navigated()
            }
        }

        setHasOptionsMenu(true)

        viewModel.photosId.observe(viewLifecycleOwner){
            /*Log.d(TAG,"photosIds recibidos: ${viewModel.photosId.value?.size}")
            if (viewModel.photos.value != null) {
                viewModel.evalSavedPhotos()

            }*/
        }


        viewModel._fechasPerseverance.observe(viewLifecycleOwner){
            Log.d(TAG, "fechas PERSEVERANCE: $it")
        }
        viewModel._fechasCuriosity.observe(viewLifecycleOwner){
            Log.d(TAG, "fechas CURIOSITY: $it")
        }
        viewModel._fechasOpportunity.observe(viewLifecycleOwner){
            Log.d(TAG, "fechas OPPORTUNITY: $it")
        }
        viewModel._fechasSpirit.observe(viewLifecycleOwner){
            Log.d(TAG, "fechas SPIRIT: $it")
        }

        viewModel.showAlertDialog.observe(viewLifecycleOwner){
            if (it){
                val builder = AlertDialog.Builder(this.requireContext())
                builder.setMessage("Fecha ya visitada\n¿Revisar?")
                    .setCancelable(false)
                    .setPositiveButton("Confirmar"){ dialog, _ ->
                        viewModel.getFotos(true)
                    }
                    .setNegativeButton("Cancelar"){ dialog, _ ->
                        dialog.dismiss()
                    }
                builder.create().show()
                viewModel.alertDialogShowed()
            }
        }

        return binding.root
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.mars_fotos_menu, menu)
    }
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//         Log.d("xxAf","item selected: ${item.itemId}")
        when(item.itemId){
            R.id.marsPhotosListFragment -> {
                viewModel.loadSavedPhotos()
                findNavController().navigate(
                    MarsRoverPhotosFragmentDirections.actionMarsRoverPhotosFragmentToMarsPhotosListFragment()
                )
            }

            R.id.fechasVistasFragment -> {
                val fechas = when(viewModel.rover.value){
                    "perseverance" -> viewModel._fechasPerseverance.value
                    "curiosity" -> viewModel._fechasCuriosity.value
                    "opportunity" -> viewModel._fechasOpportunity.value
                    else -> viewModel._fechasSpirit.value
                }
                requireView().findNavController()
                    .navigate(MarsRoverPhotosFragmentDirections.actionMarsRoverPhotosFragmentToFechasVistasFragment2(fechas!!.toTypedArray(), viewModel.rover.value!!))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class MarsFotosAdapter(val clickListener: FotoSaveListener) : ListAdapter<DomainMarsPhoto, MarsFotosAdapter.FotoViewHolder>(MarsPhotosDiffCallback) {
        /*var datos = listOf<DomainMarsPhoto>()
            set(value) { field = value  }*/

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
            return FotoViewHolder.from(parent)
        }

        override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
            val item = getItem(position)
            holder.binding.foto = item
            holder.binding.saveBtn.setOnClickListener {
                clickListener.onClick(item)
                it.visibility = View.INVISIBLE
//                notifyItemChanged(position)   ¡¡¡¡ DA ERROR POR INTENTAR EJECUTAR EL METODO ...ViewGroup.getParect() EN OBJETO NULL !!!!
//                notifyItemChanged(holder.absoluteAdapterPosition)     ¡¡¡¡ MISMO ERROR !!!!
            }
            val pos =   if(position < 10)   String.format("0%s", (position +1).toString())
                        else                (position +1).toString()
            holder.binding.posicion.text = String.format("%s.-", pos)
            holder.binding.executePendingBindings()
        }

//        override fun getItemCount(): Int = datos.size

        class FotoViewHolder(val binding: MarsFotoItemBinding) : RecyclerView.ViewHolder(binding.root){
            companion object{
                fun from(parent: ViewGroup): FotoViewHolder{
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val binding = MarsFotoItemBinding.inflate(layoutInflater, parent, false)
                    return FotoViewHolder(binding)
                }
            }
        }

        companion object MarsPhotosDiffCallback: DiffUtil.ItemCallback<DomainMarsPhoto>(){
            override fun areItemsTheSame(
                oldItem: DomainMarsPhoto,
                newItem: DomainMarsPhoto
            ): Boolean {
//                Log.d(TAG,"areItemsTheSame")
                return oldItem === newItem
//                return oldItem.marsPhotoId == newItem.marsPhotoId
            }

            override fun areContentsTheSame(
                oldItem: DomainMarsPhoto,
                newItem: DomainMarsPhoto
            ): Boolean {
                Log.d(TAG,"areContentsTheSame")
                return oldItem.marsPhotoId == newItem.marsPhotoId && oldItem.saved == newItem.saved
//                return oldItem == newItem
            }
        }

    }

    class FotoSaveListener(val clickListener: (foto: DomainMarsPhoto) -> Unit){
        fun onClick(foto: DomainMarsPhoto) = clickListener(foto)
    }

}