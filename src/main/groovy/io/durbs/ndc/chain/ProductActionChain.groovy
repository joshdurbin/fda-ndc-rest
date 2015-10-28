package io.durbs.ndc.chain

import com.google.inject.Inject
import com.google.inject.Singleton
import io.durbs.ndc.config.RESTAPIConfig
import io.durbs.ndc.service.ProductService
import io.durbs.ndc.domain.product.Product

import ratpack.groovy.handling.GroovyChainAction
import ratpack.jackson.Jackson

@Singleton
class ProductActionChain extends GroovyChainAction {

  @Inject
  ProductService productService

  @Inject
  RESTAPIConfig restAPIConfig

  @Override
  void execute() throws Exception {

    get('random') {

      productService.getRandomProduct()
        .single()
        .subscribe { Product product ->

        render Jackson.json(product)
      }
    }

    get('search') {

      final String searchTerm = request.queryParams.q

      productService.searchForProductsByTerms(searchTerm)
        .toList()
        .subscribe { List<Product> products ->

        render Jackson.json(products)
      }
    }

    get('productTypeNames') {

      productService.productTypeNames
        .toList()
        .subscribe { List<String> names ->

        render Jackson.json(names)
      }
    }

    get('marketingCategoryNames') {

      productService.marketingCategoryNames
        .toList()
        .subscribe { List<String> names ->

        render Jackson.json(names)
      }
    }

    get('teaser') {

      productService.teaserProducts
        .toList()
        .subscribe { List<Product> teaserProducts ->

        render Jackson.json(teaserProducts)
      }
    }

    get(':ndcCode') {

      final String ndcCode = pathTokens.ndcCode

      productService.getProductsByNDCCode(ndcCode)
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

      productService.getAllProducts()
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
