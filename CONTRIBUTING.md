# Contributing to NEAR JSON-RPC Kotlin Client

Thank you for your interest in contributing to the NEAR JSON-RPC Kotlin Client! This document provides guidelines and instructions for contributing to the project.

## Getting Started

### Prerequisites

- Java 21 or later
- Kotlin 1.9.23+ (bundled with Gradle)
- Python 3.8+ (for code generation)
- Git
- A GitHub account
- IntelliJ IDEA (recommended) or any Kotlin-compatible IDE

### Setting Up Your Development Environment

1. **Fork the Repository**

   ```bash
   # Visit https://github.com/space-rock/near-jsonrpc-kotlin
   # Click the "Fork" button
   ```

2. **Clone Your Fork**

   ```bash
   git clone https://github.com/YOUR_USERNAME/near-jsonrpc-kotlin.git
   cd near-jsonrpc-kotlin
   ```

3. **Add Upstream Remote**

   ```bash
   git remote add upstream https://github.com/space-rock/near-jsonrpc-kotlin.git
   ```

4. **Setup Python Environment** (for code generation)

   ```bash
   cd scripts
   ./setup.sh
   cd ..
   ```

5. **Build the Project**

   ```bash
   ./gradlew build
   ```

6. **Run Tests**
   ```bash
   ./gradlew test
   ```

## Code Generation

This project uses Python scripts to generate Kotlin code from the NEAR OpenAPI specification.

### When to Regenerate Code

Regenerate code when:
- Updating the OpenAPI specification
- Modifying generation scripts
- Adding support for new RPC methods

### How to Regenerate

```bash
# Setup Python environment (first time only)
cd scripts
./setup.sh

# Run full code generation
./codegen.sh

# Or run individual generators
python3 generate_types.py    # Generate Kotlin types and methods
python3 generate_mock.py      # Generate mock JSON data
python3 generate_tests.py     # Generate test files

# Return to root and format
cd ..
./gradlew ktlintFormat
```

### Updating OpenAPI Spec

```bash
# Download latest specification
curl -L -o scripts/openapi.json https://raw.githubusercontent.com/near/near-jsonrpc-client-rs/master/openapi.json

# Regenerate all code
cd scripts
./codegen.sh
cd ..

# Review changes
git diff
```

## Development Workflow

### 1. Start with an Issue

- Check existing issues or create a new one
- Discuss your proposed changes
- Wait for maintainer approval before starting major work

### 2. Create a Feature Branch

```bash
# Update your local main branch
git checkout main
git pull upstream main

# Create a new feature branch
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

### 3. Make Your Changes

Follow these guidelines:

- Write clean, readable Kotlin code
- Follow existing code style
- Add/update tests as needed
- Update documentation
- Keep commits focused and atomic

### 4. Run Quality Checks

Before committing:

```bash
# Format code
./gradlew ktlintFormat

# Run tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Build package
./gradlew build

# Check code style
./gradlew ktlintCheck
```

### 5. Commit Your Changes

Follow [Conventional Commits](#commit-conventions) format.

### 6. Push and Create PR

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub.

## Commit Conventions

We use [Conventional Commits](https://www.conventionalcommits.org/) for clear and automated release management.

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring without feature changes
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `build`: Build system or dependency changes
- `ci`: CI/CD configuration changes
- `chore`: Other changes that don't modify src or test files

### Scopes

- `client`: Changes to near-jsonrpc-client module
- `types`: Changes to near-jsonrpc-types module
- `codegen`: Changes to code generation scripts
- `tests`: Test-related changes
- `examples`: Example code changes
- `gradle`: Gradle build configuration changes

### Examples

```bash
# Feature
feat(client): add request timeout configuration

# Bug fix
fix(types): correct serialization for AccountView

# Documentation
docs(client): add usage examples for query method

# Code generation
chore(codegen): update OpenAPI specification to latest

# Breaking change (note the !)
feat(client)!: change client initialization API

