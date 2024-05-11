package com.example.glem

import CustomAdapter
import ListItem
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

open class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        val listView = findViewById<ListView>(R.id.listView)

        val items = listOf(
            ListItem(R.drawable.ic_launcher_background, "1", "One"),
            ListItem(R.drawable.ic_launcher_background, "2", "Two")
            // Add more items...
        )

        val adapter = CustomAdapter(this, items)
        listView.adapter = adapter

        // Setting up the item click listener
        listView.setOnItemClickListener { parent, view, position, id ->
            val item = adapter.getItem(position)
            Toast.makeText(
                this,
                "Clicked: ${item?.numberInDigit} - ${item?.numbersInText}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.func1 -> {
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(applicationContext, "Foi pra 1", Toast.LENGTH_LONG).show()
            true
        }
        R.id.func2 -> {
            Toast.makeText(this, "Foi pra 2", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, CadastrarCarros::class.java))
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

}