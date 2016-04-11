package io.durbs.ndc.command.api.auth

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.AuthenticationService
import rx.Observable

@CompileStatic
class ValidateAuthenticationToken extends HystrixObservableCommand<Boolean> {

  final AuthenticationService authorizationService
  final String authenticationToken

  ValidateAuthenticationToken(AuthenticationService authorizationService, String authenticationToken) {
    super(HystrixCommandGroupKey.Factory.asKey('ValidateAuthenticationToken'))

    this.authorizationService = authorizationService
    this.authenticationToken = authenticationToken
  }

  @Override
  protected Observable<Boolean> construct() {

    authorizationService.authCodeExists(authenticationToken).bindExec()
  }

}