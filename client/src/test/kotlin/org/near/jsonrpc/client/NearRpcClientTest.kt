package org.near.jsonrpc.client

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import org.near.jsonrpc.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Basic integration tests for NearRpcClient.
 * For comprehensive mock JSON validation, see ClientMockValidationTest.
 */
class NearRpcClientTest {
    @Test
    fun `client can be created with default config`() {
        val client = NearRpcClient.default("https://rpc.testnet.near.org")
        assertNotNull(client)
    }

    @Test
    fun `client handles mock response correctly`() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            """
                            {
                                "jsonrpc": "2.0",
                                "id": 1,
                                "result": {
                                    "version": {
                                        "version": "1.0.0",
                                        "build": "test"
                                    },
                                    "chain_id": "testnet",
                                    "protocol_version": 100,
                                    "latest_protocol_version": 100,
                                    "rpc_addr": "0.0.0.0:3030",
                                    "validators": [],
                                    "sync_info": {
                                        "latest_block_hash": "abc123",
                                        "latest_block_height": 12345,
                                        "latest_state_root": "xyz789",
                                        "latest_block_time": "2023-01-01T00:00:00.000000000Z",
                                        "syncing": false,
                                        "earliest_block_hash": "abc000",
                                        "earliest_block_height": 1,
                                        "earliest_block_time": "2023-01-01T00:00:00.000000000Z",
                                        "epoch_id": "epoch1",
                                        "epoch_start_height": 1000
                                    },
                                    "validator_account_id": null,
                                    "validator_public_key": null,
                                    "node_public_key": "ed25519:test",
                                    "account_id": null,
                                    "node_key": null,
                                    "uptime_sec": 3600,
                                    "detailed_debug_status": null
                                }
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val json =
                Json {
                    ignoreUnknownKeys = true
                    serializersModule = nearSerializersModule
                }

            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) { json(json) }
                }

            val rpcClient =
                NearRpcClient.fromClient(
                    endpoint = "https://rpc.testnet.near.org",
                    client = client,
                    json = json,
                )

            // Note: This is a simplified test. In production, we'd use proper serializers
            // The mock validation tests in ClientMockValidationTest cover the actual types
            val result =
                rpcClient.call(
                    method = "status",
                    params = emptyList<String>(),
                    paramsSerializer = kotlinx.serialization.builtins.ListSerializer(String.serializer()),
                    resultSerializer = JsonElement.serializer(),
                )

            assertNotNull(result)
        }

    @Test
    fun `client throws exception on error response`() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            """
                            {
                                "jsonrpc": "2.0",
                                "id": 1,
                                "error": {
                                    "code": -32600,
                                    "message": "Invalid Request",
                                    "data": null
                                }
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val json =
                Json {
                    ignoreUnknownKeys = true
                    serializersModule = nearSerializersModule
                }

            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) { json(json) }
                }

            val rpcClient =
                NearRpcClient.fromClient(
                    endpoint = "https://rpc.testnet.near.org",
                    client = client,
                    json = json,
                )

            try {
                rpcClient.call(
                    method = "invalid_method",
                    params = emptyList<String>(),
                    paramsSerializer = kotlinx.serialization.builtins.ListSerializer(String.serializer()),
                    resultSerializer = JsonElement.serializer(),
                )
                throw AssertionError("Expected JsonRpcException to be thrown")
            } catch (e: JsonRpcException) {
                assertEquals(-32600, e.code)
                assertEquals("JSON-RPC Error -32600: Invalid Request", e.message)
            }
        }
}
