package com.freeletics.migrator

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert

import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple Test for  [SharedPrefsMigratorVersionStore]
 */
@RunWith(AndroidJUnit4::class)
class SharedPrefsMigratorVersionStoreTest {

    @Test
    fun readAndWrite() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        val versionStore = SharedPrefsMigratorVersionStore(appContext)

        // Clear all previous
        versionStore.sharedPrefs.edit().clear().apply()

        // If no migration run yet, it should return null
        Assert.assertEquals(null, versionStore.getMigratedUntilVersion())

        // Save a value
        val versionValue = 10
        versionStore.setMigratedUntilVersion(versionValue)

        // Read value
        Assert.assertEquals(versionValue, versionStore.getMigratedUntilVersion())

        // Update a value
        val newVersionValue = 20
        versionStore.setMigratedUntilVersion(newVersionValue)

        // Read value
        Assert.assertEquals(newVersionValue, versionStore.getMigratedUntilVersion())
    }

    @Test
    fun setNegativeOrZeroAsVersionThrowException() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val versionStore = SharedPrefsMigratorVersionStore(appContext)

        try {
            versionStore.setMigratedUntilVersion(0)
            Assert.fail("IllegalArgumentException expected")
        } catch (t: IllegalArgumentException) {
            Assert.assertEquals("Version must be >= 1", t.message)
        }

        try {
            versionStore.setMigratedUntilVersion(-1)
            Assert.fail("IllegalArgumentException expected")
        } catch (t: IllegalArgumentException) {
            Assert.assertEquals("Version must be >= 1", t.message)
        }
    }
}
