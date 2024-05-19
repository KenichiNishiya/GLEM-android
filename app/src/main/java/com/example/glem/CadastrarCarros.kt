package com.example.glem

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CadastrarCarros : AppCompatActivity() {

    private lateinit var selectedImageUri: Uri
    private lateinit var imageView: ImageView
    private var isEditMode = false
    private var carId: String? = null
    private var originalImageUrl: String? = null
    private var isImageSelected = false

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 100
        private const val PERMISSION_REQUEST_CODE = 101
        private const val MAX_IMAGE_SIZE = 1024 // Max size in pixels for width/height
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_carros)

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

        val marca = findViewById<EditText>(R.id.inputMarca)
        val modelo = findViewById<EditText>(R.id.inputModelo)
        val ano = findViewById<EditText>(R.id.inputAno)
        val preco = findViewById<EditText>(R.id.inputPreco)
        val descricao = findViewById<EditText>(R.id.inputDescricao)
        imageView = findViewById(R.id.imageView)

        val inputImageButton: Button = findViewById(R.id.inputImagem)
        inputImageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
                } else {
                    selectImageFromGallery()
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
                } else {
                    selectImageFromGallery()
                }
            }
        }

        val addButton: Button = findViewById(R.id.button_add)
        addButton.setOnClickListener {
            val marcaText = marca.text.toString()
            val modeloText = modelo.text.toString()
            val anoText = ano.text.toString()
            val precoText = preco.text.toString()
            val descricaoText = descricao.text.toString()

            if (marcaText.isNotBlank() &&
                modeloText.isNotBlank() &&
                anoText.isNotBlank() &&
                precoText.isNotBlank() &&
                descricaoText.isNotBlank()) {
                if (isEditMode) {
                    updateCarInFirestore(marcaText, modeloText, anoText, precoText, descricaoText)
                } else {
                    if (isImageSelected) {
                        uploadImageToFirebase(marcaText, modeloText, anoText, precoText, descricaoText)
                    } else {
                        Toast.makeText(applicationContext, "Selecione uma imagem", Toast.LENGTH_LONG).show()
                    }
                }

                // Reset all fields and the ImageView to default state
                marca.setText("")
                modelo.setText("")
                ano.setText("")
                preco.setText("")
                descricao.setText("")
                imageView.setImageURI(null) // Remove image from ImageView
                // Ensure that the ImageView is blank or transparent
                imageView.setImageResource(android.R.color.transparent)
                selectedImageUri = Uri.EMPTY // Reset the selectedImageUri to avoid false positives
                isImageSelected = false // Reset the image selection state
            } else {
                Toast.makeText(applicationContext, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            }
        }

        // Check if in edit mode and pre-fill fields
        isEditMode = intent.getBooleanExtra("isEditMode", false)
        if (isEditMode) {
            carId = intent.getStringExtra("carId")
            marca.setText(intent.getStringExtra("marca"))
            modelo.setText(intent.getStringExtra("modelo"))
            ano.setText(intent.getStringExtra("ano"))
            preco.setText(intent.getStringExtra("preco"))
            descricao.setText(intent.getStringExtra("descricao"))
            originalImageUrl = intent.getStringExtra("imageUrl")

            // Load the existing image
            Glide.with(this).load(originalImageUrl).into(imageView)
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
                isImageSelected = true // Set the image selection state to true
                // Resize and display the image using Glide
                Glide.with(this)
                    .load(uri)
                    .override(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                    .into(imageView)
            }
        }
    }

    private fun resizeImage(uri: Uri): ByteArray {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val width = if (originalBitmap.width > originalBitmap.height) MAX_IMAGE_SIZE else (MAX_IMAGE_SIZE * aspectRatio).toInt()
        val height = if (originalBitmap.height > originalBitmap.width) MAX_IMAGE_SIZE else (MAX_IMAGE_SIZE / aspectRatio).toInt()

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun uploadImageToFirebase(marca: String, modelo: String, ano: String, preco: String, descricao: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        val resizedImageBytes = resizeImage(selectedImageUri)
        val uploadTask = imageRef.putBytes(resizedImageBytes)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                addDataToFirestore(marca, modelo, ano, preco, descricao, imageUrl)
            }
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCarInFirestore(marca: String, modelo: String, ano: String, preco: String, descricao: String) {
        val db = FirebaseFirestore.getInstance()
        val carDoc = db.collection("carros").document(carId!!)

        val data = hashMapOf(
            "marca" to marca,
            "modelo" to modelo,
            "ano" to ano,
            "preco" to preco,
            "descricao" to descricao,
            "imagem" to originalImageUrl
        )

        // If a new image is selected, upload it and update the image URL
        if (isImageSelected) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

            val resizedImageBytes = resizeImage(selectedImageUri)
            val uploadTask = imageRef.putBytes(resizedImageBytes)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    data["imagem"] = imageUrl
                    carDoc.set(data)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Carro atualizado com sucesso", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Erro ao atualizar o carro", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(applicationContext, "Erro ao carregar nova imagem", Toast.LENGTH_SHORT).show()
            }
        } else {
            // No new image selected, just update the Firestore document
            carDoc.set(data)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Carro atualizado com sucesso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Erro ao atualizar o carro", Toast.LENGTH_SHORT).show()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery()
            } else {
                Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
