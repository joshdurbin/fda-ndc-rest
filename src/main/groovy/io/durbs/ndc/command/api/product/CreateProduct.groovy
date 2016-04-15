package io.durbs.ndc.command.api.product

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import ratpack.handling.Context
import rx.Observable
import rx.functions.Func1

import static ratpack.jackson.Jackson.fromJson

@CompileStatic
class CreateProduct extends HystrixObservableCommand<Product> {

  final Context context
  final ProductService productService
  final BaseAPIRequestParameters requestParameters

  CreateProduct(Context context) {

    super(HystrixCommandGroupKey.Factory.asKey('CreateProduct'))

    this.context = context
    this.productService = context.get(ProductService)
    this.requestParameters = new BaseAPIRequestParameters(context)
  }

  @Override
  protected Observable<Product> construct() {

    context.parse(fromJson(Product))
      .observe()
      .flatMap ({ final Product product ->

      productService.saveProduct(product)
    } as Func1)
      .bindExec()
  }
}
