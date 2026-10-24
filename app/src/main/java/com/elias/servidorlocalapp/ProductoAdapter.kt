package com.elias.servidorlocalapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter :
    RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private var productos: List<Producto> = emptyList()

    class ProductoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val txtNombre: TextView =
            itemView.findViewById(R.id.txtNombre)

        val txtDescripcion: TextView =
            itemView.findViewById(R.id.txtDescripcion)

        val txtPrecio: TextView =
            itemView.findViewById(R.id.txtPrecio)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductoViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)

        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: ProductoViewHolder,
        position: Int
    ) {
        val producto = productos[position]

        holder.txtNombre.text = producto.nombre
        holder.txtDescripcion.text = producto.descripcion
        holder.txtPrecio.text = "$%.2f".format(
            java.util.Locale.US,
            producto.precio
        )
    }

    override fun getItemCount(): Int {
        return productos.size
    }

    fun actualizar(nuevosProductos: List<Producto>) {
        productos = nuevosProductos
        notifyDataSetChanged()
    }
}
