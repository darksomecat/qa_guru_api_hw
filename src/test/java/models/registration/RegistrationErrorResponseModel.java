package models.registration;

import java.util.List;

public record RegistrationErrorResponseModel (
        List<String> username,
        List<String> password
) {}
