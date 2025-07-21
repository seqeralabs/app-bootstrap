package io.seqera.app

import java.nio.file.Files
import java.nio.file.Path

import groovy.util.logging.Slf4j
import io.micronaut.runtime.Micronaut
import groovy.transform.CompileStatic
import io.seqera.app.utils.BuildInfo
import io.seqera.app.utils.RuntimeInfo

/**
 * Application launcher class
 */
@Slf4j
@CompileStatic
class Application {

    static void main(String[] args) {
        log.info( "Starting ${BuildInfo.name} - version: ${BuildInfo.fullVersion} - ${RuntimeInfo.info('; ')} - CPUs ${Runtime.runtime.availableProcessors()}" )
        setupConfig()
        Micronaut.build(args)
                .banner(false)
                .mainClass(Application.class)
                .start();
    }

    static void setupConfig() {
        // config file
        def configFile = Path.of('config.yml').toAbsolutePath()
        if( System.getenv('APP_CONFIG_FILE') ) {
            configFile = Path.of(System.getenv('APP_CONFIG_FILE')).toAbsolutePath()
            log.info "Detected APP_CONFIG_FILE variable: ${configFile}"
        }
        else {
            log.info "Default config file: ${configFile}"
        }
        if( Files.exists(configFile) )
            System.setProperty('micronaut.config.files', "classpath:application.yml,file:$configFile")
        else
            log.info("Config file does not exist or cannot be accessed: $configFile")
    }
}
