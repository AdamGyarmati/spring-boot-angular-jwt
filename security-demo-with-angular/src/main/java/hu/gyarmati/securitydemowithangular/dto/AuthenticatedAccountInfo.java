package hu.gyarmati.securitydemowithangular.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class AuthenticatedAccountInfo {
    private String username;
    private List<String> roles;

    public AuthenticatedAccountInfo(UserDetails userDetails) {
        this.username = userDetails.getUsername();
        this.roles = parseRoles(userDetails);
    }

    public List<String> parseRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
