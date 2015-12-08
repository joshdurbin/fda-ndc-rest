package io.durbs.ndc.command.api

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.config.RedisConfig
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.service.CacheService
import io.durbs.ndc.service.ProductService
import org.bson.conversions.Bson
import ratpack.handling.Context
import rx.Observable

import static com.mongodb.client.model.Filters.eq

@CompileStatic
class GetProductsByNDCCode extends HystrixObservableCommand<Product> {

  final ProductService productService
  final CacheService cacheService
  final GetProductsByNDCCodeRequestParameters requestParameters
  final RedisConfig redisConfig

  GetProductsByNDCCode(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetProductsByNDCCode'))

    this.requestParameters = new GetProductsByNDCCodeRequestParameters(context)
    this.productService = context.get(ProductService)
    this.cacheService = context.get(CacheService)
    this.redisConfig = context.get(RedisConfig)
  }

  @Override
  protected Observable<Product> construct() {

    cacheService.productsCache.get(requestParameters.productNDC).bindExec()
      .switchIfEmpty(
        productService.getProducts(requestParameters.queryFilter,
        requestParameters.sortCriteria,
        requestParameters.projectionDocument,
        requestParameters.pageSize,
        requestParameters.getOffSet())
          .doOnNext { Product product ->
            cacheService.productsCache.set(product.productNDC, product).bindExec().subscribe()
          }
    )
  }

  static class GetProductsByNDCCodeRequestParameters extends BaseAPIRequestParameters {

    static final String NDC_CODE_PATH_TOKEN = 'ndcCode'
    static final String DEFAULT_NDC_CODE_PATH_TOKEN = ''

    GetProductsByNDCCodeRequestParameters(Context context) {
      super(context)
    }

    String getProductNDC() {
      context.pathTokens.get(NDC_CODE_PATH_TOKEN, DEFAULT_NDC_CODE_PATH_TOKEN)
    }

    Bson getQueryFilter() {

      eq('productNDC', getProductNDC())
    }
  }

}
