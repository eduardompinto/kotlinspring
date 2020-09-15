package com.eduardompinto.kotlinspring.usecase.githubstarred

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

private const val STARRED_URLS =
    "https://api.github.com/users/%s/starred?page=%d"

@Service
class GithubStarredFetcher(
    private val restTemplate: RestTemplate,
    private val threads: ExecutorService
) {

    fun fetchStars(user: String): List<StarredResponse> {
        var link: String? = STARRED_URLS.format(user, 1)
        val starredRepos = mutableListOf<StarredResponse>()
        while (link != null) {
            val resp = restTemplate.getForEntity(
                link,
                Array<StarredResponse>::class.java
            )
            link = getNextLink(resp.headers["link"])
            starredRepos.addAll(resp.body?.toList() ?: emptyList())
        }
        return starredRepos.sortedByDescending {
            it.stargazers_count
        }
    }

    fun fetchStars2(user: String): List<StarredResponse> {
        val firstResponse = makeRequest(user)
        val lastPage = getLastPage(firstResponse.headers["link"])
        val futures = if (lastPage != null) {
            (2..lastPage).map {
                threads.submit(Callable {
                    val re = makeRequest(user, it)
                    getResponseBody(re)
                })
            }
        } else {
            emptyList()
        }

        val extra = futures.flatMap {
            while (!it.isDone) {
            }
            it.get()
        }
        val firstPage = getResponseBody(firstResponse)
        return (firstPage + extra).sortedByDescending {
            it.stargazers_count
        }
    }

    private fun getResponseBody(re: ResponseEntity<Array<StarredResponse>>) =
        re.body?.toList() ?: emptyList()

    private fun makeRequest(
        user: String,
        page: Int = 1
    ): ResponseEntity<Array<StarredResponse>> {
        return restTemplate.getForEntity(
            STARRED_URLS.format(user, page),
            Array<StarredResponse>::class.java
        )
    }


    private fun getNextLink(link: List<String>?): String? {
        return getLink(link, "next")?.value
    }

    private fun getLastPage(link: List<String>?): Int? {
        return getLink(link, "last")?.value?.split("page=")?.last()?.toInt()
    }

    private fun getLink(link: List<String>?, rel: String): Link? {
        // 	<https://api.github.com/user/1915988/starred?page=6>; rel="prev",
        // 	<https://api.github.com/user/1915988/starred?page=8>; rel="next",
        // 	<https://api.github.com/user/1915988/starred?page=8>; rel="last",
        // 	<https://api.github.com/user/1915988/starred?page=1>; rel="first"
        return when {
            link == null || link.isEmpty() -> null
            else -> link.first().split(",").map {
                Link.buildFrom(it)
            }.firstOrNull { it.rel == rel }
        }
    }


}

private data class Link(
    val value: String,
    val rel: String
) {
    companion object {
        fun buildFrom(link: String): Link {
            val stringsToRemove = listOf("<", ">", "rel=", " ", "\"")
            val (value, rel) = stringsToRemove.fold(link) { acc, toRemove ->
                acc.replace(toRemove, "")
            }.split(";")
            return Link(value = value, rel = rel)
        }
    }
}

data class StarredResponse(
    val html_url: String,
    val name: String,
    val full_name: String,
    val stargazers_count: Int
)