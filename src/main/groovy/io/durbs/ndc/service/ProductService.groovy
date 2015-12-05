package io.durbs.ndc.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.domain.product.Product
import org.bson.conversions.Bson
import rx.Observable

@CompileStatic
@Singleton
@Slf4j
class ProductService {

  @Inject
  MongoDatabase mongoDatabase

  private static final String MONGO_COLLECTION = 'products'

  Observable<Product> getProducts(final Bson queryFilter,
                                  final Bson sortCriteria,
                                  final Bson projectionDocument,
                                  final Integer limit,
                                  final Integer recordsToSkip) {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .find(queryFilter)
      .sort(sortCriteria)
      .projection(projectionDocument)
      .limit(limit)
      .skip(recordsToSkip)
      .toObservable()
      .bindExec()
  }

  Observable<String> getDistinctList(final String propertyToQuery) {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .distinct(propertyToQuery, String)
      .toObservable()
      .bindExec()
  }


}
