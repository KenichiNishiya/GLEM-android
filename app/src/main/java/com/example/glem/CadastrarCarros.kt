package com.example.glem

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CadastrarCarros : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var imageView: ImageView

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 100
        private const val PERMISSION_REQUEST_CODE = 101
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            selectImageFromGallery()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_carros)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val marca = findViewById<EditText>(R.id.inputMarca)
        val modelo = findViewById<EditText>(R.id.inputModelo)
        val ano = findViewById<EditText>(R.id.inputAno)
        val preco = findViewById<EditText>(R.id.inputPreco)
        val descricao = findViewById<EditText>(R.id.inputDescricao)
        imageView = findViewById(R.id.imageView)

        val inputImageButton: Button = findViewById(R.id.inputImagem)
        inputImageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                selectImageFromGallery()
            }
        }

        val addButton: Button = findViewById(R.id.button_add)
        addButton.setOnClickListener {
            val marcaText = marca.text.toString()
            val modeloText = modelo.text.toString()
            val anoText = ano.text.toString()
            val precoText = preco.text.toString()
            val descricaoText = descricao.text.toString()

            if (selectedImageUri != null &&
                marcaText.isNotBlank() &&
                modeloText.isNotBlank() &&
                anoText.isNotBlank() &&
                precoText.isNotBlank() &&
                descricaoText.isNotBlank()) {

                uploadImageToFirebase(marcaText, modeloText, anoText, precoText, descricaoText)
            } else {
                Toast.makeText(applicationContext, "Preencha todos os campos e selecione uma imagem", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                imageView.setImageURI(uri)
            }
        }
    }

    private fun uploadImageToFirebase(marca: String, modelo: String, ano: String, preco: String, descricao: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        selectedImageUri?.let {
            imageRef.putFile(it)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        addDataToFirestore(marca, modelo, ano, preco, descricao, imageUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addDataToFirestore(
        marca: String,
        modelo: String,
        ano: String,
        preco: String,
        descricao: String,
        imageUrl: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val carrosCollection = db.collection("carros")
        val newCarDoc = carrosCollection.document()

        val data = hashMapOf(
            "marca" to marca,
            "modelo" to modelo,
            "ano" to ano,
            "preco" to preco,
            "descricao" to descricao,
            "imagem" to imageUrl
        )

        newCarDoc.set(data)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Inserido com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(applicationContext, "Erro ao inserir o documento", Toast.LENGTH_SHORT).show()
            }
    }
}
