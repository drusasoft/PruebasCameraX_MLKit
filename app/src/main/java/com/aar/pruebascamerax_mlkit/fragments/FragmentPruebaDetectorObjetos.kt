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
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentDetectorObjetosBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions


class FragmentPruebaDetectorObjetos: Fragment()
{

    private lateinit var binding: LayoutFragmentDetectorObjetosBinding

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraControl: CameraControl
    private var camaraEncendida = false
    private var flashEncendido = false




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = LayoutFragmentDetectorObjetosBinding.inflate(inflater, container, false)


        //****************** Clicklisteners ******************

        binding.encenderApagarBtn.setOnClickListener { if(!camaraEncendida) iniciarCameraX() else apagarCameraX() }

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
            //Para que el analizador de CameraX funcione correctamente con el Scanner de etiquetado de imagenes de ML_KIT, se usa la estrategia de analisis "ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST"
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



    private fun apagarCameraX()
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



    //*********** Para pruebas
    fun detectarObjetos(imagenAnalizar: InputImage, imageProxy: ImageProxy)
    {

        //Se configura el Detector para detectar objeto con Imagenes en Modo Live
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            //.enableClassification()//Opcional
            .build()

        //Se configura el Detector para detectar multiples objeto en Imagenes Estaticas
        /*val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()//Opcional
            .build()*/


        //Se crea el detector de objetos con la configuracion que hemos indicado previamente
        val detectorObjetos = ObjectDetection.getClient(options)

        //Se pasa la imagen a Analizar al Detector de Objeto que hemos Creado
        detectorObjetos.process(imagenAnalizar).addOnSuccessListener {listaObjetosDetectados->

            if(listaObjetosDetectados.size > 0 )
            {
                val rect = listaObjetosDetectados.get(0).boundingBox
                Log.e("Objeto Detectado", "Posicion: ${rect}")


                if(listaObjetosDetectados.get(0).labels.size > 0)
                {
                    val etiqueta = listaObjetosDetectados.get(0).labels.get(0).text
                    Log.e("Nombre Objeto Detectado", "${etiqueta}")
                }

            }


            //Se cierra la imagen capturada por el analizador de CameraX
            imageProxy.close()

        }
        .addOnFailureListener {
            Log.e("Error", "Fallo Detector Objetos")
            imageProxy.close()
        }

    }


    //******** Clase interna donde se define el analizador de Imagen usado por CameraX, El analizado devueve Resultados cada segundo aproximadamente ********

    inner class AnalizadorImagen():ImageAnalysis.Analyzer{

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy)
        {
            imageProxy.image?.let { image ->

                val imagenAnalizar = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

                //Se pasa la imagen de la que se quiere detectar objetos dentro de la misma al Escaner que se encarga de acerlo
                detectarObjetos(imagenAnalizar, imageProxy)
            }
        }

    }

    //******** Fin Clase interna AnalizadorImagen ********

}