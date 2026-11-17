package com.rojas.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.campusgps.data.LugaresRepository
import com.rojas.campusgps.data.UbicacionRepository
import com.rojas.campusgps.model.Lugar
import com.rojas.campusgps.model.LugarConDistancia
import com.rojas.campusgps.model.Posicion
import com.rojas.campusgps.model.TipoError
import com.rojas.campusgps.model.UbicacionUi
import com.rojas.campusgps.util.distanciaMetros
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    // Lista original con todos los lugares
    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )

    // Categoría seleccionada (null si no hay ninguna seleccionada)
    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada.asStateFlow()

    // Lista de lugares filtrada por la categoría seleccionada
    val lugares: StateFlow<List<LugarConDistancia>> = combine(
        _lugares,
        _categoriaSeleccionada
    ) { lista, categoria ->
        if (categoria == null) {
            lista
        } else {
            lista.filter { it.lugar.categoria == categoria }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _lugares.value)

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    // Filtra por categoría. Si ya está seleccionada, quita el filtro (pone null)
    fun filtrar(categoria: String?) {
        if (_categoriaSeleccionada.value == categoria) {
            _categoriaSeleccionada.value = null
        } else {
            _categoriaSeleccionada.value = categoria
        }
        // Al cambiar el filtro limpio la selección, por si el lugar elegido ya no se ve
        _seleccionado.value = null
    }

    fun buscar() {
        // Primero reviso si el celular tiene Google Play Services
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }

        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    // Calcula la distancia a cada lugar y ordena del más cercano al más lejano
    private fun actualizarDistancias(posicion: Posicion) {
        val lista = lugaresRepo.obtenerLugares().map { lugar ->
            LugarConDistancia(lugar, distanciaMetros(posicion, lugar))
        }
        val ordenada = lista.sortedBy { it.metros }
        _lugares.value = ordenada

        ordenada.forEach {
            Log.d("CampusGPS", "${it.lugar.nombre}: ${it.metros} m")
        }
    }

    // Guarda el lugar que el usuario tocó en la lista
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    // El más cercano es el primero de la lista (filtrada) que ya tiene distancia
    val masCercano: StateFlow<Lugar?> = lugares
        .map { lista -> lista.firstOrNull { it.metros != null }?.lugar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Convierte el error que llega del Repository en un estado de error para la pantalla
    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error(
            "Falta el permiso de ubicación",
            TipoError.SIN_PERMISO
        )
        is IllegalStateException -> UbicacionUi.Error(
            "Activa el GPS e intenta de nuevo",
            TipoError.GPS_APAGADO
        )
        else -> UbicacionUi.Error(
            e.message ?: "No se pudo obtener la ubicación",
            TipoError.OTRO
        )
    }
}
