package com.johnturkson.aws.lambda.handler.events

import com.johnturkson.aws.lambda.handler.events.serializers.HttpLambdaResponseSerializer
import kotlinx.serialization.Serializable

@Serializable(HttpLambdaResponseSerializer::class)
data class HttpLambdaResponse<T>(
    val statusCode: Int,
    val headers: Map<String, String>,
    val isBase64Encoded: Boolean,
    val body: T,
)
