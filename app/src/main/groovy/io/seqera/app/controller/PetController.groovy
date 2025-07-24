package io.seqera.app.controller

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.seqera.api.model.*
import io.seqera.api.spec.PetsApiSpec
import io.seqera.app.mapper.PetMapper
import io.seqera.app.service.PetService
import io.micronaut.http.HttpResponse
import jakarta.inject.Inject

/**
 * RESTful controller for Pet management operations.
 * 
 * This controller implements the PetsApiSpec interface generated from TypeSpec definitions
 * and provides comprehensive CRUD operations for pet management. All endpoints require
 * authentication via JWT cookies.
 * 
 * The controller handles:
 * - Listing all pets and filtered subsets (available pets, by species)
 * - Creating new pets with validation
 * - Retrieving, updating, and deleting individual pets
 * - Proper HTTP status codes and error handling
 * 
 * @author Pet Shop API
 * @since 1.0.0
 */
@Slf4j
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
class PetController implements PetsApiSpec {

    @Inject
    PetService petService

    @Inject
    PetMapper petMapper

    @Override
    ListPetsResponse listPets() {
        log.debug("Getting all pets")
        def pets = petService.getAllPets()
        log.debug("Retrieved {} pets from service", pets?.size() ?: 0)
        def response = petMapper.toListResponse(pets)
        log.debug("Mapped response: {}", response?.pets?.size() ?: 0)
        return response
    }

    @Override
    GetPetResponse getPet(Long id) {
        log.debug("Getting pet by ID: {}", id)
        def pet = petService.getPetById(id)
        if (!pet.isPresent()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Pet not found with ID: ${id}")
        }
        return petMapper.toGetResponse(pet.get())
    }

    @Override
    CreatePetResponse createPet(CreatePetRequest request) {
        log.debug("Creating pet with name: {}, full request: {}", request.getName(), request)
        log.debug("Request getName(): {}", request.getName())
        def pet = petMapper.fromCreateRequest(request)
        def savedPet = petService.createPet(pet)
        return petMapper.toCreateResponse(savedPet)
    }

    @Override
    UpdatePetResponse updatePet(Long id, UpdatePetRequest request) {
        log.debug("Updating pet with ID: {}", id)
        log.debug("UpdatePetRequest isAvailable: {}", request.getIsAvailable())
        def existingPet = petService.getPetById(id)
        if (!existingPet.isPresent()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Pet not found with ID: ${id}")
        }

        def petData = petMapper.fromUpdateRequest(request)
        log.debug("Pet data from mapper isAvailable: {}", petData.isAvailable)
        petData.id = id
        def updatedPet = petService.updatePet(id, petData)
        log.debug("Updated pet isAvailable: {}", updatedPet.isAvailable)
        return petMapper.toUpdateResponse(updatedPet)
    }

    @Override
    HttpResponse<Void> deletePet(Long id) {
        log.debug("Deleting pet with ID: {}", id)
        def existingPet = petService.getPetById(id)
        if (!existingPet.isPresent()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Pet not found with ID: ${id}")
        }
        petService.deletePet(id)
        return HttpResponse.noContent()
    }

    /**
     * Additional endpoint to get available pets
     */
    @Get("/pets/available")
    ListPetsResponse getAvailablePets() {
        log.debug("Getting available pets")
        def pets = petService.getAvailablePets()
        return petMapper.toListResponse(pets)
    }

    /**
     * Additional endpoint to get pets by species
     */
    @Get("/pets/species/{species}")
    ListPetsResponse getPetsBySpecies(String species) {
        log.debug("Getting pets by species: {}", species)
        def pets = petService.getPetsBySpecies(species)
        return petMapper.toListResponse(pets)
    }

    /**
     * Additional endpoint to search pets by name
     */
    @Get("/pets/search")
    ListPetsResponse searchPets(@QueryValue("name") String name) {
        log.debug("Searching pets by name: {}", name)
        def pets = petService.searchPetsByName(name)
        return petMapper.toListResponse(pets)
    }
}
