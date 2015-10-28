package io.durbs.ndc.command

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class GetTeaserProducts extends HystrixObservableCommand<Product> {

  final ProductService productService

  public GetTeaserProducts(ProductService productService) {
    super(HystrixCommandGroupKey.Factory.asKey('GetTeaserProducts'))
    this.productService = productService
  }

  @Override
  protected Observable<Product> construct() {

    productService.getTeaserProducts()
  }

  @Override
  protected Observable<Product> resumeWithFallback() {

    Observable.empty()
  }
}
