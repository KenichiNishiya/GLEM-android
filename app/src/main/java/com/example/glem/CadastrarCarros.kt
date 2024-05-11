package com.example.glem

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class CadastrarCarros : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastrar_carros)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //private lateinit var text_show: TextView
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Inicializando elementos View
        val marca = findViewById<EditText>(R.id.inputMarca)
        val modelo = findViewById<EditText>(R.id.inputModelo)
        val ano = findViewById<EditText>(R.id.inputAno)
        val imageView = findViewById<ImageView>(R.id.imageView)

        //var text_show = findViewById<TextView>(R.id.show_text)

        // Criando a conexao com o bd do Firebase
        val storage = Firebase.storage("gs://glem-android.appspot.com")
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/AE86.jpg")
        val localFile = File.createTempFile("tempImage", "jpg")

        imageRef.getFile(localFile)
            .addOnSuccessListener {
                // Se conseguir baixar a imagem, transforma em bitmap, dimensiona e seta na view
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 700, 500, true)
                imageView.setImageBitmap(scaledBitmap)
            }
            .addOnFailureListener {
                // Executa se acontecer algum erro
                Toast.makeText(applicationContext, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
            }

        val add_button: Button = findViewById(R.id.button_add)
        add_button.setOnClickListener {
            // Pega o input do user
            val marcaText = marca.text.toString()
            val modeloText = modelo.text.toString()
            val anoText = ano.text.toString()

            // Insere no bd se os campos estiverem preenchidos
            if(marcaText.isNotBlank() && modeloText.isNotBlank() && anoText.isNotBlank()) {
                addDataToFirestore(marcaText, modeloText, anoText)

                marca.setText("")
                modelo.setText("")
                ano.setText("")
            }
            else{
                Toast.makeText(applicationContext, "Digite todos os dados", Toast.LENGTH_LONG).show()
            }
        }

        val show_button: Button = findViewById(R.id.button_show)
        show_button.setOnClickListener {
            retrieveDataFromFirestore()
        }

//        val buttonFragment: Button = findViewById(R.id.button_fragment)
//
//        buttonFragment.setOnClickListener {
//            // Create a FragmentManager
//        }

    }

    fun addDataToFirestore(marca: String, modelo: String, ano: String) {

        // Cria uma referencia ao Firestore
        val db = FirebaseFirestore.getInstance()

        // Cria um novo documento na collection de carros
        val carrosCollection = db.collection("carros")
        val newCarDoc = carrosCollection.document()

        // Adiciona dados no documento
        val data = hashMapOf(
            "marca" to marca,
            "modelo" to modelo,
            "ano" to ano
        )

        newCarDoc.set(data)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Inserido com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(applicationContext, "Erro ao inserir o documento", Toast.LENGTH_SHORT).show()
            }
    }

    fun retrieveDataFromFirestore() {

        var text_show: TextView = findViewById(R.id.show_text)
        // Cria uma referecia ao Firestore > collection carros
        val db = FirebaseFirestore.getInstance()
        val carrosCollection = db.collection("carros")

        // Faz um get para pegar todos os dados da collection
        carrosCollection.get()
            .addOnSuccessListener { result ->
                // Cria uma string pra colocar todos os itens
                val dataStringBuilder = StringBuilder()

                // Loop para iterar cada elemento da collection
                for (document in result) {
                    val marca = document.getString("marca")
                    val modelo = document.getString("modelo")
                    val ano = document.getString("ano")

                    dataStringBuilder.append("Marca: $marca, Modelo: $modelo, Ano: $ano\n")
                }

                // Atualiza a textview
                val allData = dataStringBuilder.toString()
                text_show.text = allData
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
    }


}