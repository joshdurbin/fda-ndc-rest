import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.durbs.ndc.NDCRestConfig
import io.durbs.ndc.NDCRestModule
import io.durbs.ndc.ProductActionChain
import ratpack.config.ConfigData
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

    bindInstance(NDCRestConfig, configData.get('/config', NDCRestConfig))



    bindInstance(ObjectMapper, new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY))

    module NDCRestModule

    bindInstance Service, new Service() {

      @Override
      void onStart(StartEvent event) throws Exception {

        RxRatpack.initialize()
      }
    }
  }

  handlers {

    prefix('api/v0/product') {
      all chain(registry.get(ProductActionChain))
    }
  }
}