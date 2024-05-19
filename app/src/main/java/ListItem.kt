package com.example.glem

data class ListItem(
    val carId: String, // Add carId as the first field
    val imageUrl: String,
    val marca: String,
    val modelo: String,
    val ano: String,
    val preco: String,
    val descricao: String
)
