package io.durbs.ndc.codec.redis

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.common.base.Charsets
import com.google.inject.Singleton
import com.lambdaworks.redis.codec.RedisCodec
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product

import java.nio.ByteBuffer

@Singleton
@CompileStatic
class RedisProductCodec implements RedisCodec<String, Product> {

  static ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

  @Override
  String decodeKey(ByteBuffer bytes) {

    Charsets.UTF_8.decode(bytes).toString()
  }

  @Override
  Product decodeValue(ByteBuffer byteBuffer) {

    final byte[] bytes = new byte[byteBuffer.remaining()]
    byteBuffer.duplicate().get(bytes)

    objectMapper.readValue(new String(bytes, Charsets.UTF_8), Product)
  }

  @Override
  ByteBuffer encodeKey(String key) {

    Charsets.UTF_8.encode(key)
  }

  @Override
  ByteBuffer encodeValue(Product product) {

    ByteBuffer.wrap(objectMapper.writeValueAsString(product).getBytes(Charsets.UTF_8))
  }
}
