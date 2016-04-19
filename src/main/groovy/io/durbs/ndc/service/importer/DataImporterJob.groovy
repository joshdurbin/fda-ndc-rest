package io.durbs.ndc.service.importer

import com.google.inject.Inject
import com.google.inject.Singleton
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.config.ImporterConfig
import io.durbs.ndc.domain.product.Product
import io.durbs.ndc.service.ProductService
import org.bson.Document
import ratpack.exec.ExecController
import ratpack.server.Service
import ratpack.server.StartEvent
import ratpack.server.StopEvent

import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import static com.mongodb.client.model.Projections.fields
import static com.mongodb.client.model.Projections.include

//@CompileStatic
@Singleton
@Slf4j
class DataImporterJob implements Service, Runnable {

  private volatile ScheduledFuture<?> nextFuture
  private volatile boolean stopped

  @Inject
  DataImporterService dataImporterService

  @Inject
  ProductService productService

  @Inject
  ImporterConfig importerConfig

  @Inject
  ExecController execController

  @Override
  public void onStart(StartEvent event) throws Exception {

    scheduleNext()
  }

  @Override
  public void onStop(StopEvent event) throws Exception {
    stopped = true
    Optional.ofNullable(nextFuture).ifPresent({final Future future -> future.cancel(true)})
  }

  private void scheduleNext() {
    nextFuture = execController
      .getExecutor()
      .schedule(this, importerConfig.refreshCheckTimeInHours, TimeUnit.MINUTES)
  }

  @Override
  void run() {

    if (!stopped) {

      execController.fork()
        .onComplete({ e -> scheduleNext()})
        .start({ e ->

          log.info('Running data importer job...')

          // get cached last date run, if null, run, else compare to last update date. if older than last update date, run

          final List<String> existingProductIDs = []

          productService
            .getDistinctList('productID', new Document())
            .subscribe { final String productID ->

            existingProductIDs.add(productID)
          }

          log.info("existingProductIDs: ${existingProductIDs}")

          dataImporterService.getFDANDCProducts().each { final Product productImportCandidate ->

            productService.getProducts(new Document('productID', productImportCandidate.productID),
              new Document(),
              fields(include()),
              0,
              Integer.MAX_VALUE)
              .subscribe { final Product product ->

              log.info("FOUND PRODUCT!!! ${product}")
            }
          }

          //productService.replaceAllProducts(dataImporterService.FDANDCProducts).subscribe()
          //Runtime.getRuntime().gc()

          log.debug('Finished running data importer job!')
        })
    }
  }
}
