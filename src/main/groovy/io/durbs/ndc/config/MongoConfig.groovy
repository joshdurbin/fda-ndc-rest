package io.durbs.ndc.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class MongoConfig {

  String db
  String uri
}
