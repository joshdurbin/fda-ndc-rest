package io.durbs.ndc.service.importer

import com.google.common.base.Stopwatch
import com.google.common.io.ByteStreams
import com.google.inject.Inject
import com.google.inject.Singleton
import com.univocity.parsers.common.record.Record
import com.univocity.parsers.tsv.TsvParser
import com.univocity.parsers.tsv.TsvParserSettings
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.config.ImporterConfig
import io.durbs.ndc.domain.product.Packaging
import io.durbs.ndc.domain.product.PharmacologicalClassCategory
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.domain.product.Substance
import io.durbs.ndc.service.ProductService
import org.apache.commons.lang.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document as JsoupDocument
import org.jsoup.select.Elements

import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@CompileStatic
@Singleton
@Slf4j
class DataImporterService {

  static final Random RANDOM = new SecureRandom()
  
  static final String PRODUCTS_TSV_FILENAME = 'product.txt'
  static final String PACKAGE_TSV_FILENAME = 'package.txt'

  // HEADERS BELONGING TO BOTH THE PRODUCT AND PACKAGE FILES
  static final String PRODUCTID = 'PRODUCTID'
  static final String PRODUCTNDC = 'PRODUCTNDC'

  // HEADERS BELONGING TO THE PRODUCT FILE
  static final String PRODUCTTYPENAME = 'PRODUCTTYPENAME'
  static final String PROPRIETARYNAME = 'PROPRIETARYNAME'
  static final String PROPRIETARYNAMESUFFIX = 'PROPRIETARYNAMESUFFIX'
  static final String NONPROPRIETARYNAME = 'NONPROPRIETARYNAME'
  static final String DOSAGEFORMNAME = 'DOSAGEFORMNAME'
  static final String ROUTENAME = 'ROUTENAME'
  static final String STARTMARKETINGDATE = 'STARTMARKETINGDATE'
  static final String ENDMARKETINGDATE = 'ENDMARKETINGDATE'
  static final String MARKETINGCATEGORYNAME = 'MARKETINGCATEGORYNAME'
  static final String APPLICATIONNUMBER = 'APPLICATIONNUMBER'
  static final String LABELERNAME = 'LABELERNAME'
  static final String SUBSTANCENAME = 'SUBSTANCENAME'
  static final String ACTIVE_NUMERATOR_STRENGTH = 'ACTIVE_NUMERATOR_STRENGTH'
  static final String ACTIVE_INGRED_UNIT = 'ACTIVE_INGRED_UNIT'
  static final String PHARM_CLASSES = 'PHARM_CLASSES'
  static final String DEASCHEDULE = 'DEASCHEDULE'

  // HEADERS BELONGING TO THE PACKAGE FILE
  static final String NDCPACKAGECODE = 'NDCPACKAGECODE'
  static final String PACKAGEDESCRIPTION = 'PACKAGEDESCRIPTION'

  // CHARACTER CONSTANTS
  static final String SEMICOLON = ";"

  static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern('M/d/yyyy')

  @Inject
  ImporterConfig importerConfig

  @Inject
  ProductService productService

  public LocalDate getFDANDCMostRecentUpdateDate() {

    final Stopwatch stopwatch = Stopwatch.createStarted()
    final String KEYWORD_SEARCH = 'Updated'

    log.info("Acquiring stream to NDC web page at ${importerConfig.ndcDirectoryPage}...")

    final JsoupDocument ndcDirectoryPageDocument = Jsoup.connect(importerConfig.ndcDirectoryPage).get()

    log.info("Stream acquired and read fully in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms. Processing webpage, searching for date...")
    stopwatch.reset()
    stopwatch.start()

    final Elements panelBodyDiv = ndcDirectoryPageDocument.select('div.panel-body')
    final String firstUpdatedListItemText = panelBodyDiv.select("li:contains(${KEYWORD_SEARCH})").first().text()
    final String trimmedDateString = StringUtils.substringAfter(firstUpdatedListItemText, KEYWORD_SEARCH).trim()

    final LocalDate parsedDate = LocalDate.parse(trimmedDateString, DATE_FORMATTER)

    log.info("Finished locating last updated date of ${parsedDate} in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms")

    parsedDate
  }

