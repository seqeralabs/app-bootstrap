package io.seqera.app.controller

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.annotation.Controller
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.seqera.api.model.ServiceInfo
import io.seqera.api.model.ServiceInfoResponse
import io.seqera.api.spec.ServiceApiSpec
import io.seqera.app.utils.BuildInfo
/**
 * Service information controller implementation
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@Controller('/')
@Secured(SecurityRule.IS_ANONYMOUS)
class InfoController implements ServiceApiSpec {

    @Override
    ServiceInfoResponse getServiceInfo() {
        final info = new ServiceInfo(BuildInfo.getVersion(), BuildInfo.getCommitId())
        return new ServiceInfoResponse(info)
    }
}
