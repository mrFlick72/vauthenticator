package com.vauthenticator.server.cache

interface CacheOperation<K, O> {
    fun get(key: K): O?
    fun put(key: K, value: O)
    fun evict(key: K)
}
