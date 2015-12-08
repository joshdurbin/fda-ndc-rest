package io.durbs.ndc.command.api.product

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.CacheService
import io.durbs.ndc.service.ProductService
import ratpack.handling.Context
import rx.Observable

@CompileStatic
class GetProductTypeNames extends HystrixObservableCommand<String> {

  private static final String DISTINCT_PROPERTY_KEY = 'productTypeName'
  private static final String CACHE_KEY = 'productTypes'

  final CacheService cacheService
  final ProductService productService

  public GetProductTypeNames(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetProductTypeNames'))

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
