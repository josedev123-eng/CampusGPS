package com.rojas.campusgps.util

import android.location.Location
import com.rojas.campusgps.model.Lugar
import com.rojas.campusgps.model.Posicion

// Devuelve la distancia en metros desde mi posición hasta un lugar
fun distanciaMetros(desde: Posicion, hasta: Lugar): Float {
    val resultado = FloatArray(1)
    Location.distanceBetween(
        desde.latitud, desde.longitud,
        hasta.latitud, hasta.longitud,
        resultado
    )
    return resultado[0]
}
