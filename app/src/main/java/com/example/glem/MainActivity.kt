package com.example.glem

import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val marca = findViewById<EditText>(R.id.inputMarca)
        val modelo = findViewById<EditText>(R.id.inputModelo)
        val ano = findViewById<EditText>(R.id.inputAno)

        val storage = Firebase.storage("gs://glem-android.appspot.com")
        var storageRef = storage.reference
        var imageRef = storageRef.child("images/taipei.jpg")
        val localFile = File.createTempFile("tempImage", "jpg")

        val imageView = findViewById<ImageView>(R.id.imageView)

        imageRef.getFile(localFile)
            .addOnSuccessListener {
                // Image downloaded successfully, set it to the ImageView
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
            .addOnFailureListener {
                // Handle any errors
                //Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }

        val add_button: Button = findViewById(R.id.button_add)
        add_button.setOnClickListener {
            var marcaText = marca.text.toString()
            var modeloText = modelo.text.toString()
            var anoText = ano.text.toString()
//            val toast = Toast.makeText(applicationContext, "$marcaText, $modeloText, $anoText", Toast.LENGTH_LONG)
//            toast.show()
            if(marcaText != "" && modeloText != "" && anoText != "") {
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

    }

    // Inside your activity or fragment class
    fun addDataToFirestore(marca: String, modelo: String, ano: String) {

        // Get a reference to your Firestore database
        val db = FirebaseFirestore.getInstance()


        // Create a new document in the "carros" collection
        val carrosCollection = db.collection("carros")
        val newCarDoc = carrosCollection.document()

        // Add data to the document
        val data = hashMapOf(
            "marca" to marca,
            "modelo" to modelo,
            "ano" to ano
        )


//         Set data to the document
         newCarDoc.set(data)
             .addOnSuccessListener {
                 // Data added successfully
                 Log.d(TAG, "Data added successfully")
             }
             .addOnFailureListener { e ->
                 // Failed to add data
                 Log.w(TAG, "Error adding document", e)
             }
    }

    fun retrieveDataFromFirestore() {
        var text_show: TextView = findViewById(R.id.show_text)
// Get a reference to your Firestore database
        val db = FirebaseFirestore.getInstance()

// Reference to your "carros" collection
        val carrosCollection = db.collection("carros")

// Retrieve data from the "carros" collection
        carrosCollection.get()
            .addOnSuccessListener { result ->
                // Initialize a string to store retrieved data
                val dataStringBuilder = StringBuilder()

                // Iterate through each document in the result
                for (document in result) {
                    // Get document data and append it to the string
                    val marca = document.getString("marca")
                    val modelo = document.getString("modelo")
                    val ano = document.getString("ano")

                    dataStringBuilder.append("Marca: $marca, Modelo: $modelo, Ano: $ano\n")
                }

                // Once all data is retrieved, update the TextView
                val allData = dataStringBuilder.toString()
                text_show.text = allData
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}