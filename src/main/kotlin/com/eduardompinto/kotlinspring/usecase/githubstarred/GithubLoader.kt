package com.eduardompinto.kotlinspring.usecase.githubstarred

import org.springframework.stereotype.Service

@Service
class GithubLoader(
    private val fetcher: GithubStarredFetcher
) {

    fun listStars(user: String): List<StarredResponse> {
        return try {
            fetcher.fetchStars(user).sortedByDescending {
                it.stargazers_count
            }
        } catch (ex: Exception) {
            emptyList()
        }
    }

}