package io.durbs.ndc.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class User {

  @JsonIgnore
  String id

  String firstName
  String lastName
  String username

  @JsonIgnore
  String password

  @JsonIgnore
  String salt
}
