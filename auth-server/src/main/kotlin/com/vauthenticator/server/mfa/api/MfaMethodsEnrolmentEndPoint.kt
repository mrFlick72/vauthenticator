package com.vauthenticator.server.mfa.api

import com.vauthenticator.server.extentions.clientAppId
import com.vauthenticator.server.mfa.domain.*
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
class MfaEnrolmentAssociationEndPoint(
    private val mfaAccountMethodsRepository: MfaAccountMethodsRepository,
    private val mfaMethodsEnrollment: MfaMethodsEnrollment,
    private val mfaMethodsEnrolmentAssociation: MfaMethodsEnrollmentAssociation,
    private val permissionValidator: PermissionValidator
) {


    @GetMapping("/api/mfa/enrollment")
    fun findAllAssociatedEnrolledMfaMethods(authentication: Authentication) =
        ok(mfaMethodsEnrollment.getEnrollmentsFor(authentication.name, true)
            .map {
                MfaDeviceRepresentation(
                    it.userName,
                    it.mfaMethod,
                    it.mfaChannel,
                    it.mfaDeviceId.content,
                    it.default
                )
            }

        )

    @PutMapping("/api/mfa/device")
    fun setDefaultEnrolledMfaMethods(
        authentication: JwtAuthenticationToken,
        @RequestBody defaultMethod: SetDefaultMfaDeviceRequest
    ): ResponseEntity<Unit> {
        permissionValidator.validate(authentication, Scopes.from(Scope.MFA_ENROLLMENT))
        mfaAccountMethodsRepository.setAsDefault(
            authentication.name,
            MfaDeviceId(defaultMethod.mfaDeviceId)
        )
        return noContent().build()
    }


    @PostMapping("/api/mfa/enrollment")
    fun enrollMfa(
        authentication: JwtAuthenticationToken,
        @RequestBody enrolling: MfaEnrollmentRequest
    ): ResponseEntity<MfaEnrollmentResponse> {
        permissionValidator.validate(authentication, Scopes.from(Scope.MFA_ENROLLMENT))
        val ticketId = mfaMethodsEnrollment.enroll(
            authentication.name,
            enrolling.mfaMethod,
            enrolling.mfaChannel,
            authentication.clientAppId(),
            true
        )
        return status(HttpStatus.CREATED).body(MfaEnrollmentResponse(ticketId.content))
    }

    @PostMapping("/api/mfa/associate")
    fun associateMfaEnrollment(
        authentication: JwtAuthenticationToken,
        @RequestBody associationRequest: MfaEnrollmentAssociationRequest,
    ): ResponseEntity<Unit> {
        permissionValidator.validate(authentication, Scopes.from(Scope.MFA_ENROLLMENT))
        mfaMethodsEnrolmentAssociation.associate(associationRequest.ticket, associationRequest.code)
        return noContent().build()
    }

}

data class SetDefaultMfaDeviceRequest(
    val mfaDeviceId: String
)

data class MfaEnrollmentRequest(
    val mfaChannel: String,
    val mfaMethod: MfaMethod,
)

data class MfaEnrollmentResponse(
    val ticket: String
)

data class MfaEnrollmentAssociationRequest(
    val ticket: String,
    val code: String,
)

data class MfaDeviceRepresentation(
    val userName: String,
    val mfaMethod: MfaMethod,
    val mfaChannel: String,
    val mfaDeviceId: String,
    val default: Boolean
)