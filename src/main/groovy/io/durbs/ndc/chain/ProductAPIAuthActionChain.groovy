package io.durbs.ndc.chain

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import io.durbs.ndc.command.auth.ValidateBearerToken
import io.durbs.ndc.domain.APIAuthResult

import ratpack.groovy.handling.GroovyChainAction

@Singleton
class ProductAPIAuthActionChain extends GroovyChainAction {

  static final String API_KEY_HEADER_KEY = 'key'

  static final String RATE_LIMIT_CEILING_HEADER_KEY = 'X-Rate-Limit-Ceiling'
  static final String RATE_LIMIT_REMAINING_HEADER_KEY = 'X-Rate-Limit-Remaining'
  static final String RATE_LIMIT_NEXT_WINDOW_HEADER_KEY = 'X-Rate-Limit-Next-Window'

  @Inject
  RedisReactiveCommands<String, String> stringRedisCommands

  @Override
  void execute() throws Exception {

    all {

      if (request.headers.contains(API_KEY_HEADER_KEY)) {

        new ValidateBearerToken(stringRedisCommands, request.headers.get(API_KEY_HEADER_KEY))
          .observe()
          .singleOrDefault('')
          .subscribe { String value ->

          final APIAuthResult apiAuthResult = new APIAuthResult(authenticated: value == 'true')

          if (apiAuthResult.authenticated) {

            response.headers.add(RATE_LIMIT_CEILING_HEADER_KEY, '100')
            response.headers.add(RATE_LIMIT_REMAINING_HEADER_KEY, '99')
            response.headers.add(RATE_LIMIT_NEXT_WINDOW_HEADER_KEY, '3600')
          }

          context.next(single(apiAuthResult))
        }
      } else {
        context.next(single(new APIAuthResult(authenticated: false)))
      }
    }
  }
}
