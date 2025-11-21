package com.vauthenticator.server.role.api

import com.vauthenticator.server.role.domain.ProtectedRoleFromDeletionException
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

@RestController
class RoleEndPoint(private val roleRepository: RoleRepository) {

    @GetMapping("/api/roles")
    fun findAllRole() =
        ok().body(roleRepository.findAll())

    @PutMapping("/api/roles")
    fun saveRole(@RequestBody role: Role) = run {
        roleRepository.save(role)
        noContent().build<Unit>()
    }

    @DeleteMapping("/api/roles/{roleId}")
    fun deleteRole(@PathVariable roleId: String) = run {
        roleRepository.delete(roleId)
        noContent().build<Unit>()
    }

    @ExceptionHandler(ProtectedRoleFromDeletionException::class)
    fun defaultRoleDeleteExceptionHandler() =
        ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build<Unit>()
}