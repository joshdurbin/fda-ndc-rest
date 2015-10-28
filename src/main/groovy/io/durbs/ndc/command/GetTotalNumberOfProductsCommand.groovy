package io.durbs.ndc.command

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class GetTotalNumberOfProductsCommand extends HystrixObservableCommand<Long> {

  final ProductService productService

  public GetTotalNumberOfProductsCommand(ProductService productService) {
    super(HystrixCommandGroupKey.Factory.asKey('GetTotalNumberOfProductsCommand'))
    this.productService = productService
  }

  @Override
  protected Observable<Long> construct() {

    productService.getTotalNumberOfProducts()
  }

  @Override
  protected Observable<Long> resumeWithFallback() {

    Observable.just(0L)
  }
}