BREAKING CHANGE: NearRpcClient.default() renamed to NearRpcClient.create()
```

## Pull Request Process

### Before Submitting

1. **Ensure all tests pass**

   ```bash
   ./gradlew test
   ```

2. **Format code**

   ```bash
   ./gradlew ktlintFormat
   ```

3. **Build package**

   ```bash
   ./gradlew build
   ```

4. **Update documentation**
   - Update relevant READMEs
   - Add KDoc comments for public APIs
   - Update examples if needed

### PR Title Format

Use the same format as commit messages:

- `feat(client): add retry configuration`
- `fix(types): correct validation schema`
- `docs: improve contribution guidelines`

### PR Description Template

```markdown
## Description

Brief description of changes

## Type of Change

- [ ] Bug fix (non-breaking change)
- [ ] New feature (non-breaking change)
- [ ] Breaking change
- [ ] Documentation update

## Testing

- [ ] All tests pass
- [ ] Added new tests
- [ ] Coverage maintained/improved

## Checklist

- [ ] Follows code style (ktlint)
- [ ] Self-reviewed code
- [ ] Updated documentation
- [ ] No debug print statements
- [ ] Follows conventional commit format
```

### Review Process

1. **Automated CI/CD checks must pass**:
   - Linux build
   - All tests pass
   - Code generation succeeds
   - ktlint checks pass
   - Coverage reports generated
2. At least one maintainer review required
3. Address review feedback
4. Maintainer merges when approved

## Testing Requirements

### Test Coverage

- Maintain high test coverage (target: 70%+)
- All new code must include tests
- Tests should cover:
  - Happy path
  - Error cases
  - Edge cases
  - Serialization/deserialization

### Test Structure

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.near.jsonrpc.client.*
import org.near.jsonrpc.types.*

class ClientTest {
    private lateinit var client: NearRpcClient

    @BeforeEach
    fun setUp() {
        client = NearRpcClient.default("https://rpc.testnet.near.org")
    }

    @Test
    fun `test block query`() = runTest {
        // Arrange
        val request = RpcBlockRequest.FinalityRequest(finality = Finality.FINAL)

        // Act
        val response = client.block(request)

        // Assert
        assertNotNull(response.header.hash)
        assert(response.header.height > 0)
    }

    @Test
    fun `test invalid URL throws exception`() {
        // Assert
        assertThrows<IllegalArgumentException> {
            NearRpcClient.default("invalid-url")
        }
    }
}
```

### Mock Data Testing

```kotlin
// Serialization tests use mock JSON files
@Test
fun `test AccountView deserialization`() {
    val jsonData = loadMockData("AccountView.json")
    val json = Json { ignoreUnknownKeys = true }
    
    val accountView = json.decodeFromString<AccountView>(jsonData)
    assertNotNull(accountView.amount)
}

private fun loadMockData(filename: String): String {
    return javaClass.classLoader
        .getResourceAsStream("mock/$filename")
        ?.bufferedReader()
        ?.readText()
        ?: throw IllegalStateException("Mock file not found: $filename")
}
```

## Code Style

### Kotlin Style Guidelines

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Prefer immutability (val over var)
- Use data classes for DTOs
- Use sealed classes for discriminated unions

### Formatting

