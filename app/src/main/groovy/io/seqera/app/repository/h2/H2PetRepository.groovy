package io.seqera.app.repository.h2

import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.seqera.app.repository.PetRepository

@Primary
@Requires(env = "h2")
@JdbcRepository(dialect = Dialect.H2)
abstract class H2PetRepository implements PetRepository {
}