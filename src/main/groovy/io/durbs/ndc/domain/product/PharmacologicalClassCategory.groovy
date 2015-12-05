package io.durbs.ndc.domain.product

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class PharmacologicalClassCategory implements Serializable {

  String name
  String code
}
