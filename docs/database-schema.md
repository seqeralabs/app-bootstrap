# Database Schema Documentation

## Overview

This document describes the database schema for the App Bootstrap application, demonstrating best practices for database design, migrations, and maintenance using MigTool.

## Migration Strategy

The application uses MigTool for database migrations with the following conventions:

- **Migration files**: `m{number}__{description}.sql`
- **File pattern**: `^m(\\d+)__(.+)` (regex pattern)
- **Location**: `src/main/resources/db-schema/`

### Migration Files

1. **m01__create-users-table.sql**: Initial users table with basic fields
2. **m02__add-user-profile-table.sql**: Extended user profile information
3. **m03__add-audit-log-table.sql**: Audit trail for all table changes
4. **m04__add-indexes-and-constraints.sql**: Performance and data integrity improvements

## Tables

### users
Primary user information table.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique user identifier |
| name | VARCHAR(100) | NOT NULL | User's full name |
| email | VARCHAR(255) | NOT NULL, UNIQUE | User's email address |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Record creation time |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Record last update time |

**Indexes:**
- `idx_users_email`: Unique index on email
- `idx_users_name`: Index on name for search
- `idx_users_email_active`: Partial index for active users
- `idx_users_name_email`: Compound index for common queries

**Constraints:**
- `chk_users_email_format`: Email format validation
- `chk_users_name_length`: Minimum name length validation

### user_profiles
Extended user profile information (1:1 relationship with users).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique profile identifier |
| user_id | BIGINT | NOT NULL, REFERENCES users(id) ON DELETE CASCADE | Foreign key to users table |
| bio | TEXT | NULL | User biography |
| avatar_url | VARCHAR(512) | NULL | URL to user's avatar image |
| phone | VARCHAR(20) | NULL | User's phone number |
| address | JSONB | NULL | User's address information |
| preferences | JSONB | DEFAULT '{}' | User preferences |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Record creation time |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | Record last update time |

**Indexes:**
- `idx_user_profiles_user_id`: Index on user_id foreign key
- `idx_user_profiles_address_gin`: GIN index on address JSONB
- `idx_user_profiles_preferences_gin`: GIN index on preferences JSONB

**Constraints:**
- `uk_user_profiles_user_id`: Unique constraint ensuring one profile per user

### audit_log
Audit trail for all table changes.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique audit log identifier |
| table_name | VARCHAR(50) | NOT NULL | Name of the affected table |
| record_id | BIGINT | NOT NULL | ID of the affected record |
| operation | VARCHAR(10) | NOT NULL, CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')) | Type of operation |
| old_values | JSONB | NULL | Previous values (for UPDATE/DELETE) |
| new_values | JSONB | NULL | New values (for INSERT/UPDATE) |
| changed_by | VARCHAR(255) | NULL | User who made the change |
| changed_at | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT NOW() | When the change occurred |

**Indexes:**
- `idx_audit_log_table_record`: Compound index on table_name and record_id
- `idx_audit_log_changed_at`: Index on change timestamp
- `idx_audit_log_operation`: Index on operation type

## Views

### user_statistics
Aggregated statistics about user creation patterns.

```sql
SELECT 
    DATE_TRUNC('month', created_at) as month,
    COUNT(*) as user_count,
    COUNT(*) FILTER (WHERE created_at > NOW() - INTERVAL '30 days') as recent_users
FROM users
GROUP BY DATE_TRUNC('month', created_at)
ORDER BY month;
```

## Functions and Triggers

### audit_trigger_function()
Automatically logs all changes to audited tables in the `audit_log` table.

### update_updated_at_column()
Automatically updates the `updated_at` timestamp on record modifications.

**Triggers:**
- `users_audit_trigger`: Audit trigger for users table
- `user_profiles_audit_trigger`: Audit trigger for user_profiles table
- `update_users_updated_at`: Updated timestamp trigger for users
- `update_user_profiles_updated_at`: Updated timestamp trigger for user_profiles

## Best Practices Demonstrated

1. **Proper Data Types**: Using appropriate data types (BIGSERIAL, VARCHAR with limits, JSONB)
2. **Constraints**: Check constraints, foreign keys, unique constraints
3. **Indexing Strategy**: 
   - Primary keys (automatic)
   - Foreign keys
   - Unique constraints
   - Search-optimized indexes
   - Partial indexes for specific use cases
   - GIN indexes for JSONB columns
4. **Audit Trail**: Complete change tracking with triggers
5. **Timestamps**: Automatic creation and update timestamps
6. **JSONB Usage**: Flexible schema for preferences and address data
7. **Cascade Deletes**: Proper cleanup of related data
8. **Performance Optimization**: Strategic indexing and compound indexes
9. **Data Validation**: Database-level constraints
10. **Security**: Proper permissions and access control

## Migration Commands

MigTool migrations are executed automatically when the application starts. The `DbMigrationService` handles all migration operations.

```bash
# Run application (triggers migrations automatically)
./gradlew bootRun

# Run with local profile (H2 database, auto-create schema)
./gradlew bootRun --args='--micronaut.environments=local'

# Run tests (uses TestContainers with PostgreSQL and migrations)
./gradlew test
```

## Environment-Specific Configurations

### Local Development
- **Database**: H2 in-memory (`application-local.yml`)
- **Schema generation**: `CREATE_DROP` (auto-create/drop)
- **Migration**: Not used (schema auto-generated)
- **Usage**: `./gradlew bootRun --args='--micronaut.environments=local'`

### Production
- **Database**: PostgreSQL (`application.yml`)
- **Schema generation**: `NONE` (migrations only)
- **Migration**: MigTool with `DbMigrationService`
- **Migration files**: `src/main/resources/db-schema/m*.sql`

### Testing
- **Database**: PostgreSQL via TestContainers
- **Schema generation**: Migration-based via init scripts
- **Migration**: Uses `.withInitScript("db-schema/m01__create-users-table.sql")`
- **Isolation**: Each test gets fresh database state