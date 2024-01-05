package hu.gyarmati.securitydemowithangular.config;

public enum UserRole {
    ROLE_GUEST("GUEST"),
    ROLE_USER("USER"),
    ROLE_ADMIN("ADMIN");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
