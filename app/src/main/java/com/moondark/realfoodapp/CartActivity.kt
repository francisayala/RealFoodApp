package com.moondark.realfoodapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.moondark.realfoodapp.database.AppDatabase
import com.moondark.realfoodapp.database.CartItemEntity
import com.moondark.realfoodapp.databinding.ActivityCartBinding
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var database: AppDatabase
    private lateinit var cartAdapter: CartItemAdapter

    private var cartItems = listOf<CartItemEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupRecyclerView()
        loadCartItems()

        // Botón proceder al checkout
        binding.checkoutButton.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            } else {
                proceedToCheckout()
            }
        }

        // Botón agregar más items (volver al menú)
        binding.addMoreButton.setOnClickListener {
            finish() // Volver a MainMenuActivity
        }

        // Navegación inferior
        binding.homeButton.setOnClickListener {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        }

        binding.cartButton.setOnClickListener {
            // Ya estamos aquí
        }

        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            onQuantityChanged = { item, newQuantity ->
                updateQuantity(item, newQuantity)
            },
            onRemove = { item ->
                removeItem(item)
            }
        )

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun loadCartItems() {
        lifecycleScope.launch {
            database.cartDao().getAllCartItems().collect { items ->
                cartItems = items
                cartAdapter.submitList(items)
                updateTotal(items)
                updateEmptyState(items.isEmpty())
            }
        }
    }

    private fun updateTotal(items: List<CartItemEntity>) {
        val total = items.sumOf { it.productPrice * it.quantity }
        binding.totalText.text = "Total: ${"$%.2f".format(total)}"
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyCartText.visibility = View.VISIBLE
            binding.cartRecyclerView.visibility = View.GONE
            binding.checkoutButton.isEnabled = false
        } else {
            binding.emptyCartText.visibility = View.GONE
            binding.cartRecyclerView.visibility = View.VISIBLE
            binding.checkoutButton.isEnabled = true
        }
    }

    private fun updateQuantity(item: CartItemEntity, newQuantity: Int) {
        lifecycleScope.launch {
            val updatedItem = item.copy(quantity = newQuantity)
            database.cartDao().upsertCartItem(updatedItem)

            Toast.makeText(
                this@CartActivity,
                "${item.productName}: $newQuantity",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun removeItem(item: CartItemEntity) {
        lifecycleScope.launch {
            database.cartDao().removeFromCart(item)

            Toast.makeText(
                this@CartActivity,
                "❌ ${item.productName} eliminado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun proceedToCheckout() {
        // TODO: Implementar orden y pago
        val total = cartItems.sumOf { it.productPrice * it.quantity }

        Toast.makeText(
            this,
            "Procesando pedido por ${"$%.2f".format(total)}",
            Toast.LENGTH_LONG
        ).show()

        // Por ahora, ir a TrackingActivity
        startActivity(Intent(this, TrackingActivity::class.java))
    }
}
