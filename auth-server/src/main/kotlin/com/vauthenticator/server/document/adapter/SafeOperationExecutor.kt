package com.vauthenticator.server.document.adapter

import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path


fun readAllBytesFrom(filePath: Path): ByteArray {
    return try {
        Files.readAllBytes(filePath)
    } catch (e: IOException) {
        ByteArray(0)
    }
}

fun readAllBytesFrom(response: ResponseInputStream<GetObjectResponse?>): ByteArray {
    return try {
        response.readAllBytes()
    } catch (e: IOException) {
        ByteArray(0)
    }
}

fun contentTypeFor(filePath: Path): String {
    val contentType: String
    try {
        contentType = Files.probeContentType(filePath)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }
    return contentType
}