package me.cpele.blackbox

import android.app.Activity
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiSelector
import android.support.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class DisplayDatapointsTest {

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(Activity::class.java, false, false)

    @Test
    fun should_display_datapoints() {

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()

        device.wait(Until.hasObject(By.pkg(device.launcherPackageName).depth(0)), 5000)

        val appContext = InstrumentationRegistry.getTargetContext()
        val intent = appContext.packageManager.getLaunchIntentForPackage("me.cpele.watchbee")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(intent)

        device.findObject(UiSelector().resourceId("me.cpele.watchbee:id/main_menu_sync")).click()
        device.findObject(UiSelector().resourceId("me.cpele.watchbee:id/item_title")).click()

        Thread.sleep(TimeUnit.HOURS.toMillis(1))
    }
}