  public List<Product> getFDANDCProducts() {

    final Stopwatch stopwatch = Stopwatch.createStarted()

    log.info("Acquiring stream to NDC database file at ${importerConfig.ndcDatabaseFileURL}...")

    final byte[] bytes = ByteStreams.toByteArray(importerConfig.ndcDatabaseFileURL.toURL().openStream())

    log.info("Stream acquired and read fully in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms. Processing product records...")
    stopwatch.reset()
    stopwatch.start()

    final List<Record> productRecords = getTSVRecordsForFile(new ByteArrayInputStream(bytes), PRODUCTS_TSV_FILENAME)

    log.info("Product records processed in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms. Processing product packing records...")
    stopwatch.reset()
    stopwatch.start()

    final List<Record> productPackingRecords = getTSVRecordsForFile(new ByteArrayInputStream(bytes), PACKAGE_TSV_FILENAME)

    log.info("Product packing records processed in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms. Mapping product packaing to products...")
    stopwatch.reset()
    stopwatch.start()

    final Map<String, Product> productIDsToProducts = getProductsFromNDCPublishedDatabaseArchive(productRecords)

    log.info("Product packing records processed in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms. Mapping product packaing to products...")
    stopwatch.reset()
    stopwatch.start()

    final List<Product> products = mapProductPackagingToProducts(productIDsToProducts, productPackingRecords)

    log.info("Finished mapping product packaging records in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms.")

    products
  }

  /**
   *
   * @param productIDsToProducts
   * @param productPackagingRecords
   * @return
   */
  private static List<Product> mapProductPackagingToProducts(final Map<String, Product> productIDsToProducts, final List<Record> productPackagingRecords) {

    productPackagingRecords.each {

      final Record record ->

        try {

          if (productIDsToProducts.containsKey(record.getString(PRODUCTID))) {

            productIDsToProducts.get(record.getString(PRODUCTID)).packaging.add(
              new Packaging(
                ndcPackageCode: record.getString(NDCPACKAGECODE),
                packageDescription: record.getString(PACKAGEDESCRIPTION))
            )

          } else {

            log.error("Attempt to map packing record '${record}' failed; product ID '${record.getString(PRODUCTID)}' cannot be found.")
          }
        } catch (final Exception exception) {

          log.error("Error processing packaging record ${record}", exception)
        }
    }

    productIDsToProducts.values() as List<Product>
  }

