package dev.kowski

import dev.kowski.client.NavigationApiClient
import dev.kowski.client.NavigationApiClientFromFile
import dev.kowski.service.NavigationLinkService
import dev.kowski.service.NavigationLinkServiceImpl
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Applition tests for testing provided endpoint(s) functionality.
 */
class ApplicationTest {

    @Test
    fun `application should start with default dependencies`() {
        val tempPath = Files.createTempDirectory(null).toFile().apply { deleteOnExit() }
        try {
            withTestApplication({
                (environment.config as MapApplicationConfig).apply {
                    put(PROP_NAVIGATION_API_URL, "http://0.0.0.0")
                    put(PROP_NAVIGATION_API_KEY, "test-key")
                }
                main()
            }, { } )
        } finally {
            tempPath.deleteRecursively()
        }
    }

    @Test
    fun `should return all navigation item links`() {

        testApp("test/resources/input.json") {
            handleRequest(HttpMethod.Get, "/links").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    Files.readAllLines(Paths.get("test/resources/output_full.json")).joinToString("\n"),
                    response.content)
            }
        }
    }

    @Test
    fun `should return navigation item links under parent 'Alter'`() {

        testApp("test/resources/input.json") {
            handleRequest(HttpMethod.Get, "/links?parent=Alter").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    Files.readAllLines(Paths.get("test/resources/output_with_parent.json")).joinToString("\n"),
                    response.content)
            }
        }
    }

    @Test
    fun `should return navigation item links under parent 'Alter' sorted descending by 'label'`() {

        testApp("test/resources/input.json") {
            handleRequest(HttpMethod.Get, "/links?parent=Alter&sort=label:desc").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    Files.readAllLines(Paths.get("test/resources/output_with_parent_sort_label_desc.json")).joinToString("\n"),
                    response.content)
            }
        }
    }

    @Test
    fun `should return 404 - Not Found for non existing parent`() {

        val parent = "notfound"
        testApp("test/resources/input.json") {
            handleRequest(HttpMethod.Get, "/links?parent=$parent").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertTrue(response.content!!.contains("'$parent'"))
            }
        }
    }

    @Test
    fun `should return 400 - Bad Request for invalid sort field`() {

        val sort = "field"
        testApp("test/resources/input.json") {
            handleRequest(HttpMethod.Get, "/links?sort=$sort").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertTrue(response.content!!.contains("'$sort'"))
            }
        }
    }

    @Test
    fun `should return 400 - Bad Request for invalid sort order`() {

        val field = "label"
        val order = "foo"
        testApp("test/resources/input.json") {
            handleRequest(HttpMethod.Get, "/links?sort=$field:$order").apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertTrue(response.content!!.contains("'$field'"))
                assertTrue(response.content!!.contains("'$order'"))
            }
        }
    }

    /**
     * Convenience method used to configure a test application with API input from file (by [fileName]) and to
     * execute a [block] testing it.
     */
    private fun testApp(fileName: String, block: TestApplicationEngine.() -> Unit) {
        val tempPath = Files.createTempDirectory(null).toFile().apply { deleteOnExit() }
        val navigationApiClient: NavigationApiClient = NavigationApiClientFromFile(fileName)
        val navigationLinkService: NavigationLinkService = NavigationLinkServiceImpl(navigationApiClient)
        try {
            withTestApplication({
                mainWithDependencies(navigationLinkService, testing = true)
            }, block)
        } finally {
            tempPath.deleteRecursively()
        }
    }

}
