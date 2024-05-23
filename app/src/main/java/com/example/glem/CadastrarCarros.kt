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

    lateinit var selectedImageUri: Uri
    lateinit var imageView: ImageView
    var isEditMode = false
    var carId: String? = null
    var originalImageUrl: String? = null
    var isImageSelected = false

    val REQUEST_CODE_PICK_IMAGE = 100 // Codigo de solicitacao de imagem da galeria
    val PERMISSION_REQUEST_CODE = 101 // Codigo de solicitacao de permissao para acessar o armazenamento
    val MAX_IMAGE_SIZE = 1024 // Tamanho maximo de pixels para a imagem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_carros)

        // Configura a toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        // Configura botao de volar
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Pega os elementos graficos
        val marca = findViewById<EditText>(R.id.inputMarca)
        val modelo = findViewById<EditText>(R.id.inputModelo)
        val ano = findViewById<EditText>(R.id.inputAno)
        val preco = findViewById<EditText>(R.id.inputPreco)
        val descricao = findViewById<EditText>(R.id.inputDescricao)
        imageView = findViewById(R.id.imageView)

        // Pegar imagem
        val inputImageButton: Button = findViewById(R.id.inputImagem)
        inputImageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Verifica versao do Android (13+)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {// Pede permissao ao armazenamento se nao tiver
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
                } else {
                    selectImageFromGallery() // Seleciona imagem da galeria
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) { // Outro metodo para pedir permissao para android 12-
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
                if (isEditMode) { // Se tiver no modo de edicao
                    updateCarInFirestore(marcaText, modeloText, anoText, precoText, descricaoText)
                } else {  // Se nao estiver no modo de edicao
                    if (isImageSelected) { // Se tiver uma imagem selecionada faz o upload
                        uploadToFirebase(marcaText, modeloText, anoText, precoText, descricaoText)
                    } else { // Se nao ele exige uma imagem
                        Toast.makeText(applicationContext, "Selecione uma imagem", Toast.LENGTH_LONG).show()
                    }
                }

                // Limpa todos os TextViews
                marca.setText("")
                modelo.setText("")
                ano.setText("")
                preco.setText("")
                descricao.setText("")
                imageView.setImageURI(null) // Remove imagem do ImageView
                imageView.setImageResource(android.R.color.transparent) // Coloca a imagem transparente
                selectedImageUri = Uri.EMPTY // Reseta a URI de imagem
                isImageSelected = false // Reseta o estado de seelcao de imagem
            } else {
                Toast.makeText(applicationContext, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            }
        }

        // Verifica se esta no modo de edicao e preenche os campos com os dados
        isEditMode = intent.getBooleanExtra("isEditMode", false)
        if (isEditMode) {
            carId = intent.getStringExtra("carId")
            marca.setText(intent.getStringExtra("marca"))
            modelo.setText(intent.getStringExtra("modelo"))
            ano.setText(intent.getStringExtra("ano"))
            preco.setText(intent.getStringExtra("preco"))
            descricao.setText(intent.getStringExtra("descricao"))
            originalImageUrl = intent.getStringExtra("imageUrl")
            Glide.with(this).load(originalImageUrl).into(imageView)
        }
    }

    private fun selectImageFromGallery() {
        // Intent para selecionar imagem
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Se a imagem for selecionada com sucesso
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri // Define a URI da imagem
                isImageSelected = true
                // Redimensiona e exibe a imagem
                Glide.with(this)
                    .load(uri)
                    .override(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                    .into(imageView)
            }
        }
    }

    private fun resizeImage(uri: Uri): ByteArray {
        val inputStream: InputStream? = contentResolver.openInputStream(uri) // Abre o InputStream da URI
        val originalBitmap = BitmapFactory.decodeStream(inputStream) // Transforma em Bitmap
        inputStream?.close() // Fecha InputStream

        // Calcula porporcao da imagem
        val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val width = if (originalBitmap.width > originalBitmap.height) MAX_IMAGE_SIZE else (MAX_IMAGE_SIZE * aspectRatio).toInt()
        val height = if (originalBitmap.height > originalBitmap.width) MAX_IMAGE_SIZE else (MAX_IMAGE_SIZE / aspectRatio).toInt()

        // Redimensiona a imagem, comprime em JPEG, retorna o byte array da imagem
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun uploadToFirebase(marca: String, modelo: String, ano: String, preco: String, descricao: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        // Cria uma referencia para a imagem com o unix timestamp
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        val resizedImageBytes = resizeImage(selectedImageUri) // Redimensiona
        val uploadTask = imageRef.putBytes(resizedImageBytes) // Faz o upload

        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString() // Pega a URL da imagem uploadada
                addDataToFirestore(marca, modelo, ano, preco, descricao, imageUrl)
            }
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCarInFirestore(marca: String, modelo: String, ano: String, preco: String, descricao: String) {
        val db = FirebaseFirestore.getInstance()
        val carDoc = db.collection("carros").document(carId!!) // Pega o doc do carro especificado

        val data = hashMapOf(
            "marca" to marca,
            "modelo" to modelo,
            "ano" to ano,
            "preco" to preco,
            "descricao" to descricao,
            "imagem" to originalImageUrl
        )

        // Se houver uma nova imagem selecionada, faz o upload e depois atualiza os dados
        if (isImageSelected) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

            val resizedImageBytes = resizeImage(selectedImageUri)
            val uploadTask = imageRef.putBytes(resizedImageBytes)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString() // Pega a URL da imagem
                    data["imagem"] = imageUrl // Atualiza o URL no hashmap
                    carDoc.set(data) // Atualiza os dados no Firestore
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
            // Se nao foi selecionada uma nova imagem, apenas atualiza os dados
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
        val newCarDoc = carrosCollection.document() // Cria um novo documento

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
        if (requestCode == PERMISSION_REQUEST_CODE) { // Verifica o codigo de requisicao da permissao
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // verifica se a permissao foi concedida
                selectImageFromGallery()
            } else {
                Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
