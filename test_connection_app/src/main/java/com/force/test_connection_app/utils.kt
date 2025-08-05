package com.force.test_connection_app

import kotlin.reflect.KProperty
import kotlin.properties.ReadOnlyProperty

class Always<T>(private val block: () -> T) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = block()
}

fun <T> always(block: () -> T): ReadOnlyProperty<Any?, T> = Always(block)
