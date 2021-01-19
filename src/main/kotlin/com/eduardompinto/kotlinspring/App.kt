package com.eduardompinto.kotlinspring

import com.eduardompinto.kotlinspring.usecase.githubstarred.GithubLoader
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.util.Base64
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess


@SpringBootApplication
class App(private val githubLoader: GithubLoader) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val starred = githubLoader.listStars("eduardompinto")
        println(starred)
        exitProcess(0)
    }
}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}


@Configuration
class BeanFactory(
    @Value("\${GITHUB_USER_ID}") private val userId: String,
    @Value("\${GITHUB_API_KEY}") private val apiKey: String,
) {

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun httpClient(): HttpClient = HttpClient.newHttpClient()

    @Bean
    fun executorService(): ExecutorService = Executors.newFixedThreadPool(10)

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun jsonIgnoreUnknown() = Json { ignoreUnknownKeys = true }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun requestWithAuthHeaders(): HttpRequest.Builder = HttpRequest.newBuilder()
        .header(
            "Authorization",
            "Basic ${
                Base64.getEncoder().encodeToString("$userId:$apiKey".toByteArray())
            }"
        )

    @Bean
    @Qualifier("baseHeaders")
    fun headers(): Map<String, String> = mapOf(
        "Authorization" to "Basic ${
            Base64.getEncoder().encodeToString("$userId:$apiKey".toByteArray())
        }"
    )
}

operator fun HttpHeaders.get(key: String): List<String> = this.allValues(key)

