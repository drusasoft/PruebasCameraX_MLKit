package com.aar.pruebascamerax_mlkit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aar.pruebascamerax_mlkit.databinding.LayoutFrgamentEtiquetadoImagenesBinding



class FragmentPruebaEtiquetadoImagenes():Fragment()
{

    private lateinit var binding: LayoutFrgamentEtiquetadoImagenesBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = LayoutFrgamentEtiquetadoImagenesBinding.inflate(inflater, container, false)





        return binding.root
    }

}