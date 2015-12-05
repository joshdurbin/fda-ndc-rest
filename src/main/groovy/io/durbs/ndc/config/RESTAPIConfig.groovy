package io.durbs.ndc.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class RESTAPIConfig {

  private static final String CONFIG_LIST_SPLIT_CHAR = ','

  Integer defaultResultsPageSize
  Integer maxResultsPageSize
  Integer defaultFirstPage
  String teaserProductProperties
  String sortProperties
  Integer defaultRateLimitCeiling
  Integer defaultRateLimitRemaining
  Integer defaultRateLimitNextWindow

  List<String> getTeaserProductPropertiesAsList() {

    teaserProductProperties.split(CONFIG_LIST_SPLIT_CHAR) as List<String>
  }

  List<String> getSortPropertiesAsList() {

    sortProperties.split(CONFIG_LIST_SPLIT_CHAR) as List<String>
  }
}
