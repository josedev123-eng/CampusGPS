package com.rojas.campusgps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rojas.campusgps.model.UbicacionUi
import com.rojas.campusgps.util.formatearDistancia
import com.rojas.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()
    val lugares by vm.lugares.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val masCercano by vm.masCercano.collectAsState()
    val categoriaSeleccionada by vm.categoriaSeleccionada.collectAsState()

    val contexto = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        // Si aceptó FINE o COARSE, buscamos la ubicación
        if (resultado.values.any { it }) {
            vm.buscar()
        } else {
            vm.permisoDenegado()
        }
    }

    fun alPulsar() {
        val fine = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            vm.buscar()
        } else {
            lanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { alPulsar() },
                enabled = estado !is UbicacionUi.Buscando
            ) {
                Text("Obtener mi ubicación")
            }

            when (val e = estado) {
                is UbicacionUi.Inicial -> Text("Pulsa el botón para ver dónde estás")
                is UbicacionUi.Buscando -> CircularProgressIndicator()
                is UbicacionUi.Lista -> {
                    Text("Latitud: ${e.posicion.latitud}")
                    Text("Longitud: ${e.posicion.longitud}")
                }
                is UbicacionUi.Error -> MensajeError(error = e, onReintentar = { alPulsar() })
            }

            // Chips de filtro por categoría
            FiltroCategorias(
                categoriaSeleccionada = categoriaSeleccionada,
                onCategoriaSeleccionada = { categoria -> vm.filtrar(categoria) }
            )

            // El mapa ocupa todo el espacio libre
            MapaUbicacion(
                estado = estado,
                lugares = lugares,
                seleccionado = seleccionado,
                masCercano = masCercano,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // La lista va debajo con altura fija
            ListaLugares(
                lugares = lugares,
                seleccionado = seleccionado,
                masCercanoId = masCercano?.id,
                onClick = { id -> vm.seleccionar(id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            // Botón para ir al lugar más cercano
            OutlinedButton(
                onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                enabled = masCercano != null
            ) {
                Text("Ir al más cercano")
            }

            val cercano = masCercano
            if (cercano != null) {
                val metros = lugares.find { it.lugar.id == cercano.id }?.metros
                val texto = if (metros != null) formatearDistancia(metros) else ""
                Text("Más cercano: ${cercano.nombre} ($texto)")
            }
        }
    }
}
