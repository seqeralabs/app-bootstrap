package io.seqera.app.test

import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.TestInstance
import spock.lang.Specification

/**
 * Implements a base class for setting up DB tests
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DbSpec extends Specification implements PostgresTestContainer, TestPropertyProvider {

    @Override
    Map<String, String> getProperties() {
        return getPostgresProperties()
    }
}