package hu.gyarmati.securitydemowithangular.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthenticationRequestDto {
    private String username;
    private String password;
}
