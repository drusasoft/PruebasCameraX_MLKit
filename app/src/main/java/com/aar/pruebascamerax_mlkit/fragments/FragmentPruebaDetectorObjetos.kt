package com.aar.pruebascamerax_mlkit.fragments

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
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
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentDetectorObjetosBinding
import com.aar.pruebascamerax_mlkit.models.FragmentPruebaDetectorObjetosViewModel
import com.google.mlkit.vision.common.InputImage





class FragmentPruebaDetectorObjetos: Fragment()
{

    private lateinit var binding: LayoutFragmentDetectorObjetosBinding
    private val model by viewModels<FragmentPruebaDetectorObjetosViewModel>()

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



    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        //Se registran los Observers para las variables Livedata del ViewModel
        setObservers()
    }



    private fun apagarCameraX()
    {
        cameraProvider.unbindAll()
        camaraEncendida = false
        flashEncendido = false

        binding.btnFlash.hide()

        //Se limpia el ImageView del rectangulo que se hubiera dibujado en una deteccion anterior
        binding.imgViewTransparente.setImageBitmap(null)
        binding.textViewEtiquetaObjeto.text = "--"
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



    //Se dibuja el Rectangulo con lineas Rojas para marcar donde se encuentra el objeto detectado
    private fun dibujarLimiteObjeto(rect:Rect)
    {
        //Se limpia el ImageView del rectangulo que se hubiera dibujado en una deteccion anterior
        binding.imgViewTransparente.setImageBitmap(null)

        val alto = binding.imgViewTransparente.height
        val ancho = binding.imgViewTransparente.width

        //Se crea un BitmapTranparente (con el mismo tamaño que el ImageView que lo va a mostrar),
        //para crear un Canvas a partir de el y poder dibujar sobre dicho canvas.
        val bitmap = Bitmap.createBitmap(
            ancho, // Width
            alto, // Height
            Bitmap.Config.ARGB_8888 // Config
        )

        //Se instancia el canvas a partir del Bitmap anterior
        val canvas = Canvas(bitmap)

        //Se dibuja el rectangulo rojo sobre el canvas del Bitmap anteriormente
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = resources.getColor(R.color.rojo, null)
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(rect, paint)

        //Por ultimo se muestra el Bitmap creado en el ImageView de la IU
        binding.imgViewTransparente.setImageBitmap(bitmap)
    }



    //Se declaran los Observers para las Varibles de Tipo LiveData definidas en el ViewModel
    private fun setObservers()
    {

        //Se declara el Observer para la Variable objetoDetectadoLive que contiene el Obketo detectado por el Detector de Objetos
        model.objetoDetectadoLive.observe(viewLifecycleOwner){objetoDetectado->

            if(camaraEncendida)
            {
                //Se obtiene el rectacgulo que delimita el objeto detectado y se pasa a la funcion que se encarga de dibujarlo en pantalla
                val rect = objetoDetectado.boundingBox
                dibujarLimiteObjeto(rect)

                //Se comprueba si el objeto detectado tambien ha sido etiquetado por el detector y en dicho caso se muestra su etiqueta en pantalla
                if(objetoDetectado.labels.size > 0)
                    binding.textViewEtiquetaObjeto.text = objetoDetectado.labels.get(0).text
                else
                    binding.textViewEtiquetaObjeto.text = "--"

            }else
            {
                //Se limpia el ImageView del rectangulo que se hubiera dibujado en una deteccion anterior
                binding.imgViewTransparente.setImageBitmap(null)
                binding.textViewEtiquetaObjeto.text = "--"
            }

        }

    }





    //******** Clase interna donde se define el analizador de Imagen usado por CameraX, El analizado devueve Resultados cada segundo aproximadamente ********

    inner class AnalizadorImagen():ImageAnalysis.Analyzer{

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy)
        {
            imageProxy.image?.let { image ->

                val imagenAnalizar = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

                //Se pasa la imagen de la que se quiere detectar objetos dentro de la misma al Escaner que se encarga de acerlo (Definido en el ViewModel)
                model.detectarObjetos(imagenAnalizar, imageProxy)
            }
        }

    }

    //******** Fin Clase interna AnalizadorImagen ********

}