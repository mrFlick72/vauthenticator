package com.vauthenticator.server.role.api

import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.Group
import com.vauthenticator.server.role.domain.GroupRepository
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
class GroupEndPoint(
    private val groupRepository: GroupRepository,
    private val permissionValidator: PermissionValidator
) {

    @GetMapping("/api/groups")
    fun findAllGroups(principal: JwtAuthenticationToken): ResponseEntity<List<Group>> {
        permissionValidator.validate(principal, Scopes.from(Scope.READ_GROUP))
        val groups = groupRepository.findAll()
        return ResponseEntity.ok().body(groups)
    }

    @PutMapping("/api/groups")
    fun saveGroup(principal: JwtAuthenticationToken, @RequestBody group: Group): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.SAVE_GROUP))
        groupRepository.save(group)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/api/groups/{groupId}")
    fun deleteGroup(principal: JwtAuthenticationToken, @PathVariable groupId: String): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.DELETE_GROUP))
        groupRepository.delete(groupId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/api/groups/{groupId}/roles")
    fun associateRoleToAGroup(
        principal: JwtAuthenticationToken,
        @PathVariable groupId: String, @RequestBody representation: RoleToGroupAssociationRepresentation
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.SAVE_GROUP))
        return if (representation.haveNoCommonElements()) {
            groupRepository.roleAssociation(groupId, *representation.toBeAssociated.toTypedArray())
            groupRepository.roleDeAssociation(groupId, *representation.toBeDeAssociated.toTypedArray())
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }

}

data class RoleToGroupAssociationRepresentation(
    val toBeAssociated: List<String>, val toBeDeAssociated: List<String>
) {
    fun haveNoCommonElements(): Boolean {
        return toBeAssociated.intersect(toBeDeAssociated.toSet()).isEmpty()
    }
}