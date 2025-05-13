package com.example.agenda

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactoAdapter(
    private var lista: List<Contacto>,
    private val onEditarClick: (Contacto) -> Unit,
    private val onEliminarClick: (Int) -> Unit
) : RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder>() {

    class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombre)
        val telefono: TextView = itemView.findViewById(R.id.txtTelefono)
        val btnVer: Button = itemView.findViewById(R.id.btnVerContacto)
        val btnModificar: Button = itemView.findViewById(R.id.btnEditar)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacto, parent, false)
        return ContactoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contacto = lista[position]
        holder.nombre.text = contacto.nombre
        holder.telefono.text = formatearTelefono(contacto.telefono) // Formatear el teléfono al mostrar


        holder.btnVer.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VerContactoActivity::class.java).apply {
                putExtra("nombre", contacto.nombre)
                putExtra("telefono", contacto.telefono)
            }
            context.startActivity(intent)
        }


        holder.btnModificar.setOnClickListener {

            onEditarClick(contacto)
        }


        holder.btnEliminar.setOnClickListener {

            onEliminarClick(contacto.id)
        }
    }

    override fun getItemCount(): Int = lista.size


    fun actualizarLista(nuevaLista: List<Contacto>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }


    private fun formatearTelefono(telefono: String): String {

        if (telefono.length == 8 && !telefono.contains("-")) {
            return telefono.substring(0, 4) + "-" + telefono.substring(4)
        }
        return telefono
    }
}
