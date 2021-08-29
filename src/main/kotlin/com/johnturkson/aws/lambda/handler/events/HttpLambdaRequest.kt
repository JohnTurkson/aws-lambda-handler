package com.johnturkson.aws.lambda.handler.events

import com.johnturkson.aws.lambda.handler.events.serializers.HttpLambdaRequestSerializer
import kotlinx.serialization.Serializable

@Serializable(HttpLambdaRequestSerializer::class)
data class HttpLambdaRequest<T>(
    val version: String,
    val routeKey: String,
    val rawPath: String,
    val rawQueryString: String,
    val headers: Map<String, String>,
    val requestContext: RequestContext,
    val isBase64Encoded: Boolean,
    val body: T,
) {
    @Serializable
    data class RequestContext(
        val accountId: String,
        val apiId: String,
        val domainName: String,
        val domainPrefix: String,
        val http: Http,
        val requestId: String,
        val routeKey: String,
        val stage: String,
        val time: String,
        val timeEpoch: Long,
    ) {
        @Serializable
        data class Http(
            val method: String,
            val path: String,
            val protocol: String,
            val sourceIp: String,
            val userAgent: String,
        )
    }
}
