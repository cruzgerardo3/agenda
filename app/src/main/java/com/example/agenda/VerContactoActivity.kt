package com.example.agenda

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.graphics.Color

class VerContactoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_contacto)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        supportActionBar?.title = "Mi Agenda"


        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))


        toolbar.setPadding(
            toolbar.paddingLeft,
            toolbar.paddingTop + getStatusBarHeight(),
            toolbar.paddingRight,
            toolbar.paddingBottom
        )


        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT


        val nombre = intent.getStringExtra("nombre")
        val telefono = intent.getStringExtra("telefono")

        val txtNombre = findViewById<TextView>(R.id.txtNombreDetalle)
        val txtTelefono = findViewById<TextView>(R.id.txtTelefonoDetalle)


        txtNombre.text = nombre
        txtTelefono.text = formatearTelefono(telefono)
    }


    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }


    private fun formatearTelefono(telefono: String?): String {
        return if (!telefono.isNullOrEmpty() && telefono.length == 8) {
            telefono.substring(0, 4) + "-" + telefono.substring(4)
        } else {
            telefono ?: ""  
        }
    }

}
