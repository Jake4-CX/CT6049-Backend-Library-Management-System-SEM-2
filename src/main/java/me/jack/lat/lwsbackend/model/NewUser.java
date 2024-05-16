package me.jack.lat.lwsbackend.model;

import jakarta.validation.constraints.*;

public class NewUser {

    @NotEmpty(message = "Email cannot be empty")
    @Email
    String userEmail;
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
    String userPassword;
    @NotEmpty(message = "Role cannot be empty")
    String userRole;

    public NewUser() {

    }

    public NewUser(String userEmail, String userPassword, String userRole) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userRole = userRole;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
