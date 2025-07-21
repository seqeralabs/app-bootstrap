package io.seqera.app.service.migrate

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import io.seqera.migtool.MigTool
import jakarta.annotation.PostConstruct
import jakarta.inject.Inject

/**
 * Database migration service using MigTool
 * Automatically runs migrations on application startup for non-test environments
 */
@Slf4j
@Context
@CompileStatic
@Requires(notEnv = ["test", "local"])
class DbMigrationService {

    @Inject
    ApplicationContext context

    @PostConstruct
    void migrate() {
        log.info("DB migration begin")
        runMigtool(Datasource.fromApp(context))
        log.info("DB migration done")
    }

    private static void runMigtool(Datasource ds) {
        new MigTool()
                .withUser(ds.getUsername())
                .withPassword(ds.getPassword())
                .withUrl(ds.getUrl())
                .withDialect(ds.getType())
                .withDriver(ds.getDriver())
                .withLocations(ds.getLocations())
                .withClassLoader(DbMigrationService.class.getClassLoader())
                .withPattern("^m(\\d+)__(.+)")
                .run()
    }
}