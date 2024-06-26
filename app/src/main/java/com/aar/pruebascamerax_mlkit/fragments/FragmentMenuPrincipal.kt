package com.aar.pruebascamerax_mlkit.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.aar.pruebascamerax_mlkit.R
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentMenuPrincipalBinding
import com.google.android.material.snackbar.Snackbar
import java.util.jar.Manifest


class FragmentMenuPrincipal: Fragment()
{

    private lateinit var navController: NavController

    //Para comprobar los permisos el metodo onRequestPermissionsResult() esta deprecado, ahora se usa un ActivityResult (registerForActivityResult)
    private lateinit var requestPermissionLauncher:ActivityResultLauncher<String>



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        val binding = LayoutFragmentMenuPrincipalBinding.inflate(inflater, container, false)


        //************************************ Clicklisteners ************************************

                //Click en la Opcion Prueba Camera X
        binding.txtPruebaCameraX.setOnClickListener {

            //Primero se comprueba si se dispone de permiso del usuario para usar la Camara
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

                navController.navigate(R.id.irFragmentPruebaCameraX)
            else
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA) //Se solicita permiso al usuario para usar la camara

        }


                //Click en la Opcion Prueba LectorQR
        binding.txtPruebaLectorQR.setOnClickListener {

            //Primero se comprueba si se dispone de permiso del usuario para usar la Camara
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                navController.navigate(R.id.irFragmentPruebaLectorQR)
            else
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA) //Se solicita permiso al usuario para usar la camara
        }


                //Click en la Opcion Etiquetador Imagenes
        binding.txtPruebaEtiquetadoImagenes.setOnClickListener {

            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                navController.navigate(R.id.irFragmentPruebaEtiquetadoImagenes)
            else
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }


                //Click en la Opcion Detector Objetos
        binding.txtPruebaDetectorObjetos.setOnClickListener {

            //Primero se comprueba si se dispone de permiso del usuario para usar la Camara
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                navController.navigate(R.id.irFragmentPruebaDetectorObjetos)
            else
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)//Se solicita permiso al usuario para usar la camara

        }


                //Click  en la opcion Reconocimiento de Text
        binding.txtPruebaReconocimientoTexto.setOnClickListener {

            //Primero se comprueba si se dispone de permiso del usuario para usar la Camara
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                navController.navigate(R.id.irFragmentPruebaReconocimientoTexto)
            else
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)

        }

        //************************************ Fin Clicklisteners ************************************



        //****************** ActivityResult para Permisos ******************

        //Se registra este ActivityResult que se ejecutara cuando se se solicta al usaurio permiso para usara la camara
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){permiso->

            if(permiso)
                Snackbar.make(binding.layoutFragmentMenuPrincipal, "Permiso Concedido", Snackbar.LENGTH_LONG).show()
            else
                Snackbar.make(binding.layoutFragmentMenuPrincipal, "Permiso Rechazado", Snackbar.LENGTH_LONG).show()
        }

        //****************** Fin ActivityResult para Permisos ******************


        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        //Se instancia el Objeto navController
        navController = Navigation.findNavController(view)
    }


}