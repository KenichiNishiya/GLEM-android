package com.example.glem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configura a toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        // Configura botao de back
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Pega os elementos graficos
        val usernameEditText: EditText = findViewById(R.id.edit_text_username)
        val passwordEditText: EditText = findViewById(R.id.edit_text_password)
        val loginButton: Button = findViewById(R.id.button_login)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Login hardcoded
            if (username == "admin" && password == "admin") {
                // Armazena o estado de login
                val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
