package com.eduardompinto.kotlinspring.usecase.githubstarred


import org.apache.juli.logging.LogFactory
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.Marker
import org.springframework.context.annotation.Primary
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit


@Service
@Primary
class GithubStarredFetcherParallel(
    private val restTemplate: RestTemplate,
    private val executor: ExecutorService
) : GithubStarredFetcher {

    private val logger = LogFactory.getLog(this::class.java)

    override fun fetchStars(
        user: String
    ): List<StarredResponse> {
        logger.info("Getting starred parallel")

        val firstResponse = makeRequest(user)
        val lastPage = Link.getLastPage(firstResponse.headers["link"])
        val firstPageContent = getResponseBody(firstResponse)
        return firstPageContent + getAllPages(lastPage, user)
    }

    private inline fun getAllPages(
        lastPage: Int?,
        user: String
    ): List<StarredResponse> {
        return when (lastPage) {
            null -> emptyList()
            else -> (2..lastPage).map {
                executor.submit(Callable {
                    getResponseBody(makeRequest(user, it))
                })
            }.flatMap {
                it.get(5, TimeUnit.SECONDS)
            }
        }
    }


    private inline fun getResponseBody(re: ResponseEntity<Array<StarredResponse>>) =
        re.body?.toList() ?: emptyList()

    private inline fun makeRequest(
        user: String,
        page: Int = 1
    ): ResponseEntity<Array<StarredResponse>> {
        return restTemplate.getForEntity(
            STARRED_URLS.format(user, page),
            Array<StarredResponse>::class.java,
        )
    }

}
