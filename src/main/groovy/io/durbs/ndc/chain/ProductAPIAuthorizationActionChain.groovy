package io.durbs.ndc.chain

import com.google.common.net.HttpHeaders
import com.google.inject.Inject
import com.google.inject.Singleton
import io.durbs.ndc.command.api.auth.ValidateAuthenticationToken
import io.durbs.ndc.domain.auth.APIAuthResult
import io.durbs.ndc.service.AuthenticationService
import ratpack.groovy.handling.GroovyChainAction

@Singleton
class ProductAPIAuthorizationActionChain extends GroovyChainAction {

  @Inject
  AuthenticationService authorizationService

  @Override
  void execute() throws Exception {

    all {

      if (request.headers.contains(HttpHeaders.AUTHORIZATION)) {

        new ValidateAuthenticationToken(authorizationService, request.headers.get(HttpHeaders.AUTHORIZATION))
          .observe()
          .singleOrDefault(false)
          .subscribe { Boolean value ->

          final APIAuthResult apiAuthResult = new APIAuthResult(authenticated: value)

          context.next(single(apiAuthResult))
        }
      } else {
        context.next(single(new APIAuthResult(authenticated: false)))
      }
    }
  }
}
