package com.moondark.realfoodapp

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.moondark.realfoodapp.database.AppDatabase
import com.moondark.realfoodapp.database.ProductEntity
import com.moondark.realfoodapp.databinding.ActivityAdminBinding
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var database: AppDatabase
    private lateinit var adminAdapter: AdminProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupRecyclerView()
        loadProducts()

        binding.addProductButton.setOnClickListener {
            showAddProductDialog()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adminAdapter = AdminProductAdapter(
            onEdit = { product ->
                showEditProductDialog(product)
            },
            onDelete = { product ->
                showDeleteConfirmation(product)
            },
            onToggleAvailability = { product ->
                toggleAvailability(product)
            }
        )

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = adminAdapter
        }
    }


    private fun loadProducts() {
        lifecycleScope.launch {
            database.productDao().getAllProductsIncludingUnavailable().collect { products ->
                adminAdapter.submitList(products)
            }
        }
    }


    private fun showAddProductDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val descInput = dialogView.findViewById<EditText>(R.id.descriptionInput)
        val priceInput = dialogView.findViewById<EditText>(R.id.priceInput)
        val categoryInput = dialogView.findViewById<EditText>(R.id.categoryInput)

        AlertDialog.Builder(this)
            .setTitle("Agregar Producto")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val name = nameInput.text.toString()
                val desc = descInput.text.toString()
                val price = priceInput.text.toString().toDoubleOrNull() ?: 0.0
                val category = categoryInput.text.toString()

                if (name.isNotBlank() && price > 0 && category.isNotBlank()) {
                    addProduct(name, desc, price, category)
                } else {
                    Toast.makeText(this, "⚠️ Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditProductDialog(product: ProductEntity) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val descInput = dialogView.findViewById<EditText>(R.id.descriptionInput)
        val priceInput = dialogView.findViewById<EditText>(R.id.priceInput)
        val categoryInput = dialogView.findViewById<EditText>(R.id.categoryInput)

        // Pre-llenar con datos actuales
        nameInput.setText(product.name)
        descInput.setText(product.description)
        priceInput.setText(product.price.toString())
        categoryInput.setText(product.category)

        AlertDialog.Builder(this)
            .setTitle("Editar Producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val updatedProduct = product.copy(
                    name = nameInput.text.toString(),
                    description = descInput.text.toString(),
                    price = priceInput.text.toString().toDoubleOrNull() ?: product.price,
                    category = categoryInput.text.toString()
                )

                updateProduct(updatedProduct)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmation(product: ProductEntity) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de eliminar '${product.name}'?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun addProduct(name: String, desc: String, price: Double, category: String) {
        lifecycleScope.launch {
            val product = ProductEntity(
                name = name,
                description = desc,
                price = price,
                category = category,
                available = true
            )

            database.productDao().insertProduct(product)

            Toast.makeText(
                this@AdminActivity,
                "✅ Producto '$name' agregado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateProduct(product: ProductEntity) {
        lifecycleScope.launch {
            database.productDao().insertProduct(product)

            Toast.makeText(
                this@AdminActivity,
                "✅ Producto actualizado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteProduct(product: ProductEntity) {
        lifecycleScope.launch {
            database.productDao().deleteProduct(product)

            Toast.makeText(
                this@AdminActivity,
                "❌ '${product.name}' eliminado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleAvailability(product: ProductEntity) {
        lifecycleScope.launch {
            val updated = product.copy(available = !product.available)
            database.productDao().insertProduct(updated)

            val status = if (updated.available) "disponible ✅" else "no disponible ❌"
            Toast.makeText(
                this@AdminActivity,
                "${product.name} marcado como $status",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
