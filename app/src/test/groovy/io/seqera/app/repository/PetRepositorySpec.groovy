package io.seqera.app.repository

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.seqera.app.entity.Pet
import io.seqera.app.test.DbSpec
import jakarta.inject.Inject

/**
 * Repository test for Pet entity using DbSpec base class
 */
@MicronautTest(transactional = false, environments = ["test"])
class PetRepositorySpec extends DbSpec {

    @Inject
    PetRepository petRepository

    def cleanup() {
        // Clean up all pets after each test to ensure test isolation
        try {
            petRepository.deleteAll()
        } catch (Exception ignored) {
            // Ignore cleanup errors - database might be reset between tests
        }
    }

    def "test save and find pet"() {
        given: "a new pet"
        def pet = new Pet("Buddy", "Dog", "Golden Retriever", 3, "Golden", "Friendly dog")

        when: "saving the pet"
        def savedPet = petRepository.save(pet)

        then: "pet is saved with ID"
        savedPet.id != null
        savedPet.name == "Buddy"
        savedPet.species == "Dog"
        savedPet.breed == "Golden Retriever"
        savedPet.age == 3
        savedPet.color == "Golden"
        savedPet.description == "Friendly dog"
        savedPet.isAvailable == true
        savedPet.createdAt != null
        savedPet.updatedAt != null

        when: "finding pet by ID"
        def foundPet = petRepository.findById(savedPet.id)

        then: "pet is found"
        foundPet.isPresent()
        foundPet.get().name == "Buddy"
    }

    def "test find all pets"() {
        given: "multiple pets"
        def pet1 = new Pet("Buddy", "Dog", "Golden Retriever", 3, "Golden", "Friendly dog")
        def pet2 = new Pet("Whiskers", "Cat", "Siamese", 2, "Cream", "Calm cat")
        petRepository.save(pet1)
        petRepository.save(pet2)

        when: "finding all pets"
        def pets = petRepository.findAll().toList()

        then: "all pets are returned"
        pets.size() == 2
        pets.any { it.name == "Buddy" }
        pets.any { it.name == "Whiskers" }
    }

    def "test find by availability"() {
        given: "pets with different availability"
        def availablePet = new Pet("Buddy", "Dog", "Golden Retriever", 3, "Golden", "Available dog")
        availablePet.isAvailable = true
        def adoptedPet = new Pet("Max", "Dog", "German Shepherd", 5, "Black", "Adopted dog")
        adoptedPet.isAvailable = false
        petRepository.save(availablePet)
        petRepository.save(adoptedPet)

        when: "finding available pets"
        def availablePets = petRepository.findByIsAvailableTrue()

        then: "only available pets are returned"
        availablePets.size() == 1
        availablePets[0].name == "Buddy"

        when: "finding adopted pets"
        def adoptedPets = petRepository.findByIsAvailableFalse()

        then: "only adopted pets are returned"
        adoptedPets.size() == 1
        adoptedPets[0].name == "Max"
    }

    def "test find by species"() {
        given: "pets of different species"
        def dog = new Pet("Buddy", "Dog", "Golden Retriever", 3, "Golden", "Friendly dog")
        def cat = new Pet("Whiskers", "Cat", "Siamese", 2, "Cream", "Calm cat")
        petRepository.save(dog)
        petRepository.save(cat)

        when: "finding dogs"
        def dogs = petRepository.findBySpecies("Dog")

        then: "only dogs are returned"
        dogs.size() == 1
        dogs[0].name == "Buddy"

        when: "finding cats"
        def cats = petRepository.findBySpecies("Cat")

        then: "only cats are returned"
        cats.size() == 1
        cats[0].name == "Whiskers"
    }

    def "test find by age range"() {
        given: "pets of different ages"
        def youngPet = new Pet("Puppy", "Dog", "Labrador", 1, "Yellow", "Young dog")
        def adultPet = new Pet("Adult", "Dog", "Golden Retriever", 5, "Golden", "Adult dog")
        def seniorPet = new Pet("Senior", "Dog", "Beagle", 10, "Brown", "Senior dog")
        petRepository.save(youngPet)
        petRepository.save(adultPet)
        petRepository.save(seniorPet)

        when: "finding pets in age range 2-6"
        def midAgePets = petRepository.findByAgeBetween(2, 6)

        then: "only pets in range are returned"
        midAgePets.size() == 1
        midAgePets[0].name == "Adult"
    }

    def "test find by name containing"() {
        given: "pets with similar names"
        def buddy1 = new Pet("Buddy", "Dog", "Golden Retriever", 3, "Golden", "First buddy")
        def buddy2 = new Pet("Little Buddy", "Cat", "Tabby", 1, "Gray", "Second buddy")
        def max = new Pet("Max", "Dog", "German Shepherd", 5, "Black", "Not a buddy")
        petRepository.save(buddy1)
        petRepository.save(buddy2)
        petRepository.save(max)

        when: "searching by name containing 'Buddy'"
        def buddies = petRepository.findByNameContainsIgnoreCase("Buddy")

        then: "pets with 'Buddy' in name are returned"
        buddies.size() == 2
        buddies.any { it.name == "Buddy" }
        buddies.any { it.name == "Little Buddy" }
    }

    def "test count by availability"() {
        given: "pets with different availability"
        def available1 = new Pet("Available1", "Dog", "Breed1", 3, "Color1", "Available")
        available1.isAvailable = true
        def available2 = new Pet("Available2", "Cat", "Breed2", 2, "Color2", "Available")
        available2.isAvailable = true
        def adopted = new Pet("Adopted", "Dog", "Breed3", 5, "Color3", "Adopted")
        adopted.isAvailable = false
        petRepository.save(available1)
        petRepository.save(available2)
        petRepository.save(adopted)

        when: "counting available pets"
        def availableCount = petRepository.countByIsAvailable(true)

        then: "correct count is returned"
        availableCount == 2

        when: "counting adopted pets"
        def adoptedCount = petRepository.countByIsAvailable(false)

        then: "correct count is returned"
        adoptedCount == 1
    }
}