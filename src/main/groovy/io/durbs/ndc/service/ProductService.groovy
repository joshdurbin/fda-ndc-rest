package io.durbs.ndc.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.rx.client.MongoDatabase
import com.mongodb.rx.client.Success
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.domain.product.Product
import rx.Observable
import rx.functions.Func1

import static com.mongodb.client.model.Projections.fields
import static com.mongodb.client.model.Projections.include
import static com.mongodb.client.model.Filters.eq
import static com.mongodb.client.model.Filters.text

@CompileStatic
@Singleton
@Slf4j
class ProductService {

  @Inject
  MongoDatabase mongoDatabase

  private static final String MONGO_COLLECTION = 'products'
  private static final List<String> TEASER_PRODUCT_PROPERTIES = [ 'productNDC',
                                                                  'proprietaryName',
                                                                  'startMarketingDate',
                                                                  'endMarketingDate',
                                                                  'labelerName',
                                                                  'substances' ]

  Observable<Long> getTotalNumberOfProducts() {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .count()
      .bindExec()
  }

  Observable<Product> getAllProducts() {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .find()
      .limit(5)
      .toObservable()
      .bindExec()
  }

  Observable<Product> searchForProductsByTerms(final String searchTerm) {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .find(text(searchTerm))
      .limit(20)
      .toObservable()
      .bindExec()
  }

  Observable<Product> getProductsByNDCCode(final String ndcCode) {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .find(eq('productNDC', ndcCode))
      .limit(20)
      .toObservable()
      .bindExec()
  }

  Observable<String> getProductTypeNames() {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .distinct('productTypeName', String)
      .toObservable()
      .bindExec()
  }

  Observable<String> getMarketingCategoryNames() {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .distinct('marketingCategoryName', String)
      .toObservable()
      .bindExec()
  }

  Observable<Product> getRandomProduct() {

    getTotalNumberOfProducts().flatMap({ Long totalNumberOfProducts ->

      log.info("Selecting, random product, skipping ${totalNumberOfProducts} products...")

      mongoDatabase
        .getCollection(MONGO_COLLECTION, Product)
        .find()
        .limit(1)
        .skip(Math.floor(Math.random() * totalNumberOfProducts) as Integer)
        .toObservable()

    } as Func1).bindExec()
  }

  Observable<Product> getTeaserProducts() {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .find()
      .limit(20)
      .projection(fields(include(TEASER_PRODUCT_PROPERTIES)))
      .toObservable()
      .bindExec()
  }

  Observable<Success> saveProduct(final Product product) {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, Product)
      .insertOne(product)
      .asObservable()
      .bindExec()
  }

}
