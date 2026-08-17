package `in`.over.demo.application.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JWTAuthenticationFilter,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                it.requestMatchers(HttpMethod.POST, "/employee/*/payrolls/schedule-create").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.POST, "/employee/*/payrolls/*/schedule-update").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.POST, "/role").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.PUT, "/role/*").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.DELETE, "/role/*").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.GET, "/employee", "/employee/*").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.POST, "/employee").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.DELETE, "/employee/*").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.GET, "/employee/*/payrolls").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.GET, "/role", "/role/*").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.GET, "/records").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.PUT, "/records").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.DELETE, "/records").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.POST, "/**").hasAnyRole("ADMIN", "USER")
                it.requestMatchers(HttpMethod.PUT, "/**").hasAnyRole("ADMIN", "USER")
                it.requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ADMIN", "USER")
                it.anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint { _, response, _ -> response.sendError(401) } }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
