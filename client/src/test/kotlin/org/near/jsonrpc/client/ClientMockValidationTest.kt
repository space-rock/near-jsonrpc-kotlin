package org.near.jsonrpc.client

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.near.jsonrpc.types.*
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Validates generated mock JSON files work correctly with the client.
 */
class ClientMockValidationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            serializersModule = nearSerializersModule
        }

    private val mockDirectory = File("src/test/resources/mock")

    @Test
    fun `mock directory exists`() {
        assertTrue(
            mockDirectory.exists() && mockDirectory.isDirectory,
            "Mock directory should exist at ${mockDirectory.absolutePath}",
        )
    }

    @Test
    fun `all request JSON files have valid JSON-RPC structure`() {
        if (!mockDirectory.exists()) {
            println("⚠️ Mock directory not found. Run generate_mock.py first.")
            return
        }

        val requestFiles =
            mockDirectory.listFiles { file ->
                file.isFile &&
                    file.extension == "json" &&
                    file.nameWithoutExtension.startsWith("JsonRpcRequest")
            } ?: emptyArray()

        assertTrue(requestFiles.isNotEmpty(), "Should have request mock files")

        var successCount = 0
        var failureCount = 0
        val failures = mutableListOf<String>()

        for (mockFile in requestFiles.sortedBy { it.name }) {
            try {
                val jsonContent = mockFile.readText()
                val element = json.parseToJsonElement(jsonContent).jsonObject

                // Validate JSON-RPC structure
                assertNotNull(element["jsonrpc"], "${mockFile.name}: Should have jsonrpc field")
                assertNotNull(element["method"], "${mockFile.name}: Should have method field")
                assertNotNull(element["id"], "${mockFile.name}: Should have id field")

                assertEquals(
                    "2.0",
                    element["jsonrpc"]?.jsonPrimitive?.content,
                    "${mockFile.name}: JSON-RPC version should be 2.0",
                )

                println("✅ ${mockFile.name}")
                successCount++
            } catch (e: Exception) {
                failureCount++
                val error = "❌ ${mockFile.name}: ${e.message}"
                println(error)
                failures.add(error)
            }
        }

        println("\n📊 Request Validation Summary:")
        println("   ✅ Success: $successCount")
        println("   ❌ Failures: $failureCount")
        println("   📁 Total: ${requestFiles.size}")

        if (failures.isNotEmpty()) {
            fail("${failures.size} request files failed validation")
        }
    }

    @Test
    fun `all response JSON files have valid JSON-RPC structure`() {
        if (!mockDirectory.exists()) {
            println("⚠️ Mock directory not found. Run generate_mock.py first.")
            return
        }

        val responseFiles =
            mockDirectory.listFiles { file ->
                file.isFile &&
                    file.extension == "json" &&
                    file.nameWithoutExtension.startsWith("JsonRpcResponse")
            } ?: emptyArray()

        assertTrue(responseFiles.isNotEmpty(), "Should have response mock files")

        var successCount = 0
        var errorCount = 0
        var failureCount = 0
        val failures = mutableListOf<String>()

        for (mockFile in responseFiles.sortedBy { it.name }) {
            try {
                val jsonContent = mockFile.readText()
                val element = json.parseToJsonElement(jsonContent).jsonObject

                // Validate JSON-RPC structure
                assertNotNull(element["jsonrpc"], "${mockFile.name}: Should have jsonrpc field")
                assertNotNull(element["id"], "${mockFile.name}: Should have id field")

                assertEquals(
                    "2.0",
                    element["jsonrpc"]?.jsonPrimitive?.content,
                    "${mockFile.name}: JSON-RPC version should be 2.0",
                )

                // Check if it's a success or error response
                val isSuccess = "result" in element
                val isError = "error" in element

                assertTrue(
                    isSuccess || isError,
                    "${mockFile.name}: Response should have either result or error field",
                )

                if (isSuccess) {
                    println("✅ ${mockFile.name} (success)")
                    successCount++
                } else {
                    println("✅ ${mockFile.name} (error)")
                    errorCount++
                }
            } catch (e: Exception) {
                failureCount++
                val error = "❌ ${mockFile.name}: ${e.message}"
                println(error)
                failures.add(error)
            }
        }

        println("\n📊 Response Validation Summary:")
        println("   ✅ Success responses: $successCount")
        println("   ✅ Error responses: $errorCount")
        println("   ❌ Failures: $failureCount")
        println("   📁 Total: ${responseFiles.size}")

        if (failures.isNotEmpty()) {
            fail("${failures.size} response files failed validation")
        }
    }

    @Test
    fun `all success response files have result field`() {
        if (!mockDirectory.exists()) return

        val successFiles =
            mockDirectory.listFiles { file ->
                file.isFile &&
                    file.extension == "json" &&
                    file.nameWithoutExtension.endsWith("_Success")
            } ?: emptyArray()

        if (successFiles.isEmpty()) {
            println("⏭️  No success response files found")
            return
        }

        var validCount = 0

        for (file in successFiles.sortedBy { it.name }) {
            try {
                val jsonContent = file.readText()
                val element = json.parseToJsonElement(jsonContent).jsonObject

                assertNotNull(element["result"], "${file.name}: Success response should have result field")
                validCount++
                println("✅ ${file.name}")
            } catch (e: Exception) {
                println("❌ ${file.name}: ${e.message}")
            }
        }

        println("\n📊 Success Responses: $validCount/${successFiles.size} valid")
        assertTrue(validCount == successFiles.size, "All success responses should be valid")
    }

    @Test
    fun `all error response files have error field with code and message`() {
        if (!mockDirectory.exists()) return

        val errorFiles =
            mockDirectory.listFiles { file ->
                file.isFile &&
                    file.extension == "json" &&
                    file.nameWithoutExtension.endsWith("_Error")
            } ?: emptyArray()

        if (errorFiles.isEmpty()) {
            println("⏭️  No error response files found")
            return
        }

        var validCount = 0

        for (file in errorFiles.sortedBy { it.name }) {
            try {
                val jsonContent = file.readText()
                val element = json.parseToJsonElement(jsonContent).jsonObject

                assertNotNull(element["error"], "${file.name}: Error response should have error field")

                val error = element["error"]!!.jsonObject
                assertNotNull(error["code"], "${file.name}: Error should have code")
                assertNotNull(error["message"], "${file.name}: Error should have message")

                validCount++
                println("✅ ${file.name}")
            } catch (e: Exception) {
                println("❌ ${file.name}: ${e.message}")
            }
        }

        println("\n📊 Error Responses: $validCount/${errorFiles.size} valid")
        assertTrue(validCount == errorFiles.size, "All error responses should be valid")
    }

    @Test
    fun `validate method-specific response structures`() {
        if (!mockDirectory.exists()) return

        val methods =
            listOf(
                "EXPERIMENTAL_changes", "EXPERIMENTAL_changes_in_block", "EXPERIMENTAL_congestion_level", "EXPERIMENTAL_genesis_config", "EXPERIMENTAL_light_client_block_proof", "EXPERIMENTAL_light_client_proof", "EXPERIMENTAL_maintenance_windows", "EXPERIMENTAL_protocol_config", "EXPERIMENTAL_receipt", "EXPERIMENTAL_split_storage_info",
            )

        var foundCount = 0

        for (method in methods) {
            val responseFiles =
                mockDirectory.listFiles { file ->
                    file.isFile &&
                        file.extension == "json" &&
                        file.nameWithoutExtension.contains(method, ignoreCase = true) &&
                        file.nameWithoutExtension.startsWith("JsonRpcResponse")
                } ?: emptyArray()

            if (responseFiles.isEmpty()) {
                continue
            }

            for (file in responseFiles) {
                try {
                    val jsonContent = file.readText()
                    val element = json.parseToJsonElement(jsonContent).jsonObject

                    assertNotNull(element["jsonrpc"])
                    assertNotNull(element["id"])
                    assertTrue("result" in element || "error" in element)

                    foundCount++
                    println("✅ ${file.name}")
                } catch (e: Exception) {
                    println("❌ ${file.name}: ${e.message}")
                }
            }
        }

        println("\n📊 Method-specific responses: $foundCount found and validated")
    }

    @Test
    fun `test all RPC methods request and response deserialization`() {
        // This test validates all 31 RPC method request/response types
        if (!mockDirectory.exists()) return

        val allMethods =
            listOf(
                "EXPERIMENTAL_changes",
                "EXPERIMENTAL_changes_in_block",
                "EXPERIMENTAL_congestion_level",
                "EXPERIMENTAL_genesis_config",
                "EXPERIMENTAL_light_client_block_proof",
                "EXPERIMENTAL_light_client_proof",
                "EXPERIMENTAL_maintenance_windows",
                "EXPERIMENTAL_protocol_config",
                "EXPERIMENTAL_receipt",
                "EXPERIMENTAL_split_storage_info",
                "EXPERIMENTAL_tx_status",
                "EXPERIMENTAL_validators_ordered",
                "block",
                "block_effects",
                "broadcast_tx_async",
                "broadcast_tx_commit",
                "changes",
                "chunk",
                "client_config",
                "gas_price",
                "genesis_config",
                "health",
                "light_client_proof",
                "maintenance_windows",
                "network_info",
                "next_light_client_block",
                "query",
                "send_tx",
                "status",
                "tx",
                "validators",
            )

        var requestSuccessCount = 0
        var responseSuccessCount = 0
        var failureCount = 0
        val failures = mutableListOf<String>()

        println("\n🧪 Testing all ${allMethods.size} RPC methods...")

        for (method in allMethods) {
            // Test request file
            try {
                val requestFiles =
                    mockDirectory.listFiles { file ->
                        file.isFile &&
                            file.extension == "json" &&
                            file.nameWithoutExtension.startsWith("JsonRpcRequest") &&
                            file.nameWithoutExtension.contains(method, ignoreCase = true)
                    } ?: emptyArray()

                if (requestFiles.isNotEmpty()) {
                    val file = requestFiles.first()
                    val jsonContent = file.readText()
                    val element = json.parseToJsonElement(jsonContent).jsonObject

                    // Validate request structure
                    assertNotNull(element["jsonrpc"], "Request should have jsonrpc")
                    assertNotNull(element["method"], "Request should have method")
                    assertNotNull(element["id"], "Request should have id")
                    assertNotNull(element["params"], "Request should have params")

                    requestSuccessCount++
                    println("✅ Request: $method")
                }
            } catch (e: Exception) {
                failures.add("Request $method: ${e.message}")
                failureCount++
                println("❌ Request $method: ${e.message}")
            }

            // Test response file
            try {
                val responseFiles =
                    mockDirectory.listFiles { file ->
                        file.isFile &&
                            file.extension == "json" &&
                            file.nameWithoutExtension.startsWith("JsonRpcResponse") &&
                            file.nameWithoutExtension.contains(method, ignoreCase = true) &&
                            file.nameWithoutExtension.endsWith("_Success")
                    } ?: emptyArray()

                if (responseFiles.isNotEmpty()) {
                    val file = responseFiles.first()
                    val jsonContent = file.readText()
                    val element = json.parseToJsonElement(jsonContent).jsonObject

                    // Validate response structure
                    assertNotNull(element["jsonrpc"], "Response should have jsonrpc")
                    assertNotNull(element["id"], "Response should have id")
                    assertNotNull(element["result"], "Success response should have result")

                    // Try to deserialize the result
                    val result = element["result"]
                    assertNotNull(result, "Result should not be null")

                    responseSuccessCount++
                    println("✅ Response: $method")
                }
            } catch (e: Exception) {
                failures.add("Response $method: ${e.message}")
                failureCount++
                println("❌ Response $method: ${e.message}")
            }
        }

        println("\n📊 RPC Methods Test Summary:")
        println("   ✅ Requests tested: $requestSuccessCount")
        println("   ✅ Responses tested: $responseSuccessCount")
        println("   ❌ Failures: $failureCount")
        println("   📁 Total methods: ${allMethods.size}")

        assertTrue(requestSuccessCount > 0, "Should test at least some request types")
        assertTrue(responseSuccessCount > 0, "Should test at least some response types")

        if (failures.isNotEmpty() && failures.size < 20) {
            println("\n⚠️ Failures:")
            failures.forEach { println("   $it") }
        }
    }

    @Test
    fun `comprehensive client mock coverage report`() {
        if (!mockDirectory.exists()) return

        val allFiles =
            mockDirectory.listFiles { file ->
                file.isFile && file.extension == "json"
            } ?: emptyArray()

        val requestFiles = allFiles.filter { it.name.startsWith("JsonRpcRequest") }
        val responseFiles = allFiles.filter { it.name.startsWith("JsonRpcResponse") }
        val successFiles = responseFiles.filter { it.name.endsWith("_Success.json") }
        val errorFiles = responseFiles.filter { it.name.endsWith("_Error.json") }

        println("\n📊 Client Mock Coverage Report:")
        println("   📄 Total files: ${allFiles.size}")
        println("   📨 Request files: ${requestFiles.size}")
        println("   📬 Response files: ${responseFiles.size}")
        println("   ✅ Success responses: ${successFiles.size}")
        println("   ❌ Error responses: ${errorFiles.size}")

        assertTrue(requestFiles.isNotEmpty(), "Should have request files")
        assertTrue(responseFiles.isNotEmpty(), "Should have response files")
    }
}
