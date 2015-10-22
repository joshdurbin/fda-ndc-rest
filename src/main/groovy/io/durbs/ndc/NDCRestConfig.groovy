package io.durbs.ndc

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class NDCRestConfig {

  Integer defaultResultsPageSize
  Integer maxResultsPageSize
  Integer defaultFirstPage

  String db
  String uri
  String collection
}
