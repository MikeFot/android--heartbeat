package com.michaelfotiadis.heartbeat.core.model

import java.util.*

class Optional<T> {

    private var value: T? = null

    private constructor() {
        this.value = null
    }

    private constructor(value: T) {
        this.value = Objects.requireNonNull(value)
    }

    interface Action<T> {
        fun apply(value: T)
    }

    fun get(): T {
        return value!!
    }

    fun isPresent(): Boolean {
        return value != null
    }

    fun ifPresent(action: Action<T>) {
        value?.run {
            action.apply(this)
        }
    }

    companion object {

        fun <T> empty(): Optional<T> {
            return Optional()
        }

        fun <T> of(value: T): Optional<T> {
            return Optional(value)
        }
    }

}