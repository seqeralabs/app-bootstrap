package io.seqera.app.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.seqera.app.Application
import io.seqera.app.exchange.ServiceInfo
import io.seqera.app.exchange.ServiceInfoResponse
import io.seqera.app.utils.BuildInfo
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(application = Application)
class ControllerTest extends Specification {

    @Inject
    @Client('/')
    HttpClient httpClient

    void "should call service info"() {
        given: 'A secured URL is accessed with Basic Auth'
        def request = HttpRequest .GET("/service-info")
        when:
        def resp  = httpClient.toBlocking().exchange(request, ServiceInfoResponse)
        then: 'the endpoint can be accessed'
        resp.status == HttpStatus.OK
        resp.body().serviceInfo == new ServiceInfo(BuildInfo.version, BuildInfo.commitId)
    }

}
