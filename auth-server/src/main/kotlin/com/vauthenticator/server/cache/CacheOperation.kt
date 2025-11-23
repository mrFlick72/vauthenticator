package com.vauthenticator.server.cache

import java.util.*

interface CacheOperation<K, O> {
    fun get(key: K): Optional<O>
    fun put(key: K, value: O)
    fun evict(key: K)
}