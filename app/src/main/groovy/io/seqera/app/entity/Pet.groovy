package io.seqera.app.entity

import io.micronaut.data.annotation.*
import io.micronaut.data.model.naming.NamingStrategies

/**
 * Pet entity representing a pet in the pet shop system.
 * 
 * This entity maps to the 'pets' table and includes automatic audit fields
 * for creation and update timestamps.
 * 
 * @author Pet Shop API
 * @since 1.0.0
 */
@MappedEntity(value = "pets", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase)
class Pet {

    @Id
    @GeneratedValue
    Long id

    String name
    String species
    String breed
    Integer age
    String color
    String description
    Boolean isAvailable = true

    @DateCreated
    Date createdAt

    @DateUpdated
    Date updatedAt

    Pet() {}

    Pet(String name, String species, String breed, Integer age, String color, String description) {
        this.name = name
        this.species = species
        this.breed = breed
        this.age = age
        this.color = color
        this.description = description
        this.isAvailable = true
    }
}