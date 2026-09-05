package com.vauthenticator.server.role.api

import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.role.domain.ProtectedRoleFromDeletionException
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
class RoleEndPoint(
    private val roleRepository: RoleRepository,
    private val permissionValidator: PermissionValidator
) {

    @GetMapping("/api/roles")
    fun findAllRole(principal: JwtAuthenticationToken) = run {
        permissionValidator.validate(principal, Scopes.from(Scope.READ_ROLE))
        ok().body(roleRepository.findAll())
    }

    @PutMapping("/api/roles")
    fun saveRole(principal: JwtAuthenticationToken, @RequestBody role: Role) = run {
        permissionValidator.validate(principal, Scopes.from(Scope.SAVE_ROLE))
        roleRepository.save(role)
        noContent().build<Unit>()
    }

    @DeleteMapping("/api/roles/{roleId}")
    fun deleteRole(principal: JwtAuthenticationToken, @PathVariable roleId: String) = run {
        permissionValidator.validate(principal, Scopes.from(Scope.DELETE_ROLE))
        roleRepository.delete(roleId)
        noContent().build<Unit>()
    }

    @ExceptionHandler(ProtectedRoleFromDeletionException::class)
    fun defaultRoleDeleteExceptionHandler() =
        ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build<Unit>()
}