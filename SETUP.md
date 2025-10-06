# Setup Guide

This guide will help you set up your development environment for contributing to the NEAR JSON-RPC Kotlin Client.

## System Requirements

### Required Software

- **Java**: 21 or higher

  ```bash
  # Check version
  java -version

  # macOS: Install via Homebrew
  brew install openjdk@21

  # Linux: Install OpenJDK
  sudo apt-get install openjdk-21-jdk

  # Or use SDKMAN
  sdk install java 21-tem
  ```

- **Kotlin**: 1.9.23+ (bundled with Gradle)

  ```bash
  # Check version (after building project)
  ./gradlew --version
  ```

- **Python**: 3.8 or higher (for code generation)

  ```bash
  # Check version
  python3 --version

  # macOS
  brew install python3

  # Linux
  sudo apt-get install python3 python3-pip python3-venv
  ```

- **Git**: Latest version
  ```bash
  # Check version
  git --version
  ```

### Recommended Software

- **IntelliJ IDEA**: For Kotlin development

  ```bash
  # macOS
  brew install --cask intellij-idea-ce

  # Or download from https://www.jetbrains.com/idea/
  ```

- **ktlint**: For code formatting (included in project)

  ```bash
  # Check formatting
  ./gradlew ktlintCheck
  ```

- **GitHub CLI**: For easier PR management

  ```bash
  # macOS
  brew install gh

  # Linux
  sudo apt install gh
  ```

## Environment Setup

### 1. Fork and Clone

```bash
# Fork the repository on GitHub first
# Then clone your fork
git clone https://github.com/YOUR_USERNAME/near-jsonrpc-kotlin.git
cd near-jsonrpc-kotlin

# Add upstream remote
git remote add upstream https://github.com/space-rock/near-jsonrpc-kotlin.git

# Verify remotes
git remote -v
```

### 2. Setup Python Environment

```bash
# Setup Python virtual environment for code generation
cd scripts
./setup.sh
cd ..

# This will:
# - Create Python virtual environment
# - Install required packages (jsonschema)
# - Prepare code generation tools
```

### 3. Initial Build

```bash
# Build the project
./gradlew build

# Build without tests (faster)
./gradlew assemble

# Clean and build
./gradlew clean build
```

### 4. Verify Setup

```bash
# Run tests to verify setup
./gradlew test

# Expected: All tests should pass
```

## Project Structure

### Gradle Multi-Module Setup

The project is organized as a Gradle multi-module project:

- **`:types`**: Core type definitions with kotlinx.serialization
- **`:client`**: RPC client implementation with Ktor
- **`:example`**: Example usage and demonstrations

### Build Configuration

The `build.gradle.kts` defines:

- **Kotlin Version**: 1.9.23
- **Java Target**: 21
- **Plugins**: Kotlin JVM, kotlinx.serialization, ktlint, jacoco
- **Dependencies**: Ktor client, kotlinx.serialization, kotlinx.coroutines

## Development Commands

### Gradle Commands

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :client:build
./gradlew :types:build

# Run tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "org.near.jsonrpc.client.NearRpcClientTest"

# Run specific test method
./gradlew test --tests "org.near.jsonrpc.client.NearRpcClientTest.testBlockQuery"

# Clean build artifacts
./gradlew clean

# List all tasks
./gradlew tasks

# List all projects
./gradlew projects

# Check dependencies
./gradlew dependencies

# Refresh dependencies
./gradlew build --refresh-dependencies
```

### Code Formatting

```bash
# Check code formatting
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat

# Format specific module
./gradlew :client:ktlintFormat
./gradlew :types:ktlintFormat
```

### Running Examples

```bash
# Run the example application
./gradlew :example:run

# Run with specific arguments
./gradlew :example:run --args="arg1 arg2"
```

## Code Generation Setup

### Understanding Code Generation

The project uses Python scripts to generate Kotlin code from NEAR's OpenAPI specification:

1. **`generate_types.py`**: Generates Kotlin types and RPC method wrappers
2. **`generate_mock.py`**: Generates mock JSON test data
3. **`generate_tests.py`**: Generates Kotlin test files

### Initial Setup

```bash
# First time setup
cd scripts
./setup.sh

# This creates a Python virtual environment and installs dependencies
```

### Running Code Generation

```bash
# Generate all code (from scripts directory)
cd scripts
./codegen.sh

