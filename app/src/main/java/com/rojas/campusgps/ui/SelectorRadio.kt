package com.rojas.campusgps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Componente visual para elegir el radio de cercanía (100m, 200m, 500m)
@Composable
fun SelectorRadio(
    radioActual: Int,
    habilitado: Boolean,
    onRadioSeleccionado: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val opciones = listOf(100, 200, 500)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { opcion ->
            FilterChip(
                selected = (opcion == radioActual),
                onClick = { onRadioSeleccionado(opcion) },
                enabled = habilitado,
                label = { Text("$opcion m") }
            )
        }
    }
}
