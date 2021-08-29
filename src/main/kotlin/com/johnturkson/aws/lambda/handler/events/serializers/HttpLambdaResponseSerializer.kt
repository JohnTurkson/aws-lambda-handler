package com.johnturkson.aws.lambda.handler.events.serializers

import com.johnturkson.aws.lambda.handler.events.HttpLambdaResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.util.Base64

internal class HttpLambdaResponseSerializer<T>(private val bodySerializer: KSerializer<T>) : KSerializer<HttpLambdaResponse<T>> {
    override val descriptor = bodySerializer.descriptor
    
    override fun serialize(encoder: Encoder, value: HttpLambdaResponse<T>) {
        require(encoder is JsonEncoder)
        
        val body = encoder.json.encodeToString(bodySerializer, value.body).let { rawBody ->
            if (value.isBase64Encoded) Base64.getEncoder().encodeToString(rawBody.toByteArray()) else rawBody
        }
        
        val keys = mapOf(
            "statusCode" to JsonPrimitive(value.statusCode),
            "headers" to encoder.json.encodeToJsonElement(MapSerializer(String.serializer(), String.serializer()), value.headers),
            "isBase64Encoded" to JsonPrimitive(value.isBase64Encoded),
            "body" to JsonPrimitive(body),
        )
        
        encoder.encodeJsonElement(JsonObject(keys))
    }
    
    override fun deserialize(decoder: Decoder): HttpLambdaResponse<T> {
        require(decoder is JsonDecoder)
        val response = decoder.decodeJsonElement() as JsonObject
        
        val statusCode = requireNotNull(response["statusCode"]?.jsonPrimitive?.int)
        val headers = decoder.json.decodeFromJsonElement(
            MapSerializer(String.serializer(), String.serializer()),
            response["headers"] ?: JsonObject(emptyMap())
        )
        val isBase64Encoded = requireNotNull(response["isBase64Decoded"]?.jsonPrimitive?.boolean)
        val body = requireNotNull(response["body"]?.jsonPrimitive?.content).let { rawBody ->
            val decodedBody = if (isBase64Encoded) Base64.getDecoder().decode(rawBody).decodeToString() else rawBody
            decoder.json.decodeFromString(bodySerializer, decodedBody)
        }
        
        return HttpLambdaResponse(statusCode, headers, isBase64Encoded, body)
    }
}
