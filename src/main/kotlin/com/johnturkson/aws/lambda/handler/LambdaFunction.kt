package com.johnturkson.aws.lambda.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

abstract class LambdaFunction<I, O>(
    protected val serializer: Json,
    private val decoder: DeserializationStrategy<I>,
    private val encoder: SerializationStrategy<O>,
) : RequestStreamHandler {
    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        val request = input.bufferedReader().use { reader -> reader.readText() }
        val response = handle(request, context)
        output.bufferedWriter().use { writer -> writer.write(response) }
    }
    
    open fun handle(input: String, context: Context): String {
        return runCatching { decode(input, context) }
            .recover { exception ->
                val output = onFailure(exception, context)
                return encode(output, context)
            }
            .mapCatching { request -> process(request, context) }
            .recover { exception ->
                val output = onFailure(exception, context)
                return encode(output, context)
            }
            .mapCatching { response -> encode(response, context) }
            .recover { exception ->
                val output = onFailure(exception, context)
                return encode(output, context)
            }
            .getOrThrow()
    }
    
    open fun decode(input: String, context: Context): I {
        return serializer.decodeFromString(decoder, input)
    }
    
    abstract fun process(request: I, context: Context): O
    
    open fun encode(output: O, context: Context): String {
        return serializer.encodeToString(encoder, output)
    }
    
    open fun onFailure(exception: Throwable, context: Context): O {
        throw NotImplementedError()
    }
}
