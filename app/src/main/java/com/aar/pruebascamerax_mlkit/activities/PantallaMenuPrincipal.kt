package com.aar.pruebascamerax_mlkit.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.aar.pruebascamerax_mlkit.R
import com.aar.pruebascamerax_mlkit.databinding.LayoutPantallaMenuPrincipalBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class PantallaMenuPrincipal : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        val binding = LayoutPantallaMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Se instancia la Toolbar
        setSupportActionBar(binding.toolbarPantallaPrincipal)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setTitle("")


        //Se Crea Objeto NavController
        val navController = Navigation.findNavController(this, R.id.NavHostFragment)

        //Añadimos el navController a la Toolbar(Actionbar), Para que se muestre la flecha volver y el titulo del frgament en la toolbar cuando se navega a otros fragments desde el fragment home
        binding.toolbarPantallaPrincipal.setupWithNavController(navController)

        //Listener que se ejecuta cada vez que navegamos a un Fragment, lo usaoi para mostrar en la Toolbar el titulo correcto de cada Fragment
        navController.addOnDestinationChangedListener{controller, destination, arguments ->

            when(destination.id)
            {
                controller.graph.startDestinationId->{ supportActionBar!!.setTitle(R.string.titMenuPrincipal) }
                R.id.fragmentPruebaCameraX->{ supportActionBar!!.setTitle(R.string.titFragmentCameraX) }
                R.id.fragmentPruebaLectorQR->{ supportActionBar!!.setTitle(R.string.titFragmentLectorQR) }
                R.id.fragmentPruebaEtiquetadoImagenes->{ supportActionBar!!.setTitle(R.string.titFragmentEtiquetadoImagenes)}
                R.id.fragmentPruebaDetectorObjetos->{ supportActionBar!!.setTitle(R.string.titFragmentDetectorObjetos) }
                R.id.fragmentPruebaReconocimientoTexto->{ supportActionBar!!.setTitle(R.string.titFragmentReconocimientoTexto) }
            }

        }



    }

}