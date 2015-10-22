package io.durbs.ndc

import com.google.inject.Inject
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
class ProductLookupService {

  @Inject
  NDCRestConfig config

  private MongoDatabase mongoDatabase

  ProductLookupService() {

    final ConnectionString connectionString = new ConnectionString('mongodb://heroku_33rvtbr0:go1epcr944593v04cqrik8t01u@ds029824.mongolab.com:29824/heroku_33rvtbr0')
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
    mongoDatabase = rxMongoClient.getDatabase('heroku_33rvtbr0')
  }

  Observable<Product> getAll() {

    mongoDatabase
      .getCollection('products', Product)
      .find()
      .limit(10)
      .toObservable()
      .bindExec()
  }

  Observable<Product> search(final String searchTerm) {

    mongoDatabase
      .getCollection('products', Product)
      .find(text(searchTerm))
      .limit(10)
      .toObservable()
      .bindExec()
  }

  Observable<Product> getByNDCCode(final String ndcCode) {

    mongoDatabase
      .getCollection('products', Product)
      .find(eq('productNDC', ndcCode))
      .limit(10)
      .toObservable()
      .bindExec()
  }

}
