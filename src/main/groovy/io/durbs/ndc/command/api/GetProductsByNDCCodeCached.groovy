package io.durbs.ndc.command.api

import com.lambdaworks.redis.SetArgs
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.config.RedisConfig
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.CacheService
import io.durbs.ndc.service.ProductService
import ratpack.handling.Context
import rx.Observable

@CompileStatic
class GetProductsByNDCCodeCached extends HystrixObservableCommand<Product> {

  final ProductService productService
  final CacheService cacheService
  final GetProductsByNDCCode.GetProductsByNDCCodeRequestParameters requestParameters
  final RedisConfig redisConfig

  GetProductsByNDCCodeCached(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetProductsByNDCCode'))

    this.requestParameters = new GetProductsByNDCCode.GetProductsByNDCCodeRequestParameters(context)
    this.productService = context.get(ProductService)
    this.cacheService = context.get(CacheService)
    this.redisConfig = context.get(RedisConfig)
  }

  @Override
  protected Observable<Product> construct() {

    Observable.concat(cacheService.productsCache.get(requestParameters.productNDC).bindExec(),
      productService.getProducts(requestParameters.queryFilter,
        requestParameters.sortCriteria,
        requestParameters.projectionDocument,
        requestParameters.pageSize,
        requestParameters.getOffSet())
          .doOnNext { Product product -> cacheService.productsCache.set(product.productNDC, product, SetArgs.Builder.ex(redisConfig.cacheTTLInSeconds)).subscribe() }
    ).first()
  }

}
