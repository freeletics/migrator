package com.freeletics.migrator

/**
 * Responsible to store the current version
 */
interface MigratorVersionStore {
    /**
     * Migration runMigrationsIfNeeded until that version
     *
     * @return null if [setMigratedUntilVersion] has never  been called before (so no migration did ever runMigrationsIfNeeded before) or
     * the last version of the last migration that ran.
     */
    fun getMigratedUntilVersion(): Int?

    /**
     * set the version until migration ran
     */
    fun setMigratedUntilVersion(version: Int)
}