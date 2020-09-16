package com.eduardompinto.kotlinspring.usecase.githubstarred

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class GithubStarredController(
    private val fetcher: GithubStarredFetcher
) {

    @GetMapping("/{user}", produces = [APPLICATION_JSON_VALUE])
    fun listStars(@PathVariable user: String): ResponseEntity<*> {
        return try {
            val response = fetcher.fetchStars(user).sortedByDescending {
                it.stargazers_count
            }
            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            val errorBody = mapOf("message" to ex.message)
            ResponseEntity.status(500).body(errorBody)
        }
    }

}