package com.vauthenticator.server.mfa.api

import com.vauthenticator.server.extentions.clientAppId
import com.vauthenticator.server.mfa.domain.*
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
class MfaEnrolmentAssociationEndPoint(
    private val mfaDevices: MfaDevices,
    private val mfaMethodsEnrollment: MfaMethodsEnrollment,
    private val mfaMethodsEnrolmentAssociation: MfaMethodsEnrollmentAssociation,
    private val permissionValidator: PermissionValidator
) {


    @GetMapping("/api/mfa/enrollment")
    fun findAllAssociatedEnrolledMfaMethods(
        authentication: Authentication,
        httpSession: HttpSession
    ): ResponseEntity<List<MfaDeviceRepresentation>> {
        permissionValidator.validate(authentication, httpSession, Scopes.from(Scope.MFA_ENROLLMENT))

        return ok(mfaMethodsEnrollment.getEnrollmentsFor(authentication.name, true)
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
    }

    @PutMapping("/api/mfa/device")
    fun setDefaultEnrolledMfaMethods(
        authentication: Authentication?,
        @RequestBody defaultMethod: SetDefaultMfaDeviceRequest
    ): ResponseEntity<Unit> {
        val principal = permissionValidator.validate(authentication, Scopes.from(Scope.MFA_ENROLLMENT))
        mfaDevices.setAsDefault(
            principal.name,
            MfaDeviceId(defaultMethod.mfaDeviceId)
        )
        return noContent().build()
    }


    @DeleteMapping("/api/mfa/enrollment/{mfaDeviceId}")
    fun revokeMfaDevice(
        authentication: Authentication?,
        @PathVariable mfaDeviceId: String
    ): ResponseEntity<Unit> {
        val principal = permissionValidator.validate(authentication, Scopes.from(Scope.MFA_ENROLLMENT))
        mfaDevices.revoke(principal.name, MfaDeviceId(mfaDeviceId), principal.clientAppId())
        return noContent().build()
    }

    @PostMapping("/api/mfa/enrollment")
    fun enrollMfa(
        authentication: Authentication?,
        @RequestBody enrolling: MfaEnrollmentRequest
    ): ResponseEntity<MfaEnrollmentResponse> {
        val principal = permissionValidator.validate(authentication, Scopes.from(Scope.MFA_ENROLLMENT))
        val ticketId = mfaMethodsEnrollment.enroll(
            principal.name,
            enrolling.mfaMethod,
            enrolling.mfaChannel,
            principal.clientAppId(),
            true
        )
        return status(HttpStatus.CREATED).body(MfaEnrollmentResponse(ticketId.content))
    }

    @PostMapping("/api/mfa/associate")
    fun associateMfaEnrollment(
        authentication: Authentication?,
        @RequestBody associationRequest: MfaEnrollmentAssociationRequest,
    ): ResponseEntity<Unit> {
        val principal = permissionValidator.validate(authentication, Scopes.from(Scope.MFA_ENROLLMENT))
        mfaMethodsEnrolmentAssociation.associate(principal.name, associationRequest.ticket, associationRequest.code)
        return noContent().build()
    }

    @ExceptionHandler(MfaDeviceNotFoundException::class)
    fun mfaDeviceNotFoundExceptionHandler() = notFound().build<Unit>()

    @ExceptionHandler(MfaDeviceRevocationException::class)
    fun mfaDeviceRevocationExceptionHandler(e: MfaDeviceRevocationException) =
        status(HttpStatus.CONFLICT).body(MfaDeviceRevocationRefusalResponse(e.refusal))

    @ExceptionHandler(MfaAssociationFailureException::class)
    fun mfaAssociationFailureExceptionHandler(e: MfaAssociationFailureException) =
        status(
            when (e.reason) {
                MfaAssociationFailureReason.WRONG_CODE -> HttpStatus.UNPROCESSABLE_CONTENT
                MfaAssociationFailureReason.INVALID_TICKET -> HttpStatus.GONE
                MfaAssociationFailureReason.ALREADY_ASSOCIATED -> HttpStatus.CONFLICT
            }
        ).body(MfaAssociationFailureResponse(e.reason))

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

data class MfaDeviceRevocationRefusalResponse(
    val reason: MfaDeviceRevocationRefusal
)

data class MfaAssociationFailureResponse(
    val reason: MfaAssociationFailureReason
)

data class MfaDeviceRepresentation(
    val userName: String,
    val mfaMethod: MfaMethod,
    val mfaChannel: String,
    val mfaDeviceId: String,
    val default: Boolean
)