package com.eduardompinto.kotlinspring.usecase.githubstarred


import com.eduardompinto.kotlinspring.get
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class GithubStarredFetcherSequential(
    private val httpClient: HttpClient,
    private val httpRequestBuilder: HttpRequest.Builder
) : GithubStarredFetcher {

    override fun fetchStars(user: String): List<StarredResponse> {
        var url: String? = STARRED_URLS.format(user, 1)
        val starredRepos = mutableListOf<StarredResponse>()
        while (url != null) {
            val request = httpRequestBuilder
                .uri(URI(url))
                .GET()
                .build()
            val resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            url = Link.getNextLinkUrl(resp.headers()["link"])
            if (resp.body() != null) {
                starredRepos.addAll(Json.decodeFromString(resp.body()))
            }
        }
        return starredRepos
    }

}