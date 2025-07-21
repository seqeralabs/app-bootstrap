package io.seqera.app.mapper

import io.micronaut.context.annotation.Mapper
import io.micronaut.core.annotation.Introspected
import io.seqera.api.exchange.*
import io.seqera.app.entity.Pet
import jakarta.inject.Singleton

/**
 * Declarative bean mapper for Pet entity mappings using Micronaut Bean Mapper
 * Maps between Pet entity and various Pet exchange objects
 */
@Singleton
@Introspected
abstract class PetMapper {

    /**
     * Map Pet entity to Pet exchange object
     */
    abstract io.seqera.api.exchange.Pet toPet(Pet entity)

    /**
     * Map CreatePetRequest to Pet entity
     * Sets isAvailable to true by default
     */
    @Mapper.Mapping(to = "isAvailable", from = "#{true}")
    abstract Pet fromCreateRequest(CreatePetRequest request)

    /**
     * Map UpdatePetRequest to Pet entity
     */
    abstract Pet fromUpdateRequest(UpdatePetRequest request)

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
        List<io.seqera.api.exchange.Pet> pets = entities.collect { entity -> toPet(entity) }
        return new ListPetsResponse(pets)
    }
}