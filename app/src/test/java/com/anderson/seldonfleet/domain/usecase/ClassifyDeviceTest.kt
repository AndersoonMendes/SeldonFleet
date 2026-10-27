
package com.anderson.seldonfleet.domain.usecase

import com.anderson.seldonfleet.data.dto.DeviceDto
import com.anderson.seldonfleet.data.mapper.DeviceMapper
import com.anderson.seldonfleet.domain.model.Device
import com.anderson.seldonfleet.domain.model.DeviceStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ClassifyDeviceTest {

    private val classificador = ClassifyDeviceUseCase()

    private fun dispositivo(
        capacidade: Int? = 1000,
        minutos: Int = 60,
        consumo: Double? = 10.0,
        falhas: Int = 0,
        jank: Double = 0.0
    ): Device {
        return Device(
            id = "teste-001",
            model = "Modelo de teste",
            soc = "Teste SoC",
            tier = "mid",
            ramMb = 4096,
            androidApi = 35,
            batteryCapacityMah = capacidade,
            batteryLevelPct = 80,
            screenOnMinutes = minutos,
            energyConsumed = consumo,
            energyUnit = "mAh",
            nominalVoltage = null,
            jankFramesPct = jank,
            crashCount7d = falhas,
            lastSeen = "2026-10-25T10:00:00Z"
        )
    }

    @Test
    fun dispositivoSemProblemasDeveSerOk() {
        assertEquals(
            DeviceStatus.OK,
            classificador.execute(dispositivo())
        )
    }

    @Test
    fun capacidadeAusenteDeveSerIndeterminado() {
        assertEquals(
            DeviceStatus.INDETERMINADO,
            classificador.execute(dispositivo(capacidade = null))
        )
    }

    @Test
    fun capacidadeZeroDeveSerIndeterminado() {
        assertEquals(
            DeviceStatus.INDETERMINADO,
            classificador.execute(dispositivo(capacidade = 0))
        )
    }

    @Test
    fun tempoDeTelaZeroDeveSerIndeterminado() {
        assertEquals(
            DeviceStatus.INDETERMINADO,
            classificador.execute(dispositivo(minutos = 0))
        )
    }

    @Test
    fun consumoAltoDeveSerCritico() {
        assertEquals(
            DeviceStatus.CRITICO,
            classificador.execute(dispositivo(consumo = 200.0))
        )
    }

    @Test
    fun tresFalhasDevemSerCritico() {
        assertEquals(
            DeviceStatus.CRITICO,
            classificador.execute(dispositivo(falhas = 3))
        )
    }

    @Test
    fun jankAltoDeveSerCritico() {
        assertEquals(
            DeviceStatus.CRITICO,
            classificador.execute(dispositivo(jank = 15.0))
        )
    }

    @Test
    fun umaFalhaDeveGerarAtencao() {
        assertEquals(
            DeviceStatus.ATENCAO,
            classificador.execute(dispositivo(falhas = 1))
        )
    }

    @Test
    fun jankModeradoDeveGerarAtencao() {
        assertEquals(
            DeviceStatus.ATENCAO,
            classificador.execute(dispositivo(jank = 8.0))
        )
    }

    @Test
    fun consumoEquivalenteEmMahEMwhDeveDarMesmoStatus() {
        val mapper = DeviceMapper()

        val dtoMah = criarDto(
            consumo = 100.0,
            unidade = "mAh",
            tensao = null
        )

        val dtoMwh = criarDto(
            consumo = 300.0,
            unidade = "mWh",
            tensao = 3.0
        )

        val resultadoMah = classificador.execute(mapper.toDomain(dtoMah))
        val resultadoMwh = classificador.execute(mapper.toDomain(dtoMwh))

        assertEquals(resultadoMah, resultadoMwh)
        assertEquals(DeviceStatus.ATENCAO, resultadoMah)
    }

    private fun criarDto(
        consumo: Double,
        unidade: String,
        tensao: Double?
    ) = DeviceDto(
        id = "teste-001",
        model = "Modelo de teste",
        soc = "Teste SoC",
        tier = "mid",
        ramMb = 4096,
        androidApi = 35,
        batteryCapacityMah = 1000,
        batteryLevelPct = 80,
        screenOnMinutes = 60,
        energyConsumed = consumo,
        energyUnit = unidade,
        nominalVoltage = tensao,
        jankFramesPct = 0.0,
        crashCount7d = 0,
        lastSeen = null
    )
}
