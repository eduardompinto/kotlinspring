package com.eduardompinto.kotlinspring

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.web.client.RestTemplate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SpringBootApplication
class KotlinspringApplication

fun main(args: Array<String>) {
	runApplication<KotlinspringApplication>(*args)
}

@Configuration
class BeanFactory(
	@Value("\${GITHUB_USER_ID}") private val userId: String,
	@Value("\${GITHUB_API_KEY}") private val apiKey: String,
) {

	@Bean
	fun restTemplate() = RestTemplate().apply {
		interceptors.add(BasicAuthenticationInterceptor(userId, apiKey))
	}

	@Bean
	fun executorService(): ExecutorService = Executors.newFixedThreadPool(10)
}