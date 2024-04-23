package com.aar.pruebascamerax_mlkit.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.aar.pruebascamerax_mlkit.R
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentReconocimientoTextoBinding
import com.aar.pruebascamerax_mlkit.models.FragmentPruebaReconocimientoTextoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.common.InputImage





class FragmentPruebaReconocimientoTexto:Fragment()
{

    private lateinit var binding:LayoutFragmentReconocimientoTextoBinding
    private val model by viewModels<FragmentPruebaReconocimientoTextoViewModel>()

    private lateinit var cameraProvider:ProcessCameraProvider
    private lateinit var cameraControl:CameraControl

    private var camaraEncendida:Boolean = false
    private var flashEncendido:Boolean = false
    private var capturaIniciada:Boolean = false



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = LayoutFragmentReconocimientoTextoBinding.inflate(inflater, container, false)


        //Se muestra el menu de opcione en la ToolBar
        crearMenuOpciones()


        //************************************ Clicklisteners **************************************

        binding.encenderApagarBtn.setOnClickListener { if(!camaraEncendida) iniciarCameraX() else apagarCameraX() }

        binding.btnFlash.setOnClickListener { if(flashEncendido) apagarFlash() else encederFlash() }

        binding.btnCaptura.setOnClickListener { if(!capturaIniciada) capturaIniciada = true }

        //************************************ Fin Clicklisteners **********************************


        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        //Se registran los Observers para la variables LiveData
        setObservers()
    }



    private fun crearMenuOpciones()
    {

        val menuHost:MenuHost = requireActivity()

        menuHost.addMenuProvider(object:MenuProvider{

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater)
            {
                menuInflater.inflate(R.menu.menu_reconocimiento_texto, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                //Se configura el Reconocedor de Texto para el tipo de caracter indicado por el usuario
                when(menuItem.itemId)
                {
                    R.id.menu_caracteres_latinos->{ }
                    R.id.menu_caracteres_Devanagari->{ }
                    R.id.menu_caracteres_Japonenes->{ }
                    R.id.menu_caracteres_Koreanos->{ }
                    R.id.menu_caracteres_Chinos->{ }
                }

                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }


    //Se inica Camera X
    private fun iniciarCameraX()
    {
        //Se crea una instancia del Objeto ProcessCameraProvider, que permite vincular la camara con el propietario del ciclo de vida (En este caso el Fragment)
        //y con esto no hay que preocuparse de abrir y cerrar la camara, ya que cameraX es consciente del ciclo de vida
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

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

            try{

                //Se obtiene el objeto camera vinculando el objeto CameraProvider al ciclo de vida de la actividad y se le asocia la camara seleccionada y el analizador de imagen definido previamente
                //val camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview)//******* para Pruebas se crea el objeto camera sin asociarle un analizador de imagenes
                val camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)

                //Se obtiene el objeto CameraControl que nos permite controlar ciertos aspectos de la camara (como por ejemplo hacer zoom, encender flash...)
                cameraControl = camera.cameraControl

                //Se muestra en el objeto PreviewView definido en el XML lo caprturado por la camara
                preview.setSurfaceProvider(binding.previewCameraX.surfaceProvider)

                //Se indica en la variable que la camara esta encendida y se muestran el boton para encender y apagar el flash
                camaraEncendida = true

                //Se muestran el boton para encender y apagar el flash, Btn de inicar captura
                binding.btnFlash.show()
                binding.btnCaptura.show()

            }catch(exception:Exception)
            {
                Toast.makeText(requireContext(), R.string.errorCameraX, Toast.LENGTH_LONG).show()
                camaraEncendida = false
            }


        }, ContextCompat.getMainExecutor(requireContext()))//En este ultimo parametro indicamos que el Listener se ejecute en el hilo principal

    }



    //Se apaga CameraX, liberando los recursos
    private fun apagarCameraX()
    {
        cameraProvider.unbindAll()
        camaraEncendida = false
        capturaIniciada = false

        binding.btnFlash.hide()
        binding.btnCaptura.hide()

    }



    //Se enciende el flash de la camara
    private fun encederFlash()
    {
        cameraControl.enableTorch(true)
        flashEncendido = true
    }



    //se apaga el flash de la camara
    private fun apagarFlash()
    {
        cameraControl.enableTorch(false)
        flashEncendido = false
    }



    //Se registran los observers para las variables LiveData definidas en el ViewModel
    private fun setObservers()
    {

        //Observer para la variable LiveData que indica si la operacion de Reconocimento de Texto ha siso exitosa o no
        model.resultadoCapturaLive.observe(viewLifecycleOwner){resultadoCaptura->

            when(resultadoCaptura)
            {
                "OK"->{
                    Toast.makeText(requireContext(), "Reconocimiento Texto Correcto", Toast.LENGTH_LONG).show()
                    apagarCameraX()//Se apaga CameraX
                }

                "KO"->{
                    Toast.makeText(requireContext(), "Error, no se ha podido reconocer texto", Toast.LENGTH_LONG).show()
                    apagarCameraX()//Se apaga CameraX
                }
            }

        }


        //Variable liveData que contiene el texto reconocido en la imagen analizado
        model.resultadotextoLive.observe(viewLifecycleOwner){textoReconocido->

            if(textoReconocido != "")
                mostrarTextoCapturado(textoReconocido)//Se muestra el texto capturado en un cuadro de dialogo
        }

    }




    //Se muestra un cuado de dialogo con el texto capturado
    private fun mostrarTextoCapturado(textoCapturado:String)
    {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.titDialogTextoCapturado)
            .setMessage(textoCapturado)
            .setPositiveButton(R.string.btnAceptar, null)
            .setCancelable(false)
            .show()
    }




    //Clase interna donde se define el analizador de Imagen usado por CameraX, El analizado devueve Resultados cada segundo aproximadamente
    inner class AnalizadorImagen():ImageAnalysis.Analyzer
    {

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy)
        {

            //La Captura y anlisis de la Imagen por parte de Camera X solo se realiza cuando el usuario pulsa el boton iniciar Capturar
            if(capturaIniciada)
            {

                //Se obtiene la imagen obtenida por CamaraX
                imageProxy.image?.let {image->

                    val imagenAnalizar = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

                    //Se pasa la imagen obtenida por la camara al metodo definido en el ViewModel encargado de reconocer el texto de la Imagen
                    model.reconocimientoTexto(imagenAnalizar, imageProxy)

                }

            }else { imageProxy.close() }//Se cierra la imagen capturada por el analizador de CameraX

        }

    }


}