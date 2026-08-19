package fyi._4rsxyzt.demo.application.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

@Component
class JWTService(
    @Value($$"${jwt.secret}") secret: String,
    @Value($$"${jwt.expiration-minutes}") private val expirationMinutes: Long,
) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generate(employeeId: Long, username: String, roles: List<String>): String = JWT.create()
        .withSubject(username)
        .withClaim("employeeId", employeeId)
        .withClaim("roles", roles)
        .withIssuedAt(Date.from(Instant.now()))
        .withExpiresAt(Date.from(Instant.now().plusSeconds(expirationMinutes * 60)))
        .sign(algorithm)

    fun verify(token: String): DecodedJWT? = try {
        JWT.require(algorithm).build().verify(token)
    } catch (_: JWTVerificationException) {
        null
    }
}
