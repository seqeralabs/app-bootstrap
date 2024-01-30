package io.seqera.app.exchange

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import io.micronaut.serde.annotation.Serdeable

/**
 * Model basic service info
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Serdeable
@Canonical
@CompileStatic
class ServiceInfo {

    /** Application version string */
    String version;

    /** Build commit ID */
    String commitId;

}
