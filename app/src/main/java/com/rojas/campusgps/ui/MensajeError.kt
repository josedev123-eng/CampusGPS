package com.rojas.campusgps.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rojas.campusgps.model.TipoError
import com.rojas.campusgps.model.UbicacionUi

@Composable
fun MensajeError(
    error: UbicacionUi.Error,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(error.mensaje, color = MaterialTheme.colorScheme.onErrorContainer)

        when (error.tipo) {
            TipoError.SIN_PERMISO -> {
                // Abre la pantalla de ajustes de la app para dar el permiso a mano
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", contexto.packageName, null)
                    contexto.startActivity(intent)
                }) {
                    Text("Abrir ajustes")
                }
            }
            TipoError.GPS_APAGADO, TipoError.OTRO -> {
                Button(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }
            TipoError.SIN_PLAY_SERVICES -> {
                // Sin botón: no hay nada que reintentar
            }
        }
    }
}
