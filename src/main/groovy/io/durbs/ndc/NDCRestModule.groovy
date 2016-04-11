package io.durbs.ndc

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.mongodb.ConnectionString
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.async.client.MongoClientSettings
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import com.mongodb.connection.ClusterSettings
import com.mongodb.connection.ConnectionPoolSettings
import com.mongodb.connection.ServerSettings
import com.mongodb.connection.SocketSettings
import com.mongodb.connection.SslSettings
import com.mongodb.connection.netty.NettyStreamFactoryFactory
import com.mongodb.rx.client.MongoClients
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.chain.AuthorizationTokenAPIActionChain
import io.durbs.ndc.chain.ProductAPIActionChain
import io.durbs.ndc.chain.ProductAPIAuthorizationActionChain

import io.durbs.ndc.codec.MongoProductCodec
import io.durbs.ndc.codec.redis.APIAuthRecordCodec
import io.durbs.ndc.codec.redis.ProductCodec
import io.durbs.ndc.config.MongoConfig
import io.durbs.ndc.config.RedisConfig
import io.durbs.ndc.domain.auth.APIAuthRecord
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.AuthenticationService
import io.durbs.ndc.service.CacheService
import io.durbs.ndc.service.ProductService
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import ratpack.server.Service
import ratpack.server.StartEvent

@CompileStatic
@Slf4j
class NDCRestModule extends AbstractModule {

  @Override
  protected void configure() {

    // codecs
    bind(APIAuthRecordCodec)
    bind(ProductCodec)

    // chains
    bind(AuthorizationTokenAPIActionChain)
    bind(ProductAPIActionChain)
    bind(ProductAPIAuthorizationActionChain)

    // services
    bind(AuthenticationService)
    bind(CacheService)
    bind(ProductService)
  }

  @Provides
  @Singleton
  RedisReactiveCommands<String, APIAuthRecord> apiAuthRecordRedisCommands(
    final RedisConfig redisConfig,
    final APIAuthRecordCodec apiAuthRecordCodec) {

    final RedisClient redisClient = RedisClient.create(redisConfig.uri)
    redisClient.connect(apiAuthRecordCodec).reactive()
  }

  @Provides
  @Singleton
  RedisReactiveCommands<String, String> stringRedisCommands(final RedisConfig redisConfig) {

    final RedisClient redisClient = RedisClient.create(redisConfig.uri)
    redisClient.connect().reactive()
  }

  @Provides
  @Singleton
  RedisReactiveCommands<String, Product> productRedisCommands(
    final RedisConfig redisConfig,
    final ProductCodec productCodec) {

    final RedisClient redisClient = RedisClient.create(redisConfig.uri)
    redisClient.connect(productCodec).reactive()
  }

  @Provides
  @Singleton
  MongoDatabase provideMongoDatabase(final MongoConfig mongoConfig) {

    final ConnectionString connectionString = new ConnectionString(mongoConfig.uri)

    final CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
      MongoClient.getDefaultCodecRegistry(),
      CodecRegistries.fromCodecs(new MongoProductCodec()))

    MongoClientSettings.Builder mongoClientSettingsBuidler = MongoClientSettings.builder()
      .codecRegistry(codecRegistry)
      .clusterSettings(ClusterSettings.builder().applyConnectionString(connectionString).build())
      .connectionPoolSettings(ConnectionPoolSettings.builder().applyConnectionString(connectionString).build())
      .serverSettings(ServerSettings.builder().build()).credentialList(connectionString.getCredentialList())
      .sslSettings(SslSettings.builder().applyConnectionString(connectionString).build())
      .socketSettings(SocketSettings.builder().applyConnectionString(connectionString).build())

    if (SslSettings.builder().applyConnectionString(connectionString).build().enabled) {
      mongoClientSettingsBuidler.streamFactoryFactory(new NettyStreamFactoryFactory())
    }

