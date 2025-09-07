# Pet Shop API - Modern Java/Micronaut Application

A comprehensive Pet Shop API demonstrating modern Java/Micronaut application best practices with multi-module structure, TypeSpec-driven API development, JWT authentication, and production-ready patterns.

## 🏗️ Architecture Overview

This project demonstrates a clean, maintainable multi-module architecture with API-first design:

```
app-bootstrap/
├── api/                     # API definitions and model objects
│   ├── src/main/java/
│   │   └── io/seqera/api/
│   │       └── model/       # Generated Request/Response DTOs
│   └── spec/                # TypeSpec API definitions (source of truth)
│       ├── main.tsp         # Main API specification
│       └── package.json     # TypeSpec build configuration
├── app/                     # Main application implementation
│   └── src/main/
│       ├── java/
│       │   └── io/seqera/api/
│       │       └── spec/    # Generated Controller interfaces
│       └── groovy/
│           └── io/seqera/app/
│               ├── controller/  # API implementation
│               ├── service/     # Business logic layer
│               ├── repository/  # Data access layer
│               ├── entity/      # Domain entities
│               └── security/    # Authentication providers
├── buildSrc/                # Custom Gradle plugins
└── docs/                    # API documentation
```

## 🚀 Key Features & Components

### Application Structure

#### 1. **API Module** (`api/`)
- **TypeSpec Source**: `spec/main.tsp` - Single source of truth for API definitions
- **Generated Models**: `src/main/java/io/seqera/api/model/` - DTOs for request/response

#### 2. **Application Module** (`app/`)
- **Generated Interfaces**: `src/main/java/io/seqera/api/spec/` - Controller interfaces
- **Entities**: `entity/Pet.groovy` - Domain models with Micronaut Data annotations
- **Repositories**: `repository/PetRepository.groovy` - Data access with multiple database support
- **Services**: Business logic layer (currently handled by controllers)
- **Controllers**: `controller/PetController.groovy` - API implementation, `AuthController.groovy` - Authentication handling
- **Security**: `security/BasicAuthenticationProvider.groovy` - JWT authentication

### Technology Stack
- **Framework**: Micronaut 4.9.7 with Groovy
- **Authentication**: JWT with HTTP-only cookies
- **Database**: PostgreSQL (production) + H2 (local development)
- **Data Access**: Micronaut Data JDBC with multiple dialect support
- **API Documentation**: TypeSpec → OpenAPI → Swagger UI
- **Security**: JWT-based authentication with cookie storage
- **Build**: Gradle with custom convention plugins
- **Containerization**: Google Jib for optimized container images

## 🔐 Security Architecture

### JWT Cookie Authentication
- **Authentication Provider**: Custom `BasicAuthenticationProvider` with hardcoded credentials
- **Token Storage**: HTTP-only cookies for security
- **Session Management**: JWT tokens with configurable expiration
- **Protected Endpoints**: All `/pets/**` endpoints require authentication
- **Public Endpoints**: Login pages, static resources, API documentation

### Security Configuration
- **Login Flow**: POST to `/login` → JWT cookie set → Redirect to `/dashboard`
- **Logout Flow**: POST to `/logout` → Cookie cleared → Redirect to `/`
- **Authentication Mode**: Cookie-based (not session-based)
- **JWT Secret**: 256-bit minimum requirement (configured in `application.yml`)

### Default Credentials
- **Admin**: `username: admin`, `password: password`
- **User**: `username: user`, `password: secret`

## 🗂️ Database Architecture

### Repository Pattern with Multi-Database Support
- **PostgreSQL**: Production database with full SQL features
- **H2**: Local development with PostgreSQL compatibility mode
- **Repository Abstraction**: `@Primary` and `@Requires(env = "h2")` annotations for environment-specific implementations

### Entity Design
```groovy
@MappedEntity("pets")
class Pet {
    @Id @GeneratedValue Long id
    String name, species, breed, color, description
    Integer age
    Boolean isAvailable
    @DateCreated OffsetDatTime createdAt
    @DateUpdated OffsetDatTime updatedAt
}
```

## 🛠️ API Development Workflow

### TypeSpec-Driven Development with Versioning

The API uses **TypeSpec's native versioning** with the `@versioned` decorator for type-safe API evolution:

1. **Define API Contract** in `api/spec/main.tsp` with versioning:
   ```typescript
   @service(#{
     title: "Pet Shop API",
   })
   @versioned(Versions)
   namespace PetShop {
   
   enum Versions {
     @doc("1.0.0") v1_0: "1.0.0",
   }
   
   @route("/pets")
   interface Pets {
     @get list(): Pet[];
     @post create(@body pet: CreatePetRequest): Pet;
     @get("{id}") get(@path id: int64): Pet;
   }
   }
   ```

2. **Generate Versioned OpenAPI Specs**:
   ```bash
   cd api/spec
   npm install
   npm run build
   # Generates: api/build/openapi/openapi-1.0.0.yaml (versioned)
   # Generates: api/build/openapi/openapi-latest.yaml (highest version)
   ```

