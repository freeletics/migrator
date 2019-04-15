package com.freeletics.migrator

class InMemoryMigratorVersionStore(private var migrationUntilVersion: Int? = null) : MigratorVersionStore {

    override fun getMigratedUntilVersion(): Int? = synchronized(this) {
        migrationUntilVersion
    }

    override fun setMigratedUntilVersion(version: Int) {
        synchronized(this) {
            migrationUntilVersion = version
        }
    }
}