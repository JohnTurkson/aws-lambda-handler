package com.johnturkson.aws.lambda.handler

import com.amazonaws.services.lambda.runtime.Context
import com.johnturkson.aws.lambda.handler.events.HttpLambdaRequest
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import java.util.Base64

abstract class HttpLambdaFunction<I, O>(
    serializer: Json,
    decoder: DeserializationStrategy<I>,
    encoder: SerializationStrategy<O>,
) : LambdaFunction<I, O>(serializer, decoder, encoder) {
    override fun decode(input: String, context: Context): I {
        val request = serializer.decodeFromString(HttpLambdaRequest.serializer(), input)
        val body = when {
            request.isBase64Encoded -> Base64.getDecoder().decode(request.body).decodeToString()
            else -> request.body
        }
        return super.decode(body, context)
    }
}
