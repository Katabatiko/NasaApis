package com.gonpas.nasaapis.ui.apods

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.databinding.FragmentMasApodsBinding

class MasApodsFragment : Fragment() {

    private val viewModel: MasApodsViewModel by lazy {
        // solo se puede acceder al viewmodel despues del onActivityCreated()
        val application = requireNotNull(activity).application
        val viewModelFactory = MasApodsViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[MasApodsViewModel::class.java]
    }

//    private lateinit var viewModel: MasApodsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentMasApodsBinding>(inflater, R.layout.fragment_mas_apods, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val dia = binding.dia
        val mes = binding.mes
        val anno = binding.anno

        dia.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("xxMaf","dia: $p0")
                viewModel.setDia(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        mes.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("xxMaf","mes: $p0")
                viewModel.setMes(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        anno.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("xxMaf","año: $p0")
                viewModel.setAnno(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        viewModel.apod.observe(viewLifecycleOwner){
            Log.d("xxMaf","apod observer: $it")
            if (it != null) {
                this.findNavController().navigate(
                    MasApodsFragmentDirections.actionMasApodsFragmentToTodayApodFragment(it)
                )
                viewModel.navigated()
            }
        }

        return binding.root
    }

    

}