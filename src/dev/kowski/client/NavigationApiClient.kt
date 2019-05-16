package dev.kowski.client

import dev.kowski.model.NavigationResponse

/**
 * Interface for retrieving navigation information from an arbitrary API.
 */
interface NavigationApiClient {

    /**
     * Get navigation information in form of a [NavigationResponse].
     */
    suspend fun getNavigation(): NavigationResponse
}