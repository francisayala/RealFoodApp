package com.moondark.realfoodapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moondark.realfoodapp.database.CartItemEntity
import com.moondark.realfoodapp.databinding.ItemCartBinding
import android.content.Context

class CartItemAdapter(
    private val onQuantityChanged: (CartItemEntity, Int) -> Unit,
    private val onRemove: (CartItemEntity) -> Unit
) : ListAdapter<CartItemEntity, CartItemAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, parent.context, onQuantityChanged, onRemove)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCartBinding,
        private val context: Context,  // ← AGREGADO
        private val onQuantityChanged: (CartItemEntity, Int) -> Unit,
        private val onRemove: (CartItemEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyManager = CurrencyManager(context)  // ← AGREGADO

        fun bind(item: CartItemEntity) {
            binding.apply {
                productName.text = item.productName
                val formattedPrice = currencyManager.formatPrice(item.productPrice)
                productPrice.text = "x${item.quantity}$formattedPrice"
                quantityText.text = item.quantity.toString()

                // Botón aumentar
                increaseButton.setOnClickListener {
                    onQuantityChanged(item, item.quantity + 1)
                }

                // Botón disminuir
                decreaseButton.setOnClickListener {
                    if (item.quantity > 1) {
                        onQuantityChanged(item, item.quantity - 1)
                    } else {
                        onRemove(item)
                    }
                }

                // Botón eliminar
                removeButton.setOnClickListener {
                    onRemove(item)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CartItemEntity>() {
        override fun areItemsTheSame(oldItem: CartItemEntity, newItem: CartItemEntity): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItemEntity, newItem: CartItemEntity): Boolean {
            return oldItem == newItem
        }
    }
}
