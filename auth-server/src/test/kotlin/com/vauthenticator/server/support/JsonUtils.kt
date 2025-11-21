package com.vauthenticator.server.support

object JsonUtils {

    fun prettifyInOneLineJsonFrom(absoluteFilePath: String) =
        FileUtils.loadAllLinesFileFor(absoluteFilePath)
            .joinToString("") {
                it.replace(": ", ":")
                    .replace(", ", ",")
                    .trim()
            }
}