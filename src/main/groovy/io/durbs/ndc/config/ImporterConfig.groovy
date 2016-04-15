package io.durbs.ndc.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class ImporterConfig {

  String ndcDirectoryPage
  String ndcDatabaseFileURL
  Long refreshCheckTimeInHours
}
