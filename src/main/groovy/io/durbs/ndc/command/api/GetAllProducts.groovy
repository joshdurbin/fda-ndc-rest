package io.durbs.ndc.command.api

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import ratpack.handling.Context
import rx.Observable

@CompileStatic
class GetAllProducts extends HystrixObservableCommand<Product> {

  final ProductService productService
  final BaseAPIRequestParameters requestParameters

  GetAllProducts(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetAllProducts'))

    this.productService = context.get(ProductService)
    this.requestParameters = new BaseAPIRequestParameters(context)
  }

  @Override
  protected Observable<Product> construct() {

    productService.getProducts(requestParameters.queryFilter,
      requestParameters.sortCriteria,
      requestParameters.projectionDocument,
      requestParameters.pageSize,
      requestParameters.getOffSet())
  }

}
