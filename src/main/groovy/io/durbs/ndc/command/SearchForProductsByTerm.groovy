package io.durbs.ndc.command

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class SearchForProductsByTerm extends HystrixObservableCommand<Product> {

  final ProductService productService
  final String searchTerm

  public SearchForProductsByTerm(ProductService productService, String searchTerm) {
    super(HystrixCommandGroupKey.Factory.asKey('SearchForProductsByTerm'))
    this.productService = productService
    this.searchTerm = searchTerm
  }

  @Override
  protected Observable<Product> construct() {

    productService.searchForProductsByTerms(searchTerm)
  }

  @Override
  protected Observable<Product> resumeWithFallback() {

    Observable.empty()
  }
}
