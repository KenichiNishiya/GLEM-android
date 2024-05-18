package com.example.glem

import CustomAdapter
import ListItem
import android.app.Activity
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
                    val imageUrl = document.getString("imagem") ?: "default_image_url"
                    val marca = document.getString("marca") ?: "Unknown"
                    val modelo = document.getString("modelo") ?: "Unknown"
                    val ano = document.get("ano")?.let {
                        if (it is Long) it.toString() else it as? String ?: "Unknown"
                    } ?: "Unknown"
                    val preco = document.get("preco")?.toString() ?: "Unknown"
                    val descricao = document.getString("descricao") ?: "No description"

                    items.add(ListItem(imageUrl, marca, modelo, ano, preco, descricao))
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

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.func1 -> {
            startActivity(Intent(this, MainActivity::class.java))
            true
        }
        R.id.func2 -> {
            startActivityForResult(Intent(this, CadastrarCarros::class.java), REQUEST_CODE_ADD_CAR)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_CAR && resultCode == Activity.RESULT_OK) {
            retrieveDataFromFirestore() // Refresh data
        }
    }
}
