package com.johnturkson.aws.lambda.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream

interface LambdaFunction<I, O> : RequestStreamHandler {
    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        val request = input.bufferedReader().use { reader -> reader.readText() }
        val response = handle(request, context)
        output.bufferedWriter().use { writer -> writer.write(response) }
    }
    
    fun handle(input: String, context: Context): String {
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
    
    fun decode(input: String, context: Context): I
    
    fun process(request: I, context: Context): O
    
    fun encode(output: O, context: Context): String
    
    fun onFailure(exception: Throwable, context: Context): O {
        throw NotImplementedError()
    }
}
