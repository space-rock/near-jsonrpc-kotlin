# Deployment Guide

This guide covers the deployment and release process for the NEAR JSON-RPC Kotlin Client, including tagging, versioning, and distribution via JitPack and Maven.

## Overview

The project uses:

- **Git Tags** for version management
- **GitHub Actions** for CI/CD automation
- **JitPack** for Maven artifact distribution
- **Semantic Versioning** for version numbers
- **Gradle** for build and publishing
- **Conventional Commits** for changelog generation (recommended)

### Package Structure

The Kotlin project contains three modules:

```
near-jsonrpc-kotlin
├── types                # Type definitions and serialization
├── client               # RPC client implementation
└── example              # Usage examples
```

## Release Process

### Standard Release Flow

1. **Development**: Merge PRs to `main` branch
2. **Version Update**: Update version in `build.gradle.kts`
3. **Tag Creation**: Create and push a git tag for the release
4. **GitHub Release**: Create GitHub release with changelog
5. **Distribution**: JitPack automatically builds and publishes artifacts

### Release Types

Following Semantic Versioning:

- Bug fixes → Patch release (0.0.X)
- New features → Minor release (0.X.0)
- Breaking changes → Major release (X.0.0)

### Release Process Overview

1. **Merge PRs** to `main` branch
2. **Update version** in `build.gradle.kts`
3. **Create and push tag** (e.g., `v1.0.0`)
4. **Create GitHub release** with changelog
5. **JitPack builds** artifacts automatically
6. **Users can install** via Gradle/Maven

## Version Management

### Version File

The version is defined in the root `build.gradle.kts`:

```kotlin
allprojects {
    group = "com.near"
    version = "0.1.0-SNAPSHOT"
}
```

### Updating Version

```bash
# Edit build.gradle.kts
# Change version line:
# version = "0.1.0-SNAPSHOT"  →  version = "1.0.0"

# Commit the change
git add build.gradle.kts
git commit -m "chore: bump version to 1.0.0"
git push origin main
```

### Semantic Versioning

