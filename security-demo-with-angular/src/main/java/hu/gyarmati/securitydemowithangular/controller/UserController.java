package hu.gyarmati.securitydemowithangular.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.gyarmati.securitydemowithangular.config.UserRole;
import hu.gyarmati.securitydemowithangular.domain.CustomUser;
import hu.gyarmati.securitydemowithangular.dto.*;
import hu.gyarmati.securitydemowithangular.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegisterCommand command) {
        userService.register(command);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/getMe")
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<CustomUserDetails> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails loggedInUser = (UserDetails) authentication.getPrincipal();
        return new ResponseEntity<>(userService.getMe(loggedInUser.getUsername()), HttpStatus.OK);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest req) {
        req.getSession().invalidate();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@RequestBody AuthenticationRequestDto authenticationRequest) {
        AuthenticationResponseDto responseDto = userService.login(authenticationRequest);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) throws IOException {
        RefreshTokenResponse tokenResponse = userService.refreshToken(refreshTokenRequest);
        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello", HttpStatus.OK);
    }
}
