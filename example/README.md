# NEAR JSON-RPC Kotlin Client - Example

This example demonstrates how to use the NEAR JSON-RPC Kotlin client to interact with NEAR Protocol RPC endpoints.

## Overview

The example showcases all available RPC methods with real API calls to the NEAR testnet archival RPC endpoint. It demonstrates:

- Initializing the client
- Making type-safe RPC calls
- Handling responses and errors
- Pretty-printing JSON responses

## Running the Example

From the project root directory:

```bash
./gradlew :example:run
```

Or from the example directory:

```bash
cd example
../gradlew run
```

## What It Does

The example calls all 31 RPC methods available in the NEAR JSON-RPC API:

1. **EXPERIMENTAL_changes** - Query state changes
2. **EXPERIMENTAL_changes_in_block** - Query changes in a block
3. **EXPERIMENTAL_congestion_level** - Check shard congestion
4. **EXPERIMENTAL_genesis_config** - Get genesis configuration
5. **EXPERIMENTAL_maintenance_windows** - Query maintenance windows
6. **EXPERIMENTAL_receipt** - Fetch receipt by ID
7. **EXPERIMENTAL_split_storage_info** - Get split storage info
8. **EXPERIMENTAL_tx_status** - Query transaction status
9. **EXPERIMENTAL_validators_ordered** - Get ordered validators
10. **block** - Fetch block information
11. **changes** - Query state changes
12. **chunk** - Fetch chunk details
13. **gas_price** - Get current gas price
14. **health** - Check node health
15. **network_info** - Get network information
16. **next_light_client_block** - Get next light client block
17. **query** - Query account, code, or call functions
18. **status** - Get node status
19. **tx** - Query transaction details
20. **validators** - Get validator information

## Code Structure

```kotlin
// Initialize the client
val client = NearRpcClient.default("https://archival-rpc.testnet.fastnear.com")

// Make a type-safe RPC call
val request = RpcBlockRequest.Finality(finality = Finality.FINAL)
val response = client.block(request)

// Response is strongly typed
println("Block height: ${response.header.height}")
```

## Key Features

- **Type Safety**: All requests and responses are strongly typed
- **Error Handling**: Exceptions are thrown for RPC errors
- **Coroutines**: All methods are suspend functions for async/await
- **Serialization**: Automatic JSON serialization/deserialization
- **Pretty Printing**: JSON responses are formatted for readability

## Customization

You can modify the example to:

- Use different RPC endpoints (mainnet, testnet, custom)
- Test specific methods
- Add your own request parameters
- Integrate into your application

## Example Output

```
🚀 NEAR JSON-RPC Client - Complete Method Examples
===================================================

================================================================================
1. EXPERIMENTAL_changes
================================================================================

✓ Response:
{
  "blockHash": "...",
  "changes": [...]
}

📊 TEST SUMMARY
================================================================================
✅ Successful: 20
❌ Failed: 0
📈 Total: 20
```

## Dependencies

The example uses:

- **near-jsonrpc-types**: Generated type definitions
- **near-jsonrpc-client**: RPC client and methods
- **Ktor**: HTTP client
- **kotlinx-serialization**: JSON serialization
- **kotlinx-coroutines**: Async/await support