# Or run individual generators
python3 generate_types.py    # Generate Types.kt and Methods.kt
python3 generate_mock.py      # Generate Mock/*.json files
python3 generate_tests.py     # Generate test Kotlin files

# Return to root
cd ..

# Format generated code
./gradlew ktlintFormat
```

### Updating OpenAPI Specification

```bash
# Download latest specification
curl -L -o scripts/openapi.json \
  https://raw.githubusercontent.com/near/near-jsonrpc-client-rs/master/openapi.json

# Regenerate all code
cd scripts
./codegen.sh
cd ..

# Review generated changes
git diff types/src/main/kotlin/
git diff client/src/main/kotlin/

# Test the generated code
./gradlew test
```

### Customizing Generation

To modify code generation behavior, edit the Python scripts in `scripts/`:

- **`generate_types.py`**: Customize type mapping, sealed class generation, method signatures
- **`generate_mock.py`**: Customize mock data generation, add special cases
- **`generate_tests.py`**: Customize test generation, add test patterns

## Testing Setup

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with info logging
./gradlew test --info

# Run tests with debug logging
./gradlew test --debug

# Run specific test target
./gradlew :client:test
./gradlew :types:test

# Run specific test class
./gradlew test --tests "org.near.jsonrpc.client.NearRpcClientTest"

# Run specific test method
./gradlew test --tests "org.near.jsonrpc.client.NearRpcClientTest.testBlockQuery"

# Run tests in parallel (default)
./gradlew test --parallel

# Run tests with maximum parallelism
./gradlew test --max-workers=8

# Continue running tests after failure
./gradlew test --continue
```

### Test Coverage

```bash
# Generate coverage reports
./gradlew test jacocoTestReport

# View coverage reports
# Client: open client/build/reports/jacoco/test/html/index.html
# Types: open types/build/reports/jacoco/test/html/index.html

# macOS: Open coverage report in browser
open client/build/reports/jacoco/test/html/index.html

# Linux: Open coverage report
xdg-open client/build/reports/jacoco/test/html/index.html
```

### Test Structure

The project has multiple test types:

1. **Unit Tests**: Tests for individual components
   - `NearRpcClientTest`: Client logic tests
   - `JsonRpcModelsTest`: Type serialization tests

2. **Integration Tests**: Tests for actual RPC calls
   - `NearRpcClientIntegrationTest`: Live RPC tests
   - `MethodsIntegrationTest`: Method-specific integration tests

3. **Mock Validation Tests**: Tests for mock data
   - `ClientMockValidationTest`: Client mock validation
   - `TypesMockValidationTest`: Type mock validation

## IntelliJ IDEA Setup

### Import Project

1. Open IntelliJ IDEA
2. File → Open
3. Select the `near-jsonrpc-kotlin` directory
4. IntelliJ will automatically detect it as a Gradle project
5. Wait for Gradle sync to complete

### Recommended Settings

1. **Kotlin Plugin**: Ensure Kotlin plugin is enabled
2. **Code Style**: 
   - File → Settings → Editor → Code Style → Kotlin
   - Set from: Project (uses .editorconfig)
3. **Gradle Settings**:
   - File → Settings → Build, Execution, Deployment → Build Tools → Gradle
   - Build and run using: Gradle
   - Run tests using: Gradle

### Running Tests in IntelliJ

- Right-click on test class → Run
- Right-click on test method → Run
- Use the green play button in the gutter
- View coverage: Run → Run with Coverage

