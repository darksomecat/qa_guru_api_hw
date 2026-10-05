package models.user;

public record UserResponseModel(
        Integer id,
        String username,
        String firstName,
        String lastName,
        String email,
        String remoteAddr
) {}