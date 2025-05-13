package com.example.agenda

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgregarContactoActivity : AppCompatActivity() {

    private lateinit var edtNombre: EditText
    private lateinit var edtTelefono: EditText
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_contacto)

        edtNombre = findViewById(R.id.edtNombre)
        edtTelefono = findViewById(R.id.edtTelefono)
        btnGuardar = findViewById(R.id.btnGuardar)

        formatearTelefono(edtTelefono)

        btnGuardar.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val telefono = edtTelefono.text.toString().trim()

            if (nombre.isNotEmpty() && telefono.isNotEmpty()) {
                val nuevoContacto = Contacto(nombre = nombre, telefono = telefono)
                guardarContacto(nuevoContacto)
            } else {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Campos vacíos")
                    .setContentText("Completa todos los campos")
                    .setConfirmText("OK")
                    .show()
            }
        }
    }

    private fun formatearTelefono(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private val maxLength = 9

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true

                val digits = s.toString().replace("-", "")
                val formatted = when {
                    digits.length > 4 -> digits.substring(0, 4) + "-" + digits.substring(4)
                    else -> digits
                }

                editText.setText(formatted.take(maxLength))
                editText.setSelection(editText.text.length)

                isFormatting = false
            }
        })
    }

    private fun guardarContacto(contacto: Contacto) {
        ApiClient.service.agregarContacto(contacto).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    SweetAlertDialog(this@AgregarContactoActivity, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Guardado")
                        .setContentText("El contacto fue agregado exitosamente")
                        .setConfirmClickListener {
                            it.dismissWithAnimation()
                            setResult(RESULT_OK)
                            finish()
                        }
                        .show()
                } else {
                    SweetAlertDialog(this@AgregarContactoActivity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("No se pudo guardar el contacto")
                        .show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                SweetAlertDialog(this@AgregarContactoActivity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de red")
                    .setContentText("No se pudo conectar con el servidor")
                    .show()
            }
        })
    }
}
