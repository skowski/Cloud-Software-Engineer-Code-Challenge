package dev.kowski.service

import dev.kowski.client.NavigationApiClient
import dev.kowski.client.NavigationApiClientImpl
import dev.kowski.model.*
import io.ktor.features.NotFoundException
import java.util.*

/**
 * Service class for processing navigation links. Used to query a navigation API which implements
 * the [NavigationApiClientImpl] interface and providing a [NavigationResponse].
 */
class NavigationLinkServiceImpl(private val navigationApiClient: NavigationApiClient): NavigationLinkService {

    /**
     * Get links from configured external API, possibly filtered by parent (label) and sorted.
     */
    override suspend fun getLinks(parent: String?, sort: List<String>?): List<LinksResponse> {

        val entries = navigationApiClient.getNavigation().navigationEntries

        val filtered = filterByParent(entries, parent)
        val transformed = transform(filtered)
        val sorted = sort(transformed, sort)

        return sorted
    }

    /**
     * Filters the given [input] tree structures of navigation entries for items with parent labeled
     * [parent]. Returns all items that have a corresponding parent item, multiple parent items with
     * same label are possible and all qualifying items are returned.
     * If parameter [parent] is null the input is returned. Empty parameter [parent] is
     * handled like all other non-empty input.
     * Throws [NotFoundException] when no corresponding parent is found.
     */
    private fun filterByParent(input: List<NavigationEntry>, parent: String?): List<NavigationEntry> {

        if (parent == null) {
            return input
        }

        val toVisit: Queue<NavigationEntry> = ArrayDeque()
        val filtered = mutableListOf<NavigationEntry>()
        input.forEach { toVisit.offer(it) }

        while (toVisit.isNotEmpty()) {
            val current = toVisit.poll()
            if (current.label == parent) {
                when (current) {
                    is NavigationEntryWithChildren -> filtered.addAll(current.children)
                    is NavigationEntryWithLink -> filtered.add(current)
                }
            } else if (current is NavigationEntryWithChildren) {
                current.children.forEach { toVisit.offer(it) }
            }
        }

        if (filtered.isEmpty()) {
            throw NotFoundException("No parent with label '$parent' found")
        }
        return filtered
    }

    /**
     * Transforms the given [input] tree structures of navigation entries into a flat list
     * of [LinksResponse]s.
     */
    private fun transform(input: List<NavigationEntry>): List<LinksResponse> {

        fun inner(entry: NavigationEntry, parentLabels: List<String>): List<LinksResponse> {

            val labels = parentLabels + entry.label
            return when (entry) {
                is NavigationEntryWithChildren -> entry.children.flatMap { inner(it, labels) }
                is NavigationEntryWithLink -> listOf(LinksResponse(labels.joinToString(" - "), entry.url))
            }
        }
        return input.flatMap { inner(it, listOf()) }
    }

    /**
     * Sorts the given [input] according to sort information given by [sort] (for syntax see
     * https://specs.openstack.org/openstack/api-wg/guidelines/pagination_filter_sort.html#sorting).
     * Returns the input when no sort is given.
     */
    private fun sort(input: List<LinksResponse>, sort: List<String>?): List<LinksResponse> {

        if (sort == null || sort.isEmpty()) {
            return input
        }

        return sort
            .map { it.splitToPair(":") }
            .map { LinksResponse.comparator(it.first, it.second) }
            .reduce { acc, comparator -> acc.then(comparator) }
            .let {
                input.sortedWith(it)
            }
    }
}

/**
 * Split String to a [Pair] with given [delimiter]. Fails when resulting split is empty or
 * size greater than two. Second component of result can be null.
 * Used for splitting query parameter for sort.
 */
private fun String.splitToPair(delimiter: String): Pair<String, String?> {
    return this.split(delimiter).let {split ->
        when(split.size) {
            1 -> Pair(split[0], null)
            2 -> Pair(split[0], split[1])
            else -> throw IllegalArgumentException("")
        }
    }
}