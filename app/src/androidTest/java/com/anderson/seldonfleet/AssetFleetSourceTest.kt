package com.anderson.seldonfleet

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.anderson.seldonfleet.data.source.AssetFleetSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssetFleetSourceTest {

    @Test
    fun carregaOs30DispositivosDaFrota() {
        val context =
            InstrumentationRegistry.getInstrumentation().targetContext

        val source = AssetFleetSource(context)
        val devices = source.loadDevices()

        assertEquals(30, devices.size)

        assertTrue(
            devices.any { it.id == "DEV-0137" }
        )

        assertTrue(
            devices.any { it.id == "DEV-0204" }
        )
    }
}
