package com.example.signup

import androidx.compose.runtime.Composable
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.httpFetcher
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

@Composable
fun provideKamelConfig(): KamelConfig {
    val httpClient = HttpClient(OkHttp) {
        // Configure your HttpClient here if needed
    }

    return KamelConfig {
        httpFetcher(httpClient)
    }
}