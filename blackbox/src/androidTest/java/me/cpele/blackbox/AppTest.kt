package me.cpele.blackbox

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.Until
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppTest {

    companion object {
        const val TIMEOUT = 10_000L;
        val START_TIMEOUT = TimeUnit.HOURS.toMillis(1);
    }

    @Test
    fun gh4() {

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        device.wait(Until.findObject(By.res("me.cpele.fleabrainer:id/item_title")), START_TIMEOUT)
                .click()
    }
}
