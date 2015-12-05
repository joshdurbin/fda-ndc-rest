package io.durbs.ndc.error

import groovy.transform.CompileStatic
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context

@CompileStatic
class ErrorHandler implements ServerErrorHandler {

  @Override
  void error(Context context, Throwable throwable) throws Exception {

    context.wait()
  }
}
