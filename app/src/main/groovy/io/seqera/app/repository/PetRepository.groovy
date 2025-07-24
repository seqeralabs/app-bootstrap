package io.seqera.app.repository

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.seqera.app.entity.Pet

/**
 * Repository interface for Pet entity operations.
 * 
 * This repository provides comprehensive CRUD operations for Pet entities including
 * specialized query methods for common business operations like finding pets by
 * availability, species, age ranges, and name searches.
 * 
 * The repository uses PostgreSQL dialect by default but can be overridden by
 * environment-specific implementations.
 * 
 * @author Pet Shop API  
 * @since 1.0.0
 */
@Repository
@JdbcRepository(dialect = Dialect.POSTGRES)
interface PetRepository extends CrudRepository<Pet, Long> {

    /**
     * Find all pets that are available for adoption
     */
    List<Pet> findByIsAvailableTrue()

    /**
     * Find all pets that are not available for adoption
     */
    List<Pet> findByIsAvailableFalse()

    /**
     * Find pets by species
     */
    List<Pet> findBySpecies(String species)

    /**
     * Find pets by species and availability
     */
    List<Pet> findBySpeciesAndIsAvailable(String species, Boolean isAvailable)

    /**
     * Find pets by age range
     */
    List<Pet> findByAgeBetween(Integer minAge, Integer maxAge)

    /**
     * Find pets by name containing (case insensitive)
     */
    List<Pet> findByNameContainsIgnoreCase(String name)

    /**
     * Count pets by availability status
     */
    Long countByIsAvailable(Boolean isAvailable)

    /**
     * Count pets by species
     */
    Long countBySpecies(String species)
}