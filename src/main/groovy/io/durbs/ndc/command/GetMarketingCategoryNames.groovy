package io.durbs.ndc.command

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class GetMarketingCategoryNames extends HystrixObservableCommand<String> {

  final ProductService productService

  public GetMarketingCategoryNames(ProductService productService) {
    super(HystrixCommandGroupKey.Factory.asKey('GetMarketingCategoryNames'))
    this.productService = productService
  }

  @Override
  protected Observable<String> construct() {

    productService.getMarketingCategoryNames()
  }

  @Override
  protected Observable<String> resumeWithFallback() {

    Observable.empty()
  }
}