3. **Generate Java Code**:
   ```bash
   ./gradlew :app:generateApiCode
   # Generates interfaces in app/src/main/java/io/seqera/api/spec/
   # Generates DTOs in api/src/main/java/io/seqera/api/model/
   ```

4. **Implement Controllers**: Controllers implement the generated interfaces

### API Versioning Strategy

This project uses **TypeSpec's native `@versioned` decorator** for type-safe API evolution:

#### Version Management
- **Version Definition**: Versions are defined in the `Versions` enum using semantic versioning
- **TypeSpec Compilation**: Produces `openapi-{version}.yaml` files (one per version) 
- **Latest File**: Bash oneliner finds highest version and creates `openapi-latest.yaml`
- **Backward Compatibility**: Existing consumers can continue using `openapi-latest.yaml`

#### Adding New API Versions
```typescript
// In api/spec/main.tsp
enum Versions {
  @doc("1.0.0") v1_0: "1.0.0",  // Current version
  @doc("1.1.0") v1_1: "1.1.0",  // New version with added features
  @doc("2.0.0") v2_0: "2.0.0",  // Breaking changes
}

// Adding version-specific features
interface PetApi {
  @get list(): Pet[];
  
  // New field in v1.1
  @added(Versions.v1_1)
  @get("/categories") getCategories(): PetCategory[];
  
  // Breaking change in v2.0
  @renamedFrom(Versions.v2_0, "list")
  @get("/all") getAllPets(): Pet[];
}
```

#### Generated Files
- `api/build/openapi/openapi-1.0.0.yaml` - Version-specific API spec
- `api/build/openapi/openapi-1.1.0.yaml` - New version with added features  
- `api/build/openapi/openapi-latest.yaml` - Always contains highest version

### API Specification Publishing
- **Swagger UI**: Automatically generated at `/openapi/index.html`
- **OpenAPI Spec**: Available at `/openapi/openapi-latest.yaml`
- **Versioned Specs**: Versioned files like `/openapi/openapi-1.0.0.yaml`
- **WebJars Integration**: Uses Swagger UI WebJar for consistent styling

## 🚀 Getting Started

### Prerequisites
- Java 21 (with compatibility for Java 17)
- PostgreSQL 15+ (for production mode)
- Node.js (for TypeSpec compilation)

### Local Development Setup

#### 1. Using H2 Database (Recommended for Development)
```bash
# Run with H2 in-memory database
./gradlew :app:run -PmicronautEnvs=h2

# Access the application
open http://localhost:8080/
```

#### 2. Using PostgreSQL
```bash
# Start PostgreSQL with Docker
docker run -d \
  --name postgres-pet-shop \
  -e POSTGRES_DB=app_bootstrap \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

# Run the application
./gradlew :app:run
```

### Local Configuration Implications

When running locally, the application:
- **H2 Mode**: Uses in-memory database, data is lost on restart
- **PostgreSQL Mode**: Requires external database, data persists
- **Authentication**: Uses hardcoded credentials (not suitable for production)
- **JWT Secret**: Uses default secret (should be overridden via `JWT_SECRET` env var)
- **CORS**: Development-friendly CORS settings
- **Logging**: Debug level enabled for `io.seqera` packages

## 📊 API Documentation & Usage

### Access Points
- **Application**: http://localhost:8080/
- **Login Page**: http://localhost:8080/login
- **Dashboard**: http://localhost:8080/dashboard (requires authentication)
- **API Documentation**: http://localhost:8080/openapi/index.html
- **Pet API**: http://localhost:8080/pets (requires authentication)

### Example Usage
```bash
# Login and save cookies
curl -c cookies.txt -X POST http://localhost:8080/login \
  -d "username=admin&password=password" \
  -H "Content-Type: application/x-www-form-urlencoded"

# Access protected Pet API
curl -b cookies.txt http://localhost:8080/pets

# Create a new pet
curl -b cookies.txt -X POST http://localhost:8080/pets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fluffy",
    "species": "Cat",
    "breed": "Persian",
    "age": 3,
    "color": "White",
    "description": "Friendly and playful"
  }'
```

## 🧪 Development & Testing

### Code Generation & Documentation

#### API Documentation Generation
```bash
# Generate OpenAPI spec from TypeSpec source
./gradlew :api:generateOpenApi

# Generate Swagger UI for interactive documentation
./gradlew :app:generateSwaggerUI

# Access generated documentation
open http://localhost:8080/openapi/index.html
```

#### Code Generation Workflow
```bash
# Complete API code generation pipeline (recommended)
./gradlew :app:generateApiCode

# This task performs the following steps:
# 1. Generates OpenAPI spec from TypeSpec source (api/spec/main.tsp)
# 2. Generates Java interfaces for controllers (app/src/main/java/io/seqera/api/spec/)
# 3. Generates DTOs/model classes (api/src/main/java/io/seqera/api/model/)
# 4. Copies generated code to appropriate modules
```

