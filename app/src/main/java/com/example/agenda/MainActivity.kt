package com.example.agenda

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import cn.pedant.SweetAlert.SweetAlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var contactoAdapter: ContactoAdapter
    private lateinit var edtBuscar: EditText
    private lateinit var btnBuscar: Button
    private lateinit var btnAgregar: Button

    private val agregarContactoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) obtenerContactos()
    }

    private val modificarContactoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) obtenerContactos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        recyclerView = findViewById(R.id.recyclerContactos)
        edtBuscar = findViewById(R.id.edtBuscar)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnAgregar = findViewById(R.id.btnAgregar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        contactoAdapter = ContactoAdapter(emptyList(), ::onContactoClick, ::onEliminarContacto)
        recyclerView.adapter = contactoAdapter

        obtenerContactos()

        btnBuscar.setOnClickListener {
            val query = edtBuscar.text.toString().trim()
            if (query.isNotEmpty()) buscarContactos(query)
            else obtenerContactos()
        }

        btnAgregar.setOnClickListener {
            val intent = Intent(this, AgregarContactoActivity::class.java)
            agregarContactoLauncher.launch(intent)
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun obtenerContactos() {
        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText("Cargando...")
            .apply {
                setCancelable(false)
                show()
            }

        ApiClient.service.getContactos().enqueue(object : Callback<List<Contacto>> {
            override fun onResponse(call: Call<List<Contacto>>, response: Response<List<Contacto>>) {
                loadingDialog.dismissWithAnimation()
                if (response.isSuccessful) {
                    val contactos = response.body() ?: emptyList()
                    contactoAdapter.actualizarLista(contactos)
                } else {
                    SweetAlertDialog(this@MainActivity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("No se pudo obtener la lista de contactos.")
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Contacto>>, t: Throwable) {
                loadingDialog.dismissWithAnimation()
                SweetAlertDialog(this@MainActivity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar con el servidor.")
                    .show()
            }
        })
    }

    private fun buscarContactos(query: String) {
        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText("Buscando...")
            .apply {
                setCancelable(false)
                show()
            }

        ApiClient.service.buscarContactos(query).enqueue(object : Callback<List<Contacto>> {
            override fun onResponse(call: Call<List<Contacto>>, response: Response<List<Contacto>>) {
                loadingDialog.dismissWithAnimation()
                if (response.isSuccessful) {
                    val resultados = response.body() ?: emptyList()
                    contactoAdapter.actualizarLista(resultados)

                    if (resultados.isEmpty()) {
                        SweetAlertDialog(this@MainActivity, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Sin resultados")
                            .setContentText("No se encontraron contactos para \"$query\".")
                            .show()
                    }
                } else {
                    SweetAlertDialog(this@MainActivity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("No se pudo buscar contactos.")
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Contacto>>, t: Throwable) {
                loadingDialog.dismissWithAnimation()
                SweetAlertDialog(this@MainActivity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar con el servidor.")
                    .show()
            }
        })
    }

    private fun onContactoClick(contacto: Contacto) {
        val intent = Intent(this, ModificarContactoActivity::class.java).apply {
            putExtra("id", contacto.id)
            putExtra("nombre", contacto.nombre)
            putExtra("telefono", contacto.telefono)
        }
        modificarContactoLauncher.launch(intent)
    }

    private fun onEliminarContacto(contactoId: Int) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Estás seguro?")
            .setContentText("Esta acción eliminará el contacto.")
            .setConfirmText("Sí, eliminar")
            .setCancelText("Cancelar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                ApiClient.service.eliminarContacto(contactoId).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            SweetAlertDialog(this@MainActivity, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Eliminado")
                                .setContentText("El contacto ha sido eliminado.")
                                .show()
                            obtenerContactos()
                        } else {
                            SweetAlertDialog(this@MainActivity, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("No se pudo eliminar el contacto.")
                                .show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        SweetAlertDialog(this@MainActivity, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error de conexión")
                            .setContentText("No se pudo conectar con el servidor.")
                            .show()
                    }
                })
            }
            .setCancelClickListener { dialog -> dialog.dismissWithAnimation() }
            .show()
    }
}


