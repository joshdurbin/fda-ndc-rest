package io.durbs.ndc.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.rx.client.MongoDatabase
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic

import io.durbs.ndc.domain.product.Product
import rx.Observable

import static com.mongodb.client.model.Projections.fields
import static com.mongodb.client.model.Projections.include
import static com.mongodb.client.model.Filters.eq
import static com.mongodb.client.model.Filters.text

@CompileStatic
@Singleton
class ProductService {

  @Inject
  MongoDatabase mongoDatabase

  private static final String MONGO_COLLECTION = 'products'
  private static final HystrixCommandGroupKey HYSTRIX_COMMAND_GROUP_KEY = HystrixCommandGroupKey.Factory.asKey('product-service')
  private static final List<String> TEASER_PRODUCT_PROPERTIES = [ 'productNDC', 'proprietaryName', 'startMarketingDate', 'endMarketingDate', 'labelerName', 'substances' ]

  Observable<Long> getTotalNumberOfProducts() {

    new HystrixObservableCommand<Long>(
      HystrixObservableCommand.Setter.withGroupKey(HYSTRIX_COMMAND_GROUP_KEY).andCommandKey(HystrixCommandKey.Factory.asKey('get-total-number-of-products'))) {

      @Override
      protected Observable<Long> construct() {

        mongoDatabase
          .getCollection(MONGO_COLLECTION, Product)
          .count()
      }

      @Override
      protected Observable<Long> resumeWithFallback() {

        Observable.just(0L)
      }
    }.toObservable().bindExec()
  }

  Observable<Product> getAllProducts() {

    new HystrixObservableCommand<Product>(
      HystrixObservableCommand.Setter.withGroupKey(HYSTRIX_COMMAND_GROUP_KEY).andCommandKey(HystrixCommandKey.Factory.asKey('get-all-products'))) {

      @Override
      protected Observable<Product> construct() {

        mongoDatabase
          .getCollection(MONGO_COLLECTION, Product)
          .find()
          .limit(20)
          .toObservable()
      }

      @Override
      protected Observable<Product> resumeWithFallback() {

        Observable.empty()
      }
    }.toObservable().bindExec()
  }

  Observable<Product> searchForProductsByTerms(final String searchTerm) {

    new HystrixObservableCommand<Product>(
      HystrixObservableCommand.Setter.withGroupKey(HYSTRIX_COMMAND_GROUP_KEY).andCommandKey(HystrixCommandKey.Factory.asKey('search-for-products-by-term'))) {

      @Override
      protected Observable<Product> construct() {

        mongoDatabase
          .getCollection(MONGO_COLLECTION, Product)
          .find(text(searchTerm))
          .limit(20)
          .toObservable()
      }

      @Override
      protected Observable<Product> resumeWithFallback() {

        Observable.empty()
      }
    }.toObservable().bindExec()
  }

  Observable<Product> getProductsByNDCCode(final String ndcCode) {

    new HystrixObservableCommand<Product>(
      HystrixObservableCommand.Setter.withGroupKey(HYSTRIX_COMMAND_GROUP_KEY).andCommandKey(HystrixCommandKey.Factory.asKey('get-products-by-ndc-code'))) {

      @Override
      protected Observable<Product> construct() {

        mongoDatabase
          .getCollection(MONGO_COLLECTION, Product)
          .find(eq('productNDC', ndcCode))
          .limit(20)
          .toObservable()
      }

      @Override
      protected Observable<Product> resumeWithFallback() {

        Observable.empty()
      }
    }.toObservable().bindExec()
  }

  Observable<String> getProductTypeNames() {

    new HystrixObservableCommand<String>(
      HystrixObservableCommand.Setter.withGroupKey(HYSTRIX_COMMAND_GROUP_KEY).andCommandKey(HystrixCommandKey.Factory.asKey('get-product-type-names'))) {

      @Override
      protected Observable<String> construct() {

        mongoDatabase
          .getCollection(MONGO_COLLECTION, Product)
          .distinct('productTypeName', String)
          .toObservable()
      }

      @Override
      protected Observable<String> resumeWithFallback() {

        Observable.empty()
      }
    }.toObservable().bindExec()
  }

  Observable<String> getMarketingCategoryNames() {

    new HystrixObservableCommand<String>(
      HystrixObservableCommand.Setter.withGroupKey(HYSTRIX_COMMAND_GROUP_KEY).andCommandKey(HystrixCommandKey.Factory.asKey('get-marketing-category-names'))) {

      @Override
      protected Observable<String> construct() {

        mongoDatabase
          .getCollection(MONGO_COLLECTION, Product)
          .distinct('marketingCategoryName', String)
          .toObservable()
      }

      @Override
      protected Observable<String> resumeWithFallback() {

        Observable.empty()
      }
    }.toObservable().bindExec()
  }

  Observable<Product> getRandomProduct() {

//    new HystrixObservableCommand<Product>(
//      HystrixObservableCommand.Setter.withGroupKey(HYSTRIX_COMMAND_GROUP_KEY).andCommandKey(HystrixCommandKey.Factory.asKey('get-random-product'))) {
//
//      @Override
//      protected Observable<Product> construct() {

        getTotalNumberOfProducts().flatMap {

          mongoDatabase
            .getCollection(MONGO_COLLECTION, Product)
            .find()
            .limit(1)
            .skip(Math.floor(Math.random() * it) as Integer)
            .toObservable()
            .bindExec()
        }
//      }
//
//    }.toObservable().bindExec()
  }

  Observable<Product> getTeaserProducts() {

    new HystrixObservableCommand<Product>(
      HystrixObservableCommand.Setter.withGroupKey(HYSTRIX_COMMAND_GROUP_KEY).andCommandKey(HystrixCommandKey.Factory.asKey('get-teaser-products'))) {

      @Override
      protected Observable<Product> construct() {

        mongoDatabase
          .getCollection(MONGO_COLLECTION, Product)
          .find()
          .limit(20)
          .projection(fields(include(TEASER_PRODUCT_PROPERTIES)))
          .toObservable()
      }

      @Override
      protected Observable<Product> resumeWithFallback() {

        Observable.empty()
      }
    }.toObservable().bindExec()
  }

}
