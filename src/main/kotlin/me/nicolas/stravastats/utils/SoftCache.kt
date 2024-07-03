package me.nicolas.stravastats.utils

import java.lang.ref.ReferenceQueue

import java.lang.ref.SoftReference

/**
 * [SoftCache] caches items with a [SoftReference] wrapper.
 * A soft reference is a reference that is garbage-collected less aggressively.
 */
@Suppress("UNCHECKED_CAST")
class SoftCache<K, V : Any> : GenericCache<K, V> {

    private val cache = HashMap<K, SoftEntry<K, V>>()

    override val size: Int
        get() = cache.size

    override fun clear() = cache.clear()

    private val referenceQueue = ReferenceQueue<Any>()

    private class SoftEntry<K, V>(val key: K, value: V, referenceQueue: ReferenceQueue<Any>)
        : SoftReference<Any>(value, referenceQueue)

    override fun set(key: K, value: V) {
        removeUnreachableItems()
        val softEntry = SoftEntry(key, value, referenceQueue)
        cache[key] = softEntry
    }

    override fun remove(key: K): V? {
        val softEntry = cache.remove(key)
        removeUnreachableItems()

        return softEntry?.get()?.let { return it as V}
    }

    override fun get(key: K): V? {
        val softEntry = cache[key] as SoftEntry<*, *>?
        softEntry?.get()?.let { return it as V }
        cache.remove(key)
        return null
    }

    private fun removeUnreachableItems() {
        var softEntry = referenceQueue.poll() as SoftEntry<*, *>?
        while (softEntry != null) {
            val key = softEntry.key
            cache.remove(key)
            softEntry = referenceQueue.poll() as SoftEntry<*, *>?
        }
    }
}