#!/bin/bash
# Code generation pipeline: regenerates Kotlin types, mocks, tests, and formats code from openapi.json

set -e

echo "🚀 Starting code generation..."
echo ""

cd "$(dirname "$0")"

echo "📝 Step 1/4: Generating Kotlin types and methods..."
python3 generate_types.py
if [ $? -eq 0 ]; then
    echo "✅ Types and methods generated"
else
    echo "❌ Failed to generate types and methods"
    exit 1
fi
echo ""

echo "📝 Step 2/4: Generating mock JSON files..."
python3 generate_mock.py
if [ $? -eq 0 ]; then
    echo "✅ Mock JSON files generated"
else
    echo "❌ Failed to generate mock JSON files"
    exit 1
fi
echo ""

echo "📝 Step 3/4: Generating test files..."
python3 generate_tests.py
if [ $? -eq 0 ]; then
    echo "✅ Test files generated"
else
    echo "❌ Failed to generate test files"
    exit 1
fi
echo ""

echo "📝 Step 4/4: Formatting Kotlin code..."
cd ..
./gradlew ktlintFormat
if [ $? -eq 0 ]; then
    echo "✅ Code formatted"
else
    echo "⚠️ Code formatting encountered issues"
fi
echo ""

echo "✨ Code generation complete!"
echo ""
