package io.seqera.app.security

import io.seqera.app.security.BasicAuthenticationProvider
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationRequest
import spock.lang.Specification

class JwtAuthenticationSpec extends Specification {

    def "test JWT-based authentication works"() {
        given:
        def authProvider = new BasicAuthenticationProvider()
        def authRequest = new AuthenticationRequest<String, String>() {
            @Override
            String getIdentity() { return "admin" }
            
            @Override
            String getSecret() { return "password" }
        }

        when:
        def response = authProvider.authenticate(HttpRequest.POST('/login', [:]), authRequest)

        then:
        response != null
        response.isAuthenticated()
        response.authentication.present
        response.authentication.get().name == "admin"
        response.authentication.get().roles.contains("ROLE_USER")
    }

    def "test authentication failure still works"() {
        given:
        def authProvider = new BasicAuthenticationProvider()
        def authRequest = new AuthenticationRequest<String, String>() {
            @Override
            String getIdentity() { return "wrong" }
            
            @Override
            String getSecret() { return "wrong" }
        }

        when:
        def response = authProvider.authenticate(HttpRequest.POST('/login', [:]), authRequest)

        then:
        response != null
        !response.isAuthenticated()
        response.authentication.empty
    }
}