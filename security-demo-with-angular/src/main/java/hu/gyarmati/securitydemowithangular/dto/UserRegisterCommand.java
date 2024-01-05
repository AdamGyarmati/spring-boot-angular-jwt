package hu.gyarmati.securitydemowithangular.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRegisterCommand {
    private String username;
    private String password;
}
