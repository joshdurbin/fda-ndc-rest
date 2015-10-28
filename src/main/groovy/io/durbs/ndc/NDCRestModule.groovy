package io.durbs.ndc

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
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
import io.durbs.ndc.chain.ProductActionChain
import io.durbs.ndc.codec.ProductCodec
import io.durbs.ndc.config.MongoConfig
import io.durbs.ndc.service.ProductService
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

@CompileStatic
@Slf4j
class NDCRestModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(ProductActionChain)
    bind(ProductService)
  }

  @Provides
  @Singleton
  MongoDatabase provideMongoDatabase(MongoConfig mongoConfig) {

    final ConnectionString connectionString = new ConnectionString(mongoConfig.uri)

    final CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
      MongoClient.getDefaultCodecRegistry(),
      CodecRegistries.fromCodecs(new ProductCodec()))

    final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
      .codecRegistry(codecRegistry)
      .clusterSettings(ClusterSettings.builder().applyConnectionString(connectionString).build())
      .connectionPoolSettings(ConnectionPoolSettings.builder().applyConnectionString(connectionString).build())
      .serverSettings(ServerSettings.builder().build()).credentialList(connectionString.getCredentialList())
      .sslSettings(SslSettings.builder().applyConnectionString(connectionString).build())
      .socketSettings(SocketSettings.builder().applyConnectionString(connectionString).build())
      .build()

    log.info("Creaitng MongoDatabase")

    MongoClients.create(mongoClientSettings).getDatabase(mongoConfig.db)
  }
}
