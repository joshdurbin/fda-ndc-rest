package io.durbs.ndc.chain

import com.google.inject.Inject
import com.google.inject.Singleton
import io.durbs.ndc.command.GetAllProducts
import io.durbs.ndc.command.GetMarketingCategoryNames
import io.durbs.ndc.command.GetProductTypeNames
import io.durbs.ndc.command.GetProductsByNDCCode
import io.durbs.ndc.command.GetRandomProduct
import io.durbs.ndc.command.GetTeaserProducts
import io.durbs.ndc.command.GetTotalNumberOfProductsCommand
import io.durbs.ndc.command.SearchForProductsByTerm
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

    get('count') {

      new GetTotalNumberOfProductsCommand(productService)
        .observe()
        .single()
        .subscribe { Long count ->

        render Jackson.json(count)
      }
    }

    get('random') {

      new GetRandomProduct(productService)
        .observe()
        .single()
        .subscribe { Product product ->

        render Jackson.json(product)
      }
    }

    get('search') {

      final String searchTerm = request.queryParams.q

      new SearchForProductsByTerm(productService, searchTerm)
        .observe()
        .toList()
        .subscribe { List<Product> products ->

        render Jackson.json(products)
      }
    }

    get('productTypeNames') {

      new GetProductTypeNames(productService)
        .observe()
        .toList()
        .subscribe { List<String> names ->

        render Jackson.json(names)
      }
    }

    get('marketingCategoryNames') {

      new GetMarketingCategoryNames(productService)
        .observe()
        .toList()
        .subscribe { List<String> names ->

        render Jackson.json(names)
      }
    }

    get('teaser') {

      new GetTeaserProducts(productService)
        .observe()
        .toList()
        .subscribe { List<Product> teaserProducts ->

        render Jackson.json(teaserProducts)
      }
    }

    get(':ndcCode') {

      final String ndcCode = pathTokens.ndcCode

      new GetProductsByNDCCode(productService, ndcCode)
        .observe()
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

      new GetAllProducts(productService)
        .observe()
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
