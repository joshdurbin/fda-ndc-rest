package io.durbs.ndc.command

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class GetProductTypeNames extends HystrixObservableCommand<String> {

  final ProductService productService

  public GetProductTypeNames(ProductService productService) {
    super(HystrixCommandGroupKey.Factory.asKey('GetProductTypeNames'))
    this.productService = productService
  }

  @Override
  protected Observable<String> construct() {

    productService.getProductTypeNames()
  }

  @Override
  protected Observable<String> resumeWithFallback() {

    Observable.empty()
  }
}
