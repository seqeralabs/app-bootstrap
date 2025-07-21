package io.seqera.app.mapper

import io.micronaut.core.annotation.Introspected
import io.seqera.api.exchange.*
import io.seqera.app.entity.Pet
import jakarta.inject.Singleton

/**
 * Bean mapper for Pet entity mappings
 * Maps between Pet entity and various Pet exchange objects
 */
@Singleton
@Introspected
class PetMapper {

    /**
     * Map Pet entity to Pet exchange object
     */
    io.seqera.api.exchange.Pet toPet(Pet entity) {
        return new io.seqera.api.exchange.Pet(
            entity.id,
            entity.name,
            entity.species,
            entity.age,
            entity.isAvailable != null ? entity.isAvailable : true,
            entity.createdAt ? java.time.OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(entity.createdAt.getTime()), 
                java.time.ZoneOffset.UTC) : null,
            entity.updatedAt ? java.time.OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(entity.updatedAt.getTime()), 
                java.time.ZoneOffset.UTC) : null
        ).breed(entity.breed)
         .color(entity.color)
         .description(entity.description)
    }

    /**
     * Map CreatePetRequest to Pet entity
     */
    Pet fromCreateRequest(CreatePetRequest request) {
        return new Pet(
            request.getName(),
            request.getSpecies(),
            request.getBreed(),
            request.getAge(),
            request.getColor(),
            request.getDescription()
        )
    }

    /**
     * Map UpdatePetRequest to Pet entity
     */
    Pet fromUpdateRequest(UpdatePetRequest request) {
        def pet = new Pet()
        pet.name = request.getName()
        pet.species = request.getSpecies()
        pet.breed = request.getBreed()
        pet.age = request.getAge()
        pet.color = request.getColor()
        pet.description = request.getDescription()
        pet.isAvailable = request.getIsAvailable()
        return pet
    }

    /**
     * Map Pet entity to CreatePetResponse
     */
    CreatePetResponse toCreateResponse(Pet entity) {
        return new CreatePetResponse(toPet(entity))
    }

    /**
     * Map Pet entity to UpdatePetResponse
     */
    UpdatePetResponse toUpdateResponse(Pet entity) {
        return new UpdatePetResponse(toPet(entity))
    }

    /**
     * Map Pet entity to GetPetResponse
     */
    GetPetResponse toGetResponse(Pet entity) {
        return new GetPetResponse(toPet(entity))
    }

    /**
     * Map list of Pet entities to ListPetsResponse
     */
    ListPetsResponse toListResponse(List<Pet> entities) {
        if (entities == null) {
            return new ListPetsResponse([])
        }
        try {
            def pets = entities.collect { entity -> 
                try {
                    return toPet(entity)
                } catch (Exception e) {
                    throw new RuntimeException("Failed to map Pet entity with ID ${entity.id}: ${e.message}", e)
                }
            }
            return new ListPetsResponse(pets)
        } catch (Exception e) {
            throw new RuntimeException("Failed to map pets list: ${e.message}", e)
        }
    }
}