package com.rojas.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.rojas.campusgps.model.Lugar
import com.rojas.campusgps.model.LugarConDistancia
import com.rojas.campusgps.model.UbicacionUi

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    masCercano: Lugar?,
    radioMetros: Int,
    tienePermiso: Boolean,
    modifier: Modifier = Modifier
) {
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.0464, -77.0428), 12f)
    }

    // Cuando llega la ubicación o cambia el radio, la cámara viaja a mi posición
    // con el zoom necesario para que se vea todo el círculo
    LaunchedEffect(estado, radioMetros) {
        if (estado is UbicacionUi.Lista) {
            val miPosicion = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            camara.animate(CameraUpdateFactory.newLatLngZoom(miPosicion, zoomParaRadio(radioMetros)))
        }
    }

    // Cuando se toca un lugar de la lista, la cámara viaja hasta él.
    // Si no hay lugar seleccionado (por ejemplo, al cambiar o quitar el filtro),
    // la cámara vuelve a mi ubicación actual.
    LaunchedEffect(seleccionado, lugares) {
        val lugar = lugares.find { it.lugar.id == seleccionado }?.lugar
        if (lugar != null) {
            camara.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(lugar.latitud, lugar.longitud), 17f)
            )
        } else if (estado is UbicacionUi.Lista) {
            val miPosicion = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            camara.animate(CameraUpdateFactory.newLatLngZoom(miPosicion, zoomParaRadio(radioMetros)))
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = camara,
        // Activa el punto azul de ubicación nativo solo si hay permiso
        properties = MapProperties(isMyLocationEnabled = tienePermiso),
        // Activa el botón nativo para ir a mi ubicación solo si hay permiso
        uiSettings = MapUiSettings(myLocationButtonEnabled = tienePermiso)
    ) {
        if (estado is UbicacionUi.Lista) {
            val miPosicion = LatLng(estado.posicion.latitud, estado.posicion.longitud)

            Marker(
                state = rememberUpdatedMarkerState(position = miPosicion),
                title = "Estás aquí"
            )

            // Círculo que representa el radio de cercanía alrededor de mi ubicación
            Circle(
                center = miPosicion,
                radius = radioMetros.toDouble(),
                fillColor = Color(0x332196F3), // Azul semitransparente
                strokeColor = Color(0xFF2196F3), // Borde azul
                strokeWidth = 2f
            )
        }

        // Un marcador por cada lugar: verde el más cercano, azul los demás
        lugares.forEach { item ->
            val color = if (item.lugar.id == masCercano?.id) {
                BitmapDescriptorFactory.HUE_GREEN
            } else {
                BitmapDescriptorFactory.HUE_AZURE
            }
            key(item.lugar.id) {
                Marker(
                    state = rememberUpdatedMarkerState(
                        position = LatLng(item.lugar.latitud, item.lugar.longitud)
                    ),
                    title = item.lugar.nombre,
                    snippet = item.lugar.categoria,
                    icon = BitmapDescriptorFactory.defaultMarker(color)
                )
            }
        }
    }
}

// Zoom del mapa según el radio: mientras más grande el radio, más lejos la cámara
private fun zoomParaRadio(radioMetros: Int): Float {
    return when {
        radioMetros <= 100 -> 17f
        radioMetros <= 200 -> 16f
        else -> 15f
    }
}
