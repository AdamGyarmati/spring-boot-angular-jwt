package hu.gyarmati.securitydemowithangular.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponseDto {
    private String accessToken;
    private String refreshToken;
    private CustomUserDetails user;
}
