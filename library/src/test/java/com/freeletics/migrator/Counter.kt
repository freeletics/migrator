package com.freeletics.migrator

import java.util.concurrent.atomic.AtomicInteger

class Counter {

    private var value = AtomicInteger()

    fun next() : Int = value.incrementAndGet()
}