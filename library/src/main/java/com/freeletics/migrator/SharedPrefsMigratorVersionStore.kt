package com.freeletics.migrator

import android.content.Context
import android.content.SharedPreferences

/**
 * A [MigratorVersionStore] implementation that uses [SharedPreferences] to store value.
 */
class SharedPrefsMigratorVersionStore(context: Context) : MigratorVersionStore {

    // internal visibility for testing
    internal val sharedPrefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("MIGRATION_VERSION_STORE", Context.MODE_PRIVATE)

    private val KEY_LAST_VERSION = "lastVersionSuccessfullyMigrated"

    override fun getMigratedUntilVersion(): Int? {
        val version = sharedPrefs.getInt(KEY_LAST_VERSION, -1)
        return if (version == -1)
            null
        else version
    }

    override fun setMigratedUntilVersion(version: Int) {
        if (version <= 0) {
            throw IllegalArgumentException("Version must be >= 1")
        }
        sharedPrefs.edit().putInt(KEY_LAST_VERSION, version).apply()
    }
}