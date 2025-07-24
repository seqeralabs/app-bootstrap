import "@typespec/http";
import "@typespec/rest";
import "@typespec/openapi3";

using TypeSpec.Http;
using TypeSpec.Rest;

/**
 * Pet Shop API
 * A comprehensive example of Micronaut application best practices for a pet shop
 */
@service({
  title: "Pet Shop API",
  version: "1.0.0",
})
namespace PetShop;

/**
 * Service information model
 */
model ServiceInfo {
  /**
   * Application version
   */
  version: string;

  /**
   * Build commit ID
   */
  commitId: string;
}

/**
 * Service information response
 */
model ServiceInfoResponse {
  serviceInfo: ServiceInfo;
}

/**
 * Pet model for API responses
 */
model Pet {
  /**
   * Pet ID
   */
  id: int64;

  /**
   * Pet's name
   */
  name: string;

  /**
   * Pet species (e.g., dog, cat, bird)
   */
  species: string;

  /**
   * Pet breed
   */
  breed?: string;

  /**
   * Pet age in years
   */
  age: int32;

  /**
   * Pet color
   */
  color?: string;

  /**
   * Pet description
   */
  description?: string;

  /**
   * Whether the pet is available for adoption
   */
  isAvailable: boolean;

  /**
   * Creation timestamp
   */
  createdAt: utcDateTime;

  /**
   * Last update timestamp
   */
  updatedAt: utcDateTime;
}

/**
 * Pet creation request
 */
model CreatePetRequest {
  /**
   * Pet's name
   */
  @minLength(1)
  @maxLength(100)
  name: string;

  /**
   * Pet species (e.g., dog, cat, bird)
   */
  @minLength(1)
  @maxLength(50)
  species: string;

  /**
   * Pet breed
   */
  @maxLength(50)
  breed?: string;

  /**
   * Pet age in years
   */
  @minValue(0)
  @maxValue(50)
  age: int32;

  /**
   * Pet color
   */
  @maxLength(20)
  color?: string;

  /**
   * Pet description
   */
  @maxLength(500)
  description?: string;
}

/**
 * Pet creation response
 */
model CreatePetResponse {
  pet: Pet;
}

/**
 * Pet update request
 */
model UpdatePetRequest {
  /**
   * Pet's name
   */
  @minLength(1)
  @maxLength(100)
  name: string;

  /**
   * Pet species (e.g., dog, cat, bird)
   */
  @minLength(1)
  @maxLength(50)
  species: string;

  /**
   * Pet breed
   */
  @maxLength(50)
  breed?: string;

  /**
   * Pet age in years
   */
  @minValue(0)
  @maxValue(50)
  age: int32;

  /**
   * Pet color
   */
  @maxLength(20)
  color?: string;

  /**
   * Pet description
   */
  @maxLength(500)
  description?: string;

  /**
   * Whether the pet is available for adoption
   */
  isAvailable: boolean;
}

/**
 * Pet update response
 */
model UpdatePetResponse {
  pet: Pet;
}

/**
 * Get pet response
 */
model GetPetResponse {
  pet: Pet;
}

/**
 * List pets response
 */
model ListPetsResponse {
  pets: Pet[];
}

/**
 * Error response
 */
@error
model ErrorResponse {
  /**
   * Error message
   */
  message: string;

  /**
   * Error details
   */
  details?: string;
}

/**
 * Service information operations
 */
@tag("Service")
interface ServiceApi {
  /**
   * Get service information
   */
  @get
  @route("/service-info")
  getServiceInfo(): ServiceInfoResponse;
}

/**
 * Pet management operations
 */
@tag("Pets")
@route("/api/v1/pets")
interface PetApi {
  /**
   * List all pets
   */
  @get
  @route("/")
  listPets(): ListPetsResponse;

  /**
   * Get pet by ID
   */
  @get
  @route("/{id}")
  getPet(@path id: int64): GetPetResponse | ErrorResponse;

  /**
   * Create a new pet
   */
  @post
  @route("/")
  createPet(@body request: CreatePetRequest): CreatePetResponse | ErrorResponse;

  /**
   * Update an existing pet
   */
  @put
  @route("/{id}")
  updatePet(@path id: int64, @body request: UpdatePetRequest): UpdatePetResponse | ErrorResponse;

  /**
   * Delete a pet
   */
  @delete
  @route("/{id}")
  deletePet(@path id: int64): void | ErrorResponse;
}