package io.durbs.ndc.command.api.product

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import org.bson.conversions.Bson
import ratpack.handling.Context
import rx.Observable

import static com.mongodb.client.model.Filters.and
import static com.mongodb.client.model.Filters.exists
import static com.mongodb.client.model.Filters.gte
import static com.mongodb.client.model.Filters.lte
import static com.mongodb.client.model.Filters.or
import static com.mongodb.client.model.Sorts.descending
import static com.mongodb.client.model.Sorts.orderBy

@CompileStatic
class GetActiveProducts extends HystrixObservableCommand<Product> {

  final ProductService productService
  final GetActiveProductsRequestParameters requestParameters

  GetActiveProducts(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetActiveProducts'))

    this.productService = context.get(ProductService)
    this.requestParameters = new GetActiveProductsRequestParameters(context)
  }

  @Override
  protected Observable<Product> construct() {

    productService.getProducts(requestParameters.queryFilter,
      requestParameters.sortCriteria,
      requestParameters.projectionDocument,
      requestParameters.pageSize,
      requestParameters.getOffSet())
  }

  static class GetActiveProductsRequestParameters extends BaseAPIRequestParameters {

    GetActiveProductsRequestParameters(Context context) {
      super(context)
    }

    Bson getQueryFilter() {

      final Date now = new Date()

      and(lte('startMarketingDate', now), or(exists('endMarketingDate', false), gte('endMarketingDate', now)))
    }

    Bson getSortCriteria() {

      if (context.request.queryParams.containsKey(BaseAPIRequestParameters.SORT_ASCENDING_QUERY_PARAM_KEY)
        || context.request.queryParams.containsKey(BaseAPIRequestParameters.SORT_DESCENDING_QUERY_PARAM_KEY)) {

        super.sortCriteria
      } else {

        orderBy(descending('startMarketingDate'))
      }
    }
  }
}
