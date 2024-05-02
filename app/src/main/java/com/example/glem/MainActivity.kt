package com.example.glem

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.func1 -> {
            val fragmentManager: FragmentManager = supportFragmentManager

            // Begin a FragmentTransaction
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

            // Replace the contents of the container with the new fragment
            val fragment = HomeFragment()
            fragmentTransaction.replace(R.id.fragment_container, fragment)

            // Add the transaction to the back stack
            fragmentTransaction.addToBackStack(null)

            // Commit the transaction
            fragmentTransaction.commit()


            true
        }
        R.id.func2 -> {
            val fragmentManager: FragmentManager = supportFragmentManager

            // Begin a FragmentTransaction
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

            // Replace the contents of the container with the new fragment
            val fragment = ScrollingFragment()
            fragmentTransaction.replace(R.id.fragment_container, fragment)

            // Add the transaction to the back stack
            fragmentTransaction.addToBackStack(null)

            // Commit the transaction
            fragmentTransaction.commit()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

}