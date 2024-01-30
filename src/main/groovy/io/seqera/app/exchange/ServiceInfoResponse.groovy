package io.seqera.app.exchange

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import io.micronaut.serde.annotation.Serdeable

/**
 * Implement service-info endpoint response
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Serdeable
@Canonical
@CompileStatic
class ServiceInfoResponse {

    ServiceInfo serviceInfo

}
