package hu.gyarmati.securitydemowithangular.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // ELőször azt nézzük meg hogy a login path-re jött-e a kérés, ha igen akkor nem csinálunk semmit
        if (request.getServletPath().equals("/api/users/login")
                || request.getServletPath().equals("/api/users/token/refresh")
                || request.getServletPath().equals("/api/users/register")) {
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    String token = authorizationHeader.substring("Bearer ".length());
                    // Ugyan az a secret kell amivel aláírtad a token-t.
                    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String username = decodedJWT.getSubject();
                    System.out.println(decodedJWT.getClaim("roles"));
                    String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
                    UserDetails user = User
                            .withUsername(username)
                            .authorities(authorities)
                            .password("asd")
                            .build();
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    // Ezzel adjuk át a spring securitynek a user-t
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);
                } catch (Exception ex) {
                    log.error("Error logging in: {}", ex.getMessage());
//                    response.setHeader("error", ex.getMessage());
////                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
//                    Map<String, String> error = new HashMap<>();
//                    error.put("error_message", ex.getMessage());
//                    response.setContentType(APPLICATION_JSON_VALUE);
//                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                    response.setStatus(403);
//                    new ObjectMapper().writeValue(response.getOutputStream(), error);

                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }
}
