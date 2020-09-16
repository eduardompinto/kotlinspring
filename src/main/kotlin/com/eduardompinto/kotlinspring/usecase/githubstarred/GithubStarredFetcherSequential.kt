package com.eduardompinto.kotlinspring.usecase.githubstarred

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GithubStarredFetcherSequential(
    private val restTemplate: RestTemplate
) : GithubStarredFetcher {

    override fun fetchStars(user: String): List<StarredResponse> {
        var url: String? = STARRED_URLS.format(user, 1)
        val starredRepos = mutableListOf<StarredResponse>()
        while (url != null) {
            val resp = restTemplate.getForEntity(
                url,
                Array<StarredResponse>::class.java
            )
            url = Link.getNextLinkUrl(resp.headers["link"])
            starredRepos.addAll(resp.body?.toList() ?: emptyList())
        }
        return starredRepos
    }

}