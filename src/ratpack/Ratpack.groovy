import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

import io.durbs.ndc.NDCRestModule
import io.durbs.ndc.chain.ProductActionChain
import io.durbs.ndc.config.MongoConfig
import io.durbs.ndc.config.RESTAPIConfig
import io.durbs.ndc.config.RedisConfig
import ratpack.config.ConfigData
import ratpack.hystrix.HystrixMetricsEventStreamHandler
import ratpack.hystrix.HystrixModule
import ratpack.rx.RxRatpack
import ratpack.server.Service
import ratpack.server.StartEvent

import static ratpack.groovy.Groovy.ratpack

ratpack {
  bindings {

    ConfigData configData = ConfigData.of { c ->
      c.yaml("$serverConfig.baseDir.file/application.yaml")
      c.env()
      c.sysProps()
    }

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

    bindInstance Service, new Service() {

      @Override
      void onStart(StartEvent event) throws Exception {

        RxRatpack.initialize()
      }
    }
  }

  handlers {

    get('loaderio-69848b6992185269cd57a7fc9d760715.txt') {
      render('loaderio-69848b6992185269cd57a7fc9d760715')
    }

    prefix('api/v0/product') {
      all chain(registry.get(ProductActionChain))
    }

    get('hystrix.stream', new HystrixMetricsEventStreamHandler())
  }
}