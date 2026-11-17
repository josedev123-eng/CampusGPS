package com.rojas.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.rojas.campusgps.model.Posicion
import kotlinx.coroutines.tasks.await

class UbicacionRepositoryGoogle(private val context: Context) : UbicacionRepository {

    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun obtenerPosicion(): Result<Posicion> {
        return try {
            val ubicacion = cliente.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (ubicacion != null) {
                Result.success(Posicion(ubicacion.latitude, ubicacion.longitude))
            } else {
                // getCurrentLocation devuelve null cuando el GPS está apagado
                Result.failure(IllegalStateException("Ubicación no disponible"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun playServicesDisponible(): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }
}
