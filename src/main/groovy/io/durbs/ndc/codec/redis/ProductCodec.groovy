package io.durbs.ndc.codec.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Charsets
import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.codec.RedisCodec
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product

import java.nio.ByteBuffer

@Singleton
@CompileStatic
class ProductCodec implements RedisCodec<String, Product> {

  @Inject
  ObjectMapper objectMapper

  @Override
  String decodeKey(final ByteBuffer bytes) {

    Charsets.UTF_8.decode(bytes).toString()
  }

  @Override
  Product decodeValue(final ByteBuffer byteBuffer) {

    final byte[] bytes = new byte[byteBuffer.remaining()]
    byteBuffer.duplicate().get(bytes)

    objectMapper.readValue(new String(bytes, Charsets.UTF_8), Product)
  }

  @Override
  ByteBuffer encodeKey(final String key) {

    Charsets.UTF_8.encode(key)
  }

  @Override
  ByteBuffer encodeValue(final Product product) {

    ByteBuffer.wrap(objectMapper.writeValueAsString(product).getBytes(Charsets.UTF_8))
  }
}
