package io.seqera.app.controller

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.seqera.app.exchange.ServiceInfo
import io.seqera.app.exchange.ServiceInfoResponse
import io.seqera.app.utils.BuildInfo
/**
 * A bare minimal oath controller
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@io.micronaut.http.annotation.Controller('/')
class Controller {

    @Get('/service-info')
    HttpResponse<ServiceInfoResponse> info() {
        final info = new ServiceInfo(BuildInfo.getVersion(), BuildInfo.getCommitId())
        HttpResponse.ok(new ServiceInfoResponse(info))
    }
}
