package io.durbs.ndc.command.api

import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class GetMarketingCategoryNames extends HystrixObservableCommand<String> {

  private static final String DISTINCT_PROPERTY_KEY = 'marketingCategoryName'
  private static final String CACHE_KEY = 'marketingCategories'

  final RedisReactiveCommands<String, String> cache
  final ProductService productService

  GetMarketingCategoryNames(ProductService productService, RedisReactiveCommands<String, String> cache) {
    super(HystrixCommandGroupKey.Factory.asKey('GetMarketingCategoryNames'))

    this.productService = productService
    this.cache = cache
  }

  @Override
  protected Observable<String> construct() {

    Observable.concat(cache.smembers(CACHE_KEY).bindExec(),
      productService.getDistinctList(DISTINCT_PROPERTY_KEY)
        .doOnNext { String categoryName -> cache.sadd(CACHE_KEY, categoryName).subscribe() })
  }

}
