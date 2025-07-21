package io.seqera.app.security

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationFailureReason
import spock.lang.Specification

class BasicAuthenticationProviderUnitSpec extends Specification {

    BasicAuthenticationProvider authProvider = new BasicAuthenticationProvider()

    def "test successful authentication with admin credentials"() {
        given:
        def request = HttpRequest.POST('/login', [:])
        def authRequest = new AuthenticationRequest<String, String>() {
            @Override
            String getIdentity() { return "admin" }
            
            @Override
            String getSecret() { return "password" }
        }

        when:
        def response = authProvider.authenticate(request, authRequest)

        then:
        response != null
        response.isAuthenticated()
        response.authentication.present
        response.authentication.get().name == "admin"
        response.authentication.get().roles.contains("ROLE_USER")
    }

    def "test successful authentication with user credentials"() {
        given:
        def request = HttpRequest.POST('/login', [:])
        def authRequest = new AuthenticationRequest<String, String>() {
            @Override
            String getIdentity() { return "user" }
            
            @Override
            String getSecret() { return "secret" }
        }

        when:
        def response = authProvider.authenticate(request, authRequest)

        then:
        response != null
        response.isAuthenticated()
        response.authentication.present
        response.authentication.get().name == "user"
        response.authentication.get().roles.contains("ROLE_USER")
    }

    def "test failed authentication with invalid credentials"() {
        given:
        def request = HttpRequest.POST('/login', [:])
        def authRequest = new AuthenticationRequest<String, String>() {
            @Override
            String getIdentity() { return "invalid" }
            
            @Override
            String getSecret() { return "wrong" }
        }

        when:
        def response = authProvider.authenticate(request, authRequest)

        then:
        response != null
        !response.isAuthenticated()
        response.authentication.empty
    }

    def "test failed authentication with empty username"() {
        given:
        def request = HttpRequest.POST('/login', [:])
        def authRequest = new AuthenticationRequest<String, String>() {
            @Override
            String getIdentity() { return "" }
            
            @Override
            String getSecret() { return "password" }
        }

        when:
        def response = authProvider.authenticate(request, authRequest)

        then:
        response != null
        !response.isAuthenticated()
    }

    def "test failed authentication with empty password"() {
        given:
        def request = HttpRequest.POST('/login', [:])
        def authRequest = new AuthenticationRequest<String, String>() {
            @Override
            String getIdentity() { return "admin" }
            
            @Override
            String getSecret() { return "" }
        }

        when:
        def response = authProvider.authenticate(request, authRequest)

        then:
        response != null
        !response.isAuthenticated()
    }

    def "test valid credentials check"() {
        expect:
        authProvider.isValidCredentials("admin", "password") == true
        authProvider.isValidCredentials("user", "secret") == true
        authProvider.isValidCredentials("invalid", "wrong") == false
        authProvider.isValidCredentials("admin", "wrong") == false
        authProvider.isValidCredentials("", "") == false
    }
}