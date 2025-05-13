package com.example.agenda

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("obtener_contactos.php")
    fun getContactos(): Call<List<Contacto>>

    @GET("buscar_contacto.php")
    fun buscarContactos(@Query("query") query: String): Call<List<Contacto>>

    @POST("agregar_contacto.php")
    fun agregarContacto(@Body contacto: Contacto): Call<ApiResponse>

    // Editar un contacto
    @POST("update_contacto.php")
    fun editarContacto(@Body contacto: Contacto): Call<ApiResponse>

    // Eliminar un contacto
    @DELETE("delete_contacto.php")
    fun eliminarContacto(@Query("id") id: Int): Call<ApiResponse>
}
