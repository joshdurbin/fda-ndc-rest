package io.durbs.ndc.command.api

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.service.ProductService
import org.apache.commons.lang.math.NumberUtils
import org.bson.conversions.Bson
import ratpack.handling.Context
import rx.Observable

import static com.mongodb.client.model.Filters.gte

@CompileStatic
class GetRandomProduct extends HystrixObservableCommand<Product> {

  final ProductService productService
  final GetRandomProductRequestParameters requestParameters

  GetRandomProduct(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetRandomProduct'))

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

      NumberUtils.INTEGER_ONE
    }

  }

}
