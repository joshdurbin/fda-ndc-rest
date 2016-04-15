package io.durbs.ndc.domain.auth

import groovy.transform.Canonical
import groovy.transform.CompileStatic

import java.time.LocalDateTime

@CompileStatic
@Canonical
class APIAuthRecord {

  LocalDateTime creationTime
  LocalDateTime expirationTime

  String authCode

  String contactName
  String contactEmailAddress
}
