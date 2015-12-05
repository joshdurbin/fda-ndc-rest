package io.durbs.ndc

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.mongodb.ConnectionString
import com.mongodb.MongoClient
import com.mongodb.async.client.MongoClientSettings
import com.mongodb.connection.ClusterSettings
import com.mongodb.connection.ConnectionPoolSettings
import com.mongodb.connection.ServerSettings
import com.mongodb.connection.SocketSettings
import com.mongodb.connection.SslSettings
import com.mongodb.rx.client.MongoClients
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.chain.ProductAPIActionChain
import io.durbs.ndc.chain.ProductAPIAuthActionChain

import io.durbs.ndc.codec.mongo.MongoProductCodec
import io.durbs.ndc.codec.mongo.MongoUserCodec
import io.durbs.ndc.codec.redis.RedisProductCodec
import io.durbs.ndc.config.MongoConfig
import io.durbs.ndc.config.RedisConfig
import io.durbs.ndc.domain.product.Product

import io.durbs.ndc.service.ProductService
import io.durbs.ndc.service.UserService
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

@CompileStatic
@Slf4j
class NDCRestModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(ProductAPIActionChain)
    bind(ProductAPIAuthActionChain)
    bind(ProductService)
    bind(UserService)
  }

  @Provides
  @Singleton
  RedisReactiveCommands<String, String> stringRedisCommands(RedisConfig redisConfig) {

    final RedisClient redisClient = RedisClient.create(redisConfig.uri)
    redisClient.connect().reactive()
  }

  @Provides
  @Singleton
  RedisReactiveCommands<String, Product> productRedisCommands(RedisConfig redisConfig) {

    final RedisClient redisClient = RedisClient.create(redisConfig.uri)
    redisClient.connect(new RedisProductCodec()).reactive()
  }

  @Provides
  @Singleton
  MongoDatabase provideMongoDatabase(MongoConfig mongoConfig) {

    final ConnectionString connectionString = new ConnectionString(mongoConfig.uri)

    final CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
      MongoClient.getDefaultCodecRegistry(),
      CodecRegistries.fromCodecs(new MongoProductCodec(), new MongoUserCodec()))

    final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
      .codecRegistry(codecRegistry)
      .clusterSettings(ClusterSettings.builder().applyConnectionString(connectionString).build())
      .connectionPoolSettings(ConnectionPoolSettings.builder().applyConnectionString(connectionString).build())
      .serverSettings(ServerSettings.builder().build()).credentialList(connectionString.getCredentialList())
      .sslSettings(SslSettings.builder().applyConnectionString(connectionString).build())
      .socketSettings(SocketSettings.builder().applyConnectionString(connectionString).build())
      .build()

    MongoClients.create(mongoClientSettings).getDatabase(mongoConfig.db)
  }
}
