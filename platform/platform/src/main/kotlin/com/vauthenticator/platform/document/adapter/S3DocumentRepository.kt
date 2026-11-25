package com.vauthenticator.platform.document.adapter

import com.vauthenticator.platform.document.domain.Document
import com.vauthenticator.platform.document.domain.DocumentRepository
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.IOException


class S3DocumentRepository(private val s3Client: S3Client, private val buketName: String) :
    DocumentRepository {

    override fun loadDocument(type: String, path: String): Document {
        val request = GetObjectRequest.builder().bucket(buketName).key(documentKeyFor(type, path)).build()
        val response = s3Client.getObject(request)
        return Document(
            response.response().contentType(),
            path,
            readAllBytesFrom(response)
        )
    }


    override fun saveDocument(type: String, document: Document) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(buketName)
                .key(documentKeyFor(type, document.path))
                .contentType(document.contentType)
                .build(),
            RequestBody.fromBytes(document.content)
        )
    }
}

private fun readAllBytesFrom(response: ResponseInputStream<GetObjectResponse?>): ByteArray {
    return try {
        response.readAllBytes()
    } catch (e: IOException) {
        ByteArray(0)
    }
}
