package com.anderson.seldonfleet.data.mapper

import com.anderson.seldonfleet.data.dto.DeviceDto
import com.anderson.seldonfleet.domain.model.Device

class DeviceMapper {

    fun toDomain(dto: DeviceDto): Device {
        val energyInMah = when (dto.energyUnit) {
            "mAh" -> dto.energyConsumed
            "mWh" -> {
                val voltage = dto.nominalVoltage

                if (voltage != null && voltage > 0.0) {
                    dto.energyConsumed?.div(voltage)
                } else {
                    null
                }
            }
            else -> null
        }

        return Device(
            id = dto.id.orEmpty(),
            model = dto.model.orEmpty(),
            soc = dto.soc.orEmpty(),
            tier = dto.tier.orEmpty(),
            ramMb = dto.ramMb ?: 0,
            androidApi = dto.androidApi ?: 0,
            batteryCapacityMah = dto.batteryCapacityMah,
            batteryLevelPct = dto.batteryLevelPct ?: 0,
            screenOnMinutes = dto.screenOnMinutes ?: 0,
            energyConsumed = energyInMah,
            energyUnit = "mAh",
            nominalVoltage = null,
            jankFramesPct = dto.jankFramesPct ?: 0.0,
            crashCount7d = dto.crashCount7d ?: 0,
            lastSeen = dto.lastSeen
        )
    }
}
