package io.durbs.ndc.command.api

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.service.ProductService
import org.bson.conversions.Bson
import ratpack.handling.Context
import rx.Observable

import static com.mongodb.client.model.Filters.text

@CompileStatic
class SearchForProductsByTerm extends HystrixObservableCommand<Product> {

  final ProductService productService
  final SearchForProductsByTermRequestParameters requestParameters

  SearchForProductsByTerm(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('SearchForProductsByTerm'))

    this.productService = context.get(ProductService)
    this.requestParameters = new SearchForProductsByTermRequestParameters(context)
  }

  @Override
  protected Observable<Product> construct() {

    productService.getProducts(requestParameters.queryFilter,
      requestParameters.sortCriteria,
      requestParameters.projectionDocument,
      requestParameters.pageSize,
      requestParameters.getOffSet())
  }

  static class SearchForProductsByTermRequestParameters extends BaseAPIRequestParameters {

    static final String SEARCH_QUERY_PARAM_KEY = 'q'
    static final String DEFAULT_SEARCH_TERM = ''

    SearchForProductsByTermRequestParameters(Context context) {
      super(context)
    }

    Bson getQueryFilter() {
      text(context.request.queryParams.get(SEARCH_QUERY_PARAM_KEY, DEFAULT_SEARCH_TERM))
    }
  }
}
