package io.github.chrislo27.witnessclone.util

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.OutputStream


object JsonHandler {

    val OBJECT_MAPPER: ObjectMapper = createObjectMapper(false)
//	val GSON: Gson = createObjectMapper()

    @JvmStatic
    fun createObjectMapper(failOnUnknown: Boolean = false, prettyPrinted: Boolean = true): ObjectMapper {
        val mapper = ObjectMapper()
                .enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
                .enable(MapperFeature.USE_ANNOTATIONS)
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .registerModule(AfterburnerModule())
                .registerModule(KotlinModule())
                .registerModule(JavaTimeModule())

        if (!failOnUnknown) {
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

        if (prettyPrinted) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT)
        }

        return mapper
    }

    @JvmStatic
    inline fun <reified T> fromJson(json: String): T {
        return OBJECT_MAPPER.readValue(json, T::class.java)
    }

    @JvmStatic
    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return OBJECT_MAPPER.readValue(json, clazz)
    }

    @JvmStatic
    fun toJson(obj: Any, stream: OutputStream) {
        OBJECT_MAPPER.writeValue(stream, obj)
    }

    @JvmStatic
    fun toJson(obj: Any): String {
        return OBJECT_MAPPER.writeValueAsString(obj)
    }

    @JvmStatic
    fun <T> toJson(obj: Any, clazz: Class<T>): String {
        return OBJECT_MAPPER.writeValueAsString(clazz.cast(obj))
    }

}
