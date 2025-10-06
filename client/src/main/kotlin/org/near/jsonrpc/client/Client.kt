package org.near.jsonrpc.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import org.near.jsonrpc.types.*

/**
 * NEAR JSON-RPC client for type-safe RPC calls to NEAR Protocol nodes.
 *
 * Use the extension methods in Methods.kt for type-safe access to specific RPC methods.
 */
class NearRpcClient private constructor(
    private val client: HttpClient,
    private val endpoint: String,
    private val json: Json,
) {
    /**
     * Execute a JSON-RPC call.
     *
     * @throws JsonRpcException if the RPC call returns an error
     */
    suspend fun <Params, Result> call(
        method: String,
        params: Params,
        paramsSerializer: KSerializer<Params>,
        resultSerializer: KSerializer<Result>,
        id: Int = 1,
    ): Result {
        val paramsJson = json.encodeToJsonElement(paramsSerializer, params)

        val requestBody =
            buildJsonObject {
                put("jsonrpc", JsonPrimitive("2.0"))
                put("method", JsonPrimitive(method))
                put("params", paramsJson)
                put("id", JsonPrimitive(id))
            }

        val requestJson = json.encodeToString(JsonObject.serializer(), requestBody)
        val responseBody: String =
            client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(requestJson)
            }.body()

        val responseJson = json.parseToJsonElement(responseBody).jsonObject

        if ("error" in responseJson) {
            val errorJson = responseJson["error"]!!.jsonObject
            val message = errorJson["message"]?.jsonPrimitive?.content ?: "Unknown error"
            val code = errorJson["code"]?.jsonPrimitive?.intOrNull ?: -1
            val data = errorJson["data"]
            throw JsonRpcException(
                message = message,
                code = code,
                data = data,
            )
        }

        val resultJson = responseJson["result"] ?: JsonNull
        return json.decodeFromJsonElement(resultSerializer, resultJson)
    }

    companion object {
        /**
         * Create a NearRpcClient with default configuration.
         */
        fun default(endpoint: String): NearRpcClient {
            val json =
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = false
                    explicitNulls = false
                    serializersModule = nearSerializersModule
                }
            val httpClient =
                HttpClient {
                    install(ContentNegotiation) {
                        json(json)
                    }
                }
            return NearRpcClient(httpClient, endpoint, json)
        }

        /**
         * Create a NearRpcClient with custom configuration.
         */
        fun fromClient(
            endpoint: String,
            client: HttpClient,
            json: Json =
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    serializersModule = nearSerializersModule
                },
        ): NearRpcClient = NearRpcClient(client, endpoint, json)
    }
}

/**
 * Exception thrown when a JSON-RPC call returns an error.
 */
class JsonRpcException(
    message: String,
    val code: Int,
    val data: JsonElement?,
) : Exception("JSON-RPC Error $code: $message")
