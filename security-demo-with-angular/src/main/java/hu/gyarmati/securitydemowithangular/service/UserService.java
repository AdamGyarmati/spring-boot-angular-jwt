package hu.gyarmati.securitydemowithangular.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.gyarmati.securitydemowithangular.config.UserRole;
import hu.gyarmati.securitydemowithangular.domain.CustomUser;
import hu.gyarmati.securitydemowithangular.dto.*;
import hu.gyarmati.securitydemowithangular.exceptionhandling.UsernameAlreadyInDatabaseException;
import hu.gyarmati.securitydemowithangular.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUser customUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("username not found"));

        String[] roles = customUser.getRoles().stream()
                .map(UserRole::toString)
                .toArray(String[]::new);

        return User
                .withUsername(customUser.getUsername())
                .authorities(AuthorityUtils.createAuthorityList(roles))
                .password(customUser.getPassword())
                .build();
    }

    public void register(UserRegisterCommand command) {
        CustomUser customUserTest = null;
        try {
            customUserTest = findByUsername(command.getUsername());
        } catch (UsernameNotFoundException e) {

        }

        if (customUserTest != null) {
            throw new UsernameAlreadyInDatabaseException(command.getUsername());
        }

        CustomUser customUser = new CustomUser();
        customUser.setPassword(passwordEncoder.encode(command.getPassword()));
        customUser.setUsername(command.getUsername());
        customUser.setRoles(List.of(UserRole.ROLE_USER));

        userRepository.save(customUser);
    }

    public CustomUserDetails getMe(String username) {
        return userRepository.findByUsername(username).map(user -> {
                    CustomUserDetails customUserDetails = new CustomUserDetails();
                    customUserDetails.setUsername(user.getUsername());
                    customUserDetails.setRoles(user.getRoles().stream().map(UserRole::getRole).collect(Collectors.toList()));
                    return customUserDetails;
                })
                .get();
    }

    public CustomUser findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Not found"));
    }

    public AuthenticationResponseDto login(AuthenticationRequestDto authenticationRequest) {
        // search user
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        User user = (User) authentication.getPrincipal();

        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        String accessToken = JWT.create()
                // Unique data about user
                .withSubject(user.getUsername())
//                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 1000))
                // Company name or company url
                .withIssuer("http://localhost:8080/api/users/login")
                // Roles
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
        String refreshToken = JWT.create()
                // Unique data about user
                .withSubject(user.getUsername())
                // Hosszabb idő kell, bármennyi lehet
                .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                // Company name or company url
                .withIssuer("http://localhost:8080/api/users/login")
                .sign(algorithm);

        return new AuthenticationResponseDto(accessToken, refreshToken, new CustomUserDetails(user));
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {


        String refreshToken = refreshTokenRequest.getRefreshToken();
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(refreshToken);
        String username = decodedJWT.getSubject();

        CustomUser user = findByUsername(username);
        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 1000))
                .withIssuer("http://localhost:8080/api/users/token/refresh")
                .withClaim("roles", user.getRoles().stream().map(Enum::toString).collect(Collectors.toList()))
                .sign(algorithm);

        return new RefreshTokenResponse(accessToken);
    }
}

