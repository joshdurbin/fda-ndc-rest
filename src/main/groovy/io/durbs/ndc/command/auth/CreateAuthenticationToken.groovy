package io.durbs.ndc.command.auth

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import ratpack.handling.Context
import rx.Observable

@CompileStatic
class CreateAuthenticationToken extends HystrixObservableCommand<Product> {

  final Context context

  public CreateAuthenticationToken(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('CreateAuthenticationToken'))

    this.context = context
  }

  @Override
  protected Observable<Product> construct() {

  }
}
