package io.durbs.ndc.command.api

import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.service.ProductService
import org.bson.conversions.Bson
import ratpack.handling.Context
import rx.Observable

import static com.mongodb.client.model.Filters.gte

@CompileStatic
class GetRandomProduct extends HystrixObservableCommand<Product> {

  private static final Integer PAGE_SIZE = 1

  final ProductService productService
  final GetRandomProductRequestParameters requestParameters

  GetRandomProduct(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetRandomProduct'))

    RedisReactiveCommands commands = context.get(RedisReactiveCommands)
    this.productService = context.get(ProductService)
    this.requestParameters = new GetRandomProductRequestParameters(context)
  }

  @Override
  protected Observable<Product> construct() {

    productService.getProducts(requestParameters.queryFilter,
      requestParameters.sortCriteria,
      requestParameters.projectionDocument,
      requestParameters.pageSize,
      requestParameters.getOffSet())
  }

  static class GetRandomProductRequestParameters extends BaseAPIRequestParameters {

    GetRandomProductRequestParameters(Context context) {
      super(context)
    }

    Bson getQueryFilter() {

      gte('randomKey', Math.random())
    }

    Integer getPageSize() {

      PAGE_SIZE
    }

  }

}
