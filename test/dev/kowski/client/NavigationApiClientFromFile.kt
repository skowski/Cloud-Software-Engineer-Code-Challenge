package dev.kowski.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.kowski.model.NavigationResponse
import kotlinx.coroutines.coroutineScope
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Implementation of [NavigationApiClient] which retrieves the navigation information from file system.
 * Used for testing.
 */
class NavigationApiClientFromFile(private val fileName: String) : NavigationApiClient {

    /**
     * Get navigation information from a file.
     */
    override suspend fun getNavigation(): NavigationResponse = coroutineScope {
        val objectMapper = jacksonObjectMapper()
        objectMapper.readValue(
            Files.readAllLines(Paths.get(fileName)).joinToString("\n"),
            NavigationResponse::class.java
        )
    }
}