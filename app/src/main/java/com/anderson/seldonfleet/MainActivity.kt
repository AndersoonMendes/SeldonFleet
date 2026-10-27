package com.anderson.seldonfleet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anderson.seldonfleet.data.mapper.DeviceMapper
import com.anderson.seldonfleet.data.repository.FleetRepositoryImpl
import com.anderson.seldonfleet.data.source.AssetFleetSource
import com.anderson.seldonfleet.domain.model.Device
import com.anderson.seldonfleet.domain.model.DeviceStatus
import com.anderson.seldonfleet.domain.usecase.ClassifyDeviceUseCase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                FleetScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetScreen() {
    val context = LocalContext.current

    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var filtro by remember { mutableStateOf("TODOS") }
    var somenteFavoritos by remember { mutableStateOf(false) }
    var selecionado by remember { mutableStateOf<Device?>(null) }

    val preferences = remember {
        context.getSharedPreferences("seldon_fleet", android.content.Context.MODE_PRIVATE)
    }

    val favoritos = remember {
        mutableStateOf(preferences.getStringSet("favoritos", emptySet())?.toSet() ?: emptySet())
    }

    val classificador = remember { ClassifyDeviceUseCase() }

    LaunchedEffect(Unit) {
        try {
            val source = AssetFleetSource(context)
            val repository = FleetRepositoryImpl(source, DeviceMapper())
            devices = repository.getDevices()
        } catch (e: Exception) {
            error = e.message ?: "Não foi possível carregar frota.json"
        }
    }

    val classificados = devices.map { device ->
        device to classificador.execute(device)
    }

    val contagens = DeviceStatus.entries.associateWith { status ->
        classificados.count { it.second == status }
    }

    val listaFiltrada = classificados.filter { (device, status) ->
        (filtro == "TODOS" || status.name == filtro) &&
                (!somenteFavoritos || device.id in favoritos.value)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SeldonFleet", fontWeight = FontWeight.Bold)
                        Text(
                            "Monitoramento de dispositivos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Resumo da frota", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResumoCard("Total", devices.size.toString(), Modifier.weight(1f))
                ResumoCard("OK", "${contagens[DeviceStatus.OK] ?: 0}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResumoCard(
                    "Atenção",
                    "${contagens[DeviceStatus.ATENCAO] ?: 0}",
                    Modifier.weight(1f)
                )
                ResumoCard(
                    "Críticos",
                    "${contagens[DeviceStatus.CRITICO] ?: 0}",
                    Modifier.weight(1f)
                )
                ResumoCard(
                    "Indeterminados",
                    "${contagens[DeviceStatus.INDETERMINADO] ?: 0}",
                    Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Filtrar por status", fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("TODOS", "OK", "ATENCAO", "CRITICO", "INDETERMINADO")
                    .forEach { status ->
                        FilterChip(
                            selected = filtro == status,
                            onClick = { filtro = status },
                            label = {
                                Text(
                                    when (status) {
                                        "TODOS" -> "Todos"
                                        "ATENCAO" -> "Atenção"
                                        "CRITICO" -> "Crítico"
                                        "INDETERMINADO" -> "N/D"
                                        else -> "OK"
                                    }
                                )
                            }
                        )
                    }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { somenteFavoritos = !somenteFavoritos }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = somenteFavoritos,
                    onCheckedChange = { somenteFavoritos = it }
                )
                Text("Mostrar somente favoritos")
            }

            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            when {
                error != null -> {
                    Text("Erro ao carregar a frota: $error")
                }

                devices.isEmpty() -> {
                    CircularProgressIndicator()
                    Text("Carregando dispositivos...")
                }

                listaFiltrada.isEmpty() -> {
                    Text("Nenhum dispositivo encontrado.")
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listaFiltrada, key = { it.first.id }) { (device, status) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selecionado = device },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            device.model,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            device.id,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text("Bateria: ${device.batteryLevelPct}%")
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            statusLabel(status),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            if (device.id in favoritos.value) "★ Favorito" else "☆"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selecionado?.let { device ->
        val status = classificador.execute(device)
        val favorito = device.id in favoritos.value

        AlertDialog(
            onDismissRequest = { selecionado = null },
            title = { Text(device.model) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ID: ${device.id}")
                    Text("Status: ${statusLabel(status)}")
                    Text("Justificativa", fontWeight = FontWeight.Bold)
                    Text(classificationReason(device, status))
                    Text("Sistema: ${device.soc}")
                    Text("Android API: ${device.androidApi}")
                    Text("RAM: ${device.ramMb} MB")
                    Text("Capacidade: ${device.batteryCapacityMah ?: "N/D"} mAh")
                    Text("Bateria atual: ${device.batteryLevelPct}%")
                    Text("Tempo de tela: ${device.screenOnMinutes} min")
                    Text("Consumo: ${device.energyConsumed ?: "N/D"} mAh")
                    Text("Jank: ${device.jankFramesPct}%")
                    Text("Falhas em 7 dias: ${device.crashCount7d}")
                    Text("Última atividade: ${device.lastSeen ?: "N/D"}")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val novos = favoritos.value.toMutableSet()
                        if (favorito) {
                            novos.remove(device.id)
                        } else {
                            novos.add(device.id)
                        }

                        favoritos.value = novos.toSet()
                        preferences.edit()
                            .putStringSet("favoritos", novos.toSet())
                            .apply()
                    }
                ) {
                    Text(if (favorito) "Remover favorito" else "Adicionar favorito")
                }
            },
            dismissButton = {
                TextButton(onClick = { selecionado = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun ResumoCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(titulo, style = MaterialTheme.typography.bodySmall)
            Text(valor, style = MaterialTheme.typography.titleLarge)
        }
    }
}
private fun statusLabel(status: DeviceStatus): String {
    return when (status) {
        DeviceStatus.OK -> "OK"
        DeviceStatus.ATENCAO -> "ATENÇÃO"
        DeviceStatus.CRITICO -> "CRÍTICO"
        DeviceStatus.INDETERMINADO -> "INDETERMINADO"
    }
}
private fun classificationReason(device: Device, status: DeviceStatus): String {
    val capacity = device.batteryCapacityMah?.toDouble()
    val energy = device.energyConsumed

    val relativeDrain = if (capacity != null && capacity > 0.0 &&
        device.screenOnMinutes > 0 && energy != null &&
        energy.isFinite() && energy >= 0.0){
        val screenHours = device.screenOnMinutes / 60.0
        (energy / screenHours / capacity) * 100.0
    } else {
        null
    }

    return when (status) {
        DeviceStatus.INDETERMINADO ->
            "Não foi possível calcular o consumo relativo. " +
                    "Verifique a capacidade da bateria, o consumo " +
                    "e o tempo de tela."

        DeviceStatus.CRITICO -> when {
            relativeDrain != null && relativeDrain >= 12.0 ->
                "Consumo relativo de bateria de " +
                        "%.1f%% por hora (limite crítico: 12%%/h)."
                            .format(relativeDrain)

            device.crashCount7d >= 3 ->
                "Pelo menos 3 falhas registradas nos últimos 7 dias."

            device.jankFramesPct >= 15.0 ->
                "Taxa de quadros perdidos (jank) igual ou superior a 15%."

            else ->
                "Um ou mais critérios de criticidade foram atingidos."
        }

        DeviceStatus.ATENCAO -> when {
            relativeDrain != null && relativeDrain >= 7.0 ->
                "Consumo relativo de bateria de " +
                        "%.1f%% por hora (limite de atenção: 7%%/h)."
                            .format(relativeDrain)

            device.crashCount7d >= 1 ->
                "Foi registrada pelo menos 1 falha nos últimos 7 dias."

            device.jankFramesPct >= 8.0 ->
                "Taxa de quadros perdidos (jank) igual ou superior a 8%."

            else ->
                "Um critério de atenção foi atingido."
        }

        DeviceStatus.OK ->
            "Nenhum dos limites de consumo, falhas ou jank foi atingido."
    }
}