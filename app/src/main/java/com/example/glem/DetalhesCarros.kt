package com.example.glem

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

class DetalhesCarros : AppCompatActivity() {

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

        val imageUrl = intent.getStringExtra("imageUrl")
        val marca = intent.getStringExtra("marca")
        val modelo = intent.getStringExtra("modelo")
        val ano = intent.getStringExtra("ano")
        val preco = intent.getStringExtra("preco")
        val descricao = intent.getStringExtra("descricao")

        Glide.with(this).load(imageUrl).into(carImage)
        textMarcaModeloAno.text = "$marca $modelo - $ano"
        textPreco.text = "R$ $preco"
        textDescricao.text = descricao

        buttonNegociar.setOnClickListener {
            // Implement negotiation action here
        }
    }
}
