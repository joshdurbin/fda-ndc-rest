package io.durbs.ndc.domain.auth

import groovy.transform.CompileStatic

import java.time.LocalDateTime

@CompileStatic
class APIAuthRecord {

  LocalDateTime creationTime
  LocalDateTime renewalTime
  LocalDateTime expirationTime

  String authCode

  String contactName
  String contactEmailAddress
}
