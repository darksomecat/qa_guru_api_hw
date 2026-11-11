package helpers;

import models.login.LoginBodyModel;
import models.login.LoginResponseModel;
import models.registration.RegistrationBodyModel;
import models.user.UserResponseModel;

import static api.LoginApiClient.login;
import static api.RegistrationApiClient.register;
import static api.UserApiClient.getMe;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.refresh;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static specs.UserSpec.successfulUpdateUserResponseSpec;
import static utils.GeneratedUtils.getUserIdFromToken;

public final class AuthHelper {

    public static final String AUTH_STORAGE_KEY = "book_club_auth";

    private AuthHelper() {
    }

    public static void ensureUserExists(String username, String password) {
        register(new RegistrationBodyModel(username, password))
                .statusCode(anyOf(is(201), is(400)));
    }

    public static AuthSession loginAs(String username, String password) {
        ensureUserExists(username, password);

        LoginResponseModel tokens = login(new LoginBodyModel(username, password))
                .spec(successfulLoginResponseSpec)
                .extract().as(LoginResponseModel.class);

        Integer userId = Integer.valueOf(getUserIdFromToken(tokens.access()));
        return new AuthSession(tokens.access(), tokens.refresh(), userId, username);
    }

    public static void injectUiAuth(AuthSession session) {
        UserResponseModel user = getMe(session.accessToken())
                .spec(successfulUpdateUserResponseSpec)
                .extract().as(UserResponseModel.class);

        String authJson = """
                {
                  "user": {
                    "id": %d,
                    "username": "%s",
                    "firstName": "%s",
                    "lastName": "%s",
                    "email": "%s",
                    "remoteAddr": "%s"
                  },
                  "accessToken": "%s",
                  "refreshToken": "%s",
                  "isAuthenticated": true
                }
                """.formatted(
                user.id(),
                escapeJson(user.username()),
                escapeJson(nullToEmpty(user.firstName())),
                escapeJson(nullToEmpty(user.lastName())),
                escapeJson(nullToEmpty(user.email())),
                escapeJson(nullToEmpty(user.remoteAddr())),
                escapeJson(session.accessToken()),
                escapeJson(session.refreshToken())
        );

        open("/");
        executeJavaScript("localStorage.clear();");
        executeJavaScript("localStorage.setItem(arguments[0], arguments[1]);", AUTH_STORAGE_KEY, authJson);
        refresh();
        $("[data-testid='profile-link']").shouldBe(visible);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record AuthSession(String accessToken, String refreshToken, Integer userId, String username) {
    }
}
