package io.seqera.app.service.migrate

import groovy.transform.CompileStatic
import io.micronaut.context.ApplicationContext
import io.micronaut.jdbc.BasicJdbcConfiguration

/**
 * Datasource adapter for MigTool integration
 */
@CompileStatic
class Datasource {
    String url
    String username
    String password
    String driver
    String locations = "classpath:db-schema/"

    static Datasource fromApp(ApplicationContext ctx) {
        return fromJdbc(ctx.getBean(BasicJdbcConfiguration.class))
    }

    static Datasource fromJdbc(BasicJdbcConfiguration jdbc) {
        return new Datasource(
            url: jdbc.url,
            username: jdbc.username,
            password: jdbc.password,
            driver: jdbc.driverClassName
        )
    }

    String getType() {
        return url.split(":")[1]  // Extract dialect from JDBC URL (e.g., "postgresql" from "jdbc:postgresql://...")
    }

    String getLocations() {
        return locations ?: "classpath:db-schema/"
    }
}