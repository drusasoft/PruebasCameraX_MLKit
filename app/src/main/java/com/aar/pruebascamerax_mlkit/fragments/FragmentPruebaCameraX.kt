package com.aar.pruebascamerax_mlkit.fragments

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
import com.aar.pruebascamerax_mlkit.databinding.LayoutFragmentPruebaCameraxBinding
import java.nio.ByteBuffer


class FragmentPruebaCameraX: Fragment()
{

    private lateinit var binding:LayoutFragmentPruebaCameraxBinding

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraControl: CameraControl
    private var camaraEncendida:Boolean = false

    private var nivelZoom = 0
    private var zoom = 0.0f
    private var flashEncendido = false



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {

        binding = LayoutFragmentPruebaCameraxBinding.inflate(inflater, container, false)


        //****************** Clicklisteners ******************

        binding.encenderApagarBtn.setOnClickListener { if(!camaraEncendida) iniciarCameraX() else apagarCameraX() }

        binding.btnFlash.setOnClickListener { if(!flashEncendido) encenderFlash() else apagarFlash() }

        binding.btnZoomMas.setOnClickListener { aumentarZoom() }

        binding.btnZoomMenos.setOnClickListener { reducirZoom() }

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
            val imageAnalyzer = ImageAnalysis.Builder().build().also { it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), AnalizadorImagen()) }


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
                binding.btnZoomMas.show()
                binding.btnZoomMenos.show()

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

        binding.btnZoomMas.hide()
        binding.btnZoomMenos.hide()
        binding.btnFlash.hide()
    }



    private fun aumentarZoom()
    {
        if(nivelZoom < 9)
        {
            nivelZoom++
            zoom += 0.1F
            cameraControl.setLinearZoom(zoom)
            Log.e("Nivel Zoom", "${zoom}")
        }
    }



    private fun reducirZoom()
    {
        if(nivelZoom > 0)
        {
            nivelZoom--
            zoom -= 0.1F
            cameraControl.setLinearZoom(zoom)
            Log.e("Nivel Zoom", "${zoom}")
        }
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




    //Clase interna donde se define el analizador de Imagen usado por CameraX, El analizado devueve Resultados cada segundo aproximadamente
    private class AnalizadorImagen():ImageAnalysis.Analyzer
    {

        private fun ByteBuffer.toByteArray(): ByteArray
        {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        //En este metodo es donde se recibe la imagen capturada por los camara para que podamos realizar con ella la accion que queramos (Analizarla...etc)
        override fun analyze(image: ImageProxy)
        {
            //Se analiza la imagen obtenida por la Camara para obtener el nivel de luminosidad de la misma
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            //Mostramos en un Log el nivel de luminosidad de la Imagen Capturada por la Camara
            Log.e("Analizador de Luminosidad", "${luma}")

            image.close()
        }

    }

}