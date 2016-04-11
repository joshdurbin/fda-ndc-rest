package io.durbs.ndc.codec.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Charsets
import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.codec.RedisCodec
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.auth.APIAuthRecord

import java.nio.ByteBuffer

@Singleton
@CompileStatic
class APIAuthRecordCodec implements RedisCodec<String, APIAuthRecord> {

  @Inject
  ObjectMapper objectMapper

  @Override
  String decodeKey(final ByteBuffer bytes) {

    Charsets.UTF_8.decode(bytes).toString()
  }

  @Override
  APIAuthRecord decodeValue(final ByteBuffer byteBuffer) {

    final byte[] bytes = new byte[byteBuffer.remaining()]
    byteBuffer.duplicate().get(bytes)

    objectMapper.readValue(new String(bytes, Charsets.UTF_8), APIAuthRecord)
  }

  @Override
  ByteBuffer encodeKey(final String key) {

    Charsets.UTF_8.encode(key)
  }

  @Override
  ByteBuffer encodeValue(final APIAuthRecord apiAuthRecord) {

    ByteBuffer.wrap(objectMapper.writeValueAsString(apiAuthRecord).getBytes(Charsets.UTF_8))
  }
}
