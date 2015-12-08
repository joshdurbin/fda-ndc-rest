package io.durbs.ndc.command.api

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.CacheService
import io.durbs.ndc.service.ProductService
import ratpack.handling.Context
import rx.Observable

@CompileStatic
class GetMarketingCategoryNames extends HystrixObservableCommand<String> {

  private static final String DISTINCT_PROPERTY_KEY = 'marketingCategoryName'
  private static final String CACHE_KEY = 'marketingCategories'

  final CacheService cacheService
  final ProductService productService

  GetMarketingCategoryNames(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetMarketingCategoryNames'))

    this.productService = context.get(ProductService)
    this.cacheService = context.get(CacheService)
  }

  @Override
  protected Observable<String> construct() {

    cacheService.stringsCache.smembers(CACHE_KEY).bindExec()
      .switchIfEmpty(
        productService.getDistinctList(DISTINCT_PROPERTY_KEY)
        .doOnNext { String categoryName ->
          cacheService.stringsCache.sadd(CACHE_KEY, categoryName).subscribe() }
      )
  }
}
