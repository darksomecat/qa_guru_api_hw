package api;

import io.restassured.response.ValidatableResponse;
import models.login.LoginBodyModel;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.withoutContentTypeRequestSpec;

public class LoginApiClient extends BaseApiClient {

    public static ValidatableResponse login(LoginBodyModel loginBody) {
        return given(request())
                .body(loginBody)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    public static String loginAndGetRefreshToken(LoginBodyModel loginBody) {
        return login(loginBody)
                .extract()
                .path("refresh");
    }

    public static ValidatableResponse loginWithEmptyBody() {
        return given(request())
                .body(EMPTY_BODY)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    public static ValidatableResponse loginWithRawBody(String rawBody) {
        return given(request())
                .body(rawBody)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    public static ValidatableResponse loginWithInvalidDataTypes(Map<String, Object> rawBody) {
        return given(request())
                .body(rawBody)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    public static ValidatableResponse loginWithoutContentType(LoginBodyModel loginBody) {
        return given(withoutContentTypeRequestSpec)
                .body(loginBody)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    private LoginApiClient() {
    }
}