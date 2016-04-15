package io.durbs.ndc.command.api.product

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.CacheService
import io.durbs.ndc.service.ProductService
import org.bson.Document
import ratpack.handling.Context
import rx.Observable

@CompileStatic
class GetLabelerNames extends HystrixObservableCommand<String> {

  private static final String DISTINCT_PROPERTY_KEY = 'labelerName'
  private static final String CACHE_KEY = 'labelerNames'

  final CacheService cacheService
  final ProductService productService

  GetLabelerNames(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetLabelerNames'))

    this.productService = context.get(ProductService)
    this.cacheService = context.get(CacheService)
  }

  @Override
  protected Observable<String> construct() {

    cacheService.stringsCache.smembers(CACHE_KEY).bindExec()
      .switchIfEmpty(
        productService.getDistinctList(DISTINCT_PROPERTY_KEY, new Document())
        .doOnNext { String labelerName ->
          cacheService.stringsCache.sadd(CACHE_KEY, labelerName).subscribe() }
      )
  }
}
