package dev.kowski.service

import dev.kowski.model.LinksResponse

/**
 * Service interface for retrieving navigation links.
 */
interface NavigationLinkService {

    /**
     * Get links from configured external API, possibly filtered by parent (label) and sorted.
     */
    suspend fun getLinks(parent: String? = null, sort: List<String>? = null): List<LinksResponse>
}