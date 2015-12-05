package io.durbs.ndc.command.auth

import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic

import rx.Observable

@CompileStatic
class ValidateBearerToken extends HystrixObservableCommand<String> {

  final RedisReactiveCommands<String, String> stringRedisCommands
  final String bearerToken

  ValidateBearerToken(RedisReactiveCommands<String, String> stringRedisCommands, String bearerToken) {
    super(HystrixCommandGroupKey.Factory.asKey('ValidateBearerToken'))

    this.stringRedisCommands = stringRedisCommands
    this.bearerToken = bearerToken
  }

  @Override
  protected Observable<String> construct() {

    stringRedisCommands.get(bearerToken).bindExec()
  }

}