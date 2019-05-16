package dev.kowski

import com.codahale.metrics.JmxReporter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializationFeature
import dev.kowski.routes.links
import dev.kowski.service.NavigationLinkService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.config.ApplicationConfig
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.metrics.Metrics
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.util.error
import org.koin.core.KoinApplication
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import java.util.concurrent.TimeUnit

const val PROP_NAVIGATION_API_URL = "application.navigation.api.url"
const val PROP_NAVIGATION_API_KEY = "application.navigation.api.key"

/**
 * Location class for links for use with locations feature.
 */
@Location("/links")
data class Links(val parent: String? = null, val sort: String? = null)


/**
 * Main module with starting with default dependencies.
 */
@Suppress("unused") // Referenced in application.conf
fun Application.main() {

    /**
     * Add Koin for Dependency Injection/Service Location. Pass necessary parameters to Koin.
     */
    install(Koin) {
        ktorProperties(
            environment.config,
            listOf(
                PROP_NAVIGATION_API_URL,
                PROP_NAVIGATION_API_KEY
            )
        )
        modules(applicationModule)
    }

    val navigationLinkService by inject<NavigationLinkService>()

    mainWithDependencies(navigationLinkService, testing = false)
}

/**
 * Manually pass dependencies to main module for exchange them in tests.
 */
fun Application.mainWithDependencies(navigationLinkService: NavigationLinkService, testing: Boolean = false) {

    /**
     * Add call logging for all requests at trace level.
     */
    install(CallLogging) {
        level = Level.TRACE
        filter { call -> call.request.path().startsWith("/") }
        callIdMdc("X-Request-ID")
    }

    /**
     * Add callId for traceing of requests.
     */
    install(CallId) {
        generate(10)
    }

    /**
     * Add Ktor default headers ('date' and 'server').
     */
    install(DefaultHeaders)

    // No Metrics in testing, at least not to JMX because of problems when running several tests in one test class
    // against test application instance
    if (!testing) {

        /**
         * Add metrics, for now report via JMX but possibly change to report to Graphite or something similar.
         */
        install(Metrics) {
            // switch to another reporter like Graphite
            val reporter = JmxReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
            reporter.start()
        }

    }

    /**
     * Add automatic content conversion for JSON via Jackson (according to Content-Type and Accept headers)
     */
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            enable(JsonGenerator.Feature.ESCAPE_NON_ASCII)
            setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        }
    }

    /**
     * Some basic error handling. For now just handle [NotFoundException] (parent element with given label
     * not found) and [IllegalArgumentException] (input parameter for sorting not valid).
     * For all other errors respond with INTERNAL_SERVER_ERROR and the error message as a response.
     * Maybe refine for finer grained error handling.
     */
    install(StatusPages) {
        exception<NotFoundException> { cause ->
            call.respond(HttpStatusCode.NotFound, cause.localizedMessage)
        }
        exception<IllegalArgumentException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.localizedMessage)
        }
        exception<Throwable> { cause ->
            log.error(cause)
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    /**
     * Install Locations feature for type safe locations/routing.
     */
    install(Locations)

    /**
     * Routing function, contains all routes of the service.
     */
    routing {
        links(navigationLinkService)
    }

}

/**
 * Workaround for passing Ktor config properties identified by [names] to Koin.
 */
fun KoinApplication.ktorProperties(config: ApplicationConfig, names: List<String>) {
    val ktorProperties = names.associateWith { config.property(it).getString() }
    properties(ktorProperties)
}
