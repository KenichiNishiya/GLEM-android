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

    private lateinit var carId: String // Variable to store the car document ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_carros)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val carImage: ImageView = findViewById(R.id.car_image)
        val textMarcaModeloAno: TextView = findViewById(R.id.text_marca_modelo_ano)
        val textPreco: TextView = findViewById(R.id.text_preco)
        val textDescricao: TextView = findViewById(R.id.text_descricao)
        val buttonNegociar: Button = findViewById(R.id.button_negociar)
        val buttonDeletar: Button = findViewById(R.id.button_deletar)
        val buttonEditar: Button = findViewById(R.id.button_editar)

        val imageUrl = intent.getStringExtra("imageUrl")
        val marca = intent.getStringExtra("marca")
        val modelo = intent.getStringExtra("modelo")
        val ano = intent.getStringExtra("ano")
        val preco = intent.getStringExtra("preco")
        val descricao = intent.getStringExtra("descricao")
        carId = intent.getStringExtra("carId") ?: ""

        Glide.with(this).load(imageUrl).into(carImage)
        textMarcaModeloAno.text = "$marca $modelo - $ano"
        textPreco.text = "R$ $preco"
        textDescricao.text = descricao

        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            buttonEditar.setOnClickListener {
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

            buttonDeletar.setOnClickListener {
                if (carId.isNotBlank()) {
                    showDeleteConfirmationDialog()
                } else {
                    Toast.makeText(this, "Invalid car ID.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            buttonEditar.isEnabled = false
            buttonEditar.visibility = View.GONE // Hide the button
            buttonDeletar.isEnabled = false
            buttonDeletar.visibility = View.GONE // Hide the button
        }

        buttonNegociar.setOnClickListener {
            val phoneNumber = "+5511995684681" // Replace with the actual phone number
            openWhatsApp(phoneNumber)
        }
    }

    private fun openWhatsApp(phoneNumber: String) {
        val uri = Uri.parse("https://wa.me/$phoneNumber")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.whatsapp")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp not installed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete this car?")
        builder.setPositiveButton("Yes") { dialog, which ->
            deleteCarFromFirestore()
        }
        builder.setNegativeButton("No") { dialog, which ->
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
                finish() // Close the activity after deletion
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao deletar o carro", Toast.LENGTH_SHORT).show()
            }
    }
}
