package io.durbs.ndc

import com.google.inject.Inject
import com.google.inject.Singleton
import io.durbs.ndc.domain.Product
import ratpack.groovy.handling.GroovyChainAction
import ratpack.jackson.Jackson

@Singleton
class ProductActionChain extends GroovyChainAction {

  @Inject
  ProductLookupService productLookupService

  @Override
  void execute() throws Exception {

    get('search') {

      final String searchTerm = request.queryParams.q

      productLookupService.search(searchTerm)
        .toList()
        .subscribe { List<Product> products ->

        render Jackson.json(products)
      }
    }

    get(':ndcCode') {

      final String ndcCode = pathTokens.ndcCode

      productLookupService.getByNDCCode(ndcCode)
        .single()
        .subscribe { Product product ->
        if (product) {
          render Jackson.json(product)
        } else {
          clientError 404
        }
      }
    }

    get {
      productLookupService.getAll()
        .toList()
        .subscribe { List<Product> products ->

        if (products) {
          render Jackson.json(products)
        } else {
          render Jackson.json([])
        }
      }
    }
  }
}
