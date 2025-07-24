package io.seqera.app.test

/**
 * Define a trait to configure PostgreSQL tests
 */
trait PostgresTestContainer {

    Map<String,String> getPostgresProperties() {
        return [
                "datasources.default.driver-class-name": "org.testcontainers.jdbc.ContainerDatabaseDriver",
                "datasources.default.url": "jdbc:tc:postgresql:15.2:///testdb?TC_INITSCRIPT=file:${SchemaFileMerger.getMergedSchemaFile()}"
        ]
    }
}
