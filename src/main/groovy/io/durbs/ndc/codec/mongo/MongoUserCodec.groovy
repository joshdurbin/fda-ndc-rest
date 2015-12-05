package io.durbs.ndc.codec.mongo

import io.durbs.ndc.domain.User
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext

class MongoUserCodec implements CollectibleCodec<User> {

  static final Codec<Document> documentCodec = new DocumentCodec()

  @Override
  User decode(final BsonReader reader, final DecoderContext decoderContext) {

    final Document document = documentCodec.decode(reader, decoderContext)

    new User(id: document.getString('id'),
      firstName: document.getString('firstName'),
      lastName: document.getString('lastName'),
      username: document.getString('username'),
      password: document.getString('password'),
      salt: document.getString('salt')
    )
  }

  @Override
  void encode(final BsonWriter writer, final User user, final EncoderContext encoderContext) {

    final Document document = new Document()

    if (user.id) {
      document.put('id', user.id)
    }

    if (user.firstName) {
      document.put('firstName', user.firstName)
    }

    if (user.lastName) {
      document.put('lastName', user.lastName)
    }

    if (user.username) {
      document.put('username', user.username)
    }

    if (user.password) {
      document.put('password', user.password)
    }

    if (user.salt) {
      document.put('salt', user.salt)
    }

    documentCodec.encode(writer, document, encoderContext);
  }

  @Override
  Class<User> getEncoderClass() {
    User
  }

  @Override
  User generateIdIfAbsentFromDocument(final User user) {

    if (documentHasId(user)) {
      user.setId(UUID.randomUUID() as String)
    }

    user
  }

  @Override
  boolean documentHasId(final User user) {
    user.id
  }

  @Override
  BsonValue getDocumentId(final User user) {

    if (!documentHasId(user)) {
      throw new IllegalStateException('The user does not contain an _id');
    }

    new BsonString(user.id);
  }

}
