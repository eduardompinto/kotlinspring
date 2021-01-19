package com.eduardompinto.kotlinspring.usecase.githubstarred


import com.eduardompinto.kotlinspring.get
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit


@Service
class GithubStarredFetcherParallel(
    private val httpClient: HttpClient,
    private val httpRequestBuilder: HttpRequest.Builder,
    private val json: Json,
    private val executor: ExecutorService
) : GithubStarredFetcher {

    private val logger = LogFactory.getLog(this::class.java)

    override fun fetchStars(
        user: String
    ): List<StarredResponse> {
        logger.info("Getting starred parallel")
        val firstResponse = makeRequest(user)
        val lastPage = Link.getLastPage(firstResponse.headers()["link"])
        val firstPageContent = parseResponse(firstResponse)
        return firstPageContent.toList() + getAllPages(lastPage, user)
    }

    private fun getAllPages(
        lastPage: Int?,
        user: String
    ): List<StarredResponse> {
        return when (lastPage) {
            null -> emptyList()
            else -> (2..lastPage).map {
                executor.submit(Callable {
                    parseResponse(makeRequest(user, it))
                })
            }.flatMap {
                it.get(5, TimeUnit.SECONDS)
            }
        }
    }


    private fun parseResponse(re: HttpResponse<String>): List<StarredResponse> {
        val c = re.body() ?: return emptyList()
        return json.decodeFromString(c)
    }

    private fun makeRequest(
        user: String,
        page: Int = 1
    ): HttpResponse<String> {
        val request = httpRequestBuilder
            .uri(URI(STARRED_URLS.format(user, page)))
            .GET()
            .build()
        return httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )
    }

}