Follow [SemVer](https://semver.org/) guidelines:

- **Major (X.0.0)**: Breaking API changes
- **Minor (0.X.0)**: New features, backwards compatible
- **Patch (0.0.X)**: Bug fixes, backwards compatible

### Version Numbering Examples

```
1.0.0 → 1.0.1  # Bug fix
1.0.1 → 1.1.0  # New feature
1.1.0 → 2.0.0  # Breaking change
```

### Snapshot Versions

For development versions:

```kotlin
version = "1.1.0-SNAPSHOT"  // Development version
version = "1.1.0"           // Release version
```

## Creating a Release

### Standard Release Process

#### 1. Prepare the Release

```bash
# Ensure you're on main and up to date
git checkout main
git pull origin main

# Ensure clean working directory
git status

# Run all tests
./gradlew test

# Format code
./gradlew ktlintFormat

# Build in release mode
./gradlew clean build
```

#### 2. Update Version

```bash
# Edit build.gradle.kts
# Update version from "0.1.0-SNAPSHOT" to "1.0.0"

# Commit version change
git add build.gradle.kts
git commit -m "chore: bump version to 1.0.0"
git push origin main
```

#### 3. Create and Push Tag

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release v1.0.0

Features:
- Add new RPC method support
- Improve error handling

Bug Fixes:
- Fix serialization issue for AccountView
"

# Push tag
git push origin v1.0.0
```

#### 4. Create GitHub Release

```bash
# Using GitHub CLI
gh release create v1.0.0 \
  --title "v1.0.0" \
  --notes "## What's Changed

### Features
- Add new RPC method support (#123)
- Improve error handling (#124)

### Bug Fixes
- Fix serialization issue for AccountView (#125)

**Full Changelog**: https://github.com/space-rock/near-jsonrpc-kotlin/compare/v0.1.0...v1.0.0"

# Or create manually on GitHub:
# https://github.com/space-rock/near-jsonrpc-kotlin/releases/new
```

#### 5. Verify JitPack Build

```bash
# JitPack will automatically build the release
# Check build status at:
# https://jitpack.io/#space-rock/near-jsonrpc-kotlin

# Test installation in a new project
# Add to build.gradle.kts:
# implementation("com.github.space-rock.near-jsonrpc-kotlin:client:1.0.0")
```

### Pre-release Versions

For beta or alpha releases:

```bash
# Update version with pre-release suffix
# In build.gradle.kts:
version = "2.0.0-beta.1"

# Commit and push
git add build.gradle.kts
git commit -m "chore: bump version to 2.0.0-beta.1"
git push origin main

# Create tag
git tag -a v2.0.0-beta.1 -m "Release v2.0.0-beta.1 (Beta)"
git push origin v2.0.0-beta.1

# Create GitHub release with pre-release flag
gh release create v2.0.0-beta.1 \
  --title "v2.0.0-beta.1" \
  --notes "Beta release for testing" \
  --prerelease
```

## Distribution

### JitPack

The primary distribution method is via JitPack, which builds Maven artifacts from GitHub releases.

#### JitPack Configuration

The `jitpack.yml` file configures the build:

```yaml
jdk:
  - openjdk21

before_install:
  - cd scripts
  - pip install -r requirements.txt
  - bash codegen.sh
  - cd ..

install:
  - ./gradlew clean build publishToMavenLocal -x test
```

#### Using the Package

**Gradle (Kotlin DSL):**

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.space-rock.near-jsonrpc-kotlin:client:1.0.0")
    implementation("com.github.space-rock.near-jsonrpc-kotlin:types:1.0.0")
}
```

**Gradle (Groovy):**

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.space-rock.near-jsonrpc-kotlin:client:1.0.0'
    implementation 'com.github.space-rock.near-jsonrpc-kotlin:types:1.0.0'
}
```

**Maven:**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.space-rock.near-jsonrpc-kotlin</groupId>
        <artifactId>client</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Version Resolution

JitPack uses git tags for version resolution:

- **Specific Version**: `implementation("...client:1.0.0")`
- **Latest Release**: `implementation("...client:+")` (not recommended)
- **Snapshot**: `implementation("...client:main-SNAPSHOT")` (development only)
- **Commit Hash**: `implementation("...client:abc1234")` (specific commit)

### Testing Package Resolution

```bash
# Test that Gradle can resolve the package
./gradlew dependencies

# Check specific dependency
./gradlew dependencies --configuration runtimeClasspath | grep near-jsonrpc

# Refresh dependencies
./gradlew build --refresh-dependencies
```

## CI/CD Pipeline

### Workflow Overview

The project uses GitHub Actions workflows:

1. **CI/CD** (`.github/workflows/ci-cd.yml`)
2. **Code Generation** (`.github/workflows/generate.yml`)
3. **Publish** (`.github/workflows/publish.yml`)

### CI/CD Workflow

The `.github/workflows/ci-cd.yml` runs on every push and PR:

**Triggers:**
- Push to `main` branch
- Pull requests to `main` branch

**Jobs:**

1. **test-linux**: Tests on Ubuntu with Java 21
   - Sets up Java and Python environments
   - Generates Kotlin code from OpenAPI spec
   - Builds Gradle project
   - Runs tests with code coverage
   - Uploads coverage to Codecov

2. **validate-package**: Validates Gradle project structure (PR only)
   - Runs after tests pass
   - Validates project structure

### Code Generation Workflow

The `.github/workflows/generate.yml` can be triggered manually or on schedule:

**Triggers:**
- Manual workflow dispatch
- Scheduled (if configured)

**Process:**
1. Downloads latest OpenAPI spec
2. Regenerates Kotlin code
3. Builds and tests the project
4. Creates PR if changes detected

### Code Coverage

The project uses **Codecov** for code coverage tracking:

**Configuration:**
- Coverage is generated using Jacoco
- Configuration file: `codecov.yml` at repository root
- Coverage reports are uploaded to Codecov

**Setup:**
1. Set `CODECOV_TOKEN` repository secret
2. Coverage is automatically uploaded on every CI run
3. View reports at: `https://codecov.io/gh/space-rock/near-jsonrpc-kotlin`

**Coverage Requirements:**
- Target: 70-100% coverage
- Reports include both `client` and `types` modules

### Publish Workflow

The `.github/workflows/publish.yml` can handle release automation:

**Triggers:**
- Push of version tags (e.g., `v*`)
- Manual workflow dispatch

**Jobs:**
1. Validates the release build
2. Runs all tests
3. Creates GitHub release (if not exists)
4. JitPack automatically picks up the tag

## JitPack Integration

### How JitPack Works

1. **Tag Detection**: JitPack monitors GitHub releases and tags
2. **Build Trigger**: When a tag is pushed, JitPack builds the project
3. **Artifact Publishing**: Built artifacts are cached and served
4. **Dependency Resolution**: Users can reference the version in their builds

### JitPack Build Process

```bash
# JitPack runs these commands:
1. git clone https://github.com/space-rock/near-jsonrpc-kotlin.git
2. git checkout v1.0.0
3. cd scripts && pip install -r requirements.txt
4. bash codegen.sh
5. cd .. && ./gradlew clean build publishToMavenLocal -x test
```

### Checking JitPack Build Status

```bash
# Visit JitPack page
open https://jitpack.io/#space-rock/near-jsonrpc-kotlin

# Check specific version build log
open https://jitpack.io/com/github/space-rock/near-jsonrpc-kotlin/client/1.0.0/build.log

# Force rebuild (if needed)
# Visit: https://jitpack.io/#space-rock/near-jsonrpc-kotlin
# Click "Look up" button to trigger rebuild
```

### JitPack Badge

Add to README.md:

```markdown
[![JitPack](https://jitpack.io/v/space-rock/near-jsonrpc-kotlin.svg)](https://jitpack.io/#space-rock/near-jsonrpc-kotlin)
```

## Security

### Access Control

1. **GitHub Access**
   - Protected branches for `main`
   - Required reviews for PRs
   - Signed commits recommended
   - Tag protection enabled

2. **Release Access**
   - Limit release creation to maintainers
   - Use GitHub environments for production releases
   - Require manual approval for releases

### Security Checklist

- [ ] GitHub branch protection enabled
- [ ] Tag protection enabled for `v*` pattern
- [ ] All dependencies reviewed
- [ ] No sensitive data in code
- [ ] Security advisories enabled
- [ ] Dependabot alerts enabled
- [ ] CODECOV_TOKEN stored as secret

### Secure Release Process

```bash
# Use signed commits
git config --global commit.gpgsign true

# Sign tags
git tag -s v1.0.0 -m "Release v1.0.0"

# Verify tag signature
git tag -v v1.0.0
```

## Rollback Procedures

### Tag and Release Rollback

```bash
# 1. Delete the faulty tag locally and remotely
git tag -d v1.2.3
git push --delete origin v1.2.3

# 2. Delete the GitHub release
gh release delete v1.2.3 --yes

# 3. Clear JitPack cache (if needed)
# Visit: https://jitpack.io/#space-rock/near-jsonrpc-kotlin
# Use "Look up" to rebuild or contact JitPack support

# 4. Revert problematic commits (if needed)
git revert <commit-hash>
git push origin main

# 5. Create a new patch release
# Update version in build.gradle.kts to 1.2.4
git add build.gradle.kts
git commit -m "chore: bump version to 1.2.4"
git push origin main

git tag -a v1.2.4 -m "Release v1.2.4 - Fixes issues in v1.2.3"
git push origin v1.2.4
```

### Communication

When rolling back a release:

1. **Immediate**: Create GitHub issue explaining the problem
2. **Users**: Update GitHub release notes with warning
3. **Documentation**: Add to known issues
4. **Fix**: Release patch version ASAP
5. **Post-mortem**: Document what went wrong and how to prevent it

### Emergency Hotfix Process

```bash
# 1. Create hotfix branch from main
git checkout -b hotfix/critical-bug main

# 2. Fix the bug
# Make necessary changes

# 3. Test thoroughly
./gradlew test

# 4. Merge back to main
git checkout main
git merge --no-ff hotfix/critical-bug
git push origin main

# 5. Create new patch release immediately
# Update version in build.gradle.kts
git add build.gradle.kts
git commit -m "chore: bump version to 1.2.4 (hotfix)"
git push origin main

git tag -a v1.2.4 -m "Hotfix: Critical bug fix"
git push origin v1.2.4

# 6. Create GitHub release
gh release create v1.2.4 --title "v1.2.4 (Hotfix)" --notes "Critical bug fix"
```

## Monitoring

### Package Usage

Monitor via GitHub:

- Stars and watchers
- Forks
- Dependent repositories
- Traffic analytics

```bash
# Check dependent repositories
gh api /repos/space-rock/near-jsonrpc-kotlin/dependents

# View traffic
# Visit: https://github.com/space-rock/near-jsonrpc-kotlin/graphs/traffic
```

### JitPack Statistics

```bash
# Check JitPack download statistics
# Visit: https://jitpack.io/#space-rock/near-jsonrpc-kotlin

# View build logs
# https://jitpack.io/com/github/space-rock/near-jsonrpc-kotlin/client/VERSION/build.log
```

### Issue Tracking

Monitor for deployment-related issues:

1. **GitHub Issues**: Filter for `deployment` or `release` labels
2. **GitHub Discussions**: Check for user reports
3. **CI/CD Failures**: Monitor workflow runs
4. **JitPack Build Failures**: Check build logs

### Health Checks

```bash
# Verify package can be resolved by Gradle
./gradlew dependencies --refresh-dependencies

# Test installation in a clean project
mkdir test-install && cd test-install
gradle init --type kotlin-application
# Add package dependency to build.gradle.kts
./gradlew build
```

## Troubleshooting

### Common Issues

#### Tag Already Exists

```bash
# Error: Tag v1.0.0 already exists
# Solution: Delete local and remote tag, then recreate
git tag -d v1.0.0
git push --delete origin v1.0.0

# Create new tag
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

#### GitHub Actions Fails

```bash
# Check workflow logs
gh run list
gh run view <run-id>

# View specific job logs
gh run view <run-id> --log

# Re-run failed jobs
gh run rerun <run-id>

# Check specific workflow runs
gh run list --workflow=ci-cd.yml
gh run list --workflow=generate.yml
gh run list --workflow=publish.yml
```

#### JitPack Build Fails

```bash
# Check JitPack build log
open https://jitpack.io/com/github/space-rock/near-jsonrpc-kotlin/client/1.0.0/build.log

# Common issues:
# 1. jitpack.yml configuration error
# 2. Build script failure
# 3. Test failures (use -x test to skip)
# 4. Missing dependencies

# Force rebuild
# Visit: https://jitpack.io/#space-rock/near-jsonrpc-kotlin
# Click "Look up" button

# Test locally with JitPack commands
cd scripts
pip install -r requirements.txt
bash codegen.sh
cd ..
./gradlew clean build publishToMavenLocal -x test
```

#### Gradle Cannot Resolve Package

```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew build --refresh-dependencies

# Check if tag exists
git ls-remote --tags origin

# Verify JitPack has built the version
open https://jitpack.io/#space-rock/near-jsonrpc-kotlin

# Try with explicit version
# implementation("com.github.space-rock.near-jsonrpc-kotlin:client:1.0.0")
```

#### Version Mismatch

```bash
# Ensure build.gradle.kts version matches the git tag
cat build.gradle.kts | grep version

# Update if needed
# Edit build.gradle.kts
git add build.gradle.kts
git commit -m "chore: sync version"
git push origin main
```

#### Code Generation Fails

```bash
# Ensure Python environment is set up
cd scripts
./setup.sh

# Activate virtual environment
source venv/bin/activate

# Run code generation manually
python3 generate_types.py
python3 generate_mock.py
python3 generate_tests.py

# Check for errors
deactivate
cd ..

# Format generated code
./gradlew ktlintFormat
```

### Emergency Procedures

1. **Broken Release**

   ```bash
   # Delete tag and release
   git tag -d v1.2.3
   git push --delete origin v1.2.3
   gh release delete v1.2.3 --yes

   # Notify users
   gh issue create --title "Critical issue in v1.2.3" --label critical
   
   # Update release notes with warning (if not deleted)
   ```

2. **Security Issue**

   ```bash
   # Create security advisory
   # Go to: https://github.com/space-rock/near-jsonrpc-kotlin/security/advisories/new

   # Immediate hotfix
   git checkout -b security/CVE-fix main
   # Fix vulnerability
   ./gradlew test
   git commit -am "fix: security vulnerability"
   git push

   # Fast-track PR and release patch
   ```

3. **CI Failure**
   ```bash
   # Check CI logs
   gh run list --limit 5
   
   # Manual verification
   ./gradlew clean build test
   
   # If CI is broken but code is good, tag manually
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```

### Debug Commands

```bash
# Verify tag
git show v1.0.0

# List all tags
git tag -l

# Check remote tags
git ls-remote --tags origin

# Show Gradle project structure
./gradlew projects

# Show dependencies
./gradlew dependencies

# Test publication locally
./gradlew publishToMavenLocal

# Check local Maven repository
ls -la ~/.m2/repository/com/near/
```

## Best Practices

### Pre-Release Checklist

- [ ] All tests passing (`./gradlew test`)
- [ ] Code formatted (`./gradlew ktlintFormat`)
- [ ] No compiler warnings
- [ ] Generated code up to date (if OpenAPI spec changed)
- [ ] Documentation updated
- [ ] CHANGELOG.md updated (if maintained)
- [ ] Migration guide (if breaking changes)
- [ ] Examples still work (`./gradlew :example:run`)
- [ ] Version updated in `build.gradle.kts`
- [ ] JitPack configuration tested

### Release Notes Template

```markdown
## What's Changed

### Features
- Add support for new RPC methods (#123)
- Improve error handling in client (#124)

### Bug Fixes
- Fix serialization for AccountView (#125)
- Correct deserialization for nested sealed classes (#126)

### Breaking Changes
- Change client initialization API (#127)
  - Migration: Update `NearRpcClient.default()` to `NearRpcClient.create()`
  - See MIGRATING.md for details

### Code Generation
- Update OpenAPI specification to latest version
- Regenerate all types and methods

### Dependencies
- Update Ktor to 2.3.8
- Update kotlinx.serialization to 1.6.3

**Full Changelog**: https://github.com/space-rock/near-jsonrpc-kotlin/compare/v0.1.0...v1.0.0
```

### Post-Release Tasks

1. **Verify Release**
   - Check GitHub release page
   - Verify JitPack build succeeded
   - Test installation in clean project
   - Verify examples compile and run
   - Check that documentation is accessible

2. **Update Documentation**
   - Update README version references
   - Add migration notes (if breaking)
   - Update code examples
   - Update API documentation (if using Dokka)

3. **Announce** (for major releases)
   - GitHub discussions
   - Twitter/social media
   - NEAR community channels
   - Update project website (if applicable)

4. **Monitor**
   - Watch for issues
   - Check CI status
   - Monitor GitHub discussions
   - Respond to user feedback
   - Monitor JitPack build status

### Versioning Strategy

**Development Cycle:**
```
0.1.0-SNAPSHOT → Development
0.1.0          → Release
0.2.0-SNAPSHOT → Next development cycle
```

**Stable Releases:**
```
1.0.0 → First stable release
1.1.0 → New features
1.1.1 → Bug fixes
2.0.0 → Breaking changes
```

## Additional Resources

- [Gradle Publishing Documentation](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [JitPack Documentation](https://jitpack.io/docs/)
- [GitHub Releases Documentation](https://docs.github.com/en/repositories/releasing-projects-on-github)
- [Semantic Versioning](https://semver.org/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Tag Documentation](https://git-scm.com/book/en/v2/Git-Basics-Tagging)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [CONTRIBUTING.md](./CONTRIBUTING.md)
- [SETUP.md](./SETUP.md)
- [README.md](./README.md)
