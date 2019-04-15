package com.freeletics.migrator

import java.lang.IllegalStateException
import java.lang.RuntimeException

sealed class MigrationCompleted {
    object Never : MigrationCompleted()
    data class Completed(val completedAtCounter: Int) : MigrationCompleted()
}

class MigrationWithCounter(
    private val counter: Counter,
    override val version: Int
) : Migration {

    var migrationCompleted: MigrationCompleted = MigrationCompleted.Never

    override fun migrate() {
        migrationCompleted = MigrationCompleted.Completed(counter.next())
    }
}

class SimpleMigration(override val version: Int) : Migration{
    override fun migrate() {
        throw RuntimeException("Method should never be invoked")
    }
}