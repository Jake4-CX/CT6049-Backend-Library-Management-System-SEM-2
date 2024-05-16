package me.jack.lat.lwsbackend.service.oracleDB;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import me.jack.lat.lwsbackend.model.NewUser;
import me.jack.lat.lwsbackend.util.EnvVariableUtil;
import me.jack.lat.lwsbackend.util.JwtUtil;
import me.jack.lat.lwsbackend.util.OracleDBUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class UserService {

    private static final int REFRESH_TOKEN_EXPIRATION_TIME = Integer.parseInt(EnvVariableUtil.getVariable("REFRESH_TOKEN_EXPIRATION_TIME"));
    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    public UserService() {
    }

    public Error createUser(NewUser newUser) {

        if (userExists(newUser)) {
            return new Error("User with the same email already exists.");
        }

        // No pre-existing user with the same email, create new user

        String userPassword = BCrypt.withDefaults().hashToString(12, newUser.getUserPassword().toCharArray());

        if (!(List.of("FINANCE_DIRECTOR", "CHIEF_LIBRARIAN", "LIBRARIAN", "USER").contains(newUser.getUserRole().toUpperCase()))) {
            return new Error("Invalid user role.");
        }

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO DWUSERS (userEmail, userPassword, userRole) VALUES (?, ?, ?)");
            preparedStatement.setString(1, newUser.getUserEmail());
            preparedStatement.setString(2, userPassword);
            preparedStatement.setString(3, newUser.getUserRole());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 1) {
                return null;
            } else {
                return new Error("Failed creating new user");
            }

        } catch (SQLException e) {
            return new Error("Failed creating new user");
        }

    }

    public boolean userExists(NewUser newUser) {
        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT userEmail FROM DWUSERS WHERE userEmail = ?");
            preparedStatement.setString(1, newUser.getUserEmail());

            return preparedStatement.executeQuery().next();

        } catch (SQLException e) {
            logger.warning("Failed saving user: " + e.getMessage());
            return false;
        }
    }

    public HashMap<String, Object> loginValidation(String userEmail, String userPassword) {

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM DWUSERS WHERE userEmail = ?");
            preparedStatement.setString(1, userEmail);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("User found, email: " + resultSet.getString("userEmail") + " password: " + resultSet.getString("userPassword"));
                String databaseUserPassword = resultSet.getString("userPassword");

                // Compare hashed password with provided password
                if (BCrypt.verifyer().verify(userPassword.toCharArray(), databaseUserPassword).verified) {

                    Integer dwUserId = resultSet.getInt("id");
                    String userRole = resultSet.getString("userRole");

                    // Generate JWT token (access & refresh)
                    String accessToken = JwtUtil.generateAccessToken(String.valueOf(dwUserId), userRole);
                    String refreshToken = JwtUtil.generateRefreshToken(String.valueOf(dwUserId));

                    assignUserRefreshToken(dwUserId, refreshToken);

                    HashMap<String, Object> userEntity = new HashMap<>(){{
                        put("userId", dwUserId);
                        put("userEmail", resultSet.getString("userEmail"));
                        put("userRole", userRole);
                    }};

                    HashMap<String, String> userToken = new HashMap<>(){{
                        put("accessToken", accessToken);
                        put("refreshToken", refreshToken);
                    }};

                    return new HashMap<>(){{
                        put("user", userEntity);
                        put("token", userToken);
                    }};
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed Login Validation: " + e.getMessage());
            return null;

        }

        return null;
    }

    public void assignUserRefreshToken(Integer dwUserId, String refreshToken) {
        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO DWREFRESHTOKENS (refreshToken, dwUserId, expirationDate) VALUES (?, ?, ?)");
            preparedStatement.setString(1, refreshToken);
            preparedStatement.setInt(2, dwUserId);
            preparedStatement.setDate(3, new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME));

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            logger.warning("Failed Assigning User's Refresh Token: " + e.getMessage());
        }
    }

    public void unassignUserRefreshToken(Integer dwUserId, String refreshToken) {
        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM DWREFRESHTOKENS WHERE refreshToken = ? AND dwUserId = ?");
            preparedStatement.setString(1, refreshToken);
            preparedStatement.setInt(2, dwUserId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            logger.warning("Failed Deleting User's Refresh Token: " + e.getMessage());
        }
    }

    public HashMap<String, Object> findUserById(Integer dwUserId) {
        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM DWUSERS WHERE id = ?");
            preparedStatement.setInt(1, dwUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                return new HashMap<>(){{
                    put("userId", dwUserId);
                    put("userEmail", resultSet.getString("userEmail"));
                    put("userRole", resultSet.getString("userRole"));
                }};
            }
        } catch (SQLException e) {
            return null;

        }

        return null;
    }

    public HashMap<String, Object> refreshUser(String refreshToken) {

        try {
            Claims claims = JwtUtil.decodeRefreshToken(refreshToken);
            String dwUserId = claims.getSubject();

            HashMap<String, Object> user = findUserById(Integer.valueOf(dwUserId));

            if (user == null) {
                // User not found
                return null;
            }

            if ((user.get("userId") != Integer.valueOf(dwUserId))) {
                // User ID in token does not match user ID in database
                return null;
            }

            // Generate JWT token (access & refresh)
            String newAccessToken = JwtUtil.generateAccessToken(dwUserId, user.get("userRole").toString());
            String newRefreshToken = JwtUtil.generateRefreshToken(dwUserId);

            assignUserRefreshToken(Integer.valueOf(dwUserId), newRefreshToken);
            unassignUserRefreshToken(Integer.valueOf(dwUserId), refreshToken);

            return new HashMap<>(){{
                put("accessToken", newAccessToken);
                put("refreshToken", newRefreshToken);
            }};

        } catch (Exception e) {
            // Invalid token
            return null;
        }
    }

    public HashMap<String, Object> validateAccessToken(String accessToken) {

        try {
            Claims claims = JwtUtil.decodeAccessToken(accessToken);
            String dwUserId = claims.getSubject();

            return findUserById(Integer.valueOf(dwUserId));

        } catch (Exception e) {
            return null;
        }
    }
}
