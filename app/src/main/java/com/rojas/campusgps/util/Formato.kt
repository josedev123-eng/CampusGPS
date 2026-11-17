package com.rojas.campusgps.util

// Menos de 1000 m: "350 m". Desde 1000 m: "1.5 km"
fun formatearDistancia(metros: Float): String {
    return if (metros < 1000) {
        "${metros.toInt()} m"
    } else {
        String.format("%.1f km", metros / 1000)
    }
}
