package com.moondark.realfoodapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.moondark.realfoodapp.database.AppDatabase
import com.moondark.realfoodapp.database.CartItemEntity
import com.moondark.realfoodapp.database.ProductEntity
import com.moondark.realfoodapp.databinding.ActivityMainMenuBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var database: AppDatabase
    private lateinit var productAdapter: ProductAdapter

    private lateinit var currencyManager: CurrencyManager

    private var currentFilter: String? = null
    private val cartQuantities = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        currencyManager = CurrencyManager(this)

        // DEBUG: Ver productos en BD
        lifecycleScope.launch {
            val products = database.productDao().getAllProducts().first()
            Log.e("PRODUCTOS_DEBUG", "=================================")
            Log.e("PRODUCTOS_DEBUG", "Total productos en BD: ${products.size}")

            if (products.isEmpty()) {
                Log.e("PRODUCTOS_DEBUG", "⚠️ NO HAY PRODUCTOS - Insertando...")
                insertInitialProducts()

                delay(1000)
                val productsAfter = database.productDao().getAllProducts().first()
                Log.e("PRODUCTOS_DEBUG", "Productos después de insertar: ${productsAfter.size}")
            } else {
                Log.e("PRODUCTOS_DEBUG", "✅ Productos encontrados:")
                products.forEach { product ->
                    Log.e("PRODUCTOS_DEBUG", "  - ${product.name} ($${product.price})")
                }
            }
            Log.e("PRODUCTOS_DEBUG", "=================================")
        }

        loadCartQuantities()
        setupRecyclerView()
        loadProducts()
        updateExchangeRatesIfNeeded()

        binding.pizzaButton.setOnClickListener { onFilterClick("Pizza") }
        binding.tomatoButton.setOnClickListener { onFilterClick("Burgers") }
        binding.pepperButton.setOnClickListener { onFilterClick("Drinks") }
        binding.garlicButton.setOnClickListener { onFilterClick("Salads") }

        binding.searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchBar.text?.toString().orEmpty()
                if (query.isNotBlank()) {
                    searchProducts(query)
                    hideKeyboard()
                } else {
                    Toast.makeText(this, "Ingresa un término de búsqueda", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        binding.homeButton.setOnClickListener {
            currentFilter = null
            loadProducts()
        }

        binding.cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // En onCreate(), después de los listeners de los botones
        binding.productsText.setOnLongClickListener {
            // Mantén presionado "Our products" para entrar a Admin
            startActivity(Intent(this, AdminActivity::class.java))
            Toast.makeText(this, "🔧 Modo Admin", Toast.LENGTH_SHORT).show()
            true
        }

    }


    private fun insertInitialProducts() {
        lifecycleScope.launch {
            val products = listOf(
                ProductEntity(
                    name = "Pizza Margarita",
                    description = "Tomate, mozzarella y albahaca fresca",
                    price = 8.50,
                    category = "Pizza",
                    available = true
                ),
                ProductEntity(
                    name = "Pizza Pepperoni",
                    description = "Pepperoni y queso mozzarella",
                    price = 9.50,
                    category = "Pizza",
                    available = true
                ),
                ProductEntity(
                    name = "Pizza 4 Quesos",
                    description = "Mozzarella, parmesano, gorgonzola y provolone",
                    price = 10.00,
                    category = "Pizza",
                    available = true
                ),
                ProductEntity(
                    name = "Hamburguesa Clásica",
                    description = "Carne, lechuga, tomate y queso cheddar",
                    price = 6.99,
                    category = "Burgers",
                    available = true
                ),
                ProductEntity(
                    name = "Hamburguesa BBQ",
                    description = "Carne, cebolla caramelizada, bacon y salsa BBQ",
                    price = 8.50,
                    category = "Burgers",
                    available = true
                ),
                ProductEntity(
                    name = "Hamburguesa Vegana",
                    description = "Hamburguesa de lentejas, aguacate y rúcula",
                    price = 7.99,
                    category = "Burgers",
                    available = true
                ),
                ProductEntity(
                    name = "Coca-Cola",
                    description = "Refresco 500ml",
                    price = 2.00,
                    category = "Drinks",
                    available = true
                ),
                ProductEntity(
                    name = "Sprite",
                    description = "Refresco de lima-limón 500ml",
                    price = 2.00,
                    category = "Drinks",
                    available = true
                ),
                ProductEntity(
                    name = "Agua Mineral",
                    description = "Agua mineral natural 500ml",
                    price = 1.50,
                    category = "Drinks",
                    available = true
                ),
                ProductEntity(
                    name = "Ensalada César",
                    description = "Lechuga, pollo, parmesano, croutones y aderezo César",
                    price = 7.50,
                    category = "Salads",
                    available = true
                ),
                ProductEntity(
                    name = "Ensalada Griega",
                    description = "Tomate, pepino, cebolla, aceitunas y queso feta",
                    price = 6.99,
                    category = "Salads",
                    available = true
                ),
                ProductEntity(
                    name = "Papas Fritas",
                    description = "Papas fritas crocantes con sal",
                    price = 3.50,
                    category = "Sides",
                    available = true
                ),
                ProductEntity(
                    name = "Aros de Cebolla",
                    description = "Aros de cebolla empanizados",
                    price = 4.00,
                    category = "Sides",
                    available = true
                )
            )

            database.productDao().insertProducts(products)
            Log.e("PRODUCTOS_DEBUG", "✅ ${products.size} productos insertados")
        }
    }

    private fun loadCartQuantities() {
        lifecycleScope.launch {
            database.cartDao().getAllCartItems().collect { cartItems ->
                cartQuantities.clear()
                cartItems.forEach { item ->
                    cartQuantities[item.productId] = item.quantity
                }
                if (::productAdapter.isInitialized) {
                    productAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onAddToCart = { product ->
                addToCart(product)
            },
            onQuantityChanged = { product, newQuantity ->
                updateCartQuantity(product, newQuantity)
            },
            getQuantityInCart = { productId ->
                cartQuantities[productId] ?: 0
            }
        )

        binding.productsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainMenuActivity, 2)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            if (currentFilter != null) {
                database.productDao().getProductsByCategory(currentFilter!!).collect { products ->
                    Log.e("PRODUCTOS_DEBUG", "Productos filtrados por ${currentFilter}: ${products.size}")
                    productAdapter.submitList(products)
                }
            } else {
                database.productDao().getAllProducts().collect { products ->
                    Log.e("PRODUCTOS_DEBUG", "Productos totales cargados en UI: ${products.size}")
                    productAdapter.submitList(products)
                }
            }
        }
    }

    private fun searchProducts(query: String) {
        lifecycleScope.launch {
            database.productDao().getAllProducts().collect { allProducts ->
                val filtered = allProducts.filter { product ->
                    product.name.contains(query, ignoreCase = true) ||
                            product.description.contains(query, ignoreCase = true)
                }

                productAdapter.submitList(filtered)

                if (filtered.isEmpty()) {
                    Toast.makeText(
                        this@MainMenuActivity,
                        "No se encontraron productos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun onFilterClick(category: String) {
        currentFilter = category
        Toast.makeText(this, "Filtrando por $category", Toast.LENGTH_SHORT).show()
        loadProducts()
    }

    private fun addToCart(product: ProductEntity) {
        lifecycleScope.launch {
            val existingItem = database.cartDao().getCartItemByProductId(product.id)

            val newQuantity = if (existingItem != null) {
                existingItem.quantity + 1
            } else {
                1
            }

            cartQuantities[product.id] = newQuantity
            productAdapter.notifyDataSetChanged()

            val cartItem = CartItemEntity(
                productId = product.id,
                productName = product.name,
                productPrice = product.price,
                quantity = newQuantity
            )

            database.cartDao().upsertCartItem(cartItem)

            Toast.makeText(
                this@MainMenuActivity,
                "✅ ${product.name} (x$newQuantity)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateCartQuantity(product: ProductEntity, newQuantity: Int) {
        lifecycleScope.launch {
            if (newQuantity > 0) {
                cartQuantities[product.id] = newQuantity
                productAdapter.notifyDataSetChanged()

                val cartItem = CartItemEntity(
                    productId = product.id,
                    productName = product.name,
                    productPrice = product.price,
                    quantity = newQuantity
                )
                database.cartDao().upsertCartItem(cartItem)

            } else {
                cartQuantities.remove(product.id)
                productAdapter.notifyDataSetChanged()

                database.cartDao().removeByProductId(product.id)

                Toast.makeText(
                    this@MainMenuActivity,
                    "❌ ${product.name} eliminado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
    }

    private fun updateExchangeRatesIfNeeded() {
        lifecycleScope.launch {
            Log.d("CURRENCY_FLOW", "═══════════════════════════════════")
            Log.d("CURRENCY_FLOW", "🔍 Verificando necesidad de actualizar tasas...")

            if (!currencyManager.areCachedRatesValid()) {
                Log.d("CURRENCY_FLOW", "⏰ Cache expirado o inexistente")
                Log.d("CURRENCY_FLOW", "📥 Descargando tasas desde API...")

                val result = currencyManager.updateExchangeRates(this@MainMenuActivity)

                result.onSuccess {
                    Log.d("CURRENCY_FLOW", "✅ Tasas descargadas y guardadas")

                    Toast.makeText(
                        this@MainMenuActivity,
                        "💱 Tasas de cambio actualizadas",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Refrescar UI
                    productAdapter.notifyDataSetChanged()
                }

                result.onFailure { exception ->
                    Log.e("CURRENCY_FLOW", "❌ Error al descargar tasas: ${exception.message}")

                    // Verificar si hay tasas en cache antiguos
                    val hasOldRates = currencyManager.getCachedRate("EUR") != 1.0

                    if (hasOldRates) {
                        Log.w("CURRENCY_FLOW", "⚠️ Usando tasas antiguas del cache")
                        Toast.makeText(
                            this@MainMenuActivity,
                            "⚠️ Usando tasas antiguas (sin conexión)",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Log.e("CURRENCY_FLOW", "❌ No hay tasas disponibles")
                        Toast.makeText(
                            this@MainMenuActivity,
                            "❌ Error: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Log.d("CURRENCY_FLOW", "✅ Usando tasas en cache (aún válidas)")

                val cacheAge = System.currentTimeMillis() - currencyManager.getRatesTimestamp()

                val hoursOld = cacheAge / (1000 * 60 * 60)

                Log.d("CURRENCY_FLOW", "📅 Antigüedad del cache: $hoursOld horas")
            }

            Log.d("CURRENCY_FLOW", "═══════════════════════════════════")
        }
    }

    override fun onResume() {
        super.onResume()
        loadCartQuantities()
    }
}
