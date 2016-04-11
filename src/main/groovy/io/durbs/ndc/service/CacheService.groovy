package io.durbs.ndc.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.domain.product.Product

@CompileStatic
@Singleton
@Slf4j
class CacheService {

  @Inject
  RedisReactiveCommands<String, Product> productsCache

  @Inject
  RedisReactiveCommands<String, String> stringsCache



}
