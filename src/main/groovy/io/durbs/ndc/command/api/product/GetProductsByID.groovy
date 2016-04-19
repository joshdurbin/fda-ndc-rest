package io.durbs.ndc.command.api.product

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.transform.CompileStatic
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.command.BaseAPIRequestParameters
import io.durbs.ndc.service.ProductService
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import ratpack.handling.Context
import rx.Observable

import static com.mongodb.client.model.Filters.eq

@CompileStatic
class GetProductsByID extends HystrixObservableCommand<Product> {

  final ProductService productService
  final GetProductsByNDCCodeRequestParameters requestParameters

  GetProductsByID(Context context) {
    super(HystrixCommandGroupKey.Factory.asKey('GetProductsByID'))

    this.requestParameters = new GetProductsByNDCCodeRequestParameters(context)
    this.productService = context.get(ProductService)
  }

  @Override
  protected Observable<Product> construct() {

    productService.getProducts(requestParameters.queryFilter,
      requestParameters.sortCriteria,
      requestParameters.projectionDocument,
      requestParameters.pageSize,
      requestParameters.getOffSet())
      .bindExec()
  }

  static class GetProductsByNDCCodeRequestParameters extends BaseAPIRequestParameters {

    static final String ID_CODE_PATH_TOKEN = 'id'
    static final String DEFAULT_NDC_CODE_PATH_TOKEN = ''

    GetProductsByNDCCodeRequestParameters(Context context) {
      super(context)
    }

    String getID() {
      context.pathTokens.get(ID_CODE_PATH_TOKEN, DEFAULT_NDC_CODE_PATH_TOKEN)
    }

    Bson getQueryFilter() {

      eq('_id', new ObjectId(getID()))
    }
  }

}