  /**
   *
   * @param records
   * @return
   */
  private static Map<String, Product> getProductsFromNDCPublishedDatabaseArchive(final List<Record> records) {

    final Map<String, Product> products = records.collectEntries { final Record record ->

      try {

        final List<Substance> substances = []

        if (record.getString(SUBSTANCENAME)?.contains(SEMICOLON) && record.getString(ACTIVE_NUMERATOR_STRENGTH)?.contains(SEMICOLON) && record.getString(ACTIVE_INGRED_UNIT)?.contains(SEMICOLON)) {

          final List<String> substancNames = record.getString(SUBSTANCENAME)?.split(SEMICOLON) as List<String>
          final List<String> substanceStrength = record.getString(ACTIVE_NUMERATOR_STRENGTH)?.split(SEMICOLON) as List<String>
          final List<String> substanceUnit = record.getString(ACTIVE_INGRED_UNIT)?.split(SEMICOLON) as List<String>

          substancNames.eachWithIndex { String substanceName, Integer substanceNamesIndex ->

            substances.add(
              new Substance(
                name: substancNames.get(substanceNamesIndex),
                activeNumeratorStrength: substanceStrength.get(substanceNamesIndex) ? Double.parseDouble(substanceStrength.get(substanceNamesIndex)) : null,
                activeIngredUnit: substanceUnit.get(substanceNamesIndex)
              )
            )
          }

        } else {

          substances.add(new Substance(
            name: record.getString(SUBSTANCENAME),
            activeNumeratorStrength: record.getString(ACTIVE_NUMERATOR_STRENGTH) ? Double.parseDouble(record.getString(ACTIVE_NUMERATOR_STRENGTH)) : null,
            activeIngredUnit: record.getString(ACTIVE_INGRED_UNIT)
          ))
        }

        [record.getString(PRODUCTID), new Product(
          randomKey: RANDOM.nextDouble(),
          productID: record.getString(PRODUCTID),
          productNDC: record.getString(PRODUCTNDC),
          productTypeName: record.getString(PRODUCTTYPENAME),
          proprietaryName: record.getString(PROPRIETARYNAME),
          proprietaryNameSuffix: record.getString(PROPRIETARYNAMESUFFIX),
          nonProprietaryName: record.getString(NONPROPRIETARYNAME)?.split(SEMICOLON) as List<String>,
          dosageFormName: record.getString(DOSAGEFORMNAME),
          routeName: record.getString(ROUTENAME)?.split(SEMICOLON) as List<String>,
          startMarketingDate:  record.getString(STARTMARKETINGDATE) ? LocalDate.parse(record.getString(STARTMARKETINGDATE), DateTimeFormatter.BASIC_ISO_DATE) : null,
          endMarketingDate:  record.getString(ENDMARKETINGDATE) ? LocalDate.parse(record.getString(ENDMARKETINGDATE), DateTimeFormatter.BASIC_ISO_DATE) : null,
          marketingCategoryName:  record.getString(MARKETINGCATEGORYNAME),
          applicationNumber: record.getString(APPLICATIONNUMBER),
          labelerName:  record.getString(LABELERNAME),
          substances: substances,
          pharmacologicalClassCategories: record.getString(PHARM_CLASSES)?.split(',')?.collect { final String line ->

            if (line.contains('[') && line.contains(']')) {
              new PharmacologicalClassCategory(name: line, code: line.substring(line.indexOf('[') + 1, line.indexOf(']')))
            } else {
              new PharmacologicalClassCategory(name: line)
            }},

          deaScheduleNumber: record.getString(DEASCHEDULE),
          packaging: []
        )]
      } catch (final Exception exception) {

        log.error("Error processing product record ${record}", exception)
      }
    }

    products
  }

  /**
   *
   * Takes an inputstream, converts it to a zipinputstream and checks for a filename match then passes that stream
   *   position to the TSV parser to product a list of records.
   *
   * @param inputStream
   * @param filename
   * @return
   */
  private static List<Record> getTSVRecordsForFile(final InputStream inputStream, final String filename) {

    List<Record> records = []

    final TsvParserSettings settings = new TsvParserSettings()
    settings.setHeaderExtractionEnabled(true)

    final TsvParser parser = new TsvParser(settings)

    try {

      final ZipInputStream zipInputStream = new ZipInputStream(inputStream)

      ZipEntry zipEntry

      while ((zipEntry = zipInputStream.getNextEntry()) != null) {

        if (zipEntry.name == filename) {

          log.info("Zip entry filename match ${filename}. Processing input stream...")
          Stopwatch stopwatch = Stopwatch.createStarted()

          records = parser.parseAllRecords(zipInputStream)

          stopwatch.stop()
          log.debug("Finished processing records in ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms")

          // WE HAVE TO BREAK BECAUSE THE PRASER WILL CLOSE THE STREAM
          break
        }
      }
    } catch (final Exception exception) {

      log.error("An exception occcurred trying to read records for the file ${PRODUCTS_TSV_FILENAME}")

    } finally {

      inputStream?.close()
    }

    return records
  }

}
