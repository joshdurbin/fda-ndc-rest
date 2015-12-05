package io.durbs.ndc.codec.mongo

import io.durbs.ndc.domain.product.Packaging
import io.durbs.ndc.domain.product.PharmacologicalClassCategory
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.domain.product.Substance
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class MongoProductCodec implements CollectibleCodec<Product> {

  static final Codec<Document> documentCodec = new DocumentCodec()

  @Override
  Product decode(final BsonReader reader, final DecoderContext decoderContext) {

    final Document document = documentCodec.decode(reader, decoderContext)

    new Product(randomKey: document.getDouble('randomKey'),
      productID: document.getString('productID'),
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
  void encode(final BsonWriter writer, final Product product, final EncoderContext encoderContext) {

    final Document document = new Document()

    if (product.randomKey) {
      document.put('randomKey', product.randomKey)
    } else {

      final Random random = new Random()

      document.put('randomKey', random.nextDouble())
    }

    if (product.productID) {
      document.put('productID', product.productID)
    }

    if (product.productNDC) {
      document.put('productNDC', product.productNDC)
    }

    if (product.productTypeName) {
      document.put('productTypeName', product.productTypeName)
    }

    if (product.proprietaryName) {
      document.put('proprietaryName', product.proprietaryName)
    }

    if (product.proprietaryNameSuffix) {
      document.put('proprietaryNameSuffix', product.proprietaryNameSuffix)
    }

    if (product.nonProprietaryName) {
      document.put('nonProprietaryName', product.nonProprietaryName)
    }

    if (product.dosageFormName) {
      document.put('dosageFormName', product.dosageFormName)
    }

    if (product.routeName) {
      document.put('routeName', product.routeName)
    }

    if (product.startMarketingDate) {
      document.put('startMarketingDate', Date.from(product.startMarketingDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
    }

    if (product.endMarketingDate) {
      document.put('endMarketingDate', Date.from(product.endMarketingDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
    }

    if (product.marketingCategoryName) {
      document.put('marketingCategoryName', product.marketingCategoryName)
    }

    if (product.applicationNumber) {
      document.put('applicationNumber', product.applicationNumber)
    }

    if (product.labelerName) {
      document.put('labelerName', product.labelerName)
    }

    if (product.substances) {
      document.put('substances', product.substances.collect { Substance substance ->

        new Document()
          .append('name', substance.name)
          .append('activeNumeratorStrength', substance.activeNumeratorStrength)
          .append('activeIngredUnit', substance.activeIngredUnit)
      })
    }

    if (product.pharmacologicalClassCategories) {
      document.put('pharmacologicalClassCategories', product.pharmacologicalClassCategories.collect { PharmacologicalClassCategory pharmacologicalClassCategory ->

        new Document()
          .append('name', pharmacologicalClassCategory.name)
          .append('code', pharmacologicalClassCategory.code)
      })
    }

    if (product.deaScheduleNumber) {
      document.put('deaScheduleNumber', product.deaScheduleNumber)
    }

    if (product.packaging) {
      document.put('packaging', product.packaging.collect { Packaging packaging ->

        new Document()
          .append('ndcPackageCode', packaging.ndcPackageCode)
          .append('packageDescription', packaging.packageDescription)
      })
    }

    documentCodec.encode(writer, document, encoderContext);
  }

  @Override
  Class<Product> getEncoderClass() {
    Product
  }

  @Override
  Product generateIdIfAbsentFromDocument(final Product product) {

    if (documentHasId(product)) {
      product.setId(UUID.randomUUID() as String)
    }

    product
  }

  @Override
  boolean documentHasId(final Product product) {
    product.id
  }

  @Override
  BsonValue getDocumentId(final Product product) {

    if (!documentHasId(product)) {
      throw new IllegalStateException('The product does not contain an _id');
    }

    new BsonString(product.id);
  }

  static final LocalDate BSON_DOCUMENT_TO_LOCALDATE(final Document document, final String propertyKey) {
    document.containsKey(propertyKey) ? LocalDateTime.ofInstant(
      Instant.ofEpochMilli(document.getDate(propertyKey).getTime()),
      ZoneId.systemDefault()).toLocalDate() : null
  }
}
