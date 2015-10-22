package io.durbs.ndc

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class NDCRestConfig {

  Integer defaultResultsPageSize
  Integer maxResultsPageSize
  Integer defaultFirstPage

  String lookupServiceDB
  String lookupServiceURI
  String lookupServiceCollection
}
