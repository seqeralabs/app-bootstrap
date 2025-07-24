package io.seqera.app.test

import java.nio.file.Files
import java.nio.file.Path

import spock.lang.Specification
import spock.lang.TempDir

class SchemaFileMergerTest extends Specification {

    @TempDir
    Path tempDir

    def cleanup() {
        // Reset the cached state after each test
        SchemaFileMerger.reset()
    }

    def "should get list of SQL files from existing schema directory"() {
        given: "a directory with SQL files"
        def schemaDir = tempDir.resolve("db-schema")
        Files.createDirectories(schemaDir)
        
        def file1 = schemaDir.resolve("m01__initial.sql")
        def file2 = schemaDir.resolve("m02__update.sql")
        def file3 = schemaDir.resolve("m03__final.sql")
        def nonSqlFile = schemaDir.resolve("readme.txt")
        
        Files.write(file1, ["-- Initial schema"].asList())
        Files.write(file2, ["-- Update schema"].asList())
        Files.write(file3, ["-- Final schema"].asList())
        Files.write(nonSqlFile, ["Not a SQL file"].asList())

        when: "getting SQL files"
        def sqlFiles = SchemaFileMerger.getSqlFiles(schemaDir.toString())

        then: "should return only SQL files in sorted order"
        sqlFiles.size() == 3
        sqlFiles[0].fileName.toString() == "m01__initial.sql"
        sqlFiles[1].fileName.toString() == "m02__update.sql"
        sqlFiles[2].fileName.toString() == "m03__final.sql"
    }

    def "should handle empty directory"() {
        given: "an empty directory"
        def schemaDir = tempDir.resolve("empty-schema")
        Files.createDirectories(schemaDir)

        when: "getting SQL files"
        def sqlFiles = SchemaFileMerger.getSqlFiles(schemaDir.toString())

        then: "should return empty list"
        sqlFiles.isEmpty()
    }

    def "should handle non-existent directory"() {
        given: "a non-existent directory path"
        def nonExistentDir = tempDir.resolve("non-existent").toString()

        when: "getting SQL files"
        def sqlFiles = SchemaFileMerger.getSqlFiles(nonExistentDir)

        then: "should return empty list"
        sqlFiles.isEmpty()
    }

    def "should create merged schema file successfully"() {
        given: "a directory with multiple SQL files"
        def schemaDir = tempDir.resolve("db-schema")
        Files.createDirectories(schemaDir)
        
        def file1 = schemaDir.resolve("m01__tables.sql")
        def file2 = schemaDir.resolve("m02__indexes.sql")
        
        Files.write(file1, [
            "CREATE TABLE users (id SERIAL PRIMARY KEY);",
            "INSERT INTO users VALUES (1);"
        ])
        Files.write(file2, [
            "CREATE INDEX idx_users_id ON users(id);"
        ])

        when: "creating merged schema file"
        def mergedFilePath = SchemaFileMerger.createMergedSchemaFile(schemaDir.toString())

        then: "should create a valid temporary file"
        mergedFilePath != null
        def mergedFile = new File(mergedFilePath)
        mergedFile.exists()
        mergedFile.name.startsWith("merged-schema-")
        mergedFile.name.endsWith(".sql")

        and: "file content should contain all SQL files in correct order"
        def content = mergedFile.text
        content.contains("-- Content from: m01__tables.sql")
        content.contains("-- Content from: m02__indexes.sql")
        content.contains("CREATE TABLE users (id SERIAL PRIMARY KEY);")
        content.contains("INSERT INTO users VALUES (1);")
        content.contains("CREATE INDEX idx_users_id ON users(id);")
        
        // Ensure correct order
        def tablesIndex = content.indexOf("CREATE TABLE users")
        def indexIndex = content.indexOf("CREATE INDEX idx_users_id")
        tablesIndex < indexIndex
    }

    def "should throw exception for non-existent schema directory"() {
        given: "a non-existent directory path"
        def nonExistentDir = tempDir.resolve("non-existent").toString()

        when: "creating merged schema file"
        SchemaFileMerger.createMergedSchemaFile(nonExistentDir)

        then: "should throw RuntimeException"
        def ex = thrown(RuntimeException)
        ex.message.contains("Failed to create merged schema file")
        ex.cause instanceof IllegalStateException
        ex.cause.message.contains("Schema directory not found")
    }

    def "should throw exception for empty schema directory"() {
        given: "an empty directory"
        def schemaDir = tempDir.resolve("empty-schema")
        Files.createDirectories(schemaDir)

        when: "creating merged schema file"
        SchemaFileMerger.createMergedSchemaFile(schemaDir.toString())

        then: "should throw RuntimeException"
        def ex = thrown(RuntimeException)
        ex.message.contains("Failed to create merged schema file")
        ex.cause instanceof IllegalStateException
        ex.cause.message.contains("No SQL files found")
    }

    def "should cache merged schema file path"() {
        given: "a directory with SQL files"
        def schemaDir = tempDir.resolve("db-schema")
        Files.createDirectories(schemaDir)
        
        def file1 = schemaDir.resolve("schema.sql")
        Files.write(file1, ["CREATE TABLE test (id INT);"])

        when: "calling getMergedSchemaFile multiple times"
        def path1 = SchemaFileMerger.getMergedSchemaFile()
        def path2 = SchemaFileMerger.getMergedSchemaFile()

        then: "should return the same path (cached)"
        path1 == path2
        new File(path1).exists()
    }

    def "should reset cached schema file path"() {
        given: "a directory with SQL files"
        def schemaDir = tempDir.resolve("db-schema")
        Files.createDirectories(schemaDir)
        
        def file1 = schemaDir.resolve("schema.sql")
        Files.write(file1, ["CREATE TABLE test (id INT);"])

        when: "getting merged schema file, resetting, then getting again"
        def path1 = SchemaFileMerger.getMergedSchemaFile()
        SchemaFileMerger.reset()
        def path2 = SchemaFileMerger.getMergedSchemaFile()

        then: "should create a new file after reset"
        path1 != path2
        new File(path1).exists()
        new File(path2).exists()
    }

    def "should handle files with complex SQL content"() {
        given: "SQL files with various SQL constructs"
        def schemaDir = tempDir.resolve("db-schema")
        Files.createDirectories(schemaDir)
        
        def file1 = schemaDir.resolve("m01__complex.sql")
        Files.write(file1, [
            "-- This is a comment",
            "CREATE TABLE users (",
            "    id SERIAL PRIMARY KEY,",
            "    name VARCHAR(255) NOT NULL,",
            "    email VARCHAR(255) UNIQUE",
            ");",
            "",
            "INSERT INTO users (name, email) VALUES",
            "    ('John Doe', 'john@example.com'),",
            "    ('Jane Smith', 'jane@example.com');"
        ])

        when: "creating merged schema file"
        def mergedFilePath = SchemaFileMerger.createMergedSchemaFile(schemaDir.toString())

        then: "should preserve all content including comments and formatting"
        def content = new File(mergedFilePath).text
        content.contains("-- This is a comment")
        content.contains("CREATE TABLE users (")
        content.contains("id SERIAL PRIMARY KEY,")
        content.contains("INSERT INTO users (name, email) VALUES")
        content.contains("('John Doe', 'john@example.com')")
    }
}
