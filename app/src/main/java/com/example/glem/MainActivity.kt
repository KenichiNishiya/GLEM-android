package com.example.glem

import CustomAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore

open class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_ADD_CAR = 200

    lateinit var listView: ListView
    lateinit var adapter: CustomAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    val items = mutableListOf<ListItem>() // Initially empty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        listView = findViewById(R.id.listView)
        adapter = CustomAdapter(this, items)
        listView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener {
            retrieveDataFromFirestore()
        }

        retrieveDataFromFirestore()

        // Setting up the item click listener
        listView.setOnItemClickListener { parent, view, position, id ->
            val item = adapter.getItem(position)
            if (item != null) {
                val intent = Intent(this, DetalhesCarros::class.java).apply {
                    putExtra("imageUrl", item.imageUrl)
                    putExtra("marca", item.marca)
                    putExtra("modelo", item.modelo)
                    putExtra("ano", item.ano)
                    putExtra("preco", item.preco)
                    putExtra("descricao", item.descricao)
                    putExtra("carId", item.carId) // Make sure carId is passed
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error: Item not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun retrieveDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val carrosCollection = db.collection("carros")

        carrosCollection.get()
            .addOnSuccessListener { result ->
                items.clear() // Clear existing items
                for (document in result) {
                    val carId = document.id // Get document ID
                    val imageUrl = document.getString("imagem") ?: "default_image_url"
                    val marca = document.getString("marca") ?: "Unknown"
                    val modelo = document.getString("modelo") ?: "Unknown"
                    val ano = document.get("ano")?.let {
                        if (it is Long) it.toString() else it as? String ?: "Unknown"
                    } ?: "Unknown"
                    val preco = document.get("preco")?.toString() ?: "Unknown"
                    val descricao = document.getString("descricao") ?: "No description"

                    items.add(ListItem(carId, imageUrl, marca, modelo, ano, preco, descricao))
                }
                adapter.notifyDataSetChanged() // Notify the adapter of the data change
                swipeRefreshLayout.isRefreshing = false // Hide the refresh indicator
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Error loading data", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false // Hide the refresh indicator
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        menu?.findItem(R.id.func2)?.isVisible = isLoggedIn
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.func2 -> {
            startActivityForResult(Intent(this, CadastrarCarros::class.java), REQUEST_CODE_ADD_CAR)
            true
        }
        R.id.func3 -> {
            startActivityForResult(Intent(this, LoginActivity::class.java), REQUEST_CODE_ADD_CAR)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
//    override fun onStop() {
//        super.onStop()
//        // Clear login state when the app is stopped
//        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
//        with(sharedPref.edit()) {
//            putBoolean("isLoggedIn", false)
//            apply()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // Clear login state when the app is closed
//        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
//        with(sharedPref.edit()) {
//            putBoolean("isLoggedIn", false)
//            apply()
//        }
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_CAR && resultCode == Activity.RESULT_OK) {
            retrieveDataFromFirestore() // Refresh data
        }
    }
}
