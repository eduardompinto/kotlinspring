package com.eduardompinto.kotlinspring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class BeanFactory {

    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun executorService(): ExecutorService = Executors.newFixedThreadPool(10)
}