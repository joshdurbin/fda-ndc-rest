package io.durbs.ndc.command.api.product

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import org.bson.conversions.Bson
import ratpack.handling.Context
import rx.Observable

import static com.mongodb.client.model.Filters.gte
import static com.mongodb.client.model.Filters.lte
import static com.mongodb.client.model.Filters.or

@CompileStatic
class GetInactiveProducts extends HystrixObservableCommand<Product> {

  final ProductService productService
  final GetInactiveProductsRequestParameters requestParameters

  GetInactiveProducts(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetInactiveProducts'))

    this.productService = context.get(ProductService)
    this.requestParameters = new GetInactiveProductsRequestParameters(context)
  }

  @Override
  protected Observable<Product> construct() {

    productService.getProducts(requestParameters.queryFilter,
      requestParameters.sortCriteria,
      requestParameters.projectionDocument,
      requestParameters.pageSize,
      requestParameters.getOffSet())
  }

  static class GetInactiveProductsRequestParameters extends BaseAPIRequestParameters {

    GetInactiveProductsRequestParameters(Context context) {
      super(context)
    }

    Bson getQueryFilter() {

      or(gte('startMarketingDate', new Date()), lte('endMarketingDate', new Date()))
    }
  }

}
