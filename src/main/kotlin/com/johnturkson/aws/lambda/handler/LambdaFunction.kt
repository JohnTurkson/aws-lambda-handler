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
        val request = runCatching { decode(input, context) }.getOrElse { exception ->
            val output = onDecodingFailure(exception, input, context)
            return encode(output, context)
        }
        
        val response = runCatching { process(request, context) }.getOrElse { exception ->
            val output = onProcessingFailure(exception, request, context)
            return encode(output, context)
        }
        
        return encode(response, context)
    }
    
    fun decode(input: String, context: Context): I
    
    fun process(request: I, context: Context): O
    
    fun encode(output: O, context: Context): String
    
    fun onDecodingFailure(exception: Throwable, input: String, context: Context): O {
        throw NotImplementedError()
    }
    
    fun onProcessingFailure(exception: Throwable, request: I, context: Context): O {
        throw NotImplementedError()
    }
}
