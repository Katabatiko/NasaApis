package com.gonpas.nasaapis.ui.marsroverphotos

import android.app.Application
import android.graphics.Color
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
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.FragmentMarsRoverPhotosBinding
import com.gonpas.nasaapis.databinding.MarsFotoItemBinding
import com.gonpas.nasaapis.network.RoversPhotosDTO
import com.gonpas.nasaapis.ui.apods.ApodsFragmentDirections

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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
        ViewModelProvider(this, viewModelFactory).get(MarsRoverPhotosViewModel::class.java)
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

        })
        binding.marsFotos.adapter = adapter

        viewModel.photos.observe(viewLifecycleOwner){ fotos ->
            if (fotos != null){
                if (fotos.isNotEmpty()) {
                    adapter.let {
                        it.datos = fotos.asList()
                        it.notifyDataSetChanged()
                    }
                }else{
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.mars_fotos_menu, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//         Log.d("xxAf","item selected: ${item.itemId}")
        when(item.itemId){
            R.id.marsPhotosListFragment -> requireView().findNavController().navigate(
                MarsRoverPhotosFragmentDirections.actionMarsRoverPhotosFragmentToMarsPhotosListFragment())

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

    class MarsFotosAdapter(val clickListener: FotoSaveListener) : RecyclerView.Adapter<MarsFotosAdapter.FotoViewHolder>() {
        var datos = listOf<RoversPhotosDTO>()
            set(value) { field = value  }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
            return FotoViewHolder.from(parent)
        }

        override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
            val item = datos[position]
            holder.binding.foto = item
            holder.binding.saveBtn.setOnClickListener {
                clickListener.onClick(item)
                it.isEnabled = false
                it.setBackgroundResource(R.color.gris_claro)
            }
            holder.binding.executePendingBindings()
        }

        override fun getItemCount(): Int = datos.size

        class FotoViewHolder(val binding: MarsFotoItemBinding) : RecyclerView.ViewHolder(binding.root){
            companion object{
                fun from(parent: ViewGroup): FotoViewHolder{
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val binding = MarsFotoItemBinding.inflate(layoutInflater, parent, false)
                    return FotoViewHolder(binding)
                }
            }
        }

    }

    class FotoSaveListener(val clickListener: (foto: RoversPhotosDTO) -> Unit){
        fun onClick(foto: RoversPhotosDTO) = clickListener(foto)
    }
}