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
    val items = mutableListOf<ListItem>() // Vazio inicialmente

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
        supportActionBar?.title = "" // Remove o titulo "GLEM"
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        listView = findViewById(R.id.listView)
        adapter = CustomAdapter(this, items)
        listView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener {
            retrieveDataFromFirestore()
        }

        retrieveDataFromFirestore()

        // Quando clica em um item do list view
        listView.setOnItemClickListener { parent, view, position, id ->
            // Pega o item do adapter na position clicada
            val item = adapter.getItem(position)
            if (item != null) {
                // Cria um intent com uma refencia a classe de DetalhesCarros
                val intent = Intent(this, DetalhesCarros::class.java).apply {// configura e retorna o intent
                    putExtra("imageUrl", item.imageUrl) // Adiciona dados extras para serem passados
                    putExtra("marca", item.marca)
                    putExtra("modelo", item.modelo)
                    putExtra("ano", item.ano)
                    putExtra("preco", item.preco)
                    putExtra("descricao", item.descricao)
                    putExtra("carId", item.carId)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Item não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun retrieveDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val carrosCollection = db.collection("carros")

        carrosCollection.get()
            .addOnSuccessListener { result ->
                items.clear() // Limpa os items ja existentes
                for (document in result) {
                    val carId = document.id // Get document ID
                    val imageUrl = document.getString("imagem") ?: "https://firebasestorage.googleapis.com/v0/b/glem-android.appspot.com/o/images%2F1716332048577.jpg?alt=media&token=0ee5239e-a8a9-4a31-9b3c-ed216f0c7396"
                    val marca = document.getString("marca") ?: "Não informada"
                    val modelo = document.getString("modelo") ?: "Não informado"
                    val ano = document.get("ano")?.let {
                        if (it is Long) it.toString() else it as? String
                    } ?: "Não informado"
                    val preco = document.get("preco")?.toString() ?: "Não informado"
                    val descricao = document.getString("descricao") ?: "Não informada"

                    items.add(ListItem(carId, imageUrl, marca, modelo, ano, preco, descricao))
                }
                adapter.notifyDataSetChanged() // Notifica o adapter para ele atualizar a listview
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Error loading data", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
    }

    // Infla o menu com os items
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    // Verifica se esta logado para deixar o botao de adicionar carros visivel
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        menu?.findItem(R.id.inserirCarros)?.isVisible = isLoggedIn
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.inserirCarros -> {
            startActivity(Intent(this, CadastrarCarros::class.java))
            true
        }
        R.id.login -> {
            startActivity(Intent(this, LoginActivity::class.java))
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
