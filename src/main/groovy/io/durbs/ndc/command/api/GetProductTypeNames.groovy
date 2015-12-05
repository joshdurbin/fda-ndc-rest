package io.durbs.ndc.command.api

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.ProductService
import ratpack.handling.Context
import rx.Observable

@CompileStatic
class GetProductTypeNames extends HystrixObservableCommand<String> {

  private static final String DISTINCT_PROPERTY_KEY = 'productTypeName'

  final ProductService productService

  public GetProductTypeNames(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetProductTypeNames'))

    this.productService = context.get(ProductService)
  }

  @Override
  protected Observable<String> construct() {

    productService.getDistinctList(DISTINCT_PROPERTY_KEY)
  }

}
