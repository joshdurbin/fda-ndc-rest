package io.durbs.ndc.command.auth

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.service.CacheService
import rx.Observable

@CompileStatic
class ValidateBearerToken extends HystrixObservableCommand<String> {

  final CacheService cacheService
  final String bearerToken

  ValidateBearerToken(CacheService cacheService, String bearerToken) {
    super(HystrixCommandGroupKey.Factory.asKey('ValidateBearerToken'))

    this.cacheService = cacheService
    this.bearerToken = bearerToken
  }

  @Override
  protected Observable<String> construct() {

    cacheService.stringsCache.get(bearerToken).bindExec()
  }

}