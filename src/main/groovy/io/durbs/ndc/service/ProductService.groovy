package io.durbs.ndc.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.client.result.DeleteResult
import com.mongodb.rx.client.MongoDatabase
import com.mongodb.rx.client.Success
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.config.MongoConfig
import io.durbs.ndc.domain.product.Product
import org.bson.Document
import org.bson.conversions.Bson
import rx.Observable
import rx.functions.Func1

@CompileStatic
@Singleton
@Slf4j
class ProductService {

  @Inject
  MongoDatabase mongoDatabase

  @Inject
  MongoConfig mongoConfig

  Observable<Product> getProducts(final Bson queryFilter,
                                  final Bson sortCriteria,
                                  final Bson projectionDocument,
                                  final Integer limit,
                                  final Integer recordsToSkip) {

    mongoDatabase
      .getCollection(mongoConfig.collection, Product)
      .find(queryFilter)
      .sort(sortCriteria)
      .projection(projectionDocument)
      .limit(limit)
      .skip(recordsToSkip)
      .toObservable()
      .bindExec()
  }

  Observable<Product> saveProduct(final Product product) {

    mongoDatabase
      .getCollection(mongoConfig.collection, Product)
      .insertOne(product)
      .map( { Success success ->

      product
    } as Func1)
      .bindExec()
  }

  Observable<DeleteResult> removeProduct(final Bson queryFilter) {

    mongoDatabase
      .getCollection(mongoConfig.collection, Product)
      .deleteOne(queryFilter)
      .bindExec()
  }

  // temporary
  Observable<Long> replaceAllProducts(final List<Product> products) {

    mongoDatabase
      .getCollection(mongoConfig.collection, Product)
      .deleteMany(new Document())
      .flatMap({ final DeleteResult deleteResult ->

      mongoDatabase
        .getCollection(mongoConfig.collection, Product)
        .insertMany(products)
    } as Func1)
      .countLong()
      .bindExec()
  }

  Observable<String> getDistinctList(final String propertyToQuery,
                                     final Bson filter) {

    mongoDatabase
      .getCollection(mongoConfig.collection, Product)
      .distinct(propertyToQuery, String)
      .filter(filter)
      .toObservable()
      .bindExec()
  }

}
