package com.example.agenda

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import cn.pedant.SweetAlert.SweetAlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModificarContactoActivity : AppCompatActivity() {

    private lateinit var edtNombre: EditText
    private lateinit var edtTelefono: EditText
    private lateinit var btnGuardar: Button

    private var contactoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_contacto)

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
        window.statusBarColor = resources.getColor(android.R.color.transparent)

        edtNombre = findViewById(R.id.edtNombre)
        edtTelefono = findViewById(R.id.edtTelefono)
        btnGuardar = findViewById(R.id.btnGuardar)

        contactoId = intent.getIntExtra("id", -1)
        val nombre = intent.getStringExtra("nombre")
        val telefono = intent.getStringExtra("telefono")

        edtNombre.setText(nombre)
        edtTelefono.setText(telefono)

        // Agregar un TextWatcher para formatear el teléfono mientras el usuario lo escribe
        edtTelefono.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    val formattedPhone = formatearTelefono(s.toString())
                    if (s.toString() != formattedPhone) {
                        edtTelefono.removeTextChangedListener(this)
                        edtTelefono.setText(formattedPhone)
                        edtTelefono.setSelection(formattedPhone.length) // Mover el cursor al final
                        edtTelefono.addTextChangedListener(this)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnGuardar.setOnClickListener {
            val nombreModificado = edtNombre.text.toString()
            var telefonoModificado = edtTelefono.text.toString()

            // Formatear el teléfono antes de guardar
            telefonoModificado = formatearTelefono(telefonoModificado)

            if (nombreModificado.isNotBlank() && telefonoModificado.isNotBlank()) {
                val contacto = Contacto(id = contactoId, nombre = nombreModificado, telefono = telefonoModificado)
                actualizarContacto(contacto)
            } else {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Campos vacíos")
                    .setContentText("Por favor, completa todos los campos.")
                    .setConfirmText("OK")
                    .show()
            }
        }
    }

    // Método para formatear el teléfono
    private fun formatearTelefono(telefono: String?): String {
        return if (!telefono.isNullOrEmpty() && telefono.length == 8) {
            telefono.substring(0, 4) + "-" + telefono.substring(4)
        } else {
            telefono ?: ""  // Si no tiene formato adecuado, se devuelve tal cual o vacío si es nulo
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun actualizarContacto(contacto: Contacto) {
        val apiService = ApiClient.service
        apiService.editarContacto(contacto).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        SweetAlertDialog(this@ModificarContactoActivity, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Actualizado")
                            .setContentText("El contacto fue actualizado correctamente")
                            .setConfirmClickListener {
                                it.dismissWithAnimation()
                                val resultIntent = Intent()
                                resultIntent.putExtra("id", contacto.id)
                                resultIntent.putExtra("nombre", contacto.nombre)
                                resultIntent.putExtra("telefono", contacto.telefono)
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }
                            .show()
                    } else {
                        SweetAlertDialog(this@ModificarContactoActivity, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("No se pudo actualizar el contacto")
                            .show()
                    }
                } else {
                    SweetAlertDialog(this@ModificarContactoActivity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Respuesta inválida del servidor")
                        .show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                SweetAlertDialog(this@ModificarContactoActivity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de red")
                    .setContentText("No se pudo conectar con el servidor")
                    .show()
            }
        })
    }
}


