package me.cpele.blackbox

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.Until
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class DisplayDatapointsTest {

    companion object {
        const val TIMEOUT = 10_000L;
    }

    @Test
    fun should_display_datapoints() {

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        device.wait(Until.findObject(By.res("me.cpele.watchbee:id/main_menu_sync")), TIMEOUT).click()
        device.wait(Until.findObject(By.res("me.cpele.watchbee:id/item_title")), TIMEOUT).click()
    }
}