## Additional Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)
- [Ktor Client Documentation](https://ktor.io/docs/client.html)
- [kotlinx.serialization Guide](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
- [NEAR RPC Documentation](https://docs.near.org/api/rpc/introduction)
- [OpenAPI Specification](https://github.com/near/near-jsonrpc-client-rs/blob/master/openapi.json)
- [CONTRIBUTING.md](./CONTRIBUTING.md) - Contribution guidelines
- [README.md](./README.md) - Project overview

## CI/CD Local Setup

### Running CI Checks Locally

Simulate the CI/CD workflow locally:

```bash
# Run all CI checks
./gradlew clean build test jacocoTestReport ktlintCheck

# Create a local CI script
cat > check-ci.sh << 'EOF'
#!/bin/bash
set -e

echo "🐍 Setting up Python environment..."
cd scripts
if [ ! -d "venv" ]; then
  ./setup.sh
fi
source venv/bin/activate

echo "🔄 Generating Kotlin code from OpenAPI spec..."
bash codegen.sh
deactivate
cd ..

echo "📦 Building Gradle project..."
./gradlew clean build

echo "🧪 Running tests with coverage..."
./gradlew test jacocoTestReport

echo "✨ Checking code formatting..."
./gradlew ktlintCheck

echo "✅ All CI checks passed!"
EOF

chmod +x check-ci.sh
./check-ci.sh
```

### Understanding the CI/CD Workflows

The project uses GitHub Actions workflows:

1. **CI/CD** (`.github/workflows/ci-cd.yml`)
   - Runs on every push and PR
   - Tests on Linux (Ubuntu)
   - Generates code from OpenAPI spec
   - Runs tests with code coverage
   - Uploads coverage to Codecov

2. **Code Generation** (`.github/workflows/generate.yml`)
   - Runs on schedule or manually
   - Downloads latest OpenAPI spec
   - Regenerates Kotlin code if changes detected
   - Creates PR with updates

3. **Publish** (`.github/workflows/publish.yml`)
   - Runs on release tags
   - Publishes to JitPack
   - Creates GitHub release

## Troubleshooting

### Common Issues

#### 1. Java Version Mismatch

```bash
# Error: Java 21 required, but 11 found
# Solution: Update Java

# Check current version
java -version

# macOS: Install Java 21
brew install openjdk@21

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Or use SDKMAN
sdk install java 21-tem
sdk use java 21-tem

# Verify version
java -version
```

#### 2. Build Failures

```bash
# Clean and rebuild
./gradlew clean build

# Clear Gradle cache
rm -rf ~/.gradle/caches
./gradlew build --refresh-dependencies

# Stop Gradle daemon
./gradlew --stop
./gradlew build
```

#### 3. Test Failures

```bash
# Run tests with verbose output
./gradlew test --info

# Run tests with stack traces
./gradlew test --stacktrace

# Run specific test to isolate issue
./gradlew test --tests "SpecificTestClass"

# Check for network issues (integration tests require network)
# Skip integration tests
./gradlew test -x :client:test
```

#### 4. Python Environment Issues

```bash
# Recreate Python virtual environment
cd scripts
rm -rf venv
./setup.sh

# Verify Python packages
source venv/bin/activate
pip list
deactivate
```

#### 5. ktlint Issues

```bash
# Auto-fix formatting issues
./gradlew ktlintFormat

# If ktlint fails, check specific errors
./gradlew ktlintCheck --info

# Manually format specific file
./gradlew :client:ktlintFormat
```

#### 6. Gradle Wrapper Issues

```bash
# If gradlew is not executable
chmod +x gradlew

# Update Gradle wrapper
./gradlew wrapper --gradle-version=8.5

# Verify wrapper
./gradlew --version
```

#### 7. IntelliJ IDEA Issues

```bash
# Invalidate caches and restart
# File → Invalidate Caches → Invalidate and Restart

# Reimport Gradle project
# Right-click on project → Gradle → Reload Gradle Project

# Rebuild project
# Build → Rebuild Project
```

### Getting Help

If you encounter issues not covered here:

1. Check the [main README](./README.md)
2. Search [GitHub Issues](https://github.com/space-rock/near-jsonrpc-kotlin/issues)
3. Check [CONTRIBUTING.md](./CONTRIBUTING.md) for guidelines
4. Create a new issue with:
   - Java version (`java -version`)
   - Gradle version (`./gradlew --version`)
   - Kotlin version
   - Platform (macOS/Linux, version)
   - Full error message
   - Steps to reproduce

## Performance Tips

### Gradle Performance

```bash
# Enable Gradle daemon (should be default)
echo "org.gradle.daemon=true" >> gradle.properties

# Enable parallel builds
echo "org.gradle.parallel=true" >> gradle.properties

# Increase memory
echo "org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m" >> gradle.properties

# Enable configuration cache (experimental)
./gradlew build --configuration-cache
```

### Build Cache

```bash
# Enable build cache
echo "org.gradle.caching=true" >> gradle.properties

# Use build cache
./gradlew build --build-cache

# Clean build cache
rm -rf ~/.gradle/caches/build-cache-1
```

## Next Steps

After completing setup:

1. Read [CONTRIBUTING.md](./CONTRIBUTING.md) for contribution guidelines
2. Explore the [example](./example/src/main/kotlin/org/near/jsonrpc/example/NearJsonRpcExample.kt)
3. Try running the example: `./gradlew :example:run`
4. Make your first contribution!
