package com.anderson.seldonfleet.domain.repository

import com.anderson.seldonfleet.domain.model.Device

interface DeviceRepository {
    fun getDevices(): List<Device>
}