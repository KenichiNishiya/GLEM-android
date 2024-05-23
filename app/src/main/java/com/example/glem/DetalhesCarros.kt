package com.example.glem

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class DetalhesCarros : AppCompatActivity() {

    lateinit var carId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_carros)

        // Configuracao do toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        // Quando clica no botao de voltar do menu seria igual botao o de voltar do sistema
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Encontra os elementos do layout
        val carImage: ImageView = findViewById(R.id.car_image)
        val textMarcaModeloAno: TextView = findViewById(R.id.text_marca_modelo_ano)
        val textPreco: TextView = findViewById(R.id.text_preco)
        val textDescricao: TextView = findViewById(R.id.text_descricao)
        val buttonNegociar: Button = findViewById(R.id.button_negociar)
        val buttonDeletar: Button = findViewById(R.id.button_deletar)
        val buttonEditar: Button = findViewById(R.id.button_editar)

        // Pega os dados passados com a Intent
        val imageUrl = intent.getStringExtra("imageUrl")
        val marca = intent.getStringExtra("marca")
        val modelo = intent.getStringExtra("modelo")
        val ano = intent.getStringExtra("ano")
        val preco = intent.getStringExtra("preco")
        val descricao = intent.getStringExtra("descricao")
        carId = intent.getStringExtra("carId") ?: ""

        // Carrega os dados
        Glide.with(this).load(imageUrl).into(carImage)
        textMarcaModeloAno.text = "$marca $modelo - $ano"
        textPreco.text = "R$ $preco"
        textDescricao.text = descricao

        // Verificado estado do login
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        // Se estiver logado mostra os botoes de remover e editar
        if (isLoggedIn) {
            buttonEditar.setOnClickListener {
                // Leva os dados pra activity de alterar
                val intent = Intent(this, CadastrarCarros::class.java).apply {
                    putExtra("isEditMode", true)
                    putExtra("carId", carId)
                    putExtra("imageUrl", imageUrl)
                    putExtra("marca", marca)
                    putExtra("modelo", modelo)
                    putExtra("ano", ano)
                    putExtra("preco", preco)
                    putExtra("descricao", descricao)
                }
                startActivity(intent)
            }

            // Botao de remover o documento do bd
            buttonDeletar.setOnClickListener {
                if (carId.isNotBlank()) {
                    showDeleteConfirmationDialog()
                } else {
                    Toast.makeText(this, "ID invalido", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Se nao estiver logado desabilita e esconde os botoes
            buttonEditar.isEnabled = false
            buttonEditar.visibility = View.GONE
            buttonDeletar.isEnabled = false
            buttonDeletar.visibility = View.GONE
        }

        buttonNegociar.setOnClickListener {
            val numero = "+5511914188176"
            openWhatsApp(numero)
        }
    }

    private fun openWhatsApp(numero: String) {
        val uri = Uri.parse("https://wa.me/$numero") // Cria URI para o whats
        val intent = Intent(Intent.ACTION_VIEW, uri) // Cria uma Intent pra abrir o URI
        intent.setPackage("com.whatsapp") // Define que sera aberto pelo whats

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp nao instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirma remoção")
        builder.setMessage("Tem certeza que quer remover esse carro?")
        builder.setPositiveButton("Sim") { dialog, which ->
            deleteCarFromFirestore()
        }
        builder.setNegativeButton("Não") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun deleteCarFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val carDoc = db.collection("carros").document(carId)

        carDoc.delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Carro deletado com sucesso", Toast.LENGTH_SHORT).show()
                finish() // Fecha a activity apos excluir o documento
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao deletar o carro", Toast.LENGTH_SHORT).show()
            }
    }
}
