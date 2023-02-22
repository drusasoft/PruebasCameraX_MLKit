package com.aar.pruebascamerax_mlkit.models

import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage





class FragmentPruebasLectorQRViewModel:ViewModel()
{

    //************************************* Variables LiveData *************************************
    private val _tipoCodigoLive = MutableLiveData<String>()

    val tipoCodigoLive:LiveData<String>
        get() = _tipoCodigoLive
    //********************************** Fin Variables LiveData ************************************


    fun limpiarVariablesLiveData(){ _tipoCodigoLive.value = ""}


    fun escanearCodigoQR(imagen: InputImage, imageProxy: ImageProxy)
    {

        //Se configura el Escaner de Codigos, para que solo pueda leer codigos con formaro QR y AZTEC (Asi va mas Rapido)
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC).build()

        //Se inicia el Escaner de Codigos indicandole los formatos de Codigos permitidos
        val scanner = BarcodeScanning.getClient(options)

        //Se procesa la imagen pasada como parametro, para leer el CodigoQR
        scanner.process(imagen).addOnSuccessListener { codigosQR->

            codigosQR.forEach {

                val valueType = it.valueType

                when(valueType)
                {
                    Barcode.TYPE_PHONE->{ _tipoCodigoLive.value = "QR Llamada" }

                    Barcode.TYPE_EMAIL->{ _tipoCodigoLive.value = "QR Email" }

                    Barcode.TYPE_SMS->{ _tipoCodigoLive.value = "QR Sms" }

                    Barcode.TYPE_GEO->{ _tipoCodigoLive.value = "QR Geo" }

                    Barcode.TYPE_WIFI->{ _tipoCodigoLive.value = "QR Wifi" }
                }

            }

            //Se cierra la imagen capturada por el analizador de CameraX
            imageProxy.close()

        }.addOnCanceledListener{

            Log.e("Error", "Escaneo Cancelado")
            imageProxy.close()
        }

    }
}