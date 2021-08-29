package com.johnturkson.aws.lambda.handler

import com.johnturkson.aws.lambda.handler.events.HttpLambdaRequest
import com.johnturkson.aws.lambda.handler.events.HttpLambdaResponse

typealias HttpLambdaFunction<I, O> = LambdaFunction<HttpLambdaRequest<I>, HttpLambdaResponse<O>>
