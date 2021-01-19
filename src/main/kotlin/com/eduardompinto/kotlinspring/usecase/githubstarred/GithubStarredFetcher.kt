package com.eduardompinto.kotlinspring.usecase.githubstarred

import kotlinx.serialization.Serializable


const val STARRED_URLS =
    "https://api.github.com/users/%s/starred?page=%d"

interface GithubStarredFetcher {

    fun fetchStars(user: String): List<StarredResponse>

}

internal data class Link(
    val value: String,
    val rel: String
) {
    companion object {
        fun getNextLinkUrl(link: List<String>?): String? {
            return getLinks(link).firstOrNull {
                it.rel == "next"
            }?.value
        }

        fun getLastPage(link: List<String>?): Int? {
            return getLinks(link).firstOrNull {
                it.rel == "last"
            }?.value?.split("page=")?.last()?.toInt()
        }

        private fun getLinks(link: List<String>?): List<Link> {
            // 	<https://api.github.com/user/1915988/starred?page=6>; rel="prev",
            // 	<https://api.github.com/user/1915988/starred?page=8>; rel="next",
            // 	<https://api.github.com/user/1915988/starred?page=8>; rel="last",
            // 	<https://api.github.com/user/1915988/starred?page=1>; rel="first"
            return when {
                link == null || link.isEmpty() -> emptyList()
                else -> link.first().split(",").map {
                    buildFrom(it)
                }
            }
        }

        private fun buildFrom(link: String): Link {
            val stringsToRemove = listOf("<", ">", "rel=", " ", "\"")
            val (value, rel) = stringsToRemove.fold(link) { acc, toRemove ->
                acc.replace(toRemove, "")
            }.split(";")
            return Link(value = value, rel = rel)
        }
    }
}

@Serializable
data class StarredResponse(
    val html_url: String,
    val name: String,
    val full_name: String,
    val stargazers_count: Int
)