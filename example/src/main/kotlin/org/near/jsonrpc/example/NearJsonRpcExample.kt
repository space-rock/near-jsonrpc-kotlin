package org.near.jsonrpc.example

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.near.jsonrpc.client.*
import org.near.jsonrpc.types.*
import java.util.Base64

/**
 * NEAR JSON-RPC Client usage examples demonstrating all available RPC methods.
 */
fun main() =
    runBlocking {
        demonstrateAllMethods()
    }

fun prettyPrint(
    value: Any,
    label: String,
) {
    val json = Json { prettyPrint = true }
    println("\n$label:")
    when (value) {
        is String -> println(value)
        else -> {
            try {
                println(value.toString())
            } catch (e: Exception) {
                println(value.toString())
            }
        }
    }
}

suspend fun demonstrateAllMethods() {
    var successCount = 0
    var failureCount = 0
    val failures = mutableListOf<Pair<String, String>>()

    try {
        val client = NearRpcClient.default("https://archival-rpc.testnet.fastnear.com")

        println("🚀 NEAR JSON-RPC Client - Method Examples")
        println("==========================================\n")

        println("\n" + "=".repeat(80))
        println("1. EXPERIMENTAL_changes")
        println("=".repeat(80))
        try {
            val request =
                RpcStateChangesInBlockByTypeRequest.AccountChangesByBlockId(
                    blockId = BlockId.IntegerValue(216_862_270),
                    accountIds = listOf("relay.aurora"),
                    changesType = "account_changes",
                )
            val response = client.experimentalChanges(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_changes" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("2. EXPERIMENTAL_changes_in_block")
        println("=".repeat(80))
        try {
            val request =
                RpcStateChangesInBlockRequest.BlockIdRequest(
                    blockId = BlockId.IntegerValue(216_910_612),
                )
            val response = client.experimentalChangesInBlock(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_changes_in_block" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("3. EXPERIMENTAL_congestion_level")
        println("=".repeat(80))
        try {
            val request =
                RpcCongestionLevelRequest.Variant0(
                    blockId = org.near.jsonrpc.types.BlockId.IntegerValue(216_838_942),
                    shardId = 1,
                )
            val response = client.experimentalCongestionLevel(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_congestion_level" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("4. EXPERIMENTAL_genesis_config")
        println("=".repeat(80))
        try {
            val response = client.experimentalGenesisConfig(GenesisConfigRequest)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_genesis_config" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("5. EXPERIMENTAL_maintenance_windows")
        println("=".repeat(80))
        try {
            val request = RpcMaintenanceWindowsRequest(accountId = "node0")
            val response = client.experimentalMaintenanceWindows(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_maintenance_windows" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("6. EXPERIMENTAL_receipt")
        println("=".repeat(80))
        try {
            val request = RpcReceiptRequest(receiptId = "3zYdPjRrhAbGCnZv7aypJ7JCAn1Vj3JSQ4RKZueV73kv")
            val response = client.experimentalReceipt(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_receipt" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("7. EXPERIMENTAL_split_storage_info")
        println("=".repeat(80))
        try {
            val response = client.experimentalSplitStorageInfo(RpcSplitStorageInfoRequest)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_split_storage_info" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("8. EXPERIMENTAL_tx_status")
        println("=".repeat(80))
        try {
            val request =
                RpcTransactionStatusRequest.Variant1(
                    senderAccountId = "relay.aurora",
                    txHash = "FB5d5Ehn7Q4Bx8XwrWV19jtTDJsTvaR7YnPgCkbSKPRP",
                )
            val response = client.experimentalTxStatus(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_tx_status" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("9. EXPERIMENTAL_validators_ordered")
        println("=".repeat(80))
        try {
            val request = RpcValidatorsOrderedRequest(blockId = BlockId.IntegerValue(216_910_612))
            val response = client.experimentalValidatorsOrdered(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("EXPERIMENTAL_validators_ordered" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("10. block")
        println("=".repeat(80))
        try {
            val request = RpcBlockRequest.FinalityRequest(finality = Finality.FINAL)
            val response = client.block(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("block" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("11. changes")
        println("=".repeat(80))
        try {
            val request =
                RpcStateChangesInBlockByTypeRequest.AccountChangesByBlockId(
                    blockId = BlockId.IntegerValue(216_910_612),
                    accountIds = listOf("aurora.pool.f863973.m0"),
                    changesType = "account_changes",
                )
            val response = client.changes(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("changes" to (e.message ?: "Unknown error"))
        }

        // chunk

        println("\n" + "=".repeat(80))
        println("12. chunk")
        println("=".repeat(80))
        try {
            // Get a chunk hash from the latest block
            val blockRequest = RpcBlockRequest.FinalityRequest(finality = Finality.FINAL)
            val block = client.block(blockRequest)

            val chunkHash = block.chunks.firstOrNull()?.chunkHash
            if (chunkHash != null) {
                val request = RpcChunkRequest.ChunkId(chunkId = chunkHash)
                val response = client.chunk(request)
                prettyPrint(response, "✓ Response")
                successCount++
            } else {
                prettyPrint(mapOf("error" to "No chunks in block"), "✗ Error")
                failureCount++
                failures.add("chunk" to "No chunks in block")
            }
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("chunk" to (e.message ?: "Unknown error"))
        }

        // gas_price

        println("\n" + "=".repeat(80))
        println("13. gas_price")
        println("=".repeat(80))
        try {
            val request = RpcGasPriceRequest(blockId = null)
            val response = client.gasPrice(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("gas_price" to (e.message ?: "Unknown error"))
        }

        // health

        println("\n" + "=".repeat(80))
        println("14. health")
        println("=".repeat(80))
        try {
            val response = client.health(RpcHealthRequest)
            if (response != null) {
                prettyPrint(response, "✓ Response")
            } else {
                println("✓ Response: Node is healthy (null response)")
            }
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("health" to (e.message ?: "Unknown error"))
        }

        // network_info

        println("\n" + "=".repeat(80))
        println("15. network_info")
        println("=".repeat(80))
        try {
            val response = client.networkInfo(RpcNetworkInfoRequest)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("network_info" to (e.message ?: "Unknown error"))
        }

        // next_light_client_block

        println("\n" + "=".repeat(80))
        println("16. next_light_client_block")
        println("=".repeat(80))
        try {
            val blockRequest = RpcBlockRequest.FinalityRequest(finality = Finality.FINAL)
            val block = client.block(blockRequest)

            val request = RpcLightClientNextBlockRequest(lastBlockHash = block.header.hash)
            val response = client.nextLightClientBlock(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("next_light_client_block" to (e.message ?: "Unknown error"))
        }

        // query (view_account)

        println("\n" + "=".repeat(80))
        println("17. view_account")
        println("=".repeat(80))
        try {
            val request =
                RpcQueryRequest.ViewAccountByFinality(
                    finality = Finality.FINAL,
                    accountId = "guestbook.near-examples.testnet",
                    requestType = "view_account",
                )
            val response = client.query(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("query (view_account)" to (e.message ?: "Unknown error"))
        }

        // query (view_code)

        println("\n" + "=".repeat(80))
        println("17. view_code")
        println("=".repeat(80))
        try {
            val request =
                RpcQueryRequest.ViewCodeByBlockId(
                    blockId = BlockId.IntegerValue(217_371_061),
                    accountId = "guestbook.near-examples.testnet",
                    requestType = "view_code",
                )
            val response = client.query(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("query (view_code)" to (e.message ?: "Unknown error"))
        }

        // query (call_function)

        println("\n" + "=".repeat(80))
        println("17. call_function")
        println("=".repeat(80))
        try {
            val json = Json
            val args = mapOf("account_id" to "zavodil2.testnet")
            val jsonData = json.encodeToString(args)
            val argsBase64 = Base64.getEncoder().encodeToString(jsonData.toByteArray())

            val request =
                RpcQueryRequest.CallFunctionBySyncCheckpoint(
                    syncCheckpoint = SyncCheckpoint.EARLIEST_AVAILABLE,
                    accountId = "usdn.testnet",
                    argsBase64 = argsBase64,
                    methodName = "ft_balance_of",
                    requestType = "call_function",
                )
            val response = client.query(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("query (call_function)" to (e.message ?: "Unknown error"))
        }

        // status

        println("\n" + "=".repeat(80))
        println("18. status")
        println("=".repeat(80))
        try {
            val response = client.status(RpcStatusRequest)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("status" to (e.message ?: "Unknown error"))
        }

        // tx

        println("\n" + "=".repeat(80))
        println("19. tx")
        println("=".repeat(80))
        try {
            val request =
                RpcTransactionStatusRequest.Variant1(
                    senderAccountId = "relay.aurora",
                    txHash = "FB5d5Ehn7Q4Bx8XwrWV19jtTDJsTvaR7YnPgCkbSKPRP",
                )
            val response = client.tx(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("tx" to (e.message ?: "Unknown error"))
        }

        // validators

        println("\n" + "=".repeat(80))
        println("20. validators")
        println("=".repeat(80))
        try {
            val request = RpcValidatorRequest.Latest
            val response = client.validators(request)
            prettyPrint(response, "✓ Response")
            successCount++
        } catch (e: Exception) {
            prettyPrint(mapOf("error" to e.message), "✗ Error")
            failureCount++
            failures.add("validators" to (e.message ?: "Unknown error"))
        }

        println("\n" + "=".repeat(80))
        println("📊 TEST SUMMARY")
        println("=".repeat(80))
        println("\n✅ Successful: $successCount")
        println("❌ Failed: $failureCount")
        println("📈 Total: ${successCount + failureCount}")

        if (failures.isNotEmpty()) {
            println("\n" + "-".repeat(80))
            println("Failed Methods:")
            println("-".repeat(80))
            failures.forEachIndexed { index, (method, error) ->
                println("\n${index + 1}. $method")
                println("   Error: $error")
            }
        }

        println("\n" + "=".repeat(80))
    } catch (e: Exception) {
        println("\n❌ Fatal Error: ${e.message}")
        e.printStackTrace()
    }
}
