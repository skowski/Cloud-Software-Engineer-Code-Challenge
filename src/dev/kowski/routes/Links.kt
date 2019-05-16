package dev.kowski.routes

import dev.kowski.Links
import dev.kowski.service.NavigationLinkService
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

/**
 * Register route(s) for the [Links] route '/links'.
 */
fun Route.links(navigationLinkService: NavigationLinkService) {

    get<Links> { parameter ->

        val parent = parameter.parent
        val sort = parameter.sort?.split(",")?.toList()

        val links = navigationLinkService.getLinks(parent, sort)

        call.respond(links)
    }

}