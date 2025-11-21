package com.vauthenticator.server.support

import java.nio.file.Files
import java.nio.file.Paths

object FileUtils {

    fun loadAllLinesFileFor(path: String) = Files.readAllLines(Paths.get("src/test/resources", path))
}