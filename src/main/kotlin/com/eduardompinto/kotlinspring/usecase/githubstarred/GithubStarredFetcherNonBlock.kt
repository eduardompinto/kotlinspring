package com.eduardompinto.kotlinspring.usecase.githubstarred


import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux


@Service
@Primary
class GithubStarredFetcherNonBlock(
    private val json: Json,
) : GithubStarredFetcher {

    private val client = WebClient.create()

    override fun fetchStars(user: String): List<StarredResponse> {
        val resp = getPage(user, 1).exchange().block()!!
        val lastPage = Link.getLastPage(resp.headers().header("link")) ?: 2

        val otherPages: List<String> = Flux.merge((2..lastPage).map { page ->
            getPage(user, page)
                .retrieve()
                .bodyToMono(String::class.java)
                .log()
                .doOnRequest {
                    println("Making request [$page]")
                }.doOnSuccess {
                    println("Done to [$page]")
                }
        }).collectList().block() ?: emptyList()

        return parseResponse(resp.bodyToMono(String::class.java).block()) + (otherPages.map {
            parseResponse(it)
        }.flatten())
    }

    private fun parseResponse(respAsString: String?): List<StarredResponse> =
        if (respAsString.isNullOrBlank()) emptyList()
        else json.decodeFromString(respAsString)

    private fun getPage(user: String, page: Int) = client.get()
        .uri { uriBuilder ->
            uriBuilder
                .scheme("https")
                .host("api.github.com")
                .path("users/$user/starred")
                .queryParam("page", page)
                .build()
        }.also {
            println("Building request to page [$page]")
        }

}