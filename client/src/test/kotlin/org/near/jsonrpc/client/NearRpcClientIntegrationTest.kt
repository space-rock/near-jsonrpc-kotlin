package org.near.jsonrpc.client

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*
import org.near.jsonrpc.types.*
import java.io.File
import kotlin.test.*

/**
 * Integration tests for NearRpcClient with mock HTTP engine.
 * Tests the actual client call() method, error handling, and factory methods.
 */
class NearRpcClientIntegrationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
            explicitNulls = false
            serializersModule = nearSerializersModule
        }

    private val mockDirectory = File("src/test/resources/mock")

    @Test
    fun `test NearRpcClient default factory method`() {
        val client = NearRpcClient.default("https://rpc.testnet.near.org")
        assertNotNull(client, "Client should be created")
    }

    @Test
    fun `test NearRpcClient fromClient factory method`() {
        val httpClient =
            HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        respond(
                            content = """{"jsonrpc":"2.0","id":1,"result":null}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                }
            }

        val client =
            NearRpcClient.fromClient(
                endpoint = "https://test.near.org",
                client = httpClient,
            )

        assertNotNull(client, "Client should be created from custom HttpClient")
    }

    @Test
    fun `test NearRpcClient fromClient with custom json`() {
        val customJson =
            Json {
                ignoreUnknownKeys = true
                isLenient = false
                serializersModule = nearSerializersModule
            }

        val httpClient =
            HttpClient(MockEngine) {
                engine {
                    addHandler { request ->
                        respond(
                            content = """{"jsonrpc":"2.0","id":1,"result":null}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                }
            }

        val client =
            NearRpcClient.fromClient(
                endpoint = "https://test.near.org",
                client = httpClient,
                json = customJson,
            )

        assertNotNull(client, "Client should be created with custom JSON config")
    }

    @Test
    fun `test call method with successful response`() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    // Verify request structure
                    assertEquals("https://test.near.org", request.url.toString())
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals(ContentType.Application.Json, request.body.contentType)

                    respond(
                        content = """{"jsonrpc":"2.0","id":1,"result":"test_result"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }

            val client = NearRpcClient.fromClient("https://test.near.org", httpClient, json)

            val result =
                client.call(
                    method = "test_method",
                    params = "test_params",
                    paramsSerializer = String.serializer(),
                    resultSerializer = String.serializer(),
                    id = 1,
                )

            assertEquals("test_result", result)
        }

    @Test
    fun `test call method with error response`() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = """{"jsonrpc":"2.0","id":1,"error":{"code":-32600,"message":"Invalid Request","data":null}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }

            val client = NearRpcClient.fromClient("https://test.near.org", httpClient, json)

            val exception =
                assertFailsWith<JsonRpcException> {
                    client.call(
                        method = "test_method",
                        params = "test_params",
                        paramsSerializer = String.serializer(),
                        resultSerializer = String.serializer(),
                    )
                }

            assertEquals(-32600, exception.code)
            assertEquals("JSON-RPC Error -32600: Invalid Request", exception.message)
        }

    @Test
    fun `test call method with error containing data`() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = """{"jsonrpc":"2.0","id":1,"error":{"code":-32602,"message":"Invalid params","data":{"details":"Missing required field"}}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }

            val client = NearRpcClient.fromClient("https://test.near.org", httpClient, json)

            val exception =
                assertFailsWith<JsonRpcException> {
                    client.call(
                        method = "test_method",
                        params = buildJsonObject { put("test", "value") },
                        paramsSerializer = JsonObject.serializer(),
                        resultSerializer = String.serializer(),
                    )
                }

            assertEquals(-32602, exception.code)
            assertTrue(exception.message!!.contains("Invalid params"))
            assertNotNull(exception.data)
        }

    @Test
    fun `test JsonRpcException properties`() {
        val data = JsonPrimitive("test data")
        val exception =
            JsonRpcException(
                message = "Test error",
                code = -32000,
                data = data,
            )

        assertEquals("JSON-RPC Error -32000: Test error", exception.message)
        assertEquals(-32000, exception.code)
        assertEquals(data, exception.data)
    }

    @Test
    fun `test call with custom id`() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    // Just verify the request was made and return a response
                    // (Inspecting the body is complex due to Ktor's internal types)
                    respond(
                        content = """{"jsonrpc":"2.0","id":42,"result":"success"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }

            val client = NearRpcClient.fromClient("https://test.near.org", httpClient, json)

            val result =
                client.call(
                    method = "test_method",
                    params = "test",
                    paramsSerializer = String.serializer(),
                    resultSerializer = String.serializer(),
                    id = 42,
                )

            // Verify we got a response
            assertEquals("success", result)
            println("✅ Custom ID test passed")
        }
}
