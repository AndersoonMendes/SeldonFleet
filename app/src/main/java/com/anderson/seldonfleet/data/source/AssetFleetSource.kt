package com.anderson.seldonfleet.data.source

import android.content.Context
import com.anderson.seldonfleet.data.dto.DeviceDto
import org.json.JSONObject

class AssetFleetSource(
    private val context: Context
) {

    fun loadDevices(): List<DeviceDto> {
        val jsonString = context.assets
            .open("frota.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(jsonString)
        val devicesJson = root.getJSONArray("devices")

        return List(devicesJson.length()) { index ->
            val item = devicesJson.getJSONObject(index)

            DeviceDto(
                id = item.readString("id"),
                model = item.readString("model"),
                soc = item.readString("soc"),
                tier = item.readString("tier"),
                ramMb = item.readInt("ramMb"),
                androidApi = item.readInt("androidApi"),
                batteryCapacityMah =
                    item.readInt("batteryCapacityMah"),
                batteryLevelPct = item.readInt("batteryLevelPct"),
                screenOnMinutes = item.readInt("screenOnMinutes"),
                energyConsumed = item.readDouble("energyConsumed"),
                energyUnit = item.readString("energyUnit"),
                nominalVoltage = item.readDouble("nominalVoltage"),
                jankFramesPct = item.readDouble("jankFramesPct"),
                crashCount7d = item.readInt("crashCount7d"),
                lastSeen = item.readString("lastSeen")
            )
        }
    }

    private fun JSONObject.readString(key: String): String? {
        if (!has(key) || isNull(key)) return null

        return optString(key, "")
            .takeIf { it.isNotBlank() }
    }

    private fun JSONObject.readInt(key: String): Int? {
        if (!has(key) || isNull(key)) return null

        return optInt(key, Int.MIN_VALUE)
            .takeIf { it != Int.MIN_VALUE }
    }

    private fun JSONObject.readDouble(key: String): Double? {
        if (!has(key) || isNull(key)) return null

        return optDouble(key, Double.NaN)
            .takeIf { it.isFinite() }
    }
}
