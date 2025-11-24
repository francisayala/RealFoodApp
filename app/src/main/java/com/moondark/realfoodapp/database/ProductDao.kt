package com.moondark.realfoodapp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // LEER todos los productos disponibles
    @Query("SELECT * FROM products WHERE available = 1")
    fun getAllProducts(): Flow<List<ProductEntity>>


    //  NUEVA FUNCIÓN (para Admin)
    @Query("SELECT * FROM products ORDER BY available DESC, category, name")
    fun getAllProductsIncludingUnavailable(): Flow<List<ProductEntity>>

    // LEER productos por categoría (Pizza, Burgers, etc.)
    @Query("SELECT * FROM products WHERE category = :category AND available = 1")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    // LEER un producto específico por ID
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    // INSERTAR un producto nuevo (o reemplazar si ya existe)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    // INSERTAR varios productos a la vez
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // ELIMINAR un producto
    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // ELIMINAR todos los productos (limpiar tabla)
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
