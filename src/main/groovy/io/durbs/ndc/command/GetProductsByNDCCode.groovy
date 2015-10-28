package io.durbs.ndc.command

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import rx.Observable

@CompileStatic
class GetProductsByNDCCode extends HystrixObservableCommand<Product> {

  final ProductService productService
  final String ndcCode

  public GetProductsByNDCCode(ProductService productService, String ndcCode) {
    super(HystrixCommandGroupKey.Factory.asKey('GetProductsByNDCCode'))
    this.productService = productService
    this.ndcCode = ndcCode
  }

  @Override
  protected Observable<Product> construct() {

    productService.getProductsByNDCCode(ndcCode)
  }

  @Override
  protected Observable<Product> resumeWithFallback() {

    Observable.empty()
  }
}
