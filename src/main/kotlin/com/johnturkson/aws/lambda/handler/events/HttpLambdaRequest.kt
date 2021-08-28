package com.johnturkson.aws.lambda.handler.events

import kotlinx.serialization.Serializable

@Serializable
data class HttpLambdaRequest(
    val version: String,
    val routeKey: String,
    val rawPath: String,
    val rawQueryString: String,
    val headers: Map<String, String>,
    val requestContext: RequestContext,
    val body: String,
    val isBase64Encoded: Boolean,
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
