package io.seqera.app.service

import groovy.util.logging.Slf4j
import io.micronaut.data.exceptions.DataAccessException
import io.seqera.app.entity.Pet
import io.seqera.app.repository.PetRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional

/**
 * Service for Pet entity business logic
 */
@Slf4j
@Singleton
@Transactional
class PetService {

    @Inject
    PetRepository petRepository

    /**
     * Get all pets
     */
    List<Pet> getAllPets() {
        log.debug("Getting all pets")
        return petRepository.findAll().toList()
    }

    /**
     * Get pet by ID
     */
    Optional<Pet> getPetById(Long id) {
        log.debug("Getting pet by ID: {}", id)
        return petRepository.findById(id)
    }

    /**
     * Create a new pet
     */
    Pet createPet(Pet pet) {
        log.info("Creating pet with name: {}", pet.name)
        try {
            def savedPet = petRepository.save(pet)
            log.info("Created pet with ID: {}", savedPet.id)
            return savedPet
        } catch (DataAccessException e) {
            log.error("Failed to create pet with name: {}", pet.name, e)
            throw new RuntimeException("Failed to create pet: ${e.message}", e)
        }
    }

    /**
     * Update an existing pet
     */
    Pet updatePet(Long id, Pet petData) {
        log.info("Updating pet with ID: {}", id)
        def existingPet = petRepository.findById(id)
        if (!existingPet.isPresent()) {
            log.warn("Pet with ID {} not found for update", id)
            throw new RuntimeException("Pet not found with ID: ${id}")
        }

        def pet = existingPet.get()
        pet.name = petData.name
        pet.species = petData.species
        pet.breed = petData.breed
        pet.age = petData.age
        pet.color = petData.color
        pet.description = petData.description
        pet.isAvailable = petData.isAvailable

        try {
            def updatedPet = petRepository.update(pet)
            log.info("Updated pet with ID: {}", id)
            return updatedPet
        } catch (DataAccessException e) {
            log.error("Failed to update pet with ID: {}", id, e)
            throw new RuntimeException("Failed to update pet: ${e.message}", e)
        }
    }

    /**
     * Delete a pet by ID
     */
    boolean deletePet(Long id) {
        log.info("Deleting pet with ID: {}", id)
        def existingPet = petRepository.findById(id)
        if (!existingPet.isPresent()) {
            log.warn("Pet with ID {} not found for deletion", id)
            throw new RuntimeException("Pet not found with ID: ${id}")
        }

        try {
            petRepository.deleteById(id)
            log.info("Deleted pet with ID: {}", id)
            return true
        } catch (DataAccessException e) {
            log.error("Failed to delete pet with ID: {}", id, e)
            throw new RuntimeException("Failed to delete pet: ${e.message}", e)
        }
    }

    /**
     * Get available pets
     */
    List<Pet> getAvailablePets() {
        log.debug("Getting available pets")
        return petRepository.findByIsAvailableTrue()
    }

    /**
     * Get pets by species
     */
    List<Pet> getPetsBySpecies(String species) {
        log.debug("Getting pets by species: {}", species)
        return petRepository.findBySpecies(species)
    }

    /**
     * Get pets by age range
     */
    List<Pet> getPetsByAgeRange(Integer minAge, Integer maxAge) {
        log.debug("Getting pets by age range: {} - {}", minAge, maxAge)
        return petRepository.findByAgeBetween(minAge, maxAge)
    }

    /**
     * Search pets by name
     */
    List<Pet> searchPetsByName(String name) {
        log.debug("Searching pets by name: {}", name)
        return petRepository.findByNameContainsIgnoreCase(name)
    }

    /**
     * Get pet statistics
     */
    Map<String, Long> getPetStatistics() {
        log.debug("Getting pet statistics")
        return [
            total: petRepository.count(),
            available: petRepository.countByIsAvailable(true),
            adopted: petRepository.countByIsAvailable(false)
        ]
    }
}