We use [ktlint](https://github.com/pinterest/ktlint) for code formatting:

```bash
# Check code style
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat

# Format specific module
./gradlew :client:ktlintFormat
./gradlew :types:ktlintFormat
```

### Naming Conventions

- **Classes/Interfaces**: PascalCase (e.g., `RpcBlockRequest`, `AccountView`)
- **Functions/Methods**: camelCase (e.g., `performRequest`, `block`)
- **Variables**: camelCase (e.g., `blockHeight`, `accountId`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRIES`, `DEFAULT_TIMEOUT`)
- **Packages**: lowercase (e.g., `org.near.jsonrpc.client`)

### KDoc Comments

Document public APIs with KDoc:

```kotlin
/**
 * Executes a JSON-RPC block query.
 *
 * @param request The block request parameters
 * @return The block response containing header and chunks
 * @throws JsonRpcException if the RPC call fails
 */
suspend fun block(request: RpcBlockRequest): RpcBlockResponse {
    // Implementation
}
```

## Community

### Getting Help

- 📖 Read the documentation first
- 🔍 Search existing issues
- 💬 Ask in discussions
- 🐛 File detailed bug reports

### Ways to Contribute

- 🐛 Report bugs
- 💡 Suggest features
- 📖 Improve documentation
- 🔧 Submit pull requests
- 👥 Help others in discussions
- ⭐ Star the repository
- 🧪 Improve test coverage
- 📝 Update code generation scripts
- 🔄 Update dependencies

### Recognition

Contributors are recognized in:

- GitHub contributors page
- Release notes
- Special mentions for significant contributions

## Module-Specific Guidelines

### Types Module (`types`)

- All types must be serializable with kotlinx.serialization
- Use `@Serializable` annotation
- Use sealed classes for discriminated unions
- Include proper `@SerialName` annotations
- Test serialization/deserialization

Example:
```kotlin
@Serializable
sealed class RpcBlockRequest {
    @Serializable
    @SerialName("finality")
    data class FinalityRequest(
        val finality: Finality
    ) : RpcBlockRequest()

    @Serializable
    @SerialName("block_id")
    data class BlockIdRequest(
        @SerialName("block_id")
        val blockId: BlockId
    ) : RpcBlockRequest()
}
```

### Client Module (`client`)

- All RPC methods must be suspend functions
- Use proper error handling with JsonRpcException
- Include comprehensive tests
- Document all public APIs

Example:
```kotlin
/**
 * Query the NEAR blockchain.
 */
suspend fun query(request: RpcQueryRequest): RpcQueryResponse {
    return call(
        method = "query",
        params = request,
        paramsSerializer = RpcQueryRequest.serializer(),
        resultSerializer = RpcQueryResponse.serializer()
    )
}
```

### Example Module (`example`)

- Keep examples simple and focused
- Include comments explaining key concepts
- Ensure examples run successfully
- Update when APIs change

## Pre-Commit Checklist

Before submitting your PR, ensure:

- [ ] All tests pass (`./gradlew test`)
- [ ] Code is formatted (`./gradlew ktlintFormat`)
- [ ] No compiler warnings
- [ ] Documentation is updated
- [ ] Examples work correctly (`./gradlew :example:run`)
- [ ] Commit messages follow conventions
- [ ] Generated code is up to date (if modified OpenAPI spec)
- [ ] Coverage reports generated (`./gradlew jacocoTestReport`)

## Dependency Management

### Adding Dependencies

When adding new dependencies:

1. Use specific versions (avoid `+` or ranges)
2. Document why the dependency is needed
3. Check for security vulnerabilities
4. Prefer well-maintained libraries
5. Update both implementation and test dependencies

Example:
```kotlin
dependencies {
    // HTTP client - required for RPC calls
    implementation("io.ktor:ktor-client-core:2.3.8")
    
    // JSON serialization - required for request/response handling
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
```

### Updating Dependencies

```bash
# Check for dependency updates
./gradlew dependencyUpdates

# Update specific dependency in build.gradle.kts
# Run tests to ensure compatibility
./gradlew test

# Update documentation if API changes
```

## CI/CD Integration

### GitHub Actions

The project uses GitHub Actions for CI/CD:

- **CI/CD Workflow**: Runs on every push and PR
  - Builds the project
  - Runs all tests
  - Generates coverage reports
  - Validates code formatting

- **Code Generation Workflow**: Updates OpenAPI spec
  - Can be triggered manually
  - Regenerates code if spec changes

### Local CI Simulation

```bash
# Run the same checks as CI
./gradlew clean build test jacocoTestReport ktlintCheck

# Or use the check-ci script (if available)
./check-ci.sh
```

## Questions?

If you have questions about contributing:

1. Check this guide, [SETUP.md](./SETUP.md), and [README.md](./README.md)
2. Search existing issues/discussions
3. Open a new discussion
4. Contact maintainers

Thank you for contributing to the NEAR JSON-RPC Kotlin Client! 🚀
