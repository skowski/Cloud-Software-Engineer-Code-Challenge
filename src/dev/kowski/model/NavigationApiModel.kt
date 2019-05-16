package dev.kowski.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Base class for all navigation entries fetched from external navigation API.
 *
 * Must list all JSON subtypes here for Jackson JSON processing using property 'type' as discriminator.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = NavigationSection::class, name = "section"),
    JsonSubTypes.Type(value = NavigationNode::class, name = "node"),
    JsonSubTypes.Type(value = NavigationLink::class, name = "link"),
    JsonSubTypes.Type(value = NavigationExtLink::class, name = "external-link")
)
sealed class NavigationEntry {
    abstract val label: String
}

/**
 * Base class for navigation entries that have child entries (possibly empty).
 */
sealed class NavigationEntryWithChildren : NavigationEntry() {
    abstract val children: List<NavigationEntry>
}

/**
 * Base class for navigation entries that have an URL link.
 */
sealed class NavigationEntryWithLink : NavigationEntry() {
    abstract val url: String
}




/**
 * Data class container for response from Navigation API.
 */
data class NavigationResponse(val navigationEntries: List<NavigationEntry>)

/**
 * Data class for a navigation entry of type 'section' with child entries.
 */
data class NavigationSection(override val label: String, override val children: List<NavigationEntry>) :
    NavigationEntryWithChildren()

/**
 * Data class for a navigation entry of type 'node' with child entries.
 */
data class NavigationNode(override val label: String, override val children: List<NavigationEntry>) :
    NavigationEntryWithChildren()

/**
 * Data class for a navigation entry of type 'link' with an URL.
 */
data class NavigationLink(override val label: String, override val url: String) :
    NavigationEntryWithLink()

/**
 * Data class for a navigation entry of type 'external-link' with an URL.
 */
data class NavigationExtLink(override val label: String, override val url: String) :
    NavigationEntryWithLink()
