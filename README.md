# NEAR JSON-RPC Kotlin Client

[![Kotlin 1.9+](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)
[![CI/CD](https://github.com/space-rock/near-jsonrpc-kotlin/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/space-rock/near-jsonrpc-kotlin/actions/workflows/ci-cd.yml)
[![Coverage](https://codecov.io/gh/space-rock/near-jsonrpc-kotlin/branch/main/graph/badge.svg)](https://codecov.io/gh/space-rock/near-jsonrpc-kotlin)
[![JitPack](https://jitpack.io/v/space-rock/near-jsonrpc-kotlin.svg)](https://jitpack.io/#space-rock/near-jsonrpc-kotlin)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 🚀 Features

- **🎯 Type-Safe Design**: Kotlin types generated directly from OpenAPI specification
- **⚡ Native Kotlin Performance**: Built with Kotlin coroutines for async operations
- **🛡️ Comprehensive Type System**: Kotlinx.serialization for all RPC requests and responses
- **🔀 Discriminated Union Types**: Sealed classes for complex requests like `query` and `changes`
- **📦 Minimal Dependencies**: Ktor client and kotlinx.serialization only
- **🧪 Extensive Test Coverage**: Comprehensive test suites with mock data and Jacoco coverage
- **🔄 Auto-Generated Code**: Types, methods, and tests generated from NEAR's OpenAPI spec
- **📱 Multi-Platform Ready**: JVM-based with potential for Kotlin Multiplatform
- **🎭 Mock Data Generation**: Python-based mock JSON generation for testing

## Overview

A type-safe, high-performance Kotlin library for interacting with NEAR Protocol JSON-RPC API. Built with Kotlin coroutines and automatically generated from the official OpenAPI specification.

## Modules

| Module                  | Description                                     |
| ----------------------- | ----------------------------------------------- |
| `near-jsonrpc-client`   | JSON-RPC client with all RPC method wrappers    |
| `near-jsonrpc-types`    | Kotlin serializable types for requests/responses|

## Quick Start

### Gradle (Kotlin DSL)

Add JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.space-rock.near-jsonrpc-kotlin:client:0.1.0-SNAPSHOT")
    implementation("com.github.space-rock.near-jsonrpc-kotlin:types:0.1.0-SNAPSHOT")
}
```

### Gradle (Groovy)

Add to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.space-rock.near-jsonrpc-kotlin:client:0.1.0-SNAPSHOT'
    implementation 'com.github.space-rock.near-jsonrpc-kotlin:types:0.1.0-SNAPSHOT'
}
```

### Maven

Add JitPack repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add dependencies:

```xml
<dependency>
    <groupId>com.github.space-rock.near-jsonrpc-kotlin</groupId>
    <artifactId>client</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.github.space-rock.near-jsonrpc-kotlin</groupId>
    <artifactId>types</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Basic Usage

```kotlin
import kotlinx.coroutines.runBlocking
import org.near.jsonrpc.client.*
import org.near.jsonrpc.types.*

fun main() = runBlocking {
    // Initialize client
    val client = NearRpcClient.default("https://rpc.testnet.near.org")

    // Query block information
    val blockRequest = RpcBlockRequest.FinalityRequest(finality = Finality.FINAL)
    val blockResponse = client.block(blockRequest)
    println("Block hash: ${blockResponse.header.hash}")

    // Query account details
    val queryRequest = RpcQueryRequest.ViewAccountByFinality(
        finality = Finality.FINAL,
        accountId = "example.testnet",
        requestType = "view_account"
    )
    val queryResponse = client.query(queryRequest)

    // Check node health
    val healthResponse = client.health(RpcHealthRequest)
    println("Node is healthy: ${healthResponse == null}")
}
```

## Installation

### Prerequisites

- Kotlin 1.9.23 or later
- Java 21 or later
- Gradle 8.0+ (included via wrapper)
- Python 3.8+ (for code generation)

### Development Setup

```bash
# Clone the repository
git clone https://github.com/space-rock/near-jsonrpc-kotlin.git
cd near-jsonrpc-kotlin

# Setup Python environment for code generation
cd scripts
./setup.sh
cd ..

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport
```

## Code Generation

The project uses Python scripts to generate Kotlin code from NEAR's OpenAPI specification:

### Generation Pipeline

1. **Types & Methods** (`scripts/generate_types.py`)
   - Generates `types/src/main/kotlin/org/near/jsonrpc/types/Types.kt` with all serializable types
   - Generates `client/src/main/kotlin/org/near/jsonrpc/client/Methods.kt` with RPC method wrappers
   - Parses OpenAPI spec and creates Kotlin sealed classes, data classes, and enums

2. **Mock Data** (`scripts/generate_mock.py`)
   - Generates JSON mock files in `*/src/test/resources/mock/` directories
   - Creates valid test data for all type structures

3. **Test Suites** (`scripts/generate_tests.py`)
   - Generates comprehensive unit tests
   - Creates serialization/deserialization tests for all types
   - Ensures type safety across the entire API surface

### Running Code Generation

```bash
# Generate all code (types, mocks, and tests)
cd scripts
./codegen.sh

# Or run individual generators
python3 generate_types.py    # Generate Kotlin types and methods
python3 generate_mock.py      # Generate mock JSON data
python3 generate_tests.py     # Generate test files
```

### Updating OpenAPI Specification

```bash
# Download the latest OpenAPI spec
curl -L -o scripts/openapi.json https://raw.githubusercontent.com/near/near-jsonrpc-client-rs/master/openapi.json

# Regenerate all code
cd scripts
./codegen.sh
```

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with verbose output
./gradlew test --info

# Run specific test suite
./gradlew :client:test
./gradlew :types:test

# Generate coverage report
./gradlew test jacocoTestReport
```

### Test Structure

- **Unit Tests**: Test individual components and methods
- **Integration Tests**: Test actual RPC calls (when enabled)
- **Mock Validation Tests**: Verify all types serialize/deserialize correctly from mock JSON
- **Mock Data**: Comprehensive JSON fixtures for all response types

### Coverage Reports

After running tests with coverage, reports are available at:
- `client/build/reports/jacoco/test/html/index.html`
- `types/build/reports/jacoco/test/html/index.html`

## Examples

See [`example/src/main/kotlin/org/near/jsonrpc/example/NearJsonRpcExample.kt`](./example/src/main/kotlin/org/near/jsonrpc/example/NearJsonRpcExample.kt) for a comprehensive demonstration of all RPC methods.

```bash
# Run the example
./gradlew :example:run
```

## Error Handling

The client provides structured error handling:

```kotlin
import org.near.jsonrpc.client.JsonRpcException

try {
    val response = client.block(request)
    // Handle success
} catch (e: JsonRpcException) {
    println("RPC error: ${e.message}")
    println("Error code: ${e.code}")
    println("Error data: ${e.data}")
} catch (e: Exception) {
    println("Unknown error: ${e.message}")
}
```

## Architecture

### Design Principles

1. **Type-Safe Kotlin**
   - Generated from OpenAPI spec
   - Comprehensive kotlinx.serialization support
   - Sealed classes for discriminated unions

2. **Modern Coroutines**
   - Suspend functions for all network calls
   - Structured concurrency support
   - Non-blocking I/O with Ktor

3. **Minimal Dependencies**
   - Ktor client for HTTP
   - kotlinx.serialization for JSON
   - No external runtime dependencies

4. **Generated Code**
   - Types auto-generated from spec
   - Methods auto-generated with proper signatures
   - Tests auto-generated for coverage

## Available RPC Methods

The client supports all NEAR JSON-RPC methods:

### Block / Chunk Methods
- `block` - Get block details
- `chunk` - Get chunk details

### Account / Contract Methods
- `query` - Query account, contract state, or call functions
- `changes` - Get state changes

### Transaction Methods
- `tx` - Get transaction status
- `EXPERIMENTAL_tx_status` - Get detailed transaction status

### Network Methods
- `status` - Get node status
- `health` - Check node health
- `network_info` - Get network information
- `gas_price` - Get current gas price

### Validator Methods
- `validators` - Get validator information
- `EXPERIMENTAL_validators_ordered` - Get ordered validator list

### Light Client Methods
- `next_light_client_block` - Get next light client block

### Experimental Methods
- `EXPERIMENTAL_changes` - Get state changes (experimental)
- `EXPERIMENTAL_changes_in_block` - Get changes in a specific block
- `EXPERIMENTAL_genesis_config` - Get genesis configuration
- `EXPERIMENTAL_receipt` - Get receipt information
- `EXPERIMENTAL_congestion_level` - Get network congestion level
- `EXPERIMENTAL_maintenance_windows` - Get maintenance windows
- `EXPERIMENTAL_split_storage_info` - Get split storage information

## Code Style

The project uses ktlint for code formatting:

```bash
# Check code style
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat
```

## Contributing

Contributions are welcome! Please ensure:

1. All tests pass (`./gradlew test`)
2. Code is formatted with ktlint (`./gradlew ktlintFormat`)
3. New features include tests
4. Generated code is updated if needed

```bash
# Before submitting a PR
./gradlew test                    # Run all tests
./gradlew ktlintFormat            # Format code
cd scripts && ./codegen.sh        # Regenerate if needed
```

## License

Apache License 2.0 - see [LICENSE](./LICENSE) for details.

## Links

- [NEAR Protocol](https://near.org)
- [NEAR RPC Documentation](https://docs.near.org/api/rpc/introduction)
- [OpenAPI Specification](https://github.com/near/near-jsonrpc-client-rs/blob/master/openapi.json)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Ktor Client](https://ktor.io/docs/client.html)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)

---

Built with ❤️ for the NEAR Kotlin community
