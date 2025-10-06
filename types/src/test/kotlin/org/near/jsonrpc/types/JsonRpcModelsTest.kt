package org.near.jsonrpc.types

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Basic serialization tests for generated types.
 * For comprehensive mock JSON validation, see TypesMockValidationTest.
 */
class JsonRpcModelsTest {
    private val json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            serializersModule = nearSerializersModule
        }

    @Test
    fun `can serialize and deserialize primitive types`() {
        val accountId: AccountId = "alice.near"
        val encoded = json.encodeToString<AccountId>(accountId)
        val decoded = json.decodeFromString<AccountId>(encoded)
        assertEquals(accountId, decoded)
    }

    @Test
    fun `can serialize and deserialize numeric types`() {
        val shardId: ShardId = 123L
        val encoded = json.encodeToString<ShardId>(shardId)
        val decoded = json.decodeFromString<ShardId>(encoded)
        assertEquals(shardId, decoded)
    }

    @Test
    fun `can serialize and deserialize enum types`() {
        val finality = Finality.FINAL
        val encoded = json.encodeToString(finality)
        val decoded = json.decodeFromString<Finality>(encoded)
        assertEquals(finality, decoded)
        assertEquals("\"final\"", encoded)
    }

    @Test
    fun `enum serializes with correct SerialName`() {
        val direction = Direction.LEFT
        val encoded = json.encodeToString(direction)
        assertEquals("\"Left\"", encoded)

        val decoded = json.decodeFromString<Direction>(encoded)
        assertEquals(direction, decoded)
    }

    @Test
    fun `nearSerializersModule is configured`() {
        assertNotNull(nearSerializersModule)
    }

    @Test
    fun `JsonElement can be used as fallback type`() {
        val element: JsonElement = JsonPrimitive("test")
        val encoded = json.encodeToString(element)
        val decoded = json.decodeFromString<JsonElement>(encoded)
        assertEquals(element, decoded)
    }
}
