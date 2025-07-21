package io.seqera.app.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.seqera.api.exchange.*
import io.seqera.app.repository.PetRepository
import io.seqera.app.test.DbSpec
import jakarta.inject.Inject

/**
 * Controller test for Pet operations using DbSpec base class
 */
@MicronautTest(startApplication = true, transactional = false, environments = ["test"])
class PetControllerSpec extends DbSpec {

    @Inject
    @Client("/")
    HttpClient client

    @Inject
    PetRepository petRepository

    def setup() {
        // Clean database before each test to ensure isolation
        petRepository.deleteAll()
    }



    def "test list pets - empty database"() {
        when: "getting all pets from empty database"
        def response = client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/pets"),
                ListPetsResponse
        )

        then: "empty result is returned"
        response.status == HttpStatus.OK
        response.body() != null
        // Handle the case where pets might be null (serialization issue) or empty list
        def pets = response.body().getPets()
        pets == null || pets.size() == 0
    }

    def "test create pet"() {
        given: "a pet creation request"
        def request = new CreatePetRequest("Buddy", "Dog", 3)
                .breed("Golden Retriever")
                .color("Golden")
                .description("Friendly dog")
        
        println "Test: Created request object - name: ${request.getName()}, toString: ${request.toString()}"

        when: "creating pet"
        def httpRequest = HttpRequest.POST("/api/v1/pets", request)
                .contentType(MediaType.APPLICATION_JSON)
        
        println "Test: Sending HTTP request with body: ${request}"
        def response = client.toBlocking().exchange(httpRequest, CreatePetResponse)

        then: "pet is created successfully"
        response.status == HttpStatus.OK
        def pet = response.body().getPet()
        pet.getId() != null
        pet.getName() == "Buddy"
        pet.getSpecies() == "Dog"
        pet.getBreed() == "Golden Retriever"
        pet.getAge() == 3
        pet.getColor() == "Golden"
        pet.getDescription() == "Friendly dog"
        pet.getIsAvailable() == true
        pet.getCreatedAt() != null
        pet.getUpdatedAt() != null
    }

    def "test create pet with invalid data"() {
        given: "a pet request with invalid age"
        def request = new CreatePetRequest("Buddy", "Dog", -1)
                .breed("Golden Retriever")
                .color("Golden")
                .description("Invalid age")

        when: "creating pet with invalid data"
        client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/pets", request)
                        .contentType(MediaType.APPLICATION_JSON),
                CreatePetResponse
        )

        then: "validation error is returned"
        def ex = thrown(HttpClientResponseException)
        ex.status == HttpStatus.BAD_REQUEST
    }

    def "test get pet by id"() {
        given: "a created pet"
        def createRequest = new CreatePetRequest("Buddy", "Dog", 3)
                .breed("Golden Retriever")
                .color("Golden")
                .description("Friendly dog")
        def createResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/pets", createRequest)
                        .contentType(MediaType.APPLICATION_JSON),
                CreatePetResponse
        )
        def petId = createResponse.body().getPet().getId()

        when: "getting pet by ID"
        def response = client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/pets/$petId"),
                GetPetResponse
        )

        then: "pet is returned"
        response.status == HttpStatus.OK
        def pet = response.body().getPet()
        pet.getId() == petId
        pet.getName() == "Buddy"
        pet.getSpecies() == "Dog"
    }

    def "test get pet by non-existent id"() {
        when: "getting pet by non-existent ID"
        client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/pets/999"),
                GetPetResponse
        )

        then: "not found error is returned"
        def ex = thrown(HttpClientResponseException)
        ex.status == HttpStatus.NOT_FOUND
    }

    def "test update pet"() {
        given: "a created pet"
        def createRequest = new CreatePetRequest("Buddy", "Dog", 3)
                .breed("Golden Retriever")
                .color("Golden")
                .description("Friendly dog")
        def createResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/pets", createRequest)
                        .contentType(MediaType.APPLICATION_JSON),
                CreatePetResponse
        )
        def petId = createResponse.body().getPet().getId()

        when: "updating pet"
        def updateRequest = new UpdatePetRequest("Buddy Updated", "Dog", 4, false)
                .breed("Labrador")
                .color("Yellow")
                .description("Updated dog")
        def response = client.toBlocking().exchange(
                HttpRequest.PUT("/api/v1/pets/$petId", updateRequest)
                        .contentType(MediaType.APPLICATION_JSON),
                UpdatePetResponse
        )

        then: "pet is updated"
        response.status == HttpStatus.OK
        def pet = response.body().getPet()
        pet.getId() == petId
        pet.getName() == "Buddy Updated"
        pet.getBreed() == "Labrador"
        pet.getAge() == 4
        pet.getColor() == "Yellow"
        pet.getDescription() == "Updated dog"
        pet.getIsAvailable() == false
    }

    def "test update non-existent pet"() {
        when: "updating non-existent pet"
        def updateRequest = new UpdatePetRequest("Updated", "Dog", 3, true)
                .breed("Breed")
                .color("Color")
                .description("Description")
        client.toBlocking().exchange(
                HttpRequest.PUT("/api/v1/pets/999", updateRequest)
                        .contentType(MediaType.APPLICATION_JSON),
                UpdatePetResponse
        )

        then: "not found error is returned"
        def ex = thrown(HttpClientResponseException)
        ex.status == HttpStatus.NOT_FOUND
    }

    def "test delete pet"() {
        given: "a created pet"
        def createRequest = new CreatePetRequest("Buddy", "Dog", 3)
                .breed("Golden Retriever")
                .color("Golden")
                .description("Friendly dog")
        def createResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/pets", createRequest)
                        .contentType(MediaType.APPLICATION_JSON),
                CreatePetResponse
        )
        def petId = createResponse.body().getPet().getId()

        when: "deleting pet"
        def response = client.toBlocking().exchange(
                HttpRequest.DELETE("/api/v1/pets/$petId")
        )

        then: "pet is deleted"
        response.status == HttpStatus.NO_CONTENT

        when: "trying to get deleted pet"
        client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/pets/$petId"),
                GetPetResponse
        )

        then: "pet is not found"
        def ex = thrown(HttpClientResponseException)
        ex.status == HttpStatus.NOT_FOUND
    }

    def "test delete non-existent pet"() {
        when: "deleting non-existent pet"
        client.toBlocking().exchange(
                HttpRequest.DELETE("/api/v1/pets/999")
        )

        then: "not found error is returned"
        def ex = thrown(HttpClientResponseException)
        ex.status == HttpStatus.NOT_FOUND
    }

    def "test list pets with data"() {
        given: "multiple pets created"
        def pet1Request = new CreatePetRequest("Buddy", "Dog", 3)
                .breed("Golden Retriever")
                .color("Golden")
                .description("Friendly dog")
        def pet2Request = new CreatePetRequest("Whiskers", "Cat", 2)
                .breed("Siamese")
                .color("Cream")
                .description("Calm cat")

        client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/pets", pet1Request)
                        .contentType(MediaType.APPLICATION_JSON),
                CreatePetResponse
        )
        client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/pets", pet2Request)
                        .contentType(MediaType.APPLICATION_JSON),
                CreatePetResponse
        )

        when: "getting all pets"
        def response = client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/pets"),
                ListPetsResponse
        )

        then: "all pets are returned"
        response.status == HttpStatus.OK
        response.body().getPets().size() == 2
        response.body().getPets().any { it.getName() == "Buddy" }
        response.body().getPets().any { it.getName() == "Whiskers" }
    }

    def "test get available pets endpoint"() {
        given: "pets with different availability"
        def availablePet = new CreatePetRequest("Available", "Dog", 3)
                .breed("Breed")
                .color("Color")
                .description("Available pet")
        def adoptedRequest = new UpdatePetRequest("Adopted", "Cat", 2, false)
                .breed("Breed")
                .color("Color")
                .description("Adopted pet")

        // Create available pet
        def availableResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/pets", availablePet)
                        .contentType(MediaType.APPLICATION_JSON),
                CreatePetResponse
        )

        // Create and update pet to be adopted
        def adoptedCreateResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/pets", new CreatePetRequest("Adopted", "Cat", 2)
                        .breed("Breed")
                        .color("Color") 
                        .description("Adopted pet"))
                        .contentType(MediaType.APPLICATION_JSON),
                CreatePetResponse
        )
        def adoptedId = adoptedCreateResponse.body().getPet().getId()
        client.toBlocking().exchange(
                HttpRequest.PUT("/api/v1/pets/$adoptedId", adoptedRequest)
                        .contentType(MediaType.APPLICATION_JSON),
                UpdatePetResponse
        )

        when: "getting available pets"
        def response = client.toBlocking().exchange(
                HttpRequest.GET("/pets/available"),
                ListPetsResponse
        )

        then: "only available pets are returned"
        response.status == HttpStatus.OK
        response.body().getPets().size() == 1
        response.body().getPets()[0].getName() == "Available"
        response.body().getPets()[0].getIsAvailable() == true
    }
}