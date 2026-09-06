package com.vauthenticator.server.keys.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.time.Duration

@RestController
class KeyEndPoint(
    @Value("\${key.master-key.id}") private val masterKey: String,
    private val keyRepository: KeyRepository,
    private val signatureKeyRotation: SignatureKeyRotation,
    private val permissionValidator: PermissionValidator
) {

    @GetMapping("/api/keys")
    fun loadAllKeys(principal: JwtAuthenticationToken): ResponseEntity<List<Map<String, Any>>> {
        permissionValidator.validate(principal, Scopes.from(Scope.KEY_READER))

        return keyRepository.signatureKeys()
            .keys.map { mapOf("masterKey" to it.masterKid.content(), "kid" to it.kid.content(), "ttl" to it.expirationDateTimestamp) }
            .let { ResponseEntity.ok(it) }
    }


    @PostMapping("/api/keys")
    fun createKey() =
        keyRepository.createKeyFrom(MasterKid(masterKey))
            .let { ResponseEntity.status(HttpStatus.CREATED).build<Unit>() }

    @PostMapping("/api/keys/rotate")
    fun rotateKey(@RequestBody body: RotateKeyRequest) =
        signatureKeyRotation.rotate(MasterKid(masterKey), Kid(body.kid), Duration.ofSeconds(body.keyTtl))
            .let { ResponseEntity.status(HttpStatus.NO_CONTENT).build<Unit>() }

    @DeleteMapping("/api/keys")
    fun deleteKey(@RequestBody body: DeleteKeyRequest) =
        keyRepository.deleteKeyFor(
            Kid(body.kid),
            body.keyPurpose,
            Duration.ofSeconds(body.keyTtl)
        )
            .let { ResponseEntity.noContent().build<Unit>() }

    @ExceptionHandler(KeyDeletionException::class)
    fun keyDeletionExceptionHandler(ex: KeyDeletionException) = ResponseEntity.badRequest().body(ex.message);
}

data class DeleteKeyRequest(
    val kid: String,
    @JsonProperty("key_purpose") val keyPurpose: KeyPurpose,
    @JsonProperty("key_ttl") val keyTtl: Long
)

data class RotateKeyRequest(
    val kid: String,
    @JsonProperty("key_ttl") val keyTtl: Long
)