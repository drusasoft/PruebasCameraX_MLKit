package com.aar.pruebascamerax_mlkit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentPruebaCameraxBinding

class FragmentPruebaCameraX: Fragment()
{

    private lateinit var binding:LayoutFragmentPruebaCameraxBinding

    private lateinit var preview: Preview
    private lateinit var camera: Camera
    private lateinit var cameraControl: CameraControl
    private val camaraEncendida:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {

        binding = LayoutFragmentPruebaCameraxBinding.inflate(inflater, container, false)

        //****************** Clicklisteners ******************

        binding.encenderApagarBtn.setOnClickListener {

            if(!camaraEncendida) iniciarCameraX() else apagarCameraX()
        }

        //****************** Fin Clicklisteners ******************

        return binding.root

    }



    private fun iniciarCameraX()
    {
        //Se crea una instancia del Objeto ProcessCameraProvider, que permite vincular la camara con el  propietario del ciclo de vida (En este caso el Fragment)
        //y con esto no hay que preocuparse de abrir y cerrar la camara, ya que cameraX es consciente del ciclo de vida
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable{

            //Se obtiene un CameraProvider para vincularlo con el Ciclo de Vida  de la camara con el del Fragment
            val cameraProvider = cameraProviderFuture.get()

            //Se inicializa el objeto Preview
            preview = Preview.Builder().build()

            //Se selecciona la camara trasera usando el objeto CameraSelector
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()



        }, ContextCompat.getMainExecutor(requireContext()))//En este ultimo parametro indicamos que el Listener se ejecute en el hilo principal


    }


    private fun apagarCameraX()
    {

    }


}