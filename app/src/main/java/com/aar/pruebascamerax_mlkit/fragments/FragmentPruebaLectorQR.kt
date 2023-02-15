package com.aar.pruebascamerax_mlkit.fragments

import android.annotation.SuppressLint
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
import com.aar.pruebascamerax_mlkit.R
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentPruebaLectorQrBinding
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class FragmentPruebaLectorQR: Fragment()
{

    private lateinit var binding:LayoutFragmentPruebaLectorQrBinding

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



    //Se inicia CameraX
    private fun iniciarCameraX()
    {
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


    //********** para pruebas
    private fun escanearCodigoQR(imagen:InputImage, imageProxy: ImageProxy)
    {
        //Log.e("Pruebas", "escanearCodigoQR")

        //Se configura el Escaner de Codigos, para que solo pueda leer codigos con formaro QR y AZTEC (Asi va mas Rapido)
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC).build()

        //Se inicia el Escaner de Codigos indicandole los formatos de Codigos permitidos
        val scanner = BarcodeScanning.getClient(options)

        //Se procesa la imagen pasada como parametro, para leer el CodigoQR
        val result = scanner.process(imagen).addOnSuccessListener { codigosQR->

            Log.e("Scanneo OK", "Codigo Escaneado Correctamente")

            codigosQR.forEach {

                val valueType = it.valueType

                when(valueType)
                {
                    Barcode.TYPE_PHONE->{ Log.e("CodigoQR", "Tipo LLamda") }

                    Barcode.TYPE_EMAIL->{ Log.e("CodigoQR", "Tipo Email") }

                    Barcode.TYPE_SMS->{ Log.e("CodigoQR", "Tipo SMS") }

                    else ->{ Log.e("Mierda", "No es Codigo") }
                }

            }

            imageProxy.close()



        }.addOnCanceledListener {
            Log.e("Error", "Escaneo Cancelado")
            imageProxy.close()
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

                //Se pasa la imagen al Escaner de CodigosQR
                escanearCodigoQR(imagenAnalizar, imageProxy)

            }

        }

    }

    //******** Fin Clase interna AnalizadorImagen ********


}