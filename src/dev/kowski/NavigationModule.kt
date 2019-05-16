package dev.kowski

import com.fasterxml.jackson.databind.DeserializationFeature
import dev.kowski.client.NavigationApiClient
import dev.kowski.client.NavigationApiClientImpl
import dev.kowski.service.NavigationLinkService
import dev.kowski.service.NavigationLinkServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import org.koin.dsl.module


/**
 * Koin module defining the default dependencies.
 */
val applicationModule = module {

    single {
        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = JacksonSerializer {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
        }
    }

    single<NavigationApiClient> {
        NavigationApiClientImpl(
            get(),
            getProperty(PROP_NAVIGATION_API_URL),
            getProperty(PROP_NAVIGATION_API_KEY)
        )
    }

    single<NavigationLinkService> { NavigationLinkServiceImpl(get()) }

}