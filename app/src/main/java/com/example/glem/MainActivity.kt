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

class MainActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var adapter: CustomAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    var items = mutableListOf<ListItem>() // Vazio inicialmente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuracao do toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "" // Remove o titulo "GLEM"
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark) // Muda cor

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        listView = findViewById(R.id.listView)
        adapter = CustomAdapter(this, items) // Iniciliza o adapter
        listView.adapter = adapter // Configura o adapter no listview

        swipeRefreshLayout.setOnRefreshListener {
            retrieveDataFromFirestore() // Carrega os dados quando desliza pra baixo
        }

        retrieveDataFromFirestore() // Carrega dados de inicio

        // Quando clica em um item do list view
        listView.setOnItemClickListener { parent, view, position, id ->
            val item = adapter.getItem(position) // Pega o item do adapter na position clicada
            if (item != null) {
                // Cria um intent com uma refencia a classe de DetalhesCarros
                val intent = Intent(this, DetalhesCarros::class.java).apply {// configura e retorna o intent
                    putExtra("imageUrl", item.imageUrl) // Adiciona dados extras para serem passados no intent
                    putExtra("marca", item.marca)
                    putExtra("modelo", item.modelo)
                    putExtra("ano", item.ano)
                    putExtra("preco", item.preco)
                    putExtra("descricao", item.descricao)
                    putExtra("carId", item.carId)
                }
                startActivity(intent) // Inicia a activity com os dados
            } else {
                Toast.makeText(this, "Item não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun retrieveDataFromFirestore() {
        val db = FirebaseFirestore.getInstance() // Instancia do Firestore
        val carrosCollection = db.collection("carros")

        carrosCollection.get()
            .addOnSuccessListener { result ->
                items.clear() // Limpa os items ja existentes
                for (document in result) {
                    val carId = document.id // Pega o id do documento
                    val imageUrl = document.getString("imagem") ?: "https://firebasestorage.googleapis.com/v0/b/glem-android.appspot.com/o/images%2F1716332048577.jpg?alt=media&token=0ee5239e-a8a9-4a31-9b3c-ed216f0c7396"
                    val marca = document.getString("marca") ?: "Não informada"
                    val modelo = document.getString("modelo") ?: "Não informado"
                    val ano = document.get("ano")?.toString() ?: "Não informado"
                    val preco = document.get("preco")?.toString() ?: "Não informado"
                    val descricao = document.getString("descricao") ?: "Não informada"

                    items.add(ListItem(carId, imageUrl, marca, modelo, ano, preco, descricao))
                }
                adapter.notifyDataSetChanged() // Notifica o adapter para ele atualizar a listview
                swipeRefreshLayout.isRefreshing = false // Nao atualiza sem essa linha
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Error loading data", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false // Nao atualiza sem essa linha
            }
    }

    // Infla o menu com os items
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu) // Pega o xml de menu com as opcoes
        return true
    }

    // Verifica se esta logado para deixar o botao de adicionar carros visivel
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE) // Obtem as preferencias compartilhadas
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false) // Verifica se esta logado
        menu?.findItem(R.id.inserirCarros)?.isVisible = isLoggedIn // Define a visibilidade do item de acordo com o estado de login
        return super.onPrepareOptionsMenu(menu)
    }

    // Leva pra outras telas de acordo com a opcao do menu
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
            super.onOptionsItemSelected(item)// Pra qualquer outra opcao, a superclasse cuida dela
        }
    }
}
