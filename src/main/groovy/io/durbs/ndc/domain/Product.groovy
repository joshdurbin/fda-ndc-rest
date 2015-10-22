package io.durbs.ndc.domain

import groovy.transform.Canonical
import groovy.transform.CompileStatic

import java.time.LocalDate

@Canonical
@CompileStatic
class Product {

  String productID
  String productNDC
  String productTypeName
  String proprietaryName
  String proprietaryNameSuffix
  List<String> nonProprietaryName
  String dosageFormName
  String routeName
  LocalDate startMarketingDate
  LocalDate endMarketingDate
  String marketingCategoryName
  String applicationNumber
  String labelerName
  List<Substance> substances
  List<String> pharmacologicalClassCategories
  String deaScheduleNumber
  List<Packaging> packaging
}
