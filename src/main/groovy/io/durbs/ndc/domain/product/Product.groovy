package io.durbs.ndc.domain.product

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

import java.time.LocalDate

@Canonical
@CompileStatic
@EqualsAndHashCode(excludes = ['id'])
class Product {

  String id

  // THE RANDOM KEY EXISTS FOR THE RANDOM API RETURN FUNCTIONALITY, SEE THE 'GetRandomProduct' COMMAND
  @JsonIgnore
  Double randomKey

  String productID
  String productNDC
  String productTypeName
  String proprietaryName
  String proprietaryNameSuffix
  List<String> nonProprietaryName
  String dosageFormName
  List<String> routeName
  LocalDate startMarketingDate
  LocalDate endMarketingDate
  String marketingCategoryName
  String applicationNumber
  String labelerName
  List<Substance> substances
  List<PharmacologicalClassCategory> pharmacologicalClassCategories
  String deaScheduleNumber
  List<Packaging> packaging
}
