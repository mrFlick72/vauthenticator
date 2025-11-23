package com.vauthenticator.management

import java.nio.file.Files
import java.nio.file.Paths

object FileUtils {

    fun loadFileFor(path: String) = String(ClassLoader.getSystemResourceAsStream(path).readAllBytes())
    fun loadAllLinesFileFor(path: String) = Files.readAllLines(Paths.get("src/test/resources", path))
}