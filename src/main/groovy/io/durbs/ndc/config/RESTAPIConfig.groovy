package io.durbs.ndc.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class RESTAPIConfig {

  Integer defaultResultsPageSize
  Integer maxResultsPageSize
  Integer defaultFirstPage
}
