package com.rojas.campusgps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Componente visual de chips para seleccionar una categoría de filtro
@Composable
fun FiltroCategorias(
    categoriaSeleccionada: String?,
    onCategoriaSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categorias = listOf("Estudio", "Comida", "Eventos", "Servicios")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categorias.forEach { categoria ->
            FilterChip(
                selected = (categoria == categoriaSeleccionada),
                onClick = { onCategoriaSeleccionada(categoria) },
                label = { Text(categoria) }
            )
        }
    }
}