    MongoClients.create(mongoClientSettingsBuidler.build()).getDatabase(mongoConfig.db)
  }

  @Provides
  @Singleton
  public Service setup(final MongoConfig mongoConfig) {

    new Service() {

      @Override
      void onStart(StartEvent event) throws Exception {

        MongoClient mongoClient

        try {

          // OPEN A BLOCKING CONNECTION
          mongoClient = new MongoClient(new MongoClientURI(mongoConfig.uri))

          if (!mongoClient.listDatabaseNames().contains(mongoConfig.db)) {

            log.info("Creating Mongo DB '${mongoConfig.db}'...")

            // GET IMPLICITLY CREATES THE DB
            mongoClient.getDatabase(mongoConfig.db)
          }

          if (!mongoClient.getDatabase(mongoConfig.db).listCollectionNames().contains(mongoConfig.collection)) {

            log.info("Creating Mongo collection '${mongoConfig.collection}' in DB '${mongoConfig.db}'...")
            mongoClient.getDatabase(mongoConfig.db).createCollection(mongoConfig.collection)
          }

          final MongoCollection<Document> rootCollection = mongoClient.getDatabase(mongoConfig.db).getCollection(mongoConfig.collection)
          final List<String> indexNames = rootCollection.listIndexes().collect { Document index -> index.getString('name') }

          final String ndcCodeIndexName = "ndc-code"
          if (!indexNames.contains(ndcCodeIndexName)) {

            log.info("Creating index ${ndcCodeIndexName}...")
            rootCollection.createIndex(new Document('productNDC', 1), new IndexOptions(name: ndcCodeIndexName))
          }

          final String labelerIndexName = "labeler"
          if (!indexNames.contains(labelerIndexName)) {

            log.info("Creating index ${labelerIndexName}...")
            rootCollection.createIndex(new Document('labelerName', 1), new IndexOptions(name: labelerIndexName))
          }

          final String startDateIndexName = "start-date"
          if (!indexNames.contains(startDateIndexName)) {

            log.info("Creating index ${startDateIndexName}...")
            rootCollection.createIndex(new Document('startMarketingDate', 1), new IndexOptions(name: startDateIndexName))
          }

          final String startAndEndDateIndexName = "start-end-date"
          if (!indexNames.contains(startAndEndDateIndexName)) {

            log.info("Creating index ${startAndEndDateIndexName}...")
            rootCollection.createIndex(new Document('startMarketingDate', 1).append('endMarketingDate', 1), new IndexOptions(name: startAndEndDateIndexName))
          }

          final String productTypeIndexName = "product-type"
          if (!indexNames.contains(productTypeIndexName)) {

            log.info("Creating index ${productTypeIndexName}...")
            rootCollection.createIndex(new Document('productTypeName', 1), new IndexOptions(name: productTypeIndexName))
          }

          final String marketingCategoryIndexName = "marketing-cat-name"
          if (!indexNames.contains(marketingCategoryIndexName)) {

            log.info("Creating index ${marketingCategoryIndexName}...")
            rootCollection.createIndex(new Document('marketingCategoryName', 1), new IndexOptions(name: marketingCategoryIndexName))
          }

          final String randomKeyIndexName = "random-key"
          if (!indexNames.contains(randomKeyIndexName)) {

            log.info("Creating index ${randomKeyIndexName}...")
            rootCollection.createIndex(new Document('randomKey', 1), new IndexOptions(name: randomKeyIndexName))
          }

          final String textIndexName = "text-with-weights"
          if (!indexNames.contains(textIndexName)) {

            log.info("Creating index ${textIndexName}...")
            rootCollection.createIndex(
              new Document('$**', 'text'),
              new IndexOptions(name: textIndexName,
                weights: new Document().append('proprietaryName', 5)
                  .append('labelerName', 4)
                  .append('nonProprietaryName', 3)
                  .append('substances.name', 2)
                  .append('pharmacologicalClassCategories.name', 2)))
          }

        } catch (final Exception exception) {

          log.error("An error occurred building the mongo indexes...", exception)

        } finally {

          mongoClient?.close()
        }
      }
    }
  }
}
