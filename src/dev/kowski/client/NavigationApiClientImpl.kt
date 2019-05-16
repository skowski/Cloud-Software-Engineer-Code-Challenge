package dev.kowski.client

import dev.kowski.model.NavigationResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.coroutineScope

/**
 * Implementation querying an external API via provided [client] at given address using given API key.
 */
class NavigationApiClientImpl(private val client: HttpClient, private val apiUrl: String, private val apiKey: String) :
    NavigationApiClient {

    /**
     * Get navigation information from configured external API.
     */
    override suspend fun getNavigation(): NavigationResponse = coroutineScope {
        client.get<NavigationResponse>(apiUrl) {
            header("x-api-key", apiKey)
        }
    }

}