#### Manual Code Generation Steps
```bash
# Step 1: Generate OpenAPI specification
cd api/spec
npm install
npm run build

# Step 2: Generate server-side interfaces and models
./gradlew :app:generateServerOpenApiApis    # Controller interfaces
./gradlew :app:generateServerOpenApiModels  # DTO classes

# Step 3: Copy generated code to proper locations
./gradlew :app:generateApiCode              # Automated copy task
```

#### Publishing API Client to Maven

##### Local Publishing (for testing)
```bash
# Publish API client to local Maven repository
./gradlew :api:publishToMavenLocal

# The API client will be available as:
# Group ID: io.seqera
# Artifact ID: api
# Version: [from VERSION file]
```

##### Production Publishing
```bash
# Publish to Seqera Maven repository (requires AWS credentials)
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export PUBLISH_REPO_URL=s3://maven.seqera.io/releases  # or /snapshots

# Publish the API client
./gradlew :api:publish

# Alternative: Set credentials via Gradle properties
./gradlew :api:publish \
  -Paws_access_key_id=your_access_key \
  -Paws_secret_access_key=your_secret_key \
  -Ppublish_repo_url=s3://maven.seqera.io/releases
```

##### Client Usage Example
After publishing, other projects can consume the API client:

```groovy
// In consuming project's build.gradle
dependencies {
    implementation 'io.seqera:api:1.0.0'
}
```

```java
// Using the generated models
import io.seqera.api.model.Pet;
import io.seqera.api.model.CreatePetRequest;

CreatePetRequest request = new CreatePetRequest()
    .name("Fluffy")
    .species("Cat")
    .breed("Persian")
    .age(3);
```

#### Generated Code Structure
```
api/src/main/java/io/seqera/api/
└── model/                          # Generated DTOs
    ├── Pet.java                    # Pet entity model
    ├── CreatePetRequest.java       # Create request DTO
    ├── UpdatePetRequest.java       # Update request DTO
    └── ...

app/src/main/java/io/seqera/api/
└── spec/                           # Generated controller interfaces
    ├── PetsApiSpec.java            # Pet API interface
    ├── AuthApiSpec.java            # Auth API interface
    └── ...
```

#### Development Workflow Best Practices

1. **API-First Development**: Always start by updating `api/spec/main.tsp`
2. **Code Generation**: Run `./gradlew :app:generateApiCode` after TypeSpec changes
3. **Implementation**: Implement generated interfaces in controller classes
4. **Testing**: Test both API contracts and implementations
5. **Publishing**: Publish API client for external consumers

```bash
# Typical development cycle
vim api/spec/main.tsp              # 1. Update API specification
./gradlew :app:generateApiCode     # 2. Generate code
vim app/src/main/groovy/...        # 3. Implement controllers
./gradlew test                     # 4. Run tests
```

### Testing
```bash
# Run all tests
./gradlew test

# Run with H2 database
./gradlew :app:test

# Run specific test suites  
./gradlew :app:test --tests "*PetRepositorySpec"
./gradlew :app:test --tests "*AuthenticationSpec"
```

### Build & Deployment
```bash
# Build application
./gradlew build

# Build container image
./gradlew :app:jibDockerBuild

# Run linting and type checking
./gradlew :app:compileGroovy
```

## 🏛️ Component Architecture

### Controller Layer (`controller/`)
- **PetController**: Implements `PetsApiSpec` interface from generated code
- **AuthController**: Handles login/logout, dashboard rendering
- **Security Annotations**: `@Secured` for authentication requirements

### Repository Layer (`repository/`)
- **PetRepository**: Abstract base with common CRUD operations
- **PostgresPetRepository**: PostgreSQL-specific implementation
- **H2PetRepository**: H2-specific implementation with `@Primary` for local development

### Entity Layer (`entity/`)
- **Pet**: Domain entity with Micronaut Data annotations
- **Audit Fields**: `@DateCreated`, `@DateUpdated` for automatic timestamps
- **Validation**: Bean validation annotations for data integrity

### Security Layer (`security/`)
- **BasicAuthenticationProvider**: Implements authentication logic
- **JWT Configuration**: Cookie-based JWT with proper security headers
- **Route Protection**: Security rules in `application.yml`

## 📚 Additional Documentation

For detailed component documentation, see the JavaDoc in each class:

- **PetController**: RESTful pet management endpoints
- **PetRepository**: Data access patterns and database abstraction  
- **BasicAuthenticationProvider**: Authentication flow and JWT handling
- **Pet Entity**: Domain model and data validation rules

## 🔧 Configuration

### Environment-Specific Configuration
- **`application.yml`**: Base configuration
- **`application-h2.yml`**: H2 database settings for local development
- **Security Settings**: JWT configuration, authentication providers
- **Database**: Multiple datasource support with environment switching

### Production Considerations
- **JWT Secret**: Override `JWT_SECRET` environment variable
- **Database**: Configure proper PostgreSQL connection
- **Authentication**: Replace hardcoded credentials with proper user management
- **CORS**: Restrict CORS settings for production domains
- **Logging**: Adjust log levels for production monitoring
