package io.durbs.ndc

import io.durbs.ndc.domain.Packaging
import io.durbs.ndc.domain.PharmacologicalClassCategory
import io.durbs.ndc.domain.Product
import io.durbs.ndc.domain.Substance
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ProductCodec implements Codec<Product> {

  static final Codec<Document> documentCodec = new DocumentCodec()

  @Override
  Product decode(BsonReader reader, DecoderContext decoderContext) {

    final Document document = documentCodec.decode(reader, decoderContext)

    new Product(productID: document.getString('productID'),
      productNDC: document.getString('productNDC'),
      productTypeName: document.getString('productTypeName'),
      proprietaryName: document.getString('proprietaryName'),
      proprietaryNameSuffix: document.getString('proprietaryNameSuffix'),
      nonProprietaryName: document.get('nonProprietaryName', List),
      dosageFormName: document.getString('dosageFormName'),
      routeName: document.get('routeName', List),
      startMarketingDate: BSON_DOCUMENT_TO_LOCALDATE(document, 'startMarketingDate'),
      endMarketingDate: BSON_DOCUMENT_TO_LOCALDATE(document, 'endMarketingDate'),
      marketingCategoryName: document.getString('marketingCategoryName'),
      applicationNumber: document.getString('applicationNumber'),
      labelerName: document.getString('labelerName'),
      substances: document.get('substances', List).collect { Document substanceDocument ->
        new Substance(name: substanceDocument.getString('name'),
          activeNumeratorStrength: substanceDocument.getDouble('activeNumeratorStrength'),
          activeIngredUnit: substanceDocument.getString('activeIngredUnit'))
      },
      pharmacologicalClassCategories: document.get('pharmacologicalClassCategories', List).collect { Document pharmacologicalClassCategoryDocument ->
        new PharmacologicalClassCategory(name: pharmacologicalClassCategoryDocument.getString('name'),
          code: pharmacologicalClassCategoryDocument.getString('code'),)
      },
      deaScheduleNumber: document.getString('deaScheduleNumber'),
      packaging: document.get('packaging', List).collect { Document packagingDocument ->
        new Packaging(ndcPackageCode: packagingDocument.getString('ndcPackageCode'),
          packageDescription: packagingDocument.getString('packageDescription'))
      })

  }

  @Override
  void encode(BsonWriter writer, Product value, EncoderContext encoderContext) {

    // DO NOTHING, DATA IS IMMUTABLE, READ ONLY
  }

  @Override
  Class<Product> getEncoderClass() {
    Product
  }

  static final LocalDate BSON_DOCUMENT_TO_LOCALDATE(final Document document, final String propertyKey) {
    document.containsKey(propertyKey) ? LocalDateTime.ofInstant(
      Instant.ofEpochMilli(document.getDate(propertyKey).getTime()),
      ZoneId.systemDefault()).toLocalDate() : null
  }
}
