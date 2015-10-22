package io.durbs.ndc

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.ConnectionString
import com.mongodb.MongoClient
import com.mongodb.async.client.MongoClientSettings
import com.mongodb.connection.ClusterSettings
import com.mongodb.connection.ConnectionPoolSettings
import com.mongodb.connection.ServerSettings
import com.mongodb.connection.SocketSettings
import com.mongodb.connection.SslSettings
import com.mongodb.rx.client.MongoClient as RXMongoClient
import com.mongodb.rx.client.MongoClients
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.Product
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import rx.Observable

import static com.mongodb.client.model.Filters.eq
import static com.mongodb.client.model.Filters.text

@CompileStatic
@Singleton
class ProductLookupService {

  private NDCRestConfig config
  private MongoDatabase mongoDatabase

  @Inject
  ProductLookupService(NDCRestConfig config) {

    this.config = config

    final ConnectionString connectionString = new ConnectionString(config.lookupServiceURI)
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

    RXMongoClient rxMongoClient = MongoClients.create(mongoClientSettings)
    mongoDatabase = rxMongoClient.getDatabase(config.lookupServiceDB)
  }

  Observable<Product> getAll() {

    mongoDatabase
      .getCollection(config.lookupServiceCollection, Product)
      .find()
      .limit(10)
      .toObservable()
      .bindExec()
  }

  Observable<Product> search(final String searchTerm) {

    mongoDatabase
      .getCollection(config.lookupServiceCollection, Product)
      .find(text(searchTerm))
      .toObservable()
      .bindExec()
  }

  Observable<Product> getByNDCCode(final String ndcCode) {

    mongoDatabase
      .getCollection(config.lookupServiceCollection, Product)
      .find(eq('productNDC', ndcCode))
      .limit(10)
      .toObservable()
      .bindExec()
  }

}
