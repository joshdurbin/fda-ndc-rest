package io.durbs.ndc.domain.product

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
