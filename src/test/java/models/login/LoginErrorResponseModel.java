package models.login;

import java.util.List;

public record LoginErrorResponseModel(
        List<String> username,
        List<String> password,
        String detail
) {}