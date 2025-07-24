package io.seqera.app.test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors

/**
 * Helper class for merging multiple SQL schema files into a single temporary file
 * for PostgreSQL TestContainers initialization.
 */
class SchemaFileMerger {

    private static String mergedSchemaFilePath = null
    private static final Object lock = new Object()

    /**
     * Get the path to the merged schema file, creating it if necessary.
     * This method is thread-safe and caches the result for subsequent calls.
     *
     * @return the absolute path to the merged schema file
     * @throws RuntimeException if schema files cannot be found or merged
     */
    static String getMergedSchemaFile() {
        synchronized (lock) {
            if (mergedSchemaFilePath == null) {
                mergedSchemaFilePath = createMergedSchemaFile()
            }
            return mergedSchemaFilePath
        }
    }

    /**
     * Create a temporary file containing all SQL schema files merged together.
     * Files are processed in alphabetical order to ensure consistent execution.
     *
     * @param schemaDirectory the directory containing SQL files (defaults to "src/main/resources/db-schema")
     * @return the absolute path to the created temporary file
     * @throws RuntimeException if the operation fails
     */
    static String createMergedSchemaFile(String schemaDirectory = "src/main/resources/db-schema") {
        try {
            Path schemaDir = Paths.get(schemaDirectory)
            
            if (!Files.exists(schemaDir) || !Files.isDirectory(schemaDir)) {
                throw new IllegalStateException("Schema directory not found: ${schemaDir}")
            }

            // Collect all .sql files and sort them alphabetically
            List<Path> sqlFiles = Files.list(schemaDir)
                .filter { path -> path.toString().endsWith(".sql") }
                .sorted()
                .collect(Collectors.toList())

            if (sqlFiles.isEmpty()) {
                throw new IllegalStateException("No SQL files found in schema directory: ${schemaDir}")
            }

            // Create temporary file for merged schema
            Path tempFile = Files.createTempFile("merged-schema-", ".sql")
            tempFile.toFile().deleteOnExit()

            // Merge all SQL files into the temporary file
            Files.newBufferedWriter(tempFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).withCloseable { writer ->
                sqlFiles.each { sqlFile ->
                    writer.write("-- Content from: ${sqlFile.fileName}\n")
                    Files.lines(sqlFile).forEach { line ->
                        writer.write(line)
                        writer.write("\n")
                    }
                    writer.write("\n")
                }
            }

            return tempFile.toAbsolutePath().toString()
        } catch (Exception e) {
            throw new RuntimeException("Failed to create merged schema file from directory: ${schemaDirectory}", e)
        }
    }

    /**
     * Reset the cached merged schema file path.
     * Useful for testing purposes to force recreation of the merged file.
     */
    static void reset() {
        synchronized (lock) {
            mergedSchemaFilePath = null
        }
    }

    /**
     * Get the list of SQL files in the schema directory without merging them.
     * Useful for testing and validation purposes.
     *
     * @param schemaDirectory the directory to scan
     * @return list of SQL file paths sorted alphabetically
     */
    static List<Path> getSqlFiles(String schemaDirectory = "src/main/resources/db-schema") {
        Path schemaDir = Paths.get(schemaDirectory)
        
        if (!Files.exists(schemaDir) || !Files.isDirectory(schemaDir)) {
            return []
        }

        return Files.list(schemaDir)
            .filter { path -> path.toString().endsWith(".sql") }
            .sorted()
            .collect(Collectors.toList())
    }
}
