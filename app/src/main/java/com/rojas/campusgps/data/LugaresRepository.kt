package com.rojas.campusgps.data

import com.rojas.campusgps.model.Lugar

interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}
