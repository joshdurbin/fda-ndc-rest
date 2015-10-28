package io.durbs.ndc.command

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class GetAllProducts extends HystrixObservableCommand<Product> {

  final ProductService productService

  public GetAllProducts(ProductService productService) {
    super(HystrixCommandGroupKey.Factory.asKey('GetAllProducts'))
    this.productService = productService
  }

  @Override
  protected Observable<Product> construct() {

    productService.getAllProducts()
  }

  @Override
  protected Observable<Product> resumeWithFallback() {

    Observable.empty()
  }
}
