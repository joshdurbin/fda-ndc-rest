import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

import io.durbs.ndc.NDCRestModule
import io.durbs.ndc.chain.AuthorizationTokenAPIActionChain
import io.durbs.ndc.chain.ProductAPIActionChain
import io.durbs.ndc.chain.ProductAPIAuthorizationActionChain
import io.durbs.ndc.config.ImporterConfig
import io.durbs.ndc.config.MongoConfig
import io.durbs.ndc.config.RESTAPIConfig
import io.durbs.ndc.config.RedisConfig
import io.durbs.ndc.service.NDCDataImporterService
import ratpack.config.ConfigData
import ratpack.hystrix.HystrixMetricsEventStreamHandler
import ratpack.hystrix.HystrixModule
import ratpack.rx.RxRatpack

import static ratpack.groovy.Groovy.ratpack

ratpack {
  bindings {

    RxRatpack.initialize()

    ConfigData configData = ConfigData.of { c ->
      c.yaml("$serverConfig.baseDir.file/application.yaml")
      c.env()
      c.sysProps()
    }

    bindInstance(ImporterConfig, configData.get('/importer', ImporterConfig))
    bindInstance(MongoConfig, configData.get('/mongo', MongoConfig))
    bindInstance(RESTAPIConfig, configData.get('/api', RESTAPIConfig))
    bindInstance(RedisConfig, configData.get('/redis', RedisConfig))

    bindInstance(ObjectMapper, new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY))

    module NDCRestModule
    module new HystrixModule().sse()
  }

  handlers {

    // CHAINS FOR CREATING AND RENEWING TOKENS
    prefix('api/v0/authorization') {

      all chain(registry.get(AuthorizationTokenAPIActionChain))
    }

    // CHAINS FOR PRODUCT DATA ACCESS
    prefix('api/v0/product') {

      all chain(registry.get(ProductAPIAuthorizationActionChain))
      all chain(registry.get(ProductAPIActionChain))
    }

    get('oOmsM6rHVhCJNWA5PVk1ar8eHr5fTwPyjuGnPzDeafnOxPYyvgAVhFBj2QLu8zYuXKqYSNepmKiOK53m7kClx50iHx6EGeZ3hGul5syjDcEOUE6nyBBjG9APF43gfQT8') { NDCDataImporterService importerService ->

      importerService.dropAndReplaceProductDatabase()
    }

    get('hystrix.stream', new HystrixMetricsEventStreamHandler())
  }
}