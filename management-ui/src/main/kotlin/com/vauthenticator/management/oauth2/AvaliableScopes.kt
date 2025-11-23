package com.vauthenticator.management.oauth2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
class AvailableScopesEndPoint(private val availableScopeRepository: AvailableScopeRepository) {
    @GetMapping("/scopes")
    fun availableScopes() =
        ok().body(availableScopeRepository.getScopes())
}

@JvmInline
value class Scope(val content: String)

@JvmInline
value class Scopes(val content: List<Scope>)
interface AvailableScopeRepository {

    fun getScopes(): Scopes
}

class WellKnownEndPointAvailableScopeRepository(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val restTemplate: RestTemplate
) : AvailableScopeRepository {
    private val endpoint = "/.well-known/openid-configuration"
    override fun getScopes(): Scopes {
        val entity = restTemplate.getForObject("${baseUrl}$endpoint", String::class.java)
        val rawScopes = objectMapper.readTree(entity).get("scopes_supported") as ArrayNode
        return Scopes(rawScopes.map { Scope(it.asText()) })
    }

}