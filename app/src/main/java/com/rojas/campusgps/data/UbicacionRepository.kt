package com.rojas.campusgps.data

import com.rojas.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
    fun playServicesDisponible(): Boolean
}
