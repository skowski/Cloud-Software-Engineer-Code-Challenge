package dev.kowski.model

import java.util.Comparator

/**
 * Data class for 'links' responses to clients.
 */
data class LinksResponse(val label: String, val url: String) {

    companion object Sort {

        /**
         * Get comparator for given field and sort order. Field name must exist and sort order must
         * be one of 'asc', 'desc' or null (defaults to asc), otherwise [IllegalArgumentException] is
         * thrown.
         */
        fun comparator(field: String, order: String?): Comparator<LinksResponse> {

            val comparator = when (field.toLowerCase()) {
                "label" -> compareBy(LinksResponse::label)
                "url" -> compareBy((LinksResponse::url))
                else -> throw IllegalArgumentException("Unknown field '$field' for sorting.")
            }

            return when (order?.toLowerCase()) {
                null, "asc" -> comparator
                "desc" -> comparator.reversed()
                else -> throw java.lang.IllegalArgumentException(
                    "Invalid sort order '$order' for field '$field'. Valid values are 'asc'" +
                            " and 'desc' or empty (defaults to 'asc')."
                )
            }
        }

    }
}