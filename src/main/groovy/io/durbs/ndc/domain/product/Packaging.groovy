package io.durbs.ndc.domain.product

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class Packaging {

  String ndcPackageCode
  String packageDescription
}
