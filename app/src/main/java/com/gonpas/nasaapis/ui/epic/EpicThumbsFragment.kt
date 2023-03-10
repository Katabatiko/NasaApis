package com.gonpas.nasaapis.ui.epic

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.EpicItemBinding
import com.gonpas.nasaapis.databinding.FragmentEpicThumbsBinding
import com.gonpas.nasaapis.domain.DomainEpic

private const val TAG = "xxEtf"

class EpicThumbsFragment : Fragment() {

    private val viewModel: EpicThumbsViewModel by lazy {
        val application = requireNotNull(activity).application
        val viewModelFactory = EpicThumbsViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[EpicThumbsViewModel::class.java]
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentEpicThumbsBinding>(inflater, R.layout.fragment_epic_thumbs, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val btnColeccion = binding.btnCollection
        btnColeccion.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setCollection(isChecked)
            viewModel.getLastEpic()
        }


        val adapter = EpicThumbsAdapter(EpicThumbsAdapter.OnClickListener{
            viewModel.goFullscreen(it)
        }, viewModel)
        binding.epicsThumbs.adapter = adapter

        viewModel.epics.observe(viewLifecycleOwner){list ->
//            Log.d("xxEtf","it size: ${list.size}")
            adapter.let { it.datos = list }
            adapter.notifyDataSetChanged()
//            Log.d(TAG,"adapter datos size: ${adapter.datos.size}")
        }

        /*viewModel.coleccion.observe(viewLifecycleOwner){
            viewModel.getLastEpic()
        }*/

        viewModel.navigateToFullScreenEpic.observe(viewLifecycleOwner){
            if (it != null){
                val imageUrl = "https://epic.gsfc.nasa.gov/archive/%s/%s/%s/%s/png/%s.png"
                val fecha = it.date.split(" ")[0].split("-")
                val url = imageUrl.format(viewModel.coleccion.value, fecha[0], fecha[1], fecha[2], it.imageName)
                findNavController().navigate(EpicThumbsFragmentDirections.actionEpicThumsFragmentToEpicFullscreenFragment(url, it.date.split(" ")[1]))
                viewModel.navigated()
            }
        }

        viewModel.navigateToSliderEpics.observe(viewLifecycleOwner){
//            Log.d(TAG,"Observados cambios en navigate to slider")
            if(it.isNotEmpty()){
                findNavController().navigate(EpicThumbsFragmentDirections.actionEpicThumsFragmentToEpicSliderFragment(it.toTypedArray(), viewModel.coleccion.value!!))
                viewModel.navigatedToSlider()
            }
        }


        binding.year.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setAnno(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.month.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setMes(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.day.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.setDia(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) { }
        })


        return binding.root
    }
}

class EpicThumbsAdapter(private val onClickListener: OnClickListener, val viewModel: EpicThumbsViewModel) : RecyclerView.Adapter<EpicThumbsAdapter.EpicViewHolder>() {

    var datos = listOf<DomainEpic>()
        set(value) { field = value }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpicViewHolder {
        return EpicViewHolder.from(parent)
    }

    override fun getItemCount() = datos.size

    override fun onBindViewHolder(holder: EpicViewHolder, position: Int) {
        val item = datos[position]
        holder.itemView.setOnClickListener{
            onClickListener.onClick(item)
        }
        holder.binding.epic = item
        holder.binding.viewModel = viewModel
       // Log.d(TAG,"item: ${item.imageName}")
        holder.binding.executePendingBindings()
    }

    class EpicViewHolder(val binding: EpicItemBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun from(parent: ViewGroup) : EpicViewHolder{
                //Log.d(TAG","Creango epic view holder")
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EpicItemBinding.inflate(layoutInflater, parent, false)
                return EpicViewHolder(binding)
            }
        }
    }
    /**
     * Custom listener that handles clicks on [RecyclerView] items.  Passes the [DomainEpic]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [DomainEpic]
     */
    class OnClickListener(val clickListener: (epic: DomainEpic) -> Unit){
        fun onClick(epic: DomainEpic) = clickListener(epic)
    }
}