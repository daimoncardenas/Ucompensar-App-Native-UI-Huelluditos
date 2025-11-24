package com.example.huelluditos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Customer(val id: Int, val name: String, val email: String)
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String?,
    val imageUri: String?
)

data class CartItemView(val id: Int, val productName: String, val quantity: Int, val unitPrice: Double)

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "huelluditos.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT,
                createdAt TEXT,
                updatedAt TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                price REAL,
                description TEXT,
                imageUri TEXT,
                createdAt TEXT,
                updatedAt TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE carts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customerId INTEGER,
                status TEXT,
                createdAt TEXT,
                lat REAL,
                lon REAL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE cart_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cartId INTEGER,
                productId INTEGER,
                quantity INTEGER,
                unitPrice REAL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS cart_items")
        db.execSQL("DROP TABLE IF EXISTS carts")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS customers")
        onCreate(db)
    }

    // ---------- Users ----------

    fun registerUser(name: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
            val now = System.currentTimeMillis().toString()
            put("createdAt", now)
            put("updatedAt", now)
        }
        return try {
            db.insertOrThrow("customers", null, values) > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun login(email: String, password: String): Customer? {
        val db = readableDatabase
        val cursor = db.query(
            "customers",
            arrayOf("id", "name", "email", "password"),
            "email = ? AND password = ?",
            arrayOf(email, password),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getInt(0)
                val name = it.getString(1)
                val em = it.getString(2)
                return Customer(id, name, em)
            }
        }
        return null
    }

    // ---------- Products (CRUD) ----------

    fun insertProduct(
        name: String,
        price: Double,
        description: String?,
        imageUri: String? = null
    ): Boolean {
        val db = writableDatabase
        val now = System.currentTimeMillis().toString()
        val values = ContentValues().apply {
            put("name", name)
            put("price", price)
            put("description", description)
            put("imageUri", imageUri)
            put("createdAt", now)
            put("updatedAt", now)
        }
        val result = db.insert("products", null, values)
        db.close()
        return result > 0
    }

    fun getAllProducts(): List<Product> {
        val db = readableDatabase
        val cursor = db.query(
            "products",
            arrayOf("id", "name", "price", "description", "imageUri"),
            null, null, null, null,
            "createdAt DESC"
        )
        val products = mutableListOf<Product>()
        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    Product(
                        id = it.getInt(0),
                        name = it.getString(1),
                        price = it.getDouble(2),
                        description = it.getString(3),
                        imageUri = it.getString(4)
                    )
                )
            }
        }
        return products
    }

    fun updateProduct(productId: Int, name: String, price: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("price", price)
            put("updatedAt", System.currentTimeMillis().toString())
        }
        val rows = db.update("products", values, "id = ?", arrayOf(productId.toString()))
        db.close()
        return rows > 0
    }

    fun deleteProduct(productId: Int): Boolean {
        val db = writableDatabase
        val rows = db.delete("products", "id = ?", arrayOf(productId.toString()))
        db.close()
        return rows > 0
    }

    // ---------- Cart & cart items ----------

    private fun getOrCreateOpenCart(customerId: Int): Int {
        val db = writableDatabase

        val cursor = db.query(
            "carts",
            arrayOf("id"),
            "customerId = ? AND status = 'OPEN'",
            arrayOf(customerId.toString()),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }

        val values = ContentValues().apply {
            put("customerId", customerId)
            put("status", "OPEN")
            put("createdAt", System.currentTimeMillis().toString())
        }
        val id = db.insert("carts", null, values)
        return id.toInt()
    }

    fun addProductToCart(customerId: Int, product: Product) {
        val db = writableDatabase
        val cartId = getOrCreateOpenCart(customerId)

        val cursor = db.query(
            "cart_items",
            arrayOf("id", "quantity"),
            "cartId = ? AND productId = ?",
            arrayOf(cartId.toString(), product.id.toString()),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val itemId = it.getInt(0)
                val currentQty = it.getInt(1)
                val values = ContentValues().apply {
                    put("quantity", currentQty + 1)
                }
                db.update("cart_items", values, "id = ?", arrayOf(itemId.toString()))
            } else {
                val values = ContentValues().apply {
                    put("cartId", cartId)
                    put("productId", product.id)
                    put("quantity", 1)
                    put("unitPrice", product.price)
                }
                db.insert("cart_items", null, values)
            }
        }
    }

    fun getCartItemsForCustomer(customerId: Int): List<CartItemView> {
        val db = readableDatabase
        val query = """
            SELECT ci.id, p.name, ci.quantity, ci.unitPrice
            FROM cart_items ci
            JOIN carts c ON ci.cartId = c.id
            JOIN products p ON ci.productId = p.id
            WHERE c.customerId = ? AND c.status = 'OPEN'
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(customerId.toString()))
        val items = mutableListOf<CartItemView>()
        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    CartItemView(
                        id = it.getInt(0),
                        productName = it.getString(1),
                        quantity = it.getInt(2),
                        unitPrice = it.getDouble(3)
                    )
                )
            }
        }
        return items
    }

    fun saveCartLocation(customerId: Int, lat: Double, lon: Double) {
        val db = writableDatabase
        val cursor = db.query(
            "carts",
            arrayOf("id"),
            "customerId = ? AND status = 'OPEN'",
            arrayOf(customerId.toString()),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val cartId = it.getInt(0)
                val values = ContentValues().apply {
                    put("lat", lat)
                    put("lon", lon)
                    put("status", "COMPLETED")
                }
                db.update("carts", values, "id = ?", arrayOf(cartId.toString()))
            }
        }
    }

    // ---------- Customers CRUD ----------

    fun getAllCustomers(): List<Customer> {
        val db = readableDatabase
        val cursor = db.query(
            "customers",
            arrayOf("id", "name", "email"),
            null, null, null, null,
            "createdAt DESC"
        )
        val customers = mutableListOf<Customer>()
        cursor.use {
            while (it.moveToNext()) {
                customers.add(
                    Customer(
                        id = it.getInt(0),
                        name = it.getString(1),
                        email = it.getString(2)
                    )
                )
            }
        }
        return customers
    }

    fun updateCustomer(customerId: Int, name: String, email: String, password: String? = null): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("updatedAt", System.currentTimeMillis().toString())
            if (!password.isNullOrEmpty()) {
                put("password", password)
            }
        }

        return try {
            val rows = db.update("customers", values, "id = ?", arrayOf(customerId.toString()))
            rows > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun deleteCustomer(customerId: Int): Boolean {
        val db = writableDatabase
        // First check if customer has any carts
        val cartCursor = db.query(
            "carts",
            arrayOf("id"),
            "customerId = ?",
            arrayOf(customerId.toString()),
            null, null, null
        )

        val hasCarts = cartCursor.count > 0
        cartCursor.close()

        if (hasCarts) {
            // Customer has carts, cannot delete
            db.close()
            return false
        }

        val rows = db.delete("customers", "id = ?", arrayOf(customerId.toString()))
        db.close()
        return rows > 0
    }
}
