package com.moondark.realfoodapp

import android.content.Context  // ← IMPORT AGREGADO
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moondark.realfoodapp.database.ProductEntity
import com.moondark.realfoodapp.databinding.ItemProductBinding

class ProductAdapter(
    private val onAddToCart: (ProductEntity) -> Unit,
    private val onQuantityChanged: (ProductEntity, Int) -> Unit,
    private val getQuantityInCart: (Int) -> Int
) : ListAdapter<ProductEntity, ProductAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, parent.context, onAddToCart, onQuantityChanged, getQuantityInCart)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemProductBinding,
        private val context: Context,  // ← AGREGADO
        private val onAddToCart: (ProductEntity) -> Unit,
        private val onQuantityChanged: (ProductEntity, Int) -> Unit,
        private val getQuantityInCart: (Int) -> Int
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyManager = CurrencyManager(context)  // ← AGREGADO

        fun bind(product: ProductEntity) {
            binding.apply {
                productName.text = product.name
                productDescription.text = product.description
                productPrice.text = currencyManager.formatPrice(product.price)  // ← CAMBIADO

                val quantity = getQuantityInCart(product.id)

                if (quantity > 0) {
                    addToCartButton.visibility = View.GONE
                    quantityControls.visibility = View.VISIBLE
                    quantityText.text = quantity.toString()
                } else {
                    addToCartButton.visibility = View.VISIBLE
                    quantityControls.visibility = View.GONE
                }

                addToCartButton.setOnClickListener {
                    onAddToCart(product)
                }

                increaseButton.setOnClickListener {
                    onQuantityChanged(product, quantity + 1)
                }

                decreaseButton.setOnClickListener {
                    val newQuantity = quantity - 1
                    onQuantityChanged(product, newQuantity)
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
