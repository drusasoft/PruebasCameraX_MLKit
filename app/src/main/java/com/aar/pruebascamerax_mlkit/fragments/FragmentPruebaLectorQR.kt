package com.aar.pruebascamerax_mlkit.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aar.pruebascamerax_mlkit.R
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentPruebaLectorQrBinding
import com.aar.pruebascamerax_mlkit.models.FragmentPruebasLectorQRViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors





class FragmentPruebaLectorQR: Fragment()
{

    private lateinit var binding:LayoutFragmentPruebaLectorQrBinding
    private val model by viewModels<FragmentPruebasLectorQRViewModel>()

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraControl: CameraControl
    private var camaraEncendida = false
    private var flashEncendido = false



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {

        binding = LayoutFragmentPruebaLectorQrBinding.inflate(inflater, container, false)


        //****************** Clicklisteners ******************

        binding.encenderApagarBtn.setOnClickListener { if(!camaraEncendida) iniciarCameraX() else apagarCamaraX() }

        binding.btnFlash.setOnClickListener { if(!flashEncendido) encenderFlash() else apagarFlash() }

        //****************** Fin Clicklisteners ******************


        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        //Se registran los Observers para las variables Livedata del ViewModel
        setObservers()
    }



    //Se inicia CameraX
    private fun iniciarCameraX()
    {
        model.limpiarVariablesLiveData()//Se limpia el contenido de la variable LiveData definida en el ViewModel

        //Se crea una instancia del Objeto ProcessCameraProvider, que permite vincular la camara con el  propietario del ciclo de vida (En este caso el Fragment)
        //y con esto no hay que preocuparse de abrir y cerrar la camara, ya que cameraX es consciente del ciclo de vida
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable{

            //Se obtiene un CameraProvider para vincularlo con el Ciclo de Vida  de la camara con el del Fragment
            cameraProvider = cameraProviderFuture.get()

            //Se inicializa el objeto Preview
            val preview = Preview.Builder().build()

            //Se selecciona la camara trasera usando el objeto CameraSelector
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            
            //Se asocia el analizador de Imagen definido en la clase interna AnalizadorImagen con CameraX
            //Para que el analizador de CameraX funcione correctamente con el Scanner de codigos de ML_KIT, se usa la estrategia de analisis "ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST"
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setImageQueueDepth(1)
                .build()
                .also { it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), AnalizadorImagen()) }

            try {

                //Antes de usar la camara se libera su recurso
                cameraProvider.unbindAll()

                //Se obtiene el objeto camera vinculando el objeto CameraProvider al ciclo de vida del Fragment
                val camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)

                //Se obtiene el objeto CameraControl que nos permite controlar ciertos aspectos de la camara (como por ejemplo hacer zoom, encender flash...)
                cameraControl = camera.cameraControl

                //Se muestra en el objeto PreviewView definido en el XML lo caprturado por la camara
                preview.setSurfaceProvider(binding.previewCameraX.surfaceProvider)

                //Se indica en la variable que la camara esta encendida
                camaraEncendida = true
                binding.btnFlash.show()

            }catch(exception:Exception)
            {
                Toast.makeText(requireContext(), R.string.errorCameraX, Toast.LENGTH_LONG).show()
                camaraEncendida = false
            }

        }, ContextCompat.getMainExecutor(requireContext()))//En este ultimo parametro indicamos que el Listener se ejecute en el hilo principal

    }



    //Se apaga CameraX, liberando los recursos
    private fun apagarCamaraX()
    {
        cameraProvider.unbindAll()
        camaraEncendida = false
        flashEncendido = false

        binding.btnFlash.hide()
    }



    private fun encenderFlash()
    {
        cameraControl.enableTorch(true)
        flashEncendido = true
    }



    private fun apagarFlash()
    {
        cameraControl.enableTorch(false)
        flashEncendido = false
    }


    //Se declaran los observers para las variables LiveData definidas en el ViewModel
    private fun setObservers()
    {

        //Observer para la variable tipoCodigoLive que contiene el tipo de Codigo Escaneado
        model.tipoCodigoLive.observe(viewLifecycleOwner){tipoCodigo->

            if(tipoCodigo != "")
            {
                //Una vez capturado el codigo correctamente, se detiene la Camara
                apagarCamaraX()

                val textoDialogo = "Codigo escaneado del tipo: ${tipoCodigo}"

                //Se muestra un Cuadro de Dialogo, informando del tipo de codigo escaneado
                MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered)
                    .setTitle("Codigo Escaneado")
                    .setMessage(textoDialogo)
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show()
            }

        }

    }



    //******** Clase interna donde se define el analizador de Imagen usado por CameraX, El analizado devueve Resultados cada segundo aproximadamente ********

    inner class AnalizadorImagen():ImageAnalysis.Analyzer
    {

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {

            imageProxy.image?.let { image->

                //Se crea la imagen que se va a pasar al scaner de codigos QR, dicha imagen se contruye pasando la Imagen obtenida por el Analizador de CameraX y su rotacion en grados
                val imagenAnalizar = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

                //Se pasa la imagen al Escaner de CodigosQR (Definido en el ViewModel)
                model.escanearCodigoQR(imagenAnalizar, imageProxy)
            }

        }

    }

    //******** Fin Clase interna AnalizadorImagen ********


}