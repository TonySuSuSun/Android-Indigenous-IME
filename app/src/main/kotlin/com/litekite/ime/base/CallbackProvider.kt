package com.litekite.ime.base

@Suppress("UNUSED")
interface CallbackProvider<T> {

    val callbacks: ArrayList<T>

    fun addCallback(cb: T) {
        if (!callbacks.contains(cb)) {
            callbacks.add(cb)
        }
    }

    fun removeCallback(cb: T) {
        callbacks.remove(cb)
    }
}
