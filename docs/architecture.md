# Architecture Overview

## Project Structure

The application follows a multi-module architecture that separates API definitions from implementation:

### API Subproject (`api/`)
- **Purpose**: Contains TypeSpec definitions and generated model objects. This module serves as the contract layer, defining the API specification in a technology-agnostic way and generating all necessary artifacts for type-safe communication between client and server. It ensures API consistency and enables contract-first development where the API design drives the implementation.
- **Key Components**:
  - `spec/main.tsp` - TypeSpec API definitions (source of truth)
  - `src/main/java/io/seqera/api/model/` - Generated Java model objects (DTOs)

### App Subproject (`app/`)
- **Purpose**: Backend implementation and business logic. This module contains the actual application implementation that fulfills the contracts defined in the API module. It implements the business rules, data persistence, security, and all runtime concerns while adhering to the interfaces generated from the TypeSpec definitions. This separation allows for clean architecture where business logic is decoupled from API contracts.
- **Key Components**:
  - `src/main/java/io/seqera/api/spec/` - Generated controller interfaces
  - `src/main/groovy/io/seqera/app/` - Business implementation

## TypeSpec-Driven Development

**TypeSpec as Source of Truth**: All API definitions are written in TypeSpec, from which everything else is generated:

1. **TypeSpec Definition** (`api/spec/main.tsp`)
   - Defines API endpoints, request/response models, and validation rules
   - Single source of truth for the entire API contract

2. **Generated Artifacts**:
   - **OpenAPI Specification** - Standard REST API documentation
   - **Java Model Objects** - Request/response DTOs with validation
   - **Controller Interfaces** - Type-safe API contracts
   - **API Documentation** - Swagger UI and interactive docs

## Backend Package Architecture

The backend follows a layered architecture with clear separation of concerns:

### Controller (`io.seqera.app.controller`)
- **Purpose**: Expose business logic via REST interfaces
- **Responsibility**: Handle HTTP requests, validation, and response formatting
- **Example**: `PetController.groovy` implements `PetsApiSpec` interface

### Service (`io.seqera.app.service`) 
- **Purpose**: Implement business logic
- **Responsibility**: Core business rules, orchestration, and workflow
- **Example**: `PetService.groovy` handles pet management operations

### Repository (`io.seqera.app.repository`)
- **Purpose**: Implement database data access
- **Responsibility**: CRUD operations, queries, and data persistence
- **Example**: `PetRepository.groovy` with database-specific implementations

### Entity (`io.seqera.app.entity`)
- **Purpose**: Map database tables to Java objects
- **Responsibility**: Represent database structure and relationships
- **Example**: `Pet.groovy` with Micronaut Data annotations

### Mapper (`io.seqera.app.mapper`)
- **Purpose**: Conversion logic between entities and models
- **Responsibility**: Transform data between layers (Entity ↔ Model)
- **Example**: `PetMapper.groovy` converts between `Pet` entity and API models

## REST/Model Best Practices

### Top-Level Model Naming Convention

Top-level model objects follow the pattern: **`Verb-Entity-Request/Response`**

**Examples**:
- `CreatePetRequest` - Request to create a new pet
- `CreatePetResponse` - Response after creating a pet
- `UpdatePetRequest` - Request to update an existing pet
- `UpdatePetResponse` - Response after updating a pet
- `GetPetResponse` - Response when retrieving a pet
- `ListPetsResponse` - Response when listing multiple pets

### Nested Model Objects

Nested model objects mimic the entity names directly:

**Examples**:
- `Pet` - Core pet model (matches `Pet` entity)
- `ServiceInfo` - Service information model
- `ErrorResponse` - Standard error response format

### Benefits of This Approach

1. **Consistency**: Clear, predictable naming across the entire API
2. **Type Safety**: Generated models provide compile-time validation
3. **Documentation**: Self-documenting API contracts
4. **Maintainability**: Changes in TypeSpec automatically propagate to all layers
5. **Contract-First**: API design drives implementation, not the reverse