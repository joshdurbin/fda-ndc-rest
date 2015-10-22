package io.durbs.ndc

import com.google.inject.AbstractModule
import groovy.transform.CompileStatic

@CompileStatic
class NDCRestModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(ProductActionChain)
    bind(ProductLookupService)
  }
}
