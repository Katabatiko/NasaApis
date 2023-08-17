package com.gonpas.nasaapis.ui.marsroverphotos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.FechaItemBinding
import com.gonpas.nasaapis.databinding.FragmentListaFechasMarteBinding
import com.gonpas.nasaapis.domain.DomainFechaVista

private val TAG = "xxFvf"

class FechasVistasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentListaFechasMarteBinding>(inflater, R.layout.fragment_lista_fechas_marte, container, false)

        binding.lifecycleOwner = this

        val datos: List<DomainFechaVista> = FechasVistasFragmentArgs.fromBundle(requireArguments()).listaFechas.asList()
//        Log.d(TAG,"lista fechas: $datos")

        binding.rover = FechasVistasFragmentArgs.fromBundle(requireArguments()).rover

        val adapter = FechasVistasAdapter()
        binding.fechasVistas.adapter = adapter
        adapter.data = datos

        return binding.root
    }
}

class FechasVistasAdapter : RecyclerView.Adapter<FechasVistasAdapter.FechaViewHolder>(){

    var data = listOf<DomainFechaVista>()
        set(value) { field = value }

    class FechaViewHolder(val binding: FechaItemBinding) : RecyclerView.ViewHolder(binding.root){

        companion object{
            fun from(parent: ViewGroup): FechaViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FechaItemBinding.inflate(layoutInflater, parent,false)
                return FechaViewHolder(binding)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FechaViewHolder {
        return FechaViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FechaViewHolder, position: Int) {
        val item = data[position]
        holder.binding.fechaVista = item
    }

    override fun getItemCount(): Int {
        return data.size
    }
}