package com.anderson.seldonfleet.domain.usecase

import com.anderson.seldonfleet.domain.model.Device
import com.anderson.seldonfleet.domain.model.DeviceStatus


class ClassifyDeviceUseCase {
    fun execute(device: Device): DeviceStatus{

        //Verifica se temos os dados para calcular o consumo
        if (device.batteryCapacityMah == null ||
            device.batteryCapacityMah <= 0 ||
            device.screenOnMinutes <= 0 ||
            (device.energyUnit == "mWh" && device.nominalVoltage == null))
        {
            return DeviceStatus.INDETERMINADO
        }

        //Se possui dados suficientes, converte para mAh
        val energyMah = when(device.energyUnit){
            "mAh" -> device.energyConsumed
            "mWh" -> (device.energyConsumed / device.nominalVoltage!!)
            else -> return DeviceStatus.INDETERMINADO
        }

        //Calcular consumo médio por hora de tela ligada
        val hoursOn = (device.screenOnMinutes / 60.0)

        val absoluteDrain = (energyMah / hoursOn)

        val relativeDrain = ((absoluteDrain / device.batteryCapacityMah.toDouble()) * 100)

        //Condições críticas
        if(relativeDrain >= 12.0 || device.crashCount7d >= 3 ||
            device.jankFramesPct >= 15.0){
            return DeviceStatus.CRITICO
        }

        //Condição de atenção
        if(relativeDrain >= 7.0 || device.crashCount7d >= 1 ||
            device.jankFramesPct >= 8.0){
            return DeviceStatus.ATENCAO
        }

        //Tudo OK e nenhuma condição anterior foi atendida
        return DeviceStatus.OK
    }
}