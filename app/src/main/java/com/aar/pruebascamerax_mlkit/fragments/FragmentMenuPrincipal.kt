package com.aar.pruebascamerax_mlkit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentMenuPrincipalBinding


class FragmentMenuPrincipal: Fragment()
{

    private lateinit var navController: NavController


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        val binding = LayoutFragmentMenuPrincipalBinding.inflate(inflater, container, false)



        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        //Se instancia el Objeto navController
        navController = Navigation.findNavController(view)
    }


}