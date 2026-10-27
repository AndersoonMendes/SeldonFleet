
package com.anderson.seldonfleet.domain.usecase

import com.anderson.seldonfleet.domain.model.Device
import com.anderson.seldonfleet.domain.model.DeviceStatus

class ClassifyDeviceUseCase {

    fun execute(device: Device): DeviceStatus {
        val capacity = device.batteryCapacityMah?.toDouble()
        val energyMah = device.energyConsumed

        if (
            capacity == null ||
            capacity <= 0.0 ||
            device.screenOnMinutes <= 0 ||
            energyMah == null ||
            !energyMah.isFinite() ||
            energyMah < 0.0
        ) {
            return DeviceStatus.INDETERMINADO
        }

        val screenHours = device.screenOnMinutes / 60.0
        val absoluteDrain = energyMah / screenHours
        val relativeDrain = (absoluteDrain / capacity) * 100.0

        if (
            relativeDrain >= 12.0 ||
            device.crashCount7d >= 3 ||
            device.jankFramesPct >= 15.0
        ) {
            return DeviceStatus.CRITICO
        }

        if (
            relativeDrain >= 7.0 ||
            device.crashCount7d >= 1 ||
            device.jankFramesPct >= 8.0
        ) {
            return DeviceStatus.ATENCAO
        }

        return DeviceStatus.OK
    }
}
