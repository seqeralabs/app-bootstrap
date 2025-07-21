package io.seqera.app.test

/**
 * Define a trait to configure PostgreSQL tests
 */
trait PostgresTestContainer {

    Map<String,String> getPostgresProperties() {
        Map.of(
            "datasources.default.driver-class-name", "org.testcontainers.jdbc.ContainerDatabaseDriver",
            "datasources.default.url", "jdbc:tc:postgresql:15:///testdb?TC_INITSCRIPT=file:src/main/resources/db-schema/m01__create-pets-table.sql"
        )
    }
}