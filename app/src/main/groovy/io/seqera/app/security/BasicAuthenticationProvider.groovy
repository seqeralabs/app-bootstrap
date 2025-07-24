package io.seqera.app.security

import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationFailureReason
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.provider.AuthenticationProvider
import jakarta.inject.Singleton

/**
 * Basic authentication provider for JWT token generation.
 * 
 * This provider implements hardcoded username/password authentication suitable for
 * development and demonstration purposes. In production, this should be replaced with
 * a proper user management system backed by a database or external identity provider.
 * 
 * Supported credentials:
 * - admin/password (ROLE_USER)
 * - user/secret (ROLE_USER)
 * 
 * Upon successful authentication, the provider returns user details that are used
 * to generate JWT tokens stored in HTTP-only cookies.
 * 
 * @author Pet Shop API
 * @since 1.0.0
 */
@Singleton
class BasicAuthenticationProvider implements AuthenticationProvider<HttpRequest<?>, String, String> {

    @Override
    AuthenticationResponse authenticate(@Nullable HttpRequest<?> httpRequest, 
                                        AuthenticationRequest<String, String> authenticationRequest) {
        
        String username = authenticationRequest.identity
        String password = authenticationRequest.secret
        
        if (isValidCredentials(username, password)) {
            return AuthenticationResponse.success(username, ["ROLE_USER"])
        } else {
            return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH)
        }
    }
    
    private boolean isValidCredentials(String username, String password) {
        // Basic hardcoded authentication - in production, use proper user storage
        return (username == "admin" && password == "password") ||
               (username == "user" && password == "secret")
    }
}