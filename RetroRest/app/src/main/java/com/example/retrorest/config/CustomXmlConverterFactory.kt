package com.example.retrorest.config



import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

import retrofit2.Converter
import retrofit2.Retrofit
import java.io.StringWriter
import java.lang.reflect.Type

class CustomXmlConverterFactory private constructor() : Converter.Factory() {

    private val serializer: Serializer = Persister()


    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return Converter { responseBody ->
            serializer.read(type as Class<*>, responseBody.byteStream())
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return Converter<Any, RequestBody> { value ->
            val writer = StringWriter()
            serializer.write(value, writer)
            writer.toString()
                .toRequestBody("application/xml".toMediaTypeOrNull())
        }
    }

    companion object {
        fun create(): CustomXmlConverterFactory {
            return CustomXmlConverterFactory()
        }
    }
}
