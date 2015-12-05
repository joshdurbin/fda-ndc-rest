package io.durbs.ndc.command

import groovy.transform.CompileStatic
import io.durbs.ndc.config.RESTAPIConfig
import io.durbs.ndc.domain.APIAuthResult
import org.bson.BsonDocument
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import ratpack.handling.Context

import static com.mongodb.client.model.Projections.exclude
import static com.mongodb.client.model.Projections.fields
import static com.mongodb.client.model.Projections.include
import static com.mongodb.client.model.Sorts.ascending
import static com.mongodb.client.model.Sorts.descending
import static com.mongodb.client.model.Sorts.orderBy

@CompileStatic
class BaseAPIRequestParameters {

  static final String PAGE_SIZE_QUERY_PARAM_KEY = 'pageSize'
  static final String PAGE_NUMBER_QUERY_PARAM_KEY = 'pageNumber'
  static final String EXCLUDE_QUERY_PARAM_KEY = 'exclude'
  static final String SORT_ASCENDING_QUERY_PARAM_KEY = 'ascending'
  static final String SORT_DESCENDING_QUERY_PARAM_KEY = 'descending'

  static final EmptyFilter EMPTY_BSON_FILTER = new EmptyFilter()

  static final String SPLIT_CHAR = ','

  final Context context
  final RESTAPIConfig restapiConfig

  BaseAPIRequestParameters(Context context) {
    this.context = context
    this.restapiConfig = context.get(RESTAPIConfig)
  }

  Integer getPageNumber() {

    final Integer suppliedPageNumber

    if ((context.request.queryParams.get(PAGE_NUMBER_QUERY_PARAM_KEY) as CharSequence)?.isNumber()) {
      suppliedPageNumber = (context.request.queryParams.get(PAGE_NUMBER_QUERY_PARAM_KEY) as Integer).abs()
    } else {
      suppliedPageNumber = restapiConfig.defaultFirstPage
    }

    suppliedPageNumber
  }

  Integer getPageSize() {

    final Integer suppliedPageSize

    if ((context.request.queryParams.get(PAGE_SIZE_QUERY_PARAM_KEY) as CharSequence)?.isNumber()) {
      suppliedPageSize = (context.request.queryParams.get(PAGE_SIZE_QUERY_PARAM_KEY) as Integer).abs()
    } else {
      suppliedPageSize = restapiConfig.defaultResultsPageSize
    }

    final Integer limit

    if (suppliedPageSize > restapiConfig.maxResultsPageSize) {
      limit = restapiConfig.maxResultsPageSize
    } else {
      limit = suppliedPageSize
    }

    limit
  }

  Integer getOffSet() {
    pageNumber * pageSize
  }

  Bson getQueryFilter() {
    EMPTY_BSON_FILTER
  }

  Bson getSortCriteria() {

    final List<String> ascendingProperties
    final List<String> descendingProperties

    if (context.request.queryParams.containsKey(SORT_ASCENDING_QUERY_PARAM_KEY)) {

      ascendingProperties = context.request.queryParams.get(SORT_ASCENDING_QUERY_PARAM_KEY).split(SPLIT_CHAR) as List
      ascendingProperties.retainAll(restapiConfig.sortPropertiesAsList)

    } else {

      ascendingProperties = []
    }

    if (context.request.queryParams.containsKey(SORT_DESCENDING_QUERY_PARAM_KEY)) {

      descendingProperties = context.request.queryParams.get(SORT_DESCENDING_QUERY_PARAM_KEY, '').split(SPLIT_CHAR) as List
      descendingProperties.retainAll(restapiConfig.sortPropertiesAsList)

    } else {

      descendingProperties = []
    }

    if (!ascendingProperties.intersect(descendingProperties).empty) {
      throw new IllegalArgumentException("The following properties cannot both be excluded and included: ${ascendingProperties.intersect(descendingProperties)}")
    }

    orderBy(ascending(ascendingProperties), descending(descendingProperties))
  }

  Bson getProjectionDocument() {

    if (!authenticated) {

      fields(include(restapiConfig.teaserProductPropertiesAsList))

    } else if (context.request.queryParams.containsKey(EXCLUDE_QUERY_PARAM_KEY)
      && context.request.queryParams.get(EXCLUDE_QUERY_PARAM_KEY).split(SPLIT_CHAR)) {

      fields(exclude(context.request.queryParams.get(EXCLUDE_QUERY_PARAM_KEY).split(SPLIT_CHAR)))

    } else {

      fields(include())
    }
  }

  Boolean getAuthenticated() {
    context.maybeGet(APIAuthResult).orElse(new APIAuthResult(authenticated: false)).authenticated
  }

  private static class EmptyFilter implements Bson {

    @Override
    public <TDocument> BsonDocument toBsonDocument(Class<TDocument> tDocumentClass, CodecRegistry codecRegistry) {

      new BsonDocument()
    }
  }
}
