package io.durbs.ndc.command.api

import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class GetProductsByNDCCodeCached extends HystrixObservableCommand<Product> {

  final ProductService productService
  final RedisReactiveCommands<String, Product> redisProductsCache
  final GetProductsByNDCCode.GetProductsByNDCCodeRequestParameters requestParameters

  GetProductsByNDCCodeCached(ProductService productService, RedisReactiveCommands<String, Product> redisProductsCache, GetProductsByNDCCode.GetProductsByNDCCodeRequestParameters requestParameters) {
    super(HystrixCommandGroupKey.Factory.asKey('GetProductsByNDCCode'))

    this.requestParameters = requestParameters
    this.productService = productService
    this.redisProductsCache = redisProductsCache
  }

  @Override
  protected Observable<Product> construct() {

    Observable.concat(redisProductsCache.get(requestParameters.productNDC).bindExec(),
      productService.getProducts(requestParameters.queryFilter,
        requestParameters.sortCriteria,
        requestParameters.projectionDocument,
        requestParameters.pageSize,
        requestParameters.getOffSet())
          .doOnNext { Product product -> redisProductsCache.set(product.productNDC, product).subscribe() }
    ).first()
  }

}
