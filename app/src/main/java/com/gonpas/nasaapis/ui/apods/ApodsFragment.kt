package com.gonpas.nasaapis.ui.apods

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.asListDomainModel
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.databinding.ApodItemBinding
import com.gonpas.nasaapis.databinding.FragmentApodsViewerBinding
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.repository.NasaRepository

class ApodsFragment : Fragment() {


    private val viewModel: ApodsViewModel by lazy {
        // solo se puede acceder al viewmodel despues del onActivityCreated()
        val application = requireNotNull(activity).application
        val nasaRepository = NasaRepository( getDatabase(application))
        val viewModelFactory = ApodsViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory).get(ApodsViewModel::class.java)
    }

    private lateinit var apodsAdapter: ApodsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        val binding = DataBindingUtil.inflate<FragmentApodsViewerBinding>(inflater, R.layout.fragment_apods_viewer, container, false)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.apodViewModel = viewModel

        apodsAdapter = ApodsAdapter(ApodListener {
            viewModel.displayTodayApod(it)
        })

        binding.root.findViewById<RecyclerView>(R.id.apods_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = apodsAdapter
        }
        //binding.apodsList.adapter = apodsAdapter

        viewModel.allApods.observe(viewLifecycleOwner){ list ->
            apodsAdapter.let {
                it.submitList(list.asListDomainModel())
            }
//            Log.d("xxAf","Observados cambios en la lista de apods")
//            Log.d("xxAf","Total apods observados: ${list.size}")
        }

        viewModel.navigateToTodayApod.observe(viewLifecycleOwner){
            if(it != null){
                this.findNavController().navigate(ApodsFragmentDirections.actionApodsFragmentToTodayApodFragment(it))
                viewModel.doneNavigation()
            }
        }

        viewModel.errorMsg.observe(viewLifecycleOwner){
            Toast.makeText(this.context, it, Toast.LENGTH_LONG).show()
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.apods_options_menu, menu)
    }
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//         Log.d("xxAf","item selected: ${item.itemId}")
        when(item.itemId){
            R.id.masApodsFragment -> requireView().findNavController().navigate(ApodsFragmentDirections.actionApodsFragmentToMasApodsFragment())
            R.id.randomApods -> viewModel.getApodsAleatorios()
        }
        return super.onOptionsItemSelected(item)
    }
}

/**
 * ViewHolder for Apod items. All work is done by data binding.
 */
class ApodViewHolder(val binding: ApodItemBinding): RecyclerView.ViewHolder(binding.root){

    /*val title = binding.picTitle
    val copyright = binding.copyright
    val date = binding.date
    val apod = binding.apod*/

    companion object{
        fun from(parent: ViewGroup): ApodViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ApodItemBinding.inflate(layoutInflater, parent, false)
            return ApodViewHolder(binding)
        }
    }

    fun bind(item: DomainApod){
        //Log.d("xxAf","hdurl: ${item.hdurl}")
        binding.domainApod = item
        //binding.clickListener = clickListener
        binding.executePendingBindings()
    }
}

class ApodsAdapter(val clickListener: ApodListener): ListAdapter<DomainApod, ApodViewHolder>(ApodDiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApodViewHolder {
        return ApodViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ApodViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemView.setOnClickListener{
            clickListener.onClick(item)
        }

        holder.bind(item)
    }

    companion object ApodDiffCallback: DiffUtil.ItemCallback<DomainApod>(){
        override fun areItemsTheSame(oldItem: DomainApod, newItem: DomainApod): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: DomainApod, newItem: DomainApod): Boolean {
            return oldItem == newItem
        }
    }
}

class ApodListener(val clickListener: (apod: DomainApod) -> Unit){
    fun onClick(apod: DomainApod) = clickListener(apod)
}
