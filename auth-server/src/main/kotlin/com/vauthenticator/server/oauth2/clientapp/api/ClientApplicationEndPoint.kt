package com.vauthenticator.server.oauth2.clientapp.api

import com.vauthenticator.server.oauth2.clientapp.domain.*
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
class ClientApplicationEndPoint(
    private val clientApplicationRepository: ClientApplicationRepository,
    private var storeClientApplication: StoreClientApplication,
    private val readClientApplication: ReadClientApplication,
    private val permissionValidator: PermissionValidator
) {

    @PutMapping("/api/client-applications/{clientAppId}")
    fun storeClientApplication(
        principal: JwtAuthenticationToken,
        @PathVariable("clientAppId") clientAppId: String,
        @RequestBody clientAppRepresentation: ClientAppRepresentation
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.SAVE_CLIENT_APPLICATION))

        val aClientApp = ClientAppRepresentation.fromRepresentationToDomain(clientAppId, clientAppRepresentation)
        val storeWithPassword = clientAppRepresentation.storePassword
        storeClientApplication.store(aClientApp, storeWithPassword)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/api/client-applications/{clientAppId}/client-secret")
    fun resetPasswordForClientApplicationV2(
        principal: JwtAuthenticationToken,
        @PathVariable("clientAppId") clientAppId: String,
        @RequestBody body: ClientAppSecretRepresentation
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.SAVE_CLIENT_APPLICATION))

        storeClientApplication.resetPassword(ClientAppId(clientAppId), Secret(body.secret))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/api/client-applications")
    fun viewAllClientApplications(
        principal: JwtAuthenticationToken,
    ): ResponseEntity<List<ClientAppInListRepresentation>> {
        permissionValidator.validate(principal, Scopes.from(Scope.READ_CLIENT_APPLICATION))
        return readClientApplication.findAll()
            .map { ClientAppInListRepresentation.fromDomainToRepresentation(it) }
            .let {
                ResponseEntity.ok(it)
            }

    }

    @GetMapping("/api/client-applications/{clientAppId}")
    fun viewAClientApplication(
        principal: JwtAuthenticationToken,
        @PathVariable("clientAppId") clientAppId: String
    ): ResponseEntity<ClientAppRepresentation> {
        permissionValidator.validate(principal, Scopes.from(Scope.READ_CLIENT_APPLICATION))
        return readClientApplication.findOne(ClientAppId(clientAppId))
            .map { ClientAppRepresentation.fromDomainToRepresentation(it) }
            .map {
                ResponseEntity.ok(it)
            }.orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("/api/client-applications/{clientAppId}")
    fun deleteAClientApplication(
        principal: JwtAuthenticationToken,
        @PathVariable("clientAppId") clientAppId: String
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.DELETE_CLIENT_APPLICATION))

        clientApplicationRepository.delete(ClientAppId(clientAppId))
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(ClientApplicationNotFound::class)
    fun clientApplicationNotFoundHandler() = ResponseEntity.notFound().build<Unit>()

    @ExceptionHandler(UnsupportedClientAppOperationException::class)
    fun clientApplicationUnsupportedClientAppOperationExceptionHandler() = ResponseEntity.internalServerError().build<Unit>()

}

data class ClientAppSecretRepresentation(val secret: String)

data class ClientAppRepresentation(
    var clientAppName: String,
    var secret: String,
    var withPkce: Boolean,
    var confidential: Boolean,
    var storePassword: Boolean,
    var scopes: List<String>,
    var authorizedGrantTypes: List<String>,
    var webServerRedirectUri: String,
    var allowedOrigins: List<String>,
    var accessTokenValidity: Long,
    var refreshTokenValidity: Long,
    var postLogoutRedirectUri: String,
    var logoutUri: String
) {
    companion object {
        fun fromDomainToRepresentation(clientApplication: ClientApplication, storePassword: Boolean = false) =
            ClientAppRepresentation(
                clientAppName = clientApplication.clientAppName.content,
                secret = clientApplication.secret.content,
                storePassword = storePassword,
                confidential = clientApplication.confidential,
                withPkce = clientApplication.withPkce.content,
                scopes = clientApplication.scopes.content.map { it.content },
                authorizedGrantTypes = clientApplication.authorizedGrantTypes.content.map { it.name.lowercase() },
                webServerRedirectUri = clientApplication.webServerRedirectUri.content,
                allowedOrigins = clientApplication.allowedOrigins.content.map { it.content },
                accessTokenValidity = clientApplication.accessTokenValidity.content,
                refreshTokenValidity = clientApplication.refreshTokenValidity.content,
                postLogoutRedirectUri = clientApplication.postLogoutRedirectUri.content,
                logoutUri = clientApplication.logoutUri.content,
            )

        fun fromRepresentationToDomain(clientAppId: String, representation: ClientAppRepresentation) =
            ClientApplication(
                clientAppId = ClientAppId(clientAppId),
                clientAppName = ClientAppName(representation.clientAppName),
                confidential = representation.confidential,
                secret = Secret(representation.secret),
                withPkce = WithPkce(representation.withPkce),
                scopes = Scopes(representation.scopes.map { Scope(it) }.toSet()),
                authorizedGrantTypes = AuthorizedGrantTypes(representation.authorizedGrantTypes.map { it.uppercase() }
                    .map { AuthorizedGrantType.valueOf(it) }),
                webServerRedirectUri = CallbackUri(representation.webServerRedirectUri),
                allowedOrigins = AllowedOrigins(representation.allowedOrigins.map { AllowedOrigin(it) }.toSet()),
                accessTokenValidity = TokenTimeToLive(representation.accessTokenValidity),
                refreshTokenValidity = TokenTimeToLive(representation.refreshTokenValidity),
                postLogoutRedirectUri = PostLogoutRedirectUri(representation.postLogoutRedirectUri),
                logoutUri = LogoutUri(representation.logoutUri)
            )
    }
}


data class ClientAppInListRepresentation(
    var clientAppId: String,
    var clientAppName: String,
    var scopes: List<String>,
    var authorizedGrantTypes: List<String>
) {
    companion object {
        fun fromDomainToRepresentation(clientApplication: ClientApplication) =
            ClientAppInListRepresentation(
                clientAppName = clientApplication.clientAppName.content,
                clientAppId = clientApplication.clientAppId.content,
                scopes = clientApplication.scopes.content.map { it.content },
                authorizedGrantTypes = clientApplication.authorizedGrantTypes.content.map { it.name.lowercase() },
            )
    }
}