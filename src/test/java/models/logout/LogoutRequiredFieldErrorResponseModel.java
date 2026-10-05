package models.logout;

import java.util.List;

public record LogoutRequiredFieldErrorResponseModel(
        List<String> refresh
) {}
