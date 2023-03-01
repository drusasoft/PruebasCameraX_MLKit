package com.aar.pruebascamerax_mlkit.models

import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions





class FragmentPruebaDetectorObjetosViewModel:ViewModel()
{

    //************************************* Variables LiveData *************************************
    private val _objetoDetectadoLive = MutableLiveData<DetectedObject>()

    val objetoDetectadoLive: LiveData<DetectedObject>
            get() = _objetoDetectadoLive
    //********************************** Fin Variables LiveData ************************************




    fun detectarObjetos(imagenAnalizar: InputImage, imageProxy: ImageProxy)
    {

        //Se configura el Detector para detectar objeto con Imagenes en Modo Live
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()//Opcional
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

            //Se guada el objeto detectado en la variable LiveData
            if(listaObjetosDetectados.size > 0 )
                _objetoDetectadoLive.value = listaObjetosDetectados.get(0)

            //Se cierra la imagen capturada por el analizador de CameraX
            imageProxy.close()

        }.addOnFailureListener {
            Log.e("Error", "Fallo Detector Objetos")
            imageProxy.close()
        }

    }

}