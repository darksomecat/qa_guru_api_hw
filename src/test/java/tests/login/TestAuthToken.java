package tests.login;

import models.login.LoginBodyModel;
import models.login.LoginErrorResponseModel;
import models.login.LoginResponseModel;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;
import tests.testData.TestData;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.withoutContentTypeRequestSpec;
import static specs.BaseSpec.baseRequestSpec;
import static specs.LoginSpec.badRequestLoginResponseSpec;
import static specs.LoginSpec.jsonParseErrorLoginResponseSpec;
import static specs.LoginSpec.loginWithoutContentTypeResponseSpec;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static specs.LoginSpec.unauthorizedLoginResponseSpec;
import static specs.RegistrationSpec.successfulRegistrationResponseSpec;

public class TestAuthToken extends TestBase {

    @Test
    @DisplayName("Успешная авторизация")
    public void successfulLoginTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        step("Регистрация нового пользователя", () -> {
            given(baseRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });

        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

        LoginResponseModel loginResponse = step("Авторизация и получение токенов", () -> {
            return given(baseRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(successfulLoginResponseSpec)
                    .extract().as(LoginResponseModel.class);
        });

        step("Проверка формата access и refresh токенов", () -> {
            String expectedTokenPart = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

            assertThat(loginResponse.access()).startsWith(expectedTokenPart);
            assertThat(loginResponse.refresh()).startsWith(expectedTokenPart);
            assertThat(loginResponse.access()).isNotEqualTo(expectedTokenPart);
        });
    }

    @Test
    @DisplayName("Ошибка авторизации при передаче пустого username")
    public void loginWithBlankUsernameTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        step("Регистрация нового пользователя", () -> {
            given(baseRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });

        LoginBodyModel loginData = new LoginBodyModel("", testData.password);

        LoginErrorResponseModel errorResponse = step("Отправка запроса авторизации с пустым username", () -> {
            return given(baseRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(badRequestLoginResponseSpec)
                    .extract().as(LoginErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о пустом username", () ->
                assertThat(errorResponse.username()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Проверка чувствительности к регистру в username")
    public void loginCaseSensitivityTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        step("Регистрация нового пользователя", () -> {
            given(baseRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });

        LoginBodyModel loginData = new LoginBodyModel(testData.username.toUpperCase(), testData.password);

        LoginErrorResponseModel errorResponse = step("Отправка запроса авторизации с username в верхнем регистре", () -> {
            return given(baseRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(unauthorizedLoginResponseSpec)
                    .extract().as(LoginErrorResponseModel.class);
        });

        step("Проверка ответа (401) и сообщения о неверных данных", () ->
                assertThat(errorResponse.detail()).isEqualTo("Invalid username or password."));
    }

    @Test
    @DisplayName("Успешная авторизация с пробелами в username")
    public void loginWhitespaceTrimmingTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        step("Регистрация нового пользователя", () -> {
            given(baseRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });

        String usernameWithSpaces = "   " + testData.username + "   ";
        LoginBodyModel loginData = new LoginBodyModel(usernameWithSpaces, testData.password);

        LoginResponseModel loginResponse = step("Авторизация с пробелами в username", () -> {
            return given(baseRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(successfulLoginResponseSpec)
                    .extract().as(LoginResponseModel.class);
        });

        step("Проверка формата access и refresh токенов", () -> {
            String expectedTokenPart = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

            assertThat(loginResponse.access()).startsWith(expectedTokenPart);
            assertThat(loginResponse.refresh()).startsWith(expectedTokenPart);
        });
    }

    @Test
    @DisplayName("Ошибка авторизации при передаче пустого password")
    public void loginWithBlankPasswordTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        step("Регистрация нового пользователя", () -> {
            given(baseRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });

        LoginBodyModel loginData = new LoginBodyModel(testData.username, "");

        LoginErrorResponseModel errorResponse = step("Отправка запроса авторизации с пустым password", () -> {
            return given(baseRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(badRequestLoginResponseSpec)
                    .extract().as(LoginErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о пустом password", () ->
                assertThat(errorResponse.password()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Ошибка авторизации при вводе неверного пароля")
    public void loginWithWrongPasswordTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        step("Регистрация нового пользователя", () -> {
            given(baseRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });

        LoginBodyModel loginData = new LoginBodyModel(testData.username, "wrong_" + testData.password);

        LoginErrorResponseModel errorResponse = step("Отправка запроса авторизации с неверным паролем", () -> {
            return given(baseRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(unauthorizedLoginResponseSpec)
                    .extract().as(LoginErrorResponseModel.class);
        });

        step("Проверка ответа (401) и сообщения о неверных данных", () ->
                assertThat(errorResponse.detail()).contains("Invalid username or password."));
    }

    @Test
    @DisplayName("Ошибка авторизации для несуществующего в системе пользователя")
    public void loginWithNonExistentUserTest() {
        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

        LoginErrorResponseModel errorResponse = step("Отправка запроса авторизации для несуществующего пользователя", () -> {
            return given(baseRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(unauthorizedLoginResponseSpec)
                    .extract().as(LoginErrorResponseModel.class);
        });

        step("Проверка ответа (401) и сообщения о неверных данных", () ->
                assertThat(errorResponse.detail()).contains("Invalid username or password."));
    }

    @Test
    @DisplayName("Ошибка авторизации при отправке пустого тела запроса")
    public void loginWithEmptyBodyTest() {
        LoginErrorResponseModel errorResponse = step("Отправка запроса авторизации с пустым телом", () -> {
            return given(baseRequestSpec)
                    .body("{}")
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(badRequestLoginResponseSpec)
                    .extract().as(LoginErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщений об обязательных полях", () -> {
            assertThat(errorResponse.username()).containsExactly("This field is required.");
            assertThat(errorResponse.password()).containsExactly("This field is required.");
        });
    }

    @Test
    @DisplayName("Ошибка авторизации без Content-Type")
    public void LoginWithoutContentTypeTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        step("Регистрация нового пользователя", () -> {
            given(baseRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });

        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

        String responseBody = step("Отправка запроса авторизации без Content-Type", () -> {
            return given(withoutContentTypeRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(loginWithoutContentTypeResponseSpec)
                    .extract().path("detail");
        });

        step("Проверка ответа (415) и сообщения о неподдерживаемом типе", () ->
                assertThat(responseBody).contains("Unsupported media type \"text/plain; charset=ISO-8859-1\" in request."));
    }

    @Test
    @DisplayName("Ошибка авторизации при передаче некорректных типов данных вместо строк")
    public void loginWithInvalidDataTypesTest() {
        LoginErrorResponseModel errorResponse = step("Отправка запроса авторизации с некорректными типами данных", () -> {
            return given(baseRequestSpec)
                    .body(TestData.invalidDataTypesJson)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(badRequestLoginResponseSpec)
                    .extract().as(LoginErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщений о неверном типе данных", () -> {
            assertThat(errorResponse.username()).containsExactly("Not a valid string.");
            assertThat(errorResponse.password()).containsExactly("Not a valid string.");
        });
    }

    @Test
    @DisplayName("Ошибка авторизации при отправке синтаксически некорректного JSON")
    public void loginWithMalformedJsonTest() {
        String errorDetail = step("Отправка запроса авторизации с некорректным JSON", () -> {
            return given(baseRequestSpec)
                    .body(testData.malformedJson)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(jsonParseErrorLoginResponseSpec)
                    .extract().path("detail");
        });

        step("Проверка ответа (400) и сообщения об ошибке парсинга JSON", () ->
                assertThat(errorDetail).contains("JSON parse error"));
    }

}
