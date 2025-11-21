package com.vauthenticator.server.document.adapter

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.nio.file.Paths

const val documentBucket: String = "document-bucket"

object DocumentUtils {

    fun s3Client(): S3Client {
        return S3Client.builder()
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("ACCESS_KEY_ID", "SECRET_ACCESS_KEY")
                )
            ).region(Region.US_EAST_1)
            .endpointOverride(URI.create("http://localhost:4566"))
            .forcePathStyle(true)
            .build()
    }

    fun initDocumentTests(client: S3Client) {
        try {
            client.createBucket(
                CreateBucketRequest.builder()
                    .bucket(documentBucket)
                    .build()
            )
        } catch (e: Exception) {
        }

        val resource = DocumentUtils::class.java.getResource("/index.html")
        client.putObject(
            PutObjectRequest.builder()
                .bucket(documentBucket)
                .key("mail/templates/index.html")
                .build(),
            RequestBody.fromFile(Paths.get(resource!!.path))
        )
    }
}
