package models.user;

import java.util.List;

public record UserErrorResponseModel(
        List<String> username,
        List<String> firstName,
        List<String> lastName,
        List<String> email
) {}