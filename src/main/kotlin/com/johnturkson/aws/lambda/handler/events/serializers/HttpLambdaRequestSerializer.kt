package com.johnturkson.aws.lambda.handler.events.serializers

import com.johnturkson.aws.lambda.handler.events.HttpLambdaRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.util.Base64

internal class HttpLambdaRequestSerializer<T>(private val bodySerializer: KSerializer<T>) : KSerializer<HttpLambdaRequest<T>> {
    override val descriptor = bodySerializer.descriptor
    
    override fun serialize(encoder: Encoder, value: HttpLambdaRequest<T>) {
        require(encoder is JsonEncoder)
        
        val body = encoder.json.encodeToString(bodySerializer, value.body).let { rawBody ->
            if (value.isBase64Encoded) Base64.getEncoder().encodeToString(rawBody.toByteArray()) else rawBody
        }
        
        val keys = mapOf(
            "version" to JsonPrimitive(value.version),
            "routeKey" to JsonPrimitive(value.routeKey),
            "rawPath" to JsonPrimitive(value.rawPath),
            "rawQueryString" to JsonPrimitive(value.rawQueryString),
            "headers" to encoder.json.encodeToJsonElement(MapSerializer(String.serializer(), String.serializer()), value.headers),
            "requestContext" to encoder.json.encodeToJsonElement(HttpLambdaRequest.RequestContext.serializer(), value.requestContext),
            "isBase64Encoded" to JsonPrimitive(value.isBase64Encoded),
            "body" to JsonPrimitive(body),
        )
        
        encoder.encodeJsonElement(JsonObject(keys))
    }
    
    override fun deserialize(decoder: Decoder): HttpLambdaRequest<T> {
        require(decoder is JsonDecoder)
        val request = decoder.decodeJsonElement() as JsonObject
        
        val version = requireNotNull(request["version"]?.jsonPrimitive?.content)
        val routeKey = requireNotNull(request["routeKey"]?.jsonPrimitive?.content)
        val rawPath = requireNotNull(request["rawPath"]?.jsonPrimitive?.content)
        val rawQueryString = requireNotNull(request["rawQueryString"]?.jsonPrimitive?.content)
        val headers = decoder.json.decodeFromJsonElement(
            MapSerializer(String.serializer(), String.serializer()),
            request["headers"] ?: JsonObject(emptyMap())
        )
        val requestContext = decoder.json.decodeFromJsonElement(
            HttpLambdaRequest.RequestContext.serializer(),
            requireNotNull(request["requestContext"])
        )
        val isBase64Encoded = requireNotNull(request["isBase64Encoded"]?.jsonPrimitive?.boolean)
        val body = requireNotNull(request["body"]?.jsonPrimitive?.content).let { rawBody ->
            val decodedBody = if (isBase64Encoded) Base64.getDecoder().decode(rawBody).decodeToString() else rawBody
            decoder.json.decodeFromString(bodySerializer, decodedBody)
        }
        
        return HttpLambdaRequest(version, routeKey, rawPath, rawQueryString, headers, requestContext, isBase64Encoded, body)
    }
}
