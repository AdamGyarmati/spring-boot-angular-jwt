package hu.gyarmati.securitydemowithangular.exceptionhandling;

public class UsernameAlreadyInDatabaseException extends RuntimeException {
    private String username;

    public UsernameAlreadyInDatabaseException(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
