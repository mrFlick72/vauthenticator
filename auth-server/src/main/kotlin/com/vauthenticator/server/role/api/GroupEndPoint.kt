package com.vauthenticator.server.role.api

import com.vauthenticator.server.role.domain.Group
import com.vauthenticator.server.role.domain.GroupRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class GroupEndPoint(private val groupRepository: GroupRepository) {

    @GetMapping("/api/groups")
    fun findAllGroups(): ResponseEntity<List<Group>> {
        val groups = groupRepository.findAll()
        return ResponseEntity.ok().body(groups)
    }

    @PutMapping("/api/groups")
    fun saveGroup(@RequestBody group: Group): ResponseEntity<Unit> {
        groupRepository.save(group)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/api/groups/{groupId}")
    fun deleteGroup(@PathVariable groupId: String): ResponseEntity<Unit> {
        groupRepository.delete(groupId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/api/groups/{groupId}/roles")
    fun associateRoleToAGroup(
        @PathVariable groupId: String, @RequestBody representation: RoleToGroupAssociationRepresentation
    ): ResponseEntity<Unit> = if (representation.haveNoCommonElements()) {
        groupRepository.roleAssociation(groupId, *representation.toBeAssociated.toTypedArray())
        groupRepository.roleDeAssociation(groupId, *representation.toBeDeAssociated.toTypedArray())
        ResponseEntity.noContent().build()
    } else {
        ResponseEntity.badRequest().build()
    }

}

data class RoleToGroupAssociationRepresentation(
    val toBeAssociated: List<String>, val toBeDeAssociated: List<String>
) {
    fun haveNoCommonElements(): Boolean {
        return toBeAssociated.intersect(toBeDeAssociated.toSet()).isEmpty()
    }
}