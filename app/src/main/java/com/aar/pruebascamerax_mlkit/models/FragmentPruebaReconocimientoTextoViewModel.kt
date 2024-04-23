package com.aar.pruebascamerax_mlkit.models

import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions





class FragmentPruebaReconocimientoTextoViewModel:ViewModel()
{


    //************************************* Variables LiveData *************************************
    private val _resultadoTextoLive = MutableLiveData<String>()
    private val _resultadoCaptutaLive = MutableLiveData<String>()

    val resultadotextoLive: LiveData<String>
        get() = _resultadoTextoLive

    val resultadoCapturaLive: LiveData<String>
        get() = _resultadoCaptutaLive
    //********************************** Fin Variables LiveData ************************************

    //Por defecto el reconocedor de Texto se configura para reconocer caracteres latino
    private var reconocedorText = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)




    //se configura el Reconocedor de Texto para que detecte el tipo de caracteres indicado por el usaurio
    fun configurarReconocedorTexto(tipoCaracteres:String)
    {
        when(tipoCaracteres)
        {
            "Latino"->{ reconocedorText.optionalFeatures }
            "Devanagari"->{ reconocedorText = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build()) }
            "Japonenes"->{ reconocedorText = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())}
            "Koreanos"->{ reconocedorText = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build()) }
            "Chinos"->{ reconocedorText = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build()) }
        }

    }



    //Se obtiene el texto de la imagen obtenida desde Camera X
    fun reconocimientoTexto(imagenAnalizar: InputImage, imageProxy: ImageProxy)
    {
        //Antes de iniciar el proceso de reconocimiento de texto, se limpia el contenido de las variables Livedata
        _resultadoTextoLive.value = ""
        _resultadoCaptutaLive.value = ""

        //Se pasa la imagen obtenida por Camera X al reconocedor de texto que se encarga de analizar y detectar el texto que contenga
        reconocedorText.process(imagenAnalizar)
            .addOnSuccessListener {textoCapturado->
                //La imagen se ha procesado correctamente y se ha detectado texto
                Log.e("Texto Capturado", textoCapturado.text)

                //Se guarda el texto capturado en la variable LiveData
                _resultadoTextoLive.value = textoCapturado.text

                //Se indica en la variable LiveData _resultadoCaptutaLive, que el resultado de la captura ha sido OK
                _resultadoCaptutaLive.value = "OK"

                imageProxy.close()//Se cierra la imagen capturada por el analizador de CameraX

            }
            .addOnFailureListener {exception->
                //Se ha producido un error durante el procesamiento de la imagen

                //Se indica en la variable LiveData _resultadoCaptutaLive, que el resultado de la captura ha sido KO
                _resultadoCaptutaLive.value = "KO"
            }


    }


}