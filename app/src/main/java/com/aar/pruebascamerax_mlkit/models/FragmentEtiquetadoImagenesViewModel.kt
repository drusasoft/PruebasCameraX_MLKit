package com.aar.pruebascamerax_mlkit.models

import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.text.DecimalFormat





class FragmentEtiquetadoImagenesViewModel:ViewModel()
{

    //************************************* Variables LiveData *************************************
    private val _etiquetaOro = MutableLiveData<String>()
    private val _etiquetaPlata = MutableLiveData<String>()
    private val _etiquetaBronze = MutableLiveData<String>()

    val etiquetaOro: LiveData<String>
        get() = _etiquetaOro

    val etiquetaPlata: LiveData<String>
        get() = _etiquetaPlata

    val etiquetaBronze: LiveData<String>
        get() = _etiquetaBronze

    //********************************** Fin Variables LiveData ************************************


    private val decimalFormat = DecimalFormat("#.##")


    //Metodo donde se configura y ejecuta el Etiquetador de Imagenes
    fun etiquetarImagenes(imagenAnalizar: InputImage, imageProxy: ImageProxy) {

        //Se configura y ejecuta el Etiquetador de Imagenes, dicho etiquetador de imnagenes lo configura para que solo eqtiquete aquellos objetos con mas de un 70% de coincidencia
        /*val options = ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.7f)
                        .build()*/

        val etiquetador = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        etiquetador.process(imagenAnalizar).addOnSuccessListener { listaEtiquetas ->

            //Muestro en los TextView definidos en la Interfaz De Usuario, las 3 priomeras etiquetas de la lista obtenida,
            //ya que esas 3 primeras son las que tendran mayor porcentaje de coincidencia
            when (listaEtiquetas.size) {

                0 -> {
                    _etiquetaOro.value = "--"
                    _etiquetaPlata.value = "--"
                    _etiquetaBronze.value = "--"
                }

                1 -> {

                    val coincidencia1 = decimalFormat.format(listaEtiquetas.get(0).confidence * 100)

                    _etiquetaOro.value = "${listaEtiquetas.get(0).text} - ${coincidencia1}%"
                    _etiquetaPlata.value = "--"
                    _etiquetaBronze.value = "--"
                }

                2 -> {

                    val coincidencia1 = decimalFormat.format(listaEtiquetas.get(0).confidence * 100)
                    val coincidencia2 = decimalFormat.format(listaEtiquetas.get(1).confidence * 100)

                    _etiquetaOro.value = "${listaEtiquetas.get(0).text} - ${coincidencia1}%"
                    _etiquetaPlata.value = "${listaEtiquetas.get(1).text} - ${coincidencia2}%"
                    _etiquetaBronze.value = "--"
                }

                3 -> {

                    val coincidencia1 = decimalFormat.format(listaEtiquetas.get(0).confidence * 100)
                    val coincidencia2 = decimalFormat.format(listaEtiquetas.get(1).confidence * 100)
                    val coincidencia3 = decimalFormat.format(listaEtiquetas.get(2).confidence * 100)

                    _etiquetaOro.value = "${listaEtiquetas.get(0).text} - ${coincidencia1}%"
                    _etiquetaPlata.value = "${listaEtiquetas.get(1).text} - ${coincidencia2}%"
                    _etiquetaBronze.value = "${listaEtiquetas.get(2).text} - ${coincidencia3}%"
                }

            }


            //Se cierra la imagen capturada por el analizador de CameraX
            imageProxy.close()


        }.addOnFailureListener {
            Log.e("Error", "Fallo Etiquetador Imagenes")
            imageProxy.close()
        }

    }


}