package com.gonpas.nasaapis.ui.epic

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.EpicItemBinding
import com.gonpas.nasaapis.databinding.FragmentEpicThumbsBinding
import com.gonpas.nasaapis.domain.DomainEpic

class EpicThumsFragment : Fragment() {

    private val viewModel: EpicThumbsViewModel by lazy {
        val application = requireNotNull(activity).application
        val viewModelFactory = EpicThumbsViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[EpicThumbsViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentEpicThumbsBinding>(inflater, R.layout.fragment_epic_thumbs, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = EpicThumbsAdapter()
        binding.epicsThumbs.adapter = adapter

        viewModel.epics.observe(viewLifecycleOwner){
            it.let { adapter.datos = it }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}

class EpicThumbsAdapter : RecyclerView.Adapter<EpicThumbsAdapter.EpicViewHolder>() {

    var datos = listOf<DomainEpic>()
        set(value) { field = value}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpicViewHolder {
        return EpicViewHolder.from(parent)
    }

    override fun getItemCount() = datos.size

    override fun onBindViewHolder(holder: EpicViewHolder, position: Int) {
        val item = datos[position]
        holder.binding.epic = item
        holder.binding.executePendingBindings()
    }

    class EpicViewHolder(val binding: EpicItemBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun from(parent: ViewGroup) : EpicViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EpicItemBinding.inflate(layoutInflater, parent, false)
                return EpicViewHolder(binding)
            }
        }

    }
}