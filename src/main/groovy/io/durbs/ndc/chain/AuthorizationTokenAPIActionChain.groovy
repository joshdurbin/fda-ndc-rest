package io.durbs.ndc.chain

import com.google.inject.Inject
import com.google.inject.Singleton
import io.durbs.ndc.service.AuthenticationService
import ratpack.groovy.handling.GroovyChainAction

@Singleton
class AuthorizationTokenAPIActionChain extends GroovyChainAction {

  @Inject
  AuthenticationService authorizationService

  @Override
  void execute() throws Exception {

    post {

      authorizationService
        .createAPIAuthorizationRecord('Josh', 'durbinjo593@gmail.com')
        .subscribe { Boolean ok ->

        render "Yeah, ok. I made it: '{ok}'"
      }
    }

    delete {

      authorizationService
    }
  }
}
