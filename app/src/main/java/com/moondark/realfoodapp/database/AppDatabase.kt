package com.moondark.realfoodapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        UserProfileEntity::class
    ],
    version = 2,  // ← CAMBIO: De 1 a 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "realfood_database"
                )
                    .fallbackToDestructiveMigration()  // ← AGREGADO: Recrea la BD si cambia
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val productDao = database.productDao()
            productDao.deleteAllProducts()

            val sampleProducts = listOf(
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

            productDao.insertProducts(sampleProducts)
        }
    }
}
