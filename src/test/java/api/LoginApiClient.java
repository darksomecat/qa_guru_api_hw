package api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import models.login.LoginBodyModel;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.withoutContentTypeRequestSpec;

public class LoginApiClient extends BaseApiClient {

    @Step("POST /auth/token/ — авторизация пользователя")
    public static ValidatableResponse login(LoginBodyModel loginBody) {
        return given(request())
                .body(loginBody)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("POST /auth/token/ — авторизация пользователя и получение refresh-токена")
    public static String loginAndGetRefreshToken(LoginBodyModel loginBody) {
        return login(loginBody)
                .extract()
                .path("refresh");
    }

    @Step("POST /auth/token/ — авторизация с пустым телом запроса")
    public static ValidatableResponse loginWithEmptyBody() {
        return given(request())
                .body(EMPTY_BODY)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("POST /auth/token/ — авторизация с произвольным телом запроса")
    public static ValidatableResponse loginWithRawBody(String rawBody) {
        return given(request())
                .body(rawBody)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("POST /auth/token/ — авторизация с некорректными типами данных")
    public static ValidatableResponse loginWithInvalidDataTypes(Map<String, Object> rawBody) {
        return given(request())
                .body(rawBody)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("POST /auth/token/ — авторизация без Content-Type")
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
