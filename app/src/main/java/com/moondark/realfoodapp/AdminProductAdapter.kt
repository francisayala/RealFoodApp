package com.moondark.realfoodapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moondark.realfoodapp.database.ProductEntity
import com.moondark.realfoodapp.databinding.ItemAdminProductBinding

class AdminProductAdapter(
    private val onEdit: (ProductEntity) -> Unit,
    private val onDelete: (ProductEntity) -> Unit,
    private val onToggleAvailability: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, AdminProductAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onEdit, onDelete, onToggleAvailability)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAdminProductBinding,
        private val onEdit: (ProductEntity) -> Unit,
        private val onDelete: (ProductEntity) -> Unit,
        private val onToggleAvailability: (ProductEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity) {
            binding.apply {
                productName.text = product.name
                productPrice.text = "$${String.format("%.2f", product.price)}"
                productCategory.text = product.category
                productDescription.text = product.description

                // Cambiar apariencia según disponibilidad
                if (product.available) {
                    // Disponible: colores normales
                    binding.root.setCardBackgroundColor(
                        android.graphics.Color.parseColor("#3A3F4F")
                    )
                    productName.alpha = 1.0f
                    productDescription.alpha = 1.0f
                    productPrice.alpha = 1.0f
                } else {
                    // NO disponible: más oscuro y transparente
                    binding.root.setCardBackgroundColor(
                        android.graphics.Color.parseColor("#2A2A2A")
                    )
                    productName.alpha = 0.5f
                    productDescription.alpha = 0.5f
                    productPrice.alpha = 0.5f
                }

                availableSwitch.isChecked = product.available
                availableSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != product.available) {
                        onToggleAvailability(product)
                    }
                }

                editButton.setOnClickListener {
                    onEdit(product)
                }

                deleteButton.setOnClickListener {
                    onDelete(product)
                }
            }
        }

    }

    private class DiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem == newItem
        }
    }
}
