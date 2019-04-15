package com.freeletics.migrator

import android.content.Context

/**
 * A little tool that runs
 */
class Migrator(
    /**
     * A list of all migrations.
     */
    private val migrations: List<Migration>,

    /**
     * The store where the lates version has been stored
     */
    private val migratorVersionStore: MigratorVersionStore
) {

    /**
     * Convinience constructor that uses [SharedPrefsMigratorVersionStore] under the hood.
     */
    constructor(
        /**
         * The context used to open  the shared preferences
         */
        context: Context,
        /**
         * All migrations that should runMigrationsIfNeeded
         */
        vararg migrations: Migration
    ) : this(
        migrations = migrations.toList(),
        migratorVersionStore = SharedPrefsMigratorVersionStore(context)
    )

    init {
        if (migrations.isEmpty()) {
            throw IllegalArgumentException("List of Migrations is empty")
        }
    }

    /**
     * This might be a long running and blocking call.
     * You better run it on a background thread if you don't want to block any thread.
     *
     * It may throw [IllegalArgumentException] or [IllegalStateException] if the input arguments (passed through the
     * constructor parameter) doesn't match our requirements.
     */
    fun runMigrationsIfNeeded() {

        // TODO sorting runs now all the time.
        // Should we prefer performance over convinient API (now no "current" version is not specified in contrast to
        // SQLite database migrations where you have to specify a "current" version as a developer )
        val sorted = migrations.sortedBy { it.version }

        val lastRunMigrationVersion = migratorVersionStore.getMigratedUntilVersion()
        val migrationsToRun = if (lastRunMigrationVersion == null) {
            sorted
        } else {
            val binarySearchIndex = sorted.binarySearch {
                if (it.version == lastRunMigrationVersion)
                    0
                else
                    it.version - lastRunMigrationVersion
            }

            if (binarySearchIndex < 0) {
                throw IllegalStateException(
                    "Last time we migrated to version = $lastRunMigrationVersion but couldn't find " +
                        "a migration with that version in the migration list"
                )
            }
            sorted.subList(fromIndex = binarySearchIndex + 1, toIndex = sorted.size)
        }

        if (migrationsToRun.isNotEmpty()) {
            // check if migration.version is unique
            var previousMigrationValue: Int? = null
            for (m in migrations) {
                if (m.version <= 0) {
                    throw IllegalArgumentException(
                        "Migration version must be > 1 " +
                            "but found a migration with version = ${m.version}"
                    )
                }
                val previousVersion = previousMigrationValue
                if (previousVersion != null && previousVersion == m.version) {
                    throw IllegalArgumentException(
                        "At least two migrations with version = $previousVersion found. " +
                            "Version must be unique and positive integer."
                    )
                }
                previousMigrationValue = m.version
            }

            // Run the migrations
            migrationsToRun.forEach {
                it.migrate()
                migratorVersionStore.setMigratedUntilVersion(it.version)
            }
        }
    }
}