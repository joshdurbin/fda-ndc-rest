package io.durbs.ndc.codec.redis

import com.google.common.base.Charsets
import com.lambdaworks.redis.codec.RedisCodec
import io.durbs.ndc.domain.product.Product

import java.nio.ByteBuffer

class RedisProductCodec implements RedisCodec<String, Product> {

  @Override
  String decodeKey(ByteBuffer bytes) {
    Charsets.UTF_8.decode(bytes).toString()
  }

  @Override
  Product decodeValue(ByteBuffer bytes) {

    byte[] byts = new byte[bytes.capacity()];
    bytes.get(byts);

    ObjectInputStream istream = new ObjectInputStream(new ByteArrayInputStream(byts));
    Object readObject = istream.readObject()
    readObject as Product
  }

  @Override
  ByteBuffer encodeKey(String key) {
    Charsets.UTF_8.encode(key)
  }

  @Override
  ByteBuffer encodeValue(Product value) {

    ByteArrayOutputStream result = new ByteArrayOutputStream()
    ObjectOutputStream outputStream = new ObjectOutputStream(result)
    outputStream.writeObject(value)
    outputStream.close()

    ByteBuffer buffer = ByteBuffer.allocate(result.size())
    buffer.put(result.toByteArray())
    buffer.flip()

    buffer
  }
}
