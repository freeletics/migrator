package com.freeletics.migrator

interface Migration {
    val version: Int

    fun migrate()
}