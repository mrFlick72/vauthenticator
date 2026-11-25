package com.vauthenticator.platform.cache


interface CacheContentConverter<O> {
    fun getObjectFromCacheContentFor(cacheContent: String): O
    fun loadableContentIntoCacheFor(source: O): String
}