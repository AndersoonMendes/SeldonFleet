package com.anderson.seldonfleet.data.repository

import com.anderson.seldonfleet.data.mapper.DeviceMapper
import com.anderson.seldonfleet.data.source.AssetFleetSource
import com.anderson.seldonfleet.domain.model.Device
import com.anderson.seldonfleet.domain.repository.DeviceRepository

class FleetRepositoryImpl(
    private val source: AssetFleetSource,
    private val mapper: DeviceMapper
) : DeviceRepository {

    override fun getDevices(): List<Device> {
        val deviceDtos = source.loadDevices()

        return deviceDtos.map { dto ->
            mapper.toDomain(dto)
        }
    }
}
