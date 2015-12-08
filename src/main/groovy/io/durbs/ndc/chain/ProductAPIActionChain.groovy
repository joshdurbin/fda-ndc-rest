package io.durbs.ndc.chain

import com.google.inject.Singleton
import io.durbs.ndc.command.api.product.GetActiveProducts
import io.durbs.ndc.command.api.product.GetAllProducts
import io.durbs.ndc.command.api.product.GetInactiveProducts
import io.durbs.ndc.command.api.product.GetMarketingCategoryNames
import io.durbs.ndc.command.api.product.GetProductTypeNames
import io.durbs.ndc.command.api.product.GetProductsByNDCCode
import io.durbs.ndc.command.api.product.GetRandomProduct
import io.durbs.ndc.command.api.product.SearchForProductsByTerm
import io.durbs.ndc.domain.product.Product
import ratpack.groovy.handling.GroovyChainAction
import ratpack.jackson.Jackson

@Singleton
class ProductAPIActionChain extends GroovyChainAction {

  @Override
  void execute() throws Exception {

    get('random') {

      new GetRandomProduct(context)
        .observe()
        .single()
        .subscribe { Product product ->

        render Jackson.json(product)
      }
    }

    get('search') {

      new SearchForProductsByTerm(context)
        .observe()
        .toList()
        .subscribe { List<Product> products ->

        render Jackson.json(products)
      }
    }

    get('status/active') {

      new GetActiveProducts(context)
        .observe()
        .toList()
        .subscribe { List<Product> products ->

        render Jackson.json(products)
      }
    }

    get ('status/inactive') {

      new GetInactiveProducts(context)
        .observe()
        .toList()
        .subscribe { List<Product> products ->

        render Jackson.json(products)
      }
    }

    get('types') {

      new GetProductTypeNames(context)
        .observe()
        .toList()
        .subscribe { List<String> names ->

        render Jackson.json(names)
      }
    }

    get('types/:type') {

      render "${pathTokens.get('type')}"
    }

    get('marketingCategories') {

      new GetMarketingCategoryNames(context)
        .observe()
        .toList()
        .subscribe { List<String> names ->

        render Jackson.json(names)
      }
    }

    get('marketingCategories/:category') {

      render "${pathTokens.get('category')}"
    }

    get(':ndcCode') {

      new GetProductsByNDCCode(context)
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

      new GetAllProducts(context)
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
