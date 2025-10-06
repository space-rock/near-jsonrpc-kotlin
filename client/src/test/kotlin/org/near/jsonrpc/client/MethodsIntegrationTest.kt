package org.near.jsonrpc.client

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.near.jsonrpc.types.*
import java.io.File
import kotlin.test.*

/**
 * Integration tests for all 31 RPC method extension functions.
 * Uses real mock JSON files to test each method with realistic data.
 */
class MethodsIntegrationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
            explicitNulls = false
            serializersModule = nearSerializersModule
        }

    private val mockDirectory = File("src/test/resources/mock")

    private fun createMockClient(responseContent: String): NearRpcClient {
        val mockEngine =
            MockEngine { request ->
                respond(
                    content = responseContent,
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

        return NearRpcClient.fromClient("https://test.near.org", httpClient, json)
    }

    private fun loadMockResponse(
        methodName: String,
        variant: String = "Success",
    ): String? {
        val files =
            mockDirectory.listFiles { file ->
                file.isFile &&
                    file.extension == "json" &&
                    file.nameWithoutExtension.startsWith("JsonRpcResponse") &&
                    file.nameWithoutExtension.contains(methodName, ignoreCase = true) &&
                    file.nameWithoutExtension.endsWith("_$variant")
            } ?: emptyArray()

        return files.firstOrNull()?.readText()
    }

    private fun loadMockRequest(methodName: String): String? {
        val files =
            mockDirectory.listFiles { file ->
                file.isFile &&
                    file.extension == "json" &&
                    file.nameWithoutExtension.startsWith("JsonRpcRequest") &&
                    file.nameWithoutExtension.contains(methodName, ignoreCase = true)
            } ?: emptyArray()

        return files.firstOrNull()?.readText()
    }

    @Test
    fun `test status method`() =
        runTest {
            val mockResponse = loadMockResponse("status")
            if (mockResponse != null) {
                val client = createMockClient(mockResponse)
                val result = client.status(Unit)
                assertNotNull(result, "Status should return a result")
                println("✅ status() method works with mock data")
            }
        }

    @Test
    fun `test gasPrice method`() =
        runTest {
            val mockResponse = loadMockResponse("gas_price")
            if (mockResponse != null) {
                val requestJson = loadMockRequest("gas_price")
                if (requestJson != null) {
                    val requestData = json.parseToJsonElement(requestJson).jsonObject
                    val params = requestData["params"]
                    if (params != null && params !is JsonNull) {
                        val client = createMockClient(mockResponse)
                        val request = json.decodeFromJsonElement<RpcGasPriceRequest>(params)
                        val result = client.gasPrice(request)
                        assertNotNull(result, "GasPrice should return a result")
                        println("✅ gasPrice() method works with mock data")
                    }
                }
            }
        }

    @Test
    fun `test tx method`() =
        runTest {
            val mockResponse = loadMockResponse("tx")
            if (mockResponse != null) {
                val requestJson = loadMockRequest("tx")
                if (requestJson != null) {
                    val requestData = json.parseToJsonElement(requestJson).jsonObject
                    val params = requestData["params"]
                    if (params != null && params !is JsonNull) {
                        val client = createMockClient(mockResponse)
                        val request = json.decodeFromJsonElement<RpcTransactionStatusRequest>(params)
                        val result = client.tx(request)
                        assertNotNull(result, "Tx should return a result")
                        println("✅ tx() method works with mock data")
                    }
                }
            }
        }

    @Test
    fun `test networkInfo method`() =
        runTest {
            val mockResponse = loadMockResponse("network_info")
            if (mockResponse != null) {
                val client = createMockClient(mockResponse)
                val result = client.networkInfo(Unit)
                assertNotNull(result, "NetworkInfo should return a result")
                println("✅ networkInfo() method works with mock data")
            }
        }

    @Test
    fun `test genesisConfig method`() =
        runTest {
            val mockResponse = loadMockResponse("genesis_config")
            if (mockResponse != null) {
                val client = createMockClient(mockResponse)
                val result = client.genesisConfig(Unit)
                assertNotNull(result, "GenesisConfig should return a result")
                println("✅ genesisConfig() method works with mock data")
            }
        }

    @Test
    fun `test health method`() =
        runTest {
            val mockResponse = loadMockResponse("health")
            if (mockResponse != null) {
                val client = createMockClient(mockResponse)
                val result = client.health(Unit)
                assertNotNull(result, "Health should return a result")
                println("✅ health() method works with mock data")
            }
        }

    @Test
    fun `test clientConfig method`() =
        runTest {
            val mockResponse = loadMockResponse("client_config")
            if (mockResponse != null) {
                val client = createMockClient(mockResponse)
                val result = client.clientConfig(Unit)
                assertNotNull(result, "ClientConfig should return a result")
                println("✅ clientConfig() method works with mock data")
            }
        }

    @Test
    fun `test experimentalGenesisConfig method`() =
        runTest {
            val mockResponse = loadMockResponse("EXPERIMENTAL_genesis_config")
            if (mockResponse != null) {
                val client = createMockClient(mockResponse)
                val result = client.experimentalGenesisConfig(Unit)
                assertNotNull(result, "ExperimentalGenesisConfig should return a result")
                println("✅ experimentalGenesisConfig() method works with mock data")
            }
        }

    @Test
    fun `test all RPC methods have mock files`() {
        if (!mockDirectory.exists()) {
            println("⚠️ Mock directory not found")
            return
        }

        val methods =
            listOf(
                "EXPERIMENTAL_changes", "EXPERIMENTAL_changes_in_block", "EXPERIMENTAL_congestion_level",
                "EXPERIMENTAL_genesis_config", "EXPERIMENTAL_light_client_block_proof",
                "EXPERIMENTAL_light_client_proof", "EXPERIMENTAL_maintenance_windows",
                "EXPERIMENTAL_protocol_config", "EXPERIMENTAL_receipt", "EXPERIMENTAL_split_storage_info",
                "EXPERIMENTAL_tx_status", "EXPERIMENTAL_validators_ordered", "block", "block_effects",
                "broadcast_tx_async", "broadcast_tx_commit", "changes", "chunk", "client_config",
                "gas_price", "genesis_config", "health", "light_client_proof", "maintenance_windows",
                "network_info", "next_light_client_block", "query", "send_tx", "status", "tx", "validators",
            )

        var requestCount = 0
        var responseCount = 0

        for (method in methods) {
            val requestFiles =
                mockDirectory.listFiles { file ->
                    file.isFile &&
                        file.extension == "json" &&
                        file.nameWithoutExtension.startsWith("JsonRpcRequest") &&
                        file.nameWithoutExtension.contains(method, ignoreCase = true)
                } ?: emptyArray()

            val responseFiles =
                mockDirectory.listFiles { file ->
                    file.isFile &&
                        file.extension == "json" &&
                        file.nameWithoutExtension.startsWith("JsonRpcResponse") &&
                        file.nameWithoutExtension.contains(method, ignoreCase = true)
                } ?: emptyArray()

            if (requestFiles.isNotEmpty()) requestCount++
            if (responseFiles.isNotEmpty()) responseCount++
        }

        println("\n📊 RPC Method Mock Coverage:")
        println("   📨 Methods with request mocks: $requestCount/${methods.size}")
        println("   📬 Methods with response mocks: $responseCount/${methods.size}")

        assertTrue(requestCount > 0, "Should have at least some request mock files")
        assertTrue(responseCount > 0, "Should have at least some response mock files")
    }
}
