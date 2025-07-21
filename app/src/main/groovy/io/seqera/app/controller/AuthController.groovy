package io.seqera.app.controller

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.micronaut.views.View
import java.security.Principal

/**
 * Authentication and web page controller for the Pet Shop application.
 * 
 * This controller handles the web-based authentication flow and serves HTML pages
 * using Thymeleaf templates. It provides:
 * 
 * - Landing page for anonymous users
 * - Login page with error handling
 * - Dashboard for authenticated users  
 * - Integration with Micronaut Security for JWT cookie authentication
 * 
 * The controller works in conjunction with BasicAuthenticationProvider and
 * Micronaut's built-in logout functionality to provide a complete web-based
 * authentication experience.
 * 
 * @author Pet Shop API
 * @since 1.0.0
 */
@Slf4j
@Controller
class AuthController {

    /**
     * Home page - accessible to everyone
     */
    @Get("/")
    @Secured(SecurityRule.IS_ANONYMOUS)
    @View("index")
    @Produces(MediaType.TEXT_HTML)
    HttpResponse<Map<String, Object>> index() {
        log.debug("Serving home page")
        return HttpResponse.ok([:])
    }

    /**
     * Login page - accessible to anonymous users
     */
    @Get("/login")
    @Secured(SecurityRule.IS_ANONYMOUS)
    @View("login")
    @Produces(MediaType.TEXT_HTML)
    HttpResponse<Map<String, Object>> login(HttpRequest<?> request) {
        log.debug("Serving login page")
        
        // Check for error parameter
        def error = request.getParameters().get("error")
        def model = [:]
        if (error != null) {
            model.error = "Invalid username or password"
        }
        
        return HttpResponse.ok(model)
    }

    /**
     * Dashboard page - accessible to authenticated users only
     */
    @Get("/dashboard")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @View("dashboard")
    @Produces(MediaType.TEXT_HTML)
    HttpResponse<Map<String, Object>> dashboard(Principal principal) {
        log.debug("Serving dashboard for user: {}", principal.name)
        
        def model = [
            username: principal.name
        ]
        
        return HttpResponse.ok(model)
    }

}